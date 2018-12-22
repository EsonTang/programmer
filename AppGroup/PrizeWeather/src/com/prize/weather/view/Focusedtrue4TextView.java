package com.prize.weather.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * 
 * @author wangzhong
 *
 */
public class Focusedtrue4TextView extends TextView {

	public Focusedtrue4TextView(Context context, AttributeSet attrs,
			int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public Focusedtrue4TextView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public Focusedtrue4TextView(Context context) {
		super(context);
	}

	@Override
	public boolean isFocused() {
		return true;
	}

}
