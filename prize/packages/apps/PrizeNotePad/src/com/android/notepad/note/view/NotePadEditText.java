
/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：NotePad编辑输入框控件
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

package com.android.notepad.note.view;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.android.notepad.R;
import com.android.notepad.note.NotePadEditActivity;
import com.android.notepad.note.util.BitmapTools;
import com.android.notepad.note.util.DialogUtils;
import com.android.notepad.note.util.FileUtil;
import com.android.notepad.note.util.MediaFile;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import android.net.Uri;
import android.content.Intent;

/**
 **
 * 类描述：NotePad编辑输入框控件
 * @author 朱道鹏
 * @version V1.0
 */
public class NotePadEditText extends EditText {
	
	private static final String TAG = "NotePadEditText";

	public static final int ID_PASTE = android.R.id.paste;
	public static final int ID_CUT = android.R.id.cut;
	private int CLIP_BOARD_LAST_OPERATION = 0;
	public static final int INSERT_STYLE = 1;
	public static final int APPEND_STYLE = 2;
	private Context mContext;
	private InputMethodManager inputManager;
	private int mStyle;
	private String mContent;
	private int mSelectionStart;
	private int mSelectionEnd;
	private boolean isMove;

	private float mDownY;
	private float mMoveY;
	private ImageCreatAsyncTask mAsyncTask = null;

	public NotePadEditText(Context context){
		this(context ,null);
		mContext = context;
	}
	public NotePadEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
	}

	/**
	 * 方法描述：用正则表达式对路径进行匹配，匹配成功则生成对应的图片显示在EditText中
	 * @param Context  String
	 * @return void
	 * @see NotePadEditText#setSpanContent
	 */
	public void setSpanContent(String path, int style){
		if(null != path){
			mStyle = style;
			synchronized (FileUtil.CONFIG_FILE){
				Log.d(TAG, "setSpanContent() path = "+path);
				mAsyncTask = new ImageCreatAsyncTask(path);
				mAsyncTask.execute();
			}
		}
	} 

	public int getClipBoardLastOpeartion(){
		return CLIP_BOARD_LAST_OPERATION;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			isMove = false;
			mSelectionStart = getSelectionStart();
			mSelectionEnd = getSelectionEnd();
			mDownY = getY();
			break;
		case MotionEvent.ACTION_MOVE:
			mMoveY = getY();
			float distance = mMoveY - mDownY;
			if(Math.abs(distance)>20){
				isMove = true;
			}
			break;
		case MotionEvent.ACTION_UP:
			if(isMove){
				setSelection(mSelectionStart);
				return true;
			}
			break;

		default:
			break;
		}
		return super.onTouchEvent(event);
	}

	@Override
	public boolean onTextContextMenuItem(int id) {
		//粘帖板
		final ClipboardManager clip = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
		switch (id) {
		case ID_PASTE:
			// 粘贴板内容
			CLIP_BOARD_LAST_OPERATION = ID_PASTE;
			ClipData cd=clip.getPrimaryClip(); 
			String sequence=cd.getItemAt(0).getText().toString(); 

			if(null != sequence){
				String paste = sequence;
				setSpanContent(paste,INSERT_STYLE);
				return true;
			}
			break;
		case ID_CUT:
			CLIP_BOARD_LAST_OPERATION = ID_CUT;
			break;
		case android.R.id.shareText:			
			String sequence2 = getText().toString().substring(getSelectionStart(),getSelectionEnd());
			Log.d("","Kehwa sequence2: " + sequence2);
			Pattern pattern = Pattern.compile(MediaFile.IMAGE_FORMAT_PATTERN);
			Matcher m = pattern.matcher(sequence2);
			String imagePath = null;
			while(m.find()){
				imagePath = m.group();
				imagePath = imagePath.substring(MediaFile.IMAGE_FORMAT_HEADER.length());
				File pictureFile = new File(imagePath);
				Uri pictureUri = Uri.fromFile(pictureFile);
				Intent intent = new Intent(Intent.ACTION_SEND);
				intent.setType("image/*");
				intent.putExtra(Intent.EXTRA_STREAM, pictureUri);
				mContext.startActivity(Intent.createChooser(intent, null));
				return true;
			}			
			break;
		}
		return super.onTextContextMenuItem(id);
	}
	
	public void cancleImageCreatAsyncTask(){
		if(mAsyncTask != null){
			mAsyncTask.cancel(true);
		}
	}

	private class ImageCreatAsyncTask extends AsyncTask<Void,Void,SpannableString>{

		private String mPath;
		private Dialog mLodingDialog = null;

		public ImageCreatAsyncTask(String path) {
			super();
			mPath = path;
			Log.d(TAG, "ImageCreatAsyncTask() path = "+path);
		}
		
		@Override
		protected void onPreExecute() {
			try {
				mLodingDialog = DialogUtils.createLoadingDialog(mContext, mContext.getResources().getString(
						R.string.data_loading_notice));
				mLodingDialog.show();
			} catch (Exception e) {
				mAsyncTask.cancel(true);
			}
			super.onPreExecute();
		}

		@Override
		protected SpannableString doInBackground(Void... params) {
			File imageFile =null;
			String imagePath = null;
			Pattern pattern = Pattern.compile(MediaFile.IMAGE_PATH_PATTERN);
			String newPath = mPath;
			String oldPath = null;
			Matcher matcher = pattern.matcher(newPath);
			while(matcher.find()){
				oldPath = matcher.group();
				String mUuid = NotePadEditActivity.getUuid();
				String currentEventFileDir = FileUtil.CONFIG_FILE + File.separator + mUuid;
				if(!oldPath.startsWith(currentEventFileDir) && oldPath.startsWith(FileUtil.CONFIG_FILE)){
					int index = oldPath.lastIndexOf(File.separator);
					String fileStarHeader = oldPath.substring(0,index);
					newPath = newPath.replace(fileStarHeader, currentEventFileDir);

					String fileName = oldPath.substring(index+1);
					String clipBoardfilePath = FileUtil.CLIP_BOARD_FILE + File.separator + fileName;
					File mTemporaryFile = new File(clipBoardfilePath);
					String newFilePath = currentEventFileDir + File.separator + fileName;
					if(mTemporaryFile.exists()){
						FileUtil.copyFile(clipBoardfilePath, newFilePath);
						mTemporaryFile.delete();
					}else{
						newPath = newPath.replace(newFilePath, "");
					}
				}
			}
			SpannableString ss = new SpannableString(newPath); 
			Bitmap sourceBitmap = null;
			Bitmap bitmap = null;
			ImageSpan imgSpan = null;
			Pattern imgFtPattern = Pattern.compile(MediaFile.IMAGE_FORMAT_PATTERN);
			Matcher m = imgFtPattern.matcher(newPath);
			
			WindowManager wm = (WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE);

			int mWMWidth = wm.getDefaultDisplay().getWidth();
			int mImageWidth = (mWMWidth-120)/2;
			while(m.find()){
				try {
					imagePath = m.group();
					imagePath = imagePath.substring(MediaFile.IMAGE_FORMAT_HEADER.length());
					
					imageFile = new File(imagePath);
					// Decode image size
					BitmapFactory.Options o = new BitmapFactory.Options();
					o.inJustDecodeBounds = true;
					BitmapFactory.decodeStream(new FileInputStream(imageFile), null, o);
					BitmapFactory.Options o2 = new BitmapFactory.Options();
					o2.inSampleSize = o.outWidth/mImageWidth;
					sourceBitmap = BitmapFactory.decodeStream(new FileInputStream(imageFile),
                         null, o2);						 
					if(null != sourceBitmap){
					    bitmap = BitmapTools.getScaleBitmap(sourceBitmap, mImageWidth);					    
					    imgSpan = new ImageSpan(mContext,bitmap);
						if(ss.length() >= (m.end() + 1)){
							ss.setSpan(imgSpan, m.start() - 1, m.end() + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
						}
					}		
					
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			}
			return ss;
		}
		
		@Override
		protected void onPostExecute(SpannableString spannableString) {
			if(null != spannableString){
				if(INSERT_STYLE == mStyle){
					Editable editable = getText();
					int start = getSelectionStart();
					editable.insert(start, spannableString);
				}else{
					append(spannableString);
				}
				setSelection(getText().toString().length());
				try {
					mLodingDialog.dismiss();
				} catch (Exception e) {
					Log.d(TAG, "onPostExecute(),Error-- mLodingDialog = "+mLodingDialog);
				}
				mLodingDialog = null;
			}
			super.onPostExecute(spannableString);
		}
		
		@Override
		protected void onCancelled(SpannableString result) {
			Context theContext = mLodingDialog.getContext();
			try {
				if(mLodingDialog != null && mLodingDialog.isShowing()){
					Log.d(TAG, "onCancelled(),Context = "+theContext);
					mLodingDialog.dismiss();
				}
			} catch (Exception e) {
				Log.d(TAG, "onCancelled(),Error--Context = "+theContext);
			}
			mLodingDialog = null;
			mAsyncTask = null;
			super.onCancelled(result);
		}
	}

}  
