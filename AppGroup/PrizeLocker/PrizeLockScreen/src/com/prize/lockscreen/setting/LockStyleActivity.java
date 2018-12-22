package com.prize.lockscreen.setting;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.StatusBarManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.prize.ext.res.LockConfigBean;
import com.prize.lockscreen.utils.SharedPreferencesTool;
import com.prize.prizelockscreen.R;
/***
 * 锁屏样式
 * @author fanjunchen
 *
 */
public class LockStyleActivity extends Activity implements OnClickListener {

	private GridView mGrid;
	
	private List<StyleData> mDatas = new ArrayList<StyleData>();
	
	private LayoutInflater mInflate;
	
	private int mStyleType;
	
	private StyleAdapter mAdapter;
	@Override
	protected void onCreate(Bundle instance) {
		super.onCreate(instance);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
			Window window = getWindow();
			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			int color = getResources().getColor(R.color.color_title);
			window.setStatusBarColor(color);
		}
		
		setContentView(R.layout.lock_style_all);
		
		WindowManager.LayoutParams lp= getWindow().getAttributes();
        lp.statusBarInverse = StatusBarManager.STATUS_BAR_INVERSE_GRAY;
        getWindow().setAttributes(lp);
        
		init();
	}

	private void init() {
		
		mInflate = LayoutInflater.from(this);
		
		mGrid = (GridView) findViewById(R.id.style_grid);
		
		TextView tv = (TextView)findViewById(R.id.title);
		tv.setText(R.string.lock_style);
		initData();
		
		mAdapter = new StyleAdapter();
		mGrid.setAdapter(mAdapter);
		mGrid.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int pos, long id) {
				jumpToPreview(pos);
			}
		});
	}
	/***
	 * 初始化数据
	 */
	private void initData() {
		StyleData d = new StyleData();
		d.name = getString(R.string.lock_style_default);
		d.styleType = LockConfigBean.DEFAULT_LOCK_TYPE;
		d.imgResId = R.drawable.default_;
		d.sImgResId = R.drawable.default_s;
		d.bgImgResId = R.drawable.default_lock;
		mDatas.add(d);
		
		d = new StyleData();
		d.name = getString(R.string.lock_style_fly);
		d.styleType = LockConfigBean.FLY_LOCK_TYPE;
		d.imgResId = R.drawable.fly_style;
		d.sImgResId = R.drawable.fly_style_s;
		d.bgImgResId = R.drawable.fly_lock;
		mDatas.add(d);
		
		d = new StyleData();
		d.name = getString(R.string.lock_style_blink);
		d.styleType = LockConfigBean.CLOSE_LOCK_TYPE;
		d.imgResId = R.drawable.blink_style;
		d.sImgResId = R.drawable.blink_style_s;
		d.bgImgResId = R.drawable.blink_lock;
		mDatas.add(d);
		
		d = new StyleData();
		d.name = getString(R.string.lock_style_circle);
		d.styleType = LockConfigBean.CIRCLE_LOCK_TYPE;
		d.imgResId = R.drawable.circle_style;
		d.sImgResId = R.drawable.circle_style_s;
		d.bgImgResId = R.drawable.circle_lock;
		mDatas.add(d);
		
		d = new StyleData();
		d.name = getString(R.string.lock_style_fashion);
		d.styleType = LockConfigBean.COLOR_LOCK_TYPE;
		d.imgResId = R.drawable.fashion_style;
		d.sImgResId = R.drawable.fashion_style_s;
		d.bgImgResId = R.drawable.fashion_lock;
		mDatas.add(d);
	}
	
	private void jumpToPreview(int pos) {
		Intent it = new Intent(this, LockStylePreviewActivity.class);
		it.putExtra(LockStylePreviewActivity.P_DATA, mDatas.get(pos));
		
		startActivity(it);
		it = null;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.back:
				finish();
				break;
		}
	}

	/***
	 * 打开预览页面
	 * @param index
	 */
	private void openPreview(int index) {
		Intent intent = new Intent();
		ComponentName cn = new ComponentName(this, OpenLockScreenPwd.class);
		intent.setComponent(cn);
		startActivity(intent);
	}
	@Override
	protected void onResume() {
		super.onResume();
		int t = SharedPreferencesTool.getLockStyle(this);
		if (mStyleType != t) {
			mStyleType = t;
			if (mAdapter != null)
				mAdapter.notifyDataSetChanged();
		}
	}
	/***
	 * 样式 数据适配器
	 * @author june
	 *
	 */
	class StyleAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return mDatas.size();
		}

		@Override
		public Object getItem(int position) {
			return mDatas.get(position);
		}

		@Override
		public long getItemId(int pos) {
			return pos;
		}

		@Override
		public View getView(int pos, View convertView, ViewGroup parent) {
			MyHolder holder;
			if (null == convertView) {
				holder = new MyHolder();
				convertView = mInflate.inflate(R.layout.style_grid_item, null);
				holder.selImg = (ImageView)convertView.findViewById(R.id.img_sel);
				holder.previewImg = (ImageView)convertView.findViewById(R.id.img);
				holder.nameTxt = (TextView)convertView.findViewById(R.id.style_name);
				
				convertView.setTag(holder);
			}
			else {
				holder = (MyHolder)convertView.getTag();
			}
			
			StyleData d = mDatas.get(pos);
			
			holder.nameTxt.setText(d.name);
			if (d.styleType == mStyleType) {
				holder.selImg.setVisibility(View.VISIBLE);
			}
			else
			{
				holder.selImg.setVisibility(View.GONE);
			}
			
			holder.previewImg.setImageResource(d.sImgResId);
			
			return convertView;
		}
	}
	
	static class MyHolder {
		ImageView selImg, previewImg;
		TextView nameTxt;
	}
}
