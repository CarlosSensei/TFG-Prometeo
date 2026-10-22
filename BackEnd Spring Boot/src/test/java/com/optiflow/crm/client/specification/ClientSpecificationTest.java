package com.optiflow.crm.client.specification;

import com.optiflow.crm.client.dto.ClientSearchDto;
import com.optiflow.crm.client.model.Client;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

/**
 * Pruebas UNITARIAS de la construcción de predicados en ClientSpecification.
 * No se inicia Spring Boot ni se accede a H2.
 *
 * Nota: estos tests validan el Criteria API generado, pero NO ejecutan SQL real.
 * La ejecución contra JPA/H2 corresponde a un test de integración posterior.
 */
@ExtendWith(MockitoExtension.class)
class ClientSpecificationTest {

    @Mock private Root<Client> root;
    @Mock private CriteriaQuery<?> query;
    @Mock private CriteriaBuilder criteriaBuilder;

    @Test
    void givenNullFilters_whenFilterBy_thenDoNotCreatePredicate() {
        ClientSearchDto search = new ClientSearchDto();
        search.setActive(null); // Por defecto ClientSearchDto lleva active=true.

        Predicate result = specification(search).toPredicate(root, query, criteriaBuilder);

        assertNull(result);
        verifyNoInteractions(root, criteriaBuilder);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "\t"})
    void givenBlankStringFilters_whenFilterBy_thenIgnoreThem(String blank) {
        ClientSearchDto search = new ClientSearchDto();
        search.setName(blank);
        search.setSurname(blank);
        search.setLastSurname(blank);
        search.setDni(blank);
        search.setPhoneNumber(blank);
        search.setEmail(blank);
        search.setCity(blank);
        search.setPostalCode(blank);
        search.setActive(null);

        Predicate result = specification(search).toPredicate(root, query, criteriaBuilder);

        assertNull(result);
        verifyNoInteractions(root, criteriaBuilder);
    }

    @Test
    void givenName_whenFilterBy_thenUseContainsPattern() {

        // GIVEN
        ClientSearchDto search = new ClientSearchDto();
        search.setActive(null);
        search.setName("Ana");

        @SuppressWarnings("unchecked")
        Path<String> path = mock(Path.class);

        @SuppressWarnings("unchecked")
        Expression<String> lowercase = mock(Expression.class);

        Predicate expected = mock(Predicate.class);

        // Simulamos el acceso al campo name
        when(root.<String>get("name")).thenReturn(path);

        // Simulamos la conversión a minúsculas
        when(criteriaBuilder.lower(path)).thenReturn(lowercase);

        // Simulamos el predicado LIKE
        when(criteriaBuilder.like(lowercase, "%ana%")).thenReturn(expected);

        // WHEN
        Predicate result = specification(search).toPredicate(root, query, criteriaBuilder);

        // THEN
        assertSame(expected, result);

        verify(root).get("name");
        verify(criteriaBuilder).lower(path);
        verify(criteriaBuilder).like(lowercase, "%ana%");
    }

    @ParameterizedTest(name = "{0} transforma {1} en {2}")
    @MethodSource("caseInsensitiveTextFields")
    void givenTextFilter_whenFilterBy_thenIgnoreCaseAndMatchSubstring(
            String property, String input, String pattern) {
        ClientSearchDto search = new ClientSearchDto();
        search.setActive(null);
        setTextFilter(search, property, input);

        assertLowercaseLike(search, property, pattern);
    }

    static Stream<Arguments> caseInsensitiveTextFields() {
        return Stream.of(
                Arguments.of("surname", "GARCIA", "%garcia%"),
                Arguments.of("lastSurname", "LOPEZ", "%lopez%"),
                Arguments.of("email", "EJEMPLO@MAIL.COM", "%ejemplo@mail.com%"),
                Arguments.of("city", "BARCELONA", "%barcelona%"),
                Arguments.of("postalCode", "08036", "%08036%")
        );
    }

    @Test
    void givenDni_whenFilterBy_thenUseActualJavaEntityProperty() {
        ClientSearchDto search = new ClientSearchDto();
        search.setActive(null);
        search.setDni("12345678Z");

        // Acepta tanto 'Dni' (campo publicado en el repositorio) como 'dni'
        // (campo recomendado después de la normalización), según Client REAL.
        // Detecta si la Specification aún referencia un nombre que ya no existe.
        String javaProperty = Arrays.stream(Client.class.getDeclaredFields())
                .map(Field::getName)
                .filter(field -> field.equalsIgnoreCase("dni"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Client no tiene propiedad Java para dni"));

        assertLowercaseLike(search, javaProperty, "%12345678z%");
    }

    @Test
    void givenPhone_whenFilterBy_thenUseContainsPattern() {
        ClientSearchDto search = new ClientSearchDto();
        search.setActive(null);
        search.setPhoneNumber("600");

        @SuppressWarnings("unchecked")
        Path<String> path = mock(Path.class);
        Predicate expected = mock(Predicate.class);

        when(root.<String>get("phoneNumber")).thenReturn(path);
        when(criteriaBuilder.like(path, "%600%")).thenReturn(expected);

        Predicate result = specification(search).toPredicate(root, query, criteriaBuilder);

        assertSame(expected, result);
        verify(criteriaBuilder).like(path, "%600%");
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void givenActive_whenFilterBy_thenMatchExactBoolean(boolean active) {
        ClientSearchDto search = new ClientSearchDto();
        search.setActive(active);

        @SuppressWarnings("unchecked")
        Path<Boolean> path = mock(Path.class);
        Predicate expected = mock(Predicate.class);

        when(root.<Boolean>get("active")).thenReturn(path);
        when(criteriaBuilder.equal(path, active)).thenReturn(expected);

        Predicate result = specification(search).toPredicate(root, query, criteriaBuilder);

        assertSame(expected, result);
        verify(criteriaBuilder).equal(path, active);
    }

    @Test
    void givenSurnameAndCity_whenFilterBy_thenCombineWithAnd() {
        ClientSearchDto search = new ClientSearchDto();
        search.setActive(null);
        search.setSurname("PEREZ");
        search.setCity("MADRID");

        Predicate surnamePredicate = stubLowercaseLike("surname", "%perez%");
        Predicate cityPredicate = stubLowercaseLike("city", "%madrid%");
        Predicate expectedAnd = mock(Predicate.class);
        when(criteriaBuilder.and(surnamePredicate, cityPredicate)).thenReturn(expectedAnd);

        Predicate result = specification(search).toPredicate(root, query, criteriaBuilder);

        assertSame(expectedAnd, result);
        verify(criteriaBuilder).and(surnamePredicate, cityPredicate);
    }

    @Test
    void givenSurnameAndDefaultActive_whenFilterBy_thenCombineWithAnd() {
        ClientSearchDto search = new ClientSearchDto();
        search.setSurname("PEREZ");
        // active es true por defecto en ClientSearchDto.

        Predicate surnamePredicate = stubLowercaseLike("surname", "%perez%");
        @SuppressWarnings("unchecked")
        Path<Boolean> activePath = mock(Path.class);
        Predicate activePredicate = mock(Predicate.class);
        when(root.<Boolean>get("active")).thenReturn(activePath);
        when(criteriaBuilder.equal(activePath, true)).thenReturn(activePredicate);

        Predicate expectedAnd = mock(Predicate.class);
        when(criteriaBuilder.and(surnamePredicate, activePredicate)).thenReturn(expectedAnd);

        Predicate result = specification(search).toPredicate(root, query, criteriaBuilder);

        assertSame(expectedAnd, result);
        verify(criteriaBuilder).and(surnamePredicate, activePredicate);
    }

    private static Specification<Client> specification(ClientSearchDto search) {
        return ClientSpecification.filterBy(search);
    }

    private void assertLowercaseLike(ClientSearchDto search, String property, String pattern) {
        Predicate expected = stubLowercaseLike(property, pattern);

        Predicate result = specification(search).toPredicate(root, query, criteriaBuilder);

        assertSame(expected, result);
        verify(root).get(property);
    }

    private Predicate stubLowercaseLike(String property, String pattern) {
        @SuppressWarnings("unchecked")
        Path<String> path = mock(Path.class);
        @SuppressWarnings("unchecked")
        Expression<String> lowercase = mock(Expression.class);
        Predicate predicate = mock(Predicate.class);

        when(root.<String>get(property)).thenReturn(path);
        when(criteriaBuilder.lower(path)).thenReturn(lowercase);
        when(criteriaBuilder.like(lowercase, pattern)).thenReturn(predicate);

        return predicate;
    }

    private static void setTextFilter(ClientSearchDto search, String property, String value) {
        switch (property) {
            case "surname" -> search.setSurname(value);
            case "lastSurname" -> search.setLastSurname(value);
            case "email" -> search.setEmail(value);
            case "city" -> search.setCity(value);
            case "postalCode" -> search.setPostalCode(value);
            default -> throw new IllegalArgumentException("Campo inesperado: " + property);
        }
    }
}
