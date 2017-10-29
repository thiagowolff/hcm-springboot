package br.com.litecode.domain.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Embeddable;

@Embeddable
@Getter
@Setter
public class UserPreferences {
	private boolean disableNotificationMessages;
	private boolean disableNotificationSounds;

	public UserPreferences() {
		disableNotificationMessages = false;
		disableNotificationSounds = false;
	}
}