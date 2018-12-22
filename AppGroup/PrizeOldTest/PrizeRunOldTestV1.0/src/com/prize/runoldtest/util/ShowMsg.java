package com.prize.runoldtest.util;

import android.content.Context;
import android.widget.Toast;

public class ShowMsg {

	public static Toast toast;
	
	public static void showmsg(Context context,String str) {
		if (toast == null) {
            toast = Toast.makeText(context, str, Toast.LENGTH_SHORT);
        } else {
            toast.cancel();
            toast.setText(str);
        }
        toast.show();
	} 
}
