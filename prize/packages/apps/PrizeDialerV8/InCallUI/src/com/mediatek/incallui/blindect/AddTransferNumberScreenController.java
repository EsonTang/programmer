package com.mediatek.incallui.blindect;

import android.content.Context;
import android.content.Intent;

import com.android.incallui.Log;
import com.mediatek.incallui.recorder.BaseScreenController;

public class AddTransferNumberScreenController extends BaseScreenController {

    public static AddTransferNumberScreenController sInstance =
            new AddTransferNumberScreenController();
    private static final String TAG = "AddTransferNumberScreenController";

    public static synchronized AddTransferNumberScreenController getInstance() {
        if (sInstance == null) {
            sInstance = new AddTransferNumberScreenController();
        }
        return sInstance;
    }

    public void showAddTransferNumberDialog(Context context, String callId) {
        Log.d(TAG, "showAddTransferNumberDialog...");
        super.showDialog(context, callId, this);
    }

    public void setAddTransferNumberDialog(AddTransferNumberScreen screen) {
        super.setDialog(screen);
    }

    public void clearAddTransferNumberDialog() {
        super.clearDialog(this);
    }

    public void dismissAddTransferNumberDialog() {
        Log.d(TAG, "dismissAddTransferNUmberDialog...");
        super.dismissDialog(this);
    }
}
