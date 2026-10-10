package com.optiflow.crm.order;

import com.optiflow.crm.client.ClientRepository;
import com.optiflow.crm.client.model.Client;

import com.optiflow.crm.order.domain.OrderDomainService;
import com.optiflow.crm.order.dto.OrderDto;
import com.optiflow.crm.order.dto.OrderSearchDto;
import com.optiflow.crm.order.mapper.OrderMapper;
import com.optiflow.crm.order.model.Order;
import com.optiflow.crm.order.model.OrderService;
import com.optiflow.crm.order.specification.OrderSpecification;

import com.optiflow.crm.quote.QuoteRepository;
import com.optiflow.crm.quote.model.Quote;

import com.optiflow.crm.shared.exception.ClientNotFoundException;
import com.optiflow.crm.shared.exception.OrderNotFoundException;
import com.optiflow.crm.shared.exception.QuoteAlreadyConvertedException;
import com.optiflow.crm.shared.exception.QuoteNotFoundException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import org.springframework.data.jpa.domain.Specification;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ClientRepository clientRepository;
    private final QuoteRepository quoteRepository;
    private final OrderMapper orderMapper;
    private final OrderDomainService orderDomainService;

    public OrderServiceImpl(
            OrderRepository orderRepository,
            ClientRepository clientRepository,
            QuoteRepository quoteRepository,
            OrderMapper orderMapper,
            OrderDomainService orderDomainService
    ) {
        this.orderRepository = orderRepository;
        this.clientRepository = clientRepository;
        this.quoteRepository = quoteRepository;
        this.orderMapper = orderMapper;
        this.orderDomainService = orderDomainService;
    }


    @Override
    public Page<Order> findAll(Pageable pageable) {

        return orderRepository.findByActiveTrue(pageable);
    }


    @Override
    public Page<Order> search(OrderSearchDto searchDto) {

        Specification<Order> specification = OrderSpecification.filterBy(searchDto);

        int page = searchDto.getPage() != null ? searchDto.getPage() : 0;

        int size = searchDto.getSize() != null ? searchDto.getSize() : 10;

        String sortBy = searchDto.getSortBy() != null
                        ? searchDto.getSortBy() : "orderDate";

        Sort.Direction direction = "asc".equalsIgnoreCase(searchDto.getSortDirection())
                        ? Sort.Direction.ASC : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        return orderRepository.findAll(specification, pageable);
    }


    @Override
    public Page<Order> findByClientId(Long clientId, Pageable pageable) {

        return orderRepository.findByClientIdAndActiveTrue(clientId, pageable);
    }


    @Override
    public Order findById(Long id) {

        return orderRepository.findById(id).orElseThrow(() -> new OrderNotFoundException(id));
    }


    @Override
    @Transactional
    public Order create(OrderDto orderDto) {

        Client client = clientRepository.findById(orderDto.getClientId()).orElseThrow(() ->
                                new ClientNotFoundException(orderDto.getClientId()));

        Order order = orderMapper.toEntity(orderDto);

        order.setClient(client);

        /*
         * Si existe quoteId, únicamente relacionamos
         * el pedido con el presupuesto.
         *
         * Para una conversión completa Quote -> Order
         * debe utilizarse createFromQuote().
         */
        if (orderDto.getQuoteId() != null) {

            Quote quote = quoteRepository.findById(orderDto.getQuoteId()).orElseThrow(() ->
                                    new QuoteNotFoundException(orderDto.getQuoteId()));

            if (orderRepository.existsByQuoteId(quote.getId())) {
                throw new QuoteAlreadyConvertedException(quote.getId());
            }

            order.setQuote(quote);
        }

        order.setOrderNumber(generateOrderNumber());

        orderDomainService.initializeOrder(order);
        orderDomainService.calculateTotals(order);

        return orderRepository.save(order);
    }


    @Override
    @Transactional
    public Order createFromQuote(Long quoteId) {

        Quote quote = quoteRepository.findById(quoteId).orElseThrow(() ->
                                new QuoteNotFoundException(quoteId));

        orderDomainService.validateQuoteConversion(quote);

        if (orderRepository.existsByQuoteId(quoteId)) {

            throw new QuoteAlreadyConvertedException(quoteId);
        }

        Order order = orderDomainService.createFromQuote(quote);

        order.setOrderNumber(generateOrderNumber());

        Order savedOrder = orderRepository.save(order);

        quote.markAsConverted();

        quoteRepository.save(quote);

        return savedOrder;
    }


    @Override
    @Transactional
    public Order update(Long id, OrderDto orderDto) {

        Order existingOrder = findById(id);

        orderMapper.updateEntity(orderDto, existingOrder);

        if (orderDto.getClientId() != null) {

            Client client = clientRepository.findById(orderDto.getClientId())
                            .orElseThrow(() ->
                                    new ClientNotFoundException(orderDto.getClientId()));

            existingOrder.setClient(client);
        }

        orderDomainService.initializeOrder(existingOrder);

        orderDomainService.calculateTotals(existingOrder);

        return orderRepository.save(existingOrder);
    }


    @Override
    @Transactional
    public void delete(Long id) {

        Order order = findById(id);

        order.setActive(false);

        orderRepository.save(order);
    }


    private String generateOrderNumber() {

        int year = LocalDate.now().getYear();

        String prefix = "O-" + year + "-";

        Optional<Order> lastOrder = orderRepository
                        .findTopByOrderNumberStartingWithOrderByOrderNumberDesc(prefix);

        int nextNumber = 1;

        if (lastOrder.isPresent()) {

            String lastOrderNumber = lastOrder.get().getOrderNumber();

            String sequence = lastOrderNumber.substring(prefix.length());

            nextNumber = Integer.parseInt(sequence) + 1;
        }

        return prefix + String.format("%06d", nextNumber);
    }
}