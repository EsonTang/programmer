package com.example.framerecorder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.app.StatusBarManager;
import android.app.AlertDialog;
import android.view.View;
import android.view.LayoutInflater;
import android.widget.TextView;
import android.widget.Button;
import android.view.WindowManager;

public class NotificationReceiver extends BroadcastReceiver {
    public static final String NOTIFY_ACTION = "com.example.framerecorder.notification";
	private static AlertDialog alertDialog;
	private TextView title;
    private TextView content;
    private Button diaNeg;
    private View divideLine;
    private Button diaSure;
	private TextView seperate;

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
		StatusBarManager mStatusBarManager= (StatusBarManager) context.getSystemService(Context.STATUS_BAR_SERVICE);
        if(action.equals(NOTIFY_ACTION)){
			if (alertDialog == null) {
				alertDialog = new AlertDialog.Builder(context).create();
				LayoutInflater inflater = LayoutInflater.from(context);
				View dialogView = inflater.inflate(R.layout.dialog_view_titled, null);
				title = (TextView) dialogView.findViewById(R.id.title);
				content = (TextView) dialogView.findViewById(R.id.content);
				diaNeg = (Button) dialogView.findViewById(R.id.dia_neg);
				divideLine = dialogView.findViewById(R.id.divide_line);
				diaSure = (Button) dialogView.findViewById(R.id.dia_sure);
				seperate = (TextView) dialogView.findViewById(R.id.seperate);
				title.setVisibility(View.GONE);
				seperate.setVisibility(View.GONE);
				content.setText(context.getString(R.string.stop_hint));
				diaNeg.setVisibility(View.VISIBLE);
				divideLine.setVisibility(View.VISIBLE);
				diaNeg.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						if (alertDialog != null) {
							alertDialog.dismiss();
						}
					}
				});
				diaSure.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						if (alertDialog != null) {
							alertDialog.dismiss();
						}
						Intent stoptintent = new Intent(context.getApplicationContext(), TakeScreenRecordService.class);
						context.stopService(stoptintent);
						mStatusBarManager.clearStatusBarBackgroundColor();
					}
				});
				alertDialog.setOnDismissListener(new OnDismissListener() {
					
					@Override
					public void onDismiss(DialogInterface dialog) {
						alertDialog = null;
						
					}
				});
				alertDialog.setView(dialogView);
				alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
				alertDialog.show();
			}
        }
    }
}
