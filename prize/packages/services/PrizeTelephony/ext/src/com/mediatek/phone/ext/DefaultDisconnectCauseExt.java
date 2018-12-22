package com.mediatek.phone.ext;

import android.telecom.DisconnectCause;
import android.util.Log;

public class DefaultDisconnectCauseExt implements IDisconnectCauseExt {

    /**
     * called to find the telephonydisconnectcausecode
     *
     * @param telephonyDisconnectCause
     * @param default error
     * @return error
     */
    @Override
    public int toDisconnectCauseCode(int telephonyDisconnectCause, int error) {
        return error;
    }

    /**
     * called to find the corresponding disconnect cause lable
     * corresponding to telephonydisconnectcausecode
     *
     * @param telephonyDisconnectCause
     * @param default string
     * @return string
     */
    @Override
    public CharSequence toDisconnectCauseLabel(int telephonyDisconnectCause,
            CharSequence string) {
        return string;
    }

    /**
     * called to find the corresponding disconnect cause description
     * corresponding to telephonydisconnectcausecode
     *
     * @param telephonyDisconnectCause
     * @param default string
     * @return string
     */
    @Override
    public CharSequence toDisconnectCauseDescription(int telephonyDisconnectCause,
            CharSequence string) {
        return string;
    }

}
