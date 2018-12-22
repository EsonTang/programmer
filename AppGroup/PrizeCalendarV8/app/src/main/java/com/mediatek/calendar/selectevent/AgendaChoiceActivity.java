package com.mediatek.calendar.selectevent;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.android.calendar.CalendarController;
import com.android.calendar.R;
import com.android.calendar.agenda.AgendaFragment;
import com.mediatek.calendar.extension.IAgendaChoiceForExt;

import java.lang.reflect.Field;

///M:This class is for Choice calendar item
public class AgendaChoiceActivity extends Activity implements IAgendaChoiceForExt {
    private static final String KEY_OTHER_APP_RESTORE_TIME = "other_app_request_time";

    private CalendarController mController;

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        initStatusBar();
        getActionBarContainer().addView(LayoutInflater.from(this).inflate(R.layout.prize_agendachoice_actionbar, null,false));
        // This needs to be created before setContentView
        mController = CalendarController.getInstance(this);
        setContentView(R.layout.agenda_choice);

        long timeMillis = -1;
        if (icicle != null) {
            timeMillis = icicle.getLong(KEY_OTHER_APP_RESTORE_TIME);
        } else {
            timeMillis = System.currentTimeMillis();
        }

        setFragments(timeMillis);
    }

    private void setFragments(long timeMillis) {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        AgendaFragment frag = new EventSelectionFragment(timeMillis);
        ft.replace(R.id.agenda_choice_frame, frag);
        ft.commit();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(KEY_OTHER_APP_RESTORE_TIME, mController.getTime());
    }

    @Override
    public void retSelectedEvent(Intent ret) {
        setResult(Activity.RESULT_OK, ret);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // To remove its CalendarController instance if exists
        CalendarController.removeInstance(this);
    }

    private void initStatusBar() {
        Window window = getWindow();
        window.setStatusBarColor(getResources().getColor(R.color.prize_bottom_button_bg_color));

        WindowManager.LayoutParams lp = getWindow().getAttributes();
        try {
            Class statusBarManagerClazz = Class.forName("android.app.StatusBarManager");
            Field grayField = statusBarManagerClazz.getDeclaredField("STATUS_BAR_INVERSE_GRAY");
            Object gray = grayField.get(statusBarManagerClazz);
            Class windowManagerLpClazz = lp.getClass();
            Field statusBarInverseField = windowManagerLpClazz.getDeclaredField("statusBarInverse");
            statusBarInverseField.set(lp,gray);
            getWindow().setAttributes(lp);
        } catch (Exception e) {
        }
    }

    public FrameLayout getActionBarContainer() {
        Window window = getWindow();
        View v = window.getDecorView();
        int resId = getResources().getIdentifier("action_bar_container", "id", "android");
        return (FrameLayout)v.findViewById(resId);
    }
}
///@}
