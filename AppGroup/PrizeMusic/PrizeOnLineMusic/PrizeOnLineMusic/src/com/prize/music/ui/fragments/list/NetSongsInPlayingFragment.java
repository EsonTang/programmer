package com.prize.music.ui.fragments.list;

import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.prize.app.constants.Constants;
import com.prize.app.util.JLog;
import com.prize.music.R;
import com.prize.music.database.MusicInfo;
import com.prize.music.helpers.utils.MusicUtils;
import com.prize.music.service.ApolloService;

public class NetSongsInPlayingFragment extends Fragment{

	ListView mListView;
	List<MusicInfo> mList = new ArrayList<MusicInfo>();
	NetSongsListInAudioPlayAdapter mAdapter;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View root = inflater.inflate(R.layout.songs_list_in_playing_layout, container, false);
		mListView = (ListView) root.findViewById(android.R.id.list);
		mList.clear();
				
		init();		
		setListener();
		return root;
	}
	
	private void setListener() {
		// TODO Auto-generated method stub
		mListView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				List<MusicInfo> arraylist = mAdapter.getArrayList();
				try {					
					MusicUtils.playMusic(getActivity(),arraylist.get(arg2), "", arraylist, Constants.KEY_SONGS);
					mAdapter.notifyDataSetChanged();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}

	@Override
	public void onStart() {
		super.onStart();
		IntentFilter filter = new IntentFilter();
		filter.addAction(ApolloService.META_CHANGED);
		filter.addAction(ApolloService.PLAYSTATE_CHANGED);
		getActivity().registerReceiver(mMediaStatusReceiver, filter);
	}

	@Override
	public void onStop() {

		super.onStop();
		getActivity().unregisterReceiver(mMediaStatusReceiver);
	}

	/**
	 * 更新数据在需要的时候
	 */
	private final BroadcastReceiver mMediaStatusReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			if (mListView != null) {
					mAdapter.notifyDataSetChanged();
			}
		}
	};
	
	
	private void init() {
		// TODO Auto-generated method stub
		mAdapter = new NetSongsListInAudioPlayAdapter(getActivity());
		mListView.setAdapter(mAdapter);
		try {
			if(MusicUtils.mService != null){
				mList.clear();
				mList.addAll(MusicUtils.mService.getCurrentMusicInfoList());
			}
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		mAdapter.setList(mList,false,0);
		mAdapter.notifyDataSetChanged();
		
	}

	class NetSongsListInAudioPlayAdapter extends BaseAdapter{

		protected AnimationDrawable mPeakTwoAnimation;
		Handler mHandler;
		Context context;
		LayoutInflater layoutinflater ;
		List<MusicInfo> mList = new ArrayList<MusicInfo>();
		private long mListId = 0;
		public NetSongsListInAudioPlayAdapter(Context context){
			this.context = context;
			layoutinflater = LayoutInflater.from(context);
			mHandler = new Handler();
		}
		
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return mList.size();
		}

		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return mList.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return arg0;
		}
		
		public List<MusicInfo> getArrayList(){
			return mList;
		}

		@Override
		public View getView(final int arg0, View arg1, ViewGroup arg2) {
			// TODO Auto-generated method stub
			Holder holder = null;
			if(arg1 == null){
				holder = new Holder();
				arg1 = layoutinflater.inflate(R.layout.songsinplaying_list_item, null);
				holder.song_name = (TextView)arg1.findViewById(R.id.listview_item_line_one);
				holder.singer_name = (TextView)arg1.findViewById(R.id.listview_item_line_two);
				holder.peak_two = (ImageView)arg1.findViewById(R.id.peak_two);
//				holder.peak_two.setImageResource(R.anim.peak_meter_orange);
				holder.checkbox = (CheckBox)arg1.findViewById(R.id.checkBox);
				arg1.setTag(holder);
			} else {
				holder = (Holder) arg1.getTag();
			}
			holder.peak_two.setImageResource(R.anim.peak_meter_orange);
			holder.song_name.setText(mList.get(arg0).songName);
			holder.singer_name.setText(mList.get(arg0).singer);
			
			if(MusicUtils.mService != null){			
				try {
					if(MusicUtils.getCurrentMusicInfo().songId == mList.get(arg0).songId && 
							MusicUtils.getCurrentMusicInfo().source_type.equals(mList.get(arg0).source_type)){
						holder.song_name.setTextColor(context.getResources().getColor(R.color.gold_color));
						holder.singer_name.setTextColor(context.getResources().getColor(R.color.gold_color));
						holder.peak_two.setVisibility(View.VISIBLE);
						mPeakTwoAnimation = (AnimationDrawable) holder.peak_two.getDrawable();
						if(MusicUtils.mService.isPlaying()){
							mHandler.post(new Runnable() {
								public void run() {
							JLog.i("0000", "当前播放mPeakTwoAnimation.start();");
									mPeakTwoAnimation.start();
								}
							});
						} else {
							JLog.i("0000", "当前播放	mPeakTwoAnimation.stop()");
							mPeakTwoAnimation.stop();
							holder.peak_two.setImageResource(R.drawable.icon_play_stop_black);
						}
					} else {
						holder.song_name.setTextColor(context.getResources().getColor(R.color.white));
						holder.singer_name.setTextColor(context.getResources().getColor(R.color.white));
						holder.peak_two.setVisibility(View.INVISIBLE);
						JLog.i("0000", "当前播放holder.peak_two.setVisibility(View.INVISIBLE)");
					}
				} catch (Exception e) {
					e.printStackTrace();
					JLog.i("0000", "Exception"+e);
				}
			}
			return arg1;
		}
		
		class Holder{
			TextView song_name;
			TextView singer_name;
			ImageView peak_two;
			CheckBox checkbox;
		}
		
		public void setList(List<MusicInfo> list ,boolean add, long list_id){
			this.mListId  = list_id;
			if(add){
				mList.addAll(list);
				notifyDataSetChanged();
			}else{
				mList.clear();
				mList.addAll(list);
				notifyDataSetChanged();
			}
		}
		
		public void play(View convertView) {
			Holder viewholder = (Holder) convertView.getTag();
			if (viewholder != null && viewholder.peak_two != null) {
				showAnim(viewholder.peak_two);
			}
		}
		
		private ImageView oldView;
		private void showAnim(ImageView newView) {
			if (newView == null || newView.equals(oldView))
				return;
			if (oldView != null)
				oldView.setVisibility(View.INVISIBLE);
			oldView = newView;
			oldView.setVisibility(View.VISIBLE);
			mPeakTwoAnimation = (AnimationDrawable) oldView.getDrawable();
			try {
				if(MusicUtils.mService != null && MusicUtils.mService.isPlaying()){
					mHandler.post(new Runnable() {
						public void run() {
							mPeakTwoAnimation.start();
						}
					});
				} else {
					mPeakTwoAnimation.stop();
				}
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}


	}

}
