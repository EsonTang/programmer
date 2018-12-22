package com.prize.applock.fingerprintapplock;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.CancellationSignal;
import android.util.Log;

/**
 * Created by Administrator on 2016/7/9.
 */
public class FingerprintCallback extends FingerprintManager.AuthenticationCallback {

    private Callback mCallback;
    private FingerprintManager mFingerprintManager;
    private Context mContext;
    private CancellationSignal mCancellationSignal;
    private boolean mSelfCancelled;

    public FingerprintCallback(FingerprintManager fingerprintManager, Context context, Callback callback) {
        mFingerprintManager = fingerprintManager;
        mContext = context;
        mCallback = callback;
    }

    @Override
    public void onAuthenticationError(int errorCode, CharSequence errString) {
        Log.i("john", "errorCode : " + errorCode);
        super.onAuthenticationError(errorCode, errString);
        mCallback.onError(errorCode,errString);
    }

    @Override
    public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
        super.onAuthenticationHelp(helpCode, helpString);
    }

    @Override
    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
        super.onAuthenticationSucceeded(result);
        mCallback.onAuthenticated();
    }

    @Override
    public void onAuthenticationFailed() {
        super.onAuthenticationFailed();
        mCallback.onFailed();
    }

    public boolean isFingerprintAuthAvailable() {
        return mFingerprintManager.isHardwareDetected()
                && mFingerprintManager.hasEnrolledFingerprints();
    }

    public void startListening() {
        if (!isFingerprintAuthAvailable()) {
            return;
        }
        stopListening();
        mSelfCancelled = false;
        mCancellationSignal = new CancellationSignal();
        mFingerprintManager
                .authenticate(null, mCancellationSignal, 0 /* flags */, this, null);
    }
    public void resetErrorTimes()
    {
        byte[] token = null; /* TODO: pass real auth token once fp HAL supports it */
        mFingerprintManager.resetTimeout(token);
    }
    public void stopListening() {
        if (mCancellationSignal != null) {
            mSelfCancelled = true;
            mCancellationSignal.cancel();
            mCancellationSignal = null;
        }
    }
    public boolean isListening()
    {
    	return (mCancellationSignal != null);
    }
    public interface Callback {
        void onAuthenticated();
        void onFailed();
        void onError(int error,CharSequence errString);
    }

}
