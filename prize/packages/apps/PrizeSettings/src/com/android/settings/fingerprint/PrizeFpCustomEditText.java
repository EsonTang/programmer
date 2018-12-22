package com.android.settings.fingerprint;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.EditText;

import com.android.settings.R;

public class PrizeFpCustomEditText extends EditText {  

	private Paint mPaint;  
	/** 
	 * @param context 
	 * @param attrs 
	 */  
	public PrizeFpCustomEditText(Context context, AttributeSet attrs) {  
		super(context, attrs);  
		// TODO Auto-generated constructor stub  
		mPaint = new Paint();  
		mPaint.setStyle(Paint.Style.STROKE);  
		mPaint.setColor(context.getResources().getColor(R.color.prize_fp_dialog_edit_text_line_color));  
	}  

	@Override  
	public void onDraw(Canvas canvas)  {  
		super.onDraw(canvas);  
		canvas.drawLine(0,this.getHeight()-1,  this.getWidth()-1, this.getHeight()-1, mPaint);  
	}  
} 