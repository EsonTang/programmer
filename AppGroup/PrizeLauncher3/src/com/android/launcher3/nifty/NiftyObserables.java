/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：被观察者 分发广播类
 *当前版本：V1.0
 *作	者：zhouerlong
 *完成日期：2015-8-8
 *********************************************/
package com.android.launcher3.nifty;

import java.util.ArrayList;
import java.util.List;

import com.android.launcher3.StateInfo;

import android.os.AsyncTask;

/**
 * @author Administrator
 *观察监听
 */
public class NiftyObserables {
	private List<NiftyObservers> mObservers = new ArrayList<NiftyObservers>();

	public void registerObserver(NiftyObservers observer) {
		// M by zhouerlong
		synchronized (mObservers) {
			if (!mObservers.contains(observer)) {
				mObservers.add(observer);

			}
		}
	}
	
	
	/**
	 * 清除所有注册监听
	 */
	public void onDestroy() {
		unregisterAll();
	}

	/**移除单个监听
	 * @param observer
	 */
	public void unregisterObserver(NiftyObservers observer) {
		synchronized (mObservers) {
			mObservers.remove(observer);
		}
	}

	/**
	 * 移除所有监听
	 */
	public void unregisterAll() {
		synchronized (mObservers) {
			mObservers.clear();
		}
	}
  class NotifyTask  extends AsyncTask<Void, NiftyObservers, Boolean> {

	@Override
	protected Boolean doInBackground(Void... params) {
		synchronized (mObservers) {
			for (int i = mObservers.size() - 1; i >= 0; i--) {
				if (i== 0) {
					p.lasted = true;
				}else {
					p.lasted = false;
				}
				publishProgress(mObservers.get(i));
			}
		}
		return true;
	}

	@Override
	protected void onProgressUpdate(NiftyObservers... values) {
		// TODO Auto-generated method stub
		super.onProgressUpdate(values);
		values[0].onChanged(p);
		
	}
	  
  }
  
  StateInfo p = new StateInfo();
	/**
	 * 执行广播时间
	 */
	public void notifyChanged(boolean state) {
		p.state= state;
		
		NotifyTask task = new NotifyTask();
		task.execute();
	}
}
