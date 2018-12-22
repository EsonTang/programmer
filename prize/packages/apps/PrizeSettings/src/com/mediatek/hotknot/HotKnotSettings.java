package com.mediatek.settings.hotknot;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.settings.InstrumentedFragment;
import com.android.settings.R;
import com.android.settings.SettingsActivity;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.dashboard.SummaryLoader;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.SearchIndexableRaw;
import com.android.settings.widget.SwitchBar;

import com.mediatek.hotknot.HotKnotAdapter;

import java.util.ArrayList;
import java.util.List;

public class HotKnotSettings extends SettingsPreferenceFragment implements Indexable  {
    private static final String TAG = "HotKnotSettings";
    private HotKnotEnabler mHotKnotEnabler;
    private IntentFilter mIntentFilter;
    private HotKnotAdapter mAdapter;

    private SwitchBar mSwitchBar;

    /**
     * The broadcast receiver is used to handle the nfc adapter state changed
     */
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

        }
    };

    @Override
    protected int getMetricsCategory() {
        return InstrumentedFragment.METRICS_HOTKNOT;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SettingsActivity activity = (SettingsActivity) getActivity();
        mAdapter = HotKnotAdapter.getDefaultAdapter(activity);
        if (mAdapter == null) {
            Log.d("@M_" + TAG, "Hotknot adapter is null, finish Hotknot settings");
            getActivity().finish();
        }

        mIntentFilter = new IntentFilter(HotKnotAdapter.ACTION_ADAPTER_STATE_CHANGED);
    }

    @Override
    public void onStart() {
        super.onStart();

        // On/off switch
        final SettingsActivity activity = (SettingsActivity) getActivity();
        mSwitchBar = activity.getSwitchBar();
        Log.d("@M_" + TAG, "onCreate, mSwitchBar = " + mSwitchBar);
        mHotKnotEnabler = new HotKnotEnabler(activity, mSwitchBar);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.hotknot_settings, container, false);
        TextView textView = (TextView) view
                .findViewById(R.id.hotknot_warning_msg);
        if (textView != null) {
            textView.setText(getString(R.string.hotknot_charging_warning,
                    getString(R.string.hotknot_settings_title)));
        }
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mHotKnotEnabler != null) {
            mHotKnotEnabler.teardownSwitchBar();
        }
    }

    public void onResume() {
        super.onResume();
        if (mHotKnotEnabler != null) {
            mHotKnotEnabler.resume();
        }
        getActivity().registerReceiver(mReceiver, mIntentFilter);
    }

    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(mReceiver);
        if (mHotKnotEnabler != null) {
            mHotKnotEnabler.pause();
        }
    }

    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
    new BaseSearchIndexProvider() {
        @Override
        public List<SearchIndexableRaw> getRawDataToIndex(Context context, boolean enabled) {
            final List<SearchIndexableRaw> result = new ArrayList<SearchIndexableRaw>();
            final Resources res = context.getResources();

            HotKnotAdapter adapter = HotKnotAdapter.getDefaultAdapter(context);
            if (adapter != null) {
                // Add fragment title
                SearchIndexableRaw data = new SearchIndexableRaw(context);
                data.title = res.getString(R.string.hotknot_settings_title);
                data.screenTitle = res.getString(R.string.hotknot_settings_title);
                data.keywords = res.getString(R.string.hotknot_settings_title);
                result.add(data);
            }
            return result;
        }
    };

    private static class SummaryProvider extends BroadcastReceiver
            implements SummaryLoader.SummaryProvider {

        private final Context mContext;
        private final SummaryLoader mSummaryLoader;
        private HotKnotAdapter mAdapter;

        public SummaryProvider(Context context, SummaryLoader summaryLoader) {
            mContext = context;
            mSummaryLoader = summaryLoader;
            mAdapter = HotKnotAdapter.getDefaultAdapter(context);
        }

        @Override
        public void setListening(boolean listening) {
            if (listening) {
                if (mAdapter != null) {
                    IntentFilter intentFilter = new IntentFilter();
                    intentFilter.addAction(HotKnotAdapter.ACTION_ADAPTER_STATE_CHANGED);
                    mContext.registerReceiver(this, intentFilter);
                    int initState = mAdapter.isEnabled() ? HotKnotAdapter.STATE_ENABLED
                            : HotKnotAdapter.STATE_DISABLED;
                    mSummaryLoader.setSummary(this, getSummary(initState));
                }
            } else {
                if (mAdapter != null) {
                    mContext.unregisterReceiver(this);
                }
            }
        }

        private String getSummary(int state) {
            String summary = null;
            switch (state) {
                case HotKnotAdapter.STATE_ENABLED:
                    summary = mContext.getResources().getString(R.string.switch_on_text);
                    break;
                case HotKnotAdapter.STATE_DISABLED:
                    summary = mContext.getResources().getString(R.string.switch_off_text);
                    break;
                default:
                    break;
            }
            return summary;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            int state = intent.getIntExtra(HotKnotAdapter.EXTRA_ADAPTER_STATE, -1);
            Log.d(TAG, "HotKnot state changed to " + state);
            mSummaryLoader.setSummary(this, getSummary(state));
        }
    }

    public static final SummaryLoader.SummaryProviderFactory SUMMARY_PROVIDER_FACTORY
            = new SummaryLoader.SummaryProviderFactory() {
        @Override
        public SummaryLoader.SummaryProvider createSummaryProvider(
                Activity activity, SummaryLoader summaryLoader) {
            return new SummaryProvider(activity, summaryLoader);
        }
    };

}

class HotKnotDescriptionPref extends Preference {
    public HotKnotDescriptionPref(Context context, AttributeSet attrs,
            int defStyle) {
        super(context, attrs, defStyle);
    }

    public HotKnotDescriptionPref(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        TextView title = (TextView) holder.findViewById(android.R.id.title);
        if (title != null) {
            title.setSingleLine(false);
            title.setMaxLines(3);
        }
    }

}
