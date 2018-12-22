package com.prize.boot.appadapter;

import java.util.ArrayList;
import java.util.List;

import org.xutils.x;
import org.xutils.image.ImageOptions;

import com.prize.boot.customui.CheckImageView;
import com.prize.boot.util.OneKeyInstallData.DataEntity.AppsEntity;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.prize.boot.R;

public class OneKeyInsallAdapter extends BaseAdapter {

	private Context mContext;
	//set default image -pengcancan-20160303
	private ImageOptions imageOptions;
	
	private List<AppsEntity> mChecks = new ArrayList<AppsEntity>();
	
	public List<AppsEntity> getChecks() {
		return mChecks;
	}
	
	private List<AppsEntity> mAppDatas;
	class CheckInfo {
		boolean check;
	}

	private ArrayList<CheckInfo> checkPositionlist = new ArrayList<CheckInfo>();

	public OneKeyInsallAdapter(Context context, List<AppsEntity> mAppDatas) {
		super();
		this.mContext = context;
		this.mAppDatas = mAppDatas;
		for (AppsEntity item : mAppDatas) {
			CheckInfo c = new CheckInfo();
			c.check = true;
			checkPositionlist.add(c);
		}
		mChecks.addAll(mAppDatas);
		imageOptions = new ImageOptions.Builder()
                .setImageScaleType(ImageView.ScaleType.CENTER_CROP)
                .setLoadingDrawableId(R.drawable.icon_detail_default)
                .setFailureDrawableId(R.drawable.icon_detail_default)
                .build();
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mAppDatas.size();
	}

	@Override
	public AppsEntity getItem(int position) {
		// TODO Auto-generated method stub
		return mAppDatas.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder mHolder;
		if (convertView == null) {
			mHolder = new ViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(R.layout.dialog_one_key_install_item, parent, false);
			mHolder.mIconIV = (CheckImageView)convertView.findViewById(R.id.appItem_img_id);
			mHolder.mTitleTV =(TextView)convertView.findViewById(R.id.appItem_name_id);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}
		AppsEntity mAppsEntity = getItem(position);
		
		if (!TextUtils.isEmpty(mAppsEntity.getLargeIcon())) {
			x.image().bind(mHolder.mIconIV, mAppsEntity.getLargeIcon(),imageOptions);
		} else {
			if (mAppsEntity.getIconUrl() != null) {
				x.image().bind(mHolder.mIconIV, mAppsEntity.getIconUrl(),imageOptions);
			}
		} 
		mHolder.mIconIV.setTag(mAppsEntity);
		mHolder.mTitleTV.setText(mAppsEntity.getName());

		// 全选
		mHolder.mIconIV.setCheck(checkPositionlist.get(position).check);
		
		mHolder.mIconIV.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				CheckImageView iconView = (CheckImageView) v;
				AppsEntity info = (AppsEntity) iconView.getTag();
				if (!iconView.isCheck()) {
					iconView.setCheck(true);
					checkPositionlist.get(position).check = true;
					if (!mChecks.contains(info)) {
						mChecks.add(info);
					}
				} else {
					if (mChecks.contains(info)) {
						mChecks.remove(info);
					}
					iconView.setCheck(false);
					checkPositionlist.get(position).check = false;
				}
				v.invalidate();
			}
		});
		
		return convertView;
	}

	class ViewHolder {
		private CheckImageView mIconIV;
		private TextView mTitleTV;
	}
}
