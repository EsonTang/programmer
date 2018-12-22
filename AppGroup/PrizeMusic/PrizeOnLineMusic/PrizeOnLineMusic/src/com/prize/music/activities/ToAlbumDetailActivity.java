package com.prize.music.activities;

import com.prize.app.constants.Constants;
import com.prize.app.util.JLog;
import com.prize.music.IApolloService;
import com.prize.music.helpers.utils.MusicUtils;
import com.prize.music.helpers.utils.StateBarUtils;
import com.prize.music.helpers.utils.UiUtils;
import com.prize.music.service.ApolloService;
import com.prize.music.service.ServiceToken;
import com.prize.music.ui.fragments.BottomActionBarFragment;
import com.prize.music.R;

import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

public class ToAlbumDetailActivity extends FragmentActivity implements ServiceConnection{
     
	private BottomActionBarFragment mBActionbar;
	private int album_id;
	private ServiceToken mToken;
	 @Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		StateBarUtils.initStateBar(this);
		setContentView(R.layout.albumdetail_layout);
	    
		if(getIntent()!=null){
			Bundle mbundle = getIntent().getExtras();
			album_id = mbundle.getInt("album_id");
		}
		UiUtils.gotoAlbumDeatail(this, album_id, 
				R.id.AlbumFragment_container, Constants.KEY_ALBUM);
		init();
	}
	 
	 private void init() {
		 mBActionbar = (BottomActionBarFragment) getSupportFragmentManager()
					.findFragmentById(R.id.bottomactionbar_new);
		 bottom_action_bar_album_art = (ImageView)findViewById(R.id.bottom_action_bar_album_art);
		 mBActionbar.setBackgroud(R.color.white);
	}
	 
	 private ImageView bottom_action_bar_album_art;
		public ImageView getBottomView() {
			return bottom_action_bar_album_art;
	 }

	@Override
     protected void onResume() {
			super.onResume();
			mBActionbar.getBottom_action_bar_dragview().setOnClickListener(
					new OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent intent = new Intent(ToAlbumDetailActivity.this,
							AudioPlayerActivity.class);
					startActivity(intent);
		  	}
		});
	 }

	@Override
	public void onServiceConnected(ComponentName name, IBinder obj) {
		MusicUtils.mService = IApolloService.Stub.asInterface(obj);
		mBActionbar.updateBottomActionBar();
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		MusicUtils.mService = null;
	}
	
	@Override
	protected void onStart() {
		mToken = MusicUtils.bindToService(this, this);
		IntentFilter filter = new IntentFilter();
		filter.addAction(ApolloService.META_CHANGED);
		super.onStart();
	}
	
	@Override
	protected void onStop() {
		if (MusicUtils.mService != null)
			MusicUtils.unbindFromService(mToken);
		super.onStop();
	}
	
	@Override
	protected void onDestroy() {
		if (MusicUtils.mService != null)
			MusicUtils.unbindFromService(mToken);
		super.onDestroy();
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		this.finish();
	}
}
