package com.prize.permissionmanage;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import com.prize.permissionmanage.R;

/**
 * Created by prize on 2018/1/27.
 */
public class Myperference extends Preference {
    boolean mIsShowIcon = false;
    public Myperference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public Myperference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public Myperference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public Myperference(Context context,boolean isShowIcon) {
        super(context);
            setLayoutResource(R.layout.prize_preference_material_settings);
        setWidgetLayoutResource(R.layout.preference_widget_right_arrow);
        mIsShowIcon = isShowIcon;
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        View view1 = view.findViewById(R.id.icon_frame);
        if(view1 != null && mIsShowIcon){
            view1.setVisibility(View.VISIBLE);
        }else if(view1 != null && !mIsShowIcon){
            view1.setVisibility(View.GONE);
        }
    }

}
