package com.android.settings.fingerprint;

import android.app.Dialog;
import android.content.Context;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.settings.R;
import android.app.AlertDialog;
import android.content.DialogInterface;
public class PrizeFpOperationDialogUtils { 

	public static Dialog createDoubleButtonEditDialog(Context context, String mTitleName, String mFpName,
			OnClickListener mConfirmClickListener, OnClickListener mCancelClickListener) {  
		/*LayoutInflater inflater = LayoutInflater.from(context);  
		View v = inflater.inflate(R.layout.prize_fp_double_mid_edit_nopsw_dialog, null);
		RelativeLayout layout = (RelativeLayout) v.findViewById(R.id.dialog_view);
		TextView titleView = (TextView)layout.findViewById(R.id.dialog_title);
		PrizeFpCustomEditText contentEdit = (PrizeFpCustomEditText)layout.findViewById(R.id.content_text_edit);
		TextView confirmButton = (TextView)layout.findViewById(R.id.confirm_button);
		TextView cancelButton = (TextView)layout.findViewById(R.id.cancel_button);
		titleView.setText(mTitleName);
		contentEdit.setText(mFpName);
		contentEdit.setSelection(mFpName.length());
		confirmButton.setOnClickListener(mConfirmClickListener);
		cancelButton.setOnClickListener(mCancelClickListener);

		Dialog operationDialog = new Dialog(context, R.style.prize_fp_operation_dialog);

		operationDialog.setCancelable(true);
		operationDialog.setCanceledOnTouchOutside(false);
		operationDialog.setContentView(layout, new RelativeLayout.LayoutParams(  
				RelativeLayout.LayoutParams.MATCH_PARENT,  
				RelativeLayout.LayoutParams.MATCH_PARENT));*/
              LayoutInflater inflater = LayoutInflater.from(context); 
		View v = inflater.inflate(R.layout.prize_fp_double_mid_edit_nopsw_dialog, null);
		RelativeLayout layout = (RelativeLayout) v.findViewById(R.id.dialog_view);
		PrizeFpCustomEditText contentEdit = (PrizeFpCustomEditText)layout.findViewById(R.id.content_text_edit);
		
	       AlertDialog editdialog = null;
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		
		builder.setTitle(mTitleName);		
		builder.setPositiveButton(R.string.prize_fp_operation_confirm,new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,int which) {
					mConfirmClickListener.onClick(null);
                            }
			});
		builder.setNegativeButton(R.string.prize_fp_operation_cancel,new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,int which) {
					mCancelClickListener.onClick(null);
                            }
			});
              editdialog = builder.create();
		editdialog.setView(layout);
		contentEdit.setText(mFpName);
		contentEdit.setSelection(mFpName.length());
		editdialog.show();
		return editdialog;  
	}

    /*PRIZE-Change-M_Fingerprint-wangzhong-2016_6_28-start*/
    /*public static Dialog createDoubleButtonTextDialog(Context context, String mTitleName, String mContent,
            OnClickListener mConfirmClickListener, OnClickListener mCancelClickListener) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.prize_fp_double_mid_text_dialog, null);
        RelativeLayout layout = (RelativeLayout) v.findViewById(R.id.dialog_view);
        TextView titleView = (TextView)layout.findViewById(R.id.dialog_title);
        TextView midView = (TextView)layout.findViewById(R.id.content_text_edit);
        TextView confirmButton = (TextView)layout.findViewById(R.id.confirm_button);
        TextView cancelButton = (TextView)layout.findViewById(R.id.cancel_button);
        titleView.setText(mTitleName);
        midView.setText(mContent);
        confirmButton.setOnClickListener(mConfirmClickListener);
        cancelButton.setOnClickListener(mCancelClickListener);

        Dialog operationDialog = new Dialog(context, R.style.prize_fp_operation_dialog);

        operationDialog.setCancelable(true);
        operationDialog.setCanceledOnTouchOutside(false);
        operationDialog.setContentView(layout, new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT));
        operationDialog.show();
        return operationDialog;
    }*/
    public static Dialog createDoubleButtonTextDialog(Context context, String title, String message,
            OnClickListener mConfirmClickListener, OnClickListener mCancelClickListener) {
        return createDoubleButtonTextDialog(context, title, message, mConfirmClickListener, mCancelClickListener, 
			context.getResources().getString(R.string.prize_fp_operation_confirm), context.getResources().getString(R.string.prize_fp_operation_cancel));
    }
    /*PRIZE-Change-M_Fingerprint-wangzhong-2016_6_28-end*/

    /*PRIZE-Add-M_Fingerprint-wangzhong-2016_6_28-start*/
    public static Dialog createDoubleButtonTextDialog(Context context, String title, String message,
            OnClickListener mConfirmClickListener, OnClickListener mCancelClickListener, String confirmButtonText,
            String cancelButtonText) {
        /*LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.prize_fp_double_mid_text_dialog, null);
        RelativeLayout layout = (RelativeLayout) v.findViewById(R.id.dialog_view);
        TextView titleView = (TextView)layout.findViewById(R.id.dialog_title);
        TextView midView = (TextView)layout.findViewById(R.id.content_text_edit);
        TextView confirmButton = (TextView)layout.findViewById(R.id.confirm_button);
        TextView cancelButton = (TextView)layout.findViewById(R.id.cancel_button);
        titleView.setText(title);
        midView.setText(message);
        if (null != confirmButtonText) confirmButton.setText(confirmButtonText);
        if (null != cancelButtonText) cancelButton.setText(cancelButtonText);
        confirmButton.setOnClickListener(mConfirmClickListener);
        cancelButton.setOnClickListener(mCancelClickListener);

        Dialog operationDialog = new Dialog(context, R.style.prize_fp_operation_dialog);

        operationDialog.setCancelable(true);
        operationDialog.setCanceledOnTouchOutside(false);
        operationDialog.setContentView(layout, new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT));
        operationDialog.show();
        return operationDialog;*/
	AlertDialog textdialog = null;
	AlertDialog.Builder builder = new AlertDialog.Builder(context);	
      
	builder.setTitle(title);
	builder.setMessage(message);
	builder.setPositiveButton(confirmButtonText,new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,int which) {
					mConfirmClickListener.onClick(null);
                            }
			});
	builder.setNegativeButton(cancelButtonText,new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog,int which) {
				mCancelClickListener.onClick(null);
                        }
		});
	textdialog = builder.create();
	textdialog.show();
	return textdialog;
    }
    /*PRIZE-Add-M_Fingerprint-wangzhong-2016_6_28-end*/

}

