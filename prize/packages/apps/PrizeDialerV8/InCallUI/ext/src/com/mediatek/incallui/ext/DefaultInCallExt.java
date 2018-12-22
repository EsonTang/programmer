package com.mediatek.incallui.ext;

import android.app.Fragment;
import android.os.Handler;
import android.telecom.DisconnectCause;
import android.util.Log;
/**
 * Default implementation for IICallExt.
 */
public class DefaultInCallExt implements IInCallExt {

    @Override
    public String replaceString(String defaultString, String hint) {
        return defaultString;
    }

    @Override
    public void customizeSelectPhoneAccountDialog(Fragment fragment) {
    }

    @Override
    public void showHandoverNotification(Handler handler, int stage, int ratType) {
    }
    @Override
    public void onInCallPresenterSetUp(Object statusbarNotifier,
            Object state, Object callList) {
    }

    @Override
    public void onInCallPresenterTearDown() {
    }

    @Override
    public void maybeShowBatteryDialog(Object newstate, Object oldState){
    }

    @Override
    public void maybeDismissBatteryDialog(){
    }

    @Override
    public boolean maybeShowErrorDialog(DisconnectCause disconnectCause){
        Log.d("DefaultInCallExt", "maybeShowErrorDialog disconnectCause = " + disconnectCause);
        return false;
    }

    @Override
    public boolean showCongratsPopup(DisconnectCause disconnectCause) {
        Log.d("DefaultInCallExt", "maybeShowErrorDialog disconnectCause = " + disconnectCause);
        return false;
    }
}
