package org.purl.accessor.util;

public class StringHelper {
	public static String escapeURL(String s) {
		return s.replaceAll("&", "&amp;");
	}
}
