package com.synapps.ea.reporting.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.BeanWrapperImpl;

/**
 * @author Mark Billingham
 *
 */
public class BeanValuePlaceholderResolver {
	private static final Pattern placeholderPattern = Pattern.compile("\\{(.*?)\\}");

	/**
	 * @param tokenized
	 * @param bean
	 * @return
	 */
	public static String resolvePlaceholders(String tokenized, Object bean) {
		Matcher matcher = placeholderPattern.matcher(tokenized);
		BeanWrapperImpl wrapper = new BeanWrapperImpl(bean);
		StringBuffer sb = new StringBuffer();

		while (matcher.find()) {
			String token = matcher.group(1);
			Object value = null;
			if (wrapper.isReadableProperty(token)) {
				value = wrapper.getPropertyValue(token);
			}
			if (value != null) {
				matcher.appendReplacement(sb, value.toString());
			} else {
				matcher.appendReplacement(sb, "");
			}
		}
		matcher.appendTail(sb);
		return sb.toString();

	}
}
