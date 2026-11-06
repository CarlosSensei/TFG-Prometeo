package com.optiflow.crm.search.specification;

import com.optiflow.crm.client.model.Client;
import org.springframework.data.jpa.domain.Specification;

import java.util.Locale;

public final class GlobalSearchSpecification {

    private GlobalSearchSpecification() {
    }

    public static Specification<Client> clientsMatching(String query) {

        String normalized = query.trim()
                .toLowerCase(Locale.ROOT)
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");

        String pattern = "%" + normalized + "%";

        return (root, criteriaQuery, cb) -> cb.and(

                // Solo clientes activos.
                cb.isTrue(root.get("active")),

                // Coincidencia en cualquiera de los campos.
                cb.or(
                        cb.like(cb.lower(root.get("name")), pattern, '\\'),
                        cb.like(cb.lower(root.get("surname")), pattern, '\\'),
                        cb.like(cb.lower(root.get("lastSurname")), pattern, '\\'),
                        cb.like(cb.lower(root.get("dni")), pattern, '\\'),
                        cb.like(root.get("phoneNumber"), pattern, '\\'),
                        cb.like(cb.lower(root.get("email")), pattern, '\\'),
                        cb.like(cb.lower(root.get("city")), pattern, '\\')
                )
        );
    }
}
