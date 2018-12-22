package com.android.soundrecorder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;

public class MediaButtonReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
  
    String intentAction = intent.getAction();
    KeyEvent keyEvent = (KeyEvent) intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);

    if (Intent.ACTION_MEDIA_BUTTON.equals(intentAction)) {
        Intent it = new Intent(SoundRecorder.MEDIA_ACT);
        it.putExtra(Intent.EXTRA_KEY_EVENT, keyEvent);
        context.sendBroadcast(it);
    }
    }
}