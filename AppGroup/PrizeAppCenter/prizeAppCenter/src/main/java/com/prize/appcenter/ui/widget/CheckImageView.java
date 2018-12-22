package com.prize.appcenter.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.prize.appcenter.R;

/**
 * 
 ** 
 * 被选中或未被选中的ImageView
 * 
 * @author zhouerlong
 * @version V1.0
 */
public class CheckImageView extends ImageView {
	private Paint paint;
	private Paint paintBorder;
	private Bitmap mSrcBitmap;
	private Drawable mCheck;
	private Drawable mNormal;
	/**
	 * 圆角的弧度
	 */
	private float mRadius;
	private boolean isCheck = false;

	public boolean isCheck() {
		return isCheck;
	}

	public void setCheck(boolean isCheck) {
		this.isCheck = isCheck;
	}

	public CheckImageView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		TypedArray ta = context.obtainStyledAttributes(attrs,
				R.styleable.CustomImageView, defStyleAttr, 0);
		mCheck = context.getResources().getDrawable(
				R.drawable.onekey_checked_selector);
		mNormal = context.getResources().getDrawable(
				R.drawable.onekey_unchecked_selector);
		mRadius = ta.getDimension(R.styleable.CustomImageView_iv_radius, 0);
		int srcResource = attrs.getAttributeResourceValue(
				"http://schemas.android.com/apk/res/android", "src", 0);
		if (srcResource != 0)
			mSrcBitmap = BitmapFactory.decodeResource(getResources(),
					srcResource);
		ta.recycle();
		paint = new Paint();
		paint.setAntiAlias(true);
		paintBorder = new Paint();
		paintBorder.setAntiAlias(true);
	}

	public CheckImageView(Context context, AttributeSet attrs) {
		this(context, attrs, R.attr.customImageViewStyle);
	}

	public CheckImageView(Context context) {
		this(context, null);
	}

	@Override
	protected void onDraw(Canvas canvas) {

		// super.onDraw(canvas);
		// int w = getWidth();
		// int h = getHeight();
		Bitmap image = drawableToBitmap(getDrawable());
		int iconW = mNormal.getIntrinsicWidth();
		int iconH = mNormal.getIntrinsicHeight();
		int width = canvas.getWidth() - getPaddingLeft()-getPaddingRight();
		int height = canvas.getHeight() - getPaddingTop() - getPaddingBottom();
		Bitmap reSizeImage = reSizeImage(image, width, height);
		canvas.drawBitmap(createRoundImage(reSizeImage, width, height),
				getPaddingLeft(), getPaddingTop(), null);
		canvas.save();
		mCheck.setBounds(0, 0,  iconW, iconH);
		mNormal.setBounds(0, 0, iconW, iconH);
		canvas.translate((int) (width - iconW/2), (int) (height - iconH));
		if (isCheck) {
			mCheck.draw(canvas);
		} else {
			mNormal.draw(canvas);
		}
		canvas.restore();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int width = MeasureSpec.getSize(widthMeasureSpec);
		int height = MeasureSpec.getSize(heightMeasureSpec);
		setMeasuredDimension(width, height);
	}

	/**
	 * drawable转bitmap
	 * 
	 * @param drawable
	 * @return
	 */
	private Bitmap drawableToBitmap(Drawable drawable) {
		if (drawable == null) {
			if (mSrcBitmap != null) {
				return mSrcBitmap;
			} else {
				return null;
			}
		} else if (drawable instanceof BitmapDrawable) {
			return ((BitmapDrawable) drawable).getBitmap();
		}
		Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
				drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
		drawable.draw(canvas);
		return bitmap;
	}

	/**
	 * 重设Bitmap的宽高
	 * 
	 * @param bitmap
	 * @param newWidth
	 * @param newHeight
	 * @return
	 */
	private Bitmap reSizeImage(Bitmap bitmap, int newWidth, int newHeight) {
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		// 计算出缩放比
		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;
		// 矩阵缩放bitmap
		Matrix matrix = new Matrix();

		matrix.postScale(scaleWidth, scaleHeight);
		return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
	}

	/**
	 * 画圆角
	 * 
	 * @param source
	 * @param width
	 * @param height
	 * @return
	 */
	private Bitmap createRoundImage(Bitmap source, int width, int height) {
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		Bitmap target = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		Canvas canvas = new Canvas(target);
		RectF rect = new RectF(0, 0, width, height);
		canvas.drawRoundRect(rect, mRadius, mRadius, paint);
		// 核心代码取两个图片的交集部分
		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
		canvas.drawBitmap(source, 0, 0, paint);
		return target;
	}
}
