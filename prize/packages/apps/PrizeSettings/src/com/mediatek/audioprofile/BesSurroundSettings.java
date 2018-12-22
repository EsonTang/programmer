package com.mediatek.audioprofile;

import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v7.preference.PreferenceScreen;
import android.util.Log;
import android.widget.Switch;

import com.android.settings.InstrumentedFragment;
import com.android.settings.R;
import com.android.settings.SettingsActivity;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.widget.SwitchBar;

public class BesSurroundSettings extends SettingsPreferenceFragment implements
        SwitchBar.OnSwitchChangeListener, BesSurroundItem.OnClickListener {

    private static final String XLOGTAG = "Settings/AudioP";
    private static final String TAG = "BesSurroundSettings:";
    private Context mContext;
    private Switch mSwitch;
    private SwitchBar mSwitchBar;
    private boolean mCreated;
    private boolean mValidListener;

    private static final String KEY_MOVIE_MODE = "movie_mode";
    private static final String KEY_MUSIC_MODE = "music_mode";

    private BesSurroundItem mMovieMode;
    private BesSurroundItem mMusicMode;
    private BesSurroundItem[] mBesSurroundItems;

    private AudioManager mAudioManager;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (mCreated) {
            mSwitchBar.show();
            return;
        }
        mCreated = true;
        PreferenceScreen root = getPreferenceScreen();
        if (root != null) {
            root.removeAll();
        }
        addPreferencesFromResource(R.xml.audioprofile_bessurrond_settings);
        mContext = getActivity();
        mMovieMode = (BesSurroundItem) findPreference(KEY_MOVIE_MODE);
        mMusicMode = (BesSurroundItem) findPreference(KEY_MUSIC_MODE);
        mMovieMode.setOnClickListener(this);
        mMusicMode.setOnClickListener(this);
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mSwitchBar = ((SettingsActivity) mContext).getSwitchBar();
        mSwitch = mSwitchBar.getSwitch();
        mSwitchBar.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mSwitchBar.hide();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!mValidListener) {
            mSwitchBar.addOnSwitchChangeListener(this);
            mValidListener = true;
        }
        initBesSurroundStatus();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mValidListener) {
            mSwitchBar.removeOnSwitchChangeListener(this);
            mValidListener = false;
        }
    }

    @Override
    public void onSwitchChanged(Switch switchView, boolean isChecked) {
        Log.d("@M_" + XLOGTAG, TAG + "onSwitchChanged: " + isChecked);
        SoundEnhancement.setBesSurroundState(mAudioManager, isChecked);
        getPreferenceScreen().setEnabled(isChecked);
    }

    private void initBesSurroundStatus() {
        mSwitch.setChecked(SoundEnhancement.getBesSurroundState(mAudioManager));
        getPreferenceScreen().setEnabled(mSwitch.isChecked());
        boolean modeMovie = false;
        modeMovie = SoundEnhancement.getBesSurroundMode(mAudioManager)
            == SoundEnhancement.BESSURROUND_MODE_MOVIE;
        mMovieMode.setChecked(modeMovie);
        mMusicMode.setChecked(!modeMovie);
    }

    public void onRadioButtonClicked(BesSurroundItem emiter) {
        if (emiter == mMovieMode) {
            SoundEnhancement.setBesSurroundMode(mAudioManager,
                    SoundEnhancement.BESSURROUND_MODE_MOVIE);
            mMusicMode.setChecked(false);
        } else if (emiter == mMusicMode) {
            SoundEnhancement.setBesSurroundMode(mAudioManager,
                    SoundEnhancement.BESSURROUND_MODE_MUSIC);
            mMovieMode.setChecked(false);
        }
        emiter.setChecked(true);
    }

    @Override
    protected int getMetricsCategory() {
        return InstrumentedFragment.METRICS_SOUNDENHANCEMENT;
    }
}
