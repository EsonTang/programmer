package com.android.prize;

import android.view.SurfaceHolder;

public interface IBuzzyStrategy {
	void openCamera();
	void closeCamera();
	void startPreview();
	boolean isOcclusion();
	void saveMainBmp(byte[] data);
	int getCheckTime();
	public void attachSurfaceViewLayout();
	public void detachSurfaceViewLayout();
}
