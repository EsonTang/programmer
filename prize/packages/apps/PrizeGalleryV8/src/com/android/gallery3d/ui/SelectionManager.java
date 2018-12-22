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

import android.os.Handler;

import com.android.gallery3d.app.AbstractGalleryActivity;
import com.android.gallery3d.app.TimeLineDataLoader;
import com.android.gallery3d.data.ClusterAlbum;
import com.android.gallery3d.data.ContentListener;
import com.android.gallery3d.data.DataManager;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.data.MediaObject;
import com.android.gallery3d.data.MediaSet;
import com.android.gallery3d.data.Path;
import com.android.gallery3d.util.Future;
import com.android.gallery3d.util.FutureListener;
import com.android.gallery3d.util.LogUtil;
import com.android.gallery3d.util.ThreadPool.Job;
import com.android.gallery3d.util.ThreadPool.JobContext;

import com.mediatek.gallery3d.adapter.ContainerSet;
import com.mediatek.galleryframework.base.MediaData;
import com.prize.slideselect.ISelectMode;

import java.util.concurrent.locks.ReentrantLock;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class SelectionManager implements TimeLineDataLoader.DataListener {
    @SuppressWarnings("unused")
    private static final String TAG = "Gallery2/SelectionManager";

    public static final int ENTER_SELECTION_MODE = 1;
    public static final int LEAVE_SELECTION_MODE = 2;
    public static final int SELECT_ALL_MODE = 3;
    /// M: [BEHAVIOR.ADD] @{
    // when click deselect all in menu, not leave selection mode
    public static final int DESELECT_ALL_MODE = 4;
    /// @}

    private Set<Path> mClickedSet;
    private Set<Path> mSlideSet;
    private MediaSet mSourceMediaSet;
    private SelectionListener mListener;
    private DataManager mDataManager;
    private boolean mInverseSelection;
    private boolean mIsAlbumSet;
    private boolean mSetIsAlbumSet;
    private boolean mInSelectionMode;
    private boolean mAutoLeave = true;
    private int mTotal;
    /** mTotalSelectable is the count of items
     * exclude not selectable such as Title item in TimeLine. */
    private int mTotalSelectable;
    private TimeLineDataLoader mTimeLineDataLoader;
	private ReentrantLock lock = new ReentrantLock();

    @Override
    public void onContentChanged(int index) {
    }

    /*@Override
    public void onSizeChanged(int size) {
        if (mInverseSelection) {
            selectAll();
        }
    }*/

    @Override
    public void onSizeChanged() {
        if (mInverseSelection) {
            selectAll();
        }
    }

    @Override
    public void onUpdateContent() {

    }

    public interface SelectionListener {
        public void onSelectionModeChange(int mode);
        public void onSelectionChange(Path path, boolean selected);
        /// M: [BEHAVIOR.ADD] @{
        public void onSelectionRestoreDone();
        /// @}
    }

    public SelectionManager(AbstractGalleryActivity activity, boolean isAlbumSet) {
        /// M: [BEHAVIOR.ADD] @{
        mActivity = activity;
        mMainHandler = new Handler(activity.getMainLooper());
        /// @}
        mDataManager = activity.getDataManager();
        mClickedSet = new HashSet<Path>();
        mSlideSet = new HashSet<Path>();
        mIsAlbumSet = isAlbumSet;
        mTotal = -1;
        mTotalSelectable = -1;
    }

    // Whether we will leave selection mode automatically once the number of
    // selected items is down to zero.
    public void setAutoLeaveSelectionMode(boolean enable) {
        mAutoLeave = enable;
    }

    public void setSelectionListener(SelectionListener listener) {
        mListener = listener;
    }

    public void selectAll() {
        LogUtil.i(TAG, "<selectAll>");
        mInverseSelection = true;
        mClickedSet.clear();
        mTotal = -1;
        mTotalSelectable = -1;
        enterSelectionMode();
        if (mListener != null) mListener.onSelectionModeChange(SELECT_ALL_MODE);
    }

    public void deSelectAll() {
        LogUtil.i(TAG, "<deSelectAll>");
        /// M: [BEHAVIOR.MARK] @{
        // when click deselect all in menu, not leave selection mode
        /* leaveSelectionMode(); */
        /// @}
        mInverseSelection = false;
        mClickedSet.clear();
        /// M: [BEHAVIOR.ADD] @{
        // when click deselect all in menu, not leave selection mode
        if (mListener != null) {
            mListener.onSelectionModeChange(DESELECT_ALL_MODE);
        }
        /// @}
    }

    public boolean inSelectAllMode() {
        /// M: [BUG.ADD] @{
        // Not in select all mode, if not all items are selected now
        if (getTotalSelectableCount() != 0) {
            return getTotalSelectableCount() == getSelectedCount();
        }
        /// @}
        return mInverseSelection;
    }

    public boolean inSelectionMode() {
        return mInSelectionMode;
    }

    public void enterSelectionMode() {
        if (mInSelectionMode) return;
        LogUtil.i(TAG, "<enterSelectionMode>");
        mInSelectionMode = true;
        if (mListener != null) mListener.onSelectionModeChange(ENTER_SELECTION_MODE);
        if (mTimeLineDataLoader != null) {
            mTimeLineDataLoader.setDataListener(this);
        }
    }

    public void leaveSelectionMode() {
        if (!mInSelectionMode) return;

        LogUtil.i(TAG, "<leaveSelectionMode>");
        mInSelectionMode = false;
        mInverseSelection = false;
        mClickedSet.clear();
        mSlideSet.clear();
        //prize-wuliang 20180225 leave Selection Mode reset mTotalSelectable
        mTotalSelectable = -1;
        /// M: [BUG.ADD] @{
        // Clear mTotal so that it will be re-calculated
        // next time user enters selection mode
        mTotal = -1;
        /// @}
        /// M: [BEHAVIOR.ADD] @{
        if (mRestoreSelectionTask != null) {
            mRestoreSelectionTask.cancel();
        }
        /// @}
        if (mListener != null) mListener.onSelectionModeChange(LEAVE_SELECTION_MODE);
		if (mTimeLineDataLoader != null) {
            mTimeLineDataLoader.removeDataListener(this);
        }
    }

    private boolean mIsSlide;
    public void slideControlStart() {
        LogUtil.i(TAG, "&&&&&&slideControlStart");
        mIsSlide = true;
        mSlideSet.clear();
        mSlideSet.addAll(mClickedSet);
    }

    public void slideControlEnd() {
        LogUtil.i(TAG, "&&&&&&slideControlEnd");
        mClickedSet.clear();
        mClickedSet.addAll(mSlideSet);
        mSlideSet.clear();
        mIsSlide = false;
    }

    public void slideControlSelect(boolean isAdd, ArrayList<MediaItem> items) {
        if (items == null) {
            return;
        }
        LogUtil.i(TAG, "&&&&&&slideControlSelect isAdd=" + isAdd + " mInverseSelection=" + mInverseSelection);
        mSlideSet.clear();
        mSlideSet.addAll(mClickedSet);
        LogUtil.i(TAG, "&&&&&&slideControlSelect mSlideSet=" + mSlideSet);
        for (MediaItem item : items) {
            if (item.isSelectable()) {
                if (isAdd) {
                    if (!mInverseSelection) {
                        LogUtil.i(TAG, "&&&&&&slideControlSelect1 item=" + item);
                        if (!mSlideSet.contains(item.getPath())) {
                            mSlideSet.add(item.getPath());
                        }
                    } else {
                        LogUtil.i(TAG, "&&&&&&slideControlSelect2 item=" + item);
                        if (mSlideSet.contains(item.getPath())) {
                            mSlideSet.remove(item.getPath());
                        }
                    }
                } else {
                    if (!mInverseSelection) {
                        LogUtil.i(TAG, "&&&&&&slideControlSelect3 item=" + item);
                        if (mSlideSet.contains(item.getPath())) {
                            mSlideSet.remove(item.getPath());
                        }
                    } else {
                        LogUtil.i(TAG, "&&&&&&slideControlSelect4 item=" + item);
                        if (!mSlideSet.contains(item.getPath())) {
                            mSlideSet.add(item.getPath());
                        }
                    }
                }
            }
        }
        if (mListener != null) mListener.onSelectionChange(null, isAdd);
    }

    public boolean isItemSelected(Path itemId) {
        LogUtil.i(TAG, "&&&&&&isItemSelected mIsSlide=" + mIsSlide + " mClickedSet.size()=" + mClickedSet.size());
        boolean isSelected = mInverseSelection ^ (mIsSlide ? mSlideSet.contains(itemId) : mClickedSet.contains(itemId));
        LogUtil.i(TAG, "&&&&&&isItemSelected end");
        return isSelected;
    }

    public boolean isSelected(Path path) {
        return mInverseSelection ^ mClickedSet.contains(path);
    }
    
    public boolean isTotalSelect(){
    	return (getSelectedCount() == getTotalSelectableCount());
    }
    
    private int getTotalCount() {
        if (mSourceMediaSet == null) return -1;

        if (mTotal < 0) {
            mTotal = mIsAlbumSet
                    ? mSourceMediaSet.getSubMediaSetCount()
                    : mSourceMediaSet.getMediaItemCount();
        }
        return mTotal;
    }

    /**
     * Some items is not selectable. such as Title item in TimeLine.
     *
     * @return total selectable count.
     */
    private int getTotalSelectableCount() {
        if (mSourceMediaSet == null) return -1;
        if (mTotalSelectable < 0) {
            mTotalSelectable = mIsAlbumSet
                    ? mSourceMediaSet.getSubMediaSetCount()
                    : (mSourceMediaSet.getMediaItemCount() - mSourceMediaSet.getSubMediaSetCount());
        }
        return mTotalSelectable;
    }

    public int getSelectedCount() {
        int count = mIsSlide ? mSlideSet.size() : mClickedSet.size();
        if (mInverseSelection) {
            count = getTotalSelectableCount() - count;
        }
        return count;
    }

    public void toggle(Path path) {
        /// M: [DEBUG.ADD] @{
        LogUtil.i(TAG, "<toggle> path = " + path);
        /// @}
        if (mClickedSet.contains(path)) {
            mClickedSet.remove(path);
        } else {
            enterSelectionMode();
            mClickedSet.add(path);
        }

        // Convert to inverse selection mode if everything is selected.
        int count = getSelectedCount();
        if (count == getTotalSelectableCount()) {
            selectAll();
        }

        if (mListener != null) mListener.onSelectionChange(path, isItemSelected(path));
        if (count == 0 && mAutoLeave) {
            leaveSelectionMode();
        }
    }

    public boolean isTimeLineSetAllSelect(Path path) {
        LogUtil.i(TAG, "isTimeLineSetAllSelect path=" + path);
        int selectedCount = 0;
        if (isTotalSelect()) {
            return true;
        } else if ((selectedCount = getSelectedCount()) == 0) {
            return false;
        }
        MediaSet targetSet = mActivity.getDataManager().getMediaSet(path);
        if (targetSet == null) return false;
        ClusterAlbum clusterAlbum = ((ClusterAlbum)targetSet);
        if (selectedCount < clusterAlbum.getSelectableItemCount()) {
            return false;
        }
        ArrayList<Path> paths = clusterAlbum.getMediaItems();
        LogUtil.i(TAG, "isTimeLineSetAllSelect paths.size=" + paths.size());
        boolean isTitleAll = isTitleAll(paths);
        LogUtil.i(TAG, "isTimeLineSetAllSelect mClickedSet.size=" + mClickedSet.size() + " mInverseSelection=" + mInverseSelection + " isTitleAll=" + isTitleAll);
        return isTitleAll;
    }

    private boolean isTitleAll(ArrayList<Path> paths) {
        if (mInverseSelection) {
			boolean isAll = false;
            Set<Path> set = new HashSet<Path>();
			lock.lock();
		    try{
				set.addAll(getSelectSet());
				int preSize = getSelectSet().size() + paths.size();
				set.addAll(paths);
				isAll = preSize == set.size();
		    }catch(Exception e){
				isAll = false;
		    }
			finally{
				set.clear();
				lock.unlock();
		    }
            LogUtil.i(TAG, "isTimeLineSetAllSelect isAll=" + isAll);
            return isAll;
        } else {
            return mClickedSet.containsAll(paths);
        }
    }

    public void toggleTimeLineSet(ArrayList<Path> paths) {
        boolean isAll = isTitleAll(paths);
        if (isAll && mInverseSelection) {
            mClickedSet.addAll(paths);
        } else if (isAll && !mInverseSelection) {
            mClickedSet.removeAll(paths);
        } else if (!isAll && !mInverseSelection) {
            mClickedSet.addAll(paths);
            enterSelectionMode();
        } else {
            mClickedSet.removeAll(paths);
            enterSelectionMode();
        }
        int count = getSelectedCount();
        if (count == getTotalSelectableCount()) {
            selectAll();
        }
        if (mListener != null) mListener.onSelectionChange(paths.get(0), isItemSelected(paths.get(0)));
        if (count == 0 && mAutoLeave)
            leaveSelectionMode();
    }
    private static boolean expandMediaSet(ArrayList<Path> items, MediaSet set, int maxSelection) {
        int subCount = set.getSubMediaSetCount();
        for (int i = 0; i < subCount; i++) {
            if (!expandMediaSet(items, set.getSubMediaSet(i), maxSelection)) {
                return false;
            }
        }
        int total = set.getMediaItemCount();
        int batch = 50;
        int index = 0;

        while (index < total) {
            int count = index + batch < total
                    ? batch
                    : total - index;
            ArrayList<MediaItem> list = set.getMediaItem(index, count);
            if (list != null
                    && list.size() > (maxSelection - items.size())) {
                return false;
            }
            for (MediaItem item : list) {
                items.add(item.getPath());
            }
            index += batch;
        }
        return true;
    }

    public ArrayList<Path> getSelected(boolean expandSet) {
        return getSelected(expandSet, Integer.MAX_VALUE);
    }

    private Set<Path> getSelectSet() {
        return mIsSlide ? mSlideSet : mClickedSet;
    }

    /// M: [BUG.MODIFY] @{
    /*
    public ArrayList<Path> getSelected(boolean expandSet, int maxSelection) {
        ArrayList<Path> selected = new ArrayList<Path>();
    */
    public ArrayList<Path> getSelected(boolean expandSet, final int maxSelection) {
        final ArrayList<Path> selected = new ArrayList<Path>();
        Set<Path> clickedSet = getSelectSet();
    /// @}
        if (mIsAlbumSet) {
            if (mInverseSelection) {
                int total = getTotalCount();
                for (int i = 0; i < total; i++) {
                    MediaSet set = mSourceMediaSet.getSubMediaSet(i);
                    /// M: [BUG.ADD] if set is null, should continue and return directly. @{
                    if (set == null) {
                        continue;
                    }
                    /// @}
                    Path id = set.getPath();
                    if (!clickedSet.contains(id)) {
                        if (expandSet) {
                            if (!expandMediaSet(selected, set, maxSelection)) {
                                return null;
                            }
                        } else {
                            addPathIfSelectable(selected, id);
                            if (selected.size() > maxSelection) {
                                return null;
                            }
                        }
                    }
                }
            } else {
                for (Path id : clickedSet) {
                    if (expandSet) {
                    	try {
                            if (!expandMediaSet(selected, mDataManager.getMediaSet(id),
                                    maxSelection)) {
                                return null;
                            }
						} catch (Exception e) {
							// TODO: handle exception
							return null;
						}
       
                    } else {
                        addPathIfSelectable(selected, id);
                        if (selected.size() > maxSelection) {
                            return null;
                        }
                    }
                }
            }
        } else {
            if (mInverseSelection) {
                int total = getTotalCount();

                int index = 0;
                while (index < total) {
                    int count = Math.min(total - index, MediaSet.MEDIAITEM_BATCH_FETCH_COUNT);
                    ArrayList<MediaItem> list = mSourceMediaSet.getMediaItem(index, count);
                    for (MediaItem item : list) {
                    	if(item == null){
                    		continue;
                    	}
                        Path id = item.getPath();
                        if (!clickedSet.contains(id)) {
                            addPathIfSelectable(selected, id);
                            if (selected.size() > maxSelection) {
                                return null;
                            }
                        }
                    }
                    index += count;
                }
            } else {
                /// M: [BUG.MODIFY] @{
                /*
                for (Path id : mClickedSet) {
                    addPathIfSelectable(selected, id);
                    if (selected.size() > maxSelection) {
                        return null;
                    }
                }
                */
                // Check if items in click set are still in mSourceMediaSet,
                // if not, we do not add it to selected list.
                ArrayList<Path> selectedPathTemple = new ArrayList<Path>();
                selectedPathTemple.addAll(clickedSet);
                mDataManager.mapMediaItems(selectedPathTemple, new MediaSet.ItemConsumer() {
                    public void consume(int index, MediaItem item) {
                        if (selected.size() < maxSelection && item != null) {
                            addPathIfSelectable(selected, item.getPath());
                        }
                    }

                    /// M: [BUG.ADD] @{
                    @Override
                    public boolean stopConsume() {
                        return false;
                    }
                    /// @}
                }, 0);
                /// @}
            }
        }
        return selected;
    }

    public void setSourceMediaSet(MediaSet set) {
        mSourceMediaSet = set;
        mTotal = -1;
        //prize-wuliang 20180225 MediaSet Changed reset mTotalSelectable
        mTotalSelectable = -1;
    }


    //********************************************************************
    //*                              MTK                                 *
    //********************************************************************

    ArrayList<Path> mPrepared;
    public static final Object LOCK = new Object();

    // Save and restore selection in thread pool to avoid ANR
    private AbstractGalleryActivity mActivity = null;
    private final Handler mMainHandler;
    private ArrayList<Path> mSelectionPath = null;
    private ArrayList<Long> mSelectionGroupId = null;
    private Future<?> mSaveSelectionTask;
    private Future<?> mRestoreSelectionTask;

    public ArrayList<Path> getPrepared() {
        return mPrepared;
    }

    public void setPrepared(ArrayList<Path> prepared) {
        mPrepared = prepared;
    }

    public boolean contains(Path path) {
        if (inSelectAllMode()) {
            return true;
        }
        return mClickedSet.contains(path);
    }

    public void onSourceContentChanged() {
        // reset and reload total count since source set data has changed
        mTotal = -1;
        //prize-wuliang 20180225 Content Changed reset mTotalSelectable
        mTotalSelectable = -1;
        int count = getTotalSelectableCount();
        LogUtil.d(TAG, "<onSourceContentChanged> New total=" + count);
        if (count == 0) {
            leaveSelectionMode();
        }
    }

    // used by ActionModeHandler computeShareIntent
    public ArrayList<Path> getSelected(JobContext jc, boolean expandSet, final int maxSelection) {
        final ArrayList<Path> selected = new ArrayList<Path>();
        Set<Path> clickedSet = getSelectSet();
        if (mIsAlbumSet) {
            if (mInverseSelection) {
                int total = getTotalCount();
                for (int i = 0; i < total; i++) {
                    if (jc.isCancelled()) {
                        LogUtil.i(TAG, "<getSelected> jc.isCancelled() - 1");
                        return null;
                    }
                    MediaSet set = mSourceMediaSet.getSubMediaSet(i);
                    // if set is null, should continue and return directly.
                    if (set == null) {
                        continue;
                    }
                    Path id = set.getPath();
                    if (!clickedSet.contains(id)) {
                        if (expandSet) {
                            if (!expandMediaSet(jc, selected, set, maxSelection)) {
                                return null;
                            }
                        } else {
                            addPathIfSelectable(selected, id);
                            if (selected.size() > maxSelection) {
                                return null;
                            }
                        }
                    }
                }
            } else {
                for (Path id : clickedSet) {
                    if (jc.isCancelled()) {
                        LogUtil.i(TAG, "<getSelected> jc.isCancelled() - 2");
                        return null;
                    }
                    if (expandSet) {
                        if (!expandMediaSet(jc, selected, mDataManager.getMediaSet(id),
                                maxSelection)) {
                            return null;
                        }
                    } else {
                        addPathIfSelectable(selected, id);
                        if (selected.size() > maxSelection) {
                            return null;
                        }
                    }
                }
            }
        } else {
            if (mInverseSelection) {
                int total = getTotalCount();
                int index = 0;
                while (index < total) {
                    int count = Math.min(total - index, MediaSet.MEDIAITEM_BATCH_FETCH_COUNT);
                    ArrayList<MediaItem> list = mSourceMediaSet.getMediaItem(index, count);
                    for (MediaItem item : list) {
                        if (jc.isCancelled()) {
                            LogUtil.i(TAG, "<getSelected> jc.isCancelled() - 3");
                            return null;
                        }
                        Path id = item.getPath();
                        if (!clickedSet.contains(id)) {
                            addPathIfSelectable(selected, id);
                            if (selected.size() > maxSelection) {
                                return null;
                            }
                        }
                    }
                    index += count;
                }
            } else {
                //  we check if items in click set are still in mSourceMediaSet,
                // if not, we do not add it to selected list.
                ArrayList<Path> selectedPathTemple = new ArrayList<Path>();
                selectedPathTemple.addAll(clickedSet);
                mDataManager.mapMediaItems(selectedPathTemple, new MediaSet.ItemConsumer() {
                    public void consume(int index, MediaItem item) {
                        if (selected.size() < maxSelection) {
                            addPathIfSelectable(selected, item.getPath());
                        }
                    }

                    @Override
                    public boolean stopConsume() {
                        return false;
                    }
                }, 0);
            }
        }
        return selected;
    }

    private static boolean expandMediaSet(JobContext jc, ArrayList<Path> items, MediaSet set, int maxSelection) {
        if (jc.isCancelled()) {
            LogUtil.i(TAG, "<expandMediaSet> jc.isCancelled() - 1");
            return false;
        }
        if (set == null) {
            LogUtil.i(TAG, "<expandMediaSet> set == null, return false");
            return false;
        }
        int subCount = set.getSubMediaSetCount();
        for (int i = 0; i < subCount; i++) {
            if (jc.isCancelled()) {
                LogUtil.i(TAG, "<expandMediaSet> jc.isCancelled() - 2");
                return false;
            }
            if (!expandMediaSet(items, set.getSubMediaSet(i), maxSelection)) {
                return false;
            }
        }
        int total = set.getMediaItemCount();
        int batch = 50;
        int index = 0;

        while (index < total) {
            if (jc.isCancelled()) {
                LogUtil.i(TAG, "<expandMediaSet> jc.isCancelled() - 3");
                return false;
            }
            int count = index + batch < total
                    ? batch
                    : total - index;
            ArrayList<MediaItem> list = set.getMediaItem(index, count);
            if (list != null
                    && list.size() > (maxSelection - items.size())) {
                return false;
            }
            for (MediaItem item : list) {
                if (jc.isCancelled()) {
                    LogUtil.i(TAG, "<expandMediaSet> jc.isCancelled() - 4");
                    return false;
                }
                items.add(item.getPath());
            }
            index += batch;
        }
        return true;
    }
	
	private void addPathIfSelectable(ArrayList<Path> selected, Path path) {
        if (mDataManager != null) {
            MediaObject mediaObject = mDataManager.getMediaObject(path);
            if (mediaObject != null && mediaObject.isSelectable()) {
                selected.add(path);
            }
        }
    }
	
	public void setTimeLineDataLoader(TimeLineDataLoader loader) {
        mTimeLineDataLoader = loader;
    }

    // do save and restore selection in thread pool to avoid ANR @{
    public void saveSelection() {
        if (mSaveSelectionTask != null) {
            mSaveSelectionTask.cancel();
        }
        LogUtil.i(TAG, "<saveSelection> submit task");
        mSaveSelectionTask = mActivity.getThreadPool().submit(new Job<Void>() {
            @Override
            public Void run(final JobContext jc) {
                synchronized (LOCK) {
                    exitInverseSelectionAfterSave();
                    return null;
                }
            }
        });
    }

    private void exitInverseSelectionAfterSave() {
        LogUtil.i(TAG, "exitInverseSelectionAfterSave");
        if (mInverseSelection && mSelectionPath != null) {
            mClickedSet.clear();
            int restoreSize = mSelectionPath.size();
            for (int i = 0; i < restoreSize; i++) {
                mClickedSet.add(mSelectionPath.get(i));
            }
            mInverseSelection = false;
        }
    }

    private class RestoreSelectionJobListener implements FutureListener<Void> {
        @Override
        public void onFutureDone(Future<Void> future) {
            mMainHandler.post(new Runnable() {
                @Override
                public void run() {
                    mListener.onSelectionRestoreDone();
                }
            });
        }
    }

    private class RestoreSelectionJob implements Job<Void> {
        @Override
        public Void run(final JobContext jc) {
            synchronized (LOCK) {
                LogUtil.i(TAG, "<restoreSelection> task begin");
                if (jc.isCancelled()) {
                    LogUtil.i(TAG, "<restoreSelection> task cancelledin job run 1");
                    return null;
                }
                if (mSourceMediaSet == null || mSelectionPath == null) {
                    return null;
                }
                mTotal = mIsAlbumSet ? mSourceMediaSet.getSubMediaSetCount() : mSourceMediaSet
                        .getMediaItemCount();
                Path path = null;
                Set<Path> availablePaths = new HashSet<Path>();
                // remove dirty entry
                if (mIsAlbumSet) {
                    MediaSet set = null;
                    for (int i = 0; i < mTotal; ++i) {
                        set = mSourceMediaSet.getSubMediaSet(i);
                        if (jc.isCancelled()) {
                            LogUtil.i(TAG, "<restoreSelection> task cancelled, in job run 2");
                            return null;
                        }
                        if (set != null) {
                            path = set.getPath();
                            if (mSelectionPath.contains(path)) {
                                availablePaths.add(path);
                            }
                        }
                    }
                } else {
                    ArrayList<MediaItem> items = mSourceMediaSet.getMediaItem(0, mTotal);
                    if (items != null && items.size() > 0) {
                        for (MediaItem item : items) {
                            if (jc.isCancelled()) {
                                LogUtil.i(TAG, "<restoreSelection> task cancelledin job run 3");
                                return null;
                            }
                            path = item.getPath();
                            if (mSelectionPath.contains(path)) {
                                availablePaths.add(path);
                            /// restore continuous shot group whose first image has changed @{
                            } else if (mSelectionGroupId != null
                                    && !(mSourceMediaSet instanceof ContainerSet)
                                    && item.getMediaData() != null
                                    && item.getMediaData().mediaType ==
                                        MediaData.MediaType.CONTAINER) {
                                long groupId = item.getMediaData().groupID;
                                if (groupId != 0 && mSelectionGroupId.contains(groupId)) {
                                    LogUtil.i(TAG, "<restoreSelection> add [path] " + path + ", [name] "
                                            + item.getName() + " for conshot");
                                    availablePaths.add(path);
                                }
                            }
                            /// @}
                        }
                    }
                }
                // leave select all mode and set clicked set
                mInverseSelection = false;
                mClickedSet.clear();
                mClickedSet = availablePaths;
                // clear saved selection when done
                mSelectionPath.clear();
                mSelectionPath = null;
                if (mSelectionGroupId != null) {
                    mSelectionGroupId.clear();
                    mSelectionGroupId = null;
                }
                LogUtil.i(TAG, "<restoreSelection> task end");
                return null;
            }
        }
    }

    public void restoreSelection() {
        if (mRestoreSelectionTask != null) {
            mRestoreSelectionTask.cancel();
        }
        LogUtil.i(TAG, "<restoreSelection> submit task");
        mRestoreSelectionTask = mActivity.getThreadPool().submit(new RestoreSelectionJob(),
                new RestoreSelectionJobListener());
    }

	public void setIsAlbumSet(boolean mIsAlbumSet) {
		this.mIsAlbumSet = mIsAlbumSet;
	}
	
}
