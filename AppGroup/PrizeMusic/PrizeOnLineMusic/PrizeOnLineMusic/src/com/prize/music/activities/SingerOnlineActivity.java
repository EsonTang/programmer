/**
 * 
 */

package com.prize.music.activities;

import java.util.HashMap;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.SimpleOnPageChangeListener;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.prize.app.constants.RequestMethods;
import com.prize.app.constants.RequestResCode;
import com.prize.app.util.JLog;
import com.prize.app.util.SDKUtil;
import com.prize.app.xiami.RequestManager;
import com.prize.music.IApolloService;
import com.prize.music.IfragToActivityLister;
import com.prize.music.helpers.utils.MusicUtils;
import com.prize.music.helpers.utils.StateBarUtils;
import com.prize.music.helpers.utils.UILimageUtil;
import com.prize.music.online.task.SingerByTypeTask;
import com.prize.music.service.ApolloService;
import com.prize.music.service.ServiceToken;
import com.prize.music.ui.adapters.PagerAdapter;
import com.prize.music.ui.fragments.AlbumFragment;
import com.prize.music.ui.fragments.AlbumFragment.AlbumsCallBack;
import com.prize.music.ui.fragments.BottomActionBarFragment;
import com.prize.music.ui.fragments.HotSongsFragment;
import com.prize.music.ui.fragments.HotSongsFragment.HotSongsCallBack;
import com.prize.music.R;
import com.prize.onlinemusibean.ArtistsBean;
import com.xiami.sdk.XiamiSDK;
import com.xiami.sdk.utils.ImageUtil;

/**
 * @author  
 * @Note 歌手的歌曲界面activity
 */
public class SingerOnlineActivity extends FragmentActivity implements OnClickListener, 
       IfragToActivityLister,AlbumsCallBack ,HotSongsCallBack,ServiceConnection{

	private ViewPager mViewPager = null;

	private BottomActionBarFragment mBActionbar;

	private TextView action_back;
	private ImageView action_search;
	private TextView hotSongsTv;
	private TextView albumTv;
	
	private ImageView img_Iv;
	
	// mod by p
	private Bundle mbundle;
	private int artist_id;
//	private ArtistsBean itemData;
	
	private RequestManager requestManager;
	private XiamiSDK xiamiSDK;
    
	private ServiceToken mToken;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		StateBarUtils.initStateBar(this);

		setContentView(R.layout.activity_singerdetail_layout);
		
		requestManager = RequestManager.getInstance();
		xiamiSDK = new XiamiSDK(this,SDKUtil.KEY, SDKUtil.SECRET);
		
		findViewById();
		initUpperHalf();
		initPager();

		setListener();
	}
 
	private void findViewById() {
		mBActionbar = (BottomActionBarFragment) getSupportFragmentManager()
				.findFragmentById(R.id.bottomactionbar_new);
		bottom_action_bar_album_art = (ImageView)findViewById(R.id.bottom_action_bar_album_art);
		// mBActionbar.setUpQueueSwitch(this);

		action_search = (ImageView) findViewById(R.id.action_search);
		action_back = (TextView) findViewById(R.id.action_back);
		hotSongsTv =  (TextView) findViewById(R.id.hotsongs_tv);
		albumTv =  (TextView) findViewById(R.id.album_tv);
		img_Iv = (ImageView) findViewById(R.id.img_Iv);
		
		mViewPager = (ViewPager)findViewById(R.id.id_stickynavlayout_viewpager);
		mViewPager.setPageMargin(getResources().getInteger(
				R.integer.viewpager_margin_width));
		
	}
	
	private ImageView bottom_action_bar_album_art;
	public ImageView getBottomView() {
		return bottom_action_bar_album_art;
	}

	@Override
	public boolean onGenericMotionEvent(MotionEvent event) {
		return true;
	}

	private void setListener() {
		action_back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				SingerOnlineActivity.this.finish();

			}
		});
		
		action_search.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(SingerOnlineActivity.this, SearchActivity.class);
				startActivity(intent);
			}
		});
		
		hotSongsTv.setOnClickListener(this);
		albumTv.setOnClickListener(this);
		mHotSongsFragment.setSingerCallBack(this);
		mAlbumFragment.setSingerCallBack(this);
		
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			super.onBackPressed();
			return true;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void onToggleButton() {
		if (mViewPager != null) {
			int cur = mViewPager.getCurrentItem();
			if (cur == 0) {
				mViewPager.setCurrentItem(1);
			} else {
				mViewPager.setCurrentItem(0);
			}
		}
	}

	/**
	 *
	 */
	private void initUpperHalf() {
	
		if(getIntent()!=null){
			mbundle = getIntent().getExtras();
			artist_id = mbundle.getInt("artist_id");
//			if (mbundle != null) {
//				itemData = (ArtistsBean) mbundle.getSerializable("ArtistsBean");
//				if(itemData==null){
//					loadData();
//				}
//			}
		}
//		if(itemData!=null){
//			lineOne = itemData.artist_name;
//			action_back.setText(lineOne);
//			if (!TextUtils.isEmpty(itemData.artist_logo)) {
//				ImageLoader.getInstance().displayImage(ImageUtil.transferImgUrl(itemData.artist_logo,640),
//						img_Iv, UILimageUtil.getTwoOneZeroDpLoptions(), null);
//			}
//		}
		loadData();
	}
	//加载banner图
	private void loadData() {
		SingerByTypeTask task = new SingerByTypeTask(xiamiSDK, RequestMethods.ARTIST_DETAIL, mHandler);
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("artist_id", artist_id);
		task.execute(params);
	}
	
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			
			switch (msg.what) {
			case RequestResCode.REQUEST_OK:
				Gson gson = requestManager.getGson();
				JsonElement element = (JsonElement) msg.obj;
				ArtistsBean bean = gson.fromJson(element, ArtistsBean.class);
			     
				if(bean!=null){
					if(bean.artist_name!=null){
						action_back.setText(bean.artist_name);
					}
					if (!TextUtils.isEmpty(bean.artist_logo)) {
						ImageLoader.getInstance().displayImage(ImageUtil.transferImgUrl(bean.artist_logo,640),
								img_Iv, UILimageUtil.getDetaiHeadImgUILoptions(), null);
					}
//					if(!TextUtils.isEmpty(bean.songs_count+"")){
//						hotSongsTv.setText("热门歌曲"+bean.songs_count);
//					}
//					if(!TextUtils.isEmpty(bean.songs_count+"")){
//						albumTv.setText("专辑"+bean.albums_count);
//					}
				}
				break;
			case RequestResCode.REQUEST_FAILE:
                loadData();
				break;
			case RequestResCode.REQUEST_EXCEPTION:
				break;
			}
		};
	};
	
	HotSongsFragment mHotSongsFragment;
	AlbumFragment mAlbumFragment;
	
	/**
	 * Initiate ViewPager and PagerAdapter
	 */
	private void initPager() {

		PagerAdapter mPagerAdapter = new PagerAdapter(
				getSupportFragmentManager());
		
		mHotSongsFragment=new HotSongsFragment(this,artist_id);
		mAlbumFragment=new AlbumFragment(this,artist_id);
		mPagerAdapter.addFragment(mHotSongsFragment);
		mPagerAdapter.addFragment(mAlbumFragment);

		// Set up ViewPager
		mViewPager.setPageMarginDrawable(R.drawable.viewpager_margin);
		mViewPager.setOffscreenPageLimit(mPagerAdapter.getCount());
		mViewPager.setAdapter(mPagerAdapter);
		mViewPager.setOnPageChangeListener(new PageListener());

	}

	private class PageListener extends SimpleOnPageChangeListener {

		public void onPageSelected(int cur) {
			if (cur == 0) {
				
				hotSongsTv.setTextColor(getResources().getColor(
						R.color.text_color_ff5f00));
				albumTv.setTextColor(getResources().getColor(
						R.color.gray_black));
			}else {
				
				hotSongsTv.setTextColor(getResources().getColor(
						R.color.gray_black));
				albumTv.setTextColor(getResources().getColor(R.color.text_color_ff5f00));
			}
		}
	}

	@Override
	public void onClick(View v) {
		onToggleButton();

	}

	@Override
	public void countNum(int count) {

	}

	@Override
	public void processAction(String action) {

	}

	@Override
	protected void onDestroy() {
		if (MusicUtils.mService != null)
			MusicUtils.unbindFromService(mToken);
		if (mHandler != null) {
			mHandler.removeCallbacksAndMessages(null);
		}
		super.onDestroy();
	}

	@Override
	public void AlbumSetTotal(int total) {
		albumTv.setText(this.getString(R.string.singer_album,total));
	}

	@Override
	public void AlbumSetHotSongsTotal(int total) {
		hotSongsTv.setText(this.getString(R.string.singer_hotsongs,total));
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
	protected void onResume() {
		mBActionbar.getBottom_action_bar_dragview().setOnClickListener(
				new OnClickListener() {

			@Override
			public void onClick(View v) {
				   Intent intent = new Intent(SingerOnlineActivity.this,
						AudioPlayerActivity.class);
				   startActivity(intent);
			}
		});
		super.onResume();
	}
}
