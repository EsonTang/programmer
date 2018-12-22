
/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：异步加载请求数据类
 *当前版本：V1.0
 *作	者：zhouerlong
 *完成日期：2015-8-5
 *********************************************/

package com.android.launcher3.search.data;

import java.util.List;

import android.os.AsyncTask;

import com.android.launcher3.search.GroupMemberBean;

public class RequestDataTask extends
		AsyncTask<String, GroupMemberBean, List<GroupMemberBean>> {
	AsyncTaskCallback mOptionTask;

	@Override
	protected List<GroupMemberBean> doInBackground(String... reqeust) {
		return mOptionTask.doBackground(reqeust[0]);

	}

	public void doPublishProgress(GroupMemberBean info) {
		publishProgress(info);
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
	}

	@Override
	protected void onPostExecute(List<GroupMemberBean> result) {
		// TODO Auto-generated method stub
		super.onPostExecute(result);
		mOptionTask.onPostExecute(result);
		
	}

	public RequestDataTask(AsyncTaskCallback mOptionTask) {
		super();
		this.mOptionTask = mOptionTask;
		
	}

	@Override
	protected void onProgressUpdate(GroupMemberBean... values) {
		// TODO Auto-generated method stub
		super.onProgressUpdate(values);
		mOptionTask.onProgressUpdate(values);
	}

}
