package br.com.litecode.util;

import com.google.common.base.Strings;

import java.text.MessageFormat;
import java.util.MissingResourceException;
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

	public static String getFormattedLabel(String labelKey, String value) {
	    if (Strings.isNullOrEmpty(value)) {
	        return "";
        }

        String label;
        try {
            label = bundle.getString(labelKey);
        } catch (MissingResourceException e) {
	        label = "[" + labelKey + "]";
        }

		return String.format("%s: %s", label, value);
	}
}