package com.mediatek.phone.callsetting;

import android.util.Log;

/** Class to utility in test case. */
public class TestUtils {
    private static final String TAG = "TestUtils";

    /** wait condition to check in sleep time. */
    public interface Condition {
        /** condition to check in sleep time. */
        public boolean isMet();
    }

    /**
     * M: Wait until a {@code Condition} is met,
     * Looping and check the condition in sleep time.
     * @param message to identify this wait
     * @param condition to interrupt the sleep
     * @param timeoutSeconds to timeout
     */
    public static boolean waitUntil(String message, Condition condition,
            int timeoutSeconds) {
        Log.d(TAG, message + ": Waiting...");
        final long timeout = System.currentTimeMillis() + timeoutSeconds * 1000;

        while (System.currentTimeMillis() < timeout) {
            if (condition.isMet()) {
                return true;
            }
        }
        return false;
    }
}
