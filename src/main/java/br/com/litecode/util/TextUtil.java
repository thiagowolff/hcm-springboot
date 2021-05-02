package br.com.litecode.util;

import java.text.Normalizer;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class TextUtil {
	public static String normalizeText(String text) {
		return Normalizer.normalize(text.trim(), Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
	}

	public static String toHtmlLineBreaks(String text) {
        return text.replaceAll(System.lineSeparator(), "<br/>");
	}

	public static boolean randomBoolean() {
        return ThreadLocalRandom.current().nextBoolean();
	}

	public static String randomString(List<String> strings) {
        return strings.get(ThreadLocalRandom.current().nextInt(0, strings.size()));
	}
}