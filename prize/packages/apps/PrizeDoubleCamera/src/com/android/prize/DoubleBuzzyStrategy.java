package com.android.prize;

import java.io.ByteArrayOutputStream;
import com.android.camera.R;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Arrays;

import com.android.camera.CameraActivity;
import com.android.camera.ui.PreviewSurfaceView;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import com.android.camera.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.FrameLayout;
import android.app.ActivityManagerNative;
import android.app.IActivityManager;

public class DoubleBuzzyStrategy implements IBuzzyStrategy {

	protected static final String TAG = "GYLog DoubleBuzzyStrategy";
	private static final int PREVIEW_SIZE_SECOND_WIGHT = 320;
	private static final int PREVIEW_SIZE_SECOND_HEIGHT = 240;
	private Camera mSecondaryCamera = null;
	int mainIndex = 0;
	int secondIndex = 0;
	private boolean isMainColorSimilar = false;
	private boolean isSecondColorSimilar = false;
	private byte[] mMainData;
	private byte[] mSecondaryData;
	private Bitmap bmpMain;
	private Bitmap bmpSecond;
	private static final int BACK_CAMERA_SECOND = 2;
	private Activity mActivity;
	private static final int COLOR_ABSOLUTE_THRESHOLD = 20;// Color difference
	// threshold
	private static final float COLOR_PROPORTION_THRESHOLD = 0.7f;// Color
	private static final int BRIGHTNESS_VALUE_THRESHOLD = 5;// brightness_value
	private static final int BACK_CAMERA_MAIN = 1;
	private static final int PICKER_POINT_NUM = 8; // GET_POINT_NUM x
	// GET_POINT_NUM
	
	private FrameLayout mCurSurfaceViewLayout;
	private SurfaceView mSurfaceView;
	private SurfaceHolder mSurfaceHolder;
	private boolean mIsReadSurfaceData = false;
	
	private static final int CALCULATE_BITMAP_MIN_DIMENSION = 320;
	
	private ColorHistogram mColorHistogram;

    private int mBrightnessValue = 0;
	private PreviewCallback mOneShotPreviewCallback = new PreviewCallback() {
		@Override
		public void onPreviewFrame(byte[] data, Camera camera) {
			// Log.d(TAG, "[Second mOneShotPreviewCallback.onPreviewFrame]");
			if (secondIndex > 3) {
				// transforms NV21 pixel data into RGB pixels
				// int aaa = camera.getParameters().getPreviewFormat();
				Log.i(TAG, "[mOneShotPreviewCallback]second-------------------->>>preview");
				//setSecondaryData(data);
				Parameters mParameters = mSecondaryCamera.getParameters();
				mBrightnessValue = mParameters.getInt("brightness_value");
				onBrightnessValue();
				Log.i(TAG, "[mOneShotPreviewCallback]second---->>>mBrightnessValue = "+mBrightnessValue);
				secondIndex = 0;
			}
			secondIndex++;
		}
	};
	private static int tempConut = 0;
	int brightnessValuesLenth = 3;
	int[] brightnessValues=new int[brightnessValuesLenth];
	private void onBrightnessValue(){
		try {
			brightnessValues[tempConut] = mBrightnessValue;
			tempConut++;
			if(tempConut>(brightnessValuesLenth-1)){
				Log.i(TAG, "second-------------------->>>isOcclusion()");
				tempConut = 0;
				getBrightnessValueState();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private boolean getBrightnessValueState(){
		try {
			int curIndex = 0;
			for (int i = 0; i < brightnessValues.length; i++) {
				if(brightnessValues[i]<BRIGHTNESS_VALUE_THRESHOLD){
					curIndex++;
				}
			}
			isOcclusionValue = curIndex>(brightnessValuesLenth-1)?true:false;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	public DoubleBuzzyStrategy(Activity activity) {
		mActivity = activity;
	}
	
	@Override
	public void openCamera() {
		try {
			if (mSecondaryCamera == null) {
				Log.d(TAG, "mSecondaryCamera.openCamera()");
				setPropery("1");
				mSecondaryCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
			}
			Log.d(TAG, "mSecondaryCamera.openCamera() down");
		} catch (Exception exception) {
			mSecondaryCamera = null;
			Log.d(TAG, "mSecondaryCamera.openCamera error:" + exception.getMessage());
		}
	}

	@Override
	public void closeCamera() {
		if (mSecondaryCamera != null) {
			Log.d(TAG, "mSecondaryCamera closeCamera");
			mSecondaryCamera.setPreviewCallback(null);
			mSecondaryCamera.stopPreview();
			mSecondaryCamera.release();
			mSecondaryCamera = null;
			setPropery("0");
		}
	}

	private static boolean isOcclusionValue = false;
	@Override
	public boolean isOcclusion() {
		return isOcclusionValue;
		/*if (bmpMain != null) {
			Log.i(TAG, "main-------------------->>>getColorProportion()");
			isMainColorSimilar = getColorProportion(zoomImage(bmpMain, PREVIEW_SIZE_SECOND_WIGHT, PREVIEW_SIZE_SECOND_HEIGHT));
		}
		if (bmpSecond != null) {
			Log.i(TAG, "second-------------------->>>getColorProportion()");
			isSecondColorSimilar = getColorProportion(bmpSecond);
		}
		Log.d(TAG, "[DoubleCameraMode] mHandler() isMainColorSimilar = " + isMainColorSimilar + ", isSecondColorSimilar = " + isSecondColorSimilar);
		return isMainColorSimilar || isSecondColorSimilar;*/
	}

	@Override
	public void saveMainBmp(byte[] data) {
		saveBitmap(data, BACK_CAMERA_MAIN);
	}

	@Override
	public void startPreview() {
		if (mSecondaryCamera != null) {
			try {
				Camera.Parameters parameters = mSecondaryCamera.getParameters();
				parameters.setPreviewSize(PREVIEW_SIZE_SECOND_WIGHT, PREVIEW_SIZE_SECOND_HEIGHT);
				parameters.setZSDMode("off");
				mSecondaryCamera.setParameters(parameters);
				mSecondaryCamera.setPreviewDisplay(mSurfaceHolder);
				mSecondaryCamera.setPreviewCallback(mOneShotPreviewCallback);
				mSecondaryCamera.startPreview();
			} catch (Exception e) {
				e.printStackTrace();
				Log.d(TAG, "startPreview mSecondaryCamera == null");
			}
		}
	}
	
	public Bitmap zoomImage(Bitmap bgimage, double newWidth, double newHeight) {
		// 获取这个图片的宽和高
		float width = bgimage.getWidth();
		float height = bgimage.getHeight();
		// 创建操作图片用的matrix对象
		Matrix matrix = new Matrix();
		// 计算宽高缩放率
		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;
		// 缩放图片动作
		matrix.postScale(scaleWidth, scaleHeight);
		Bitmap bitmap = Bitmap.createBitmap(bgimage, 0, 0, (int) width, (int) height, matrix, true);
		return bitmap;
	}

	private void saveBitmap(byte[] data, int cameraID) {
		long starttime = System.currentTimeMillis();
		Size size = null;
		if (cameraID == BACK_CAMERA_SECOND) {
			if (mSecondaryCamera != null) {
				size = mSecondaryCamera.getParameters().getPreviewSize();
			}
		} else {
			if (mActivity instanceof CameraActivity) {
				size = ((CameraActivity) mActivity).getParameters().getPreviewSize();
			}
		}
		if (size == null) {
			return;
		}
		final YuvImage image = new YuvImage(data, ImageFormat.NV21, size.width, size.height, null);
		ByteArrayOutputStream os = new ByteArrayOutputStream(data.length);
		if (!image.compressToJpeg(new Rect(0, 0, size.width, size.height), 100, os)) {
			return;
		}
		byte[] tmp = os.toByteArray();
		Bitmap bmp = BitmapFactory.decodeByteArray(tmp, 0, tmp.length);
		if (cameraID == BACK_CAMERA_MAIN) {
			bmpMain = bmp;
		} else {
			bmpSecond = bmp;
		}
	}

	/**
	 * 计算图片bitmap颜色比重
	 */
	private boolean getColorProportion(Bitmap bmp) {
		
		Bitmap mBitmap = BitmapScale(bmp);
		int w = mBitmap.getWidth();
		int h = mBitmap.getHeight();
		int[] pixels = new int[w*h];
		mBitmap.getPixels(pixels, 0, w, 0, 0, w, h);
		if(mColorHistogram == null){
			mColorHistogram = new ColorHistogram();
		}
		
		mColorHistogram.setPixels(pixels);
		int color = mColorHistogram.getAverageColors();
		return isOverCameraColor(color);
	}
	
	private boolean isOverCameraColor(int color) {
		// TODO Auto-generated method stub
		Log.d(TAG, "isOverCameraColor color = "+color);
		if(color <= 10 ){
			return true;
		}
		return false;
	}
	
	/**
	 * 颜色值是否相近
	 * 
	 * @param basecolor
	 * @param contrastcolor
	 * @return
	 */
	private boolean isSimilarPixel(int basecolor, int contrastcolor) {
		int temp = Math.abs(basecolor - contrastcolor);
		return temp < COLOR_ABSOLUTE_THRESHOLD ? true : false;
	}
	
	private int calculateAverage(int[] data) {
		Arrays.sort(data);
		for (int i = 0; i < data.length; i++) {
			// Log.v(TAG, "data["+i+"] = " + data[i]);
		}
		int temp = 0;
		for (int i = 10; i < (data.length - 10); i++) {
			temp += data[i];
		}
		// Log.v(TAG, "[calculateAverage] temp = " + Integer.toHexString(temp));
		return temp / (data.length - 20);
	}

	@Override
	public int getCheckTime() {
		return 700;
	}
	
	public void attachSurfaceViewLayout() {
		Log.i(TAG, "mSecondaryCamera [attachSurfaceViewLayout] begin ");

		if (mSurfaceView == null) {
			FrameLayout surfaceViewRoot = (FrameLayout) mActivity.findViewById(R.id.camera_surfaceview_root);
			mCurSurfaceViewLayout = (FrameLayout) mActivity.getLayoutInflater()
					.inflate(R.layout.secondary_camera_preview_layout, null);
			mSurfaceView = (PreviewSurfaceView) mCurSurfaceViewLayout.findViewById(R.id.secondary_camera_preview);
			mSurfaceHolder = mSurfaceView.getHolder();
			surfaceViewRoot.addView(mCurSurfaceViewLayout);
		}
		Log.i(TAG, "mSecondaryCamera [attachSurfaceViewLayout] end ");
	}

	public void detachSurfaceViewLayout() {
		Log.i(TAG, "detachSurfaceViewLayout [attachSurfaceViewLayout] begin ");

		if (mSurfaceView != null) {
			FrameLayout surfaceViewRoot = (FrameLayout) mActivity.findViewById(R.id.camera_surfaceview_root);
			surfaceViewRoot.removeView(mCurSurfaceViewLayout);
			mSurfaceView = null;
		}
		Log.i(TAG, "detachSurfaceViewLayout [attachSurfaceViewLayout] end ");
	}

	public void setIsReadSurfaceData(boolean isReading){
		mIsReadSurfaceData = isReading;
	}
	
	public boolean isReadSurfaceData(){
		return mIsReadSurfaceData;
	}
	
	public Bitmap BitmapScale(Bitmap bitmap) { 
		int minDimension = Math.min(bitmap.getWidth(), bitmap.getHeight());
		if (minDimension <= CALCULATE_BITMAP_MIN_DIMENSION) { 
			// If the bitmap is small enough already, just return it 
			return bitmap; 
		} 
	    float scaleRatio = CALCULATE_BITMAP_MIN_DIMENSION / (float) minDimension; 
		return Bitmap.createScaledBitmap(bitmap, Math.round(bitmap.getWidth() * scaleRatio), Math.round(bitmap.getHeight() * scaleRatio), false); 
	}
	
	public void setMainData(byte[] data){
		mMainData = data;
	}
	
	public void setSecondaryData(byte[] data){
		mSecondaryData = data;
	}
	
	/** * Class which provides a histogram for RGB values. */ 
	    class ColorHistogram { 
		private  int[] mColors; 
		private  int[] mColorCounts; 
		private  int mNumberColors; 
		private  int mAverageColors; 
		/** * A new {@link ColorHistogram} instance. * * @param pixels array of image contents */ 
		ColorHistogram() { 
			// Sort the pixels to enable counting below 
		} 
		
		public void setPixels(int[] pixels){
			Arrays.sort(pixels); // Count number of distinct colors 
			mNumberColors = countDistinctColors(pixels); // Create arrays 
			mColors = new int[mNumberColors]; 
			mColorCounts = new int[mNumberColors]; // Finally count the frequency of each color 
			countFrequencies(pixels); 
			mAverageColors = getAverageColor(pixels);
		}
		/** * @return 获取共用多少柱不同颜色 number of distinct colors in the image. */ 
		private int getNumberOfColors() { 
			return mNumberColors; 
		} 
		/** * @return 获取排好序后的不同颜色的数组 an array containing all of the distinct colors in the image. */ 
		private int[] getColors() {
			return mColors; 
		} 
		/** * @return 获取保存每一柱有多高的数组 an array containing the frequency of a distinct colors within the image. */ 
		private int[] getColorCounts() { 
			return mColorCounts; 
		} 
	   
		private int  getAverageColors() { 
			return mAverageColors; 
		} 
		
		//计算共用多少柱不同颜色 
		private int countDistinctColors(final int[] pixels) { 
			if (pixels.length < 2) { // If we have less than 2 pixels we can stop here 
				return pixels.length; 
			} 
			// If we have at least 2 pixels, we have a minimum of 1 color... 
			int colorCount = 1; 
			int currentColor = pixels[0]; // Now iterate from the second pixel to the end, counting distinct colors 
			for (int i = 1; i < pixels.length; i++) { // If we encounter a new color, increase the population 
				if (pixels[i] != currentColor) { 
					currentColor = pixels[i]; 
					colorCount++; 
				} 
			} 
			return colorCount; 
		} 
		
		//计算每一柱有多高
		private void countFrequencies(final int[] pixels) { 
			if (pixels.length == 0) {
				return; 
			} 
			int currentColorIndex = 0; 
			int currentColor = pixels[0]; 
			mColors[currentColorIndex] = currentColor; 
			mColorCounts[currentColorIndex] = 1; 
			if (pixels.length == 1) { // If we only have one pixel, we can stop here 
				return; 
			}
			// Now iterate from the second pixel to the end, population distinct colors 
			for (int i = 1; i < pixels.length; i++) { 
				if (pixels[i] == currentColor) { 
					// We've hit the same color as before, increase population 
					mColorCounts[currentColorIndex]++; 
				} else { // We've hit a new color, increase index 
					currentColor = pixels[i]; 
					currentColorIndex++; 
					mColors[currentColorIndex] = currentColor;
					mColorCounts[currentColorIndex] = 1; 
				} 
			} 
		}
		
		//计算最高一注是什么颜色
		public int getMaxWeightColor(){
			int colorCount = mColorCounts[0];
			int color = mColors[0];
			for(int i = 0; i < mColorCounts.length; i++){
				if(mColorCounts[i] > colorCount){
					colorCount = mColorCounts[i];
					color = mColors[i];
				}
			}
			return color;
		}
		
		private  int getAverageColor(int[] pixels){
			int color = 0;
			for(int i = 0; i < pixels.length; i = i+5){
				color = color + Color.green(pixels[i]) ;
			}
			return color/pixels.length;
		}
	}
	    
	private void setPropery(String value){
			/*try{
				Log.v(TAG, "[DoubleCamera]setPropery  start...");
				android.os.SystemProperties.set("sys.prize.dualcamera",value);
				Log.v(TAG, "[DoubleCamera]setPropery  end...");
			} catch (Exception e) {  
				// TODO Auto-generated catch block  
				e.printStackTrace();  
				Log.v(TAG, "[DoubleCamera]setPropery  Exception...");
			}*/
			Log.v(TAG, "[DoubleCamera] set propery start!");
			try {
				IActivityManager am = ActivityManagerNative.getDefault();
				//am.setPropertyForExternal("sys.prize.dualcamera",value);
				Log.v(TAG, "[DoubleCamera] set propery end!");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
}
