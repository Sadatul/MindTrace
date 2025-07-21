package com.sadi.backend.services.impls;

import com.google.firebase.messaging.*;
import com.sadi.backend.dtos.requests.ReminderDTO;
import com.sadi.backend.entities.User;
import com.sadi.backend.entities.UserDevice;
import com.sadi.backend.repositories.UserDeviceRepository;
import com.sadi.backend.services.abstractions.ReminderSenderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Profile("!test")
@Slf4j
@RequiredArgsConstructor
public class ReminderSenderServiceImpl implements ReminderSenderService {
    private final UserDeviceRepository userDeviceRepository;

    @Override
    public void sendReminder(ReminderDTO req) {
        log.debug("Sending Reminder Request {}", req);
        List<String> tokens = getTokens(req);
        if (tokens.isEmpty()) return;
        MulticastMessage messages = messageFactory(req, tokens);

        try {
            BatchResponse response = FirebaseMessaging.getInstance().sendEachForMulticast(messages);
            if(response.getFailureCount() > 0)
            {
                List<String> failedTokens = getFailedTokens(response, tokens);
                userDeviceRepository.deleteAllTokens(failedTokens, req.getUserId());
            }
        } catch (FirebaseMessagingException e) {
            log.error("Unable to send Reminder Request {} {}", req, e.getMessage());
        }

    }

    private static MulticastMessage messageFactory(ReminderDTO req, List<String> tokens) {
        return MulticastMessage.builder().setNotification(Notification.builder()
                    .setTitle(req.getTitle())
                    .setBody(req.getDescription())
                    .build()
                ).putAllData(req.getMap())
                 .addAllTokens(tokens).build();
    }

    private List<String> getTokens(ReminderDTO req) {
        return userDeviceRepository
                .findByUser(new User(req.getUserId())).stream()
                .map(UserDevice::getToken).toList();
    }

    private static List<String> getFailedTokens(BatchResponse response, List<String> tokens) {
        List<SendResponse> responses = response.getResponses();
        List<String> failedTokens = new ArrayList<>();
        for (int i = 0; i < responses.size(); i++) {
            if (!responses.get(i).isSuccessful()) {
                failedTokens.add(tokens.get(i));
            }
        }
        return failedTokens;
    }
}
