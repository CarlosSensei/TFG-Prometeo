package com.optiflow.crm.dashboard;

import com.optiflow.crm.dashboard.dto.DashboardDto;
import com.optiflow.crm.dashboard.dto.DashboardCalendarDto;
import com.optiflow.crm.followup.model.FollowUp;
import com.optiflow.crm.order.model.Order;
import com.optiflow.crm.quote.model.Quote;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class DashboardService {
    private final EntityManager em;

    public DashboardService(EntityManager em) { this.em = em; }

    @Transactional(readOnly = true)
    public DashboardDto getSummary() {
        LocalDateTime now = LocalDateTime.now();
        long clients = count("select count(c) from Client c where c.active = true");
        long quotes = count("""
                select count(q) from Quote q where q.active = true
                and q.status in (com.optiflow.crm.quote.model.QuoteStatus.DRAFT, com.optiflow.crm.quote.model.QuoteStatus.SENT, com.optiflow.crm.quote.model.QuoteStatus.ACCEPTED)
                """);
        long orders = count("""
                select count(o) from Order o where o.active = true
                and o.status in (com.optiflow.crm.order.model.OrderStatus.PENDING,com.optiflow.crm.order.model.OrderStatus.CONFIRMED,com.optiflow.crm.order.model.OrderStatus.IN_PROGRESS,com.optiflow.crm.order.model.OrderStatus.READY)
                """);
        long pending = count("""
                select count(f) from FollowUp f where f.active = true
                and f.status = com.optiflow.crm.followup.model.FollowUpStatus.PENDING
                """);
        long overdue = em.createQuery("""
                select count(f) from FollowUp f where f.active = true
                and f.status = com.optiflow.crm.followup.model.FollowUpStatus.PENDING and f.scheduledAt < :now
                """, Long.class).setParameter("now", now).getSingleResult();
        long converted = count("""
                select count(q) from Quote q where q.active = true
                and q.status = com.optiflow.crm.quote.model.QuoteStatus.CONVERTED
                """);
        long decided = count("""
                select count(q) from Quote q where q.active = true
                and q.status in (com.optiflow.crm.quote.model.QuoteStatus.ACCEPTED,com.optiflow.crm.quote.model.QuoteStatus.REJECTED,com.optiflow.crm.quote.model.QuoteStatus.CONVERTED)
                """);
        BigDecimal revenue = em.createQuery("""
                select sum(o.total) from Order o where o.active = true
                and o.status = com.optiflow.crm.order.model.OrderStatus.DELIVERED
                """, BigDecimal.class).getSingleResult();
        if (revenue == null) revenue = BigDecimal.ZERO;

        List<DashboardDto.ActivityDto> activity = new ArrayList<>();
        List<Quote> latestQuotes = em.createQuery("""
                select q from Quote q where q.active = true
                order by q.createdAt desc, q.id desc
                """, Quote.class).setMaxResults(5).getResultList();
        for (Quote q : latestQuotes) {
            activity.add(new DashboardDto.ActivityDto("QUOTE", q.getId(),
                    q.getQuoteNumber(), q.getStatus().name(), q.getCreatedAt()));
        }
        List<Order> latestOrders = em.createQuery("""
                select o from Order o where o.active = true
                order by o.createdAt desc, o.id desc
                """, Order.class).setMaxResults(5).getResultList();
        for (Order o : latestOrders) {
            activity.add(new DashboardDto.ActivityDto("ORDER", o.getId(),
                    o.getOrderNumber(), o.getStatus().name(), o.getCreatedAt()));
        }
        activity.sort(Comparator.comparing(DashboardDto.ActivityDto::occurredAt,
                Comparator.nullsLast(Comparator.reverseOrder())));
        if (activity.size() > 6) activity = new ArrayList<>(activity.subList(0, 6));

        List<FollowUp> next = em.createQuery("""
                select f from FollowUp f join fetch f.quote q
                where f.active = true and f.status = com.optiflow.crm.followup.model.FollowUpStatus.PENDING
                order by f.scheduledAt asc, f.id asc
                """, FollowUp.class).setMaxResults(6).getResultList();
        List<DashboardDto.FollowUpSummaryDto> followUps = next.stream()
                .map(f -> new DashboardDto.FollowUpSummaryDto(
                        f.getId(), f.getQuote().getId(), f.getQuote().getQuoteNumber(),
                        f.getType().name(), f.getScheduledAt(), f.getScheduledAt().isBefore(now)))
                .toList();

        return new DashboardDto(clients, quotes, orders, pending, overdue,
                converted, decided, conversionRate(converted, decided),
                revenue, activity, followUps);
    }


    /** Seguimientos pendientes del mes y próximas acciones, sin el límite de seis del resumen. */
    @Transactional(readOnly = true)
    public DashboardCalendarDto getCalendar(YearMonth month) {
        LocalDateTime start = month.atDay(1).atStartOfDay();
        LocalDateTime end = month.plusMonths(1).atDay(1).atStartOfDay();
        LocalDateTime now = LocalDateTime.now();

        List<FollowUp> inMonth = em.createQuery("""
                select f from FollowUp f join fetch f.quote q
                where f.active = true
                  and f.status = com.optiflow.crm.followup.model.FollowUpStatus.PENDING
                  and f.scheduledAt >= :start and f.scheduledAt < :end
                order by f.scheduledAt asc, f.id asc
                """, FollowUp.class)
                .setParameter("start", start)
                .setParameter("end", end)
                .getResultList();

        // Incluye las vencidas primero: también son acciones por resolver.
        List<FollowUp> next = em.createQuery("""
                select f from FollowUp f join fetch f.quote q
                where f.active = true
                  and f.status = com.optiflow.crm.followup.model.FollowUpStatus.PENDING
                order by f.scheduledAt asc, f.id asc
                """, FollowUp.class)
                .setMaxResults(8)
                .getResultList();

        return new DashboardCalendarDto(month.getYear(), month.getMonthValue(),
                inMonth.stream().map(f -> toCalendarAction(f, now)).toList(),
                next.stream().map(f -> toCalendarAction(f, now)).toList());
    }

    private DashboardCalendarDto.CalendarActionDto toCalendarAction(FollowUp f, LocalDateTime now) {
        return new DashboardCalendarDto.CalendarActionDto(
                f.getId(), f.getQuote().getId(), f.getQuote().getQuoteNumber(),
                f.getType().name(), f.getScheduledAt(), f.getScheduledAt().isBefore(now));
    }

    private long count(String jpql) {
        return em.createQuery(jpql, Long.class).getSingleResult();
    }

    public static BigDecimal conversionRate(long converted, long decided) {
        if (decided == 0) return BigDecimal.ZERO.setScale(1);
        return BigDecimal.valueOf(converted).multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(decided), 1, RoundingMode.HALF_UP);
    }
}
