package br.com.litecode.service;

import lombok.extern.slf4j.Slf4j;
import net.pushover.client.PushoverClient;
import net.pushover.client.PushoverException;
import net.pushover.client.PushoverMessage;
import net.pushover.client.PushoverRestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PushoverService {
    @Value("${pushover.enabled:false}")
    private boolean enabled;

    @Value("${pushover.token}")
    private String apiToken;

    @Value("${pushover.user}")
    private String userToken;

    public void sendNotification(String message) throws PushoverException {
        if (!enabled) {
            log.info("Pushover service disabled. Message: {}", message);
            return;
        }

        PushoverClient pushoverClient = new PushoverRestClient();
        pushoverClient.pushMessage(PushoverMessage.builderWithApiToken(apiToken)
                .setUserId(userToken)
                .setMessage(message)
                .build());
    }
}
