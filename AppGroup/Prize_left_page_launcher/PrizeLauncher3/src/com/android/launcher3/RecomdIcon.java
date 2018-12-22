package com.android.launcher3;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.prize.app.net.datasource.base.AppsItemBean;

public class RecomdIcon extends TextView implements Iicon<AppsItemBean> {

	public RecomdIcon(Context context, AttributeSet attrs, int defStyleAttr,
			int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		// TODO Auto-generated constructor stub
	}

	public RecomdIcon(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		// TODO Auto-generated constructor stub
	}

	public RecomdIcon(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public RecomdIcon(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	public void loadImage(AppsItemBean info) {
		
		
		try {
			this.setCompoundDrawablesWithIntrinsicBounds(
					null,
					new FastBitmapDrawable(UILimageUtil
							.getDefaultBitmap(mContext)), null, null);

		} catch (Exception e) {
			// TODO: handle exception
		}
		ImageLoader.getInstance().displayImage(
				info.iconUrl,
				new TextViewWrapAware(this,
						mContext, null),UILimageUtil.getUILoptions(mContext));
    }
	@Override
	public void applyIconInfo(AppsItemBean info) {
		setText(info.name);
		loadImage(info);
		setTag(info);

	}
	
	@Override
    protected void drawableStateChanged() {
		setFilter();

        super.drawableStateChanged();
    }
	
	
	/**  
     *   设置滤镜
     */
    private void setFilter() {
    		
        //先获取设置的src图片
        Drawable drawable=this.getCompoundDrawables()[1];
            if(drawable!=null){
                //设置滤镜
            	if(isPressed()) {
                    drawable.setColorFilter(Color.GRAY,PorterDuff.Mode.MULTIPLY); 
            	} else {
                    drawable.setColorFilter(null); 
            	}
            }
        this.invalidate();
    }

}
