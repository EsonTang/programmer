package com.prize.runoldtest.util;

public class ResultUtil {
	private static boolean isTakePictureSuccess = true;
	private static boolean isAutoFocusSuccess = true;
	private static boolean isOpenCameraSuccess = true;
	private static boolean isCloseCameraSuccess = true;
	public static  boolean isShutter = false;
	public static boolean isShutter() {
		return isShutter;
	}
	public static void setShutter(boolean isShutter) {
		ResultUtil.isShutter = isShutter;
	}
	public static boolean isTakePictureSuccess() {
		return isTakePictureSuccess;
	}
	public static void setTakePictureSuccess(boolean isTakePictureSuccess) {
		ResultUtil.isTakePictureSuccess = isTakePictureSuccess;
	}
	public static boolean isAutoFocusSuccess() {
		return isAutoFocusSuccess;
	}
	public static void setAutoFocusSuccess(boolean isAutoFocusSuccess) {
		ResultUtil.isAutoFocusSuccess = isAutoFocusSuccess;
	}
	public static boolean isOpenCameraSuccess() {
		return isOpenCameraSuccess;
	}
	public static void setOpenCameraSuccess(boolean isOpenCameraSuccess) {
		ResultUtil.isOpenCameraSuccess = isOpenCameraSuccess;
	}
	public static boolean isCloseCameraSuccess() {
		return isCloseCameraSuccess;
	}
	public static void setCloseCameraSuccess(boolean isCloseCameraSuccess) {
		ResultUtil.isCloseCameraSuccess = isCloseCameraSuccess;
	}
	
}
