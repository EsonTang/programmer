package com.prize.music.activities;

import java.io.File;
import java.util.ArrayList;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;

import com.prize.app.beans.ClientInfo;
import com.prize.app.constants.Constants;
import com.prize.app.util.CommonUtils;
import com.prize.app.util.JLog;
import com.prize.app.util.ToastUtils;
import com.prize.custmerxutils.XExtends;
import com.prize.music.helpers.utils.StateBarUtils;
import com.prize.music.ui.fragments.PromptDialogFragment;
import com.prize.music.views.CornerImageView;
import com.prize.music.R;

/**
 **
 * 意见反馈
 * 
 * @author longbaoxiu
 * @version V1.0
 */
public class FeedbackExActivity extends FragmentActivity {
	/** 消息编辑框 */
	private EditText contentET;
	private Button add_Btn;
	private int PICTURE = 100;
	private String picturePath;
	private ArrayList<File> files = new ArrayList<File>();
	private ArrayList<String> picturePaths = new ArrayList<String>();
	private LinearLayout tableLayout;
	private PromptDialogFragment df;
	private ProgressDialog dialog;
	private boolean flag = true;
	private Button feedback_send_btn;
	private boolean isRequesting = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		StateBarUtils.initStateBar(this,
				getResources().getColor(R.color.statusbar_color));
		setContentView(R.layout.activity_feedbackex);
		StateBarUtils.changeStatus(getWindow());
		contentET = (EditText) findViewById(R.id.fedbck_content_et);
		add_Btn = (Button) findViewById(R.id.add_Btn);
		tableLayout = (LinearLayout) findViewById(R.id.tableLayout);
		InputFilter emojiFilter = CommonUtils.getEmojiFilter();
		contentET.setFilters(new InputFilter[] { emojiFilter });
		// mMyAdaptet = new MyAdaptet();

		feedback_send_btn = (Button) findViewById(R.id.feedback_send_btn);

		feedback_send_btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				for (int i = 0; i < picturePaths.size(); i++) {
					files.add(new File(picturePaths.get(i)));
				}
				sendFeedback();
			}
		});

		add_Btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				Intent picture = new Intent(
						Intent.ACTION_PICK,
						android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
				// picture.addCategory(Intent.CATEGORY_OPENABLE);
				picture.setType("image/*");
				startActivityForResult(picture, PICTURE);

			}
		});
		findViewById(R.id.action_back).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {

						onBackPressed();

					}
				});
	}

	private void sendFeedback() {
		String content = contentET.getText().toString();
		if (TextUtils.isEmpty(content.trim())) {
			ToastUtils.showToast(R.string.feedback_msg_empty);
			contentET.setText("");
			return;
		}
		if (ClientInfo.networkType == ClientInfo.NONET) {
			ToastUtils.showToast(R.string.nonet_connect);
			return;
		}
		if (isRequesting) {
			ToastUtils.showToast(R.string.questing);
			return;
		}
		flag = false;
		String url = Constants.GIS_URL + "/feedback/post";
		RequestParams params = new RequestParams(url);
		params.setMultipart(true);
		params.addBodyParameter("content", content);
		params.setConnectTimeout(30 * 1000);
		for (int i = 0; i < files.size(); i++) {
			params.addBodyParameter("file" + (i + 1), files.get(i), null);
		}

		XExtends.http().post(params, new Callback.ProgressCallback<String>() {

			@Override
			public void onSuccess(String result) {
				if (dialog != null && dialog.isShowing()
						&& !FeedbackExActivity.this.isFinishing()) {
					dialog.dismiss();
				}
				ToastUtils.showToast(R.string.send_fedbck_tip);
				finish();

				isRequesting = false;
			}

			@Override
			public void onError(Throwable ex, boolean isOnCallback) {
				JLog.i("long", ex.toString());
				if (dialog != null && dialog.isShowing()
						&& !FeedbackExActivity.this.isFinishing()) {
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
				dialog = new ProgressDialog(FeedbackExActivity.this);
				dialog.setMessage(getString(R.string.committing));
				dialog.setCancelable(true);
				if (!FeedbackExActivity.this.isFinishing()) {
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
			ToastUtils.showToast("注意：请不要选择视频文件");
			return;
		}
		if (resultCode == Activity.RESULT_OK && null != data) {
			lp.weight = 1;
			lp.rightMargin = 15;
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
				// Bitmap bm = BitmapFactory.decodeFile(bitMap, options);
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
		if (selectedImage.toString().contains("content://")) {
			String[] filePathColumn = { MediaStore.Images.Media.DATA };
			Cursor cursor = getContentResolver().query(selectedImage,
					filePathColumn, null, null, null);
			if (cursor == null)
				return "";
			cursor.moveToFirst();
			int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
			picturePath = cursor.getString(columnIndex);
			cursor.close();
			return picturePath;
		} else {
			String file = selectedImage.toString();
			String files[] = file.split("//");
			picturePath = files[1];
			return picturePath;
		}
	}

	@Override
	public void onBackPressed() {
		// //判断隐藏软键盘是否弹出
		if (getWindow().getAttributes().softInputMode == WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE) {
			// 隐藏软键盘
			getWindow().setSoftInputMode(
					WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
			return;
		}
		if (flag
				&& (picturePaths.size() > 0 || contentET.getText().toString()
						.trim().length() > 0)) {
			if (df == null || !df.isAdded()) {
				df = PromptDialogFragment.newInstance(
						getString(R.string.caution),
						getString(R.string.caution_content),
						getString(R.string.alert_button_yes), null,
						mDeletePromptListener);

			}
			df.show(getSupportFragmentManager(), "sureDialog");
		} else {
			super.onBackPressed();
		}
	}

	/**
	 * 编辑后退出提示框
	 */
	private View.OnClickListener mDeletePromptListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			df.dismissAllowingStateLoss();
			FeedbackExActivity.this.finish();
		}
	};

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
}
