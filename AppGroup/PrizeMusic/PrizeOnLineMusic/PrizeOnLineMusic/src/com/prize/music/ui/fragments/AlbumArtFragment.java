package com.prize.music.ui.fragments;

import android.content.ComponentName;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.prize.music.IApolloService;
import com.prize.music.R;
import com.prize.music.helpers.utils.MusicUtils;
import com.prize.music.service.ApolloService;
import com.prize.music.service.ServiceToken;

/**
 * 专辑封面
 * 
 * @author 显示图片
 *
 */
public class AlbumArtFragment extends Fragment implements ServiceConnection {
	public ImageView albumArt;
	private ServiceToken mToken;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.nowplaying_album_art, null);
		albumArt = (ImageView) root.findViewById(R.id.audio_player_album_art);
		// getArguments().getString(key);
		return root;
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder obj) {
		MusicUtils.mService = IApolloService.Stub.asInterface(obj);
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		MusicUtils.mService = null;
	}

	@Override
	public void onStart() {

		// Bind to Service
//		mToken = MusicUtils.bindToService(getActivity(), this);

		IntentFilter filter = new IntentFilter();
		filter.addAction(ApolloService.META_CHANGED);
		super.onStart();
	}

	@Override
	public void onStop() {
		// Unbind
//		if (MusicUtils.mService != null)
//			MusicUtils.unbindFromService(mToken);

		// TODO: clear image cache

		super.onStop();
	}
	
	@Override
	public void onDestroy() {
//		if (MusicUtils.mService != null)
//			MusicUtils.unbindFromService(mToken);
		super.onDestroy();
	}
}
