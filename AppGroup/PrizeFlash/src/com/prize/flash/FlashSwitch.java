
 /*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：
 *当前版本：
 *作	者：
 *完成日期：
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

package com.prize.flash;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Switch;
import android.view.View.OnTouchListener;

public class FlashSwitch extends RelativeLayout {
	private int bottomEdge = 0;                         //距离顶部的距离
	private int moveY = 0;                              //手指移动的Y坐标
	private int downY;  								//手指按下的Y坐标
	
	private String switchStatus = "on";                 //按钮的状态
	private RelativeLayout.LayoutParams layoutParams;   //滑块的布局参数
	private SwitchLister mSwitchLister;                 //按钮开关的监听事件
	private int mThumbGap;                              //滑块距离背景的上半部分间距
	private final static float THUMBGAPRATIO = 0.05f;   //滑块距离背景的上半部分间距占整个高度的百分比
	
	private String lastSwitchStatus;
	
	private String TAG = "FlashSwitch";
	public interface SwitchLister{
		public void switchOn();
		public void switchOff();
	}
	
	public FlashSwitch(Context context) {
		
		super(context);
		// TODO Auto-generated constructor stub
		
	}
	
	public FlashSwitch(Context context, AttributeSet attrs) {
		
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}
	
	public FlashSwitch(Context context, AttributeSet attrs, int defStyleAttr) {
		
		super(context, attrs, defStyleAttr);
		// TODO Auto-generated constructor stub
		
	}	
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		
		// TODO Auto-generated method stub
		
		super.onLayout(changed, l, t, r, b);
		init();
	}
	
	public void init(){
		if(bottomEdge == 0){
			mThumbGap = (int) (getHeight()*THUMBGAPRATIO);
			layoutParams = (LayoutParams)getChildAt(0).getLayoutParams();
			layoutParams.topMargin = mThumbGap;
			bottomEdge = (int) (getHeight() - getChildAt(0).getHeight() - mThumbGap);
			getChildAt(0).setLayoutParams(layoutParams);
			setOnClickListener(new OnClickListener() {
				
				public void onClick(View v) {
					
					// TODO Auto-generated method stub
					Log.d("FlashSwitch", "on click lastSwitchStatus:"+lastSwitchStatus+",currentSwitchStatus:"+switchStatus);
					if(lastSwitchStatus.equals(switchStatus)){  //点击事件
						if(layoutParams.topMargin == mThumbGap){
							turnOff();
						}else{
							turnOn();
						}
					}
				}
			});
			
		}
	}
	
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		
		// TODO Auto-generated method stub
		switch(event.getAction()){
			case MotionEvent.ACTION_DOWN:
				downY = (int) event.getRawY();
				lastSwitchStatus = switchStatus;
				break;
			case MotionEvent.ACTION_MOVE:
				moveY = (int) event.getRawY();
				moving(moveY,downY);
				break;
			case MotionEvent.ACTION_UP:
				if(isMoveUp(moveY, downY)){
					moveUp();
				}else{
					moveDown();
				}
				break;
		}
		return super.onTouchEvent(event);
	}
	
	
	public void moving(int moveY, int downY) {
		
		// TODO Auto-generated method stub
		int distance = moveY - downY;
		if(switchStatus.equals("on")){ //向下滑
			if(distance > 0 && Math.abs(distance) < (bottomEdge-mThumbGap)){
				layoutParams.topMargin =mThumbGap + distance;
			}else if(distance >= (bottomEdge-mThumbGap)){ //超过滑动范围，取最大的范围值
				moveDown();
			}
		}else{   //向上滑
			 if(distance < 0 && Math.abs(distance) < (bottomEdge-mThumbGap)){
				layoutParams.topMargin = bottomEdge + distance;
			}else if(distance <= -(bottomEdge-mThumbGap)){
				moveUp();
			}
		}
		getChildAt(0).setLayoutParams(layoutParams);
	}

	public boolean isMoveUp(int moveY, int downY){
		return moveY - downY > (bottomEdge-mThumbGap)/2 ? false : true;
	}
	
	
	 /**
	 * 方法描述：滑块向上移动
	 * @param 参数名 说明
	 * @return 返回类型 说明
	 * @see 类名/完整类名/完整类名#方法名
	 */
	public void moveUp(){
		switchStatus = "on";
		layoutParams.topMargin = mThumbGap;
		getChildAt(0).setLayoutParams(layoutParams);
		if(mSwitchLister != null){
			mSwitchLister.switchOn();
		}
	}
	
	
	 /**
	 * 方法描述：滑块向下移动
	 * @param 参数名 说明
	 * @return 返回类型 说明
	 * @see 类名/完整类名/完整类名#方法名
	 */
	public void moveDown(){
		switchStatus = "off";
		layoutParams.topMargin = bottomEdge;
		getChildAt(0).setLayoutParams(layoutParams);
		if(mSwitchLister != null){
			mSwitchLister.switchOff();
		}
	}

	
	 /**
	 * 方法描述：设置监听
	 * @param 参数名 说明
	 * @return 返回类型 说明
	 * @see 类名/完整类名/完整类名#方法名
	 */
	public void setSwitchLister(SwitchLister mSwitchLister){
		this.mSwitchLister = mSwitchLister;
	}
	
	
	 /**
	 * 方法描述：设置switch为开
	 * @param 参数名 说明
	 * @return 返回类型 说明
	 * @see 类名/完整类名/完整类名#方法名
	 */
	public void turnOn(){
		moveUp();
	}
	
	
	 /**
	 * 方法描述：设置switch为关
	 * @param 参数名 说明
	 * @return 返回类型 说明
	 * @see 类名/完整类名/完整类名#方法名
	 */
	public void turnOff(){
		moveDown();
	}
	
	public boolean isSwitch(){
		if(switchStatus.equals("on")){
			return true;
		}else{
			return false;
		}
	}
	
	public void setSwitchStatue(String statue){
		switchStatus = statue;
	}
}

