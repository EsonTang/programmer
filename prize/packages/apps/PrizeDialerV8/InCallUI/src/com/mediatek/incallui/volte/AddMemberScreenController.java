package com.mediatek.incallui.volte;

import android.content.Context;
import android.content.Intent;

import com.android.incallui.Log;
import com.mediatek.incallui.recorder.BaseScreenController;

public class AddMemberScreenController extends BaseScreenController{

    private static final String LOG_TAG = "AddMemberScreenController";
    /**
     * This is the max caller count of the conference, including the host.
     */
    private static AddMemberScreenController sInstance = new AddMemberScreenController();
    private AddMemberScreen mAddMemberDialog;
    private String mConferenceCallId;

    public static synchronized AddMemberScreenController getInstance() {
        if (sInstance == null) {
            sInstance = new AddMemberScreenController();
        }
        return sInstance;
    }

    public void showAddMemberDialog(Context context) {
        Log.d(this, "showAddMemberDialog...");
        super.showDialog(context, null, this);
    }

    public void setAddMemberScreen(AddMemberScreen screen) {
        super.setDialog(screen);
    }

    public void clearAddMemberScreen() {
        super.clearDialog(this);
    }

    public void updateConferenceCallId(String conferenceCallId) {
        mConferenceCallId = conferenceCallId;
    }

    public void dismissAddMemberDialog() {
        Log.d(this, "dismissAddMemberDialog...");
        super.dismissDialog(this);
    }

    public String getConferenceCallId() {
        return mConferenceCallId;
    }

    public boolean IsAddMemberScreenShown() {
        return mAddMemberDialog != null && !mAddMemberDialog.isFinishing();
    }
}
