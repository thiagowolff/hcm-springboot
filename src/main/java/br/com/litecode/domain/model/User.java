package br.com.litecode.domain.model;

import com.google.common.base.Joiner;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

@Entity
@Getter
@Setter
@Table(name="\"user\"")
public class User {
	public enum Role {
        DEVELOPER,
		ADMIN,
		USER;

        @Override
        public String toString() {
            return "ROLE_" + name();
        }
    }
    
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY, generator = "user_generator")
	@SequenceGenerator(name="user_generator", sequenceName="user_user_id_seq")
	private Integer userId;

	@Enumerated(EnumType.STRING)
	private Role role;

	private String username;
	private String password;
	private String name;
	private boolean active;
	private Instant lastAccess;
	private Instant creationDate;
	private String sessionId;
	private String lastAccessLocation;
	private String timeZone;

	@Embedded
	private UserSettings userSettings;
	
    public User() {
    	active = true;
    	creationDate = Instant.now();
		userSettings = new UserSettings();
    }

    public String getLastAccessDuration() {
    	if (lastAccess == null) {
    		return "nunca acessou";
		}

		Duration duration = Duration.between(lastAccess, Instant.now());

		long days = duration.toDays();
		long hours = duration.minusDays(days).toHours();
		long minutes = duration.minusDays(days).minusHours(hours).toMinutes();

		String lastAccess = minutes + "m";
		if (days > 0 || hours > 0) {
			lastAccess = hours + "h " + lastAccess;
			if (days > 0) {
				lastAccess = days + "d " + lastAccess;
			}
		}

		return lastAccess;
	}

    public String getLastAccessLocationFormatted() {
		if (lastAccessLocation == null) {
			return "";
		}

		JsonObject locationJson = JsonParser.parseString(lastAccessLocation).getAsJsonObject();

		String ip = locationJson.get("ip") == null ? null : "IP: " + locationJson.get("ip").getAsString();
		String city = locationJson.get("city") == null ? null : "City: " + locationJson.get("city").getAsString();
		String region = locationJson.get("region") == null ? null : "Region: " + locationJson.get("region").getAsString();
		String country = locationJson.get("country") == null ? null : "Country: " + locationJson.get("country").getAsString();
		String location = locationJson.get("loc") == null ? null : "Location: " + locationJson.get("loc").getAsString();

		return Joiner.on("<br/>").skipNulls().join(ip, city, region, country, location);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		User user = (User) o;
		return Objects.equals(username, user.username);
	}

	@Override
	public int hashCode() {
		return Objects.hash(username);
	}

	@Override
	public String toString() {
		return "[" + userId + "] " + username;
	}
}