package com.android.prizefloatwindow.view;

import com.android.prizefloatwindow.R;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
public class CircleMenuLayout  extends ViewGroup
{
	private int mRadius;
	private static final float RADIO_DEFAULT_CHILD_DIMENSION = 3/10f;//1 / 4f;
	private float RADIO_DEFAULT_CENTERITEM_DIMENSION = 1 / 6f; 
	private double mStartAngle = -90;    
	private String[] mItemTexts;
	private Drawable[] mItemImgs; 
	private int mMenuItemCount; 

	private int mMenuItemLayoutId = R.layout.circle_menu_item;

	public CircleMenuLayout(Context context, AttributeSet attrs){
		super(context, attrs);
		
		setPadding(0, 0, 0, 0);  
	}
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
		int resWidth = 0;
		int resHeight = 0;
		int width = MeasureSpec.getSize(widthMeasureSpec);
		int widthMode = MeasureSpec.getMode(widthMeasureSpec);

		int height = MeasureSpec.getSize(heightMeasureSpec);
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		if (widthMode != MeasureSpec.EXACTLY|| heightMode != MeasureSpec.EXACTLY){
			resWidth = getSuggestedMinimumWidth();
			resWidth = resWidth == 0 ? getDefaultWidth() : resWidth;
			resHeight = getSuggestedMinimumHeight();
			resHeight = resHeight == 0 ? getDefaultWidth() : resHeight;
		} else{
			resWidth = resHeight = Math.min(width, height);
		}

		setMeasuredDimension(resWidth, resHeight);

		mRadius = Math.max(getMeasuredWidth(), getMeasuredHeight());

		final int count = getChildCount();
		int childSize = (int) (mRadius * RADIO_DEFAULT_CHILD_DIMENSION);
		int childMode = MeasureSpec.EXACTLY;

		for (int i = 0; i < count; i++){
			final View child = getChildAt(i);

			if (child.getVisibility() == GONE){
				continue;
			}

			int makeMeasureSpec = -1;

			if (child.getId() == R.id.id_circle_menu_item_center){
				makeMeasureSpec = MeasureSpec.makeMeasureSpec(
						(int) (mRadius * RADIO_DEFAULT_CENTERITEM_DIMENSION),childMode);
			} else{
				makeMeasureSpec = MeasureSpec.makeMeasureSpec(childSize,childMode);
			}
			child.measure(makeMeasureSpec, makeMeasureSpec);
		}

	}
	public interface OnMenuItemClickListener{ 
		void itemClick(View view, int pos);
		void itemCenterClick(View view);
	}

	private OnMenuItemClickListener mOnMenuItemClickListener;

	public void setOnMenuItemClickListener(OnMenuItemClickListener mOnMenuItemClickListener){
		this.mOnMenuItemClickListener = mOnMenuItemClickListener;
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b){
		int layoutRadius = mRadius;
		final int childCount = getChildCount();
		int left, top;
		int cWidth = (int) (layoutRadius * RADIO_DEFAULT_CHILD_DIMENSION);
		Log.d("snail_", "----------layoutRadius-----=="+layoutRadius);
		float angleDelay = 45;//180 / (getChildCount() - 1); 
		for (int i = 0; i < childCount; i++){
			final View child = getChildAt(i);

			if (child.getId() == R.id.id_circle_menu_item_center)
				continue;

			if (child.getVisibility() == GONE) 
			{
				continue;
			}

			mStartAngle %= 180;   
			
			float tmp = layoutRadius / 2f - cWidth / 2; 
			left = layoutRadius      
					/ 2
					+ (int) Math.round(tmp 
							* Math.cos(Math.toRadians(mStartAngle)) - 1 / 2f
							* cWidth);
			top = layoutRadius 
					/ 2
					+ (int) Math.round(tmp
							* Math.sin(Math.toRadians(mStartAngle)) - 1 / 2f
							* cWidth);
			//Log.d("snail_", "----------tmp=="+tmp+" left=="+left+"  top=="+top+"  mStartAngle=="+mStartAngle+"  cWidth=="+cWidth);
			child.layout(left, top, left + cWidth, top + cWidth);
			mStartAngle += angleDelay;
		}
		mStartAngle = -90;   
		View cView = findViewById(R.id.id_circle_menu_item_center);
		if (cView != null){
			cView.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v)
				{

					if (mOnMenuItemClickListener != null)
					{
						mOnMenuItemClickListener.itemCenterClick(v);
					}
				} 
			}); 
			int cl = layoutRadius / 2 - cView.getMeasuredWidth() / 2;
			int cr = cl + cView.getMeasuredWidth();  
			//Log.d("snail_onLayout", "---cl==="+cl+"  cr=="+cr);
			cView.layout(cl, cl-20, cr, cr-20);
		}

	}
	
	public void updateMenuItemIconsAndTexts(Drawable[] resIds, String[] texts) {
		mItemImgs = resIds;
		mItemTexts = texts;
		for(int i=1;i< getChildCount();i++){
			View view = getChildAt(i); 
			if (view.getId() == R.id.id_circle_menu_item_center) 
				return;
			TextView textView = (TextView)view.findViewById(R.id.id_circle_menu_item_text);
			textView.setText(mItemTexts[i-1]);
			ImageView imv = (ImageView) view.findViewById(R.id.id_circle_menu_item_image);
			imv.setImageDrawable(mItemImgs[i-1]);
		}
	}
	
	/**
	 * set  menu   text and img
	 * 
	 * @param resIds
	 */
	public void setMenuItemIconsAndTexts(Drawable[] resIds, String[] texts){
		mItemImgs = resIds;
		mItemTexts = texts;

		if (resIds == null && texts == null){
			throw new IllegalArgumentException("null");
		}

		mMenuItemCount = resIds == null ? texts.length : resIds.length;

		if (resIds != null && texts != null){
			mMenuItemCount = Math.min(resIds.length, texts.length);
		}
		addMenuItems();
	}

	public void setMenuItemLayoutId(int mMenuItemLayoutId){
		this.mMenuItemLayoutId = mMenuItemLayoutId;
	}

	private void addMenuItems(){
		LayoutInflater mInflater = LayoutInflater.from(getContext());
		for (int i = 0; i < mMenuItemCount; i++){
			final int j = i;
			View view = mInflater.inflate(mMenuItemLayoutId, this, false);
			ImageView iv = (ImageView) view.findViewById(R.id.id_circle_menu_item_image);
			TextView tv = (TextView) view.findViewById(R.id.id_circle_menu_item_text);
            
			if (iv != null){
				iv.setVisibility(View.VISIBLE);
				iv.setImageDrawable(mItemImgs[i]);
				iv.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View v){

						if (mOnMenuItemClickListener != null){
							mOnMenuItemClickListener.itemClick(v, j);
						}
					}
				});
			}
			if (tv != null){
				tv.setVisibility(View.VISIBLE);
				tv.setText(mItemTexts[i]);
			}

			addView(view);
		}
	}

	/**
	 * get default layout size
	 * 
	 * @return
	 */
	private int getDefaultWidth()
	{
		WindowManager wm = (WindowManager) getContext().getSystemService(
				Context.WINDOW_SERVICE);
		DisplayMetrics outMetrics = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(outMetrics);
		return Math.min(outMetrics.widthPixels, outMetrics.heightPixels);
	}
}
