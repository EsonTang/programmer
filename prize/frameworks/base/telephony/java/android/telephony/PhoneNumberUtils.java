/*
* Copyright (C) 2014 MediaTek Inc.
* Modification based on code covered by the mentioned copyright
* and/or permission notice(s).
*/
/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.telephony;

import com.android.i18n.phonenumbers.NumberParseException;
// MTK-START
import com.android.i18n.phonenumbers.Phonemetadata.PhoneMetadata;
// MTK-END
import com.android.i18n.phonenumbers.PhoneNumberUtil;
import com.android.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.android.i18n.phonenumbers.Phonenumber.PhoneNumber;
import com.android.i18n.phonenumbers.ShortNumberUtil;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.location.CountryDetector;
import android.net.Uri;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.provider.Contacts;
import android.provider.ContactsContract;
import android.telecom.PhoneAccount;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.TtsSpan;
import android.util.SparseIntArray;

import static com.android.internal.telephony.TelephonyProperties.PROPERTY_OPERATOR_IDP_STRING;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/// M: @{
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.TelephonyProperties;

import com.mediatek.common.MPlugin;
import com.mediatek.common.telephony.IPhoneNumberExt;
import com.mediatek.internal.telephony.ITelephonyEx;
import com.mediatek.internal.telephony.cdma.pluscode.IPlusCodeUtils;
import com.mediatek.internal.telephony.cdma.pluscode.PlusCodeProcessor;

import java.util.ArrayList;
import java.util.HashMap;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
/// @}

/**
 * Various utilities for dealing with phone number strings.
 */
public class PhoneNumberUtils
{
    /*
     * Special characters
     *
     * (See "What is a phone number?" doc)
     * 'p' --- GSM pause character, same as comma
     * 'n' --- GSM wild character
     * 'w' --- GSM wait character
     */
    public static final char PAUSE = ',';
    public static final char WAIT = ';';
    public static final char WILD = 'N';

    /*
     * Calling Line Identification Restriction (CLIR)
     */
    private static final String CLIR_ON = "*31#";
    private static final String CLIR_OFF = "#31#";

    /*
     * TOA = TON + NPI
     * See TS 24.008 section 10.5.4.7 for details.
     * These are the only really useful TOA values
     */
    public static final int TOA_International = 0x91;
    public static final int TOA_Unknown = 0x81;

    static final String LOG_TAG = "PhoneNumberUtils";
    private static final boolean DBG = false;

    /*
     * global-phone-number = ["+"] 1*( DIGIT / written-sep )
     * written-sep         = ("-"/".")
     */
    private static final Pattern GLOBAL_PHONE_NUMBER_PATTERN =
            Pattern.compile("[\\+]?[0-9.-]+");

    /** True if c is ISO-LATIN characters 0-9 */
    public static boolean
    isISODigit (char c) {
        return c >= '0' && c <= '9';
    }

    /** True if c is ISO-LATIN characters 0-9, *, # */
    public final static boolean
    is12Key(char c) {
        return (c >= '0' && c <= '9') || c == '*' || c == '#';
    }

    /** True if c is ISO-LATIN characters 0-9, *, # , +, WILD  */
    public final static boolean
    isDialable(char c) {
        return (c >= '0' && c <= '9') || c == '*' || c == '#' || c == '+' || c == WILD;
    }

    /** True if c is ISO-LATIN characters 0-9, *, # , + (no WILD)  */
    public final static boolean
    isReallyDialable(char c) {
        return (c >= '0' && c <= '9') || c == '*' || c == '#' || c == '+';
    }

    /** True if c is ISO-LATIN characters 0-9, *, # , +, WILD, WAIT, PAUSE   */
    public final static boolean
    isNonSeparator(char c) {
        return (c >= '0' && c <= '9') || c == '*' || c == '#' || c == '+'
                || c == WILD || c == WAIT || c == PAUSE;
    }

    /** This any anything to the right of this char is part of the
     *  post-dial string (eg this is PAUSE or WAIT)
     */
    public final static boolean
    isStartsPostDial (char c) {
        return c == PAUSE || c == WAIT;
    }

    private static boolean
    isPause (char c){
        return c == 'p'||c == 'P';
    }

    private static boolean
    isToneWait (char c){
        return c == 'w'||c == 'W';
    }


    /** Returns true if ch is not dialable or alpha char */
    private static boolean isSeparator(char ch) {
        return !isDialable(ch) && !(('a' <= ch && ch <= 'z') || ('A' <= ch && ch <= 'Z'));
    }

    /** Extracts the phone number from an Intent.
     *
     * @param intent the intent to get the number of
     * @param context a context to use for database access
     *
     * @return the phone number that would be called by the intent, or
     *         <code>null</code> if the number cannot be found.
     */
    public static String getNumberFromIntent(Intent intent, Context context) {
        String number = null;

        Uri uri = intent.getData();

        if (uri == null) {
            return null;
        }

        String scheme = uri.getScheme();

        if (scheme.equals("tel") || scheme.equals("sip")) {
            return uri.getSchemeSpecificPart();
        }

        if (context == null) {
            return null;
        }

        String type = intent.resolveType(context);
        String phoneColumn = null;

        // Correctly read out the phone entry based on requested provider
        final String authority = uri.getAuthority();
        if (Contacts.AUTHORITY.equals(authority)) {
            phoneColumn = Contacts.People.Phones.NUMBER;
        } else if (ContactsContract.AUTHORITY.equals(authority)) {
            phoneColumn = ContactsContract.CommonDataKinds.Phone.NUMBER;
        }

        Cursor c = null;
        try {
            c = context.getContentResolver().query(uri, new String[] { phoneColumn },
                    null, null, null);
            if (c != null) {
                if (c.moveToFirst()) {
                    number = c.getString(c.getColumnIndex(phoneColumn));
                }
            }
        } catch (RuntimeException e) {
            Rlog.e(LOG_TAG, "Error getting phone number.", e);
        } finally {
            if (c != null) {
                c.close();
            }
        }

        return number;
    }

    /** Extracts the network address portion and canonicalizes
     *  (filters out separators.)
     *  Network address portion is everything up to DTMF control digit
     *  separators (pause or wait), but without non-dialable characters.
     *
     *  Please note that the GSM wild character is allowed in the result.
     *  This must be resolved before dialing.
     *
     *  Returns null if phoneNumber == null
     */
    public static String
    extractNetworkPortion(String phoneNumber) {
        if (phoneNumber == null) {
            return null;
        }

        int len = phoneNumber.length();
        StringBuilder ret = new StringBuilder(len);

        for (int i = 0; i < len; i++) {
            char c = phoneNumber.charAt(i);
            // Character.digit() supports ASCII and Unicode digits (fullwidth, Arabic-Indic, etc.)
            int digit = Character.digit(c, 10);
            if (digit != -1) {
                ret.append(digit);
            } else if (c == '+') {
                // Allow '+' as first character or after CLIR MMI prefix
                String prefix = ret.toString();
                if (prefix.length() == 0 || prefix.equals(CLIR_ON) || prefix.equals(CLIR_OFF)) {
                    ret.append(c);
                }
            } else if (isDialable(c)) {
                ret.append(c);
            } else if (isStartsPostDial (c)) {
                break;
            }
        }

        return ret.toString();
    }

    /**
     * Extracts the network address portion and canonicalize.
     *
     * This function is equivalent to extractNetworkPortion(), except
     * for allowing the PLUS character to occur at arbitrary positions
     * in the address portion, not just the first position.
     *
     * @hide
     */
    public static String extractNetworkPortionAlt(String phoneNumber) {
        if (phoneNumber == null) {
            return null;
        }

        int len = phoneNumber.length();
        StringBuilder ret = new StringBuilder(len);
        boolean haveSeenPlus = false;

        for (int i = 0; i < len; i++) {
            char c = phoneNumber.charAt(i);
            if (c == '+') {
                if (haveSeenPlus) {
                    continue;
                }
                haveSeenPlus = true;
            }
            if (isDialable(c)) {
                ret.append(c);
            } else if (isStartsPostDial (c)) {
                break;
            }
        }

        /// M: @{
        vlog("[extractNetworkPortionAlt] phoneNumber: " + ret.toString());
        /// @}

        return ret.toString();
    }

    /**
     * Strips separators from a phone number string.
     * @param phoneNumber phone number to strip.
     * @return phone string stripped of separators.
     */
    public static String stripSeparators(String phoneNumber) {
        if (phoneNumber == null) {
            return null;
        }
        int len = phoneNumber.length();
        StringBuilder ret = new StringBuilder(len);

        for (int i = 0; i < len; i++) {
            char c = phoneNumber.charAt(i);
            // Character.digit() supports ASCII and Unicode digits (fullwidth, Arabic-Indic, etc.)
            int digit = Character.digit(c, 10);
            if (digit != -1) {
                ret.append(digit);
            } else if (isNonSeparator(c)) {
                ret.append(c);
            }
        }

        return ret.toString();
    }

    /**
     * Translates keypad letters to actual digits (e.g. 1-800-GOOG-411 will
     * become 1-800-4664-411), and then strips all separators (e.g. 1-800-4664-411 will become
     * 18004664411).
     *
     * @see #convertKeypadLettersToDigits(String)
     * @see #stripSeparators(String)
     *
     * @hide
     */
    public static String convertAndStrip(String phoneNumber) {
        return stripSeparators(convertKeypadLettersToDigits(phoneNumber));
    }

    /**
     * Converts pause and tonewait pause characters
     * to Android representation.
     * RFC 3601 says pause is 'p' and tonewait is 'w'.
     * @hide
     */
    public static String convertPreDial(String phoneNumber) {
        if (phoneNumber == null) {
            return null;
        }
        int len = phoneNumber.length();
        StringBuilder ret = new StringBuilder(len);

        for (int i = 0; i < len; i++) {
            char c = phoneNumber.charAt(i);

            if (isPause(c)) {
                c = PAUSE;
            } else if (isToneWait(c)) {
                c = WAIT;
            }
            ret.append(c);
        }
        return ret.toString();
    }

    /** or -1 if both are negative */
    static private int
    minPositive (int a, int b) {
        if (a >= 0 && b >= 0) {
            return (a < b) ? a : b;
        } else if (a >= 0) { /* && b < 0 */
            return a;
        } else if (b >= 0) { /* && a < 0 */
            return b;
        } else { /* a < 0 && b < 0 */
            return -1;
        }
    }

    private static void log(String msg) {
        Rlog.d(LOG_TAG, msg);
    }
    /** index of the last character of the network portion
     *  (eg anything after is a post-dial string)
     */
    static private int
    indexOfLastNetworkChar(String a) {
        int pIndex, wIndex;
        int origLength;
        int trimIndex;

        origLength = a.length();

        pIndex = a.indexOf(PAUSE);
        wIndex = a.indexOf(WAIT);

        trimIndex = minPositive(pIndex, wIndex);

        if (trimIndex < 0) {
            return origLength - 1;
        } else {
            return trimIndex - 1;
        }
    }

    /**
     * Extracts the post-dial sequence of DTMF control digits, pauses, and
     * waits. Strips separators. This string may be empty, but will not be null
     * unless phoneNumber == null.
     *
     * Returns null if phoneNumber == null
     */

    public static String
    extractPostDialPortion(String phoneNumber) {
        if (phoneNumber == null) return null;

        int trimIndex;
        StringBuilder ret = new StringBuilder();

        trimIndex = indexOfLastNetworkChar (phoneNumber);

        for (int i = trimIndex + 1, s = phoneNumber.length()
                ; i < s; i++
        ) {
            char c = phoneNumber.charAt(i);
            if (isNonSeparator(c)) {
                ret.append(c);
            }
        }

        return ret.toString();
    }

    /**
     * Compare phone numbers a and b, return true if they're identical enough for caller ID purposes.
     */
    public static boolean compare(String a, String b) {
        // We've used loose comparation at least Eclair, which may change in the future.

        return compare(a, b, false);
    }

    /**
     * Compare phone numbers a and b, and return true if they're identical
     * enough for caller ID purposes. Checks a resource to determine whether
     * to use a strict or loose comparison algorithm.
     */
    public static boolean compare(Context context, String a, String b) {
        boolean useStrict = context.getResources().getBoolean(
               com.android.internal.R.bool.config_use_strict_phone_number_comparation);
        return compare(a, b, useStrict);
    }

    /**
     * @hide only for testing.
     */
    public static boolean compare(String a, String b, boolean useStrictComparation) {
        return (useStrictComparation ? compareStrictly(a, b) : compareLoosely(a, b));
    }

    /**
     * Compare phone numbers a and b, return true if they're identical
     * enough for caller ID purposes.
     *
     * - Compares from right to left
     * - requires MIN_MATCH (7) characters to match
     * - handles common trunk prefixes and international prefixes
     *   (basically, everything except the Russian trunk prefix)
     *
     * Note that this method does not return false even when the two phone numbers
     * are not exactly same; rather; we can call this method "similar()", not "equals()".
     *
     * @hide
     */
    public static boolean
    compareLoosely(String a, String b) {
        int ia, ib;
        int matched;
        int numNonDialableCharsInA = 0;
        int numNonDialableCharsInB = 0;

        if (a == null || b == null) return a == b;

        if (a.length() == 0 || b.length() == 0) {
            return false;
        }

        ia = indexOfLastNetworkChar (a);
        ib = indexOfLastNetworkChar (b);
        matched = 0;

        while (ia >= 0 && ib >=0) {
            char ca, cb;
            boolean skipCmp = false;

            ca = a.charAt(ia);

            if (!isDialable(ca)) {
                ia--;
                skipCmp = true;
                numNonDialableCharsInA++;
            }

            cb = b.charAt(ib);

            if (!isDialable(cb)) {
                ib--;
                skipCmp = true;
                numNonDialableCharsInB++;
            }

            if (!skipCmp) {
                if (cb != ca && ca != WILD && cb != WILD) {
                    break;
                }
                ia--; ib--; matched++;
            }
        }

        /// M: @{
        // MIN match length for CT/CTA
        int minMatchLen = MIN_MATCH;
        if (sIsCtaSupport || sIsOP09Support) {
            minMatchLen = MIN_MATCH_CTA;
        }

        vlog("[compareLoosely] a: " + a + ", b: " + b + ", minMatchLen:" + minMatchLen);

        if (matched < minMatchLen) {
            int effectiveALen = a.length() - numNonDialableCharsInA;
            int effectiveBLen = b.length() - numNonDialableCharsInB;


            // if the number of dialable chars in a and b match, but the matched chars < MIN_MATCH,
            // treat them as equal (i.e. 404-04 and 40404)
            if (effectiveALen == effectiveBLen && effectiveALen == matched) {
                return true;
            }
            /// M: @{
            vlog("[compareLoosely] return: false");
            /// @}
            return false;
        }

        // At least one string has matched completely;
        if (matched >= minMatchLen && (ia < 0 || ib < 0)) {
            return true;
        }
        /// @}

        /*
         * Now, what remains must be one of the following for a
         * match:
         *
         *  - a '+' on one and a '00' or a '011' on the other
         *  - a '0' on one and a (+,00)<country code> on the other
         *     (for this, a '0' and a '00' prefix would have succeeded above)
         */

        if (matchIntlPrefix(a, ia + 1)
            && matchIntlPrefix (b, ib +1)
        ) {
            return true;
        }

        if (matchTrunkPrefix(a, ia + 1)
            && matchIntlPrefixAndCC(b, ib +1)
        ) {
            return true;
        }

        if (matchTrunkPrefix(b, ib + 1)
            && matchIntlPrefixAndCC(a, ia +1)
        ) {
            return true;
        }
        /// M: @{
        vlog("[compareLoosely] return: false");
        /// @}
        return false;
    }

    /**
     * @hide
     */
    public static boolean
    compareStrictly(String a, String b) {
        return compareStrictly(a, b, true);
    }

    /**
     * @hide
     */
    public static boolean
    compareStrictly(String a, String b, boolean acceptInvalidCCCPrefix) {
        if (a == null || b == null) {
            return a == b;
        } else if (a.length() == 0 && b.length() == 0) {
            return false;
        }

        int forwardIndexA = 0;
        int forwardIndexB = 0;

        CountryCallingCodeAndNewIndex cccA =
            tryGetCountryCallingCodeAndNewIndex(a, acceptInvalidCCCPrefix);
        CountryCallingCodeAndNewIndex cccB =
            tryGetCountryCallingCodeAndNewIndex(b, acceptInvalidCCCPrefix);
        boolean bothHasCountryCallingCode = false;
        boolean okToIgnorePrefix = true;
        boolean trunkPrefixIsOmittedA = false;
        boolean trunkPrefixIsOmittedB = false;
        if (cccA != null && cccB != null) {
            if (cccA.countryCallingCode != cccB.countryCallingCode) {
                // Different Country Calling Code. Must be different phone number.
                return false;
            }
            // When both have ccc, do not ignore trunk prefix. Without this,
            // "+81123123" becomes same as "+810123123" (+81 == Japan)
            okToIgnorePrefix = false;
            bothHasCountryCallingCode = true;
            forwardIndexA = cccA.newIndex;
            forwardIndexB = cccB.newIndex;
        } else if (cccA == null && cccB == null) {
            // When both do not have ccc, do not ignore trunk prefix. Without this,
            // "123123" becomes same as "0123123"
            okToIgnorePrefix = false;
        } else {
            if (cccA != null) {
                forwardIndexA = cccA.newIndex;
            } else {
                int tmp = tryGetTrunkPrefixOmittedIndex(b, 0);
                if (tmp >= 0) {
                    forwardIndexA = tmp;
                    trunkPrefixIsOmittedA = true;
                }
            }
            if (cccB != null) {
                forwardIndexB = cccB.newIndex;
            } else {
                int tmp = tryGetTrunkPrefixOmittedIndex(b, 0);
                if (tmp >= 0) {
                    forwardIndexB = tmp;
                    trunkPrefixIsOmittedB = true;
                }
            }
        }

        int backwardIndexA = a.length() - 1;
        int backwardIndexB = b.length() - 1;
        while (backwardIndexA >= forwardIndexA && backwardIndexB >= forwardIndexB) {
            boolean skip_compare = false;
            final char chA = a.charAt(backwardIndexA);
            final char chB = b.charAt(backwardIndexB);
            if (isSeparator(chA)) {
                backwardIndexA--;
                skip_compare = true;
            }
            if (isSeparator(chB)) {
                backwardIndexB--;
                skip_compare = true;
            }

            if (!skip_compare) {
                if (chA != chB) {
                    return false;
                }
                backwardIndexA--;
                backwardIndexB--;
            }
        }

        if (okToIgnorePrefix) {
            if ((trunkPrefixIsOmittedA && forwardIndexA <= backwardIndexA) ||
                !checkPrefixIsIgnorable(a, forwardIndexA, backwardIndexA)) {
                if (acceptInvalidCCCPrefix) {
                    // Maybe the code handling the special case for Thailand makes the
                    // result garbled, so disable the code and try again.
                    // e.g. "16610001234" must equal to "6610001234", but with
                    //      Thailand-case handling code, they become equal to each other.
                    //
                    // Note: we select simplicity rather than adding some complicated
                    //       logic here for performance(like "checking whether remaining
                    //       numbers are just 66 or not"), assuming inputs are small
                    //       enough.
                    return compare(a, b, false);
                } else {
                    return false;
                }
            }
            if ((trunkPrefixIsOmittedB && forwardIndexB <= backwardIndexB) ||
                !checkPrefixIsIgnorable(b, forwardIndexA, backwardIndexB)) {
                if (acceptInvalidCCCPrefix) {
                    return compare(a, b, false);
                } else {
                    return false;
                }
            }
        } else {
            // In the US, 1-650-555-1234 must be equal to 650-555-1234,
            // while 090-1234-1234 must not be equal to 90-1234-1234 in Japan.
            // This request exists just in US (with 1 trunk (NDD) prefix).
            // In addition, "011 11 7005554141" must not equal to "+17005554141",
            // while "011 1 7005554141" must equal to "+17005554141"
            //
            // In this comparison, we ignore the prefix '1' just once, when
            // - at least either does not have CCC, or
            // - the remaining non-separator number is 1
            boolean maybeNamp = !bothHasCountryCallingCode;
            while (backwardIndexA >= forwardIndexA) {
                final char chA = a.charAt(backwardIndexA);
                if (isDialable(chA)) {
                    if (maybeNamp && tryGetISODigit(chA) == 1) {
                        maybeNamp = false;
                    } else {
                        return false;
                    }
                }
                backwardIndexA--;
            }
            while (backwardIndexB >= forwardIndexB) {
                final char chB = b.charAt(backwardIndexB);
                if (isDialable(chB)) {
                    if (maybeNamp && tryGetISODigit(chB) == 1) {
                        maybeNamp = false;
                    } else {
                        return false;
                    }
                }
                backwardIndexB--;
            }
        }

        return true;
    }

    /**
     * Returns the rightmost MIN_MATCH (5) characters in the network portion
     * in *reversed* order
     *
     * This can be used to do a database lookup against the column
     * that stores getStrippedReversed()
     *
     * Returns null if phoneNumber == null
     */
    public static String
    toCallerIDMinMatch(String phoneNumber) {
        /// M: @{
        if (TextUtils.isEmpty(phoneNumber)) {
            return phoneNumber;
        }
        String np = extractNetworkPortionAlt(phoneNumber);
        int minMatchLen = MIN_MATCH;
        if (sIsCtaSupport || sIsOP09Support) {
            minMatchLen = MIN_MATCH_CTA;
        }

        String strStrippedReversed = internalGetStrippedReversed(np, minMatchLen);
        vlog("[toCallerIDMinMatch] phoneNumber: " + phoneNumber +
                 ", minMatchLen: " + minMatchLen + ", result:" + strStrippedReversed);
        /// @}
        return strStrippedReversed;
    }

    /**
     * Returns the network portion reversed.
     * This string is intended to go into an index column for a
     * database lookup.
     *
     * Returns null if phoneNumber == null
     */
    public static String
    getStrippedReversed(String phoneNumber) {
        String np = extractNetworkPortionAlt(phoneNumber);

        if (np == null) return null;

        return internalGetStrippedReversed(np, np.length());
    }

    /**
     * Returns the last numDigits of the reversed phone number
     * Returns null if np == null
     */
    private static String
    internalGetStrippedReversed(String np, int numDigits) {
        if (np == null) return null;

        StringBuilder ret = new StringBuilder(numDigits);
        int length = np.length();

        for (int i = length - 1, s = length
            ; i >= 0 && (s - i) <= numDigits ; i--
        ) {
            char c = np.charAt(i);

            ret.append(c);
        }

        return ret.toString();
    }

    /**
     * Basically: makes sure there's a + in front of a
     * TOA_International number
     *
     * Returns null if s == null
     */
    public static String
    stringFromStringAndTOA(String s, int TOA) {
        if (s == null) return null;

        if (TOA == TOA_International && s.length() > 0 && s.charAt(0) != '+') {
            return "+" + s;
        }

        return s;
    }

    /**
     * Returns the TOA for the given dial string
     * Basically, returns TOA_International if there's a + prefix
     */

    public static int
    toaFromString(String s) {
        if (s != null && s.length() > 0 && s.charAt(0) == '+') {
            return TOA_International;
        }

        return TOA_Unknown;
    }

    /**
     *  3GPP TS 24.008 10.5.4.7
     *  Called Party BCD Number
     *
     *  See Also TS 51.011 10.5.1 "dialing number/ssc string"
     *  and TS 11.11 "10.3.1 EF adn (Abbreviated dialing numbers)"
     *
     * @param bytes the data buffer
     * @param offset should point to the TOA (aka. TON/NPI) octet after the length byte
     * @param length is the number of bytes including TOA byte
     *                and must be at least 2
     *
     * @return partial string on invalid decode
     *
     * FIXME(mkf) support alphanumeric address type
     *  currently implemented in SMSMessage.getAddress()
     */
    public static String
    calledPartyBCDToString (byte[] bytes, int offset, int length) {
        boolean prependPlus = false;
        StringBuilder ret = new StringBuilder(1 + length * 2);

        if (length < 2) {
            return "";
        }

        //Only TON field should be taken in consideration
        if ((bytes[offset] & 0xf0) == (TOA_International & 0xf0)) {
            prependPlus = true;
        }

        internalCalledPartyBCDFragmentToString(
                ret, bytes, offset + 1, length - 1);

        if (prependPlus) {
            /// M: @{
            if (ret.length() == 0) {
                // If the only thing there is a prepended plus, return ""
                return "";
            } else {
                return prependPlusToNumber(ret.toString());
            }
            /// @}
        }

        return ret.toString();
    }

    private static void
    internalCalledPartyBCDFragmentToString(
        StringBuilder sb, byte [] bytes, int offset, int length) {
        for (int i = offset ; i < length + offset ; i++) {
            byte b;
            char c;

            c = bcdToChar((byte)(bytes[i] & 0xf));

            if (c == 0) {
                return;
            }
            sb.append(c);

            // FIXME(mkf) TS 23.040 9.1.2.3 says
            // "if a mobile receives 1111 in a position prior to
            // the last semi-octet then processing shall commence with
            // the next semi-octet and the intervening
            // semi-octet shall be ignored"
            // How does this jive with 24.008 10.5.4.7

            b = (byte)((bytes[i] >> 4) & 0xf);

            if (b == 0xf && i + 1 == length + offset) {
                //ignore final 0xf
                break;
            }

            c = bcdToChar(b);
            if (c == 0) {
                return;
            }

            sb.append(c);
        }

    }

    /**
     * Like calledPartyBCDToString, but field does not start with a
     * TOA byte. For example: SIM ADN extension fields
     */

    public static String
    calledPartyBCDFragmentToString(byte [] bytes, int offset, int length) {
        StringBuilder ret = new StringBuilder(length * 2);

        internalCalledPartyBCDFragmentToString(ret, bytes, offset, length);

        return ret.toString();
    }

    /** returns 0 on invalid value */
    private static char
    bcdToChar(byte b) {
        if (b < 0xa) {
            return (char)('0' + b);
        } else switch (b) {
            case 0xa: return '*';
            case 0xb: return '#';
            case 0xc: return PAUSE;
            case 0xd: return WILD;
            /// M: add wait for ANR @{
            case 0xe: return WAIT;
            /// @}
            default: return 0;
        }
    }

    private static int
    charToBCD(char c) {
        if (c >= '0' && c <= '9') {
            return c - '0';
        } else if (c == '*') {
            return 0xa;
        } else if (c == '#') {
            return 0xb;
        } else if (c == PAUSE) {
            return 0xc;
        } else if (c == WILD) {
            return 0xd;
        } else if (c == WAIT) {
            return 0xe;
        } else {
            throw new RuntimeException ("invalid char for BCD " + c);
        }
    }

    /**
     * Return true iff the network portion of <code>address</code> is,
     * as far as we can tell on the device, suitable for use as an SMS
     * destination address.
     */
    public static boolean isWellFormedSmsAddress(String address) {
        String networkPortion =
                PhoneNumberUtils.extractNetworkPortion(address);

        return (!(networkPortion.equals("+")
                  || TextUtils.isEmpty(networkPortion)))
               && isDialable(networkPortion);
    }

    public static boolean isGlobalPhoneNumber(String phoneNumber) {
        if (TextUtils.isEmpty(phoneNumber)) {
            return false;
        }

        Matcher match = GLOBAL_PHONE_NUMBER_PATTERN.matcher(phoneNumber);
        return match.matches();
    }

    private static boolean isDialable(String address) {
        for (int i = 0, count = address.length(); i < count; i++) {
            if (!isDialable(address.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private static boolean isNonSeparator(String address) {
        for (int i = 0, count = address.length(); i < count; i++) {
            if (!isNonSeparator(address.charAt(i))) {
                return false;
            }
        }
        return true;
    }
    /**
     * Note: calls extractNetworkPortion(), so do not use for
     * SIM EF[ADN] style records
     *
     * Returns null if network portion is empty.
     */
    public static byte[]
    networkPortionToCalledPartyBCD(String s) {
        String networkPortion = extractNetworkPortion(s);
        return numberToCalledPartyBCDHelper(networkPortion, false);
    }

    /**
     * Same as {@link #networkPortionToCalledPartyBCD}, but includes a
     * one-byte length prefix.
     */
    public static byte[]
    networkPortionToCalledPartyBCDWithLength(String s) {
        String networkPortion = extractNetworkPortion(s);
        return numberToCalledPartyBCDHelper(networkPortion, true);
    }

    /**
     * Convert a dialing number to BCD byte array
     *
     * @param number dialing number string
     *        if the dialing number starts with '+', set to international TOA
     * @return BCD byte array
     */
    public static byte[]
    numberToCalledPartyBCD(String number) {
        return numberToCalledPartyBCDHelper(number, false);
    }

    /**
     * If includeLength is true, prepend a one-byte length value to
     * the return array.
     */
    private static byte[]
    numberToCalledPartyBCDHelper(String number, boolean includeLength) {
        int numberLenReal = number.length();
        int numberLenEffective = numberLenReal;
        boolean hasPlus = number.indexOf('+') != -1;
        if (hasPlus) numberLenEffective--;

        if (numberLenEffective == 0) return null;

        int resultLen = (numberLenEffective + 1) / 2;  // Encoded numbers require only 4 bits each.
        int extraBytes = 1;                            // Prepended TOA byte.
        if (includeLength) extraBytes++;               // Optional prepended length byte.
        resultLen += extraBytes;

        byte[] result = new byte[resultLen];

        int digitCount = 0;
        for (int i = 0; i < numberLenReal; i++) {
            char c = number.charAt(i);
            if (c == '+') continue;
            int shift = ((digitCount & 0x01) == 1) ? 4 : 0;
            result[extraBytes + (digitCount >> 1)] |= (byte)((charToBCD(c) & 0x0F) << shift);
            digitCount++;
        }

        // 1-fill any trailing odd nibble/quartet.
        if ((digitCount & 0x01) == 1) result[extraBytes + (digitCount >> 1)] |= 0xF0;

        int offset = 0;
        if (includeLength) result[offset++] = (byte)(resultLen - 1);
        result[offset] = (byte)(hasPlus ? TOA_International : TOA_Unknown);

        return result;
    }

    //================ Number formatting =========================

    /** The current locale is unknown, look for a country code or don't format */
    public static final int FORMAT_UNKNOWN = 0;
    /** NANP formatting */
    public static final int FORMAT_NANP = 1;
    /** Japanese formatting */
    public static final int FORMAT_JAPAN = 2;

    /** List of country codes for countries that use the NANP */
    private static final String[] NANP_COUNTRIES = new String[] {
        "US", // United States
        "CA", // Canada
        "AS", // American Samoa
        "AI", // Anguilla
        "AG", // Antigua and Barbuda
        "BS", // Bahamas
        "BB", // Barbados
        "BM", // Bermuda
        "VG", // British Virgin Islands
        "KY", // Cayman Islands
        "DM", // Dominica
        "DO", // Dominican Republic
        "GD", // Grenada
        "GU", // Guam
        "JM", // Jamaica
        "PR", // Puerto Rico
        "MS", // Montserrat
        "MP", // Northern Mariana Islands
        "KN", // Saint Kitts and Nevis
        "LC", // Saint Lucia
        "VC", // Saint Vincent and the Grenadines
        "TT", // Trinidad and Tobago
        "TC", // Turks and Caicos Islands
        "VI", // U.S. Virgin Islands
    };

    private static final String KOREA_ISO_COUNTRY_CODE = "KR";

    /**
     * Breaks the given number down and formats it according to the rules
     * for the country the number is from.
     *
     * @param source The phone number to format
     * @return A locally acceptable formatting of the input, or the raw input if
     *  formatting rules aren't known for the number
     *
     * @deprecated Use link #formatNumber(String phoneNumber, String defaultCountryIso) instead
     */
    @Deprecated
    public static String formatNumber(String source) {
        SpannableStringBuilder text = new SpannableStringBuilder(source);
        formatNumber(text, getFormatTypeForLocale(Locale.getDefault()));
        return text.toString();
    }

    /**
     * Formats the given number with the given formatting type. Currently
     * {@link #FORMAT_NANP} and {@link #FORMAT_JAPAN} are supported as a formating type.
     *
     * @param source the phone number to format
     * @param defaultFormattingType The default formatting rules to apply if the number does
     * not begin with +[country_code]
     * @return The phone number formatted with the given formatting type.
     *
     * @hide
     * @deprecated Use link #formatNumber(String phoneNumber, String defaultCountryIso) instead
     */
    @Deprecated
    public static String formatNumber(String source, int defaultFormattingType) {
        SpannableStringBuilder text = new SpannableStringBuilder(source);
        formatNumber(text, defaultFormattingType);
        return text.toString();
    }

    /**
     * Returns the phone number formatting type for the given locale.
     *
     * @param locale The locale of interest, usually {@link Locale#getDefault()}
     * @return The formatting type for the given locale, or FORMAT_UNKNOWN if the formatting
     * rules are not known for the given locale
     *
     * @deprecated Use link #formatNumber(String phoneNumber, String defaultCountryIso) instead
     */
    @Deprecated
    public static int getFormatTypeForLocale(Locale locale) {
        String country = locale.getCountry();

        return getFormatTypeFromCountryCode(country);
    }

    /**
     * Formats a phone number in-place. Currently {@link #FORMAT_JAPAN} and {@link #FORMAT_NANP}
     * is supported as a second argument.
     *
     * @param text The number to be formatted, will be modified with the formatting
     * @param defaultFormattingType The default formatting rules to apply if the number does
     * not begin with +[country_code]
     *
     * @deprecated Use link #formatNumber(String phoneNumber, String defaultCountryIso) instead
     */
    @Deprecated
    public static void formatNumber(Editable text, int defaultFormattingType) {
        int formatType = defaultFormattingType;

        if (text.length() > 2 && text.charAt(0) == '+') {
            if (text.charAt(1) == '1') {
                formatType = FORMAT_NANP;
            } else if (text.length() >= 3 && text.charAt(1) == '8'
                && text.charAt(2) == '1') {
                formatType = FORMAT_JAPAN;
            } else {
                formatType = FORMAT_UNKNOWN;
            }
        }

        switch (formatType) {
            case FORMAT_NANP:
                formatNanpNumber(text);
                return;
            case FORMAT_JAPAN:
                formatJapaneseNumber(text);
                return;
            case FORMAT_UNKNOWN:
                removeDashes(text);
                return;
        }
    }

    private static final int NANP_STATE_DIGIT = 1;
    private static final int NANP_STATE_PLUS = 2;
    private static final int NANP_STATE_ONE = 3;
    private static final int NANP_STATE_DASH = 4;

    /**
     * Formats a phone number in-place using the NANP formatting rules. Numbers will be formatted
     * as:
     *
     * <p><code>
     * xxxxx
     * xxx-xxxx
     * xxx-xxx-xxxx
     * 1-xxx-xxx-xxxx
     * +1-xxx-xxx-xxxx
     * </code></p>
     *
     * @param text the number to be formatted, will be modified with the formatting
     *
     * @deprecated Use link #formatNumber(String phoneNumber, String defaultCountryIso) instead
     */
    @Deprecated
    public static void formatNanpNumber(Editable text) {
        int length = text.length();
        if (length > "+1-nnn-nnn-nnnn".length()) {
            // The string is too long to be formatted
            return;
        } else if (length <= 5) {
            // The string is either a shortcode or too short to be formatted
            return;
        }

        CharSequence saved = text.subSequence(0, length);

        // Strip the dashes first, as we're going to add them back
        removeDashes(text);
        length = text.length();

        // When scanning the number we record where dashes need to be added,
        // if they're non-0 at the end of the scan the dashes will be added in
        // the proper places.
        int dashPositions[] = new int[3];
        int numDashes = 0;

        int state = NANP_STATE_DIGIT;
        int numDigits = 0;
        for (int i = 0; i < length; i++) {
            char c = text.charAt(i);
            switch (c) {
                case '1':
                    if (numDigits == 0 || state == NANP_STATE_PLUS) {
                        state = NANP_STATE_ONE;
                        break;
                    }
                    // fall through
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                case '0':
                    if (state == NANP_STATE_PLUS) {
                        // Only NANP number supported for now
                        text.replace(0, length, saved);
                        return;
                    } else if (state == NANP_STATE_ONE) {
                        // Found either +1 or 1, follow it up with a dash
                        dashPositions[numDashes++] = i;
                    } else if (state != NANP_STATE_DASH && (numDigits == 3 || numDigits == 6)) {
                        // Found a digit that should be after a dash that isn't
                        dashPositions[numDashes++] = i;
                    }
                    state = NANP_STATE_DIGIT;
                    numDigits++;
                    break;

                case '-':
                    state = NANP_STATE_DASH;
                    break;

                case '+':
                    if (i == 0) {
                        // Plus is only allowed as the first character
                        state = NANP_STATE_PLUS;
                        break;
                    }
                    // Fall through
                default:
                    // Unknown character, bail on formatting
                    text.replace(0, length, saved);
                    return;
            }
        }

        if (numDigits == 7) {
            // With 7 digits we want xxx-xxxx, not xxx-xxx-x
            numDashes--;
        }

        // Actually put the dashes in place
        for (int i = 0; i < numDashes; i++) {
            int pos = dashPositions[i];
            text.replace(pos + i, pos + i, "-");
        }

        // Remove trailing dashes
        int len = text.length();
        while (len > 0) {
            if (text.charAt(len - 1) == '-') {
                text.delete(len - 1, len);
                len--;
            } else {
                break;
            }
        }
    }

    /**
     * Formats a phone number in-place using the Japanese formatting rules.
     * Numbers will be formatted as:
     *
     * <p><code>
     * 03-xxxx-xxxx
     * 090-xxxx-xxxx
     * 0120-xxx-xxx
     * +81-3-xxxx-xxxx
     * +81-90-xxxx-xxxx
     * </code></p>
     *
     * @param text the number to be formatted, will be modified with
     * the formatting
     *
     * @deprecated Use link #formatNumber(String phoneNumber, String defaultCountryIso) instead
     */
    @Deprecated
    public static void formatJapaneseNumber(Editable text) {
        JapanesePhoneNumberFormatter.format(text);
    }

    /**
     * Removes all dashes from the number.
     *
     * @param text the number to clear from dashes
     */
    private static void removeDashes(Editable text) {
        int p = 0;
        while (p < text.length()) {
            if (text.charAt(p) == '-') {
                text.delete(p, p + 1);
           } else {
                p++;
           }
        }
    }

    /**
     * Formats the specified {@code phoneNumber} to the E.164 representation.
     *
     * @param phoneNumber the phone number to format.
     * @param defaultCountryIso the ISO 3166-1 two letters country code.
     * @return the E.164 representation, or null if the given phone number is not valid.
     */
    public static String formatNumberToE164(String phoneNumber, String defaultCountryIso) {
        return formatNumberInternal(phoneNumber, defaultCountryIso, PhoneNumberFormat.E164);
    }

    /**
     * Formats the specified {@code phoneNumber} to the RFC3966 representation.
     *
     * @param phoneNumber the phone number to format.
     * @param defaultCountryIso the ISO 3166-1 two letters country code.
     * @return the RFC3966 representation, or null if the given phone number is not valid.
     */
    public static String formatNumberToRFC3966(String phoneNumber, String defaultCountryIso) {
        return formatNumberInternal(phoneNumber, defaultCountryIso, PhoneNumberFormat.RFC3966);
    }

    /**
     * Formats the raw phone number (string) using the specified {@code formatIdentifier}.
     * <p>
     * The given phone number must have an area code and could have a country code.
     * <p>
     * The defaultCountryIso is used to validate the given number and generate the formatted number
     * if the specified number doesn't have a country code.
     *
     * @param rawPhoneNumber The phone number to format.
     * @param defaultCountryIso The ISO 3166-1 two letters country code.
     * @param formatIdentifier The (enum) identifier of the desired format.
     * @return the formatted representation, or null if the specified number is not valid.
     */
    private static String formatNumberInternal(
            String rawPhoneNumber, String defaultCountryIso, PhoneNumberFormat formatIdentifier) {

        PhoneNumberUtil util = PhoneNumberUtil.getInstance();
        try {
            PhoneNumber phoneNumber = util.parse(rawPhoneNumber, defaultCountryIso);
            if (util.isValidNumber(phoneNumber)) {
                /// M: @{
                if (formatIdentifier == PhoneNumberFormat.RFC3966) {
                    String postDial = extractPostDialPortion(rawPhoneNumber);
                    if (postDial != null && postDial.length() > 0) {
                        phoneNumber = new PhoneNumber().mergeFrom(phoneNumber)
                                .setExtension(postDial.substring(1));
                    }
                }
                /// @}
                return util.format(phoneNumber, formatIdentifier);
            }
        } catch (NumberParseException ignored) { }

        return null;
    }

    /**
     * Format a phone number.
     * <p>
     * If the given number doesn't have the country code, the phone will be
     * formatted to the default country's convention.
     *
     * @param phoneNumber
     *            the number to be formatted.
     * @param defaultCountryIso
     *            the ISO 3166-1 two letters country code whose convention will
     *            be used if the given number doesn't have the country code.
     * @return the formatted number, or null if the given number is not valid.
     */
    public static String formatNumber(String phoneNumber, String defaultCountryIso) {
        // Do not attempt to format numbers that start with a hash or star symbol.
        if (phoneNumber.startsWith("#") || phoneNumber.startsWith("*")) {
            return phoneNumber;
        }

        PhoneNumberUtil util = PhoneNumberUtil.getInstance();
        String result = null;
        try {
            PhoneNumber pn = util.parseAndKeepRawInput(phoneNumber, defaultCountryIso);
            /**
             * Need to reformat any local Korean phone numbers (when the user is in Korea) with
             * country code to corresponding national format which would replace the leading
             * +82 with 0.
             */
            if (KOREA_ISO_COUNTRY_CODE.equals(defaultCountryIso) &&
                    (pn.getCountryCode() == util.getCountryCodeForRegion(KOREA_ISO_COUNTRY_CODE)) &&
                    (pn.getCountryCodeSource() ==
                            PhoneNumber.CountryCodeSource.FROM_NUMBER_WITH_PLUS_SIGN)) {
                result = util.format(pn, PhoneNumberUtil.PhoneNumberFormat.NATIONAL);
            } else {
                result = util.formatInOriginalFormat(pn, defaultCountryIso);
            }
        } catch (NumberParseException e) {
        }
        return result;
    }

    /**
     * Format the phone number only if the given number hasn't been formatted.
     * <p>
     * The number which has only dailable character is treated as not being
     * formatted.
     *
     * @param phoneNumber
     *            the number to be formatted.
     * @param phoneNumberE164
     *            the E164 format number whose country code is used if the given
     *            phoneNumber doesn't have the country code.
     * @param defaultCountryIso
     *            the ISO 3166-1 two letters country code whose convention will
     *            be used if the phoneNumberE164 is null or invalid, or if phoneNumber
     *            contains IDD.
     * @return the formatted number if the given number has been formatted,
     *            otherwise, return the given number.
     */
    public static String formatNumber(
            String phoneNumber, String phoneNumberE164, String defaultCountryIso) {
        int len = phoneNumber.length();
        for (int i = 0; i < len; i++) {
            if (!isDialable(phoneNumber.charAt(i))) {
                return phoneNumber;
            }
        }
        PhoneNumberUtil util = PhoneNumberUtil.getInstance();
        // Get the country code from phoneNumberE164
        if (phoneNumberE164 != null && phoneNumberE164.length() >= 2
                && phoneNumberE164.charAt(0) == '+') {
            try {
                // The number to be parsed is in E164 format, so the default region used doesn't
                // matter.
                PhoneNumber pn = util.parse(phoneNumberE164, "ZZ");
                String regionCode = util.getRegionCodeForNumber(pn);
                if (!TextUtils.isEmpty(regionCode) &&
                    // This makes sure phoneNumber doesn't contain an IDD
                    normalizeNumber(phoneNumber).indexOf(phoneNumberE164.substring(1)) <= 0) {
                    defaultCountryIso = regionCode;
                }
            } catch (NumberParseException e) {
            }
        }
        String result = formatNumber(phoneNumber, defaultCountryIso);
        return result != null ? result : phoneNumber;
    }

    /**
     * Normalize a phone number by removing the characters other than digits. If
     * the given number has keypad letters, the letters will be converted to
     * digits first.
     *
     * @param phoneNumber the number to be normalized.
     * @return the normalized number.
     */
    public static String normalizeNumber(String phoneNumber) {
        if (TextUtils.isEmpty(phoneNumber)) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        int len = phoneNumber.length();
        for (int i = 0; i < len; i++) {
            char c = phoneNumber.charAt(i);
            // Character.digit() supports ASCII and Unicode digits (fullwidth, Arabic-Indic, etc.)
            int digit = Character.digit(c, 10);
            if (digit != -1) {
                sb.append(digit);
            } else if (sb.length() == 0 && c == '+') {
                sb.append(c);
            } else if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
                return normalizeNumber(PhoneNumberUtils.convertKeypadLettersToDigits(phoneNumber));
            }
        }
        return sb.toString();
    }

    /**
     * Replaces all unicode(e.g. Arabic, Persian) digits with their decimal digit equivalents.
     *
     * @param number the number to perform the replacement on.
     * @return the replaced number.
     */
    public static String replaceUnicodeDigits(String number) {
        StringBuilder normalizedDigits = new StringBuilder(number.length());
        for (char c : number.toCharArray()) {
            int digit = Character.digit(c, 10);
            if (digit != -1) {
                normalizedDigits.append(digit);
            } else {
                normalizedDigits.append(c);
            }
        }
        return normalizedDigits.toString();
    }

    // Three and four digit phone numbers for either special services,
    // or 3-6 digit addresses from the network (eg carrier-originated SMS messages) should
    // not match.
    //
    // This constant used to be 5, but SMS short codes has increased in length and
    // can be easily 6 digits now days. Most countries have SMS short code length between
    // 3 to 6 digits. The exceptions are
    //
    // Australia: Short codes are six or eight digits in length, starting with the prefix "19"
    //            followed by an additional four or six digits and two.
    // Czech Republic: Codes are seven digits in length for MO and five (not billed) or
    //            eight (billed) for MT direction
    //
    // see http://en.wikipedia.org/wiki/Short_code#Regional_differences for reference
    //
    // However, in order to loose match 650-555-1212 and 555-1212, we need to set the min match
    // to 7.
    static final int MIN_MATCH = 7;

    /**
     * Checks a given number against the list of
     * emergency numbers provided by the RIL and SIM card.
     *
     * @param number the number to look up.
     * @return true if the number is in the list of emergency numbers
     *         listed in the RIL / SIM, otherwise return false.
     */
    public static boolean isEmergencyNumber(String number) {
        return isEmergencyNumber(getDefaultVoiceSubId(), number);
    }

    /**
     * Checks a given number against the list of
     * emergency numbers provided by the RIL and SIM card.
     *
     * @param subId the subscription id of the SIM.
     * @param number the number to look up.
     * @return true if the number is in the list of emergency numbers
     *         listed in the RIL / SIM, otherwise return false.
     * @hide
     */
    public static boolean isEmergencyNumber(int subId, String number) {
        // Return true only if the specified number *exactly* matches
        // one of the emergency numbers listed by the RIL / SIM.
        return isEmergencyNumberInternal(subId, number, true /* useExactMatch */);
    }

    /**
     * Checks if given number might *potentially* result in
     * a call to an emergency service on the current network.
     *
     * Specifically, this method will return true if the specified number
     * is an emergency number according to the list managed by the RIL or
     * SIM, *or* if the specified number simply starts with the same
     * digits as any of the emergency numbers listed in the RIL / SIM.
     *
     * This method is intended for internal use by the phone app when
     * deciding whether to allow ACTION_CALL intents from 3rd party apps
     * (where we're required to *not* allow emergency calls to be placed.)
     *
     * @param number the number to look up.
     * @return true if the number is in the list of emergency numbers
     *         listed in the RIL / SIM, *or* if the number starts with the
     *         same digits as any of those emergency numbers.
     *
     * @hide
     */
    public static boolean isPotentialEmergencyNumber(String number) {
        return isPotentialEmergencyNumber(getDefaultVoiceSubId(), number);
    }

    /**
     * Checks if given number might *potentially* result in
     * a call to an emergency service on the current network.
     *
     * Specifically, this method will return true if the specified number
     * is an emergency number according to the list managed by the RIL or
     * SIM, *or* if the specified number simply starts with the same
     * digits as any of the emergency numbers listed in the RIL / SIM.
     *
     * This method is intended for internal use by the phone app when
     * deciding whether to allow ACTION_CALL intents from 3rd party apps
     * (where we're required to *not* allow emergency calls to be placed.)
     *
     * @param subId the subscription id of the SIM.
     * @param number the number to look up.
     * @return true if the number is in the list of emergency numbers
     *         listed in the RIL / SIM, *or* if the number starts with the
     *         same digits as any of those emergency numbers.
     * @hide
     */
    public static boolean isPotentialEmergencyNumber(int subId, String number) {
        // Check against the emergency numbers listed by the RIL / SIM,
        // and *don't* require an exact match.
        return isEmergencyNumberInternal(subId, number, false /* useExactMatch */);
    }

    /**
     * Helper function for isEmergencyNumber(String) and
     * isPotentialEmergencyNumber(String).
     *
     * @param number the number to look up.
     *
     * @param useExactMatch if true, consider a number to be an emergency
     *           number only if it *exactly* matches a number listed in
     *           the RIL / SIM.  If false, a number is considered to be an
     *           emergency number if it simply starts with the same digits
     *           as any of the emergency numbers listed in the RIL / SIM.
     *           (Setting useExactMatch to false allows you to identify
     *           number that could *potentially* result in emergency calls
     *           since many networks will actually ignore trailing digits
     *           after a valid emergency number.)
     *
     * @return true if the number is in the list of emergency numbers
     *         listed in the RIL / sim, otherwise return false.
     */
    private static boolean isEmergencyNumberInternal(String number, boolean useExactMatch) {
        return isEmergencyNumberInternal(getDefaultVoiceSubId(), number, useExactMatch);
    }

    /**
     * Helper function for isEmergencyNumber(String) and
     * isPotentialEmergencyNumber(String).
     *
     * @param subId the subscription id of the SIM.
     * @param number the number to look up.
     *
     * @param useExactMatch if true, consider a number to be an emergency
     *           number only if it *exactly* matches a number listed in
     *           the RIL / SIM.  If false, a number is considered to be an
     *           emergency number if it simply starts with the same digits
     *           as any of the emergency numbers listed in the RIL / SIM.
     *           (Setting useExactMatch to false allows you to identify
     *           number that could *potentially* result in emergency calls
     *           since many networks will actually ignore trailing digits
     *           after a valid emergency number.)
     *
     * @return true if the number is in the list of emergency numbers
     *         listed in the RIL / sim, otherwise return false.
     */
    private static boolean isEmergencyNumberInternal(int subId, String number,
            boolean useExactMatch) {
        return isEmergencyNumberInternal(subId, number, null, useExactMatch);
    }

    /**
     * Checks if a given number is an emergency number for a specific country.
     *
     * @param number the number to look up.
     * @param defaultCountryIso the specific country which the number should be checked against
     * @return if the number is an emergency number for the specific country, then return true,
     * otherwise false
     *
     * @hide
     */
    public static boolean isEmergencyNumber(String number, String defaultCountryIso) {
            return isEmergencyNumber(getDefaultVoiceSubId(), number, defaultCountryIso);
    }

    /**
     * Checks if a given number is an emergency number for a specific country.
     *
     * @param subId the subscription id of the SIM.
     * @param number the number to look up.
     * @param defaultCountryIso the specific country which the number should be checked against
     * @return if the number is an emergency number for the specific country, then return true,
     * otherwise false
     * @hide
     */
    public static boolean isEmergencyNumber(int subId, String number, String defaultCountryIso) {
        return isEmergencyNumberInternal(subId, number,
                                         defaultCountryIso,
                                         true /* useExactMatch */);
    }

    /**
     * Checks if a given number might *potentially* result in a call to an
     * emergency service, for a specific country.
     *
     * Specifically, this method will return true if the specified number
     * is an emergency number in the specified country, *or* if the number
     * simply starts with the same digits as any emergency number for that
     * country.
     *
     * This method is intended for internal use by the phone app when
     * deciding whether to allow ACTION_CALL intents from 3rd party apps
     * (where we're required to *not* allow emergency calls to be placed.)
     *
     * @param number the number to look up.
     * @param defaultCountryIso the specific country which the number should be checked against
     * @return true if the number is an emergency number for the specific
     *         country, *or* if the number starts with the same digits as
     *         any of those emergency numbers.
     *
     * @hide
     */
    public static boolean isPotentialEmergencyNumber(String number, String defaultCountryIso) {
        return isPotentialEmergencyNumber(getDefaultVoiceSubId(), number, defaultCountryIso);
    }

    /**
     * Checks if a given number might *potentially* result in a call to an
     * emergency service, for a specific country.
     *
     * Specifically, this method will return true if the specified number
     * is an emergency number in the specified country, *or* if the number
     * simply starts with the same digits as any emergency number for that
     * country.
     *
     * This method is intended for internal use by the phone app when
     * deciding whether to allow ACTION_CALL intents from 3rd party apps
     * (where we're required to *not* allow emergency calls to be placed.)
     *
     * @param subId the subscription id of the SIM.
     * @param number the number to look up.
     * @param defaultCountryIso the specific country which the number should be checked against
     * @return true if the number is an emergency number for the specific
     *         country, *or* if the number starts with the same digits as
     *         any of those emergency numbers.
     * @hide
     */
    public static boolean isPotentialEmergencyNumber(int subId, String number,
            String defaultCountryIso) {
        return isEmergencyNumberInternal(subId, number,
                                         defaultCountryIso,
                                         false /* useExactMatch */);
    }

    /**
     * Helper function for isEmergencyNumber(String, String) and
     * isPotentialEmergencyNumber(String, String).
     *
     * @param number the number to look up.
     * @param defaultCountryIso the specific country which the number should be checked against
     * @param useExactMatch if true, consider a number to be an emergency
     *           number only if it *exactly* matches a number listed in
     *           the RIL / SIM.  If false, a number is considered to be an
     *           emergency number if it simply starts with the same digits
     *           as any of the emergency numbers listed in the RIL / SIM.
     *
     * @return true if the number is an emergency number for the specified country.
     */
    private static boolean isEmergencyNumberInternal(String number,
                                                     String defaultCountryIso,
                                                     boolean useExactMatch) {
        return isEmergencyNumberInternal(getDefaultVoiceSubId(), number, defaultCountryIso,
                useExactMatch);
    }

    /**
     * Helper function for isEmergencyNumber(String, String) and
     * isPotentialEmergencyNumber(String, String).
     *
     * @param subId the subscription id of the SIM.
     * @param number the number to look up.
     * @param defaultCountryIso the specific country which the number should be checked against
     * @param useExactMatch if true, consider a number to be an emergency
     *           number only if it *exactly* matches a number listed in
     *           the RIL / SIM.  If false, a number is considered to be an
     *           emergency number if it simply starts with the same digits
     *           as any of the emergency numbers listed in the RIL / SIM.
     *
     * @return true if the number is an emergency number for the specified country.
     * @hide
     */
    private static boolean isEmergencyNumberInternal(int subId, String number,
                                                     String defaultCountryIso,
                                                     boolean useExactMatch) {
        /// M: Support ECC retry @{
        return isEmergencyNumberExt(subId, number, defaultCountryIso, useExactMatch);
        /// @}
    }

    /**
     * Checks if a given number is an emergency number for the country that the user is in.
     *
     * @param number the number to look up.
     * @param context the specific context which the number should be checked against
     * @return true if the specified number is an emergency number for the country the user
     * is currently in.
     */
    public static boolean isLocalEmergencyNumber(Context context, String number) {
        return isLocalEmergencyNumber(context, getDefaultVoiceSubId(), number);
    }

    /**
     * Checks if a given number is an emergency number for the country that the user is in.
     *
     * @param subId the subscription id of the SIM.
     * @param number the number to look up.
     * @param context the specific context which the number should be checked against
     * @return true if the specified number is an emergency number for the country the user
     * is currently in.
     * @hide
     */
    public static boolean isLocalEmergencyNumber(Context context, int subId, String number) {
        return isLocalEmergencyNumberInternal(subId, number,
                                              context,
                                              true /* useExactMatch */);
    }

    /**
     * Checks if a given number might *potentially* result in a call to an
     * emergency service, for the country that the user is in. The current
     * country is determined using the CountryDetector.
     *
     * Specifically, this method will return true if the specified number
     * is an emergency number in the current country, *or* if the number
     * simply starts with the same digits as any emergency number for the
     * current country.
     *
     * This method is intended for internal use by the phone app when
     * deciding whether to allow ACTION_CALL intents from 3rd party apps
     * (where we're required to *not* allow emergency calls to be placed.)
     *
     * @param number the number to look up.
     * @param context the specific context which the number should be checked against
     * @return true if the specified number is an emergency number for a local country, based on the
     *              CountryDetector.
     *
     * @see android.location.CountryDetector
     * @hide
     */
    public static boolean isPotentialLocalEmergencyNumber(Context context, String number) {
        return isPotentialLocalEmergencyNumber(context, getDefaultVoiceSubId(), number);
    }

    /**
     * Checks if a given number might *potentially* result in a call to an
     * emergency service, for the country that the user is in. The current
     * country is determined using the CountryDetector.
     *
     * Specifically, this method will return true if the specified number
     * is an emergency number in the current country, *or* if the number
     * simply starts with the same digits as any emergency number for the
     * current country.
     *
     * This method is intended for internal use by the phone app when
     * deciding whether to allow ACTION_CALL intents from 3rd party apps
     * (where we're required to *not* allow emergency calls to be placed.)
     *
     * @param subId the subscription id of the SIM.
     * @param number the number to look up.
     * @param context the specific context which the number should be checked against
     * @return true if the specified number is an emergency number for a local country, based on the
     *              CountryDetector.
     *
     * @hide
     */
    public static boolean isPotentialLocalEmergencyNumber(Context context, int subId,
            String number) {
        return isLocalEmergencyNumberInternal(subId, number,
                                              context,
                                              false /* useExactMatch */);
    }

    /**
     * Helper function for isLocalEmergencyNumber() and
     * isPotentialLocalEmergencyNumber().
     *
     * @param number the number to look up.
     * @param context the specific context which the number should be checked against
     * @param useExactMatch if true, consider a number to be an emergency
     *           number only if it *exactly* matches a number listed in
     *           the RIL / SIM.  If false, a number is considered to be an
     *           emergency number if it simply starts with the same digits
     *           as any of the emergency numbers listed in the RIL / SIM.
     *
     * @return true if the specified number is an emergency number for a
     *              local country, based on the CountryDetector.
     *
     * @see android.location.CountryDetector
     * @hide
     */
    private static boolean isLocalEmergencyNumberInternal(String number,
                                                          Context context,
                                                          boolean useExactMatch) {
        return isLocalEmergencyNumberInternal(getDefaultVoiceSubId(), number, context,
                useExactMatch);
    }

    /**
     * Helper function for isLocalEmergencyNumber() and
     * isPotentialLocalEmergencyNumber().
     *
     * @param subId the subscription id of the SIM.
     * @param number the number to look up.
     * @param context the specific context which the number should be checked against
     * @param useExactMatch if true, consider a number to be an emergency
     *           number only if it *exactly* matches a number listed in
     *           the RIL / SIM.  If false, a number is considered to be an
     *           emergency number if it simply starts with the same digits
     *           as any of the emergency numbers listed in the RIL / SIM.
     *
     * @return true if the specified number is an emergency number for a
     *              local country, based on the CountryDetector.
     * @hide
     */
    private static boolean isLocalEmergencyNumberInternal(int subId, String number,
                                                          Context context,
                                                          boolean useExactMatch) {
        String countryIso;
        vlog("[DBG] isLocalEmergencyNumberInternal CountryDetector start>>>");
        CountryDetector detector = (CountryDetector) context.getSystemService(
                Context.COUNTRY_DETECTOR);
        if (detector != null && detector.detectCountry() != null) {
            countryIso = detector.detectCountry().getCountryIso();
        } else {
            Locale locale = context.getResources().getConfiguration().locale;
            countryIso = locale.getCountry();
            Rlog.w(LOG_TAG, "No CountryDetector; falling back to countryIso based on locale: "
                    + countryIso);
        }
        vlog("[DBG] isLocalEmergencyNumberInternal CountryDetector end<<<");
        return isEmergencyNumberInternal(subId, number, countryIso, useExactMatch);
    }

    /**
     * isVoiceMailNumber: checks a given number against the voicemail
     *   number provided by the RIL and SIM card. The caller must have
     *   the READ_PHONE_STATE credential.
     *
     * @param number the number to look up.
     * @return true if the number is in the list of voicemail. False
     * otherwise, including if the caller does not have the permission
     * to read the VM number.
     */
    public static boolean isVoiceMailNumber(String number) {
        return isVoiceMailNumber(SubscriptionManager.getDefaultSubscriptionId(), number);
    }

    /**
     * isVoiceMailNumber: checks a given number against the voicemail
     *   number provided by the RIL and SIM card. The caller must have
     *   the READ_PHONE_STATE credential.
     *
     * @param subId the subscription id of the SIM.
     * @param number the number to look up.
     * @return true if the number is in the list of voicemail. False
     * otherwise, including if the caller does not have the permission
     * to read the VM number.
     * @hide
     */
    public static boolean isVoiceMailNumber(int subId, String number) {
        return isVoiceMailNumber(null, subId, number);
    }

    /**
     * isVoiceMailNumber: checks a given number against the voicemail
     *   number provided by the RIL and SIM card. The caller must have
     *   the READ_PHONE_STATE credential.
     *
     * @param context a non-null {@link Context}.
     * @param subId the subscription id of the SIM.
     * @param number the number to look up.
     * @return true if the number is in the list of voicemail. False
     * otherwise, including if the caller does not have the permission
     * to read the VM number.
     * @hide
     */
    public static boolean isVoiceMailNumber(Context context, int subId, String number) {
        String vmNumber;
        try {
            final TelephonyManager tm;
            if (context == null) {
                tm = TelephonyManager.getDefault();
            } else {
                tm = TelephonyManager.from(context);
            }
            vmNumber = tm.getVoiceMailNumber(subId);
        } catch (SecurityException ex) {
            return false;
        }
        // Strip the separators from the number before comparing it
        // to the list.
        number = extractNetworkPortionAlt(number);

        // compare tolerates null so we need to make sure that we
        // don't return true when both are null.
        return !TextUtils.isEmpty(number) && compare(number, vmNumber);
    }

    /**
     * Translates any alphabetic letters (i.e. [A-Za-z]) in the
     * specified phone number into the equivalent numeric digits,
     * according to the phone keypad letter mapping described in
     * ITU E.161 and ISO/IEC 9995-8.
     *
     * @return the input string, with alpha letters converted to numeric
     *         digits using the phone keypad letter mapping.  For example,
     *         an input of "1-800-GOOG-411" will return "1-800-4664-411".
     */
    public static String convertKeypadLettersToDigits(String input) {
        if (input == null) {
            return input;
        }
        int len = input.length();
        if (len == 0) {
            return input;
        }

        char[] out = input.toCharArray();

        for (int i = 0; i < len; i++) {
            char c = out[i];
            // If this char isn't in KEYPAD_MAP at all, just leave it alone.
            out[i] = (char) KEYPAD_MAP.get(c, c);
        }

        return new String(out);
    }

    /**
     * The phone keypad letter mapping (see ITU E.161 or ISO/IEC 9995-8.)
     * TODO: This should come from a resource.
     */
    private static final SparseIntArray KEYPAD_MAP = new SparseIntArray();
    static {
        KEYPAD_MAP.put('a', '2'); KEYPAD_MAP.put('b', '2'); KEYPAD_MAP.put('c', '2');
        KEYPAD_MAP.put('A', '2'); KEYPAD_MAP.put('B', '2'); KEYPAD_MAP.put('C', '2');

        KEYPAD_MAP.put('d', '3'); KEYPAD_MAP.put('e', '3'); KEYPAD_MAP.put('f', '3');
        KEYPAD_MAP.put('D', '3'); KEYPAD_MAP.put('E', '3'); KEYPAD_MAP.put('F', '3');

        KEYPAD_MAP.put('g', '4'); KEYPAD_MAP.put('h', '4'); KEYPAD_MAP.put('i', '4');
        KEYPAD_MAP.put('G', '4'); KEYPAD_MAP.put('H', '4'); KEYPAD_MAP.put('I', '4');

        KEYPAD_MAP.put('j', '5'); KEYPAD_MAP.put('k', '5'); KEYPAD_MAP.put('l', '5');
        KEYPAD_MAP.put('J', '5'); KEYPAD_MAP.put('K', '5'); KEYPAD_MAP.put('L', '5');

        KEYPAD_MAP.put('m', '6'); KEYPAD_MAP.put('n', '6'); KEYPAD_MAP.put('o', '6');
        KEYPAD_MAP.put('M', '6'); KEYPAD_MAP.put('N', '6'); KEYPAD_MAP.put('O', '6');

        KEYPAD_MAP.put('p', '7'); KEYPAD_MAP.put('q', '7'); KEYPAD_MAP.put('r', '7'); KEYPAD_MAP.put('s', '7');
        KEYPAD_MAP.put('P', '7'); KEYPAD_MAP.put('Q', '7'); KEYPAD_MAP.put('R', '7'); KEYPAD_MAP.put('S', '7');

        KEYPAD_MAP.put('t', '8'); KEYPAD_MAP.put('u', '8'); KEYPAD_MAP.put('v', '8');
        KEYPAD_MAP.put('T', '8'); KEYPAD_MAP.put('U', '8'); KEYPAD_MAP.put('V', '8');

        KEYPAD_MAP.put('w', '9'); KEYPAD_MAP.put('x', '9'); KEYPAD_MAP.put('y', '9'); KEYPAD_MAP.put('z', '9');
        KEYPAD_MAP.put('W', '9'); KEYPAD_MAP.put('X', '9'); KEYPAD_MAP.put('Y', '9'); KEYPAD_MAP.put('Z', '9');
    }

    //================ Plus Code formatting =========================
    private static final char PLUS_SIGN_CHAR = '+';
    private static final String PLUS_SIGN_STRING = "+";
    private static final String NANP_IDP_STRING = "011";
    private static final int NANP_LENGTH = 10;

    /**
     * This function checks if there is a plus sign (+) in the passed-in dialing number.
     * If there is, it processes the plus sign based on the default telephone
     * numbering plan of the system when the phone is activated and the current
     * telephone numbering plan of the system that the phone is camped on.
     * Currently, we only support the case that the default and current telephone
     * numbering plans are North American Numbering Plan(NANP).
     *
     * The passed-in dialStr should only contain the valid format as described below,
     * 1) the 1st character in the dialStr should be one of the really dialable
     *    characters listed below
     *    ISO-LATIN characters 0-9, *, # , +
     * 2) the dialStr should already strip out the separator characters,
     *    every character in the dialStr should be one of the non separator characters
     *    listed below
     *    ISO-LATIN characters 0-9, *, # , +, WILD, WAIT, PAUSE
     *
     * Otherwise, this function returns the dial string passed in
     *
     * @param dialStr the original dial string
     * @return the converted dial string if the current/default countries belong to NANP,
     * and if there is the "+" in the original dial string. Otherwise, the original dial
     * string returns.
     *
     * This API is for CDMA only
     *
     * @hide TODO: pending API Council approval
     */
    public static String cdmaCheckAndProcessPlusCode(String dialStr) {
        /// M: Support plus code @{
        String result = preProcessPlusCode(dialStr);
        if (result != null && !result.equals(dialStr)) {
            return result;
        }
        /// @}
        if (!TextUtils.isEmpty(dialStr)) {
            if (isReallyDialable(dialStr.charAt(0)) &&
                isNonSeparator(dialStr)) {
                String currIso = TelephonyManager.getDefault().getNetworkCountryIso();
                String defaultIso = TelephonyManager.getDefault().getSimCountryIso();
                if (!TextUtils.isEmpty(currIso) && !TextUtils.isEmpty(defaultIso)) {
                    return cdmaCheckAndProcessPlusCodeByNumberFormat(dialStr,
                            getFormatTypeFromCountryCode(currIso),
                            getFormatTypeFromCountryCode(defaultIso));
                }
            }
        }
        return dialStr;
    }

    /**
     * Process phone number for CDMA, converting plus code using the home network number format.
     * This is used for outgoing SMS messages.
     *
     * @param dialStr the original dial string
     * @return the converted dial string
     * @hide for internal use
     */
    public static String cdmaCheckAndProcessPlusCodeForSms(String dialStr) {
        /// M: Support plus code @{
        String result = preProcessPlusCodeForSms(dialStr);
        if (result != null && !result.equals(dialStr)) {
            return result;
        }
        /// @}

        if (!TextUtils.isEmpty(dialStr)) {
            if (isReallyDialable(dialStr.charAt(0)) && isNonSeparator(dialStr)) {
                String defaultIso = TelephonyManager.getDefault().getSimCountryIso();
                if (!TextUtils.isEmpty(defaultIso)) {
                    int format = getFormatTypeFromCountryCode(defaultIso);
                    return cdmaCheckAndProcessPlusCodeByNumberFormat(dialStr, format, format);
                }
            }
        }
        return dialStr;
    }

    /**
     * This function should be called from checkAndProcessPlusCode only
     * And it is used for test purpose also.
     *
     * It checks the dial string by looping through the network portion,
     * post dial portion 1, post dial porting 2, etc. If there is any
     * plus sign, then process the plus sign.
     * Currently, this function supports the plus sign conversion within NANP only.
     * Specifically, it handles the plus sign in the following ways:
     * 1)+1NANP,remove +, e.g.
     *   +18475797000 is converted to 18475797000,
     * 2)+NANP or +non-NANP Numbers,replace + with the current NANP IDP, e.g,
     *   +8475797000 is converted to 0118475797000,
     *   +11875767800 is converted to 01111875767800
     * 3)+1NANP in post dial string(s), e.g.
     *   8475797000;+18475231753 is converted to 8475797000;18475231753
     *
     *
     * @param dialStr the original dial string
     * @param currFormat the numbering system of the current country that the phone is camped on
     * @param defaultFormat the numbering system of the country that the phone is activated on
     * @return the converted dial string if the current/default countries belong to NANP,
     * and if there is the "+" in the original dial string. Otherwise, the original dial
     * string returns.
     *
     * @hide
     */
    public static String
    cdmaCheckAndProcessPlusCodeByNumberFormat(String dialStr,int currFormat,int defaultFormat) {
        String retStr = dialStr;

        boolean useNanp = (currFormat == defaultFormat) && (currFormat == FORMAT_NANP);

        // Checks if the plus sign character is in the passed-in dial string
        if (dialStr != null &&
            dialStr.lastIndexOf(PLUS_SIGN_STRING) != -1) {

            // Handle case where default and current telephone numbering plans are NANP.
            String postDialStr = null;
            String tempDialStr = dialStr;

            // Sets the retStr to null since the conversion will be performed below.
            retStr = null;
            if (DBG) log("checkAndProcessPlusCode,dialStr=" + dialStr);
            // This routine is to process the plus sign in the dial string by loop through
            // the network portion, post dial portion 1, post dial portion 2... etc. if
            // applied
            do {
                String networkDialStr;
                // Format the string based on the rules for the country the number is from,
                // and the current country the phone is camped
                if (useNanp) {
                    networkDialStr = extractNetworkPortion(tempDialStr);
                } else  {
                    networkDialStr = extractNetworkPortionAlt(tempDialStr);

                }

                networkDialStr = processPlusCode(networkDialStr, useNanp);

                // Concatenates the string that is converted from network portion
                if (!TextUtils.isEmpty(networkDialStr)) {
                    if (retStr == null) {
                        retStr = networkDialStr;
                    } else {
                        retStr = retStr.concat(networkDialStr);
                    }
                } else {
                    // This should never happen since we checked the if dialStr is null
                    // and if it contains the plus sign in the beginning of this function.
                    // The plus sign is part of the network portion.
                    Rlog.e("checkAndProcessPlusCode: null newDialStr", networkDialStr);
                    return dialStr;
                }
                postDialStr = extractPostDialPortion(tempDialStr);
                if (!TextUtils.isEmpty(postDialStr)) {
                    int dialableIndex = findDialableIndexFromPostDialStr(postDialStr);

                    // dialableIndex should always be greater than 0
                    if (dialableIndex >= 1) {
                        retStr = appendPwCharBackToOrigDialStr(dialableIndex,
                                 retStr,postDialStr);
                        // Skips the P/W character, extracts the dialable portion
                        tempDialStr = postDialStr.substring(dialableIndex);
                    } else {
                        // Non-dialable character such as P/W should not be at the end of
                        // the dial string after P/W processing in GsmCdmaConnection.java
                        // Set the postDialStr to "" to break out of the loop
                        if (dialableIndex < 0) {
                            postDialStr = "";
                        }
                        Rlog.e("wrong postDialStr=", postDialStr);
                    }
                }
                if (DBG) log("checkAndProcessPlusCode,postDialStr=" + postDialStr);
            } while (!TextUtils.isEmpty(postDialStr) && !TextUtils.isEmpty(tempDialStr));
        }
        return retStr;
    }

    /**
     * Wrap the supplied {@code CharSequence} with a {@code TtsSpan}, annotating it as
     * containing a phone number in its entirety.
     *
     * @param phoneNumber A {@code CharSequence} the entirety of which represents a phone number.
     * @return A {@code CharSequence} with appropriate annotations.
     */
    public static CharSequence createTtsSpannable(CharSequence phoneNumber) {
        if (phoneNumber == null) {
            return null;
        }
        Spannable spannable = Spannable.Factory.getInstance().newSpannable(phoneNumber);
        PhoneNumberUtils.addTtsSpan(spannable, 0, spannable.length());
        return spannable;
    }

    /**
     * Attach a {@link TtsSpan} to the supplied {@code Spannable} at the indicated location,
     * annotating that location as containing a phone number.
     *
     * @param s A {@code Spannable} to annotate.
     * @param start The starting character position of the phone number in {@code s}.
     * @param endExclusive The position after the ending character in the phone number {@code s}.
     */
    public static void addTtsSpan(Spannable s, int start, int endExclusive) {
        s.setSpan(createTtsSpan(s.subSequence(start, endExclusive).toString()),
                start,
                endExclusive,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    /**
     * Wrap the supplied {@code CharSequence} with a {@code TtsSpan}, annotating it as
     * containing a phone number in its entirety.
     *
     * @param phoneNumber A {@code CharSequence} the entirety of which represents a phone number.
     * @return A {@code CharSequence} with appropriate annotations.
     * @deprecated Renamed {@link #createTtsSpannable}.
     *
     * @hide
     */
    @Deprecated
    public static CharSequence ttsSpanAsPhoneNumber(CharSequence phoneNumber) {
        return createTtsSpannable(phoneNumber);
    }

    /**
     * Attach a {@link TtsSpan} to the supplied {@code Spannable} at the indicated location,
     * annotating that location as containing a phone number.
     *
     * @param s A {@code Spannable} to annotate.
     * @param start The starting character position of the phone number in {@code s}.
     * @param end The ending character position of the phone number in {@code s}.
     *
     * @deprecated Renamed {@link #addTtsSpan}.
     *
     * @hide
     */
    @Deprecated
    public static void ttsSpanAsPhoneNumber(Spannable s, int start, int end) {
        addTtsSpan(s, start, end);
    }

    /**
     * Create a {@code TtsSpan} for the supplied {@code String}.
     *
     * @param phoneNumberString A {@code String} the entirety of which represents a phone number.
     * @return A {@code TtsSpan} for {@param phoneNumberString}.
     */
    public static TtsSpan createTtsSpan(String phoneNumberString) {
        if (phoneNumberString == null) {
            return null;
        }

        // Parse the phone number
        final PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
        PhoneNumber phoneNumber = null;
        try {
            // Don't supply a defaultRegion so this fails for non-international numbers because
            // we don't want to TalkBalk to read a country code (e.g. +1) if it is not already
            // present
            phoneNumber = phoneNumberUtil.parse(phoneNumberString, /* defaultRegion */ null);
        } catch (NumberParseException ignored) {
        }

        // Build a telephone tts span
        final TtsSpan.TelephoneBuilder builder = new TtsSpan.TelephoneBuilder();
        if (phoneNumber == null) {
            // Strip separators otherwise TalkBack will be silent
            // (this behavior was observed with TalkBalk 4.0.2 from their alpha channel)
            builder.setNumberParts(splitAtNonNumerics(phoneNumberString));
        } else {
            if (phoneNumber.hasCountryCode()) {
                builder.setCountryCode(Integer.toString(phoneNumber.getCountryCode()));
            }
            builder.setNumberParts(Long.toString(phoneNumber.getNationalNumber()));
        }
        return builder.build();
    }

    // Split a phone number like "+20(123)-456#" using spaces, ignoring anything that is not
    // a digit, to produce a result like "20 123 456".
    private static String splitAtNonNumerics(CharSequence number) {
        StringBuilder sb = new StringBuilder(number.length());
        for (int i = 0; i < number.length(); i++) {
            sb.append(PhoneNumberUtils.isISODigit(number.charAt(i))
                    ? number.charAt(i)
                    : " ");
        }
        // It is very important to remove extra spaces. At time of writing, any leading or trailing
        // spaces, or any sequence of more than one space, will confuse TalkBack and cause the TTS
        // span to be non-functional!
        return sb.toString().replaceAll(" +", " ").trim();
    }

    private static String getCurrentIdp(boolean useNanp) {
        String ps = null;
        if (useNanp) {
            ps = NANP_IDP_STRING;
        } else {
            // in case, there is no IDD is found, we shouldn't convert it.
            ps = SystemProperties.get(PROPERTY_OPERATOR_IDP_STRING, PLUS_SIGN_STRING);
        }
        return ps;
    }

    private static boolean isTwoToNine (char c) {
        if (c >= '2' && c <= '9') {
            return true;
        } else {
            return false;
        }
    }

    private static int getFormatTypeFromCountryCode (String country) {
        // Check for the NANP countries
        int length = NANP_COUNTRIES.length;
        for (int i = 0; i < length; i++) {
            if (NANP_COUNTRIES[i].compareToIgnoreCase(country) == 0) {
                return FORMAT_NANP;
            }
        }
        if ("jp".compareToIgnoreCase(country) == 0) {
            return FORMAT_JAPAN;
        }
        return FORMAT_UNKNOWN;
    }

    /**
     * This function checks if the passed in string conforms to the NANP format
     * i.e. NXX-NXX-XXXX, N is any digit 2-9 and X is any digit 0-9
     * @hide
     */
    public static boolean isNanp (String dialStr) {
        boolean retVal = false;
        if (dialStr != null) {
            if (dialStr.length() == NANP_LENGTH) {
                if (isTwoToNine(dialStr.charAt(0)) &&
                    isTwoToNine(dialStr.charAt(3))) {
                    retVal = true;
                    for (int i=1; i<NANP_LENGTH; i++ ) {
                        char c=dialStr.charAt(i);
                        if (!PhoneNumberUtils.isISODigit(c)) {
                            retVal = false;
                            break;
                        }
                    }
                }
            }
        } else {
            Rlog.e("isNanp: null dialStr passed in", dialStr);
        }
        return retVal;
    }

   /**
    * This function checks if the passed in string conforms to 1-NANP format
    */
    private static boolean isOneNanp(String dialStr) {
        boolean retVal = false;
        if (dialStr != null) {
            String newDialStr = dialStr.substring(1);
            if ((dialStr.charAt(0) == '1') && isNanp(newDialStr)) {
                retVal = true;
            }
        } else {
            Rlog.e("isOneNanp: null dialStr passed in", dialStr);
        }
        return retVal;
    }

    /**
     * Determines if the specified number is actually a URI
     * (i.e. a SIP address) rather than a regular PSTN phone number,
     * based on whether or not the number contains an "@" character.
     *
     * @hide
     * @param number
     * @return true if number contains @
     */
    public static boolean isUriNumber(String number) {
        // Note we allow either "@" or "%40" to indicate a URI, in case
        // the passed-in string is URI-escaped.  (Neither "@" nor "%40"
        // will ever be found in a legal PSTN number.)
        return number != null && (number.contains("@") || number.contains("%40"));
    }

    /**
     * @return the "username" part of the specified SIP address,
     *         i.e. the part before the "@" character (or "%40").
     *
     * @param number SIP address of the form "username@domainname"
     *               (or the URI-escaped equivalent "username%40domainname")
     * @see #isUriNumber
     *
     * @hide
     */
    public static String getUsernameFromUriNumber(String number) {
        // The delimiter between username and domain name can be
        // either "@" or "%40" (the URI-escaped equivalent.)
        int delimiterIndex = number.indexOf('@');
        if (delimiterIndex < 0) {
            delimiterIndex = number.indexOf("%40");
        }
        if (delimiterIndex < 0) {
            Rlog.w(LOG_TAG,
                  "getUsernameFromUriNumber: no delimiter found in SIP addr '" + number + "'");
            delimiterIndex = number.length();
        }
        return number.substring(0, delimiterIndex);
    }

    /**
     * Given a {@link Uri} with a {@code sip} scheme, attempts to build an equivalent {@code tel}
     * scheme {@link Uri}.  If the source {@link Uri} does not contain a valid number, or is not
     * using the {@code sip} scheme, the original {@link Uri} is returned.
     *
     * @param source The {@link Uri} to convert.
     * @return The equivalent {@code tel} scheme {@link Uri}.
     *
     * @hide
     */
    public static Uri convertSipUriToTelUri(Uri source) {
        // A valid SIP uri has the format: sip:user:password@host:port;uri-parameters?headers
        // Per RFC3261, the "user" can be a telephone number.
        // For example: sip:1650555121;phone-context=blah.com@host.com
        // In this case, the phone number is in the user field of the URI, and the parameters can be
        // ignored.
        //
        // A SIP URI can also specify a phone number in a format similar to:
        // sip:+1-212-555-1212@something.com;user=phone
        // In this case, the phone number is again in user field and the parameters can be ignored.
        // We can get the user field in these instances by splitting the string on the @, ;, or :
        // and looking at the first found item.

        String scheme = source.getScheme();

        if (!PhoneAccount.SCHEME_SIP.equals(scheme)) {
            // Not a sip URI, bail.
            return source;
        }

        String number = source.getSchemeSpecificPart();
        String numberParts[] = number.split("[@;:]");

        if (numberParts.length == 0) {
            // Number not found, bail.
            return source;
        }
        number = numberParts[0];

        return Uri.fromParts(PhoneAccount.SCHEME_TEL, number, null);
    }

    /**
     * This function handles the plus code conversion
     * If the number format is
     * 1)+1NANP,remove +,
     * 2)other than +1NANP, any + numbers,replace + with the current IDP
     */
    private static String processPlusCode(String networkDialStr, boolean useNanp) {
        String retStr = networkDialStr;

        if (DBG) log("processPlusCode, networkDialStr = " + networkDialStr
                + "for NANP = " + useNanp);
        // If there is a plus sign at the beginning of the dial string,
        // Convert the plus sign to the default IDP since it's an international number
        if (networkDialStr != null &&
            networkDialStr.charAt(0) == PLUS_SIGN_CHAR &&
            networkDialStr.length() > 1) {
            String newStr = networkDialStr.substring(1);
            // TODO: for nonNanp, should the '+' be removed if following number is country code
            if (useNanp && isOneNanp(newStr)) {
                // Remove the leading plus sign
                retStr = newStr;
            } else {
                // Replaces the plus sign with the default IDP
                retStr = networkDialStr.replaceFirst("[+]", getCurrentIdp(useNanp));
            }
        }
        if (DBG) log("processPlusCode, retStr=" + retStr);
        return retStr;
    }

    // This function finds the index of the dialable character(s)
    // in the post dial string
    private static int findDialableIndexFromPostDialStr(String postDialStr) {
        for (int index = 0;index < postDialStr.length();index++) {
             char c = postDialStr.charAt(index);
             if (isReallyDialable(c)) {
                return index;
             }
        }
        return -1;
    }

    // This function appends the non-dialable P/W character to the original
    // dial string based on the dialable index passed in
    private static String
    appendPwCharBackToOrigDialStr(int dialableIndex,String origStr, String dialStr) {
        String retStr;

        // There is only 1 P/W character before the dialable characters
        if (dialableIndex == 1) {
            StringBuilder ret = new StringBuilder(origStr);
            ret = ret.append(dialStr.charAt(0));
            retStr = ret.toString();
        } else {
            // It means more than 1 P/W characters in the post dial string,
            // appends to retStr
            String nonDigitStr = dialStr.substring(0,dialableIndex);
            retStr = origStr.concat(nonDigitStr);
        }
        return retStr;
    }

    //===== Beginning of utility methods used in compareLoosely() =====

    /**
     * Phone numbers are stored in "lookup" form in the database
     * as reversed strings to allow for caller ID lookup
     *
     * This method takes a phone number and makes a valid SQL "LIKE"
     * string that will match the lookup form
     *
     */
    /** all of a up to len must be an international prefix or
     *  separators/non-dialing digits
     */
    private static boolean
    matchIntlPrefix(String a, int len) {
        /* '([^0-9*#+pwn]\+[^0-9*#+pwn] | [^0-9*#+pwn]0(0|11)[^0-9*#+pwn] )$' */
        /*        0       1                           2 3 45               */

        int state = 0;
        for (int i = 0 ; i < len ; i++) {
            char c = a.charAt(i);

            switch (state) {
                case 0:
                    if      (c == '+') state = 1;
                    else if (c == '0') state = 2;
                    else if (isNonSeparator(c)) return false;
                break;

                case 2:
                    if      (c == '0') state = 3;
                    else if (c == '1') state = 4;
                    else if (isNonSeparator(c)) return false;
                break;

                case 4:
                    if      (c == '1') state = 5;
                    else if (isNonSeparator(c)) return false;
                break;

                default:
                    if (isNonSeparator(c)) return false;
                break;

            }
        }

        return state == 1 || state == 3 || state == 5;
    }

    /** all of 'a' up to len must be a (+|00|011)country code)
     *  We're fast and loose with the country code. Any \d{1,3} matches */
    private static boolean
    matchIntlPrefixAndCC(String a, int len) {
        /*  [^0-9*#+pwn]*(\+|0(0|11)\d\d?\d? [^0-9*#+pwn] $ */
        /*      0          1 2 3 45  6 7  8                 */

        int state = 0;
        for (int i = 0 ; i < len ; i++ ) {
            char c = a.charAt(i);

            switch (state) {
                case 0:
                    if      (c == '+') state = 1;
                    else if (c == '0') state = 2;
                    else if (isNonSeparator(c)) return false;
                break;

                case 2:
                    if      (c == '0') state = 3;
                    else if (c == '1') state = 4;
                    else if (isNonSeparator(c)) return false;
                break;

                case 4:
                    if      (c == '1') state = 5;
                    else if (isNonSeparator(c)) return false;
                break;

                case 1:
                case 3:
                case 5:
                    if      (isISODigit(c)) state = 6;
                    else if (isNonSeparator(c)) return false;
                break;

                case 6:
                case 7:
                    if      (isISODigit(c)) state++;
                    else if (isNonSeparator(c)) return false;
                break;

                default:
                    if (isNonSeparator(c)) return false;
            }
        }

        return state == 6 || state == 7 || state == 8;
    }

    /** all of 'a' up to len must match non-US trunk prefix ('0') */
    private static boolean
    matchTrunkPrefix(String a, int len) {
        boolean found;

        found = false;

        for (int i = 0 ; i < len ; i++) {
            char c = a.charAt(i);

            if (c == '0' && !found) {
                found = true;
            } else if (isNonSeparator(c)) {
                return false;
            }
        }

        return found;
    }

    //===== End of utility methods used only in compareLoosely() =====

    //===== Beginning of utility methods used only in compareStrictly() ====

    /*
     * If true, the number is country calling code.
     */
    private static final boolean COUNTRY_CALLING_CALL[] = {
        true, true, false, false, false, false, false, true, false, false,
        false, false, false, false, false, false, false, false, false, false,
        true, false, false, false, false, false, false, true, true, false,
        true, true, true, true, true, false, true, false, false, true,
        true, false, false, true, true, true, true, true, true, true,
        false, true, true, true, true, true, true, true, true, false,
        true, true, true, true, true, true, true, false, false, false,
        false, false, false, false, false, false, false, false, false, false,
        false, true, true, true, true, false, true, false, false, true,
        true, true, true, true, true, true, false, false, true, false,
    };
    private static final int CCC_LENGTH = COUNTRY_CALLING_CALL.length;

    /**
     * @return true when input is valid Country Calling Code.
     */
    private static boolean isCountryCallingCode(int countryCallingCodeCandidate) {
        return countryCallingCodeCandidate > 0 && countryCallingCodeCandidate < CCC_LENGTH &&
                COUNTRY_CALLING_CALL[countryCallingCodeCandidate];
    }

    /**
     * Returns integer corresponding to the input if input "ch" is
     * ISO-LATIN characters 0-9.
     * Returns -1 otherwise
     */
    private static int tryGetISODigit(char ch) {
        if ('0' <= ch && ch <= '9') {
            return ch - '0';
        } else {
            return -1;
        }
    }

    private static class CountryCallingCodeAndNewIndex {
        public final int countryCallingCode;
        public final int newIndex;
        public CountryCallingCodeAndNewIndex(int countryCode, int newIndex) {
            this.countryCallingCode = countryCode;
            this.newIndex = newIndex;
        }
    }

    /*
     * Note that this function does not strictly care the country calling code with
     * 3 length (like Morocco: +212), assuming it is enough to use the first two
     * digit to compare two phone numbers.
     */
    private static CountryCallingCodeAndNewIndex tryGetCountryCallingCodeAndNewIndex(
        String str, boolean acceptThailandCase) {
        // Rough regexp:
        //  ^[^0-9*#+]*((\+|0(0|11)\d\d?|166) [^0-9*#+] $
        //         0        1 2 3 45  6 7  89
        //
        // In all the states, this function ignores separator characters.
        // "166" is the special case for the call from Thailand to the US. Uguu!
        int state = 0;
        int ccc = 0;
        final int length = str.length();
        for (int i = 0 ; i < length ; i++ ) {
            char ch = str.charAt(i);
            switch (state) {
                case 0:
                    if      (ch == '+') state = 1;
                    else if (ch == '0') state = 2;
                    else if (ch == '1') {
                        if (acceptThailandCase) {
                            state = 8;
                        } else {
                            return null;
                        }
                    } else if (isDialable(ch)) {
                        return null;
                    }
                break;

                case 2:
                    if      (ch == '0') state = 3;
                    else if (ch == '1') state = 4;
                    else if (isDialable(ch)) {
                        return null;
                    }
                break;

                case 4:
                    if      (ch == '1') state = 5;
                    else if (isDialable(ch)) {
                        return null;
                    }
                break;

                case 1:
                case 3:
                case 5:
                case 6:
                case 7:
                    {
                        int ret = tryGetISODigit(ch);
                        if (ret > 0) {
                            ccc = ccc * 10 + ret;
                            if (ccc >= 100 || isCountryCallingCode(ccc)) {
                                return new CountryCallingCodeAndNewIndex(ccc, i + 1);
                            }
                            if (state == 1 || state == 3 || state == 5) {
                                state = 6;
                            } else {
                                state++;
                            }
                        } else if (isDialable(ch)) {
                            return null;
                        }
                    }
                    break;
                case 8:
                    if (ch == '6') state = 9;
                    else if (isDialable(ch)) {
                        return null;
                    }
                    break;
                case 9:
                    if (ch == '6') {
                        return new CountryCallingCodeAndNewIndex(66, i + 1);
                    } else {
                        return null;
                    }
                default:
                    return null;
            }
        }

        return null;
    }

    /**
     * Currently this function simply ignore the first digit assuming it is
     * trunk prefix. Actually trunk prefix is different in each country.
     *
     * e.g.
     * "+79161234567" equals "89161234567" (Russian trunk digit is 8)
     * "+33123456789" equals "0123456789" (French trunk digit is 0)
     *
     */
    private static int tryGetTrunkPrefixOmittedIndex(String str, int currentIndex) {
        int length = str.length();
        for (int i = currentIndex ; i < length ; i++) {
            final char ch = str.charAt(i);
            if (tryGetISODigit(ch) >= 0) {
                return i + 1;
            } else if (isDialable(ch)) {
                return -1;
            }
        }
        return -1;
    }

    /**
     * Return true if the prefix of "str" is "ignorable". Here, "ignorable" means
     * that "str" has only one digit and separator characters. The one digit is
     * assumed to be trunk prefix.
     */
    private static boolean checkPrefixIsIgnorable(final String str,
            int forwardIndex, int backwardIndex) {
        boolean trunk_prefix_was_read = false;
        while (backwardIndex >= forwardIndex) {
            if (tryGetISODigit(str.charAt(backwardIndex)) >= 0) {
                if (trunk_prefix_was_read) {
                    // More than one digit appeared, meaning that "a" and "b"
                    // is different.
                    return false;
                } else {
                    // Ignore just one digit, assuming it is trunk prefix.
                    trunk_prefix_was_read = true;
                }
            } else if (isDialable(str.charAt(backwardIndex))) {
                // Trunk prefix is a digit, not "*", "#"...
                return false;
            }
            backwardIndex--;
        }

        return true;
    }

    /**
     * Returns Default voice subscription Id.
     */
    private static int getDefaultVoiceSubId() {
        /// M: Support ECC retry @{
        return SubscriptionManager.DEFAULT_SUBSCRIPTION_ID;
        //return SubscriptionManager.getDefaultVoiceSubscriptionId();
        /// @}
    }
    //==== End of utility methods used only in compareStrictly() =====


    /*
     * The config held calling number conversion map, expected to convert to emergency number.
     */
    private static final String[] CONVERT_TO_EMERGENCY_MAP = Resources.getSystem().getStringArray(
            com.android.internal.R.array.config_convert_to_emergency_number_map);
    /**
     * Check whether conversion to emergency number is enabled
     *
     * @return {@code true} when conversion to emergency numbers is enabled,
     *         {@code false} otherwise
     *
     * @hide
     */
    public static boolean isConvertToEmergencyNumberEnabled() {
        return CONVERT_TO_EMERGENCY_MAP != null && CONVERT_TO_EMERGENCY_MAP.length > 0;
    }

    /**
     * Converts to emergency number based on the conversion map.
     * The conversion map is declared as config_convert_to_emergency_number_map.
     *
     * Make sure {@link #isConvertToEmergencyNumberEnabled} is true before calling
     * this function.
     *
     * @return The converted emergency number if the number matches conversion map,
     * otherwise original number.
     *
     * @hide
     */
    public static String convertToEmergencyNumber(String number) {
        if (TextUtils.isEmpty(number)) {
            return number;
        }

        String normalizedNumber = normalizeNumber(number);

        // The number is already emergency number. Skip conversion.
        if (isEmergencyNumber(normalizedNumber)) {
            return number;
        }

        for (String convertMap : CONVERT_TO_EMERGENCY_MAP) {
            if (DBG) log("convertToEmergencyNumber: " + convertMap);
            String[] entry = null;
            String[] filterNumbers = null;
            String convertedNumber = null;
            if (!TextUtils.isEmpty(convertMap)) {
                entry = convertMap.split(":");
            }
            if (entry != null && entry.length == 2) {
                convertedNumber = entry[1];
                if (!TextUtils.isEmpty(entry[0])) {
                    filterNumbers = entry[0].split(",");
                }
            }
            // Skip if the format of entry is invalid
            if (TextUtils.isEmpty(convertedNumber) || filterNumbers == null
                    || filterNumbers.length == 0) {
                continue;
            }

            for (String filterNumber : filterNumbers) {
                if (DBG) log("convertToEmergencyNumber: filterNumber = " + filterNumber
                        + ", convertedNumber = " + convertedNumber);
                if (!TextUtils.isEmpty(filterNumber) && filterNumber.equals(normalizedNumber)) {
                    if (DBG) log("convertToEmergencyNumber: Matched. Successfully converted to: "
                            + convertedNumber);
                    return convertedNumber;
                }
            }
        }
        return number;
    }

    /// M: @{
    private static final boolean VDBG =
            SystemProperties.get("ro.build.type").equals("eng") ? true : false;
    private static final int MAX_SIM_NUM = 4;
    private static final int MIN_MATCH_CTA = 11;
    private static int sSpecificEccCat = -1;

    private static final String[] SIM_RECORDS_PROPERTY_ECC_LIST = {
        "ril.ecclist",
        "ril.ecclist1",
        "ril.ecclist2",
        "ril.ecclist3",
    };

    private static final String[] CDMA_SIM_RECORDS_PROPERTY_ECC_LIST = {
        "ril.cdma.ecclist",
        "ril.cdma.ecclist1",
        "ril.cdma.ecclist2",
        "ril.cdma.ecclist3",
    };

    private static final String[] NETWORK_ECC_LIST = {
        "ril.ecc.service.category.list",
        "ril.ecc.service.category.list.1",
        "ril.ecc.service.category.list.2",
        "ril.ecc.service.category.list.3",
    };

    private static final String[] PROPERTY_RIL_FULL_UICC_TYPE  = {
        "gsm.ril.fulluicctype",
        "gsm.ril.fulluicctype.2",
        "gsm.ril.fulluicctype.3",
        "gsm.ril.fulluicctype.4",
    };

    private static final String[] SIM_OMH_PROPERTY = {
        "ril.cdma.card.omh",
        "ril.cdma.card.omh.1",
        "ril.cdma.card.omh.2",
        "ril.cdma.card.omh.3",
    };

    private static IPlusCodeUtils sPlusCodeUtils = null;

    private static boolean sIsCtaSupport = false;
    private static boolean sIsCtaSet = false;
    private static boolean sIsC2kSupport = false;
    private static boolean sIsOP09Support = false;

    private static EccSource sXmlEcc = null;
    private static EccSource sCtaEcc = null;
    private static EccSource sNetworkEcc = null;
    private static EccSource sSimEcc = null;
    private static EccSource sPropertyEcc = null;
    private static EccSource sOmhEcc = null;
    private static EccSource sTestEcc = null;
    private static ArrayList<EccSource> sAllEccSource = null;

    /** @hide */
    public static class EccEntry {
        public static final String ECC_LIST_PATH = "/system/vendor/etc/ecc_list.xml";
        public static final String CDMA_ECC_LIST_PATH = "/system/vendor/etc/cdma_ecc_list.xml";
        public static final String CDMA_SS_ECC_LIST_PATH
                = "/system/vendor/etc/cdma_ecc_list_ss.xml";
        public static final String ECC_LIST_PATH_CIP = "/custom/etc/ecc_list.xml";
        public static final String ECC_ENTRY_TAG = "EccEntry";
        public static final String ECC_ATTR = "Ecc";
        public static final String CATEGORY_ATTR = "Category";
        public static final String CONDITION_ATTR = "Condition";
        public static final String PLMN_ATTR = "Plmn";

        public static final String PROPERTY_PREFIX = "ro.semc.ecclist.";
        public static final String PROPERTY_COUNT = PROPERTY_PREFIX + "num";
        public static final String PROPERTY_NUMBER = PROPERTY_PREFIX + "number.";
        public static final String PROPERTY_TYPE = PROPERTY_PREFIX + "type.";
        public static final String PROPERTY_PLMN = PROPERTY_PREFIX + "plmn.";
        public static final String PROPERTY_NON_ECC = PROPERTY_PREFIX + "non_ecc.";

        public static final String[] PROPERTY_TYPE_KEY =
                {"police", "ambulance", "firebrigade", "marineguard", "mountainrescue"};
        public static final Short[] PROPERTY_TYPE_VALUE = {0x0001, 0x0002, 0x0004, 0x0008, 0x0010};

        public static final String ECC_NO_SIM = "0";
        public static final String ECC_ALWAYS = "1";
        public static final String ECC_FOR_MMI = "2";

        private String mEcc;
        private String mCategory;
        private String mCondition; // ECC_NO_SIM, ECC_ALWAYS, or ECC_FOR_MMI
        private String mPlmn;
        private String mName;

        public EccEntry() {
            mEcc = new String("");
            mCategory = new String("");
            mCondition = new String("");
            mPlmn = new String("");
        }
        public EccEntry(String name, String number) {
            mName = name;
            mEcc = number;
        }
        public void setName(String name) {
            mName = name;
        }
        public String getName() {
            return mName;
        }
        public void setEcc(String strEcc) {
            mEcc = strEcc;
        }
        public void setCategory(String strCategory) {
            mCategory = strCategory;
        }
        public void setCondition(String strCondition) {
            mCondition = strCondition;
        }
        public void setPlmn(String strPlmn) {
            mPlmn = strPlmn;
        }

        public String getEcc() {
            return mEcc;
        }
        public String getCategory() {
            return mCategory;
        }
        public String getCondition() {
            return mCondition;
        }
        public String getPlmn() {
            return mPlmn;
        }

        @Override
        public String toString() {
            return ("\n" + ECC_ATTR + "=" + getEcc() + ", " + CATEGORY_ATTR + "="
                    + getCategory() + ", " + CONDITION_ATTR + "=" + getCondition()
                    + ", " + PLMN_ATTR + "=" + getPlmn()
                    + ", name=" + getName());
        }
    }

    /** @hide */
    private static class EccSource {
        private int mPhoneType = 0;
        protected ArrayList<EccEntry> mEccList = null;
        protected ArrayList<EccEntry> mCdmaEccList = null;

        public EccSource(int phoneType) {
            mPhoneType = phoneType;
            parseEccList();
        }

        public boolean isEmergencyNumber(String number, int subId, int phoneType) {
            return false;
        }

        public boolean isMatch(String strEcc, String number) {
            String numberPlus = strEcc + '+';
            // VZW CDMA Less requirement: The device shall treat the dial string *272911
            // as a 911 call. Refer to Verizon wireless E911 for LTE only or LTE multi-mode
            // VoLTE capable devices requirement for details.
            if ("SEGTYPE3".equals(SystemProperties.get("persist.operator.seg", "SEGDEFAULT")) ||
                "SEGTYPE4".equals(SystemProperties.get("persist.operator.seg", "SEGDEFAULT"))) {
                String vzwEcc = "*272" + strEcc;
                if (strEcc.equals(number) || numberPlus.equals(number) || vzwEcc.equals(number)) {
                    return true;
                }
            } else {
                if (strEcc.equals(number) || numberPlus.equals(number)) {
                    return true;
                }
            }

            return false;
        }

        public synchronized int getServiceCategory(String number, int subId) {
            if (mEccList != null) {
                for (EccEntry eccEntry : mEccList) {
                    String ecc = eccEntry.getEcc();
                    if (isMatch(ecc, number)) {
                        log("[getServiceCategory] match customized, ECC: "
                                + ecc + ", Category= " + eccEntry.getCategory());
                        return Integer.parseInt(eccEntry.getCategory());
                    }
                }
            }
            return -1;
        }

        public synchronized void addToEccList(ArrayList<EccEntry> eccList) {
            if (mEccList != null && eccList != null) {
                for (EccEntry srcEntry : mEccList) {
                    boolean bFound = false;
                    int nIndex = 0;
                    for (EccEntry destEntry : eccList) {
                        if (srcEntry.getEcc().equals(destEntry.getEcc())) {
                            bFound = true;
                            break;
                        }
                        nIndex++;
                    }

                    if (bFound) {
                        eccList.set(nIndex, srcEntry);
                    } else {
                        eccList.add(srcEntry);
                    }
                }
            }
        }

        public synchronized void parseEccList() {
        }

        public synchronized boolean isSpecialEmergencyNumber(String number) {
            return isSpecialEmergencyNumber(SubscriptionManager.DEFAULT_SUBSCRIPTION_ID, number);
        }

        public synchronized boolean isSpecialEmergencyNumber(int subId, String number) {
            if (mEccList != null) {
                // 93MD revise for ECC list control.
                // in 93MD, MD will not maintain ECC list and all ECC are decided by AP
                // To fullfill CTA requirement, we need to dial CTA number using:
                // 1. Normal call when SIM inserted with GSM phone type (return true) and SIM ready
                //    (Not SIM locked or network locked)
                // 2. ECC when No SIM or CDMA phone. (return false)
                boolean eccApCtrl = SystemProperties.get("ro.mtk_ril_mode").equals("c6m_1rild");
                boolean isGsmPhone = TelephonyManager.getDefault().getCurrentPhoneType(subId) ==
                        PhoneConstants.PHONE_TYPE_GSM;
                boolean isGsmSimInserted = isSimInsert(subId, PhoneConstants.PHONE_TYPE_GSM);
                // In CT Volte (LTE only) mode (GSM phone), should always dial using ATDE
                boolean isCt4G = isCt4GDualModeCard(subId);
                boolean isNeedCheckSpecial = !eccApCtrl || // 90/91/92MD
                        // After 93MD
                        (isGsmPhone && isGsmSimInserted && !isCt4G && isSimReady(subId));
                dlog("[isSpecialEmergencyNumber] subId: " + subId
                        + ", number: " + number
                        + ", eccApCtrl: " + eccApCtrl
                        + ", isGsmPhone: " + isGsmPhone
                        + ", isGsmSimInserted: " + isGsmSimInserted
                        + ", isCt4G: " + isCt4G
                        + ", isNeedCheckSpecial: " + isNeedCheckSpecial);
                if (isNeedCheckSpecial) {
                    for (EccEntry eccEntry : mEccList) {
                        if (eccEntry.getCondition().equals(EccEntry.ECC_FOR_MMI)) {
                            if (isMatch(eccEntry.getEcc(), number)) {
                                dlog("[isSpecialEmergencyNumber] match customized ecc");
                                return true;
                            }
                        }
                    }
                }
            }
            dlog("[isSpecialEmergencyNumber] return false number: " + number);
            return false;
        }

        public boolean isPhoneTypeSupport(int phoneType) {
            return (mPhoneType & phoneType) == 0 ? false : true;
        }

        public boolean isSimInsert(int phoneType) {
            return isSimInsert(SubscriptionManager.DEFAULT_SUBSCRIPTION_ID, phoneType);
        }

        public boolean isSimInsert(int subId, int phoneType) {
            String strEfEccList = null;
            boolean bSIMInserted = false;
            String[] propertyList = (phoneType == PhoneConstants.PHONE_TYPE_CDMA) ?
                CDMA_SIM_RECORDS_PROPERTY_ECC_LIST : SIM_RECORDS_PROPERTY_ECC_LIST;

            vlog("[DBG] isSimInsert start>>> subId: " + subId + ", phoneType: " + phoneType);

            // DEFAULT_SUBSCRIPTION_ID, check all slot
            if (SubscriptionManager.DEFAULT_SUBSCRIPTION_ID == subId) {
                for (int i = 0; i < MAX_SIM_NUM; i++) {
                    strEfEccList = SystemProperties.get(propertyList[i]);
                    if (!TextUtils.isEmpty(strEfEccList)) {
                        bSIMInserted = true;
                        break;
                    }
                }

                // double check if CDMA card is insert or not
                if (phoneType == PhoneConstants.PHONE_TYPE_CDMA) {
                    int subIdCdma = SubscriptionManager.INVALID_SUBSCRIPTION_ID;
                    int slotId = SubscriptionManager.INVALID_SIM_SLOT_INDEX;
                    TelephonyManager tm = TelephonyManager.getDefault();
                    int simCount = tm.getSimCount();
                    int tmpSubId = SubscriptionManager.INVALID_SUBSCRIPTION_ID;
                    for (int i = 0; i < simCount; i++) {
                        tmpSubId = SubscriptionManager.getSubIdUsingPhoneId(i);
                        if (tm.getCurrentPhoneType(tmpSubId) == PhoneConstants.PHONE_TYPE_CDMA) {
                            subIdCdma = tmpSubId;
                            slotId = i;
                            break;
                        }
                    }
                    if (subIdCdma != SubscriptionManager.INVALID_SUBSCRIPTION_ID) {
                        bSIMInserted = tm.hasIccCard(slotId);
                    }

                    vlog("[isSimInsert] CDMA subId:" + subIdCdma + ", slotId:" + slotId
                            + ", bSIMInserted:" + bSIMInserted);
                }
            } else {
                int slotId = SubscriptionManager.getSlotId(subId);
                if (SubscriptionManager.isValidSlotId(slotId)) {
                    strEfEccList = SystemProperties.get(propertyList[slotId]);
                    if (!TextUtils.isEmpty(strEfEccList)) {
                        bSIMInserted = true;
                    }
                }
            }
            vlog("[DBG] isSimInsert end <<<");
            return bSIMInserted;
        }

        public static boolean isEccPlmnMatch(String strPlmn) {
            if (TextUtils.isEmpty(strPlmn)) {
                return true;
            }

            for (int i = 0; i < TelephonyManager.getDefault().getPhoneCount(); i++) {
                String strRegisteredPlmn =
                        TelephonyManager.getDefault().getNetworkOperatorForPhone(i);
                if (TextUtils.isEmpty(strRegisteredPlmn)) {
                    // Check SIM PLMN
                    String strSimPlmn =
                        TelephonyManager.getDefault().getSimOperatorNumericForPhone(i);
                    if (!TextUtils.isEmpty(strSimPlmn)) {
                        String strSimPlmnFormatted =
                                strSimPlmn.substring(0, 3) + " " + strSimPlmn.substring(3);
                        vlog("[isEccPlmnMatch] SIM PLMN ("
                                + i + "): " + strSimPlmnFormatted + ", strPlmn: " + strPlmn);
                        if (strSimPlmnFormatted.equals(strPlmn)
                                || (0 == strPlmn.substring(4).compareToIgnoreCase("FFF")
                                && strPlmn.substring(0, 3).equals(
                                strSimPlmnFormatted.substring(0, 3)))) {
                            vlog("[isEccPlmnMatch] SIM PLMN matched strPlmn: " + strPlmn);
                            return true;
                        }
                    }
                } else {
                    // Check network operator PLMN
                    String strRegisteredPlmnFormatted =
                            strRegisteredPlmn.substring(0, 3) + " " +
                            strRegisteredPlmn.substring(3);
                    vlog("[isEccPlmnMatch] PLMN ("
                            + i + "): " + strRegisteredPlmnFormatted + ", strPlmn: " + strPlmn);
                    if (strRegisteredPlmnFormatted.equals(strPlmn)
                            || (0 == strPlmn.substring(4).compareToIgnoreCase("FFF")
                            && strPlmn.substring(0, 3).equals(
                            strRegisteredPlmnFormatted.substring(0, 3)))) {
                        vlog("[isEccPlmnMatch] Network PLMN matched strPlmn: " + strPlmn);
                        return true;
                    }
                }
            }
            return false;
        }

        /* Check if SIM status is ready (Ex: not PIN locked...) */
        private boolean isSimReady(int subId) {
            String strAllSimState = SystemProperties.get(TelephonyProperties.PROPERTY_SIM_STATE);
            String strCurSimState = "";
            int slotId = SubscriptionManager.getSlotId(subId);
            if (SubscriptionManager.isValidSlotId(slotId)) {
                if ((strAllSimState != null) && (strAllSimState.length() > 0)) {
                    String values[] = strAllSimState.split(",");
                    if ((slotId >= 0) && (slotId < values.length) && (values[slotId] != null)) {
                        strCurSimState = values[slotId];
                    }
                }
            }
            dlog("[isSimReady] subId: " + subId + ", strCurSimState: " + strCurSimState);
            return strCurSimState.equals("READY") ? true : false;
        }

        private boolean isCt4GDualModeCard(int subId) {
            int slotId = SubscriptionManager.getSlotId(subId);
            if (SubscriptionManager.isValidSlotId(slotId)) {
                String cardType = SystemProperties.get(PROPERTY_RIL_FULL_UICC_TYPE[slotId]);
                if (!TextUtils.isEmpty(cardType) &&
                        (cardType.indexOf("CSIM") >= 0) && (cardType.indexOf("USIM") >= 0)) {
                    return true;
                }
            }
            return false;
        }
    }

    /** @hide */
    private static class XmlEccSource extends EccSource {
        //private ArrayList<EccEntry> mCdmaEccList = null;

        public XmlEccSource(int phoneType) {
            super(phoneType);
        }

        @Override
        public synchronized void parseEccList() {
            // Parse GSM ECC list
            mEccList = new ArrayList<EccEntry>();
            String xmlPath = EccEntry.ECC_LIST_PATH_CIP;
            File fileCheck = new File(xmlPath);
            if (!fileCheck.exists()) {
                xmlPath = EccEntry.ECC_LIST_PATH;
            }
            log("[parseEccList] Read ECC list from " + xmlPath);
            parseFromXml(xmlPath, mEccList);


            // Parse CDMA ECC list
            if (sIsC2kSupport) {
                mCdmaEccList = new ArrayList<EccEntry>();
                String cdmaXmlPath;
                // CDMA_SS_ECC_LIST_PATH don't support OP12 special ECC *911, #911
                if ("ss".equals(SystemProperties.get("persist.radio.multisim.config"))
                        && !("OP12".equals(SystemProperties.get("persist.operator.optr")))) {
                    cdmaXmlPath = EccEntry.CDMA_SS_ECC_LIST_PATH;
                } else {
                    cdmaXmlPath = EccEntry.CDMA_ECC_LIST_PATH;
                }
                log("[parseEccList] Read CDMA ECC list from " + cdmaXmlPath);
                parseFromXml(cdmaXmlPath, mCdmaEccList);
            }
            dlog("[parseEccList] GSM XML ECC list: " + mEccList);
            dlog("[parseEccList] CDMA XML ECC list: " + mCdmaEccList);
        }

        @Override
        public synchronized boolean isEmergencyNumber(String number, int subId, int phoneType) {
            ArrayList<EccEntry> eccList = (phoneType == PhoneConstants.PHONE_TYPE_CDMA) ?
                    mCdmaEccList : mEccList;
            vlog("[isEmergencyNumber] eccList: " + eccList);
            if (isSimInsert(phoneType)) {
                if (eccList != null) {
                    for (EccEntry eccEntry : eccList) {
                        if (!eccEntry.getCondition().equals(EccEntry.ECC_NO_SIM)) {
                            if (isMatch(eccEntry.getEcc(), number, eccEntry.getPlmn())) {
                                log("[isEmergencyNumber] match XML ECC (w/ SIM) for phoneType: "
                                        + phoneType);
                                return true;
                            }
                        }
                    }
                }
            } else {
                if (eccList != null) {
                    for (EccEntry eccEntry : eccList) {
                        if (isMatch(eccEntry.getEcc(), number, eccEntry.getPlmn())) {
                            log("[isEmergencyNumber] match XML ECC (w/o SIM) for phoneType: "
                                    + phoneType);
                            return true;
                        }
                    }
                }
            }
            vlog("[isEmergencyNumber] no match XML ECC for phoneType: " + phoneType);
            return false;
        }

        @Override
        public synchronized int getServiceCategory(String number, int subId) {
            if (mEccList != null) {
                for (EccEntry eccEntry : mEccList) {
                    String ecc = eccEntry.getEcc();
                    if (isMatch(ecc, number, eccEntry.getPlmn())) {
                        log("[getServiceCategory] match xml customized, ECC: "
                                + ecc + ", Category= " + eccEntry.getCategory()
                                + ", plmn: " + eccEntry.getPlmn());
                        return Integer.parseInt(eccEntry.getCategory());
                    }
                }
            }
            return -1;
        }


        public boolean isMatch(String strEcc, String number, String plmn) {
            if (isMatch(strEcc, number) && isEccPlmnMatch(plmn)) {
                return true;
            }
            return false;
        }

        private synchronized void parseFromXml(String path, ArrayList<EccEntry> eccList) {
            try {
                FileReader fileReader;
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                XmlPullParser parser = factory.newPullParser();
                if (parser == null) {
                    log("[parseFromXml] XmlPullParserFactory.newPullParser() return null");
                    return;
                }

                fileReader = new FileReader(path);

                parser.setInput(fileReader);
                int eventType = parser.getEventType();
                EccEntry record = null;
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    switch (eventType) {
                        case XmlPullParser.START_TAG:
                            if (parser.getName().equals(EccEntry.ECC_ENTRY_TAG)) {
                                record = new EccEntry();
                                int attrNum = parser.getAttributeCount();
                                for (int i = 0; i < attrNum; ++i) {
                                    String name = parser.getAttributeName(i);
                                    String value = parser.getAttributeValue(i);
                                    if (name.equals(EccEntry.ECC_ATTR)) {
                                        record.setEcc(value);
                                    } else if (name.equals(EccEntry.CATEGORY_ATTR)) {
                                        record.setCategory(value);
                                    } else if (name.equals(EccEntry.CONDITION_ATTR)) {
                                        record.setCondition(value);
                                    } else if (name.equals(EccEntry.PLMN_ATTR)) {
                                        record.setPlmn(value);
                                    }
                                }
                            }
                            break;
                        case XmlPullParser.END_TAG:
                            if (parser.getName().equals(EccEntry.ECC_ENTRY_TAG)
                                    && record != null) {
                                eccList.add(record);
                            }
                            break;
                        default:
                            break;
                    }
                    eventType = parser.next();
                }
                fileReader.close();
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /** @hide */
    private static class NetworkEccSource extends EccSource {
        public NetworkEccSource(int phoneType) {
            super(phoneType);
        }

        @Override
        public synchronized boolean isEmergencyNumber(String number, int subId, int phoneType) {
            // 3GPP spec network ECC
            if (!isPhoneTypeSupport(phoneType)) {
                return false;
            }

            String strEccCategoryList = null;
            if (subId == SubscriptionManager.DEFAULT_SUBSCRIPTION_ID) {
                // if no SUB id, query all SIM network ECC
                for (int i = 0; i < MAX_SIM_NUM; i++) {
                    strEccCategoryList = SystemProperties.get(NETWORK_ECC_LIST[i]);
                    if (!TextUtils.isEmpty(strEccCategoryList)) {
                        dlog("[isEmergencyNumber] network list [" + i
                                + "]:" + strEccCategoryList);
                        for (String strEccCategory : strEccCategoryList.split(";")) {
                            if (!strEccCategory.isEmpty()) {
                                String[] strEccCategoryAry = strEccCategory.split(",");
                                if (2 == strEccCategoryAry.length) {
                                    if (isMatch(strEccCategoryAry[0], number)) {
                                        log("[isEmergencyNumber] match network ECC for phoneType: "
                                                + phoneType);
                                        return true;
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                int slotId = SubscriptionManager.getSlotId(subId);
                if (SubscriptionManager.isValidSlotId(slotId)) {
                    strEccCategoryList = SystemProperties.get(NETWORK_ECC_LIST[slotId]);
                    if (!TextUtils.isEmpty(strEccCategoryList)) {
                        dlog("[isEmergencyNumber]ril.ecc.service.category.list["
                                + slotId + "]" + strEccCategoryList);
                        for (String strEccCategory : strEccCategoryList.split(";")) {
                            if (!strEccCategory.isEmpty()) {
                                String[] strEccCategoryAry = strEccCategory.split(",");
                                if (2 == strEccCategoryAry.length) {
                                    if (isMatch(strEccCategoryAry[0], number)) {
                                        log("[isEmergencyNumber] match network ECC for phoneType: "
                                                + phoneType);
                                        return true;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return false;
        }

        @Override
        public synchronized int getServiceCategory(String number, int subId) {
            String strEccCategoryList;
            if (subId == SubscriptionManager.DEFAULT_SUBSCRIPTION_ID) {
                // Query without SUB id, query all SIM network ECC service category
                for (int i = 0; i < MAX_SIM_NUM; i++) {
                    strEccCategoryList = SystemProperties.get(NETWORK_ECC_LIST[i]);
                    if (!TextUtils.isEmpty(strEccCategoryList)) {
                        log("[getServiceCategory] Network ECC List: "
                                + strEccCategoryList);
                        for (String strEccCategory : strEccCategoryList.split(";")) {
                            if (!strEccCategory.isEmpty()) {
                                String[] strEccCategoryAry = strEccCategory.split(",");
                                if (2 == strEccCategoryAry.length) {
                                    if (isMatch(strEccCategoryAry[0], number)) {
                                        log("[getServiceCategory] match network, "
                                                + "Ecc= " + number + ", Category= "
                                                + Integer.parseInt(strEccCategoryAry[1]));
                                        return Integer.parseInt(strEccCategoryAry[1]);
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                int slotId = SubscriptionManager.getSlotId(subId);
                if (SubscriptionManager.isValidSlotId(slotId)) {
                    strEccCategoryList = SystemProperties.get(NETWORK_ECC_LIST[slotId]);
                    if (!TextUtils.isEmpty(strEccCategoryList)) {
                        log("[getServiceCategory] Network ECC List: "
                               + strEccCategoryList);
                        for (String strEccCategory : strEccCategoryList.split(";")) {
                            if (!strEccCategory.isEmpty()) {
                                String[] strEccCategoryAry = strEccCategory.split(",");
                                if (2 == strEccCategoryAry.length) {
                                    if (isMatch(strEccCategoryAry[0], number)) {
                                        log("[getServiceCategory] match network, "
                                                + "Ecc= " + number + ", Category= "
                                                + Integer.parseInt(strEccCategoryAry[1]));
                                        return Integer.parseInt(strEccCategoryAry[1]);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            // not found
            return -1;
        }
    }

    /** @hide */
    private static class SimEccSource extends EccSource {
        public SimEccSource(int phoneType) {
            super(phoneType);
        }

        public synchronized boolean isEmergencyNumber(String number, int subId, int phoneType) {
            String strEfEccList;
            if (phoneType == PhoneConstants.PHONE_TYPE_CDMA) {
                for (int i = 0; i < MAX_SIM_NUM; i++) {
                    String numbers = SystemProperties.get(CDMA_SIM_RECORDS_PROPERTY_ECC_LIST[i]);
                    if (!TextUtils.isEmpty(numbers)) {
                        for (String emergencyNum : numbers.split(",")) {
                            if (isMatch(emergencyNum, number)) {
                                log("[isEmergencyNumber] match CDMA SIM ECC for phoneType: "
                                        + phoneType);
                                return true;
                            }
                        }
                    }
                }
            } else {
                for (int i = 0; i < MAX_SIM_NUM; i++) {
                    strEfEccList = SystemProperties.get(SIM_RECORDS_PROPERTY_ECC_LIST[i]);
                    if (!TextUtils.isEmpty(strEfEccList)) {
                        for (String strEccCategory : strEfEccList.split(";")) {
                            if (!strEccCategory.isEmpty()) {
                                String[] strEccCategoryAry = strEccCategory.split(",");
                                if (2 == strEccCategoryAry.length) {
                                    if (isMatch(strEccCategoryAry[0], number)) {
                                        log("[isEmergencyNumber] match GSM SIM ECC for phoneType: "
                                                + phoneType);
                                        return true;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return false;
        }

        @Override
        public synchronized int getServiceCategory(String number, int subId) {
            String strEccCategoryList;

            for (int i = 0; i < MAX_SIM_NUM; i++) {
                strEccCategoryList = SystemProperties.get(SIM_RECORDS_PROPERTY_ECC_LIST[i]);
                if (!TextUtils.isEmpty(strEccCategoryList)) {
                    dlog("[getServiceCategory] list[" + i + "]: " + strEccCategoryList);
                    for (String strEccCategory : strEccCategoryList.split(";")) {
                        if (!strEccCategory.isEmpty()) {
                            String[] strEccCategoryAry = strEccCategory.split(",");
                            if (2 == strEccCategoryAry.length) {
                                if (isMatch(strEccCategoryAry[0], number)) {
                                    return Integer.parseInt(strEccCategoryAry[1]);
                                }
                            }
                        }
                    }
                }
            }

            return -1;
        }
    }

    /** @hide */
    private static class CtaEccSource extends EccSource {
        private static String[] sCtaList = {"120", "122", "119", "110"};

        public CtaEccSource(int phoneType) {
            super(phoneType);
        }

        @Override
        public synchronized void parseEccList() {
            EccEntry record = null;
            mEccList = new ArrayList<EccEntry>();
            for (String emergencyNum : sCtaList) {
                record = new EccEntry();
                record.setEcc(emergencyNum);
                record.setCategory("0");
                record.setCondition(EccEntry.ECC_FOR_MMI);
                mEccList.add(record);
            }

            dlog("[parseEccList] CTA ECC list: " + mEccList);
        }

        @Override
        public synchronized boolean isEmergencyNumber(String number, int subId, int phoneType) {
            if (isPhoneTypeSupport(phoneType) && isNeedCheckCtaSet() && mEccList != null) {
                for (EccEntry eccEntry : mEccList) {
                    String ecc = eccEntry.getEcc();
                    if (isMatch(ecc, number)) {
                        log("[isEmergencyNumber] match CTA ECC for phoneType: " + phoneType);
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public synchronized void addToEccList(ArrayList<EccEntry> eccList) {
            if (isNeedCheckCtaSet()) {
                super.addToEccList(eccList);
            }
        }

        @Override
        public synchronized int getServiceCategory(String number, int subId) {
            return -1;
        }

        private boolean isNeedCheckCtaSet() {
            String isDsbpSupport = SystemProperties.get("persist.radio.mtk_dsbp_support", "0");
            if (isDsbpSupport.equals("1")) {
                // Return true for no SIM case or SIM locked (PIN/PUK/Network locked)
                if (!(isSimInsert(PhoneConstants.PHONE_TYPE_GSM)
                        || isSimInsert(PhoneConstants.PHONE_TYPE_CDMA)) ||
                        isSimLocked()) {
                    vlog("[isNeedCheckCtaSet] No SIM insert, return true: ");
                    return true;
                }

                TelephonyManager tm = TelephonyManager.getDefault();
                int simCount = tm.getSimCount();
                String sbp = "0";
                for (int i = 0; i < simCount; i++) {
                    if (i == 0) {
                        sbp = SystemProperties.get("persist.radio.sim.opid", "0");
                    } else {
                        sbp = SystemProperties.get("persist.radio.sim.opid_" + i, "0");
                    }
                    vlog("[isNeedCheckCtaSet] sbp: " + sbp);
                    // Check if CMCC/CU/CT SBP ID
                    if (sbp.equals("1") || sbp.equals("2") || sbp.equals("9")) {
                        return true;
                    }
                }
                return false;
            } else {
                vlog("[isNeedCheckCtaSet] DSBP off");
                return true;
            }
        }

        private boolean isSimLocked() {
            TelephonyManager tm = TelephonyManager.getDefault();
            int simCount = tm.getSimCount();
            String strAllSimState = SystemProperties.get(TelephonyProperties.PROPERTY_SIM_STATE);
            for (int i = 0; i < simCount; i++) {
                String strCurSimState = "";
                if ((strAllSimState != null) && (strAllSimState.length() > 0)) {
                    String values[] = strAllSimState.split(",");
                    if ((i >= 0) && (i < values.length) && (values[i] != null)) {
                        strCurSimState = values[i];
                        if (strCurSimState.equals("NETWORK_LOCKED") ||
                                strCurSimState.equals("PUK_REQUIRED") ||
                                strCurSimState.equals("PIN_REQUIRED")) {
                            vlog("[isSimLocked] strCurSimState: " + strCurSimState);
                            return true;
                        }
                    }
                }
            }
            vlog("[isSimLocked] return false strAllSimState: " + strAllSimState);
            return false;
        }
    }

    /** @hide */
    private static class PropertyEccSource extends EccSource {
        public PropertyEccSource(int phoneType) {
            super(phoneType);
        }

        @Override
        public synchronized void parseEccList() {
            String strCount = SystemProperties.get(EccEntry.PROPERTY_COUNT);
            if (TextUtils.isEmpty(strCount)) {
                log("[parseEccList] empty property");
                return;
            }

            mEccList = new ArrayList<EccEntry>();

            int nCount = Integer.parseInt(strCount);
            for (int i = 0; i < nCount; i++) {
                String strNumber = SystemProperties.get(EccEntry.PROPERTY_NUMBER + i);
                if (!TextUtils.isEmpty(strNumber)) {
                    EccEntry entry = new EccEntry();
                    entry.setEcc(strNumber);

                    String strType = SystemProperties.get(EccEntry.PROPERTY_TYPE + i);
                    if (!TextUtils.isEmpty(strType)) {
                        short nType = 0;
                        for (String strTypeKey : strType.split(" ")) {
                            for (int index = 0; index < EccEntry.PROPERTY_TYPE_KEY.length;
                                    index++) {
                                if (strTypeKey.equals(EccEntry.PROPERTY_TYPE_KEY[index])) {
                                    nType |= EccEntry.PROPERTY_TYPE_VALUE[index];
                                }
                            }
                        }
                        entry.setCategory(Short.toString(nType));
                    } else {
                        entry.setCategory("0");
                    }

                    String strNonEcc = SystemProperties.get(EccEntry.PROPERTY_NON_ECC + i);
                    if (TextUtils.isEmpty(strNonEcc) || strNonEcc.equals("false")) {
                        entry.setCondition(EccEntry.ECC_ALWAYS);
                    } else {
                        entry.setCondition(EccEntry.ECC_NO_SIM);
                    }

                    String strPlmn = SystemProperties.get(EccEntry.PROPERTY_PLMN + i);
                    if (!TextUtils.isEmpty(strPlmn)) {
                        entry.setPlmn(strPlmn);
                    }

                    mEccList.add(entry);
                }
            }
            dlog("[parseEccList] property ECC list: " + mEccList);
        }

        @Override
        public synchronized boolean isEmergencyNumber(String number, int subId, int phoneType) {
            if (!isPhoneTypeSupport(phoneType)) {
                return false;
            }

            if (isSimInsert(phoneType)) {
                if (mEccList != null) {
                    for (EccEntry eccEntry : mEccList) {
                        if (!eccEntry.getCondition().equals(EccEntry.ECC_NO_SIM)) {
                            String ecc = eccEntry.getEcc();
                            if ((isMatch(ecc, number))
                                    && isEccPlmnMatch(eccEntry.getPlmn())) {
                                log("[isEmergencyNumber] match property ECC(w/ SIM) for phoneType:"
                                        + phoneType);
                                return true;
                            }
                        }
                    }
                }
            } else {
                if (mEccList != null) {
                    for (EccEntry eccEntry : mEccList) {
                        String ecc = eccEntry.getEcc();
                        if (isMatch(ecc, number)) {
                            log("[isEmergencyNumber] match property ECC(w/o SIM) for phoneType:"
                                    + phoneType);
                            return true;
                        }
                    }
                }
            }
            return false;
        }
    }

    /** @hide */
    private static class OmhEccSource extends EccSource {
        public OmhEccSource(int phoneType) {
            super(phoneType);
        }

        @Override
        public synchronized boolean isEmergencyNumber(String number, int subId, int phoneType) {
            vlog("[DBG][isEmergencyNumber] OMH start");
            if (!isPhoneTypeSupport(phoneType)) {
                return false;
            }

            try {
                ITelephonyEx telEx = ITelephonyEx.Stub.asInterface(ServiceManager.getService(
                        Context.TELEPHONY_SERVICE_EX));
                // Only check OMH ECC when OMH card insert to enhance performance
                if (isOmhCardInsert() && isSimInsert(phoneType)
                        && telEx.isUserCustomizedEcc(number)) {
                    dlog("[isEmergencyNumber] match OMH ECC for phoneType: " + phoneType);
                    return true;
                }
            } catch (RemoteException ex) {
                log("[isEmergencyNumber] RemoteException:" + ex);
                return false;
            } catch (NullPointerException ex) {
                log("[isEmergencyNumber] NullPointerException:" + ex);
                return false;
            }
            vlog("[DBG][isEmergencyNumber] OMH end");
            return false;
        }

        // Only check OMH ECC when OMH card insert to enhance performance
        private boolean isOmhCardInsert() {
            for (int i = 0; i < MAX_SIM_NUM; i++) {
                String omhCard = SystemProperties.get(SIM_OMH_PROPERTY[i], "-1");
                if ("1".equals(omhCard)) {
                    return true;
                }
            }
            return false;
        }
    }

    /** @hide */
    private static class TestEccSource extends EccSource {
        private static final String TEST_ECC_LIST = "persist.radio.mtk.testecc";

        public TestEccSource(int phoneType) {
            super(phoneType);
        }

        @Override
        public synchronized boolean isEmergencyNumber(String number, int subId, int phoneType) {
            // For VZW requirement (ALFMS01033786)
            if (!("OP12".equals(SystemProperties.get("persist.operator.optr")))) {
                return false;
            }

            if (isSimInsert(PhoneConstants.PHONE_TYPE_GSM)
                    || isSimInsert(PhoneConstants.PHONE_TYPE_CDMA)) {
                String strtestEccList = SystemProperties.get(TEST_ECC_LIST);
                if (!TextUtils.isEmpty(strtestEccList)) {
                    dlog("[isEmergencyNumber] test ECC list: " + strtestEccList);
                    for (String strEcc : strtestEccList.split(",")) {
                        if (!strEcc.isEmpty()) {
                            if (isMatch(strEcc, number)) {
                                dlog("[isEmergencyNumber] match test ECC for phoneType: "
                                        + phoneType);
                                return true;
                            }
                        }
                    }
                }
            }
            return false;
        }
    }

    // Initialization
    static {
        initialize();
    }

    private static void initialize() {
        sIsCtaSupport = "1".equals(SystemProperties.get("ro.mtk_cta_support"));
        sIsCtaSet = "1".equals(SystemProperties.get("ro.mtk_cta_set"));
        sIsC2kSupport = "1".equals(SystemProperties.get("ro.boot.opt_c2k_support"));
        sIsOP09Support = "OP09".equals(SystemProperties.get("persist.operator.optr"))
                && ("SEGDEFAULT".equals(SystemProperties.get("persist.operator.seg"))
                || "SEGC".equals(SystemProperties.get("persist.operator.seg")));

        log("Init: sIsCtaSupport: " + sIsCtaSupport +
                ", sIsCtaSet: " + sIsCtaSet + ", sIsC2kSupport: " + sIsC2kSupport +
                ", sIsOP09Support: " + sIsOP09Support);
        sPlusCodeUtils = PlusCodeProcessor.getPlusCodeUtils();
        initEccSource();
    }

    private static void initEccSource() {
        sAllEccSource = new ArrayList<EccSource>();

        sNetworkEcc = new NetworkEccSource(PhoneConstants.PHONE_TYPE_GSM);
        sPropertyEcc = new PropertyEccSource(PhoneConstants.PHONE_TYPE_GSM);
        if (sIsC2kSupport) {
            sXmlEcc = new XmlEccSource(
                    PhoneConstants.PHONE_TYPE_GSM | PhoneConstants.PHONE_TYPE_CDMA);
            sSimEcc = new SimEccSource(
                    PhoneConstants.PHONE_TYPE_GSM | PhoneConstants.PHONE_TYPE_CDMA);
            sTestEcc = new TestEccSource(
                    PhoneConstants.PHONE_TYPE_GSM | PhoneConstants.PHONE_TYPE_CDMA);
        } else {
            sXmlEcc = new XmlEccSource(PhoneConstants.PHONE_TYPE_GSM);
            sSimEcc = new SimEccSource(PhoneConstants.PHONE_TYPE_GSM);
            sTestEcc = new TestEccSource(PhoneConstants.PHONE_TYPE_GSM);
        }

        // Add EccSource according to priority
        // Network ECC > SIM ECC > Other ECC
        // 1. Add network ECC source
        sAllEccSource.add(sNetworkEcc);
        // 2. Add SIM ECC
        sAllEccSource.add(sSimEcc);

        // 3. Other ECC
        sAllEccSource.add(sXmlEcc);
        sAllEccSource.add(sPropertyEcc);
        sAllEccSource.add(sTestEcc);

        if (sIsCtaSet) {
            sCtaEcc = new CtaEccSource(PhoneConstants.PHONE_TYPE_GSM);
            sAllEccSource.add(sCtaEcc);
        }

        if (sIsC2kSupport) {
            sOmhEcc = new OmhEccSource(PhoneConstants.PHONE_TYPE_CDMA);
            sAllEccSource.add(sOmhEcc);
        }
    }

    /**
     * Return the extracted phone number.
     *
     * @param phoneNumber Phone number string.
     * @return Return number whiched is extracted the CLIR part.
     * @hide
     * @internal
     */
    public static String extractCLIRPortion(String phoneNumber) {
        if (phoneNumber == null) {
            return null;
        }

        // ex. **61*<any international number>**<timer>#
        Pattern p = Pattern.compile(
                "^([*][#]|[*]{1,2}|[#]{1,2})([0-9]{2,3})([*])([+]?[0-9]+)(.*)(#)$");
        Matcher m = p.matcher(phoneNumber);
        if (m.matches()) {
            return m.group(4); // return <any international number>
        } else if (phoneNumber.startsWith("*31#") || phoneNumber.startsWith("#31#")) {
            dlog(phoneNumber + " Start with *31# or #31#, return " + phoneNumber.substring(4));
            return phoneNumber.substring(4);
        } else if (phoneNumber.indexOf(PLUS_SIGN_STRING) != -1 &&
                   phoneNumber.indexOf(PLUS_SIGN_STRING) ==
                   phoneNumber.lastIndexOf(PLUS_SIGN_STRING)) {
            p = Pattern.compile("(^[#*])(.*)([#*])(.*)(#)$");
            m = p.matcher(phoneNumber);
            if (m.matches()) {
                if ("".equals(m.group(2))) {
                    // Started with two [#*] ends with #
                    // So no dialing number and we'll just return "" a +, this handles **21#+
                    dlog(phoneNumber + " matcher pattern1, return empty string.");
                    return "";
                } else {
                    String strDialNumber = m.group(4);
                    if (strDialNumber != null && strDialNumber.length() > 1
                            && strDialNumber.charAt(0) == PLUS_SIGN_CHAR) {
                        // Starts with [#*] and ends with #
                        // Assume group 4 is a dialing number such as *21*+1234554#
                        dlog(phoneNumber + " matcher pattern1, return " + strDialNumber);
                        return strDialNumber;
                    }
                }
            } else {
                p = Pattern.compile("(^[#*])(.*)([#*])(.*)");
                m = p.matcher(phoneNumber);
                if (m.matches()) {
                    String strDialNumber = m.group(4);
                    if (strDialNumber != null && strDialNumber.length() > 1
                            && strDialNumber.charAt(0) == PLUS_SIGN_CHAR) {
                        // Starts with [#*] and only one other [#*]
                        // Assume the data after last [#*] is dialing number
                        // (i.e. group 4) such as *31#+11234567890.
                        // This also includes the odd ball *21#+
                        dlog(phoneNumber + " matcher pattern2, return " + strDialNumber);
                        return strDialNumber;
                    }
                }
            }
        }

        return phoneNumber;
    }


    /**
     * Prepend plus to the number.
     * @param number The original number.
     * @return The number with plus sign.
     * @hide
     */
    public static String prependPlusToNumber(String number) {
        // This is an "international number" and should have
        // a plus prepended to the dialing number. But there
        // can also be Gsm MMI codes as defined in TS 22.030 6.5.2
        // so we need to handle those also.
        //
        // http://web.telia.com/~u47904776/gsmkode.htm is a
        // has a nice list of some of these GSM codes.
        //
        // Examples are:
        //   **21*+886988171479#
        //   **21*8311234567#
        //   **21*+34606445635**20#
        //   **21*34606445635**20#
        //   *21#
        //   #21#
        //   *#21#
        //   *31#+11234567890
        //   #31#+18311234567
        //   #31#8311234567
        //   18311234567
        //   +18311234567#
        //   +18311234567
        // Odd ball cases that some phones handled
        // where there is no dialing number so they
        // append the "+"
        //   *21#+
        //   **21#+
        StringBuilder ret;
        String retString = number.toString();
        Pattern p = Pattern.compile(
                "^([*][#]|[*]{1,2}|[#]{1,2})([0-9]{2,3})([*])([0-9]+)(.*)(#)$");
        Matcher m = p.matcher(retString);
        if (m.matches()) {
            ret = new StringBuilder();
            ret.append(m.group(1));
            ret.append(m.group(2));
            ret.append(m.group(3));
            ret.append("+");
            ret.append(m.group(4));
            ret.append(m.group(5));
            ret.append(m.group(6));
        } else {
            p = Pattern.compile("(^[#*])(.*)([#*])(.*)(#)$");
            m = p.matcher(retString);
            if (m.matches()) {
                if ("".equals(m.group(2))) {
                    // Started with two [#*] ends with #
                    // So no dialing number and we'll just
                    // append a +, this handles **21#+
                    ret = new StringBuilder();
                    ret.append(m.group(1));
                    ret.append(m.group(3));
                    ret.append(m.group(4));
                    ret.append(m.group(5));
                    ret.append("+");
                } else {
                    // Starts with [#*] and ends with #
                    // Assume group 4 is a dialing number
                    // such as *21*+1234554#
                    ret = new StringBuilder();
                    ret.append(m.group(1));
                    ret.append(m.group(2));
                    ret.append(m.group(3));
                    ret.append("+");
                    ret.append(m.group(4));
                    ret.append(m.group(5));
                }
            } else {
                p = Pattern.compile("(^[#*])(.*)([#*])(.*)");
                m = p.matcher(retString);
                if (m.matches()) {
                    // Starts with [#*] and only one other [#*]
                    // Assume the data after last [#*] is dialing
                    // number (i.e. group 4) such as *31#+11234567890.
                    // This also includes the odd ball *21#+
                    ret = new StringBuilder();
                    ret.append(m.group(1));
                    ret.append(m.group(2));
                    ret.append(m.group(3));
                    ret.append("+");
                    ret.append(m.group(4));
                } else {
                    // Does NOT start with [#*] just prepend '+'
                    ret = new StringBuilder();
                    ret.append('+');
                    ret.append(retString);
                }
            }
        }
        return ret.toString();
    }

    /**
     * Return the international prefix string according to country iso.
     *
     * @param countryIso Country ISO.
     * @return Return international prefix.
     * @hide
     * @internal
     */
    public static String getInternationalPrefix(String countryIso) {
        if (countryIso == null) {
            return "";
        }

        PhoneNumberUtil util = PhoneNumberUtil.getInstance();
        PhoneMetadata metadata = util.getMetadataForRegion(countryIso);
        if (metadata != null) {
            String prefix = metadata.getInternationalPrefix();
            if (countryIso.equalsIgnoreCase("tw")) {
                prefix = "0(?:0[25679] | 16 | 17 | 19)";
            }
            return prefix;
        }

        return null;
    }

    private static String preProcessPlusCode(String dialStr) {
        if (!TextUtils.isEmpty(dialStr)) {
            if (isReallyDialable(dialStr.charAt(0)) && isNonSeparator(dialStr)) {
                String currIso = TelephonyManager.getDefault().getNetworkCountryIso();
                String defaultIso = TelephonyManager.getDefault().getSimCountryIso();
                boolean needToFormat = true;
                if (!TextUtils.isEmpty(currIso) && !TextUtils.isEmpty(defaultIso)) {
                    int currFormat = getFormatTypeFromCountryCode(currIso);
                    int defaultFormat = getFormatTypeFromCountryCode(defaultIso);
                    needToFormat = !((currFormat == defaultFormat) && (currFormat == FORMAT_NANP));
                }
                if (needToFormat) {
                    dlog("preProcessPlusCode, before format number:" + dialStr);
                    String retStr = dialStr;
                    // Checks if the plus sign character is in the passed-in dial string
                    if (dialStr != null && dialStr.lastIndexOf(PLUS_SIGN_STRING) != -1) {
                        String postDialStr = null;
                        String tempDialStr = dialStr;

                        // Sets the retStr to null since the conversion will be performed below.
                        retStr = null;
                        do {
                            String networkDialStr;
                            networkDialStr = extractNetworkPortionAlt(tempDialStr);
                            if (networkDialStr != null &&
                                    networkDialStr.charAt(0) == PLUS_SIGN_CHAR &&
                                    networkDialStr.length() > 1) {
                                if (sPlusCodeUtils.canFormatPlusToIddNdd()) {
                                    networkDialStr = sPlusCodeUtils.replacePlusCodeWithIddNdd(
                                            networkDialStr);
                                } else {
                                    dlog("preProcessPlusCode, can't format plus code.");
                                    return dialStr;
                                }
                            }

                            dlog("preProcessPlusCode, networkDialStr:" + networkDialStr);
                            // Concatenates the string that is converted from network portion
                            if (!TextUtils.isEmpty(networkDialStr)) {
                                if (retStr == null) {
                                    retStr = networkDialStr;
                                } else {
                                    retStr = retStr.concat(networkDialStr);
                                }
                            } else {
                                Rlog.e(LOG_TAG, "preProcessPlusCode, null newDialStr:"
                                        + networkDialStr);
                                return dialStr;
                            }
                            postDialStr = extractPostDialPortion(tempDialStr);
                            if (!TextUtils.isEmpty(postDialStr)) {
                                int dialableIndex = findDialableIndexFromPostDialStr(postDialStr);

                                // dialableIndex should always be greater than 0
                                if (dialableIndex >= 1) {
                                    retStr = appendPwCharBackToOrigDialStr(dialableIndex,
                                             retStr, postDialStr);
                                    // Skips the P/W character, extracts the dialable portion
                                    tempDialStr = postDialStr.substring(dialableIndex);
                                } else {
                                    if (dialableIndex < 0) {
                                        postDialStr = "";
                                    }
                                    Rlog.e(LOG_TAG, "preProcessPlusCode, wrong postDialStr:"
                                            + postDialStr);
                                }
                            }
                            dlog("preProcessPlusCode, postDialStr:" + postDialStr
                                    + ", tempDialStr:" + tempDialStr);
                        } while (!TextUtils.isEmpty(postDialStr)
                                && !TextUtils.isEmpty(tempDialStr));
                    }
                    dialStr = retStr;
                    dlog("preProcessPlusCode, after format number:" + dialStr);
                } else {
                    dlog("preProcessPlusCode, no need format, currIso:" + currIso
                            + ", defaultIso:" + defaultIso);
                }
            }
        }
        return dialStr;
    }

    private static String preProcessPlusCodeForSms(String dialStr) {
        dlog("preProcessPlusCodeForSms ENTER.");
        if (!TextUtils.isEmpty(dialStr) && dialStr.startsWith("+")) {
            if (isReallyDialable(dialStr.charAt(0)) && isNonSeparator(dialStr)) {
                String defaultIso = TelephonyManager.getDefault().getSimCountryIso();
                if (getFormatTypeFromCountryCode(defaultIso) != FORMAT_NANP) {
                    if (sPlusCodeUtils.canFormatPlusCodeForSms()) {
                        String retAddr = sPlusCodeUtils.replacePlusCodeForSms(dialStr);
                        if (TextUtils.isEmpty(retAddr)) {
                            dlog("preProcessPlusCodeForSms," +
                                    " can't handle the plus code by PlusCodeUtils");
                        } else {
                            dlog("preProcessPlusCodeForSms, "
                                    + "new dialStr = " + retAddr);
                            dialStr = retAddr;
                        }
                    }
                }
            }
        }
        return dialStr;
    }

    // Phone Number Utility API revise END

    // Phone Number ECC API revise START

    /**
     * Helper function for isEmergencyNumber(String, String) and
     * isPotentialEmergencyNumber(String, String).
     *
     * Mediatek revise for retry ECC with Phone type (GSM or CDMA)
     *
     * @param subId the subscription id of the SIM.
     * @param number the number to look up.
     * @param defaultCountryIso the specific country which the number should be checked against
     * @param useExactMatch if true, consider a number to be an emergency
     *           number only if it *exactly* matches a number listed in
     *           the RIL / SIM.  If false, a number is considered to be an
     *           emergency number if it simply starts with the same digits
     *           as any of the emergency numbers listed in the RIL / SIM.
     *
     * @return true if the number is an emergency number for the specified country.
     * @hide
     */
    public static boolean isEmergencyNumberExt(int subId, String number,
            String defaultCountryIso, boolean useExactMatch) {
        // If the number passed in is null, just return false:
        if (number == null) return false;

        // If the number passed in is a SIP address, return false, since the
        // concept of "emergency numbers" is only meaningful for calls placed
        // over the cell network.
        // (Be sure to do this check *before* calling extractNetworkPortionAlt(),
        // since the whole point of extractNetworkPortionAlt() is to filter out
        // any non-dialable characters (which would turn 'abc911def@example.com'
        // into '911', for example.))
        if (isUriNumber(number)) {
            return false;
        }

        // Strip the separators from the number before comparing it
        // to the list.
        number = extractNetworkPortionAlt(number);

        dlog("[isEmergencyNumberExt] number: " + number + ", subId: " + subId + ", iso: "
                + defaultCountryIso + ", useExactMatch: " + useExactMatch);

        // MTK ECC retry by Phone type (GSM/CDMA) START
        if ((subId == SubscriptionManager.DEFAULT_SUBSCRIPTION_ID)
                    || (subId == SubscriptionManager.INVALID_SUBSCRIPTION_ID)) {
            int queryPhoneType = getQueryPhoneType(subId);

            // Query if GSM ECC
            if ((queryPhoneType & PhoneConstants.PHONE_TYPE_GSM) != 0
                    && isEmergencyNumberExt(number, PhoneConstants.PHONE_TYPE_GSM, subId)) {
                return true;
            }

            // Query if CDMA ECC
            if ((queryPhoneType & PhoneConstants.PHONE_TYPE_CDMA) != 0
                    && isEmergencyNumberExt(number, PhoneConstants.PHONE_TYPE_CDMA, subId)) {
                return true;
            }
            // MTK ECC retry by Phone type END
        } else {
            // Query current phone by type
            int queryPhoneType = TelephonyManager.getDefault().getCurrentPhoneType(subId);
            boolean ret = false;
            if (queryPhoneType == PhoneConstants.PHONE_TYPE_CDMA) {
                ret = isEmergencyNumberExt(number, PhoneConstants.PHONE_TYPE_CDMA, subId);
            } else {
                // Query GSM ECC for all other phone type except CDMA phone (IMS/SIP phone)
                ret = isEmergencyNumberExt(number, PhoneConstants.PHONE_TYPE_GSM, subId);
            }
            if (ret) {
                return true;
            }
        }

        // AOSP ECC check by country ISO (Local emergency number)
        // ECC may conflict with MMI code like (*112#) because ShortNumberUtil
        // will remove non digit chars before match ECC, so we add MMI code check
        // before match ECC by ISO
        if (defaultCountryIso != null && !isMmiCode(number)) {
            ShortNumberUtil util = new ShortNumberUtil();
            boolean ret = false;
            if (useExactMatch) {
                ret = util.isEmergencyNumber(number, defaultCountryIso);
            } else {
                ret = util.connectsToEmergencyNumber(number, defaultCountryIso);
            }
            dlog("[isEmergencyNumberExt] AOSP check return: " +
                    ret + ", iso: " + defaultCountryIso + ", useExactMatch: " + useExactMatch);
            return ret;
        }

        dlog("[isEmergencyNumber] no match ");
        return false;
    }

    /**
     * Checks a given number against the list of
     * emergency numbers provided by the RIL and SIM card.
     *
     * @param number the number to look up.
     * @param phoneType CDMA or GSM for checking different ECC list.
     * @return true if the number is in the list of emergency numbers
     *         listed in the RIL / SIM, otherwise return false.
     * @hide
     * @internal
     */
    public static boolean isEmergencyNumberExt(String number, int phoneType) {
        dlog("[isEmergencyNumberExt], number:" + number + ", phoneType:" + phoneType);
        return isEmergencyNumberExt(number, phoneType,
            SubscriptionManager.DEFAULT_SUBSCRIPTION_ID);
    }

    /**
     * Checks a given number against the list of
     * emergency numbers provided by the RIL and SIM card by sub id.
     *
     * @param number the number to look up.
     * @param phoneType CDMA or GSM for checking different ECC list.
     * @param subId sub id to query.
     * @return true if the number is in the list of emergency numbers
     *         listed in the RIL / SIM, otherwise return false.
     * @hide
     */
    public static boolean isEmergencyNumberExt(String number, int phoneType, int subId) {
        vlog("[isEmergencyNumberExt], number:" + number + ", phoneType:" + phoneType);

        for (EccSource es : sAllEccSource) {
            if (es.isEmergencyNumber(number, subId, phoneType)) {
                return true;
            }
        }

        dlog("[isEmergencyNumberExt] no match for phoneType: " + phoneType);
        return false;
    }

    /**
     * Check if the dailing number is a special ECC
     *
     * Add for CTA requirement to check if sim insert or not to decide dial using
     * emergency call or normal call for emergency numbers.
     * CTA requirment:
     *    1. For CTA numbers (110, 119, 122,120), should always display ECC UI
     *    2. Dial using normal call when SIM insert and ECC call when No SIM.
     * Here we use SIM ecc reported by MD to decide if SIM is insert or not which
     * is the same as the logic in isEmergencyNumber().
     * @param dialString dailing number string.
     * @return true if it is a special ECC.
     * @hide
     * @internal
     */
    public static boolean isSpecialEmergencyNumber(String dialString) {
        /* Special emergency number will show ecc in MMI but sent to nw as normal call */
        return isSpecialEmergencyNumber(SubscriptionManager.DEFAULT_SUBSCRIPTION_ID, dialString);
    }

    /**
     * Check if the dailing number is a special ECC
     *
     * Add for CTA requirement to check if sim insert or not to decide dial using
     * emergency call or normal call for emergency numbers.
     * CTA requirment:
     *    1. For CTA numbers (110, 119, 122,120), should always display ECC UI
     *    2. Dial using normal call when SIM insert and ECC call when No SIM.
     * Here we use SIM ecc reported by MD to decide if SIM is insert or not which
     * is the same as the logic in isEmergencyNumber().
     * @param dialString dailing number string.
     * @return true if it is a special ECC.
     * @hide
     */
    public static boolean isSpecialEmergencyNumber(int subId, String dialString) {
        /* Special emergency number will show ecc in MMI but sent to nw as normal call */
        if (sNetworkEcc.isEmergencyNumber(dialString, subId, PhoneConstants.PHONE_TYPE_GSM) ||
                sSimEcc.isEmergencyNumber(dialString, subId, PhoneConstants.PHONE_TYPE_GSM)) {
                // If network or SIM ecc, should not treat as special emergency number
                return false;
        }

        for (EccSource es : sAllEccSource) {
            if (es.isSpecialEmergencyNumber(subId, dialString)) {
                return true;
            }
        }

        log("[isSpecialEmergencyNumber] not special ecc");
        return false;
    }

    /**
     * Get Ecc List which will be sync to MD.
     *
     * @param none.
     * @return Ecc List with type ArrayList<EccEntry>.
     * @hide
     */
    public static ArrayList<EccEntry> getEccList() {
        // Currently we only sync xml/property/cta ecc list to MD
        ArrayList<EccEntry> resList = new ArrayList<EccEntry>();
        sXmlEcc.addToEccList(resList);
        sPropertyEcc.addToEccList(resList);
        if (sIsCtaSet) {
            sCtaEcc.addToEccList(resList);
        }
        dlog("[getEccList] ECC list: " + resList);
        return resList;
    }

    /**
     * Set specific ECC category.
     *
     * @param eccCat represent a setted specific ECC category
     * @hide
     */
    public static void setSpecificEccCategory(int eccCat) {
        log("[setSpecificEccCategory] set ECC category: " + eccCat);
        sSpecificEccCat = eccCat;
    }

    /**
     * Get the service category for the given ECC number.
     * @param number The ECC number.
     * @return The service category for the given number.
     * @hide
     */
    public static int getServiceCategoryFromEcc(String number) {
        return getServiceCategoryFromEccBySubId(number,
                SubscriptionManager.DEFAULT_SUBSCRIPTION_ID);
    }

    /**
     * Get the service category for the given ECC number.
     * @param number The ECC number.
     * @param subId  The sub id to query
     * @return The service category for the given number.
     * @hide
     */
    public static int getServiceCategoryFromEccBySubId(String number, int subId) {
        /// M: support for release 12, specific ECC category from NW. @{
        if (sSpecificEccCat >= 0) {
            log("[getServiceCategoryFromEccBySubId] specific ECC category: " + sSpecificEccCat);
            int eccCat = sSpecificEccCat;
            sSpecificEccCat = -1; // reset specific ecc category
            return eccCat;
        }
        /// @}

        for (EccSource es : sAllEccSource) {
            int category = es.getServiceCategory(number, subId);
            if (category >= 0) {
                return category;
            }
        }

        log("[getServiceCategoryFromEccBySubId] no matched, ECC: " + number
                + ", subId: " + subId);
        return 0;
    }

    private static int getQueryPhoneType(int subId) {
        int simNum = TelephonyManager.getDefault().getPhoneCount();
        boolean needQueryGsm = false;
        boolean needQueryCdma = false;

        vlog("[DBG] getQueryPhoneType start");

        // Only Query GSM and CDMA for C2K Project to enhance performance
        if (sIsC2kSupport) {
            for (int i = 0; i < simNum; i++) {
                vlog("[DBG][getQueryPhoneType] getCurrentPhoneTypeForSlot start");
                /*
                int[] allSubId = SubscriptionManager.getSubId(i);
                if (allSubId == null) {
                    dlog("[getQueryPhoneType] allSubId is null");
                    continue;
                }
                vlog("[getQueryPhoneType] allSubId:" + allSubId[0]);
                int phoneType = TelephonyManager.getDefault().getCurrentPhoneType(allSubId[0]);
                */
                int phoneType = TelephonyManager.getDefault().getCurrentPhoneTypeForSlot(i);
                if (phoneType == PhoneConstants.PHONE_TYPE_GSM) {
                    needQueryGsm = true;
                } else if (phoneType == PhoneConstants.PHONE_TYPE_CDMA) {
                    needQueryCdma = true;
                }
                vlog("[DBG][getQueryPhoneType] getCurrentPhoneTypeForSlot end");
            }

            // If no SIM insert in all slot, should query GSM
            // Example: SS load with CDMAPhone type only, it will query
            // CDMA only which cause the 3GPP No SIM ECC(000,08,118)
            // can't dial out as ECC even there is no SIM insert.
            if (!needQueryGsm && !isSimInsert()) {
                needQueryGsm = true;
            }
        } else {
            // For GSM only project, always query GSM phone ECC only
            // to enhance performance.
            needQueryGsm = true;
        }

        // For ECC new design for N Denali+
        // Case: Insert G+G card, then remove both SIMs, the phone type
        // will be GSM phone and we'll query GSM ECC only, but in fact
        // in this case, we may call ECC through CDMA
        if (sIsC2kSupport && simNum > 1 && !needQueryCdma) {
            boolean isRoaming = false;
            ITelephonyEx telEx = ITelephonyEx.Stub.asInterface(ServiceManager.getService(
                    Context.TELEPHONY_SERVICE_EX));
            if (telEx != null) {
                int[] iccTypes = new int[simNum];
                for (int i = 0; i < simNum; i++) {
                    try {
                        iccTypes[i] = telEx.getIccAppFamily(i);
                    } catch (RemoteException ex) {
                        log("getIccAppFamily, RemoteException:" + ex);
                    } catch (NullPointerException ex) {
                        log("getIccAppFamily, NullPointerException:" + ex);
                    }
                }
                for (int i = 0; i < simNum; i++) {
                    if (iccTypes[i] >= 0x02 || isCt3gDualModeCard(i)) {
                        log("[getQueryPhoneType] Slot" + i + " is roaming");
                        isRoaming = true;
                        break;
                    }
                }
                if (!isRoaming) {
                    for (int i = 0; i < simNum; i++) {
                        if (iccTypes[i] == 0x00) {
                            vlog("[getQueryPhoneType] Slot" + i + " no card");
                            needQueryCdma = true;
                            break;
                        }
                    }
                }
            } else {
                log("[getQueryPhoneType] fail to get ITelephonyEx service");
            }
        }

        int phoneTypeRet = 0;
        if (needQueryGsm) {
            phoneTypeRet |= PhoneConstants.PHONE_TYPE_GSM;
        }
        if (needQueryCdma) {
            phoneTypeRet |= PhoneConstants.PHONE_TYPE_CDMA;
        }
        vlog("[DBG][getQueryPhoneType] needQueryGsm:" + needQueryGsm
                + ", needQueryCdma:" + needQueryCdma + ", ret: " + phoneTypeRet);
        return phoneTypeRet;
    }

    private static boolean isCt3gDualModeCard(int slotId) {
        final String[] ct3gProp = {
            "gsm.ril.ct3g",
            "gsm.ril.ct3g.2",
            "gsm.ril.ct3g.3",
            "gsm.ril.ct3g.4",
        };
        if (slotId < 0 || slotId >= ct3gProp.length) {
            return false;
        }
        return "1".equals(SystemProperties.get(ct3gProp[slotId]));
    }

    private static boolean isMmiCode(String number) {
        Pattern p = Pattern.compile("^[*][0-9]+[#]$");
        Matcher m = p.matcher(number);
        if (m.matches()) {
            return true;
        }
        return false;
    }

    private static boolean isSimInsert() {
        int simNum = TelephonyManager.getDefault().getPhoneCount();
        for (int i = 0; i < simNum; i++) {
            if (TelephonyManager.getDefault().hasIccCard(i)) {
                return true;
            }
        }
        return false;
    }

    // Phone Number ECC API revise END

    // print log only in eng load
    private static void dlog(String msg) {
        if (VDBG) {
            log(msg);
        }
    }

    // need to turn on VDBG manually before print log
    private static void vlog(String msg) {
        if (DBG) {
            log(msg);
        }
    }
    /// @}
}
