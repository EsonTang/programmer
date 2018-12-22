package com.android.settings.powermaster;

import com.android.settings.R;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class DialogUtils {
	public static Dialog getDialog(Context context,String title,
			String content ,OnClickListener cancleListener, OnClickListener confirmListener){
		LayoutInflater layoutInflater = LayoutInflater.from(context);
		View view = layoutInflater.inflate(R.layout.power_master_super_power_dialog, null);

		final Dialog dialog = new Dialog(context, R.style.power_master_super_dialog);

		ImageView iconImageView = (ImageView)view.findViewById(R.id.title_icon);
		TextView titleView = (TextView)view.findViewById(R.id.title_view);

		TextView mContentTwoView = (TextView)view.findViewById(R.id.content);

		TextView leftButton = (TextView)view.findViewById(R.id.negative_button);
		TextView rightButton = (TextView)view.findViewById(R.id.positive_button);

		

		titleView.setText(title);
		mContentTwoView.setText(content);
		
		leftButton.setOnClickListener(cancleListener);
		rightButton.setOnClickListener(confirmListener);

		dialog.setCanceledOnTouchOutside(false);
		//dialog.setCancelable(false);
		//设置它的ContentView
		dialog.setContentView(view);
		dialog.show();
		return dialog;
	}
}

