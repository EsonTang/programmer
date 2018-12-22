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
import com.prize.permissionmanage.AllAppPermissionsFragment;

/**
 * Created by prize on 2018/1/2.
 */
public class PrizeSetAppPermissionActivity extends Activity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.prize_set_app_permission);
        String packageName = getIntent().getStringExtra(Intent.EXTRA_PACKAGE_NAME);
        Log.d("ppp", "packageName == " + packageName);
        AllAppPermissionsFragment allAppPermissionsFragment = AllAppPermissionsFragment.newInstance(packageName, AllAppPermissionsFragment.TYPE_FROM_NONE, null);
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        //fragmentTransaction.add(R.id.fragment_container,allAppPermissionsFragment);
        fragmentTransaction.replace(R.id.fragment_container,allAppPermissionsFragment);
        fragmentTransaction.commit();
    }
}
