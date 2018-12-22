package com.mediatek.telecom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.android.internal.annotations.VisibleForTesting;
import com.android.server.telecom.Log;

import android.os.Build;
import android.text.TextUtils;

public class PerformanceTracker {
    //TODO: set isPeroformanceTest = true via AT CMD if necessary
    private static final boolean isPerformanceTest = true;
    private static final boolean ENABLE_TRACKER = isPerformanceTest &&
            (TextUtils.equals(Build.TYPE, "user") || TextUtils.equals(Build.TYPE, "user-debug"));

    private static final String TAG = "PerformanceTracker";
    private static final int VERSION = 1;

    public static final int TEST_MO = 0;
    public static final int TEST_ANSWER_MT = 1;
    public static final int TEST_END_CALL = 2;

    ///For MO performance track @{
    public static final String MO_RECEIVED_BROADCAST = "mo_received_broadcast";
    public static final String MO_BIND_INCALL_SERVICE_BEGIN= "bind_incall_service_begin";
    public static final String MO_BIND_INCALL_SERVICE_END= "bind_incall_service_end";
    public static final String MO_ADD_CALL_TO_INCALL_END= "add_call_to_incall_end";
    private static final ArrayList<String> sMoTimeStampList = new ArrayList<String>();
    private static final Map<String, ArrayList<Long>> sMoMap
            = new HashMap<String, ArrayList<Long>>();
    //Call is not created when receive broadcast, so record time in sMoReceiveBroadcastTime
    public static final String MO_CALL_TEMP_ID = "mo_call_temp_id";
    private static long sMoReceiveBroadcastTime = -1;
    static {
        sMoTimeStampList.add(MO_RECEIVED_BROADCAST);
        sMoTimeStampList.add(MO_BIND_INCALL_SERVICE_BEGIN);
        sMoTimeStampList.add(MO_BIND_INCALL_SERVICE_END);
        sMoTimeStampList.add(MO_ADD_CALL_TO_INCALL_END);
    }
    ///MO end @}

    ///For answer MT performance track @{
    public static final String MT_RECEIVE_ANSWER_OPERATION = "mt_receive_answer_operation";
    public static final String MT_SEND_ANSWER_TO_TELEPHONY = "send_answer_to_telephony";
    public static final String MT_TELEPHONY_RESPONSE_ACTIVE = "telephony_response_active";
    public static final String MT_UPDATE_ACTIVE_TO_INCALL= "update_active_to_incall";
    private static final ArrayList<String> sMtTimeStampList = new ArrayList<String>();
    private static final Map<String, ArrayList<Long>> sMtMap
            = new HashMap<String, ArrayList<Long>>();
    static {
        sMtTimeStampList.add(MT_RECEIVE_ANSWER_OPERATION);
        sMtTimeStampList.add(MT_SEND_ANSWER_TO_TELEPHONY);
        sMtTimeStampList.add(MT_TELEPHONY_RESPONSE_ACTIVE);
        sMtTimeStampList.add(MT_UPDATE_ACTIVE_TO_INCALL);
    }
    ///MT end @}

    ///For end call performance track @{
    public static final String END_CALL_OPERATION_RECEIVED = "end_call_operation_received";
    public static final String END_SEND_TO_TELEPHONY = "send_disconnect_to_telephony";
    public static final String END_TELEPHONY_RSP_DISCONNECTED = "telephony_response_disconnected";
    public static final String END_UPDATE_DISCONNECTED_TO_INCALL = "update_disconnected_to_incall";
    private static final ArrayList<String> sEndCallTimeStampList = new ArrayList<String>();
    private static final Map<String, ArrayList<Long>> sEndCallMap
            = new HashMap<String, ArrayList<Long>>();
    static {
        sEndCallTimeStampList.add(END_CALL_OPERATION_RECEIVED);
        sEndCallTimeStampList.add(END_SEND_TO_TELEPHONY);
        sEndCallTimeStampList.add(END_TELEPHONY_RSP_DISCONNECTED);
        sEndCallTimeStampList.add(END_UPDATE_DISCONNECTED_TO_INCALL);
    }
    ///End call end @}

    private static PerformanceTracker sInstance = new PerformanceTracker();

    public static PerformanceTracker getInstance() {
        return sInstance;
    }

    private PerformanceTracker() {
        Log.i(TAG, "version = " + VERSION);
        StringBuilder sb = new StringBuilder();

        sb.append("Dial");
        for (int i = 1; i < sMoTimeStampList.size(); i++) {
            sb.append(",")
                    .append(sMoTimeStampList.get(i - 1))
                    .append("->")
                    .append(sMoTimeStampList.get(i));
        }
        Log.i(TAG, sb.toString());

        sb.setLength(0);
        sb.append("Hangup");
        for (int i = 1; i < sEndCallTimeStampList.size(); i++) {
            sb.append(",")
                    .append(sEndCallTimeStampList.get(i - 1))
                    .append("->")
                    .append(sEndCallTimeStampList.get(i));
        }
        Log.i(TAG, sb.toString());

        sb.setLength(0);
        sb.append("Answer");
        for (int i = 1; i < sMtTimeStampList.size(); i++) {
            sb.append(",")
                    .append(sMtTimeStampList.get(i - 1))
                    .append("->")
                    .append(sMtTimeStampList.get(i));
        }
        Log.i(TAG, sb.toString());
    }

    public void trackMO(String callId, String moment) {
        if (ENABLE_TRACKER) {
            // add each moment to ArrayList, and record it.
            // then dump info on the final step.
            ArrayList<Long> timeList = null;
            switch (moment) {
            case MO_RECEIVED_BROADCAST:
                // FIXME: This MO_CALL_TEMP_ID is useless, try to remove it in later versions.
                if (MO_CALL_TEMP_ID.equals(callId)) {
                    sMoReceiveBroadcastTime = System.currentTimeMillis();
                }
                break;
            case MO_BIND_INCALL_SERVICE_BEGIN:
                timeList = new ArrayList<Long>();
                if (sMoReceiveBroadcastTime > 0) {
                    timeList.add(sMoReceiveBroadcastTime);
                    sMoReceiveBroadcastTime = -1;
                } else {
                    Log.d(TAG, "Not typical MO call, No broadcast received in advance, " +
                            "skip tracking it, callId: " + callId);
                    return;
                }
                timeList.add(System.currentTimeMillis());
                sMoMap.put(callId, timeList);
                break;
            case MO_BIND_INCALL_SERVICE_END:
                timeList = sMoMap.get(callId);
                if (timeList != null) {
                    timeList.add(System.currentTimeMillis());
                }
                break;
            case MO_ADD_CALL_TO_INCALL_END:
                timeList = sMoMap.get(callId);
                if (timeList != null) {
                    timeList.add(System.currentTimeMillis());
                    //dump info and clear the data at the last step.
                    dump(callId, sMoTimeStampList, timeList, "Dial");
                    sMoMap.remove(callId);
                }
                break;

            default:
                break;
            }
        }
    }

    public void trackAnswerCall(String callId, String moment) {
        if (ENABLE_TRACKER) {
            // add each moment to ArrayList, and record it.
            // then dump info on the final step.
            ArrayList<Long> timeList = null;
            switch (moment) {
            case MT_RECEIVE_ANSWER_OPERATION:
                timeList = new ArrayList<Long>();
                timeList.add(System.currentTimeMillis());
                sMtMap.put(callId, timeList);
                break;
            case MT_SEND_ANSWER_TO_TELEPHONY:
            case MT_TELEPHONY_RESPONSE_ACTIVE:
                timeList = sMtMap.get(callId);
                if (timeList != null) {
                    timeList.add(System.currentTimeMillis());
                }
                break;
            case MT_UPDATE_ACTIVE_TO_INCALL:
                timeList = sMtMap.get(callId);
                if (timeList != null) {
                    timeList.add(System.currentTimeMillis());
                    //dump info and clear the data at the last step.
                    dump(callId, sMtTimeStampList, timeList, "Answer");
                    sMtMap.remove(callId);
                }
                break;

            default:
                break;
            }
        }
    }

    public void trackEndCall(String callId, String moment) {
        if (ENABLE_TRACKER) {
            // add each moment to ArrayList, and record it.
            // then dump info on the final step.
            ArrayList<Long> timeList = null;
            switch (moment) {
            case END_CALL_OPERATION_RECEIVED:
                timeList = new ArrayList<Long>();
                timeList.add(System.currentTimeMillis());
                sEndCallMap.put(callId, timeList);
                break;
            case END_SEND_TO_TELEPHONY:
            case END_TELEPHONY_RSP_DISCONNECTED:
                timeList = sEndCallMap.get(callId);
                if (timeList != null) {
                    timeList.add(System.currentTimeMillis());
                }
                break;
            case END_UPDATE_DISCONNECTED_TO_INCALL:
                timeList = sEndCallMap.get(callId);
                if (timeList != null) {
                    timeList.add(System.currentTimeMillis());
                    //dump info and clear the data at the last step.
                    dump(callId, sEndCallTimeStampList, timeList, "Hangup");
                    sEndCallMap.remove(callId);
                    //when call disconnected, we also discard MO/MT dirty data if exists
                    //like the abort call's data never remove by trackMO, we assumed that
                    //every call should be disconnected or aborted
                    discardData(callId, TEST_MO);
                    discardData(callId, TEST_ANSWER_MT);
                }
                break;

            default:
                break;
            }
        }
    }

    public static void discardData(String callId, int testcase) {
        switch (testcase) {
        case TEST_MO:
            sMoMap.remove(callId);
            break;
        case TEST_ANSWER_MT:
            sMtMap.remove(callId);
            break;
        case TEST_END_CALL:
            sEndCallMap.remove(callId);
            break;

        default:
            break;
        }
    }

    @VisibleForTesting
    public void dump(String callId, List keyList, List timeList, String testcase) {
        if (keyList == null || timeList == null) {
            Log.w(TAG, "cannot dump performance log for timelist is null.");
            return;
        }
        if (keyList.size() != timeList.size()) {
            Log.w(TAG, "cannot dump performance log for timelist's size is incorrect.");
            return;
        }

        // The log format should be:
        // Telecom : PerformanceTracker: Dial,10,20,30: ICSBC.oSC@AFk
        // The "Dial,10,20,30" part would be easy to export to csv file, and analyze
        // with csv tools.
        StringBuilder sb = new StringBuilder();
        sb.append(testcase);
        for (int i = 1; i < keyList.size(); i++) {
            sb.append(",").append((Long) timeList.get(i) - (Long) timeList.get(i - 1));
        }
        Log.i(TAG, sb.toString());
    }
}
