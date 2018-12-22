package com.android.launcher3;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.widget.Checkable;
import android.widget.ImageView;

public class AsyncImageView extends ImageView implements Checkable {
	private static final int[] CHECKED_STATE_SET = { android.R.attr.state_checked };

	public AsyncImageView(Context context, AttributeSet attrs,
			int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		// TODO Auto-generated constructor stub
	}

	public AsyncImageView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		// TODO Auto-generated constructor stub
	}

	public AsyncImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	class AsyncLoad extends AsyncTask<String, Void, Drawable> {

		@Override
		protected Drawable doInBackground(String... path) {
			String resulut = path[0];
			Bitmap b = coverToBitmap(resulut);
			Drawable d = ImageUtils.bitmapToDrawable(b);
			if (b != null && !b.isRecycled()) {
				// b.recycle();
			}
			return d;
		}

		@Override
		protected void onPostExecute(Drawable result) {
			super.onPostExecute(result);
			if (result != null) {
				setImageDrawable(result);
			}
		}

	}

	public AsyncImageView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	private Bitmap coverToBitmap(String iconPreviewPath) {
		FileInputStream is = null;
		Bitmap bitmap1 = null;
		try {
			is = new FileInputStream(new File(iconPreviewPath));

			bitmap1 = BitmapFactory.decodeStream(is);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}finally {
			try {
				if(null != is) is.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return bitmap1;
	}

	public void loadImage(String path) {
		new AsyncLoad().execute(path);
	}

	// M by zel
	@Override
	protected void onDraw(Canvas canvas) {
       super.onDraw(canvas);
		// 画边框 暂时去除小边框
		/*Drawable unreadBgNinePatchDrawable = (Drawable) 
				getContext().getDrawable(R.drawable.in_use);
		int w = unreadBgNinePatchDrawable.getIntrinsicWidth()/4;
		int h = unreadBgNinePatchDrawable.getIntrinsicHeight()/4;
		DrawEditIcons.drawSelect(canvas, this,w,h);*/
	}

	public boolean isChecked() {
		return mChecked;
	}

	boolean mChecked;

	public void toggle() {
		setChecked(!mChecked);
	}

	public void setChecked(boolean checked) {
		if (checked != mChecked) {
			mChecked = checked;
			refreshDrawableState();
		}
	}

}