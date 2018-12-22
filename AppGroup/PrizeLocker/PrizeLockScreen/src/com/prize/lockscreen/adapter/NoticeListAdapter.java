package com.prize.lockscreen.adapter;

import java.util.ArrayList;

import com.prize.lockscreen.bean.NoticeBean;
import com.prize.lockscreen.bean.NoticeInfo;
import com.prize.lockscreen.service.MusicHelper;
import com.prize.lockscreen.utils.TimeUtil;
import com.prize.lockscreen.utils.ViewHolder;
import com.prize.prizelockscreen.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.session.PlaybackState;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
/***
 * 通知适配器
 * @author Administrator
 * @modified fanjunchen
 */
public class NoticeListAdapter extends BaseAdapter {

	private ArrayList<NoticeInfo> tempList;
	private LayoutInflater mInflater;

	private Context mContext;
	/**是否可以点击*/
	private static boolean isCanClick = true;

	public NoticeListAdapter(Context context, ArrayList<NoticeInfo> list) {
		mInflater = LayoutInflater.from(context);
		this.tempList = list;
		this.mContext = context;
	}

	public void setData(ArrayList<NoticeInfo> list) {
		this.tempList.addAll(list);
	}

	/***
	 * 删除某个位置的通知
	 * @param position
	 */
	public void remove(int position) {
		tempList.remove(position);
		notifyDataSetChanged();
	}

	/***
	 * 删除所有
	 */
	public void removeAll() {
		tempList.clear();
		notifyDataSetChanged();
	}
	
	@Override
	public int getCount() {
		return this.tempList.size();
	}

	@Override
	public Object getItem(int position) {
		return this.tempList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
	@Override
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		NoticeInfo mNoticeInfo = tempList.get(position);
		NoticeBean mNoticeBean = mNoticeInfo.getNoticeBean();
		if (convertView == null) {
			if (mNoticeInfo.type == NoticeInfo.NORMAL) {
				convertView = mInflater.inflate(R.layout.notice_list_item, parent,
						false);
				convertView.setTag(R.id.title, mNoticeInfo.type);
			}
			else if (mNoticeInfo.type == NoticeInfo.MUSIC){
				convertView = mInflater.inflate(R.layout.notice_music_item, parent,
						false);
				
				convertView.setTag(R.id.title, mNoticeInfo.type);
			}
		}
		else if (mNoticeInfo.type != (Integer)convertView.getTag(R.id.title)) {
			if (mNoticeInfo.type == NoticeInfo.NORMAL) {
				convertView = mInflater.inflate(R.layout.notice_list_item, parent,
						false);
				convertView.setTag(R.id.title, mNoticeInfo.type);
			}
			else if (mNoticeInfo.type == NoticeInfo.MUSIC){
				convertView = mInflater.inflate(R.layout.notice_music_item, parent,
						false);
				
				convertView.setTag(R.id.title, mNoticeInfo.type);
			}
		}

		ImageView mImageView = ViewHolder.get(convertView, R.id.icon);
		TextView mTVtitle = ViewHolder.get(convertView, R.id.title);
		TextView mTVText = ViewHolder.get(convertView, R.id.text);
		
		mImageView.setImageBitmap(setBackground(mContext,
				mNoticeBean.appIcon));
		mTVtitle.setText(mNoticeBean.title);
		mTVText.setText(mNoticeBean.text);
		if (mNoticeInfo.type == NoticeInfo.NORMAL) {
			TextView mTVDate = ViewHolder.get(convertView, R.id.text_date);
	
			mTVDate.setText(TimeUtil.millisecondToTime(mNoticeBean.when));
		}
		else if (mNoticeInfo.type == NoticeInfo.MUSIC) {
			ImageView pre = ViewHolder.get(convertView, R.id.img_pre);
			ImageView play = ViewHolder.get(convertView, R.id.img_play_pause);
			ImageView next = ViewHolder.get(convertView, R.id.img_next);
			
			pre.setOnClickListener(mClickListener);
			play.setOnClickListener(mClickListener);
			next.setOnClickListener(mClickListener);
			
			if (PlaybackState.STATE_PAUSED == mNoticeBean.status) {
				play.setImageResource(R.drawable.media_play_selector);
			}
			else
				play.setImageResource(R.drawable.media_pause_selector);
		}

		return convertView;
	}
	/***
	 * 点击事件
	 */
	private OnClickListener mClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
				case R.id.img_pre:
					if (!isCanClick)
						return ;
					isCanClick = false;
					MusicHelper m = MusicHelper.getInstance();
					if (m != null) {
						m.previous();
					}
					mHandle.sendEmptyMessageDelayed(MSG_CLICK_DELAY, DELAY_200);
					break;
				case R.id.img_next:
					if (!isCanClick)
						return ;
					isCanClick = false;
					m = MusicHelper.getInstance();
					if (m != null) {
						m.next();
					}
					mHandle.sendEmptyMessageDelayed(MSG_CLICK_DELAY, DELAY_200);
					break;
				case R.id.img_play_pause:
					if (!isCanClick)
						return ;
					isCanClick = false;
					m = MusicHelper.getInstance();
					if (m != null) {
						m.playOrPause();
					}
					mHandle.sendEmptyMessageDelayed(MSG_CLICK_DELAY, DELAY_200);
					break;
			}
		}
	};
	
	private final int DELAY_200 = 200;
	private static final int MSG_CLICK_DELAY = 2;
	
	private static final Handler mHandle = new Handler() {
		
		
		
		@Override
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
				case MSG_CLICK_DELAY:
					isCanClick = true;
					break;
			}
		}
	};
	/***
	 * 设置图片并且做缩放
	 * @param context
	 * @param drawable
	 * @return
	 */
	public static Bitmap setBackground(Context context, Drawable drawable) {
		if (drawable == null) {
			drawable= context.getResources().getDrawable(R.drawable.music_icon_default);
		}
		Bitmap APKbitmap;
		if (drawable instanceof BitmapDrawable) {
			APKbitmap = ((BitmapDrawable) drawable).getBitmap();
		} else {
			Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
					drawable.getIntrinsicHeight(), Config.ARGB_8888);
			Canvas canvas = new Canvas(bitmap);
			drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
			drawable.draw(canvas);
			APKbitmap = bitmap;
		}

		int width = APKbitmap.getWidth();
		int height = APKbitmap.getHeight();
		int newsize = context.getResources().getDimensionPixelOffset(
				R.dimen.app_icon_size);

		float scaleWidth = ((float) newsize) / width;
		float scaleHeight = ((float) newsize) / height;
		Matrix matrix = new Matrix();
		matrix.postScale(scaleWidth, scaleHeight);
		Bitmap resultBitmap = Bitmap.createBitmap(APKbitmap, 0, 0, width,
				height, matrix, true);
		return resultBitmap;
	}

}
