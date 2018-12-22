/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.android.gallery3d.app;

import android.content.Intent;
import android.os.Bundle;
import android.content.pm.ActivityInfo;

import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.common.Utils;

public class DialogPicker extends PickerActivity {
    private boolean isfromWallpaper = false;
    private static final String TAG = "Gallery2/DialogPicker";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        String action = intent.getAction();
        //if (intent.hasExtra("fromWallpaper")) {
        //    isfromWallpaper = intent.getBooleanExtra("fromWallpaper", false);
        //}
        //if(fromWallpaper){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        //}
        //Log.w(TAG,"isfromWallpaper = " + isfromWallpaper);
        if (Intent.ACTION_PICK.equalsIgnoreCase(action)) {
            // We do NOT really support the PICK intent. Handle it as
            // the GET_CONTENT. However, we need to translate the type
            // in the intent here.
            String type = Utils.ensureNotNull(intent.getType());
            if (type.startsWith("vnd.android.cursor.dir/")) {
                if (type.endsWith("/image")) intent.setType("image/*");
                if (type.endsWith("/video")) intent.setType("video/*");
            }
        }
        startGetContent(intent);
    }

    private void startGetContent(Intent intent) {
        Bundle data = intent.getExtras() != null
                ? new Bundle(intent.getExtras())
                : new Bundle();
        data.putBoolean(GalleryActivity.KEY_GET_CONTENT, true);
        int typeBits = GalleryUtils.determineTypeBits(this, intent);
//        data.putInt(KEY_TYPE_BITS, typeBits);
        data.putString(AlbumSetPage.KEY_MEDIA_PATH,
                getDataManager().getTopSetPath(typeBits));
        getStateManager().startState(AlbumDialogPage.class, data);
    }
}
