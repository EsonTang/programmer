/***
 * function:watermark
 * author: wanzhijuan
 * date:   2016-1-21
 */
package com.prize.sticker;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.Selection;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.gallery3d.R;

public class WordEditDialog extends Dialog {
	
	private EditText mWordEdit;
    private TextView mCountTv;
	private ImageView mOkIm;
	private Context mContext;
	private String mWord;
	private int mLimitLen;
	private IExchangeWord mExchangeWord;
	private boolean mIsSingleLine;
	public interface IExchangeWord {
		void callback(String newWord);
	}

	public WordEditDialog(Context context, final String word, int limit, boolean isSingleLine, IExchangeWord exchangeWord) {
		super(context, R.style.WmWordEditDialog);
		this.mContext = context;
		mWord = word;
		mLimitLen = limit;
		mIsSingleLine = isSingleLine;
		mExchangeWord = exchangeWord;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.filtershow_sticker_word_edit);
		Window dialogWindow = getWindow();
		dialogWindow.setLayout(WindowManager.LayoutParams.FILL_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
		dialogWindow.setGravity(Gravity.BOTTOM);
		dialogWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
		findViews();
		mWordEdit.setSingleLine(mIsSingleLine);
		mWordEdit.setText(mWord);
		mWordEdit.setSelection(mWord.length());
	}
	
	private void findViews() {
		mWordEdit = (EditText) findViewById(R.id.et);
		mCountTv = (TextView) findViewById(R.id.tv_count);
		mOkIm = (ImageView) findViewById(R.id.ok);
		mOkIm.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String word = mWordEdit.getText().toString();
				if (!TextUtils.isEmpty(word)) {
					if (mExchangeWord != null) {
						mExchangeWord.callback(word);
					}
					WordEditDialog.this.dismiss();
				}
			}

		});
		
		mWordEdit.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				Editable editable = mWordEdit.getText();
				int len = editable.length();
				int newLen = len;
				
				if (len > mLimitLen) {
					int selEndIndex = Selection.getSelectionEnd(editable);
					String str = editable.toString();
					String newStr = str.substring(0, mLimitLen);
					mWordEdit.setText(newStr);
					editable = mWordEdit.getText();
					
					newLen = editable.length();
					if (selEndIndex > newLen) {
						selEndIndex = editable.length();
					}
					Selection.setSelection(editable, selEndIndex);
				}
				mCountTv.setText(newLen + "/" + mLimitLen);
			}
		});
	}
	
}
