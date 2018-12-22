package com.prize.weather.util;

import java.util.regex.Pattern;

/**
 * 
 * @author wangzhong
 *
 */
public class MathUtils {
	
	public static boolean isNumeric(String str) {
		Pattern pattern = Pattern.compile("[-]?[0-9]+");
	    return pattern.matcher(str).matches();
	}

}
