package com.mediatek.camera.mode.watermark;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

import com.android.camera.R;
import com.android.camera.ui.RotateImageView;
 import android.view.inputmethod.InputMethodManager;
import android.view.WindowManagerPolicy;
public class WaterMarkDialog extends Dialog{
	
	private TextView mOkImageView;
	
//	private ImageView mCanleImageView;
	
	private TextView mRotateImageView;
	
	private EditText mEditText;
	
	private Context mContext;
	
	private TextView mTextView;
	
	private WaterMarkDialogBitmap mWaterMarkDialogBitmap;
	
	private PrizeWaterMarkTextResourceInfo	mPrizeWaterMarkTextResourceInfo;
	
	private InputMethodManager  mInputMethodManager  ;
	
	public interface WaterMarkDialogBitmap{
		public void setChangeCurrentShow(final String string,final ImageView mRotateImageView);
	}

	public WaterMarkDialog(Context context,final TextView mRotateImageView,WaterMarkDialogBitmap mWaterMarkDialogBitmap) {
		super(context,R.style.Dialog_Fullscreen_width);
		this.mRotateImageView = mRotateImageView;
		this.mContext = context;
		this.mWaterMarkDialogBitmap = mWaterMarkDialogBitmap;
		mInputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.prize_watermark_dialog_show);
		Window dialogWindow = getWindow();
		dialogWindow.setLayout(WindowManager.LayoutParams.FILL_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
//		WindowManager.LayoutParams lp = dialogWindow.getAttributes();
//		dialogWindow.getDecorView().setPadding(0, 0, 0, 0);
//		
//		lp.width =  ;
//		lp.height = ;
		dialogWindow.setGravity(Gravity.BOTTOM);
//		dialogWindow.setAttributes(lp);
        /*prize-bugid: 35333,35196-liufan-2017-06-29-start*/
        dialogWindow.setType(WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG);
        /*prize-bugid: 35333,35196-liufan-2017-06-29-end*/
		dialogWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE |
                WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
		
		mOkImageView = (TextView) findViewById(R.id.water_ok);
//		mCanleImageView = (ImageView) findViewById(R.id.water_cancle);
		mEditText = (EditText) findViewById(R.id.water_edit);
		mTextView = (TextView) findViewById(R.id.water_text);
		mOkImageView.setOnClickListener(mOkClickListener);
//		mCanleImageView.setOnClickListener(mOkClickListener);
		mPrizeWaterMarkTextResourceInfo= (PrizeWaterMarkTextResourceInfo) mRotateImageView.getTag();

		mEditText.setText(mPrizeWaterMarkTextResourceInfo.getShowString());
		mTextView.setText(mPrizeWaterMarkTextResourceInfo.getShowString().length() + "/" + mPrizeWaterMarkTextResourceInfo.getTextLimitPostion());
		mEditText.setSelection(mPrizeWaterMarkTextResourceInfo.getShowString().length());
		mEditText.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				Editable editable = mEditText.getText();
				int len = editable.length();
				int newLen = len;
				
				if (len > mPrizeWaterMarkTextResourceInfo.getTextLimitPostion()) {
					int selEndIndex = Selection.getSelectionEnd(editable);
					String str = editable.toString();
					String newStr = str.substring(0, mPrizeWaterMarkTextResourceInfo.getTextLimitPostion());
					mEditText.setText(newStr);
					editable = mEditText.getText();
					
					newLen = editable.length();
					if (selEndIndex > newLen) {
						selEndIndex = editable.length();
					}
					Selection.setSelection(editable, selEndIndex);
				}
				mTextView.setText(newLen + "/" + mPrizeWaterMarkTextResourceInfo.getTextLimitPostion());
			}
		});
	}
	
	private View.OnClickListener mOkClickListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			switch (arg0.getId()) {
			case R.id.water_ok:
				PrizeWaterMarkTextResourceInfo	mPrizeWaterMarkTextResourceInfo= (PrizeWaterMarkTextResourceInfo) mRotateImageView.getTag();
//				PrizeWaterMarkTextResourceInfo	mPrizeWaterMarkTextResourceInfo= (PrizeWaterMarkTextResourceInfo) mTextView.getTag();
				mPrizeWaterMarkTextResourceInfo.setShowString(mEditText.getText().toString());
				mRotateImageView.setText(mEditText.getText().toString());
//				mWaterMarkDialogBitmap.setChangeCurrentShow(mEditText.getText().toString(),mRotateImageView);
//				mRotateImageView.setBitmap();
//				mTextView.setText(mEditText.getText());
				break;
//			case R.id.water_cancle:
//				break;
			default:
				break;
			}
			WaterMarkDialog.this.dismiss();
		}
	};
	
	@Override
    public void dismiss() {
        if (mInputMethodManager.isActive() && getCurrentFocus() != null){
            mInputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
	   super.dismiss();
    }

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_HOME) {
			gotoHome();
			return true;
		}
		return super.onKeyUp(keyCode, event);
	}

	public void gotoHome() {
		Intent mHomeIntent =  new Intent(Intent.ACTION_MAIN, null);
		mHomeIntent.addCategory(Intent.CATEGORY_HOME);
		mHomeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
		mHomeIntent.putExtra(WindowManagerPolicy.EXTRA_FROM_HOME_KEY, true);
		mContext.startActivity(mHomeIntent);
	}
}