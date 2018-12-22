package com.android.settings.widget.wheelView;

import android.app.AlarmManager;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.LoginFilter;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.android.settings.R;

import java.text.Format;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by prize on 2018/3/15.
 */

public class PrizeTimeDialog extends Dialog implements View.OnClickListener, OnWheelChangedListener, OnWheelScrollListener {

    private WheelView hours;
    private WheelView minuts;
    private TextView dialogTile;
    private WheelView amPm;
    private String[] amAndPm;

    public PrizeTimeDialog(@NonNull Context context) {
        this(context, R.style.prize_event_status);
        //  initDialog();

    }

    public PrizeTimeDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    protected PrizeTimeDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    private void initDialog() {

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindow().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        int height = (int) getContext().getResources().getDimension(R.dimen.prize_time_dialog_height);
//        WindowManager.LayoutParams lp = getWindow().getAttributes();
//        lp.width = width;
//        lp.height = height;
//        lp.gravity = Gravity.BOTTOM;
//        getWindow().setAttributes(lp);
        getWindow().setLayout(width,height);
        getWindow().setGravity(Gravity.BOTTOM);
    }

    private void initView() {
        amAndPm = getContext().getResources().getStringArray(R.array.prize_time_picker);
        View v = LayoutInflater.from(getContext()).inflate(R.layout.prize_time_dialog_picker, null);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(v);
        amPm = (WheelView) v.findViewById(R.id.date);
        hours = (WheelView) v.findViewById(R.id.hours);
        minuts = (WheelView) v.findViewById(R.id.minutes);
        dialogTile = (TextView) v.findViewById(R.id.dialog_title);
        Button cancel = (Button) v.findViewById(R.id.cancel);
        Button confirm = (Button) v.findViewById(R.id.confirm);
        cancel.setOnClickListener(this);
        confirm.setOnClickListener(this);
        if (is24Hour()) {
            amPm.setVisibility(View.GONE);
            amPm.setVisibleItems(2);
            hours.setAdapter(new NumericWheelAdapter(0, 23,"%1$02d"));
        } else {
            amPm.setVisibility(View.VISIBLE);
            amPm.setAdapter(new ArrayWheelAdapter<String>(amAndPm));
            hours.setAdapter(new NumericWheelAdapter(1, 12,"%1$02d"));
        }
        hours.addChangingListener(this);
        hours.addScrollingListener(this);
        hours.setCyclic(true);
        minuts.setAdapter(new NumericWheelAdapter(0, 59,"%1$02d"));
        minuts.addChangingListener(this);
        minuts.addScrollingListener(this);
        minuts.setCyclic(true);
        dialogTile.setText(getCurrentTime());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initDialog();

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cancel:
                dismiss();
                break;

            case R.id.confirm:
                int hour;
                if (is24Hour()) {
                    hour = hours.getCurrentItem();
                } else {
                    hour = hours.getCurrentItem() + 1;
                }

                int minut = minuts.getCurrentItem();
                setTime(getContext(), hour, minut);
                dismiss();
                break;
            default:
                break;

        }
    }

    /* package */
    private void setTime(Context context, int hourOfDay, int minute) {
        Calendar c = Calendar.getInstance();
        if(!is24Hour()){
            int amOrPm = amPm.getCurrentItem();
            if(hourOfDay == 12){
                hourOfDay = 0;
            }
            c.set(Calendar.HOUR, hourOfDay);
            c.set(Calendar.AM_PM,amOrPm);

         }else{
            c.set(Calendar.HOUR_OF_DAY, hourOfDay);
        }
        c.set(Calendar.MINUTE, minute);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        /* prize-modify-by-lijimeng-for bugid 44711-20171206-start*/
        // long when = Math.max(c.getTimeInMillis(), MIN_DATE);
        long when = c.getTimeInMillis();
        /* prize-modify-by-lijimeng-for bugid 44711-20171206-end*/
        if (when / 1000 < Integer.MAX_VALUE) {
            ((AlarmManager) context.getSystemService(Context.ALARM_SERVICE)).setTime(when);
        }
    }

    private String getCurrentTime() {
        Calendar calendar = Calendar.getInstance();
        int hour;
        if (!is24Hour()) {
            int amOrPm = calendar.get(Calendar.AM_PM);
            amPm.setCurrentItem(amOrPm);
            hour = calendar.get(Calendar.HOUR);
            if(hour == 0){
                hour = 12;
            }
            hours.setCurrentItem(hour - 1);
        } else {
            hour = calendar.get(Calendar.HOUR_OF_DAY);
            hours.setCurrentItem(hour);
        }
        int minute = calendar.get(Calendar.MINUTE);
        minuts.setCurrentItem(minute);
        StringBuffer stringBuffer = new StringBuffer();
        String.format(Locale.getDefault(),"%1$02d",minute);
        stringBuffer.append(String.valueOf(hour)).append(":").append(String.format("%1$02d",minute));
        return stringBuffer.toString();
    }

    @Override
    public void onChanged(View wheel, int oldValue, int newValue) {
        setDialogTile();
        if (!is24Hour()) {
            if ((newValue == 11 && oldValue == 10) || (newValue == 10 && oldValue == 11)) {
                int index = amPm.getCurrentItem();
                if (index > 0) {
                    amPm.setCurrentItem(index - 1);
                } else {
                    amPm.setCurrentItem(index + 1);
                }
            }
        }
    }

    @Override
    public void onScrollingStarted(View wheel) {
    }

    @Override
    public void onScrollingFinished(View wheel) {
    }

    private void setDialogTile() {
        int hour;
        int minute;
        if (is24Hour()) {
            hour = hours.getCurrentItem();
            minute = minuts.getCurrentItem();
        } else {
            hour = hours.getCurrentItem() + 1;
            minute = minuts.getCurrentItem();
        }

        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(String.valueOf(hour)).append(":").append(String.format("%1$02d",minute));
        String dialogTitle = stringBuffer.toString();
        dialogTile.setText(dialogTitle);
    }

    private boolean is24Hour() {
        return DateFormat.is24HourFormat(getContext());
    }
}
