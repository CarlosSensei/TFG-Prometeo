package com.optiflow.crm.search;

import com.optiflow.crm.client.ClientRepository;
import com.optiflow.crm.client.model.Client;
import com.optiflow.crm.quote.QuoteRepository;
import com.optiflow.crm.quote.model.Quote;
import com.optiflow.crm.order.OrderRepository;
import com.optiflow.crm.order.model.Order;
import com.optiflow.crm.search.dto.SearchResultDto;
import com.optiflow.crm.search.specification.GlobalSearchSpecification;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class SearchService {

    private final ClientRepository clientRepository;
    private final QuoteRepository quoteRepository;
    private final OrderRepository orderRepository;

    public SearchService(
            ClientRepository clientRepository,
            QuoteRepository quoteRepository,
            OrderRepository orderRepository
    ) {
        this.clientRepository = clientRepository;
        this.quoteRepository = quoteRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional(readOnly = true)
    public List<SearchResultDto> search(String query, int limit) {

        String term = query == null ? "" : query.trim();

        if (term.length() < 2) {
            return List.of();
        }

        int safeLimit = Math.clamp(limit, 1, 20);

        // Reparto inicial del límite entre los módulos.
        // Los huecos disponibles se rellenan después.
        int perModule = safeLimit;

        var clientPage = PageRequest.of(0, perModule,
                Sort.by("surname", "name", "id"));

        var quotePage = PageRequest.of(0, perModule,
                Sort.by(Sort.Direction.DESC, "quoteDate"));

        var orderPage = PageRequest.of(0, perModule,
                Sort.by(Sort.Direction.DESC, "orderDate"));

        List<SearchResultDto> clients = clientRepository
                .findAll(
                        GlobalSearchSpecification.clientsMatching(term),
                        clientPage
                )
                .getContent()
                .stream()
                .map(this::mapClient)
                .toList();

        List<SearchResultDto> quotes = quoteRepository
                .findAll(
                        GlobalSearchSpecification.quotesMatching(term),
                        quotePage
                )
                .getContent()
                .stream()
                .map(this::mapQuote)
                .toList();

        List<SearchResultDto> orders = orderRepository
                .findAll(
                        GlobalSearchSpecification.ordersMatching(term),
                        orderPage
                )
                .getContent()
                .stream()
                .map(this::mapOrder)
                .toList();

        // Reservar espacio para cada categoría con resultados.
        List<SearchResultDto> results = new ArrayList<>();

        int baseQuota = safeLimit / 3;

        int clientCount = Math.min(clients.size(), baseQuota);
        int quoteCount = Math.min(quotes.size(), baseQuota);
        int orderCount = Math.min(orders.size(), baseQuota);

        results.addAll(clients.subList(0, clientCount));
        results.addAll(quotes.subList(0, quoteCount));
        results.addAll(orders.subList(0, orderCount));

        // Completar el espacio restante respetando
        // el orden de prioridad: clientes, presupuestos, pedidos.
        List<SearchResultDto> remaining = new ArrayList<>();
        remaining.addAll(clients.subList(clientCount, clients.size()));
        remaining.addAll(quotes.subList(quoteCount, quotes.size()));
        remaining.addAll(orders.subList(orderCount, orders.size()));

        int available = safeLimit - results.size();

        results.addAll(remaining.subList(0, Math.min(available, remaining.size())));

        return results;
    }

    private SearchResultDto mapClient(Client client) {

        String fullName = Stream.of(
                        client.getName(),
                        client.getSurname(),
                        client.getLastSurname()
                )
                .filter(value -> value != null && !value.isBlank())
                .collect(Collectors.joining(" "));

        String subtitle = joinNonBlank(
                client.getDni(),
                client.getPhoneNumber(),
                client.getEmail(),
                client.getCity()
        );

        return new SearchResultDto(
                client.getId(),
                "client",
                fullName,
                subtitle,
                client.getId()
        );
    }

    private SearchResultDto mapQuote(Quote quote) {

        return new SearchResultDto(
                quote.getId(),
                "quote",
                quote.getQuoteNumber(),
                joinNonBlank(
                        "Presupuesto",
                        "Estado: " + quote.getStatus().name(),
                        "Cliente: " + clientName(quote.getClient())
                ),
                quote.getClient().getId()
        );
    }

    private SearchResultDto mapOrder(Order order) {

        return new SearchResultDto(
                order.getId(),
                "order",
                order.getOrderNumber(),
                joinNonBlank(
                        "Pedido",
                        "Estado: " + order.getStatus().name(),
                        "Cliente: " + clientName(order.getClient())
                ),
                order.getClient().getId()
        );
    }

    private String clientName(Client client) {
        return joinNonBlank(
                client.getName(),
                client.getSurname(),
                client.getLastSurname()
        ).replace(" · ", " ");
    }

    private String joinNonBlank(String... values) {

        return Stream.of(values)
                .filter(value -> value != null && !value.isBlank())
                .collect(Collectors.joining(" · "));
    }
}
