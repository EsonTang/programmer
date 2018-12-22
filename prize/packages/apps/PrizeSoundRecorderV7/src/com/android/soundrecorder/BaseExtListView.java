package com.android.soundrecorder;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.ListView;

public class BaseExtListView extends ListView {  

	private Context mContext;
	private BaseExtAdapter adapter;
	private float lastY = -1;
	private float moveY;
	/**Current sliding position before moving */
	private int tmpScrollY = 0;
	private int currentItemHeight;

	private boolean isMoving = false;

	private Handler handler = new Handler(){
		public void handleMessage(Message msg) {
			adapter.notifyDataSetChanged();
		};
	};

	public BaseExtListView(Context context, AttributeSet attrs) {  
		super(context, attrs);  
		mContext = context;  
	}  

	public BaseExtListView(Context context, AttributeSet attrs, int defStyle) {  
		super(context, attrs, defStyle);  
		mContext = context;  
	}  

	public BaseExtListView(Context context) {  
		super(context);  
		mContext = context;  
	}  

} 

