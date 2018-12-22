/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：防误触-自定义view接口-实现
 *当前版本：
 *作	者：钟卫林
 *完成日期：2015-04-21
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
...
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
 *********************************************/
package com.prize.smart;

import java.util.Calendar;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.os.Handler;
import android.os.Message;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.os.SystemProperties;

public class CoverView01 extends CoverViewBase {
	private static final String TAG = "prize";

	private static final int __FONT_SIZE_MODE = 25;
	private static final int __FONT_SIZE_TIME = 60;
	private static final int __FONT_SIZE_VALUE = 40;
	private static final int __FONT_SIZE_HINT = 20;
	private static final int __FONT_SIZE_BASE_WIDTH = 480; // font size if test
															// for this screen
															// width!
	// so , other resolution is based on this!

	Handler mHandler1 = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0: {
				invalidate();
				mHandler1.sendEmptyMessageDelayed(0, 200);
				break;
			}
			default:
				super.handleMessage(msg);
			}
		}
	};

	int screen_pix_width = 0;
	int screen_pix_height = 0;
	long __tick = 0;
	Context mContext;
	Paint mPaint1 = new Paint();
	Bitmap mBitmap1 = null;

	public CoverView01(Context context) {
		super(context);
		mContext = context;

		mPaint1.setStrokeWidth(3);
		mPaint1.setColor(0xcc000000);
		mPaint1.setAntiAlias(true);
		mPaint1.setTextSize(100);
		//mPaint1.setTextAlign(Paint.Align.CENTER);
		mBitmap1 = BitmapFactory.decodeResource(getResources(),
				R.drawable.mistaken_touch_new);

		DisplayMetrics metric = new DisplayMetrics();
		WindowManager wm = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		wm.getDefaultDisplay().getMetrics(metric);

		screen_pix_width = metric.widthPixels;
		screen_pix_height = metric.heightPixels;
		
		Log.v(TAG, "CoverView01: width=" + screen_pix_width + ", height=" + screen_pix_height+",bmp.wight = " +mBitmap1.getWidth()+", bmp.height = "+mBitmap1.getHeight());
		mBitmap1 = Bitmap.createScaledBitmap(mBitmap1, mBitmap1.getWidth(),
				mBitmap1.getHeight(), false);
	}

	public CoverView01(Context context, AttributeSet attr) { // be called!
		super(context, attr);
	}

	public CoverView01(Context context, AttributeSet attr, int defStyle) {
		super(context, attr, defStyle);
	}

	@Override
	protected void onFinishInflate() {
		Log.v(TAG, "onFinishInflate(");
	}
	
	/** 
     * from dp to px 
     */  
    public int dip2px(float dpValue) {  
        float scale = getResources().getDisplayMetrics().density;  
        return (int) (dpValue * scale + 0.5f);  
    }  

	// /////////////////////////////////////
	private String _get_cur_hm() {
		Calendar c = Calendar.getInstance();
		int hour = c.get(Calendar.HOUR_OF_DAY);
		int minute = c.get(Calendar.MINUTE);

		__time_is24 = DateFormat.is24HourFormat(mContext);
		if (__time_is24 == false) {
			if (hour > 12) {
				hour -= 12;
			}
		}

		String r = String.format("%1$02d:%2$02d", hour, minute);
		Log.v(TAG, "r=" + r);
		return r;
	}

	int __last_key = 0;
	boolean __time_is24 = false; // false-12, true-24

	public void onShow() {
		__tick = 0;
		__last_key = 0;
	}

	private int font_height = 0;
	private int font_width = 0;

	void __get_font_height_width_from_paint1(String mStr) {
		int w = 0;
		int mFontHeight = 0;
		String mStrText = mStr;
		char ch;
		FontMetrics fm = mPaint1.getFontMetrics();
		mFontHeight = (int) (Math.ceil(fm.descent - fm.top) + 2);
		int count = mStrText.length();
		for (int i = 0; i < count; i++) {
			ch = mStrText.charAt(i);
			float[] widths = new float[1];
			String str = String.valueOf(ch);
			mPaint1.getTextWidths(str, widths);
			w += (int) Math.ceil(widths[0]);
		}
		font_width = w;
		font_height = mFontHeight;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		/**draw antifaketouch_Bitmap */
		canvas.drawColor(0xcc000000);
		if (mBitmap1 != null) {
			float left = (screen_pix_width-mBitmap1.getWidth())/2;
			float top = getResources().getDimension(R.dimen.antifaketouch_bmp_top);
			canvas.drawBitmap(mBitmap1, left, top, null);
		}

		float drawx = 0;
		float drawy = 0;
		/**draw antifaketouch_mode text*/
		String mode = mContext.getString(R.string.antifaketouch_mode1);
		float textsize = getResources().getDimension(R.dimen.antifaketouch_mode1_textsize);
		//Log.v(TAG, "coverview:onDraw() mode1 textsize = " + textsize);
		mPaint1.setTextSize(textsize);
		mPaint1.setColor(0xffffffff);
		float modetop = getResources().getDimension(R.dimen.antifaketouch_mode_top);
		drawy = modetop;
		__get_font_height_width_from_paint1(mode);
		//Log.v(TAG, "coverview:onDraw() mode1 font_width = " + font_width + ", screen_pix_width = " + screen_pix_width);
		drawx = (screen_pix_width - font_width) / 2;
		canvas.drawText(mode, drawx, modetop, mPaint1);
		
		/**draw antifaketouch_mode_value text*/
		String value = mContext.getString(R.string.antifaketouch_value);
		textsize = getResources().getDimension(R.dimen.antifaketouch_value_textsize);
		//Log.v(TAG, "coverview:onDraw() value textsizesp = " + textsize+", textsizesppx = " +dip2px(textsize));;
		mPaint1.setTextSize(textsize);
		mPaint1.setColor(0xffffffff); // black
		//mPaint1.setColor(0xffa00000); // red
		float valuebelove = getResources().getDimension(R.dimen.antifaketouch_value_belove);
		drawy = drawy + valuebelove + font_height;
		__get_font_height_width_from_paint1(value);
		//Log.v(TAG, "coverview:onDraw() value font_width = " + font_width+ ", screen_pix_width = " + screen_pix_width);
		drawx = (screen_pix_width - font_width) / 2;
		canvas.drawText(value, drawx, drawy, mPaint1);
		//drawText(canvas,value,dip2px(textsize),0xffffffff,15,drawy);
		
		/**draw antifaketouch_hint text*/
		String hint;
		if(isHwMainKeys()){
			hint = mContext.getString(R.string.antifaketouch_hint1);
		}else{
			hint = mContext.getString(R.string.antifaketouch_hint2);
		}
		textsize = getResources().getDimension(R.dimen.antifaketouch_hint_textsize);
		//Log.v(TAG, "coverview:onDraw() hint textsize = " + textsize);
		mPaint1.setTextSize(textsize);
		mPaint1.setColor(0xffffffff); // black
		//mPaint1.setColor(0xffa00000); // red
		__get_font_height_width_from_paint1(hint);
		//Log.v(TAG, "coverview:onDraw() hint font_width = " + font_width+ ", screen_pix_width = " + screen_pix_width);
		drawx = (screen_pix_width - font_width) / 2;
		float hintbottom = getResources().getDimension(R.dimen.antifaketouch_hint_bottom);
		drawy = screen_pix_height - hintbottom - font_height;
		canvas.drawText(hint, drawx, drawy, mPaint1);
		
	}
	
	public void drawText(Canvas canvas, String textValue, float textSize,int textColor,int drawX, int drawY){
	    TextPaint textPaint = new TextPaint();  
	    textPaint.setColor(textColor);  
	    textPaint.setTextSize(textSize);  
	    textPaint.setAntiAlias(true);  
	    StaticLayout layout = new StaticLayout(textValue, textPaint, screen_pix_width-30,  
	            Alignment.ALIGN_CENTER, 1.0F, 0.0F, true);  
	    canvas.save();  
	    canvas.translate(drawX, drawY);//从20，20开始画  
	    layout.draw(canvas);  
	    canvas.restore();//别忘了restore  
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// Log.v(TAG, "event.getAction()=" + event.getAction());
		if (event.getAction() == MotionEvent.ACTION_DOWN && !isFocused()) {
			requestFocus();
		}
		return true;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		Log.v(TAG, "keyCode=" + keyCode);

		if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
			// Log.v(TAG, "KEYCODE_VOLUME_UP");
			if (__last_key == 0) {
				__last_key = 1;
			} else if (__last_key == 2) { // last is 2
				// mHandler.sendEmptyMessage(_MSG_DOUBLE_KEY);
				if (mCallback != null)
					mCallback.onDoubleClick();
			}
		} else if (keyCode == KeyEvent.KEYCODE_BACK && isHwMainKeys()) {
			Log.v(TAG, "KEYCODE_BACK");
			if (__last_key == 0) {
				__last_key = 2;
			} else if (__last_key == 1) { // last is 1
				// mHandler.sendEmptyMessage(_MSG_DOUBLE_KEY);
				if (mCallback != null)
					mCallback.onDoubleClick();
			}
		} else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN && !isHwMainKeys()) {
			Log.v(TAG, "KEYCODE_POWER");
			if (__last_key == 0) {
				__last_key = 3;
			} else if (__last_key == 1) { // last is 1
				// mHandler.sendEmptyMessage(_MSG_DOUBLE_KEY);
				if (mCallback != null)
					mCallback.onDoubleClick();
			}
		} else {
			__last_key = 0;
		}
		return true;
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		// Log.v(TAG, "keyCode=" + keyCode);
		__last_key = 0;
		return true;
	}
	
	/**
    * 方法描述：判断当前版本是否是物理按键版本
    * @param 
    * @return boolean 
    */
    private static boolean isHwMainKeys() {
        return SystemProperties.get("qemu.hw.mainkeys").equals("1");
    }


	// ////////////////////////////////////////////
	protected void onAttachedToWindow() {
		Log.v(TAG, "onAttachedToWindow( ");
		super.onAttachedToWindow();
		mHandler1.sendEmptyMessageDelayed(0, 200);
	}

	@Override
	protected void onDetachedFromWindow() {
		Log.v(TAG, "onDetachedFromWindow( ");
		super.onDetachedFromWindow();
		mHandler1.removeMessages(0);
	}

}
