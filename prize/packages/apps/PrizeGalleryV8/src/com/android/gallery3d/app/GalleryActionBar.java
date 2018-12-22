/*
* Copyright (C) 2014 MediaTek Inc.
* Modification based on code covered by the mentioned copyright
* and/or permission notice(s).
*/
/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.gallery3d.app;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.ActionBar.OnMenuVisibilityListener;
import android.app.ActionBar.OnNavigationListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.view.View.OnLayoutChangeListener;
import android.view.ViewGroup;
import android.widget.ActivityChooserModel;
import android.widget.ActivityChooserView;
import android.widget.BaseAdapter;
import android.widget.ShareActionProvider;
import android.widget.TextView;
import android.widget.TwoLineListItem;

import com.android.gallery3d.R;
import com.android.gallery3d.common.ApiHelper;

import android.view.ViewTreeObserver;

import java.util.ArrayList;

public class GalleryActionBar implements OnNavigationListener {
    @SuppressWarnings("unused")
    private static final String TAG = "Gallery2/GalleryActionBar";

    private ClusterRunner mClusterRunner;
    private CharSequence[] mTitles;
    private ArrayList<Integer> mActions;
    private Context mContext;
    private LayoutInflater mInflater;
    private AbstractGalleryActivity mActivity;
    private ActionBar mActionBar;
    private int mCurrentIndex;
    private ClusterAdapter mAdapter = new ClusterAdapter();

    private AlbumModeAdapter mAlbumModeAdapter;
    private OnAlbumModeSelectedListener mAlbumModeListener;
    private int mLastAlbumModeSelected;
    private CharSequence [] mAlbumModes;
    public static final int ALBUM_FILMSTRIP_MODE_SELECTED = 0;
    public static final int ALBUM_GRID_MODE_SELECTED = 1;
    
    public int mTitleColor = Color.BLACK;

    /// M: [BUG.ADD] @{
    private String mTitleValue = "";
    /// @}
    private int StringTitleWidth = 0;
    
    private ImageView mOverflowIm;
    public interface ClusterRunner {
        public void doCluster(int id);
    }
    /* PRIZE- modefiy bug gallery When the screenshot is white, the font color of the image editor is not clear (see screenshot).-fuqiang-2015-6-9-start*/
    public ActionBar getmActionBar() {
    	
		return mActionBar;
	}
    

    /* PRIZE- modefiy bug gallery When the screenshot is white, the font color of the image editor is not clear (see screenshot)-fuqiang-2015-6-9-end*/

    public interface OnAlbumModeSelectedListener {
        public void onAlbumModeSelected(int mode);
    }

    private static class ActionItem {
        public int action;
        public boolean enabled;
        public boolean visible;
        public int spinnerTitle;
        public int dialogTitle;
        public int clusterBy;

        public ActionItem(int action, boolean applied, boolean enabled, int title,
                int clusterBy) {
            this(action, applied, enabled, title, title, clusterBy);
        }

        public ActionItem(int action, boolean applied, boolean enabled, int spinnerTitle,
                int dialogTitle, int clusterBy) {
            this.action = action;
            this.enabled = enabled;
            this.spinnerTitle = spinnerTitle;
            this.dialogTitle = dialogTitle;
            this.clusterBy = clusterBy;
            this.visible = true;
        }
    }

    private static final ActionItem[] sClusterItems = new ActionItem[] {
        new ActionItem(FilterUtils.CLUSTER_BY_ALBUM, true, false, R.string.albums,
                R.string.group_by_album),
        new ActionItem(FilterUtils.CLUSTER_BY_LOCATION, true, false,
                R.string.locations, R.string.location, R.string.group_by_location),
        new ActionItem(FilterUtils.CLUSTER_BY_TIME, true, false, R.string.times,
                R.string.time, R.string.group_by_time),
       /// M: [BUG.MARK] @{
                /*  //Google do not relative logic for FACE and TAG Cluster,so delete them temple.
        new ActionItem(FilterUtils.CLUSTER_BY_FACE, true, false, R.string.people,
                R.string.group_by_faces),
        new ActionItem(FilterUtils.CLUSTER_BY_TAG, true, false, R.string.tags,
            R.string.group_by_tags)*/
       /// @}
    };
    
    public void setDoubleTitle(int mTitleColor){
    	this.mTitleColor = mTitleColor;
    }

    private class ClusterAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return sClusterItems.length;
        }

        @Override
        public Object getItem(int position) {
            return sClusterItems[position];
        }

        @Override
        public long getItemId(int position) {
            return sClusterItems[position].action;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.action_bar_text,
                        parent, false);
            }
            TextView view = (TextView) convertView;
            view.setText(sClusterItems[position].spinnerTitle);
            return convertView;
        }
    }

    private class AlbumModeAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return mAlbumModes.length;
        }

        @Override
        public Object getItem(int position) {
            return mAlbumModes[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.action_bar_two_line_text,
                        parent, false);
            }
            TwoLineListItem view = (TwoLineListItem) convertView;
            /// M: [BUG.MODIFY] @{
            /*view.getText1().setText(mActionBar.getTitle());*/
            view.getText1().setText(mTitleValue);
            view.getText1().setTextSize(16);
            view.getText1().setTextColor(mTitleColor);
            /// @}
            view.getText2().setText((CharSequence) getItem(position));
            view.getText2().setTextSize(14);
            view.getText2().setTextColor(mTitleColor);

            return convertView;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.action_bar_text,
                        parent, false);
            }
            TextView view = (TextView) convertView;
            view.setText((CharSequence) getItem(position));
            view.setTextColor(Color.BLACK);
            view.setBackgroundColor(Color.WHITE);
            return convertView;
        }
    }

    public static String getClusterByTypeString(Context context, int type) {
        for (ActionItem item : sClusterItems) {
            if (item.action == type) {
                return context.getString(item.clusterBy);
            }
        }
        return null;
    }

    public GalleryActionBar(AbstractGalleryActivity activity) {
    	final String overflowDesc = activity.getString(R.string.accessibility_overflow);
        // The top-level window
        final ViewGroup decor = (ViewGroup) activity.getWindow().getDecorView();
        // Wait a moment to ensure the overflow button can be located
        final ViewTreeObserver viewTreeObserver = decor.getViewTreeObserver();
	    viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
	        @Override
	        public void onGlobalLayout() {
	        	// The List that contains the matching views
                final ArrayList<View> outViews = new ArrayList<>();
                // Traverse the view-hierarchy and locate the overflow button
                decor.findViewsWithText(outViews, overflowDesc,
                        View.FIND_VIEWS_WITH_CONTENT_DESCRIPTION);
                // Guard against any errors
                if (outViews.isEmpty()) {
                    return;
                }
                // Do something with the view
                mOverflowIm = (ImageView) outViews.get(0);
                if (mOverflowResId != -1) {
                	mOverflowIm.setImageResource(mOverflowResId);
                }
                decor.getViewTreeObserver().removeOnGlobalLayoutListener(this);
	        }
	    });
        mActionBar = activity.getActionBar();
        mActionBar.setElevation(0);
        mContext = activity.getAndroidContext();
        mActivity = activity;
        mInflater = ((Activity) mActivity).getLayoutInflater();
        mCurrentIndex = 0;
    }

    private void createDialogData() {
        ArrayList<CharSequence> titles = new ArrayList<CharSequence>();
        mActions = new ArrayList<Integer>();
        for (ActionItem item : sClusterItems) {
            if (item.enabled && item.visible) {
                titles.add(mContext.getString(item.dialogTitle));
                mActions.add(item.action);
            }
        }
        mTitles = new CharSequence[titles.size()];
        titles.toArray(mTitles);
    }

    public int getHeight() {
        return mActionBar != null ? mActionBar.getHeight() : 0;
    }

    public void setClusterItemEnabled(int id, boolean enabled) {
        for (ActionItem item : sClusterItems) {
            if (item.action == id) {
                item.enabled = enabled;
                return;
            }
        }
    }

    public void setClusterItemVisibility(int id, boolean visible) {
        for (ActionItem item : sClusterItems) {
            if (item.action == id) {
                item.visible = visible;
                return;
            }
        }
    }

    public int getClusterTypeAction() {
        return sClusterItems[mCurrentIndex].action;
    }

    public void enableClusterMenu(int action, ClusterRunner runner) {
        /*if (mActionBar != null) {
            // Don't set cluster runner until action bar is ready.
            mClusterRunner = null;
            mActionBar.setListNavigationCallbacks(mAdapter, this);
            mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
            setSelectedAction(action);
            mClusterRunner = runner;
        }*/
    }

    // The only use case not to hideMenu in this method is to ensure
    // all elements disappear at the same time when exiting gallery.
    // hideMenu should always be true in all other cases.
    public void disableClusterMenu(boolean hideMenu) {
        if (mActionBar != null) {
            mClusterRunner = null;
            if (hideMenu) {
                mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
            }
        }
    }

    public void onConfigurationChanged() {
        if (mActionBar != null && mAlbumModeListener != null&&!(mActivity.getStateManager().getTopState() instanceof AlbumSetPage)) {
            OnAlbumModeSelectedListener listener = mAlbumModeListener;
            enableAlbumModeMenu(mLastAlbumModeSelected, listener);
        }
    }

    public void enableAlbumModeMenu(int selected, OnAlbumModeSelectedListener listener) {
        if (mActionBar != null) {
            if (mAlbumModeAdapter == null) {
                // Initialize the album mode options if they haven't been already
                Resources res = mActivity.getResources();
                mAlbumModes = new CharSequence[] {
                        res.getString(R.string.switch_photo_filmstrip),
                        res.getString(R.string.switch_photo_grid)};
                mAlbumModeAdapter = new AlbumModeAdapter();
            }
            mAlbumModeListener = null;
            mLastAlbumModeSelected = selected;
            mActionBar.setListNavigationCallbacks(mAlbumModeAdapter, this);
            mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
            mActionBar.setSelectedNavigationItem(selected);
            mAlbumModeListener = listener;
        }
    }

    public void disableAlbumModeMenu(boolean hideMenu) {
        if (mActionBar != null) {
            mAlbumModeListener = null;
            if (hideMenu) {
                mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
            }
        }
    }

    public void showClusterDialog(final ClusterRunner clusterRunner) {
        createDialogData();
        final ArrayList<Integer> actions = mActions;
        new AlertDialog.Builder(mContext).setTitle(R.string.group_by).setItems(
                mTitles, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Need to lock rendering when operations invoked by system UI (main thread) are
                // modifying slot data used in GL thread for rendering.
                mActivity.getGLRoot().lockRenderThread();
                try {
                    clusterRunner.doCluster(actions.get(which).intValue());
                } finally {
                    mActivity.getGLRoot().unlockRenderThread();
                }
            }
        }).create().show();
    }

    @TargetApi(ApiHelper.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void setHomeButtonEnabled(boolean enabled) {
        if (mActionBar != null) mActionBar.setHomeButtonEnabled(enabled);
    }

    public void setDisplayOptions(boolean displayHomeAsUp, boolean showTitle) {
        if (mActionBar == null) return;
        int options = 0;
        if (displayHomeAsUp) options |= ActionBar.DISPLAY_HOME_AS_UP;
        if (showTitle) options |= ActionBar.DISPLAY_SHOW_TITLE;

        mActionBar.setDisplayOptions(options,
                ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_TITLE);
        mActionBar.setHomeButtonEnabled(displayHomeAsUp);
    }

    public void setTitle(String title) {
        /// M: [BUG.MODIFY] @{
        /*if (mActionBar != null) mActionBar.setTitle(title);*/
        if (mActionBar != null) {
            mTitleValue = title;
            mActionBar.setTitle(title);
        }
        /// @}
    }
    
    public void setTitle(CharSequence title) {
        /// M: [BUG.MODIFY] @{
        /*if (mActionBar != null) mActionBar.setTitle(title);*/
        if (mActionBar != null) {
            mTitleValue = title.toString();
            mActionBar.setTitle(title);
        }
        /// @}
    }
    
    /*prize bug 13061 Set the font for the biggest show album thumbnail view pictures, display two menu button at the top right wanzhijuan 2016-3-12 start*/
    private void JudgeTitleWidth(){
    	Paint mPaint = new Paint();
    	mPaint.setTextSize(sp2px(mContext, 16));
    	if(mTitleValue == null){
    		return;
    	}
    	
    	if(mAlbumModes==null){
    		return;
    	}
    	if(mTitleValue!=null){
        	StringTitleWidth = (int) mPaint.measureText(mTitleValue) + dip2px(mContext, 16);
    	}
    
    	if(mAlbumModes!=null&&mAlbumModes.length>=1){
    		mPaint.setTextSize(sp2px(mContext, 14));
    		int mTitleMinWidth =  (int) mPaint.measureText(mAlbumModes[0].toString()) + dip2px(mContext, 16);
    		Log.i(TAG, "judgeTitleWidth mTitleMinWidth=" + mTitleMinWidth + " StringTitleWidth=" + StringTitleWidth);
    		if(mTitleMinWidth>StringTitleWidth){
    			StringTitleWidth = mTitleMinWidth;
    		}else{

    		}
    	}
    }
    
    private static float sp2px(Context context, float dpValue) {
    	final float scale = context.getResources().getDisplayMetrics().scaledDensity;
    	return dpValue * scale;
    }
    /*prize bug 13061 Set the font for the biggest show album thumbnail view pictures, display two menu button at the top right wanzhijuan 2016-3-12 end*/
    
    public static int dip2px(Context context, float dpValue) {
    	final float scale = context.getResources().getDisplayMetrics().density;
    	return (int) (dpValue * scale + 0.5f);
    }
    
    public int getTitleWidth(){
    	JudgeTitleWidth();
    	return StringTitleWidth;
    }

    public void setTitle(int titleId) {
        if (mActionBar != null) {
            /// M: [BUG.ADD] @{
            mTitleValue = mContext.getString(titleId);
            /// @}
            mActionBar.setTitle(mContext.getString(titleId));
        }
    }

    public void setSubtitle(String title) {
        if (mActionBar != null) mActionBar.setSubtitle(title);
    }

    public void show() {
        if (mActionBar != null) mActionBar.show();
    }

    public void hide() {
        if (mActionBar != null) mActionBar.hide();
    }

    public void addOnMenuVisibilityListener(OnMenuVisibilityListener listener) {
        if (mActionBar != null) mActionBar.addOnMenuVisibilityListener(listener);
    }

    public void removeOnMenuVisibilityListener(OnMenuVisibilityListener listener) {
        if (mActionBar != null) mActionBar.removeOnMenuVisibilityListener(listener);
    }

    public boolean setSelectedAction(int type) {
        if (mActionBar == null) return false;

        for (int i = 0, n = sClusterItems.length; i < n; i++) {
            ActionItem item = sClusterItems[i];
            if (item.action == type) {
                mActionBar.setSelectedNavigationItem(i);
                mCurrentIndex = i;
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
        if (itemPosition != mCurrentIndex && mClusterRunner != null
                || mAlbumModeListener != null) {
            // Need to lock rendering when operations invoked by system UI (main thread) are
            // modifying slot data used in GL thread for rendering.
            mActivity.getGLRoot().lockRenderThread();
            try {
                if (mAlbumModeListener != null) {
                    mAlbumModeListener.onAlbumModeSelected(itemPosition);
                } else {
                    mClusterRunner.doCluster(sClusterItems[itemPosition].action);
                }
            } finally {
                mActivity.getGLRoot().unlockRenderThread();
            }
        }
        return false;
    }

    private Menu mActionBarMenu;
    private ShareActionProvider mSharePanoramaActionProvider;
    private ShareActionProvider mShareActionProvider;
    private Intent mSharePanoramaIntent;
    private Intent mShareIntent;
    /// M: [FEATURE.ADD] @{
    private ActivityChooserView mActivityChooserView;
    private ActivityChooserModel mDataModel;
    /// @}

    public void createActionBarMenu(int menuRes, Menu menu) {
        mActivity.getMenuInflater().inflate(menuRes, menu);
        mActionBarMenu = menu;

        MenuItem item = menu.findItem(R.id.action_share_panorama);
        /// M: [BEHAVIOR.MARK] mask panorama share menu @{
        /*
        if (item != null) {
            mSharePanoramaActionProvider = (ShareActionProvider)
                item.getActionProvider();
            mSharePanoramaActionProvider
                .setShareHistoryFileName("panorama_share_history.xml");
            mSharePanoramaActionProvider.setShareIntent(mSharePanoramaIntent);
        }
        */
        /// @}
        item = menu.findItem(R.id.action_share);
        if (item != null) {
            mShareActionProvider = (ShareActionProvider)
                item.getActionProvider();
            mShareActionProvider
                .setShareHistoryFileName("share_history.xml");
            /// M: [FEATURE.ADD] @{
            mActivityChooserView = (ActivityChooserView) item.getActionView();
            mDataModel = ActivityChooserModel.get(mActivity, "share_history.xml");
            // set existed mOnChooseActivityListener
            if (mOnChooseActivityListener != null) {
                mDataModel
                        .setOnChooseActivityListener(mOnChooseActivityListener);
            }
            /// @}
            mShareActionProvider.setShareIntent(mShareIntent);
        }

        /// M: [BUG.ADD] Set the expand action icon resource. @{
        if (mActivityChooserView != null) {
            TypedValue outTypedValue = new TypedValue();
            mActivity.getTheme().resolveAttribute(
                    com.android.internal.R.attr.actionModeShareDrawable,
                    outTypedValue, true);
            mActivityChooserView.setExpandActivityOverflowButtonDrawable(mActivity
                    .getApplicationContext().getResources().getDrawable(
                            R.drawable.ic_menu_share_holo_light));
        }
        /// @}
    }

    public Menu getMenu() {
        return mActionBarMenu;
    }

    /// M: [FEATURE.MODIFY] @{
    /*public void setShareIntents(Intent sharePanoramaIntent, Intent shareIntent,
     ShareActionProvider.OnShareTargetSelectedListener onShareListener) {*/
    public void setShareIntents(Intent sharePanoramaIntent, Intent shareIntent,
            ActivityChooserModel.OnChooseActivityListener onChooseListener) {
    /// @}
        /// M: [BEHAVIOR.MARK] mask panorama share menu @{
        /*
        mSharePanoramaIntent = sharePanoramaIntent;
        if (mSharePanoramaActionProvider != null) {
            mSharePanoramaActionProvider.setShareIntent(sharePanoramaIntent);
        }
        */
        /// @}
        mShareIntent = shareIntent;
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(shareIntent);
            /// M: [FEATURE.MODIFY] @{
            /*mShareActionProvider.setOnShareTargetSelectedListener(
                    onChooseListener);*/
            if (mDataModel != null) {
                Log.d(TAG, "mDataModel.setOnChooseActivityListener(onChooseListener)");
                mDataModel.setOnChooseActivityListener(onChooseListener);
            }
            mOnChooseActivityListener = onChooseListener;
            /// @}
        }
    }

    //********************************************************************
    //*                              MTK                                 *
    //********************************************************************

    /**
     * Remove album mode listener.
     */
    //need to remove AlbumModeListener if doCluster in AlbumSetPage.
    public void removeAlbumModeListener() {
        if (mActionBar != null) {
            mAlbumModeListener = null;
            Log.i(TAG, "<removeAlbumModeListener> removeAlbumModeListener to doCluster");
        }
    }

    /**
     * Set action bar logo resource.
     * @param resId resource id
     */
    public void setLogo(int resId) {
        if (mActionBar != null) {
            mActionBar.setLogo(resId);
        }
    }

    /**
     * Set action bar logo resource.
     * @param logo resource
     */
    public void setLogo(Drawable logo) {
        if (mActionBar != null) {
            mActionBar.setLogo(logo);
        }
    }

    /**
     * Set action bar useLog enabled.
     * @param useLogo enable status
     */
    public void setDisplayUseLogoEnabled(boolean useLogo) {
        if (mActionBar != null) {
            mActionBar.setDisplayUseLogoEnabled(useLogo);
        }
    }

    /**
     * Notify data change.
     */
    public final void notifyDataSetChanged() {
        if (mAlbumModeAdapter != null) {
            mAlbumModeAdapter.notifyDataSetChanged();
        }
    }
    
    public void setDisplayShowHomeEnabled(boolean enable) {
        if (mActionBar != null) {
            mActionBar.setDisplayShowHomeEnabled(enable);
        }
    }
    
    private int mOverflowResId = -1;
    public void setOverflowIcon(int resId) {
    	mOverflowResId = resId;
    	if (mOverflowIm != null) {
    		mOverflowIm.setImageResource(resId);
    	}
    }

    ActivityChooserModel.OnChooseActivityListener mOnChooseActivityListener;
}
