package com.mediatek.incallui.ext;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.telecom.PhoneAccount;
import android.view.View;

public class DefaultCallCardExt implements ICallCardExt {

    @Override
    public void onViewCreated(Context context, View rootView) {
        // do nothing
    }

    /**
     * called when CallCard view destroy, based on CallCardFragment lifecycle
     *
     */
    @Override
    public void onDestroyView() {
        // do nothing
    }

    /**
     * called when secondary info view clicked
     *
     * @param clist  host CallList
     * @return boolean should skip host click logic
     */
    @Override
    public boolean handleOnClick(Object clist) {
        return false;
    }

    /**
     * called when onStateChange of CallCardPresenter
     *
     * @param clist  host CallList
     */
    @Override
    public void onIncomingStateChange(Object clist) {
        // do nothing
    }

    /**
     * Check if need set Secondary call info
     *
     * @return boolean should skip host update
     */
    @Override
    public boolean needUpdateSecondaryCall() {
        return true;
    }

    @Override
    public void onStateChange(android.telecom.Call call) {
        // do nothing
    }

    @Override
    public void updatePrimaryDisplayInfo(android.telecom.Call call) {
        // do nothing
    }

    /**
     * Return the icon drawable to represent the call provider.
     *
     * @param context for get service.
     * @param account for get icon.
     * @return The icon.
     */
    @Override
    public Drawable getCallProviderIcon(Context context, PhoneAccount account) {
        return null;
    }

    /**
     * Return the string label to represent the call provider.
     *
     * @param context for get service.
     * @param account for get lable.
     * @return The lable.
     */
    @Override
    public String getCallProviderLabel(Context context, PhoneAccount account) {
        return null;
    }

    @Override
    public void setPhoneAccountForSecondCall(PhoneAccount account) {
        // do nothing
    }

    @Override
    public void setPhoneAccountForThirdCall(PhoneAccount account) {
        // do nothing
    }

    @Override
    public boolean shouldShowCallAccountIcon() {
        return false;
    }

    @Override
    public Bitmap getSecondCallPhoneAccountBitmap() {
        return null;
    }

    @Override
    public Bitmap getThirdCallPhoneAccountBitmap() {
        return null;
    }

    @Override
    public String getSecondCallProviderLabel() {
        return null;
    }

    @Override
    public String getThirdCallProviderLabel() {
        return null;
    }

    @Override
    public void customizeCallerInfo(Context context, Object callerInfo, Cursor cursor) {
        // do nothing.
    }
}
