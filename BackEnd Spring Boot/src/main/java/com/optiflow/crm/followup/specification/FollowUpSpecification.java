package com.optiflow.crm.followup.specification;

import com.optiflow.crm.followup.dto.FollowUpSearchDto;
import com.optiflow.crm.followup.model.FollowUp;
import org.springframework.data.jpa.domain.Specification;

import static com.optiflow.crm.shared.specification.SpecificationUtils.*;

/**
 * Filtros de búsqueda avanzada de seguimientos.
 */
public final class FollowUpSpecification {

    private FollowUpSpecification() {
    }

    public static Specification<FollowUp> filterBy(FollowUpSearchDto dto) {

        return Specification.allOf(

                // Presupuesto asociado
                relatedIdEquals("quote", dto.getQuoteId()),
                // Tipo de seguimiento
                equal("type", dto.getType()),
                // Estado
                equal("status", dto.getStatus()),
                // Fecha programada
                greaterThanOrEqual("scheduledAt", dto.getScheduledFrom()),
                lessThanOrEqual("scheduledAt", dto.getScheduledTo()),
                // Fecha de finalización
                greaterThanOrEqual("completedAt", dto.getCompletedFrom()),
                lessThanOrEqual("completedAt", dto.getCompletedTo()),
                // Automatización
                equal("automated", dto.getAutomated()),
                // Baja lógica
                equal("active", dto.getActive())
        );
    }
}
