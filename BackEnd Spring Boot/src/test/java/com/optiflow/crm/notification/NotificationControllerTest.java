package com.optiflow.crm.notification;

import com.optiflow.crm.notification.dto.NotificationDto;
import com.optiflow.crm.notification.mapper.NotificationMapper;
import com.optiflow.crm.notification.model.Notification;
import com.optiflow.crm.notification.model.NotificationService;
import com.optiflow.crm.notification.model.NotificationType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.util.ReflectionTestUtils;

import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class NotificationControllerTest {

    private NotificationService notificationService;
    private NotificationMapper notificationMapper;
    private MockMvc mockMvc;

    // =====================================
    // CONFIGURACIÓN
    // =====================================

    @BeforeEach
    void setUp() {

        notificationService = mock(NotificationService.class);
        notificationMapper = mock(NotificationMapper.class);

        NotificationController controller =
                new NotificationController(
                        notificationService,
                        notificationMapper
                );

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setCustomArgumentResolvers(
                        new PageableHandlerMethodArgumentResolver())
                .setMessageConverters(
                        new JacksonJsonHttpMessageConverter(JsonMapper.builder()
                                        .findAndAddModules().build()))
                .build();
    }

    // =====================================
    // MÉTODOS AUXILIARES
    // =====================================

    private Notification createNotification(Long id) {

        Notification notification = new Notification();

        ReflectionTestUtils.setField(notification, "id", id);
        notification.setType(NotificationType.FOLLOW_UP_DUE);
        notification.setTitle("Seguimiento pendiente");
        notification.setMessage("Contactar con el cliente");
        notification.setFollowUpId(10L);
        notification.setQuoteId(5L);
        notification.setRead(false);
        notification.setActive(true);

        ReflectionTestUtils.setField(notification, "createdAt",
                LocalDateTime.of(2026, 10, 20, 10, 0));

        return notification;
    }

    private NotificationDto createDto(Long id) {

        NotificationDto dto = new NotificationDto();

        dto.setId(id);
        dto.setType(NotificationType.FOLLOW_UP_DUE);
        dto.setTitle("Seguimiento pendiente");
        dto.setMessage("Contactar con el cliente");
        dto.setFollowUpId(10L);
        dto.setQuoteId(5L);
        dto.setRead(false);

        dto.setCreatedAt(
                LocalDateTime.of(2026, 10, 20, 10, 0));

        return dto;
    }

    // =====================================
    // 1. FIND ALL
    // =====================================

    @Test
    void givenNotifications_whenFindAll_thenReturn200AndPage() throws Exception {

        // GIVEN
        Notification notification = createNotification(1L);
        NotificationDto dto = createDto(1L);

        Pageable pageable = PageRequest.of(0, 10);

        Page<Notification> page = new PageImpl<>(
                List.of(notification), pageable, 1);

        when(notificationService.findAll(any(Pageable.class))).thenReturn(page);
        when(notificationMapper.toDto(notification)).thenReturn(dto);

        // WHEN + THEN
        mockMvc.perform(get("/notifications")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(10));

        verify(notificationService).findAll(argThat(p ->
                        p.getPageNumber() == 0 && p.getPageSize() == 10));

        verify(notificationMapper).toDto(notification);
    }

    // =====================================
    // 2. EMPTY PAGE
    // =====================================

    @Test
    void givenNoNotifications_whenFindAll_thenReturnEmptyPage() throws Exception {

        // GIVEN
        when(notificationService.findAll(any(Pageable.class)))
                .thenReturn(Page.empty(PageRequest.of(0, 10)));

        // WHEN + THEN
        mockMvc.perform(get("/notifications")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));

        verifyNoInteractions(notificationMapper);
    }

    // =====================================
    // 3. PAGINATION
    // =====================================

    @Test
    void givenPagination_whenFindAll_thenUseCorrectPageable() throws Exception {

        // GIVEN
        when(notificationService.findAll(any(Pageable.class)))
                .thenReturn(Page.empty(PageRequest.of(2, 5)));

        // WHEN + THEN
        mockMvc.perform(get("/notifications")
                        .param("page", "2")
                        .param("size", "5"))
                .andExpect(status().isOk());

        // VERIFY
        verify(notificationService).findAll(argThat(p ->
                        p.getPageNumber() == 2 && p.getPageSize() == 5));
    }

    // =====================================
    // 4. FIND UNREAD
    // =====================================

    @Test
    void givenUnreadNotifications_whenFindUnread_thenReturn200() throws Exception {

        // GIVEN
        Notification notification = createNotification(2L);
        NotificationDto dto = createDto(2L);

        Page<Notification> page = new PageImpl<>(List.of(notification),
                PageRequest.of(0, 10), 1);

        when(notificationService.findUnread(any(Pageable.class))).thenReturn(page);
        when(notificationMapper.toDto(notification)).thenReturn(dto);

        // WHEN + THEN
        mockMvc.perform(get("/notifications/unread")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(2))
                .andExpect(jsonPath("$.content[0].read").value(false))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(notificationService).findUnread(argThat(p ->
                        p.getPageNumber() == 0 && p.getPageSize() == 10));
    }

    // =====================================
    // 5. EMPTY UNREAD PAGE
    // =====================================

    @Test
    void givenNoUnread_whenFindUnread_thenReturnEmptyPage() throws Exception {

        // GIVEN
        when(notificationService.findUnread(any(Pageable.class)))
                .thenReturn(Page.empty(PageRequest.of(0, 10)));

        // WHEN + THEN
        mockMvc.perform(get("/notifications/unread"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));

        verifyNoInteractions(notificationMapper);
    }

    // =====================================
    // 6. COUNT UNREAD
    // =====================================

    @Test
    void givenUnreadNotifications_whenCountUnread_thenReturnCount() throws Exception {

        // GIVEN
        when(notificationService.countUnread()).thenReturn(3L);

        // WHEN + THEN
        mockMvc.perform(get("/notifications/unread/count"))
                .andExpect(status().isOk())
                .andExpect(content().string("3"));

        verify(notificationService).countUnread();
        verifyNoInteractions(notificationMapper);
    }

    // =====================================
    // 7. COUNT UNREAD ZERO
    // =====================================

    @Test
    void givenNoUnread_whenCountUnread_thenReturnZero() throws Exception {

        // GIVEN
        when(notificationService.countUnread()).thenReturn(0L);

        // WHEN + THEN
        mockMvc.perform(get("/notifications/unread/count"))
                .andExpect(status().isOk())
                .andExpect(content().string("0"));

        verify(notificationService).countUnread();
    }

    // =====================================
    // 8. MARK AS READ
    // =====================================

    @Test
    void givenUnreadNotification_whenMarkAsRead_thenReturn200() throws Exception {

        // GIVEN
        Notification notification = createNotification(9L);

        notification.setRead(true);
        notification.setReadAt(
                LocalDateTime.of(2026, 10, 20, 11, 0));

        NotificationDto dto = createDto(9L);
        dto.setRead(true);
        dto.setReadAt(notification.getReadAt());

        when(notificationService.markAsRead(9L)).thenReturn(notification);
        when(notificationMapper.toDto(notification)).thenReturn(dto);

        // WHEN + THEN
        mockMvc.perform(patch("/notifications/9/read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(9))
                .andExpect(jsonPath("$.read").value(true))
                .andExpect(jsonPath("$.readAt")
                        .value("2026-10-20T11:00:00"));

        verify(notificationService).markAsRead(9L);
        verify(notificationMapper).toDto(notification);
    }

    // =====================================
    // 9. MARK ALREADY READ
    // =====================================

    @Test
    void givenAlreadyReadNotification_whenMarkAsRead_thenReturn200() throws Exception {

        // GIVEN
        Notification notification = createNotification(9L);

        notification.setRead(true);
        notification.setReadAt(
                LocalDateTime.of(2026, 10, 20, 11, 0));

        NotificationDto dto = createDto(9L);
        dto.setRead(true);
        dto.setReadAt(notification.getReadAt());

        when(notificationService.markAsRead(9L)).thenReturn(notification);
        when(notificationMapper.toDto(notification)).thenReturn(dto);

        // WHEN + THEN
        mockMvc.perform(patch("/notifications/9/read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.read").value(true))
                .andExpect(jsonPath("$.readAt")
                        .value("2026-10-20T11:00:00"));

        verify(notificationService).markAsRead(9L);
    }

    // =====================================
    // 10. DELETE
    // =====================================

    @Test
    void givenNotification_whenDelete_thenReturn204() throws Exception {

        // WHEN + THEN
        mockMvc.perform(delete("/notifications/9"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        // VERIFY
        verify(notificationService).delete(9L);
        verifyNoInteractions(notificationMapper);
    }

    // =====================================
    // 11. NON NUMERIC ID - PATCH
    // =====================================

    @Test
    void givenNonNumericId_whenMarkAsRead_thenReturn400() throws Exception {

        // WHEN + THEN
        mockMvc.perform(patch("/notifications/abc/read"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(notificationService, notificationMapper);
    }

    // =====================================
    // 12. NON NUMERIC ID - DELETE
    // =====================================

    @Test
    void givenNonNumericId_whenDelete_thenReturn400() throws Exception {

        // WHEN + THEN
        mockMvc.perform(delete("/notifications/abc"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(notificationService, notificationMapper);
    }

    // =====================================
    // 13. COMPLETE RESPONSE
    // =====================================

    @Test
    void givenNotification_whenFindAll_thenReturnAllDtoFields() throws Exception {

        // GIVEN
        Notification notification = createNotification(1L);
        NotificationDto dto = createDto(1L);

        when(notificationService.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(notification),
                        PageRequest.of(0, 10), 1));

        when(notificationMapper.toDto(notification)).thenReturn(dto);

        // WHEN + THEN
        mockMvc.perform(get("/notifications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].type")
                        .value("FOLLOW_UP_DUE"))
                .andExpect(jsonPath("$.content[0].title")
                        .value("Seguimiento pendiente"))
                .andExpect(jsonPath("$.content[0].message")
                        .value("Contactar con el cliente"))
                .andExpect(jsonPath("$.content[0].followUpId")
                        .value(10))
                .andExpect(jsonPath("$.content[0].quoteId")
                        .value(5))
                .andExpect(jsonPath("$.content[0].read")
                        .value(false))
                .andExpect(jsonPath("$.content[0].createdAt")
                        .value("2026-10-20T10:00:00"))
                .andExpect(jsonPath("$.content[0].active")
                        .doesNotExist());

        verify(notificationMapper).toDto(notification);
    }
}
