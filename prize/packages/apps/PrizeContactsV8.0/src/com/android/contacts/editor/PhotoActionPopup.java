/*
* Copyright (C) 2014 MediaTek Inc.
* Modification based on code covered by the mentioned copyright
* and/or permission notice(s).
*/
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
 * limitations under the License
 */

package com.android.contacts.editor;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListPopupWindow;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.android.contacts.R;
import com.android.contacts.util.PhoneCapabilityTester;
import com.android.contacts.util.UiClosables;

import java.util.ArrayList;

/**
 * Shows a popup asking the user what to do for a photo. The result is passed back to the Listener
 */
public class PhotoActionPopup {
    public static final String TAG = "PhotoActionPopup";

    /**
     * Bitmask flags to specify which actions should be presented to the user.
     */
    public static final class Flags {
        /** If set, show choice to remove photo. */
        public static final int REMOVE_PHOTO = 2;
        /** If set, show choices to take a picture with the camera, or pick one from the gallery. */
        public static final int TAKE_OR_PICK_PHOTO = 4;
        /**
         *  If set, modifies the wording in the choices for TAKE_OR_PICK_PHOTO
         *  to emphasize that the existing photo will be replaced.
         */
        public static final int TAKE_OR_PICK_PHOTO_REPLACE_WORDING = 8;
    }

    /**
     * Convenient combinations of commonly-used flags (see {@link Flags}).
     */
    public static final class Modes {
        public static final int NO_PHOTO =
                Flags.TAKE_OR_PICK_PHOTO;
        public static final int READ_ONLY_PHOTO = 0;
        public static final int WRITE_ABLE_PHOTO =
                Flags.REMOVE_PHOTO |
                Flags.TAKE_OR_PICK_PHOTO |
                Flags.TAKE_OR_PICK_PHOTO_REPLACE_WORDING;
        // When the popup represents multiple photos, the REMOVE_PHOTO option doesn't make sense.
        // The REMOVE_PHOTO option would have to remove all photos. And sometimes some of the
        // photos are readonly.
        public static final int MULTIPLE_WRITE_ABLE_PHOTOS =
                Flags.TAKE_OR_PICK_PHOTO |
                Flags.TAKE_OR_PICK_PHOTO_REPLACE_WORDING;
    }
    
    /*prize-add-huangliemin-2016-7-8-start*/
    private View mPopuWindowView;
    private PopupWindow mPopupWindow;
    /*prize-add-huangliemin-2016-7-8-end*/

    public static ArrayList<ChoiceListItem> getChoices(Context context, int mode) {
        // Build choices, depending on the current mode. We assume this Dialog is never called
        // if there are NO choices (e.g. a read-only picture is already super-primary)
        final ArrayList<ChoiceListItem> choices = new ArrayList<ChoiceListItem>(4);
        // Remove
        if ((mode & Flags.REMOVE_PHOTO) > 0) {
            choices.add(new ChoiceListItem(ChoiceListItem.ID_REMOVE,
                    context.getString(R.string.removePhoto)));
        }
        // Take photo or pick one from the gallery.  Wording differs if there is already a photo.
        if ((mode & Flags.TAKE_OR_PICK_PHOTO) > 0) {
            boolean replace = (mode & Flags.TAKE_OR_PICK_PHOTO_REPLACE_WORDING) > 0;
            final int takePhotoResId = replace ? R.string.take_new_photo : R.string.take_photo;
            final String takePhotoString = context.getString(takePhotoResId);
            /// M: Change Feature change string
            final int pickPhotoResId = replace ?
                    R.string.pick_new_photo_from_gallery : R.string.pick_photo;
            final String pickPhotoString = context.getString(pickPhotoResId);
            if (PhoneCapabilityTester.isCameraIntentRegistered(context)) {
                choices.add(new ChoiceListItem(ChoiceListItem.ID_TAKE_PHOTO, takePhotoString));
            }
            choices.add(new ChoiceListItem(ChoiceListItem.ID_PICK_PHOTO, pickPhotoString));
        }
        return choices;
    }

    public static ListPopupWindow createPopupMenu(Context context, View anchorView,
            final Listener listener, int mode) {
        final ArrayList<ChoiceListItem> choices = getChoices(context, mode);

        final ListAdapter adapter = new ArrayAdapter<ChoiceListItem>(context,
                R.layout.select_dialog_item, choices);

        final ListPopupWindow listPopupWindow = new ListPopupWindow(context);
        final OnItemClickListener clickListener = new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final ChoiceListItem choice = choices.get(position);
                switch (choice.getId()) {
                    case ChoiceListItem.ID_REMOVE:
                        listener.onRemovePictureChosen();
                        break;
                    case ChoiceListItem.ID_TAKE_PHOTO:
                        listener.onTakePhotoChosen();
                        break;
                    case ChoiceListItem.ID_PICK_PHOTO:
                        listener.onPickFromGalleryChosen();
                        break;
                }

                UiClosables.closeQuietly(listPopupWindow);
            }
        };

        listPopupWindow.setAnchorView(anchorView);
        listPopupWindow.setAdapter(adapter);
        listPopupWindow.setOnItemClickListener(clickListener);
        listPopupWindow.setModal(true);
        listPopupWindow.setInputMethodMode(ListPopupWindow.INPUT_METHOD_NOT_NEEDED);
        final int minWidth = context.getResources().getDimensionPixelSize(
                R.dimen.photo_action_popup_min_width);
        if (anchorView.getWidth() < minWidth) {
            listPopupWindow.setWidth(minWidth);
        }
        return listPopupWindow;
    }
    
    /*prize-add-popupwindow-in-the-bottom-huangliemin-2016-7-8-start*/
    public PopupWindow createPopupWindow(Context context,
            final Listener listener, int mode) {
        final ArrayList<ChoiceListItem> choices = getChoices(context, mode);

        if(mPopuWindowView == null) {
        	mPopuWindowView = View.inflate(context, R.layout.prize_popupwindow, null);
        	/*prize-add for dido os 8.0-hpf-2017-7-22-start*/
        	View divider = mPopuWindowView.findViewById(R.id.prize_popup_title_divider);
        	View buttonContainer = mPopuWindowView.findViewById(R.id.prize_popup_bottom_button_container);
        	TextView cancelBtn = (TextView)mPopuWindowView.findViewById(R.id.cancel_btn);
        	String s = context.getResources().getString(R.string.prize_bottom_cancel);
        	Log.d(TAG,"[createPopupWindow]  s = "+s);
    		cancelBtn.setVisibility(View.VISIBLE);
    		cancelBtn.setText(s);
    		cancelBtn.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					if(mPopupWindow != null) {
	            		mPopupWindow.dismiss();
					}
				}
			});
        	divider.setVisibility(View.GONE);
        	buttonContainer.setVisibility(View.GONE);
        	/*prize-add for dido os 8.0-hpf-2017-7-22-end*/
        }
        final ViewGroup mContent = (ViewGroup)mPopuWindowView.findViewById(R.id.prize_popup_content);

    	if(mPopupWindow == null) {
    		mPopupWindow = new PopupWindow(mPopuWindowView, 
    				WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
    		//mPopupWindow.setAnimationStyle(R.style.PrizePopupWindowStyle);
    		mPopupWindow.setFocusable(true);
    		mPopupWindow.setOutsideTouchable(true);
    		mPopupWindow.setAnimationStyle(R.style.GetDialogBottomMenuAnimation);
    		//mPopupWindow.update();
    	}
    	
    	mContent.removeAllViews();
    	
    	for(int i=0;i<choices.size();i++) {
    		TextView menuItem = (TextView)View.inflate(context, R.layout.prize_popmenu_text_huangliemin_2016_7_8, null);
    		//menuItem.setPadding(0, 0, 0, 0);//prize-remove for dido os 8.0-hpf-2017-7-22
    		menuItem.setLayoutParams(new LayoutParams(WindowManager.LayoutParams.MATCH_PARENT,
    				context.getResources().getDimensionPixelOffset(R.dimen.dialog_bottom_menu_item_height)));
    		menuItem.setText(choices.get(i).toString());
    		

    		if(i == 0){
    			menuItem.setBackground(mContent.getResources().getDrawable(R.drawable.prize_selector_popup_window_uppder_btn_bg));
    		}else if(i == choices.size()-1){
    			menuItem.setBackground(mContent.getResources().getDrawable(R.drawable.prize_selector_popup_window_under_btn_bg));
    		}else{
    			menuItem.setBackground(mContent.getResources().getDrawable(R.drawable.prize_selector_popup_window_middle_btn_bg));
    		}
    		
    		mContent.addView(menuItem);
    		menuItem.setOnClickListener(new PopuItemClickListener(choices.get(i).getId(), listener));
    		if(i<choices.size()-1){
    			//if(i == 0 || i == choices.size()-1){ continue;}//prize-add for dido os 8.0-hpf-2017-7-22
    			View Divider = new View(context);
    			Divider.setLayoutParams(new LayoutParams(WindowManager.LayoutParams.MATCH_PARENT,1));
    			Divider.setBackgroundColor(context.getResources().getColor(R.color.divider_line_color_light));
    			mContent.addView(Divider);
    		}
    	}
    	
        return mPopupWindow;
    }
    
    class PopuItemClickListener implements View.OnClickListener {
    	private int mId;
    	private Listener mListener;

		public PopuItemClickListener(int id,Listener listener) {
			mId = id;
			mListener = listener;
		}
    		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (mId) {
            case ChoiceListItem.ID_REMOVE:
                mListener.onRemovePictureChosen();
                break;
            case ChoiceListItem.ID_TAKE_PHOTO:
            	mListener.onTakePhotoChosen();
                break;
            case ChoiceListItem.ID_PICK_PHOTO:
            	mListener.onPickFromGalleryChosen();
                break;
        }
			
			if(mPopupWindow!=null && mPopupWindow.isShowing()) {
				mPopupWindow.dismiss();
			}
		}
    	
    }
    
    /*prize-add-popupwindow-in-the-bottom-huangliemin-2016-7-8-end*/

    public static final class ChoiceListItem {
        private final int mId;
        private final String mCaption;

        public static final int ID_TAKE_PHOTO = 1;
        public static final int ID_PICK_PHOTO = 2;
        public static final int ID_REMOVE = 3;

        public ChoiceListItem(int id, String caption) {
            mId = id;
            mCaption = caption;
        }

        @Override
        public String toString() {
            return mCaption;
        }

        public int getId() {
            return mId;
        }
    }

    public interface Listener {
        void onRemovePictureChosen();
        void onTakePhotoChosen();
        void onPickFromGalleryChosen();
    }
}
