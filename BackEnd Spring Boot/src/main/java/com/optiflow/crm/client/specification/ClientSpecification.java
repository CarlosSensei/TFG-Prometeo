package com.optiflow.crm.client.specification;

import com.optiflow.crm.client.dto.ClientSearchDto;
import com.optiflow.crm.client.model.Client;
import org.springframework.data.jpa.domain.Specification;

// Especificaciones para busquedas personalizadas
public class ClientSpecification {

    private ClientSpecification() {
    }

    // Metodo de filtrado
    public static Specification<Client> filterBy(ClientSearchDto searchDto) {

        return Specification.allOf(
                nameContains(searchDto.getName()),
                surnameContains(searchDto.getSurname()),
                lastSurnameContains(searchDto.getLastSurname()),
                DniContains(searchDto.getDni()),
                phoneNumberContains(searchDto.getPhoneNumber()),
                emailContains(searchDto.getEmail()),
                cityContains(searchDto.getCity()),
                postalCodeContains(searchDto.getPostalCode()),
                activeEquals(searchDto.getActive())
        );
    }

    // Metodo para buscar por nombre
    private static Specification<Client> nameContains(String name) {

        return (root, query, criteriaBuilder) ->  {

            if (name == null || name.isBlank()) { return null; }

            return criteriaBuilder.like(root.get("name").as(String.class), "%" + name + "%");
        };
    }

    // Metodo para buscar por apellido
    private static Specification<Client> surnameContains(String surname) {

        return (root, query, criteriaBuilder) -> {

            if (surname == null || surname.isBlank()) { return null; }

            return criteriaBuilder.like(criteriaBuilder.lower(root.get("surname")),
                    "%" + surname.toLowerCase() + "%");
        };
    }

    // Metodo para buscar por segundo apellido
    private static Specification<Client> lastSurnameContains(String lastSurname) {

        return (root, query, criteriaBuilder) -> {

            if (lastSurname == null || lastSurname.isBlank()) { return null; }

            return criteriaBuilder.like(criteriaBuilder.lower(root.get("lastSurname")),
                    "%" + lastSurname.toLowerCase() + "%");
        };
    }

    // Metodo para buscar por DNI
    private static Specification<Client> DniContains(String Dni) {

        return (root, query, criteriaBuilder) -> {

            if (Dni == null || Dni.isBlank()) { return null; }

            return criteriaBuilder.like(criteriaBuilder.lower(root.get("Dni")),
                    "%" + Dni.toLowerCase() + "%");
        };
    }

    // Metodo para buscar por telefono
    private static Specification<Client> phoneNumberContains(String phoneNumber) {

        return (root, query, criteriaBuilder) -> {

            if (phoneNumber == null || phoneNumber.isBlank()) { return null; }

            return criteriaBuilder.like(root.get("phoneNumber"),
                    "%" + phoneNumber + "%");
        };
    }

    // Metodo para buscar por email
    private static Specification<Client> emailContains(String email) {

        return (root, query, criteriaBuilder) -> {

            if (email == null || email.isBlank()) { return null; }

            return criteriaBuilder.like(criteriaBuilder.lower(root.get("email")),
                    "%" + email.toLowerCase() + "%");
        };
    }

    // Metodo para buscar por ciudad
    private static Specification<Client> cityContains(String city) {

        return (root, query, criteriaBuilder) -> {

            if (city == null || city.isBlank()) { return null; }

            return criteriaBuilder.like(criteriaBuilder.lower(root.get("city")),
                    "%" + city.toLowerCase() + "%");
        };
    }

    // Metodo para buscar por codigo postal
    private static Specification<Client> postalCodeContains(String postalCode) {

        return (root, query, criteriaBuilder) ->  {

            if (postalCode == null || postalCode.isBlank()) { return null; }

            return criteriaBuilder.like(criteriaBuilder.lower(root.get("postalCode")),
                    "%" +  postalCode.toLowerCase() + "%");
        };
    }

    // Metodo para buscar por Active.
    private static Specification<Client> activeEquals(Boolean active) {

        return (root, query, criteriaBuilder) -> {

            if (active == null) { return null; }

            return criteriaBuilder.equal(root.get("active"), active);
        };
    }
}