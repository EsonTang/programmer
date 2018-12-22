
/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：便签编辑Activity
 *当前版本：V1.0
 *作	者：朱道鹏
 *完成日期：2015-04-17
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
package com.android.notepad.note;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.android.notepad.BaseActivity;
import com.android.notepad.NotePadActivity;
import com.android.notepad.note.database.NotePadDataBaseDao;
import com.android.notepad.note.database.NotePadDataBaseDaoImpl;
import com.android.notepad.note.model.NoteEvent;
import com.android.notepad.note.util.DateFormatUtil;
import com.android.notepad.note.util.DialogUtils;
import com.android.notepad.note.util.FileUtil;
import com.android.notepad.note.util.MediaFile;
import com.android.notepad.note.view.NotePadEditText;
import com.android.notepad.R;

import android.os.PowerManager;
import com.mediatek.common.prizeoption.PrizeOption;
import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.Time;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.content.ComponentName;

@SuppressWarnings("deprecation")
public class NotePadEditActivity extends BaseActivity {
	private static final String TAG = "NotePadEditActivity";

	/**
	 * 背景颜色res
	 */
	private int BgStatus = 5;
	private int mBGColor;

	final int BgRed1 = 1;
	final int BgYellow2 = 2;
	final int BgGreen3 = 3;
	final int BgBlue4 = 4;
	final int BgWhite5 = 5;

	/**
	 * Intent请求码
	 */
	private static final int CAMERA_REQUEST = 1;
	private static final int PICTURE_REQUEST = 2;

	/**
	 * 未更换背景前的背景res
	 * */
	private int beforeChangeBgStatus = 0;

	/**
	 * 背景选择区域控件
	 */
	private RelativeLayout mLayout;
	private RelativeLayout mLayoutTitle;
	private ImageView blue;
	private ImageView green;
	private ImageView white;
	private ImageView yellow;
	private ImageView red;

	/**
	 * 便签编辑输入框
	 */
	private NotePadEditText mEditText;
	/**
	 * 建立时间显示控件
	 */
	private TextView mTextView;
	/**
	 * 文本字体大小
	 */
	private int mFontSize;

	/**
	 * 数据库_id行号
	 */
	private int rowId;

	/**
	 * 长整型毫秒值
	 */
	private long mNotepadCreateTime;

	/**
	 * Note事件内容
	 */
	private String mNotepadContent;

	final String mPerfName = "com.android.notepad.note.sendtodesk.conf";

	/**
	 * 编辑内容是否发生改变
	 */
	private boolean isContentHasChange = false;

	/**
	 * 是否为浮选
	 */
	private boolean isFromFloatation = false;

	/**
	 * NotePad数据库对象
	 */
	private NotePadDataBaseDao mNotePadDataBaseDao;

	/**
	 * 按钮区域控件
	 */
	private LinearLayout mCameraImageView;
	private LinearLayout mPictureImageView;
	private LinearLayout mShareImageView;
	private LinearLayout mTextSizeImageView;
	
	/*--prize-add--chenjiahua--2017-11-28-start--*/
	private ImageView mCameraImageViewIcon;
	private TextView mCameraImageViewText;
	private ImageView mPictureImageViewIcon;
	private TextView mPictureImageViewText;
	/*--prize-add--chenjiahua--2017-11-28-end--*/
	

	private Timer timer = new Timer();

	/**
	 * 当前图片地址
	 */
	private String mCurrImgPath;

	private InputMethodManager inputManager;

	private HashMap<String, Integer> mMap;

	private boolean mHasContent;

	private static String mUuid;

	private static final String Twenty_Four_Hour_System = "24";
	private static final String Twelve_Hour_System = "12";
	private boolean mIsTwentyFourHourSystem = false;

	private static final String CURRENT_IMAGE_PATH_KEY = "CURRENT_IMAGE_PATH_KEY";
	/* prize-modify-by-lijimeng-for bugid 35588-20170704-start*/
	private ClipboardManager mClipboardManager;
	/* prize-modify-by-lijimeng-for bugid 35588-20170704-end*/


	/**
	 * 便签内容变化监听
	 */
	private TextWatcher textWatcher = new TextWatcher() {
		private int beforeChangedLength;
		private int afterChangedLength;
		private String changeText;
		public void afterTextChanged(Editable s) {
			if(!s.toString().equals(mNotepadContent) && !s.toString().trim().isEmpty()){
				isContentHasChange = true;
			}else if(s.toString().trim().isEmpty() && mNotepadContent != null && !"".equals(mNotepadContent)){
					isContentHasChange = true;				
			}else{
					isContentHasChange = false;
			}
			
			if(isAImagePath(changeText)){
				boolean isInput = afterChangedLength>beforeChangedLength?true:false;
				operationImagePath(isInput, changeText);
			}
		}
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			if(null != s){
				beforeChangedLength = s.length();
				String content = s.toString();
				if(count > 0 && after == 0){
					changeText = content.substring(start, start+count);
					mHasContent = false;
				}
			}
		}
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			if(null != s){
				afterChangedLength = s.length();
				String content = s.toString();
				if(count > 0 && before == 0){
					changeText = content.substring(start, start+count);
					mHasContent = false;
				}
			}
		}
	};

	/**
	 * 便签内容字体大小设置Handler
	 */
	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler(){
		public void handleMessage(Message msg) {
			mFontSize = msg.what;
			mEditText.setTextSize(mFontSize);
			isContentHasChange = true;
		};
	};

	/**
	 * 按钮点击监听
	 */
	private OnClickListener editUiOnClickListener = new  OnClickListener(){
		@SuppressLint("SimpleDateFormat") 
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.camera_image_view:				
				Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE); 
				Date date = new Date();
				SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmss");
				String imgName = "IMG_" + format.format(date) + ".jpg";
				mCurrImgPath = FileUtil.CONFIG_FILE + File.separator + mUuid + File.separator + imgName;
				File file = new File(mCurrImgPath);
				File parentFile = file.getParentFile();
				if(!parentFile.exists()){
					parentFile.mkdirs();
				}
				Uri uri = Uri.fromFile(file);
				cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
				cameraIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
				startActivityForResult(cameraIntent, CAMERA_REQUEST);
				break;
			case R.id.picture_image_view:				
				Intent pictureIntent = new Intent(Intent.ACTION_PICK, null);
				ComponentName cn2 = new ComponentName("com.android.gallery3d",
                        "com.android.gallery3d.app.DialogPicker");
                pictureIntent.setComponent(cn2);				
				pictureIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
				pictureIntent.putExtra("return-data", true);
				startActivityForResult(pictureIntent, PICTURE_REQUEST); 
				break;
			case R.id.share_image_view:
				if(mEditText.getText().toString().equals("") || mEditText.getText().toString() == null){
					Toast.makeText(NotePadEditActivity.this, R.string.share_error_hint, Toast.LENGTH_SHORT).show();
				}else{					
					shareNotePad();
				}
				break;
				//			case R.id.right_click_area:
				//				if (isContentHasChange) {
				//					saveDataToDB();
				//				}
				//				finish();
				//				break;
			case R.id.text_size_image_view:
				showTextFontDialog();
				break;
			}
		}	
	};
	private boolean mIsBackKeyDowned;

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		/* prize-modify-by-lijimeng-for bugid 35588-20170704-start*/
		mClipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
		/* prize-modify-by-ganxiayong-for bugid 48353-20170704-start*/
		ClipData cd = mClipboardManager.getPrimaryClip(); 
		if(cd != null && cd.getItemCount() > 0) {
			String sequence = cd.getItemAt(0).getText().toString();
			if(sequence.contains("<Image>")){
				mClipboardManager.setText("");
			}
		}
		/* prize-modify-by-ganxiayong-for bugid 48353-20170704-end*/
		/* prize-modify-by-lijimeng-for bugid 35588-20170704-end*/
		Log.d("NotePadEditActivity", "onCreate");
		setSubContentView(R.layout.notepads_edit_ui);
		hideWeekAndTimeView();
		hideMidView();
		hideRightClickArea();

		displayMidView();
		setMidViewTitle(getResources().getString(R.string.page_note_list)); 		   

		int lastFontSize = 0;
		if (bundle != null) {  
			mCurrImgPath = bundle.getString(CURRENT_IMAGE_PATH_KEY);
			lastFontSize = bundle.getInt("textsize");	 		
		}

		Intent intent = getIntent();
		bundle = intent.getExtras();
		if(bundle != null){
		rowId = bundle.getInt("row_id");
		mNotepadCreateTime = bundle.getLong("notepads_create_time");
		mNotepadContent = (String) bundle.getString("notepads_content");
		mBGColor = bundle.getInt("bg_color");
		mFontSize = bundle.getInt("font_size");
		mUuid = (String) bundle.getString("uuid");
		if(null == mUuid){
			UUID uuid = UUID.randomUUID();
			mUuid = uuid.toString();
		}		
		isFromFloatation = bundle.getBoolean("isFromFloatation");
		}

		if (mFontSize == 0) {
			mFontSize = 14;
		}
		
		if(lastFontSize != 0){
			mFontSize = lastFontSize;
		}

		initUI();
	}

	private void initUI() {
		mLayout = (RelativeLayout) findViewById(R.id.id_notepads_bg);
		mLayoutTitle = (RelativeLayout) findViewById(R.id.id_select_color_bar);
		mEditText = (NotePadEditText) findViewById(R.id.id_edit_notepads_contents);
		mEditText.setSaveEnabled(true);
		//		Typeface type = Typeface.create("sans-serif-light", Typeface.NORMAL);
		//		mEditText.setTypeface(type);
		mTextView = (TextView) findViewById(R.id.id_create_notepads_time);

		mCameraImageView = (LinearLayout) findViewById(R.id.camera_image_view);
		mPictureImageView = (LinearLayout) findViewById(R.id.picture_image_view);
		mShareImageView = (LinearLayout) findViewById(R.id.share_image_view);
		mTextSizeImageView = (LinearLayout) findViewById(R.id.text_size_image_view);
		
		/*--prize-add--chenjiahua--2017-11-28-start--*/
		mCameraImageViewIcon = (ImageView) findViewById(R.id.camera_image_view_icon);
		mCameraImageViewText = (TextView) findViewById(R.id.camera_image_view_text);
		mPictureImageViewIcon = (ImageView) findViewById(R.id.picture_image_view_icon);
		mPictureImageViewText = (TextView) findViewById(R.id.picture_image_view_text);
		/*--prize-add--chenjiahua--2017-11-28-end--*/

		mCameraImageView.setOnClickListener(editUiOnClickListener);
		mPictureImageView.setOnClickListener(editUiOnClickListener);
		mShareImageView.setOnClickListener(editUiOnClickListener);
		mTextSizeImageView.setOnClickListener(editUiOnClickListener);
		
		if(NotePadEditActivity.this.isInMultiWindowMode()){
			setPhotoAndCameraClickable(false);
		}else{
			setPhotoAndCameraClickable(true);
		}

		refreshTimeSystem();
		
		if( null!= mNotepadContent){
			Log.d("NotePadEditText", "initUI() mNotepadContent"+mNotepadContent);			
			mEditText.setText("");
			Log.d("NotePadEditText", "Kehwa mEditText.getText().toString(): " + mEditText.getText().toString());		
			mEditText.setSpanContent(mNotepadContent,NotePadEditText.APPEND_STYLE);
			mEditText.setSelection(mEditText.getText().toString().length());
		}else{
			mEditText.setText("");
			mEditText.setText(mNotepadContent);
		}
		//若为新建Note，则进入时自动弹出键盘
		if(mNotepadContent == null || mNotepadContent.length()<=0){
			timer.schedule(new TimerTask(){
				public void run(){
					InputMethodManager inputManager =
							(InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
					inputManager.showSoftInput(mEditText, 0);
				}
			},400);
		}

		mEditText.setTextSize(mFontSize);
		mEditText.addTextChangedListener(textWatcher);
		if(mBGColor!=0){
			BgStatus = mBGColor;
		}else{
			BgStatus = 0;
		}
		setBgColor(BgStatus);

		blue = (ImageView) findViewById(R.id.id_color_blue4);
		green = (ImageView) findViewById(R.id.id_color_green3);
		white = (ImageView) findViewById(R.id.id_color_white5);
		yellow = (ImageView) findViewById(R.id.id_color_yellow2);
		red = (ImageView) findViewById(R.id.id_color_red1);

		setBgButtonListener();
	}
	
	private void setPhotoAndCameraClickable(boolean clickable){
		if(clickable){
			mCameraImageView.setClickable(true);
			mPictureImageView.setClickable(true);
			mCameraImageViewIcon.setBackground(getDrawable(R.drawable.camera_button_selector));
			mPictureImageViewIcon.setBackground(getDrawable(R.drawable.picture_button_selector));
			mCameraImageViewText.setTextColor(getResources().getColorStateList(R.drawable.edit_text_color));
			mPictureImageViewText.setTextColor(getResources().getColorStateList(R.drawable.edit_text_color));
		}else{
			mCameraImageView.setClickable(false);
			mPictureImageView.setClickable(false);
			mCameraImageViewIcon.setBackground(getDrawable(R.drawable.camera_unclickable));
			mPictureImageViewIcon.setBackground(getDrawable(R.drawable.picture_unclickable));
			mCameraImageViewText.setTextColor(getResources().getColor(R.color.note_edit_text_color_unable));
			mPictureImageViewText.setTextColor(getResources().getColor(R.color.note_edit_text_color_unable));
		}
	}
	
	@Override
    public void onMultiWindowModeChanged(boolean isInMultiWindowMode) {
        super.onMultiWindowModeChanged(isInMultiWindowMode);
    }

	@Override
	protected void onResume() {
		refreshTimeSystem();
		if(NotePadEditActivity.this.isInMultiWindowMode()){
			setPhotoAndCameraClickable(false);
		}else{
			setPhotoAndCameraClickable(true);
		}
		super.onResume();
	}
		
	@Override
	protected void onDestroy() {
		mEditText.cancleImageCreatAsyncTask();
		super.onDestroy();
	}

	private void refreshTimeSystem(){
		Time time = new Time();
		time.set(mNotepadCreateTime);

		ContentResolver resolver = this.getContentResolver();
		String strTimeFormat = android.provider.Settings.System.getString(resolver,android.provider.Settings.System.TIME_12_24);

		if(null != strTimeFormat && strTimeFormat.equals(Twenty_Four_Hour_System)){
			mIsTwentyFourHourSystem = true;
		}else{
			mIsTwentyFourHourSystem = false;
		}

		String formatDate = DateFormatUtil.getDateInfoString(this, mNotepadCreateTime);
		int hour = time.hour;
		int minute = time.minute;
		String amOrPmAndTimeString = DateFormatUtil.getAmOrPmAndTimeString(this, hour, minute,mIsTwentyFourHourSystem);
		int weekNum = time.weekDay;
		String weekDayString = DateFormatUtil.getWeekDayString(this, weekNum);

		mTextView.setText(formatDate+"   "+amOrPmAndTimeString+"   "+weekDayString);
	}

	@Override  
	protected void onSaveInstanceState(Bundle outState) {  
		super.onSaveInstanceState(outState);  
		outState.putString(CURRENT_IMAGE_PATH_KEY, mCurrImgPath); 
		outState.putInt("textsize",mFontSize);		
	} 

	/**
	 * 方法描述：设备便签背景
	 * @param int 
	 * @return void
	 * @see NotePadEditActivity#setBgColor
	 */
	private void setBgColor(int bg_status) {
		switch (bg_status) {
		case 1:
			mLayout.setBackgroundColor(getResources().getColor(R.color.color_red));
			mLayoutTitle.setBackgroundResource(R.color.white);
			break;
		case 2:
			mLayout.setBackgroundColor(getResources().getColor(R.color.color_yellow));
			mLayoutTitle.setBackgroundResource(R.color.white);
			break;
		case 3:
			mLayout.setBackgroundColor(getResources().getColor(R.color.color_green));
			mLayoutTitle.setBackgroundResource(R.color.white);
			break;
		case 4:
			mLayout.setBackgroundColor(getResources().getColor(R.color.color_blue));
			mLayoutTitle.setBackgroundResource(R.color.white);
			break;
		case 5:
			mLayout.setBackgroundColor(getResources().getColor(R.color.color_while));
			mLayoutTitle.setBackgroundResource(R.color.white);
			break;
		default:
			mLayout.setBackgroundColor(getResources().getColor(R.color.transparent));
			mLayoutTitle.setBackgroundResource(R.color.white);
			break;
		}
	}


	/**
	 * 方法描述：设置背景按钮监听
	 * @param void
	 * @return void
	 * @see NotePadEditActivity#setBgButtonListener
	 */
	private void setBgButtonListener() {
		blue.setOnClickListener(this);
		green.setOnClickListener(this);
		white.setOnClickListener(this);
		yellow.setOnClickListener(this);
		red.setOnClickListener(this);
	}

	public void onClick(View v) {
		beforeChangeBgStatus = BgStatus;
		if (mLayout == null) {
			mLayout = (RelativeLayout) findViewById(R.id.id_notepads_bg);
		}
		if (mLayoutTitle == null) {
			mLayoutTitle = (RelativeLayout) findViewById(R.id.id_select_color_bar);
		}

		switch (v.getId()) {
		case R.id.id_color_blue4: {
			mLayout.setBackgroundColor(getResources().getColor(R.color.color_blue));
			mLayoutTitle.setBackgroundResource(R.color.white);
			BgStatus = BgBlue4;
			break;
		}
		case R.id.id_color_green3: {
			mLayout.setBackgroundColor(getResources().getColor(R.color.color_green));
			mLayoutTitle.setBackgroundResource(R.color.white);
			BgStatus = BgGreen3;
			break;
		}
		case R.id.id_color_white5: {
			mLayout.setBackgroundColor(getResources().getColor(R.color.color_while));
			mLayoutTitle.setBackgroundResource(R.color.white);
			BgStatus = BgWhite5;
			break;
		}
		case R.id.id_color_yellow2: {
			mLayout.setBackgroundColor(getResources().getColor(R.color.color_yellow));
			mLayoutTitle.setBackgroundResource(R.color.white);
			BgStatus = BgYellow2;
			break;
		}
		case R.id.id_color_red1: {
			mLayout.setBackgroundColor(getResources().getColor(R.color.color_red));
			mLayoutTitle.setBackgroundResource(R.color.white);
			BgStatus = BgRed1;
			break;
		}
		case R.id.left_click_area:
			Log.e("tag","left_click_area,isContentHasChange = " + isContentHasChange);
			setLeftButtonPressed(true);
			if (isContentHasChange) {
				NoteListPage.setNoNeedLoadData(false);
				saveDataToDB();

				if (isFromFloatation) {
					isFromFloatation = false;
					Intent intent = new Intent();
					intent.setClass(this, NotePadActivity.class);
					startActivity(intent);
					finish();
				}
			}

			if(!isContentHasChange){
				setResult(3, getIntent());
			}

			finish();
			break;
		}

		if (beforeChangeBgStatus != BgStatus) {
			isContentHasChange = true;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Log.e(TAG,"onKeyDown");
			mIsBackKeyDowned = true;
		}
		return super.onKeyDown(keyCode,event);
	}

	/**
	 * 方法描述：回退按钮监听
	 * @param int KeyEvent
	 * @return boolean
	 * @see NotePadEditActivity#onKeyDown
	 */
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && mIsBackKeyDowned) {
			Log.d(TAG,"onKeyUp");
			mIsBackKeyDowned = false;

			//Contacts entrance  start
			if(!isContentHasChange){
				setResult(3, getIntent());
			}
			//Contacts entrance  end

			//文本框中内容变化时，才需保存到数据库或更新数据库
			Log.d(TAG, "isContentHasChange = "+isContentHasChange);
			if (isContentHasChange) {
				NoteListPage.setNoNeedLoadData(false);
				saveDataToDB();

				Log.d(TAG,"isFromFloatation = " + isFromFloatation);
				if (isFromFloatation) {
					isFromFloatation = false;
					Intent intent = new Intent();
					intent.setClass(this, NotePadActivity.class);
					startActivity(intent);
					finish();
				}
				isContentHasChange  = false;
			}
		}



		return super.onKeyUp(keyCode, event);
	}

	/**
	 * 方法描述：便签事件保存到数据库
	 * @param void
	 * @return void
	 * @see NotePadEditActivity#saveDataToDB
	 */
	private void saveDataToDB() {
		new SaveDbThread(isContentHasChange).start();
	}

	public void removeCutFileToClipBoardFile(ClipboardManager clip){
		final List<File> fileList = FileUtil.getDirFiles(mUuid);
		final ClipData clipData= clip.getPrimaryClip(); 
		final int mClipBoardLastStatus = mEditText.getClipBoardLastOpeartion();
		if(mClipBoardLastStatus == android.R.id.cut){
			String sequenceContent = null;
			String imagePath = null;
			String newPath = null;
			String fileStartHeader = FileUtil.CONFIG_FILE + File.separator + mUuid;
			String newHeader = FileUtil.CLIP_BOARD_FILE;
			File clipBoardFile = new File(newHeader);
			if(clipBoardFile.exists()){
				File[] fileLists = clipBoardFile.listFiles();
				for(File file:fileLists){
					file.delete();
				}
			}else{
				clipBoardFile.mkdirs();
			}
			sequenceContent = clipData.getItemAt(0).getText().toString(); 
			for(File file:fileList){
				imagePath = file.getPath();
				if(!mMap.containsKey(imagePath)){
					if(null != sequenceContent && sequenceContent.contains(imagePath)){
						newPath = imagePath.replace(fileStartHeader, newHeader);
						FileUtil.copyFile(imagePath, newPath);
						file.delete();
					}
				}
			}
		}else{
			for(File file:fileList){
				if(!mMap.containsKey(file.getPath())){
					file.delete();
				}
			}
		}
	}


	/**
	 * 方法描述：便签事件分享
	 * @param void
	 * @return void
	 * @see NotePadEditActivity#shareNotePad
	 */
	private void shareNotePad() {
		String shareContent = mEditText.getText().toString();
		List<String> pashList = getImagePashList(shareContent);
		Intent intent = null;
		if(pashList.isEmpty()){
			intent = new Intent(Intent.ACTION_SEND);
			intent.setType("text/plain");
			intent.putExtra(Intent.EXTRA_TEXT, shareContent);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		}else{
			intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
			intent.setType("image/*");
			ArrayList<Uri> picturesUriArrayList = new ArrayList<Uri>();
			Uri pictureUri = null;
			File pictureFile = null;
			for(int i=0; i<pashList.size(); i++){  
				try{  
					pictureFile = new File(pashList.get(i));
					pictureUri=Uri.fromFile(pictureFile);
					picturesUriArrayList.add(pictureUri);
				} catch (Exception e) {  
					e.printStackTrace();  
				}  
			}  
			intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, picturesUriArrayList); 
		}
		startActivity(Intent.createChooser(intent, null));
	}


	/**
	 * 方法描述：弹出字体选择对话框
	 * @param void
	 * @return void
	 * @see NotePadEditActivity#showTextFontDialog
	 */
	private void showTextFontDialog() {
		final String[] items = new String[4];
		items[0] = getResources().getString(R.string.font_small);
		items[1] = getResources().getString(R.string.font_medium);
		items[2] = getResources().getString(R.string.font_large);
		items[3] = getResources().getString(R.string.font_super);

		DialogUtils.getFrontSettingDialog(this, R.drawable.ic_launcher, 
				getResources().getString(R.string.dialog_title),items,mFontSize, handler);    
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(resultCode != 0){
			switch (requestCode) {
			case CAMERA_REQUEST:
				mEditText.setSpanContent(" " + "<Image>" + mCurrImgPath + " ", NotePadEditText.INSERT_STYLE);
				break;
			case PICTURE_REQUEST:
				//获取图片路径
				Uri uri = data.getData(); 
				String[] proj = {MediaStore.Images.Media.DATA};
				//好像是android多媒体数据库的封装接口，具体的看Android文档
				Cursor cursor = getContentResolver().query(uri, proj, null, null, null); 
				//按我个人理解 这个是获得用户选择的图片的索引值
				if(null != cursor){
					int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
					//将光标移至开头 ，这个很重要，不小心很容易引起越界
					cursor.moveToFirst();
					//最后根据索引值获取图片路径
					mCurrImgPath = cursor.getString(column_index);
				}else{
					mCurrImgPath = uri.getPath();
				}
				boolean isImageFile = MediaFile.isImageFileType(mCurrImgPath);
				if(isImageFile){
					Log.d("NotePadEditText","onActivityResult() mCurrImgPath = "+mCurrImgPath);
					mEditText.setSpanContent(" " + "<Image>" + mCurrImgPath + " ", NotePadEditText.INSERT_STYLE);
				}else{
					Toast.makeText(this, R.string.not_support_no_picture_files, Toast.LENGTH_SHORT).show();
				}
				break;
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	public static String getUuid(){
		return mUuid;
	}

	private boolean isAImagePath(String path){
		if(null != path){
			boolean flag = false;
			Pattern pattern = Pattern.compile(MediaFile.IMAGE_FORMAT_PATTERN);
			Matcher m = pattern.matcher(path);
			while(m.find()){
				flag = true;
			}
			return flag;
		}else{
			return false;
		}
	}

	private void operationImagePath(boolean flag, String path){
		if(flag){
			getImageHashMap(path);
		}else{
			if(mMap.containsKey(path)){
				int value = mMap.get(path);
				if(value > 1){
					mMap.put(path, --value);
				}else{
					mMap.remove(path);
				}
			}
		}
	}

	private void getImageHashMap(String content){
		Pattern pattern = Pattern.compile(MediaFile.IMAGE_FORMAT_PATTERN);
		Matcher m = pattern.matcher(content);
		String imagePath = null;
		if(null == mMap){
			mMap = new HashMap<String, Integer>();
		}
		mMap.clear();
		while(m.find()){
			imagePath = m.group();
			imagePath = imagePath.substring(MediaFile.IMAGE_FORMAT_HEADER.length());
			if(mMap.containsKey(imagePath)){
				int value = mMap.get(imagePath);
				mMap.put(imagePath, ++value);
			}else{
				mMap.put(imagePath, 1);
			}
		}
	}

	private List<String> getImagePashList(String content){
		Pattern pattern = Pattern.compile(MediaFile.IMAGE_FORMAT_PATTERN);
		Matcher m = pattern.matcher(content);
		String imagePath = null;
		List<String> pashList = new ArrayList<String>();
		while(m.find()){
			imagePath = m.group();
			imagePath = imagePath.substring(MediaFile.IMAGE_FORMAT_HEADER.length());
			pashList.add(imagePath);
		}
		return pashList;
	}

	private class SaveDbThread extends Thread {
		private boolean mIsContentChanged;
		public SaveDbThread(boolean isContentChanged) {
			mIsContentChanged = isContentChanged;
		}

		@Override
		public void run() {
			/* prize-modify-by-lijimeng-for bugid 35588-20170704-start*/
			//final ClipboardManager clip = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
			/* prize-modify-by-lijimeng-for bugid 35588-20170704-end*/
			synchronized (FileUtil.CONFIG_FILE){
				String content = mEditText.getText().toString();
				Log.e("tag","saveDataToDB content"+content);
				int id_bg_color = BgStatus;

				List<String> pathList = null;
				getImageHashMap(content);          //把所有匹配到的图片路径存入到map集合中
				if(null != mMap){
					Iterator iter = mMap.keySet().iterator();
					pathList = new ArrayList<String>();
					for(;iter.hasNext();){
						String key = (String)iter.next();
						pathList.add(key);                 //把map集合的key存进集合pathList中
					}
				}

				String newFileName = null;
				if(null != pathList){
					for(String path:pathList){
						if(!path.startsWith(FileUtil.CONFIG_FILE)){
							newFileName = FileUtil.copyNewNoteImageFile(path, mUuid);
							if(null != newFileName){
								content = content.replace(path, newFileName);				//替换
							}
						}else if(path.startsWith(FileUtil.CLIP_BOARD_FILE)){
							newFileName = FileUtil.copyNewNoteImageFile(path, mUuid);
							if(null != newFileName){
								content = content.replace(path, newFileName);
							}
						}
					}
//						Log.e("tag","newFileName = " + newFileName);
//						Log.e("tag","content = " + content);
				}
				getImageHashMap(content);
				/* prize-modify-by-lijimeng-for bugid 35588-20170704-start*/
				if(mClipboardManager != null){
					removeCutFileToClipBoardFile(mClipboardManager);      //里面改了
				}
				//removeCutFileToClipBoardFile(clip);      //里面改了
				/* prize-modify-by-lijimeng-for bugid 35588-20170704-end*/
				if (!content.isEmpty()) {
					boolean update_widget = true;

					NoteEvent event = new NoteEvent();
					event.setContents(content);
					event.setCreateDate(mNotepadCreateTime);
					event.setBgColor(id_bg_color);
					event.setFontSize(mFontSize);
					event.setUuid(mUuid);

					mNotePadDataBaseDao = new NotePadDataBaseDaoImpl(NotePadEditActivity.this);

					//新建的便签事件
					if (NoteListPage.isNewCreate) {
						mNotePadDataBaseDao.create(event);            //新建便签事件插入数据库中
						NoteListPage.isNewCreate = false;

						//更改了某便签事件
					} else if (mIsContentChanged || isFromFloatation) {
						if (rowId == 0) {
							mNotePadDataBaseDao.create(event);
						}else{
							event.setCreateDate(System.currentTimeMillis());
							event.setId(rowId);
							mNotePadDataBaseDao.update(event);
						}
					} else {
						setResult(NotePadActivity.NO_NEED_LOAD_DATA);
						update_widget = false;
					}
					if(update_widget){
						//WidgetUpdateReceiver.sendNoteUpdate(this);
					}

				} else {
					if (NoteListPage.isNewCreate) {
						NoteListPage.isNewCreate = false;
					} else {
						mNotePadDataBaseDao = new NotePadDataBaseDaoImpl(NotePadEditActivity.this);
						mNotePadDataBaseDao.deleteById(rowId);
						FileUtil.deleteNoteImageFile(mUuid);
					}
				}
				Intent intent = new Intent(NoteListPage.BROAD_CAST_ACTION);
				sendBroadcast(intent);
			}
		}
	}
}
