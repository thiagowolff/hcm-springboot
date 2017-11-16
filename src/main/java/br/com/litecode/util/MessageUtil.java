package br.com.litecode.util;

import java.text.MessageFormat;
import java.util.ResourceBundle;

public final class MessageUtil {
	private static final String BUNDLE_NAME = "messages";
	private static final ResourceBundle bundle;
	
	static {
		bundle = ResourceBundle.getBundle(BUNDLE_NAME);
	}
	
	public static String getMessage(String key, Object... params) {
		return MessageFormat.format(bundle.getString(key), params);
	}
}