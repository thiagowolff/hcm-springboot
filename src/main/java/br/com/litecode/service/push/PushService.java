package br.com.litecode.service.push;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class PushService {
	@Autowired
	private SimpMessagingTemplate simpMessagingTemplate;

	public void publish(PushChannel channel, Object message, String user) {
		Map<String, Object> headers = ImmutableMap.of("user-name", user);
		simpMessagingTemplate.convertAndSend("/topic/" + channel.name().toLowerCase(),  message, headers);
	}
}
