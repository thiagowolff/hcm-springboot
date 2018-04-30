package br.com.litecode.domain.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Embeddable;

@Embeddable
@Getter
@Setter
public class UserSettings {
	private boolean notificationMessages;
	private boolean notificationSounds;

	public UserSettings() {
		notificationMessages = true;
		notificationSounds = true;
	}
}