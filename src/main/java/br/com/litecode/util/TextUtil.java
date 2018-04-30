package br.com.litecode.util;

import java.text.Normalizer;

public final class TextUtil {
	public static String normalizeText(String text) {
		if (text == null) {
			throw new IllegalArgumentException("text is null");
		}
		return Normalizer.normalize(text.trim(), Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
	}
}