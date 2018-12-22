package com.mediatek.settings.ext;

import android.content.Context;
import android.content.ContextWrapper;
import android.support.v7.preference.PreferenceGroup;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

public class DefaultPplSettingsEntryExt extends ContextWrapper implements IPplSettingsEntryExt {
    private static final String TAG = "PPL/PplSettingsEntryExt";
    public DefaultPplSettingsEntryExt(Context context) {
        super(context);
    }

    public void addPplPrf(PreferenceGroup prefGroup) {
        Log.d(TAG,"addPplPrf() default");
    }

    public void enablerResume() {
        Log.d(TAG,"enablerResume() default");
    }

    public void enablerPause() {
        Log.d(TAG,"enablerPause() default");
    }
}
