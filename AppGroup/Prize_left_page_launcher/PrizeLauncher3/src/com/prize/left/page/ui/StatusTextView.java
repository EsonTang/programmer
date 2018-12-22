package com.prize.left.page.ui;

import com.android.launcher3.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.TextView;
/***
 * 带状态的textView
 * @author fanjunchen
 *
 */
public class StatusTextView extends TextView {
	/***
	 * 是否被选中
	 */
	private boolean isSel = false;
	/**正常字体颜色*/
	private int normalColor;
	/**选中字体颜色*/
	private int selColor;

	public StatusTextView(Context context) {
		this(context, null);
	}

	public StatusTextView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public StatusTextView(Context context, AttributeSet attrs, int defStyleAttr) {
		this(context, attrs, defStyleAttr, 0);
	}

	public StatusTextView(Context context, AttributeSet attrs,
			int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		// TODO Auto-generated constructor stub
		TypedArray typedArray = context.obtainStyledAttributes(attrs,
                R.styleable.StatusTextView);
		
		normalColor = typedArray.getColor(R.styleable.StatusTextView_normalColor, Color.WHITE);
		selColor = typedArray.getColor(R.styleable.StatusTextView_selColor, Color.BLACK);
		typedArray.recycle();
	}
	
	public void setSel(boolean sel) {
		isSel = sel;
		if (sel) {
			setTextColor(selColor);
		}
		else
			setTextColor(normalColor);
			
	}
	
	public boolean getSelStatus() {
		return isSel;
	}

}
