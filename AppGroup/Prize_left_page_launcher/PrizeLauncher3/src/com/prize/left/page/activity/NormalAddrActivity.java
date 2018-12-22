package com.prize.left.page.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import com.android.launcher3.R;
import com.prize.left.page.adapter.AddrAdapter;
import com.prize.left.page.bean.table.NormalAddrTable;
import com.prize.left.page.model.SetAddrModel;
import com.prize.left.page.util.CommonUtils;
import com.prize.left.page.util.ToastUtils;

/***
 * 常用地址设置activity
 * @author fanjunchen
 *
 */
public class NormalAddrActivity extends Activity implements View.OnClickListener {

	private final String TAG = "NormalAddrActivity";
	
	private TextView titleView;
	
	private String mTitle = null;
	
	private AutoCompleteTextView autoHome, autoCompany;
	
	private AddrAdapter homeAdapter, companyAdapter;
	/**是否点击设置家里住址 1 家里, 2 公司*/
	private int isClickHome = 1;
	
	private SetAddrModel model;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		initStatusBar();
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.normal_addr_set);
		
		CommonUtils.changeStatus(getWindow());
        
        initView();
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
	/***
	 * 设置标题及使刷新按钮不可见
	 */
	private void setTitle() {
		titleView = (TextView) findViewById(R.id.tv_title);
		titleView.setText(R.string.str_normal_set_add);
	}

	/***
	 * 初始化控件
	 */
	private void initView() {
		setTitle();
		
		model = new SetAddrModel(this);
		
		NormalAddrTable addr = model.getAddr();
		autoHome = (AutoCompleteTextView)findViewById(R.id.auto_home);
		homeAdapter = new AddrAdapter(this);
		autoHome.setAdapter(homeAdapter);
		autoHome.setThreshold(2);
		
		model.setHomeAdapter(homeAdapter);
		
		autoCompany = (AutoCompleteTextView)findViewById(R.id.auto_company);
		companyAdapter = new AddrAdapter(this);
		autoCompany.setAdapter(companyAdapter);
		autoCompany.setThreshold(2);
		
		if (addr != null) {
			autoHome.setText(addr.homeAddr);
			
			autoCompany.setText(addr.companyAddr);
		}
		
		model.setCompanyAdapter(companyAdapter);
		
		autoHome.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					if (autoHome.getText().toString().trim().length() > 1) {
						if (homeAdapter.getCount() > 0)
							autoHome.showDropDown();
					}
					isClickHome = 1;
					model.setFreshType(isClickHome);
				}
			}
		});
		
		autoCompany.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					if (autoCompany.getText().toString().trim().length() > 1) {
						if (companyAdapter.getCount() > 0)
							autoCompany.showDropDown();
					}
					isClickHome = 2;
					model.setFreshType(isClickHome);
				}
			}
		});
		
		OnItemClickListener itemClk = new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int pos,
					long arg3) {
				if (isClickHome == 1) {
					homeAdapter.setAddr(pos);
				}
				else if (isClickHome == 2) {
					companyAdapter.setAddr(pos);
				}
			}
		};
		
		autoCompany.setOnItemClickListener(itemClk);
		autoHome.setOnItemClickListener(itemClk);
	}
	@Override
	public void onClick(View v) {
		int id = v.getId();
		
		switch (id) {
			case R.id.txt_btn: // 提交并保存到DB中去 完成
				String str1 = autoHome.getText().toString();
				String str2 = autoCompany.getText().toString();
				if (TextUtils.isEmpty(str1)) {
					ToastUtils.showToast(this, R.string.str_fill_home);
					return;
				}
				
				if (TextUtils.isEmpty(str2)) {
					ToastUtils.showToast(this, R.string.str_fill_company);
					return;
				}
				// 保存住址
				if (model.saveAddr(str1,str2)) {
					hideInputMethod();
					finish();
				}
				break;
			case R.id.btn_back: // 返回
				hideInputMethod();
				finish();
				break;
		}
	}
	
	/** 
     * Hides the input method. 
     */  
    protected void hideInputMethod() {  
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);  
        if (imm != null) {  
            imm.hideSoftInputFromWindow(autoHome.getWindowToken(), 0);
        }  
    }  
	@Override
	protected void onPause() {
		hideInputMethod();
		super.onPause();
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
}
