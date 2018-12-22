package com.prize.music.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

/**
 * 
 **
 * 解决嵌套只显示部分的GridView
 * 
 * @author longbaoxiu
 * @version V1.0
 */
public class SongListGridView extends GridView {
	public SongListGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public SongListGridView(Context context) {
		super(context);
	}

	public SongListGridView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

		int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
				MeasureSpec.AT_MOST);
		super.onMeasure(widthMeasureSpec, expandSpec);
	}
}
