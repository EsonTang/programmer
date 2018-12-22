package com.android.camera.ui;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Base64;
import android.util.DisplayMetrics;
import com.android.camera.Log;
import android.view.Display;
import android.view.ViewDebug.ExportedProperty;
import android.widget.ImageView;

import com.android.camera.CameraActivity;
import com.android.camera.R;
import com.android.camera.Util;

public class PrizeRotateImageView extends RotateImageView  implements
	CameraActivity.OnOrientationListener{
	
	private final static String TAG = "PrizeRotateImageView";

	private Drawable mDrawable = null;
	
	private Paint mPaint;
	
	private Context mContext;
	
	public PrizeRotateImageView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		init(context);
		registerOrientationListener(context);
		/*prize-add-bugid:53690 set the current orientation of combinview -xiaoping-20180327-start*/
		setCurrentOrientation(context);
		/*prize-add-bugid:53690 set the current orientation of combinview -xiaoping-20180327-end*/
	}

	public PrizeRotateImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		init(context);
		registerOrientationListener(context);
		/*prize-add-bugid:53690 set the current orientation of combinview -xiaoping-20180327-start*/
		setCurrentOrientation(context);
		/*prize-add-bugid:53690 set the current orientation of combinview -xiaoping-20180327-end*/
	}

	private void init(Context context) {
		// TODO Auto-generated method stub
	    mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

	    Activity mActivity = (Activity)context;
	    
	    Display display = mActivity.getWindowManager().getDefaultDisplay();
	    DisplayMetrics displayMetrics = new DisplayMetrics();
	    display.getMetrics(displayMetrics);
	    
	    mPaint.setTextSize(12 * displayMetrics.density);  

	    mPaint.setStyle(Paint.Style.FILL); 
	}


	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		canvas.save();
		super.onDraw(canvas);
		canvas.restore();
        Drawable drawable = getDrawable();
        if (drawable == null) {
            Log.e(TAG, "drawable == null, return");
            return;
        }
        
        Rect bounds = drawable.getBounds();
        int w = bounds.right - bounds.left;
        int h = bounds.bottom - bounds.top;
        int left = getPaddingLeft();
        int top = getPaddingTop();
        int right = getPaddingRight();
        int bottom = getPaddingBottom();
        int width = getWidth() - left - right;
        int height = getHeight() - top - bottom;

        canvas.translate(left + width / 2, top + height / 2);
      
        canvas.rotate(-getCurrentDegree());
        Rect rect = new Rect();
        mPaint.getTextBounds((String) this.getContentDescription(), 0, ((String) this.getContentDescription()).length(), rect);
        canvas.translate(- rect.width()/2, rect.height()/2+h/2);
        if(this.getTag()!=null && ((boolean) this.getTag())){ 
        	 mPaint.setColor(getResources().getColor(R.color.main_color));
        	 mPaint.setAlpha(255);
        }else{
            if(this.isPressed()){
                mPaint.setColor(Color.WHITE);
                mPaint.setAlpha(76);
            }else{
                mPaint.setColor(Color.WHITE);
                mPaint.setAlpha(153);
            }
        }


        canvas.drawText((String) this.getContentDescription(), 0, rect.height(), mPaint);
//        canvas.restoreToCount(saveCount);
		
	}
	
	@Override
	@ExportedProperty(category = "accessibility")
	public CharSequence getContentDescription() {
		// TODO Auto-generated method stub
		return super.getContentDescription();
	}


	public synchronized Drawable byteToDrawable(String icon) {    
        byte[] img=Base64.decode(icon.getBytes(), Base64.DEFAULT);  
        Bitmap bitmap;    
        if (img != null) {
            bitmap = BitmapFactory.decodeByteArray(img,0, img.length);    
            @SuppressWarnings("deprecation")  
            Drawable drawable = new BitmapDrawable(bitmap);    
                
            return drawable;    
        }    
        return null;    
    
    }  
	
	private void registerOrientationListener(Context context) {
        if (context instanceof CameraActivity) {
            CameraActivity camera = (CameraActivity) context;
            camera.addOnOrientationListener(this);
        }
    }
	private int mOrientation;
	@Override
    public void onOrientationChanged(int orientation) {
        if (mOrientation != orientation) {
            mOrientation = orientation;
            Util.setOrientation(this, mOrientation, true);
        }
    }
	
	@Override
	public void setContentDescription(CharSequence contentDescription) {
		// TODO Auto-generated method stub
//		if(contentDescription!=null){
//			mDrawable = byteToDrawable(contentDescription.toString());
//		}
		super.setContentDescription(contentDescription);
	}
	
	/*prize-add-bugid:53690 set the current orientation of combinview -xiaoping-20180327-start*/
	private void setCurrentOrientation(Context context) {
		int currentOriention = 0;
		if (context instanceof CameraActivity) {
			currentOriention = ((CameraActivity)context).getOrientationCompensation();
		}
		Log.i(TAG, "currentOriention: "+currentOriention);
		Util.setOrientation(this, currentOriention, true);
		mOrientation = currentOriention;
	}
	/*prize-add-bugid:53690 get the default orientation of combinview -xiaoping-20180327-end*/

}
