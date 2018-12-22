package com.prize.prizethemecenter.ui.widget;

import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.prize.prizethemecenter.R;
import com.prize.prizethemecenter.activity.SearchActivity;
import com.prize.prizethemecenter.ui.utils.CommonUtils;

/**
 * 带有搜索联想的搜索框
 * @author pengyang
 */
public class SearchView extends LinearLayout implements OnClickListener{
	/*** 输入框  */
	public EditText etInput;

	/*** 删除键  */
	private ImageView ivDelete;

	/*** 返回按钮*/
	private ImageView btnBack;
	/*** 搜索按钮*/
	private ImageView search_btn;

	/**
	 * 上下文对象
	 */
	private SearchActivity mContext;
	/**
	 * 搜索回调接口
	 */
	private SearchViewListener mListener;

	private boolean isActivity = true; // 默认true
	/*** 当前关键字 */
	private String currentText = "";

	public void setHint(String hint) {
		if (!TextUtils.isEmpty(hint) && etInput != null) {
			etInput.setHint(hint);
//			notifyStartSearching(hint);
		}
	}

	public void setText(String text) {
		if (!TextUtils.isEmpty(text) && etInput != null) {
			etInput.setText(text);
			notifyStartSearching(text);
		}
	}

	public void setSearchViewListener(SearchViewListener listener)
	{
		mListener = listener;
	}

	public SearchView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = (SearchActivity) context;
		LayoutInflater.from(context).inflate(R.layout.search_head_layout, this);
		initViews();
	}

	public void setIsActivity(boolean state) {
		isActivity = state;
	}

	private void initViews() {
		etInput = (EditText) findViewById(R.id.search_et_input);
		ivDelete = (ImageView) findViewById(R.id.search_iv_delete);
		btnBack = (ImageView) findViewById(R.id.search_btn_back);
		search_btn = (ImageView) findViewById(R.id.search_btn);

		ivDelete.setOnClickListener(this);
		btnBack.setOnClickListener(this);
		search_btn.setOnClickListener(this);

		etInput.addTextChangedListener(new EditChangedListener());
		etInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView textView, int actionId,
					KeyEvent keyEvent) {
				if (etInput.hasFocus()
						&& actionId == EditorInfo.IME_ACTION_SEARCH) {
					notifyStartSearching(etInput.getText().toString());
				}
				return true;
			}
		});

		etInput.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// show history
				if(event.getAction()==MotionEvent.ACTION_DOWN){
					if("".equals(currentText)){
						mListener.showHistory();
					}
				}
				return false;
			}
		});
	}

	/**
	 * 让EditText获取焦点
	 */
	public void requstFocus() {
		if (etInput != null) {
			etInput.requestFocus();
			etInput.setFocusable(true);
		}
	}

	/**
	 * 通知监听者 进行搜索操作
	 *
	 * @param text
	 */
	private void notifyStartSearching(String text) {
		Editable query = etInput.getText();
		String key = query.toString().trim();
		if (!TextUtils.isEmpty(key)) {
			processSearchAction(text);
		}
		else if (!TextUtils.isEmpty(etInput.getHint())) {
			processSearchAction(etInput.getHint().toString());
		}
		else {
		}
	}

	private class EditChangedListener implements TextWatcher {
		@Override
		public void beforeTextChanged(CharSequence charSequence, int i, int i2,
				int i3) {

		}

		@Override
		public void afterTextChanged(Editable editable) {
			String content = editable.toString();
			if (!"".equals(content) && etInput.hasFocus()
					&& !currentText.equals(content)) {
				ivDelete.setVisibility(VISIBLE);
                mListener.showTips(content);
			} else {
				ivDelete.setVisibility(GONE);
				mListener.showTips(content);
			}
			currentText = content;
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
		}
	}

	/**
	 * 响应搜索动作
	 * @param text 关键字
	 */
	private void processSearchAction(String text) {
		if (mListener != null) {
			mListener.onSearch(text);
		}
		// 隐藏软键盘
		InputMethodManager imm = (InputMethodManager) mContext
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
	}


	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.search_iv_delete:
			etInput.setText("");
			ivDelete.setVisibility(GONE);
			break;
		case R.id.search_btn_back:
			mContext.finish();
			InputMethodManager imm = (InputMethodManager) mContext
					.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
			break;
		case R.id.search_btn:
			if(CommonUtils.isFastDoubleClick())
				return;
			notifyStartSearching(etInput.getText().toString());
			break;
		}
	}

	/**
	 * search view回调方法
	 */
	public interface SearchViewListener {
		/**
		 * @param text 搜索关键字
		 */
		void onSearch(String text);

        /**显示搜索历史*/
		void showHistory();

		/**实时匹配*/
		void showTips(String content);
	}

	/**
	 * @param text
	 * 点击热门索引或搜索记录传入的关键字
	 */
	public void setTextForEditText(String text) {
		if (etInput != null) {
			this.currentText = text;
			etInput.setText(text);
			etInput.setSelection(text.length());
			ivDelete.setVisibility(VISIBLE);
			processSearchAction(text);
		}
	}
}
