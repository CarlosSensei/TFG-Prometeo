package com.optiflow.crm.quote.specification;
import com.optiflow.crm.quote.dto.QuoteSearchDto;
import com.optiflow.crm.quote.model.Quote;

import org.springframework.data.jpa.domain.Specification;

import static com.optiflow.crm.shared.specification.SpecificationUtils.*;

/**
 * Filtros de búsqueda avanzada de presupuestos.
 * Combina las condiciones mediante AND.
 */
public final class QuoteSpecification {

    private QuoteSpecification() {
    }

    public static Specification<Quote> filterBy(QuoteSearchDto dto) {

        return Specification.allOf(

                // Número de presupuesto
                containsIgnoreCase("quoteNumber", dto.getQuoteNumber()),
                // Cliente asociado
                relatedIdEquals("client", dto.getClientId()),
                // Estado del presupuesto
                equal("status", dto.getStatus()),
                // Fechas del presupuesto
                greaterThanOrEqual("quoteDate", dto.getQuoteDateFrom()),
                lessThanOrEqual("quoteDate", dto.getQuoteDateTo()),
                // Fechas de caducidad
                greaterThanOrEqual("expirationDate", dto.getExpirationDateFrom()),
                lessThanOrEqual("expirationDate", dto.getExpirationDateTo()),
                // Baja lógica
                equal("active", dto.getActive())
        );
    }
}
