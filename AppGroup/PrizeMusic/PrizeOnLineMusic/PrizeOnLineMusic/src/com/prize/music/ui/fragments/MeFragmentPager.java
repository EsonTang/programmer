package com.prize.music.ui.fragments;

import java.util.ArrayList;

import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.prize.app.beans.Person;
import com.prize.app.constants.RequestResCode;
import com.prize.app.database.dao.GameDAO;
import com.prize.app.util.CommonUtils;
import com.prize.app.util.JLog;
import com.prize.music.helpers.utils.UILimageUtil;
import com.prize.music.helpers.utils.UiUtils;
import com.prize.music.page.BasePager;
import com.prize.music.views.CircleImageViewTwo;
import com.prize.music.R;
import com.prize.onlinemusibean.SongDetailInfo;

/**
 **
 * 下载完成
 * 
 * @author longbaoxiu
 * @version V1.0
 */
public class MeFragmentPager extends BasePager {
	private static final String TAG = "MeFragmentPager";
	private ArrayList<SongDetailInfo> list = new ArrayList<SongDetailInfo>();

	private FragmentActivity context;
	private Person person = null;
	CircleImageViewTwo local_icon;
	TextView user_name;
	TextView user_login;

	public MeFragmentPager(FragmentActivity activity) {
		super(activity);
		this.context = activity;

	}

	public void onActivityCreated() {

	}

	/**
	 * 初始化界面
	 */
	public View onCreateView() {
		LayoutInflater inflater = LayoutInflater.from(activity);
		View root = inflater.inflate(R.layout.fragment_me_layout, null);
		local_icon = (CircleImageViewTwo) root.findViewById(R.id.local_icon);
		user_login = (TextView) root.findViewById(R.id.user_login);
		user_name = (TextView) root.findViewById(R.id.user_name);
		user_login.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				UiUtils.jumpToLoginActivity();
			}
		});
		user_name.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!TextUtils.isEmpty(CommonUtils.queryUserId())) {
					UiUtils.jumpLogoutActivity();
				} else {
					UiUtils.jumpToLoginActivity();
				}
			}
		});
		local_icon.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!TextUtils.isEmpty(CommonUtils.queryUserId())) {
					UiUtils.jumpLogoutActivity();
				} else {
					UiUtils.jumpToLoginActivity();
				}
			}
		});
		return root;
	}

	private Handler rankHandler = new Handler() {
		public void handleMessage(Message msg) {
			hideWaiting();
			switch (msg.what) {
			case RequestResCode.REQUEST_OK:

				break;
			case RequestResCode.REQUEST_FAILE:

				break;
			case RequestResCode.REQUEST_EXCEPTION:
				break;
			}
		};
	};

	@Override
	public void onDestroy() {
		if (rankHandler != null) {
			rankHandler.removeCallbacksAndMessages(null);
		}
	}

	@Override
	public String getPageName() {
		return "DownLoadCompletePager";
	}

	@Override
	public void onResume() {
		super.onResume();
		person = CommonUtils.queryUserInfo(context);
		if (!TextUtils.isEmpty(CommonUtils.queryUserId())) {
			if (person != null) {
				if (!TextUtils.isEmpty(person.getAvatar())) {
					ImageLoader.getInstance().displayImage(person.getAvatar(),
							local_icon, UILimageUtil.getPersonHeadImg());
				} else {
					if ((person.getSex()) == 0) {
						local_icon
								.setImageResource(R.drawable.cloud_woman_small);
					} else if ((person.getSex()) == 1) {
						local_icon.setImageResource(R.drawable.cloud_man_small);
					}
				}
				user_name
						.setText(TextUtils.isEmpty(person.getRealName()) ? person
								.getUserId() : person.getRealName());
				user_name.setVisibility(View.VISIBLE);
				user_login.setVisibility(View.GONE);
			}
		} else {
			user_name.setVisibility(View.GONE);
			user_login.setVisibility(View.VISIBLE);
			local_icon.setImageResource(R.drawable.local_head_icon);
		}
		// GameDAO.getInstance().getDownLoadedAppList();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void loadData() {
		// TODO Auto-generated method stub

	}

}
