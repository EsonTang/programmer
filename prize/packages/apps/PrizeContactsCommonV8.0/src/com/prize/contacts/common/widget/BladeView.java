/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.prize.contacts.common.widget;

import java.util.Locale;

import com.prize.contacts.common.util.UniverseUtils;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.widget.TextView;
import android.view.Gravity;
import android.widget.PopupWindow;
import android.view.MotionEvent;
import android.graphics.Paint;
import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint.Align;
import android.graphics.Paint.FontMetrics;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.Layout;
import android.text.Layout.Alignment;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewDebug;
import android.view.accessibility.AccessibilityEvent;
import android.widget.RemoteViews.RemoteView;
import com.android.contacts.common.R;

	/**PRIZE-add-match char-shiyicheng-20150909-start*/
import com.android.contacts.common.list.ContactEntryListFragment;
	/**PRIZE-add-match char-shiyicheng-20150909-end*/
/**PRIZE-add-match char-shiyicheng-20150915-start*/
import android.content.SharedPreferences;
/**PRIZE-add-match char-shiyicheng-20150915-end*/
import android.os.SystemProperties;	
public class BladeView extends View {
	private int mMinCharHeight = 8;
	private int mCharHeight = 5;
	private OnItemClickListener mOnItemClickListener;
	private PopupWindow mPopupWindow;
	private TextView mPopupText;
	private int mTextColor;
	private boolean mTouched = false;
	private boolean mIsTW = false;
	private int mCurItem = 0;
	private int mLastItem = 0;//prize-add-huangpengfei-2016-9-28
	/**PRIZE-add-match char-shiyicheng-20150906-start*/
	private static boolean mIsNeedToDraw = false;
	private static int[] mmTextColor = new int[/*27*/26];       //prize modify zhaojian 8.0 2017725
	/**PRIZE-add-match char-shiyicheng-20150906-end*/

	/**PRIZE-add-match char-shiyicheng-20150909-start*/
	private int mprize = 0;
	/**PRIZE-add-match char-shiyicheng-20150909-end*/

	private float mHeight = 0;
	private static int mBladeWidth = 26;
	private static int mBladeGap = 5;
	/**PRIZE-add-match char-shiyicheng-20150915-start*/
	private SharedPreferences sp;
	/**PRIZE-add-match char-shiyicheng-20150915-end*/
	private Drawable mPupupBgDrawable;//prize-add-hpf-2017-12-27

	private String[] mAlphabet = {/*"★",*/"A", "B", "C", "D", "E", "F", "G",
			"H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T",
			"U", "V", "W", "X", "Y", "Z", "#" };      //prize modify zhaojian 8.0 2017725

	private String[] mTraditional = { "1", "2", "3", "4", "5", "6", "7",
			"8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18",
			"19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29",
			"30", "31", "32", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J",
			"K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W",
			"X", "Y", "Z", "#" };

	private String[] mTraditionalFull = { "1劃", "2劃", "3劃", "4劃", "5劃",
			"6劃", "7劃", "8劃", "9劃", "10劃", "11劃", "12劃", "13劃", "14劃", "15劃",
			"16劃", "17劃", "18劃", "19劃", "20劃", "21劃", "22劃", "23劃", "24劃",
			"25劃", "26劃", "27劃", "28劃", "29劃", "30劃", "31劃", "32劃", "A", "B",
			"C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O",
			"P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "#" };
	private String TAG = "BladeView";
	private Context mContext;

	public BladeView(Context context) {
		this(context, null);
	}

	public BladeView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public BladeView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		final Resources.Theme theme = getContext().getTheme();
		mBladeWidth = mContext.getResources().getDimensionPixelSize(
				R.dimen.contact_bladeview_width);
		mBladeGap = mContext.getResources().getDimensionPixelSize(
				R.dimen.contact_bladeview_gap);
		mMinCharHeight = mContext.getResources().getDimensionPixelSize(
				R.dimen.contact_bladeview_min_height);
		mIsTW = isTw(context);
		mPupupBgDrawable = (Drawable) getContext().getResources().getDrawable(R.drawable.prize_blade_popup_shape);//prize-add-hpf-2017-12-27
	}

/**PRIZE-add-match char-shiyicheng-20150906-start*/
	public void configCharacterColor(String sction){

    		for(int i=0; i<mAlphabet.length; i++){
    			if(sction.equalsIgnoreCase(mAlphabet[i])){
    				mIsNeedToDraw = true;
			    	mCurItem= i;
    			//	Log.d(TAG,"configCharacterColor sction="+sction+" display"+"  mCurItem="+i);
    				break;
    			}
    		}
    }

 	public void configCharacterColorToDefault(){

        for(int i=0; i<mmTextColor.length; i++){

                mmTextColor[i] = Color.TRANSPARENT;

        }
    }

public void reDraw(){
    //	Log.d(TAG,"reDraw start");
    	if(!mIsNeedToDraw) return;
    	invalidate();
    	mIsNeedToDraw = false;
    //	Log.d(TAG,"reDraw finish");
    }

/**PRIZE-add-match char-shiyicheng-20150906-end*/
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
/**PRIZE-add-shiyicheng-20150919-start*/
	sp = getContext().getSharedPreferences("prize_config", 0);
	boolean mDialtactsActivityEnabled = sp.getBoolean("mprize_boolean", false);
/**PRIZE-add-shiyicheng-20150919-end*/
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);
		mHeight = (float) heightSize;
		int charNumber = getAlphabet(mIsTW).length;
    /*prize-change-huangliemin-2016-7-28*/
    /*
		int charHeigtht = (heightSize - (charNumber + 1) * mBladeGap)
				/ charNumber;
    */
    int charHeigtht = mContext.getResources().getDimensionPixelSize(R.dimen.prize_bladeview_text_size);
    /*prize-change-huangliemin-2016-7-28*/
		if (charHeigtht > mBladeWidth) {
			charHeigtht = mContext.getResources().getDimensionPixelSize(R.dimen.prize_bladeview_text_size);/*mBladeWidth - 2;*///prize-change-huangliemin-2016-7-28
			mCharHeight = charHeigtht;
			mBladeGap = (heightSize - charHeigtht * charNumber)
					/ (charNumber + 1);
					
		/**PRIZE-add-shiyicheng-20150919-start*/
			if(mDialtactsActivityEnabled){
				
			sp = getContext().getSharedPreferences("prize_config", 0);
			SharedPreferences.Editor editor = sp.edit();
			editor.putInt("mBladeGap", mBladeGap);
			 editor.commit();
			}
			
		/**PRIZE-add-shiyicheng-20150919-end*/
		} else if (charHeigtht < mMinCharHeight && !mIsTW  
				&& !(mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)) {
			heightSize = (int) mHeight;
			charHeigtht = mMinCharHeight;
			mCharHeight = mMinCharHeight;
			mBladeGap = (heightSize - charHeigtht * charNumber)
					/ (charNumber + 1);
	
		} else {
			mCharHeight = charHeigtht;
		}
		int widthSize = mBladeWidth + getPaddingRight()
				+ getPaddingLeft() + 30; /**PRIZE-add-match char-shiyicheng-20150906-start*/
		setMeasuredDimension(widthSize, heightSize);

	}

	@Override
	protected void onDraw(Canvas canvas) {

	/**PRIZE-add-match char-shiyicheng-20150915-start*/
	sp = getContext().getSharedPreferences("prize_config", 0);
	boolean mDialtactsActivityEnabled = sp.getBoolean("mprize_boolean", false);
	/**PRIZE-add-match char-shiyicheng-20150915-end*/

/**PRIZE-add-shiyicheng-20150919-start*/
	if(mDialtactsActivityEnabled){

	int prize_bladegap = sp.getInt("mBladeGap", 0);
	
	mBladeGap = prize_bladegap;
	}
					
/**PRIZE-add-shiyicheng-20150919-end*/
	
		int textSize = mCharHeight;
		int charNumber = getAlphabet(mIsTW).length;
		super.onDraw(canvas);
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setTextAlign(Align.CENTER);
		for (int i = 0; i < charNumber; ++i) {
			String currentChar = getAlphabet(mIsTW)[i];
			paint.setColor(this.getResources().getColor(
						R.color.contact_bladeview_index_text_color));
			int y = i * mCharHeight + (i + 1) * mBladeGap;
			int x = (getWidth() + getPaddingLeft() - getPaddingRight()) / 2;
			if (mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
				if (mIsTW) {
					if (i % 8 != 0) {
						currentChar = " ";
					}
					if (i == 0) {
						currentChar = " ";
					}
					paint.setTextSize((mCharHeight) * 5);
					textSize = (mCharHeight) * 5;
				} else {
					if (i % 3 != 0) {
						currentChar = " ";
					}
					paint.setTextSize((mCharHeight) * 2);
					textSize = (mCharHeight) * 2;
				}
			} else if (mIsTW) {
				if (i == 0 || i % 3 != 0) {
					currentChar = " ";
				}
				paint.setTextSize((mCharHeight) * 2);
				textSize = (mCharHeight) * 2;
			} else {
		/**PRIZE-add-match char-shiyicheng-20150909-start*/
				if(mDialtactsActivityEnabled){
					
					mprize = (mCharHeight * 23)/28;
					
				}else{
					mprize = mCharHeight;
				
				}
		/**PRIZE-add-match char-shiyicheng-20150909-end*/
		/**PRIZE-add-match char-shiyicheng-20150909-start*/	
				textSize = mprize; //mCharHeight;
		/**PRIZE-add-match char-shiyicheng-20150909-end*/
				paint.setTextSize(textSize);
				FontMetricsInt fontMetrics = paint.getFontMetricsInt();
				Rect targetRect = new Rect(getPaddingLeft(), y, getWidth()
						- getPaddingRight(), y + mCharHeight);
				int baseline = targetRect.top+ (targetRect.bottom - targetRect.top
								- fontMetrics.bottom + fontMetrics.top) / 2- fontMetrics.top;
				if (mCurItem == i) {
					paint.setColor(this.getResources().getColor(
							R.color.contact_bladeview_index_pressed_text_color));
					
					/*prize-change-huangpengfei_2016-8-30-start*/
					int bgBound = getContext().getResources().getDimensionPixelOffset(R.dimen.prize_contacts_blade_view_bg_bound);
					Drawable drawable = (Drawable) getContext().getResources()
							.getDrawable(R.drawable.prize_blade_bg_shape);
					drawable.setBounds(targetRect.centerX() -bgBound/*- mprize / 2 - 12*/, 
									   targetRect.centerY() -bgBound/*- mprize / 2 - 12*/,
									   targetRect.centerX() +bgBound/*+ mprize / 2 + 12*/,
									   targetRect.centerY() +bgBound/*+ mprize / 2 + 12*/);
					/*prize-change-huangpengfei_2016-8-30-start*/
					
					drawable.draw(canvas);
				}
			}
			FontMetricsInt fontMetrics = paint.getFontMetricsInt();
			Rect targetRect = new Rect(getPaddingLeft(), y, getWidth()
					- getPaddingRight(), y + textSize);
			int baseline = targetRect.top+ (targetRect.bottom - targetRect.top
							- fontMetrics.bottom + fontMetrics.top) / 2- fontMetrics.top;
			
			Log.d(TAG,"[onDraw] currentChar="+currentChar+"   x="+x+"   baseline="+baseline+"   mprize="+mprize);
			canvas.drawText(currentChar, x, baseline, paint);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		final int action = event.getActionMasked();
		final int charNumber = getAlphabet(mIsTW).length;
		if (action == MotionEvent.ACTION_DOWN
				|| action == MotionEvent.ACTION_MOVE) {
			getParent().requestDisallowInterceptTouchEvent(true);
			int item = (int) (event.getY() / (mCharHeight + mBladeGap));
			if (item < 0 || item >= charNumber) {
				return true;
			}
			//showPopup(item);
			performItemClicked(this,item);
			mTouched = true;
			mCurItem = item;
			if (UniverseUtils.UNIVERSEUI_SUPPORT) {
				this.setBackgroundResource(R.drawable.lyt_call_bg_addnew);
			}
			
			/*prize-add-huangpengfei-2016-9-28*/
			if(mLastItem != item){
				invalidate();
				showPopup(item);
				Log.d(TAG,"[onTouchEvent]  showPopup()");
			}
			
			mLastItem = item;
			/*prize-add-huangpengfei-2016-9-28*/
			
		} else {
			mLastItem = 0;
			dismissPopup();
			if (UniverseUtils.UNIVERSEUI_SUPPORT) {
				this.setBackgroundColor(Color.TRANSPARENT);
			}
			mTouched = false;
		}
		return true;
	}

	private void showPopup(int item) {
		if (mPopupWindow == null) {
			mPopupWindow = new PopupWindow(this.getResources()
					.getDimensionPixelSize(
							R.dimen.contact_bladeview_popup_width), this
					.getResources().getDimensionPixelSize(
							R.dimen.contact_bladeview_popup_width));

			mPopupText = new TextView(getContext());
			
			/*prize-change fix bug[51843]-hpf-2018-03-5-start*/
			Resources res = this.getResources(); 
			Configuration config=new Configuration();  
			config.setToDefaults();  
			res.updateConfiguration(config,res.getDisplayMetrics() );
			boolean isPrizeFontSize = SystemProperties.getBoolean("persist.sys.prize.fontsize",false);
			if(isPrizeFontSize){
				mPopupText.setTextSize((float)((res.getDimension(
					R.dimen.contact_bladeview_popup_text_size))/1.7));
			}else{
				mPopupText.setTextSize(res.getDimension(
					R.dimen.contact_bladeview_popup_text_size));
			}
			/*prize-change fix bug[51843]-hpf-2018-03-5-end*/
			mPopupText
					.setBackgroundDrawable(mPupupBgDrawable);//prize-change-hpf-2017-12-27

			mPopupText.setTextColor(this.getResources().getColor(
					R.color.contact_bladeview_popup_text_color));

			mPopupText.setGravity(Gravity.CENTER_HORIZONTAL
					| Gravity.CENTER_VERTICAL);
			mPopupWindow.setContentView(mPopupText);
		}

		String text = getAlphabet(mIsTW)[item];
		mPopupText.setText(text);
		if (mPopupWindow.isShowing()) {
			mPopupWindow.update();
		} else {
			mPopupWindow.showAtLocation(getRootView(),
					Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
		}
	}

	private void dismissPopup() {
		if (mPopupWindow != null) {
			mPopupWindow.dismiss();
		}
	}

	public void setOnItemClickListener(OnItemClickListener listener) {
		mOnItemClickListener = listener;
	}

	private void performItemClicked(View view,int item) {
		if (mOnItemClickListener != null) {
			mOnItemClickListener.onItemClick(view,item);
		}
	}

	public interface OnItemClickListener {
		void onItemClick(View view,int item);
	}

	public boolean isTw(Context context) {
		String country = context.getResources().getConfiguration().locale
				.getCountry();
		if ("TW".equals(country)) {
		/**PRIZE-add-match char-switch-TW-launcher-shiyicheng-20150921-start*/
			return false;//true;
		/**PRIZE-add-match char-switch-TW-launcher-shiyicheng-20150921-end*/
		}
		return false;
	}

	public String[] getAlphabet(boolean isTW) {
		if (isTW) {
		/**PRIZE-add-match char-switch-TW-launcher-shiyicheng-20150921-start*/
			return mAlphabet;//mTraditional;
		/**PRIZE-add-match char-switch-TW-launcher-shiyicheng-20150921-end*/
		} else {
			return mAlphabet;
		}
	}

	public String[] getFullAlphabet(boolean isTW) {
		if (isTW) {
		/**PRIZE-add-match char-switch-TW-launcher-shiyicheng-20150921-start*/
			return mAlphabet;//mTraditionalFull;
		/**PRIZE-add-match char-switch-TW-launcher-shiyicheng-20150921-end*/
		} else {
			return mAlphabet;
		}
	}
}
