
package com.prize.qihoo.cleandroid.sdk;

import android.content.Context;

import com.qihoo360.mobilesafe.opti.env.clear.TrashClearEnv;
import com.qihoo360.mobilesafe.opti.i.trashclear.TrashInfo;

public abstract class BaseOptiTask {

    protected Context mContext;

    protected int mType = TrashClearEnv.TYPE_ALL_ITEMS;

    protected int[] mTrashTypes = null;

    protected Callback mScanCallback;

    protected Callback mClearCallback;

    private boolean isScanFinish = true;

    private boolean isScanCancelled;

    private boolean isClearFinish = true;

    private boolean isClearCancelled;

    public BaseOptiTask(Context context) {
        mContext = context;
    }

    public void setType(int type, int[] trashTypes) {
        mType = type;
        mTrashTypes = trashTypes;
    }

    public void setCallback(Callback scanCallback, Callback clearCallback) {
        mScanCallback = scanCallback;
        mClearCallback = clearCallback;
    }

    public abstract void scan();

    public abstract void clear();

    public abstract void onDestroy();

    protected void scanStart() {
        isScanFinish = false;
        isScanCancelled = false;
        if (mScanCallback != null) {
            mScanCallback.onStart();
        }
    }

    public void scanFinish() {
        isScanFinish = true;
        if (mScanCallback != null) {
            mScanCallback.onFinish(isScanCancelled);
        }
    }

    public boolean isScanFinish() {
        return isScanFinish;
    }

    public void cancelScan() {
        isScanCancelled = true;
    }

    public boolean isScanCancelled() {
        return this.isScanCancelled;
    }

    protected void clearStart() {
        isClearFinish = false;
        isClearCancelled = false;
        if (mClearCallback != null) {
            mClearCallback.onStart();
        }
    }

    protected void clearFinish() {
        isClearFinish = true;
        if (mClearCallback != null) {
            mClearCallback.onFinish(isClearCancelled);
        }
    }

    public boolean isClearFinish() {
        return isClearFinish || isClearCancelled();
    }

    public void cancelClear() {
        this.isClearCancelled = true;
    }

    public boolean isClearCancelled() {
        return this.isClearCancelled;
    }

    /**
     * 一键优化回调接口
     */
    public static interface Callback {
        void onStart();

        void onProgressUpdate(int progress, int max);

        void onDataUpdate(long length, long checkedLength, TrashInfo info);

        void onFinish(boolean isCanceled);
    }
}
