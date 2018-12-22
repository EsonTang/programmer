package com.android.gallery3d.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.gallery3d.R;

/**
 * photoeditor UI
 * @author wanzhijuan
 * 2015-11-20
 *
 */
public class TabView extends FrameLayout {

    private ImageView mIconView;
    private TextView mTextView;

    public TabView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TabView(Context context) {
        super(context);
    }

    public TabView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.tab_view_item, this);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TabView);
        int icon = a.getResourceId(R.styleable.TabView_tabview_icon, 0);
        int text = a.getResourceId(R.styleable.TabView_tabview_text, 0);
        mIconView = (ImageView) findViewById(R.id.icon);
        mTextView = (TextView) findViewById(R.id.text);
        if (icon > 0) {
            mIconView.setImageResource(icon);
        }
        
        if (text > 0) {
            mTextView.setText(text);
        }
//        a.recycle();
    }

    public void setText(int resId) {
        mTextView.setText(resId);
    }
    
    public void setIcon(int resId) {
        mIconView.setImageResource(resId);
    }
    
    @Override
    public void setSelected(boolean isSelect) {
        mIconView.setSelected(isSelect);
        mTextView.setSelected(isSelect);
    }
    
}
