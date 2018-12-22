package com.android.gallery3d.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import com.android.gallery3d.R;

public class PrizeActionModeLayout extends LinearLayout{

	private Context mContext;
	
	private View view;
	
	public PrizeActionModeLayout(Context context) {
		super(context);
		this.mContext =  context;
		initView();
		// TODO Auto-generated constructor stub
	}
	
	public PrizeActionModeLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		this.mContext =  context;
		initView();
	}
	
	public PrizeActionModeLayout(Context context, AttributeSet attrs,
			int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		// TODO Auto-generated constructor stub
		this.mContext =  context;
		initView();
	}
	
	public PrizeActionModeLayout(Context context, AttributeSet attrs,
			int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		// TODO Auto-generated constructor stub
		this.mContext =  context;
		initView();
	}
	
	private void initView() {
		// TODO Auto-generated method stub
		view = LayoutInflater.from(mContext).inflate(R.layout.prize_action_mode, null);
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		WindowManager wm = (WindowManager) mContext
                .getSystemService(Context.WINDOW_SERVICE);

    	int width1 = wm.getDefaultDisplay().getWidth();
    	int height1 = wm.getDefaultDisplay().getHeight();
    	super.onMeasure(width1, heightMeasureSpec);
//    	view = LayoutInflater.from(mContext).inflate(R.layout.prize_action_mode,null);
//    	PrizeActionModeLayout.this.addView(view, new LayoutParams(width1, LayoutParams.MATCH_PARENT));
		
	}
	
	public View getCustomView(){
		if(view!=null){
			return view;
		}
		return null;
	}

}
