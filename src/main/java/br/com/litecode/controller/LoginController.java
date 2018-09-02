package br.com.litecode.controller;

import br.com.litecode.domain.model.User.Role;
import lombok.extern.slf4j.Slf4j;
import org.omnifaces.util.Faces;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.enterprise.context.RequestScoped;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;

@Slf4j
@RequestScoped
@Component
public class LoginController {

    public void setClientTimeZone() {
        String clientTimeZone = Faces.getRequestParameter("clientTimeZone");

        if (clientTimeZone == null) {
            return;
        }

        int timeZoneOffset = Integer.parseInt(clientTimeZone);
        if (timeZoneOffset >= 0) {
            clientTimeZone = "+" + timeZoneOffset;
        }

        ZoneId timeZone = ZoneId.of("GMT" + clientTimeZone);
        Faces.getSessionMap().put("timeZone", timeZone);
        log.info("Client time zone: {}", timeZone);
    }

    public void checkIfAlreadyLoggedIn() throws IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
            Faces.redirect("");
        }
    }

	public boolean hasDevRights() {
		return Faces.isUserInRole(Role.DEVELOPER.name());
	}

	public boolean hasAdminRights() {
		return Faces.isUserInRole(Role.ADMIN.name()) || hasDevRights();
	}

	public boolean isNewVersion() {
		LocalDate versionDate = Faces.getApplicationAttribute("versionDate");
		return LocalDate.now().isBefore(versionDate.plusDays(3));
	}
}
