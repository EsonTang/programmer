package com.android.appnetcontrol;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.INetworkManagementService;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.WhiteListManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*-prize add by lihuangyuan,for whitelist -2017-05-06-start-*/
/*-prize add by lihuangyuan,for whitelist -2017-05-06-end-*/
public class AppNetControlActivity extends Activity implements OnClickListener{  
	
	public static final String WIFI_PREFS_NAME = "WifiPrefsFile";
	public static final String G_PREFS_NAME = "GPrefsFile";
    private ListView listView;  
      
    private Map<Integer, Boolean> g_isSelected;
	private Map<Integer, Boolean> wifi_isSelected;	
      
    private List beSelectedData = new ArrayList();    
      
    ListAdapter adapter;  
    
	CheckBox wifi_select_all;
    CheckBox g_select_all;
	//ImageView back_img;
    private  int mNetType;
	private boolean mWifiAllChecked = true;//recoder the wifi of app is all select or not 
	private boolean mGAllChecked = true;
	
    private List<AppInfo> mListAppInfo = null;
	 private ActionBar mActionBar;
	INetworkManagementService mNetMgr;
    PackageManager pm;
	private ExecutorService mExecutorService;

	@Override
    public void onCreate(Bundle savedInstanceState) {  
        super.onCreate(savedInstanceState);
	//	initStatusBar();		
        setContentView(R.layout.activity_app_net_control);
		mExecutorService = Executors.newFixedThreadPool(5);
		String title = getResources().getString(R.string.layout_title);
		setTitle(title);
		mNetMgr = INetworkManagementService.Stub.asInterface(ServiceManager.getService(Context.NETWORKMANAGEMENT_SERVICE));
		
        listView = (ListView) this.findViewById(R.id.listviewApp);  
        mListAppInfo = new ArrayList<AppInfo>();
		pm = this.getPackageManager();
		
        queryAppInfo();  
        initList();  
		 mActionBar = getActionBar();
        if (mActionBar != null) {
            mActionBar.setDisplayHomeAsUpEnabled(true);
            mActionBar.setHomeButtonEnabled(true);
			mActionBar.setHomeAsUpIndicator(R.drawable.ic_ab_back_material_prize);
        }
    }  
      
    void initList(){  
          
        if (mListAppInfo == null || mListAppInfo.size() == 0)  
            return;  
        //if (g_isSelected != null)  
        //    g_isSelected = null;  
        g_isSelected = new HashMap<Integer, Boolean>();

		//if (wifi_isSelected != null)  
        //    wifi_isSelected = null;  
        wifi_isSelected = new HashMap<Integer, Boolean>();
		
        for (int i = 0; i < mListAppInfo.size(); i++) {  
            g_isSelected.put(i, true);
			wifi_isSelected.put(i, true);
        }  
		
        adapter = new ListAdapter(this, mListAppInfo);  
        listView.setAdapter(adapter);  
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		listView.setNestedScrollingEnabled(false); 
		listView.setFocusable(false);
        adapter.notifyDataSetChanged();  
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {  
            public void onItemClick(AdapterView<?> parent, View view,  
                    int position, long id) {  
               Log.i("map", mListAppInfo.get(position).toString());  
            }  
        });  
        
		wifi_select_all = (CheckBox)findViewById(R.id.wifi_all_select);
        g_select_all = (CheckBox)findViewById(R.id.g_all_select);
		//back_img = (ImageView)findViewById(R.id.back_img);
		wifi_select_all.setOnClickListener(this);
		g_select_all.setOnClickListener(this);
	//	back_img.setOnClickListener(this);
		
		readData();
    }  
    
	public void onClick(View v) {
    	switch(v.getId()) {
    	case R.id.wifi_all_select:
			mNetType = 1;
    		if (!wifi_select_all.isChecked()) {
				mWifiAllChecked = false;
				for (int i = 0; i < mListAppInfo.size(); i++) {
					setAppFirewall(i, mNetType, true);
				}
				Toast.makeText(AppNetControlActivity.this, "所有应用禁止使用WiFi", Toast.LENGTH_SHORT).show();
			} else {
				mWifiAllChecked = true;
				for (int i = 0; i < mListAppInfo.size(); i++) {
					if (!wifi_isSelected.get(i)) {
						setAppFirewall(i, mNetType, false);
					}
				}
			}
    		break;
    	case R.id.g_all_select:
			mNetType = 0;
    		if (!g_select_all.isChecked()) {
				mGAllChecked = false;
				for (int i = 0; i < mListAppInfo.size(); i++) {
					setAppFirewall(i, mNetType, true);
				}
				Toast.makeText(AppNetControlActivity.this, "所有应用禁止使用移动数据", Toast.LENGTH_SHORT).show();
			} else {
				mGAllChecked = true;
				for (int i = 0; i < mListAppInfo.size(); i++) {
					if (!g_isSelected.get(i)) {
						setAppFirewall(i, mNetType, false);
					}
				}
			}
    		break;
		// case R.id.back_img:
			// this.finish();
			// break;
    	}
    }

	private void readData() {
		SharedPreferences wSharePref = getSharedPreferences(WIFI_PREFS_NAME, 0);
		SharedPreferences gSharePref = getSharedPreferences(G_PREFS_NAME, 0);

		int uid;
		for (int i = 0; i < mListAppInfo.size(); i++) {
			uid = getUid(i);
			boolean wifi_enable = wSharePref.getBoolean(uid + "", true);
			boolean g_enable = gSharePref.getBoolean(uid + "", true);
			
			if (wifi_enable) {
				wifi_isSelected.put(i, true);
				listView.setItemChecked(i, true);
			} else {
				wifi_isSelected.put(i, false);
				listView.setItemChecked(i, false);
			}
			if (g_enable) {
				g_isSelected.put(i, true);
				listView.setItemChecked(i, true);
			} else {
				g_isSelected.put(i, false);
				listView.setItemChecked(i, false);
			}
		}
		
		boolean wifi_all = wSharePref.getBoolean("wifi_all", true);
		boolean g_all = gSharePref.getBoolean("g_all", true);
		mWifiAllChecked = wifi_all;
		mGAllChecked = g_all;
		if (!wifi_all) {
			wifi_select_all.setChecked(false);
		}
		if (!g_all) {
			g_select_all.setChecked(false);
		}
	}
	
	/**
	*netType: wifi 1, 3g 0
	*/
	private void setAppFirewall(final int position, int netType, boolean enabled) {
		if (netType == 1) {
			wifi_isSelected.put(position, !enabled);
		} else if (netType == 0){
			g_isSelected.put(position, !enabled);
		}
		listView.setItemChecked(position, !enabled);
		adapter.notifyDataSetChanged();
		/* prize-modify-by-lijimeng-for bugid 51680-20180305-start*/
		if(mExecutorService != null){
			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					try {
						Log.d("mengge","position == "+position);
						Log.d("PrizeNetControl", "setAppFirewall position:" + position + ";enabled:" + enabled);
						mNetMgr.setFirewallUidChainRule(getUid(position), netType, enabled);
					} catch (RemoteException e) {
						e.printStackTrace();
					} catch (Exception e) {
						Log.d("PrizeNetControl", "setAppFirewall exception");
					}
				}
			};
			mExecutorService.execute(runnable);
			/* prize-modify-by-lijimeng-for bugid 51680-20180305-end*/
		}

//		try {
//			Log.d("PrizeNetControl", "setAppFirewall position:" + position + ";enabled:" + enabled);
//			mNetMgr.setFirewallUidChainRule(getUid(position), netType, enabled);
//        } catch (RemoteException e) {
//			e.printStackTrace();
//		} catch (Exception e) {
//			Log.d("PrizeNetControl", "setAppFirewall exception");
//		}
	}
    /*-prize add by lihuangyuan,for whitelist -2017-05-06-start-*/
    public static boolean isPkgInArray(String pkgname,String []arylist)
    {
       if(arylist == null)return false;
    	for(int i=0;i<arylist.length;i++)
    	{
    		if(pkgname.equals(arylist[i]))
    		{
    			return true;
    		}
    	}
	return false;
    }
    /*-prize add by lihuangyuan,for whitelist -2017-05-06-end-*/
    public void queryAppInfo() {
		List<ApplicationInfo> listAppcations = pm  
                .getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);
		Collections.sort(listAppcations, new ApplicationInfo.DisplayNameComparator(pm));
    	if (mListAppInfo != null) {
    		mListAppInfo.clear();
		/*-prize add by lihuangyuan,for whitelist -2017-05-06-start-*/
		WhiteListManager whiteListMgr = (WhiteListManager)getSystemService(Context.WHITELIST_SERVICE);
		 String [] forbadeList = whiteListMgr.getNetForbadeList();
		 /*-prize add by lihuangyuan,for whitelist -2017-05-06-end-*/
    		for(ApplicationInfo app : listAppcations) {
				if ((app.flags & ApplicationInfo.FLAG_SYSTEM) <= 0 
						|| (app.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
					String pkgName = app.packageName;
					String appLabel = (String)app.loadLabel(pm);
					Drawable icon = app.loadIcon(pm);
					AppInfo appInfo = new AppInfo();
					appInfo.setAppLabel(appLabel);
					appInfo.setPkgName(pkgName);
					appInfo.setAppIcon(icon);
					
					 if(isPkgInArray(pkgName,forbadeList))
					{
						continue;
					}
					/*--prize-add by lihuangyuan,for skip  com.gangyun.beautysnap --2017-03-22-end-*/
					mListAppInfo.add(appInfo);
				}
    		}
    	}
    }
	
	private int getUid(int position) {
		int Uid = 0;
		try {
			Uid = pm.getApplicationInfo(mListAppInfo.get(position).getPkgName(), PackageManager.GET_ACTIVITIES).uid;
		} catch (NameNotFoundException e) {
            e.printStackTrace();
        }
		return Uid;
	}
	/*-prize-add by lihuangyuan,for refresh on resume-2017-04-18-start-*/
    @Override
	protected void onResume() {
		super.onResume();
		readData();
	}
	/*-prize-add by lihuangyuan,for refresh on resume-2017-04-18-end-*/
	protected void onPause() {
		super.onPause();

		int uid;
		SharedPreferences wSharePref = getSharedPreferences(WIFI_PREFS_NAME, 0);
		SharedPreferences gSharePref = getSharedPreferences(G_PREFS_NAME, 0);

		SharedPreferences.Editor wEditor = wSharePref.edit();
		SharedPreferences.Editor gEditor = gSharePref.edit();

		for (int i = 0; i < mListAppInfo.size(); i++) {
    		boolean isWifiChecked = wifi_isSelected.get(i);
    		boolean isGsmChecked = g_isSelected.get(i);
			uid = getUid(i);
			wEditor.putBoolean(uid + "", isWifiChecked);
			gEditor.putBoolean(uid + "", isGsmChecked);
    	}
		wEditor.putBoolean("wifi_all", mWifiAllChecked);
		gEditor.putBoolean("g_all", mGAllChecked);
		wEditor.commit();
		gEditor.commit();
	}
	
    class ListAdapter extends BaseAdapter {  
  
        private Context context;  
  
        private List cs;  
  
        private LayoutInflater inflater; 		
  
        public ListAdapter(Context context, List data) {  
            this.context = context;  
            this.cs = data;  
            initLayoutInflater();  
        }  
  
        void initLayoutInflater() {  
            inflater = LayoutInflater.from(context);  
        }  
  
        public int getCount() {  
            return cs.size();  
        }  
  
        public Object getItem(int position) {  
            return cs.get(position);  
        }  
  
        public long getItemId(int position) {  
            return 0;  
        }  
  
        public View getView(int position1, View convertView, ViewGroup parent) {  
            ViewHolder holder = null;  
            View view = null;  
            final int position = position1;  
            if (convertView == null) {  
                convertView = inflater.inflate(R.layout.app_item, null);
                holder = new ViewHolder();
                holder.appIcon = (ImageView)convertView.findViewById(R.id.imgApp);
                holder.tvAppLabel = (TextView)convertView.findViewById(R.id.tvAppLabel);
                //holder.tvPkgName = (TextView)convertView.findViewById(R.id.tvPkgName);
                holder.itemCheck_wifi = (CheckBox)convertView.findViewById(R.id.itemcheck_wifi);
                holder.itemCheck_g = (CheckBox)convertView.findViewById(R.id.itemcheck_3g); 
                convertView.setTag(holder);  
            } else {  
                view = convertView;  
                holder = (ViewHolder) view.getTag();  
            }
            
            AppInfo appInfo = (AppInfo)getItem(position);
    		holder.appIcon.setImageDrawable(appInfo.getAppIcon());
    		holder.tvAppLabel.setText(appInfo.getAppLabel());
    		//holder.tvPkgName.setText(appInfo.getPkgName());
			
			holder.itemCheck_g.setOnClickListener(new OnClickListener() {  
                public void onClick(View v) {
					boolean cu = !g_isSelected.get(position);
					boolean gRecod = false;
					mNetType = 0;
					if (!cu) {
						Toast.makeText(AppNetControlActivity.this, mListAppInfo.get(position).getAppLabel() + "禁止使用移动数据", Toast.LENGTH_SHORT).show();
					}
					
					setAppFirewall(position, mNetType, !cu);
					
					if (!mGAllChecked) {
						for (int i = 0; i < mListAppInfo.size(); i++) {
							if (!g_isSelected.get(i)) {
								gRecod = true;
							}
						}
						if (!gRecod) {
							g_select_all.setChecked(true);
							mGAllChecked = true;
						}
					}
					
					if (mGAllChecked && !cu) {
						g_select_all.setChecked(false);
						mGAllChecked = false;
					}
				}
			});
            
			holder.itemCheck_wifi.setOnClickListener(new OnClickListener() {  
                public void onClick(View v) {
					boolean cu = !wifi_isSelected.get(position);
					boolean wRecod = false;
					mNetType = 1;
					if (!cu) {
						Toast.makeText(AppNetControlActivity.this, mListAppInfo.get(position).getAppLabel() + "禁止使用WiFi", Toast.LENGTH_SHORT).show();
					}
					
					setAppFirewall(position, mNetType, !cu);
					
					if (!mWifiAllChecked) {
						for (int i = 0; i < mListAppInfo.size(); i++) {
							if (!wifi_isSelected.get(i)) {
								wRecod = true;
							}
						}
						if (!wRecod) {
							wifi_select_all.setChecked(true);
							mWifiAllChecked = true;
						}
					}
					
					if (mWifiAllChecked && !cu) {
						wifi_select_all.setChecked(false);
						mWifiAllChecked = false;
					}
				}
			});

            holder.itemCheck_wifi.setChecked(wifi_isSelected.get(position));
            holder.itemCheck_g.setChecked(g_isSelected.get(position));
            return convertView;  
        }  
    }  
      
    class ViewHolder {  
    	ImageView appIcon;
		TextView tvAppLabel;
		//TextView tvPkgName;
		CheckBox itemCheck_wifi;
		CheckBox itemCheck_g;
  
    }

	private void initStatusBar() {
		Window window = getWindow();
		window.requestFeature(Window.FEATURE_NO_TITLE);
		if(VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
			window = getWindow();
			window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS 
					| WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
			window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
					//| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
					| View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			window.setStatusBarColor(Color.TRANSPARENT);
			//window.setNavigationBarColor(Color.TRANSPARENT);
		}
	}
	
	  @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
			onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
	
	@Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}  
