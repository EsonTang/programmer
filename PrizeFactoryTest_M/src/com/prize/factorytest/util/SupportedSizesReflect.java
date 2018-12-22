/**
 * SupportedSizesReflect.java
 * 版权所有(C) 2013 
 * 创建:cuiran 2013-7-22 下午4:54:22
 */
package com.prize.factorytest.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import android.hardware.Camera;
import android.hardware.Camera.Size;

/**
 * TODO 系统支持的分辨率
 * @author cuiran
 * @version 1.0.0
 */
public class SupportedSizesReflect {

	private static Method Parameters_getSupportedPreviewSizes = null;
	private static Method Parameters_getSupportedPictureSizes = null;

	static {
		initCompatibility();
	};

	private static void initCompatibility() {
		try {
			Parameters_getSupportedPreviewSizes = Camera.Parameters.class.getMethod(
					"getSupportedPreviewSizes", new Class[] {});

			Parameters_getSupportedPictureSizes = Camera.Parameters.class.getMethod(
					"getSupportedPictureSizes", new Class[] {});

		} catch (NoSuchMethodException nsme) {
			nsme.printStackTrace();
			Parameters_getSupportedPreviewSizes = Parameters_getSupportedPictureSizes = null;
		}
	}

	/**
	 * Android 2.1之后有效
	 * @param p
	 * @return Android1.x返回null
	 */
	public static List<Size> getSupportedPreviewSizes(Camera.Parameters p) {
		return getSupportedSizes(p, Parameters_getSupportedPreviewSizes);
	}

	public static List<Size> getSupportedPictureSizes(Camera.Parameters p){
		return getSupportedSizes(p, Parameters_getSupportedPictureSizes);
	}	

	@SuppressWarnings("unchecked")
	private static List<Size> getSupportedSizes(Camera.Parameters p, Method method){
		try {
			if (method != null) {
				return (List<Size>) method.invoke(p);
			} else {
				return null;
			}
		} catch (InvocationTargetException ite) {
			Throwable cause = ite.getCause();
			if (cause instanceof RuntimeException) {
				throw (RuntimeException) cause;
			} else if (cause instanceof Error) {
				throw (Error) cause;
			} else {
				throw new RuntimeException(ite);
			}
		} catch (IllegalAccessException ie) {
			return null;
		}
	}

}
