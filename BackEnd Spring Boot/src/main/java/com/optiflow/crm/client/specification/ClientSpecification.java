package com.optiflow.crm.client.specification;

import com.optiflow.crm.client.dto.ClientSearchDto;
import com.optiflow.crm.client.model.Client;
import org.springframework.data.jpa.domain.Specification;

public class ClientSpecification {

    private ClientSpecification() {
    }

    public static Specification<Client> filterBy(ClientSearchDto searchDto) {

        return Specification.allOf(
                nameContains(searchDto.getName()),
                firstSurnameContains(searchDto.getFirstSurname()),
                lastSurnameContains(searchDto.getLastSurname()),
                DNIContains(searchDto.getDNI()),
                phoneNumberContains(searchDto.getPhoneNumber()),
                emailContains(searchDto.getEmail()),
                cityContains(searchDto.getCity()),
                postalCodeContains(searchDto.getPostalCode()),
                activeEquals(searchDto.getActive())
        );
    }

    private static Specification<Client> nameContains(String name) {

        return (root, query, criteriaBuilder) ->  {

            if (name == null || name.isBlank()) { return null; }

            return criteriaBuilder.like(root.get("name").as(String.class), "%" + name + "%");
        };
    }

    private static Specification<Client> firstSurnameContains(String firstSurname) {

        return (root, query, criteriaBuilder) -> {

            if (firstSurname == null || firstSurname.isBlank()) { return null; }

            return criteriaBuilder.like(criteriaBuilder.lower(root.get("firstSurname")),
                    "%" + firstSurname.toLowerCase() + "%");
        };
    }

    private static Specification<Client> lastSurnameContains(String lastSurname) {

        return (root, query, criteriaBuilder) -> {

            if (lastSurname == null || lastSurname.isBlank()) { return null; }

            return criteriaBuilder.like(criteriaBuilder.lower(root.get("lastSurname")),
                    "%" + lastSurname.toLowerCase() + "%");
        };
    }

    private static Specification<Client> DNIContains(String DNI) {

        return (root, query, criteriaBuilder) -> {

            if (DNI == null || DNI.isBlank()) { return null; }

            return criteriaBuilder.like(criteriaBuilder.lower(root.get("DNI")),
                    "%" + DNI.toLowerCase() + "%");
        };
    }

    private static Specification<Client> phoneNumberContains(String phoneNumber) {

        return (root, query, criteriaBuilder) -> {

            if (phoneNumber == null || phoneNumber.isBlank()) { return null; }

            return criteriaBuilder.like(root.get("phoneNumber"),
                    "%" + phoneNumber + "%");
        };
    }

    private static Specification<Client> emailContains(String email) {

        return (root, query, criteriaBuilder) -> {

            if (email == null || email.isBlank()) { return null; }

            return criteriaBuilder.like(criteriaBuilder.lower(root.get("email")),
                    "%" + email.toLowerCase() + "%");
        };
    }

    private static Specification<Client> cityContains(String city) {

        return (root, query, criteriaBuilder) -> {

            if (city == null || city.isBlank()) { return null; }

            return criteriaBuilder.like(criteriaBuilder.lower(root.get("city")),
                    "%" + city.toLowerCase() + "%");
        };
    }

    private static Specification<Client> postalCodeContains(String postalCode) {

        return (root, query, criteriaBuilder) ->  {

            if (postalCode == null || postalCode.isBlank()) { return null; }

            return criteriaBuilder.like(criteriaBuilder.lower(root.get("postalCode")),
                    "%" +  postalCode.toLowerCase() + "%");
        };
    }

    private static Specification<Client> activeEquals(Boolean active) {

        return (root, query, criteriaBuilder) -> {

            if (active == null) { return null; }

            return criteriaBuilder.equal(root.get("active"), active);
        };
    }
}