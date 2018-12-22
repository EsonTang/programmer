package com.mediatek.incallui.compat;

import android.content.Context;
import android.provider.ContactsContract.CommonDataKinds.Phone;

/**
 * Compatibility utility class about ContactsContract.
 */
public class ContactsCompat {
    /**
     * Compatibility utility class about ContactsContract.CommonDataKinds.Phone.
     */
    public static class PhoneCompat {
        private static final String PHONE_CLASS =
                "android.provider.ContactsContract$CommonDataKinds$Phone";
        private static final String GET_TYPE_LABEL_METHOD = "getTypeLabel";

        public static CharSequence getTypeLabel(Context context, int labelType,
                CharSequence label) {
            CharSequence res = "";
            if (InCallUICompatUtils.isMethodAvailable(PHONE_CLASS, GET_TYPE_LABEL_METHOD,
                    Context.class, int.class, CharSequence.class)) {
                ///M: Using new API for AAS phone number label lookup.
                res = Phone.getTypeLabel(context, labelType, label);
            } else {
                res = Phone.getTypeLabel(context.getResources(), labelType, label);
            }
            return res;
        }
    }
}
