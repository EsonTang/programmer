/*
* created. prize-linkh-20150724
*/
package com.android.settings;

import android.content.Context;
import android.support.v7.preference.CheckBoxPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.content.res.TypedArray;

public class NavBarStylePreference extends CheckBoxPreference {
    private static final String TAG = "NavBarStylePreference";
    private int styleIndex = -1;

    public interface OnClickListener {
        public abstract void onRadioButtonClicked(NavBarStylePreference emiter);
    }

    private OnClickListener mListener = null;

    public NavBarStylePreference(Context context, AttributeSet attrs,
            int defStyle) {
        super(context, attrs, defStyle);

        setLayoutResource(R.layout.preference_navbar_style_prize);
        setWidgetLayoutResource(R.layout.preference_widget_radiobutton);
        
        TypedArray ta = getContext().obtainStyledAttributes(attrs, new int[] {R.attr.navbarStyle});
        styleIndex = ta.getInteger(0, styleIndex);
        ta.recycle();
        Log.d(TAG, "styleIndex ? " + styleIndex);
    }

    public NavBarStylePreference(Context context, AttributeSet attrs) {
        this(context, attrs,
                com.android.internal.R.attr.checkBoxPreferenceStyle);
    }

    public NavBarStylePreference(Context context) {
        this(context, null);
    }

    public int getStyleIndex() {
        return styleIndex;
    }

    void setOnClickListener(OnClickListener listener) {
        mListener = listener;
    }

    @Override
    public void onClick() {
        Log.d(TAG, "onClick()");
        if (isChecked()) {
            return;
        }
        setChecked(true);
        
        if (mListener != null) {            
            mListener.onRadioButtonClicked(this);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("NavBarStylePreference{")
          .append(Integer.toHexString(System.identityHashCode(this)))
          .append("|")
          .append(styleIndex)
          .append("}");
        return sb.toString();
    }
}