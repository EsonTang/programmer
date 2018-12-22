/*
* Copyright (C) 2014 MediaTek Inc.
* Modification based on code covered by the mentioned copyright
* and/or permission notice(s).
*/
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

package com.android.contacts.detail;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.CommonDataKinds.Photo;
import android.provider.ContactsContract.DisplayPhoto;
import android.provider.ContactsContract.RawContacts;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.WindowManager;
//import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.ListPopupWindow;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.Toast;

import com.android.contacts.R;
import com.android.contacts.editor.PhotoActionPopup;
import com.android.contacts.common.model.AccountTypeManager;
import com.android.contacts.common.model.RawContactModifier;
import com.android.contacts.common.model.RawContactDelta;
import com.android.contacts.common.model.ValuesDelta;
import com.android.contacts.common.model.account.AccountType;
import com.android.contacts.common.model.RawContactDeltaList;
import com.android.contacts.util.ContactPhotoUtils;
import com.mediatek.contacts.ContactsSystemProperties;
import com.mediatek.contacts.util.DrmUtils;
import com.mediatek.contacts.util.Log;
import com.android.contacts.util.UiClosables;

import java.io.FileNotFoundException;
import java.util.List;
/**
 * Handles displaying a photo selection popup for a given photo view and dealing with the results
 * that come back.
 */
public abstract class PhotoSelectionHandler implements OnClickListener {

    private static final String TAG = PhotoSelectionHandler.class.getSimpleName();

    private static final int REQUEST_CODE_CAMERA_WITH_DATA = 1001;
    private static final int REQUEST_CODE_PHOTO_PICKED_WITH_DATA = 1002;
    private static final int REQUEST_CROP_PHOTO = 1003;

    //prize add for bug 54884 and 54865 by zhaojian 20180110 start
    private static final String PRIZE_GALLERY_PACKAGE_NAME  = "com.android.gallery3d";
    private static final String PRIZE_GALLERY_PICKER_PHOTO_ACTIVITY_NAME  = "com.android.gallery3d.app.DialogPicker";
    //prize add for bug 54884 and 54865 by zhaojian 20180110 end

    // Height and width (in pixels) to request for the photo - queried from the provider.
    private static int mPhotoDim;
    // Default photo dimension to use if unable to query the provider.
    private static final int mDefaultPhotoDim = 720;

    protected final Context mContext;
    private final View mChangeAnchorView;
    private final int mPhotoMode;
    private final int mPhotoPickSize;
    private final Uri mCroppedPhotoUri;
    private final Uri mTempPhotoUri;
    private final RawContactDeltaList mState;
    private final boolean mIsDirectoryContact;
    private ListPopupWindow mPopup;
    private PopupWindow mPopupWindow;//prize-add-huangliemin-2016-7-8
    private PhotoActionPopup mPAP;//prize-add for dido os8.0-hpf-2017-8-26

    public PhotoSelectionHandler(Context context, View changeAnchorView, int photoMode,
            boolean isDirectoryContact, RawContactDeltaList state) {
        mContext = context;
        mChangeAnchorView = changeAnchorView;
        mPhotoMode = photoMode;
        mTempPhotoUri = ContactPhotoUtils.generateTempImageUri(context);
        mCroppedPhotoUri = ContactPhotoUtils.generateTempCroppedImageUri(mContext);
        mIsDirectoryContact = isDirectoryContact;
        mState = state;
        mPhotoPickSize = getPhotoPickSize();
        mPAP = new PhotoActionPopup();//prize-add for dido os8.0-hpf-2017-8-26
    }

    public void destroy() {
        UiClosables.closeQuietly(mPopup);
    }

    public abstract PhotoActionListener getListener();

    @Override
    public void onClick(View v) {
		
        /*PRIZE-add -zhudaopeng-2016-8-11-start*/
        InputMethodManager im = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE); 
        im.hideSoftInputFromWindow(mChangeAnchorView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        /*PRIZE-add -zhudaopeng-2016-8-11-end*/
        final PhotoActionListener listener = getListener();
        if (listener != null) {
            if (getWritableEntityIndex() != -1) {
            	//prize-change-for-bottom-menu-huangliemin-2016-7-8-start
//                mPopup = PhotoActionPopup.createPopupMenu(
//                        mContext, mChangeAnchorView, listener, mPhotoMode);
//                mPopup.setOnDismissListener(new OnDismissListener() {
//                    @Override
//                    public void onDismiss() {
//                        listener.onPhotoSelectionDismissed();
//                    }
//                });
//				/*PRIZE-change-huangliemin-2016-6-16-start*/
//				mPopup.setVerticalOffset(160);
//				mPopup.setHorizontalOffset(-210);
//				/*PRIZE-change-huangliemin-2016-6-16-end*/
//                mPopup.show();
            	if(mPAP != null){
            		mPopupWindow = mPAP.createPopupWindow(mContext, listener, mPhotoMode);
            	}
            	if(mPopupWindow != null){
            		/*prize-change-huangliemin-2016-7-29-start*/
                	final Activity mActivity = (Activity)mContext;
                	WindowManager.LayoutParams params = mActivity.getWindow().getAttributes();
                	params.alpha=0.7f;
                	mActivity.getWindow().setAttributes(params);
                	mPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            			
            			@Override
            			public void onDismiss() {
            				// TODO Auto-generated method stub
            				WindowManager.LayoutParams params = mActivity.getWindow().getAttributes();
            				params.alpha=1f;
            				mActivity.getWindow().setAttributes(params);
            				listener.onPhotoSelectionDismissed();
            			}
            		});
                	/*
                	mPopupWindow.setOnDismissListener(new OnDismissListener() {
    					
    					@Override
    					public void onDismiss() {
    						// TODO Auto-generated method stub
    						listener.onPhotoSelectionDismissed();
    					}
    				});
    				*/
                	/*prize-change-huangliemin-2016-7-29-end*/
                	mPopupWindow.showAtLocation(mChangeAnchorView, Gravity.BOTTOM, 0, 0);
                	//prize-change-for-bottom-menu-huangliemin-2016-7-8-end
            	}
            	
            }
        }
    }

    /**
     * Attempts to handle the given activity result.  Returns whether this handler was able to
     * process the result successfully.
     * @param requestCode The request code.
     * @param resultCode The result code.
     * @param data The intent that was returned.
     * @return Whether the handler was able to process the result.
     */
    public boolean handlePhotoActivityResult(int requestCode, int resultCode, Intent data) {
        final PhotoActionListener listener = getListener();
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                // Cropped photo was returned
                case REQUEST_CROP_PHOTO: {
                    final Uri uri;
                    if (data != null && data.getData() != null) {
                        uri = data.getData();
                    } else {
                        uri = mCroppedPhotoUri;
                    }

                    try {
                        // delete the original temporary photo if it exists
                        mContext.getContentResolver().delete(mTempPhotoUri, null, null);
                        listener.onPhotoSelected(uri);
                        return true;
                    } catch (FileNotFoundException e) {
                        return false;
                    }
                }

                // Photo was successfully taken or selected from gallery, now crop it.
                case REQUEST_CODE_PHOTO_PICKED_WITH_DATA:
                case REQUEST_CODE_CAMERA_WITH_DATA:
                    final Uri uri;
                    boolean isWritable = false;
                    if (data != null && data.getData() != null) {
                        uri = data.getData();
                        /// M:fix ALPS01258109:if it is drm image, just return the
                        //  inputUri to CropImage @{
                        Log.d(TAG, "[handlePhotoActivityResult] uri:" + uri);
                        if (DrmUtils.isDrmImage(mContext, uri)) {
                            isWritable = true;
                        }
                        ///@}
                    } else {
                        uri = listener.getCurrentPhotoUri();
                        isWritable = true;
                    }
                    final Uri toCrop;
                    if (isWritable) {
                        // Since this uri belongs to our file provider, we know that it is writable
                        // by us. This means that we don't have to save it into another temporary
                        // location just to be able to crop it.
                        toCrop = uri;
                    } else {
                        toCrop = mTempPhotoUri;
                        try {
                            if (!ContactPhotoUtils.savePhotoFromUriToUri(mContext, uri,
                                            toCrop, false)) {
                                return false;
                            }
                        } catch (SecurityException e) {
                            Log.d(TAG, "Did not have read-access to uri : " + uri);
                            return false;
                        }
                    }

                    doCropPhoto(toCrop, mCroppedPhotoUri);
                    return true;
            }
        }
        Log.w(TAG, "[handlePhotoActivityResult]the result: " + resultCode + " is not for photo");
        return false;
    }

    /**
     * Return the index of the first entity in the contact data that belongs to a contact-writable
     * account, or -1 if no such entity exists.
     */
    private int getWritableEntityIndex() {
        // Directory entries are non-writable.
        if (mIsDirectoryContact) return -1;
        return mState.indexOfFirstWritableRawContact(mContext);
    }

    /**
     * Return the raw-contact id of the first entity in the contact data that belongs to a
     * contact-writable account, or -1 if no such entity exists.
     */
    protected long getWritableEntityId() {
        int index = getWritableEntityIndex();
        if (index == -1) return -1;
        return mState.get(index).getValues().getId();
    }

    /**
     * Utility method to retrieve the entity delta for attaching the given bitmap to the contact.
     * This will attach the photo to the first contact-writable account that provided data to the
     * contact.  It is the caller's responsibility to apply the delta.
     * @return An entity delta list that can be applied to associate the bitmap with the contact,
     *     or null if the photo could not be parsed or none of the accounts associated with the
     *     contact are writable.
     */
    public RawContactDeltaList getDeltaForAttachingPhotoToContact() {
        // Find the first writable entity.
        int writableEntityIndex = getWritableEntityIndex();
        if (writableEntityIndex != -1) {
            // We are guaranteed to have contact data if we have a writable entity index.
            final RawContactDelta delta = mState.get(writableEntityIndex);

            // Need to find the right account so that EntityModifier knows which fields to add
            final ContentValues entityValues = delta.getValues().getCompleteValues();
            final String type = entityValues.getAsString(RawContacts.ACCOUNT_TYPE);
            final String dataSet = entityValues.getAsString(RawContacts.DATA_SET);
            final AccountType accountType = AccountTypeManager.getInstance(mContext).getAccountType(
                        type, dataSet);

            final ValuesDelta child = RawContactModifier.ensureKindExists(
                    delta, accountType, Photo.CONTENT_ITEM_TYPE);
            child.setFromTemplate(false);
            child.setSuperPrimary(true);

            return mState;
        }
        return null;
    }

    /** Used by subclasses to delegate to their enclosing Activity or Fragment. */
    protected abstract void startPhotoActivity(Intent intent, int requestCode, Uri photoUri);

    /**
     * Sends a newly acquired photo to Gallery for cropping
     */
    private void doCropPhoto(Uri inputUri, Uri outputUri) {
        final Intent intent = getCropImageIntent(inputUri, outputUri);
        if (!hasIntentHandler(intent)) {
            try {
                getListener().onPhotoSelected(inputUri);
            } catch (FileNotFoundException e) {
                Log.e(TAG, "Cannot save uncropped photo", e);
                Toast.makeText(mContext, R.string.contactPhotoSavedErrorToast,
                        Toast.LENGTH_LONG).show();
            }
            return;
        }
        try {
            // Launch gallery to crop the photo
            startPhotoActivity(intent, REQUEST_CROP_PHOTO, inputUri);
        } catch (Exception e) {
            Log.e(TAG, "Cannot crop image", e);
            Toast.makeText(mContext, R.string.photoPickerNotFoundText, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Should initiate an activity to take a photo using the camera.
     * @param photoFile The file path that will be used to store the photo.  This is generally
     *     what should be returned by
     *     {@link PhotoSelectionHandler.PhotoActionListener#getCurrentPhotoFile()}.
     */
    private void startTakePhotoActivity(Uri photoUri) {
        final Intent intent = getTakePhotoIntent(photoUri);
        startPhotoActivity(intent, REQUEST_CODE_CAMERA_WITH_DATA, photoUri);
    }

    /**
     * Should initiate an activity pick a photo from the gallery.
     * @param photoFile The temporary file that the cropped image is written to before being
     *     stored by the content-provider.
     *     {@link PhotoSelectionHandler#handlePhotoActivityResult(int, int, Intent)}.
     */
    private void startPickFromGalleryActivity(Uri photoUri) {
        final Intent intent = getPhotoPickIntent(photoUri);
        /** M: Bug Fix for CR ALPS00335098 @{ */
        if (ContactsSystemProperties.MTK_DRM_SUPPORT) {
            Log.d(TAG, "[startPickFromGalleryActivity] use DRM intent : " + intent);
            final String drm_level = "android.intent.extra.drm_level";
            final int level_fl = 1;
            intent.putExtra(drm_level, level_fl);
        }
        /** @} */
        startPhotoActivity(intent, REQUEST_CODE_PHOTO_PICKED_WITH_DATA, photoUri);
    }

    private int getPhotoPickSize() {
        if (mPhotoDim != 0) {
            return mPhotoDim;
        }

        // Note that this URI is safe to call on the UI thread.
        Cursor c = mContext.getContentResolver().query(DisplayPhoto.CONTENT_MAX_DIMENSIONS_URI,
                new String[]{DisplayPhoto.DISPLAY_MAX_DIM}, null, null, null);
        if (c != null) {
            try {
                if (c.moveToFirst()) {
                    mPhotoDim = c.getInt(0);
                }
            } finally {
                c.close();
            }
        }
        return mPhotoDim != 0 ? mPhotoDim : mDefaultPhotoDim;
    }

    /**
     * Constructs an intent for capturing a photo and storing it in a temporary output uri.
     */
    private Intent getTakePhotoIntent(Uri outputUri) {
        final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE, null);
        ContactPhotoUtils.addPhotoPickerExtras(intent, outputUri);
        return intent;
    }

    /**
     * Constructs an intent for picking a photo from Gallery, and returning the bitmap.
     */
    private Intent getPhotoPickIntent(Uri outputUri) {
        final Intent intent = new Intent(Intent.ACTION_PICK, null);
        //prize add for bug 54884 and 54865 by zhaojian 20180110 start
        intent.setClassName(PRIZE_GALLERY_PACKAGE_NAME,PRIZE_GALLERY_PICKER_PHOTO_ACTIVITY_NAME);
        //prize add for bug 54884 and 54865 by zhaojian 20180110 end
        intent.setType("image/*");
        ContactPhotoUtils.addPhotoPickerExtras(intent, outputUri);
        return intent;
    }

    private boolean hasIntentHandler(Intent intent) {
        final List<ResolveInfo> resolveInfo = mContext.getPackageManager()
                .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return resolveInfo != null && resolveInfo.size() > 0;
    }

    /**
     * Constructs an intent for image cropping.
     */
    private Intent getCropImageIntent(Uri inputUri, Uri outputUri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(inputUri, "image/*");
        ContactPhotoUtils.addPhotoPickerExtras(intent, outputUri);
        ContactPhotoUtils.addCropExtras(intent, mPhotoPickSize);
        return intent;
    }

    public abstract class PhotoActionListener implements PhotoActionPopup.Listener {
        @Override
        public void onRemovePictureChosen() {
            // No default implementation.
        }

        @Override
        public void onTakePhotoChosen() {
            try {
                // Launch camera to take photo for selected contact
                startTakePhotoActivity(mTempPhotoUri);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(
                        mContext, R.string.photoPickerNotFoundText, Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onPickFromGalleryChosen() {
            try {
                // Launch picker to choose photo for selected contact
                startPickFromGalleryActivity(mTempPhotoUri);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(
                        mContext, R.string.photoPickerNotFoundText, Toast.LENGTH_LONG).show();
            }
        }

        /**
         * Called when the user has completed selection of a photo.
         * @throws FileNotFoundException
         */
        public abstract void onPhotoSelected(Uri uri) throws FileNotFoundException;

        /**
         * Gets the current photo file that is being interacted with.  It is the activity or
         * fragment's responsibility to maintain this in saved state, since this handler instance
         * will not survive rotation.
         */
        public abstract Uri getCurrentPhotoUri();

        /**
         * Called when the photo selection dialog is dismissed.
         */
        public abstract void onPhotoSelectionDismissed();
    }
}
