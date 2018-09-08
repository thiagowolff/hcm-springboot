package br.com.litecode.util;

import java.text.Normalizer;
import java.util.concurrent.ThreadLocalRandom;

public final class TextUtil {
	public static String normalizeText(String text) {
		if (text == null) {
			throw new IllegalArgumentException("text is null");
		}
		return Normalizer.normalize(text.trim(), Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
	}

	public static boolean randomBoolean() {
        return ThreadLocalRandom.current().nextBoolean();
	}
}