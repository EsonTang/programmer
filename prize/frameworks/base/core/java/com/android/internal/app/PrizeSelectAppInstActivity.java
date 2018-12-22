/* app multi instances feature. prize-linkh-20151222 */

package com.android.internal.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.UserHandle;
import android.util.Log;
import android.util.PrizeAppInstanceUtils;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.internal.widget.ResolverDrawerLayout;
import android.os.Build;
import com.prize.internal.R;
import android.os.StrictMode;
// Nav bar color customized feature. prize-linkh-2017.08.17 @{
import android.graphics.Color;
import com.mediatek.common.prizeoption.PrizeOption;
// @}

public class PrizeSelectAppInstActivity extends Activity {
    private static final String TAG = "PrizeSelectAppInstActivity";
    private static final boolean IS_USER_BUILD = "user".equals(Build.TYPE);

    private Intent mTargetIntent;
    private String mTargetPkg;
    private boolean mFromResolverActivity;
    
    private GridView mAppInstGridView;
    private LayoutInflater mLayoutInflater;
    private AppInstAdapter mAppInstAdapter;
    
    private PrizeAppInstanceUtils mPrizeAppInstanceUtils;
    private PackageManager mPackageManager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_inst_main_prize);

        logAppInstancesInfo("onCreate(): intent=" + getIntent());
        
        Intent intent = getIntent();        
        mFromResolverActivity = intent.getBooleanExtra(Intent.EXTRA_FROM_RESOLVER_ACTIVITY, false);        
        logAppInstancesInfo("mFromResolverActivity=" + mFromResolverActivity);
        
        mTargetPkg = intent.getStringExtra(Intent.EXTRA_APP_INST_PKG_NAME);
        logAppInstancesInfo("mTargetPkg=" + mTargetPkg);
        if(mTargetPkg == null) {
            finish();
            return;
        }
        
        Parcelable targetParcelable = intent.getParcelableExtra(Intent.EXTRA_APP_INST_INTENT);
        if (!(targetParcelable instanceof Intent)) {
            logAppInstancesInfo("Target is not an intent: " + targetParcelable);
            finish();
            return;
        }
        
        mTargetIntent = (Intent)targetParcelable;        
        logAppInstancesInfo("mTargetIntent=" + mTargetIntent);
        
        mLayoutInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mPrizeAppInstanceUtils = PrizeAppInstanceUtils.getInstance(this);
        mPackageManager = getPackageManager();

        final ResolverDrawerLayout rdl = (ResolverDrawerLayout)findViewById(com.android.internal.R.id.contentPanel);
        rdl.setOnDismissedListener(new ResolverDrawerLayout.OnDismissedListener() {
            @Override
            public void onDismissed() {
                finish();
            }
        });
        rdl.setMaxCollapsedHeight(getResources().getDimensionPixelSize(R.dimen.max_collapsed_height_for_app_inst));
        rdl.setMaxCollapsedHeightSmall(getResources().getDimensionPixelSize(R.dimen.max_collapsed_height_small_for_app_inst));        
        rdl.setIgnoredHeightAdjustmentOnMeasure(true); // solve bug-50789
        
        final LinearLayout header = (LinearLayout)findViewById(R.id.header);
        ViewGroup.LayoutParams lp = header.getLayoutParams();
        if(lp instanceof ResolverDrawerLayout.LayoutParams) {
            ResolverDrawerLayout.LayoutParams rlp = (ResolverDrawerLayout.LayoutParams)lp;
            rlp.alwaysShow = true;
            header.setLayoutParams(rlp);
        }
        
        final TextView tv = (TextView)findViewById(android.R.id.title);
        tv.setText(com.android.internal.R.string.whichApplication);
        
        mAppInstGridView = (GridView)findViewById(R.id.appInstGridView);
        final int maxInst = mPrizeAppInstanceUtils.getMaxInstancesPerApp();
        mAppInstGridView.setNumColumns(adjustNumColumns(maxInst));
        
        mAppInstAdapter = new AppInstAdapter(mTargetPkg, mTargetIntent, mPrizeAppInstanceUtils);
        mAppInstGridView.setAdapter(mAppInstAdapter);

        // Nav bar color customized feature. prize-linkh-2017.08.17 @{
        if (PrizeOption.PRIZE_NAVBAR_COLOR_CUST) {
            getWindow().setDimAmount(0.25f);
            getWindow().setIgnoreDimAmountAdjustment(true);
            getWindow().setNavigationBarColor(Color.BLACK);
        }
        // @}
    }
    
    private static void logAppInstancesInfo(String msg) {
        if(!PrizeAppInstanceUtils.ALLOW_DBG_INFO || IS_USER_BUILD) {
            return;
        }
        
        Log.d(TAG, "AppInst**" + msg);        
    }
    
    private int adjustNumColumns(int total) {
        int num;
        if(total > 1) {
            num = 3;
        } else if(total == 1) {
            num = 2;
        } else {
            num = 1;
        }
        
        return num;
    }
    
    private final class AppInstAdapter extends BaseAdapter implements View.OnClickListener {
        private String mTargetPkg;
        private Intent mTargetIntent;
        private SparseArray<ItemInfo> mSparseArray = new SparseArray<ItemInfo>();
        private int mMaxInstances;
        private PrizeAppInstanceUtils mAppInstanceUtils;
        
        public AppInstAdapter(String targetPkg, Intent targetIntent, 
                PrizeAppInstanceUtils prizeAppInstanceUtils) {
            mAppInstanceUtils = prizeAppInstanceUtils;
            mTargetPkg = targetPkg;
            mTargetIntent = targetIntent;
            mMaxInstances = mAppInstanceUtils.getMaxInstancesPerApp();
            
            rebuildList();
        }
        
        public void setMaxInstancesPerApp(int num) {
            if(mMaxInstances != num) {
                mMaxInstances = num;
                rebuildList();
            }
        }
        
        public void setTargetPkg(String targetPkg) {
            if(mTargetPkg != targetPkg) {
                mTargetPkg = targetPkg;
                rebuildList();              
            }
        }
        
        public void setTargetIntent(Intent targetIntent) {
            mTargetIntent = targetIntent;
        }
        
        public void rebuildList() {
            mSparseArray.clear();
            
            ApplicationInfo ai = null;
            if(mTargetPkg != null) {
                try {
                     ai = mPackageManager.getApplicationInfo(mTargetPkg, 0);
                } catch(NameNotFoundException e) {
                    logAppInstancesInfo(Log.getStackTraceString(e));
                }
                
                if(ai == null) {
                    notifyDataSetChanged();
                    return;
                }
                
                ItemInfo oriItem = new ItemInfo();
                oriItem.drawable = ai.loadIcon(mPackageManager);
                oriItem.title = ai.loadLabel(mPackageManager);
                oriItem.appInst = 0;
                mSparseArray.put(0, oriItem);
                logAppInstancesInfo("add original item: appInst=" + oriItem.appInst + ", title=" + oriItem.title 
                        + ", drawable=" + oriItem.drawable);
                
                for(int idx = 1; idx <= mMaxInstances; ++idx) {
                    ItemInfo item = new ItemInfo();
                    item.appInst = idx;
                    item.title = mAppInstanceUtils.addCloneAppStringForAppInst(idx, oriItem.title.toString());
                    item.drawable = mAppInstanceUtils.getIconDrawableForAppInst(mTargetPkg, idx, oriItem.drawable);
                    logAppInstancesInfo("add item: appInst=" + item.appInst + ", title=" + item.title 
                            + ", drawable=" + item.drawable);
                    mSparseArray.put(idx, item);
                }

                logAppInstancesInfo("Total items: " + mSparseArray.size());
            }
            
            notifyDataSetChanged();
        }
        
        @Override
        public int getCount() {
            return mSparseArray.size();
        }

        @Override
        public Object getItem(int position) {
            return mSparseArray.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = newView(parent);
            }
            
            bindView(convertView, position);

            return convertView;
        }
        
        private View newView(ViewGroup parent) {
            View v = mLayoutInflater.inflate(R.layout.app_inst_item_prize, parent, false);
            ViewHolder h = new ViewHolder();
            h.icon = (ImageView)v.findViewById(R.id.app_inst_icon);
            h.text = (TextView)v.findViewById(R.id.app_inst_title);
            v.setTag(h);
            v.setOnClickListener(this);
            return v;
        }
        
        private void bindView(View view, int position) {
            ItemInfo item = mSparseArray.get(position);
            if(item == null) {
                return;
            }
            
            ViewHolder vh = (ViewHolder)view.getTag();
            vh.appInst = item.appInst;
            vh.icon.setImageDrawable(item.drawable);
            vh.text.setText(item.title);            
        }
        
        private void startTargetActivity(int appInst) {
            logAppInstancesInfo("startTargetActivity(). appInst=" + appInst + ", targetIntent=" + mTargetIntent);
            
            if(mTargetIntent == null) {
                return;
            }
            
            Intent i = new Intent(mTargetIntent);
            i.setAppInstanceIndex(appInst);

            logAppInstancesInfo("startTargetActivity(). Disable death on file uri exposure!");
            StrictMode.disableDeathOnFileUriExposure();
            try {
		   /*prize-modify by lihuangyuan,for fingerapplock-2017-07-04-start*/
                //startActivityAsCaller(i, null, false, UserHandle.USER_NULL);
                startActivity(i);
		   /*prize-modify by lihuangyuan,for fingerapplock-2017-07-04-end*/
            } catch(Exception e) {
                logAppInstancesInfo(Log.getStackTraceString(e));
            } finally {
                logAppInstancesInfo("startTargetActivity(). Enable death on file uri exposure!");
                StrictMode.enableDeathOnFileUriExposure();
            }
        }

        @Override
        public void onClick(View v) {
            ViewHolder vh = (ViewHolder)v.getTag();         
            startTargetActivity(vh.appInst);
            finish();
        }       
    }
    
    private final class ViewHolder {
        ImageView icon;
        TextView text;
        int appInst;
    }
    
    private final class ItemInfo {
        Drawable drawable;
        CharSequence title;
        int appInst;
    }
}
