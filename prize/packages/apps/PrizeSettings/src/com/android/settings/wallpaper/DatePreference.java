package com.android.settings.wallpaper;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.os.Handler;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.Animator;
import com.android.settings.R;
//import android.preference.Preference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;

public class DatePreference extends Preference implements OnClickListener{

    private static Context mContext;
    private static TextView text;
    private static ImageView image;
    private static ObjectAnimator objectAnim;

    private OnDateClickListener mOnDateClickListener;

    public DatePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWidgetLayoutResource(R.layout.prize_date_preference_layout);
        mContext = context;
    }


    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.date_text){
            Log.i("wallpaper","DatePreference  onClick.. **");
            if(mOnDateClickListener != null){
                mOnDateClickListener.onDateClick(this);
            }
        }
    }


    public interface OnDateClickListener{
        void onDateClick(DatePreference p);
    }
    
    public void setOnDateClickListener(OnDateClickListener l) {
    	mOnDateClickListener = l;
        notifyChanged();
    }

    public static void clearAnimation(){
    	Log.i("wallpaper","datePreference clearAnimation ");
    	image.clearAnimation();
        image.setVisibility(View.GONE);
        text.setVisibility(View.VISIBLE);
    }
    public static void startAnimation(){
        text.setVisibility(View.GONE);
        image.setVisibility(View.VISIBLE);
        Animation progressAni = AnimationUtils.loadAnimation(mContext, R.anim.update_magazine_data);
        LinearInterpolator interpolator = new LinearInterpolator();
        progressAni.setInterpolator(interpolator);
        if(progressAni != null){
            image.startAnimation(progressAni);
        }
        
    }
    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
    	super.onBindViewHolder(holder);
        text = (TextView) holder.findViewById(R.id.date_text);
        image = (ImageView) holder.findViewById(R.id.date_image);
        text.setOnClickListener(this);
        text.setEnabled(true);
    }


}
