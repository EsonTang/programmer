package com.mediatek.fpspolicy;

import android.os.RemoteException;
import android.view.IDockedStackListener;
import android.view.WindowManagerGlobal;

import com.android.server.input.InputWindowHandle;

public class FpsInfo extends IDockedStackListener.Stub {
    private static FpsInfo sInstance;
    private native void nativeSetInputWindows(InputWindowHandle[] windowHandles);
    private native void nativeSetWindowFlag(int flag, int mask);

    public static final int FLAG_MULTI_WINDOW = 0x01;

    private FpsInfo() {
        try {
            WindowManagerGlobal.getWindowManagerService().registerDockedStackListener(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setInputWindows(InputWindowHandle[] windowHandles) {
        nativeSetInputWindows(windowHandles);
    }

    public void setWindowFlag(int flag, int mask) {
        nativeSetWindowFlag(flag, mask);
    }

    @Override
    public void onDividerVisibilityChanged(boolean visible) throws RemoteException {
    }

    @Override
    public void onDockedStackExistsChanged(final boolean exists)
            throws RemoteException {
        if (exists) {
            nativeSetWindowFlag(FLAG_MULTI_WINDOW, FLAG_MULTI_WINDOW);
        } else {
            nativeSetWindowFlag(0, FLAG_MULTI_WINDOW);
        }
    }

    @Override
    public void onDockedStackMinimizedChanged(boolean minimized, long animDuration)
            throws RemoteException {
    }

    @Override
    public void onAdjustedForImeChanged(boolean adjustedForIme, long animDuration)
            throws RemoteException {
    }

    @Override
    public void onDockSideChanged(int newDockSide) throws RemoteException {
    }
    
        /* prize-add-split screen-liyongli-20170708-start */
        @Override
        public void prizeOnFocusStackChanged(int focusedStackId, int lastFocusedStackId) throws RemoteException {
        }
        /* prize-add-split screen-liyongli-20170708-end */

        /* prize-add-split screen, return btn exit split screen-liyongli-20170724-start */
        @Override
        public void prizeOnRequestExitSplitScreen(int btnType, int focusedStackId) throws RemoteException {
        }
        /* prize-add-split screen, return btn exit split screen-liyongli-20170724-end */

    public static FpsInfo getInstance() {
        if (sInstance != null)
            return sInstance;

        sInstance = new FpsInfo();
        return sInstance;
    }
}
