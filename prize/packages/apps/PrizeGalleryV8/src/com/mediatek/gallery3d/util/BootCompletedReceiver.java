package com.mediatek.gallery3d.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.android.gallery3d.app.BatchService;
import com.mediatek.gallery3d.adapter.FeatureHelper;

public class BootCompletedReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, final Intent intent) {
        if (FeatureHelper.isCacheFileExists(context)) {
            return;
        }
        context.startService(new Intent(context.getApplicationContext(), BatchService.class));
    }
}
