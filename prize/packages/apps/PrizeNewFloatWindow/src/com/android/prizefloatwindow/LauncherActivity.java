package com.android.prizefloatwindow;


import android.app.Activity;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.opengl.Visibility;
import android.os.Build;
import java.lang.reflect.Field;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.android.prizefloatwindow.config.Config;
import com.android.prizefloatwindow.utils.ActionUtils;
import com.android.prizefloatwindow.utils.SPHelperUtils;
import com.android.prizefloatwindow.view.CircleMenuLayout;
import com.android.prizefloatwindow.view.CircleMenuLayout.OnMenuItemClickListener;

import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class LauncherActivity extends Activity implements OnClickListener,OnCheckedChangeListener{

	  
	private String TAG = "snail_LauncherActivity";
	//switch 
	private Switch showFloarwSwitch,hideFloatSwitch;
	private LinearLayout showFloarwLayout,hideFloatLayout,modechose_layout,quickset_layout,menuset_layout;
    private ImageView backView;
    private TextView title_text;
    private ExecutorService mExecutorService;
    private WindowManager wm;
    private PackageInfo  packageInfo;
    private AppOpsManager mAppOpsManager;
    private CircleMenuLayout mCircleMenuLayout; 
    //functionsettting
    private LinearLayout quick_mode_layout,menu_mode_layout;
    private CheckBox quick_mode_ck,menu_mode_ck;
    //quick settings
    private LinearLayout single_lay,double_lay,long_lay;//action
	private TextView single_action,double_action,long_action;
    //reset 
	private TextView float_reset;
 
    private String[] mItemTexts;
	private Drawable[] mItemImgs;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Window window = getWindow();
		window.requestFeature(Window.FEATURE_NO_TITLE);
		if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) { 
			window = getWindow();
			window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
			window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN| View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			window.setStatusBarColor(getResources().getColor(R.color.status_color));
		}
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);  
		setContentView(R.layout.activity_launcher);
		mExecutorService = Executors.newFixedThreadPool(2);
		try {
			 packageInfo = this.getPackageManager().getPackageInfo("com.android.prizefloatwindow", 0);
       } catch (NameNotFoundException e) {
           e.printStackTrace();
       }
		wm = (WindowManager) LauncherActivity.this.getSystemService(Context.WINDOW_SERVICE);
		 mAppOpsManager = (AppOpsManager) LauncherActivity.this.getSystemService(Context.APP_OPS_SERVICE);
		initFloatPerMission("com.android.prizefloatwindow");
		mItemTexts = new String[] {getString(R.string.control), getString(R.string.lockscreen), getString(R.string.back),
	    		getString(R.string.screenshot), getString(R.string.xiaoku) };
		mItemImgs = new Drawable[] { getResources().getDrawable(R.drawable.control_center),getResources().getDrawable(R.drawable.lock_screen), getResources().getDrawable(R.drawable.back),
				getResources().getDrawable(R.drawable.screenshot), getResources().getDrawable(R.drawable.xiaoku_robot)
				 };
		mCircleMenuLayout = (CircleMenuLayout) findViewById(R.id.id_menulayout);
		mCircleMenuLayout.setMenuItemIconsAndTexts(mItemImgs, mItemTexts);
		mCircleMenuLayout.setOnMenuItemClickListener(new OnMenuItemClickListener()
		{
			
			@Override
			public void itemClick(View view, int pos){
				String action="";
				if(pos==0){
					 action=SPHelperUtils.getString(Config.FLOAT_MENU1, Config.default_menu1_action);
					 startListActivity(Config.FLOAT_MENU1,action);
				}else if(pos==1) {
					action=SPHelperUtils.getString(Config.FLOAT_MENU2, Config.default_menu2_action);
					startListActivity(Config.FLOAT_MENU2,action);
				}else if(pos==2) {
					action=SPHelperUtils.getString(Config.FLOAT_MENU3, Config.default_menu3_action);
					startListActivity(Config.FLOAT_MENU3,action);
				}else if(pos==3) {
					action=SPHelperUtils.getString(Config.FLOAT_MENU4, Config.default_menu4_action);
					startListActivity(Config.FLOAT_MENU4,action);
				}else if(pos==4) {
					action=SPHelperUtils.getString(Config.FLOAT_MENU5, Config.default_menu5_action);
					startListActivity(Config.FLOAT_MENU5,action);
				}
			}
			@Override
			public void itemCenterClick(View view){
			}
		});
		initStatusBar();
		initwidget(); 
		initSwitchState(); 
	} 
	 
	
	
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		// TODO Auto-generated method stub
		switch (buttonView.getId()) {
		case R.id.float_openswitch_bar:
			Log.d("snail_", "--------onCheckedChanged-----isChecked=="+isChecked+"  isFloatShow=="+ArcTipViewController.getInstance().isFloatShow());
			if(isChecked && !ArcTipViewController.getInstance().isFloatShow()){
				toShowFloatWindow();
			}else if(!isChecked){
				toStopFloatWindow();
			} 
			initLayoutShow();
			break;
		case R.id.float_switch_hide:
			if(isChecked){
				if(ArcTipViewController.getInstance().isFloatShow()){
					SPHelperUtils.save(Config.FLOAT_AUTOHIDE, true);
					Log.d("snail_", "--------float_switch_hide-------isChecked=="+isChecked);
					ArcTipViewController.getInstance().refreshAutoHideState(true);
				}
			}else{
				if(ArcTipViewController.getInstance().isFloatShow()){
					SPHelperUtils.save(Config.FLOAT_AUTOHIDE, false);
					ArcTipViewController.getInstance().refreshAutoHideState(false);
				}
			}
			break;
		case R.id.menu_mode_ck:
			if(isChecked){
				quickset_layout.setVisibility(View.GONE);
				menuset_layout.setVisibility(View.VISIBLE);
				quick_mode_ck.setChecked(false);
				SPHelperUtils.save(Config.FLOAT_MODE,true);
				updateMenuSettingsShow();
				
			}else {
				quickset_layout.setVisibility(View.VISIBLE);
				menuset_layout.setVisibility(View.GONE);
			}
			break;
		case R.id.quick_mode_ck:
			if(isChecked){
				quickset_layout.setVisibility(View.VISIBLE);
				menuset_layout.setVisibility(View.GONE);
				menu_mode_ck.setChecked(false);
				SPHelperUtils.save(Config.FLOAT_MODE,false);
				updateQuickSettingsShow();
			}else {
				quickset_layout.setVisibility(View.GONE);
				menuset_layout.setVisibility(View.VISIBLE);
			}
			break;
		default:
			break;
		}
		
	}

	
	private void updateQuickSettingsShow(){
		String singleStr = SPHelperUtils.getString(Config.FLOAT_SINGLE, Config.default_single_action);
		single_action.setText(ActionUtils.getTranslate(getApplicationContext(), singleStr));
		String doubleStr = SPHelperUtils.getString(Config.FLOAT_DOUBLE, Config.default_double_action);
		double_action.setText(ActionUtils.getTranslate(getApplicationContext(), doubleStr));
		String longStr = SPHelperUtils.getString(Config.FLOAT_LONG, Config.default_long_action);
		long_action.setText(ActionUtils.getTranslate(getApplicationContext(), longStr));
	}
	private void updateMenuSettingsShow(){
		String menu1action = SPHelperUtils.getString(Config.FLOAT_MENU1, Config.default_menu1_action);
		String menu1Str = ActionUtils.getTranslate(getApplicationContext(), menu1action);
		Drawable menu1Img = ActionUtils.getIcon(getApplicationContext(), menu1action);
		
		String menu2action = SPHelperUtils.getString(Config.FLOAT_MENU2, Config.default_menu2_action);
		String menu2Str = ActionUtils.getTranslate(getApplicationContext(), menu2action);
		Drawable menu2Img = ActionUtils.getIcon(getApplicationContext(), menu2action);
		
		String menu3action = SPHelperUtils.getString(Config.FLOAT_MENU3, Config.default_menu3_action);
		String menu3Str = ActionUtils.getTranslate(getApplicationContext(), menu3action);
		Drawable menu3Img = ActionUtils.getIcon(getApplicationContext(), menu3action);
		
		String menu4action = SPHelperUtils.getString(Config.FLOAT_MENU4, Config.default_menu4_action);
		String menu4Str = ActionUtils.getTranslate(getApplicationContext(), menu4action);
		Drawable menu4Img = ActionUtils.getIcon(getApplicationContext(), menu4action);
		
		String menu5action = SPHelperUtils.getString(Config.FLOAT_MENU5, Config.default_menu5_action);
		String menu5Str = ActionUtils.getTranslate(getApplicationContext(), menu5action);
		Drawable menu5Img = ActionUtils.getIcon(getApplicationContext(), menu5action);
		
		mItemTexts = new String[] {menu1Str,menu2Str,menu3Str,menu4Str,menu5Str}; 
		mItemImgs = new Drawable[] {menu1Img,menu2Img,menu3Img,menu4Img,menu5Img}; 
		mCircleMenuLayout.updateMenuItemIconsAndTexts(mItemImgs, mItemTexts);
		
	}
	private void initLayoutShow() {
		// TODO Auto-generated method stub
		int isFloatshow = Settings.System.getInt(getContentResolver(),Settings.System.PRIZE_NEW_FLOAT_WINDOW,0);
		if(isFloatshow == 0){
			modechose_layout.setVisibility(View.GONE);
			quickset_layout.setVisibility(View.GONE);
			menuset_layout.setVisibility(View.GONE);
			hideFloatLayout.setVisibility(View.GONE);
		}else {
			modechose_layout.setVisibility(View.VISIBLE);
			hideFloatLayout.setVisibility(View.VISIBLE);
			boolean isMenuMode = SPHelperUtils.getBoolean(Config.FLOAT_MODE, Config.default_mode_menu);
			if(isMenuMode){
				if(!menu_mode_ck.isChecked()){
					menu_mode_ck.setChecked(true);
				}else {
					quickset_layout.setVisibility(View.GONE);
					menuset_layout.setVisibility(View.VISIBLE);
					quick_mode_ck.setChecked(false);
					SPHelperUtils.save(Config.FLOAT_MODE,true);
					updateMenuSettingsShow();
				}
				quick_mode_ck.setChecked(false);
			}else {
				menu_mode_ck.setChecked(false);
				if(!quick_mode_ck.isChecked()){
					quick_mode_ck.setChecked(true);
				}else {
					quickset_layout.setVisibility(View.VISIBLE);
					menuset_layout.setVisibility(View.GONE);
					menu_mode_ck.setChecked(false);
					SPHelperUtils.save(Config.FLOAT_MODE,false);
					updateQuickSettingsShow();
				}
			}
			updateAutoHideState();
		}
	}



	private void toShowFloatWindow() {
		// TODO Auto-generated method stub
		Settings.System.putInt(getContentResolver(),Settings.System.PRIZE_NEW_FLOAT_WINDOW,1);
		Intent iFWService = new Intent(LauncherActivity.this,FloatWindowService.class);
		LauncherActivity.this.startService(iFWService);
	}
	private void toStopFloatWindow() {
		// TODO Auto-generated method stub
		Settings.System.putInt(getContentResolver(),Settings.System.PRIZE_NEW_FLOAT_WINDOW,0);
		if(ArcTipViewController.getInstance().isFloatShow()){
			ArcTipViewController.getInstance().remove();
		}
		Intent iFWService = new Intent(LauncherActivity.this,FloatWindowService.class);
		LauncherActivity.this.stopService(iFWService);
	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		initLayoutShow(); 
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.back_btn:   
			finish();
			
			break;
		case R.id.float_openswitch_layout:
			showFloarwSwitch.performClick();
			break;
		case R.id.float_layout_hide:
			hideFloatSwitch.performClick();
			break;
		case R.id.quick_mode_layout:
			if(quick_mode_ck.isChecked()){
				return;
			}
			quick_mode_ck.performClick();
			break;
		case R.id.menu_mode_layout:
			if(menu_mode_ck.isChecked()){
				return;
			}
			menu_mode_ck.performClick();
			break;
		case R.id.single_lay:
			String singleStr = SPHelperUtils.getString(Config.FLOAT_SINGLE, Config.default_single_action);
			startListActivity(Config.FLOAT_SINGLE,singleStr);
			break;
		case R.id.double_lay: 
			String doubleStr = SPHelperUtils.getString(Config.FLOAT_DOUBLE, Config.default_double_action);
			startListActivity(Config.FLOAT_DOUBLE,doubleStr);
			break;
		case R.id.long_lay:
			String longStr = SPHelperUtils.getString(Config.FLOAT_LONG, Config.default_long_action);
			startListActivity(Config.FLOAT_LONG,longStr);
			break;
		case R.id.float_reset:
			resetMenuAction();
			break;
		default:
			break;
		}
	}
	private void updateAutoHideState() {
		// TODO Auto-generated method stub
		boolean isAutoHide = SPHelperUtils.getBoolean(Config.FLOAT_AUTOHIDE, Config.default_autohide);
		if(isAutoHide){
			Log.d("snail_", "--------updateAutoHideState-------isChecked=="+hideFloatSwitch.isChecked());
			if(!hideFloatSwitch.isChecked()){
				hideFloatSwitch.setChecked(true);
			}else {
				if(ArcTipViewController.getInstance().isFloatShow()){
					SPHelperUtils.save(Config.FLOAT_AUTOHIDE, true);
					ArcTipViewController.getInstance().refreshAutoHideState(true);
				}
			}
		}else { 
			if(hideFloatSwitch.isChecked()){
				hideFloatSwitch.setChecked(false);
			}else {
				if(ArcTipViewController.getInstance().isFloatShow()){
					SPHelperUtils.save(Config.FLOAT_AUTOHIDE, false);
					ArcTipViewController.getInstance().refreshAutoHideState(false);
				}
			}
		}
	}
	private void initSwitchState() { 
		// TODO Auto-generated method stub
		int isFloatshow = Settings.System.getInt(getContentResolver(),Settings.System.PRIZE_NEW_FLOAT_WINDOW,0);
		if (isFloatshow == 1) {
			if(!showFloarwSwitch.isChecked()){
				showFloarwSwitch.setChecked(true);
			}else {
				if(!ArcTipViewController.getInstance().isFloatShow()){
					toShowFloatWindow();
					initLayoutShow();
				}
			}
			updateAutoHideState();
		} else {
			if(showFloarwSwitch.isChecked()){
				showFloarwSwitch.setChecked(false);
			}else {
				toStopFloatWindow();
				initLayoutShow();
			}
			
		}
	}
	private void initwidget() {  
		// TODO Auto-generated method stub
		 backView = (ImageView) findViewById(R.id.back_btn);
		 backView.setOnClickListener(this);
		 showFloarwSwitch = (Switch) findViewById(R.id.float_openswitch_bar);
		 showFloarwSwitch.setOnCheckedChangeListener(this);
		 hideFloatSwitch = (Switch) findViewById(R.id.float_switch_hide);
		 hideFloatSwitch.setOnCheckedChangeListener(this);
		 showFloarwLayout = (LinearLayout) findViewById(R.id.float_openswitch_layout);
		 showFloarwLayout.setOnClickListener(this);
		 hideFloatLayout = (LinearLayout) findViewById(R.id.float_layout_hide);
		 hideFloatLayout.setOnClickListener(this);
		 title_text = (TextView)findViewById(R.id.title_text);
		 title_text.setText(R.string.title);
		 //mainlayout
		 modechose_layout = (LinearLayout) findViewById(R.id.modechose_layout);
		 quickset_layout = (LinearLayout) findViewById(R.id.quickset_layout);
		 menuset_layout = (LinearLayout) findViewById(R.id.menuset_layout);

		 //modesettings
		 quick_mode_layout = (LinearLayout) findViewById(R.id.quick_mode_layout);
		 quick_mode_layout.setOnClickListener(this);
		 menu_mode_layout = (LinearLayout) findViewById(R.id.menu_mode_layout);
		 menu_mode_layout.setOnClickListener(this);
		 quick_mode_ck = (CheckBox) findViewById(R.id.quick_mode_ck);
		 quick_mode_ck.setOnCheckedChangeListener(this);
		 menu_mode_ck = (CheckBox) findViewById(R.id.menu_mode_ck);
		 menu_mode_ck.setOnCheckedChangeListener(this);
		 //quick settings
		 //single 
		 single_lay = (LinearLayout) findViewById(R.id.single_lay);
		 single_lay.setOnClickListener(this);
		 single_action = (TextView)findViewById(R.id.single_action);
		 //double 
		 double_lay = (LinearLayout) findViewById(R.id.double_lay);
		 double_lay.setOnClickListener(this);
		 double_action = (TextView)findViewById(R.id.double_action);
		 //long
		 long_lay = (LinearLayout) findViewById(R.id.long_lay);
		 long_lay.setOnClickListener(this);
		 long_action = (TextView)findViewById(R.id.long_action);
		 //reset 
		 float_reset = (TextView)findViewById(R.id.float_reset);
		 float_reset.setOnClickListener(this);
		 
	}
	private void startListActivity(String title,String action){
		Intent it = new Intent();
		it.setClass(LauncherActivity.this, FunctionlistActivity.class);
		it.putExtra("modelaction", title);
		it.putExtra("action", action);
		LauncherActivity.this.startActivity(it);   
	}
	private void initStatusBar() {
		Window window = getWindow();
		window.setStatusBarColor(getResources().getColor(R.color.statusbar_inverse));

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
	private void resetMenuAction() {
		SPHelperUtils.save(Config.FLOAT_MENU1, Config.default_menu1_action);
		SPHelperUtils.save(Config.FLOAT_MENU2, Config.default_menu2_action);
		SPHelperUtils.save(Config.FLOAT_MENU3, Config.default_menu3_action);
		SPHelperUtils.save(Config.FLOAT_MENU4, Config.default_menu4_action);
		SPHelperUtils.save(Config.FLOAT_MENU5, Config.default_menu5_action);
		updateMenuSettingsShow();
	}
	private void initFloatPerMission(final String pkgString){
		Log.d("snail_initFloatPerMission", "------canDrawOverlays=="+Settings.canDrawOverlays(LauncherActivity.this));
		//if(!Settings.canDrawOverlays(LauncherActivity.this)){
			if(mExecutorService != null){
				Runnable runnable = new Runnable() {
					@Override
					public void run() {
						if(packageInfo != null){
							Log.d("snail_initFloatPerMission", "------packageInfo.applicationInfo.uid=="+packageInfo.applicationInfo.uid);
							mAppOpsManager.setMode(AppOpsManager.OP_SYSTEM_ALERT_WINDOW,packageInfo.applicationInfo.uid, pkgString,AppOpsManager.MODE_ALLOWED);
							wm.setFloatEnable(pkgString, true);
						}else {
							try {
								Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,Uri.parse("package:"+ LauncherActivity.this.getPackageName()));
								LauncherActivity.this.startActivity(intent);
							} catch (Throwable e) {
							} 
						}
					}
				};
				mExecutorService.execute(runnable);
			}
		//}
        if (!Settings.System.canWrite(LauncherActivity.this)) {
             Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
             intent.setData(Uri.parse("package:" + LauncherActivity.this.getPackageName()));
             intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
             LauncherActivity.this.startActivity(intent);
        } 
	}
}
