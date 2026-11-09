package com.optiflow.crm.order.specification;
import com.optiflow.crm.order.dto.OrderSearchDto;
import com.optiflow.crm.order.model.Order;

import org.springframework.data.jpa.domain.Specification;

import static com.optiflow.crm.shared.specification.SpecificationUtils.*;

/**
 * Filtros de búsqueda avanzada de pedidos.
 * Todas las condiciones se combinan mediante AND.
 */
public final class OrderSpecification {

    private OrderSpecification() {
    }

    public static Specification<Order> filterBy(OrderSearchDto dto) {

        return Specification.allOf(

                // Número de pedido
                containsIgnoreCase("orderNumber", dto.getOrderNumber()),
                // Relaciones
                relatedIdEquals("client", dto.getClientId()),
                relatedIdEquals("seller", dto.getSellerId()),
                relatedIdEquals("quote", dto.getQuoteId()),
                // Estado
                equal("status", dto.getStatus()),
                // Fecha del pedido
                greaterThanOrEqual("orderDate", dto.getOrderDateFrom()),
                lessThanOrEqual("orderDate", dto.getOrderDateTo()),
                // Fecha prevista de entrega
                greaterThanOrEqual("expectedDeliveryDate", dto.getExpectedDeliveryDateFrom()),
                lessThanOrEqual("expectedDeliveryDate", dto.getExpectedDeliveryDateTo()),
                // Fecha de entrega efectiva
                greaterThanOrEqual("deliveryDate", dto.getDeliveryDateFrom()),
                lessThanOrEqual("deliveryDate", dto.getDeliveryDateTo()),
                // Baja lógica
                equal("active", dto.getActive()),
                // Estados seleccionados (multiselección)
                (root, query, cb) -> {

                    if (dto.getStatuses() == null || dto.getStatuses().isEmpty()) {
                        return null;
                    }
                    return root.get("status").in(dto.getStatuses());
                },
                // Importe mínimo
                greaterThanOrEqual("total", dto.getMinTotal()),
                // Importe máximo
                lessThanOrEqual("total", dto.getMaxTotal())
        );
    }
}
