package com.prize.left.page.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.text.InputFilter;
import android.text.Spanned;

public class FilterUtils {

	/***
	 * 过滤emoji
	 */
	public static InputFilter getEmojiFilter() {
		InputFilter emojiFilter = new InputFilter() {

			Pattern emoji = Pattern
					.compile(
							"[\ud83c\udc00-\ud83c\udfff]|[\ud83d\udc00-\ud83d\udfff]|[\u2600-\u27ff]",
							Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE);

			@Override
			public CharSequence filter(CharSequence source, int start, int end,
					Spanned dest, int dstart, int dend) {

				Matcher emojiMatcher = emoji.matcher(source);
				if (emojiMatcher.find()) {
					return "";
				}

				return null;
			}
		};
		return emojiFilter;
	}

}
