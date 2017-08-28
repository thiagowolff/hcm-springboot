package br.com.litecode.domain.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Getter
@Setter
public class User {
	public enum Role { DEV, ADMIN, USER }
    
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer userId;

	@Enumerated(EnumType.STRING)
	private Role role;

	private String username;
	private String password;
	private String name;
	private Instant lastAccess;
	private Instant creationDate;
	private String lastAccessLocation;
	private String sessionId;
	
    public User() {
    	creationDate = Instant.now();
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((username == null) ? 0 : username.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		if (!(obj instanceof User)) {
			return false;
		}
		User other = (User) obj;
		return username.equals(other.username);
	}

	@Override
	public String toString() {
		return "[" + userId + "] " + username;
	}
}