package com.prize.music.page;

import com.prize.music.helpers.utils.UiUtils;
import com.prize.music.R;

import android.support.v4.app.FragmentActivity;


public class KoreaPager extends SingerBasePager {
	   
		private FragmentActivity context;
		public KoreaPager(FragmentActivity activity) {
			super(activity);
			context = activity;
		}
		
		@Override
		public void OnBandClick(){
			UiUtils.JumpToSingerByTypeActivity(context, UiUtils.ConcatString(
					context, R.string.search_korea, R.string.singer_B),
					"korea_B");
		}
	    
		@Override
		public void OnWomenClick() {
			UiUtils.JumpToSingerByTypeActivity(context, UiUtils.ConcatString(
					context, R.string.search_korea, R.string.singer_W),
					"korea_F");
		}
	    
		@Override
		public void OnManClick() {
			UiUtils.JumpToSingerByTypeActivity(context, UiUtils.ConcatString(
					context, R.string.search_korea, R.string.singer_M),
					"korea_M");
		}
	   
	}
