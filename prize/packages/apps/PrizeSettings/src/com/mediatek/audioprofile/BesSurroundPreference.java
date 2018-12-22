package com.mediatek.audioprofile;

import android.content.Context;
import android.media.AudioManager;
import android.support.v7.preference.PreferenceViewHolder;
import android.support.v7.preference.TwoStatePreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Checkable;
import android.widget.CompoundButton;
import android.widget.Switch;

public class BesSurroundPreference extends TwoStatePreference {

    private static final String XLOGTAG = "Settings/AudioP";

    private AudioManager mAudioManager;
    private final Listener mListener = new Listener();

    private class Listener implements CompoundButton.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            Log.d("@M_" + XLOGTAG, "BesSurroundPreference onChange value :" + isChecked);
            //set the bessurround state
            SoundEnhancement.setBesSurroundState(mAudioManager, isChecked);
            BesSurroundPreference.this.setChecked(isChecked);
        }
    }

    public BesSurroundPreference(Context context, AttributeSet attrs,
            int defStyle) {
        super(context, attrs, defStyle);
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }

    public BesSurroundPreference(Context context, AttributeSet attrs) {
        this(context, attrs, com.android.internal.R.attr.switchPreferenceStyle);
    }

    public BesSurroundPreference(Context context) {
        this(context, null);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        BesSurroundPreference.this.setChecked(
                SoundEnhancement.getBesSurroundState(mAudioManager));
        View checkableView = holder.findViewById(com.mediatek.internal.R.id.imageswitch);
        if (checkableView != null && checkableView instanceof Checkable) {
            ((Checkable) checkableView).setChecked(isChecked());

            //sendAccessibilityEvent(checkableView);

            if (checkableView instanceof Switch) {
                final Switch switchView = (Switch) checkableView;
                switchView.setFocusable(false);
                switchView.setOnCheckedChangeListener(mListener);
            }
        }

        //syncSummaryView(view);
    }


    @Override
    protected void onClick() {
        super.onClick();
    }

}
