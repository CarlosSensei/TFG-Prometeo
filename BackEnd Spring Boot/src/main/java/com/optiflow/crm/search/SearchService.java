package com.optiflow.crm.search;

import com.optiflow.crm.client.ClientRepository;
import com.optiflow.crm.client.model.Client;
import com.optiflow.crm.search.dto.SearchResultDto;
import com.optiflow.crm.search.specification.GlobalSearchSpecification;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class SearchService {

    private final ClientRepository clientRepository;

    public SearchService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @Transactional(readOnly = true)
    public List<SearchResultDto> search(String query, int limit) {

        String term = query == null ? "" : query.trim();

        // Evitar búsquedas vacías o demasiado generales.
        if (term.length() < 2) {
            return List.of();
        }

        // Limitar la cantidad de resultados.
        int safeLimit = Math.max(1, Math.min(limit, 20));

        PageRequest pageable = PageRequest.of(0, safeLimit,
                Sort.by(
                        Sort.Direction.ASC,
                        "surname",
                        "name",
                        "id"
                )
        );

        return clientRepository.findAll(
                        GlobalSearchSpecification.clientsMatching(term),
                        pageable
                ).getContent()
                .stream()
                .map(this::mapClient)
                .toList();
    }

    private SearchResultDto mapClient(Client client) {

        String fullName = Stream.of(
                        client.getName(),
                        client.getSurname(),
                        client.getLastSurname()
                )
                .filter(value -> value != null && !value.isBlank())
                .collect(Collectors.joining(" "));

        String subtitle = Stream.of(
                        client.getDni(),
                        client.getPhoneNumber(),
                        client.getEmail(),
                        client.getCity()
                )
                .filter(value -> value != null && !value.isBlank())
                .collect(Collectors.joining(" · "));

        return new SearchResultDto(client.getId(), "client", fullName, subtitle);
    }
}
