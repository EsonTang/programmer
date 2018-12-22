package com.prize.videoc;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import android.R.integer;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Paint.Style;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.lidroid.xutils.BitmapUtils;
import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.prize.videoc.bean.PVideo;
import com.prize.videoc.db.DbManager;
import com.prize.videoc.db.RecordDao;
import com.prize.videoc.db.VideoProvider;
import com.prize.videoc.presenter.BroadcastPresenter;
import com.prize.videoc.presenter.HistoryUpdateBroadcast;
import com.prize.videoc.presenter.IBroadcastView;
import com.prize.videoc.to.JumpContext;
import com.prize.videoc.to.ToLocalPlayer;
import com.prize.videoc.widget.GridViewWith;
import com.prize.videoc.R;
/**
 * Created by yiyi on 2015/6/9.
 */
public class LocalActivity extends Activity implements IBroadcastView {
	private BroadcastPresenter mBroadcastPresenter;
	
	private HistoryUpdateBroadcast mHistoryUpdateBroadcast;
	
	private static LocalActivity instance;
	
	public static synchronized LocalActivity getInstance() {
		return instance;
	}
	
	enum State {
		Normal,
		Edit
	}
	
	private State mState = State.Normal;
	
	@ViewInject(R.id.content_gw)
	private GridViewWith mContentGrid;
	@ViewInject(R.id.cancelDel_btn)
	private TextView mCancelBtn;
	@ViewInject(R.id.edit_btn)
	private TextView mEditBtn;

	private DbUtils dbUtils;
	private NAdapter mAdapter;
	private View mHeaderView;
	private BitmapUtils mBitmapUtils;
	private JumpContext mJumpContext;
	private boolean isReadyToDel = false;
	private boolean hasRecord = false;
	private boolean delRecordNeeded = false;
	private Set<Integer> toDelIdlist = new HashSet<Integer>();
	
	private String mNetAppPackageName;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
			Window window = getWindow();
			window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
					| WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
			window.getDecorView().setSystemUiVisibility(
					View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
							| View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			window.setStatusBarColor(Color.TRANSPARENT);
//			window.setNavigationBarColor(Color.TRANSPARENT);
		}
		setContentView(R.layout.activity_local);
		mNetAppPackageName = getResources().getString(R.string.net_app);
		instance=this;
		
		ViewUtils.inject(this);
		mBroadcastPresenter = new BroadcastPresenter(this);
		mHistoryUpdateBroadcast=new HistoryUpdateBroadcast();
		registerReceiver(mHistoryUpdateBroadcast, new IntentFilter("com.prize.videoc.HISTORYUPDATE"));
		dbUtils = DbManager.getInstance().getDb();
		mBitmapUtils = new BitmapUtils(getApplicationContext());
		mJumpContext = new JumpContext();
		mHeaderView = getLayoutInflater().inflate(R.layout.local_header, null);
		mContentGrid.addHeaderView(mHeaderView);
		mContentGrid.setAdapter(mAdapter = new NAdapter());
		mContentGrid
				.setOnItemClickListener(new GridViewWith.OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						//prize-public-bug: 11158 App went south when running Monkey test-pengcancan -20160112-start
						if (position>=mAdapter.getCount()||mState == State.Edit) {
							return;
						}
						//prize-public-bug: 11158 App went south when running Monkey test-pengcancan -20160112-end
						PVideo video = (PVideo) mAdapter.getItem(position);
						mJumpContext.setTarget(new ToLocalPlayer(video.getId()));
						mJumpContext.doJump(getApplicationContext());

						RecordDao.saveLastPlay(dbUtils, video);
					}
				});
		loadContent();
		mEditBtn.setTextColor(R.color.disable_grey);
		/**跳转到youku */ 
		View AqiImage = mHeaderView.findViewById(R.id.icon_aqi);
		AqiImage.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				//mJumpContext.setTarget(new ToQiyiPlayer());
				//mJumpContext.doJump(getApplicationContext());
				goToAqi();
			}
		});
		
		if (!isAppExist(mNetAppPackageName)) {	//com.youku.phone  com.tencent.qqlive  com.qiyi.video
			mHeaderView.findViewById(R.id.net_videos).setVisibility(View.GONE);
			mHeaderView.findViewById(R.id.icon_aqi).setVisibility(View.GONE);
			mHeaderView.findViewById(R.id.sub_videos).setVisibility(View.GONE);
			} 
		else {	
				mHeaderView.findViewById(R.id.net_videos).setVisibility(View.VISIBLE);
				mHeaderView.findViewById(R.id.icon_aqi).setVisibility(View.VISIBLE);
				mHeaderView.findViewById(R.id.sub_videos).setVisibility(View.VISIBLE);
			}
		mBroadcastPresenter.registerUri(getApplicationContext(),
				MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
	}

	protected void goToAqi() {
		Intent intent = getPackageManager().getLaunchIntentForPackage(mNetAppPackageName);
		if(intent!=null){
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
		}
	}

	private void loadContent() {
		List<PVideo> list = VideoProvider.getList(getApplicationContext());
		mAdapter.setDatas(list);
		if (list == null || list.size() == 0) {
			cancelClk(null);
		}
	}

	@OnClick(R.id.edit_btn)
	public void editClk(View v) {
		if (isReadyToDel) {
			doDel();
		} else {
			initDel();
		}
	}
    
	private boolean isAppExist(String appPackage) {
		try {		
		     this.getApplication().getPackageManager().getApplicationInfo(appPackage, 0);
					return true;	
		   } catch (Exception e) {
		   }		
		   return false;	
		}
	private void doDel() {
		Iterator<Integer> it = toDelIdlist.iterator();
		PVideo video = RecordDao.findLastPlay(dbUtils);
		while (it.hasNext()) {
			Integer id = it.next();
			VideoProvider.delVideo(getApplicationContext(), id);
			if (video != null && video.getId() == id) {
				delRecordNeeded = true;
			}
		}
		if (delRecordNeeded) {
			RecordDao.delAll(dbUtils);
			onNoRecord();
			hasRecord=false;
		}
		if (delRecordNeeded || toDelIdlist.size() > 0) {
			loadContent();
		}
		toDelIdlist.clear();
		mEditBtn.setTextColor(R.color.disable_grey);
	}

	private void initDel() {
		if (mAdapter.getCount()==0) {
			return;
		}
		mState = State.Edit;
		mCancelBtn.setText(R.string.back);
		mCancelBtn.setCompoundDrawablesWithIntrinsicBounds(
				R.drawable.cancel_slt, 0, 0, 0);
		mEditBtn.setText(R.string.delete);
		mEditBtn.setTextColor(R.color.disable_grey);
		/*ImageView selImage = (ImageView) mHeaderView
				.findViewById(R.id.sel_image);
		if (hasRecord) {
			selImage.setVisibility(View.VISIBLE);
			selImage.setImageResource(R.drawable.sel);
		}*/
		((LinearLayout)mHeaderView.findViewById(R.id.to_hide_content)).setVisibility(View.GONE);
		isReadyToDel = true;
		toDelIdlist.clear();
		delRecordNeeded = false;
		mAdapter.notifyDataSetChanged();
	}

	@OnClick(R.id.cancelDel_btn)
	public void cancelClk(View v) {
		if (!isReadyToDel)
			return;
		mState = State.Normal;
		mCancelBtn.setText(R.string.app_name);
		mEditBtn.setText(R.string.edit);
		mEditBtn.setTextColor(Color.BLACK);
		((LinearLayout)mHeaderView.findViewById(R.id.to_hide_content)).setVisibility(View.VISIBLE);
		mCancelBtn.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
		isReadyToDel = false;
		toDelIdlist.clear();
		mAdapter.notifyDataSetChanged();
		mHeaderView.findViewById(R.id.sel_image).setVisibility(View.GONE);
	}

	@Override
	public void onBackPressed() {
		if (isReadyToDel)
			cancelClk(mCancelBtn);
		else
			super.onBackPressed();
	}

	private void onNoRecord() {
		hasRecord = false;
		mHeaderView.findViewById(R.id.clk_image).setVisibility(View.GONE);
		ImageView thumbnailImage = (ImageView) mHeaderView
				.findViewById(R.id.thumbnail_image);
		int width=getWindowManager().getDefaultDisplay().getWidth();
		int height=(int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,170, getResources().getDisplayMetrics());
		Bitmap mBitmap=Bitmap.createBitmap(width,height, Config.ARGB_8888);
		Canvas mCanvas=new Canvas(mBitmap);
		Paint mPaint=new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaint.setColor(Color.parseColor("#F0F0F0"));
		mPaint.setStyle(Style.FILL);
		Path mClipPath = new Path();
		mClipPath.addRoundRect(new RectF(0, 0, width,height), 14.0f, 14.0f,Path.Direction.CW);
		mCanvas.drawPath(mClipPath, mPaint);
		thumbnailImage.setImageBitmap(mBitmap);
		mPaint=null;
		mCanvas=null;
		mHeaderView.findViewById(R.id.sel_image).setVisibility(View.GONE);
		mHeaderView.findViewById(R.id.emptyView).setVisibility(View.VISIBLE);
	}

	@Override
	protected void onResume() {
		super.onResume();

		PVideo video = RecordDao.findLastPlay(dbUtils);
		if (video != null) {
			if (RecordDao.verifyFileExistence(video)) {
				hasRecord = true;
				mHeaderView.setVisibility(View.VISIBLE);
				ImageView thumbnailImage = (ImageView) mHeaderView
						.findViewById(R.id.thumbnail_image);
				final int id = video.getId();
				View clkImage = mHeaderView.findViewById(R.id.clk_image);
				clkImage.setVisibility(View.VISIBLE);
				clkImage.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						mJumpContext.setTarget(new ToLocalPlayer(id));
						mJumpContext.doJump(getApplicationContext());
					}
				});
				mBitmapUtils.displayFromVideo(thumbnailImage, video.getPath());
				mHeaderView.findViewById(R.id.sel_image).setOnClickListener(
						new View.OnClickListener() {

							@Override
							public void onClick(View v) {
								ImageView selImage = (ImageView) v;
								if (delRecordNeeded) {
									selImage.setImageResource(R.drawable.sel);
								} else {
									selImage.setImageResource(R.drawable.sel_0);
								}
								delRecordNeeded = !delRecordNeeded;
							}
						});
				mHeaderView.findViewById(R.id.emptyView).setVisibility(
						View.GONE);
			}else {
				RecordDao.delAll(dbUtils);
				onNoRecord();
			}
		} else {
			onNoRecord();
		}
		if (mAdapter.getCount()>0) {
			mEditBtn.setTextColor(Color.BLACK);
		}

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mBroadcastPresenter.unRegister(getApplicationContext());
		unregisterReceiver(mHistoryUpdateBroadcast);
	}

	@Override
	public void onReceive(Intent intent) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onChange() {
		loadContent();

	}

	private class NAdapter extends BaseAdapter {
		private List<PVideo> mDatas;

		private void setDatas(List<PVideo> list) {
			mDatas = list;
			notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			if (mDatas == null)
				return 0;
			return mDatas.size();
		}

		@Override
		public Object getItem(int position) {
			return mDatas.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView == null) {
				convertView = LayoutInflater.from(getApplicationContext())
						.inflate(R.layout.video_item, null);
				holder = new ViewHolder();
				holder.titleText = (TextView) convertView
						.findViewById(R.id.video_title);
				holder.thumbnailImage = (ImageView) convertView
						.findViewById(R.id.thumbnail_image);
				holder.selImage = (ImageView) convertView
						.findViewById(R.id.sel_image);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			PVideo video = mDatas.get(position);
			holder.titleText.setText(video.getTitle());
			mBitmapUtils.displayFromVideo(holder.thumbnailImage,
					video.getPath());
			final ImageView selImage = holder.selImage;
			final int id = video.getId();
			holder.selImage.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					if (toDelIdlist.contains(id)) {
						toDelIdlist.remove(id);
						if(toDelIdlist.size()==0){
							mEditBtn.setTextColor(R.color.disable_grey);
						}
						selImage.setImageResource(R.drawable.sel);
					} else {
						toDelIdlist.add(id);
						if (toDelIdlist.size()>0) {
							mEditBtn.setTextColor(Color.BLACK);
						}
						selImage.setImageResource(R.drawable.sel_0);
					}
				}
			});
			if (toDelIdlist.contains(id))
				selImage.setImageResource(R.drawable.sel_0);
			else {
				selImage.setImageResource(R.drawable.sel);
			}
			if (isReadyToDel)
				holder.selImage.setVisibility(View.VISIBLE);
			else
				holder.selImage.setVisibility(View.GONE);
			return convertView;
		}

		class ViewHolder {
			TextView titleText;
			ImageView thumbnailImage, selImage;
		}

	}
	
	public void updateHistory(PVideo video) {
		if (video!=null) {
			mBitmapUtils.displayFromVideo((ImageView) mHeaderView
					.findViewById(R.id.thumbnail_image), video.getPath());
		}else {
			onNoRecord();
		}
	}
}
