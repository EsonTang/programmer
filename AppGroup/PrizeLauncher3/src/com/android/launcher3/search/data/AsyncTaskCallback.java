
/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：异步加载接口
 *当前版本：V1.0
 *作	者：zhouerlong
 *完成日期：2015-8-5
 *********************************************/

package com.android.launcher3.search.data;

import java.util.HashMap;
import java.util.List;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.android.launcher3.search.CharacterParser;
import com.android.launcher3.search.GroupMemberBean;

public abstract class AsyncTaskCallback {

	/**
	 * 汉字转换成拼音的类
	 */
	CharacterParser characterParser;
	/**
	 * 每一项的item响应接口方法
	 * @param item
	 */
	public abstract void onClick(GroupMemberBean item);
	public abstract List<GroupMemberBean> doBackground(String reqeust);
	public abstract void excute();
	public abstract void setAsyncTask(RequestDataTask task);

	public abstract void onProgressUpdate(GroupMemberBean... values);

	public abstract void onPostExecute(List<GroupMemberBean> result);

	/**
	 * 此方法是在是配置中调用的
	 * 请查看MyExpandableAdapter getchildView
	 * @param groupPosition 组groupid
	 * @param childPosition 组内item position
	 * @param isLastChild 
	 * @param convertView
	 * @param parent
	 * @param group 组集合
	 * @param child 
	 * @return
	 */
	public abstract View getchildView(final int groupPosition,
			final int childPosition, boolean isLastChild, View convertView,
			ViewGroup parent,List<GroupMemberBean> group,HashMap<Integer,List<GroupMemberBean>> child);

	abstract void setCallback(Runnable callback);

	public GroupMemberBean fillData(String title) {

		GroupMemberBean sortModel = new GroupMemberBean();
		sortModel.setName(title);

		characterParser = CharacterParser.getInstance();
		// 汉字转换成拼音
		String pinyin = characterParser.getSelling(title);
		String sortString = pinyin.substring(0, 1).toLowerCase();

		// 正则表达式，判断首字母是否是英文字母
		if (sortString.matches("[a-z]")) {
			sortModel.setSortLetters(sortString.toLowerCase());
		} else {
			sortModel.setSortLetters("#");
		}

		return sortModel;

	}
}