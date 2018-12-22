package com.prize.permissionmanage;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.content.Intent;

import com.prize.permissionmanage.R;
import com.prize.permissionmanage.AllPermissionAppsFragment;

/**
 * Created by prize on 2018/1/2.
 */
public class PrizePermissionAppActivity extends Activity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.prize_set_app_permission);
        String permissioninfoname = getIntent().getStringExtra(Intent.EXTRA_PERMISSION_NAME);
        String groupname = getIntent().getStringExtra("groupname");
        AllPermissionAppsFragment allPermissionAppsFragment = AllPermissionAppsFragment.newInstance(groupname, AllPermissionAppsFragment.TYPE_FROM_NONE, permissioninfoname);
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        //fragmentTransaction.add(R.id.fragment_container,allAppPermissionsFragment);
        fragmentTransaction.replace(R.id.fragment_container,allPermissionAppsFragment);
        fragmentTransaction.commit();
    }
}
