package com.optiflow.crm.shared.specification;

import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.Locale;

/**
 * Utilidades reutilizables para construir
 * Specifications de Spring Data JPA.
 * Los valores nulos o los textos vacíos
 * no generan condiciones de filtrado.
 */
public final class SpecificationUtils {

    private SpecificationUtils() {
    }

    /**
     * Búsqueda parcial de texto, sin distinguir
     * mayúsculas y minúsculas.
     */
    public static <T> Specification<T> containsIgnoreCase(String field, String value) {

        return (root, query, cb) -> {

            if (value == null || value.isBlank()) {
                return null;
            }

            String normalized = value.trim()
                    .toLowerCase(Locale.ROOT)
                    .replace("\\", "\\\\")
                    .replace("%", "\\%")
                    .replace("_", "\\_");

            return cb.like(cb.lower(root.get(field)),
                    "%" + normalized + "%", '\\');
        };
    }

    /**
     * Igualdad sobre un atributo de la entidad.
     * Admite Long, Boolean, Enum, String, etc.
     */
    public static <T, V> Specification<T> equal(String field, V value) {

        return (root, query, cb) ->
                value == null ? null : cb.equal(root.get(field), value);
    }

    /**
     * Igualdad sobre el ID de una relación.
     * Ejemplo: client.id = 5
     */
    public static <T> Specification<T> relatedIdEquals(String relation, Long id) {

        return (root, query, cb) ->
                id == null ? null : cb.equal(root.get(relation).get("id"), id);
    }

    /**
     * Valor mínimo inclusivo.
     * Aplicable a LocalDate, LocalDateTime,
     * BigDecimal y otros tipos comparables.
     */
    public static <T, V extends Comparable<? super V>>
    Specification<T> greaterThanOrEqual(String field, V value) {

        return (root, query, cb) ->
                value == null ? null : cb.greaterThanOrEqualTo(root.<V>get(field), value);
    }

    // Valor máximo inclusivo.
    public static <T, V extends Comparable<? super V>>
    Specification<T> lessThanOrEqual(String field, V value) {

        return (root, query, cb) ->
                value == null ? null : cb.lessThanOrEqualTo(root.<V>get(field), value);
    }

    // Precio mínimo.
    public static <T> Specification<T> minPrice(String field, BigDecimal value) {

        return greaterThanOrEqual(field, value);
    }

    // Precio máximo.
    public static <T> Specification<T> maxPrice(String field, BigDecimal value) {

        return lessThanOrEqual(field, value);
    }

    /**
     * Disponibilidad de stock.
     * true  -> stock > 0
     * false -> stock = 0
     * null  -> no filtrar
     */
    public static <T> Specification<T> inStock(String field, Boolean value) {

        return (root, query, cb) -> {

            if (value == null) {
                return null;
            }

            if (value) {
                return cb.greaterThan(root.<Integer>get(field), 0);
            }

            return cb.equal(root.get(field), 0);
        };
    }


    /**
     * Busca texto en el nombre completo de una entidad relacionada.
     * Ejemplo:
     * seller.firstName + " " + seller.lastName
     * Permite buscar tanto por nombre como por apellido.
     */
    public static <T> Specification<T> relatedFullNameContains(
            String relation,
            String firstNameField,
            String lastNameField,
            String value
    ) {
        return (root, query, cb) -> {

            if (value == null || value.isBlank()) {
                return null;
            }

            String normalized = value.trim()
                    .toLowerCase(Locale.ROOT)
                    .replace("\\", "\\\\")
                    .replace("%", "\\%")
                    .replace("_", "\\_");

            String pattern = "%" + normalized + "%";

            var related = root.get(relation);
            var firstName = cb.lower(related.<String>get(firstNameField));
            var lastName = cb.lower(related.<String>get(lastNameField));
            var fullName = cb.concat(cb.concat(firstName, " "), lastName);

            return cb.or(
                    cb.like(firstName, pattern, '\\'),
                    cb.like(lastName, pattern, '\\'),
                    cb.like(fullName, pattern, '\\')
            );
        };
    }

}
