package br.com.litecode.security;

import br.com.litecode.domain.model.User;
import br.com.litecode.domain.model.User.Role;
import br.com.litecode.domain.repository.UserRepository;
import br.com.litecode.service.PushoverService;
import com.google.common.io.ByteStreams;
import lombok.extern.slf4j.Slf4j;
import net.pushover.client.PushoverException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.time.ZoneId;
import java.util.TimeZone;

@Component
@Slf4j
public class AuthenticationListener implements ApplicationListener<InteractiveAuthenticationSuccessEvent> {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserSessionTracker userSessionTracker;

    @Autowired
    private PushoverService pushoverService;

    @Autowired
    private HttpServletRequest httpServletRequest;

    @Override
    public void onApplicationEvent(InteractiveAuthenticationSuccessEvent event) {
        UserPrincipal userPrincipal = (UserPrincipal) event.getAuthentication().getPrincipal();
        User user = userPrincipal.getUser();

        if (user.getRole() != Role.DEVELOPER) {
            HttpSession activeSession = userSessionTracker.getUserSession(user);
            if (activeSession != null && !httpServletRequest.getSession().getId().equals(activeSession.getId())) {
                userSessionTracker.killUserSession(user);
            }
        }

        user.setLastAccess(Instant.now());
        user.setSessionId(httpServletRequest.getSession().getId());

        String lastAccessLocation = getLastAccessLocation();
        if (lastAccessLocation != null) {
            user.setLastAccessLocation(lastAccessLocation);
            log.debug("Last access location: {}", user.getLastAccessLocation());
        }

        ZoneId timeZone = (ZoneId) httpServletRequest.getSession().getAttribute("timeZone");
        if (timeZone != null) {
            user.setTimeZone(timeZone.getId());
        } else {
            user.setTimeZone(getDefaultTimeZone());
        }

        userRepository.save(user);
        userSessionTracker.addUserSession(user, httpServletRequest.getSession());

        sendLoginNotification(user);
    }

    private String getLastAccessLocation() {
        try {
            URL url = new URL("https://ipinfo.io/" + httpServletRequest.getRemoteAddr() + "/json");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            return new String(ByteStreams.toByteArray(connection.getInputStream()));
        } catch (Exception e) {
            log.warn("Unable to retrieve IP geo location for {}", httpServletRequest.getRemoteAddr());
            return null;
        }
    }

    private void sendLoginNotification(User user) {
        try {
            pushoverService.sendNotification("User " + user.getUsername() + " logged in");
        } catch (PushoverException e) {
            log.error("Unable to send login notification", e);
        }
    }

    private String getDefaultTimeZone() {
        int offsetInMillis = TimeZone.getDefault().getRawOffset();
        String offset = String.format("%02d:%02d", Math.abs(offsetInMillis / 3600000), Math.abs((offsetInMillis / 60000) % 60));
        return "GMT" + (offsetInMillis >= 0 ? "+" : "-") + offset;
    }
}
