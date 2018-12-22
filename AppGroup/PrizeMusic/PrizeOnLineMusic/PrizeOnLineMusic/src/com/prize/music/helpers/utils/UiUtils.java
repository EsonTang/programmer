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

package com.prize.music.helpers.utils;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

import com.prize.app.BaseApplication;
import com.prize.app.constants.Constants;
import com.prize.app.util.ToastUtils;
import com.prize.cloud.activity.LogonActivity;
import com.prize.cloud.activity.MainActivityCloud;
import com.prize.cloud.activity.PersonActivity;
import com.prize.music.activities.BatchSelectActivity;
import com.prize.music.activities.SearchActivity;
import com.prize.music.activities.SettingActivity;
import com.prize.music.activities.SingerByTypeActivity;
import com.prize.music.activities.SingerOnlineActivity;
import com.prize.music.activities.ToAlbumDetailActivity;
import com.prize.music.ui.fragments.DailyDetailFragment;
import com.prize.music.ui.fragments.DetailFragment;
import com.prize.music.R;
import com.prize.onlinemusibean.ArtistsBean;
import com.prize.onlinemusibean.SongDetailInfo;

/**
 * 挑转专辑 歌单 排行 ，电台详情
 **
 * 
 * @author longbaoxiu
 * @version V1.0
 */
public class UiUtils {
	/**
	 * 
	 * 电台详情
	 * 
	 * @param context
	 * @param id
	 *            请求参数int类型
	 * @param type
	 *            类型（Constants.KEY_COLLECT，Constants.KEY_ALBUM，Constants.
	 *            KEY_SONGS，Constants.KEY_RADIO，Constants.KEY_RANK）
	 * 
	 * @param title
	 * 
	 * @param logo
	 *            url
	 * 
	 * @return void
	 */
	public static void gotoMoreDaily(FragmentActivity context, String id,
			String type, String title, String logo) {
		FragmentTransaction ft = context.getSupportFragmentManager()
				.beginTransaction();
		ft.add(R.id.MainFragment_container,
				DailyDetailFragment.newInstance(id, type, title, logo),
				DailyDetailFragment.class.getName());
		ft.addToBackStack(null);
		ft.commitAllowingStateLoss();

	}

	/**
	 * 
	 * 
	 * @param context
	 * @param id
	 *            请求参数int类型
	 * @param type
	 *            类型（Constants.KEY_COLLECT，Constants.KEY_ALBUM，Constants.
	 *            KEY_SONGS，Constants.KEY_RADIO，Constants.KEY_RANK）
	 * @return void
	 * @see
	 */
	public static void gotoMoreDaily(FragmentActivity context, int id,
			String type) {
		if(CommonClickUtils.isFastDoubleClick())
			return;
		FragmentTransaction ft = context.getSupportFragmentManager()
				.beginTransaction();
		ft.add(R.id.MainFragment_container,
				DetailFragment.newInstance(id + "", type),
				DetailFragment.class.getName());
		ft.addToBackStack(null);
		ft.commitAllowingStateLoss();

	}
	/**
	 * 
	 * 
	 * @param context
	 * @param id
	 *            请求参数int类型
	 * @param type
	 *            类型（Constants.KEY_COLLECT，Constants.KEY_ALBUM，Constants.
	 *            KEY_SONGS，Constants.KEY_RADIO，Constants.KEY_RANK）
	 * @return void
	 * @see
	 */
	public static void gotoMoreDailyFromSortMenu(FragmentActivity context, int id,
			String type,String whereFrom) {
		FragmentTransaction ft = context.getSupportFragmentManager()
				.beginTransaction();
		ft.add(R.id.MainFragment_container,
				DetailFragment.newInstance(String.valueOf(id), type, whereFrom),
				DetailFragment.class.getName());
		ft.addToBackStack(null);
		ft.commitAllowingStateLoss();
		
	}

	/**
	 * 
	 * 无请求参数的传递挑转 入今日推荐，热门歌曲
	 * 
	 * @param context
	 * @param resType
	 *            请求参数String类型 ，不能使用int类型
	 * @return void
	 */
	public static void gotoDailyMoreDaily(FragmentActivity context, String param) {
		FragmentTransaction ft = context.getSupportFragmentManager()
				.beginTransaction();
		ft.add(R.id.MainFragment_container,
				DailyDetailFragment.newInstance(param),
				DailyDetailFragment.class.getName());
		ft.addToBackStack(null);
		ft.commitAllowingStateLoss();

	}

	/**
	 * 
	 * 
	 * @param context
	 * @param resType
	 *            请求参数String类型 ，不能使用int类型
	 * @param type
	 *            类型（Constants.KEY_COLLECT，Constants.KEY_ALBUM，Constants.
	 *            KEY_SONGS，Constants.KEY_RADIO，Constants.KEY_RANK）
	 * @return void
	 * @see
	 */
	public static void gotoMoreDaily(FragmentActivity context, String resType,
			String type) {
		FragmentTransaction ft = context.getSupportFragmentManager()
				.beginTransaction();
		ft.add(R.id.MainFragment_container,
				DailyDetailFragment.newInstance(resType, type, null, null),
				DailyDetailFragment.class.getName());
		ft.addToBackStack(null);
		ft.commitAllowingStateLoss();

	}
	/**
	 * 
	 * 
	 * @param context
	 * @param id
	 *            请求参数int类型
	 * @param type
	 *            类型（Constants.KEY_COLLECT，Constants.KEY_ALBUM，Constants.
	 *            KEY_SONGS，Constants.KEY_RADIO，Constants.KEY_RANK）
	 * @return void
	 * @see
	 */
	public static void gotoAlbumDeatail(FragmentActivity context, int id,int containerId,
			String type) {
		FragmentTransaction ft = context.getSupportFragmentManager()
				.beginTransaction();
		ft.add(containerId,
				DetailFragment.newInstance(id + "", type),
				DetailFragment.class.getName());
		ft.addToBackStack(null);
		ft.commitAllowingStateLoss();

	}

	/**
	 * 跳转到批量编辑
	 * 
	 * @param mainActivity
	 * @param list
	 * @return void
	 * @see
	 */
	public static void goToBatchEidtActivity(Context mainActivity,
			List<SongDetailInfo> list) {
		Intent intent;
		if (list != null && !list.isEmpty()) {
			intent = new Intent(mainActivity, BatchSelectActivity.class);
			intent.putParcelableArrayListExtra(Constants.INTENTTRANSBEAN,
					(ArrayList<SongDetailInfo>) list);
			mainActivity.startActivity(intent);
		} else {
			ToastUtils.showToast(R.string.nodata_to_operator);
		}
	}

	public static void goToSearchtActivity(Context mainActivity) {
		Intent intent = new Intent(mainActivity, SearchActivity.class);
		mainActivity.startActivity(intent);
	}
	
	public static void goToSettingtActivity(Context mainActivity) {
		Intent intent = new Intent(mainActivity, SettingActivity.class);
		mainActivity.startActivity(intent);
	}

	/**
	 * 方法描述：跳转到云账号登录页面
	 * 
	 * @return void
	 * @see 类名/完整类名/完整类名#方法名
	 */
	public static void jumpToLoginActivity() {
		Intent intent = new Intent(BaseApplication.curContext,
				MainActivityCloud.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		BaseApplication.curContext.startActivity(intent);
	}

	/**
	 * 方法描述：跳转到云账号登录页面
	 * 
	 * @return void
	 * @see 类名/完整类名/完整类名#方法名
	 */
	public static void jumpLogoutActivity() {
		Intent intent = new Intent(BaseApplication.curContext,
				PersonActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		BaseApplication.curContext.startActivity(intent);
	}
	
		
	/**
	 * 跳转到歌手详情页
	 * @param context
	 * @param artist_id
	 */
	public static void JumpToSingerOnlineActivity(Context context,ArtistsBean bean,int artist_id){
		Intent intent = new Intent(context, SingerOnlineActivity.class);
		Bundle bundle = new Bundle();
		bundle.putSerializable("ArtistsBean", bean);
		bundle.putInt("artist_id", artist_id);
		intent.putExtras(bundle);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS); 
		context.startActivity(intent);
	}
	
	public static void JumpToSingerByTypeActivity(Context context,String title,String type){
		Intent intent = new Intent(context, SingerByTypeActivity.class);
		intent.putExtra("title", title);
		intent.putExtra("type", type);
		context.startActivity(intent);
	}
	
	public static String ConcatString(Context context ,int first,int end){
		return context.getString(first).concat(context.getString(end));
	}
	
	public static void JumpToAlbumDetail(Context context,int album_id){
		Intent intent = new Intent(context, ToAlbumDetailActivity.class);
		Bundle bundle = new Bundle();
		bundle.putInt("album_id", album_id);
		intent.putExtras(bundle);
		context.startActivity(intent);
	}

}
