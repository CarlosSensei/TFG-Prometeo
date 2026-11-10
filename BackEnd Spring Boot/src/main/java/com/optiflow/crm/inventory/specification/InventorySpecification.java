package com.optiflow.crm.inventory.specification;

import com.optiflow.crm.inventory.dto.InventorySearchDto;
import com.optiflow.crm.inventory.model.InventoryItem;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static com.optiflow.crm.shared.specification.SpecificationUtils.*;

/**
 * Filtros de búsqueda avanzada de inventario.
 */
public final class InventorySpecification {

    private InventorySpecification() {
    }

    public static Specification<InventoryItem> filterBy(InventorySearchDto dto) {

        return Specification.allOf(

                // Referencia SKU
                containsIgnoreCase("sku", dto.getSku()),
                // Nombre del artículo
                containsIgnoreCase("name", dto.getName()),
                // Marca
                containsIgnoreCase("brand", dto.getBrand()),
                // Familia o tipo de artículo
                anyOf("type", dto.getTypes()),
                equal("type", dto.getTypes() != null && !dto.getTypes().isEmpty()
                        ? null : dto.getType()),
                // Intervalo de precio de venta
                minPrice("salePrice", dto.getMinPrice()),
                maxPrice("salePrice", dto.getMaxPrice()),
                // Disponibilidad
                inStock("stock", dto.getInStock()),
                // Baja lógica
                equal("active", dto.getActive())
        );
    }

    private static <T, V> Specification<T> anyOf(String field, List<V> values) {
        return (root, query, cb) ->
                values == null || values.isEmpty() ? null : root.get(field).in(values);
    }
}
