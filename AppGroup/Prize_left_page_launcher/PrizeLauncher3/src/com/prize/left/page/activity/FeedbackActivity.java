package com.prize.left.page.activity;

import java.io.File;
import java.util.ArrayList;

import org.xutils.x;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.PaintDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.android.launcher3.R;
import com.prize.left.page.adapter.ExpandableListAdapter;
import com.prize.left.page.ui.CornerImageView;
import com.prize.left.page.util.ClientInfo;
import com.prize.left.page.util.CommonUtils;
import com.prize.left.page.util.FilterUtils;
import com.prize.left.page.util.ToastUtils;

/**
 * 意见反馈
 * @author fanjunchen
 */
public class FeedbackActivity extends Activity implements View.OnClickListener {
	/** 消息编辑框 */
	private EditText contentET;
	
	private Button add_Btn;
	
	private int PICTURE = 100;
	
	private String picturePath;
	
	private ArrayList<File> files = new ArrayList<File>();
	
	private ArrayList<String> picturePaths = new ArrayList<String>();
	
	private LinearLayout tableLayout;
	
	private ProgressDialog dialog;
	
	private boolean flag = true;
	
	private Button feedback_send_btn;
	
	private boolean isRequesting = false;
	
	private PopupWindow popupWindow;
	private ExpandableListView mExpandableListView;
	private ExpandableListAdapter mExpandableListAdapter;
	private String[] groupList;
	
	private String[][] childList = {
			{ "操作的时候卡,不流畅", "崩溃了", "长时间出现白屏" },
			{ "找不到想要的结果", "想要的结果排在了后面", "出现了相同的结果无法选择", "搜索结果慢,其他搜索问题" },
			{ "无法退出,总是在后台运行", "连不上网,加载不了", "控件清理遇到问题" }, {} };
	
	private int currentParamInt = -1;
	
	public static final String SELECTSUB = "selectsub";
	private int enterPosition = 0;
	private boolean isFirst = true;
	
	private LinearLayout select_Llyt;
	private TextView select_class_Tv;
	private TextView sub_title_Tv;
	private EditText contact_et;
	
	private AlertDialog mDialog = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		initStatusBar();
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_feedbackex);
		
		initView();
	}
	
	private void initView() {
		
		contentET = (EditText) findViewById(R.id.fedbck_content_et);
		add_Btn = (Button) findViewById(R.id.add_Btn);
		tableLayout = (LinearLayout) findViewById(R.id.tableLayout);
		
		CommonUtils.changeStatus(getWindow());
		
		InputFilter emojiFilter = FilterUtils.getEmojiFilter();
		contentET.setFilters(new InputFilter[] { emojiFilter });

		feedback_send_btn = (Button) findViewById(R.id.feedback_send_btn);

		feedback_send_btn.setOnClickListener(this);

		add_Btn.setOnClickListener(this);
		
		select_class_Tv = (TextView) findViewById(R.id.select_class_Tv);
		sub_title_Tv = (TextView) findViewById(R.id.sub_title_Tv);
		contact_et = (EditText) findViewById(R.id.contact_et);
		select_Llyt = (LinearLayout) findViewById(R.id.select_Llyt);
		
		initData();
		setListener();
		init();
		
		setTitle();
	}
	
	private void initData() {

		String selectsub = getIntent().getStringExtra(SELECTSUB);
		groupList = getResources().getStringArray(R.array.feed_title_array);
		int len = groupList.length;
		if (TextUtils.isEmpty(selectsub)) {
			return;
		}
		for (int i = 0; i < len; i++) {
			if (selectsub.equals(groupList[i])) {
				enterPosition = i;
				break;
			}
		}
	}
	
	private void init() {

		mExpandableListAdapter = new ExpandableListAdapter(groupList,
				childList, this);

	}

	private void setListener() {
		select_Llyt.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				getPopupWindow(v);
			}
		});

	}
	
	/***
	 * 获取PopupWindow实例
	 * @param v
	 */
	private void getPopupWindow(View v) {
		if (null != popupWindow) {
			if (popupWindow.isShowing()) {
				popupWindow.dismiss();
			} else {
				popupWindow.showAsDropDown(v);
			}
		} else {
			initPopuptWindow();
			popupWindow.showAsDropDown(v);
		}
	}
	
	protected void initPopuptWindow() {

		// 获取自定义布局文件activity_popupwindow_left.xml的视图
		View popupWindow_view = getLayoutInflater().inflate(
				R.layout.popupwindow_layout, null, false);
		mExpandableListView = (ExpandableListView) popupWindow_view
				.findViewById(R.id.expandView);
		// mExpandableListView.set
		mExpandableListView.setAdapter(mExpandableListAdapter);
		// 创建PopupWindow实例,200,LayoutParams.MATCH_PARENT分别是宽度和高度
		popupWindow = new PopupWindow(popupWindow_view,
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, true);
		// 设置动画效果
		// 点击其他地方消失
		// popupWindow.setAnimationStyle(R.style.mypopwindow_anim_style);
		popupWindow.setOutsideTouchable(true);
		popupWindow.setBackgroundDrawable(new PaintDrawable());
		popupWindow_view.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (popupWindow != null && popupWindow.isShowing()) {
					popupWindow.dismiss();
					popupWindow = null;
				}
				return true;
			}
		});
		this.mExpandableListView.setGroupIndicator(null);
		mExpandableListView.expandGroup(enterPosition);
		this.mExpandableListView.setChildDivider(new ColorDrawable(
				android.R.color.transparent));
		this.mExpandableListView
				.setOnChildClickListener(new OnChildClickListener() {

					@Override
					public boolean onChildClick(ExpandableListView parent,
							View v, int groupPosition, int childPosition,
							long id) {

						String group = FeedbackActivity.this.groupList[groupPosition];
						String param = FeedbackActivity.this.childList[groupPosition][childPosition];
						select_class_Tv.setText(group);
						sub_title_Tv.setText(param);
						sub_title_Tv.setVisibility(View.VISIBLE);
						mExpandableListAdapter.setCurrentChildId(childPosition);
						mExpandableListAdapter.setSelectGroupId(groupPosition);

						if (null != popupWindow && popupWindow.isShowing()) {
							popupWindow.dismiss();
						}
						return true;
					}
				});

		this.mExpandableListView
				.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {

					@Override
					public boolean onGroupClick(
							ExpandableListView paramExpandableListView,
							View paramView, int paramInt, long paramLong) {
						int length = childList[paramInt].length;
						if (length <= 0) {
							select_class_Tv.setText(groupList[paramInt]);
							mExpandableListAdapter.setCurrentChildId(-1);
							mExpandableListAdapter.setSelectGroupId(paramInt);
							sub_title_Tv.setText("");
							sub_title_Tv.setVisibility(View.GONE);
							if (null != popupWindow && popupWindow.isShowing()) {
								popupWindow.dismiss();
							}
						}
						return false;
					}
				});

		// 这里是控制只有一个group展开的效果
		this.mExpandableListView
				.setOnGroupExpandListener(new OnGroupExpandListener() {
					@Override
					public void onGroupExpand(int groupPosition) {
						for (int i = 0; i < mExpandableListAdapter
								.getGroupCount(); i++) {
							if (groupPosition != i) {
								mExpandableListView.collapseGroup(i);
							} else {
							}
						}
					}
				});
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btn_back:
				hideInputMethod();
				onBackPressed();
				break;
			case R.id.feedback_send_btn:
				for (int i = 0; i < picturePaths.size(); i++) {
					files.add(new File(picturePaths.get(i)));
				}
				sendFeedback();
				break;
			case R.id.add_Btn:
				Intent picture = new Intent(
						Intent.ACTION_PICK,
						android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
				picture.setType("image/*");
				startActivityForResult(picture, PICTURE);
				break;
			case R.id.btn_sure:
				if (mDialog != null)
					mDialog.dismiss();
				hideInputMethod();
				finish();
				break;
			case R.id.btn_cancel:
				if (mDialog != null)
					mDialog.dismiss();
				break;
		}
	}
	
	@Override
	public void onBackPressed() {
		if (flag
				&& (picturePaths.size() > 0 || contentET.getText().toString()
						.trim().length() > 0)) {
			if (mDialog == null) {
				mDialog = new AlertDialog.Builder(this).create();
			}
			mDialog.show();
			Window w = mDialog.getWindow();
			w.setContentView(R.layout.left_dialog_hint);
			w.findViewById(R.id.btn_sure).setOnClickListener(this);
			w.findViewById(R.id.btn_cancel).setOnClickListener(this);
			return;
		}
		super.onBackPressed();
	}
	/***
	 * 设置标题及使刷新按钮不可见
	 */
	private void setTitle() {
		TextView titleView = (TextView) findViewById(R.id.tv_title);
		findViewById(R.id.btn_refresh).setVisibility(View.GONE);
		titleView.setText(R.string.str_feedback);
	}
	/***
	 * 初始化状态栏
	 */
	protected void initStatusBar() {
		
		Window window = getWindow();
		window.requestFeature(Window.FEATURE_NO_TITLE);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			window.setStatusBarColor(getResources().getColor(
					R.color.white));//status_color
		}
		
	}

	private void sendFeedback() {
		String content = contentET.getText().toString();
		
		String contactInfo = contact_et.getText().toString();
		String fbType0 = select_class_Tv.getText().toString();
		String fbType1 = sub_title_Tv.getText().toString();
		String fbType = fbType0 + ":" + fbType1;
		
		if (TextUtils.isEmpty(content.trim())) {
			ToastUtils.showToast(this, R.string.feedback_msg_empty);
			contentET.setText("");
			return;
		}
		
		if (TextUtils.isEmpty(fbType0)) {
			ToastUtils.showToast(this, R.string.pl_select_class);
			return;
		}
		
		if (ClientInfo.networkType == ClientInfo.NONET) {
			ToastUtils.showToast(this, R.string.nonet_connect);
			return;
		}
		if (isRequesting) {
			ToastUtils.showToast(this, R.string.questing);
			return;
		}
		flag = false;
		
		String url = "http://launcher.szprize.cn/zyp/api/feedback";
		RequestParams params = new RequestParams(url);
		params.setMultipart(true);
		params.addBodyParameter("content", content);
		params.setConnectTimeout(30 * 1000);
		params.addBodyParameter("fbType", fbType);
		
		if (!TextUtils.isEmpty(contactInfo)) {
			params.addBodyParameter("contactInfo", contactInfo);
		}
		
		for (int i = 0; i < files.size(); i++) {
			params.addBodyParameter("file" + (i + 1), files.get(i), null);
		}
		x.http().post(params, new Callback.ProgressCallback<String>() {

			@Override
			public void onSuccess(String result) {
				if (dialog != null && dialog.isShowing()
						&& !FeedbackActivity.this.isFinishing()) {
					dialog.dismiss();
				}
				ToastUtils.showToast(FeedbackActivity.this, R.string.send_fedbck_tip);
				hideInputMethod();
				finish();
				isRequesting = false;
			}

			@Override
			public void onError(Throwable ex, boolean isOnCallback) {

				if (dialog != null && dialog.isShowing()
						&& !FeedbackActivity.this.isFinishing()) {
					dialog.dismiss();
				}
				isRequesting = false;
				ToastUtils.showToast(FeedbackActivity.this, ex.getMessage());
			}

			@Override
			public void onCancelled(CancelledException cex) {

			}

			@Override
			public void onFinished() {

			}

			@Override
			public void onWaiting() {
			}

			@Override
			public void onStarted() {

				isRequesting = true;
				dialog = new ProgressDialog(FeedbackActivity.this);
				dialog.setMessage(getString(R.string.committing));
				dialog.setCancelable(true);
				if (!FeedbackActivity.this.isFinishing()) {
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
		hideInputMethod();
	}

	LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
			android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
			LayoutParams.WRAP_CONTENT);

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		super.onActivityResult(requestCode, resultCode, data);
		if (data == null) {
			return;
		}
		if (data.toString().contains("video")) {
			ToastUtils.showToast(this, "注意：请不要选择视频文件");
			return;
		}
		if (resultCode == Activity.RESULT_OK && null != data) {
			lp.weight = 1;
			lp.rightMargin = 12;
			String bitMap = getBitmap(data);
			if (bitMap != null) {
				picturePaths.add(bitMap);
				JuageAddBtn();
				final View view = LayoutInflater.from(this).inflate(
						R.layout.item_feedback, null);
				view.setTag(bitMap);
				view.setLayoutParams(lp);
				CornerImageView game_iv = (CornerImageView) view
						.findViewById(R.id.game_iv);
				game_iv.setScaleType(ScaleType.CENTER_CROP);
				Bitmap bm = decodeSampledBitmapFromResource(bitMap, 100, 100);
				
				game_iv.setImageBitmap(bm);
				ImageView delete_iv = (ImageView) view
						.findViewById(R.id.delete_iv);
				delete_iv.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						String path = (String) view.getTag();
						for (int i = 0; i < picturePaths.size(); i++) {
							if (picturePaths.get(i).equals(path)) {
								picturePaths.remove(i);
								break;
							}
						}
						tableLayout.removeView(view);
						JuageAddBtn();
					}
				});
				tableLayout.addView(view);
				// }

			}
		}
	}

	private void JuageAddBtn() {
		if (picturePaths.size() >= 3) {
			add_Btn.setVisibility(View.GONE);
		} else {
			add_Btn.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * 调用系统相册的操作,在onActivityResult中调用
	 * 
	 * @param data
	 *            onActivityResult中的Intent
	 */
	public String getBitmap(Intent data) {
		Uri selectedImage = data.getData();
		if(selectedImage.toString().contains("content://")){
			String[] filePathColumn = { MediaStore.Images.Media.DATA };
			Cursor cursor = getContentResolver().query(selectedImage,
					filePathColumn, null, null, null);
			if(cursor==null)
				return "";
			cursor.moveToFirst();
			int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
			picturePath = cursor.getString(columnIndex);
			cursor.close();
			return picturePath;
		}else{
			String file = selectedImage.toString();
			String files[] = file.split("//");
			picturePath = files[1];
			return picturePath;
		}
	}

	/**
	 * 计算大小，防止加载过大图片 造成内存溢出
	 * 
	 * @param pathName
	 *            路径
	 * @param reqWidth
	 *            需要的宽度
	 * @param reqHeight
	 *            需要的高度
	 * @return Bitmap Bitmap
	 */
	private Bitmap decodeSampledBitmapFromResource(String pathName,
			int reqWidth, int reqHeight) {
		// 第一次解析将inJustDecodeBounds设置为true，来获取图片大小
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(pathName, options);
		options.inSampleSize = calculateInSampleSize(options, reqWidth,
				reqHeight);
		// 使用获取到的inSampleSize值再次解析图片
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeFile(pathName, options);
	}

	/**
	 * 
	 * @param options
	 *            BitmapFactory.Options
	 * @param reqWidth
	 *            需要的宽度
	 * @param reqHeight
	 *            需要的高度
	 * @return int 缩放比例
	 */
	private int calculateInSampleSize(BitmapFactory.Options options,
			int reqWidth, int reqHeight) {
		// 源图片的高度和宽度
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;
		if (height > reqHeight || width > reqWidth) {
			// 计算出实际宽高和目标宽高的比率
			final int heightRatio = Math.round((float) height
					/ (float) reqHeight);
			final int widthRatio = Math.round((float) width / (float) reqWidth);
			// 选择宽和高中最小的比率作为inSampleSize的值，这样可以保证最终图片的宽和高
			// 一定都会大于等于目标的宽和高。
			inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
		}
		return inSampleSize;
	}
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		if (hasFocus && isFirst && select_Llyt != null) {
			getPopupWindow(select_Llyt);
			isFirst = false;
		}
	}
	@Override
	protected void onStop() {
		super.onStop();
	}
	/** 
     * Hides the input method. 
     */  
    protected void hideInputMethod() {  
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);  
        if (imm != null) {
        	View v = getCurrentFocus();
        	if (v != null)
        		imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }  
    }
}
