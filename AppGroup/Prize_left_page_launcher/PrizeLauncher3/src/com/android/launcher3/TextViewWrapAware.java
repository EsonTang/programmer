package com.android.launcher3;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.lqsoft.lqtheme.LqThemeParser;
import com.nostra13.universalimageloader.core.imageaware.ViewAware;

public class TextViewWrapAware extends ViewAware {
	Context mContext;

	public TextViewWrapAware(ImageView imageView, boolean checkActualViewSize) {
		super(imageView, checkActualViewSize);
		// TODO Auto-generated constructor stub
	}

	public TextViewWrapAware(TextView  view, Context c, IconCache i) {
		super(view);
		this.mContext = c;
	}
	
	
	public TextViewWrapAware(TextView view) {
		super(view);
	}
	
	public Bitmap mask(Bitmap icon) {
		Bitmap mask = LqThemeParser.getMaskIcon(mContext);
		Bitmap reslut = ImageUtils.getMaskIcon(icon,
				UILimageUtil.getDefaultBitmap(mContext));
		if(mask ==null) {
			reslut =ImageUtils.getMaskIcon(icon,
					reslut);
		}
		if (reslut != null) {
			if (reslut.getHeight() != Utilities.sIconTextureHeight) {

				reslut = ImageUtils.resizeIcon(reslut,
						Utilities.sIconTextureHeight,
						Utilities.sIconTextureWidth);
			}

		}
		return reslut;
	}

	@Override
	protected void setImageBitmapInto(Bitmap icon, View view) {

		try {
			if (view instanceof TextView) {
				if(!LqThemeParser.isDefaultLoacalTHeme()) {
					Bitmap b = Bitmap.createBitmap(icon);
					icon = IconCache.getThemeIcon(null, icon, true, null, mContext);
					
					if(icon ==null) {
						icon =mask(b);
					}
				}else {
				icon = ImageUtils.getMaskIcon(icon, LqThemeParser.getMaskIcon(mContext));
				if (icon != null) {

					if (icon.getHeight() != Utilities.sIconTextureHeight) {

						icon = ImageUtils
								.resizeIcon(
										icon,
										Utilities.sIconTextureHeight,
										Utilities.sIconTextureWidth);
					}

				}}
				
				
				((TextView) view).setCompoundDrawablesWithIntrinsicBounds(null, new FastBitmapDrawable(icon), null, null);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	
	}

	@Override
	protected void setImageDrawableInto(Drawable drawable, View view) {
		try {
			if (view instanceof TextView) {
				Bitmap icon =  ImageUtils.getMaskIcon(ImageUtils.drawableToBitmap1(drawable), LqThemeParser.getMaskIcon(mContext));
				if (icon != null) {

					if (icon.getHeight() != Utilities.sIconTextureHeight) {

						icon = ImageUtils
								.resizeIcon(
										icon,
										Utilities.sIconTextureHeight,
										Utilities.sIconTextureWidth);
					}

					((TextView) view).setCompoundDrawables(
							null,
							Utilities
									.createIconDrawable(icon),
							null, null);
				}}
		} catch (Exception e) {
			// TODO: handle exception
		}

	}

}
