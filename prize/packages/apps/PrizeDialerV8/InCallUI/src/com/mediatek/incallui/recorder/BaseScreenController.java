package com.mediatek.incallui.recorder;

import com.android.incallui.Log;
import com.mediatek.incallui.blindect.AddTransferNumberScreen;
import com.mediatek.incallui.blindect.AddTransferNumberScreenController;
import com.mediatek.incallui.volte.AddMemberScreen;
import com.mediatek.incallui.volte.AddMemberScreenController;

import android.content.Context;
import android.content.Intent;

public class BaseScreenController {

    private AddTransferNumberScreen mAddTransferNumberDialog;
    private AddMemberScreen mAddMemberDialog;

    public void showDialog(Context context, String exrtaParam, Object o) {
        Intent intent;
        if (o instanceof AddTransferNumberScreenController) {
            intent = new Intent(context, AddTransferNumberScreen.class);
            intent.putExtra("CallId", exrtaParam);
        } else {
            intent = new Intent(context, AddMemberScreen.class);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public void setDialog(Object o) {
        // If there has one "Dialog" already, dismiss it first. quick-click may cause this.
        if (o instanceof AddTransferNumberScreen) {
            if (mAddTransferNumberDialog != null) {
                mAddTransferNumberDialog.finish();
            }
            mAddTransferNumberDialog = (AddTransferNumberScreen) o;
        } else {
            if (mAddMemberDialog != null) {
                mAddMemberDialog.finish();
            }
            mAddMemberDialog = (AddMemberScreen) o;
        }
    }

    public void clearDialog(Object o) {
        if (o instanceof AddTransferNumberScreenController && mAddTransferNumberDialog != null
                && mAddTransferNumberDialog.isFinishing()) {
            mAddTransferNumberDialog = null;
        }
        if (o instanceof AddMemberScreenController && mAddMemberDialog != null
                && mAddMemberDialog.isFinishing()) {
            mAddMemberDialog = null;
        }
    }

    public void dismissDialog(Object o) {
        if (o instanceof AddTransferNumberScreenController && mAddTransferNumberDialog != null) {
            mAddTransferNumberDialog.finish();
            mAddTransferNumberDialog = null;
        }
        if (o instanceof AddMemberScreenController && mAddMemberDialog != null) {
            mAddMemberDialog.finish();
            mAddMemberDialog = null;
        }
    }
}
