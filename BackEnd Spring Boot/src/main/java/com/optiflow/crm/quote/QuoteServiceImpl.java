package com.optiflow.crm.quote;

import com.optiflow.crm.auth.CurrentUserService;
import com.optiflow.crm.client.ClientRepository;
import com.optiflow.crm.client.model.Client;
import com.optiflow.crm.inventory.InventoryRepository;
import com.optiflow.crm.inventory.model.InventoryItem;
import com.optiflow.crm.quote.domain.QuoteDomainService;
import com.optiflow.crm.quote.dto.QuoteDto;
import com.optiflow.crm.quote.dto.QuoteItemDto;
import com.optiflow.crm.quote.dto.QuoteSearchDto;
import com.optiflow.crm.quote.mapper.QuoteMapper;
import com.optiflow.crm.quote.model.Quote;
import com.optiflow.crm.quote.model.QuoteItem;
import com.optiflow.crm.quote.model.QuoteService;

import com.optiflow.crm.quote.specification.QuoteSpecification;
import com.optiflow.crm.shared.exception.ClientNotFoundException;
import com.optiflow.crm.shared.exception.InventoryItemNotFoundException;
import com.optiflow.crm.shared.exception.QuoteItemNotFoundException;
import com.optiflow.crm.shared.exception.QuoteNotFoundException;
import com.optiflow.crm.user.model.User;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

// Service de presupuestos con la logica ppal
@Service
public class QuoteServiceImpl implements QuoteService {

    // Inyeccion de repositories, services y mapper
    private final QuoteRepository quoteRepository;
    private final ClientRepository clientRepository;
    private final QuoteMapper quoteMapper;
    private final QuoteDomainService quoteDomainService;
    private final CurrentUserService currentUserService;
    private final InventoryRepository inventoryRepository;

    public QuoteServiceImpl(
            QuoteRepository quoteRepository,
            ClientRepository clientRepository,
            QuoteMapper quoteMapper,
            QuoteDomainService quoteDomainService,
            CurrentUserService currentUserService,
            InventoryRepository inventoryRepository
    ) {
        this.quoteRepository = quoteRepository;
        this.clientRepository = clientRepository;
        this.quoteMapper = quoteMapper;
        this.quoteDomainService = quoteDomainService;
        this.currentUserService = currentUserService;
        this.inventoryRepository = inventoryRepository;
    }

    // Metodo de busqueda paginada de presupuestos
    @Override
    public Page<Quote> findAll(Pageable pageable) {

        return quoteRepository.findByActiveTrue(pageable);
    }

    // Buscar por id
    @Override
    public Quote findById(Long id) {

        return quoteRepository.findById(id).orElseThrow(() ->
                        new QuoteNotFoundException(id));
    }

    // Busqueda avanzada de presupuestos
    @Override
    public Page<Quote> search(QuoteSearchDto searchDto) {

        Specification<Quote> specification = QuoteSpecification.filterBy(searchDto);

        int page = searchDto.getPage() != null
                ? searchDto.getPage() : 0;

        int size = searchDto.getSize() != null
                ? searchDto.getSize() : 10;

        String sortBy = searchDto.getSortBy() != null
                ? searchDto.getSortBy() : "quoteDate";

        Sort.Direction direction =
                "asc".equalsIgnoreCase(searchDto.getSortDirection())
                        ? Sort.Direction.ASC : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        return quoteRepository.findAll(specification, pageable);
    }

    // Buscar por cliente
    @Override
    public Page<Quote> findByClientId(Long clientId, Pageable pageable) {

        return quoteRepository.findByClientIdAndActiveTrue(clientId, pageable);
    }

    // Crear un presupuesto
    @Override
    @Transactional
    public Quote create(QuoteDto quoteDto) {

        Client client = clientRepository.findById(quoteDto.getClientId())
                .orElseThrow(() -> new ClientNotFoundException(quoteDto.getClientId()));

        Quote quote = quoteMapper.toEntity(quoteDto);
        User seller = currentUserService.getCurrentUser();

        quote.setSeller(seller);
        quote.setClient(client);
        quote.setQuoteNumber(generateQuoteNumber());

        quoteDomainService.initializeQuote(quote);

        if (quoteDto.getItems() != null) {

            for (QuoteItemDto itemDto : quoteDto.getItems()) {

                InventoryItem inventoryItem =
                        inventoryRepository.findById(itemDto.getInventoryItemId()).orElseThrow(() ->
                                        new InventoryItemNotFoundException(itemDto.getInventoryItemId()));

                if (Boolean.FALSE.equals(inventoryItem.getActive())) {
                    throw new InventoryItemNotFoundException(inventoryItem.getId());
                }

                QuoteItem quoteItem = new QuoteItem();

                quoteItem.setQuote(quote);
                quoteItem.setInventoryItem(inventoryItem);
                quoteItem.setDescription(inventoryItem.getName());
                quoteItem.setQuantity(itemDto.getQuantity());
                quoteItem.setUnitPrice(inventoryItem.getSalePrice());
                quoteItem.setTaxRate(inventoryItem.getTaxRate());
                quoteItem.setDiscount(itemDto.getDiscount() != null
                                ? itemDto.getDiscount() : BigDecimal.ZERO);

                quoteDomainService.calculateItem(quoteItem);
                quote.getItems().add(quoteItem);
            }
        }

        quoteDomainService.calculateTotals(quote);

        return quoteRepository.save(quote);
    }

    // Actualizar un presupuesto
    @Override
    @Transactional
    public Quote update(Long id, QuoteDto quoteDto) {

        Quote existingQuote = findById(id);

        // Actualizamos campos generales del presupuesto
        quoteMapper.updateEntity(quoteDto, existingQuote);

        // Cambio de cliente, si se recibe
        if (quoteDto.getClientId() != null) {

            Client client = clientRepository.findById(quoteDto.getClientId())
                    .orElseThrow(() -> new ClientNotFoundException(quoteDto.getClientId()));

            existingQuote.setClient(client);
        }

        // Sincronizamos los items
        if (quoteDto.getItems() != null) {
            updateQuoteItems(existingQuote, quoteDto.getItems());
        }

        // Recalculamos el presupuesto completo
        quoteDomainService.calculateTotals(existingQuote);

        return quoteRepository.save(existingQuote);
    }

    // Borrado de un presupuesto desactivandolo
    @Override
    public void delete(Long id) {

        Quote quote = findById(id);

        quote.setActive(false);

        quoteRepository.save(quote);
    }

    // Generar un numero de presupuesto
    private String generateQuoteNumber() {

        int year = LocalDate.now().getYear();

        String prefix = "Q-" + year + "-";

        Optional<Quote> lastQuote = quoteRepository
                        .findTopByQuoteNumberStartingWithOrderByQuoteNumberDesc(prefix);

        int nextNumber = 1;

        if (lastQuote.isPresent()) {

            String lastQuoteNumber = lastQuote.get().getQuoteNumber();
            String sequence = lastQuoteNumber.substring(prefix.length());
            nextNumber = Integer.parseInt(sequence) + 1;
        }

        return prefix + String.format("%06d", nextNumber);
    }

    // Actualizar items del presupuesto
    private void updateQuoteItems(Quote quote, List<QuoteItemDto> itemDtos) {

        quote.getItems().removeIf(existingItem ->
                existingItem.getId() != null && itemDtos.stream().noneMatch(dto ->
                                dto.getId() != null && dto.getId().equals(existingItem.getId())));

        for (QuoteItemDto itemDto : itemDtos) {

            QuoteItem quoteItem;
            boolean isNewItem = itemDto.getId() == null;

            if (!isNewItem) {

                quoteItem = quote.getItems()
                        .stream()
                        .filter(item -> itemDto.getId().equals(item.getId()))
                        .findFirst()
                        .orElseThrow(() -> new QuoteItemNotFoundException(itemDto.getId()));

            } else {

                quoteItem = new QuoteItem();
                quoteItem.setQuote(quote);
                quote.getItems().add(quoteItem);
            }

            InventoryItem inventoryItem =
                    inventoryRepository.findById(itemDto.getInventoryItemId()).orElseThrow(() ->
                                    new InventoryItemNotFoundException(itemDto.getInventoryItemId()));

            if (Boolean.FALSE.equals(inventoryItem.getActive())) {
                throw new InventoryItemNotFoundException(inventoryItem.getId());
            }

            quoteItem.setInventoryItem(inventoryItem);

            if (isNewItem) {

                quoteItem.setDescription(inventoryItem.getName());
                quoteItem.setUnitPrice(inventoryItem.getSalePrice());
                quoteItem.setTaxRate(inventoryItem.getTaxRate());
            }

            quoteItem.setQuantity(itemDto.getQuantity());
            quoteItem.setDiscount(itemDto.getDiscount() != null
                            ? itemDto.getDiscount() : BigDecimal.ZERO);

            quoteDomainService.calculateItem(quoteItem);
        }
    }

}