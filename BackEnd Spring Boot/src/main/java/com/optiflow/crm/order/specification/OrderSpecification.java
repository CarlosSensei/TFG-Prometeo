package com.optiflow.crm.order.specification;

import com.optiflow.crm.order.dto.OrderSearchDto;
import com.optiflow.crm.order.model.Order;
import com.optiflow.crm.order.model.OrderStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class OrderSpecification {

    private OrderSpecification() {
    }

    public static Specification<Order> filterBy(OrderSearchDto searchDto) {

        return Specification.allOf(
                orderNumberContains(searchDto.getOrderNumber()),
                clientIdEquals(searchDto.getClientId()),
                quoteIdEquals(searchDto.getQuoteId()),
                statusEquals(searchDto.getStatus()),
                orderDateFrom(searchDto.getOrderDateFrom()),
                orderDateTo(searchDto.getOrderDateTo()),
                expectedDeliveryDateFrom(searchDto.getExpectedDeliveryDateFrom()),
                expectedDeliveryDateTo(searchDto.getExpectedDeliveryDateTo()),
                deliveryDateFrom(searchDto.getDeliveryDateFrom()),
                deliveryDateTo(searchDto.getDeliveryDateTo()),
                activeEquals(searchDto.getActive())
        );
    }

    private static Specification<Order> orderNumberContains(String orderNumber) {

        return (root, query, criteriaBuilder) -> {

            if (orderNumber == null || orderNumber.isBlank()) { return null; }

            return criteriaBuilder.like(criteriaBuilder.lower(root.get("orderNumber")),
                    "%" + orderNumber.toLowerCase() + "%");
        };
    }

    private static Specification<Order> clientIdEquals(Long clientId) {

        return (root, query, criteriaBuilder) -> {

            if (clientId == null) { return null; }

            return criteriaBuilder.equal(root.get("client").get("id"), clientId );
        };
    }

    private static Specification<Order> quoteIdEquals(Long quoteId) {

        return (root, query, criteriaBuilder) -> {

            if (quoteId == null) { return null; }

            return criteriaBuilder.equal(root.get("quote").get("id"), quoteId);
        };
    }

    private static Specification<Order> statusEquals(OrderStatus status) {

        return (root, query, criteriaBuilder) -> {

            if (status == null) { return null; }

            return criteriaBuilder.equal(root.get("status"), status);
        };
    }

    private static Specification<Order> orderDateFrom(LocalDate orderDateFrom) {

        return (root, query, criteriaBuilder) -> {

            if (orderDateFrom == null) { return null; }

            return criteriaBuilder.greaterThanOrEqualTo(root.get("orderDate"), orderDateFrom);
        };
    }

    private static Specification<Order> orderDateTo(LocalDate orderDateTo) {

        return (root, query, criteriaBuilder) -> {

            if (orderDateTo == null) { return null; }

            return criteriaBuilder.lessThanOrEqualTo(root.get("orderDate"), orderDateTo);
        };
    }

    private static Specification<Order> expectedDeliveryDateFrom(
            LocalDate expectedDeliveryDateFrom
    ) {

        return (root, query, criteriaBuilder) -> {

            if (expectedDeliveryDateFrom == null) { return null; }

            return criteriaBuilder.greaterThanOrEqualTo(
                    root.get("expectedDeliveryDate"), expectedDeliveryDateFrom);
        };
    }

    private static Specification<Order> expectedDeliveryDateTo(
            LocalDate expectedDeliveryDateTo
    ) {

        return (root, query, criteriaBuilder) -> {

            if (expectedDeliveryDateTo == null) { return null; }

            return criteriaBuilder.lessThanOrEqualTo(root.get("expectedDeliveryDate"),
                    expectedDeliveryDateTo);
        };
    }

    private static Specification<Order> deliveryDateFrom(LocalDate deliveryDateFrom) {

        return (root, query, criteriaBuilder) -> {

            if (deliveryDateFrom == null) { return null; }

            return criteriaBuilder.greaterThanOrEqualTo(root.get("deliveryDate"), deliveryDateFrom);
        };
    }

    private static Specification<Order> deliveryDateTo(LocalDate deliveryDateTo) {

        return (root, query, criteriaBuilder) -> {

            if (deliveryDateTo == null) { return null; }

            return criteriaBuilder.lessThanOrEqualTo(root.get("deliveryDate"), deliveryDateTo);
        };
    }

    private static Specification<Order> activeEquals(Boolean active) {

        return (root, query, criteriaBuilder) -> {

            if (active == null) { return null; }

            return criteriaBuilder.equal(root.get("active"), active);
        };
    }
}