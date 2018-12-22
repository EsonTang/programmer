
/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：便签ListView适配器
 *当前版本：V1.0
 *作	者：朱道鹏
 *完成日期：2015-04-17
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

package com.android.notepad.note;

import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.android.notepad.note.model.NoteEvent;
import com.android.notepad.note.util.DateFormatUtil;
import com.android.notepad.note.util.MediaFile;
import com.android.notepad.note.view.NotePadListView;
import com.android.notepad.R;

import android.annotation.SuppressLint;
import android.app.ActionBar.LayoutParams;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 **
 * 类描述：便签ListView适配器
 * @author 朱道鹏
 * @version V1.0
 */
public class NotePadAdapter extends BaseAdapter {
	private LayoutInflater inflater;
	private ViewHolder mViewHolder;
	public boolean isVisibleFlag;
	private Context mContext;

	/**
	 * 是否选中
	 */
	private HashMap<Integer, Boolean> isSelect;
	private List<NoteEvent> mEventList;
	private int mItemHeight = 0;

	/**
	 * 初始高度（最小高度）为120
	 */
	private static int MIN_HEIGHT = 0;

	@SuppressLint("UseSparseArrays")
	public NotePadAdapter(Context context, List<NoteEvent> eventList, int itemHeight) {
		super();
		mContext = context;
		isSelect = new HashMap<Integer, Boolean>();
		setNoteEventList(eventList);
		mItemHeight = itemHeight;
		
		inflater = LayoutInflater.from(context);	
	}

	@SuppressLint("HandlerLeak")
	public Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == 1) {
				isVisibleFlag = true;
				notifyDataSetInvalidated();
			}else if (msg.what == 2) {
				notifyDataSetInvalidated();
			}
		}

	};

	/**
	 * 方法描述：更新ListView数据
	 * @param List<NoteEvent>
	 * @return void
	 * @see NotePadAdapter#setNoteEventList
	 */
	public void setNoteEventList(List<NoteEvent> eventList){
		if(null != eventList){
			mEventList = eventList;
		}
		isSelect.clear();
		for(int i=0;i<mEventList.size();i++){
			isSelect.put(i, false);
		}
	}

	/**
	 * 方法描述：获得当前Item的高度
	 * @param void
	 * @return int
	 * @see NotePadAdapter#getCurrentItemHeight
	 */
	public int getCurrentItemHeight(){
		if(mItemHeight == 0){
			MIN_HEIGHT = NotePadListView.MIN_HEIGHT;
			mItemHeight = MIN_HEIGHT;
		}
		return mItemHeight;
	}

	/**
	 * 方法描述：设置Item高度
	 * @param int
	 * @return void
	 * @see NotePadAdapter#setItemHeight
	 */
	public void setItemHeight(int height){
		mItemHeight = height;
	}

	@Override
	public int getCount() {
		return mEventList.size();
	}

	@Override
	public NoteEvent getItem(int position) {
		return mEventList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.item_listview, null);
			mViewHolder = new ViewHolder();		
			mViewHolder.guideView = (ImageView) convertView.findViewById(R.id.guide_view);
			mViewHolder.check_box = (CheckBox) convertView.findViewById(R.id.check_box);
			mViewHolder.content_view = (TextView) convertView.findViewById(R.id.id_per_item_summary);
			mViewHolder.time_view = (TextView) convertView.findViewById(R.id.id_per_item_createtime);
			mViewHolder.mLinearLayout =  (LinearLayout) convertView.findViewById(R.id.id_per_item_layout);

			convertView.setTag(mViewHolder);
		} else {
			mViewHolder = (ViewHolder) convertView.getTag();
		}
		NoteEvent event = mEventList.get(position);
		bindView(event);
		CheckBox checkBox = mViewHolder.check_box;
		checkBox.setChecked(isSelect.get(position));
		if(mItemHeight > MIN_HEIGHT){
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
					LayoutParams.WRAP_CONTENT, mItemHeight);
			mViewHolder.mLinearLayout.setLayoutParams(params);
		}
		return convertView;
	}

	@SuppressWarnings("deprecation")
	public void bindView(NoteEvent event) {
		String content = event.getContents();
		long time = event.getCreateDate();
		content = getTheFormatContent(content);
		mViewHolder.content_view.setText(content);
		Time timer = new Time();
		timer.set(time);
		
		ContentResolver resolver = mContext.getContentResolver();
        String strTimeFormat = android.provider.Settings.System.getString(resolver,android.provider.Settings.System.TIME_12_24);
         
        boolean mIsTwentyFourHourSystem = false;
        if(null != strTimeFormat && strTimeFormat.equals("24")){
        	mIsTwentyFourHourSystem = true;
        }else{
        	mIsTwentyFourHourSystem = false;
        }

		//格式化当前时间
		String formatDate = DateFormatUtil.getDateInfoString(mContext, time);
		int hour = timer.hour;
		int minute = timer.minute;
		String amOrPmAndTimeString = DateFormatUtil.getAmOrPmAndTimeString(mContext, hour, minute,mIsTwentyFourHourSystem);
		int weekNum = timer.weekDay;
		String weekDayString = DateFormatUtil.getWeekDayString(mContext, weekNum);

		mViewHolder.time_view.setText(formatDate+"   "+amOrPmAndTimeString+"   "+weekDayString);

		int i = event.getBgColor();		

		switch (i) {
		case 0:
			mViewHolder.mLinearLayout.setBackgroundColor(mContext.getResources().getColor(R.color.transparent));
			break;
		case 1:
			mViewHolder.mLinearLayout.setBackgroundColor(mContext.getResources().getColor(R.color.color_red));
			break;
		case 2:
			mViewHolder.mLinearLayout.setBackgroundColor(mContext.getResources().getColor(R.color.color_yellow));
			break;
		case 3:
			mViewHolder.mLinearLayout.setBackgroundColor(mContext.getResources().getColor(R.color.color_green));
			break;
		case 4:
			mViewHolder.mLinearLayout.setBackgroundColor(mContext.getResources().getColor(R.color.color_blue));
			break;
		case 5:
			mViewHolder.mLinearLayout.setBackgroundColor(mContext.getResources().getColor(R.color.color_while));
			break;
		}		

		if(isVisibleFlag){
			mViewHolder.guideView.setVisibility(View.GONE);
			mViewHolder.check_box.setVisibility(View.VISIBLE);
		}else{
			mViewHolder.guideView.setVisibility(View.VISIBLE);
			mViewHolder.check_box.setVisibility(View.GONE);			
		}	
	}

	/**
	 * 方法描述：获取HashMap对象
	 * @param void
	 * @return HashMap<Integer, Boolean>
	 * @see NotePadAdapter#getIsSelect
	 */
	public HashMap<Integer, Boolean> getIsSelect() {
		return isSelect;
	}

	/**
	 * 方法描述：更新HashMap内容
	 * @param HashMap<Integer, Boolean>
	 * @return void
	 * @see NotePadAdapter#setIsSelect
	 */
	public void setIsSelect(HashMap<Integer, Boolean> isSelect) {
		this.isSelect = isSelect;
	}

	/**
	 * 方法描述：格式化当前相片地址，在列表中统一显示为：[图片]
	 * @param String
	 * @return String
	 * @see NotePadAdapter#getTheFormatContent
	 */
	private String getTheFormatContent(String content){
		Pattern pattern = Pattern.compile(MediaFile.IMAGE_FORMAT_PATTERN);
		Matcher m = pattern.matcher(content);
		String formatContent = content;
		String imageFromat = mContext.getResources().getString(R.string.format_image_path_string);
		String imagePath = null;
		while(m.find()){
			imagePath = m.group();
			formatContent = formatContent.replace(imagePath, imageFromat);
		}
		return formatContent;
	}

	public static class ViewHolder {
		public LinearLayout mLinearLayout;
		public TextView content_view;
		public TextView time_view;
		public CheckBox check_box;
		public ImageView guideView;
	}

}
