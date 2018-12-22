/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.server;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IPowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Slog;

import com.android.server.power.ShutdownThread;

public class SupersaverShutdownActivity extends Activity {

    private static final String TAG = "SupersaverShutdownActivity";

    private static final int MODE_INVALID = 0;
    private static final int MODE_INTO_POWER_SAVER = 1;
    private static final int MODE_QUIT_POWER_SAVER = 2;
    private int mMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        if (Intent.ACTION_INTO_POWER_SAVER.equals(intent.getAction())){
            mMode = MODE_INTO_POWER_SAVER;
        }else if (Intent.ACTION_QUIT_POWER_SAVER.equals(intent.getAction())){
            mMode = MODE_QUIT_POWER_SAVER;
        }else {
            mMode = MODE_INVALID;
        }

        Slog.i(TAG, "PowerExtendMode onCreate(): mMode=" + mMode);
        Thread thr = new Thread("SupersaverShutdownActivity") {
            @Override
            public void run() {
            IPowerManager lPowerManager = IPowerManager.Stub.asInterface(ServiceManager.getService(Context.POWER_SERVICE));
                try {
                    if (MODE_INTO_POWER_SAVER == mMode) {
                        Slog.i(TAG, "PowerExtendMode lPowerManager.switchSuperSaverMode(true)->intoSupersaverMode");
                        lPowerManager.switchSuperSaverMode(true);
                    } else if (MODE_QUIT_POWER_SAVER == mMode) {
                        Slog.i(TAG, "PowerExtendMode lPowerManager.switchSuperSaverMode(false)->quitSupersaverMode");
                        lPowerManager.switchSuperSaverMode(false);
                    }
                } catch (RemoteException e) {
                }
            }
        };
        thr.start();
        finish();
        // Wait for us to tell the power manager to shutdown.
        try {
            thr.join();
        } catch (InterruptedException e) {
        }
    }
}
