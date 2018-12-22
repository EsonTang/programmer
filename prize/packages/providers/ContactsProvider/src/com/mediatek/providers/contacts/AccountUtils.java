package com.mediatek.providers.contacts;

import android.provider.ContactsContract.RawContacts;

/**
 * M: Contacts account utils, provide account info API.
 */
public class AccountUtils {

    // Local account types string, same with AccountTypeUtils.java in ContactsCommon.
    public static final String ACCOUNT_TYPE_LOCAL_PHONE = "Local Phone Account";
    // Local phone account name
    public static final String ACCOUNT_NAME_LOCAL_PHONE = "Phone";

    private static final String ACCOUNT_TYPE_SIM = "SIM Account";
    private static final String ACCOUNT_TYPE_USIM = "USIM Account";
    private static final String ACCOUNT_TYPE_RUIM = "RUIM Account";
    private static final String ACCOUNT_TYPE_CSIM = "CSIM Account";

    // Local account type selection string, use to append SQLITE query statement.
    private static final String LOCAL_ACCOUNT_TYPES = "('" +
            ACCOUNT_TYPE_LOCAL_PHONE + "' , '" +
            ACCOUNT_TYPE_SIM + "' , '" +
            ACCOUNT_TYPE_USIM + "' , '" +
            ACCOUNT_TYPE_RUIM + "' , '" +
            ACCOUNT_TYPE_CSIM + "')";

    // Local account type collection string, use to append SQLITE query statement.
    private static final String LOCAL_SUPPORT_GROUP_ACCOUNT_TYPES = "('" +
            ACCOUNT_TYPE_LOCAL_PHONE + "' , '" +
            ACCOUNT_TYPE_USIM + "')";

    /**
     * return local account selection
     */
    public static String getLocalAccountSelection() {
        return "(" + RawContacts.ACCOUNT_NAME + " IS NULL AND " +
                RawContacts.ACCOUNT_TYPE + " IS NULL OR " +
                RawContacts.ACCOUNT_TYPE + " IN " + LOCAL_ACCOUNT_TYPES + ")";
    }

    /**
     * return sync accounts selection
     */
    public static String getSyncAccountSelection() {
        return "(" + RawContacts.ACCOUNT_TYPE + " NOT IN " + LOCAL_ACCOUNT_TYPES + ")";
    }

    /**
     * return local account type selection
     */
    public static String getLocalSupportGroupAccountSelection() {
        return "(" + RawContacts.ACCOUNT_NAME + " IS NULL AND " +
               RawContacts.ACCOUNT_TYPE + " IS NULL OR " +
               RawContacts.ACCOUNT_TYPE + " IN " + AccountUtils.LOCAL_SUPPORT_GROUP_ACCOUNT_TYPES +
               ")";
    }

    /**
     * Check if the account is local account
     */
    public static boolean isLocalAccount(String accountType, String accountName) {
        return (accountType == null && accountName == null)
                || (accountType != null && LOCAL_ACCOUNT_TYPES.contains(accountType));
    }

    /**
     * Check if the account is SIM account.
     */
    public static boolean isSimAccount(String accountType) {
        return accountType != null && LOCAL_ACCOUNT_TYPES.contains(accountType)
                && !accountType.equals(ACCOUNT_TYPE_LOCAL_PHONE);
    }

}
