package com.mediatek.incallui.recorder;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.widget.EditText;

import com.android.incallui.Log;
import com.android.internal.app.AlertActivity;
import com.android.internal.app.AlertController;
import com.android.incallui.R;
import com.mediatek.incallui.blindect.AddTransferNumberScreen;
import com.mediatek.incallui.recorder.BaseAlertScreen;

import com.mediatek.incallui.volte.AddMemberEditView;
import com.mediatek.incallui.volte.AddMemberScreen;

public class BaseAlertScreen extends AlertActivity {

    private static final String TAG = "BaseAlertScreen";
    private static final int ADD_CONTACT_RESULT = 10000;
    private static final int ADD_CONFERENCE_MEMBER_RESULT = 10001;
    private boolean mWaitForResult = false;
    public Map<String, String> mContactsMap = new HashMap<String, String>();
    public EditText mTransferEditeView;
    public static AddMemberEditView mEditView;

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause()... mWaitForResult: " + mWaitForResult);
        // If we are pausing while not waiting for result from startActivityForResult(),
        // finish ourself. consider case of home key pressed while we are showing.
        if (!mWaitForResult) {
            finish();
        }
        mWaitForResult = false;
    }

    public void chooseFromContacts(Object object) {
        mWaitForResult = true;
        Intent intent = new Intent(Intent.ACTION_PICK, Phone.CONTENT_URI);
        /// M: fix CR:ALPS02672772,can not select sip/Ims number in contact selection activity. @{
        intent.putExtra("isCallableUri", true);
        /// @}
        if (object instanceof AddMemberScreen) {
            startActivityForResult(intent, ADD_CONFERENCE_MEMBER_RESULT);
        } else {
            startActivityForResult(intent, ADD_CONTACT_RESULT);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult, request = " + requestCode + ", ret = " + resultCode);
        if (RESULT_OK == resultCode) {
            switch (requestCode) {
                case ADD_CONTACT_RESULT:
                    handleChooseContactsResult(getApplicationContext(), data, ADD_CONTACT_RESULT);
                    break;
                case ADD_CONFERENCE_MEMBER_RESULT:
                    handleChooseContactsResult(getApplicationContext(), data,
                            ADD_CONFERENCE_MEMBER_RESULT);
                    break;
                default:
                    break;
            }
        }
    }

    public void handleChooseContactsResult(Context context, Intent data, int requestCode) {
        Uri uri = data.getData();
        // query from contacts
        String name = null;
        String number = null;
        Cursor c = context.getContentResolver().query(uri,
                new String[] { Phone.DISPLAY_NAME, Phone.NUMBER }, null, null, null);
        try {
            if (c.moveToNext()) {
                name = c.getString(0);
                number = c.getString(1);
                mContactsMap.put(number, name);
            }
        } finally {
            c.close();
        }
        Log.d(TAG, "ChooseContactsResult " + uri + ", name = " + name + ", number = " + number);
        if (requestCode == ADD_CONFERENCE_MEMBER_RESULT) {
            mEditView.append(number + ",");
        }
        if (requestCode == ADD_CONTACT_RESULT) {
            mTransferEditeView.setText(number);
        }

    }
}
