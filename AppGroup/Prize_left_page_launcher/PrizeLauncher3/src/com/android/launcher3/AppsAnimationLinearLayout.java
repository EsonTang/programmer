package com.android.launcher3;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.android.launcher3.notify.PreferencesManager;

import android.annotation.SuppressLint;
import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ImageView.ScaleType;
//add by zhouerlong
import android.widget.LinearLayout.LayoutParams;

public class AppsAnimationLinearLayout extends LinearLayout implements
		View.OnClickListener {

	private View mView = null;
	private Launcher mLauncher;

	public AppsAnimationLinearLayout(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
		// TODO Auto-generated method stub
		super.onInitializeAccessibilityNodeInfo(info);
	}

	@Override
	public boolean onInterceptHoverEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		return super.onInterceptHoverEvent(event);
	}


	class ItemInfo {
		public String title;
		public Drawable icon;
		public int position;
	}
	
	/*public List<ItemInfo> getItemInfos() {
		List<ItemInfo> themes = new ArrayList<ItemInfo>();
		ItemInfo def = new ItemInfo();
		def.icon = this.getContext().getResources()
				.getDrawable(R.drawable.scale);
		def.title = this.getContext().getString(R.string.def); //默认

		ItemInfo swirl = new ItemInfo();//旋风
		swirl.icon = this.getContext().getResources()
				.getDrawable(R.drawable.swirl);
		// A by zhouerlong
		swirl.title = this.getContext().getString(R.string.swirl);

		ItemInfo rotateY = new ItemInfo();//Y旋转
		rotateY.icon = this.getContext().getResources()
				.getDrawable(R.drawable.rotatey);
		// A by zhouerlong
		rotateY.title = this.getContext().getString(R.string.rotateY); 

		ItemInfo rotateX = new ItemInfo();//X旋转
		rotateX.icon = this.getContext().getResources()
				.getDrawable(R.drawable.rotatex);
		// A by zhouerlong
		rotateX.title = this.getContext().getString(R.string.rotateX);

		ItemInfo fadeout = new ItemInfo();//从小到大旋转
		fadeout.icon = this.getContext().getResources()
				.getDrawable(R.drawable.fadeout);
		// A by zhouerlong
		fadeout.title = this.getContext().getString(R.string.fadeout);

		ItemInfo random = new ItemInfo();
		random.icon = this.getContext().getResources()
				.getDrawable(R.drawable.random);
		// A by zhouerlong
		random.title = this.getContext().getString(R.string.random);

		themes.add(def);//默认
		themes.add(swirl);//旋风
		themes.add(rotateY);//Y 旋转
		themes.add(rotateX);//X旋转
		themes.add(fadeout);//从小到大
		themes.add(random);//随机

		return themes;

	}*/

	public AppsAnimationLinearLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ThemesAdapter getAdapter(Context context, int resourceid, int id,
			List<ItemInfo> themes) {
		return new ThemesAdapter(context, resourceid, id, themes);
	}

	public void setupThemesFromAdapter(Context context, int resource, int id,
			List<ItemInfo> themes) {
		ThemesAdapter themeAdater = this.getAdapter(context, resource, id,
				themes);
		for (int i = 0; i < themeAdater.getCount(); i++) {

			this.setOrientation(HORIZONTAL);
			this.addView(themeAdater.getView(i, null, null),
						//modify by zhouerlong
					new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,
							LayoutParams.FILL_PARENT));
						//modify by zhouerlong
		}
	}

	public void setLauncher(Launcher launcher) {
		mLauncher = launcher;
	}

	public AppsAnimationLinearLayout(Context context) {
		super(context);
	}

	class HodlerView {
		ImageView themeView;
		TextView textView;
	}

	// add by zhouerlong
	class ThemesAdapter extends ArrayAdapter<ItemInfo> {
		private List<ItemInfo> mThemes;
		private LayoutInflater mInflater;
		private int mResource;

		public ThemesAdapter(Context context, int resource,
				int textViewResourceId, List<ItemInfo> objects) {
			super(context, resource, textViewResourceId, objects);
			this.mResource = resource;
			this.mThemes = objects;
		}

		@Override
		public ItemInfo getItem(int position) {
			// TODO Auto-generated method stub
			return this.mThemes.get(position);
		}

		public void addItemInfo(ItemInfo th) {
			mThemes.add(th);
			notifyDataSetChanged();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			mInflater = LayoutInflater.from(this.getContext());
			HodlerView holder;
			ItemInfo item = this.getItem(position);
			// add by zhouerlong
			item.position = position;
			if (convertView == null) {
				holder = new HodlerView();
				convertView = this.mInflater.inflate(mResource, null);
				holder.themeView = (ImageView) convertView
						.findViewById(R.id.theme_id);
				holder.textView = (TextView) convertView
						.findViewById(R.id.title);
				convertView.setTag(position);
				convertView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
			} else {
				holder = (HodlerView) convertView.getTag();
			}
			if (holder.themeView != null) {
				holder.themeView.setImageDrawable(item.icon);
				holder.themeView.setScaleType(ScaleType.FIT_CENTER);
				// add by zhouerlong
				holder.themeView
						.setOnClickListener(AppsAnimationLinearLayout.this);
				holder.themeView.setTag(position);
			}
			if (holder.textView != null) {
				holder.textView.setText(item.title);
			}
			return convertView;
		}

		@Override
		public int getCount() {
			return super.getCount();
		}

	}

	// add by zhouerlong
	public void setCurrentSelect() {
		this.getChildAt(
				PreferencesManager.getCurrentAnimSelect(this.getContext()))
				.setSelected(true);
		;
	}

	// add by zhouerlong
	public void updateThemeSelect(View v) {
		for (int i = 0; i < this.getChildCount(); i++) {
			this.getChildAt(i).setSelected(false);
		}
		v.setSelected(true);
	}

	

	@Override
	public void onClick(View view) {
		if (mView != null) {
			mView.setSelected(false);
		}
		updateThemeSelect(view);
		mView = view;
		// add by zhouerlong
		int index = (Integer) view.getTag();
		
		PreferencesManager.setCurrentAnimation(this.getContext(), index);// add
																			// by
																			// zhouerlong
		this.mLauncher.OnChangeAnimation(view);

	}

}
