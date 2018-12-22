package com.goodix.util;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.EditText;

import com.goodix.fpsetting.R;

public class CustomEditText extends EditText {  

	private Paint mPaint;  
	/** 
	 * @param context 
	 * @param attrs 
	 */  
	public CustomEditText(Context context, AttributeSet attrs) {  
		super(context, attrs);  
		// TODO Auto-generated constructor stub  
		mPaint = new Paint();  
		mPaint.setStyle(Paint.Style.STROKE);  
		mPaint.setColor(context.getResources().getColor(R.color.dialog_edit_text_line_color));  
	}  

	@Override  
	public void onDraw(Canvas canvas)  {  
		super.onDraw(canvas);  
		canvas.drawLine(0,this.getHeight()-1,  this.getWidth()-1, this.getHeight()-1, mPaint);  
	}  
} 