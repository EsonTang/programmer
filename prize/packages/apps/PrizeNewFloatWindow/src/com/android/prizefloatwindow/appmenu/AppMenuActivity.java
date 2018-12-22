package com.android.prizefloatwindow.appmenu;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.android.prizefloatwindow.FunctionlistActivity;
import com.android.prizefloatwindow.LauncherActivity;
import com.android.prizefloatwindow.R;
import com.android.prizefloatwindow.config.Config;
import com.android.prizefloatwindow.utils.ActionUtils;
import com.android.prizefloatwindow.utils.SPHelperUtils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class AppMenuActivity extends Activity {

	private ListView lv_app_list;
    private AppAdapter mAppAdapter;
    private String modeaction;
    private ImageView backView;
    private TextView title_text;
    private String oldaction;
    private List<String> mMenuActionList = new ArrayList<String>();
    private MyHandler mHandler = new MyHandler(this);
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Window window = getWindow();
		window.requestFeature(Window.FEATURE_NO_TITLE);
		window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
		window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN| View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
		window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
		window.setStatusBarColor(getResources().getColor(R.color.status_color));
		setContentView(R.layout.activity_appmenu);
		initStatusBar();
		updateMenuList();
		modeaction = getIntent().getStringExtra("modeaction");
		oldaction = getIntent().getStringExtra("action");
		lv_app_list = (ListView) findViewById(R.id.lv_app_list);
		lv_app_list.setOnItemClickListener(new OnItemClickListener() {  
            @Override  
            public void onItemClick(AdapterView<?> parent, View view,int position, long id) {  
            	if(mAppAdapter != null){
            		MyAppInfo item=(MyAppInfo) mAppAdapter.getItem(position); 
            		Log.d("snail_", "------onItemClick-----position=="+position+"  name=="+item.getAppName()+"  pkg=="+item.getPkgName()+"  modeaction=="+modeaction);
            		if(!item.isSelect()){
            			SPHelperUtils.save(modeaction,item.getPkgName());
            			Intent itIntent = new Intent(AppMenuActivity.this, LauncherActivity.class);
            			itIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            			startActivity(itIntent);
            			finish();
            		}
            	}
            }  
        }); 
		title_text = (TextView)findViewById(R.id.title_text);
		 title_text.setText(R.string.application);
		 backView = (ImageView) findViewById(R.id.back_btn);
		 backView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		mAppAdapter = new AppAdapter();
		lv_app_list.setAdapter(mAppAdapter); 
		initAppList();
	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}
	private void updateMenuList() {
		// TODO Auto-generated method stub
		mMenuActionList.clear();
		mMenuActionList.add(SPHelperUtils.getString(Config.FLOAT_MENU1, Config.default_menu1_action));
		mMenuActionList.add(SPHelperUtils.getString(Config.FLOAT_MENU2, Config.default_menu2_action));
		mMenuActionList.add(SPHelperUtils.getString(Config.FLOAT_MENU3, Config.default_menu3_action));
		mMenuActionList.add(SPHelperUtils.getString(Config.FLOAT_MENU4, Config.default_menu4_action));
		mMenuActionList.add(SPHelperUtils.getString(Config.FLOAT_MENU5, Config.default_menu5_action));
	}
	private void initAppList(){
        new Thread(){
            @Override
            public void run() {
                final List<MyAppInfo> appInfos = ActionUtils.scanLocalInstallAppList(AppMenuActivity.this,AppMenuActivity.this.getPackageManager(),oldaction,mMenuActionList);
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mAppAdapter.setData(appInfos);
                    }
                });
            }
        }.start();
    }

	private static class MyHandler extends Handler {
		private WeakReference<Context> reference;
		public MyHandler(Context context) {
			reference = new WeakReference<>(context);
		}
		@Override
		public void handleMessage(Message msg) {
			AppMenuActivity activity = (AppMenuActivity) reference.get();
			if (activity != null) {

			}
		}
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

    class AppAdapter extends BaseAdapter {

        List<MyAppInfo> myAppInfos = new ArrayList<MyAppInfo>();

        public void setData(List<MyAppInfo> myAppInfos) {
        	this.myAppInfos.clear();
        	this.myAppInfos.addAll(myAppInfos);
            notifyDataSetChanged();
        }

        public List<MyAppInfo> getData() {
            return myAppInfos;
        }

        @Override
        public int getCount() {
            if (myAppInfos != null && myAppInfos.size() > 0) {
                return myAppInfos.size();
            }
            return 0;
        }

        @Override
        public Object getItem(int position) {
            if (myAppInfos != null && myAppInfos.size() > 0) {
                return myAppInfos.get(position);
            }
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder mViewHolder;
            MyAppInfo myAppInfo = myAppInfos.get(position); 
            if (convertView == null) {
                mViewHolder = new ViewHolder(); 
                convertView = LayoutInflater.from(getBaseContext()).inflate(R.layout.item_app_info, null);
                mViewHolder.iv_app_icon = (ImageView) convertView.findViewById(R.id.iv_app_icon);
                mViewHolder.tx_app_name = (TextView) convertView.findViewById(R.id.tv_app_name);
                mViewHolder.iv_app_ck = (ImageView) convertView.findViewById(R.id.iv_app_ck);
                convertView.setTag(mViewHolder);
            } else {
                mViewHolder = (ViewHolder) convertView.getTag();
            }
            if(myAppInfo.isSelect()){
            	mViewHolder.tx_app_name.setTextColor(getResources().getColor(R.color.gray));
            }else {
            	mViewHolder.tx_app_name.setTextColor(getResources().getColor(R.color.black));
			}
            if(myAppInfo.isCurAction()){
            	mViewHolder.iv_app_ck.setVisibility(View.VISIBLE);
            }else {
            	mViewHolder.iv_app_ck.setVisibility(View.GONE);
			}
            mViewHolder.tx_app_name.setText(myAppInfo.getAppName());
            mViewHolder.iv_app_icon.setImageDrawable(myAppInfo.getImage());
            return convertView;
        }

        class ViewHolder {

            ImageView iv_app_icon;
            TextView tx_app_name;
            ImageView iv_app_ck;
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
    }
}
