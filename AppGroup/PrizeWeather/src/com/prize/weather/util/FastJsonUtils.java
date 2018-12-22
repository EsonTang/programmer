package com.prize.weather.util;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

/**
 * 
 * @author wangzhong
 * 
 */
public class FastJsonUtils {
	
	/**
	 * Converts JSON type of data into common string list.
	 * @param jsonData		JSON types of data.
	 * @return				String list.
	 * @throws Exception
	 */
	public static List<String> getListString(String jsonData) throws Exception {
		return JSON.parseArray(jsonData, String.class);
	}

	/**
	 * Converts JSON type of data into Java objects.
	 * @param jsonData		JSON types of data.
	 * @param clazz			Specify the Java objects.
	 * @return				Java object.
	 * @throws Exception
	 */
	public static <T> T getSingleBean(String jsonData, Class<T> clazz) throws Exception {
		return JSON.parseObject(jsonData, clazz);
	}

	/**
	 * Convert JSON types of data to the specified list of Java objects.
	 * @param jsonData		JSON types of data.
	 * @param clazz			Specify the Java objects.
	 * @return				The list of Java objects.
	 * @throws Exception
	 */
	public static <T> List<T> getListBean(String jsonData , Class<T> clazz) throws Exception {
		return JSON.parseArray(jsonData, clazz);
	}
	
	/**
	 * The JSON type of data into a more complex Java objects list.
	 * @param jsonData		JSON types of data.
	 * @return				More complex Java objects list.
	 * @throws Exception
	 */
	public static List<Map<String, Object>> getMapListBean(String jsonData) throws Exception {
		return JSON.parseObject(jsonData, new TypeReference<List<Map<String, Object>>>(){});
	}
	
	
	/**
	 * Object to json string.
	 * @param srcData
	 * @return
	 */
	public static String convertDataToJsonString(Object srcData){
		return JSON.toJSONString(srcData);
	}
	
}
