package br.com.litecode.service.push;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum PushChannel {
	REFRESH,
	PROGRESS,
	NOTIFY,
	ALARM
}
