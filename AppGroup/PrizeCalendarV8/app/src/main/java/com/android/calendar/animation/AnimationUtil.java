package com.android.calendar.animation;

import android.app.Fragment;
import android.content.Context;
import android.support.v4.view.ViewPager;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.Log;
import android.view.View;

import com.android.calendar.R;
import com.android.calendar.Utils;
import com.android.calendar.hormonth.DateUtil;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by hky on 2017/8/3.
 */

public class AnimationUtil {
    public static final AnimationUtil mAnimationUtil = new AnimationUtil();
    int viewHeight;
    int count;
    ViewPager monthView;
    private AnimationUtil(){

    }

    DateUtil mDateUtil = DateUtil.dateUtil;
     public int getCountNum(long timeMillis,Fragment view,Context context){
        if(monthView == null){
            monthView = (ViewPager)view.getView().findViewById(R.id.viewpager);
        }
        Time time = new Time();
        time.set(timeMillis);
        count = mDateUtil.setRows(time.year,time.month+1,context);
        getViewHeight(count);
        long timeOfWeekStart = getTimeOfWeekStart(timeMillis,context);
        Time weekStartTime = new Time();
        weekStartTime.set(timeOfWeekStart);
        int num = -1;
        if((weekStartTime.month==11&&time.month==0) || weekStartTime.month<time.month){
            num = 1;
        }else{
            for(int j=0;j<count;j++){
                if((weekStartTime.monthDay-7*j)<=1){
                    num = j+1;
                    break;
                }
            }
        }
        return num;
    }

    public int getWeek(long timeMillis,Context context) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeMillis);
        //第几周
//        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        Log.d("hekeyi","[AniamtionUtil]-getWeek Utils.getFirstDayOfWeek = "+Utils.getFirstDayOfWeek(context));
        if( Utils.getFirstDayOfWeek(context)==1){
            calendar.setFirstDayOfWeek(Calendar.MONDAY);
        }else if( Utils.getFirstDayOfWeek(context)==0){
            calendar.setFirstDayOfWeek(Calendar.SUNDAY);
        }else if( Utils.getFirstDayOfWeek(context)==6){
            calendar.setFirstDayOfWeek(Calendar.SATURDAY);
        }
        int week = calendar.get(Calendar.WEEK_OF_MONTH);
        //第几天，从周日开始
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        Log.d("hekeyi","[AniamtionUtil]-getWeek week = "+week+" day = "+day);
        return week;
    }

    public int getCountNum(long timeMillis,Context context){
        Time time = new Time();
        time.set(timeMillis);
        count = mDateUtil.setRows(time.year,time.month+1,context);
        getViewHeight(count);
        long timeOfWeekStart = getTimeOfWeekStart(timeMillis,context);
        Time weekStartTime = new Time();
        weekStartTime.set(timeOfWeekStart);
        int num = -1;
        if((weekStartTime.month==11&&time.month==0) || weekStartTime.month<time.month){
            num = 1;
        }else{
            for(int j=0;j<count;j++){
                if((weekStartTime.monthDay-7*j)<=1){
                    num = j+1;
                    break;
                }
            }
        }

        return num;
    }


    private long getTimeOfWeekStart(long timeMillis,Context context){
        Time time1 = new Time();
        time1.set(timeMillis);
        Calendar ca = Calendar.getInstance();
        ca.setTimeInMillis(timeMillis);
        Log.d("hekeyi","[AnimationUtil]-getTimeOfWeekStart Utils.getFirstDayOfWeek(context) = "+Utils.getFirstDayOfWeek(context));
        if( Utils.getFirstDayOfWeek(context)==1){
            ca.setFirstDayOfWeek(Calendar.MONDAY);
        }else if( Utils.getFirstDayOfWeek(context)==0){
            ca.setFirstDayOfWeek(Calendar.SUNDAY);
        }else if( Utils.getFirstDayOfWeek(context)==6){
            ca.setFirstDayOfWeek(Calendar.SATURDAY);
        }
        ca.set(Calendar.DAY_OF_WEEK, ca.getFirstDayOfWeek());

        return ca.getTimeInMillis();
    }


    private void getViewHeight(int count){
        if(count<6){
            monthView.getHeight();
            viewHeight = monthView.getHeight()/5;
        }else {
            viewHeight = monthView.getHeight()/6;
        }
    }



    public static void setMainTitleBackground(Context mContext, int month, View view){
        if(view==null) return;
        switch (month){
            case 0:
                view.setBackground(mContext.getResources().getDrawable(R.drawable.title_bg0));
                break;
            case 1:
                view.setBackground(mContext.getResources().getDrawable(R.drawable.title_bg1));
                break;
            case 2:
                view.setBackground(mContext.getResources().getDrawable(R.drawable.title_bg2));
                break;
            case 3:
                view.setBackground(mContext.getResources().getDrawable(R.drawable.title_bg3));
                break;
            case 4:
                view.setBackground(mContext.getResources().getDrawable(R.drawable.title_bg4));
                break;
            case 5:
                view.setBackground(mContext.getResources().getDrawable(R.drawable.title_bg5));
                break;
            case 6:
                view.setBackground(mContext.getResources().getDrawable(R.drawable.title_bg6));
                break;
            case 7:
                view.setBackground(mContext.getResources().getDrawable(R.drawable.title_bg7));
                break;
            case 8:
                view.setBackground(mContext.getResources().getDrawable(R.drawable.title_bg8));
                break;
            case 9:
                view.setBackground(mContext.getResources().getDrawable(R.drawable.title_bg9));
                break;
            case 10:
                view.setBackground(mContext.getResources().getDrawable(R.drawable.title_bg10));
                break;
            case 11:
                view.setBackground(mContext.getResources().getDrawable(R.drawable.title_bg11));
                break;
        }
    }


}
