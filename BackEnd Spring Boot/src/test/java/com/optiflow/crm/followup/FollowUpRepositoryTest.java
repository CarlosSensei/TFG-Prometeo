
package com.optiflow.crm.followup;

import com.optiflow.crm.client.model.Client;
import com.optiflow.crm.followup.dto.FollowUpSearchDto;
import com.optiflow.crm.followup.model.FollowUp;
import com.optiflow.crm.followup.model.FollowUpStatus;
import com.optiflow.crm.followup.model.FollowUpType;
import com.optiflow.crm.followup.specification.FollowUpSpecification;
import com.optiflow.crm.quote.model.Quote;
import com.optiflow.crm.user.model.User;
import com.optiflow.crm.user.model.UserRole;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.sql.init.mode=never"
})
@ActiveProfiles("test")
class FollowUpRepositoryTest {

    @Autowired
    private FollowUpRepository repository;

    @Autowired
    private EntityManager entityManager;

    private Client client;
    private User seller;
    private Quote quote1;
    private Quote quote2;

    // =====================================
    // CONFIGURACIÓN
    // =====================================

    @BeforeEach
    void setUp() {

        // GIVEN - Cliente
        client = new Client();
        client.setName("Ana");
        client.setSurname("García");
        client.setDni("12345678Z");
        client.setPhoneNumber("600111222");
        client.setActive(true);

        entityManager.persist(client);

        // GIVEN - Vendedor
        seller = new User();
        seller.setFirstName("Carlos");
        seller.setLastName("Vendedor");
        seller.setEmail("seller@test.com");
        seller.setPassword("test-password");
        seller.setRole(UserRole.SELLER);
        seller.setActive(true);

        entityManager.persist(seller);

        // GIVEN - Dos presupuestos
        quote1 = createQuote("Q-2026-000001");
        quote2 = createQuote("Q-2026-000002");

        flushAndClear();
    }

    // =====================================
    // MÉTODOS AUXILIARES
    // =====================================

    private Quote createQuote(String number) {

        Quote quote = new Quote();

        quote.setQuoteNumber(number);
        quote.setClient(client);
        quote.setSeller(seller);
        quote.setQuoteDate(LocalDate.of(2026, 10, 1));

        entityManager.persist(quote);

        return quote;
    }

    private FollowUp createFollowUp(
            Quote quote,
            FollowUpType type,
            FollowUpStatus status,
            LocalDateTime scheduledAt,
            boolean automated,
            boolean active) {

        FollowUp followUp = new FollowUp();

        followUp.setQuote(quote);
        followUp.setType(type);
        followUp.setStatus(status);
        followUp.setScheduledAt(scheduledAt);
        followUp.setAutomated(automated);
        followUp.setActive(active);
        followUp.setNotes("Seguimiento de prueba");

        return repository.saveAndFlush(followUp);
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    private LocalDateTime date(int day) {
        return LocalDateTime.of(2026, 10, day, 10, 0);
    }

    private Page<FollowUp> search(FollowUpSearchDto dto) {

        return repository.findAll(
                FollowUpSpecification.filterBy(dto),
                PageRequest.of(0, 10,
                        Sort.by("scheduledAt").ascending()));
    }

    // =====================================
    // 1. SAVE AND FIND BY ID
    // =====================================

    @Test
    void givenFollowUp_whenSave_thenPersistAllFields() {

        // GIVEN
        FollowUp followUp = createFollowUp(
                quote1,
                FollowUpType.CALL,
                FollowUpStatus.PENDING,
                date(10),
                false,
                true
        );

        Long id = followUp.getId();
        Long quoteId = quote1.getId();

        flushAndClear();

        // WHEN
        FollowUp found = repository.findById(id).orElseThrow();

        // THEN
        assertAll(
                () -> assertEquals(id, found.getId()),
                () -> assertEquals(quoteId, found.getQuote().getId()),
                () -> assertEquals(FollowUpType.CALL, found.getType()),
                () -> assertEquals(FollowUpStatus.PENDING, found.getStatus()),
                () -> assertEquals(date(10), found.getScheduledAt()),
                () -> assertFalse(found.getAutomated()),
                () -> assertTrue(found.getActive()),
                () -> assertNotNull(found.getCreatedAt()),
                () -> assertNotNull(found.getUpdatedAt()));
    }

    // =====================================
    // 2. FIND ACTIVE
    // =====================================

    @Test
    void givenActiveAndInactive_whenFindActive_thenExcludeInactive() {

        // GIVEN
        createFollowUp(
                quote1, FollowUpType.CALL,
                FollowUpStatus.PENDING,
                date(10), false, true
        );

        createFollowUp(
                quote1, FollowUpType.EMAIL,
                FollowUpStatus.PENDING,
                date(11), false, false
        );

        flushAndClear();

        // WHEN
        Page<FollowUp> result = repository.findByActiveTrue(
                        PageRequest.of(0, 10));

        // THEN
        assertEquals(1, result.getTotalElements());
        assertTrue(result.getContent().getFirst().getActive());
        assertEquals(FollowUpType.CALL, result.getContent().getFirst().getType());
    }

    // =====================================
    // 3. PAGINATION
    // =====================================

    @Test
    void givenThreeFollowUps_whenFindActive_thenPaginate() {

        // GIVEN
        for (int day = 10; day <= 12; day++) {

            createFollowUp(
                    quote1,
                    FollowUpType.CALL,
                    FollowUpStatus.PENDING,
                    date(day),
                    false,
                    true
            );
        }

        flushAndClear();

        // WHEN
        Page<FollowUp> result = repository.findByActiveTrue(
                        PageRequest.of(0, 2,
                                Sort.by("scheduledAt").ascending()));

        // THEN
        assertEquals(3, result.getTotalElements());
        assertEquals(2, result.getContent().size());
        assertEquals(2, result.getTotalPages());
        assertEquals(0, result.getNumber());
    }

    // =====================================
    // 4. FIND BY QUOTE
    // =====================================

    @Test
    void givenDifferentQuotes_whenFindByQuote_thenFilterActive() {

        // GIVEN
        createFollowUp(
                quote1, FollowUpType.CALL,
                FollowUpStatus.PENDING,
                date(10), false, true
        );

        createFollowUp(
                quote1, FollowUpType.EMAIL,
                FollowUpStatus.PENDING,
                date(11), false, false
        );

        createFollowUp(
                quote2, FollowUpType.CALL,
                FollowUpStatus.PENDING,
                date(12), false, true
        );

        Long quoteId = quote1.getId();

        flushAndClear();

        // WHEN
        Page<FollowUp> result = repository.findByQuoteIdAndActiveTrue(
                        quoteId, PageRequest.of(0, 10));

        // THEN
        assertEquals(1, result.getTotalElements());
        assertEquals(quoteId, result.getContent().getFirst().getQuote().getId());
        assertTrue(result.getContent().getFirst().getActive());
    }

    // =====================================
    // 5. PENDING AND DUE
    // =====================================

    @Test
    void givenPendingDueFollowUps_whenFindDue_thenReturnMatching() {

        // GIVEN
        createFollowUp(
                quote1, FollowUpType.CALL,
                FollowUpStatus.PENDING,
                date(10), false, true
        );

        createFollowUp(
                quote1, FollowUpType.EMAIL,
                FollowUpStatus.PENDING,
                date(15), true, true
        );

        createFollowUp(
                quote1, FollowUpType.CALL,
                FollowUpStatus.COMPLETED,
                date(9), false, true
        );

        createFollowUp(
                quote2, FollowUpType.CALL,
                FollowUpStatus.PENDING,
                date(8), false, false
        );

        flushAndClear();

        // WHEN
        List<FollowUp> result = repository
                        .findByStatusAndScheduledAtLessThanEqualAndActiveTrue(
                                FollowUpStatus.PENDING, date(12));

        // THEN
        assertEquals(1, result.size());
        assertEquals(date(10), result.getFirst().getScheduledAt());
        assertEquals(FollowUpStatus.PENDING, result.getFirst().getStatus());
    }

    // =====================================
    // 6. FUTURE FOLLOWUPS
    // =====================================

    @Test
    void givenFutureFollowUp_whenFindDue_thenExcludeIt() {

        // GIVEN
        createFollowUp(
                quote1,
                FollowUpType.CALL,
                FollowUpStatus.PENDING,
                date(20),
                false,
                true
        );

        flushAndClear();

        // WHEN
        List<FollowUp> result = repository
                        .findByStatusAndScheduledAtLessThanEqualAndActiveTrue(
                                FollowUpStatus.PENDING, date(15));

        // THEN
        assertTrue(result.isEmpty());
    }

    // =====================================
    // 7. SEARCH BY TYPE AND STATUS
    // =====================================

    @Test
    void givenTypeAndStatus_whenSearch_thenCombineFilters() {

        // GIVEN
        createFollowUp(
                quote1, FollowUpType.CALL,
                FollowUpStatus.PENDING,
                date(10), false, true
        );

        createFollowUp(
                quote1, FollowUpType.EMAIL,
                FollowUpStatus.PENDING,
                date(11), false, true
        );

        createFollowUp(
                quote2, FollowUpType.CALL,
                FollowUpStatus.COMPLETED,
                date(12), false, true
        );

        flushAndClear();

        FollowUpSearchDto dto = new FollowUpSearchDto();

        dto.setType(FollowUpType.CALL);
        dto.setStatus(FollowUpStatus.PENDING);

        // WHEN
        Page<FollowUp> result = search(dto);

        // THEN
        assertEquals(1, result.getTotalElements());
        assertEquals(FollowUpType.CALL, result.getContent().getFirst().getType());
        assertEquals(FollowUpStatus.PENDING, result.getContent().getFirst().getStatus());
    }

    // =====================================
    // 8. SEARCH BY SCHEDULED DATE RANGE
    // =====================================

    @Test
    void givenScheduledRange_whenSearch_thenFilterDates() {

        // GIVEN
        createFollowUp(
                quote1, FollowUpType.CALL,
                FollowUpStatus.PENDING,
                date(5), false, true
        );

        createFollowUp(
                quote1, FollowUpType.CALL,
                FollowUpStatus.PENDING,
                date(15), false, true
        );

        createFollowUp(
                quote1, FollowUpType.CALL,
                FollowUpStatus.PENDING,
                date(25), false, true
        );

        flushAndClear();

        FollowUpSearchDto dto = new FollowUpSearchDto();

        dto.setScheduledFrom(date(10));
        dto.setScheduledTo(date(20));

        // WHEN
        Page<FollowUp> result = search(dto);

        // THEN
        assertEquals(1, result.getTotalElements());
        assertEquals(date(15), result.getContent().getFirst().getScheduledAt());
    }

    // =====================================
    // 9. SEARCH AUTOMATED
    // =====================================

    @Test
    void givenAutomatedTrue_whenSearch_thenReturnAutomated() {

        // GIVEN
        createFollowUp(
                quote1, FollowUpType.EMAIL,
                FollowUpStatus.PENDING,
                date(10), true, true
        );

        createFollowUp(
                quote1, FollowUpType.CALL,
                FollowUpStatus.PENDING,
                date(11), false, true
        );

        flushAndClear();

        FollowUpSearchDto dto = new FollowUpSearchDto();

        dto.setAutomated(true);

        // WHEN
        Page<FollowUp> result = search(dto);

        // THEN
        assertEquals(1, result.getTotalElements());
        assertTrue(result.getContent().getFirst().getAutomated());
    }

    // =====================================
    // 10. SEARCH COMPLETED BY DATE
    // =====================================

    @Test
    void givenCompletedRange_whenSearch_thenFilterCompletedAt() {

        // GIVEN
        FollowUp first = createFollowUp(
                quote1, FollowUpType.CALL,
                FollowUpStatus.COMPLETED,
                date(5), false, true
        );

        first.setCompletedAt(date(6));
        repository.saveAndFlush(first);

        FollowUp second = createFollowUp(
                quote1, FollowUpType.EMAIL,
                FollowUpStatus.COMPLETED,
                date(14), false, true
        );

        second.setCompletedAt(date(15));
        repository.saveAndFlush(second);

        flushAndClear();

        FollowUpSearchDto dto = new FollowUpSearchDto();

        dto.setStatus(FollowUpStatus.COMPLETED);
        dto.setCompletedFrom(date(10));
        dto.setCompletedTo(date(20));

        // WHEN
        Page<FollowUp> result = search(dto);

        // THEN
        assertEquals(1, result.getTotalElements());
        assertEquals(date(15), result.getContent().getFirst().getCompletedAt());
    }

    // =====================================
    // 11. UPDATE FOLLOWUP
    // =====================================

    @Test
    void givenFollowUp_whenUpdate_thenPersistChanges() {

        // GIVEN
        FollowUp followUp = createFollowUp(
                quote1,
                FollowUpType.CALL,
                FollowUpStatus.PENDING,
                date(10),
                false,
                true
        );

        Long id = followUp.getId();

        // WHEN
        followUp.setStatus(FollowUpStatus.COMPLETED);
        followUp.setResult("Cliente interesado");
        followUp.setCompletedAt(date(11));

        repository.saveAndFlush(followUp);

        flushAndClear();

        // THEN
        FollowUp updated = repository.findById(id).orElseThrow();

        assertAll(
                () -> assertEquals(FollowUpStatus.COMPLETED, updated.getStatus()),
                () -> assertEquals("Cliente interesado", updated.getResult()),
                () -> assertEquals(date(11), updated.getCompletedAt()),
                () -> assertNotNull(updated.getUpdatedAt()));
    }

    // =====================================
    // 12. LOGICAL DELETE
    // =====================================

    @Test
    void givenFollowUp_whenDeactivate_thenRemainInDatabase() {

        // GIVEN
        FollowUp followUp = createFollowUp(
                quote1,
                FollowUpType.CALL,
                FollowUpStatus.PENDING,
                date(10),
                false,
                true
        );

        Long id = followUp.getId();

        // WHEN
        followUp.setActive(false);

        repository.saveAndFlush(followUp);

        flushAndClear();

        // THEN
        FollowUp found = repository.findById(id).orElseThrow();

        assertFalse(found.getActive());

        Page<FollowUp> active = repository.findByActiveTrue(PageRequest
                .of(0, 10));

        assertEquals(0, active.getTotalElements());
    }

    // =====================================
    // 13. CLAIM EVENT RETURN 1
    // =====================================

    @Test
    void givenDueFollowUp_whenClaimEvent_thenReturnOneAndSaveDate() {

        // GIVEN
        LocalDateTime scheduledAt = date(10);
        LocalDateTime publishedAt = date(11);

        FollowUp followUp = createFollowUp(
                quote1,
                FollowUpType.CALL,
                FollowUpStatus.PENDING,
                scheduledAt,
                false,
                true
        );

        Long id = followUp.getId();

        flushAndClear();

        // WHEN
        int claimed = repository.claimDueEvent(
                id,
                FollowUpStatus.PENDING,
                scheduledAt,
                publishedAt
        );

        // THEN
        assertEquals(1, claimed);

        FollowUp updated = repository.findById(id).orElseThrow();

        assertEquals(publishedAt, updated.getDueEventPublishedAt());
    }

    // =====================================
    // 14. ALREADY CLAIMED RETURN 0
    // =====================================

    @Test
    void givenAlreadyClaimedFollowUp_whenClaimAgain_thenReturnZero() {

        // GIVEN
        LocalDateTime scheduledAt = date(10);

        FollowUp followUp = createFollowUp(
                quote1,
                FollowUpType.CALL,
                FollowUpStatus.PENDING,
                scheduledAt,
                false,
                true
        );

        Long id = followUp.getId();

        flushAndClear();

        // WHEN
        int first = repository.claimDueEvent(
                id, FollowUpStatus.PENDING, scheduledAt, date(11));

        int second = repository.claimDueEvent(
                id, FollowUpStatus.PENDING, scheduledAt, date(12));

        // THEN
        assertEquals(1, first);
        assertEquals(0, second);

        FollowUp updated = repository.findById(id).orElseThrow();

        assertEquals(date(11), updated.getDueEventPublishedAt());
    }

    // =====================================
    // 14. AVOID RESCHEDULED OLD DATE
    // =====================================

    @Test
    void givenRescheduledFollowUp_whenClaimOldDate_thenReturnZero() {

        // GIVEN
        LocalDateTime oldDate = date(10);
        LocalDateTime newDate = date(20);

        FollowUp followUp = createFollowUp(
                quote1,
                FollowUpType.CALL,
                FollowUpStatus.PENDING,
                oldDate,
                false,
                true
        );

        Long id = followUp.getId();

        // Simulamos una reprogramación.
        followUp.setScheduledAt(newDate);
        repository.saveAndFlush(followUp);

        flushAndClear();

        // WHEN
        int claimed = repository.claimDueEvent(
                id,
                FollowUpStatus.PENDING,
                oldDate,
                date(15)
        );

        // THEN
        assertEquals(0, claimed);

        FollowUp updated = repository.findById(id).orElseThrow();

        assertNull(updated.getDueEventPublishedAt());
        assertEquals(newDate, updated.getScheduledAt());
    }
}
