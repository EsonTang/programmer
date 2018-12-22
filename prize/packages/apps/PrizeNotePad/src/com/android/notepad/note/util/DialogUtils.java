
/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：Dialog对话框工具
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

package com.android.notepad.note.util;

import com.android.notepad.R;

import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.view.Window;

import android.view.Gravity;
/**
 **
 * 类描述：Dialog对话框工具类
 * @author 朱道鹏
 * @version V1.0
 */
public class DialogUtils {
	private static CheckBox frontOneCheckBox;
	private static CheckBox frontTwoCheckBox;
	private static CheckBox frontThirdCheckBox;
	private static CheckBox frontFourCheckBox;
	private static int  mFontSize;
	private static TextView leftButton;
	private static TextView rightButton;
	private static Handler mHandler;
	private static Dialog mDialog;

	
	 /**
	 * 方法描述：获得Dialog对象
	 * @param Context   int   String   String[]   int   Handler
	 * @return Dialog
	 * @see DialogUtils#getFrontSettingDialog
	 */
	public static Dialog getFrontSettingDialog(Context context,int resId,String title,
			String[] items, int fontSize, final Handler handler){
		mHandler = handler;
		LayoutInflater layoutInflater = LayoutInflater.from(context);
		View dialogView = layoutInflater.inflate(R.layout.layout_dialog, null);

		Dialog dialog = new Dialog(context);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);		

		ImageView iconImageView = (ImageView)dialogView.findViewById(R.id.title_icon);
		TextView titleView = (TextView)dialogView.findViewById(R.id.title_view);

		RelativeLayout frontOneRl = (RelativeLayout)dialogView.findViewById(R.id.item1_rl);
		frontOneRl.setOnClickListener(itemClickListener);
		TextView frontOneView = (TextView)dialogView.findViewById(R.id.front_1);
		frontOneCheckBox = (CheckBox)dialogView.findViewById(R.id.front_1_chexk_box);

		RelativeLayout frontTwoRl = (RelativeLayout)dialogView.findViewById(R.id.item2_rl);
		frontTwoRl.setOnClickListener(itemClickListener);
		TextView frontTwoView = (TextView)dialogView.findViewById(R.id.front_2);
		frontTwoCheckBox = (CheckBox)dialogView.findViewById(R.id.front_2_chexk_box);

		RelativeLayout frontThirdRl = (RelativeLayout)dialogView.findViewById(R.id.item3_rl);
		frontThirdRl.setOnClickListener(itemClickListener);
		TextView frontThirdView = (TextView)dialogView.findViewById(R.id.front_3);
		frontThirdCheckBox = (CheckBox)dialogView.findViewById(R.id.front_3_chexk_box);

		RelativeLayout frontFourRl = (RelativeLayout)dialogView.findViewById(R.id.item4_rl);
		frontFourRl.setOnClickListener(itemClickListener);
		TextView frontFourView = (TextView)dialogView.findViewById(R.id.front_4);
		frontFourCheckBox = (CheckBox)dialogView.findViewById(R.id.front_4_chexk_box);

		leftButton = (TextView)dialogView.findViewById(R.id.positive_button);
		rightButton = (TextView)dialogView.findViewById(R.id.negative_button);

		mFontSize = fontSize;
		switch (fontSize) {
		case 14:
			frontOneCheckBox.setChecked(true); 
			frontTwoCheckBox.setChecked(false);  
			frontThirdCheckBox.setChecked(false);
			frontFourCheckBox.setChecked(false);
			break;
		case 16:
			frontOneCheckBox.setChecked(false); 
			frontTwoCheckBox.setChecked(true);  
			frontThirdCheckBox.setChecked(false);
			frontFourCheckBox.setChecked(false);
			break;
		case 18:
			frontOneCheckBox.setChecked(false); 
			frontTwoCheckBox.setChecked(false);  
			frontThirdCheckBox.setChecked(true);
			frontFourCheckBox.setChecked(false);
			break;
		case 20:
			frontOneCheckBox.setChecked(false); 
			frontTwoCheckBox.setChecked(false);  
			frontThirdCheckBox.setChecked(false);
			frontFourCheckBox.setChecked(true);
			break;
		default:
			frontOneCheckBox.setChecked(true); 
			frontTwoCheckBox.setChecked(false);  
			frontThirdCheckBox.setChecked(false);
			frontFourCheckBox.setChecked(false);
			break;
		}

		iconImageView.setBackgroundResource(resId);
		titleView.setText(title);
		frontOneView.setText(items[0]);
		frontTwoView.setText(items[1]);
		frontThirdView.setText(items[2]);
		frontFourView.setText(items[3]);
		leftButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Message msg = new Message();
				msg.what = mFontSize;
				handler.sendMessage(msg);
				dialog.dismiss();
			}
		});
		rightButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
			}
		});

		dialog.setCanceledOnTouchOutside(false);
		//设置它的ContentView
		dialog.setContentView(dialogView);		
		//WindowManager.LayoutParams lp = mWindow.getAttributes();
		//lp.width = 688;//720
		//lp.width = 1020;//1080		
		//mWindow.setAttributes(lp);
		dialog.show();
		Window mWindow = dialog.getWindow();
		mWindow.setGravity(Gravity.BOTTOM);
		mWindow.setWindowAnimations(R.style.mypopwindow_anim_style);
		mWindow.getDecorView().setPadding(24, 0, 24, 0);
		mWindow.setBackgroundDrawableResource(R.color.transparent);
		mDialog = dialog;
		return dialog;
	}

	/**
	 * 字体Item点击监听
	 */
	private static OnClickListener itemClickListener = new OnClickListener() {
		@Override
		public void onClick(View view) {
			switch (view.getId()) {
			case R.id.item1_rl:
				mFontSize = 14;
				frontOneCheckBox.setChecked(true); 
				frontTwoCheckBox.setChecked(false);  
				frontThirdCheckBox.setChecked(false);
				frontFourCheckBox.setChecked(false);
				break;
			case R.id.item2_rl:
				mFontSize = 16;
				frontOneCheckBox.setChecked(false); 
				frontTwoCheckBox.setChecked(true);  
				frontThirdCheckBox.setChecked(false);
				frontFourCheckBox.setChecked(false);
				break;
			case R.id.item3_rl:
				mFontSize = 18;
				frontOneCheckBox.setChecked(false); 
				frontTwoCheckBox.setChecked(false);  
				frontThirdCheckBox.setChecked(true);
				frontFourCheckBox.setChecked(false);
				break;
			case R.id.item4_rl:
				mFontSize = 20;
				frontOneCheckBox.setChecked(false); 
				frontTwoCheckBox.setChecked(false);  
				frontThirdCheckBox.setChecked(false);
				frontFourCheckBox.setChecked(true);
				break;
			}
			Message msg = new Message();
			msg.what = mFontSize;
			mHandler.sendMessage(msg);
			mDialog.dismiss();
		}
	};
	
	/** 
	 * 得到自定义的progressDialog 
	 * @param context 
	 * @param msg 
	 * @return 
	 */  
	public static Dialog createLoadingDialog(Context context, String msg) {  
		LayoutInflater inflater = LayoutInflater.from(context);  
		View v = inflater.inflate(R.layout.loading_dialog, null);// 得到加载view  
		RelativeLayout layout = (RelativeLayout) v.findViewById(R.id.dialog_view);// 加载布局  
		// main.xml中的ImageView  
		ImageView spaceshipImage = (ImageView) v.findViewById(R.id.img);  
//		TextView tipTextView = (TextView) v.findViewById(R.id.tipTextView);// 提示文字  
		// 加载动画  
		Animation hyperspaceJumpAnimation = AnimationUtils.loadAnimation(  
				context, R.anim.loading_animation);  
		// 使用ImageView显示动画  
		spaceshipImage.startAnimation(hyperspaceJumpAnimation);  
//		tipTextView.setText(msg);// 设置加载信息  

		Dialog loadingDialog = new Dialog(context, R.style.loading_dialog);// 创建自定义样式dialog  

		loadingDialog.setCancelable(true);// 不可以用“返回键”取消  
		loadingDialog.setCanceledOnTouchOutside(false);
		loadingDialog.setContentView(layout, new RelativeLayout.LayoutParams(  
				RelativeLayout.LayoutParams.WRAP_CONTENT,  
				RelativeLayout.LayoutParams.WRAP_CONTENT));// 设置布局  
		return loadingDialog;  

	}
}

