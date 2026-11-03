package com.optiflow.crm.client;

import com.optiflow.crm.client.dto.ClientDto;
import com.optiflow.crm.client.dto.ClientSearchDto;
import com.optiflow.crm.client.dto.ClientTimeLineItemDto;
import com.optiflow.crm.client.dto.ClientTimelineEventDto;
import com.optiflow.crm.client.mapper.ClientMapper;
import com.optiflow.crm.client.model.Client;
import com.optiflow.crm.client.model.ClientService;
import com.optiflow.crm.client.specification.ClientSpecification;
import com.optiflow.crm.followup.model.FollowUp;
import com.optiflow.crm.followup.FollowUpRepository;
import com.optiflow.crm.order.OrderRepository;
import com.optiflow.crm.order.model.Order;
import com.optiflow.crm.quote.QuoteRepository;
import com.optiflow.crm.quote.model.Quote;
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

        String sortBy = searchDto.getSortBy() != null ? searchDto.getSortBy() : "firstSurname";

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

        // Comprobar que el cliente existe
        findById(clientId);

        List<ClientTimelineEventDto> timeline = new ArrayList<>();

        // Recuperar presupuestos del cliente
        List<Quote> quotes =
                quoteRepository.findByClientIdAndActiveTrue(clientId);

        // Recuperar pedidos del cliente
        List<Order> orders =
                orderRepository.findByClientIdAndActiveTrue(clientId);

        // Recuperar seguimientos asociados a sus presupuestos
        List<FollowUp> followUps =
                followUpRepository.findByQuoteClientIdAndActiveTrue(clientId);

        // Transformar las entidades en eventos del timeline
        // timeline.add(...) para cada presupuesto
        // timeline.add(...) para cada pedido
        // timeline.add(...) para cada seguimiento

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
}
