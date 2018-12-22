package com.mediatek.launcher3.ext;

public class DefaultOperatorChecker implements IOperatorChecker {
    private static final String TAG = "DefaultOperatorChecker";
    private boolean mSupportCycleSliding=false;

	public void setmSupportCycleSliding(boolean mSupportCycleSliding) {
		this.mSupportCycleSliding = mSupportCycleSliding;
	}

	@Override
    public boolean supportEditAndHideApps() {
        LauncherLog.d(TAG, "default supportEditAndHideApps called.");
        return false; 
    }

    @Override
    public boolean supportAppListCycleSliding() {
        LauncherLog.d(TAG, "default supportAppListCycleSliding called.");
        return mSupportCycleSliding;
    }
}
