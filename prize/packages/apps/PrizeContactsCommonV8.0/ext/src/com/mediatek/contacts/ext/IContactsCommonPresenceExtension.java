package com.mediatek.contacts.ext;

import com.android.contacts.common.list.ContactListItemView;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.view.ViewGroup;

public interface IContactsCommonPresenceExtension {
    /**
     * Checks if contact is video call capable.
     * @param number number.
     * @return true if contact is video call capable.
     */
    boolean isVideoCallCapable(String number);

    /**
     * Checks if plugin is active to show video icon.
     * @return true if op08 plugin active, otherwise false.
     */
    boolean isShowVideoIcon();

    /**
     * Set Video Icon alpha value.
     * @param number contact number.
     * @param thirdIcon video icon.
     */
    void setVideoIconAlpha(String number, Drawable thirdIcon);

    /**
     * Checks if any number in contactId is video call capable,
     * if capable, add the view in contact list item.
     * @param contactId Contact Id.
     * @param viewGroup host view.
     */
    void addVideoCallView(long contactId, ViewGroup viewGroup);
    /**
    * @param widthMeasureSpec
    * @param heightMeasureSpec
    */
   void onMeasure(int widthMeasureSpec, int heightMeasureSpec);

   /**
    * @param changed
    * @param leftBound
    * @param topBound
    * @param rightBound
    * @param bottomBound
    */
   void onLayout(boolean changed, int leftBound, int topBound, int rightBound,
           int bottomBound);
   /**
    * getWidthWithPadding.
    * @return padding width
    */
    int getWidthWithPadding();

    /**
     * processIntent
     * @param intent contains contact info
     */
    void processIntent(Intent intent);

    void bindPhoneNumber(ContactListItemView view, Cursor cursor);
}
