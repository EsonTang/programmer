package com.prize.factorytest.FingerPrint;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.prize.factorytest.R;

/**
 **
 * 类描述：继承于Activity，用于标题栏区域自定义,沉浸式状态栏一体化
 * @author 朱道鹏
 * @version V1.0
 */
public class BaseActivity extends Activity implements OnClickListener{

	/**内容区域的布局*/
	private View contentView;
	/**标题栏区域*/
	private RelativeLayout headerLayoutArea;
	/**左侧点击区域*/
	private RelativeLayout leftClickArea;
	/**子布局区域*/
	private LinearLayout subContentLayout;
	/**回退按钮控件*/
	private Button backButton;
	/**更多操作按钮控件*/
	private LinearLayout baseLayoutArea;
	private TextView titleHeaderView;
	private RelativeLayout rightClickArea;
	private TextView operationButton;
	private TextView midTitleView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Window window = getWindow();  
		window.requestFeature(Window.FEATURE_NO_TITLE);  
		if(VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {  
			window = getWindow();  
			window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS  
					| WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);  
			window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN  
					| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION  
					| View.SYSTEM_UI_FLAG_LAYOUT_STABLE);  
			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);  
			window.setStatusBarColor(getResources().getColor(R.color.title_header_bg_color));  
//			window.setNavigationBarColor(Color.TRANSPARENT);  
		} 

		setContentView(R.layout.activity_base);
		initViews();
	}

	/**
	 * 方法描述：初始化标题栏区域
	 * @param void
	 * @return void
	 * @see BaseActivity#initViews
	 */
	private void initViews() {
		baseLayoutArea = (LinearLayout) findViewById(R.id.base_avitivity_rl);

		headerLayoutArea = (RelativeLayout) findViewById(R.id.header_layout);

		leftClickArea = (RelativeLayout) findViewById(R.id.left_click_area);
		leftClickArea.setOnClickListener(this);

		backButton = (Button) findViewById(R.id.back_image_view);
		
		rightClickArea = (RelativeLayout) findViewById(R.id.right_click_area);
		rightClickArea.setOnClickListener(this);

		operationButton = (TextView) findViewById(R.id.right_operation_button);

		titleHeaderView = (TextView) findViewById(R.id.title_header_view);
		midTitleView = (TextView) findViewById(R.id.mid_title_view);

		subContentLayout = (LinearLayout) findViewById(R.id.sub_layout);
	}

	/**
	 * 方法描述：设置子Activity布局区域
	 * @param int 子Activity布局资源文件resId
	 * @return void
	 * @see setSubContentView(int resId)
	 */

	public void setSubContentView(int resId) {
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		contentView = inflater.inflate(resId, null);
		LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT);
		contentView.setLayoutParams(layoutParams);
		if (null != subContentLayout) {
			subContentLayout.addView(contentView);
		}
	}

	/**
	 * 方法描述：添加内容区域
	 * @param View 需要添加的控件
	 * @return void
	 * @see BaseActivity#setContentLayout
	 */
	public void setContentLayout(View view) {
		if (null != subContentLayout) {
			subContentLayout.addView(view);
		}
	}

	/**
	 * 方法描述：得到子Activity布局View
	 * @param void
	 * @return View
	 * @see BaseActivity#getLyContentView
	 */
	public View getLyContentView() {
		return contentView;
	}

	/**
	 * 方法描述：设置标题栏区域的背景
	 * @param int color的resId
	 * @return void
	 * @see BaseActivity#setHeaderLayoutAreaRes
	 */
	public void setHeaderLayoutAreaRes(int resId) {
		if (null != headerLayoutArea) {
			headerLayoutArea.setBackgroundColor(resId);
		}
	}

	/**
	 * 方法描述：设置标题栏区域的背景
	 * @param Drawable
	 * @return void
	 * @see BaseActivity#setHeaderLayoutAreaDrawable
	 */
	public void setHeaderLayoutAreaDrawable(Drawable drawable) {
		if (null != headerLayoutArea) {
			headerLayoutArea.setBackground(drawable);
		}
	}

	/**
	 * 方法描述：显示标题栏区域
	 * @param void
	 * @return void
	 * @see BaseActivity#displayHeaderLayoutArea
	 */
	public void displayHeaderLayoutArea() {
		if (null != headerLayoutArea) {
			headerLayoutArea.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * 方法描述：隐藏标题栏区域
	 * @param void
	 * @return void
	 * @see BaseActivity#displayHeaderLayoutArea
	 */
	public void hideHeaderLayoutArea() {
		if (null != headerLayoutArea) {
			headerLayoutArea.setVisibility(View.INVISIBLE);
		}
	}

	/**
	 * 方法描述：设置左侧点击区域背景
	 * @param int 背景resId
	 * @return void
	 * @see BaseActivity#setLeftClickAreaRes
	 */
	public void setLeftClickAreaRes(int resId) {
		if (null != leftClickArea) {
			leftClickArea.setBackgroundResource(resId);
		}
	}

	/**
	 * 方法描述：设置左侧点击区域背景
	 * @param Drawable
	 * @return void
	 * @see BaseActivity#setLeftClickAreaDrawable
	 */
	public void setLeftClickAreaDrawable(Drawable drawable) {
		if (null != leftClickArea) {
			leftClickArea.setBackground(drawable);
		}
	}

	/**
	 * 方法描述：显示左侧点击区域
	 * @param void
	 * @return void
	 * @see BaseActivity#displayLeftClickArea
	 */
	public void displayLeftClickArea() {
		if (null != leftClickArea) {
			leftClickArea.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * 方法描述：隐藏左侧点击区域
	 * @param void
	 * @return void
	 * @see BaseActivity#hideLeftClickArea
	 */
	public void hideLeftClickArea() {
		if (null != leftClickArea) {
			leftClickArea.setVisibility(View.INVISIBLE);
		}
	}
	
	/**
	 * 方法描述：隐藏左侧点击区域
	 * @param void
	 * @return void
	 * @see BaseActivity#hideLeftClickArea
	 */
	public void hideLeftClickAreaGone() {
		if (null != leftClickArea) {
			leftClickArea.setVisibility(View.GONE);
		}
	}

	/**
	 * 方法描述：设置左侧点击监听
	 * @param OnClickListener
	 * @return void
	 * @see BaseActivity#setLeftClickAreaListener
	 */
	public void setLeftClickAreaListener(OnClickListener onClickListener) {
		if (null != leftClickArea) {
			leftClickArea.setOnClickListener(onClickListener);
		}
	}

	/**
	 * 方法描述：设置左边按钮(回退按钮)的图片资源
	 * @param int 背景resId
	 * @return void
	 * @see BaseActivity#setBackButtonRes
	 */
	public void setBackButtonRes(int resId) {
		if (null != backButton) {
			backButton.setBackgroundResource(resId);
		}
	}

	/**
	 * 方法描述：设置左边按钮(回退按钮)的图片资源
	 * @param Drawable
	 * @return void
	 * @see BaseActivity#setBackButtonDrawable
	 */
	public void setBackButtonDrawable(Drawable drawable) {
		if (null != backButton) {
			backButton.setBackground(drawable);
		}
	}

	/**
	 * 方法描述：显示左边的按钮(回退按钮)
	 * @param void
	 * @return void
	 * @see BaseActivity#displayBackButton
	 */
	public void displayBackButton() {
		if (null != backButton) {
			backButton.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * 方法描述：隐藏左边的按钮(回退按钮)
	 * @param void
	 * @return void
	 * @see BaseActivity#hideBackButton
	 */
	public void hideBackButton() {
		if (null != backButton) {
			backButton.setVisibility(View.INVISIBLE);
		}
	}
	
	/**
	 * 方法描述：隐藏左边的按钮(回退按钮)
	 * @param void
	 * @return void
	 * @see BaseActivity#hideBackButton
	 */
	public void hideBackButtonGone() {
		if (null != backButton) {
			backButton.setVisibility(View.GONE);
		}
	}
	
	/**
	 * 方法描述：设置左侧按钮监听
	 * @param boolean
	 * @return void
	 * @see BaseActivity#setOperationButtonEnable
	 */

	public void setLeftButtonPressed(boolean flag) {
		if (null != backButton) {
			backButton.setPressed(flag);
		}
	}
	
	/**
	 * 方法描述：显示右侧点击区域
	 * @param void
	 * @return void
	 * @see BaseActivity#displayRightClickArea
	 */
	public void displayRightClickArea() {
		if (null != rightClickArea) {
			rightClickArea.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * 方法描述：隐藏右侧点击区域
	 * @param void
	 * @return void
	 * @see BaseActivity#hideRightClickArea
	 */
	public void hideRightClickArea() {
		if (null != rightClickArea) {
			rightClickArea.setVisibility(View.INVISIBLE);
		}
	}

	/**
	 * 方法描述：设置右侧点击监听
	 * @param OnClickListener
	 * @return void
	 * @see BaseActivity#setRightClickAreaListener
	 */
	public void setRightClickAreaListener(OnClickListener onClickListener) {
		if (null != rightClickArea) {
			rightClickArea.setOnClickListener(onClickListener);
		}
	}

	/**
	 * 方法描述：设置右边按钮(操作按钮)的图片资源
	 * @param int 背景resId
	 * @return void
	 * @see BaseActivity#setOperationButtonRes
	 */
	public void setOperationButtonRes(int resId) {
		if (null != operationButton) {
			operationButton.setBackgroundResource(resId);
		}
	}

	/**
	 * 方法描述：设置右边按钮(操作按钮)的图片资源
	 * @param Drawable
	 * @return void
	 * @see BaseActivity#setOperationButtonDrawable
	 */
	public void setOperationButtonDrawable(Drawable drawable) {
		if (null != operationButton) {
			operationButton.setBackground(drawable);
		}
	}

	/**
	 * 方法描述：显示右边的按钮(操作按钮)
	 * @param void
	 * @return void
	 * @see BaseActivity#displayOperationButton
	 */
	public void displayOperationButton() {
		if (null != operationButton) {
			operationButton.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * 方法描述：隐藏右边的按钮(操作按钮)
	 * @param void
	 * @return void
	 * @see BaseActivity#hideOperationButton
	 */
	public void hideOperationButton() {
		if (null != operationButton) {
			operationButton.setVisibility(View.INVISIBLE);
		}
	}

	/**
	 * 方法描述：设置右侧按钮监听
	 * @param boolean
	 * @return void
	 * @see BaseActivity#setOperationButtonPressed
	 */

	public void setOperationButtonPressed(boolean flag) {
		if (null != operationButton) {
			operationButton.setPressed(flag);
		}
	}
	
	public void setTitleHeaderText(String title){
		if (null != titleHeaderView) {
			titleHeaderView.setText(title);
		}
	}
	
	public void setMidTitleText(String title){
		if (null != midTitleView) {
			midTitleView.setText(title);
		}
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.left_click_area:
			backButton.setEnabled(true);
			finish();
			break;
		default:
			break;
		}
	}
}
