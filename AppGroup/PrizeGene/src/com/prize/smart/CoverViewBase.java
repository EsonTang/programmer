/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：防误触-自定义view接口
 *当前版本：
 *作	者： 钟卫林
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

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

public abstract class CoverViewBase extends View {
	public static interface Callback {
		public void onDoubleClick();
	}

	Callback mCallback = null;

	public void setCallback(Callback callback) {
		mCallback = callback;
	}

	abstract void onShow();

	public CoverViewBase(Context context) {
		super(context);
	}

	public CoverViewBase(Context context, AttributeSet attr) { // be called!
		super(context, attr);
	}

	public CoverViewBase(Context context, AttributeSet attr, int defStyle) {
		super(context, attr, defStyle);
	}
}
