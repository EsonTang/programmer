/*****************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：
 *当前版本：
 *作	者：huanglingjun
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
 ********************************************/
package com.prize.cloud.activity;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayLargerImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.prize.app.beans.Person;
import com.prize.cloud.R;
import com.prize.cloud.bean.CloudAccount;
import com.prize.cloud.task.AvatarTask;
import com.prize.cloud.task.ProfileTask;
import com.prize.cloud.task.TaskCallback;
import com.prize.cloud.util.AppManager;
import com.prize.cloud.util.Utils;
import com.prize.cloud.widgets.CircleImageView;
import com.prize.cloud.widgets.NickNameDialog;
import com.prize.cloud.widgets.NickNameDialog.NickNameInfo;
import com.prize.cloud.widgets.ProDialog;
import com.prize.cloud.widgets.SelectImgDialog;
import com.prize.cloud.widgets.SelectImgDialog.SelectHeadImg;
import com.prize.cloud.widgets.SelectSexDialog;
import com.prize.cloud.widgets.SelectSexDialog.SelectSex;

/**
 * 类描述：个人信息
 * 
 * @author huanglingjun
 * @version
 */
public class PersonActivity extends BaseActivity implements OnClickListener {
	private CircleImageView mHead_img;
	private ImageView head_img_back_id;
	private TextView mEnter;
	private TextView mNickName;
	private TextView mSex;
	private TextView mKbId;
	private TextView mPhone;
	private TextView mEmail;
	private TextView mTitle;

	private SelectImgDialog imgDialog;
	private SelectSexDialog sexDialog;
	private NickNameDialog nickNameDialog;

	private String cutImageFilePath;
	private String imageFilePath;
	private Uri mUri;

	private static final int IMAGE_CODE = 0;
	private static final int PHOTO_REQUEST_CUT = 1;
	private static final int PHOTO_REQUEST_CAREMA = 2;

	private static final int CANCEL = 0;
	private static final int SURE = 1;
	private static final int TAKE_PHOTO = 1;
	private static final int PHOTOS = 2;
	private static final int MAN = 1;
	private static final int WOMAN = 2;

	private String userId;
	private String oldNickName;
	private String oldSex;
	private Person person;
	private RelativeLayout mRelayHead, mRelayNickName, mRelaySex, mRelayBind;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		person = Utils.getPersonalInfo(this);
		if (person == null) {
			Toast.makeText(getApplicationContext(), this.getResources().getString(R.string.login_error),
					Toast.LENGTH_SHORT).show();
			startActivity(new Intent(this, MainActivityCloud.class));
			finish();
			return;
		}
		setContentView(R.layout.own_information);
		initView();
		AppManager.getAppManager().addActivity(this);
	}

	private void initView() {
		mHead_img = (CircleImageView) findViewById(R.id.head_img_id);
		mEnter = (TextView) findViewById(R.id.enter_id);
		mNickName = (TextView) findViewById(R.id.nickname_id);
		mSex = (TextView) findViewById(R.id.sex_id);
		mKbId = (TextView) findViewById(R.id.kb_id);
		mPhone = (TextView) findViewById(R.id.safe_phone_id);
		mEmail = (TextView) findViewById(R.id.bind_email_id);
		head_img_back_id = (ImageView) findViewById(R.id.head_img_back_id);
		mRelayHead = (RelativeLayout) findViewById(R.id.relay_head_id);
		mRelayNickName = (RelativeLayout) findViewById(R.id.relay_nickname_id);
		mRelaySex = (RelativeLayout) findViewById(R.id.relay_sex_id);
		mRelayBind = (RelativeLayout) findViewById(R.id.relay_bind_id);

		mHead_img.setOnClickListener(this);
		mNickName.setOnClickListener(this);
		mEnter.setOnClickListener(this);
		mSex.setOnClickListener(this);
		mKbId.setOnClickListener(this);
		mPhone.setOnClickListener(this);
		mEmail.setOnClickListener(this);
		mRelayHead.setOnClickListener(this);
		mRelayNickName.setOnClickListener(this);
		mRelaySex.setOnClickListener(this);
		mRelayBind.setOnClickListener(this);
		mTitle = (TextView) findViewById(R.id.title_id);
		mTitle.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				doHttpPost();
			}
		});

		initData();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		boolean isBind = intent.getBooleanExtra("binded", true);
		if (isBind) {
			Person person = Utils.getPersonalInfo(this);
			if (!TextUtils.isEmpty(person.getEmail())) {
				mEmail.setText(person.getEmail());
			} else {
				mEmail.setText(this.getString(R.string.unbind_email));
			}
		} else {
			mEmail.setText(this.getString(R.string.unbind_email));
		}
	}

	/**
	 * 方法描述：初始化数据
	 * 
	 * @return void
	 * @see 类名/完整类名/完整类名#方法名
	 */
	private void initData() {
		try {
			userId = person.getUserId();
			if (!TextUtils.isEmpty(person.getRealName())) {
				mNickName.setText(person.getRealName());
				oldNickName = person.getRealName();
			} else {
				mNickName.setText(userId);
				oldNickName = userId;
			}
			if ((person.getSex()) == 0) {
				mSex.setText(this.getString(R.string.woman));
				oldSex = this.getString(R.string.woman);
			} else {
				mSex.setText(this.getString(R.string.man));
				oldSex = this.getString(R.string.man);
			}
			if (!TextUtils.isEmpty(userId)) {
				mKbId.setText(userId);
			}
			if (!TextUtils.isEmpty(person.getPhone())) {
				mPhone.setText(person.getPhone());
			}
			if (!TextUtils.isEmpty(person.getEmail())) {
				mEmail.setText(person.getEmail());
			} else {
				mEmail.setText(this.getString(R.string.unbind_email));
			}
			if (!TextUtils.isEmpty(person.getAvatar())) {

				DisplayLargerImageOptions options = new DisplayLargerImageOptions.Builder()
						.cacheInMemory(true).cacheOnDisk(true).build();
				ImageLoader.getInstance().displayImage(person.getAvatar(),
						mHead_img, options);

			} else {
				head_img_back_id.setBackgroundResource(Color.TRANSPARENT);
				if ((person.getSex()) == 0) {
					mHead_img.setImageResource(R.drawable.cloud_woman_small);
				} else if ((person.getSex()) == 1) {
					mHead_img.setImageResource(R.drawable.cloud_man_small);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.relay_head_id || id == R.id.head_img_id
				|| id == R.id.enter_id) {
			setHeadImg();
		} else if (id == R.id.relay_nickname_id || id == R.id.nickname_id) {
			setNickName();
		} else if (id == R.id.relay_sex_id || id == R.id.sex_id) {
			setSex();
		} else if (id == R.id.relay_bind_id || id == R.id.bind_email_id) {
			bindEmail();
		}

	}

	private void bindEmail() {
		String email = mEmail.getText().toString();
		if (TextUtils.isEmpty(email) || !Utils.isEmail(email)) {
			Intent intent = new Intent(this, BindActivity.class);
			this.startActivity(intent);
		} else {
			Intent it = new Intent(this, BindedActivity.class);
			it.putExtra("email", email);
			startActivity(it);
		}
	}

	/**
	 * 方法描述：执行网络请求，上传头像，昵称，性别等信息
	 * 
	 * @return void
	 * @see 类名/完整类名/完整类名#方法名
	 */
	private void doHttpPost() {
		CloudAccount cloudAccount = Utils.curAccount(this);
		if (cloudAccount == null) {
			finish();
			return;
		}

		final ProDialog proDialog = new ProDialog(this,
				ProgressDialog.THEME_HOLO_LIGHT,
				this.getString(R.string.upload));

		TaskCallback<Void> taskCallback = new TaskCallback<Void>() {

			@Override
			public void onTaskSuccess(Void data) {
				if (proDialog != null && proDialog.isShowing()) {
					proDialog.dismiss();
				}
				Toast.makeText(getApplicationContext(),
						PersonActivity.this.getString(R.string.upload_success),
						Toast.LENGTH_LONG).show();
				jumpToPersonCneterActivity();
				// returnIntent();
				finish();
			}

			@Override
			public void onTaskError(int errorCode, String msg) {
				if (proDialog != null && proDialog.isShowing()) {
					proDialog.dismiss();
				}
				// Toast.makeText(getApplicationContext(),PersonActivity.this.getString(R.string.upload_fail),
				// Toast.LENGTH_LONG).show();
				Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG)
						.show();
				jumpToPersonCneterActivity();
				finish();
			}
		};
		String sex = mSex.getText().toString().trim();
		String nickName = mNickName.getText().toString().trim();
		if (!TextUtils.isEmpty(cutImageFilePath) && isNetworkAvailable()) {
			doAvatarTask(cloudAccount, sex, nickName, taskCallback);
			proDialog.show();

		} else if (isNetworkAvailable() && !TextUtils.isEmpty(oldSex)
				&& !TextUtils.isEmpty(oldNickName)) {
			if (!oldSex.equals(sex) || !oldNickName.equals(nickName)) {
				doProfileTask(cloudAccount, sex, nickName, taskCallback);
				proDialog.show();
			} else {
				jumpToPersonCneterActivity();
				finish();
			}

		} else {
			if (!TextUtils.isEmpty(oldSex) && !TextUtils.isEmpty(oldNickName)) {
				if (!oldSex.equals(sex) || !oldNickName.equals(nickName)) {
					Toast.makeText(getApplicationContext(),
							this.getString(R.string.network_unavailable),
							Toast.LENGTH_LONG).show();
				}
			}
			jumpToPersonCneterActivity();
			finish();
		}
	}

	/**
	 * 方法描述：不更新头像只更新昵称和性别的网络请求
	 * 
	 * @return void
	 * @see 类名/完整类名/完整类名#方法名
	 */
	public void doProfileTask(CloudAccount cloudAccount, String sex,
			String nickName, TaskCallback<Void> taskCallback) {
		ProfileTask profileTask = new ProfileTask(this, taskCallback, userId,
				cloudAccount.getPassport());
		if (sex.equals(this.getString(R.string.woman))) {
			profileTask.setGender(0);
		} else if (sex.equals(this.getString(R.string.man))) {
			profileTask.setGender(1);
		}
		profileTask.setRealname(nickName);
		profileTask.doExecute();
	}

	/**
	 * 方法描述：执行更新头像的网络请求
	 * 
	 * @return void
	 * @see 类名/完整类名/完整类名#方法名
	 */
	public void doAvatarTask(CloudAccount cloudAccount, String sex,
			String nickName, TaskCallback<Void> taskCallback) {
		AvatarTask task = new AvatarTask(this, taskCallback, userId,
				cloudAccount.getPassport());
		task.setIcon(cutImageFilePath);
		if (sex.equals(this.getString(R.string.woman))) {
			task.setGender(0);
		} else if (sex.equals(this.getString(R.string.man))) {
			task.setGender(1);
		}
		task.setRealname(nickName);
		task.doExecute();
	}

	/**
	 * 方法描述：设置昵称
	 * 
	 * @return void
	 * @see 类名/完整类名/完整类名#方法名
	 */
	private void setNickName() {
		String oldNickName = mNickName.getText().toString().trim();
		nickNameDialog = new NickNameDialog(this, R.style.add_dialog,
				oldNickName);
		nickNameDialog.setNickNameInfo(new NickNameInfo() {

			@Override
			public void onClick(int which, String newNickName) {
				// TODO Auto-generated method stub
				switch (which) {
				case CANCEL:
					dismissNickNameDialog();
					break;

				case SURE:
					mNickName.setText(newNickName);
					dismissNickNameDialog();
					break;

				default:
					break;

				}
			}
		});
		nickNameDialog.show();
	}

	/**
	 * 方法描述：设置性别
	 * 
	 * @return void
	 * @see 类名/完整类名/完整类名#方法名
	 */
	private void setSex() {
		sexDialog = new SelectSexDialog(this, R.style.add_dialog);
		Window window = sexDialog.getWindow();
		window.setGravity(Gravity.BOTTOM);
		window.setWindowAnimations(R.style.mypopwindow_anim_style);

		sexDialog.setShSex(new SelectSex() {

			@Override
			public void onClick(int which) {
				switch (which) {
				case CANCEL:
					dismissSexDialog();
					break;

				case MAN:
					mSex.setText(PersonActivity.this.getString(R.string.man));
					dismissSexDialog();
					break;

				case WOMAN:
					mSex.setText(PersonActivity.this.getString(R.string.woman));
					dismissSexDialog();
					break;

				default:
					break;

				}
			}
		});

		sexDialog.show();
	}

	/**
	 * 方法描述：设置头像
	 * 
	 * @return void
	 * @see 类名/完整类名/完整类名#方法名
	 */
	private void setHeadImg() {
		imgDialog = new SelectImgDialog(this, R.style.add_dialog);
		Window window = imgDialog.getWindow();
		window.setGravity(Gravity.BOTTOM);
		window.setWindowAnimations(R.style.mypopwindow_anim_style);

		imgDialog.setShImg(new SelectHeadImg() {

			@Override
			public void onClick(int which) {
				switch (which) {
				case CANCEL:
					if (imgDialog.isShowing()) {
						imgDialog.dismiss();
					}
					break;

				case TAKE_PHOTO:
					camera();
					break;

				case PHOTOS:
					Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
					intent.addCategory(Intent.CATEGORY_OPENABLE);
					intent.setType("image/*");

					startActivityForResult(intent, IMAGE_CODE);
					break;

				default:
					break;
				}
			}
		});

		imgDialog.show();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (imgDialog != null && imgDialog.isShowing()) {
			imgDialog.dismiss();
		}
		try {
			if (resultCode != RESULT_OK) {
				// Toast.makeText(getApplicationContext(),
				// this.getString(R.string.edit_head_failure),
				// Toast.LENGTH_SHORT).show();
				return;
			}
			if (requestCode == IMAGE_CODE && data.getData() != null) {
				Uri originalUri = data.getData(); // 获得图片的uri
				crop(originalUri);
			}
			if (requestCode == PHOTO_REQUEST_CUT) {
				if (data == null) {
					Toast.makeText(getApplicationContext(),
							this.getString(R.string.edit_head_failure),
							Toast.LENGTH_SHORT).show();
					return;
				}
				// Bitmap bm = data.getParcelableExtra("data");
				/*
				 * Bitmap bm = BitmapFactory.decodeFile(ImagePathUtils.getPath(
				 * this, data.getData()));
				 */
				Bitmap bm = BitmapFactory.decodeFile(imageFilePath);
				head_img_back_id
						.setBackgroundResource(R.drawable.cloud_person_headimg_backgroud);
				mHead_img.setImageBitmap(bm);
				cutImageFilePath = imageFilePath;
				// imageFilePath = ImagePathUtils.getPath(this, data.getData());
			}

			if (requestCode == PHOTO_REQUEST_CAREMA) {
				crop(mUri);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	/**
	 * 方法描述：裁剪图片
	 * 
	 * @param uri
	 * @return void
	 * @see 类名/完整类名/完整类名#方法名
	 */
	private void crop(Uri uri) {
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(uri, "image/*");
		intent.putExtra("crop", "true");
		// 裁剪框的比例，1：1
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		// 裁剪后输出图片的尺寸大小
		intent.putExtra("outputX", 150);
		intent.putExtra("outputY", 150);
		intent.putExtra("scale", true);
		intent.putExtra("outputFormat", "JPG");// 图片格式
		File file = getTempFile();
		if (file == null) {
			Toast.makeText(this, this.getString(R.string.check_sdk),
					Toast.LENGTH_SHORT).show();
			return;
		}
		intent.putExtra("output", Uri.fromFile(file)); // 裁剪后图片存储路径
		intent.putExtra("return-data", false);

		startActivityForResult(intent, PHOTO_REQUEST_CUT);
	}

	/**
	 * 方法描述：启动照相机拍照
	 * 
	 * @return void
	 * @see 类名/完整类名/完整类名#方法名
	 */
	public void camera() {
		Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
		File file = getTempFile();
		if (file == null) {
			Toast.makeText(this, this.getString(R.string.check_sdk),
					Toast.LENGTH_SHORT).show();
			return;
		}
		mUri = Uri.fromFile(file);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, mUri);
		startActivityForResult(intent, PHOTO_REQUEST_CAREMA);
	}

	/**
	 * 方法描述：得到裁剪后图片文件
	 * 
	 * @return File
	 * @see 类名/完整类名/完整类名#方法名
	 */
	public File getTempFile() {
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			Date date = new Date();
			SimpleDateFormat dateFormat = new SimpleDateFormat(
					"yyyyMMdd_HHmmss");
			String imgName = "IMG_" + dateFormat.format(date) + ".jpg";
			// imageFilePath = "/sdcard/prizecloud/pictures/"+ imgName;
			// imageFilePath =
			// Environment.getExternalStorageDirectory().getAbsolutePath()+"/prizecloud/pictures/"+
			// imgName;
			imageFilePath = Environment.getExternalStorageDirectory()
					.getAbsolutePath() + "/prizecloud/pictures/" + imgName;
			// Log.e("huang", "path="+path);
			File tempFile = new File(imageFilePath);
			File parentFile = tempFile.getParentFile();
			if (!parentFile.exists()) {
				parentFile.mkdirs();
			}
			return tempFile;
		}
		return null;
	}

	@Override
	public void onBackPressed() {
		doHttpPost();
	}

	public void dismissNickNameDialog() {
		if (nickNameDialog != null && nickNameDialog.isShowing()) {
			nickNameDialog.dismiss();
		}
	}

	public void dismissSexDialog() {
		if (sexDialog != null && sexDialog.isShowing()) {
			sexDialog.dismiss();
		}
	}

	/**
	 * 方法描述：判断当前网络是否可用
	 * 
	 * @return boolean
	 * @see 类名/完整类名/完整类名#方法名
	 */
	private boolean isNetworkAvailable() {
		ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		if (manager.getActiveNetworkInfo() != null) {
			return manager.getActiveNetworkInfo().isAvailable();
		}
		return false;
	}

	/**
	 * 方法描述：给logonActivity回传值
	 * 
	 * @return void
	 * @see 类名/完整类名/完整类名#方法名
	 */
	public void returnIntent() {
		Intent intent = new Intent();
		setResult(1001, intent);
	}

	// public void onBackClk(View v) {
	// onBackPressed();
	// }

	public void jumpToPersonCneterActivity() {
		/*
		 * Intent intent = new Intent(this, PersonalCenterActivity.class);
		 * intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		 * startActivity(intent); finish();
		 */
		AppManager.getAppManager().finishAllActivity();
	}
}
