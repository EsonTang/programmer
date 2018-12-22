/*
 * Copyright (C) 2011 The Android Open Source Project
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

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;

import com.android.gallery3d.R;
import com.android.gallery3d.ui.GLRootView;

public class PickerActivity extends AbstractGalleryActivity
        implements OnClickListener {

    public static final String KEY_ALBUM_PATH = "album-path";

    @Override
    public void onCreate(Bundle savedInstanceState) {      

		super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_picker);
        View back = findViewById(R.id.im_left);
        back.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.im_left) {
            this.getStateManager().finishState(this.getStateManager().getTopState());
        }
    }
}
