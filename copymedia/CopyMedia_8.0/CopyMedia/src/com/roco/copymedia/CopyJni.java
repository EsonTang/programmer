package com.roco.copymedia;

import java.io.File;

import android.content.Context;

public class CopyJni {
	static{
		System.loadLibrary("jni_copymedia");
	}
	
	public static native void startProcess(Context ctx, String src, String dst);
	
	public static native void doSomething(File src, File dst);
	
	public static native boolean isCopyDown();
	
	public static native void copyDown(Context ctx, String dst);

}
