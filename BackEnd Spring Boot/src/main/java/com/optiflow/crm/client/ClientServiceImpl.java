package com.optiflow.crm.client;

import com.optiflow.crm.client.dto.ClientDto;
import com.optiflow.crm.client.dto.ClientStatsDto;
import com.optiflow.crm.client.dto.ClientSearchDto;
import com.optiflow.crm.client.dto.ClientTimelineEventDto;
import com.optiflow.crm.client.mapper.ClientMapper;
import com.optiflow.crm.client.model.Client;
import com.optiflow.crm.client.model.ClientService;
import com.optiflow.crm.client.specification.ClientSpecification;
import com.optiflow.crm.followup.model.FollowUp;
import com.optiflow.crm.followup.model.FollowUpStatus;
import com.optiflow.crm.followup.FollowUpRepository;
import com.optiflow.crm.order.OrderRepository;
import com.optiflow.crm.order.model.Order;
import com.optiflow.crm.order.model.OrderStatus;
import com.optiflow.crm.quote.QuoteRepository;
import com.optiflow.crm.quote.model.Quote;
import com.optiflow.crm.quote.model.QuoteStatus;
import com.optiflow.crm.shared.exception.ClientAlreadyExistsException;
import com.optiflow.crm.shared.exception.ClientNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Objects;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

// Implementacion del ClientService
@Service
public class ClientServiceImpl implements ClientService {

    // Inyeccion de repositorios y mapper
    private final ClientRepository clientRepository;
    private final ClientMapper clientMapper;
    private final QuoteRepository quoteRepository;
    private final OrderRepository orderRepository;
    private final FollowUpRepository followUpRepository;

    public ClientServiceImpl(ClientRepository clientRepository,
                             ClientMapper clientMapper,
                             QuoteRepository quoteRepository,
                             OrderRepository orderRepository,
                             FollowUpRepository followUpRepository) {
        this.clientRepository = clientRepository;
        this.clientMapper = clientMapper;
        this.quoteRepository = quoteRepository;
        this.orderRepository = orderRepository;
        this.followUpRepository = followUpRepository;
    }

    // metodo de busqueda paginada
    @Override
    public Page<Client> findAll(Pageable pageable) {
        return clientRepository.findByActiveTrue(pageable);
    }

    // metodo de busqueda personalizada
    @Override
    public Page<Client> search(ClientSearchDto searchDto) {

        Specification<Client> specification = ClientSpecification.filterBy(searchDto);

        int page = searchDto.getPage() != null ? searchDto.getPage() : 0;

        int size = searchDto.getSize() != null ? searchDto.getSize() : 10;


        String requestedSort = searchDto.getSortBy();

        String sortBy = switch (
                requestedSort != null ? requestedSort : ""
                ) {
            case "name", "surname", "lastSurname",
                 "dni", "email", "phoneNumber",
                 "city", "createdAt" -> requestedSort;

            default -> "surname";
        };

        Sort.Direction direction = "desc".equalsIgnoreCase(searchDto.getSortDirection())
                        ? Sort.Direction.DESC : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        return clientRepository.findAll(specification, pageable);
    }

    // Metodo de busqueda por id
    @Override
    public Client findById(Long id) {
        return clientRepository.findById(id).orElseThrow(() -> new ClientNotFoundException(id));
    }

    // Metodo de creacion de cliente
    @Override
    @Transactional
    public Client create(ClientDto dto) {

        Optional<Client> existing = clientRepository.findByDniIgnoreCase(dto.getDni());

        if (existing.isPresent()) {

            Client client = existing.get();

            if (Boolean.FALSE.equals(client.getActive())) {
                client.setActive(true);
                clientMapper.updateEntity(dto, client);

                return clientRepository.save(client);
            }

            throw new ClientAlreadyExistsException(dto.getDni());
        }

        Client client = clientMapper.toEntity(dto);

        client.setActive(true);

        return clientRepository.save(client);
    }

    // Metodo de guardar cliente
    @Override
    public Client save(Client client) {
        return clientRepository.save(client);
    }

    // Metodo de actualizar cliente
    @Override
    public Client update(Long id, Client client) {

        Client existingClient = findById(id);

        existingClient.setName(client.getName());
        existingClient.setSurname(client.getSurname());
        existingClient.setLastSurname(client.getLastSurname());
        existingClient.setDni(client.getDni());
        existingClient.setPhoneNumber(client.getPhoneNumber());
        existingClient.setEmail(client.getEmail());
        existingClient.setBirthDate(client.getBirthDate());
        existingClient.setAddress(client.getAddress());
        existingClient.setCity(client.getCity());
        existingClient.setPostalCode(client.getPostalCode());
        existingClient.setNotes(client.getNotes());

        return clientRepository.save(existingClient);
    }

    // Metodo para el timeline de pedidos y presupuestos

    @Override
    @Transactional(readOnly = true)
    public List<ClientTimelineEventDto> getTimeline(Long clientId) {

        // Validar la existencia del cliente
        findById(clientId);

        List<ClientTimelineEventDto> timeline = new ArrayList<>();

        // PRESUPUESTOS
        List<Quote> quotes =
                quoteRepository.findByClientIdAndActiveTrue(clientId);

        for (Quote quote : quotes) {

            timeline.add(new ClientTimelineEventDto(
                    "QUOTE-" + quote.getId(),
                    "QUOTE",
                    quote.getId(),
                    quote.getCreatedAt(),
                    "Presupuesto " + quote.getQuoteNumber(),
                    quote.getNotes(),
                    quote.getStatus().name(),
                    quote.getTotal()
            ));
        }

        // PEDIDOS
        List<Order> orders =
                orderRepository.findByClientIdAndActiveTrue(clientId);

        for (Order order : orders) {

            timeline.add(new ClientTimelineEventDto(
                    "ORDER-" + order.getId(),
                    "ORDER",
                    order.getId(),
                    order.getCreatedAt(),
                    "Pedido " + order.getOrderNumber(),
                    order.getNotes(),
                    order.getStatus().name(),
                    order.getTotal()
            ));
        }

        // SEGUIMIENTOS
        List<FollowUp> followUps =
                followUpRepository.findByQuoteClientIdAndActiveTrue(clientId);

        for (FollowUp followUp : followUps) {

            timeline.add(new ClientTimelineEventDto(
                    "FOLLOW_UP-" + followUp.getId(),
                    "FOLLOW_UP",
                    followUp.getId(),
                    followUp.getCreatedAt(),
                    "Seguimiento " + followUp.getType().name(),
                    followUp.getResult() != null
                            ? followUp.getResult()
                            : followUp.getNotes(),
                    followUp.getStatus().name(),
                    null
            ));
        }

        // Orden cronológico: más recientes primero
        timeline.sort(
                Comparator.comparing(
                        ClientTimelineEventDto::date
                ).reversed()
        );

        return timeline;
    }


    // Metodo de borrado de cliente. Solo desactiva cuenta.
    @Override
    public void delete(Long id) {

        Client client = findById(id);

        client.setActive(false);

        clientRepository.save(client);
    }


    @Override
    @Transactional(readOnly = true)
    public ClientStatsDto getStats(Long clientId) {

        // 1. Verificar que el cliente existe.
        findById(clientId);

        // 2. Recuperar operaciones activas.
        List<Quote> quotes =
                quoteRepository.findByClientIdAndActiveTrue(clientId);

        List<Order> orders =
                orderRepository.findByClientIdAndActiveTrue(clientId);

        List<FollowUp> followUps =
                followUpRepository.findByQuoteClientIdAndActiveTrue(clientId);

        ClientStatsDto stats = new ClientStatsDto();

        // 3. Número total de operaciones.
        stats.setQuotesCount(quotes.size());
        stats.setOrdersCount(orders.size());

        // 4. Presupuestos convertidos.
        long convertedQuotes = quotes.stream()
                .filter(quote ->
                        quote.getStatus() == QuoteStatus.CONVERTED)
                .count();

        stats.setConvertedQuotesCount(convertedQuotes);

        // 5. Presupuestos abiertos.
        long openQuotes = quotes.stream()
                .filter(quote -> {
                    QuoteStatus status = quote.getStatus();

                    return status == QuoteStatus.DRAFT
                            || status == QuoteStatus.SENT
                            || status == QuoteStatus.ACCEPTED;
                })
                .count();

        stats.setOpenQuotesCount(openQuotes);

        // 6. Pedidos pendientes.
        long pendingOrders = orders.stream()
                .filter(order -> {
                    OrderStatus status = order.getStatus();

                    return status != OrderStatus.DELIVERED
                            && status != OrderStatus.CANCELLED;
                })
                .count();

        stats.setPendingOrdersCount(pendingOrders);

        // 7. Seguimientos pendientes.
        long pendingFollowUps = followUps.stream()
                .filter(followUp ->
                        followUp.getStatus() == FollowUpStatus.PENDING)
                .count();

        stats.setPendingFollowUps(pendingFollowUps);

        // 8. Pedidos entregados para calcular ventas.
        List<Order> deliveredOrders = orders.stream()
                .filter(order ->
                        order.getStatus() == OrderStatus.DELIVERED)
                .toList();

        BigDecimal totalSpent = deliveredOrders.stream()
                .map(Order::getTotal)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        stats.setTotalSpent(
                totalSpent.setScale(2, RoundingMode.HALF_UP)
        );

        // 9. Ticket medio.
        BigDecimal averageTicket = BigDecimal.ZERO;

        if (!deliveredOrders.isEmpty()) {
            averageTicket = totalSpent.divide(
                    BigDecimal.valueOf(deliveredOrders.size()),
                    2,
                    RoundingMode.HALF_UP
            );
        }

        stats.setAverageTicket(averageTicket);

        // 10. Tasa de conversión: valor entre 0 y 100.
        BigDecimal conversionRate = BigDecimal.ZERO;

        if (!quotes.isEmpty()) {
            conversionRate = BigDecimal.valueOf(convertedQuotes)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(
                            BigDecimal.valueOf(quotes.size()),
                            2,
                            RoundingMode.HALF_UP
                    );
        }

        stats.setConversionRate(conversionRate);

        // 11. Fecha del último pedido.
        LocalDate lastOrderDate = orders.stream()
                .map(Order::getOrderDate)
                .filter(Objects::nonNull)
                .max(LocalDate::compareTo)
                .orElse(null);

        stats.setLastOrderDate(lastOrderDate);

        // 12. Última actividad comercial.

        LocalDateTime lastQuoteActivity = quotes.stream()
                .map(q -> q.getUpdatedAt() != null
                        ? q.getUpdatedAt()
                        : q.getCreatedAt())
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        LocalDateTime lastOrderActivity = orders.stream()
                .map(o -> o.getUpdatedAt() != null
                        ? o.getUpdatedAt()
                        : o.getCreatedAt())
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        LocalDateTime lastFollowUpActivity = followUps.stream()
                .map(f -> f.getUpdatedAt() != null
                        ? f.getUpdatedAt()
                        : f.getCreatedAt())
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        LocalDateTime lastActivity =
                java.util.stream.Stream.of(
                                lastQuoteActivity,
                                lastOrderActivity,
                                lastFollowUpActivity
                        )
                        .filter(Objects::nonNull)
                        .max(LocalDateTime::compareTo)
                        .orElse(null);

        stats.setLastActivity(lastActivity);


        return stats;
    }

}
