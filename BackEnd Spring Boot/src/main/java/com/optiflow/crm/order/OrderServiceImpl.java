package com.optiflow.crm.order;

import com.optiflow.crm.auth.CurrentUserService;
import com.optiflow.crm.client.ClientRepository;
import com.optiflow.crm.client.model.Client;

import com.optiflow.crm.inventory.InventoryRepository;
import com.optiflow.crm.inventory.model.InventoryItem;
import com.optiflow.crm.inventory.model.InventoryService;
import com.optiflow.crm.inventory.movement.StockMovementService;
import com.optiflow.crm.inventory.movement.model.StockMovementType;
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

import com.optiflow.crm.user.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import org.springframework.data.jpa.domain.Specification;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

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
    private final CurrentUserService currentUserService;
    private final StockMovementService stockMovementService;

    public OrderServiceImpl(
            OrderRepository orderRepository,
            ClientRepository clientRepository,
            QuoteRepository quoteRepository,
            InventoryRepository inventoryRepository,
            OrderMapper orderMapper,
            OrderDomainService orderDomainService,
            InventoryService inventoryService,
            CurrentUserService currentUserService,
            StockMovementService stockMovementService
    ) {
        this.orderRepository = orderRepository;
        this.clientRepository = clientRepository;
        this.quoteRepository = quoteRepository;
        this.inventoryRepository = inventoryRepository;
        this.orderMapper = orderMapper;
        this.orderDomainService = orderDomainService;
        this.inventoryService = inventoryService;
        this.currentUserService = currentUserService;
        this.stockMovementService = stockMovementService;
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


        String requestedSort = searchDto.getSortBy();
        String sortBy = switch (requestedSort != null ? requestedSort : "") {
            case "orderNumber",
                 "orderDate",
                 "expectedDeliveryDate",
                 "deliveryDate",
                 "status",
                 "total",
                 "createdAt" -> requestedSort;

            default -> "orderDate";
        };

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

    @Override
    @Transactional(readOnly = true)
    public OrderDto findDetailById(Long id) {

        Order order = findById(id);

        return orderMapper.toDto(order);
    }

    // Crear un pedido desde cero
    @Override
    @Transactional
    public Order create(OrderDto orderDto) {

        Client client = clientRepository.findById(orderDto.getClientId()).orElseThrow(() ->
                        new ClientNotFoundException(orderDto.getClientId()));

        User seller = currentUserService.getCurrentUser();
        Order order = orderMapper.toEntity(orderDto);
        order.setStatus(OrderStatus.PENDING);

        order.setClient(client);
        order.setSeller(seller);
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

                    throw new InventoryItemNotAvailableException(inventoryItem.getId());
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
    private Order findByIdForUpdate(Long id) {

        return orderRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }


    @Override
    @Transactional
    public Order update(Long id, OrderDto dto) {

        Order order = findByIdForUpdate(id);

        orderDomainService.validateCanEdit(order);

        if (dto.getClientId() == null || !dto.getClientId().equals(
                        order.getClient().getId())) {
            throw new InvalidOrderStatusException(
                    "No se puede cambiar el cliente del pedido"
            );
        }

        if (dto.getStatus() != null && dto.getStatus() != OrderStatus.PENDING) {
            throw new InvalidOrderStatusException(
                    "El estado solo puede cambiar mediante el workflow"
            );
        }

        if (dto.getItems() != null) {
            throw new InvalidOrderStatusException(
                    "Las líneas del pedido no se modifican " +
                            "desde este endpoint"
            );
        }

        if (dto.getOrderDate() != null && !dto.getOrderDate().equals(order.getOrderDate())) {
            throw new InvalidOrderStatusException(
                    "La fecha original del pedido no se puede modificar"
            );
        }

        if (dto.getDeliveryDate() != null) {
            throw new InvalidOrderStatusException(
                    "La fecha de entrega la establece el workflow"
            );
        }

        if (dto.getQuoteId() != null && (order.getQuote() == null ||
                        !dto.getQuoteId().equals(order.getQuote().getId()))) {
            throw new InvalidOrderStatusException(
                    "No se puede modificar el presupuesto de origen"
            );
        }

        LocalDate expected = dto.getExpectedDeliveryDate();

        if (expected != null && expected.isBefore(order.getOrderDate())) {
            throw new IllegalArgumentException(
                    "La entrega prevista no puede ser anterior " +
                            "a la fecha del pedido"
            );
        }

        order.setExpectedDeliveryDate(expected);
        order.setNotes(dto.getNotes());

        return orderRepository.save(order);
    }


    // Borrar un pedido desactivandolo
    @Override
    @Transactional
    public void delete(Long id) {

        Order order = findByIdForUpdate(id);

        orderDomainService.validateCanDelete(order);

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

        Order order = findByIdForUpdate(id);

        orderDomainService.validateCanConfirm(order);

        // Consolidamos cantidades del mismo artículo
        // y adquirimos los bloqueos en orden estable.
        Map<Long, Integer> quantities = order.getItems()
                .stream()
                .collect(Collectors.toMap(
                        item -> item.getInventoryItem().getId(),
                        OrderItem::getQuantity,
                        Integer::sum,
                        TreeMap::new
                ));

        for (var entry : quantities.entrySet()) {
            stockMovementService.applyOrderMovement(
                    entry.getKey(), order.getId(), entry.getValue(), StockMovementType.EXIT);
        }

        order.setStatus(OrderStatus.CONFIRMED);

        return orderRepository.save(order);
    }

    @Override
    @Transactional
    public Order cancel(Long id) {

        Order order = findByIdForUpdate(id);

        orderDomainService.validateCanCancel(order);

        boolean stockWasDiscounted =
                order.getStatus() == OrderStatus.CONFIRMED ||
                        order.getStatus() == OrderStatus.IN_PROGRESS ||
                        order.getStatus() == OrderStatus.READY;

        if (stockWasDiscounted) {

            Map<Long, Integer> quantities = order.getItems()
                    .stream()
                    .collect(Collectors.toMap(
                            item -> item.getInventoryItem().getId(),
                            OrderItem::getQuantity,
                            Integer::sum,
                            TreeMap::new
                    ));

            for (var entry : quantities.entrySet()) {
                stockMovementService.applyOrderMovement(
                        entry.getKey(), order.getId(), entry.getValue(), StockMovementType.ENTRY);
            }
        }

        order.setStatus(OrderStatus.CANCELLED);

        return orderRepository.save(order);
    }


    @Override
    @Transactional
    public OrderDto confirmDetail(Long id) {

        Order order = confirm(id);

        return orderMapper.toDto(order);
    }


    @Override
    @Transactional
    public OrderDto startPreparationDetail(Long id) {

        Order order = findByIdForUpdate(id);

        orderDomainService.startPreparation(order);

        return orderMapper.toDto(orderRepository.save(order));
    }

    @Override
    @Transactional
    public OrderDto markReadyDetail(Long id) {

        Order order = findByIdForUpdate(id);

        orderDomainService.markReady(order);

        return orderMapper.toDto(orderRepository.save(order));
    }

    @Override
    @Transactional
    public OrderDto deliverDetail(Long id) {

        Order order = findByIdForUpdate(id);

        orderDomainService.deliver(order);

        return orderMapper.toDto(orderRepository.save(order));
    }

    @Override
    @Transactional
    public OrderDto cancelDetail(Long id) {

        Order order = cancel(id);

        return orderMapper.toDto(order);
    }

}