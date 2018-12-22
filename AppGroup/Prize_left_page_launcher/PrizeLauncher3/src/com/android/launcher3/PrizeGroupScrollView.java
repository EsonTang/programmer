package com.android.launcher3;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

public class PrizeGroupScrollView extends PrizeScrollView {

	private Launcher mLauncher;

	public PrizeGroupScrollView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		mLauncher = (Launcher) context;
		initDatas();
	}

	private List<IconInfo> groups = new ArrayList<IconInfo>();

	public PrizeGroupScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mLauncher = (Launcher) context;
		initDatas();
	}

	public PrizeGroupScrollView(Context context) {
		super(context);
		mLauncher = (Launcher) context;
		initDatas();
	}

	private void initDatas() {
		IconInfo widgetInfo = new IconInfo();
		widgetInfo.icon = this.getContext().getDrawable(
				R.drawable.togglebar_widget_item);
		widgetInfo.title = this.getContext().getString(
				R.string.widget_button_text);
		IconInfo effctInfo = new IconInfo();
		effctInfo.icon = this.getContext().getDrawable(
				R.drawable.togglebar_effect_item);
		effctInfo.title = this.getContext().getString(R.string.effects);
		IconInfo themeInfo = new IconInfo();
		themeInfo.icon = this.getContext().getDrawable(
				R.drawable.thumbnail_entry_multi_select1);
		themeInfo.title = this.getContext().getString(
				R.string.themeset_button_text);
		IconInfo wallInfo = new IconInfo();
		wallInfo.icon = this.getContext().getDrawable(
				R.drawable.thumbnail_entry_multi_select1);
		wallInfo.title = this.getContext().getString(
				R.string.wallpaper);
		groups.add(widgetInfo);
		groups.add(effctInfo);
		groups.add(themeInfo);
//		groups.add(wallInfo);
		setDatas(groups);
		updatePageCounts();

	}

	@Override
	public void onClick(View view) {

		super.onClick(view);
		IconInfo info = (IconInfo) view.getTag();
		mLauncher.onCheckedChanged(info.position);
	}

	@Override
	protected boolean applyInfo(Object t, View icon) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected View syncGetLayout(ViewGroup layout, Object t) {
		// TODO Auto-generated method stub
		return null;
	}

}
