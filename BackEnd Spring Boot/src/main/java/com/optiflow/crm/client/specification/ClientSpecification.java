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
                containsIgnoreCase("dni", dto.getDni()),
                containsIgnoreCase("phoneNumber", dto.getPhoneNumber()),
                containsIgnoreCase("email", dto.getEmail()),
                containsIgnoreCase("city", dto.getCity()),
                containsIgnoreCase("postalCode", dto.getPostalCode()),
                equal("active", dto.getActive())
        );
    }
}
