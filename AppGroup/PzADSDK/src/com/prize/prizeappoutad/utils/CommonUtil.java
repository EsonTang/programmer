package com.prize.prizeappoutad.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class CommonUtil {

	/***
	 * json to object&lt;T&gt;
	 * 
	 * @param jsonString
	 * @param cls
	 * @return
	 */
	public static <T> T getObject(String jsonString, Class<T> cls) {
		T t = null;
		try {
			Gson gson = new Gson();
			t = gson.fromJson(jsonString, cls);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return t;
	}

	public static <T> String toGson(T cls) {
		String t = null;
		try {
			Gson gson = new Gson();
			t = gson.toJson(cls);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return t;
	}

	/***
	 * json to List&lt;T&gt;
	 * 
	 * @param jsonString
	 * @param cls
	 * @return
	 */
	public static <T> List<T> getObjects(String jsonString, Class<T[]> cls) {
		List<T> list = null;
		try {
			Gson gson = new Gson();
			list = gson.fromJson(jsonString, new TypeToken<List<T>>() {
			}.getType());
			list = stringToArray(jsonString, cls);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	public static <T> List<T> stringToArray(String s, Class<T[]> clazz) {
		T[] arr = new Gson().fromJson(s, clazz);
		return Arrays.asList(arr);
	}

	/***
	 * json to List<Map<String, Object>>
	 * 
	 * @param jsonString
	 * @return
	 */
	public static List<Map<String, Object>> listKeyMaps(String jsonString) {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		try {
			Gson gson = new Gson();
			list = gson.fromJson(jsonString,
					new TypeToken<List<Map<String, Object>>>() {
					}.getType());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}
}
