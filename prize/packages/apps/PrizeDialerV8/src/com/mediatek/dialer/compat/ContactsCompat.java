package com.mediatek.dialer.compat;

import java.lang.reflect.Method;

import android.content.Context;
import android.provider.ContactsContract.Aas;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.text.TextUtils;
import com.android.contacts.common.util.PermissionsUtil;

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
            if (DialerCompatExUtils.isMethodAvailable(PHONE_CLASS, GET_TYPE_LABEL_METHOD,
                    Context.class, int.class, CharSequence.class)) {
                if (labelType == Aas.PHONE_TYPE_AAS
                        && !TextUtils.isEmpty(label)
                        && !PermissionsUtil.hasContactsPermissions(context)) {
                    return "";
                }
                ///M: Using new API for AAS phone number label lookup.
                res = Phone.getTypeLabel(context, labelType, label);
            } else {
                res = Phone.getTypeLabel(context.getResources(), labelType, label);
            }
            return res;
        }
    }
}
