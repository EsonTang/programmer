/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：
 *当前版本：
 *作	者：
 *完成日期：
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
...
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
 *********************************************/

package com.prize.music.ui.fragments;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.prize.app.beans.ClientInfo;
import com.prize.app.constants.Constants;
import com.prize.app.download.DownloadHelper;
import com.prize.app.util.CommonUtils;
import com.prize.app.util.JLog;
import com.prize.app.util.ToastUtils;
import com.prize.music.activities.MainActivity;
import com.prize.music.activities.SearchActivity;
import com.prize.music.helpers.DownLoadUtils;
import com.prize.music.helpers.utils.RankUtils;
import com.prize.music.helpers.utils.StateBarUtils;
import com.prize.music.helpers.utils.UILimageUtil;
import com.prize.music.helpers.utils.UiUtils;
import com.prize.music.ui.fragments.DetailListFragment.RequestBack;
import com.prize.music.R;
import com.prize.onlinemusibean.SongDetailInfo;
import com.prize.onlinemusibean.response.RankDetailResponse;
import com.prize.onlinemusibean.response.RecomendHotSongsResponse;
import com.prize.onlinemusibean.response.SceneDetailResponse;
import com.xiami.sdk.utils.ImageUtil;

public class DailyDetailFragment extends Fragment implements OnClickListener,
		OnTouchListener, RequestBack {
	private final String TAG = "DailyDetailFragment";
	private MainActivity mainActivity;
	private View layoutView;
	private ViewPager mViewPager;
	private FragmentPagerAdapter mAdapter;
	private DetailListFragment mFragment;
	private TextView action_back;
	private TextView all_downLoad_Tv;
	private TextView batch_select_Tv;
	private ImageView img_Iv;
	private TextView numsong_Tv;
	private String title;
	// private ImageView action_search;
	private String id;
	private String type;
	private String logo;
	private RankDetailResponse mRankDetailResponse;
	private SceneDetailResponse mSceneDetailResponse;
	private RecomendHotSongsResponse bean;
	private ArrayList<SongDetailInfo> canListerList = new ArrayList<SongDetailInfo>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {

			title = getArguments().getString(Constants.TITLE);

			id = getArguments().getString(Constants.KEY);
			type = getArguments().getString(Constants.TYPE);
			logo = getArguments().getString(Constants.LOGO);
		}
	}

	/**
	 * 解决事件穿透问题
	 */
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		view.setOnTouchListener(this);
		super.onViewCreated(view, savedInstanceState);
	}

	/**
	 * 解决fragment事件穿透问题
	 */
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		return true;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mainActivity = (MainActivity) getActivity();
		if (layoutView == null) {
			layoutView = inflater.inflate(R.layout.fragment_dailydetail_layout,
					null);
			StateBarUtils.changeOriginalStatus(mainActivity.getWindow());
			findViewById();
			init();
			setListener();
		}
		// 缓存的rootView需要判断是否已经被加过parent，
		// 如果有parent需要从parent删除，要不然会发生这个rootview已经有parent的错误。
		ViewGroup parent = (ViewGroup) layoutView.getParent();
		if (parent != null) {
			parent.removeView(layoutView);
		}
		return layoutView;
	}

	protected void findViewById() {
		mViewPager = (ViewPager) layoutView
				.findViewById(R.id.id_stickynavlayout_viewpager);
		action_back = (TextView) layoutView.findViewById(R.id.action_back);
		batch_select_Tv = (TextView) layoutView
				.findViewById(R.id.batch_select_Tv);
		all_downLoad_Tv = (TextView) layoutView
				.findViewById(R.id.all_downLoad_Tv);
		numsong_Tv = (TextView) layoutView.findViewById(R.id.numsong_Tv);
		img_Iv = (ImageView) layoutView.findViewById(R.id.img_Iv);
		// action_search = (ImageView)
		// layoutView.findViewById(R.id.action_search);
		if (!TextUtils.isEmpty(title)) {
			action_back.setText(title);
		}
	}

	protected void setListener() {
		action_back.setOnClickListener(this);
		all_downLoad_Tv.setOnClickListener(this);
		batch_select_Tv.setOnClickListener(this);
		mFragment.setBack(this);
		// action_search.setOnClickListener(this);
	}

	protected void init() {
		mFragment = new DetailListFragment();
		Bundle bundle = new Bundle();
		if (!TextUtils.isEmpty(title)) {
			bundle.putString(Constants.TITLE, title);
		}
		if (!TextUtils.isEmpty(id)) {
			bundle.putString(Constants.KEY, id);
		}
		if (!TextUtils.isEmpty(type)) {
			bundle.putString(Constants.TYPE, type);
		}
		if (!TextUtils.isEmpty(logo)) {
			bundle.putString(Constants.LOGO, logo);
		}
		mFragment.setArguments(bundle);
		mAdapter = new FragmentPagerAdapter(getChildFragmentManager()) {

			@Override
			public Fragment getItem(int arg0) {

				return mFragment;
			}

			@Override
			public int getCount() {

				return 1;
			}

		};
		mViewPager.setAdapter(mAdapter);
		mViewPager.setCurrentItem(0);

	}

	@Override
	public void onClick(View v) {
		Intent intent = null;
		switch (v.getId()) {
		case R.id.action_back:
			DailyDetailFragment.this.getFragmentManager().popBackStack();
			break;
		case R.id.batch_select_Tv:
			UiUtils.goToBatchEidtActivity(mainActivity, canListerList);
			break;
		case R.id.all_downLoad_Tv:
			if (ClientInfo.networkType == ClientInfo.NONET) {
				ToastUtils.showToast(R.string.net_error);
				return;
			}
			if (canListerList.size() <= 0) {
				ToastUtils.showToast(R.string.nodata_to_operator);
				return;
			}
			boolean hasDownload = false;
			ArrayList<SongDetailInfo> list =new ArrayList<SongDetailInfo>();
			for (SongDetailInfo songDetailInfo : canListerList) {
				if (!DownloadHelper.isFileExists(songDetailInfo)) {
					hasDownload = true;
					list.add(songDetailInfo);
				}
			}
			if (!hasDownload) {
				ToastUtils.showToast(R.string.no_downloadTask);
			} else {
				if (ClientInfo.networkType == ClientInfo.NONET) {
					ToastUtils.showToast(R.string.net_error);
					return;
				}
				DownLoadUtils.downloadMultMusic(list);
				ToastUtils.showToast(R.string.add_download_queue_ok);

			}
			break;
		case R.id.action_search:
			intent = new Intent(mainActivity, SearchActivity.class);
			mainActivity.startActivity(intent);
			break;

		default:
			break;
		}

	}

	@Override
	public void onDestroy() {
		StateBarUtils.changeStatus(mainActivity.getWindow());
		if (mFragment != null) {
			mFragment.setBack(null);
		}
		super.onDestroy();
	}

	@Override
	public void back(Object res,ArrayList<SongDetailInfo> list) {
		if (res != null && getActivity() != null) {
			if (res instanceof RecomendHotSongsResponse) {
				bean = (RecomendHotSongsResponse) res;
				if (bean != null) {
					if (!TextUtils.isEmpty(bean.title)) {
						action_back.setText(bean.title);
					}
					if (!TextUtils.isEmpty(bean.logo)) {
						JLog.i(TAG, ImageUtil.transferImgUrl(bean.logo, 640));
						ImageLoader.getInstance().displayImage(
								ImageUtil.transferImgUrl(bean.logo, 640),
								img_Iv, UILimageUtil.getADCacheUILoptions());
					}

//					list = bean.songs;
					canListerList =list;
//					list = bean.songs;
//					canListerList = CommonUtils
//							.filterUnabelListerSong(bean.songs);
					numsong_Tv.setText(getActivity().getString(
							R.string.number_songs, canListerList.size()));
				}

				return;
			}
			if (res instanceof RankDetailResponse) {
				mRankDetailResponse = (RankDetailResponse) res;
				if (mRankDetailResponse != null) {

					if (!TextUtils.isEmpty(mRankDetailResponse.title)) {
						if (!RankUtils.setRankDeatailImagAndText(img_Iv,
								action_back, mRankDetailResponse.title)) {
							action_back.setText(mRankDetailResponse.title);
							String url = TextUtils
									.isEmpty(mRankDetailResponse.logo) ? mRankDetailResponse.logo_middle
									: mRankDetailResponse.logo;
							if (!TextUtils.isEmpty(url)) {
								JLog.i(TAG, ImageUtil.transferImgUrl(url, 640));
								ImageLoader.getInstance().displayImage(
										ImageUtil.transferImgUrl(url, 640),
										img_Iv,
										UILimageUtil
												.getDetaiHeadImgUILoptions());
							}

						}
					}
				}
				canListerList = list;
				numsong_Tv.setText(getActivity().getString(
						R.string.number_songs, canListerList.size()));
				return;
			}
		}
		if (res instanceof SceneDetailResponse) {
			mSceneDetailResponse = (SceneDetailResponse) res;
			if (mSceneDetailResponse != null) {
				if (!TextUtils.isEmpty(title)) {
					action_back.setText(title);
				} else {
					if (!TextUtils.isEmpty(mSceneDetailResponse.radio_name)) {
						action_back.setText(mSceneDetailResponse.radio_name);
					}
				}
				String url = mSceneDetailResponse.logo;
				if (!TextUtils.isEmpty(url)) {
					JLog.i(TAG, ImageUtil.transferImgUrl(url, 640));
					ImageLoader.getInstance().displayImage(
							ImageUtil.transferImgUrl(url, 640), img_Iv,
							UILimageUtil.getDetaiHeadImgUILoptions());
				} else {
					if (!TextUtils.isEmpty(logo)) {
						JLog.i(TAG, ImageUtil.transferImgUrl(logo, 640));
						ImageLoader.getInstance().displayImage(
								ImageUtil.transferImgUrl(logo, 640), img_Iv,
								UILimageUtil.getDetaiHeadImgUILoptions());
					}
				}

			}
			canListerList = list;
			numsong_Tv.setText(getActivity().getString(R.string.number_songs,
					canListerList.size()));

		}

	}

	/**
	 * 
	 * 
	 * @param id
	 *            请求参数
	 * @param type
	 *            类型
	 * @param title
	 *            title
	 * @param logo
	 *            url
	 * @return DailyDetailFragment
	 * @see
	 */
	public static DailyDetailFragment newInstance(String id, String type,
			String title, String logo) {
		DailyDetailFragment tabFragment = new DailyDetailFragment();
		Bundle bundle = new Bundle();
		bundle.putString(Constants.KEY, id);
		bundle.putString(Constants.TYPE, type);
		if (!TextUtils.isEmpty(title)) {
			bundle.putString(Constants.TITLE, title);

		}
		if (!TextUtils.isEmpty(logo)) {
			bundle.putString(Constants.LOGO, logo);

		}
		tabFragment.setArguments(bundle);
		return tabFragment;
	}

	public static DailyDetailFragment newInstance(String title) {
		DailyDetailFragment tabFragment = new DailyDetailFragment();
		Bundle bundle = new Bundle();
		if (!TextUtils.isEmpty(title)) {
			bundle.putString(Constants.TITLE, title);

		}
		tabFragment.setArguments(bundle);
		return tabFragment;
	}
}
