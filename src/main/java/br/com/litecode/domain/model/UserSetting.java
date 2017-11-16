package br.com.litecode.domain.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Embeddable;

@Embeddable
@Getter
@Setter
public class UserSetting {
	private boolean notificationMessages;
	private boolean notificationSounds;

	public UserSetting() {
		notificationMessages = true;
		notificationSounds = true;
	}
}