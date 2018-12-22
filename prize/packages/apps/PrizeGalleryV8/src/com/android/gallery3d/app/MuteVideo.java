/*
 * Copyright (C) 2014 MediaTek Inc.
 * Modification based on code covered by the mentioned copyright
 * and/or permission notice(s).
 */
/*
 * Copyright (C) 2012 The Android Open Source Project
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

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.MediaStore.Video;
import android.provider.MediaStore.Video.Media;
import android.widget.Toast;

import com.android.gallery3d.R;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.util.SaveVideoFileInfo;
import com.android.gallery3d.util.SaveVideoFileUtils;
import com.mediatek.galleryfeature.config.FeatureConfig;

import com.mediatek.gallery3d.video.SlowMotionItem;
import java.io.IOException;

public class MuteVideo {

    private static final String TAG = "Gallery2/VideoPlayer/MuteVideo";
    private ProgressDialog mMuteProgress;

    private String mFilePath = null;
    private Uri mUri = null;
    private Uri mNewVideoUri = null;
    private SaveVideoFileInfo mDstFileInfo = null;
    private Activity mActivity = null;
    private final Handler mHandler = new Handler();

    final String TIME_STAMP_NAME = "'MUTE'_yyyyMMdd_HHmmss";
    // / M: add for show mute error toast @{
    private final Runnable mShowErrorToastRunnable = new Runnable() {
        @Override
        public void run() {
            Toast.makeText(mActivity.getApplicationContext(),
                    mActivity.getString(R.string.video_mute_err),
                    Toast.LENGTH_SHORT).show();
        }
    };
    // / M: @}

    public MuteVideo(String filePath, Uri uri, Activity activity) {
        mUri = uri;
        mFilePath = filePath;
        mActivity = activity;
    }

    public void muteInBackground() {
        Log.v(TAG, "[muteInBackground]...");
        mDstFileInfo = SaveVideoFileUtils.getDstMp4FileInfo(TIME_STAMP_NAME,
                mActivity.getContentResolver(), mUri, null, false,
                mActivity.getString(R.string.folder_download));

        showProgressDialog();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    boolean isMuteSuccessful = VideoUtils.startMute(mFilePath,
                            mDstFileInfo, mMuteProgress);
                    if (!isMuteSuccessful) {
                        Log.v(TAG, "[muteInBackground] mute failed");
                        mHandler.removeCallbacks(mShowErrorToastRunnable);
                        mHandler.post(mShowErrorToastRunnable);
                        if (mDstFileInfo.mFile.exists()) {
                            mDstFileInfo.mFile.delete();
                        }
                        return;
                    }
                    // /M: Get new video uri.
                    mNewVideoUri = null;
                    mNewVideoUri = SaveVideoFileUtils.insertContent(
                            mDstFileInfo, mActivity.getContentResolver(), mUri);
                    Log.v(TAG, "mNewVideoUri = " + mNewVideoUri);
                    // /M: If current video is slow motion, update slow motion
                    // info to db.
                    updateSlowMotionInfoToDBIfNeed(mActivity, mUri, mNewVideoUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // After muting is done, trigger the UI changed.
                Log.v(TAG, "[muteInBackground] post mTriggerUiChangeRunnable");
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(
                                mActivity.getApplicationContext(),
                                mActivity.getString(R.string.save_into, mDstFileInfo.mFolderName),
                                Toast.LENGTH_SHORT).show();
                        if (mMuteProgress != null) {
                            mMuteProgress.dismiss();
                            mMuteProgress = null;
                            if (mNewVideoUri != null) {
                                // Show the result only when the activity not stopped.
                                Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
                                intent.setDataAndType(mNewVideoUri, "video/*");
                                intent.putExtra(MediaStore.EXTRA_FINISH_ON_COMPLETION, false);
                                mActivity.startActivity(intent);
                            }
                        }
                    }
                });
            }
        }).start();
    }

    /*
     * M: Update slow motion info to db, if current video is slow motion video.
     */
    private void updateSlowMotionInfoToDBIfNeed(final Context context,
            final Uri sourceVideoUri, final Uri newVideoUri) {
        // if current trimed video is slow motion video, update slow motion info
        // to db.
        SlowMotionItem sourceItem = new SlowMotionItem(context, sourceVideoUri);

        if (sourceItem.isSlowMotionVideo()) {
            // When slow motion end time is equal to duration, correct the slow
            // motion end time.
            int endTime = 0;
            // Get muted file duration.
            int duration = SaveVideoFileUtils
                    .retriveVideoDurationMs(mDstFileInfo.mFile.getPath());
            if (duration < sourceItem.getSectionEndTime()) {
                // to avoid overflow, use Long type to calculate
                endTime = (int) ((long) duration
                        * sourceItem.getSectionEndTime() / sourceItem
                        .getDuration());
            } else {
                endTime = sourceItem.getSectionEndTime();
            }
            Log.v(TAG,
                    "[updateSlowMotionInfoToDBIfNeed] muted file duration = "
                            + duration + ", sourceItem.getSectionEndTime() = "
                            + sourceItem.getSectionEndTime()
                            + ", sourceItem.getDuration() = "
                            + sourceItem.getDuration() + ", endTime = "
                            + endTime);
            SlowMotionItem item = new SlowMotionItem(context, newVideoUri);
            item.setSectionStartTime(sourceItem.getSectionStartTime());
            item.setSectionEndTime(endTime);
            item.setSpeed(sourceItem.getSpeed());
            item.updateItemToDB();
        }
    }

    // /M:fix google bug
    // mute video is not done, when long press power key to power off,
    // muteVideo runnable still there run after gallery activity destoryed.@{
    public void cancelMute() {
        Log.v(TAG, "[cancleMute] mMuteProgress = " + mMuteProgress);
        if (mMuteProgress != null) {
            mMuteProgress.dismiss();
            mMuteProgress = null;
        }
    }

    // @}

    private void showProgressDialog() {
        mMuteProgress = new ProgressDialog(mActivity);
        mMuteProgress.setTitle(mActivity.getString(R.string.muting));
        mMuteProgress.setMessage(mActivity.getString(R.string.please_wait));
        mMuteProgress.setCancelable(false);
        mMuteProgress.setCanceledOnTouchOutside(false);
        mMuteProgress.show();
    }

}
