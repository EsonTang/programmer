package com.android.lpserver;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Dialog;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityManager;
import android.widget.TextView;
import android.widget.Toast;

import com.android.lpserver.util.StatusBarUtils;

import java.lang.reflect.Field;
import java.util.List;


/**
 * Grab the main interface
 */
public class MainActivity extends BaseSettingsActivity {
    private static final String TAG = "MainActivity";

    private Dialog mTipsDialog;
    private MainFragment mMainFragment;

    public MainActivity(){
        super();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //prize modify v8.0 zhaojian 2017912 start
        //setNotificationStatus();
        StatusBarUtils.setStatusBar(getWindow(),this);
        //prize modify v8.0 zhaojian 2017912 end

        setActionBar();
        QHBApplication.activityStartMain(this);

        IntentFilter filter = new IntentFilter();
        filter.addAction(Config.ACTION_QIANGHONGBAO_SERVICE_CONNECT);
        filter.addAction(Config.ACTION_QIANGHONGBAO_SERVICE_DISCONNECT);
        filter.addAction(Config.ACTION_NOTIFY_LISTENER_SERVICE_DISCONNECT);
        filter.addAction(Config.ACTION_NOTIFY_LISTENER_SERVICE_CONNECT);
        registerReceiver(qhbConnectReceiver, filter);
        Log.d(TAG,"MainActivity...onCreate...registerReceiver");

        // qq
        updateServiceStatus();
    }

    private void setActionBar() {
        /*prize modify zhaojian 8.0 2017803 start*/
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });

        }else {
            Toolbar toolbar = (Toolbar) findViewById(R.id.helper_toolBar);
            toolbar.setTitle("");               //Let the original title toolbar does not display
            TextView textView = (TextView) findViewById(R.id.toolbar_title);
            textView.setText(R.string.app_name);
            toolbar.setBackgroundColor(getResources().getColor(R.color.white));
            toolbar.setNavigationIcon(R.drawable.back_button);
            setSupportActionBar(toolbar);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }
        /*prize modify zhaojian 8.0 2017803 end*/
    }

    private void setNotificationStatus() {
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                //| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            window.setStatusBarColor(getResources().getColor(R.color.prize_actionbar_bg_color_v8));      // prize modify zhaojian 8.0 2017803
        }else {
            window.setStatusBarColor(Color.TRANSPARENT);
        }

        WindowManager.LayoutParams lp = getWindow().getAttributes();
//        lp.statusBarInverse = StatusBarManager.STATUS_BAR_INVERSE_GRAY;
//        getWindow().setAttributes(lp);

        try {
            Class statusBarManagerClazz = Class.forName("android.app.StatusBarManager");
            Field grayField = statusBarManagerClazz.getDeclaredField("STATUS_BAR_INVERSE_GRAY");
            Object gray = grayField.get(statusBarManagerClazz);
            Class windowManagerLpClazz = lp.getClass();
            Field statusBarInverseField = windowManagerLpClazz.getDeclaredField("statusBarInverse");
            statusBarInverseField.set(lp,gray);
            getWindow().setAttributes(lp);
        } catch (Exception e) {
            Log.d(TAG,"error");
        }
    }

    @Override
    protected boolean isShowBack() {
        return false;
    }

    @Override
    public Fragment getSettingsFragment() {
        mMainFragment = new MainFragment();
        return mMainFragment;
    }

    private BroadcastReceiver qhbConnectReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(isFinishing()) {
                return;
            }
            String action = intent.getAction();
            Log.d(TAG, "receive-->" + action);
            if(Config.ACTION_QIANGHONGBAO_SERVICE_CONNECT.equals(action)) {
                if (mTipsDialog != null) {
                    mTipsDialog.dismiss();
                }
            } /*else if(Config.ACTION_QIANGHONGBAO_SERVICE_DISCONNECT.equals(action)) {
                Log.d(TAG,"hongbao service disconnect");
                showOpenAccessibilityServiceDialog();
            }*/ else if(Config.ACTION_NOTIFY_LISTENER_SERVICE_CONNECT.equals(action)) {
                if(mMainFragment != null) {
                    Log.d(TAG,"MainActivity...qhbConnectReceiver...listen notification bar connect");
                    mMainFragment.updateNotifyPreference();
                }
            } else if(Config.ACTION_NOTIFY_LISTENER_SERVICE_DISCONNECT.equals(action)) {
                if(mMainFragment != null) {
                    Log.d(TAG,"MainActivity...qhbConnectReceiver...listen notification bar disconnect");
                    mMainFragment.updateNotifyPreference();
                }
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG,"onResume..." + QiangHongBaoService.isRunning());
        Log.d(TAG,"onResume..." + MainFragment.isAccessibilitySettingsOn(this));
        if(QiangHongBaoService.isRunning()) {
            if(mTipsDialog != null) {
                mTipsDialog.dismiss();
            }
        }/* else {
            showOpenAccessibilityServiceDialog();
        }*/

        //prize-add-bugid-32658- switch is wrong -2017.4.18-start
        if(!MainFragment.isAccessibilitySettingsOn(this)){
            showOpenAccessibilityServiceDialog();
        }
        //prize-add-bugid-32658- switch is wrong -2017.4.18-end

        // qq
        updateServiceStatus();
    }

    private void updateServiceStatus() {
        AccessibilityManager accessibilityManager = (AccessibilityManager) getSystemService(Context.ACCESSIBILITY_SERVICE);
        List<AccessibilityServiceInfo> accessibilityServices = accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC);
        for (AccessibilityServiceInfo info : accessibilityServices) {
            if (info.getId().equals(getPackageName() + "/.QiangHongBaoService")) {
                break;
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(qhbConnectReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mTipsDialog = null;
    }

    /** Show the dialog box without opening the secondary service*/
    public void showOpenAccessibilityServiceDialog() {
        if(mTipsDialog != null && mTipsDialog.isShowing()) {
            return;
        }
        View view = getLayoutInflater().inflate(R.layout.dialog_tips_layout, null);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAccessibilityServiceSettings();
            }
        });
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.open_service_title);
        builder.setView(view);
        builder.setPositiveButton(R.string.open_service_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                openAccessibilityServiceSettings();
            }
        });
        mTipsDialog = builder.show();
    }

    /** Open the settings for the secondary services*/
    private void openAccessibilityServiceSettings() {
        try {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
            Toast.makeText(this, R.string.tips, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
