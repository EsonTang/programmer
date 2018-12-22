/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：
 *当前版本：
 *作	者：huanglingjun
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
import android.widget.ScrollView;

/**
 * 类描述：垃圾清理结果页滑动需要监听事件
 * 
 * @author huanglingjun
 * @version 版本
 */
public class NewScrollView extends ScrollView {
	public ScrollChangeListener scrollChangeListener;

	public NewScrollView(Context context) {
		super(context);
	}

	public NewScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}


	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {

		// TODO Auto-generated method stub
		super.onScrollChanged(l, t, oldl, oldt);
		if (scrollChangeListener != null) {
			scrollChangeListener.onScroll(t);
		}
	}

	public void setOnScrollChangedListener(ScrollChangeListener scrollChangeListener) {
		this.scrollChangeListener = scrollChangeListener;
	}

	public static interface ScrollChangeListener {
		void onScroll(int scrollY);
	}
}
