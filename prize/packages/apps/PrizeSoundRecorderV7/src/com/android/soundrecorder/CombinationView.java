package com.android.soundrecorder;


import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

public class CombinationView extends FrameLayout {

    private ImageView mIconView;  
    private TextView mTextView;
    
    public CombinationView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    
    public CombinationView(Context context) {
        super(context);
    }
    
    public CombinationView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.bottom_view, this); 
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CombinationView);  
        int icon = a.getResourceId(R.styleable.CombinationView_icon, 0);
        int text = a.getResourceId(R.styleable.CombinationView_text, 0);
        mIconView = (ImageView) findViewById(R.id.icon);
        mTextView = (TextView) findViewById(R.id.text);
        if (icon > 0) {
            mIconView.setImageResource(icon);
        }
        
        if (text > 0) {
            mTextView.setText(text);
        }
        a.recycle();
    }

    public void setText(int resId) {
        mTextView.setText(resId);
    }
    
    public void setIcon(int resId) {
        mIconView.setImageResource(resId);
    }

	@Override
	public void setEnabled(boolean enabled) {
		mIconView.setEnabled(enabled);
		mTextView.setEnabled(enabled);
		super.setEnabled(enabled);
	}
    
}
