/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：
 *当前版本：
 *作	者：
 *完成日期：
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
...
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
 *********************************************/

package com.prize.appcenter.ui.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayLargerImageOptions;
import com.prize.app.net.datasource.base.AppCommentData.AppComment;
import com.prize.app.util.JLog;
import com.prize.appcenter.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * 类描述：评论列表adapter
 * 
 * @author huanglingjun
 * @version 版本
 */
public class AppCommentAdapter extends BaseAdapter {

	private Context mCtx;
	private ArrayList<AppComment> datas = new ArrayList<AppComment>();
	private int[] heads = { R.drawable.header_one, R.drawable.header_two,
			R.drawable.header_three, R.drawable.header_four };
	private DisplayLargerImageOptions options;
	private int mItemCount = 0;
	private boolean isDetail;

	public AppCommentAdapter(Context mCtx) {
		super();
		this.mCtx = mCtx;
		if (this.mCtx != null) {
			options = new DisplayLargerImageOptions.Builder().showImageOnFail(
					R.drawable.comment_default_icon).build();
		}
	}
	
//	public void setIsDetail(boolean isDetail) {
//		this.isDetail = isDetail;
//	}
	public void setCount(int itemCount) {
		if (itemCount <= 0)
			return;
		mItemCount = itemCount;
	}

	@Override
	public int getCount() {
		if (mItemCount > 0) {
			return mItemCount;
		}
		return datas.size();
	}

	@Override
	public Object getItem(int position) {

		return datas.get(position);
	}

	@Override
	public long getItemId(int position) {

		return position;
	}

	/**
	 * 方法描述：分页加载跟多添加数据
	 * 
	 * @param moreDatas ArrayList<AppComment>
	 */
	public void addData(ArrayList<AppComment> moreDatas) {
		if (datas != null && moreDatas != null && moreDatas.size() >= 0) {
			datas.addAll(moreDatas);
		}
		notifyDataSetChanged();
	}

	/**
	 * 方法描述：评论成功后重置数据
	 * 
	 * @param newDatas  ArrayList<AppComment>
	 */
	public void reSetData(ArrayList<AppComment> newDatas) {
		if (datas != null && newDatas != null && newDatas.size() >= 0) {
			datas.clear();
			datas.addAll(newDatas);
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
		if (convertView == null) {
			viewHolder = new ViewHolder();
			convertView = LayoutInflater.from(mCtx).inflate(
					R.layout.item_listview_comment_id, null);
			/*viewHolder.userHeadimg = (CircleImageViewTwo) convertView
					.findViewById(R.id.user_headimg_id);*/
			viewHolder.userName = (TextView) convertView
					.findViewById(R.id.user_id);
			viewHolder.commentTime = (TextView) convertView
					.findViewById(R.id.time_tv);
			viewHolder.commentContent = (TextView) convertView
					.findViewById(R.id.comment_content_id);
			viewHolder.phoneInfo = (TextView) convertView.findViewById(R.id.phone_tv);
			viewHolder.divideLine = convertView.findViewById(R.id.game_image_tag);
			convertView.setTag(viewHolder);
			convertView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		AppComment data = datas.get(position);
		// BitmapMgr.loadBitmapSync(viewHolder.userHeadimg, data.iconUrl,
		// R.drawable.ic_launcher);
		/*if (!TextUtils.isEmpty(data.nickName)) {
			viewHolder.userName.setText(data.nickName);
		} else if (!TextUtils.isEmpty(data.accountId + "")) {
			if (data.accountId == 0) {
				viewHolder.userName.setText(mCtx
						.getString(R.string.anonymity_user));
			} else {
				viewHolder.userName.setText(data.accountId + "");
			}
		} else {
			viewHolder.userName
					.setText(mCtx.getString(R.string.anonymity_user));
		}*/
		if (!TextUtils.isEmpty(data.showName)) {
			viewHolder.userName.setText(data.showName);
		}else {
			viewHolder.userName
					.setText(mCtx.getString(R.string.anonymity_user));
		}

		viewHolder.commentTime.setText(getTimeString(data.createTime));
		viewHolder.commentContent.setText(data.content);
		if (JLog.isDebug) {
			JLog.i("MainActivity","data.model="+data.model+"--data.mobile="+data.mobile+"--");

		}
		viewHolder.phoneInfo.setText((data.model!=null &&!TextUtils.isEmpty(data.mobile)&& data.model.contains(data.mobile))? data.model:
				((data.model != null) ? (data.mobile+" "+data.model).trim() : data.mobile));
		if (isDetail) {
			viewHolder.commentContent.setMaxLines(2);
			viewHolder.divideLine.setVisibility(View.GONE);
		}
//		int index = getIndex();
		/*ImageLoader.getInstance().displayImage(data.avatarUrl, viewHolder.userHeadimg,
				UILimageUtil.getUILoptions(R.drawable.comment_default_icon), null);*/
		// TODO Auto-generated method stub
		return convertView;
	}

//	private int getIndex() {
//		int index = (int) (Math.random() * 4);
//		return index;
//	}

	private String getTimeString(String time){
		String returnValue = "";
		SimpleDateFormat format =  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			long period = (System.currentTimeMillis() - format.parse(time).getTime()) / 1000 /60;
			if(period < 5){
				returnValue = mCtx.getResources().getString(R.string.just_now);
			}else if(period >= 5 && period < 60){
				returnValue = mCtx.getResources().getString(R.string.in_one_hour);
			}else {
				returnValue = time.substring(0, time.length()-3);
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return returnValue;
	}

	private class ViewHolder {
		//public CircleImageViewTwo userHeadimg;
		TextView userName;
		TextView commentTime;
		TextView commentContent;
		View divideLine;
		TextView phoneInfo;
	}
}
