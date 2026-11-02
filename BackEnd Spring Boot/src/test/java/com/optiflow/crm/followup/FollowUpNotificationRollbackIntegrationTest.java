package com.optiflow.crm.followup;

import com.optiflow.crm.followup.model.FollowUp;
import com.optiflow.crm.followup.model.FollowUpStatus;
import com.optiflow.crm.followup.model.FollowUpType;
import com.optiflow.crm.notification.model.NotificationService;
import com.optiflow.crm.notification.model.NotificationType;
import com.optiflow.crm.order.QuoteToOrderTestDataFactory;
import com.optiflow.crm.quote.model.Quote;
import com.optiflow.crm.quote.model.QuoteStatus;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@Import(QuoteToOrderTestDataFactory.class)
@Sql(
        scripts = "/sql/cleanup-followup-test.sql",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
@Sql(
        scripts = "/sql/cleanup-followup-test.sql",
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
)
class FollowUpNotificationRollbackIntegrationTest {

    @Autowired
    private FollowUpScheduler followUpScheduler;

    @Autowired
    private FollowUpRepository followUpRepository;

    @Autowired
    private QuoteToOrderTestDataFactory testDataFactory;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockitoSpyBean
    private NotificationService notificationService;

    @Test
    void givenNotificationFailure_whenRetry_thenCreateNotification() {

        // GIVEN: seguimiento vencido y confirmado en H2
        Long followUpId = transactionTemplate.execute(tx -> {

            Quote quote = testDataFactory.createQuote(
                    QuoteStatus.SENT
            );

            FollowUp followUp = new FollowUp();
            followUp.setQuote(quote);
            followUp.setType(FollowUpType.CALL);
            followUp.setStatus(FollowUpStatus.PENDING);
            followUp.setScheduledAt(
                    LocalDateTime.now().minusHours(2)
            );
            followUp.setActive(true);
            followUp.setAutomated(false);

            return followUpRepository.saveAndFlush(followUp)
                    .getId();
        });

        assertNotNull(followUpId);

        // El primer intento falla.
        // El segundo utiliza el comportamiento real.
        doThrow(new IllegalStateException(
                "Fallo simulado de notificación"
        ))
                .doCallRealMethod()
                .when(notificationService)
                .createFollowUpNotification(
                        eq(followUpId),
                        anyLong(),
                        eq(NotificationType.FOLLOW_UP_DUE),
                        anyString(),
                        anyString()
                );

        // WHEN: primer intento
        assertThrows(
                IllegalStateException.class,
                () -> followUpScheduler.checkPendingFollowUps()
        );

        // THEN: la reclamación se ha revertido
        LocalDateTime publishedAt = jdbcTemplate.queryForObject(
                """
                SELECT due_event_published_at
                FROM follow_ups
                WHERE id = ?
                """,
                LocalDateTime.class,
                followUpId
        );

        assertNull(publishedAt);

        assertEquals(0L, countNotifications(followUpId));

        // WHEN: segundo intento
        followUpScheduler.checkPendingFollowUps();

        // THEN: se crea una notificación
        assertEquals(1L, countNotifications(followUpId));

        LocalDateTime publishedAfterRetry =
                jdbcTemplate.queryForObject(
                        """
                        SELECT due_event_published_at
                        FROM follow_ups
                        WHERE id = ?
                        """,
                        LocalDateTime.class,
                        followUpId
                );

        assertNotNull(publishedAfterRetry);

        // WHEN: tercer intento
        followUpScheduler.checkPendingFollowUps();

        // THEN: sin duplicados
        assertEquals(1L, countNotifications(followUpId));
    }

    private Long countNotifications(Long followUpId) {

        return jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM notifications
                WHERE follow_up_id = ?
                  AND type = 'FOLLOW_UP_DUE'
                """,
                Long.class,
                followUpId
        );
    }
}
