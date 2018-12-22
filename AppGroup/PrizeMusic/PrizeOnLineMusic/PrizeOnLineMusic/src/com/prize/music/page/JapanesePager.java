package com.prize.music.page;

import com.prize.music.helpers.utils.UiUtils;
import com.prize.music.R;

import android.support.v4.app.FragmentActivity;

public class JapanesePager extends SingerBasePager{
	private FragmentActivity context;
	public JapanesePager(FragmentActivity activity) {
		super(activity);
		context = activity;
	}
	
	@Override
	public void OnBandClick(){
		UiUtils.JumpToSingerByTypeActivity(context, UiUtils.ConcatString(
				context, R.string.search_japanese, R.string.singer_B),
				"japanese_B");
	}
    
	@Override
	public void OnWomenClick() {
		UiUtils.JumpToSingerByTypeActivity(context, UiUtils.ConcatString(
				context, R.string.search_japanese, R.string.singer_W),
				"japanese_F");
	}
    
	@Override
	public void OnManClick() {
		UiUtils.JumpToSingerByTypeActivity(context, UiUtils.ConcatString(
				context, R.string.search_japanese, R.string.singer_M),
				"japanese_M");
	}
  
}

