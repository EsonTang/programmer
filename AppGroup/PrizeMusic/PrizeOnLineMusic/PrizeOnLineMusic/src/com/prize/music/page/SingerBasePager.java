package com.prize.music.page;

import java.util.HashMap;

import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.prize.app.constants.RequestMethods;
import com.prize.app.constants.RequestResCode;
import com.prize.app.util.SDKUtil;
import com.prize.music.R;
import com.prize.music.helpers.utils.UiUtils;
import com.prize.music.online.task.RecommendSingerTask;
import com.prize.music.ui.adapters.HotSingerAdapter;
import com.prize.onlinemusibean.RecommendSingerResponse;
import com.xiami.sdk.XiamiSDK;

/**
 * 华语音乐
 * 
 * @author pengyang
 *
 */
public class SingerBasePager extends BasePager implements OnClickListener {

	private FragmentActivity context;
	private XiamiSDK xiamiSDK;
	private HotSingerAdapter singerAdapter;

	private TextView singer_M;
	private TextView singer_W;
	private TextView singer_B;
	private GridView recommand_singer_gv;

	public SingerBasePager(FragmentActivity activity) {
		super(activity);
		this.context = activity;
		xiamiSDK = new XiamiSDK(activity, SDKUtil.KEY, SDKUtil.SECRET);
		singerAdapter = new HotSingerAdapter(context);
	}

	@Override
	public View onCreateView() {
		LayoutInflater inflater = LayoutInflater.from(context);
		View rootView = inflater.inflate(R.layout.pager_more_singer, null);

		singer_M = (TextView) rootView.findViewById(R.id.singer_M_tv);
		singer_W = (TextView) rootView.findViewById(R.id.singer_W_tv);
		singer_B = (TextView) rootView.findViewById(R.id.singer_B_tv);
		singer_M.setOnClickListener(this);
		singer_W.setOnClickListener(this);
		singer_B.setOnClickListener(this);

		recommand_singer_gv = (GridView) rootView
				.findViewById(R.id.recommand_singer_gv);
		recommand_singer_gv.setAdapter(singerAdapter);

		// 热门歌手的点击事件
		recommand_singer_gv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				// 跳转到单个歌手

				if (singerAdapter.getItem(position) != null) {
					UiUtils.JumpToSingerOnlineActivity(activity,
							singerAdapter.getItem(position),
							singerAdapter.getItem(position).artist_id);
				}
			}
		});

		return rootView;
	}

	private Handler recSingerHandler = new Handler() {

		public void handleMessage(Message msg) {
			hideWaiting();
			switch (msg.what) {
			case RequestResCode.REQUEST_OK:

				Gson gson = requestManager.getGson();
				JsonElement element = (JsonElement) msg.obj;
				RecommendSingerResponse beans = gson.fromJson(element,
						RecommendSingerResponse.class);

				// 显示
				if (singerAdapter != null && beans.artists != null) {
					singerAdapter.setData(beans.artists);
				}
				break;
			case RequestResCode.REQUEST_FAILE:
				if (null != singerAdapter && singerAdapter.getCount() == 0) {
					loadingFailed(new ReloadFunction() {

						@Override
						public void reload() {
							loadData();
						}
					});
				}
				break;
			case RequestResCode.REQUEST_EXCEPTION:
				break;
			}
		};
	};

	@Override
	public void loadData() {
		RecommendSingerTask singertask = new RecommendSingerTask(xiamiSDK,
				RequestMethods.METHOD_RECOMMEND_ARTIST, recSingerHandler);
		HashMap<String, Object> params = new HashMap<String, Object>();
		singertask.execute(params);
	}

	@Override
	public void onActivityCreated() {

	}

	@Override
	public String getPageName() {
		return "ChinesePager";
	}

	@Override
	public void onDestroy() {
		if (recSingerHandler != null) {
			recSingerHandler.removeCallbacksAndMessages(null);
		}
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.singer_M_tv:
			OnManClick();
			break;
		case R.id.singer_W_tv:
			OnWomenClick();
			break;
		case R.id.singer_B_tv:
			OnBandClick();
			break;
		default:
			break;
		}
	}

	/** 点击歌手组合 */
	public void OnBandClick() {
	}

	/** 点击女歌手 */
	public void OnWomenClick() {
	}

	/** 点击男歌手 */
	public void OnManClick() {
	}

}
