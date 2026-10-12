package com.optiflow.crm.order;

import com.optiflow.crm.client.ClientRepository;
import com.optiflow.crm.client.model.Client;

import com.optiflow.crm.inventory.InventoryRepository;
import com.optiflow.crm.inventory.model.InventoryItem;
import com.optiflow.crm.inventory.model.InventoryService;
import com.optiflow.crm.order.domain.OrderDomainService;
import com.optiflow.crm.order.dto.OrderDto;
import com.optiflow.crm.order.dto.OrderItemDto;
import com.optiflow.crm.order.dto.OrderSearchDto;
import com.optiflow.crm.order.mapper.OrderMapper;
import com.optiflow.crm.order.model.Order;
import com.optiflow.crm.order.model.OrderItem;
import com.optiflow.crm.order.model.OrderService;
import com.optiflow.crm.order.model.OrderStatus;
import com.optiflow.crm.order.specification.OrderSpecification;

import com.optiflow.crm.quote.QuoteRepository;
import com.optiflow.crm.quote.model.Quote;

import com.optiflow.crm.shared.exception.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import org.springframework.data.jpa.domain.Specification;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

// Implementacion del service de Pedidos
@Service
public class OrderServiceImpl implements OrderService {

    // Inyeccion de repositories, services y mappers
    private final OrderRepository orderRepository;
    private final ClientRepository clientRepository;
    private final QuoteRepository quoteRepository;
    private final InventoryRepository inventoryRepository;
    private final OrderMapper orderMapper;
    private final OrderDomainService orderDomainService;
    private final InventoryService inventoryService;

    public OrderServiceImpl(
            OrderRepository orderRepository,
            ClientRepository clientRepository,
            QuoteRepository quoteRepository,
            InventoryRepository inventoryRepository,
            OrderMapper orderMapper,
            OrderDomainService orderDomainService,
            InventoryService inventoryService
    ) {
        this.orderRepository = orderRepository;
        this.clientRepository = clientRepository;
        this.quoteRepository = quoteRepository;
        this.inventoryRepository = inventoryRepository;
        this.orderMapper = orderMapper;
        this.orderDomainService = orderDomainService;
        this.inventoryService = inventoryService;
    }

    // Busqueda paginada de pedidos
    @Override
    public Page<Order> findAll(Pageable pageable) {

        return orderRepository.findByActiveTrue(pageable);
    }

    // Busqueda avanzada de pedidos
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

    // Buscar por el Id del cliente
    @Override
    public Page<Order> findByClientId(Long clientId, Pageable pageable) {

        return orderRepository.findByClientIdAndActiveTrue(clientId, pageable);
    }

    // Buscar pot Id
    @Override
    public Order findById(Long id) {

        return orderRepository.findById(id).orElseThrow(() -> new OrderNotFoundException(id));
    }

    // Crear un pedido desde cero
    @Override
    @Transactional
    public Order create(OrderDto orderDto) {

        Client client = clientRepository.findById(orderDto.getClientId()).orElseThrow(() ->
                        new ClientNotFoundException(orderDto.getClientId()));

        Order order = orderMapper.toEntity(orderDto);

        order.setClient(client);
        order.setOrderNumber(generateOrderNumber());
        orderDomainService.initializeOrder(order);

        // Construcción de OrderItems desde inventario
        if (orderDto.getItems() != null) {

            for (OrderItemDto itemDto : orderDto.getItems()) {

                InventoryItem inventoryItem = inventoryRepository
                                .findById(itemDto.getInventoryItemId())
                                .orElseThrow(() ->
                                        new InventoryItemNotFoundException(
                                                itemDto.getInventoryItemId()));

                if (Boolean.FALSE.equals(inventoryItem.getActive())) {

                    throw new InventoryItemNotFoundException(inventoryItem.getId());
                }

                OrderItem orderItem = new OrderItem();

                orderItem.setOrder(order);
                orderItem.setInventoryItem(inventoryItem);
                orderItem.setDescription(inventoryItem.getName());
                orderItem.setQuantity(itemDto.getQuantity());
                orderItem.setUnitPrice(inventoryItem.getSalePrice());
                orderItem.setTaxRate(inventoryItem.getTaxRate());
                orderItem.setDiscount(itemDto.getDiscount() != null
                                ? itemDto.getDiscount() : BigDecimal.ZERO);

                orderDomainService.calculateItem(orderItem);

                order.getItems().add(orderItem);
            }
        }

        orderDomainService.calculateTotals(order);

        return orderRepository.save(order);
    }

    // Crear un nuevo pedido desde presupuesto
    @Override
    @Transactional
    public Order createFromQuote(Long quoteId) {

        Quote quote = quoteRepository.findById(quoteId)
                .orElseThrow(() -> new QuoteNotFoundException(quoteId));

        orderDomainService.validateQuoteConversion(quote);

        if (orderRepository.existsByQuoteId(quoteId)) {
            throw new QuoteAlreadyConvertedException(quoteId);
        }

        Order order = orderDomainService.createFromQuote(quote);

        order.setOrderNumber(generateOrderNumber());

        orderDomainService.calculateTotals(order);

        Order savedOrder = orderRepository.save(order);

        quote.markAsConverted();

        quoteRepository.save(quote);

        return savedOrder;
    }

    // Actualizar un pedido
    @Override
    @Transactional
    public Order update(Long id, OrderDto orderDto) {

        Order existingOrder = findById(id);

        orderMapper.updateEntity(orderDto, existingOrder);

        if (orderDto.getClientId() != null) {

            Client client = clientRepository.findById(orderDto.getClientId()).orElseThrow(() ->
                                    new ClientNotFoundException(orderDto.getClientId()));

            existingOrder.setClient(client);
        }

        orderDomainService.initializeOrder(existingOrder);
        orderDomainService.calculateTotals(existingOrder);

        return orderRepository.save(existingOrder);
    }

    // Borrar un pedido desactivandolo
    @Override
    @Transactional
    public void delete(Long id) {

        Order order = findById(id);

        order.setActive(false);

        orderRepository.save(order);
    }

    // Generar Numero de pedido
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

    // Confirmar un pedido y reservar stock
    @Override
    @Transactional
    public Order confirm(Long id) {

        Order order = findById(id);

        orderDomainService.validateCanConfirm(order);

        for (OrderItem orderItem : order.getItems()) {

            InventoryItem inventoryItem = orderItem.getInventoryItem();
            inventoryService.decreaseStock(inventoryItem.getId(), orderItem.getQuantity());
        }

        order.setStatus(OrderStatus.CONFIRMED);

        return orderRepository.save(order);
    }

    // Cancelar un pedido y subir stock
    @Override
    @Transactional
    public Order cancel(Long id) {

        Order order = findById(id);

        orderDomainService.validateCanCancel(order);

        boolean stockWasDiscounted = order.getStatus() == OrderStatus.CONFIRMED
                        || order.getStatus() == OrderStatus.IN_PROGRESS
                        || order.getStatus() == OrderStatus.READY;

        if (stockWasDiscounted) {

            for (OrderItem orderItem : order.getItems()) {

                InventoryItem inventoryItem = orderItem.getInventoryItem();
                inventoryService.increaseStock(inventoryItem.getId(), orderItem.getQuantity());
            }
        }

        order.setStatus(OrderStatus.CANCELLED);

        return orderRepository.save(order);
    }

}