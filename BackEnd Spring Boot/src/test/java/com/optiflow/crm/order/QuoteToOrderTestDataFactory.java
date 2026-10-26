package com.optiflow.crm.order;

import com.optiflow.crm.client.ClientRepository;
import com.optiflow.crm.client.model.Client;
import com.optiflow.crm.inventory.InventoryRepository;
import com.optiflow.crm.inventory.model.InventoryItem;
import com.optiflow.crm.inventory.model.InventoryItemType;
import com.optiflow.crm.quote.QuoteRepository;
import com.optiflow.crm.quote.model.Quote;
import com.optiflow.crm.quote.model.QuoteItem;
import com.optiflow.crm.quote.model.QuoteStatus;
import com.optiflow.crm.user.UserRepository;
import com.optiflow.crm.user.model.User;
import com.optiflow.crm.user.model.UserRole;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.UUID;

@Component
public class QuoteToOrderTestDataFactory {

    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final InventoryRepository inventoryRepository;
    private final QuoteRepository quoteRepository;
    private final OrderRepository orderRepository;

    public QuoteToOrderTestDataFactory(
            ClientRepository clientRepository,
            UserRepository userRepository,
            InventoryRepository inventoryRepository,
            OrderRepository orderRepository,
            QuoteRepository quoteRepository) {

        this.clientRepository = clientRepository;
        this.userRepository = userRepository;
        this.inventoryRepository = inventoryRepository;
        this.quoteRepository = quoteRepository;
        this.orderRepository = orderRepository;
    }

    private static final AtomicInteger SEQUENCE =
            new AtomicInteger(10000000);


    private String generateTestDni() {

        int number = SEQUENCE.getAndIncrement();

        String letters = "TRWAGMYFPDXBNJZSQVHLCKE";

        char letter = letters.charAt(number % 23);

        return String.format("%08d%c", number, letter);
    }


    public Quote createQuote(QuoteStatus status) {

        String suffix = UUID.randomUUID()
                .toString()
                .substring(0, 8);

        // 1. CLIENTE
        Client client = new Client();
        client.setName("Ana");
        client.setSurname("García");
        client.setDni(generateTestDni());
        client.setPhoneNumber("600123456");
        client.setActive(true);

        client = clientRepository.saveAndFlush(client);

        // 2. VENDEDOR
        User seller = new User();
        seller.setFirstName("Carlos");
        seller.setLastName("Vendedor");
        seller.setEmail("vendedor-" + suffix + "@optiflow.com");
        seller.setPassword("test-password");
        seller.setRole(UserRole.SELLER);
        seller.setActive(true);

        seller = userRepository.saveAndFlush(seller);

        // 3. INVENTARIO
        InventoryItem inventoryItem = new InventoryItem();
        inventoryItem.setSku("FRAME-" + suffix);
        inventoryItem.setName("Montura de prueba");
        inventoryItem.setType(InventoryItemType.FRAME);
        inventoryItem.setSalePrice(new BigDecimal("100.00"));
        inventoryItem.setTaxRate(new BigDecimal("21.00"));
        inventoryItem.setStock(10);
        inventoryItem.setActive(true);

        inventoryItem = inventoryRepository.saveAndFlush(
                inventoryItem
        );

        // 4. PRESUPUESTO
        Quote quote = new Quote();

        quote.setQuoteNumber(
                "Q-" + UUID.randomUUID()
                        .toString()
                        .substring(0, 8)
                        .toUpperCase()
        );

        quote.setClient(client);
        quote.setSeller(seller);
        quote.setStatus(status);
        quote.setQuoteDate(LocalDate.now());
        quote.setActive(true);

        // 5. ARTÍCULO DEL PRESUPUESTO
        QuoteItem item = new QuoteItem();

        item.setQuote(quote);
        item.setInventoryItem(inventoryItem);
        item.setDescription("Montura de prueba");
        item.setQuantity(2);
        item.setUnitPrice(new BigDecimal("100.00"));
        item.setDiscount(new BigDecimal("10.00"));
        item.setTaxRate(new BigDecimal("21.00"));

        item.setSubtotal(new BigDecimal("190.00"));
        item.setTaxAmount(new BigDecimal("39.90"));
        item.setTotal(new BigDecimal("229.90"));

        quote.getItems().add(item);

        // 6. TOTALES
        quote.setSubtotal(new BigDecimal("190.00"));
        quote.setTaxAmount(new BigDecimal("39.90"));
        quote.setTotal(new BigDecimal("229.90"));

        return quoteRepository.saveAndFlush(quote);
    }


    public void cleanUp(Quote quote) {

        Long quoteId = quote.getId();

        // Eliminamos cualquier pedido asociado, si existe.
        orderRepository.findByQuoteId(quoteId)
                .ifPresent(orderRepository::delete);

        quoteRepository.deleteById(quoteId);

        inventoryRepository.deleteById(
                quote.getItems().getFirst()
                        .getInventoryItem().getId()
        );

        clientRepository.deleteById(
                quote.getClient().getId()
        );

        userRepository.deleteById(
                quote.getSeller().getId()
        );
    }

}

