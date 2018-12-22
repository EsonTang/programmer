package com.android.launcher3;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.lqsoft.LqServiceUpdater.LqService;
import com.lqsoft.lqtheme.LqShredPreferences;
import com.nostra13.universalimageloader.core.imageaware.ViewAware;

public class TextViewWrapAware extends ViewAware {
	Context mContext;
	IconCache mIconcache;

	public TextViewWrapAware(ImageView imageView, boolean checkActualViewSize) {
		super(imageView, checkActualViewSize);
		// TODO Auto-generated constructor stub
	}

	public TextViewWrapAware(BubbleTextView  view, Context c, IconCache i) {
		super(view);
		this.mContext = c;
		this.mIconcache = i;
	}
	
	public TextViewWrapAware(BubbleTextView imageView) {
		super(imageView);
	}
	
	

	@Override
	protected void setImageBitmapInto(Bitmap bitmap, View view) {

		if (view instanceof BubbleTextView) {
			BubbleTextView b = (BubbleTextView) view;
			if (LqShredPreferences.isLqtheme(b.getContext())) {
				if(bitmap.getConfig()==null) {
//					bitmap = ImageUtils.reCreateBitmap(bitmap);
					bitmap=ImageUtils.drawableToBitmap1(ImageUtils.bitmapToDrawable(bitmap));
					
				}
				bitmap = LqService.getInstance().getIcon(null, bitmap, false, "");
			}
			if(bitmap ==null) {
				Log.i("zhouerlong", "你好");
			}
			if(bitmap !=null) {
				if(Launcher.isSupportIconSize) {
				b.setCompoundDrawablesWithIntrinsicBounds(
						null,
						new FastBitmapDrawable(bitmap), null, null);
				}else {
					b.setCompoundDrawables(null,
							 Utilities.createIconDrawable(bitmap), null, null);
				}
			}
		}
		
	}

	@Override
	protected void setImageDrawableInto(Drawable drawable, View view) {

		if (view instanceof BubbleTextView) {

			Bitmap src = ImageUtils.drawableToBitmap1(drawable);
			BubbleTextView b = (BubbleTextView) view;
			if (LqShredPreferences.isLqtheme(b.getContext())) {
				src = LqService.getInstance().getIcon(null, src, true, "");
			}
			if(Launcher.isSupportIconSize) {
			b.setCompoundDrawablesWithIntrinsicBounds(
					null,
					new FastBitmapDrawable(src), null, null);
			}else {
				b.setCompoundDrawables(null,
						 Utilities.createIconDrawable(src), null, null);
				
			}
		}
		if (drawable instanceof AnimationDrawable) {
			((AnimationDrawable) drawable).start();
		}

	}

}
