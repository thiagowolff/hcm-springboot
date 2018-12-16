package br.com.litecode.util;

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

@Slf4j
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

	public static void reloadMessagesResourceBundle() {
		try {
			Field field = ResourceBundle.getBundle(BUNDLE_NAME).getClass().getSuperclass().getDeclaredField("cacheList");
			field.setAccessible(true);
			Map cache = (Map) field.get(null);
			cache.clear();

			ResourceBundle.clearCache();
		} catch (IllegalAccessException | NoSuchFieldException e) {
			log.warn("Unable to reload messages resource bundle. " + e);
		}
	}
}