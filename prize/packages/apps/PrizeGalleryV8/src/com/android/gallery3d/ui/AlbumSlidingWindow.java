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

package com.android.gallery3d.ui;

import android.graphics.Bitmap;
import android.os.Message;

import com.android.gallery3d.app.AbstractGalleryActivity;
import com.android.gallery3d.app.AlbumDataLoader;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.data.MediaObject;
import com.android.gallery3d.data.MediaObject.PanoramaSupportCallback;
import com.android.gallery3d.data.Path;
import com.android.gallery3d.glrenderer.BitmapTexture;
import com.android.gallery3d.glrenderer.Texture;
import com.android.gallery3d.glrenderer.TextureUploader;
import com.android.gallery3d.glrenderer.TiledTexture;
import com.android.gallery3d.util.Future;
import com.android.gallery3d.util.FutureListener;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.JobLimiter;
import com.android.gallery3d.util.LogUtil;
import com.android.gallery3d.util.ThreadPool;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.mediatek.galleryframework.base.MediaData;
import com.android.gallery3d.data.LocalVideo;

public class AlbumSlidingWindow implements AlbumDataLoader.DataListener {
    @SuppressWarnings("unused")
    private static final String TAG = "Gallery2/AlbumSlidingWindow";

    private static final int MSG_UPDATE_ENTRY = 0;
    /// M: [PERF.MODIFY] @{
    /*private static final int JOB_LIMIT = 2;*/
     // Dynamic control thread number according to CPU Cores from constant number
     private static final int JOB_LIMIT = ThreadPool.PARALLEL_THREAD_NUM; //2;
    /// @}
    private VideoLabelMaker mVideoLabelMaker;
    private TextureUploader mLabelUploader;
     
    public static interface Listener {
        public void onSizeChanged(int size);
        public void onContentChanged();
    }

    public static class AlbumEntry {
        public MediaItem item;
        public Path path;
        public boolean isPanorama;
        public int rotation;
        public int mediaType;
        public boolean isWaitDisplayed;
        public BitmapTexture bitmapTexture;
        public Texture content;
        private BitmapLoader contentLoader;
        private PanoSupportListener mPanoSupportListener;
        public BitmapTexture videoThumb;
        private BitmapLoader videoLoader;
        private ThumbnailTarget thumbnailTarget;
    }

    private final AlbumDataLoader mSource;
    private final AlbumEntry mData[];
    private final SynchronizedHandler mHandler;
    private final JobLimiter mThreadPool;
    /// M: [BEHAVIOR.ADD] mVideoMicroThumbDecoder specializes on video thumbnail decoding
    private final JobLimiter mVideoMicroThumbDecoder;
    private final TextureUploader mTileUploader;

    private int mSize;

    private int mContentStart = 0;
    private int mContentEnd = 0;

    private int mActiveStart = 0;
    private int mActiveEnd = 0;

    private Listener mListener;

    private int mActiveRequestCount = 0;
    private boolean mIsActive = false;
    private AbstractGalleryActivity mActivity;

    private class PanoSupportListener implements PanoramaSupportCallback {
        public final AlbumEntry mEntry;
        public PanoSupportListener (AlbumEntry entry) {
            mEntry = entry;
        }
        @Override
        public void panoramaInfoAvailable(MediaObject mediaObject, boolean isPanorama,
                boolean isPanorama360) {
            if (mEntry != null) mEntry.isPanorama = isPanorama;
        }
    }

    public AlbumSlidingWindow(AbstractGalleryActivity activity,
            AlbumDataLoader source, int cacheSize) {
        source.setDataListener(this);
        mSource = source;
        mData = new AlbumEntry[cacheSize];
        mSize = source.size();
        mActivity = activity;

        if (!GalleryUtils.isShowVideoThumbnail()) {
            mVideoLabelMaker = new VideoLabelMaker(activity.getAndroidContext());
            mLabelUploader = new TextureUploader(activity.getGLRoot());
        }
        mHandler = new SynchronizedHandler(activity.getGLRoot()) {
            @Override
            public void handleMessage(Message message) {
                Utils.assertTrue(message.what == MSG_UPDATE_ENTRY);
                ((EntryUpdater) message.obj).updateEntry();
            }
        };

        mThreadPool = new JobLimiter(activity.getThreadPool(), JOB_LIMIT);
        /// M: [BEHAVIOR.ADD] mVideoMicroThumbDecoder specializes on video thumbnail decoding @{
        final int VIDEO_MICRO_THUMB_DECODER_JOB_LIMIT = 2;
        mVideoMicroThumbDecoder = new JobLimiter(activity.getThreadPool(),
                VIDEO_MICRO_THUMB_DECODER_JOB_LIMIT);
        /// @}
        mTileUploader = new TextureUploader(activity.getGLRoot());
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    public AlbumEntry get(int slotIndex) {
        if (!isActiveSlot(slotIndex)) {
            Utils.fail("invalid slot: %s outsides (%s, %s)",
                    slotIndex, mActiveStart, mActiveEnd);
        }
        return mData[slotIndex % mData.length];
    }

    public boolean isActiveSlot(int slotIndex) {
        return slotIndex >= mActiveStart && slotIndex < mActiveEnd;
    }

    private void setContentWindow(int contentStart, int contentEnd) {
        LogUtil.i(TAG, "setContentWindow contentStart=" + contentStart + " contentEnd=" + contentEnd + " mContentStart=" + mContentStart + " mContentEnd=" + mContentEnd);
    	if (contentStart == mContentStart && contentEnd == mContentEnd) return;

        if (!mIsActive) {
            mContentStart = contentStart;
            mContentEnd = contentEnd;
            mSource.setActiveWindow(contentStart, contentEnd);
            return;
        }

        if (contentStart >= mContentEnd || mContentStart >= contentEnd) {
            for (int i = mContentStart, n = mContentEnd; i < n; ++i) {
                freeSlotContent(i);
            }
            mSource.setActiveWindow(contentStart, contentEnd);
            for (int i = contentStart; i < contentEnd; ++i) {
                prepareSlotContent(i);
            }
        } else {
            for (int i = mContentStart; i < contentStart; ++i) {
                freeSlotContent(i);
            }
            for (int i = contentEnd, n = mContentEnd; i < n; ++i) {
                freeSlotContent(i);
            }
            mSource.setActiveWindow(contentStart, contentEnd);
            for (int i = contentStart, n = mContentStart; i < n; ++i) {
                prepareSlotContent(i);
            }
            for (int i = mContentEnd; i < contentEnd; ++i) {
                prepareSlotContent(i);
            }
        }

        mContentStart = contentStart;
        mContentEnd = contentEnd;
    }

    public void setActiveWindow(int start, int end) {
        if (!(start <= end && end - start <= mData.length && end <= mSize)) {
            Utils.fail("%s, %s, %s, %s", start, end, mData.length, mSize);
        }
        AlbumEntry data[] = mData;

        mActiveStart = start;
        mActiveEnd = end;

        int contentStart = Utils.clamp((start + end) / 2 - data.length / 2,
                0, Math.max(0, mSize - data.length));
        int contentEnd = Math.min(contentStart + data.length, mSize);
        setContentWindow(contentStart, contentEnd);
        updateTextureUploadQueue();
        if (mIsActive) updateAllImageRequests();
    }

    private void uploadBgTextureInSlot(int index) {
        if (index < mContentEnd && index >= mContentStart) {
            AlbumEntry entry = mData[index % mData.length];
            if (entry.bitmapTexture != null) {
                mTileUploader.addBgTexture(entry.bitmapTexture);
            }
            if (entry.videoThumb != null && mLabelUploader != null) {
                mLabelUploader.addBgTexture(entry.videoThumb);
            }
        }
    }

    private void updateTextureUploadQueue() {
        if (!mIsActive) return;
        mTileUploader.clear();
        if (mLabelUploader != null) {
            mLabelUploader.clear();
        }

        // add foreground textures
        for (int i = mActiveStart, n = mActiveEnd; i < n; ++i) {
            AlbumEntry entry = mData[i % mData.length];
            if (entry.bitmapTexture != null) {
                mTileUploader.addBgTexture(entry.bitmapTexture);
            }
            if (entry.videoThumb != null && mLabelUploader != null) {
                mLabelUploader.addBgTexture(entry.videoThumb);
            }
        }

        // add background textures
        int range = Math.max(
                (mContentEnd - mActiveEnd), (mActiveStart - mContentStart));
        for (int i = 0; i < range; ++i) {
            uploadBgTextureInSlot(mActiveEnd + i);
            uploadBgTextureInSlot(mActiveStart - i - 1);
        }
    }

    // We would like to request non active slots in the following order:
    // Order:    8 6 4 2                   1 3 5 7
    //         |---------|---------------|---------|
    //                   |<-  active  ->|
    //         |<-------- cached range ----------->|
    private void requestNonactiveImages() {
        int range = Math.max(
                (mContentEnd - mActiveEnd), (mActiveStart - mContentStart));
        for (int i = 0 ;i < range; ++i) {
            requestSlotImage(mActiveEnd + i);
            requestSlotImage(mActiveStart - 1 - i);
        }
    }

    // return whether the request is in progress or not
    private boolean requestSlotImage(final int slotIndex) {
        LogUtil.i(TAG, "requestSlotImage slotIndex=" + slotIndex + " mContentStart=" + mContentStart + " mContentEnd=" + mContentEnd);
        if (slotIndex < mContentStart || slotIndex >= mContentEnd) return false;
        final AlbumEntry entry = mData[slotIndex % mData.length];
        if (entry == null || entry.content != null || entry.item == null) {
            LogUtil.i(TAG, "requestSlotImage entry.content != null || entry.item == null");
            return false;
        }

        // Set up the panorama callback
        entry.mPanoSupportListener = new PanoSupportListener(entry);
        entry.item.getPanoramaSupport(entry.mPanoSupportListener);
        if (entry.videoLoader != null) entry.videoLoader.startLoad();
        if (entry.thumbnailTarget == null) {
            LogUtil.i(TAG, "onResourceReady entry.thumbnailTarget == null");
        } else if (entry.thumbnailTarget.getRequest() == null) {
            LogUtil.i(TAG, "onResourceReady entry.thumbnailTarget.getRequest == null");
        } else {
            LogUtil.i(TAG, "onResourceReady entry.thumbnailTarget.getRequest isRun=" + entry.thumbnailTarget.getRequest().isRunning()
            + " isComple=" + entry.thumbnailTarget.getRequest().isComplete() + " isFail=" + entry.thumbnailTarget.getRequest().isFailed()
            + " isCancel=" + entry.thumbnailTarget.getRequest().isCancelled() + " isPause=" + entry.thumbnailTarget.getRequest().isPaused()
            + " issourceSet=" + entry.thumbnailTarget.getRequest().isResourceSet());
        }
        if (entry.thumbnailTarget == null || entry.thumbnailTarget.getRequest() == null
                || (entry.thumbnailTarget.getRequest().isComplete() || entry.thumbnailTarget.getRequest().isFailed() || entry.thumbnailTarget.getRequest().isCancelled())
                || slotIndex != entry.thumbnailTarget.getSlotIndex() || !entry.item.getPath().equals(entry.thumbnailTarget.getMediaItem().getPath())) {
            LogUtil.i(TAG, "onResourceReady request slotIndex=" + slotIndex + " path=" + entry.item.getPath());
            /*if (entry.thumbnailTarget != null) {
                Glide.clear(entry.thumbnailTarget);
            }*/
            entry.thumbnailTarget = Glide.with(mActivity.getApplication()).load(entry.item.getFilePath()).asBitmap().centerCrop().override(GalleryUtils.sBitmapWidth, GalleryUtils.sBitmapWidth).into(new ThumbnailTarget(slotIndex, entry.item));
            return true;
        }

        /*entry.contentLoader.startLoad();*/
        /*return entry.contentLoader.isRequestInProgress();*/
        return true;
    }

    private void cancelNonactiveImages() {
        int range = Math.max(
                (mContentEnd - mActiveEnd), (mActiveStart - mContentStart));
        for (int i = 0 ;i < range; ++i) {
            cancelSlotImage(mActiveEnd + i);
            cancelSlotImage(mActiveStart - 1 - i);
        }
    }

    private void cancelSlotImage(final int slotIndex) {
        if (slotIndex < mContentStart || slotIndex >= mContentEnd) return;
        final AlbumEntry item = mData[slotIndex % mData.length];
        if (item.contentLoader != null) item.contentLoader.cancelLoad();
        if (item.videoLoader != null) item.videoLoader.cancelLoad();
//        if (item.thumbnailTarget != null) Glide.clear(item.thumbnailTarget);
    }

    private void freeSlotContent(final int slotIndex) {
        AlbumEntry data[] = mData;
        int index = slotIndex % data.length;
        final AlbumEntry entry = data[index];
        if (entry.contentLoader != null) entry.contentLoader.recycle();
        if (entry.bitmapTexture != null) entry.bitmapTexture.recycle();
        if (entry.videoLoader != null) entry.videoLoader.recycle();
        /*if (GalleryUtils.isOnMainThread()) {
            LogUtil.i(TAG, "freeSlotContent slotIndex=" + slotIndex);
            if (entry.thumbnailTarget != null) Glide.clear(entry.thumbnailTarget);
        }*/
        /*mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LogUtil.i(TAG, "freeSlotContent slotIndex=" + slotIndex);
                if (entry.thumbnailTarget != null) Glide.clear(entry.thumbnailTarget);
            }
        });*/
        data[index] = null;
    }

    private void prepareSlotContent(int slotIndex) {
        AlbumEntry entry = new AlbumEntry();
        MediaItem item = mSource.get(slotIndex); // item could be null;
        entry.item = item;
        entry.mediaType = (item == null)
                ? MediaItem.MEDIA_TYPE_UNKNOWN
                : entry.item.getMediaType();
        entry.path = (item == null) ? null : item.getPath();
        entry.rotation = (item == null) ? 0 : item.getRotation();
		if (!GalleryUtils.isShowVideoThumbnail()) {
                 if (item instanceof LocalVideo) {
                     int duration = ((LocalVideo) item).durationInSec;
                     LogUtil.i(TAG, "video thumb loader duration=" + duration);
                     entry.videoLoader = new VideoThumbLoader(slotIndex, GalleryUtils.formatDuration(
                             mActivity.getAndroidContext(), duration));
                 }
             }
       /* entry.contentLoader = new ThumbnailLoader(slotIndex, entry.item);*/
        mData[slotIndex % mData.length] = entry;
    }

    private void updateAllImageRequests() {
        if (GalleryUtils.isOnMainThread()) {
            updateAllImageRequestsRunOnMainThread();
        } else {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    updateAllImageRequestsRunOnMainThread();
                }
            });
        }
    }

    private void updateAllImageRequestsRunOnMainThread() {
        LogUtil.i(TAG, "updateAllImageRequests mActiveStart=" + mActiveStart + " mActiveEnd=" + mActiveEnd);
        mActiveRequestCount = 0;
        for (int i = mActiveStart, n = mActiveEnd; i < n; ++i) {
            if (requestSlotImage(i)) ++mActiveRequestCount;
        }
        LogUtil.i(TAG, "updateAllImageRequests mActiveRequestCount=" + mActiveRequestCount);
        if (mActiveRequestCount == 0) {
            requestNonactiveImages();
        } else {
            cancelNonactiveImages();
        }
        LogUtil.i(TAG, "updateAllImageRequests end");
    }

    private static interface EntryUpdater {
        public void updateEntry();
    }

    private class ThumbnailTarget extends SimpleTarget<Bitmap> {

        private final int mSlotIndex;
        private final MediaItem mItem;

        public ThumbnailTarget(int slotIndex, MediaItem item) {
            mSlotIndex = slotIndex;
            mItem = item;
        }

        public int getSlotIndex() {
            return mSlotIndex;
        }

        public MediaItem getMediaItem() {
            return mItem;
        }

        @Override
        public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
//            Glide.clear(this);
            Bitmap bitmap = resource;
            LogUtil.i(TAG, "onResourceReady mSlotIndex=" + mSlotIndex + " bitmap=" + bitmap);
            if (bitmap == null) {
                return; // error or recycled
            }

            /// @}
            AlbumEntry entry = mData[mSlotIndex % mData.length];
            if (entry == null) {
                LogUtil.i(TAG, "onResourceReady entry == null");
                return;
            } else if (entry.item == null) {
                LogUtil.i(TAG, "onResourceReady entry.item == null");
                return;
            } else if (!entry.item.getPath().equals(mItem.getPath())) {
                LogUtil.i(TAG, "onResourceReady path unequest");
                return;
            }
            LogUtil.i(TAG, "onResourceReady mSlotIndex=" + mSlotIndex + " path=" + mItem.getPath() + " oriPath=" + entry.item.getPath());
            entry.bitmapTexture = new BitmapTexture(bitmap);
            entry.content = entry.bitmapTexture;

            if (isActiveSlot(mSlotIndex)) {
                mTileUploader.addBgTexture(entry.bitmapTexture);
                --mActiveRequestCount;
                if (mActiveRequestCount == 0) requestNonactiveImages();
                if (mListener != null) mListener.onContentChanged();
            } else {
                mTileUploader.addBgTexture(entry.bitmapTexture);
            }

        }
    }

    private class ThumbnailLoader extends BitmapLoader implements EntryUpdater {
        private final int mSlotIndex;
        private final MediaItem mItem;

        public ThumbnailLoader(int slotIndex, MediaItem item) {
            mSlotIndex = slotIndex;
            mItem = item;
        }

        @Override
        protected Future<Bitmap> submitBitmapTask(FutureListener<Bitmap> l) {
            /// M: [BEHAVIOR.ADD] mVideoMicroThumbDecoder specializes on video thumbnail decoding @{
            if (MediaObject.MEDIA_TYPE_VIDEO == mItem.getMediaType()) {
                return mVideoMicroThumbDecoder.submit(mItem
                        .requestImage(MediaItem.TYPE_MICROTHUMBNAIL), this);
            }
            /// @}
            return mThreadPool.submit(
                    mItem.requestImage(MediaItem.TYPE_MICROTHUMBNAIL), this);
        }

        @Override
        protected void onLoadComplete(Bitmap bitmap) {
            mHandler.obtainMessage(MSG_UPDATE_ENTRY, this).sendToTarget();
        }

        @Override
        public void updateEntry() {
            Bitmap bitmap = getBitmap();
            if (bitmap == null) return; // error or recycled
            AlbumEntry entry = mData[mSlotIndex % mData.length];
            entry.bitmapTexture = new BitmapTexture(bitmap);
            entry.content = entry.bitmapTexture;

            if (isActiveSlot(mSlotIndex)) {
                mTileUploader.addBgTexture(entry.bitmapTexture);
                --mActiveRequestCount;
                if (mActiveRequestCount == 0) requestNonactiveImages();
                if (mListener != null) mListener.onContentChanged();
            } else {
                mTileUploader.addBgTexture(entry.bitmapTexture);
            }

            /// M: [TESTCASE.ADD] @{
            mBitmapLoaded = true;
            if (isActiveSlot(mSlotIndex) && mActiveRequestCount == 0) {
                mDecodeFinished = true;
                mDecodeFinishTime = System.currentTimeMillis();
            }
            /// @}
        }
    }

    private class VideoThumbLoader extends BitmapLoader implements EntryUpdater {
        private final int mSlotIndex;
        private final String mDuration;

        public VideoThumbLoader(
                int slotIndex, String duration) {
            mSlotIndex = slotIndex;
            mDuration = duration;
        }

        @Override
        protected Future<Bitmap> submitBitmapTask(FutureListener<Bitmap> l) {
            return mThreadPool.submit(mVideoLabelMaker.requestLabel(mDuration), l);
        }

        @Override
        protected void onLoadComplete(Bitmap bitmap) {
            mHandler.obtainMessage(MSG_UPDATE_ENTRY, this).sendToTarget();
        }

        @Override
        public void updateEntry() {
            Bitmap bitmap = getBitmap();
            /// M: [FEATURE.MODIFY] notify VTSP of decoding fail @{
            // if (bitmap == null) return; // error or recycled
            if (bitmap == null) {
                return; // error or recycled
            }
            /// @}

            AlbumEntry entry = mData[mSlotIndex % mData.length];
            BitmapTexture texture = new BitmapTexture(bitmap);
            texture.setOpaque(false);
            entry.videoThumb = texture;

            if (isActiveSlot(mSlotIndex)) {
                if (mLabelUploader != null) {
                    mLabelUploader.addFgTexture(texture);
                }
                --mActiveRequestCount;
                if (mActiveRequestCount == 0) requestNonactiveImages();
                if (mListener != null) mListener.onContentChanged();
            } else {
                if (mLabelUploader != null) {
                    mLabelUploader.addBgTexture(texture);
                }
            }

            /// M: [TESTCASE.ADD] @{
            mBitmapLoaded = true;
            if (isActiveSlot(mSlotIndex) && mActiveRequestCount == 0) {
                mDecodeFinished = true;
                mDecodeFinishTime = System.currentTimeMillis();
            }
            /// @}
        }
    }
    @Override
    public void onSizeChanged(int size) {
        if (mSize != size) {
            mSize = size;
            if (mListener != null) mListener.onSizeChanged(mSize);
            if (mContentEnd > mSize) mContentEnd = mSize;
            if (mActiveEnd > mSize) mActiveEnd = mSize;
        }
    }

    @Override
    public void onContentChanged(int index) {
        LogUtil.d(TAG, "onContentChanged index=" + index + " mContentStart=" + mContentStart + " mContentEnd=" + mContentEnd);
        if (index >= mContentStart && index < mContentEnd && mIsActive) {
            freeSlotContent(index);
            prepareSlotContent(index);
        }
    }

    @Override
    public void onUpdateContent() {
        updateAllImageRequests();
        /*if (mListener != null && isActiveSlot(index)) {
            mListener.onContentChanged();
        }*/
    }

    public void resume() {
        mIsActive = true;
        TiledTexture.prepareResources();
        for (int i = mContentStart, n = mContentEnd; i < n; ++i) {
            prepareSlotContent(i);
        }
        updateAllImageRequests();
    }

    public void pause() {
        mIsActive = false;
        mTileUploader.clear();
        if (mLabelUploader != null) {
            mLabelUploader.clear();
        }
        TiledTexture.freeResources();
        for (int i = mContentStart, n = mContentEnd; i < n; ++i) {
            freeSlotContent(i);
        }
    }

    //********************************************************************
    //*                              MTK                                 *
    //********************************************************************

    public boolean mDecodeFinished = false;
    public long mDecodeFinishTime = 0;

    ///M: [MEMORY.ADD] @{
    /**
     * Recycle the contentLoader.
     *
     * @param entry
     *            The AlbumEntry whose contentLoader will be recycled
     */
    public void recycle(AlbumEntry entry) {
        if (entry.contentLoader != null) {
            entry.contentLoader.recycle();
        }
    }
    /// @}

    // returns true if all active/visible slots are filled
    public boolean isAllActiveSlotsFilled() {
        int start = mActiveStart;
        int end = mActiveEnd;

        if (start < 0 || start >= end) {
            LogUtil.w(TAG, "<isAllActiveSlotFilled> active range not ready yet");
            return false;
        }

        AlbumEntry entry;
        BitmapLoader loader;
        for (int i = start; i < end; ++i) {
            entry = mData[i % mData.length];
            if (entry == null) {
                LogUtil.i(TAG, "<isAllActiveSlotsFilled> slot " + i
                        + " is not loaded, return false");
                return false;
            }
            loader = entry.contentLoader;
            if (loader == null || !loader.isLoadingCompleted()) {
                LogUtil.i(TAG, "<isAllActiveSlotsFilled> slot " + i
                        + " is not loaded, return false");
                return false;
            }
        }

        LogUtil.i(TAG, "<isAllActiveSlotsFilled> return true");
        return true;
    }
}
