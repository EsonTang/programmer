package com.prize.music.page;

import com.prize.music.helpers.utils.UiUtils;
import com.prize.music.R;

import android.support.v4.app.FragmentActivity;

public class EuropePager extends SingerBasePager{
	private FragmentActivity context;
	public EuropePager(FragmentActivity activity) {
		super(activity);
		context = activity;
	}
	
	@Override
	public void OnBandClick(){
		UiUtils.JumpToSingerByTypeActivity(context, UiUtils.ConcatString(
				context, R.string.search_Europe, R.string.singer_B),
				"english_B");
	}
    
	@Override
	public void OnWomenClick() {
		UiUtils.JumpToSingerByTypeActivity(context, UiUtils.ConcatString(
				context, R.string.search_Europe, R.string.singer_W),
				"english_F");
	}
    
	@Override
	public void OnManClick() {
		UiUtils.JumpToSingerByTypeActivity(context, UiUtils.ConcatString(
				context, R.string.search_Europe, R.string.singer_M),
				"english_M");
	}
  
}
