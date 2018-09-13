package br.com.litecode.service.push;

import br.com.litecode.domain.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class PushService {
	private SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    public PushService(SimpMessagingTemplate simpMessagingTemplate) {
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    public void publish(PushChannel channel, Object message, User user) {
		Map<String, Object> headers = new HashMap<>();
		if (user != null) {
			headers.put("user-name", user.getUsername());
		}

		simpMessagingTemplate.convertAndSend("/topic/" + channel.name().toLowerCase(), message, headers);
	}
}
