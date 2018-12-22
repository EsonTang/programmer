package com.prize.appcenter.ui.adapter;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.prize.appcenter.R;
import com.prize.appcenter.bean.MessageBean;

import java.util.ArrayList;

/**
 * 
 ** 
 * 消息中心adapter
 * 
 * @author zhouerlong
 * @version V1.0
 */
public class MsgCenterAdapter extends BaseAdapter {

	private ArrayList<MessageBean> datas;
	private Context ctx;

	public MsgCenterAdapter(ArrayList<MessageBean> items, Context c) {
		datas = items;
		ctx = c;
	}

	@Override
	public int getCount() {
		if (datas == null) {
			return 0;
		}
		return datas.size();
	}

	@Override
	public MessageBean getItem(int position) {
		return datas.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final ViewHolder viewHolder;
		final MessageBean messageBean = datas.get(position);
		if (convertView == null) {
			viewHolder = new ViewHolder();
			convertView = LayoutInflater.from(ctx).inflate(
					R.layout.msg_center_item, null);
			viewHolder.title = (TextView) convertView.findViewById(R.id.msg_title_tv);
			viewHolder.createTime = (TextView) convertView.findViewById(R.id.msg_create_time_tv);
			viewHolder.content = (TextView) convertView.findViewById(R.id.msg_content_tv);

			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		viewHolder.title.setText(messageBean.title);
		viewHolder.createTime.setText(messageBean.createTime);
		viewHolder.content.setText(Html.fromHtml(getDispalyContent(messageBean)));

		/*if(!TextUtils.isEmpty(messageBean.copyContent)) {
			//StateListDrawable drawable = new StateListDrawable();
//			ColorStateList colorList = createColorStateList(Color.parseColor("#00000000"), Color.parseColor("#aaaaaa"));
//
//			viewHolder.content.setTextColor(colorList);
			viewHolder.content.setOnLongClickListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View view) {
					ClipboardManager cmb = (ClipboardManager)ctx.getSystemService(Context.CLIPBOARD_SERVICE);
					cmb.setText(messageBean.copyContent);
					ToastUtils.showToast(ctx.getResources().getString(R.string.content_copyed));
					return false;
				}
			});
		}else {
			viewHolder.content.setClickable(false);
		}*/

		return convertView;

	}

	private class ViewHolder {
		public TextView title;
		public TextView createTime;
		public TextView content;
	}

	private String getDispalyContent(MessageBean messageBean){
		String dispalyContent = messageBean.content;
		if(dispalyContent.contains("{")){
			//StringBuilder copyContent = new StringBuilder(dipalyContent);
			int indextStart = dispalyContent.indexOf("{");
			int indextEnd = dispalyContent.indexOf("}");

			messageBean.copyContent = dispalyContent.substring(indextStart+1, indextEnd);

			dispalyContent = dispalyContent.replace("{", "<font color='#ff7d5a'>");
			dispalyContent = dispalyContent.replace("}", "</font>");
		}
		return dispalyContent;
	}
}
