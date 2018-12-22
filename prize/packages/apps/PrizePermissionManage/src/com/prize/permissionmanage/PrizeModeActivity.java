package com.prize.permissionmanage;

import android.app.ActionBar;
import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PermissionInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.mediatek.cta.CtaUtils;
import com.prize.permissionmanage.R;
import com.prize.permissionmanage.adapter.PrizeModeAdapter;
import com.prize.permissionmanage.model.AppPermissionGroup;
import com.prize.permissionmanage.model.AppPermissions;
import com.prize.permissionmanage.model.Permission;
import com.prize.permissionmanage.utils.Utils;
import android.content.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.os.Build;
import android.widget.Toast;

/**
 * Created by prize on 2018/1/30.
 */
public class PrizeModeActivity extends Activity{

    private List<String> mList = new ArrayList<>();
	private List<Map<String, Object>> list;
    public static final int TYPE_PERMISSION_ALLOW             = 0;
    public static final int TYPE_PERMISSION_BLOCK             = 1;
    public static final int TYPE_PERMISSION_ASK               = 2;

    private AppPermissions mAppPermissions;
	private PackageManager pm;
	private PackageInfo pkgInfo;
	private AppPermissionGroup permGroup; 
	private PermissionInfo perm;
	private int permissionType = TYPE_PERMISSION_ASK;
	private PrizeModeAdapter adapter;
	private static int preselect = -1;
    private boolean mAppSupportsRuntimePermissions = true;
	private static String TAG = "PrizeModeActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.prize_mode_select);

		
        String packagename = getIntent().getStringExtra("packagename");
        String groupname = getIntent().getStringExtra("groupname");
        String permissioninfoname = getIntent().getStringExtra("permissioninfoname");
		Log.i(TAG,"onCreate packagename = " + packagename + " groupname = " + groupname + " permissioninfoname = " + permissioninfoname);

        PackageManager pm = getPackageManager();
        if (CtaUtils.isCtaSupported()) {
            try {
                pkgInfo = pm.getPackageInfo(packagename,PackageManager.GET_PERMISSIONS);
				perm = pm.getPermissionInfo(permissioninfoname, 0);
            } catch (NameNotFoundException e) {
                finish();
            }
            mAppPermissions = new AppPermissions(this, pkgInfo, null, false,
                    new Runnable() {
                @Override
                public void run() {
                    finish();
                }
            });
		   permGroup= mAppPermissions.getPermissionGroup(groupname);
        }
		if(pkgInfo != null){
			mAppSupportsRuntimePermissions = pkgInfo.applicationInfo
					.targetSdkVersion > Build.VERSION_CODES.LOLLIPOP_MR1;
		}

        ListView listView = (ListView)findViewById(R.id.listview);
        listView.setDivider(getResources().getDrawable(R.drawable.list_divider,null));
        listView.setDividerHeight(1);
		Utils.setMargins(listView,this);
		init();
		list = getData();
        adapter = new PrizeModeAdapter(list,this);
        listView.setAdapter(adapter);
		preselect = -1;
		
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.i(TAG,"onItemClick preselect = " + preselect + " position = " + position);
				if(packagename.equals("com.mediatek.camera")){
					Toast.makeText((Activity)getAndroidContext(), R.string.prize_forbid_modify_system_permission, Toast.LENGTH_SHORT).show();
					return;
				}
				if(preselect != position){
					final String[] filterPermissions = new String[] {perm.name};
					preselect = position;
					switch(position){
						case TYPE_PERMISSION_ASK:
							if(mAppSupportsRuntimePermissions) permGroup.revokeRuntimePermissions(false, filterPermissions);
							break;
						case TYPE_PERMISSION_ALLOW:
							Log.i(TAG,"grantRuntimePermissions permGroup = " + permGroup.getName() + " perm.name = " + perm.name);
							permGroup.grantRuntimePermissions(true, filterPermissions);
							break;
						case TYPE_PERMISSION_BLOCK:
							Log.i(TAG,"revokeRuntimePermissions permGroup = " + permGroup.getName() + " perm.name = " + perm.name);
							permGroup.revokeRuntimePermissions(true, filterPermissions);
							break;
						default:
							break;
					}
					updateUi();
				}
            }
        });

		
        final ActionBar ab = getActionBar();
        if (ab != null) {
            ab.setTitle(pkgInfo.applicationInfo.loadLabel(pm));
            ab.setDisplayHomeAsUpEnabled(true);
        }
    }

    public Context getAndroidContext() {
        return this;
    }

    private void init(){
        mList.add(getResources().getString(R.string.prize_mode_grant));
        mList.add(getResources().getString(R.string.prize_mode_block));
		if(mAppSupportsRuntimePermissions) mList.add(getResources().getString(R.string.prize_mode_ask));
    }

	public List<Map<String, Object>> getData(){ 
		List<Map<String, Object>> list=new ArrayList<Map<String,Object>>(); 
		Permission permission = permGroup.getPermission(perm.name);
		if(mAppSupportsRuntimePermissions){
			if(!permission.isUserFixed()){
				permissionType = TYPE_PERMISSION_ASK;
			}else{
				if(permission.isGranted()){
					permissionType = TYPE_PERMISSION_ALLOW;
				}else{
					permissionType = TYPE_PERMISSION_BLOCK;
				}
			}
		}else{
		    if (permission.hasAppOp()) {
                if (permission.isAppOpAllowed()){
					permissionType = TYPE_PERMISSION_ALLOW;
                }else{
					permissionType = TYPE_PERMISSION_BLOCK;
                }
		    }
		}
		for (int i = 0; i < mList.size(); i++) { 
		  Map<String, Object> map=new HashMap<String, Object>(); 
		  map.put("title", mList.get(i)); 
		  if(permissionType == i){
			  map.put("isShowImage", true); 
		  }else{
			  map.put("isShowImage", false); 
		  }
		  list.add(map); 
		} 
		return list; 
	  } 
    @Override
    public void onResume() {
        super.onResume();
        /// M: Refresh permission status @{
        if (CtaUtils.isCtaSupported()) {
            mAppPermissions.refresh();
        }
        ///@}
        updateUi();
    }

	public void updateUi(){
		Permission permission = permGroup.getPermission(perm.name);
		if(mAppSupportsRuntimePermissions){
			if(!permission.isUserFixed()){
				permissionType = TYPE_PERMISSION_ASK;
			}else{
				if(permission.isGranted()){
					permissionType = TYPE_PERMISSION_ALLOW;
				}else{
					permissionType = TYPE_PERMISSION_BLOCK;
				}
			}
		}else{
			if (permission.hasAppOp()) {
				if (permission.isAppOpAllowed()){
					permissionType = TYPE_PERMISSION_ALLOW;
				}else{
					permissionType = TYPE_PERMISSION_BLOCK;
				}
			}
		}
		Log.i(TAG,"updateUi permission = " + permission.getName() + " permissionType = " + permissionType);
		preselect = permissionType;
		adapter.setShowImage(permissionType);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.d("mengge","cao");
		switch (item.getItemId()) {
			case android.R.id.home: {
				finish();
				return true;
			}
		}
		return super.onOptionsItemSelected(item);
	}
}
