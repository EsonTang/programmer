package com.android.lpserver;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


public class MainFragment extends BaseSettingsFragment implements View.OnClickListener{
    private static final String TAG = "MainFragment";
    private SwitchPreference notificationPref;
    private boolean notificationChangeByUser = true;
    private Preference delayEditTextPre;
    private Dialog dialog;
    private ImageView noDelayText;
    private ImageView halfText;
    private ImageView oneText;
    private ImageView randomText;
    private View view;

    public MainFragment(){
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.main);

        //WeChat red envelope switch
        Preference wechatPref = findPreference(Config.KEY_ENABLE_WECHAT);
        wechatPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Log.d(TAG,"isAccessibilitySettingsOn...."+isAccessibilitySettingsOn(getActivity()));
                if((Boolean) newValue && !QiangHongBaoService.isRunning() && !isAccessibilitySettingsOn(getActivity())) {
                    ((MainActivity)getActivity()).showOpenAccessibilityServiceDialog();
                }
                return true;
            }
        });


        //red envelope delay
        delayEditTextPre = findPreference(Config.KEY_WECHAT_DELAY_TIME);
        delayEditTextPre.setWidgetLayoutResource(R.layout.guide_arrow);
        delayEditTextPre.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                dialog = new Dialog(getActivity(),R.style.ActionSheetDialogStyle);
                View inflate = LayoutInflater.from(getActivity()).inflate(R.layout.delay_item, null);
                RelativeLayout delayNullView = (RelativeLayout) inflate.findViewById(R.id.delay_null);
                RelativeLayout delayHalfView = (RelativeLayout) inflate.findViewById(R.id.delay_0_5s);
                RelativeLayout delayOneView = (RelativeLayout) inflate.findViewById(R.id.delay_1s);
                RelativeLayout delayRandomView = (RelativeLayout) inflate.findViewById(R.id.delay_random);
                noDelayText = (ImageView) inflate.findViewById(R.id.no_delay_text);
                halfText = (ImageView) inflate.findViewById(R.id.half_second_text);
                oneText = (ImageView) inflate.findViewById(R.id.one_second_text);
                randomText = (ImageView) inflate.findViewById(R.id.random_text);
                delayNullView.setOnClickListener(MainFragment.this);
                delayHalfView.setOnClickListener(MainFragment.this);
                delayOneView.setOnClickListener(MainFragment.this);
                delayRandomView.setOnClickListener(MainFragment.this);

                dialog.setContentView(inflate);
                Window dialogWindow = dialog.getWindow();
                dialogWindow.setGravity(Gravity.BOTTOM);
                WindowManager.LayoutParams lp = dialogWindow.getAttributes();
                WindowManager windowManager = getActivity().getWindowManager();
                Display display = windowManager.getDefaultDisplay();
                lp.width = display.getWidth();
                dialogWindow.setAttributes(lp);
                dialog.show();


                //Determine the number of SP to set the selected icon
                SharedPreferences sp = getActivity().getSharedPreferences("DELAY",Context.MODE_PRIVATE);
                int delay_time = sp.getInt("delay_time", 4);
                switch (delay_time){
                    case 1:
                        noDelayText.setVisibility(View.VISIBLE);
                        delayEditTextPre.setSummary(getResources().getString(R.string.delay_no));
                        break;
                    case 2:
                        halfText.setVisibility(View.VISIBLE);
                        delayEditTextPre.setSummary(getResources().getString(R.string.delay_half));
                        break;
                    case 3:
                        oneText.setVisibility(View.VISIBLE);
                        delayEditTextPre.setSummary(getResources().getString(R.string.delay_one));
                        break;
                    case 4:
                        randomText.setVisibility(View.VISIBLE);
                        delayEditTextPre.setSummary(getResources().getString(R.string.delay_random));
                        break;
                }

                return true;
            }
        });

        //Red envelope record
        Preference recorderPre = findPreference(Config.WECHAT_RECORDOR);
        recorderPre.setWidgetLayoutResource(R.layout.guide_arrow);
        recorderPre.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(getActivity(),HBRecordorActivity.class));
                return false;
            }
        });

        //Voice alert
        findPreference(Config.KEY_NOTIFY_SOUND).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                QHBApplication.eventStatistics(getActivity(), "notify_sound", String.valueOf(newValue));
                return true;
            }
        });

        //enable single chat red packets
        findPreference(Config.KEY_ENABLE_SINGLE).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                QHBApplication.eventStatistics(getActivity(), "enable_single", String.valueOf(newValue));
                return true;
            }
        });
    }

    /*prize add zhaojian 8.0 2017803 start*/
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                ? inflater.inflate(R.layout.prize_red_packet_preference_list_fragment, container, false)
                : super.onCreateView(inflater,container,savedInstanceState);
    }
    /*prize add zhaojian 8.0 2017803 end*/


    /** Update quick read notification settings*/
    public void updateNotifyPreference() {
        if(notificationPref == null) {
            return;
        }
        boolean running = QiangHongBaoService.isNotificationServiceRunning();    //service != null return true
        boolean enable = Config.getConfig(getActivity()).isEnableNotificationService();
        if( enable && running && !notificationPref.isChecked()) {
            QHBApplication.eventStatistics(getActivity(), "notify_service", String.valueOf(true));
            notificationChangeByUser = false;
            notificationPref.setChecked(true);
        } else if((!enable || !running) && notificationPref.isChecked()) {
            notificationChangeByUser = false;
            notificationPref.setChecked(false);
        }
    }

    /*prize add zhaojian 8.0 2017807 start*/
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onActivityCreated(savedInstanceState);
//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
//            getListView().setDivider(getActivity().getDrawable(R.drawable.list_divider_common));
//            getListView().setDividerHeight(1);
//        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            try {
                Class preferenceFragmentClass = Class.forName("android.preference.PreferenceFragment");
                Method preferenceMethod = preferenceFragmentClass.getDeclaredMethod("getListView");
                ListView listView = (ListView) preferenceMethod.invoke(this);
                listView.setDivider(getActivity().getDrawable(R.drawable.list_divider_common));
                listView.setDividerHeight(1);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }
    /*prize add zhaojian 8.0 2017807 end*/

    @Override
    public void onResume() {
        super.onResume();
        updateNotifyPreference();

        //Determine the stored data to set the random time summary
        SharedPreferences sp = getActivity().getSharedPreferences("DELAY",Context.MODE_PRIVATE);
        int delay_time = sp.getInt("delay_time", 4);
        Log.d(TAG,"delay_time="+delay_time);
        switch (delay_time){
            case 1:
                delayEditTextPre.setSummary(getResources().getString(R.string.delay_no));
                break;
            case 2:
                delayEditTextPre.setSummary(getResources().getString(R.string.delay_half));
                break;
            case 3:
                delayEditTextPre.setSummary(getResources().getString(R.string.delay_one));
                break;
            case 4:
                delayEditTextPre.setSummary(getResources().getString(R.string.delay_random));
                break;
        }
    }

    // To check if service is enabled
    public static boolean isAccessibilitySettingsOn(Context mContext) {
        int accessibilityEnabled = 0;
        final String service = mContext.getPackageName() + "/" + QiangHongBaoService.class.getCanonicalName();
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                    mContext.getApplicationContext().getContentResolver(),
                    android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
            Log.d(TAG, "accessibilityEnabled = " + accessibilityEnabled);
        } catch (Settings.SettingNotFoundException e) {
            Log.d(TAG, "Error finding setting, default accessibility to not found: "
                    + e.getMessage());
        }
        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');

        if (accessibilityEnabled == 1) {
            Log.d(TAG, "***ACCESSIBILITY IS ENABLED*** -----------------");
            String settingValue = Settings.Secure.getString(
                    mContext.getApplicationContext().getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue);
                while (mStringColonSplitter.hasNext()) {
                    String accessibilityService = mStringColonSplitter.next();

                    Log.d(TAG, "-------------- > accessibilityService :: " + accessibilityService + " " + service);
                    if (accessibilityService.equalsIgnoreCase(service)) {
                        Log.d(TAG, "We've found the correct setting - accessibility is switched on!");
                        return true;
                    }
                }
            }
        } else {
            Log.d(TAG, "***ACCESSIBILITY IS DISABLED***");
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        SharedPreferences sp = getActivity().getSharedPreferences("DELAY",Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sp.edit();

        switch (v.getId()){
            case R.id.delay_null:
                delayEditTextPre.setSummary(getResources().getString(R.string.delay_no));
                // Storage of a number, when to grab a red when the number of judgments
                edit.putInt("delay_time",1);
                noDelayText.setVisibility(View.VISIBLE);
                halfText.setVisibility(View.INVISIBLE);
                oneText.setVisibility(View.INVISIBLE);
                randomText.setVisibility(View.INVISIBLE);
                break;
            case R.id.delay_0_5s:
                delayEditTextPre.setSummary(getResources().getString(R.string.delay_half));
                edit.putInt("delay_time",2);
                noDelayText.setVisibility(View.INVISIBLE);
                halfText.setVisibility(View.VISIBLE);
                oneText.setVisibility(View.INVISIBLE);
                randomText.setVisibility(View.INVISIBLE);
                break;
            case R.id.delay_1s:
                delayEditTextPre.setSummary(getResources().getString(R.string.delay_one));
                edit.putInt("delay_time",3);
                noDelayText.setVisibility(View.INVISIBLE);
                halfText.setVisibility(View.INVISIBLE);
                oneText.setVisibility(View.VISIBLE);
                randomText.setVisibility(View.INVISIBLE);
                break;
            case R.id.delay_random:
                delayEditTextPre.setSummary(getResources().getString(R.string.delay_random));
                edit.putInt("delay_time",4);
                noDelayText.setVisibility(View.INVISIBLE);
                halfText.setVisibility(View.INVISIBLE);
                oneText.setVisibility(View.INVISIBLE);
                randomText.setVisibility(View.VISIBLE);
                break;
        }
        edit.commit();
        dialog.dismiss();
    }
}
