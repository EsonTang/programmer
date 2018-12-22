package com.android.prize.simple.model;

import org.xutils.common.util.LogUtil;

import com.android.prize.simple.ui.SimplePage;

import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.view.View;
/***
 * 设置位置的变化监听器
 * @author fanjunchen
 *
 */
public class AnimatorUpdateXYListener implements
		AnimatorUpdateListener {
	/***
	 * 目标对象
	 */
	public View targetView;
	/**布局对象**/
	public SimplePage.LayoutParams mParams;
	/**x轴移动距离 可以是负数*/
	public int xLen = 0;
	/**y轴移动距离 可以是负数*/
	public int yLen = 0;
	
	private int startX = -1, startY = -1;
	
	public AnimatorUpdateXYListener() {
	}

	@Override
	public void onAnimationUpdate(ValueAnimator vAnim) {
		// TODO Auto-generated method stub
		Float f = (Float)vAnim.getAnimatedValue();
		
		if (xLen != 0 && mParams != null) {
			int r = (int)(xLen * f + + 0.5f);
			if (startX == -1)
				startX = mParams.getX();
			mParams.setX(startX + r);
		}
		
		if (yLen != 0 && mParams != null) {
			int r = (int)(yLen * f + + 0.5f);
			if (startY == -1)
				startY = mParams.getY();
			mParams.setY(startY + r);
		}
		
		//LogUtil.i(toString() + ";f==" + f);
		
		targetView.setLayoutParams(mParams);
	}
	
    @Override
    public String toString() {
    	// TODO Auto-generated method stub
    	return "===xLen=" + xLen 
    			+ ";yLen=" + yLen
    			+ ";startX=" + startX
    			+ ";startY=" + startY
    			+ ";x=" + mParams.getX()
    			+ ";y=" + mParams.getY();
    }

}
