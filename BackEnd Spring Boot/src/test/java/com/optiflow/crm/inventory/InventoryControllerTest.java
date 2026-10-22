package com.optiflow.crm.inventory;

import com.optiflow.crm.inventory.dto.InventoryItemDto;
import com.optiflow.crm.inventory.dto.InventorySearchDto;
import com.optiflow.crm.inventory.mapper.InventoryItemMapper;
import com.optiflow.crm.inventory.model.InventoryItem;
import com.optiflow.crm.inventory.model.InventoryItemType;
import com.optiflow.crm.inventory.model.InventoryService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;

import org.springframework.http.MediaType;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class InventoryControllerTest {

    private InventoryService inventoryService;
    private InventoryItemMapper inventoryItemMapper;
    private MockMvc mockMvc;

    // =====================================
    // CONFIGURACIÓN
    // =====================================

    @BeforeEach
    void setUp() {

        inventoryService = mock(InventoryService.class);
        inventoryItemMapper = mock(InventoryItemMapper.class);

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();

        validator.afterPropertiesSet();

        InventoryController controller =
                new InventoryController(inventoryService, inventoryItemMapper);

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setValidator(validator)
                .setMessageConverters(
                        new JacksonJsonHttpMessageConverter(JsonMapper.builder().build()))
                .build();
    }

    // =====================================
    // MÉTODOS AUXILIARES
    // =====================================

    private InventoryItem createItem(Long id) {

        InventoryItem item = new InventoryItem();

        item.setId(id);
        item.setSku("MON-001");
        item.setName("Montura graduada");
        item.setType(InventoryItemType.FRAME);

        return item;
    }

    private InventoryItemDto createItemDto(Long id) {

        InventoryItemDto dto = new InventoryItemDto();

        dto.setId(id);
        dto.setSku("MON-001");
        dto.setName("Montura graduada");
        dto.setType(InventoryItemType.FRAME);

        dto.setSalePrice(new BigDecimal("120.00"));
        dto.setTaxRate(new BigDecimal("21.00"));
        dto.setStock(10);

        return dto;
    }

    private String validItemJson() {

        return """
                {
                    "sku": "MON-001",
                    "name": "Montura graduada",
                    "type": "FRAME",
                    "salePrice": 120.00,
                    "taxRate": 21.00,
                    "stock": 10
                }
                """;
    }

    private void assertInvalidCreate(String json) throws Exception {

        mockMvc.perform(post("/inventory")
                        .contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(inventoryService, inventoryItemMapper);
    }

    // =====================================
    // 1. FIND ALL
    // =====================================

    @Test
    void givenItems_whenFindAll_thenReturn200AndPagedJson() throws Exception {

        // GIVEN
        InventoryItem item = createItem(1L);
        InventoryItemDto dto = createItemDto(1L);

        Pageable pageable = PageRequest.of(0, 10);

        Page<InventoryItem> page = new PageImpl<>(List.of(item), pageable, 1);

        when(inventoryService.findAll(any(Pageable.class))).thenReturn(page);
        when(inventoryItemMapper.toDto(item)).thenReturn(dto);

        // WHEN + THEN
        mockMvc.perform(get("/inventory")
                        .param("page", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].sku").value("MON-001"))
                .andExpect(jsonPath("$.content[0].name").value("Montura graduada"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(10));

        verify(inventoryService).findAll(argThat(p ->
                        p.getPageNumber() == 0 && p.getPageSize() == 10));

        verify(inventoryItemMapper).toDto(item);
    }

    // =====================================
    // 2. SEARCH
    // =====================================

    @Test
    void givenSearchBody_whenSearch_thenReturn200() throws Exception {

        // GIVEN
        InventoryItem item = createItem(2L);
        InventoryItemDto dto = createItemDto(2L);

        Page<InventoryItem> page = new PageImpl<>(List.of(item),
                PageRequest.of(0, 10), 1);

        when(inventoryService.search(any(InventorySearchDto.class))).thenReturn(page);
        when(inventoryItemMapper.toDto(item)).thenReturn(dto);

        String json = """
                {
                    "name": "Montura",
                    "type": "FRAME",
                    "active": true,
                    "page": 0,
                    "size": 10
                }
                """;

        // WHEN + THEN
        mockMvc.perform(post("/inventory/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(2))
                .andExpect(jsonPath("$.content[0].sku").value("MON-001"))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(inventoryService).search(
                argThat(search -> "Montura".equals(search.getName())
                                && search.getType() ==
                                InventoryItemType.FRAME
                                && Boolean.TRUE.equals(search.getActive())
                                && Integer.valueOf(0).equals(search.getPage())
                                && Integer.valueOf(10).equals(search.getSize())));

        verify(inventoryItemMapper).toDto(item);
    }

    // =====================================
    // 3. FIND BY ID
    // =====================================

    @Test
    void givenExistingId_whenFindById_thenReturn200() throws Exception {

        // GIVEN
        InventoryItem item = createItem(5L);
        InventoryItemDto dto = createItemDto(5L);

        when(inventoryService.findById(5L)).thenReturn(item);
        when(inventoryItemMapper.toDto(item)).thenReturn(dto);

        // WHEN + THEN
        mockMvc.perform(get("/inventory/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.sku").value("MON-001"))
                .andExpect(jsonPath("$.stock").value(10));

        verify(inventoryService).findById(5L);
        verify(inventoryItemMapper).toDto(item);
    }

    // =====================================
    // 4. CREATE
    // =====================================

    @Test
    void givenValidItem_whenCreate_thenReturn201() throws Exception {

        // GIVEN
        InventoryItem saved = createItem(9L);
        InventoryItemDto response = createItemDto(9L);

        when(inventoryService.create(any(InventoryItemDto.class))).thenReturn(saved);
        when(inventoryItemMapper.toDto(saved)).thenReturn(response);

        // WHEN + THEN
        mockMvc.perform(post("/inventory")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validItemJson()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(9))
                .andExpect(jsonPath("$.sku").value("MON-001"))
                .andExpect(jsonPath("$.stock").value(10));

        verify(inventoryService).create(
                argThat(dto -> "MON-001".equals(dto.getSku())
                                && "Montura graduada".equals(dto.getName())
                                && dto.getType() ==
                                InventoryItemType.FRAME
                                && Integer.valueOf(10).equals(dto.getStock())));

        verify(inventoryItemMapper).toDto(saved);
    }

    // =====================================
    // 5. UPDATE
    // =====================================

    @Test
    void givenValidItem_whenUpdate_thenReturn200() throws Exception {

        // GIVEN
        InventoryItem updated = createItem(9L);
        InventoryItemDto response = createItemDto(9L);

        when(inventoryService.update(eq(9L), any(InventoryItemDto.class)))
                .thenReturn(updated);
        when(inventoryItemMapper.toDto(updated)).thenReturn(response);

        // WHEN + THEN
        mockMvc.perform(put("/inventory/9")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validItemJson()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(9))
                .andExpect(jsonPath("$.name")
                        .value("Montura graduada"));

        verify(inventoryService).update(eq(9L), argThat(dto ->
                        "MON-001".equals(dto.getSku()) && Integer.valueOf(10)
                                .equals(dto.getStock())));

        verify(inventoryItemMapper).toDto(updated);
    }

    // =====================================
    // 6. DELETE
    // =====================================

    @Test
    void givenExistingId_whenDelete_thenReturn204() throws Exception {

        mockMvc.perform(delete("/inventory/9"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(inventoryService).delete(9L);

        verifyNoInteractions(inventoryItemMapper);
    }

    // =====================================
    // 7. MISSING SKU
    // =====================================

    @Test
    void givenMissingSku_whenCreate_thenReturn400() throws Exception {

        String json = """
                {
                    "name": "Montura graduada",
                    "type": "FRAME",
                    "salePrice": 120.00,
                    "taxRate": 21.00,
                    "stock": 10
                }
                """;

        assertInvalidCreate(json);
    }

    // =====================================
    // 8. BLANK NAME
    // =====================================

    @Test
    void givenBlankName_whenCreate_thenReturn400() throws Exception {

        String json = validItemJson()
                .replace("\"Montura graduada\"", "\"   \"");

        assertInvalidCreate(json);
    }

    // =====================================
    // 9. MISSING TYPE
    // =====================================

    @Test
    void givenMissingType_whenCreate_thenReturn400() throws Exception {

        String json = validItemJson().replace("\"type\": \"FRAME\",", "");

        assertInvalidCreate(json);
    }

    // =====================================
    // 10. NEGATIVE PRICE
    // =====================================

    @Test
    void givenNegativePrice_whenCreate_thenReturn400() throws Exception {

        String json = validItemJson()
                .replace("\"salePrice\": 120.00", "\"salePrice\": -10.00");

        assertInvalidCreate(json);
    }

    // =====================================
    // 11. NEGATIVE STOCK
    // =====================================

    @Test
    void givenNegativeStock_whenCreate_thenReturn400() throws Exception {

        String json = validItemJson().replace("\"stock\": 10", "\"stock\": -5");

        assertInvalidCreate(json);
    }

    // =====================================
    // 12. MISSING TAX RATE
    // =====================================

    @Test
    void givenMissingTaxRate_whenCreate_thenReturn400() throws Exception {

        String json = validItemJson().replace("\"taxRate\": 21.00,", "");

        assertInvalidCreate(json);
    }

    // =====================================
    // 13. NON-NUMERIC ID
    // =====================================

    @Test
    void givenNonNumericId_whenFindById_thenReturn400() throws Exception {

        mockMvc.perform(get("/inventory/abc")).andExpect(status().isBadRequest());

        verifyNoInteractions(inventoryService, inventoryItemMapper);
    }
}
