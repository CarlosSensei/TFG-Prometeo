package com.optiflow.crm.order.specification;

import com.optiflow.crm.order.dto.OrderSearchDto;
import com.optiflow.crm.order.model.Order;
import com.optiflow.crm.order.model.OrderStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

// Especificaciones de busqueda personalizadas
public class OrderSpecification {

    // Constructor vacio
    private OrderSpecification() {
    }

    // Filtros de busqueda
    public static Specification<Order> filterBy(OrderSearchDto searchDto) {

        return Specification.allOf(
                orderNumberContains(searchDto.getOrderNumber()),
                clientIdEquals(searchDto.getClientId()),
                sellerIdEquals(searchDto.getSellerId()),
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

    // Busqueda por numero de pedido
    private static Specification<Order> orderNumberContains(String orderNumber) {

        return (root, query, criteriaBuilder) -> {

            if (orderNumber == null || orderNumber.isBlank()) { return null; }

            return criteriaBuilder.like(criteriaBuilder.lower(root.get("orderNumber")),
                    "%" + orderNumber.toLowerCase() + "%");
        };
    }

    // Busqueda de pedido por cliente
    private static Specification<Order> clientIdEquals(Long clientId) {

        return (root, query, criteriaBuilder) -> {

            if (clientId == null) { return null; }

            return criteriaBuilder.equal(root.get("client").get("id"), clientId );
        };
    }

    // Busqueda de seller por Id
    private static Specification<Order> sellerIdEquals(Long sellerId) {

        return (root, query, cb) -> {

            if (sellerId == null) {
                return null;
            }

            return cb.equal(root.get("seller").get("id"), sellerId);
        };
    }

    // Busqueda por Id de presupuesto
    private static Specification<Order> quoteIdEquals(Long quoteId) {

        return (root, query, criteriaBuilder) -> {

            if (quoteId == null) { return null; }

            return criteriaBuilder.equal(root.get("quote").get("id"), quoteId);
        };
    }

    // Busqueda por estatus
    private static Specification<Order> statusEquals(OrderStatus status) {

        return (root, query, criteriaBuilder) -> {

            if (status == null) { return null; }

            return criteriaBuilder.equal(root.get("status"), status);
        };
    }

    // Busqueda por fecha de creacion desde
    private static Specification<Order> orderDateFrom(LocalDate orderDateFrom) {

        return (root, query, criteriaBuilder) -> {

            if (orderDateFrom == null) { return null; }

            return criteriaBuilder.greaterThanOrEqualTo(root.get("orderDate"), orderDateFrom);
        };
    }

    // Busqueda por fecha de creacion hasta
    private static Specification<Order> orderDateTo(LocalDate orderDateTo) {

        return (root, query, criteriaBuilder) -> {

            if (orderDateTo == null) { return null; }

            return criteriaBuilder.lessThanOrEqualTo(root.get("orderDate"), orderDateTo);
        };
    }

    // Busqueda por fecha de entrega prevista desde
    private static Specification<Order> expectedDeliveryDateFrom(LocalDate expectedDeliveryDateFrom) {

        return (root, query, criteriaBuilder) -> {

            if (expectedDeliveryDateFrom == null) { return null; }

            return criteriaBuilder.greaterThanOrEqualTo(
                    root.get("expectedDeliveryDate"), expectedDeliveryDateFrom);
        };
    }

    // Busqueda por fecha de entrega prevista hasta
    private static Specification<Order> expectedDeliveryDateTo(LocalDate expectedDeliveryDateTo) {

        return (root, query, criteriaBuilder) -> {

            if (expectedDeliveryDateTo == null) { return null; }

            return criteriaBuilder.lessThanOrEqualTo(root.get("expectedDeliveryDate"),
                    expectedDeliveryDateTo);
        };
    }

    // Busqueda por fecha de entrega completada desde
    private static Specification<Order> deliveryDateFrom(LocalDate deliveryDateFrom) {

        return (root, query, criteriaBuilder) -> {

            if (deliveryDateFrom == null) { return null; }

            return criteriaBuilder.greaterThanOrEqualTo(root.get("deliveryDate"), deliveryDateFrom);
        };
    }

    // Busqueda por fecha de entrega completada hasta
    private static Specification<Order> deliveryDateTo(LocalDate deliveryDateTo) {

        return (root, query, criteriaBuilder) -> {

            if (deliveryDateTo == null) { return null; }

            return criteriaBuilder.lessThanOrEqualTo(root.get("deliveryDate"), deliveryDateTo);
        };
    }

    // Busqueda por pedidos activos
    private static Specification<Order> activeEquals(Boolean active) {

        return (root, query, criteriaBuilder) -> {

            if (active == null) { return null; }

            return criteriaBuilder.equal(root.get("active"), active);
        };
    }
}