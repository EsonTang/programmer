package com.android.launcher3;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.launcher3.notify.PreferencesManager;
//add by zhouerlong

public class EffectLinearLayout extends LinearLayout implements
		View.OnClickListener {

	private View mView = null;
	private Launcher mLauncher;

	public EffectLinearLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	class ItemInfo {
		public String title;
		public Drawable icon;
		public int position;
	}

	/*
	 * <item>Normal</item> <item>无转场效果</item> <item>Cross</item>
	 * <item>十字翻转</item> <item>Page</item><item>翻页</item>
	 * <item>Cube(in)</item><item>盒子(内)</item>
	 * <item>Cube(out)</item><item>盒子(外)</item>
	 * <item>Carousel(left)</item><item>旋转木马(左)</item>
	 * <item>Carousel(right)</item> <item>旋转木马(右)</item>
	 */
	public List<ItemInfo> getItemInfos() {
		List<ItemInfo> themes = new ArrayList<ItemInfo>();
		ItemInfo normals = new ItemInfo();
		normals.icon = this.getContext().getResources()
				.getDrawable(R.drawable.normals);
		normals.title = this.getContext().getString(R.string.normals); // 默认
																		// 无转场效果

		ItemInfo cross = new ItemInfo();// 十字翻转
		cross.icon = this.getContext().getResources()
				.getDrawable(R.drawable.cross);
		// A by zhouerlong
		cross.title = this.getContext().getString(R.string.stack);

		ItemInfo page = new ItemInfo();// 翻页
		page.icon = this.getContext().getResources()
				.getDrawable(R.drawable.page);
		// A by zhouerlong
		page.title = this.getContext().getString(R.string.page);

		ItemInfo cube_in = new ItemInfo();// 盒子(内)
		cube_in.icon = this.getContext().getResources()
				.getDrawable(R.drawable.cube_in);
		// A by zhouerlong
		cube_in.title = this.getContext().getString(R.string.zoom_in);

		ItemInfo cube_out = new ItemInfo();// 盒子(外)
		cube_out.icon = this.getContext().getResources()
				.getDrawable(R.drawable.cube_out);
		// A by zhouerlong
		cube_out.title = this.getContext().getString(R.string.cube_out);

	/*	ItemInfo carousel_l = new ItemInfo();// 旋转木马(左)
		carousel_l.icon = this.getContext().getResources()
				.getDrawable(R.drawable.carousel_l);
		// A by zhouerlong
		carousel_l.title = this.getContext().getString(R.string.carousel_l);

		ItemInfo carousel_r = new ItemInfo();// 旋转木马(右)
		carousel_r.icon = this.getContext().getResources()
				.getDrawable(R.drawable.carousel_r);
		// A by zhouerlong
		carousel_r.title = this.getContext().getString(R.string.carousel_r);*/

		ItemInfo windmill = new ItemInfo();// 风车
		windmill.icon = this.getContext().getResources()
				.getDrawable(R.drawable.windmill);
		// A by zhouerlong
		windmill.title = this.getContext().getString(R.string.windmill);

		ItemInfo random = new ItemInfo();
		random.icon = this.getContext().getResources()
				.getDrawable(R.drawable.random);
		// A by zhouerlong
		random.title = this.getContext().getString(R.string.random);

		themes.add(normals);//
		themes.add(cross);//
		themes.add(page);//
		themes.add(cube_in);//
		themes.add(cube_out);//
//		themes.add(carousel_l);//
//		themes.add(carousel_r);//
		themes.add(windmill);//
		themes.add(random);//
		return themes;

	}

	public EffectLinearLayout(Context context, AttributeSet attrs) {
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
			// modify by zhouerlong
					new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,
							LayoutParams.FILL_PARENT));
			// modify by zhouerlong
		}
	}

	public void setLauncher(Launcher launcher) {
		mLauncher = launcher;
	}

	public EffectLinearLayout(Context context) {
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
			} else {
				holder = (HodlerView) convertView.getTag();
			}
			if (holder.themeView != null) {
				holder.themeView.setImageDrawable(item.icon);
				holder.themeView.setScaleType(ScaleType.FIT_CENTER);
				// add by zhouerlong
				holder.themeView.setOnClickListener(EffectLinearLayout.this);
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
		View child = this.getChildAt(PreferencesManager
				.getCurrentEffectSelect(this.getContext()));
		if (child == null) {
			child = this.getChildAt(PreferencesManager.DEFAULT_EFFECT_INDEX);
		}
		child.setSelected(true);
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

		PreferencesManager.setCurrentEffect(this.getContext(), index);
		mLauncher.OnSnapToRightPage();

	}

}