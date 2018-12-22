package android.preference;

import com.prize.internal.R;

import android.content.Context;
import android.preference.Preference;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;
import android.util.Log;

/**
* @hide
*/
public class PrizeSwitchPreference extends Preference {
	private int mWidgetLayoutResId = com.prize.internal.R.layout.prize_switch_preference;

    protected boolean mPrizeState;
    protected boolean mPrizeEnabled = true;
    protected OnCheckedChangeListener mListener;

	public PrizeSwitchPreference(Context context) {
		super(context);
		setWidgetLayoutResource(mWidgetLayoutResId);
	}

    /**
     * Set the radio switch state.
     * @param state On/off.
     */
    public void setPrizeOn(boolean state) {
        mPrizeState = state;
        notifyChanged();
    }

    /**
     * Set the radio switch enable state.
     * @param enable Enable.
     */
    public void setPrizeEnabled(boolean enable) {
        mPrizeEnabled = enable;
        notifyChanged();
    }

    /**
     * Set the listener for radio switch.
     * @param listener Listener of {@link CheckedChangeListener}.
     */
    public void setPrizeSwitchChangeListener(OnCheckedChangeListener listener) {
        mListener = listener;
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        Switch prizeSwitch = (Switch) view.findViewById(com.prize.internal.R.id.prize_state);
        if (prizeSwitch != null) {
            prizeSwitch.setChecked(mPrizeState);
            prizeSwitch.setEnabled(mPrizeEnabled);
			if(!enablePreferenceChange) {
				Log.e("liup","enablePreferenceChange");
            	prizeSwitch.setOnCheckedChangeListener(mListener);
			}else{
				prizeSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
							if (!callChangeListener(isChecked)) {
								Log.e("liup","callChangeListener");
								return;
							}
							Log.e("liup","setPrizeOn");
							setPrizeOn(isChecked);						
					}
				});
			}
        }
    }

    private boolean enablePreferenceChange = false;
	public void enablePreferenceChange(boolean enabled) {
		enablePreferenceChange = enabled;
	}
    @Override
    public void setEnabled(boolean enabled) {
        mPrizeEnabled = enabled;
        super.setEnabled(enabled);
    }

}