package com.optiflow.crm.client.specification;
import com.optiflow.crm.client.dto.ClientSearchDto;
import com.optiflow.crm.client.model.Client;

import org.springframework.data.jpa.domain.Specification;

import static com.optiflow.crm.shared.specification.SpecificationUtils.containsIgnoreCase;
import static com.optiflow.crm.shared.specification.SpecificationUtils.equal;

/**
 * Filtros de búsqueda avanzada de clientes.
 */
public final class ClientSpecification {

    private ClientSpecification() {
    }

    public static Specification<Client> filterBy(ClientSearchDto dto) {

        return Specification.allOf(
                containsIgnoreCase("name", dto.getName()),
                containsIgnoreCase("surname", dto.getSurname()),
                containsIgnoreCase("lastSurname", dto.getLastSurname()),
                fullNameContains(dto.getFullName()),
                containsIgnoreCase("dni", dto.getDni()),
                containsIgnoreCase("phoneNumber", dto.getPhoneNumber()),
                containsIgnoreCase("email", dto.getEmail()),
                containsIgnoreCase("city", dto.getCity()),
                containsIgnoreCase("postalCode", dto.getPostalCode()),
                equal("active", dto.getActive())
        );
    }


    private static Specification<Client> fullNameContains(String value) {
        return (root, query, cb) -> {

            if (value == null || value.isBlank()) {
                return null;
            }

            String pattern = "%" + value.trim()
                    .toLowerCase(java.util.Locale.ROOT)
                    .replace("\\", "\\\\")
                    .replace("%", "\\%")
                    .replace("_", "\\_") + "%";

            var name = cb.lower(root.<String>get("name"));
            var surname = cb.lower(root.<String>get("surname"));
            var lastSurname = cb.lower(
                    cb.coalesce(root.<String>get("lastSurname"), "")
            );
            var fullName = cb.concat(
                    cb.concat(cb.concat(name, " "), surname),
                    cb.concat(" ", lastSurname)
            );

            return cb.or(
                    cb.like(name, pattern, '\\'),
                    cb.like(surname, pattern, '\\'),
                    cb.like(lastSurname, pattern, '\\'),
                    cb.like(fullName, pattern, '\\')
            );
        };
    }

}
