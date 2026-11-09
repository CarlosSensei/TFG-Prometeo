package com.optiflow.crm.followup.specification;

import com.optiflow.crm.followup.dto.FollowUpSearchDto;
import com.optiflow.crm.followup.model.FollowUp;
import org.springframework.data.jpa.domain.Specification;
import java.util.List;

import static com.optiflow.crm.shared.specification.SpecificationUtils.*;

public final class FollowUpSpecification {

    private FollowUpSpecification() { }


    public static Specification<FollowUp> filterBy(FollowUpSearchDto dto) {

        boolean hasTypes = dto.getTypes() != null
                && !dto.getTypes().isEmpty();

        boolean hasStatuses = dto.getStatuses() != null
                && !dto.getStatuses().isEmpty();

        return Specification.allOf(

                relatedIdEquals("quote", dto.getQuoteId()),

                // Tipo: multiselección o filtro individual
                anyOf("type", dto.getTypes()),
                equal("type", hasTypes ? null : dto.getType()),

                // Estado: multiselección o filtro individual
                anyOf("status", dto.getStatuses()),
                equal("status", hasStatuses ? null : dto.getStatus()),

                // Fechas programadas
                greaterThanOrEqual("scheduledAt", dto.getScheduledFrom()),
                lessThanOrEqual("scheduledAt", dto.getScheduledTo()),

                // Fechas de finalización
                greaterThanOrEqual("completedAt", dto.getCompletedFrom()),
                lessThanOrEqual("completedAt", dto.getCompletedTo()),

                // Origen y estado activo
                equal("automated", dto.getAutomated()),
                equal("active", dto.getActive())
        );
    }


    private static <T, V> Specification<T> anyOf(String field, List<V> values) {
        return (root, query, cb) ->
                values == null || values.isEmpty() ? null : root.get(field).in(values);
    }
}
