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

package com.prize.appcenter.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.HorizontalScrollView;

/**
 * /** 类描述：huanglingjun
 * 
 * @author 作者
 * @version 版本
 */
public class ImgForHorizontalScrollview extends HorizontalScrollView {

	private int currentX;

	public ImgForHorizontalScrollview(Context context, AttributeSet attrs) {

		super(context, attrs);
	}

	public ImgForHorizontalScrollview(Context context) {

		super(context);
		// TODO Auto-generated constructor stub

	}

	public ImgForHorizontalScrollview(Context context, AttributeSet attrs,
			int defStyleAttr) {

		super(context, attrs, defStyleAttr);

	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		int scrollX = getScrollX();
		int x = (int) ev.getX();
		if (ev.getAction() == MotionEvent.ACTION_MOVE) {
			if (currentX == 0) {
				currentX = x;
			}
			// 向左划
			if (currentX > x) {
				if (getWidth() + scrollX >= computeHorizontalScrollRange()) {
					currentX = 0;
					return false;
				}
			} else if (currentX < x) {
				currentX = x;
				return true;
			}
		}
		return super.onInterceptTouchEvent(ev);
	}
}
