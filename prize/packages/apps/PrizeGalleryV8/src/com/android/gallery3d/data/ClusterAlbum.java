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

package com.android.gallery3d.data;

import java.util.ArrayList;
import com.android.gallery3d.util.GalleryUtils;

public class ClusterAlbum extends MediaSet implements ContentListener {
    @SuppressWarnings("unused")
    private static final String TAG = "Gallery2/ClusterAlbum";
    private ArrayList<Path> mPaths = new ArrayList<Path>();
    private String mName = "";
    private int mDatetaken;
    private MediaSet mBaseSet;
    private MediaSet mClusterAlbumSet;
    private MediaItem mCover;
    private final int INVALID_COUNT = -1;
    private int mCount = INVALID_COUNT;
    private int mKind = -1;
    private int mOffset;
    private ClusterType mClusterType;
    private TitleLocation mTitleLocation;


    private TimeLineTitleMediaItem mTimelineTitleMediaItem;

    public ClusterAlbum(Path path, MediaSet baseSet,
            MediaSet clusterAlbumSet, int kind, int offset) {
        super(path, nextVersionNumber());
        mBaseSet = baseSet;
        mClusterAlbumSet = clusterAlbumSet;
        mClusterAlbumSet.addContentListener(this);
        mKind = kind;
        mOffset = offset;
        mTimelineTitleMediaItem = new TimeLineTitleMediaItem(path);
    }

    public void setCoverMediaItem(MediaItem cover) {
        mCover = cover;
    }

    @Override
    public MediaItem getCoverMediaItem() {
        return mCover != null ? mCover : super.getCoverMediaItem();
    }

    void setMediaItems(ArrayList<Path> paths) {
        mPaths = paths;
    }

    void empty() {
        setItemCount(0);
        setMediaItems(new ArrayList<Path>());
    }

    public ArrayList<Path> getMediaItems() {
        if ((mPaths.size() == 0 && mCount > 0)|| (mPaths.size() != mCount)) {
            Log.i(TAG, "getMediaItems start count=" + mCount);
            ArrayList<MediaItem> items = getMediaItem(0, mCount);
            Log.i(TAG, "getMediaItems middle");
			mPaths.clear();
            for (MediaItem item : items) {
                mPaths.add(item.getPath());
            }
            Log.i(TAG, "getMediaItems end");
        }
        return mPaths;
    }

    public void setName(String name) {
        mName = name;
        mTimelineTitleMediaItem.setTitle(name);
    }
	
	public void setDatetaken(int datetaken) {
        mDatetaken = datetaken;
		if (mTimelineTitleMediaItem != null) {
             mTimelineTitleMediaItem.setDatetaken(mDatetaken);
        }
    }

    public void setTitleLocation(TitleLocation titleLocation) {
        mTitleLocation = titleLocation;
    }

    public TitleLocation getTitleLocation() {
        return mTitleLocation;
    }

    public void setOffset(int offset) {
        mOffset = offset;
    }

    public void setClusterType(ClusterType clusterType) {
        mClusterType = clusterType;
    }

    @Override
    public String getName() {
        return mName;
    }

    @Override
    public int getMediaItemCount() {
        return mCount;
    }

    @Override
    public int getSelectableItemCount() {
        return mCount;
    }

    public void setItemCount(int count) {
        mCount = count;
        if (mTimelineTitleMediaItem != null) {
            mTimelineTitleMediaItem.setCount(count);
        }
    }


    private void updateItemCounts() {
        setItemCount(mCount);
    }

    @Override
    public ArrayList<MediaItem> getMediaItem(int start, int count) {
        //return getMediaItemFromPath(mPaths, start, count, mDataManager);
        Log.i(TAG, "getMediaItem start=" + start + " count=" + count);
        updateItemCounts();
        /*if (start == 0) {
            ArrayList<MediaItem> mediaItemList = new ArrayList<MediaItem>();
            mediaItemList.addAll(mBaseSet.getMediaItem(mOffset, count - 1));
            mediaItemList.add(0, mTimelineTitleMediaItem);
            return mediaItemList;
        } else*/ {
            return mBaseSet.getMediaItem(mOffset + start, count);
        }
    }

    public ArrayList<MediaItem> getTotalMediaItem(int start, int count) {
        updateItemCounts();
        if (start == 0) {
            ArrayList<MediaItem> mediaItemList = new ArrayList<MediaItem>();
            mediaItemList.addAll(mBaseSet.getMediaItem(mOffset, count - 1));
            mediaItemList.add(0, mTimelineTitleMediaItem);
            return mediaItemList;
        } else {
            return mBaseSet.getMediaItem(mOffset + start - 1, count);
        }
    }

    @Override
    public int getItemCount() {
        return mCount;
    }

    @Override
    public int getTotalMediaItemCount() {
        return mCount + 1;
    }

    @Override
    public int getMediaType() {
        // return correct type of Timeline Title.
        return MEDIA_TYPE_TIMELINE_TITLE;
    }

    @Override
    public boolean isLoading() {
        return mClusterAlbumSet.isLoading();
    }

    @Override
    public long reload() {
        long version = mClusterAlbumSet.reload();
        if (version == INVALID_DATA_VERSION) {
            mPaths.clear();
            return INVALID_DATA_VERSION;
        }
        if (version > mDataVersion) {
            mPaths.clear();
            mDataVersion = nextVersionNumber();
        }
        return mDataVersion;
    }

    @Override
    public void onContentDirty() {
        notifyContentChanged();
    }

    @Override
    public int getSupportedOperations() {
        // Timeline title item doesn't support anything, just its sub objects supported.
        return 0;
    }

    @Override
    public void delete() {
        return;
    }

    @Override
    public boolean isLeafAlbum() {
        return true;
    }

    public TimeLineTitleMediaItem getTimelineTitle() {
        return mTimelineTitleMediaItem;
    }

    public ArrayList<String> getSubMediaItemLocation(int dataTaken) {
        return mBaseSet.getSubMediaItemLocation(mClusterType.mDatetaken);
    }

}
