package com.prize.music.page;

import android.support.v4.app.FragmentActivity;

import com.prize.music.helpers.utils.UiUtils;
import com.prize.music.R;

/**
 * 华语音乐
 * @author pengyang
 *
 */
public class ChinesePager extends SingerBasePager {
   
	private FragmentActivity context;
	public ChinesePager(FragmentActivity activity) {
		super(activity);
		context = activity;
	}
	
	@Override
	public void OnBandClick(){
		
		UiUtils.JumpToSingerByTypeActivity(context, UiUtils.ConcatString(
				context, R.string.search_chinese, R.string.singer_B),
				"chinese_B");
	}

	@Override
	public void OnWomenClick() {
		UiUtils.JumpToSingerByTypeActivity(context, UiUtils.ConcatString(
				context, R.string.search_chinese, R.string.singer_W),
				"chinese_F");
	}
    
	@Override
	public void OnManClick() {
		UiUtils.JumpToSingerByTypeActivity(context, UiUtils.ConcatString(
				context, R.string.search_chinese, R.string.singer_M),
				"chinese_M");
	}
    

}
