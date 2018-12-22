package com.android.prizefloatwindow;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.android.prizefloatwindow.appmenu.AppMenuActivity;
import com.android.prizefloatwindow.config.Config;
import com.android.prizefloatwindow.utils.SPHelperUtils;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class FunctionlistActivity extends Activity implements OnClickListener{
 
	
	
	private ImageView backView;
	private TextView titleView;
	private TextView[] mTextviews;
	private String[] mStrs;
	private List<String> mMenuActionList = new ArrayList<String>();
	private List<Integer> mMenuIntList = new ArrayList<Integer>();
	private List<String> mGestureActionList = new ArrayList<String>();
	private List<Integer> mGestureIntList = new ArrayList<Integer>();
	private RelativeLayout[] mLays;
	private ImageView[] mImgs;
	private LinearLayout wxalipay_layout,scan_layout,paycode_layout;
	private String action1Str,action2Str,action3Str,action4Str,action5Str;
	private String singleStr,doubleStr,longStr;
	private String actionStr,modeaction;
	boolean isMenuMode;
	private View nothingView;
	private TextView oldselectTV;
	private ImageView oldIV;
	private TextView main_scan_tv,wx_scan_tv,alipay_scan_tv,main_paycode_tv,wx_paycode_tv,alipay_paycode_tv;
	private TextView nothing_tv,lockscreen_tv,back_tv,home_tv,recent_tv,control_center_tv;
	private TextView screenshot_tv,xiaoku_tv,blueeye_tv,gamemode_tv,clean_tv,floatset_tv;
	
	private RelativeLayout nothing_l,lockscreen_l,back_l,home_l,recent_l,control_center_l;
	private RelativeLayout screenshot_l,xiaoku_l,blueeye_l,gamemode_l,clean_l,floatset_l,application_l;
	
	private ImageView nothing_im,lockscreen_im,back_im,home_im,recent_im,control_center_im;
	private ImageView screenshot_im,xiaoku_im,blueeye_im,gamemode_im,clean_im,floatset_im;
	
	
	private WxAlipayDialog wxAlipayDialog;
	
	private int action1Int,action2Int,action3Int,action4Int,action5Int;
	private int singleInt,doubleInt,longInt;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
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
		setContentView(R.layout.activity_functionlist);
		initStatusBar();
		initView();
		modeaction = getIntent().getStringExtra("modelaction");
		if(modeaction.equals(Config.FLOAT_SINGLE)){
			titleView.setText(getResources().getString(R.string.single_set));
		}else if(modeaction.equals(Config.FLOAT_DOUBLE)){
			titleView.setText(getResources().getString(R.string.double_set));
		}else if (modeaction.equals(Config.FLOAT_LONG)) {
			titleView.setText(getResources().getString(R.string.long_set));
		}else {
			titleView.setText(getResources().getString(R.string.menu_set));
		}
		actionStr =  getIntent().getStringExtra("action");
		initViewState();
	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}
	
	private void initViewState(){
		mTextviews =  new TextView[] {
				nothing_tv,lockscreen_tv,back_tv,home_tv,recent_tv,control_center_tv,
				screenshot_tv,xiaoku_tv,blueeye_tv,gamemode_tv,clean_tv,floatset_tv};
		
		mStrs =  new String[] {
				"nothing","lockscreen","back","home","recent","control",
				"screenshot","xiaoku","huyan","gamemode","clean","float_settings"};
		 
		mLays  =  new RelativeLayout[] {
				nothing_l,lockscreen_l,back_l,home_l,recent_l,control_center_l,
				screenshot_l,xiaoku_l,blueeye_l,gamemode_l,clean_l,floatset_l};
		mImgs  =  new ImageView[] {
				nothing_im,lockscreen_im,back_im,home_im,recent_im,control_center_im,
				screenshot_im,xiaoku_im,blueeye_im,gamemode_im,clean_im,floatset_im};
		updateState();
	}
	
	
    private void updateState(){ 
    	isMenuMode = SPHelperUtils.getBoolean(Config.FLOAT_MODE, Config.default_mode_menu);
		 //List<String> mStrsList = Arrays.asList(mStrs);
		 updateMenuList();
		 if(isMenuMode){
			 wxalipay_layout.setVisibility(View.VISIBLE);
			 application_l.setVisibility(View.VISIBLE);
			 blueeye_l.setVisibility(View.VISIBLE);
			 floatset_l.setVisibility(View.VISIBLE);
			 gamemode_l.setVisibility(View.VISIBLE);
			 nothing_l.setVisibility(View.GONE);
			 nothingView.setVisibility(View.GONE);
			 initMenuOtherAction(mMenuIntList);
			 int currentInt = Arrays.asList(mStrs).indexOf(actionStr);
			 if(currentInt >= 0){
				 oldIV=mImgs[currentInt];
				 oldIV.setVisibility(View.VISIBLE);
				 oldselectTV = mTextviews[currentInt];
				 if(mLays[currentInt] != null){
					 mLays[currentInt].setEnabled(false);
				 }
				 oldselectTV.setTextColor(getResources().getColor(R.color.gray));
			 }
			 
		 }else{
			 blueeye_l.setVisibility(View.GONE);
			 gamemode_l.setVisibility(View.GONE);
			 floatset_l.setVisibility(View.GONE);
			 nothing_l.setVisibility(View.VISIBLE);
			 nothingView.setVisibility(View.VISIBLE);
			 wxalipay_layout.setVisibility(View.GONE);
			 application_l.setVisibility(View.GONE);
			 initGestureOtherAction(mGestureIntList);
			 int currentaction = Arrays.asList(mStrs).indexOf(actionStr);
			 if(currentaction >= 0){
				 oldIV=mImgs[currentaction];
				 oldIV.setVisibility(View.VISIBLE);
			 }
		 }
    }
	private void initMenuOtherAction(List<Integer> mMenuIntList){
		for (int i = 0; i < mMenuIntList.size(); i++) {
			int index = mMenuIntList.get(i);
			if(index >= 0){
				TextView tempselectTV = mTextviews[index];
				tempselectTV.setTextColor(getResources().getColor(R.color.gray));
				if(mLays[index] != null){
					 mLays[index].setEnabled(false);
				}
			}
		}
	}
	private void initGestureOtherAction(List<Integer> mGestureIntList){
		for (int i = 0; i < mGestureIntList.size(); i++) {
			int index = mGestureIntList.get(i);
			if(index >= 0){
				TextView tempselectTV = mTextviews[index];
				tempselectTV.setTextColor(getResources().getColor(R.color.gray));
				if(mLays[index] != null){
					 mLays[index].setEnabled(false);
				}
			}
		}
	}
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.back_btn:
			finish();
			break;
		case R.id.nothing_l:
			updateState(Config.action_nothing,nothing_tv,nothing_im);
			break;
		case R.id.lockscreen_l:
			
			updateState(Config.action_lockcscreen,lockscreen_tv,lockscreen_im);
			break;
		case R.id.back_l:
			
			updateState(Config.action_back,back_tv,back_im);
			
			break;
		case R.id.home_l:
			
			updateState(Config.action_home,home_tv,home_im);
			break;
		case R.id.recent_l:
			
			updateState(Config.action_recent,recent_tv,recent_im);
			break;
		case R.id.control_center_l:
			
			updateState(Config.action_control,control_center_tv,control_center_im);
			break;
		case R.id.screenshot_l:
			
			updateState(Config.action_screenshot,screenshot_tv,screenshot_im);
			break;
		case R.id.xiaoku_l:
			
			updateState(Config.action_xiaoku,xiaoku_tv,xiaoku_im);
			break;
		case R.id.blueeye_l:
			
			updateState(Config.action_huyan,blueeye_tv,blueeye_im);
			break;
		case R.id.gamemode_l:
			
			updateState(Config.action_gamemode,gamemode_tv,gamemode_im);
			break;
		case R.id.clean_l:
			
			updateState(Config.action_clean,clean_tv,clean_im);
			break;
		case R.id.floatset_l:
			updateState(Config.action_float_settings,floatset_tv,floatset_im);
			break;
		case R.id.application_l:
			//updateState(Config.action_application,application_tv,application_im);
			Intent itIntent = new Intent(FunctionlistActivity.this, AppMenuActivity.class);
			Log.d("snail_", "-------application_l------modeaction=="+modeaction+"  actionStr=="+actionStr);
			itIntent.putExtra("modeaction", modeaction);
			itIntent.putExtra("action", actionStr);
			startActivityForResult(itIntent,1);
			break;
		case R.id.scan_layout:
			showWxAlipayDialog(true);
			break;
		case R.id.paycode_layout:
			showWxAlipayDialog(false);
			break;
		default:
			break;
		}
	}
	private void showWxAlipayDialog(final boolean isScan) {
		// TODO Auto-generated method stub
		wxAlipayDialog = new WxAlipayDialog(FunctionlistActivity.this);
		wxAlipayDialog.setCanceledOnTouchOutside(true);
		wxAlipayDialog.setCancelable(true);
		Window window=wxAlipayDialog.getWindow();
		//设置dialog显示和退出动画
		WindowManager.LayoutParams wl=window.getAttributes();//获取布局参数
		wl.x=0;//大于0右边偏移小于0左边偏移
		wl.y=0;//大于0下边偏移小于0上边偏移
		//水平全屏
		wl.width=ViewGroup.LayoutParams.MATCH_PARENT;
		//高度包裹内容
		wl.height=ViewGroup.LayoutParams.WRAP_CONTENT;
		wxAlipayDialog.onWindowAttributesChanged(wl);
		String wxStr ="";
		String alipayStr ="";
		final boolean isWxInstall = isAppInstallen("com.tencent.mm");
		final boolean isAlipayInstall = isAppInstallen("com.eg.android.AlipayGphone");
		if(isScan){
			wxStr = getResources().getString(R.string.wxscan);
			alipayStr = getResources().getString(R.string.alipayscan);
		}else {
			wxStr = getResources().getString(R.string.wechatpaycode);
			alipayStr = getResources().getString(R.string.alipaypaycode);
		}
		if(!isWxInstall){
			wxStr = getResources().getString(R.string.app_uninstall);
		}
		if(!isAlipayInstall){
			alipayStr = getResources().getString(R.string.app_uninstall);
		}
		wxAlipayDialog.setOnWxLayoutOnclickListener(wxStr, new WxAlipayDialog.onWxLayoutOnclickListener() {
            @Override
            public void onWxLayoutClick() {
            	if(!isWxInstall){
            		//Toast.makeText(FunctionlistActivity.this,"please go to download  wechat",Toast.LENGTH_LONG).show();
            	}else {
            		if(isScan){
                    	updateState(Config.action_scan_wx,wx_scan_tv,null);
                    }else {
                    	updateState(Config.action_paycode_wx,wx_paycode_tv,null);
    				}
				}
                wxAlipayDialog.dismiss();
            }
        });
		wxAlipayDialog.setOnAlipayLayoutOnclickListener(alipayStr, new WxAlipayDialog.onAlipayLayoutOnclickListener() {
            @Override
            public void onAlipayLayoutClick() {
            	if(!isAlipayInstall){
            		 //Toast.makeText(FunctionlistActivity.this,"please go to download  alipay",Toast.LENGTH_LONG).show();
            	}else {
            		if(isScan){
                    	updateState(Config.action_scan_alipay,alipay_scan_tv,null);
                    }else {
                    	updateState(Config.action_paycode_alipay,alipay_paycode_tv,null);
    				}
				}
                wxAlipayDialog.dismiss(); 
            }
        });
		wxAlipayDialog.setOnWxImgOnclickListener(isWxInstall,new WxAlipayDialog.onWxImgOnclickListener() {
            @Override
            public void onWxImgClick() {
            	
            	Log.d("snail_", "--------OnWxImgOnclick-------------------");
            	gotoDownloadWxAlipay("prizeappcenter://detail?pkg=com.tencent.mm");
                wxAlipayDialog.dismiss();
            }
        });
		wxAlipayDialog.setOnAlipayImgOnclickListener(isAlipayInstall,new WxAlipayDialog.onAlipayImgOnclickListener() {
            @Override
            public void onAlipayImgClick() {
            	Log.d("snail_", "--------onAlipayImgClick-------------------");
            	gotoDownloadWxAlipay("prizeappcenter://detail?pkg=com.eg.android.AlipayGphone");
                wxAlipayDialog.dismiss();
            }
        });
		wxAlipayDialog.show();
	}
	private void gotoDownloadWxAlipay(String uri){
		if(getMarketVersionCode()>=35){
			Intent it = new Intent();
			 it.setAction(Intent.ACTION_VIEW);
			 it.setData(Uri.parse(uri));
			 it.setPackage("com.prize.appcenter");
			 it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		     try {
		          startActivity(it);
		     } catch (ActivityNotFoundException e) {
		          Toast.makeText(this, getResources().getString(R.string.gotomarket), Toast.LENGTH_SHORT).show();
		          Log.d("snail_", "--------gotoDownloadWxAlipay------e==="+e.getMessage());
		          e.printStackTrace();
		     }
		}else {
			 Toast.makeText(this, getResources().getString(R.string.gotomarket), Toast.LENGTH_SHORT).show();
		}
		 
	}
	public  int getMarketVersionCode() {
		int localVersion = 0;
		try {
			PackageInfo packageInfo = FunctionlistActivity.this.getApplicationContext().getPackageManager().getPackageInfo("com.prize.appcenter", 0);
			localVersion = packageInfo.versionCode;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		Log.d("snail_", "--------getMarketVersionCode------localVersion==="+localVersion);
		return localVersion;
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
	private void updateState(String action,TextView functionTv,ImageView functionIv){
		if(isMenuMode){
			updateMenuList();
			if(mMenuActionList.contains(action) ){
				if(isWxAlipayAction(action)){
					Toast.makeText(FunctionlistActivity.this, getResources().getString(R.string.chosen_other), Toast.LENGTH_SHORT).show();
				}
				return;
			}
		}else {
			if(mGestureActionList.contains(action) ){
				return;
			}
		}
		/*if(oldselectTV != null){
			oldselectTV.setTextColor(getResources().getColor(R.color.black));
		}
		if(oldIV != null){
			oldIV.setVisibility(View.GONE);
		}
		functionTv.setTextColor(getResources().getColor(R.color.gray));
		oldselectTV=functionTv;
		if(functionIv != null){
			functionIv.setVisibility(View.VISIBLE);
			oldIV=functionIv;
		}*/
		SPHelperUtils.save(modeaction,action);
		finish();
	}
	private boolean  isWxAlipayAction(String action) {
		// TODO Auto-generated method stub
        if(!TextUtils.isEmpty(action)){
        	if(action.equals("scan_wx")||action.equals("scan_alipay")||action.equals("paycode_wx")||action.equals("paycode_alipay")){
        		return true;
        	}
        }
        return false;
	}
	public  boolean isAppInstallen(String packageName){  
        PackageManager pm = FunctionlistActivity.this.getPackageManager();  
        boolean installed = false;  
        try {  
             pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);  
            installed = true;  
        } catch (PackageManager.NameNotFoundException e) {  
            e.printStackTrace();  
            installed = false;  
        }  
        return  installed;  
    } 
	private void updateMenuList() {
		// TODO Auto-generated method stub
		 action1Str = SPHelperUtils.getString(Config.FLOAT_MENU1, Config.default_menu1_action);
		 action2Str = SPHelperUtils.getString(Config.FLOAT_MENU2, Config.default_menu2_action);
		 action3Str = SPHelperUtils.getString(Config.FLOAT_MENU3, Config.default_menu3_action);
		 action4Str = SPHelperUtils.getString(Config.FLOAT_MENU4, Config.default_menu4_action);
		 action5Str = SPHelperUtils.getString(Config.FLOAT_MENU5, Config.default_menu5_action);
		 
		 action1Int = Arrays.asList(mStrs).indexOf(action1Str);
		 action2Int = Arrays.asList(mStrs).indexOf(action2Str);
		 action3Int = Arrays.asList(mStrs).indexOf(action3Str);
		 action4Int = Arrays.asList(mStrs).indexOf(action4Str);
		 action5Int = Arrays.asList(mStrs).indexOf(action5Str); 
		 
		 singleStr = SPHelperUtils.getString(Config.FLOAT_SINGLE, Config.default_single_action);
		 doubleStr = SPHelperUtils.getString(Config.FLOAT_DOUBLE, Config.default_double_action);
		 longStr = SPHelperUtils.getString(Config.FLOAT_LONG, Config.default_long_action);
		 singleInt = Arrays.asList(mStrs).indexOf(singleStr);
		 doubleInt = Arrays.asList(mStrs).indexOf(doubleStr);
		 longInt = Arrays.asList(mStrs).indexOf(longStr);
		 
		 if(isMenuMode){
			 mMenuActionList.clear();
			 mMenuActionList.add(action1Str);
			 mMenuActionList.add(action2Str);
			 mMenuActionList.add(action3Str);
			 mMenuActionList.add(action4Str);
			 mMenuActionList.add(action5Str);
			 mMenuIntList.clear();
			 mMenuIntList.add(action1Int);
			 mMenuIntList.add(action2Int);
			 mMenuIntList.add(action3Int);
			 mMenuIntList.add(action4Int);
			 mMenuIntList.add(action5Int);
		 }else {
			 mGestureActionList.clear();
			 mGestureActionList.add(singleStr);
			 mGestureActionList.add(doubleStr);
			 mGestureActionList.add(longStr);
			 mGestureIntList.clear();
			 mGestureIntList.add(singleInt);
			 mGestureIntList.add(doubleInt);
			 mGestureIntList.add(longInt);
		}
	}
	@Override  
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {   
        // TODO Auto-generated method stub  
        super.onActivityResult(requestCode, resultCode, data);  
        actionStr = SPHelperUtils.getString(modeaction, Config.default_menu1_action);
        Log.d("snail_", "----------onActivityResult-----modeaction=="+modeaction+"  actionStr=="+actionStr);
        if (requestCode == 1 && resultCode == 2) {
        	if(modeaction.equals(Config.FLOAT_SINGLE)){
    			titleView.setText(getResources().getString(R.string.single_set));
    		}else if(modeaction.equals(Config.FLOAT_DOUBLE)){
    			titleView.setText(getResources().getString(R.string.double_set));
    		}else if (modeaction.equals(Config.FLOAT_LONG)) {
    			titleView.setText(getResources().getString(R.string.long_set));
    		}else {
    			titleView.setText(getResources().getString(R.string.menu_set));
    		}
    		initViewState();
        }  
    } 
	private void initView() {
		// TODO Auto-generated method stub
		backView = (ImageView) findViewById(R.id.back_btn); 
		backView.setOnClickListener(this);
		titleView = (TextView) findViewById(R.id.title_text);
		
		wxalipay_layout = (LinearLayout)findViewById(R.id.wxalipay_layout);
		scan_layout = (LinearLayout)findViewById(R.id.scan_layout);
		scan_layout.setOnClickListener(this);
		paycode_layout = (LinearLayout)findViewById(R.id.paycode_layout);
		paycode_layout.setOnClickListener(this);
		main_scan_tv = (TextView) findViewById(R.id.main_scan_tv);
		main_scan_tv.setOnClickListener(this);
		
		main_paycode_tv = (TextView) findViewById(R.id.main_paycode_tv);
		main_paycode_tv.setOnClickListener(this);
		
		nothingView = (View)findViewById(R.id.nothing_divide);
		nothing_l = (RelativeLayout)findViewById(R.id.nothing_l);
		nothing_im = (ImageView)findViewById(R.id.nothing_im);
		nothing_tv = (TextView) findViewById(R.id.nothing_tv);
		nothing_l.setOnClickListener(this);
		
		lockscreen_l = (RelativeLayout)findViewById(R.id.lockscreen_l);
		lockscreen_im = (ImageView)findViewById(R.id.lockscreen_im);
		lockscreen_tv = (TextView) findViewById(R.id.lockscreen_tv);
		lockscreen_l.setOnClickListener(this);
		
		back_l = (RelativeLayout)findViewById(R.id.back_l);
		back_im = (ImageView)findViewById(R.id.back_im);
		back_tv = (TextView) findViewById(R.id.back_tv);
		back_l.setOnClickListener(this);
		
		home_l = (RelativeLayout)findViewById(R.id.home_l);
		home_im = (ImageView)findViewById(R.id.home_im);
		home_tv = (TextView) findViewById(R.id.home_tv);
		home_l.setOnClickListener(this);
		
		recent_l = (RelativeLayout)findViewById(R.id.recent_l);
		recent_im = (ImageView)findViewById(R.id.recent_im);
		recent_tv = (TextView) findViewById(R.id.recent_tv);
		recent_l.setOnClickListener(this);
		
		control_center_l = (RelativeLayout)findViewById(R.id.control_center_l);
		control_center_im = (ImageView)findViewById(R.id.control_center_im);
		control_center_tv = (TextView) findViewById(R.id.control_center_tv);
		control_center_l.setOnClickListener(this);
		
		
		screenshot_l = (RelativeLayout)findViewById(R.id.screenshot_l);
		screenshot_im = (ImageView)findViewById(R.id.screenshot_im);
		screenshot_tv = (TextView) findViewById(R.id.screenshot_tv);
		screenshot_l.setOnClickListener(this);
		
		xiaoku_l = (RelativeLayout)findViewById(R.id.xiaoku_l);
		xiaoku_im = (ImageView)findViewById(R.id.xiaoku_im);
		xiaoku_tv = (TextView) findViewById(R.id.xiaoku_tv);
		xiaoku_l.setOnClickListener(this);
		
		blueeye_l = (RelativeLayout)findViewById(R.id.blueeye_l);
		blueeye_im = (ImageView)findViewById(R.id.blueeye_im);
		blueeye_tv = (TextView) findViewById(R.id.blueeye_tv);
		blueeye_l.setOnClickListener(this);
		
		gamemode_l = (RelativeLayout)findViewById(R.id.gamemode_l);
		gamemode_im = (ImageView)findViewById(R.id.gamemode_im);
		gamemode_tv = (TextView) findViewById(R.id.gamemode_tv);
		gamemode_l.setOnClickListener(this);
		
		clean_l = (RelativeLayout)findViewById(R.id.clean_l);
		clean_im = (ImageView)findViewById(R.id.clean_im);
		clean_tv = (TextView) findViewById(R.id.clean_tv);
		clean_l.setOnClickListener(this);
		
		floatset_l = (RelativeLayout)findViewById(R.id.floatset_l);
		floatset_im = (ImageView)findViewById(R.id.floatset_im);
		floatset_tv = (TextView) findViewById(R.id.floatset_tv);
		floatset_l.setOnClickListener(this);
		
		application_l = (RelativeLayout)findViewById(R.id.application_l);
		application_l.setOnClickListener(this);
	}
}
