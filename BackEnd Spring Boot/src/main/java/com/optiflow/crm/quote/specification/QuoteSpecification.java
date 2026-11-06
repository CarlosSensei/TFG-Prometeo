package com.optiflow.crm.quote.specification;

import com.optiflow.crm.quote.dto.QuoteSearchDto;
import com.optiflow.crm.quote.model.Quote;
import com.optiflow.crm.quote.model.QuoteStatus;
import org.springframework.data.jpa.domain.Specification;

import java.util.Set;

import static com.optiflow.crm.shared.specification.SpecificationUtils.*;

/**
 * Búsqueda avanzada de presupuestos.
 * Combina todos los filtros informados mediante AND.
 */
public final class QuoteSpecification {

    private QuoteSpecification() {
    }

    public static Specification<Quote> filterBy(QuoteSearchDto dto) {

        return Specification.allOf(

                // Número de presupuesto
                containsIgnoreCase("quoteNumber", dto.getQuoteNumber()),
                // Cliente
                relatedIdEquals("client", dto.getClientId()),
                // Vendedor por ID
                relatedIdEquals("seller", dto.getSellerId()),
                // Vendedor por nombre o apellido
                relatedFullNameContains("seller",
                        "firstName", "lastName", dto.getSellerName()),
                // Estado comercial
                equal("status", dto.getStatus()),
                statusesIn(dto.getStatuses()),
                greaterThanOrEqual("total", dto.getMinTotal()),
                lessThanOrEqual("total", dto.getMaxTotal()),
                // Fecha de presupuesto
                greaterThanOrEqual("quoteDate", dto.getQuoteDateFrom()),
                lessThanOrEqual("quoteDate", dto.getQuoteDateTo()),
                // Fecha de caducidad
                greaterThanOrEqual("expirationDate", dto.getExpirationDateFrom()),
                lessThanOrEqual("expirationDate", dto.getExpirationDateTo()),
                // Baja lógica
                equal("active", dto.getActive())
        );
    }


    private static Specification<Quote> statusesIn(Set<QuoteStatus> statuses) {
        return (root, query, cb) -> {

            if (statuses == null || statuses.isEmpty()) {
                return null;
            }

            return root.get("status").in(statuses);
        };
    }

}

