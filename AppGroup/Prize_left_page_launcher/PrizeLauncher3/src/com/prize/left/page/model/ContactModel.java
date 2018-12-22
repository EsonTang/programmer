package com.prize.left.page.model;

import java.util.List;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;

import com.android.launcher3.R;
import com.prize.left.page.bean.ContactPerson;
import com.prize.left.page.bean.ContactsCardBean;
import com.prize.left.page.request.ContactsRequest;
import com.prize.left.page.response.ContactsResponse;
import com.prize.left.page.util.DBUtils;
import com.prize.left.page.view.holder.ContactsViewHolder;
/***
 * 搜索联系人业务类(最近联系人)
 * @author fanjunchen
 *
 */
public class ContactModel extends BaseModel<ContactsResponse> {

	private ContactsRequest reqParam;
	
	private ContactsResponse response;
	
	private List<ContactPerson> allQueryData = null;
	
	private ContactsViewHolder viewHolder = null;
	/**是否展开*/
	private boolean isExpand = false;
	/**默认显示的联系人个数*/
	private final int DEFAULT_NUM = 3;
	/**是否正在运行*/
	private boolean isRunning = false;
	
	public ContactModel(Context ctx) {
		mCtx = ctx;
		reqParam = new ContactsRequest();
	}
	/**
	 * 设置查询串
	 * @param q
	 */
	public void setQuery(String q) {
		reqParam.queryStr = q;
	}
	/***
	 * 是否展开
	 * @param isOpen
	 */
	public void setExpand(boolean isOpen) {
		isExpand = isOpen;
		// 需要刷新页面
		if (isExpand)
			viewHolder.setDatas(allQueryData);
		else if (allQueryData != null)
			viewHolder.setDatas(allQueryData.subList(0, DEFAULT_NUM));
		viewHolder.setExpandText(isExpand);
	}
	
	public void setResIdentity(String res) {
	}

	@Override
	public void doGet() {
		// TODO Auto-generated method stub
		doPost();
	}

	@Override
	public void doPost() {
		// TODO Auto-generated method stub
		if (null == cancelObj || cancelObj.isCancelled()) {
			cancelObj = null;
			newHttpCallback();
			if (!isRunning) {
				isRunning = true;
				new GetContactTask().execute();
			}
		}
	}

	@Override
	public void cancel() {
		// TODO Auto-generated method stub
		if (null != cancelObj)
			cancelObj.cancel();
	}

	@Override
	public void onResponse(ContactsResponse resp) {
		// TODO Auto-generated method stub
		if (callback != null)
			callback.onResponse(resp);
		else {
			// 可以处理刷新UI的事情
			if (viewHolder == null)
				return;
			if (allQueryData != null && allQueryData.size() > DEFAULT_NUM)
				viewHolder.setExpandVisible(true);
			else
				viewHolder.setExpandVisible(false);
			viewHolder.setDatas(resp.data.items);
		}
	}
	
	private View.OnClickListener mLsn = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
				case R.id.txt_expand:
					setExpand(!isExpand);
					break;
			}
		}
	};
	
	@Override
	public void setViewHolder(RecyclerView.ViewHolder holder) {
		viewHolder = (ContactsViewHolder) holder;
		
		viewHolder.setExpandClick(mLsn);
	}
	
	@Override
	protected void newHttpCallback() {
		
	}
	/***
	 * 异步任务来查询处理数据
	 * @author fanjunchen
	 *
	 */
	class GetContactTask extends AsyncTask<Void, Void, Void> {
		List<ContactPerson> data = null;
		@Override
		protected Void doInBackground(Void... params) {
			// TODO 取数据
			if (reqParam == null || TextUtils.isEmpty(reqParam.queryStr))
				data = getContacts();
			else {
				data = DBUtils.getQueryContacts(mCtx, reqParam.queryStr);
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			// TODO 取完数据后回调
			if (response == null) {
				response = new ContactsResponse();
				response.data = new ContactsCardBean();
			}
			allQueryData = data;
			if (allQueryData != null && allQueryData.size() > DEFAULT_NUM && !isExpand)
				response.data.items = allQueryData.subList(0, DEFAULT_NUM);
			else
				response.data.items = data;
			onResponse(response);
			isRunning = false;
			super.onPostExecute(result);
		}
	}
	/***
	 * 获取到联系人
	 * @return
	 */
	public List<ContactPerson> getContacts() {
		List<ContactPerson> rs = null;
		rs = DBUtils.getCallRecord(mCtx, DEFAULT_NUM);
		if (null == rs || rs.size() < 1) {
			rs = null;
			rs = DBUtils.getPhoneContacts(mCtx, DEFAULT_NUM, null);
		}
		else {
			int s = rs.size();
			if (s > 0) {
				List<ContactPerson> bb = DBUtils.getPhoneContacts(mCtx, DEFAULT_NUM - s, rs);
				rs.addAll(bb);
			}
		}
		if(rs != null){
			for(ContactPerson person:rs){
				if(DBUtils.getQueryContacts(mCtx, person.phoneNum) != null){
					person.name = DBUtils.getQueryContacts(mCtx, person.phoneNum).get(0).name;
					break;
				}
			}
		}
		return rs;
	}
	
	@Override
	public void doBindImg() {
		if (viewHolder != null) {
			viewHolder.doBindImg();
		}
	}
}
