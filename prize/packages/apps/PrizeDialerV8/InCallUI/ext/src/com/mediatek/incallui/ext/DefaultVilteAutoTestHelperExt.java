package com.mediatek.incallui.ext;
import android.content.Context;
import android.util.Log;


public class DefaultVilteAutoTestHelperExt implements IVilteAutoTestHelperExt{

  /**
    * called when CallButtonFragment execute onActivityCreated.
    * register BroadcastReceiver to receiver vilte auto test  broadcast
    * @param context host Context
    * @param obj the CallButtonPresenter instance
    */

    @Override
    public void registerReceiverForUpgradeAndDowngrade(Context context, Object obj) {
      Log.d("DefaultVilteAutoTestHelperExt", "this is in default up/down register" );
    }

  /**
    * called when AnswerFragment execute onCreateView.
    * register BroadcastReceiver to receiver vilte auto test  broadcast
    * @param context host Context
    * @param obj the instance of AnswerPresenter
    */

    @Override
    public void registerReceiverForAcceptAndRejectUpgrade(Context context, Object obj) {
      Log.d("DefaultVilteAutoTestHelperExt", "this is in default accept/reject register" );
    }

  /**
    * called when CallButtonPresenter execute onUiUnready
    * unregister BroadcastReceiver
    */
    @Override
    public void unregisterReceiverForUpgradeAndDowngrade( ) {
      Log.d("DefaultVilteAutoTestHelperExt", "this is in default up/down unregister" );
    }

  /**
    * called when AnswerFragment execute onDestoryView
    * unregister BroadcastReceiver
    */
    @Override
    public void unregisterReceiverForAcceptAndRejectUpgrade( ) {
      Log.d("DefaultVilteAutoTestHelperExt", "this is in default accept/reject unregister" );
    }

}

