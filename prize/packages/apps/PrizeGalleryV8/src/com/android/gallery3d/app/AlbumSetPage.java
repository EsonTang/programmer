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

package com.android.gallery3d.app;

import android.Manifest;
import android.R.color;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.android.gallery3d.R;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.data.ClusterAlbum;
import com.android.gallery3d.data.DataManager;
import com.android.gallery3d.data.LocalImage;
import com.android.gallery3d.data.LocalMergeAlbum;
import com.android.gallery3d.data.MediaDetails;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.data.MediaObject;
import com.android.gallery3d.data.MediaSet;
import com.android.gallery3d.data.Path;
import com.android.gallery3d.filtershow.crop.CropActivity;
import com.android.gallery3d.filtershow.crop.CropExtras;
import com.android.gallery3d.glrenderer.FadeTexture;
import com.android.gallery3d.glrenderer.GLCanvas;
import com.android.gallery3d.picasasource.PicasaSource;
import com.android.gallery3d.settings.GallerySettings;

import com.android.gallery3d.ui.ActionModeHandler;
import com.android.gallery3d.ui.AlbumSetSlotRenderer;
import com.android.gallery3d.ui.DetailsHelper;
import com.android.gallery3d.ui.DetailsHelper.CloseListener;
import com.android.gallery3d.ui.DetailsHelper.DetailsSource;
import com.android.gallery3d.ui.GLRoot;
import com.android.gallery3d.ui.GLRootView;
import com.android.gallery3d.ui.GLView;
import com.android.gallery3d.ui.PrizeRootGLView;
import com.android.gallery3d.ui.SelectionManager;
import com.android.gallery3d.ui.SlotView;
import com.android.gallery3d.ui.SynchronizedHandler;
import com.android.gallery3d.ui.TimeLineSlotRenderer;
import com.android.gallery3d.ui.TimeLineSlotView;
import com.android.gallery3d.util.Future;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.HelpUtils;
import com.android.gallery3d.util.LogUtil;
import com.android.gallery3d.util.MediaSetUtils;
import com.mediatek.galleryframework.base.MediaData;
import com.mediatek.galleryfeature.platform.PlatformHelper;
import com.mediatek.galleryfeature.config.FeatureConfig;

import com.mediatek.gallery3d.adapter.ContainerPage;
//import com.mediatek.gallery3d.adapter.ContainerPage;
import com.mediatek.gallery3d.layout.FancyHelper;
import com.mediatek.gallery3d.layout.Layout.DataChangeListener;
import com.mediatek.gallery3d.util.PermissionHelper;
import com.prize.slideselect.ISelectMode;
import com.prize.util.DensityUtil;
import com.prize.util.GloblePrizeUtil;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import com.prize.dialog.DialogBottomMenu;
import com.prize.dialog.DialogItemOnClickListener;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import com.mediatek.galleryfeature.container.ContainerHelper;
import com.mediatek.common.prizeoption.PrizeOption;
import android.content.pm.ActivityInfo;
// new feature: disable prientation change for PRIZE_NOTCH_SCREEN

/// M: [BUG.MODIFY] leave selection mode when plug out sdcard @{
/*
public class AlbumSetPage extends ActivityState implements
        SelectionManager.SelectionListener, GalleryActionBar.ClusterRunner,
        EyePosition.EyePositionListener, MediaSet.SyncListener {
*/
public class AlbumSetPage extends ActivityState implements
		SelectionManager.SelectionListener, GalleryActionBar.ClusterRunner,
		EyePosition.EyePositionListener, MediaSet.SyncListener,
		AbstractGalleryActivity.EjectListener,AlbumButtomControls.Delegate {
	// / @}
	@SuppressWarnings("unused")
	private static final String TAG = "Gallery2/AlbumSetPage";

    private static final int MSG_PICK_PHOTO = 0;
	private static final int MSG_PICK_ALBUM = 1;
	private static final int MSG_SHOW_BUTTOM = 2;
	private static final int MSG_HIDE_BUTTOM = 3;
	private static final int MSG_REASH_BUTTON = 4;
	private static final int MSG_RESET_ONRESUM = 5;
	private static final int MSG_DELAY_RENDER = 6;
	private static final int MSG_UPDATE_MENUITEM = 7;
	private static final int DELAY_RENDERER_TIME = 500;

    public static final String KEY_MEDIA_PATH = "media-path";
    public static final String KEY_SET_TITLE = "set-title";
    public static final String KEY_SET_SUBTITLE = "set-subtitle";
    public static final String KEY_SELECTED_CLUSTER_TYPE = "selected-cluster";
	public static final String KEY_PAGE_ID = "page-id";

    private static final int DATA_CACHE_SIZE = 256;
	private static final int REQUEST_NO_ANIMATION = 0;
    private static final int REQUEST_DO_ANIMATION = 1;
    private static final int REQUEST_PHOTO = 2;
    private static final int REQUEST_ALBUM = 3;

    private static final int BIT_LOADING_RELOAD = 1;
    private static final int BIT_LOADING_SYNC = 2;


	private boolean mIsActive = false;
	private TimeLineSlotView mTimeSlotView;
	private SlotView mSetSlotView;
	private AlbumSetSlotRenderer mAllSlotRenderer;
	private TimeLineSlotRenderer mTimeSlotRenderer;
	private Config.AlbumSetPage mConfig;
	private MediaSet mMediaSet;
	private MediaSet mMediaTimeSet;
	private FrameLayout mActionBarFl;
	private View mActionBarView;
	private ImageView mToCameraIm;
	private TextView mTitleTv;
	private TextView mSelectModeTv;

	protected SelectionManager mSelectionManager;
	private AlbumSetDataLoader mAlbumSetDataAdapter;
	private TimeLineDataLoader mAlbumSetTimeAdapter;
	private boolean mGetContent;
	private boolean mGetAlbum;
	private ActionModeHandler mActionModeHandler;
	private DetailsHelper mDetailsHelper;
	private MyDetailsSource mDetailsSource;
	private MyTimeDetailsSource mTimeDetailsSource;
	private boolean mShowDetails;
	private EyePosition mEyePosition;
	private Handler mHandler;
	private boolean mLaunchedFromPhotoPage;

	// The eyes' position of the user, the origin is at the center of the
	// device and the unit is in pixels.
	private float mX;
	private float mY;
	private float mZ;
	
	private AlbumButtomControls mBottomControls;

    private Future<Integer> mSyncTask = null;

    private int mLoadingBits = 0;
	private int mTimeLoadingBits = 0;
    private boolean mInitialSynced = false;

	private View mCameraButton = null;
	private ImageView mToCameraImg = null;
	private TextView mEmptyTipsTv = null;
	private boolean mShowedEmptyToastForSelf = false;
	// / M: [BUG.ADD] if get the mTitle/mSubTitle,they will not change when
	// switch language@{
	private int mClusterType = -1;
	// / @}

	// / M: [PERF.ADD] for performance auto test@{
	public boolean mLoadingFinished = false;
	public boolean mInitialized = false;

	// / @}
	
	public static final int PAGE_TIME = 0;
	public static final int PAGE_ALL = 1;

	private int mCurrentPage = PAGE_TIME;

    @Override
    protected int getBackgroundColorId() {
        return R.color.albumset_background;
    }

	private PrizeRootGLView mRootPane;
	private Path mMediaSetPath;
	private int mTabViewHeight;

	@Override
	protected void showTabView(View tabView) {
		if (mSelectionManager.inSelectionMode()) {
			tabView.setVisibility(View.GONE);
		} else {
			tabView.setVisibility(View.VISIBLE);
		}
	}

    @Override
    public void onEyePositionChanged(float x, float y, float z) {
        mRootPane.lockRendering();
        mX = x;
        mY = y;
        mZ = z;
        /// M: [FEATURE.ADD] fancy layout @{
        if (FancyHelper.isFancyLayoutSupported() && !mActivity.isInMultiWindowMode()) {
			LogUtil.i(TAG, "<onEyePositionChanged> <Fancy> enter");
            // need to update screen width height for screen rotation
            DisplayMetrics reMetrics = getDisplayMetrics();
            FancyHelper.doFancyInitialization(reMetrics.widthPixels,
                    reMetrics.heightPixels);
            int layoutType = mEyePosition.getLayoutType();
            if (mLayoutType != layoutType) {
                mLayoutType = layoutType;
                // need clear content window cache before setting visible range
                // otherwise covers may not be loaded
				
				LogUtil.i(TAG, "<onEyePositionChanged> <Fancy> begin to switchLayout");
				mAllSlotRenderer.onEyePositionChanged(mLayoutType);
			}
		}
		// / @}
		mRootPane.unlockRendering();
		mRootPane.setEyeXyz(x, y, z);
		mRootPane.invalidate();
    }

	@Override
	public void onBackPressed() {
		if (mShowDetails) {
			hideDetails();
		} else if (mSelectionManager.inSelectionMode()) {
			mSelectionManager.leaveSelectionMode();
		} else {
			super.onBackPressed();
		}
	}
	
	public boolean getSelectInMode(){
		if (mSelectionManager != null) {
			return mSelectionManager.inSelectionMode();
		}
		return false;
	}
	
	public void unSelectMode(){
		if (mSelectionManager.inSelectionMode()) {
			mSelectionManager.leaveSelectionMode();
		}
	}

	private void getSlotCenter(int slotIndex, int center[]) {
		Rect offset = new Rect();
		mRootPane.getBoundsOf(mSetSlotView, offset);
		Rect r = mSetSlotView.getSlotRect(slotIndex);
		int scrollX = mSetSlotView.getScrollX();
		int scrollY = mSetSlotView.getScrollY();
		center[0] = offset.left + (r.left + r.right) / 2 - scrollX;
		center[1] = offset.top + (r.top + r.bottom) / 2 - scrollY;
	}

	public void onSingleTapUp(int slotIndex) {
		if (!mIsActive) return;

		if (mSelectionManager.inSelectionMode()) {
			MediaSet targetSet = mAlbumSetDataAdapter.getMediaSet(slotIndex);
			if (targetSet == null) return; // Content is dirty, we shall reload soon
			if (mRestoreSelectionDone) {
				// / M: [BUG.ADD] fix menu display abnormal @{
				if (mActionModeHandler != null) {
					mActionModeHandler.closeMenu();
				}
				/// @}
				mSelectionManager.toggle(targetSet.getPath());
				mSetSlotView.invalidate();
			} else {
				if (mWaitToast == null) {
					mWaitToast = Toast.makeText(mActivity,
							com.android.internal.R.string.wait,
							Toast.LENGTH_SHORT);
				}
				mWaitToast.show();
			}
		} else {
			/// M: [BUG.ADD] check slotIndex valid or not @{
			if (mAlbumSetDataAdapter != null
					&& !mAlbumSetDataAdapter.isActive(slotIndex)) {
				LogUtil.i(TAG, "<onSingleTapUp> slotIndex " + slotIndex
						+ " is not active, return!");
				return;
			}
			/// @}
			// Show pressed-up animation for the single-tap.
			mAllSlotRenderer.setPressedIndex(slotIndex);
			mAllSlotRenderer.setPressedUp();
			/// M: [PERF.MODIFY] send Message without 180ms delay for improving performance. @{
			//mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_PICK_ALBUM, slotIndex, 0), FadeTexture.DURATION);
			LogUtil.d(TAG, "onSingleTapUp() at " + System.currentTimeMillis());
			mHandler.sendMessage(mHandler.obtainMessage(MSG_PICK_ALBUM, slotIndex, 0));
			mAllSlotRenderer.setPressedIndex(-1);
			/// @}
		}
	}

    private void pickPhoto(int slotIndex, int page) {
        pickPhoto(slotIndex, false, page);
    }

    private void pickPhoto(int slotIndex, boolean startInFilmstrip, int page) {
        if (!mIsActive) return;

        if (!startInFilmstrip) {
            // Launch photos in lights out mode
            mActivity.getGLRoot().setLightsOutMode(true);
        }

        TimeLineDataLoader albumDataAdapter;
        TimeLineSlotView slotView;
		albumDataAdapter = mAlbumSetTimeAdapter;
		slotView = mTimeSlotView;

        MediaItem item = albumDataAdapter.get(slotIndex);

        if (mGetContent) {
            if (item == null)
                return; // Item not ready yet, ignore the click
            onGetContent(item);
        } else if (mLaunchedFromPhotoPage) {
            TransitionStore transitions = mActivity.getTransitionStore();
            transitions.put(
                    PhotoPage.KEY_ALBUMPAGE_TRANSITION,
                    PhotoPage.MSG_ALBUMPAGE_PICKED);
            transitions.put(PhotoPage.KEY_INDEX_HINT, slotIndex);
            onBackPressed();
        } else {
			/*if (isConshot(item)) {
				PlatformHelper.enterContainerPage(mActivity, item.getMediaData(), false, null);
				return;
			}*/
			/// @}
           // MediaSet targetset = albumDataAdapter.getMediaSet(slotIndex);
        	if(!mAlbumSetDataAdapter.isActive(0))return;
          
            MediaSet targetSet = mAlbumSetDataAdapter.getMediaSet(0);
            if (targetSet == null || item == null) return;
            if (targetSet instanceof LocalMergeAlbum){
                 LocalMergeAlbum mAlbum = (LocalMergeAlbum)targetSet;
                 if(null != mAlbum && null != mAlbum.mFilePath){
                     if(mAlbum.getMediaItemCount() <= 0 || !mAlbum.mFilePath.startsWith("/storage/emulated/0/DCIM/Camera/")){
                       	 targetSet = albumDataAdapter.getMediaSet(slotIndex);
                        } 
                    }
            }else{
                targetSet = albumDataAdapter.getMediaSet(slotIndex);
            }
            
             
             Bundle data = new Bundle();
             data.putInt(PhotoPage.KEY_INDEX_HINT, slotIndex-1);
             data.putString(PhotoPage.KEY_MEDIA_SET_PATH,
            		 targetSet.getPath().toString());
             data.putParcelable(PhotoPage.KEY_OPEN_ANIMATION_RECT,
                     slotView.getSlotRect(slotIndex, mRootPane));
             data.putString(PhotoPage.KEY_MEDIA_ITEM_PATH,
                     item.getPath().toString());
             //data.putBoolean(PhotoPage.KEY_FROM_TIMELINE_SCREEN, true);
             data.putInt(PhotoPage.KEY_ALBUMPAGE_TRANSITION,
                     PhotoPage.MSG_ALBUMPAGE_STARTED);
             data.putBoolean(PhotoPage.KEY_START_IN_FILMSTRIP,
                     startInFilmstrip);
             data.putBoolean(PhotoPage.KEY_IN_CAMERA_ROLL, targetSet.isCameraRoll());
             mActivity.getStateManager().startStateForResult(
                     SinglePhotoPage.class, REQUEST_PHOTO, data);
        }
    }

	private boolean isConshot(MediaItem item) {
        if (item == null) {
            return false;
        }
        MediaData md = item.getMediaData();
		LogUtil.i(TAG, "isConshot md=" + md);
		if (md != null && md.mediaType == MediaData.MediaType.CONTAINER
				&& md.subType == MediaData.SubType.CONSHOT) {
			if (md.relateData == null) {
				md.relateData = ContainerHelper.getConShotDatas(mActivity, md.groupID, md.bucketId);
			}

			if (md.relateData == null || md.relateData.size() <= 1) {
				md.relateData = null;
				if (item instanceof LocalImage) {
					LocalImage localImage = (LocalImage) item;
					Uri baseUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
					Uri uri = baseUri.buildUpon().appendPath(String.valueOf(localImage.id))
							.build();
					ContentValues values = new ContentValues();
					values.put(MediaStore.Images.ImageColumns.GROUP_ID, 0);
					mActivity.getContentResolver().update(uri, values, null, null);
				}
				return false;
			}
			return true;
		}
		return false;
	}

	private void onSingleTapUp(TimeLineSlotView.Slot slot, int page) {
		if (!mIsActive) return;
		TimeLineDataLoader albumDataAdapter;
		TimeLineSlotView slotView;
		TimeLineSlotRenderer slotRenderer;
		albumDataAdapter = mAlbumSetTimeAdapter;
		slotView = mTimeSlotView;
		slotRenderer = mTimeSlotRenderer;
		boolean isTitle = slot.isTitle;
		int slotIndex = slot.index;
		if (mSelectionManager.inSelectionMode()) {
			if (isTitle) {
				MediaSet targetSet = albumDataAdapter.getMediaSet(slotIndex);
				if (targetSet == null) return;
				ArrayList<Path> paths = ((ClusterAlbum)targetSet).getMediaItems();
				if (paths == null || paths.size() <= 0) return;
				mSelectionManager.toggleTimeLineSet(paths);
				slotRenderer.updateAllTimelineTitle(true, slot.titleIndex, slot.index);
				slotView.invalidate();
			} else {
				MediaItem item = albumDataAdapter.get(slotIndex);
				if (item == null) return; // Item not ready yet, ignore the click
				if (mSelectionManager.getSelectedCount() > 0) {
					//if (!ActionModeHandler.isThreadComplete)
					//return;
				}
				mSelectionManager.toggle(item.getPath());
				slotRenderer.updateAllTimelineTitle(false, slot.titleIndex, slot.index);
				slotView.invalidate();
			}
		} else if(!isTitle){
			// Render transition in pressed state
			slotRenderer.setPressedIndex(slotIndex);
			slotRenderer.setPressedUp();
			mHandler.sendMessage(mHandler.obtainMessage(MSG_PICK_PHOTO, slotIndex, page));
			slotRenderer.setPressedIndex(-1);
		}
	}


	public boolean isSelectMode(){
		return mSelectionManager.inSelectionMode();
	}

    private static boolean albumShouldOpenInFilmstrip(MediaSet album) {
        int itemCount = album.getMediaItemCount();
        ArrayList<MediaItem> list = (itemCount == 1) ? album.getMediaItem(0, 1) : null;
        // open in film strip only if there's one item in the album and the item exists
        return (list != null && !list.isEmpty());
    }

    WeakReference<Toast> mEmptyAlbumToast = null;

	private void showEmptyAlbumToast(int toastLength) {
//		Toast toast;
//		if (mEmptyAlbumToast != null) {
//			toast = mEmptyAlbumToast.get();
//			if (toast != null) {
//				toast.show();
//				return;
//			}
//		}
//		toast = Toast.makeText(mActivity, R.string.empty_album, toastLength);
//		mEmptyAlbumToast = new WeakReference<Toast>(toast);
//		toast.show();
	}

	private void hideEmptyAlbumToast() {
		if (mEmptyAlbumToast != null) {
			Toast toast = mEmptyAlbumToast.get();
			if (toast != null)
				toast.cancel();
		}
	}

    private void pickAlbum(int slotIndex) {
        if (!mIsActive) return;
        /// M: [BUG.ADD] check if slotIndex is active before getMediaSet @{
        if (!mAlbumSetDataAdapter.isActive(slotIndex)) {
            return;
        }
        /// @}

        MediaSet targetSet = mAlbumSetDataAdapter.getMediaSet(slotIndex);
        if (targetSet == null) return; // Content is dirty, we shall reload soon
        if (targetSet.getTotalMediaItemCount() == 0) {
            showEmptyAlbumToast(Toast.LENGTH_SHORT);
            return;
        }
        hideEmptyAlbumToast();

        String mediaPath = targetSet.getPath().toString();
        Log.d(TAG,"pickAlbum mediaPath"+ mediaPath);

        Bundle data = new Bundle(getData());
        int[] center = new int[2];
        getSlotCenter(slotIndex, center);
        data.putIntArray(AlbumPage.KEY_SET_CENTER, center);
        if (mGetAlbum && targetSet.isLeafAlbum()) {
            Activity activity = mActivity;
            Intent result = new Intent()
                    .putExtra(AlbumPicker.KEY_ALBUM_PATH, targetSet.getPath().toString());
            activity.setResult(Activity.RESULT_OK, result);
            activity.finish();
        } else if (targetSet.getSubMediaSetCount() > 0) {
            data.putString(AlbumSetPage.KEY_MEDIA_PATH, mediaPath);
            mActivity.getStateManager().startStateForResult(
                    AlbumSetPage.class, REQUEST_DO_ANIMATION, data);
        } else {
            if (!mGetContent && albumShouldOpenInFilmstrip(targetSet)) {
                data.putParcelable(PhotoPage.KEY_OPEN_ANIMATION_RECT,
                        mSetSlotView.getSlotRect(slotIndex, mRootPane));
                data.putInt(PhotoPage.KEY_INDEX_HINT, 0);
                data.putString(PhotoPage.KEY_MEDIA_SET_PATH,
                        mediaPath);
                data.putBoolean(PhotoPage.KEY_START_IN_FILMSTRIP, true);
                data.putBoolean(PhotoPage.KEY_IN_CAMERA_ROLL, targetSet.isCameraRoll());
                /// M: [BUG.ADD] @{
                data.putString(PhotoPage.KEY_MEDIA_ITEM_PATH, null);
                /// @}
                mActivity.getStateManager().startStateForResult(
                        FilmstripPage.class, AlbumPage.REQUEST_PHOTO, data);
                /// M: [BUG.ADD] avoid show selected icon when back from album page @{
                mAllSlotRenderer.setPressedIndex(-1);
                /// @}
                return;
            }
            data.putString(AlbumPage.KEY_MEDIA_PATH, mediaPath);

            // We only show cluster menu in the first AlbumPage in stack
            boolean inAlbum = mActivity.getStateManager().hasStateClass(AlbumPage.class);
            data.putBoolean(AlbumPage.KEY_SHOW_CLUSTER_MENU, !inAlbum);
            mActivity.getStateManager().startStateForResult(
                    AlbumPage.class, REQUEST_NO_ANIMATION, data);
        }
    }

	// / M:[FEATURE.ADD] play video directly. @{
	public void playVideo(Activity activity, Uri uri, String title) {
		LogUtil.i(TAG, "<playVideo> enter playVideo");
		try {
			Intent intent = new Intent(Intent.ACTION_VIEW)
					.setPackage("com.android.gallery3d")
					.setDataAndType(uri, "video/*")
					.putExtra(Intent.EXTRA_TITLE, title)
					.putExtra(MovieActivity.KEY_TREAT_UP_AS_BACK, true);
			activity.startActivityForResult(intent,
					PhotoPage.REQUEST_PLAY_VIDEO);
		} catch (ActivityNotFoundException e) {
			Toast.makeText(activity, activity.getString(R.string.video_err),
					Toast.LENGTH_SHORT).show();
		}
	}

	private boolean canBePlayed(MediaItem item) {
		int supported = item.getSupportedOperations();
		return ((supported & MediaItem.SUPPORT_PLAY) != 0 && MediaObject.MEDIA_TYPE_VIDEO == item
				.getMediaType());
	}

	private void onGetContent(final MediaItem item) {
        DataManager dm = mActivity.getDataManager();
        Activity activity = mActivity;
		// / M: [FEATURE.ADD] @{
		MediaData md = item.getMediaData();
		if (md.mediaType == MediaData.MediaType.CONTAINER
				&& md.subType == MediaData.SubType.CONSHOT) {
			PlatformHelper.enterContainerPage(activity, md, true, mData);
			return;
		}
		// / @}
        if (mData.getString(GalleryActivity.EXTRA_CROP) != null) {
            Uri uri = dm.getContentUri(item.getPath());
            Intent intent = new Intent(CropActivity.CROP_ACTION, uri)
                    .addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT)
                    .putExtras(getData());
            if (mData.getParcelable(MediaStore.EXTRA_OUTPUT) == null) {
                intent.putExtra(CropExtras.KEY_RETURN_DATA, true);
            }
            /// M: [DEBUG.ADD] @{
            LogUtil.d(TAG, "<onGetContent> start CropActivity for extra crop, uri: " + uri);
            /// @}
            activity.startActivity(intent);
            activity.finish();
        } else {
            Intent intent = new Intent(null, item.getContentUri())
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            activity.setResult(Activity.RESULT_OK, intent);
            /// M: [DEBUG.ADD] @{
            LogUtil.d(TAG, "<onGetContent> return uri: " + item.getContentUri());
            /// @}
            activity.finish();
        }
    }

	public void setPage(int page) {
		mCurrentPage = page;
        setTitle();
		if(mSelectionManager.getSelectedCount()>0){
			mSelectionManager.leaveSelectionMode();
		}
		initSelectionManager();
		//prize-public-bug:24213 only show Camera pictures on the first/third pages - pengcancan-20161109-start
		updateEmptyView(page);
		//prize-public-bug:24213 only show Camera pictures on the first/third pages - pengcancan-20161109-end
		mRootPane.setScrollPage(page);
	}

	private void initSelectionManager() {
		if (mCurrentPage == PAGE_TIME) {
			mSelectionManager.setIsAlbumSet(false);
			mSelectionManager.setSourceMediaSet(mMediaTimeSet);
		} else if (mCurrentPage == PAGE_ALL) {
			mSelectionManager.setSourceMediaSet(mMediaSet);
			mSelectionManager.setIsAlbumSet(true);
		}
	}
	
	private void onDown(int index){
		this.onDown(index, mCurrentPage);
	}

	private void onDown(int index, int page) {
		switch (page) {
		case PAGE_TIME:
			if (!mSelectionManager.inSelectionMode()) {
				mTimeSlotRenderer.setPressedIndex(index);
			}
			break;
		case PAGE_ALL:
			mAllSlotRenderer.setPressedIndex(index);
			break;
		default:
			break;
		}
	}

	private void onUp(boolean followedByLongPress){
		this.onUp(followedByLongPress, mCurrentPage);
	}
	
	private void onUp(boolean followedByLongPress, int page) {
		switch (page) {
		case PAGE_TIME:
			if (followedByLongPress) {
				mTimeSlotRenderer.setPressedIndex(-1);
			} else {
				mTimeSlotRenderer.setPressedUp();
			}
			break;
		case PAGE_ALL:
			if (followedByLongPress) {
				// Avoid showing press-up animations for long-press.
				mAllSlotRenderer.setPressedIndex(-1);
			} else {
				mAllSlotRenderer.setPressedUp();
			}
			break;
		default:
			break;
		}

	}

	public void onLongTap(int slotIndex) {
		if (mGetContent || mGetAlbum) return;
		MediaSet set = mAlbumSetDataAdapter.getMediaSet(slotIndex);
		if (set == null) return;
		/// M: [BUG.ADD] fix menu display abnormal @{
		if (mActionModeHandler != null) {
			mActionModeHandler.closeMenu();
		}
		/// @}
		mSelectionManager.setAutoLeaveSelectionMode(true);
		mSelectionManager.toggle(set.getPath());
		mSetSlotView.invalidate();
	}

    public void onLongTap(TimeLineSlotView.Slot slot, int page) {
        if (mGetContent) return;
        TimeLineDataLoader albumDataAdapter;
        TimeLineSlotView slotView;
		albumDataAdapter = mAlbumSetTimeAdapter;
		slotView = mTimeSlotView;
		boolean isTitle = slot.isTitle;
		int slotIndex = slot.index;
        if (isTitle) {
            /*MediaSet targetSet = albumDataAdapter.getMediaSet(slotIndex);
            if (targetSet == null) return;
            ArrayList<Path> paths = ((ClusterAlbum)targetSet).getMediaItems();
            if (paths == null || paths.size() <= 0) return;
            mSelectionManager.setAutoLeaveSelectionMode(true);
            mSelectionManager.toggleTimeLineSet(paths);
			mTimeSlotRenderer.updateAllTimelineTitle(true, slot.titleIndex, slot.index);
            slotView.invalidate();*/
        } else {
            MediaItem item = albumDataAdapter.get(slotIndex);
            if (item == null) return;
            mSelectionManager.setAutoLeaveSelectionMode(true);
            mSelectionManager.toggle(item.getPath());
			mTimeSlotRenderer.updateAllTimelineTitle(false, slot.titleIndex, slot.index);
            slotView.invalidate();
        }
    }

    @Override
    public void doCluster(int clusterType) {
        /// M: [FEATURE.ADD] [Runtime permission] @{
        if (clusterType == FilterUtils.CLUSTER_BY_LOCATION
                && !PermissionHelper.checkAndRequestForLocationCluster(mActivity)) {
			LogUtil.i(TAG, "<doCluster> permission not granted");
            mNeedDoClusterType = clusterType;
            return;
        }
        /// @}
        String basePath = mMediaSet.getPath().toString();
        String newPath = FilterUtils.switchClusterPath(basePath, clusterType);
        Bundle data = new Bundle(getData());
        data.putString(AlbumSetPage.KEY_MEDIA_PATH, newPath);
        data.putInt(KEY_SELECTED_CLUSTER_TYPE, clusterType);
        mActivity.getStateManager().switchState(this, AlbumSetPage.class, data);
    }

	@Override
	public void onCreate(Bundle data, Bundle restoreState) {
		super.onCreate(data, restoreState);
		
		initRootPan(mActivity);
		initializeViews();
		initializeData(data);

		// / M: [PERF.ADD] for performance auto test@{
		mInitialized = true;
		// / @}
		Context context = mActivity.getAndroidContext();
		if(mActivity instanceof GalleryActivity){
			((GalleryActivity) mActivity).setTabTitle(mCurrentPage);
		}
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		if(PrizeOption.PRIZE_NOTCH_SCREEN){
			mActionBarView = inflater
					.inflate(R.layout.notch_albumset_action_bar, null, false);
		}else{
			mActionBarView = inflater
					.inflate(R.layout.albumset_action_bar, null, false);
		}
		mTabViewHeight = context.getResources().getDimensionPixelSize(R.dimen.tabview_height);
		mToCameraIm = (ImageView) mActionBarView.findViewById(R.id.im_left);
		mTitleTv = (TextView) mActionBarView.findViewById(R.id.tv_title);
		mSelectModeTv = (TextView) mActionBarView.findViewById(R.id.tv_select);
		mGetContent = data.getBoolean(GalleryActivity.KEY_GET_CONTENT, false);
		mGetAlbum = data.getBoolean(GalleryActivity.KEY_GET_ALBUM, false);
        mClusterType = data.getInt(AlbumSetPage.KEY_SELECTED_CLUSTER_TYPE);
		mEyePosition = new EyePosition(context, this);
		mDetailsSource = new MyDetailsSource();
		mTimeDetailsSource = new MyTimeDetailsSource();
		mActionBarFl = mActivity.getActionBarFl();

		initActionBar();

		mLaunchedFromPhotoPage = mActivity.getStateManager().hasStateClass(FilmstripPage.class)
				&& !mActivity.getStateManager().hasStateClassInNearPosition(ContainerPage.class);
		mHandler = new SynchronizedHandler(mActivity.getGLRoot()) {
			@Override
			public void handleMessage(Message message) {
				switch (message.what) {
                    case MSG_PICK_PHOTO:
                        pickPhoto(message.arg1, message.arg2);
                        break;
					case MSG_PICK_ALBUM: {
						pickAlbum(message.arg1);
						break;
					}
					case MSG_SHOW_BUTTOM:
						if (mCurrentPage == PAGE_ALL) {
							mBottomControls.updateSupportedOperation(true);
						} else {
							mBottomControls.updateSupportedOperation(false);
						}
						mBottomControls.refresh();
						break;
					case MSG_HIDE_BUTTOM:
						mBottomControls.refresh();
						break;
					case MSG_REASH_BUTTON:
						mHandler.removeMessages(MSG_REASH_BUTTON);
						if (mCameraButton != null && mCameraButton.getVisibility() == View.VISIBLE) {
							setupCameraButton(false);
						}
						break;
					case MSG_RESET_ONRESUM:
						rendererResume();
						break;
					case MSG_DELAY_RENDER:
						delayRenderer(message.arg1);
						break;
					case MSG_UPDATE_MENUITEM:
						updateMenuItem();
						break;
	
					default:
						throw new AssertionError(message.what);
					}
			}
		};
		
		ViewGroup galleryRoot = (ViewGroup) ((Activity) mActivity).findViewById(R.id.gallery_root);
		if (galleryRoot != null) {
			mBottomControls = new AlbumButtomControls(this, mActivity, galleryRoot);
		}

	}

	private void initActionBar() {
		mToCameraIm.setImageResource(R.drawable.ic_title_to_camera);
		setTitle();
		mToCameraIm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GalleryUtils.startCameraActivity(mActivity);
            }
		});
        mSelectModeTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSelectionManager.setAutoLeaveSelectionMode(false);
                mSelectionManager.enterSelectionMode();
                if (mCurrentPage == PAGE_TIME) {
                    mTimeSlotView.invalidate();
					//mTimeSlotRenderer.updateAllTimelineTitleContent(true);
                }
            }
        });
	}

	private void setTitle() {
		if (mCurrentPage == PAGE_TIME) {
			mTitleTv.setText(R.string.tab_photo);
		} else {
			mTitleTv.setText(R.string.tab_gallery);
		}
	}
	
	private void delayRenderer(int page) {
		LogUtil.i(TAG, "delayRenderer page = " + page);
		if (page == PAGE_TIME) {
			mAlbumSetDataAdapter.resume();
			mAllSlotRenderer.resume();
		} else {
			mAlbumSetTimeAdapter.resume();
			mTimeSlotRenderer.resume();
		}
	}
	
	private void rendererResume() {
		LogUtil.i(TAG, "rendererResume mCurrentPage = " + mCurrentPage);
		if (mCurrentPage == PAGE_TIME) {
			mAlbumSetTimeAdapter.resume();
			mTimeSlotRenderer.resume();
		} else {
			mAlbumSetDataAdapter.resume();
			mAllSlotRenderer.resume();
		}
		mHandler.removeMessages(DELAY_RENDERER_TIME);
		Message msg = mHandler.obtainMessage(MSG_DELAY_RENDER, mCurrentPage, 0);
		mHandler.sendMessageDelayed(msg, DELAY_RENDERER_TIME);
	}
    @Override
    public void onDestroy() {
        super.onDestroy();
        cleanupCameraButton();
		if (mBottomControls != null) mBottomControls.cleanup();
        mActionModeHandler.destroy();
    }

    private boolean setupCameraButton(boolean needCorrection) {
        if (!GalleryUtils.isCameraAvailable(mActivity)) return false;
        RelativeLayout galleryRoot = (RelativeLayout) ((Activity) mActivity)
                .findViewById(R.id.gallery_root);
		if (galleryRoot == null)
			return false;
		mCameraButton = LayoutInflater.from(mActivity).inflate(R.layout.camera_enter, null);
		mToCameraImg = (ImageView) mCameraButton.findViewById(R.id.imageView2);
		mEmptyTipsTv = (TextView) mCameraButton.findViewById(R.id.tips);

		mToCameraImg.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				GalleryUtils.startCameraActivity(mActivity);
			}
		});

		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);

		lp.addRule(RelativeLayout.CENTER_IN_PARENT);
		galleryRoot.addView(mCameraButton, lp);

		return true;
	}
	
	private boolean canShowEmpty() {
		LogUtil.i(TAG, "canShowEmpty mLoadingBits=" + mLoadingBits + " mIsActive=" + mIsActive);
		if (getLoadingBit() == 0 && mIsActive) {
			return true;
		}
		return false;
	}

	private void cleanupCameraButton() {
		if (mCameraButton == null)
			return;
		RelativeLayout galleryRoot = (RelativeLayout) ((Activity) mActivity)
				.findViewById(R.id.gallery_root);
		if (galleryRoot == null)
			return;
		galleryRoot.removeView(mCameraButton);
		mCameraButton = null;
	}

	private void enableEdit(boolean enable) {
		if (mSelectModeTv != null) {
			mSelectModeTv.setEnabled(enable);
		}
	}

	private void showCameraButton() {
		enableEdit(false);
		if (mCameraButton == null && !setupCameraButton(false))
			return;
		mCameraButton.setVisibility(View.VISIBLE);
		if (mActivity.isInMultiWindowMode()){
			mToCameraImg.setVisibility(View.GONE);
			mEmptyTipsTv.setText(R.string.empty_tips);
		} else {
			mToCameraImg.setVisibility(View.VISIBLE);
			if (mCurrentPage == PAGE_TIME) {
				showPictureEmptyTip();
			} else {
				showGalleryEmptyTip();
			}
		}
	}

	private void showPictureEmptyTip() {
		mEmptyTipsTv.setText(R.string.not_picture_tip);
		mToCameraImg.setImageResource(R.drawable.no_picture_tip);
	}

	private void showGalleryEmptyTip() {
		mEmptyTipsTv.setText(R.string.not_gallery_tip);
		mToCameraImg.setImageResource(R.drawable.no_gallery_tip);
	}

	private void hideCameraButton() {
		enableEdit(true);
		if (mCameraButton == null)
			return;
		mCameraButton.setVisibility(View.GONE);
	}
	
	//prize-public-bug:24213 only show Camera pictures on the first/third pages - pengcancan-20161109-start
	private void updateEmptyView(int page) {
		if (!canShowEmpty()) return;
		int size = 0;
		if (page == PAGE_TIME) {
			size = mAlbumSetTimeAdapter.size();
		} else {
			size = mAlbumSetDataAdapter.size();
		}
		if (size == 0) {
			showCameraButton();
		} else {
			hideCameraButton();
		}
	}
	//prize-public-bug:24213 only show Camera pictures on the first/third pages - pengcancan-20161109-end

	private void clearLoadingBitPre(int loadingBit, int page) {
		if (page == PAGE_TIME) {
			mTimeLoadingBits &= ~loadingBit;
		} else {
			mLoadingBits &= ~loadingBit;
		}
	}

	private void clearLoadingBit(int loadingBit, int page) {
		clearLoadingBitPre(loadingBit, page);
		if (getLoadingBit() == 0 && mIsActive) {
			int size = 0;
			if (mCurrentPage == PAGE_TIME) {
				size = mAlbumSetTimeAdapter.size();
			} else {
				size = mAlbumSetDataAdapter.size();
			}
//			size = mAlbumSetDataAdapter.size();
			LogUtil.i(TAG, "clearLoadingBit size=" + size + "mCurrentPage= " + mCurrentPage);
			if (size == 0) {
				// If this is not the top of the gallery folder hierarchy,
				// tell the parent AlbumSetPage instance to handle displaying
				// the empty album toast, otherwise show it within this
				// instance
				if (mActivity.getStateManager().getStateCount() > 1) {
					Intent result = new Intent();
					result.putExtra(AlbumPage.KEY_EMPTY_ALBUM, true);
					setStateResult(Activity.RESULT_OK, result);
					mActivity.getStateManager().finishState(this);
				} else {
//					mShowedEmptyToastForSelf = true;
					showEmptyAlbumToast(Toast.LENGTH_LONG);
					mTimeSlotView.invalidate();
					mSetSlotView.invalidate();
					showCameraButton();
				}
				return;
			} else {
				hideCameraButton();
			}
		}
		// Hide the empty album toast if we are in the root instance of
		// AlbumSetPage and the album is no longer empty (for instance,
		// after a sync is completed and web albums have been synced)
		if (mShowedEmptyToastForSelf) {
			mShowedEmptyToastForSelf = false;
			hideEmptyAlbumToast();
			hideCameraButton();
		}
	}

	private int getLoadingBit() {
		return getLoadingBit(mCurrentPage);
	}

	private int getLoadingBit(int page) {
		if (page == PAGE_TIME) {
			return mTimeLoadingBits;
		} else {
			return mLoadingBits;
		}
	}

	private void setLoadingBit(int loadingBit, int page) {
		if (page == PAGE_TIME) {
			mTimeLoadingBits |= loadingBit;
		} else {
			mLoadingBits |= loadingBit;
		}
	}

	private void setLoadingBit(int loadingBit) {
		setLoadingBit(mCurrentPage);
	}

	@Override
	public void onPause() {
		super.onPause();
        /// M: [BUG.ADD] when user exits from current page, UpdateContent() in data loader may
        // be executed in main handler, it may cause seldom JE. @{
        if (FancyHelper.isFancyLayoutSupported() && mAlbumSetDataAdapter != null) {
            mAlbumSetDataAdapter.setFancyDataChangeListener(null);
			LogUtil.d(TAG, "<onPause> set FancyDataChangeListener as null");
        }
        /// @}
        mIsActive = false;
        /// M: [BEHAVIOR.ADD] @{
        if (mSelectionManager != null && mSelectionManager.inSelectionMode()) {
            mSelectionManager.saveSelection();
            mNeedUpdateSelection = false;
        }
        /// @}
		mHandler.removeMessages(MSG_DELAY_RENDER);
		mAlbumSetDataAdapter.pause();
		mAlbumSetTimeAdapter.pause();
		mAllSlotRenderer.pause();
		mTimeSlotRenderer.pause();

		mActionModeHandler.pause();
		/*prize-xuchunming-20160314-solve monkey error-start*/
		mHandler.removeMessages(MSG_RESET_ONRESUM);
		/*prize-xuchunming-20160314-solve monkey error-end*/
		mEyePosition.pause();
		DetailsHelper.pause();
		// Call disableClusterMenu to avoid receiving callback after paused.
		// Don't hide menu here otherwise the list menu will disappear earlier
		// than
		// the action bar, which is janky and unwanted behavior.
		if (mSyncTask != null) {
			mSyncTask.cancel();
			mSyncTask = null;
			clearLoadingBit(BIT_LOADING_SYNC, PAGE_ALL);
			clearLoadingBit(BIT_LOADING_SYNC, PAGE_TIME);
		}
		// / M: [BUG.ADD] leave selection mode when plug out sdcard @{
		mActivity.setEjectListener(null);
		// / @}

        /// M: [FEATURE.ADD] Multi-window. @{
        mActivity.setMultiWindowModeListener(null);
        /// @}
		if((mBottomMenuDeleteDialog != null)&&(mBottomMenuDeleteDialog.isShowing())){
			mBottomMenuDeleteDialog.dismiss();
		}

    }

    @Override
    public void onResume() {
        super.onResume();
        /// M: [BUG.ADD] when user exits from current page, UpdateContent() in data loader may
        // be executed in main handler, it may cause seldom JE. @{
        if (FancyHelper.isFancyLayoutSupported() && mAlbumSetDataAdapter != null) {
            mAlbumSetDataAdapter.setFancyDataChangeListener((DataChangeListener) mSetSlotView);
			LogUtil.d(TAG, "<onResume> reset FancyDataChangeListener");
        }
        /// @}
		mIsActive = true;
		setContentPane(mRootPane);
		// new feature: disable prientation change for PRIZE_NOTCH_SCREEN
		/*if(PrizeOption.PRIZE_NOTCH_SCREEN){
			Log.w(TAG,"PrizeOption.PRIZE_NOTCH_SCREEN true");
 			mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}*/
        if (!mSelectionManager.inSelectionMode()) {
            mActionBarFl.setVisibility(View.VISIBLE);
            mActionBarFl.removeAllViews();
            mActionBarFl.addView(mActionBarView);
            initActionBar();
        }
		// Set the reload bit here to prevent it exit this page in
		// clearLoadingBit().
		setLoadingBit(BIT_LOADING_RELOAD, PAGE_ALL);
		setLoadingBit(BIT_LOADING_RELOAD, PAGE_TIME);
		// / M: [BEHAVIOR.ADD] @{
		if (mSelectionManager != null && mSelectionManager.inSelectionMode()) {
			mNeedUpdateSelection = true;
			// set mRestoreSelectionDone as false if we need to restore
			// selection
			mRestoreSelectionDone = false;
		} else {
			// set mRestoreSelectionDone as true there is no need to restore
			// selection
			mRestoreSelectionDone = true;
		}
		// / @}
		rendererResume();

		mEyePosition.resume();
		mActionModeHandler.resume();

		if (mSelectionManager != null && mSelectionManager.inSelectionMode()) {
    		if (mCurrentPage == PAGE_TIME) {
    			mTimeSlotRenderer.updateAllTimelineSelect();
				if(mSelectionManager.inSelectAllMode()){
					mSelectionManager.selectAll();
				}else{
					mActionModeHandler.setTitle(getSelectedString());
				}
    		}
		}
		if (!mInitialSynced) {
			setLoadingBit(BIT_LOADING_SYNC, PAGE_ALL);
			setLoadingBit(BIT_LOADING_SYNC, PAGE_TIME);
			mSyncTask = mMediaSet.requestSync(AlbumSetPage.this);
		}
		// / M: [BUG.ADD] leave selection mode when plug out sdcard @{
		mActivity.setEjectListener(this);
		// / @}
		if(mActivity instanceof GalleryActivity){
			((GalleryActivity) mActivity).setTabTitle(mCurrentPage);
		}
        /// M: [FEATURE.ADD] [Runtime permission] @{
        if (mClusterType == FilterUtils.CLUSTER_BY_LOCATION
                && !PermissionHelper.checkLocationPermission(mActivity)) {
			LogUtil.i(TAG, "<onResume> CLUSTER_BY_LOCATION, permisison not granted, finish");
            PermissionHelper.showDeniedPrompt(mActivity);
            mActivity.getStateManager().finishState(AlbumSetPage.this);
            return;
        }
        /// @}

        /// M: [FEATURE.ADD] Multi-window. @{
        mActivity.setMultiWindowModeListener(mMultiWindowListener);
        mMultiWindowListener.onMultiWindowModeChanged(mActivity.isInMultiWindowMode());
        /// @}
		
    }

	private void initializeData(Bundle data) {
		String mediaPath = null;
		if (data.getString(KEY_MEDIA_PATH) != null) {
			mediaPath = data.getString(KEY_MEDIA_PATH);
		} else {
			mediaPath = mActivity.getDataManager().getTopSetPath(
					DataManager.INCLUDE_ALL);
		}
		mCurrentPage = data.getInt(KEY_PAGE_ID, PAGE_TIME);
		//prize-public-bug:24213 only show Camera pictures on the first/third pages - pengcancan-20161109-start
		String mLocalAllPath =  mActivity.getDataManager().getTopSetPath(
				DataManager.INCLUDE_LOCAL_ALL_ONLY)+"/"+MediaSetUtils.CAMERA_BUCKET_ID;
		LogUtil.d(TAG, "mLocalAllPath:"+mLocalAllPath);
		mMediaSet = mActivity.getDataManager().getMediaSet(mediaPath);
		String newTimePath = FilterUtils.switchClusterPath(mLocalAllPath, FilterUtils.CLUSTER_BY_TIME);
		LogUtil.d(TAG, "newTimePath:"+newTimePath);
		mMediaSetPath = Path.fromString(newTimePath);
		LogUtil.d(TAG, "mMediaSetPath:"+mMediaSetPath);
		mMediaTimeSet = mActivity.getDataManager().getMediaSet(newTimePath);
		//prize-public-bug:24213 only show Camera pictures on the first/third pages - pengcancan-20161109-end
		initSelectionManager();

		mAlbumSetDataAdapter = new AlbumSetDataLoader(mActivity, mMediaSet,
				DATA_CACHE_SIZE);
		mAlbumSetTimeAdapter = new TimeLineDataLoader(mActivity,
				mMediaTimeSet);

		mAlbumSetDataAdapter.setLoadingListener(new MyLoadingListener(PAGE_ALL));
		mAlbumSetTimeAdapter.setLoadingListener(new MyLoadingListener(PAGE_TIME));
		mAllSlotRenderer.setModel(mAlbumSetDataAdapter);
		mTimeSlotRenderer.setModel(mAlbumSetTimeAdapter);
		// / M: [FEATURE.ADD] fancy layout @{
		if (FancyHelper.isFancyLayoutSupported()) {
			mAlbumSetDataAdapter
					.setFancyDataChangeListener(mDataChangeListener);
			FancyHelper.initializeFancyThumbnailSizes(getDisplayMetrics());
		}
		// / @}
	}

	private DataChangeListener mDataChangeListener = new DataChangeListener() {

		@Override
		public void onDataChange(int index, MediaItem item, int size,
				boolean isCameraFolder, String albumName) {
			mSetSlotView.onDataChange(index, item, size, isCameraFolder, albumName);
		}
	};

	private void initializeViews() {
		mSelectionManager = new SelectionManager(mActivity, true);
		mSelectionManager.setSelectionListener(this);

		mConfig = Config.AlbumSetPage.get(mActivity);
        Config.TimeLinePage config = Config.TimeLinePage.get(mActivity);
		// / M: [FEATURE.MODIFY] fancy layout @{
		mTimeSlotView = new TimeLineSlotView(mActivity, config.slotViewSpec);
		mTimeSlotView.setSelectMode(new ISelectMode() {
			@Override
			public boolean isSelectMode() {
				return mSelectionManager.inSelectionMode();
			}

			@Override
			public boolean isSelectItem(int slotIndex) {
				int index = slotIndex;
				if (index >= mAlbumSetTimeAdapter.size()) {
					index = mAlbumSetTimeAdapter.size() - 1;
				}
				MediaItem item = mAlbumSetTimeAdapter.get(index);
				if (item == null)
					return false;
				return mSelectionManager.isSelected(item.getPath());
			}

			@Override
			public void slideControlSelect(boolean isAdd, int startSlotIndex, int endSlotIndex) {
				int fromSlotIndex = Math.min(startSlotIndex, endSlotIndex);
				int toSlotIndex = Math.max(startSlotIndex, endSlotIndex);
				ArrayList<MediaItem> items = mAlbumSetTimeAdapter.get(fromSlotIndex, toSlotIndex - fromSlotIndex + 1);
				mSelectionManager.slideControlSelect(!isSelectItem(startSlotIndex), items);
				mTimeSlotView.invalidate();
				mTimeSlotRenderer.updateAllTimelineTitle(false, -1, -1);
			}

			@Override
			public void slideControlStart() {
				mSelectionManager.slideControlStart();
			}

			@Override
			public void slideControlEnd() {
				mSelectionManager.slideControlEnd();
			}
		});
		mSetSlotView = new SlotView(mActivity, mConfig.slotViewSpec, FancyHelper.isFancyLayoutSupported(),true);
		if (FancyHelper.isFancyLayoutSupported()) {
			mSetSlotView.setPaddingSpec(mConfig.paddingTop, mConfig.paddingBottom);

		}
		// / @}

		mAllSlotRenderer = new AlbumSetSlotRenderer(mActivity, mSelectionManager,
				mSetSlotView, mConfig.labelSpec, mConfig.placeholderColor);
		mTimeSlotRenderer = new TimeLineSlotRenderer(mActivity, mTimeSlotView,
				mSelectionManager, config.labelSpec, config.placeholderColor);

		mTimeSlotView.setSlotRenderer(mTimeSlotRenderer);
		mSetSlotView.setSlotRenderer(mAllSlotRenderer);
		mTimeSlotView.setListener(new TimeLineSlotView.SimpleListener() {
			@Override
			public void onDown(int index) {
				AlbumSetPage.this.onDown(index, PAGE_TIME);
			}

			@Override
			public void onUp(boolean followedByLongPress) {
				AlbumSetPage.this.onUp(followedByLongPress, PAGE_TIME);
			}

			@Override
			public void onSingleTapUp(TimeLineSlotView.Slot slot) {
				AlbumSetPage.this.onSingleTapUp(slot, PAGE_TIME);
			}

			@Override
			public void onLongTap(TimeLineSlotView.Slot slot) {
				 AlbumSetPage.this.onLongTap(slot, PAGE_TIME);
			}
		});

		mSetSlotView.setListener(new SlotView.SimpleListener() {

			@Override
			public void onUp(boolean followedByLongPress) {
				AlbumSetPage.this.onUp(followedByLongPress, PAGE_ALL);
			}

			@Override
			public void onSingleTapUp(int index) {
				AlbumSetPage.this.onSingleTapUp(index);
			}

			@Override
			public void onScrollPositionChanged(int position, int total) {

			}

			@Override
			public void onLongTap(int index) {
				 AlbumSetPage.this.onLongTap(index);
			}

			@Override
			public void onDown(int index) {
				AlbumSetPage.this.onDown(index, PAGE_ALL);
			}
		});

		mActionModeHandler = new ActionModeHandler(mActivity, mSelectionManager);
		mActionModeHandler.setActionModeListener(new ActionModeHandler.ActionModeListener() {
			@Override
			public boolean onActionItemClicked(MenuItem item) {
				return onItemSelected(item);
			}

			// / M: [BEHAVIOR.ADD] @{
			public boolean onPopUpItemClicked(int itemId) {
				// return if restoreSelection has done
				return mRestoreSelectionDone;
			}
			// / @}

		});
		mRootPane.addComponent(mTimeSlotView);
		mRootPane.addComponent(mSetSlotView);
	}

    private MenuItem mSelectItem;
    private void updateMenuItem() {
        if (mSelectItem == null) {
            return;
        }
        if (mCurrentPage == PAGE_ALL) {
            mSelectItem.setTitle(R.string.select_album);
        } else {
            mSelectItem.setTitle(R.string.select_image);
        }
    }
	    
	@Override
	protected void onStateResult(int requestCode, int resultCode, Intent data) {
		// / M: [BUG.MARK] no need show toast @{
		/*
		 * if (data != null && data.getBooleanExtra(AlbumPage.KEY_EMPTY_ALBUM,
		 * false)) { showEmptyAlbumToast(Toast.LENGTH_SHORT); }
		 */
		// / @}
		switch (requestCode) {
			case REQUEST_DO_ANIMATION: {
				mTimeSlotView.startRisingAnimation();
				mSetSlotView.startRisingAnimation();
				break;
			}
		}
	}

	// / M: [BUG.MODIFY] @{
	/* private String getSelectedString() { */
	public String getSelectedString() {
		// / @}
		int count = mSelectionManager.getSelectedCount();

		if (count == 0) {
			return mActivity.getResources().getString(R.string.zero_select);
		} else {
			int string  = 0;

			if (mCurrentPage == PAGE_TIME) {
				string	= R.plurals.number_of_items_selected;
			} else {
				string	= R.plurals.number_of_albums_selected;
			}
			String format = mActivity.getResources().getQuantityString(string,
					count);
			return String.format(format, count);
		}
	}

	@Override
	public void onSelectionModeChange(int mode) {
		switch (mode) {
		case SelectionManager.ENTER_SELECTION_MODE: {
			mHandler.removeMessages(MSG_SHOW_BUTTOM);
			mHandler.sendEmptyMessageDelayed(MSG_SHOW_BUTTOM, 100);
			
			mActionModeHandler.startActionMode();
			/*if (mCurrentPage == PAGE_TIME) {
				mTimeSlotRenderer.updateAllTimelineTitle(true, -1);
			}*/
			if (mActivity instanceof GalleryActivity) {
				GalleryActivity galleryActivity = (GalleryActivity) mActivity;
				showTabView(galleryActivity.getTabView());
			}

			performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
			break;
		}
		case SelectionManager.LEAVE_SELECTION_MODE: {

			mActionModeHandler.finishActionMode();
			/*if (mCurrentPage == PAGE_TIME) {
				mTimeSlotRenderer.updateAllTimelineTitle(true);
			}*/
			if (mActivity instanceof GalleryActivity) {
				GalleryActivity galleryActivity = (GalleryActivity) mActivity;
				showTabView(galleryActivity.getTabView());
			}
			mHandler.removeMessages(MSG_HIDE_BUTTOM);
			mHandler.sendEmptyMessageDelayed(MSG_HIDE_BUTTOM, 100);
			mRootPane.invalidate();
			break;
		}
		// / M: [BEHAVIOR.ADD] @{
		// when click deselect all in menu, not leave selection mode
		case SelectionManager.DESELECT_ALL_MODE:
			/*if (mCurrentPage == PAGE_TIME) {
				mTimeSlotRenderer.updateAllTimelineTitle(false, -1);
			}*/
			if (mCurrentPage == PAGE_TIME) {
				//mTimeSlotRenderer.updateAllTimelineTitle(false, -1);
				mTimeSlotRenderer.updateAllTimelineSelect();
			}
			mHandler.removeMessages(MSG_SHOW_BUTTOM);
			mHandler.sendEmptyMessageDelayed(MSG_SHOW_BUTTOM, 100);
			mActionModeHandler.updateSupportedOperation();
			mRootPane.invalidate();
			break;
			// / @}
		case SelectionManager.SELECT_ALL_MODE: {
			if (mCurrentPage == PAGE_TIME) {
				//mTimeSlotRenderer.updateAllTimelineTitle(false, -1);
				mTimeSlotRenderer.updateAllTimelineSelect();
			}
			mHandler.removeMessages(MSG_SHOW_BUTTOM);
			mHandler.sendEmptyMessageDelayed(MSG_SHOW_BUTTOM, 100);
			mActionModeHandler.updateSupportedOperation();
			mRootPane.invalidate();
			break;
		}
		}
	}

	@Override
	public void onSelectionChange(Path path, boolean selected) {
		mHandler.removeMessages(MSG_SHOW_BUTTOM);
		mHandler.sendEmptyMessageDelayed(MSG_SHOW_BUTTOM, 100);
		
		mActionModeHandler.setTitle(getSelectedString());
		mActionModeHandler.updateSupportedOperation(path, selected);
	
	}

	private void hideDetails() {
		mShowDetails = false;
		mDetailsHelper.hide();
		mAllSlotRenderer.setHighlightItemPath(null);
		mTimeSlotRenderer.setHighlightItemPath(null);
		mTimeSlotView.invalidate();
		mSetSlotView.invalidate();
	}

	private void showDetails() {
		DetailsSource detailsSource = null;
		if (mCurrentPage == PAGE_ALL) {
			detailsSource = mDetailsSource;
		} else if (mCurrentPage == PAGE_TIME) {
			detailsSource = mTimeDetailsSource;
		}
		if (detailsSource != null) {
			mDetailsHelper = new DetailsHelper(mActivity, mRootPane, detailsSource);
			mDetailsHelper.setCloseListener(new CloseListener() {
				@Override
				public void onClose() {
					hideDetails();
				}
			});
			mShowDetails = true;
			mDetailsHelper.show();
		}
	}

	@Override
	public void onSyncDone(final MediaSet mediaSet, final int resultCode) {
		if (resultCode == MediaSet.SYNC_RESULT_ERROR) {
			LogUtil.d(TAG, "onSyncDone: " + Utils.maskDebugInfo(mediaSet.getName())
					+ " result=" + resultCode);
		}
		((Activity) mActivity).runOnUiThread(new Runnable() {
			@Override
			public void run() {
				GLRoot root = mActivity.getGLRoot();
				root.lockRenderThread();
				try {
					if (resultCode == MediaSet.SYNC_RESULT_SUCCESS) {
						mInitialSynced = true;
					}
					clearLoadingBit(BIT_LOADING_SYNC, PAGE_ALL);
					clearLoadingBit(BIT_LOADING_SYNC, PAGE_TIME);
					if (resultCode == MediaSet.SYNC_RESULT_ERROR && mIsActive) {
						LogUtil.w(TAG, "failed to load album set");
					}
				} finally {
					root.unlockRenderThread();
				}
			}
		});
	}

	private class MyLoadingListener implements LoadingListener {

		private int mPage;
		public MyLoadingListener(int page) {
			mPage = page;
		}

		@Override
		public void onLoadingStarted() {
			// / M: [PERF.ADD] for performance auto test@{
			mLoadingFinished = false;
			// / @}
			setLoadingBit(BIT_LOADING_RELOAD, mPage);
		}

		@Override
		public void onLoadingFinished(boolean loadingFailed) {
			// / M: [PERF.ADD] for performance auto test@{
			mLoadingFinished = true;
			// / @}
			clearLoadingBit(BIT_LOADING_RELOAD, mPage);
			// / M: [BEHAVIOR.ADD] @{
			// We have to notify SelectionManager about data change,
			// and this is the most proper place we could find till now
			boolean inSelectionMode = (mSelectionManager != null && mSelectionManager
					.inSelectionMode());
			int setCount = mMediaSet != null ? mMediaSet.getSubMediaSetCount()
					: 0;
			LogUtil.d(TAG, "<onLoadingFinished> set count=" + setCount);
			LogUtil.d(TAG, "<onLoadingFinished> inSelectionMode=" + inSelectionMode);
			mSelectionManager.onSourceContentChanged();
			boolean restore = false;
			if (setCount > 0 && inSelectionMode) {
				if (mNeedUpdateSelection) {
					mNeedUpdateSelection = false;
					restore = true;
					mSelectionManager.restoreSelection();
				}
				mActionModeHandler.updateSupportedOperation();
				mActionModeHandler.updateSelectionMenu();
			}
			if (!restore) {
				mRestoreSelectionDone = true;
			}
			// / @}
		}
	}
	
	private class MyTimeDetailsSource implements DetailsHelper.DetailsSource {
		
		private int mIndex;
		@Override
		public int size() {
			return mAlbumSetTimeAdapter.size() - mAlbumSetTimeAdapter.getTimeLineTitlesCount();
		}

		@Override
		public int setIndex() {
			if (mSelectionManager.getSelected(false) == null) return -1;
			Path id = mSelectionManager.getSelected(false).get(0);
			mIndex = mAlbumSetTimeAdapter.findItem(id);
			int indexToDisplay = mAlbumSetTimeAdapter.getIndex(id, false);
			return indexToDisplay;
		}

		@Override
		public MediaDetails getDetails() {
			// this relies on setIndex() being called beforehand
			if (mIndex < 0) return null;
			MediaObject item = mAlbumSetTimeAdapter.get(mIndex);
			if (item != null) {
				mTimeSlotRenderer.setHighlightItemPath(item.getPath());
				return item.getDetails();
			} else {
				return null;
			}
		}
	}
	
	private class MyDetailsSource implements DetailsHelper.DetailsSource {
		private int mIndex;

        @Override
        public int size() {
            return mAlbumSetDataAdapter.size();
        }

		@Override
		public int setIndex() {
			/*prize-xuchunming-20160311-solve monkey error-start*/
			if(mSelectionManager.getSelected(false).size() > 0){
				Path id = mSelectionManager.getSelected(false).get(0);
				mIndex = mAlbumSetDataAdapter.findSet(id);
			}else{
				mIndex = 0;
			}
			/*prize-xuchunming-20160311-solve monkey error-end*/
			return mIndex;
		}

		@Override
		public MediaDetails getDetails() {
			MediaObject item = mAlbumSetDataAdapter.getMediaSet(mIndex);
			if (item != null) {
				mAllSlotRenderer.setHighlightItemPath(item.getPath());
				return item.getDetails();
			} else {
				return null;
			}
		}
	}

	// ********************************************************************
	// * MTK *
	// ********************************************************************

	// Flag to specify whether mSelectionManager.restoreSelection task has done
	private boolean mRestoreSelectionDone;
	// Save selection for onPause/onResume
	private boolean mNeedUpdateSelection = false;
	// If restore selection not done in selection mode,
	// after click one slot, show 'wait' toast
	private Toast mWaitToast = null;

	// / M: [BUG.ADD] leave selection mode when plug out sdcard @{
	@Override
	public void onEjectSdcard() {
		if (mSelectionManager != null && mSelectionManager.inSelectionMode()) {
			LogUtil.i(TAG, "<onEjectSdcard> leaveSelectionMode");
			mSelectionManager.leaveSelectionMode();
		}
	}

	// / @}

	public void onSelectionRestoreDone() {
		if (!mIsActive)
			return;
		mRestoreSelectionDone = true;
		// Update selection menu after restore done @{
		mActionModeHandler.updateSupportedOperation();
		mActionModeHandler.updateSelectionMenu();
		//prize-wuliang-20180411 bug 55021 update pic data, bottom controls refresh
		if(mBottomControls != null && mSelectionManager != null && mSelectionManager.inSelectionMode()){
			Log.d(TAG, "onSelectionRestoreDone bottom controls refresh");
			mBottomControls.refresh();
		}
	}

	// / M: [FEATURE.ADD] fancy layout @{
	private int mLayoutType = -1;

	private DisplayMetrics getDisplayMetrics() {
		WindowManager wm = (WindowManager) mActivity
				.getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics reMetrics = new DisplayMetrics();
		wm.getDefaultDisplay().getRealMetrics(reMetrics);
		Log.i(TAG, "<getDisplayMetrix> <Fancy> Display Metrics: "
				+ reMetrics.widthPixels + " x " + reMetrics.heightPixels);
		return reMetrics;
	}

	// / @}
	public void initRootPan(AbstractGalleryActivity activity) {
		mRootPane = new PrizeRootGLView(activity) {
			private final float mMatrix[] = new float[16];

			@Override
			protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
				mEyePosition.resetPosition();
				LogUtil.i(TAG, "<onLayout> PrizeRootGLView left=" + left + " top=" + top + " right=" + right + " bottom=" + bottom);
				int slotViewTop = mConfig.paddingTop;
				int slotViewBottom = bottom - top - mTabViewHeight;
				int slotViewRight = right - left;
				LogUtil.i(TAG, "<onLayout> PrizeRootGLView slotViewTop=" + slotViewTop + " slotViewBottom=" + slotViewBottom + " slotViewRight=" + slotViewRight);
				if (mShowDetails) {
					mDetailsHelper.layout(left, slotViewTop, right, bottom);
				} else {
					mAllSlotRenderer.setHighlightItemPath(null);
				}
				int pageSize = getComponentCount();
				GLView view = null;
				if (pageSize > 1) {
					view = getComponent(0);
					if (view != null) {
						view.layout(-slotViewRight, slotViewTop, 0, slotViewBottom);
					}
					view = getComponent(1);
					if (view != null) {
						view.layout(0, slotViewTop + DensityUtil.dip2px(mActivity, 18), slotViewRight, slotViewBottom);
					}
					this.setScrollInit(mCurrentPage);
				} else if (pageSize > 0) {
					view = getComponent(0);
					if (view != null) {
						view.layout(0, slotViewTop, slotViewRight, slotViewBottom);
					}
				}
			}
		};
	}

	@Override
	public boolean canDisplayBottomControls() {
		return mSelectionManager.inSelectionMode();
	}

	@Override
	public boolean canDisplayBottomControl(int control) {
		if(mSelectionManager!=null&& mSelectionManager.getSelectedCount()==0){
			return false;
		}
		switch(control){
		case R.id.view_delete:
//			if(!((mAlbumSetPageOpearation & MediaObject.SUPPORT_DELETE)!= 0)){
//				return false;
//			}
			return true;
		case R.id.view_share:
//			if(!((mAlbumSetPageOpearation & MediaObject.SUPPORT_SHARE)!= 0)){
//				return false;
//			}
			return true;
		case R.id.view_set_to:
			if(mSelectionManager!=null&& mSelectionManager.getSelectedCount()==1){
				if(mCurrentPage!=1){
					if(mSelectionManager.getSelected(false).size()>0&&mSelectionManager.getSelected(false).get(0).toString().contains("/local/video/"))
						return false;
				}
				return true;
			}
			return false;
		
		case R.id.view_details:
			if(mSelectionManager!=null&& mSelectionManager.getSelectedCount()==1){
				return true;
			}
			return false;
		}
		return false;
	}
	
	public void deleteAlbum(){
		Log.i("LCWTEST", "<deleteAlbum> deleteAlbum");
    	String confirmMsg = mActivity.getResources().getQuantityString(
                R.plurals.delete_selection, 1);
        new AlertDialog.Builder(mActivity.getAndroidContext())
        .setMessage(confirmMsg)
        .setPositiveButton(R.string.ok, new AlertDialog.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				mActionModeHandler.setActionEventChange(R.id.action_delete);
			}
		})
        .setNegativeButton(R.string.cancel, new AlertDialog.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		}).create().show();
        
		
	}

    private DialogBottomMenu mBottomMenuDeleteDialog;
    private DialogItemOnClickListener mDeleteDialogOnClickListener = new DialogItemOnClickListener() {
        @Override
        public void onClickMenuItem(View v, int item_index, String item) {
            if (item.equals(mActivity.getResources().getQuantityString(R.plurals.delete_selection, 1))) {
				mActionModeHandler.setActionEventChange(R.id.action_delete);
            }
        }
    };

    public void showDeleteMenuWindow() {
        Resources res = mActivity.getResources();
        ArrayList<String> menuItemIds = new ArrayList<String>();
        menuItemIds.add(res.getQuantityString(R.plurals.delete_selection, 1));
        mBottomMenuDeleteDialog = new DialogBottomMenu(mActivity, null,
                R.layout.dialog_delete_menu_item);
        mBottomMenuDeleteDialog.setMenuItem(menuItemIds);
        mBottomMenuDeleteDialog.setMenuItemOnClickListener(mDeleteDialogOnClickListener);
        mBottomMenuDeleteDialog.show();
    }

	@Override
	public void onBottomControlClicked(int control) {
		switch(control){
		case R.id.view_delete:
			showDeleteMenuWindow();
			//deleteAlbum();
			break;
		case R.id.view_share:
			mActionModeHandler.setShareOnClick();
			break;
		case R.id.view_set_to:
			mActionModeHandler.setActionEventChange(R.id.action_setas);
			break;
	
		case R.id.view_details:
			if (mAlbumSetDataAdapter.size() != 0) {
				if (mShowDetails) {
					hideDetails();
				} else {
					showDetails();
				}
			}
			break;
		}
	}

	@Override
	public void refreshBottomControlsWhenReady() {
		if (mBottomControls == null) {
			return;
		}
	}
	
	@Override
	public boolean getBottomIsFile() {
		// TODO Auto-generated method stub
		if(mCurrentPage == 1){
			return true;
		}
		return false;
	}
	
	/// M: [BUG.ADD] Save dataManager object.
    @Override
    protected void onSaveState(Bundle outState) {
        // keep record of current DataManager object.
        String dataManager = mActivity.getDataManager().toString();
        String processId = String.valueOf(android.os.Process.myPid());
        outState.putString(KEY_DATA_OBJECT, dataManager);
        outState.putString(KEY_PROCESS_ID, processId);
		outState.putInt(KEY_PAGE_ID, mCurrentPage);
		LogUtil.i(TAG, "<onSaveState> dataManager = " + dataManager
                + ", processId = " + processId + " mCurrentPage=" + mCurrentPage);
    }
    /// @}

    /// M: [PERF.ADD] add for delete many files performance improve @{
    @Override
    public void setProviderSensive(boolean isProviderSensive) {
        mAlbumSetDataAdapter.setSourceSensive(isProviderSensive);
    }
    @Override
    public void fakeProviderChange() {
        mAlbumSetDataAdapter.fakeSourceChange();
    }
    /// @}

    /// M: [FEATURE.ADD] [Runtime permission] @{
    private int mNeedDoClusterType = 0;

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
            int[] grantResults) {
        if (PermissionHelper.isAllPermissionsGranted(permissions, grantResults)) {
            doCluster(mNeedDoClusterType);
        } else {
            PermissionHelper.showDeniedPromptIfNeeded(mActivity,
                    Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }
    /// @}

    /// M: [FEATURE.ADD] Multi-window. @{
    private boolean mIsInMultiWindowMode = false;
    private MultiWindowListener mMultiWindowListener = new MultiWindowListener();

    /**
     * Use MultiWindowListener to monitor entering or leaving multi-window.
     */
    private class MultiWindowListener implements
            AbstractGalleryActivity.MultiWindowModeListener {

        @Override
        public void onMultiWindowModeChanged(boolean isInMultiWindowMode) {
            if (mIsInMultiWindowMode == isInMultiWindowMode) {
                return;
            }
            mRootPane.lockRendering();
            Log.d(TAG, "<onMultiWindowModeChanged> isInMultiWindowMode: "
                    + isInMultiWindowMode);
            mIsInMultiWindowMode = isInMultiWindowMode;
            /*if (mIsInMultiWindowMode) {
                Log.d(TAG, "<onMultiWindowModeChanged> switch to MULTI_WINDOW_LAYOUT");
                mLayoutType = FancyHelper.MULTI_WINDOW_LAYOUT;
//                mAlbumSetView.onEyePositionChanged(mLayoutType);
                mAllSlotRenderer.onEyePositionChanged(mLayoutType);
//                mSlotView.switchLayout(mLayoutType);
                mTimeSlotView.switchLayout(mLayoutType);
				mSetSlotView.switchLayout(mLayoutType);
				mLocalSlotView.switchLayout(mLayoutType);
            } else {
                Log.d(TAG, "<onMultiWindowModeChanged> <Fancy> enter");
                DisplayMetrics reMetrics = getDisplayMetrics();
                FancyHelper.doFancyInitialization(reMetrics.widthPixels,
                        reMetrics.heightPixels);
                mLayoutType = mEyePosition.getLayoutType();
                Log.d(TAG, "<onMultiWindowModeChanged> <Fancy> begin to switchLayout");
//              mAlbumSetView.onEyePositionChanged(mLayoutType);
                mAllSlotRenderer.onEyePositionChanged(mLayoutType);
//                mSlotView.switchLayout(mLayoutType);
                mTimeSlotView.switchLayout(mLayoutType);
				mSetSlotView.switchLayout(mLayoutType);
				mLocalSlotView.switchLayout(mLayoutType);
            }*/
            mRootPane.unlockRendering();
			updateEmptyView(mCurrentPage);
		}
    }
    /// @}
}
