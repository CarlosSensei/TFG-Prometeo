package com.optiflow.crm.notification;

import com.optiflow.crm.auth.CurrentUserService;
import com.optiflow.crm.notification.model.Notification;
import com.optiflow.crm.notification.model.NotificationService;
import com.optiflow.crm.notification.model.NotificationType;
import com.optiflow.crm.quote.QuoteRepository;
import com.optiflow.crm.quote.model.Quote;
import com.optiflow.crm.user.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;

@Service
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepository;
    private final QuoteRepository quoteRepository;
    private final CurrentUserService currentUserService;

    public NotificationServiceImpl(NotificationRepository notificationRepository,
                                   QuoteRepository quoteRepository,
                                   CurrentUserService currentUserService) {
        this.notificationRepository = notificationRepository;
        this.quoteRepository = quoteRepository;
        this.currentUserService = currentUserService;
    }

    private Long currentUserId() {
        return currentUserService.getCurrentUser().getId();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Notification> findAll(Pageable pageable) {
        return notificationRepository.findByRecipientIdAndActiveTrue(currentUserId(), pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Notification> findUnread(Pageable pageable) {
        return notificationRepository.findByRecipientIdAndReadFalseAndActiveTrue(currentUserId(), pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public long countUnread() {
        return notificationRepository.countByRecipientIdAndReadFalseAndActiveTrue(currentUserId());
    }

    @Override
    @Transactional(readOnly = true)
    public Notification findById(Long id) {
        return notificationRepository.findByIdAndRecipientIdAndActiveTrue(id, currentUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Notificación no encontrada"));
    }

    /** Invocado por el listener AFTER_COMMIT, sin identidad de usuario en el scheduler. */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Notification createFollowUpNotification(Long followUpId,
                                                     Long quoteId,
                                                     NotificationType type,
                                                     String title,
                                                     String message) {
        if (notificationRepository.existsByFollowUpIdAndType(followUpId, type)) {
            return null;
        }
        Quote quote = quoteRepository.findById(quoteId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Presupuesto no encontrado: " + quoteId));
        User seller = quote.getSeller();
        if (seller == null) {
            throw new IllegalStateException("Presupuesto sin vendedor asignado: " + quoteId);
        }
        Notification notification = new Notification();
        notification.setFollowUpId(followUpId);
        notification.setQuoteId(quoteId);
        notification.setRecipient(seller);
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setRead(false);
        notification.setActive(true);
        return notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public Notification markAsRead(Long id) {
        Notification notification = findById(id);
        if (Boolean.TRUE.equals(notification.getRead())) return notification;
        notification.setRead(true);
        notification.setReadAt(LocalDateTime.now());
        return notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Notification notification = findById(id);
        notification.setActive(false);
        notificationRepository.save(notification);
    }
}
