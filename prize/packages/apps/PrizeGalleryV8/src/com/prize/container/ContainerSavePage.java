/*
* Copyright (C) 2014 MediaTek Inc.
* Modification based on code covered by the mentioned copyright
* and/or permission notice(s).
*/
/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.prize.container;


import android.app.AlertDialog;
import android.app.StatusBarManager;
import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.android.gallery3d.R;
import com.android.gallery3d.app.ActivityState;
import com.android.gallery3d.app.OrientationManager;
import com.android.gallery3d.data.LocalImage;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.data.MediaSet;
import com.android.gallery3d.data.Path;
import com.android.gallery3d.ui.DetailsHelper;
import com.android.gallery3d.ui.GLView;
import com.android.gallery3d.ui.MenuExecutor;
import com.android.gallery3d.ui.SelectionManager;
import com.android.gallery3d.ui.SynchronizedHandler;
import com.android.gallery3d.ui.WakeLockHoldingProgressListener;
import com.android.gallery3d.util.LogUtil;
import com.prize.container.ui.HorizontalScrollLayoutAdapter;
import com.prize.container.ui.SelectHorizontalScrollerLayout;
import com.android.gallery3d.app.PhotoPage;

import java.util.ArrayList;

public class ContainerSavePage extends ActivityState {
    private static final String TAG = "Gallery2/ContainerSavePage";

    private static final int MSG_HIDE_BARS = 1;
    private static final int MSG_ON_FULL_SCREEN_CHANGED = 4;
    private static final int MSG_UNFREEZE_GLROOT = 6;
    private static final int MSG_REFRESH_BOTTOM_CONTROLS = 8;
    private static final int MSG_ON_CAMERA_CENTER = 9;
    private static final int MSG_ON_PICTURE_CENTER = 10;
    private static final int MSG_REFRESH_IMAGE = 11;
    private static final int MSG_UPDATE_PHOTO_UI = 12;
    private static final int MSG_UPDATE_DEFERRED = 14;
    private static final int UNFREEZE_GLROOT_TIMEOUT = 250;

    public static final String KEY_MEDIA_ITEM_PATH = "media-item-path";
    public static final String KEY_INDEX_HINT = "index-hint";
    public static final String KEY_MEDIA_PATH = "media-path";


    private SelectionManager mSelectionManager;
    private WakeLockHoldingProgressListener mDeleteProgressListener;

    private ContainerPhotoView mPhotoView;
    private Model mModel;

    private MediaSet mMediaSet;

    private int mCurrentIndex = 0;
    private Handler mHandler;

    private boolean mFinishStateWhenResume = false;
    private Path mMediaSetPath;
    private FrameLayout mActionBarFl;
    private View mActionBarView;
    private TextView mCancelTv;
    private TextView mDoneTv;
    private TextView mTitleTv;
    private MediaItem mCurrentPhoto = null;
    private MenuExecutor mMenuExecutor;
    private boolean mIsActive;
//    private OrientationManager mOrientationManager;
    private boolean mHasCameraScreennailOrPlaceholder = false;

    private long mCameraSwitchCutoff = 0;
    private boolean mSkipUpdateCurrentPhoto = false;
    private static final long CAMERA_SWITCH_CUTOFF_THRESHOLD_MS = 300;

    private static final long DEFERRED_UPDATE_MS = 250;
    private boolean mDeferredUpdateWaiting = false;
    private long mDeferUpdateUntil = Long.MAX_VALUE;
    private int mOrientation;
    private SelectHorizontalScrollerLayout mTipChs;

    @Override
    protected int getBackgroundColorId() {
        return R.color.photo_background;
    }

    private final GLView mRootPane = new GLView() {
        @Override
        protected void onLayout(
                boolean changed, int left, int top, int right, int bottom) {
            mPhotoView.layout(0, 0, right - left, bottom - top - 60);
        }
    };

    public static interface Model extends ContainerPhotoView.Model {
        public void resume();
        public void pause();
        public boolean isEmpty();
        public void setCurrentPhoto(Path path, int indexHint);
    }

	@Override
    public void onCreate(Bundle data, Bundle restoreState) {
        super.onCreate(data, restoreState);
        mOrientation = mActivity.getRequestedOrientation();
        LogUtil.i(TAG, "onCreate mOrientation=" + mOrientation);
		/*-- fixbug : 55277 by liangchangwei 2018-4-25 --*/
        Intent intent = mActivity.getIntent();
		mLaunchFromCamera = intent.getBooleanExtra(PhotoPage.KEY_LAUNCH_FROM_CAMERA, false);
        misScureCamera = intent.getBooleanExtra(PhotoPage.IS_SECURE_CAMERA, false);
		/*-- fixbug : 55277 by liangchangwei 2018-4-25 --*/
        mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mActionBarFl = mActivity.getActionBarFl();
        mTipChs = (SelectHorizontalScrollerLayout) mActivity.getContainerTipView().findViewById(R.id.chs_bottom);
        Context context = mActivity.getAndroidContext();
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mActionBarView = inflater
                .inflate(R.layout.container_action_bar, null, false);
        mCancelTv = (TextView) mActionBarView.findViewById(R.id.tv_cancel);
        mDoneTv = (TextView) mActionBarView.findViewById(R.id.tv_done);
        mTitleTv = (TextView) mActionBarView.findViewById(R.id.tv_title);
        mSelectionManager = new SelectionManager(mActivity, false);
        mSelectionManager.enterSelectionMode();
        mSelectionManager.setAutoLeaveSelectionMode(false);
        mMenuExecutor = new MenuExecutor(mActivity, mSelectionManager);

        mPhotoView = new ContainerPhotoView(mActivity, mSelectionManager, mTipChs);
        mRootPane.addComponent(mPhotoView);
//        mOrientationManager = mActivity.getOrientationManager();
//        mActivity.getGLRoot().setOrientationSource(mOrientationManager);

        mHandler = new SynchronizedHandler(mActivity.getGLRoot()) {
            @Override
            public void handleMessage(Message message) {
                switch (message.what) {
                    case MSG_UNFREEZE_GLROOT: {
                        mActivity.getGLRoot().unfreeze();
                        break;
                    }
                    case MSG_UPDATE_DEFERRED: {
                        long nextUpdate = mDeferUpdateUntil - SystemClock.uptimeMillis();
                        if (nextUpdate <= 0) {
                            mDeferredUpdateWaiting = false;
                            updateUIForCurrentPhoto();
                        } else {
                            mHandler.sendEmptyMessageDelayed(MSG_UPDATE_DEFERRED, nextUpdate);
                        }
                        break;
                    }
                    case MSG_ON_CAMERA_CENTER: {
                        mSkipUpdateCurrentPhoto = false;
                        boolean stayedOnCamera = false;
                        if (SystemClock.uptimeMillis() < mCameraSwitchCutoff &&
                                mMediaSet.getMediaItemCount() > 1) {
                            mPhotoView.switchToImage(1);
                        } else {
                            stayedOnCamera = true;
                        }

                        if (stayedOnCamera) {
                            if (mMediaSet.getTotalMediaItemCount() > 1) {
                                /* We got here by swiping from photo 1 to the
                                   placeholder, so make it be the thing that
                                   is in focus when the user presses back from
                                   the camera app */
                                mPhotoView.switchToImage(1);
                            } else {
                                /// M: [BUG.MODIFY] getMediaItem(0) may be null, fix JE @{
                                /*updateCurrentPhoto(mModel.getMediaItem(0));*/
                                MediaItem photo = mModel.getMediaItem(0);
                                if (photo != null) {
                                    updateCurrentPhoto(photo);
                                }
                                /// @}
                            }
                        }
                        break;
                    }
                    case MSG_REFRESH_IMAGE: {
                        final MediaItem photo = mCurrentPhoto;
                        mCurrentPhoto = null;
                        updateCurrentPhoto(photo);
                        break;
                    }
                    case MSG_UPDATE_PHOTO_UI: {
                        updateUIForCurrentPhoto();
                        break;
                    }
                    default: throw new AssertionError(message.what);
                }
            }
        };

        initializeData(data);
        mPhotoView.setListener(new ContainerPhotoView.Listener() {
            @Override
            public void onSingleTapUp(int x, int y, int drawWidth, int screenWidth, int drawHeight, int screenHeight) {
                LogUtil.i(TAG, "<onSingleTapUp> x=" + x + " y=" + y + " drawWidth=" + drawWidth + " screenWidth=" + screenWidth);
                int left = (screenWidth - drawWidth) / 2;
                int right = left + drawWidth;
                int top = (screenHeight - drawHeight) / 2;
                int bottom = top + drawHeight;
                if (y >= top && y <= bottom && x >= left && x <= right) {
                    MediaItem item = mModel.getMediaItem(0);
                    if (item == null) {
                        return; // Item not ready yet, ignore the click
                    }

                    mSelectionManager.toggle(item.getPath());
                    mTipChs.toggle(item.getPath().toString());
                    setTitle();
                }
            }

        });
        mPhotoView.setFilmMode(true);
        /// @}
    }

    private HorizontalScrollLayoutAdapter adapter;
    private void initBottomTip() {
        LogUtil.i(TAG, "initBottomTip=" + adapter);
        if (adapter == null) {
            adapter = new HorizontalScrollLayoutAdapter(mActivity, mModel, R.layout.center_scorll_item, mTipChs);
            mTipChs.setAdapter(adapter);
            mTipChs.setOnItemClickListener(new SelectHorizontalScrollerLayout.OnItemClickListener() {
                @Override
                public void onItemClick(int pos) {
                    mModel.moveTo(pos);
                }

                @Override
                public void updateIndex(int pos) {
                    mModel.updateTo(pos);
                }
            });
        }
    }

    private void initializeData(Bundle data) {
        String mediaPath = data.getString(KEY_MEDIA_PATH);
        if (mediaPath == null || mediaPath.equals("")) {
            LogUtil.w(TAG, "<initializeData> data.getString(KEY_MEDIA_PATH) is not available,"
                    + " finishState when onResume");
            mFinishStateWhenResume = true;
            return;
        }
        mMediaSetPath = Path.fromString(mediaPath);
        mMediaSet = mActivity.getDataManager().getMediaSet(mMediaSetPath);
        if (mMediaSet == null || mMediaSet.getMediaItemCount() == 0) {
            // How to go into this case?
            // 1.Enter ContainerPage 2.Press home key to exit
            // 3.Delete this group of continuous shot image in FileManager
            // 4.Kill com.android.gallery3d process 5.Launch gallery
            LogUtil.w(TAG, "<initializeData> mMediaSet = " + mMediaSet + ", Path = " + mMediaSetPath
                    + ", finishState when onResume");
            mFinishStateWhenResume = true;
            return;
        }
        mSelectionManager.setSourceMediaSet(mMediaSet);

        ContainerPhotoDataAdapter pda = new ContainerPhotoDataAdapter(
                mActivity, mPhotoView, mMediaSet, null, mCurrentIndex,
                -1,
                false,
                false);

        mModel = pda;

        mPhotoView.setModel(mModel);

        pda.setDataListener(new ContainerPhotoDataAdapter.DataListener() {

            @Override
            public void onPhotoChanged(int index, Path item) {
                int oldIndex = mCurrentIndex;
                mCurrentIndex = index;
                if (mHasCameraScreennailOrPlaceholder) {
                    if (mCurrentIndex > 0) {
                        mSkipUpdateCurrentPhoto = false;
                    }

                    /// M: [FEATURE.MODIFY] @{
                        /*if (oldIndex == 0 && mCurrentIndex > 0
                         && !mPhotoView.getFilmMode()) {
                         mPhotoView.setFilmMode(true);*/
                    if (oldIndex == 0 && mCurrentIndex > 0) {
                        mPhotoView.setFilmMode(false);
                        /// @}
                    } else if (oldIndex == 2 && mCurrentIndex == 1) {
                        mCameraSwitchCutoff = SystemClock.uptimeMillis() +
                                CAMERA_SWITCH_CUTOFF_THRESHOLD_MS;
                        mPhotoView.stopScrolling();
                    } else if (oldIndex >= 1 && mCurrentIndex == 0) {
                        mSkipUpdateCurrentPhoto = true;
                    }
                }
                if (!mSkipUpdateCurrentPhoto) {
                    if (item != null) {
                        MediaItem photo = mModel.getMediaItem(0);
                        if (photo != null) updateCurrentPhoto(photo);
                    }
                }

                setTitle();
                // Reset the timeout for the bars after a swipe
                /// M: [DEBUG.ADD] @{
                LogUtil.i(TAG, "<onPhotoChanged> refreshHidingMessage");
                /// @}
            }

            @Override
            public void onLoadingFinished(boolean loadingFailed) {
                /// M: [BUG.ADD] @{
                if (mModel.getTotalCount() == 0) {
                    mActivity.getStateManager().finishState(ContainerSavePage.this);
                    return;
                }
                mLoadingFinished = true;
                /// M: [BUG.ADD] Notify mSelectionManger to update. @{
                mSelectionManager.onSourceContentChanged();
                /// @}
                if (!mModel.isEmpty()) {
                    MediaItem photo = mModel.getMediaItem(0);
                    if (photo != null) updateCurrentPhoto(photo);
                    initBottomTip();
                    mTipChs.setSelectIndex(mModel.getCurrentIndex());
                }
            }

            @Override
            public void onLoadingStarted() {
                /// M: [BUG.ADD] @{
                mLoadingFinished = false;
                /// @}
            }
        });
    }
	
    private void requestDeferredUpdate() {
        mDeferUpdateUntil = SystemClock.uptimeMillis() + DEFERRED_UPDATE_MS;
        if (!mDeferredUpdateWaiting) {
            mDeferredUpdateWaiting = true;
            mHandler.sendEmptyMessageDelayed(MSG_UPDATE_DEFERRED, DEFERRED_UPDATE_MS);
        }
    }

    private void updateUIForCurrentPhoto() {
        if (mCurrentPhoto == null) return;
        /// M: [FEATURE.ADD] [Camera independent from Gallery] @{
        // After delete all medias in camera folder, show EmptyAlbumImage,
        // set film mode as false forced.
        if (mLaunchFromCamera && mCurrentPhoto != null
                && (mCurrentPhoto.getSupportedOperations() & MediaItem.SUPPORT_BACK) != 0) {
            mPhotoView.setFilmMode(false);
        }
        /// @}
    }

    private void updateCurrentPhoto(MediaItem photo) {
        /// M: [BUG.MODIFY] @{
        /*if (mCurrentPhoto == photo) return;*/
        // Modify for update support operation menu display
        // if photo.getDataVersion() != mCurrentVersion, means the mediaItem has been updated
        if (mCurrentPhoto == photo && photo.getDataVersion() == mCurrentVersion) {
            return;
        }
        mCurrentVersion = photo.getDataVersion();
        /// @}
        mCurrentPhoto = photo;
//        requestDeferredUpdate();
    }

    @Override
    protected void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onStateResult(int requestCode, int resultCode, Intent data) {
    }

    @Override
    protected void systemUIMode() {
        /*Window window = mActivity.getWindow();
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_VISIBLE);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.statusBarInverse = StatusBarManager.STATUS_BAR_INVERSE_WHITE;
        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(Color.BLACK);
        window.setAttributes(lp);*/

        Window window = mActivity.getWindow();
        View decorView = window.getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.statusBarInverse = StatusBarManager.STATUS_BAR_INVERSE_WHITE;
        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(Color.TRANSPARENT);
		/*-- fixbug : 55277 by liangchangwei 2018-4-25 --*/
        if (mLaunchFromCamera && misScureCamera) {
			Log.w(TAG,"set Flag FLAG_SHOW_WHEN_LOCKED");
			lp.flags |= WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
        }else{
			//winParams.flags |= ~WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
			if((lp.flags&WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED) != 0){
				Log.w(TAG,"need clear Flag FLAG_SHOW_WHEN_LOCKED");
				lp.flags &= ~WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
			}
			Log.w(TAG,"Flag FLAG_SHOW_WHEN_LOCKED has clear");
        }
		/*-- fixbug : 55277 by liangchangwei 2018-4-25 --*/
		// remove by liangchangwei fix bugID 54946 --2018-4-10
		//lp.flags |= WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
        window.setAttributes(lp);
    }

    @Override
    protected void showContainerTip(View containerTip) {
        if (containerTip != null) {
            containerTip.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onPause() {
        /// M: [DEBUG.ADD] @{
        LogUtil.i(TAG, "<onPause> begin");
        /// @}
        super.onPause();
        mIsActive = false;

        mActivity.getGLRoot().unfreeze();
        mHandler.removeMessages(MSG_UNFREEZE_GLROOT);

        DetailsHelper.pause();
        // Hide the detail dialog on exit
        if (mModel != null) {
            mModel.pause();
        }
        mPhotoView.pause();
        mHandler.removeMessages(MSG_HIDE_BARS);
        mHandler.removeMessages(MSG_REFRESH_BOTTOM_CONTROLS);

        mMenuExecutor.pause();
    }

    private void initActionBar() {
        mCancelTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        mDoneTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmSave();
            }
        });
        setTitle();
    }

    private void setTitle() {
        if (mTitleTv != null) {
            int count = mSelectionManager.getSelectedCount();
            String title = "";
            if (count == 0) {
                title = mActivity.getResources().getString(R.string.container_title);
                mDoneTv.setEnabled(false);
            } else {
                mDoneTv.setEnabled(true);
                String format = mActivity.getResources().getQuantityString(
                        R.plurals.number_of_items_selected, count);
                title = String.format(format, count);
            }
            mTitleTv.setText(title);
        }
    }

    private void confirmSave() {
        int count = mSelectionManager.getSelectedCount();
        String format = mActivity.getResources().getQuantityString(
                R.plurals.container_number_of_items_save, count);
        String confirmMsg = String.format(format, count);
        new AlertDialog.Builder(mActivity.getAndroidContext())
                .setMessage(confirmMsg)
                .setPositiveButton(R.string.ok, new AlertDialog.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        save();
                    }
                })
                .setNegativeButton(R.string.cancel, new AlertDialog.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).create().show();
    }

    private void save() {
        setActionEventChange(R.id.m_action_best_shots);
    }

    public void setActionEventChange(int action) {
        if (mDeleteProgressListener == null) {
            mDeleteProgressListener = new WakeLockHoldingProgressListener(mActivity,
                    "Gallery Delete Progress Listener");
        }
        MenuExecutor.ProgressListener listener = mDeleteProgressListener;
        try {
            mSelectionManager.setPrepared(getUnSelectShotPaths());
            mMenuExecutor.setOnMenuClicked(action, listener);
        } catch (RemoteException | OperationApplicationException e) {

        }
    }

    public ArrayList<Path> getUnSelectShotPaths() throws RemoteException, OperationApplicationException {
        Log.i(TAG, "getUnSelectShotPaths start");
        ArrayList<Path> unSelectShot = new ArrayList<Path>();
        int total = mMediaSet.getMediaItemCount();
        ArrayList<MediaItem> list = mMediaSet.getMediaItem(0, total);
        ArrayList<Path> selectPaths = mSelectionManager.getSelected(false);
        ArrayList<ContentProviderOperation> ops =
                new ArrayList<ContentProviderOperation>();
        for (MediaItem item : list) {
            Path id = item.getPath();
            if (!selectPaths.contains(id)) {
                unSelectShot.add(id);
            } else {
                if (item != null && item instanceof LocalImage) {
                    LocalImage localImage = (LocalImage) item;
                    Log.i(TAG, "getUnSelectShotPaths pathName=" + localImage.getFilePath());
                    Uri baseUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    Uri uri = baseUri.buildUpon().appendPath(String.valueOf(localImage.id))
                            .build();
                    ops.add(ContentProviderOperation.newUpdate(uri)
                            .withValue(MediaStore.Images.ImageColumns.GROUP_ID, 0)
                            .build());
                }
            }
        }
        mActivity.getContentResolver().applyBatch(MediaStore.AUTHORITY, ops);
        Log.i(TAG, "getUnSelectShotPaths end");
        return unSelectShot;
    }

    @Override
    protected void onResume() {
        /// M: [DEBUG.ADD] @{
        LogUtil.i(TAG, "<onResume> begin");
        /// @}
        super.onResume();
        if (mFinishStateWhenResume) {
            LogUtil.i(TAG, "<onResume> mFinishStateWhenResume, finishState, return");
            mActivity.getStateManager().finishState(this);
            return;
        }
        if (mModel == null) {
            /// M: [BUG.ADD] pause PhotoView before finish PhotoPage @{
            mPhotoView.pause();
            /// @}
            mActivity.getStateManager().finishState(this);
            return;
        }
        /// M: [BUG.MARK] @{
        // In order to avoid black screen when PhotoPage just starts, google freeze the GLRoot when
        // resume, and unfreeze it when image updated or unfreeze time out. But this solution is not
        // suitable for N, it will cause ANR when lock/unlock screen.
        /* mActivity.getGLRoot().freeze();*/
        /// @}
        mIsActive = true;
        setContentPane(mRootPane);
        if (mActionBarFl != null) {
            mActionBarFl.removeAllViews();
            mActionBarFl.addView(mActionBarView);
            initActionBar();
        }

        mModel.resume();
        mPhotoView.resume();
        mActionBarFl.setVisibility(View.VISIBLE);

        mHandler.sendEmptyMessageDelayed(MSG_UNFREEZE_GLROOT, UNFREEZE_GLROOT_TIMEOUT);

        /// M: [BUG.ADD] @{
        // update share intent and other UI when comes back from paused status
//        updateUIForCurrentPhoto();
        /// @}
    }
    
    @Override
    protected void onDestroy() {
//        mActivity.getGLRoot().setOrientationSource(null);
        mActivity.setRequestedOrientation(mOrientation);
        // Remove all pending messages.
        /// M: [BUG.MODIFY] @{
        //mHandler.removeCallbacksAndMessages(null);
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        /// @}
        /// M: [FEATURE.ADD] @{
        mPhotoView.destroy();
        /// @}
        mTipChs.destroy();
        adapter = null;
        super.onDestroy();
    }

    //********************************************************************
    //*                              MTK                                 *
    //********************************************************************

    public boolean mLoadingFinished = false;
    private boolean mLaunchFromCamera = false;
	/*-- fixbug : 55277 by liangchangwei 2018-4-25 --*/
    private boolean misScureCamera = false;
    private long mCurrentVersion;

    protected void onSaveState(Bundle outState) {
        // keep record of current index and current photo
        mData.putInt(KEY_INDEX_HINT, mCurrentIndex);
        if (mCurrentPhoto != null) {
            Path photoPath = mCurrentPhoto.getPath();
            if (photoPath != null) {
                mData.putString(KEY_MEDIA_ITEM_PATH, photoPath.toString());
            }
        }
    }
}
