package com.prize.appcenter.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.TextView;

public class NsTextView extends TextView {
	private String text;
	private float textSize;
	private float paddingLeft;
	private float paddingRight;
	private int textColor;
	private Paint paint1 = new Paint();
	private float textShowWidth;

	public NsTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		text = this.getText().toString();
		textSize = this.getTextSize();
		textColor = this.getTextColors().getDefaultColor();
		paddingLeft = this.getPaddingLeft();
		paddingRight = this.getPaddingRight();
		paint1.setTextSize(textSize);
		paint1.setColor(textColor);
		paint1.setAntiAlias(true);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		textShowWidth = this.getMeasuredWidth() - paddingLeft - paddingRight;
		int lineCount = 0;
		text = this.getText().toString();
		if (text == null)
			return;
		char[] textCharArray = text.toCharArray();
		float drawedWidth = 0;
		float charWidth;
		for (int i = 0; i < textCharArray.length; i++) {
			charWidth = paint1.measureText(textCharArray, i, 1);
			if (textCharArray[i] == '\n') {
				lineCount++;
				drawedWidth = 0;
				continue;
			}
			if (textShowWidth - drawedWidth < charWidth) {
				lineCount++;
				drawedWidth = 0;
			}
			canvas.drawText(textCharArray, i, 1, paddingLeft + drawedWidth,
					(lineCount + 1) * textSize, paint1);
			drawedWidth += charWidth;
		}
		setHeight((int) ((lineCount + 1) * (int) textSize));
	}
}
