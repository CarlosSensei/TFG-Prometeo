package com.optiflow.crm.search.specification;

import com.optiflow.crm.client.model.Client;
import com.optiflow.crm.quote.model.Quote;
import com.optiflow.crm.order.model.Order;

import org.springframework.data.jpa.domain.Specification;
import static com.optiflow.crm.shared.specification.SpecificationUtils.*;

public final class GlobalSearchSpecification {

    private GlobalSearchSpecification() {
    }

    public static Specification<Client> clientsMatching(String term) {

        return Specification.allOf(
                equal("active", true),
                Specification.anyOf(
                        containsIgnoreCase("name", term),
                        containsIgnoreCase("surname", term),
                        containsIgnoreCase("lastSurname", term),
                        containsIgnoreCase("dni", term),
                        containsIgnoreCase("phoneNumber", term),
                        containsIgnoreCase("email", term),
                        containsIgnoreCase("city", term)
                )
        );
    }

    public static Specification<Quote> quotesMatching(String term) {

        return Specification.allOf(
                equal("active", true),
                Specification.anyOf(
                        containsIgnoreCase("quoteNumber", term),
                        relatedFullNameContains(
                                "seller",
                                "firstName",
                                "lastName",
                                term
                        )
                )
        );
    }

    public static Specification<Order> ordersMatching(String term) {

        return Specification.allOf(
                equal("active", true),
                Specification.anyOf(
                        containsIgnoreCase("orderNumber", term),
                        relatedFullNameContains(
                                "seller",
                                "firstName",
                                "lastName",
                                term
                        )
                )
        );
    }
}
