/*
* Copyright (C) 2014 MediaTek Inc.
* Modification based on code covered by the mentioned copyright
* and/or permission notice(s).
*/
/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.gallery3d.filtershow.category;

import android.app.Activity;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import com.android.gallery3d.R;
import com.android.gallery3d.filtershow.FilterShowActivity;

public class CategoryPanel extends Fragment implements View.OnClickListener {

    public static final String FRAGMENT_TAG = "CategoryPanel";
    private static final String PARAMETER_TAG = "currentPanel";

    private int mCurrentAdapter = MainPanel.LOOKS;
    private CategoryAdapter mAdapter;
    private IconView mAddButton;

    public void setAdapter(int value) {
        mCurrentAdapter = value;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        loadAdapter(mCurrentAdapter);
    }

    public void loadAdapter(int adapter) {
        FilterShowActivity activity = (FilterShowActivity) getActivity();
        switch (adapter) {
            case MainPanel.LOOKS: {
                mAdapter = activity.getCategoryLooksAdapter();
                if (mAdapter != null) {
                    mAdapter.initializeSelection(MainPanel.LOOKS);
                }
                activity.updateCategories();
                break;
            }
            case MainPanel.BORDERS: {
                mAdapter = activity.getCategoryBordersAdapter();
                if (mAdapter != null) {
                    mAdapter.initializeSelection(MainPanel.BORDERS);
                }
                activity.updateCategories();
                break;
            }
            case MainPanel.GEOMETRY: {
                mAdapter = activity.getCategoryGeometryAdapter();
                if (mAdapter != null) {
                    mAdapter.initializeSelection(MainPanel.GEOMETRY);
                }
                break;
            }
            case MainPanel.FILTERS: {
                mAdapter = activity.getCategoryFiltersAdapter();
                if (mAdapter != null) {
                    mAdapter.initializeSelection(MainPanel.FILTERS);
                }
                break;
            }
            case MainPanel.VERSIONS: {
                mAdapter = activity.getCategoryVersionsAdapter();
                if (mAdapter != null) {
                    mAdapter.initializeSelection(MainPanel.VERSIONS);
                }
                break;
            }
			/** PRIZE-watermark-wanzhijuan-2016-1-21-start **/
            case MainPanel.STICKER: {
            	mAdapter = activity.getCategoryStickersAdapter();
                if (mAdapter != null) {
                    mAdapter.initializeSelection(MainPanel.STICKER);
                }
            	break;
            }
			/** PRIZE-watermark-wanzhijuan-2016-1-21-end **/
        }
        updateAddButtonVisibility();
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        state.putInt(PARAMETER_TAG, mCurrentAdapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        LinearLayout main = (LinearLayout) inflater.inflate(
                R.layout.filtershow_category_panel_new, container,
                false);

        if (savedInstanceState != null) {
            int selectedPanel = savedInstanceState.getInt(PARAMETER_TAG);
            loadAdapter(selectedPanel);
        }

        View panelView = main.findViewById(R.id.listItems);
        if (panelView instanceof CategoryTrack) {
            CategoryTrack panel = (CategoryTrack) panelView;
            if (mAdapter != null) {
                mAdapter.setOrientation(CategoryView.HORIZONTAL);
                panel.setAdapter(mAdapter);
                mAdapter.setContainer(panel);
            }
        } else if (mAdapter != null) {
            ListView panel = (ListView) main.findViewById(R.id.listItems);
            panel.setAdapter(mAdapter);
            mAdapter.setContainer(panel);
        }

        mAddButton = (IconView) main.findViewById(R.id.addButton);
        if (mAddButton != null) {
            mAddButton.setOnClickListener(this);
            updateAddButtonVisibility();
        }
        return main;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.addButton:
                /// M: [BUG.MODIFY] @{
                /* FilterShowActivity activity = (FilterShowActivity) getActivity();
                activity.addCurrentVersion();*/
                v.setEnabled(false);
                FilterShowActivity activity = (FilterShowActivity) getActivity();
                ///M: Each click menu should start sub thread decode icon image, which occupy
                // much time and maybe cause ANR while click BACK key in the same time.
                if (activity.hasFinishApplyVersionOperation()) {
                    activity.addCurrentVersion();
                    activity.addLabel();
                }
                v.setEnabled(true);

                /// @}
                break;
        }
    }

    public void updateAddButtonVisibility() {
        if (mAddButton == null) {
            return;
        }
        FilterShowActivity activity = (FilterShowActivity) getActivity();
        if (activity.isShowingImageStatePanel() && mAdapter.showAddButton()) {
            mAddButton.setVisibility(View.VISIBLE);
            if (mAdapter != null) {
                mAddButton.setText(mAdapter.getAddButtonText());
            }
        } else {
            mAddButton.setVisibility(View.GONE);
        }
    }
}
