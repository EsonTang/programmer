package com.prize.appcenter.activity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.GridView;

import com.prize.app.beans.ClientInfo;
import com.prize.app.constants.Constants;
import com.prize.app.util.WindowMangerUtils;
import com.prize.appcenter.R;
import com.prize.appcenter.ui.actionBar.ActionBarNoTabActivity;
import com.prize.appcenter.ui.adapter.FeedBackReasonAdapter;
import com.prize.appcenter.ui.adapter.FeedBackReasonAdapter.ResOnItemClick;
import com.prize.appcenter.ui.util.ToastUtils;
import com.prize.appcenter.ui.util.UIUtils;
import com.prize.appcenter.ui.widget.PrizeCommButton;
import com.prize.custmerxutils.XExtends;
import com.tencent.stat.StatService;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;

/**
 ** 
 * 应用举报
 * 
 * @author longbaoxiu
 * @version V1.0
 */
public class AppFeedbackActivity extends ActionBarNoTabActivity {
	/** 消息编辑框 */
	private EditText contentET;
	// private PromptDialogFragment df;
	private ProgressDialog dialog;
	private PrizeCommButton feedback_send_btn;
	private boolean isRequesting = false;
	public static final String APP_NAME = "app_name";
	public static final String APP_ID = "app_id";
	public static final String APP_VERSIONNAME = "app_versionName";
	public static final String APP_PKG = "app_pkg";
	private GridView recommand_notice_gv;
	private View line;
	private FeedBackReasonAdapter mFeedBackReasonAdapter;
	private String appVersionName;
	private String appId;
	private String appName;
	private String packageName;
	private String[] childList = { "恶意广告", "携带病毒", "恶意扣费", "色情暴力内容", "含不良插件",
			"运行出错闪退", "更新失败", "无法安装", "其他问题" };

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setNeedAddWaitingView(false);
		setContentView(R.layout.activity_appfeedback);
		WindowMangerUtils.changeStatus(getWindow());
		setTitle(R.string.app_report);
		findViewById();

		init();
		feedback_send_btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				sendFeedback();
			}
		});

		setListener();

	}

	private void setListener() {
		mFeedBackReasonAdapter.setmResOnItemClick(new ResOnItemClick() {

			@Override
			public void callBack(String value) {
				if (!TextUtils.isEmpty(value) && "其他问题".equals(value)) {
					contentET.setVisibility(View.VISIBLE);
					line.setVisibility(View.VISIBLE);
				} else {
					contentET.setVisibility(View.INVISIBLE);
					line.setVisibility(View.INVISIBLE);
				}

			}
		});

		// recommand_notice_gv.setOnItemClickListener(new OnItemClickListener()
		// {
		//
		// @Override
		// public void onItemClick(AdapterView<?> parent, View view,
		// int position, long id) {
		// mFeedBackReasonAdapter.toggleCheckedState(position);
		//
		// }
		// });

	}

	private void init() {
		// /api/settings
		appName = getIntent().getStringExtra(APP_NAME);
		appVersionName = getIntent().getStringExtra(APP_VERSIONNAME);
		appId = getIntent().getStringExtra(APP_ID);
		packageName = getIntent().getStringExtra(APP_PKG);
		mFeedBackReasonAdapter = new FeedBackReasonAdapter(this);
		recommand_notice_gv.setAdapter(mFeedBackReasonAdapter);
		mFeedBackReasonAdapter.setData(childList);

	}

	private void findViewById() {

		line = findViewById(R.id.line);
		contentET = (EditText) findViewById(R.id.fedbck_content_et);
		InputFilter emojiFilter = UIUtils.getEmojiFilter();
		contentET.setFilters(new InputFilter[] { emojiFilter });
		feedback_send_btn = (PrizeCommButton) findViewById(R.id.feedback_send_btn);
		recommand_notice_gv = (GridView) findViewById(R.id.recommand_notice_gv);
		feedback_send_btn.enabelDefaultPress(true);
	}

	private void sendFeedback() {
		String fbType = mFeedBackReasonAdapter.getChooseString();
		if (TextUtils.isEmpty(fbType)) {
			ToastUtils.showToast(R.string.pl_select_class);
			return;
		}
		String content = contentET.getText().toString();
		if(contentET.getVisibility()==View.VISIBLE){
			if (TextUtils.isEmpty(content)) {
				ToastUtils.showToast(R.string.feedback_msg_empty);
				return;
			}
		}
		if (ClientInfo.networkType == ClientInfo.NONET) {
			ToastUtils.showToast(R.string.nonet_connect);
			return;
		}
		if (isRequesting) {
			ToastUtils.showToast(R.string.questing);
			return;
		}
		String url = Constants.GIS_URL + "/appinfo/complain";
		RequestParams params = new RequestParams(url);
		params.addBodyParameter("appId", appId);
		params.addBodyParameter("packageName", packageName);
		params.addBodyParameter("appName", appName);
		params.addBodyParameter("versionName", appVersionName);
		params.addBodyParameter("complainType", fbType);
		params.addBodyParameter("content", content);
		XExtends.http().post(params, new Callback.ProgressCallback<String>() {

			@Override
			public void onSuccess(String result) {
				if (dialog != null && dialog.isShowing()
						&& !AppFeedbackActivity.this.isFinishing()) {
					dialog.dismiss();
				}
				try {
					JSONObject obi = new JSONObject(result);
					int code = obi.getInt("code");
					if (code == 0) {
						ToastUtils.showToast(R.string.send_fedbck_tip);
						finish();
					} else {
						String msg = obi.getString("msg");
						ToastUtils.showToast(msg);
					}
				} catch (JSONException e) {
					e.printStackTrace();
					isRequesting = false;

				}

				isRequesting = false;
			}

			@Override
			public void onError(Throwable ex, boolean isOnCallback) {

				if (dialog != null && dialog.isShowing()
						&& !AppFeedbackActivity.this.isFinishing()) {
					dialog.dismiss();
				}
				isRequesting = false;
				ToastUtils.showToast(ex.getMessage());

			}

			@Override
			public void onCancelled(CancelledException cex) {

			}

			@Override
			public void onFinished() {

				// TODO Auto-generated method stub

			}

			@Override
			public void onWaiting() {

			}

			@Override
			public void onStarted() {

				isRequesting = true;
				dialog = new ProgressDialog(AppFeedbackActivity.this);
				dialog.setMessage(getString(R.string.committing));
				dialog.setCancelable(true);
				if (!AppFeedbackActivity.this.isFinishing()) {
					dialog.show();
				}
			}

			@Override
			public void onLoading(long total, long current,
					boolean isDownloading) {

			}

		});

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (dialog != null && dialog.isShowing()) {
			dialog.dismiss();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		StatService.onPause(this);
	}

	@Override
	public void onBack(int what, int arg1, int arg2, Object obj) {

	}

	@Override
	public String getActivityName() {
		return "FeedbackExActivity";
	}

	@Override
	protected void initActionBar() {
		findViewById(R.id.action_bar_feedback).setVisibility(View.INVISIBLE);
		super.initActionBar();
	}

	protected void onResume() {
		super.onResume();
		StatService.onResume(this);
	};

}
