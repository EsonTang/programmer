package com.android.launcher3;

import java.util.ArrayList;
import java.util.List;

import com.android.launcher3.notify.PreferencesManager;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Process;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;

public class PrizeEffctScrollView extends PrizeScrollView {

	@Override
	public void onClick(View v) {
		if(Utilities.isFastClick(1200)) return;
		super.onClick(v);
		IconInfo info = (IconInfo) v.getTag();
		int index = info.position;
		info.select = true;
		v.setSelected(true);
		PreferencesManager.setCurrentEffect(this.getContext(), index);
		((Launcher) getContext()).OnSnapToRightPage();
		update(v);
	}

	

	@Override
	protected void onDataReady(int width, int height) {
		// TODO Auto-generated method stub
		super.onDataReady(width, height);
		defaultEffct=true;
		this.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			
			@Override
			public void onGlobalLayout() {
				if(defaultEffct) {

					int id =PreferencesManager.getCurrentEffectSelect(getContext());
					int x = id % mCellCountX;
					int y = id / mCellCountX;
					ViewGroup gp =(ViewGroup) getChildAt(y);
					update(gp.getChildAt(x));
					defaultEffct=false;
				}
			}
		});
    
		
	}

	private List<IconInfo> effcts = new ArrayList<IconInfo>();

	public PrizeEffctScrollView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		initDatas();
	}

	public PrizeEffctScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initDatas();
	}
	
	
	
	

	private void initDatas() {
		IconInfo normals = new IconInfo();
		normals.icon = this.getContext().getDrawable(R.drawable.normals);
		normals.title = this.getContext().getString(R.string.normals); // 默认
																		// 无转场效果

		IconInfo cross = new IconInfo();// 十字翻转
		cross.icon = this.getContext().getDrawable(R.drawable.cross);
		// A by zhouerlong
		cross.title = this.getContext().getString(R.string.stack);

		IconInfo page = new IconInfo();// 翻页
		page.icon = this.getContext().getDrawable(R.drawable.page);
		// A by zhouerlong
		page.title = this.getContext().getString(R.string.page);

		IconInfo cube_in = new IconInfo();// 盒子(内)
		cube_in.icon = this.getContext().getDrawable(R.drawable.cube_in);
		// A by zhouerlong
		cube_in.title = this.getContext().getString(R.string.zoom_in);

		IconInfo cube_out = new IconInfo();// 盒子(外)
		cube_out.icon = this.getContext().getDrawable(R.drawable.cube_out);
		// A by zhouerlong
		cube_out.title = this.getContext().getString(R.string.cube_out);

		IconInfo windmill = new IconInfo();// 风车
		windmill.icon = this.getContext().getDrawable(R.drawable.windmill);
		// A by zhouerlong
		windmill.title = this.getContext().getString(R.string.windmill);

//		IconInfo random = new IconInfo();
//		random.icon = this.getContext().getResources()
//				.getDrawable(R.drawable.random);
//		// A by zhouerlong
//		random.title = this.getContext().getString(R.string.random);
		effcts.add(normals);//
		effcts.add(cross);//
		effcts.add(page);//
		effcts.add(cube_in);//
		effcts.add(cube_out);//
		effcts.add(windmill);//
//		effcts.add(random);//
		setDatas(effcts);
		updatePageCounts();
		

	
		
	}

	boolean defaultEffct=false;

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
