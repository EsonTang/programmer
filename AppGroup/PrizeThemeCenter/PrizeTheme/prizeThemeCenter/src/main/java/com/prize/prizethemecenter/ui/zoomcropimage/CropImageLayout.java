package com.prize.prizethemecenter.ui.zoomcropimage;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Point;
import android.media.ExifInterface;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.prize.prizethemecenter.R;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * 剪切图控件
 * @author zhy
 *
 */
public class CropImageLayout extends RelativeLayout {

	protected static final String TAG = CropImageLayout.class.getSimpleName();
	
	/**
	 * 默认缩放最小值
	 */
	private static final float DEFAULT_SCALE_MIN = 0.5f;
	/**
	 * 默认缩放最大值
	 */
	private static final float DEFAULT_SCALE_MAX = 4.0f;
	
	private ZoomCropImageView mZoomCropImageView;
	private CropImageBorderView mCropImageBorderView;

	/**
	 * 输出图片宽度
	 */
	private int mOutputWidth;
	/**
	 * 输出图片高度
	 */
	private int mOutputHeight;
	
	private int mWidth;
	private int mHeight;
	
	private Bitmap originBitmap;
	/**最大剪切高度及宽度**/
	private int maxWidth, maxHeight;
	
	private int finalWidth, finalHeight;

	public CropImageLayout(Context context){
		this(context, null);
	}
	
	public CropImageLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public CropImageLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

		TypedArray tArray = context.obtainStyledAttributes(attrs,
                R.styleable.CropImage);
		float scaleMin = tArray.getFloat(R.styleable.CropImage_scaleMin, DEFAULT_SCALE_MIN);
		float scaleMax = tArray.getFloat(R.styleable.CropImage_scaleMax, DEFAULT_SCALE_MAX);
//		float borderWidth = tArray.getDimension(R.styleable.CropImage_borderWidth,
//				getResources().getDimension(R.dimen.default_border_width));
		int borderColor = tArray.getColor(R.styleable.CropImage_borderColor, Color.TRANSPARENT);
//		int shaderColor = tArray.getColor(R.styleable.CropImage_shaderColor,
//				getResources().getColor(R.color.default_shader_color));
		int cropShape = tArray.getInt(R.styleable.CropImage_cropShape, 
				CropShape.SHAPE_RECTANGLE);
		tArray.recycle();
		
		mZoomCropImageView = new ZoomCropImageView(context);
		mZoomCropImageView.setScaleSize(scaleMin, scaleMax);
		mZoomCropImageView.setCropShape(cropShape);
		mCropImageBorderView = new CropImageBorderView(context);
//		mCropImageBorderView.setBorderWidth(borderWidth);
		mCropImageBorderView.setBorderColor(borderColor);
//		mCropImageBorderView.setShaderColor(shaderColor);
		mCropImageBorderView.setCropShape(cropShape);

		android.view.ViewGroup.LayoutParams lp = new LayoutParams(
				android.view.ViewGroup.LayoutParams.MATCH_PARENT,
				android.view.ViewGroup.LayoutParams.MATCH_PARENT);

		this.addView(mZoomCropImageView, lp);
//		this.addView(mCropImageBorderView, lp);
		
		WindowManager windowManager = (WindowManager)getContext()
				.getSystemService(Context.WINDOW_SERVICE);
		Display windowDisplay = windowManager.getDefaultDisplay();
		Point size = new Point();
		windowDisplay.getSize(size);
		Log.i(TAG, "Screen Width = " + size.x);
		Log.i(TAG, "Screen Height = " + size.y);
		maxWidth = size.x;
		maxHeight = size.y;
		size = null;
    }

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		mWidth = w;
		mHeight = h;
		initCropSize();
	}
	
	/**
	 * 裁切图片
	 * 
	 * @return
	 */
	public Bitmap crop() {
		return mZoomCropImageView.crop(finalWidth, finalHeight);
	}
	
	/**
	 * 必须提供Uri，否则无法获取到旋转信息
	 * @param uri
	 */
	public void setImageURI(Uri uri){
		String path = uri.getPath();
		int degrees = readImageRotationDegree(path);
//		originBitmap = rotaingBitmap(degrees,
//				sampleBitmap(getContext(), uri, maxWidth, maxHeight));
		try {
			originBitmap = rotaingBitmap(degrees,
					zoomBitmap(getContext(), uri, maxWidth, maxHeight));
		}
		catch (Exception e) {
			e.printStackTrace();
			return;
		}
		mZoomCropImageView.setImageBitmap(originBitmap);
	}
	
	/**
	 * 无须旋转的图片
	 * @param bitmap
	 */
	public void setBitmap(Bitmap bitmap) {
		if (null == bitmap)
			return;
		originBitmap = zoomTrue(bitmap, maxWidth, maxHeight);
		mZoomCropImageView.setImageBitmap(originBitmap);
	}
	/***
	 * 设置剪切框的可见性
	 * @param gone
	 */
	public void setBorderGone(boolean gone) {
		if (gone) {
			mCropImageBorderView.setVisibility(View.GONE);
			mZoomCropImageView.resetMatrix();
			mZoomCropImageView.setCanScale(false);
			finalWidth = maxWidth;
			finalHeight = maxHeight;
			mZoomCropImageView.setCropSize(maxWidth, maxHeight);
			mCropImageBorderView.setCropSize(maxWidth, maxHeight);
		}
		else {
			mZoomCropImageView.setCanScale(true);
			mCropImageBorderView.setVisibility(View.VISIBLE);
			
			finalWidth = mOutputWidth;
			finalHeight = mOutputHeight;
			mZoomCropImageView.setCropSize(mOutputWidth, mOutputHeight);
			mCropImageBorderView.setCropSize(mOutputWidth, mOutputHeight);
		}
	}
	/**
	 * 设置输出图片尺寸
	 * @param outputWidth
	 * @param outputHeight
	 */
	public void setOutputSize(int outputWidth, int outputHeight){
		mOutputWidth = outputWidth;
		mOutputHeight = outputHeight;
		finalWidth = mOutputWidth;
		finalHeight = outputHeight;
		initCropSize();
	}
	
	/**
	 * 设置切割形状
	 * @param cropShape
	 */
	public void setCropShape(int cropShape){
		mCropImageBorderView.setCropShape(cropShape);
		mZoomCropImageView.setCropShape(cropShape);
	}
	
	private void initCropSize(){
		if(mWidth != 0 && mHeight != 0
				&& finalWidth != 0 && finalHeight != 0) {
			final double CROP_SIZE_RATIO = 1;
			double scaleWidth = Math.floor(mWidth * CROP_SIZE_RATIO * 1.0f / finalWidth);
			double scaleHeight = Math.floor(mHeight * CROP_SIZE_RATIO * 1.0f / finalHeight);
			//放大倍数取较小值
			double scale = Math.min(scaleWidth, scaleHeight);
			
			int mCropWidth = (int)(finalWidth * scale);
			int mCropHeight = (int)(finalHeight * scale);
			
			mZoomCropImageView.setCropSize(mCropWidth, mCropHeight);
			mCropImageBorderView.setCropSize(mCropWidth, mCropHeight);
		}
	}

	/**
	 * 读取图片属性：旋转的角度
	 * @param path 图片绝对路径
	 * @return degree旋转的角度
	 */
	public static int readImageRotationDegree(String path) {
		int degree = 0;
		try {
			ExifInterface exifInterface = new ExifInterface(path);
			int orientation = exifInterface.getAttributeInt(
					ExifInterface.TAG_ORIENTATION,
					ExifInterface.ORIENTATION_NORMAL);
			switch (orientation) {
				case ExifInterface.ORIENTATION_ROTATE_90:
					degree = 90;
					break;
				case ExifInterface.ORIENTATION_ROTATE_180:
					degree = 180;
					break;
				case ExifInterface.ORIENTATION_ROTATE_270:
					degree = 270;
					break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return degree;
	}

	/**
	 * 旋转图片
	 * @param degrees
	 * @param bitmap
	 * @return Bitmap
	 */
	public static Bitmap rotaingBitmap(int degrees , Bitmap bitmap) {
		// 旋转图片 动作
		Matrix matrix = new Matrix();;
		matrix.postRotate(degrees);
		// 创建新的图片
		Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
				bitmap.getWidth(), bitmap.getHeight(), matrix, true);
		return resizedBitmap;
	}

	/**
	 * 压缩图片尺寸
	 * @return
	 */
	public static Bitmap sampleBitmap(Context context, Uri uri,
									  int width, int height){

		Bitmap bitmap = null;
		//先量尺寸，如果太大，要作sample，否则会报OutOfMemory错误
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		try {
			BitmapFactory.decodeStream(context.getContentResolver()
					.openInputStream(uri), null, options);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
			return bitmap;
		}
		Log.i(TAG, "Original Width = " + options.outWidth);
		Log.i(TAG, "Original Height = " + options.outHeight);

		int sampleWidth = 0;
		int sampleHeight = 0;
		if(options.outWidth < options.outHeight){
			if(width < height){
				sampleWidth = options.outWidth / width;
				sampleHeight = options.outHeight / height;
			}else{
				sampleWidth = options.outWidth / height;
				sampleHeight = options.outHeight / width;
			}
		}else{
			if(width < height){
				sampleWidth = options.outHeight / width;
				sampleHeight = options.outWidth / height;
			}else{
				sampleWidth = options.outHeight / height;
				sampleHeight = options.outWidth / width;
			}
		}
		int sampleSize = Math.max(sampleWidth, sampleHeight);
		options = new BitmapFactory.Options();
		options.inJustDecodeBounds = false;
		// options.inSampleSize = sampleSize;
		options.inSampleSize = 1; // 无须放大或缩小 fanjunchen
		try {
			bitmap = BitmapFactory.decodeStream(context.getContentResolver()
					.openInputStream(uri), null, options);
			Log.i(TAG, "Scaled Width = " + bitmap.getWidth());
			Log.i(TAG, "Scaled Height = " + bitmap.getHeight());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return bitmap;
	}
	
	/**
	 * 放大缩小图片
	 */
	public static Bitmap zoomBitmap(Context ctx, Uri uri, int w, int h) {
		Bitmap bitmap = null;
		//先量尺寸，如果太大，要作sample，否则会报OutOfMemory错误
		BitmapFactory.Options options = new BitmapFactory.Options();
		try {
			options.inJustDecodeBounds = false;
			options.inPreferredConfig = Bitmap.Config.RGB_565; // 会影响图片效果， 可以去掉
			Bitmap a = BitmapFactory.decodeStream(ctx.getContentResolver()
					.openInputStream(uri), null, options);
			
			bitmap = zoomTrue(a, w, h);
			if (bitmap != a && a != null)
				a.recycle();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			bitmap = null;
		} catch (OutOfMemoryError err) {
			err.printStackTrace();
			bitmap = null;
		}
		options = null;
		return bitmap;
	}
	/***
	 * 需要缩放到指定的高度或宽度，以最小的为准备
	 * @param srcBitmap
	 * @param w
	 * @param h
	 */
	public static Bitmap zoomTrue(Bitmap srcBitmap, int w, int h) {
		Bitmap bitmap = null;
		try {
			float scaleWidht = 1;
			float scaleHeight = 1;
			
			int width = srcBitmap.getWidth();
			int height = srcBitmap.getHeight();
			if(width < height){
				if(w < h){
					scaleWidht = (float)(w * 1.0f / width);
					scaleHeight = (float)(h * 1.0f / height);
				}else{
					scaleWidht = (float)(h * 1.0f / width);
					scaleHeight = (float)(w * 1.0f / height);
				}
			} else {
				if(w < h){
					scaleWidht = (float)(w * 1.0f / height);
					scaleHeight = (float)(h * 1.0f / width);
				} else {
					scaleWidht = (float)(h * 1.0f / height);
					scaleHeight = (float)(w * 1.0f / width);
				}
			}
			
			float scale = Math.max(scaleWidht, scaleHeight);
			
//			if (scale == 1.0f) { // 不缩小
				bitmap = srcBitmap;
//				return bitmap;
//			}
//			Matrix matrix = new Matrix();
//			matrix.postScale(scale, scale);
//			Bitmap newbmp = Bitmap.createBitmap(srcBitmap, 0, 0, width, height,
//					matrix, true);
//			matrix = null;
//			bitmap = newbmp;
		} catch (OutOfMemoryError err) {
			err.printStackTrace();
		}
		return bitmap;
	}

	/**
	 * 返回最初的图片
	 * @return
	 */
	public Bitmap getBitmap() {
		return originBitmap;
	}
	
	public void destroy() {
		if (originBitmap != null)
			originBitmap.recycle();
		if (mCropImageBorderView != null)
			mCropImageBorderView.destroy();
	}
}
