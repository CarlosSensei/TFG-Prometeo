package com.optiflow.crm.inventory.specification;

import com.optiflow.crm.inventory.dto.InventorySearchDto;
import com.optiflow.crm.inventory.model.InventoryItem;
import com.optiflow.crm.inventory.model.InventoryItemType;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

// Specifications para busquedas personalizadas de inventory items
public class InventorySpecification {

    private InventorySpecification() {
    }

    // Parametros de filtros
    public static Specification<InventoryItem> filterBy(InventorySearchDto searchDto) {

        return Specification.allOf(
                skuContains(searchDto.getSku()),
                nameContains(searchDto.getName()),
                brandContains(searchDto.getBrand()),
                typeEquals(searchDto.getType()),
                minPrice(searchDto.getMinPrice()),
                maxPrice(searchDto.getMaxPrice()),
                inStock(searchDto.getInStock()),
                activeEquals(searchDto.getActive())
        );
    }

    // Busqueda por sku
    private static Specification<InventoryItem> skuContains(String sku) {

        return (root, query, cb) -> {

            if (sku == null || sku.isBlank()) {
                return null;
            }

            return cb.like(cb.lower(root.get("sku")), "%" + sku.toLowerCase() + "%");
        };
    }

    // Busqueda por nombre
    private static Specification<InventoryItem> nameContains(String name) {

        return (root, query, cb) -> {

            if (name == null || name.isBlank()) {
                return null;
            }

            return cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
        };
    }

    // Busqueda por marca
    private static Specification<InventoryItem> brandContains(String brand) {

        return (root, query, cb) -> {

            if (brand == null || brand.isBlank()) {
                return null;
            }

            return cb.like(cb.lower(root.get("brand")), "%" + brand.toLowerCase() + "%");
        };
    }

    // Busqueda por familia de item
    private static Specification<InventoryItem> typeEquals(InventoryItemType type) {

        return (root, query, cb) -> {

            if (type == null) {
                return null;
            }

            return cb.equal(root.get("type"), type);
        };
    }

    // Busqueda por precio minimo
    private static Specification<InventoryItem> minPrice(BigDecimal minPrice) {

        return (root, query, cb) -> {

            if (minPrice == null) {
                return null;
            }

            return cb.greaterThanOrEqualTo(root.get("salePrice"), minPrice);
        };
    }

    // Busqueda por precio maximo
    private static Specification<InventoryItem> maxPrice(BigDecimal maxPrice) {

        return (root, query, cb) -> {

            if (maxPrice == null) {
                return null;
            }

            return cb.lessThanOrEqualTo(root.get("salePrice"), maxPrice);
        };
    }

    // Busqueda por stock disponible
    private static Specification<InventoryItem> inStock(Boolean inStock) {

        return (root, query, cb) -> {

            if (inStock == null) {
                return null;
            }

            if (inStock) {
                return cb.greaterThan(root.get("stock"), 0);
            }

            return cb.equal(root.get("stock"), 0);
        };
    }

    // Busqueda por estado activo
    private static Specification<InventoryItem> activeEquals(Boolean active) {

        return (root, query, cb) -> {

            if (active == null) {
                return null;
            }

            return cb.equal(root.get("active"), active);
        };
    }
}