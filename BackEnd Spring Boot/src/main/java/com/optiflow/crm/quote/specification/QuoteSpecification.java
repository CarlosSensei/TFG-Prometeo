package com.optiflow.crm.quote.specification;

import com.optiflow.crm.quote.dto.QuoteSearchDto;
import com.optiflow.crm.quote.model.Quote;
import com.optiflow.crm.quote.model.QuoteStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class QuoteSpecification {

    private QuoteSpecification() {
    }

    public static Specification<Quote> filterBy(QuoteSearchDto searchDto) {

        return Specification.allOf(
                quoteNumberContains(searchDto.getQuoteNumber()),
                clientIdEquals(searchDto.getClientId()),
                statusEquals(searchDto.getStatus()),
                quoteDateFrom(searchDto.getQuoteDateFrom()),
                quoteDateTo(searchDto.getQuoteDateTo()),
                expirationDateFrom(searchDto.getExpirationDateFrom()),
                expirationDateTo(searchDto.getExpirationDateTo()),
                activeEquals(searchDto.getActive())
        );
    }

    private static Specification<Quote> quoteNumberContains(String quoteNumber) {

        return (root, query, criteriaBuilder) -> {

            if (quoteNumber == null || quoteNumber.isBlank()) { return null; }

            return criteriaBuilder.like(criteriaBuilder.lower(root.get("quoteNumber")),
                    "%" + quoteNumber.toLowerCase() + "%");
        };
    }

    private static Specification<Quote> clientIdEquals(Long clientId) {

        return (root, query, criteriaBuilder) -> {

            if (clientId == null) { return null; }

            return criteriaBuilder.equal(root.get("client").get("id"), clientId);
        };
    }

    private static Specification<Quote> statusEquals(QuoteStatus status) {

        return (root, query, criteriaBuilder) -> {

            if (status == null) { return null; }

            return criteriaBuilder.equal(root.get("status"), status);
        };
    }

    private static Specification<Quote> quoteDateFrom(LocalDate quoteDateFrom) {

        return (root, query, criteriaBuilder) -> {

            if (quoteDateFrom == null) { return null; }

            return criteriaBuilder.greaterThanOrEqualTo(root.get("quoteDate"), quoteDateFrom);
        };
    }

    private static Specification<Quote> quoteDateTo(LocalDate quoteDateTo) {

        return (root, query, criteriaBuilder) -> {

            if (quoteDateTo == null) { return null; }

            return criteriaBuilder.lessThanOrEqualTo(root.get("quoteDate"), quoteDateTo);
        };
    }

    private static Specification<Quote> expirationDateFrom(LocalDate expirationDateFrom) {

        return (root, query, criteriaBuilder) -> {

            if (expirationDateFrom == null) { return null; }

            return criteriaBuilder.greaterThanOrEqualTo(root.get("expirationDate"), expirationDateFrom);
        };
    }

    private static Specification<Quote> expirationDateTo(LocalDate expirationDateTo) {

        return (root, query, criteriaBuilder) -> {

            if (expirationDateTo == null) { return null; }

            return criteriaBuilder.lessThanOrEqualTo(root.get("expirationDate"), expirationDateTo);
        };
    }

    private static Specification<Quote> activeEquals(Boolean active) {

        return (root, query, criteriaBuilder) -> {

            if (active == null) { return null; }

            return criteriaBuilder.equal(root.get("active"), active);
        };
    }
}