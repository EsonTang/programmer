package com.mediatek.dialer.ext;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;


import java.util.List;

public class DefaultDialPadExtension implements IDialPadExtension {

    private static final String TAG = "DefaultDialPadExtension";

    /**
     * for OP09
     * @param context
     * @param fragment
     * @param dialpadFragment
     */
    public void onCreate(Context context, Fragment fragment, DialpadExtensionAction action) {
        log("onCreate");
    }

    /**
     * for OP09
     * @param context
     * @param input
     * @return
     */
    public boolean handleChars(Context context, String input) {
        log("handleChars");
        return false;
    }

    /**
     * for OP09
     * @param inflater
     * @param container
     * @param savedState
     * @param resultView
     * @return
     */
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState,
            View resultView) {
        log("onCreateView");
        return resultView;
    }

    /**
     * for OP09
     */
    public void onDestroy() {
        log("onDestroy");
    }

    /**
     * for OP09
     * the dialpad hidden changed
     * @param enabled If true, show the dialpad
     */
    public void onHiddenChanged(boolean scaleIn, int delayMs) {
        log("onHiddenChanged");
    }

    /**
     * for OP09
     */
    public void showDialpadChooser(boolean enabled) {
        log("showDialpadChooser");
    }

    /**
     * for OP09
     * @param input, the string to be handled.
     */
    public String handleSecretCode(String input) {
        log("handleSecretCode");
        return input;
    }

    /**
     * for OP03 OP09
     * @param popupMenu
     * @param anchorView
     * @param menu
     */
    public void constructPopupMenu(PopupMenu popupMenu, View anchorView, Menu menu) {
        log("constructPopupMenu");
    }

    /**
     * for OP09
     * @param lastNumberDialed
     */
    public void updateDialAndDeleteButtonEnabledState(final String lastNumberDialed) {
        log("updateDialAndDeleteButtonEnabledState");
    }

    /**
     * for OP01
     * @param activity
     * @param menu
     */
    public void buildOptionsMenu(Activity activity, Menu menu) {
        log("buildOptionsMenu");
    }

    /**
     * for OP01
     * @param activity
     * @param view
     */
    public void onViewCreated(Activity activity, View view) {
        log("onViewCreated");
    }

    /**
     * for OP01
     * @param list
     * @return list
     */
    public List<String> getSingleIMEI(List<String> list) {
        log("getSingleIMEI");
        return list;
    }

    @Override
    public boolean isVideoButtonEnabled(boolean hostVideoEnabled, Object...params) {
        return hostVideoEnabled;
    }

    /**
     * for OP07
     *@param appContext
     *@param intent
     * @return false
     */
    public boolean checkVideoSetting(Context appContext, Intent intent) {
        log("checkVideoSetting");
        return false;
    }

    /**
     * for OP18
     *@param object
     */
     public void customizeDefaultTAB(Object object) {
         log("customizeDefaultTAB");
    }

    /**
     * for OP08.
     * @param dialerOptionsView
     *          Dialer options list view.
     * @param optionType
     *          Type of Dialer option.
     * @param number
     *          Query number.
     */
     public void customizeDialerOptions(View dialerOptionsView, int optionType, String number) {
         log("customizeDialerOptions");
     }

    private void log(String msg) {
//        Log.d(TAG, msg + " default");
    }

    /**
     * Request capability when input number in Dialer
     */
    @Override
    public void onSetQueryString(String number) {

    }

    /**
     * for OP09
     * called when dialpad onResume
     */
    @Override
    public void onResume() {
        log("onResume");
    }

}
