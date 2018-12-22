package com.prize.prizeappoutad.utils;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;



public class Verification {
	private String key = "0a14fc502731prizecce34";
	private static Verification instance = new Verification();
	private Verification() {}
	
	public static Verification getInstance() {
		return instance;
	}

	
	public void setKey(String key) {
		this.key = key;
	}
	
	public String getSign(Map<String, String> params) {
		TreeMap<Object, Object> treeMap = new TreeMap<Object, Object>(params);
		
		return getSign(treeMap);
	}
	
	public String getSign(TreeMap<Object, Object> treeMap) {
		StringBuilder builder = new StringBuilder();
		for(Object k : treeMap.keySet()) {
			builder.append(k).append("=").append(treeMap.get(k).toString()).append("&");
		}
		if (builder.length() > 1) {
			builder.deleteCharAt(builder.length()-1);
		}
		builder.append(key);
		return MD5.md5(builder.toString());
	}
	
	public String getSign(List<KeyValue> list) {
		StringBuilder builder = new StringBuilder();
		for(KeyValue keyValue : list) {
			builder.append(keyValue.key).append("=").append(keyValue.value.toString()).append("&");
		}
		if (builder.length() > 1) {
			builder.deleteCharAt(builder.length()-1);
		}
		builder.append(key);
		return MD5.md5(builder.toString());
	}
}
