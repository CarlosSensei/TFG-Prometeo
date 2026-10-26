package com.optiflow.crm.followup;

import com.optiflow.crm.followup.model.FollowUp;
import com.optiflow.crm.followup.model.FollowUpStatus;
import com.optiflow.crm.followup.model.FollowUpType;
import com.optiflow.crm.notification.NotificationRepository;
import com.optiflow.crm.notification.model.NotificationType;
import com.optiflow.crm.order.QuoteToOrderTestDataFactory;
import com.optiflow.crm.quote.model.Quote;
import com.optiflow.crm.quote.model.QuoteStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
@Import(QuoteToOrderTestDataFactory.class)
@Sql(
        scripts = "/sql/cleanup-followup-test.sql",
        executionPhase = ExecutionPhase.BEFORE_TEST_METHOD
)
@Sql(
        scripts = "/sql/cleanup-followup-test.sql",
        executionPhase = ExecutionPhase.AFTER_TEST_METHOD
)
class FollowUpNotificationIntegrationTest {

    @Autowired
    private FollowUpScheduler followUpScheduler;

    @Autowired
    private FollowUpRepository followUpRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private QuoteToOrderTestDataFactory testDataFactory;

    @Autowired
    private TransactionTemplate transactionTemplate;

    private Long createFollowUp(
            FollowUpStatus status,
            LocalDateTime scheduledAt,
            boolean active) {

        return transactionTemplate.execute(transaction -> {

            Quote quote = testDataFactory.createQuote(
                    QuoteStatus.SENT
            );

            FollowUp followUp = new FollowUp();

            followUp.setQuote(quote);
            followUp.setType(FollowUpType.CALL);
            followUp.setStatus(status);
            followUp.setScheduledAt(scheduledAt);
            followUp.setActive(active);
            followUp.setAutomated(false);

            return followUpRepository.saveAndFlush(followUp)
                    .getId();
        });
    }

    private long countDueNotifications(Long followUpId) {

        return notificationRepository.findAll()
                .stream()
                .filter(notification ->
                        followUpId.equals(notification.getFollowUpId())
                                && notification.getType()
                                == NotificationType.FOLLOW_UP_DUE
                )
                .count();
    }


    @Test
    void givenDueFollowUp_whenSchedulerRunsTwice_thenCreateOnlyOneNotification() {

        // GIVEN
        Long followUpId = transactionTemplate.execute(status -> {

            Quote quote = testDataFactory.createQuote(
                    QuoteStatus.SENT
            );

            FollowUp followUp = new FollowUp();

            followUp.setQuote(quote);
            followUp.setType(FollowUpType.CALL);
            followUp.setStatus(FollowUpStatus.PENDING);
            followUp.setScheduledAt(
                    LocalDateTime.now().minusHours(1)
            );
            followUp.setActive(true);
            followUp.setAutomated(false);

            return followUpRepository.saveAndFlush(followUp)
                    .getId();
        });

        assertNotNull(followUpId);

        // WHEN
        followUpScheduler.checkPendingFollowUps();

        // THEN
        long afterFirstRun = notificationRepository
                .findAll()
                .stream()
                .filter(notification ->
                        followUpId.equals(notification.getFollowUpId())
                                && notification.getType()
                                == NotificationType.FOLLOW_UP_DUE
                )
                .count();

        assertEquals(1L, afterFirstRun);

        // WHEN - Segunda ejecución
        followUpScheduler.checkPendingFollowUps();

        // THEN - No debe duplicarse
        long afterSecondRun = notificationRepository
                .findAll()
                .stream()
                .filter(notification ->
                        followUpId.equals(notification.getFollowUpId())
                                && notification.getType()
                                == NotificationType.FOLLOW_UP_DUE
                )
                .count();

        assertEquals(1L, afterSecondRun);
    }

    @ParameterizedTest
    @EnumSource(
            value = FollowUpStatus.class,
            names = {"COMPLETED", "CANCELLED"}
    )
    void givenNonPendingFollowUp_whenSchedulerRuns_thenNoNotification(
            FollowUpStatus status) {

        Long followUpId = createFollowUp(
                status,
                LocalDateTime.now().minusHours(2),
                true
        );

        followUpScheduler.checkPendingFollowUps();

        assertEquals(
                0,
                countDueNotifications(followUpId)
        );
    }

    @Test
    void givenFutureFollowUp_whenSchedulerRuns_thenNoNotification() {

        Long followUpId = createFollowUp(
                FollowUpStatus.PENDING,
                LocalDateTime.now().plusDays(2),
                true
        );

        followUpScheduler.checkPendingFollowUps();

        assertEquals(
                0,
                countDueNotifications(followUpId)
        );
    }

    @Test
    void givenInactiveFollowUp_whenSchedulerRuns_thenNoNotification() {

        Long followUpId = createFollowUp(
                FollowUpStatus.PENDING,
                LocalDateTime.now().minusHours(2),
                false
        );

        followUpScheduler.checkPendingFollowUps();

        assertEquals(
                0,
                countDueNotifications(followUpId)
        );
    }

    @Test
    void givenTwoDueFollowUps_whenSchedulerRuns_thenCreateTwoNotifications() {

        Long firstId = createFollowUp(
                FollowUpStatus.PENDING,
                LocalDateTime.now().minusHours(2),
                true
        );

        Long secondId = createFollowUp(
                FollowUpStatus.PENDING,
                LocalDateTime.now().minusHours(1),
                true
        );

        followUpScheduler.checkPendingFollowUps();

        assertEquals(1, countDueNotifications(firstId));
        assertEquals(1, countDueNotifications(secondId));

        followUpScheduler.checkPendingFollowUps();

        assertEquals(1, countDueNotifications(firstId));
        assertEquals(1, countDueNotifications(secondId));
    }
}
