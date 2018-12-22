package com.prize.left.page.view.holder;

import java.util.List;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.launcher3.IconCache;
import com.android.launcher3.ImageUtils;
import com.android.launcher3.R;
import com.android.launcher3.Utilities;
import com.lqsoft.LqServiceUpdater.LqService;
import com.prize.left.page.bean.ContactPerson;
import com.prize.left.page.ui.AnimFrameLayout;
import com.prize.left.page.util.CommonUtils;
/**
 * 新闻Holder
 */
public class ContactsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
	
	public TextView titleTxt;

	public TextView txtExpand;
	
	public LinearLayout contents;
	
	private LayoutInflater mInflater = null;
	
	private List<ContactPerson> datas = null;

	private int pos = -1;
	
	private Context mCtx;
	
	private View.OnClickListener mExClick = null;
	
	private View mTitleView;
	
	private Activity mAct = null;
	
	private RecyclerView.Adapter<RecyclerView.ViewHolder> mAdapter;
	
	public void setExpandClick(View.OnClickListener clk) {
		mExClick = clk;
		if (txtExpand != null)
			txtExpand.setOnClickListener(mExClick);
	}
	
	public void setPos(int pos) {
		this.pos = pos;
	}
	
	public void setActivity(Activity a) {
		mAct = a;
	}
	
	public void setAdapter(RecyclerView.Adapter<RecyclerView.ViewHolder> a) {
		mAdapter = a;
	}
	
	/**图片配置器*/
	/*private ImageOptions leftImgOption = new ImageOptions.Builder()
    	.setSize(100, 100)
    	.setImageScaleType(ImageView.ScaleType.CENTER_INSIDE)
    	.build();*/
	
	public ContactsViewHolder(View v) {
		super(v);
		mCtx = v.getContext();
		titleTxt = (TextView) v.findViewById(R.id.txt_title);
		txtExpand = (TextView) v.findViewById(R.id.txt_expand);
		contents = (LinearLayout) v.findViewById(R.id.content);
		
		mTitleView = v.findViewById(R.id.card_title);
		
		mInflater = LayoutInflater.from(mCtx);
		
		txtExpand.setOnClickListener(mExClick);
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.img_sms:
				Integer pos = (Integer)v.getTag();
				if (null == pos || null == datas
						|| datas.size() <= pos)
					return;
				//CommonUtils.jumpToSms(mCtx, datas.get(pos).phoneNum);
				CommonUtils.jumpToSms(mAct, datas.get(pos).phoneNum, R.anim.left_in_from_right);
				break;
			case R.id.img_phone:
				pos = (Integer)v.getTag();
				if (null == pos || null == datas
						|| datas.size() <= pos)
					return;
				CommonUtils.jumpToCallPhone(mAct, datas.get(pos).phoneNum, R.anim.left_in_from_right);//(mCtx, datas.get(pos).phoneNum);
				break;
				
			case R.id.contact_item:
				
				ContactPerson p = (ContactPerson)v.getTag();
				if (p == null)
					return;
				Intent it = new Intent();
				it.setAction(Intent.ACTION_VIEW);
				it.setData(ContentUris.withAppendedId(
						ContactsContract.Contacts.CONTENT_URI, p.contactId));
				mCtx.startActivity(it);
				if (mAct != null)
					mAct.overridePendingTransition(R.anim.left_in_from_right, 0);
				break;
		}
	}
	/***
	 * 设置美食(团购)数据
	 * @param ls
	 */
	public void setDatas(List<ContactPerson> ls) {
		int childSz = contents.getChildCount();
		datas = ls;
		if (null == ls || ls.size() < 1) {
			/*for (int i = childSz - 1; i >=0; i--) {
				contents.removeViewAt(i);
			}*/
			contents.removeAllViews();
			//contents.invalidate();
			//contents.setVisibility(View.GONE);
			mTitleView.setVisibility(View.GONE);
			RecyclerView.LayoutParams p = (RecyclerView.LayoutParams) itemView.getLayoutParams();
			if (p != null)
				p.bottomMargin = 0;
			itemView.setLayoutParams(p);
			itemView.setVisibility(View.GONE);
//			if (mAdapter != null && pos != -1)
//				mAdapter.notifyItemChanged(pos);
			itemView.invalidate();
			return;
		}
		
		RecyclerView.LayoutParams p = (RecyclerView.LayoutParams) itemView.getLayoutParams();
		if (p != null)
			p.bottomMargin = mCtx.getResources().getDimensionPixelSize(R.dimen.search_left_margin);
		//contents.setVisibility(View.VISIBLE);
		itemView.setLayoutParams(p);
		mTitleView.setVisibility(View.VISIBLE);
		itemView.setVisibility(View.VISIBLE);
		
		int dataSize = datas.size();
		
		if (dataSize < childSz) { // 先删除多余的VIEW
			for (int i = dataSize; i < childSz; i++) {
				contents.removeViewAt(dataSize);
			}
		}
		int startIndex = 0;
		// 若有需要 替换VIEW
		int min = Math.min(dataSize, childSz);
		if (min > 0) {
			for (; startIndex < min; startIndex ++) {
				AnimFrameLayout frame = (AnimFrameLayout)contents.getChildAt(startIndex);
				
				View itemView = mInflater.inflate(R.layout.contact_item, null);
				
				initItemView(itemView, startIndex);
				frame.replaceView(itemView, startIndex);
			}
		}
		
		for (; startIndex < dataSize; startIndex ++) {
			AnimFrameLayout frame = (AnimFrameLayout)mInflater.inflate(R.layout.item_base, null);
			
			View itemView = mInflater.inflate(R.layout.contact_item, null);
			
			initItemView(itemView, startIndex);
			View bv = itemView.findViewById(R.id.bottom_line);
			if (startIndex == dataSize - 1) {
				if (bv != null)
					bv.setVisibility(View.INVISIBLE);
			}
			else {
				if (bv != null)
					bv.setVisibility(View.VISIBLE);
			}
			
			frame.replaceView(itemView, startIndex);
			
			contents.addView(frame);
		}
		//itemView.invalidate();
		/*if (mAdapter != null && pos != -1)
			mAdapter.notifyItemChanged(pos);*/
	}

	private ComponentName c=new ComponentName("com.android.contacts", "com.android.contacts.activities.peopleactivity");
	/***
	 * 绑定指定的VIEW
	 * @param v
	 * @param data
	 */
	private void initItemView(final View v, int pos) {
		ContactPerson data = datas.get(pos);
		
		v.setTag(data);
		v.setOnClickListener(this);
		// 左边图片
		final ImageView imgHead = (ImageView)v.findViewById(R.id.img_head);
		// 标题或名称
		TextView txtName = (TextView)v.findViewById(R.id.txt_name);
		txtName.setText(data.name);
		
		TextView txtNum = (TextView)v.findViewById(R.id.txt_phone_num);
		txtNum.setText(data.phoneNum);
		
		View smsV = v.findViewById(R.id.img_sms);
		smsV.setTag(pos);
		smsV.setOnClickListener(this);
		
		View phoneV = v.findViewById(R.id.img_phone);
		phoneV.setTag(pos);
		phoneV.setOnClickListener(this);
		
		
		if (data.headIco != null && !data.headIco.isRecycled()) {
			if(Utilities.isLocalTheme()) {
				AsyncTask<Bitmap, Void, Bitmap> task = new AsyncTask<Bitmap, Void, Bitmap>() {

					@Override
					protected Bitmap doInBackground(Bitmap... arg0) {
						Bitmap result = IconCache.getLqIcon(null, arg0[0], true, null);
						return result;
					}

					@Override
					protected void onPostExecute(Bitmap result) {
						imgHead.setImageBitmap(result);
						super.onPostExecute(result);
					}
					
					
				};
				task.execute(data.headIco);
				
			}else {

				imgHead.setImageResource(R.drawable.ico_person_head);
			}
		}
		else {
			if(Utilities.isLocalTheme()) {
				/*Bitmap headIco = IconCache.getThemeIcon(
						c,
						null, true, null, v.getContext());
				imgHead.setImageBitmap(headIco);*/


				AsyncTask<Bitmap, Void, Bitmap> task = new AsyncTask<Bitmap, Void, Bitmap>() {

					@Override
					protected Bitmap doInBackground(Bitmap... arg0) {

						Bitmap result = IconCache.getThemeIcon(
								c,
								arg0[0], true, null, v.getContext());
						return result;
					}

					@Override
					protected void onPostExecute(Bitmap result) {
						imgHead.setImageBitmap(result);
						super.onPostExecute(result);
					}
					
					
				};
				task.execute(data.headIco);

			

			}else{
				imgHead.setImageBitmap(ImageUtils.drawableToBitmap(v.getContext().getDrawable(R.drawable.ico_person_head)));
			}}
	}
	/**
	 * 加载图片
	 */
	public void doBindImg() {
		int childSz = contents.getChildCount();
		for (int i=0; i < childSz; i ++) {
			AnimFrameLayout frame = (AnimFrameLayout)contents.getChildAt(i);
			ImageView imgLeft = (ImageView)frame.getChildAt(0).findViewById(R.id.img_head);
			if (imgLeft != null) {
				ContactPerson data = datas.get(i);
				if (data != null &&
						data.headIco != null && !data.headIco.isRecycled()) {


					if(data.headIco!=null) {
						data.headIco = IconCache.getLqIcon(null, data.headIco, true, null);
					}
					imgLeft.setImageBitmap(data.headIco);
				}
			}
		}
	}
	/***
	 * 获取常用(最近)联系人数据
	 */
	public List<ContactPerson> getDatas() {
		return datas;
	}
	/***
	 * 设置展开按钮的可见性
	 * @param isVisible
	 */
	public void setExpandVisible(boolean isVisible) {
		if (isVisible)
			txtExpand.setVisibility(View.VISIBLE);
		else
			txtExpand.setVisibility(View.INVISIBLE);
	}
	
	/***
	 * 设置展开按钮的文本
	 * @param isVisible
	 */
	public void setExpandText(boolean isExpand) {
		if (isExpand)
			txtExpand.setText(R.string.str_unexpand);
		else
			txtExpand.setText(R.string.str_expand);
	}
}
