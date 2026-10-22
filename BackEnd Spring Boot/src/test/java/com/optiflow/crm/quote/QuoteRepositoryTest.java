package com.optiflow.crm.quote;

import com.optiflow.crm.client.model.Client;
import com.optiflow.crm.quote.dto.QuoteSearchDto;
import com.optiflow.crm.quote.model.Quote;
import com.optiflow.crm.quote.model.QuoteStatus;
import com.optiflow.crm.quote.specification.QuoteSpecification;
import com.optiflow.crm.user.model.User;
import com.optiflow.crm.user.model.UserRole;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.sql.init.mode=never"
})
@ActiveProfiles("test")
class QuoteRepositoryTest {

    @Autowired
    private QuoteRepository quoteRepository;

    @Autowired
    private EntityManager entityManager;

    private Client client1;
    private Client client2;
    private User seller;

    @BeforeEach
    void setUp() {

        client1 = createClient(
                "Ana", "García",
                "12345678Z", "600111222"
        );

        client2 = createClient(
                "Luis", "Martín",
                "87654321X", "600333444"
        );

        seller = new User();
        seller.setFirstName("Carlos");
        seller.setLastName("Vendedor");
        seller.setEmail("seller@test.com");
        seller.setPassword("test-password");
        seller.setRole(UserRole.SELLER);

        entityManager.persist(seller);
        entityManager.flush();
    }

    // =====================================
    // MÉTODOS AUXILIARES
    // =====================================

    private Client createClient(
            String name,
            String surname,
            String dni,
            String phone) {

        Client client = new Client();

        client.setName(name);
        client.setSurname(surname);
        client.setDni(dni);
        client.setPhoneNumber(phone);
        client.setBirthDate(LocalDate.of(1985, 5, 20));

        entityManager.persist(client);

        return client;
    }

    private Quote createQuote(
            String number,
            Client client,
            QuoteStatus status,
            boolean active,
            LocalDate date) {

        Quote quote = new Quote();

        quote.setQuoteNumber(number);
        quote.setClient(client);
        quote.setSeller(seller);
        quote.setStatus(status);
        quote.setActive(active);
        quote.setQuoteDate(date);
        quote.setExpirationDate(date.plusDays(30));

        entityManager.persist(quote);

        return quote;
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    // =====================================
    // SAVE AND FIND BY ID
    // =====================================

    @Test
    void givenQuote_whenSave_thenPersistAndFindById() {

        // GIVEN
        Quote quote = createQuote(
                "Q-2026-000001",
                client1,
                QuoteStatus.DRAFT,
                true,
                LocalDate.of(2026, 10, 1)
        );

        Long id = quote.getId();

        flushAndClear();

        // WHEN
        Optional<Quote> result = quoteRepository.findById(id);

        // THEN
        assertTrue(result.isPresent());

        Quote found = result.get();

        assertEquals("Q-2026-000001", found.getQuoteNumber());
        assertEquals(QuoteStatus.DRAFT, found.getStatus());
        assertEquals(client1.getId(), found.getClient().getId());

        assertNotNull(found.getCreatedAt());
        assertNotNull(found.getUpdatedAt());
    }

    // =====================================
    // FIND ACTIVE
    // =====================================

    @Test
    void givenActiveAndInactiveQuotes_whenFindActive_thenExcludeInactive() {

        // GIVEN
        createQuote(
                "Q-2026-000002", client1,
                QuoteStatus.DRAFT, true,
                LocalDate.of(2026, 10, 2)
        );

        createQuote(
                "Q-2026-000003", client1,
                QuoteStatus.DRAFT, false,
                LocalDate.of(2026, 10, 3)
        );

        flushAndClear();

        // WHEN
        Page<Quote> result = quoteRepository.findByActiveTrue(
                PageRequest.of(0, 10));

        // THEN
        assertEquals(1, result.getTotalElements());
        assertTrue(result.getContent().stream().allMatch(q ->
                                Boolean.TRUE.equals(q.getActive())));
    }

    // =====================================
    // PAGINATION
    // =====================================

    @Test
    void givenThreeQuotes_whenFindActive_thenPaginate() {

        // GIVEN
        createQuote(
                "Q-2026-000004", client1,
                QuoteStatus.DRAFT, true,
                LocalDate.of(2026, 10, 4)
        );

        createQuote(
                "Q-2026-000005", client1,
                QuoteStatus.DRAFT, true,
                LocalDate.of(2026, 10, 5)
        );

        createQuote(
                "Q-2026-000006", client1,
                QuoteStatus.DRAFT, true,
                LocalDate.of(2026, 10, 6)
        );

        flushAndClear();

        // WHEN
        Pageable pageable = PageRequest.of(0, 2);

        Page<Quote> result =
                quoteRepository.findByActiveTrue(pageable);

        // THEN
        assertEquals(3, result.getTotalElements());
        assertEquals(2, result.getContent().size());
        assertEquals(2, result.getTotalPages());
        assertEquals(0, result.getNumber());
    }

    // =====================================
    // FIND BY CLIENT
    // =====================================

    @Test
    void givenDifferentClients_whenFindByClient_thenOnlyItsActiveQuotes() {

        // GIVEN
        createQuote(
                "Q-2026-000007", client1,
                QuoteStatus.DRAFT, true,
                LocalDate.of(2026, 10, 7)
        );

        createQuote(
                "Q-2026-000008", client1,
                QuoteStatus.DRAFT, false,
                LocalDate.of(2026, 10, 8)
        );

        createQuote(
                "Q-2026-000009", client2,
                QuoteStatus.DRAFT, true,
                LocalDate.of(2026, 10, 9)
        );

        flushAndClear();

        // WHEN
        Page<Quote> result = quoteRepository.findByClientIdAndActiveTrue(
                        client1.getId(), PageRequest.of(0, 10));

        // THEN
        assertEquals(1, result.getTotalElements());
        assertEquals("Q-2026-000007", result.getContent().getFirst().getQuoteNumber());
    }

    // =====================================
    // COUNT BY CLIENT
    // =====================================

    @Test
    void givenQuotes_whenCountByClient_thenCountOnlyActive() {

        // GIVEN
        createQuote(
                "Q-2026-000010", client1,
                QuoteStatus.DRAFT, true,
                LocalDate.of(2026, 10, 10)
        );

        createQuote(
                "Q-2026-000011", client1,
                QuoteStatus.SENT, true,
                LocalDate.of(2026, 10, 11)
        );

        createQuote(
                "Q-2026-000012", client1,
                QuoteStatus.DRAFT, false,
                LocalDate.of(2026, 10, 12)
        );

        createQuote(
                "Q-2026-000013", client2,
                QuoteStatus.DRAFT, true,
                LocalDate.of(2026, 10, 13)
        );

        flushAndClear();

        // WHEN
        long count = quoteRepository.countByClientIdAndActiveTrue(client1.getId());

        // THEN
        assertEquals(2L, count);
    }

    // =====================================
    // LAST QUOTE NUMBER
    // =====================================

    @Test
    void givenSeveralQuoteNumbers_whenFindLatest_thenReturnHighestNumber() {

        // GIVEN
        createQuote(
                "Q-2026-000001", client1,
                QuoteStatus.DRAFT, true,
                LocalDate.of(2026, 10, 1)
        );

        createQuote(
                "Q-2026-000015", client1,
                QuoteStatus.SENT, true,
                LocalDate.of(2026, 10, 15)
        );

        createQuote(
                "Q-2026-000009", client2,
                QuoteStatus.DRAFT, true,
                LocalDate.of(2026, 10, 9)
        );

        createQuote(
                "Q-2025-000099", client2,
                QuoteStatus.DRAFT, true,
                LocalDate.of(2025, 12, 1)
        );

        flushAndClear();

        // WHEN
        Optional<Quote> result = quoteRepository
                        .findTopByQuoteNumberStartingWithOrderByQuoteNumberDesc("Q-2026-");

        // THEN
        assertTrue(result.isPresent());
        assertEquals("Q-2026-000015", result.get().getQuoteNumber());
    }

    // =====================================
    // SEARCH BY STATUS AND ACTIVE
    // =====================================

    @Test
    void givenSearchFilters_whenFindAll_thenReturnMatchingQuotes() {

        // GIVEN
        createQuote(
                "Q-2026-000016", client1,
                QuoteStatus.DRAFT, true,
                LocalDate.of(2026, 10, 16)
        );

        createQuote(
                "Q-2026-000017", client1,
                QuoteStatus.SENT, true,
                LocalDate.of(2026, 10, 17)
        );

        createQuote(
                "Q-2026-000018", client2,
                QuoteStatus.DRAFT, false,
                LocalDate.of(2026, 10, 18)
        );

        flushAndClear();

        QuoteSearchDto search = new QuoteSearchDto();

        search.setStatus(QuoteStatus.DRAFT);
        search.setActive(true);

        // WHEN
        Page<Quote> result = quoteRepository.findAll(
                QuoteSpecification.filterBy(search),
                PageRequest.of(0, 10)
        );

        // THEN
        assertEquals(1, result.getTotalElements());
        assertEquals("Q-2026-000016", result.getContent().getFirst().getQuoteNumber());
    }

    // =====================================
    // SEARCH BY CLIENT AND DATES
    // =====================================

    @Test
    void givenClientAndDateRange_whenSearch_thenFilterCorrectly() {

        // GIVEN
        createQuote(
                "Q-2026-000019", client1,
                QuoteStatus.DRAFT, true,
                LocalDate.of(2026, 9, 15)
        );

        createQuote(
                "Q-2026-000020", client1,
                QuoteStatus.DRAFT, true,
                LocalDate.of(2026, 10, 15)
        );

        createQuote(
                "Q-2026-000021", client2,
                QuoteStatus.DRAFT, true,
                LocalDate.of(2026, 10, 15)
        );

        flushAndClear();

        QuoteSearchDto search = new QuoteSearchDto();

        search.setClientId(client1.getId());
        search.setQuoteDateFrom(LocalDate.of(2026, 10, 1));
        search.setQuoteDateTo(LocalDate.of(2026, 10, 31));

        // WHEN
        Page<Quote> result = quoteRepository.findAll(
                QuoteSpecification.filterBy(search),
                PageRequest.of(0, 10)
        );

        // THEN
        assertEquals(1, result.getTotalElements());
        assertEquals("Q-2026-000020", result.getContent().getFirst().getQuoteNumber());
    }
}
