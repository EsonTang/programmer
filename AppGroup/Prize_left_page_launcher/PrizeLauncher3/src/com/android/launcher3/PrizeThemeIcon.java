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
import android.widget.TextView;

import com.android.launcher3.bean.Theme;

public class PrizeThemeIcon extends TextView implements Iicon<Theme> {

	public PrizeThemeIcon(Context context, AttributeSet attrs,
			int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		// TODO Auto-generated constructor stub
	}

	public PrizeThemeIcon(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		// TODO Auto-generated constructor stub
	}

	public PrizeThemeIcon(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public PrizeThemeIcon(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void applyIconInfo(Theme info) {
		setText(info.themeName);
		if(info.id.equals("more")) {
			Drawable d = getContext().getDrawable(R.drawable.more);
			reSizeSetCompoundDrawble(d);
		}
		if(info.iconPreviewPath!=null)
		this.loadImage(info.iconPreviewPath);

	}

	@Override
	protected void onDraw(Canvas c) {
		super.onDraw(c);

		Drawable unreadBgNinePatchDrawable = (Drawable) getContext()
				.getDrawable(R.drawable.in_use);
		int w = unreadBgNinePatchDrawable.getIntrinsicWidth() / 4;
		int h = unreadBgNinePatchDrawable.getIntrinsicHeight() / 4;
		DrawEditIcons.drawSelect(c, this, w, h);
	}

	private Bitmap coverToBitmap(String iconPreviewPath) {
		FileInputStream is = null;
		Bitmap bitmap1 = null;
		try {
			is = new FileInputStream(new File(iconPreviewPath));

			bitmap1 = BitmapFactory.decodeStream(is);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			try {
				if (null != is)
					is.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return bitmap1;
	}

	public void loadImage(String path) {

		Bitmap b = coverToBitmap(path);
		Drawable d = ImageUtils.bitmapToDrawable(b);

		if (d != null) {
			reSizeSetCompoundDrawble(d);
		}
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
				reSizeSetCompoundDrawble(result);
			}
		}

	}
	
	public void reSizeSetCompoundDrawble(Drawable result) {

		float p = (float)result.getIntrinsicHeight()/result.getIntrinsicWidth();
		int iconszie = (int) (40*Launcher.scale);
		result.setBounds(0, 0, iconszie, (int)(iconszie*p));
//		setCompoundDrawablesWithIntrinsicBounds(null, result, null, null);
        setCompoundDrawables(null, result, null, null);
	}

}
