package com.roco.copymedia;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Prop {

	public static void set(String key, String val) {
		doMethod("android.os.SystemProperties", "set", new Class[]{String.class,String.class}, new String[]{key,val});
	}

	public static Object doMethod(String cls, String method, Class[] types, Object[] args) {
		Class clzz = null;
		try {
			clzz = Class.forName(cls);

			Method mthd = clzz.getMethod(method, types);
			System.out.println("method "+mthd);
			Object ret = mthd.invoke(clzz, args);
			System.out.println(method +" succcess !!! ret="+ret);
			return ret;
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			System.out.println(method+" failed !!!");
		}
		return null;
	}

	public static String get(String key) {
		return get(key,"");
	}

	public static String get(String key, String defVal) {
		Object ret = doMethod("android.os.SystemProperties", "get", new Class[]{String.class,String.class}, new String[]{key,defVal});
		if(ret == null){
			return defVal;
		}
		return ret.toString();
	}
}
