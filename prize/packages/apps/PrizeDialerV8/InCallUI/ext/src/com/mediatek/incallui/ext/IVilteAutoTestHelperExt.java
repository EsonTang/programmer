package com.mediatek.incallui.ext;
import android.content.Context;

public interface IVilteAutoTestHelperExt {

  /**
    * called when CallButtonFragment execute onActivityCreated.
    * register BroadcastReceiver to receiver vilte auto test  broadcast
    * @param context host Context
    * @param obj the CallButtonPresenter instance
    */
    void registerReceiverForUpgradeAndDowngrade(Context context, Object obj);

  /**
    * called when AnswerFragment execute onCreateView.
    * register BroadcastReceiver to receiver vilte auto test  broadcast
    * @param context host Context
    * @param obj the instance of AnswerPresenter
    */
    void registerReceiverForAcceptAndRejectUpgrade(Context context, Object obj);

  /**
    * called when CallButtonPresenter execute onUiUnready
    * unregister BroadcastReceiver
    */
    void unregisterReceiverForUpgradeAndDowngrade();

  /**
    * called when AnswerFragment execute onDestoryView
    * unregister BroadcastReceiver
    */
    void unregisterReceiverForAcceptAndRejectUpgrade();

}

