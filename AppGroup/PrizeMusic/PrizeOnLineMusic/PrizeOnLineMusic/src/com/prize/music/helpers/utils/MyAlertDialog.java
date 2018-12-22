package com.prize.music.helpers.utils;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

public class MyAlertDialog extends AlertDialog {
	
	private final String TAG = MyAlertDialog.class.getSimpleName();
	private MyAlertDialogFocusChangeListener lostFocus ;
	private Context mContext;
	
	public void setLostFocusListener(MyAlertDialogFocusChangeListener lostFocus) {
		this.lostFocus = lostFocus;
	}

	protected MyAlertDialog(Context context) {
		super(context);
		mContext = context;
		final IntentFilter filter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        context.registerReceiver(mReceiver, filter);
        setOnDismissListener(new OnDismissListener() {
			
			@Override
			public void onDismiss(DialogInterface dialog) {
				mContext.unregisterReceiver(mReceiver);
				Log.i(TAG, "[onDismiss]");
			}
		});
	}
	
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_CLOSE_SYSTEM_DIALOGS.equals(intent.getAction())) {
                Log.d(TAG, "Received ACTION_CLOSE_SYSTEM_DIALOGS");
                if (lostFocus != null) {
        			lostFocus.lostFocus();
        			lostFocus = null;
        		}
            }
        }
    };

}
