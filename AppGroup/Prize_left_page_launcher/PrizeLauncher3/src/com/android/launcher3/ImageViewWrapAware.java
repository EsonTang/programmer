package com.android.launcher3;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.lqsoft.LqServiceUpdater.LqService;
import com.lqsoft.lqtheme.LqShredPreferences;
import com.nostra13.universalimageloader.core.imageaware.ViewAware;

public class ImageViewWrapAware extends ViewAware {

	public ImageViewWrapAware(View view, boolean checkActualViewSize) {
		super(view, checkActualViewSize);
		// TODO Auto-generated constructor stub
	}

	public ImageViewWrapAware(View view) {
		super(view);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void setImageBitmapInto(Bitmap icon, View view) {

		try {
			if (view instanceof ImageView) {
				
				  icon = IconCache.getLqIcon( null, icon, true, null);
				 
				if (icon != null) {

					if (icon.getHeight() != Utilities.sIconTextureHeight) {

						icon = ImageUtils.resizeIcon(icon,
								Utilities.sIconTextureHeight,
								Utilities.sIconTextureWidth);
					}

					((ImageView) view).setImageDrawable(new FastBitmapDrawable(
							icon));
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
		}

	}

	@Override
	protected void setImageDrawableInto(Drawable drawable, View view) {
		try {
			if (view instanceof ImageView) {
				Bitmap icon = IconCache.getLqIcon(null,
						ImageUtils.drawableToBitmap1(drawable), true, null);
				if (icon != null) {

					if (icon.getHeight() != Utilities.sIconTextureHeight) {

						icon = ImageUtils.resizeIcon(icon,
								Utilities.sIconTextureHeight,
								Utilities.sIconTextureWidth);
					}
					((ImageView) view).setImageDrawable(Utilities
							.createIconDrawable(icon));

				}
			}
		} catch (Exception e) {
			// TODO: handle exception
		}

	}

}
