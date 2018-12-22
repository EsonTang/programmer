/*
 * Copyright (C) 2008 The Android Open Source Project
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

package com.android.launcher3;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.R.integer;
import android.content.ClipData.Item;
import android.content.ComponentName;
import android.content.ContentValues;
import android.os.AsyncTask;
import android.util.Log;

/**
 * Represents a folder containing shortcuts or apps.
 */
public class FolderInfo extends ItemInfo {

    /**
     * Whether this folder has been opened
     */
    boolean opened;

    /**
     * The apps and shortcuts
     */
    ArrayList<ShortcutInfo> contents = new ArrayList<ShortcutInfo>();
    ArrayList<ComponentName> cns = new ArrayList<ComponentName>();
    AppsInvalidateTask mTask ;

    ArrayList<FolderListener> listeners = new ArrayList<FolderListener>();

    public static  enum State {
    	NORMAL,
    	EDIT
    }

    FolderInfo() {
        itemType = LauncherSettings.Favorites.ITEM_TYPE_FOLDER;
    }

    /**
     * Add an app or shortcut
     *
     * @param item
     */
    public void add(ShortcutInfo item) {
    	ComponentName cn = item.getIntent().getComponent();		
    	if (!cns.contains(cn)) {
            contents.add(item);
            cns.add(item.intent.getComponent());
            for (int i = 0; i < listeners.size(); i++) {
                listeners.get(i).onAdd(item);
            }
            itemsChanged();
    	}
    }
    
	public void clean(Runnable callback) {
		List<ShortcutInfo> s = new ArrayList<ShortcutInfo>(contents);
		AppsInvalidateTask task = new AppsInvalidateTask(mCleanTask);
		mCleanTask.setAsyncTask(task);
		mCleanTask.setCallback(callback);
		task.execute(s);
		cns.clear();
	}
	
	
	interface AsyncTaskCallback {
		void run(List<ShortcutInfo> items);
		void setAsyncTask(AppsInvalidateTask task);
		void onProgressUpdate(ShortcutInfo... values);
		void onPostExecute(ShortcutInfo result);
		 void setCallback(Runnable callback);
	}
	
	AsyncTaskCallback mCleanTask = new AsyncTaskCallback() {
		AppsInvalidateTask mTask;
		@Override
		public void run(List<ShortcutInfo> items) {
			for (ShortcutInfo info : items) {
				mTask.doPublishProgress(info);
			}
			
		}

		private Runnable callback;

		public void setCallback(Runnable callback) {
			this.callback = callback;
		}
		@Override
		public void setAsyncTask(AppsInvalidateTask task) {
			mTask = task;
		}

		@Override
		public void onProgressUpdate(ShortcutInfo... values) {
			FolderInfo.this.remove((ShortcutInfo) values[0],
					FolderInfo.State.NORMAL);
			
		}

		@Override
		public void onPostExecute(ShortcutInfo result) {
			callback.run();
			
		}
	};
	
	AsyncTaskCallback mAddsTask = new AsyncTaskCallback() {
		AppsInvalidateTask mTask;
		@Override
		public void run(List<ShortcutInfo> items) {

	    	for(ShortcutInfo info : items) {
	    			info.select = false;
	    			mTask.doPublishProgress(info);
	    		}
		}

		@Override
		public void setAsyncTask(AppsInvalidateTask task) {
			mTask = task;
		}

		@Override
		public void onProgressUpdate(ShortcutInfo... values) {
		
	//add by zhouerlong begin 20150814
			ShortcutInfo info = values[0];
			add(info);
			updateWorkspace(info);
			
	//add by zhouerlong end 20150814

		}

		@Override
		public void onPostExecute(ShortcutInfo result) {
			
		}

		@Override
		public void setCallback(Runnable callback) {
			
		}
	};
	
	//add by zhouerlong begin 20150814
	/**过滤删除图标
	 * @param infos
	 */
	public void updateFilterToWorkspace(ArrayList<ItemInfo> infos) {

		if (listeners.get(0) != null && listeners.get(0) instanceof Folder) {
			Folder f = (Folder) listeners.get(0);
			for(ItemInfo info :infos) {

	            remove((ShortcutInfo)info,State.NORMAL);//M by zhouerlong
				f.getmLauncher().bindFolderItemsToAddWorkspace(info);
			}
		}
	}

	/**更新桌面 也就是如果文件夹编辑时候 增加的图标 默认在桌面需要删除 不然就重复啦
	 * @param info
	 */
	public void updateWorkspace(ShortcutInfo info) {
		if (listeners.get(0) != null && listeners.get(0) instanceof Folder) {
			Folder f = (Folder) listeners.get(0);

			f.getWorkspace()
					.removeViewtoFolder(info.getIntent().getComponent());
		}
	}

	//add by zhouerlong end 20150814
	/**
	 * @author kxd * {@value} 同步处理数据
	 */
	class AppsInvalidateTask extends
			AsyncTask<List<ShortcutInfo>, ShortcutInfo, ShortcutInfo> {
		AsyncTaskCallback mOptionTask;
		@Override
		protected void onCancelled() {
			// TODO Auto-generated method stub
			super.onCancelled();
		}
		
		
		public AppsInvalidateTask(AsyncTaskCallback mOptionTask) {
			super();
			this.mOptionTask = mOptionTask;
		}


		public AsyncTaskCallback getmOptionTask() {
			return mOptionTask;
		}


		public void setmOptionTask(AsyncTaskCallback mOptionTask) {
			this.mOptionTask = mOptionTask;
		}


		@Override
		protected void onPostExecute(ShortcutInfo result) {
			super.onPostExecute(result);
			this.mOptionTask.onPostExecute(result);
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected void onProgressUpdate(ShortcutInfo... values) {
			super.onProgressUpdate(values);
			mOptionTask.onProgressUpdate(values);
		}
        public void doPublishProgress(ShortcutInfo info) {
        	publishProgress(info);
        }
		@Override
		protected ShortcutInfo doInBackground(List<ShortcutInfo>... items) {
			mOptionTask.run(items[0]);
			return null;
		}

	}
	

	//mdf by zhouerlong begin 20150814
	public void adds(List<ShortcutInfo> infos, List<ShortcutInfo> filters) {
		if (infos.size() == 0) {
			return;
		}
		AppsInvalidateTask task = new AppsInvalidateTask(mAddsTask);
		mAddsTask.setAsyncTask(task);

		task.execute(infos);// add by zhouerlong
		ArrayList<ItemInfo> info = new ArrayList<>();
		info.addAll(filters);
		updateFilterToWorkspace(info);
	}

	//mdf by zhouerlong end 20150814
    /**
     * Remove an app or shortcut. Does not change the DB.
     *
     * @param item
     * @return 
     */
    //add by zel 读取文件夹所有的 Short Cut Info
    public ArrayList<ShortcutInfo> getContents() {
    	return contents;
    }
  //add by zel 读取文件夹所有的 Short Cut Info
    public ArrayList<ComponentName> getComponentNames() {
    	return cns;
    }
    public void remove(ShortcutInfo item,FolderInfo.State state) {
    	if (item !=null) {
            contents.remove(item);
            cns.remove(item.getIntent().getComponent());
    	}
        for (int i = 0; i < listeners.size(); i++) {
            listeners.get(i).onRemove(item,state);
        }
        itemsChanged();
    }

    public void setTitle(CharSequence title) {
        this.title = title;
        for (int i = 0; i < listeners.size(); i++) {
            listeners.get(i).onTitleChanged(title);
        }
    }
    public void setTitleId(int id) {
    	this.title_id = id;
    }

    @Override
    void onAddToDatabase(ContentValues values) {
        super.onAddToDatabase(values);
        values.put(LauncherSettings.Favorites.TITLE, title.toString());
        values.put(LauncherSettings.Favorites.TITLE_ID, this.title_id);
    }

    void addListener(FolderListener listener) {
        listeners.add(listener);
    }

    void removeListener(FolderListener listener) {
        if (listeners.contains(listener)) {
            listeners.remove(listener);
        }
    }

    void itemsChanged() {
        for (int i = 0; i < listeners.size(); i++) {
            listeners.get(i).onItemsChanged();
        }
    }

    @Override
    void unbind() {
        super.unbind();
        listeners.clear();
    }

    interface FolderListener {
        public void onAdd(ShortcutInfo item);
        public void onRemove(ShortcutInfo item,State state);
        public void onTitleChanged(CharSequence title);
        public void onItemsChanged();
    }

    @Override
    public String toString() {
        return "FolderInfo(id=" + this.id + " type=" + this.itemType
                + " container=" + this.container + " screen=" + screenId
                + " cellX=" + cellX + " cellY=" + cellY + " spanX=" + spanX
                + " spanY=" + spanY + " dropPos=" + dropPos + ")";
    }
}
