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

import android.content.Context;
import android.net.Uri;

import com.android.gallery3d.app.GalleryApp;
import com.android.gallery3d.util.LogUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class ClusterAlbumSet extends MediaSet implements ContentListener {
    @SuppressWarnings("unused")
    private static final String TAG = "Gallery2/ClusterAlbumSet";
    private GalleryApp mApplication;
    private MediaSet mBaseSet;
    private int mKind;
    private ArrayList<ClusterAlbum> mAlbums = new ArrayList<ClusterAlbum>();
    private boolean mIsLoading;

    private int mTotalMediaItemCount;
    /** mTotalSelectableMediaItemCount is the count of items
     * exclude not selectable such as Title item in TimeLine. */
    private int mTotalSelectableMediaItemCount;
    private ArrayList<Integer> mAlbumItemCountList;

    public ClusterAlbumSet(Path path, GalleryApp application,
            MediaSet baseSet, int kind) {
        super(path, INVALID_DATA_VERSION);
        mApplication = application;
        mBaseSet = baseSet;
        Log.d(TAG, "ClusterAlbumSet mBaseSet = " + mBaseSet);
        mKind = kind;
        baseSet.addContentListener(this);
    }

    @Override
    public MediaSet getSubMediaSet(int index) {
        /// M: [BUG.ADD] @{
        // When index >= mAlbums.size(), exception occours because of
        // multi-thread accessing. Add special check before get MediaSet.
        if (index >= mAlbums.size()) {
            Log.d(TAG, "<getSubMediaSet> index = " + index
                    + ", mAlbums.size() = " + mAlbums.size()
                    + ", return null");
            return null;
        }
        /// @}
        return mAlbums.get(index);
    }

    @Override
    public int getSubMediaSetCount() {
        Log.i(TAG, " getSubMediaSetCount=");
        return mAlbums.size();
    }

    @Override
    public String getName() {
        return mBaseSet.getName();
    }

    @Override
    public synchronized boolean isLoading() {
        return mIsLoading;
    }

    @Override
    public long reload() {
       if(mBaseSet == null){
		   return mDataVersion;
       }
        synchronized (this) {
            long version = mBaseSet.reload();
            mIsLoading = mBaseSet.isLoading();
            if (version > mDataVersion && !mIsLoading) {
                ClusterType[] clusterTypes = mBaseSet.getSubMediaItemType();
                updateClusterType(clusterTypes);
                mIsLoading = false;
                mDataVersion = nextVersionNumber();
            }
            Log.i(TAG, "reload mKind" + mKind);
            if (mKind == ClusterSource.CLUSTER_ALBUMSET_TIME || mKind == ClusterSource.CLUSTER_ALBUMSET_LOCATION) {
                calculateTotalItemsCount();
                calculateTotalSelectableItemsCount();
            }
        }
        return mDataVersion;
    }

    @Override
    public void onContentDirty() {
        notifyContentChanged();
    }

    private void updateClusterType(ClusterType[] clusterTypes) {
        if (clusterTypes == null || clusterTypes.length == 0) {
            //set the empty path to the albums which don't exist from dataManger
            for (ClusterAlbum album : mAlbums) {
                album.empty();
            }
            mAlbums.clear();
            return;
        }
        //save last paths to find the empty albums
        ArrayList<Path> oldPaths = new ArrayList<Path>();
        for (ClusterAlbum album : mAlbums) {
            oldPaths.add(album.getPath());
        }
        mAlbums.clear();
        DataManager dataManager = mApplication.getDataManager();
        int offset = 0;
        for (int i = 0, len = clusterTypes.length; i < len; i++) {
            ClusterType clusterType = clusterTypes[i];
            Path childPath = mPath.getChild(Uri.encode(String.valueOf(clusterType.mDatetaken)));

            ClusterAlbum album;
            synchronized (DataManager.LOCK) {
                album = (ClusterAlbum) dataManager.peekMediaObject(childPath);
                Log.i(TAG, "updateClusters childPath=" + childPath + " album=" + album);
                if (album == null) {
                    album = new ClusterAlbum(childPath, mBaseSet, this, mKind, offset);
                }
            }
            album.setName(TimeClustering.getTimeTitleName(mApplication.getAndroidContext(), clusterType.mDatetaken));
            album.setDatetaken(clusterType.mDatetaken);
            album.setItemCount(clusterType.mCount);
            album.setOffset(offset);
            album.setClusterType(clusterType);
            mAlbums.add(album);
            offset += clusterType.mCount;
            ArrayList<String> locations = album.getSubMediaItemLocation(clusterType.mDatetaken);
            TitleLocation root = new TitleLocation("earth");
            for (String location : locations) {
                TitleLocation.parseAddr(root, location);
            }
            album.setTitleLocation(root);
            int size = oldPaths.size();
            for (int j = size - 1; j >= 0; j--) {
                if (oldPaths.get(j) == childPath) {
                    oldPaths.remove(j);
                    break;
                }
            }
            //set the empty path to the albums which don't exist from dataManger
            for (Path path : oldPaths) {
                ClusterAlbum oldAlbum = (ClusterAlbum) dataManager.peekMediaObject(path);
                if (oldAlbum != null) {
                    oldAlbum.empty();
                }
            }
            LogUtil.i(TAG, "location=" + locations);
        }
    }

    private void calculateTotalSelectableItemsCount() {
        mTotalSelectableMediaItemCount = 0;
        if (mAlbums != null && mAlbums.size() > 0) {
            for (ClusterAlbum album : mAlbums) {
                int count = album.getSelectableItemCount();
                mTotalSelectableMediaItemCount += count;
            }
        }
    }

    @Override
    public int getSelectableItemCount() {
        return mTotalSelectableMediaItemCount;
    }

    private void calculateTotalItemsCount() {
      Log.i(TAG, "calculateTotalItemsCount start" + mAlbums);
      mTotalMediaItemCount = 0;
      if( mAlbums != null && mAlbums.size() > 0) {
          mAlbumItemCountList = new ArrayList<Integer>();
          for(ClusterAlbum album: mAlbums) {
              int count = album.getTotalMediaItemCount();
              mTotalMediaItemCount = mTotalMediaItemCount + count;
              Log.i(TAG, "calculateTotalItemsCount count" + count + " mTotalMediaItemCount=" + mTotalMediaItemCount);
              mAlbumItemCountList.add(mTotalMediaItemCount);
          }
      }
  }

  @Override
  public int getMediaItemCount() {
      return mTotalMediaItemCount;
  }

  @Override
  public ArrayList<MediaItem> getMediaItem(int start, int count) {
      if ((start + count) > mTotalMediaItemCount ) {
          count  = mTotalMediaItemCount - start;
      }
      if (count <= 0) return null;
      ArrayList<MediaItem> mediaItems = new ArrayList<MediaItem>();
      int startAlbum = findTimelineAlbumIndex(start);
      int endAlbum = findTimelineAlbumIndex(start + count - 1);
      int s;
      int lCount;
      if (mAlbums.size() > 0 && mAlbumItemCountList.size() > 0 && mAlbumItemCountList.size() > startAlbum) {
          s = mAlbums.get(startAlbum).getTotalMediaItemCount() -
                  (mAlbumItemCountList.get(startAlbum) - start);
          for (int i = startAlbum; i <= endAlbum && i < mAlbums.size(); ++i) {
              int albumCount = mAlbums.get(i).getTotalMediaItemCount();
              lCount = Math.min(albumCount - s, count);
              ArrayList<MediaItem> items = mAlbums.get(i).getTotalMediaItem(s, lCount);
              if (items != null)
                  mediaItems.addAll(items);
              count -= lCount;
              s = 0;
          }
      }
      return mediaItems;
  }

  public int findTimelineAlbumIndex(int itemIndex) {
      int index = Arrays.binarySearch(mAlbumItemCountList.toArray(new Integer[0]), itemIndex);
      if (index <  mTotalMediaItemCount && index >=  0)
          return index + 1;
      if (index < 0) {
          index = (index * (-1)) - 1;
      }
      return index;
  }

  public ClusterAlbum getAlbumFromindex(int index) {
      int aIndex = findTimelineAlbumIndex(index);
      if (aIndex < mAlbums.size() && aIndex >= 0) {
          return mAlbums.get(aIndex);
      }
      return null;
  }
}
