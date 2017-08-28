package br.com.litecode.util;

import java.time.*;
import java.util.Date;

public final class DateUtil {
	private static final ZoneId TIMEZONE = ZoneId.systemDefault();

	public static LocalDateTime toLocalDateTime(Date date) {
		return LocalDateTime.ofInstant(Instant.ofEpochMilli(date.getTime()), TIMEZONE);
	}

	public static LocalDate toLocalDate(Date date) {
		return toLocalDateTime(date).toLocalDate();
	}

	public static LocalTime toLocalTime(Date date) {
		return toLocalDateTime(date).toLocalTime();
	}

	public static Date atStartOfDay(Date date) {
		return Date.from(toLocalDate(date).atStartOfDay().atZone(TIMEZONE).toInstant());
	}

	public static Date atEndOfDay(Date date) {
		return Date.from(toLocalDate(date).atTime(23, 59, 59).atZone(TIMEZONE).toInstant());
	}

	public static Date fromLocalDateTime(LocalDateTime localDateTime) {
		return Date.from(localDateTime.atZone(TIMEZONE).toInstant());
	}
}
