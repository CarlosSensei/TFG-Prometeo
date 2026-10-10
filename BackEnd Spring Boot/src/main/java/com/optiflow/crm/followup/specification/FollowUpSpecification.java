package com.optiflow.crm.followup.specification;

import com.optiflow.crm.followup.dto.FollowUpSearchDto;
import com.optiflow.crm.followup.model.FollowUp;
import com.optiflow.crm.followup.model.FollowUpStatus;
import com.optiflow.crm.followup.model.FollowUpType;

import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

// Especificaciones personalizadas de busqueda
public class FollowUpSpecification {

    // constructor vacio
    private FollowUpSpecification() {
    }

    // Metodo para filtrar
    public static Specification<FollowUp> filterBy(FollowUpSearchDto searchDto) {

        return Specification.allOf(
                quoteIdEquals(searchDto.getQuoteId()),
                typeEquals(searchDto.getType()),
                statusEquals(searchDto.getStatus()),
                scheduledFrom(searchDto.getScheduledFrom()),
                scheduledTo(searchDto.getScheduledTo()),
                completedFrom(searchDto.getCompletedFrom()),
                completedTo(searchDto.getCompletedTo()),
                automatedEquals(searchDto.getAutomated()),
                activeEquals(searchDto.getActive())
        );
    }

    // Metodo para buscar por Id de presupuesto
    private static Specification<FollowUp> quoteIdEquals(Long quoteId) {

        return (root, query, criteriaBuilder) -> {

            if (quoteId == null) { return null; }

            return criteriaBuilder.equal(root.get("quote").get("id"), quoteId);
        };
    }

    // Metodo para buscar por tipo de seguimiento
    private static Specification<FollowUp> typeEquals(FollowUpType type) {

        return (root, query, criteriaBuilder) -> {

            if (type == null) { return null; }

            return criteriaBuilder.equal(root.get("type"), type);
        };
    }

    // Metodo para buscar por status del seguimiento
    private static Specification<FollowUp> statusEquals(FollowUpStatus status) {

        return (root, query, criteriaBuilder) -> {

            if (status == null) { return null;}

            return criteriaBuilder.equal(root.get("status"), status);
        };
    }

    // Metodo para buscar por fecha de seguimiento desde
    private static Specification<FollowUp> scheduledFrom(LocalDateTime scheduledFrom) {

        return (root, query, criteriaBuilder) -> {

            if (scheduledFrom == null) { return null; }

            return criteriaBuilder.greaterThanOrEqualTo(root.get("scheduledAt"), scheduledFrom);
        };
    }

    // Metodo para buscar por fecha de seguimiento hasta
    private static Specification<FollowUp> scheduledTo(LocalDateTime scheduledTo) {

        return (root, query, criteriaBuilder) -> {

            if (scheduledTo == null) { return null; }

            return criteriaBuilder.lessThanOrEqualTo(root.get("scheduledAt"), scheduledTo);
        };
    }

    // Metodo para buscar por fecha de seguimiento completado desde
    private static Specification<FollowUp> completedFrom(LocalDateTime completedFrom) {

        return (root, query, criteriaBuilder) -> {

            if (completedFrom == null) { return null; }

            return criteriaBuilder.greaterThanOrEqualTo(root.get("completedAt"), completedFrom);
        };
    }

    // Metodo para buscar por fecha de seguimiento completado hasta
    private static Specification<FollowUp> completedTo(LocalDateTime completedTo) {

        return (root, query, criteriaBuilder) -> {

            if (completedTo == null) { return null; }

            return criteriaBuilder.lessThanOrEqualTo(root.get("completedAt"), completedTo);
        };
    }

    // Busqueda por seguimientos automatizados
    private static Specification<FollowUp> automatedEquals(Boolean automated) {

        return (root, query, criteriaBuilder) -> {

            if (automated == null) { return null; }

            return criteriaBuilder.equal(root.get("automated"), automated);
        };
    }

    // Busqueda por seguimientos activos
    private static Specification<FollowUp> activeEquals(Boolean active) {

        return (root, query, criteriaBuilder) -> {

            if (active == null) { return null; }

            return criteriaBuilder.equal(root.get("active"), active);
        };
    }
}