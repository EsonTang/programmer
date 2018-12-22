package com.mediatek.search;

import android.content.Context;
import com.android.settings.R;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.SearchIndexableRaw;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

public class PrizeHBHelper implements Indexable {
	private static final String TAG = "PrizeHBHelper";
    private static final String intentTargetClass = "com.android.lpserver.MainActivity";
	private static final String intentTargetPackage = "com.android.lpserver";
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        @Override
        public List<SearchIndexableRaw> getRawDataToIndex(Context context,
                boolean enabled) {
            List<SearchIndexableRaw> indexables = new ArrayList<SearchIndexableRaw>();
			ActivityInfo[] ActivityInfos = null;
			try {
				ActivityInfos = context.getPackageManager().getPackageInfo(intentTargetPackage,PackageManager.GET_ACTIVITIES).activities;
			} catch (NameNotFoundException e) {
				Log.d(TAG,"Settings search intentTargetClass NameNotFoundException");
			}
			if(ActivityInfos != null && ActivityInfos.length != 0){
				
				for(ActivityInfo mActivityInfo : ActivityInfos){
					 Log.d("TAG","PrizeHBHelper ActivityInfos.name == "+mActivityInfo.name);
					if(intentTargetClass.equals(mActivityInfo.name)){
						SearchIndexableRaw indexable = new SearchIndexableRaw(context);
						indexable.title = context.getString(R.string.red_packet_helper);
						indexable.intentTargetClass = intentTargetClass;
						indexable.intentTargetPackage = intentTargetPackage;
						indexable.intentAction = "android.intent.action.MAIN";
						indexables.add(indexable);
						break;
					}
				}
			}
            return indexables;
        }
    };

}
