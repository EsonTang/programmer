package com.android.launcher3.view;

import com.android.launcher3.R;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class WallPaperDialog extends Dialog implements android.view.View.OnClickListener  {
	
	public final int LOCK = 0;
	public final int DESK = 1;
	public final int ALL = 2;
	private String[] items;
	
	public WallPaperDialog(Context context) {
		super(context);
	}
	
	public WallPaperDialog(Context context, int theme,String[] item) {
		super(context, theme);
		items = item;
	}
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.wallpaper_dialog);
    	
    	TextView title_tv = (TextView) findViewById(R.id.dlg_title_tv);
		title_tv.setText(R.string.wallpaper_dlg_title);
		TextView lock_tv = (TextView) findViewById(R.id.lock_tv);
		lock_tv.setText(items[0]);
		TextView desk_tv = (TextView) findViewById(R.id.desk_tv);
		desk_tv.setText(items[1]);
		TextView bothSet_tv = (TextView) findViewById(R.id.bothSet_tv);
		bothSet_tv.setText(items[2]);
		Button wallpaper_neg = (Button)findViewById(R.id.wallpaper_neg);
		
		wallpaper_neg.setOnClickListener(this);
		lock_tv.setOnClickListener(this);
		desk_tv.setOnClickListener(this);
		bothSet_tv.setOnClickListener(this);
		
    }

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.lock_tv:
			onItemClick.onClick(LOCK);
			break;
		case R.id.desk_tv:
			onItemClick.onClick(DESK);
			break;
		case R.id.bothSet_tv:
			onItemClick.onClick(ALL);
			break;
		case R.id.wallpaper_neg:
			this.dismiss();
			break;
		}
	}

	/**点击的回调*/
    public static interface OnItemClick{
    	void onClick(int which);
    }
    
    private OnItemClick onItemClick;
    
    public void setOnItemClick(OnItemClick onItemClick){
    	this.onItemClick = onItemClick;
    }
}
