/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein is
 * confidential and proprietary to MediaTek Inc. and/or its licensors. Without
 * the prior written permission of MediaTek inc. and/or its licensors, any
 * reproduction, modification, use or disclosure of MediaTek Software, and
 * information contained herein, in whole or in part, shall be strictly
 * prohibited.
 *
 * MediaTek Inc. (C) 2014. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER
 * ON AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL
 * WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NONINFRINGEMENT. NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH
 * RESPECT TO THE SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY,
 * INCORPORATED IN, OR SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES
 * TO LOOK ONLY TO SUCH THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO.
 * RECEIVER EXPRESSLY ACKNOWLEDGES THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO
 * OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES CONTAINED IN MEDIATEK
 * SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE
 * RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S
 * ENTIRE AND CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE
 * RELEASED HEREUNDER WILL BE, AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE
 * MEDIATEK SOFTWARE AT ISSUE, OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE
 * CHARGE PAID BY RECEIVER TO MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek
 * Software") have been modified by MediaTek Inc. All revisions are subject to
 * any receiver's applicable license agreements with MediaTek Inc.
 */
package com.android.camera.manager;

import android.R.bool;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.view.View.OnClickListener;

import com.android.camera.CameraActivity;
import com.android.camera.FeatureSwitcher;
import com.android.camera.FileSaver;
import com.android.camera.Log;
import com.android.camera.R;
import com.android.camera.SaveRequest;
import com.android.camera.Storage;
import com.android.camera.Thumbnail;
import com.android.camera.Util;
import com.android.camera.ui.RotateImageView;
import com.android.prize.CreateCirleBitmap;
import com.mediatek.camera.platform.ICameraAppUi.ViewState;
import com.mediatek.camera.util.CameraAnimation;
import com.prize.setting.NavigationBarUtils;

import java.io.File;

//TODO: we should do decoupling to move it into mediatek/ui
public class ThumbnailViewManager extends ViewManager implements OnClickListener,
        FileSaver.FileSaverListener,FileSaver.FileSaverStartListener, CameraActivity.Resumable {
    private static final String TAG = "ThumbnailViewManager";

    private static final String ACTION_UPDATE_PICTURE =
            "com.android.gallery3d.action.UPDATE_PICTURE";
    private static final String ACTION_IPO_SHUTDOWN = "android.intent.action.ACTION_SHUTDOWN_IPO";

    private static final int MSG_SAVE_THUMBNAIL = 0;
    private static final int MSG_UPDATE_THUMBNAIL = 1;
    private static final int MSG_CHECK_THUMBNAIL = 2;
    private static final int MSG_RELEASE_URI = 3;
    private static final int MSG_UPDATE_URI = 4;

    // TODO:dependency with Google packages,should change the code when do
    // decoupling
    // An image view showing the last captured picture thumbnail.
    private RotateImageView mThumbnailView;
    public  RotateImageView mPreviewThumb;
    private View mThumbnailContain;
    private SaveRequest mLastSaveRequest;
    private SaveRequest mCurrentSaveRequest;
    private Thumbnail mThumbnail;
    //
    private AsyncTask<Void, Void, Thumbnail> mLoadThumbnailTask;
    private WorkerHandler mWorkerHandler;
    private CameraAnimation mCameraAnimation;
    private Object mLock = new Object();

    private long mRefreshInterval = 0;
    private long mLastRefreshTime;
    private long mCshotThumBegin = 0;

    private boolean mResumed;
    private boolean mIsSavingThumbnail;
    private boolean mUpdateThumbnailDelayed;
    private boolean mIsUpdatingThumbnail;
    private boolean isNeedRefreshThumbnailView = true;
    private IntentFilter mUpdatePictureFilter =
            new IntentFilter(ACTION_UPDATE_PICTURE);
    /*prize-xuchunming-20180411-bugid:50825-start*/
    private CameraActivity mActivity;
    /*prize-xuchunming-20180411-bugid:50825-end*/
    //this interface just used for when animation end,will call updatethumbnail view
    public interface AnimationEndListener {
        void onAnianmationEnd();
    }

    public AnimationEndListener mListener = new AnimationEndListener() {
        @Override
        public void onAnianmationEnd() {
            Log.i(TAG, "[onAnianmationEnd]");
            mIsUpdatingThumbnail = false;
            updateThumbnailView();
        }
    };

    private BroadcastReceiver mUpdatePictureReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "mDeletePictureReceiver.onReceive(" + intent + ")");
            // why need add :getContext().isActivityOnpause() ?
            // CS -> onPause()->entry gallery to delete the CS picture,
            // so this time ,isShowing() & isFullScreen() is true,
            // but this time we don't need update the Thumbnail
            if (isShowing() && getContext().isFullScreen() && !getContext().isActivityOnpause()) {
                getLastThumbnailUncached();
            } else {
                mUpdateThumbnailDelayed = true;
            }
        }
    };

    private IntentFilter mIpoShutdownFilter = new IntentFilter(ACTION_IPO_SHUTDOWN);
    private BroadcastReceiver mIpoShutdownReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "[onReceive]intent = " + intent);
            saveThumbnailToFile();
        }
    };

    public ThumbnailViewManager(CameraActivity context) {
    	super(context, VIEW_LAYER_THUMBNAIL);
        Log.d(TAG, "[ThumbnailViewManager]new...");
        /*prize-xuchunming-20180411-bugid:50825-start*/
        mActivity = context;
        /*prize-xuchunming-20180411-bugid:50825-end*/
        setFileter(false);
        context.addResumable(this);
        mCameraAnimation = new CameraAnimation();
    }
    
    public ThumbnailViewManager(CameraActivity context, int group) {
        super(context,group);
        Log.d(TAG, "[ThumbnailViewManager]new...  ");
        /*prize-xuchunming-20180411-bugid:50825-start*/
        mActivity = context;
        /*prize-xuchunming-20180411-bugid:50825-end*/
        setFileter(false);
        context.addResumable(this);
        //context.addOnFullScreenChangedListener(this);
        mCameraAnimation = new CameraAnimation();
    }
    
    @Override
    public void begin() {
        Log.i(TAG, "[begin]...");
        if (mWorkerHandler == null) {
            HandlerThread t = new HandlerThread("thumbnail-creation-thread");
            t.start();
            mWorkerHandler = new WorkerHandler(t.getLooper());
        }
        // move register broadcast from resume to begin, since when Gallery
        // start new activity
        // to edit, will not receive this broadcast. And LocalBroadcastManager
        // use inside process
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(getContext());
        manager.registerReceiver(mUpdatePictureReceiver, mUpdatePictureFilter);
    }

    @Override
    public void resume() {
        Log.i(TAG, "[resume]mResumed = " + mResumed);
        if (!mResumed) {
            getContext().registerReceiver(mIpoShutdownReceiver, mIpoShutdownFilter);
            if (isShowing() && !mIsSavingThumbnail && !getContext().isSecureCamera()) {
                // if the ThumbnailView is not showed, do not get last
                // thumbnail.
                getLastThumbnail();
            }
            mResumed = true;
        }
    }

    @Override
    public void pause() {
        Log.i(TAG, "[pause]mResumed =" + mResumed);
        if (mResumed) {
            getContext().unregisterReceiver(mIpoShutdownReceiver);
            cancelLoadThumbnail();
            saveThumbnailToFile();
            mResumed = false;
        }
        mWorkerHandler.sendEmptyMessage(MSG_RELEASE_URI);
        /*prize-xuchunming-20180503-bugid:57006-start*/
        if(mPreviewThumb != null) {
        	mPreviewThumb.setVisibility(View.INVISIBLE);
        }
        /*prize-xuchunming-20180503-bugid:57006-end*/
    }

    @Override
    public void setEnabled(boolean enabled) {
        Log.d(TAG, "[setEnabled]enabled = " + enabled);
        super.setEnabled(enabled);
        if (mThumbnailView != null) {
            mThumbnailView.setEnabled(enabled);
            mThumbnailView.setClickable(enabled);
        }
    }

    @Override
    public void finish() {
        Log.i(TAG, "[finish]...");
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(getContext());
        manager.unregisterReceiver(mUpdatePictureReceiver);
        if (mWorkerHandler != null) {
            mWorkerHandler.getLooper().quit();
        }
    }

    @Override
    protected View getView() {
        View view = inflate(R.layout.thumbnail);
        if(view!=null){
            mThumbnailView = (RotateImageView) view.findViewById(R.id.thumbnail);
            mThumbnailView.setOnClickListener(this);
            mPreviewThumb = (RotateImageView) view.findViewById(R.id.preview_thumb);
        }else{
			 Log.i(TAG, "getView == null");
		}
        mThumbnailContain = view.findViewById(R.id.thumbnailContain);
        //NavigationBarUtils.adpaterNavigationBar(getContext(), mThumbnailContain);
        return view;
    }

    @Override
    protected void onRefresh() {
        Log.i(TAG, "[onRefresh]...");
        updateThumbnailView();
        onChangeNavigationBar(NavigationBarUtils.isShowNavigationBar(getContext()));
    }

    @Override
    public void onFileSaved(SaveRequest request) {
        Log.i(TAG, "[onFileSaved]...");
        // If current URI is not valid, don't create thumbnail.
        if(!request.isIgnoreThumbnail() && request.getUri() != null) {
        	/*prize-xuchunming-20171018-change picturezoom UI interaction-start*/
        	mCurrentSaveRequest = request;
        	Log.d(TAG, "[onFileSaved] cancel. uri: " + mCurrentSaveRequest.getUri()+", FilePath"+mCurrentSaveRequest.getFilePath());
        	/*prize-xuchunming-20171018-change picturezoom UI interaction-end*/
        	mWorkerHandler.removeMessages(MSG_UPDATE_URI);
            mWorkerHandler.sendEmptyMessage(MSG_UPDATE_URI);
        }
        
    }
    
    @Override
	public void onFileSaveStart(SaveRequest request) {
		// TODO Auto-generated method stub
    	/*prize-xuchunming-20180411-bugid:50825-start*/
    	Log.d(TAG, "onFileSaveStart: camera_capturing"+Settings.System.getInt(mActivity.getContentResolver(), "camera_capturing", 0));
    	if(Settings.System.getInt(mActivity.getContentResolver(), "camera_capturing", 0) == 0){
	    	if (!request.isIgnoreThumbnail() && mThumbnailView != null) {
	            Log.i(TAG, "[onFileSaveStart],send MSG_SAVE_THUMBNAIL.");
	            cancelLoadThumbnail();
	            int thumbnailWidth = mThumbnailView.getLayoutParams().width;
	            Thumbnail thumb = request.createThumbnail(thumbnailWidth);
	            if (thumb != null) { // just update when thumbnail valid
	                mThumbnail = thumb;
	            } else {
	                Log.w(TAG, "[handleMessage]WorkerHandler,thumb is null!");
	                return ;
	            }
	            mWorkerHandler.removeMessages(MSG_SAVE_THUMBNAIL);
	            mWorkerHandler.sendEmptyMessage(MSG_SAVE_THUMBNAIL);
	        }
    	}
    	/*prize-xuchunming-20180411-bugid:50825-end*/
	}

    @Override
    public void onClick(View v) {
        if (getContext().isFullScreen() && getContext().isCameraIdle() && mThumbnail != null) {
            Uri uri = getThumbnailUri();
            if (uri != null && uri.getPath() != null
                    && uri.getPath().isEmpty()) {
                Log.d(TAG, "[onClick] cancel. uri: " + uri + ", UriPath: "
                        + uri.getPath());
                return;
            }
            Log.i(TAG, "[onClick]call gotoGallery.");
            getContext().gotoGallery();
        }
    }
    public void forceUpdate() {
        Log.d(TAG, "[forceUpdate]...");
        // when MediaScanner Scan done, we should get thumbnail form Media Store
        getLastThumbnailUncached();
    }

    public Uri getThumbnailUri() {
        if (mCurrentSaveRequest != null && mCurrentSaveRequest.getUri() != null) {
            return mCurrentSaveRequest.getUri();
        } else {
            return Uri.parse("file://" + mThumbnail.getFilePath());
        }
    }

    public String getThumbnailMimeType() {
        if (mCurrentSaveRequest != null) {
            return getMimeType(mCurrentSaveRequest.getFilePath());
        } else {
            return getMimeType(mThumbnail.getFilePath());
        }
    }

    private String getMimeType(String filePath) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        String mime = "image/jpeg";
        if (filePath != null) {
            try {
                retriever.setDataSource(filePath);
                mime = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE);
            } catch (IllegalStateException e) {
                return mime;
            } catch (IllegalArgumentException e) {
                return mime;
            } catch (RuntimeException e) {
                return mime;
            }
        }
        Log.i(TAG, "[getMimeType] mime = " + mime);
        return mime;
    }

    public void addFileSaver(FileSaver saver) {
        if (saver != null) {
            saver.addListener(this);
            saver.addSaveStartListener(this);
        }
    }

    public void removeFileSaver(FileSaver saver) {
        if (saver != null) {
            saver.removeListener(this);
            saver.removeSaveStartListener(this);
        }
    }

    public void setRefreshInterval(int ms) {
        mRefreshInterval = ms;
        mLastRefreshTime = System.currentTimeMillis();
    }

    public void updateThumbnailView() {
        Log.i(TAG, "[updateThumbnailView]this = " + this
                + ", mIsUpdatingThumbnail    = " + mIsUpdatingThumbnail+",isNeedRefreshThumbnailView: "+isNeedRefreshThumbnailView+",mThumbnail: "+mThumbnail);
        if (mThumbnailView != null && !mIsUpdatingThumbnail) {
            if (super.isShowing()) {
                Log.i(TAG, "[updateThumbnailView]showing is true");
                if (mThumbnail != null && mThumbnail.getBitmap() != null) {
                    // here set bitmap null to avoid show last thumbnail in a
                    // moment.
                    Log.i(TAG, "[updateThumbnailView]showing is true,set VISIBLE.");
                   // mThumbnailView.setBitmap(mThumbnail.getBitmap());
                    mThumbnailView.setBitmap(CreateCirleBitmap.getCircleBitmap(mThumbnail.getBitmap(),(int)getContext().getResources().getDimension(R.dimen.thumbnail_image_size)));
                    mThumbnailView.setVisibility(View.VISIBLE);
                } else {
                	/*prize-modify-bugid:52062 ThumbnailView flicker on touch autofocus -xiaoping-20180330-start*/
					if (isNeedRefreshThumbnailView) {
						/*prize-xuchunming-20180503-bugid:57006-start*/
						if(mPreviewThumb != null) {
				        	Log.d(TAG, "mPreviewThumb.getVisibility:"+mPreviewThumb.getVisibility());
				        }
						/*prize-xuchunming-20180503-bugid:57006-end*/
						mThumbnailView.setBitmap(resToBitmap(R.drawable.prize_thumb_no_camera_pricture));
						mThumbnailView.setVisibility(View.VISIBLE);
						isNeedRefreshThumbnailView = false;
						Log.i(TAG, "[updateThumbnailView]thumbnail is null,set INVISIBLE!");
					}
					/*prize-modify-bugid:52062 ThumbnailView flicker on touch autofocus -xiaoping-20180330-end*/
                }
            } else {
                Log.i(TAG, "[updateThumbnailView]showing is false,set INVISIBLE.");
                mThumbnailView.setBitmap(resToBitmap(R.drawable.prize_thumb_no_camera_pricture));
                mThumbnailView.setVisibility(View.VISIBLE);
            }
        }
    };
    
    private Bitmap resToBitmap(int resId) {
    	BitmapDrawable bd = (BitmapDrawable) getContext().getResources().getDrawable(resId);
		Bitmap bitmap = bd.getBitmap();
		return bitmap;
    }
    
    private class LoadThumbnailTask extends AsyncTask<Void, Void, Thumbnail> {
        private boolean mLookAtCache;

        public LoadThumbnailTask(boolean lookAtCache) {
            mLookAtCache = lookAtCache;
        }

        @Override
        protected Thumbnail doInBackground(Void... params) {
            Log.i(TAG, "[doInBackground]begin.mLookAtCache = " + mLookAtCache);
            // Load the thumbnail from the file.
            try {
                ContentResolver resolver = getContext().getContentResolver();
                Thumbnail t = null;
                if (mLookAtCache) {
                    t = Thumbnail.getLastThumbnailFromFile(getContext()
                            .getFilesDir(), resolver);
                }
                if (isCancelled()) {
                    Log.w(TAG, "[doInBackground]task is cancel,return.");
                    return null;
                }
                if (t == null && Storage.isStorageReady()) {
                    Thumbnail result[] = new Thumbnail[1];
                    // Load the thumbnail from the media provider.
                    int code = Thumbnail.getLastThumbnailFromContentResolver(
                            resolver, result, mThumbnail);
                    Log.d(TAG, "getLastThumbnailFromContentResolver code = "
                            + code);
                    switch (code) {
                    case Thumbnail.THUMBNAIL_FOUND:
                        return result[0];
                    case Thumbnail.THUMBNAIL_NOT_FOUND:
                        return null;
                    case Thumbnail.THUMBNAIL_DELETED:
                        // in secure camera, if getContext().getSecureAlbumCount() <= 0,
                        // should continuous to do onPostExecute().
                        if (getContext().isSecureCamera()
                                && getContext().getSecureAlbumCount() <= 0) {
                            return null;
                        }
                        cancel(true);
                        return null;
                    default:
                        return null;
                    }
                }
                return t;
            } catch (Exception ex) {
                ex.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Thumbnail thumbnail) {
            Log.d(TAG, "[onPostExecute]isCancelled()="
                    + isCancelled());
            if (isCancelled()) {
                return;
            }
            // in secure camera, if getContext().getMediaItemCount() <= 0,
            // there is no need to get thumbnail and should invisible thumbnail
            // view
            if (getContext().isSecureCamera() && getContext().getSecureAlbumCount() <= 0) {
                mThumbnail = null;
            } else {
                mThumbnail = thumbnail;
            }
            /*prize-modify-bugid:52062 ThumbnailView flicker on touch autofocus -xiaoping-20180330-start*/
            if (mThumbnail == null) {
				isNeedRefreshThumbnailView = true;
			} else if (mThumbnail.getBitmap() == null) {
				isNeedRefreshThumbnailView = true;
			}
            /*prize-modify-bugid:52062 ThumbnailView flicker on touch autofocus -xiaoping-20180330-end*/
            updateThumbnailView();
        }
    }

    private void getLastThumbnail() {
        Log.d(TAG, "[getLastThumbnail]...");
        mLoadThumbnailTask = new LoadThumbnailTask(true).execute();
    }

    private void getLastThumbnailUncached() {
        Log.d(TAG, "[getLastThumbnailUncached]...");
        cancelLoadThumbnail();
        mLoadThumbnailTask = new LoadThumbnailTask(false).execute();
    }

    private void saveThumbnailToFile() {
        Log.d(TAG, "[saveThumbnailToFile]...");
        if (mThumbnail != null && !mThumbnail.fromFile() && mThumbnail.getUri() != null) {
            Log.d(TAG, "[saveThumbnailToFile]execute...");
            new SaveThumbnailTask().execute(mThumbnail);
        }
    }

    private class SaveThumbnailTask extends AsyncTask<Thumbnail, Void, Void> {
        @Override
        protected Void doInBackground(Thumbnail... params) {
            final int n = params.length;
            Log.d(TAG, "[doInBackground]length = " + n);
            final File filesDir = getContext().getFilesDir();
            for (int i = 0; i < n; i++) {
                params[i].saveLastThumbnailToFile(filesDir);
            }
            return null;
        }
    }

    private void sendUpdateThumbnail() {
        Log.d(TAG, "[sendUpdateThumbnail]...");
        mIsUpdatingThumbnail = true;
        mMainHandler.removeMessages(MSG_UPDATE_THUMBNAIL);
        Message msg = mMainHandler.obtainMessage(MSG_UPDATE_THUMBNAIL, mThumbnail);
        msg.sendToTarget();
    }

    private class WorkerHandler extends Handler {
        public WorkerHandler(Looper looper) {
            super(looper);
            Log.i(TAG, "[WorkerHandler]new...");
        }

        @Override
        public void handleMessage(Message msg) {
            Log.i(TAG, "[handleMessage]WorkerHandler,msg.what = " + msg.what);
            long now = System.currentTimeMillis();
            switch (msg.what) {
            case MSG_SAVE_THUMBNAIL:
                mIsSavingThumbnail = true;
                SaveRequest curRequest = mCurrentSaveRequest;
                // M: initialize the ThumbnaiView to create thumbnail when
                // camera
                // launched by 3rd apps.@{
                if (mThumbnailView == null) {
                    getView();
                }
                // @}
                /*prize-xuchunming-20171018-change picturezoom UI interaction-start*/
                if (mThumbnailView != null) {
               /*prize-xuchunming-20171018-change picturezoom UI interaction-end*/
                    if (mRefreshInterval != 0 && (now - mLastRefreshTime < mRefreshInterval)) {
                        Log.d(TAG, "[handleMessage]WorkerHandler, sendEmptyMessageDelayed.");
                        long delay = mRefreshInterval - (now - mLastRefreshTime);
                        sendEmptyMessageDelayed(MSG_SAVE_THUMBNAIL, delay);
                    } else {
                        mLastRefreshTime = now;
                        /*int thumbnailWidth = mThumbnailView.getLayoutParams().width;
                        Thumbnail thumb = curRequest.createThumbnail(thumbnailWidth);
                        if (thumb != null) { // just update when thumbnail valid
                            mThumbnail = thumb;
                        } else {
                            Log.w(TAG, "[handleMessage]WorkerHandler,thumb is null!");
                        }*/
                         Log.i(TAG, "[handleMessage]WorkerHandler,mResumed = " + mResumed);
                        if (!mResumed) {
                            // Here save thumbnail to cache, so after resumed,
                            // thumbnail will be right.
                            saveThumbnailToFile();
                        } else {
                            sendUpdateThumbnail();
                        }
                    }
                }
                mIsSavingThumbnail = false;
                break;

            case MSG_CHECK_THUMBNAIL:
                if (mThumbnail != null) {
                    boolean valid = Util.isUriValid(mThumbnail.getUri(), getContext()
                            .getContentResolver());
                    Log.d(TAG, "[handleMessage]WorkerHandler,valid = " + valid);
                    if (!valid) {
                        getLastThumbnailUncached();
                    }
                }
                break;
            case MSG_RELEASE_URI:
                if (mCurrentSaveRequest != null) {
                    mCurrentSaveRequest.releaseUri();
                }
                break;
            case MSG_UPDATE_URI:
            	if(mThumbnail != null && mCurrentSaveRequest != null) {
            		mThumbnail.setUri(mCurrentSaveRequest.getUri());
            		/*prize-xuchunming-20180427-bugid:56603-start*/
            		mThumbnail.setFilePath(mCurrentSaveRequest.getFilePath());
            		/*prize-xuchunming-20180427-bugid:56603-end*/
            	}
            	 /*prize-xuchunming-20180411-bugid:50825-start*/
                //if(((CameraActivity)mActivity).isPostviewCapture()){
                Settings.System.putInt(mActivity.getContentResolver(), "camera_capturing", 0);
                //}
                /*prize-xuchunming-20180411-bugid:50825-end*/
            default:
                break;
            }
        }
    }

    private void cancelLoadThumbnail() {
        synchronized (mLock) {
            if (mLoadThumbnailTask != null) {
                Log.d(TAG, "[cancelLoadThumbnail]...");
                mLoadThumbnailTask.cancel(true);
                mLoadThumbnailTask = null;
            }
        }
    }

    private Handler mMainHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (mThumbnail == null) {
                Log.w(TAG, "[handleMessage]mMainHandler,mThumbnail is null,return!");
                return;
            }
            Log.i(TAG, "[handleMessage]mMainHandler,msg.what = " + msg.what);
            switch (msg.what) {
            case MSG_UPDATE_THUMBNAIL:
            	/*prize-xuchunming-20171018-change picturezoom UI interaction-start*/
            	boolean isContinuousRequest = false;
            	if(mCurrentSaveRequest != null) {
            		isContinuousRequest = mCurrentSaveRequest.isContinuousRequest();
            	}
                if (!isContinuousRequest) {
                /*prize-xuchunming-20171018-change picturezoom UI interaction-end*/
                    // here set bitmap null to avoid show last thumbnail in a
                    // moment.
                    // M:Add for CMCC capture performance test case
                    Log.i(TAG, "[CMCC Performance test][Camera][Camera] camera capture end ["
                    + System.currentTimeMillis() + "]");
                    Log.d(TAG, "[handleMessage]doCaptureAnimation.");
                    mPreviewThumb.setBitmap(null);
                    mPreviewThumb.setBitmap(CreateCirleBitmap.getCircleBitmap(mThumbnail.getBitmap(),(int)getContext().getResources().getDimension(R.dimen.thumbnail_image_size)));
                     mCameraAnimation.doCaptureAnimation(mPreviewThumb,
                     getContext(), mListener);
                    } else {
                        long currentTime = System.currentTimeMillis();
                        if ((currentTime - mCshotThumBegin) >= mCameraAnimation.SLIDE_DURATION) {
                            // here set bitmap null to avoid show last thumbnail in
                            // a moment.
                            Log.d(TAG, "[handleMessage]is continuous resquest.");
                            mPreviewThumb.setBitmap(null);
                        mPreviewThumb.setBitmap(CreateCirleBitmap.getCircleBitmap(mThumbnail.getBitmap(),(int)getContext().getResources().getDimension(R.dimen.thumbnail_image_size)));
                            mCameraAnimation.doCaptureAnimation(mPreviewThumb, getContext(),
                                    mListener);
                            mCshotThumBegin = System.currentTimeMillis();
                        }
                    }
                break;
            default:
                break;
            }
        }
    };
    
    public void onChangeNavigationBar(boolean isShow){
    	if(isShow == true){
    		NavigationBarUtils.adpaterNavigationBar(getContext(), mThumbnailContain);
    	}else{
    		NavigationBarUtils.adpaterNavigationBar(getContext(), mThumbnailContain,0);
    	}
    }
    /*prize-xuchunming-20180411-bugid:50825-start*/  
    public void updateThumbnail(Bitmap bitmap) {
    	mPreviewThumb.setBitmap(null);
        mPreviewThumb.setBitmap(bitmap);
        /*prize-xuchunming-20180427-bugid:56603-start*/
        mThumbnail = Thumbnail.createThumbnail(null, bitmap, 0);
        /*prize-xuchunming-20180427-bugid:56603-end*/
        mCameraAnimation.doCaptureAnimation(mPreviewThumb,
        getContext(), mListener);
    }
    /*prize-xuchunming-20180411-bugid:50825-end*/
}
