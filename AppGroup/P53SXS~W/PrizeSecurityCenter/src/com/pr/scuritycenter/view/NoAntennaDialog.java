package com.pr.scuritycenter.view;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.os.Bundle;
import android.view.KeyEvent;

public class NoAntennaDialog extends DialogFragment {

	private NoAntennaListener mListener = null;

	public static NoAntennaDialog newInstance() {
		return new NoAntennaDialog();
	}

	public interface NoAntennaListener {
		/**
		 * Continue to operate when no antenna
		 */
		void noAntennaContinue();

		/**
		 * Cancel operate when no antenna
		 */
		void noAntennaCancel();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mListener = (NoAntennaListener) activity;
		} catch (ClassCastException e) {
			e.printStackTrace();

		}
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		CustomDialog.Builder builder = new CustomDialog.Builder(getActivity());
		 builder.setMessage("杀毒扫描即将完成，能有效查杀木马，避免恶意扣费。您确定要取消？")
		 		.setTitle("安全卫士提示")
		 		.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						 mListener.noAntennaContinue();
					}
				})
				.setNegativeButton("取消", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						mListener.noAntennaCancel();
					}
				});
		CustomDialog d = builder.create();
		d.setCanceledOnTouchOutside(false);
		d.setOnKeyListener(new OnKeyListener() {
			
			@Override
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				 if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
	                   // Log.d(TAG, "click back key, need to exit fm");
	                    mListener.noAntennaCancel();
	                }
	                return false;
			}
		});
		return d;
	}
}
