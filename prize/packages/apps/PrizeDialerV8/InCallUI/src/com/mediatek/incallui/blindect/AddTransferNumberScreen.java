package com.mediatek.incallui.blindect;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.telephony.PhoneNumberUtils;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.Button;
import android.widget.EditText;

import com.android.contacts.common.util.PhoneNumberFormatter;
import com.android.incallui.Log;
import com.android.internal.app.AlertActivity;
import com.android.internal.app.AlertController;
import com.android.incallui.Call;
import com.android.incallui.CallList;
import com.android.incallui.R;
import com.android.incallui.TelecomAdapter;
import com.mediatek.incallui.recorder.BaseAlertScreen;
import com.mediatek.telecom.TelecomManagerEx;

public class AddTransferNumberScreen extends BaseAlertScreen implements
        DialogInterface.OnClickListener, TextWatcher {

    private static final String TAG = "AddTransferNumberScreen";
    private ImageButton mChooseContact;
    private Button mTransferButton;
    private String mCallId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate...");
        super.onCreate(savedInstanceState);
        mCallId = getIntent().getExtras().getString("CallId");
        final AlertController.AlertParams alertView = mAlertParams;
        alertView.mView = createView();
        alertView.mTitle = getResources().getString(R.string.add_transfer_call_number);
        alertView.mPositiveButtonText = getString(R.string.menu_ect);
        alertView.mPositiveButtonListener = this;
        alertView.mNegativeButtonText = getString(com.android.internal.R.string.cancel);
        alertView.mNegativeButtonListener = this;
        AddTransferNumberScreenController.getInstance().setAddTransferNumberDialog(this);
        setupAlert();
        mTransferButton = (Button) mAlert.getButton(DialogInterface.BUTTON_POSITIVE);
        if (mTransferButton != null) {
            mTransferButton.setEnabled(false);
        }
    }

    public View createView() {
        View view = getLayoutInflater().inflate(R.layout.mtk_add_transfer_number, null);
        mChooseContact = (ImageButton) view.findViewById(R.id.contact_icon);
        mChooseContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                chooseFromContacts(AddTransferNumberScreen.this);
            }
        });
        mTransferEditeView = (EditText) view.findViewById(R.id.add_transfer_call_number);
        mTransferEditeView.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
        mTransferEditeView.addTextChangedListener(this);
        PhoneNumberFormatter.setPhoneNumberFormattingTextWatcher(this, mTransferEditeView);
        // show keyboard
        InputMethodManager inputMethodManager = (InputMethodManager)getSystemService
                (Context.INPUT_METHOD_SERVICE);
        inputMethodManager.showSoftInput(mTransferEditeView, InputMethodManager.SHOW_IMPLICIT);
        return view;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        // no-op
    }

    @Override
    public void onTextChanged(CharSequence text, int start, int before, int count) {
        if (mTransferButton != null) {
            mTransferButton.setEnabled(!TextUtils.isEmpty(PhoneNumberUtils.stripSeparators(text
                    .toString())));
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
        // no-op
        mTransferEditeView.setSelection(s.length());
    }

    @Override
    protected void onDestroy() {
        AddTransferNumberScreenController.getInstance().clearAddTransferNumberDialog();
        super.onDestroy();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        // TODO Auto-generated method stub
        if (DialogInterface.BUTTON_POSITIVE == which) {
            sendTransferNumber(PhoneNumberUtils.stripSeparators(mTransferEditeView.getText()
                    .toString()));
            finish();
        } else if (DialogInterface.BUTTON_NEGATIVE == which) {
            finish();
        }
    }

    public void sendTransferNumber(String number) {
        Call call = CallList.getInstance().getCallById(mCallId);
        Log.d(TAG, "sendTransferNumber->number = " + number + "===callId = " + mCallId
                +"====call = " + call);
        if (call != null && call.can(android.telecom.Call.Details.CAPABILITY_BLIND_ASSURED_ECT)) {
            TelecomAdapter.getInstance().explicitCallTransfer(
                    call.getTelecomCall().getCallId(), number,
                    TelecomManagerEx.BLIND_OR_ASSURED_ECT_FROM_NVRAM);
        }
    }
}
