package com.goodix.util;

import com.goodix.util.CustomEditText;

import android.app.Dialog;
import android.content.Context;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.goodix.fpsetting.R;

public class DialogUtils { 
	public static Dialog createOperationDialog(Context context, String mSLFpsvcFPName, 
			TextWatcher mSLFpsvcFPTextWatcher, OnClickListener mSLFpsvcFPDeleteClickListener) {  
		LayoutInflater inflater = LayoutInflater.from(context);  
		View v = inflater.inflate(R.layout.operation_dialog, null);
		RelativeLayout layout = (RelativeLayout) v.findViewById(R.id.dialog_view);
		CustomEditText contentEdit = (CustomEditText)layout.findViewById(R.id.content_text_edit);
		contentEdit.setText(mSLFpsvcFPName);
		contentEdit.setSelection(mSLFpsvcFPName.length());
		contentEdit.addTextChangedListener(mSLFpsvcFPTextWatcher);
		TextView deleteButton = (TextView)layout.findViewById(R.id.operation_button);
		deleteButton.setOnClickListener(mSLFpsvcFPDeleteClickListener);
		
		Dialog operationDialog = new Dialog(context, R.style.operation_dialog);

		operationDialog.setCancelable(true);
		operationDialog.setCanceledOnTouchOutside(false);
		operationDialog.setContentView(layout, new RelativeLayout.LayoutParams(  
				RelativeLayout.LayoutParams.MATCH_PARENT,  
				RelativeLayout.LayoutParams.MATCH_PARENT));
		operationDialog.show();
		return operationDialog;  
	}
	
	public static Dialog createOperationDialog(Context context, String mTitleName, String nPromptTitleName, TextWatcher mWatcher,
			OnClickListener mConfirmClickListener, OnClickListener mCancelClickListener) {  
		LayoutInflater inflater = LayoutInflater.from(context);  
		View v = inflater.inflate(R.layout.app_lock_psw_operation_dialog, null);
		RelativeLayout layout = (RelativeLayout) v.findViewById(R.id.dialog_view);
		TextView titleView = (TextView)layout.findViewById(R.id.dialog_title);
		TextView promptTitle = (TextView)layout.findViewById(R.id.prompt_title);
		CustomEditText contentEdit = (CustomEditText)layout.findViewById(R.id.content_text_edit);
		TextView confirmButton = (TextView)layout.findViewById(R.id.confirm_button);
		TextView cancelButton = (TextView)layout.findViewById(R.id.cancel_button);
		titleView.setText(mTitleName);
		promptTitle.setText(nPromptTitleName);
		contentEdit.addTextChangedListener(mWatcher);
		confirmButton.setOnClickListener(mConfirmClickListener);
		cancelButton.setOnClickListener(mCancelClickListener);
		
		Dialog operationDialog = new Dialog(context, R.style.operation_dialog);

		operationDialog.setCancelable(true);
		operationDialog.setCanceledOnTouchOutside(false);
		operationDialog.setContentView(layout, new RelativeLayout.LayoutParams(  
				RelativeLayout.LayoutParams.MATCH_PARENT,  
				RelativeLayout.LayoutParams.MATCH_PARENT));
		operationDialog.show();
		return operationDialog;  
	}
}

