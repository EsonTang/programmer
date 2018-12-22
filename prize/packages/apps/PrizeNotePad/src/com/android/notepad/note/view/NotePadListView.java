
/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：便器列表控件(ListView)
 *当前版本：V1.0
 *作	者：朱道鹏
 *完成日期：2015-04-28
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

package com.android.notepad.note.view;

import com.android.notepad.note.NotePadAdapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.ListView;
import android.view.View;

/**
 **
 * 类描述：便器列表控件(ListView)
 * @author 朱道鹏
 * @version V1.0
 */
public class NotePadListView extends ListView {  

	private Context mContext;
	private NotePadAdapter adapter;
	private float lastY;
	private float moveY;
	/**
	 * Item当前高度
	 */
	private int currentItemHeight;
	/**
	 * Item初始化高度(最小高度)
	 */
	public static int MIN_HEIGHT = 0;
	/**
	 * Item最大高度
	 */
	private static int MAX_HEIGHT = 0;

	/**
	 * 移动状态变量
	 */
	private boolean isMoving = false;
	/**
	 * 用于Item高度动态刷新
	 */
	private Handler handler = new Handler(){
		public void handleMessage(Message msg) {
			adapter.notifyDataSetChanged();
		};
	};

	public NotePadListView(Context context, AttributeSet attrs) {  
		super(context, attrs);  
		mContext = context;  
	}  

	public NotePadListView(Context context, AttributeSet attrs, int defStyle) {  
		super(context, attrs, defStyle);  
		mContext = context;  
	}  

	public NotePadListView(Context context) {  
		super(context);  
		mContext = context;  
	}  	

	/**
	 * 方法描述：重写监听事件处理，实现移动时Item高度增大，Up时高度复原
	 * @param MotionEvent
	 * @return boolean
	 * @see NotePadListView#onTouchEvent
	 */
	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if(null == adapter){
			adapter = (NotePadAdapter)getAdapter();
		}
		if(adapter.getCount() == 0){
			return true;
		}
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			if(MIN_HEIGHT == 0){
				View listItem = adapter.getView(0, null, this); 
				listItem.measure(0, 0); 
				MIN_HEIGHT = listItem.getMeasuredHeight();
				MAX_HEIGHT = MIN_HEIGHT*3/2;
			}
			lastY = ev.getY();
			Log.i("Down Y", ":"+lastY);
			break;
		case MotionEvent.ACTION_MOVE:
			isMoving  = true;
			moveY = ev.getY();
			int dY = (int)(moveY - lastY);
			int firstVisibilityItem = getFirstVisiblePosition();
			int endVisibilityItem = getLastVisiblePosition();
			
			int count = getCount();
			if(firstVisibilityItem == 0 && endVisibilityItem == count-1 && dY<0){
				break;
			}else if(firstVisibilityItem == 0 && endVisibilityItem != count-1 && dY<0){
				break;
			}else if(firstVisibilityItem != 0 && endVisibilityItem == count-1 && dY>0){
				break;
			}else if(firstVisibilityItem != 0 && endVisibilityItem == count-1 && dY<0){
				dY = -dY;
			}
			Log.i("Move Y", ":"+dY);
			currentItemHeight = adapter.getCurrentItemHeight();
			int moveHeight = dY/20;

			int nextHeight = 0;
			nextHeight = currentItemHeight + moveHeight;

			//如果当前可见Item最后一项为Adaper最后一项，则确定为ListView底部，若要拉伸效果，则需重设ListView高度	
			if(endVisibilityItem == count-1 && firstVisibilityItem !=0){
				if(nextHeight > MIN_HEIGHT && nextHeight <= MAX_HEIGHT){
					int totleHeight = nextHeight*count;
					ViewGroup.LayoutParams params = getLayoutParams();  
					params.height = totleHeight + (getDividerHeight() * (getCount() - 1));  
					setLayoutParams(params);
				}
			}

			if(nextHeight > MIN_HEIGHT && nextHeight <= MAX_HEIGHT){
				adapter.setItemHeight(nextHeight);
				adapter.notifyDataSetChanged();
			}

			lastY = ev.getY();
			break;
		case MotionEvent.ACTION_UP:
			isMoving = false;
			Thread thread = new Thread(new FallBackRunnable());
			try {
				thread.join();
				thread.start();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			break;
		}		
		return super.onTouchEvent(ev);
	} 

	/**
	 **
	 * 类描述：Item高度回落线程
	 * @author 朱道鹏
	 * @version V1.0
	 */
	private class FallBackRunnable implements Runnable{
		@Override
		public void run() {
			currentItemHeight = adapter.getCurrentItemHeight();
			if(currentItemHeight > MIN_HEIGHT){
				int moveHeight = currentItemHeight-MIN_HEIGHT;
				while(moveHeight!=0){
					if(currentItemHeight > MIN_HEIGHT){
						adapter.setItemHeight(currentItemHeight-1);
						handler.sendEmptyMessage(0);
					}
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					currentItemHeight = adapter.getCurrentItemHeight();
					moveHeight = moveHeight-1;
				}
			}
		}
	}

} 

