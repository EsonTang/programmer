package com.mediatek.phone.ext;

public interface IDisconnectCauseExt {
    /**
     * called to find the telephonydisconnectcausecode
     *
     * @param telephonyDisconnectCause
     * @param default error
     * @return error
     */
    int toDisconnectCauseCode(int telephonyDisconnectCause, int error);
    /**
     * called to find the corresponding disconnect
     * cause lable corresponding to telephonydisconnectcausecode
     *
     * @param telephonyDisconnectCause
     * @param default string
     * @return string
     */
    CharSequence toDisconnectCauseLabel(int telephonyDisconnectCause, CharSequence string);

    /**
     * called to find the corresponding disconnect cause description
     * corresponding to telephonydisconnectcausecode
     *
     * @param telephonyDisconnectCause
     * @param default string
     * @return string
     */
    CharSequence toDisconnectCauseDescription(int telephonyDisconnectCause, CharSequence string);
}
