/**
 * PrizeGroupMemberTileAdapter.java [V 1.0.0]
 * classes : com.android.contacts.prize.PrizeGroupMemberTileAdapter
 * huangliemin Create at 2016-7-6 3:41:35
 */
package com.android.contacts.prize;

/**
 * com.android.contacts.prize.PrizeGroupMemberTileAdapter
 * @author huangliemin <br/>
 * create at 2016-7-6 3:41:35
 */
import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.contacts.GroupMemberLoader;
import com.android.contacts.common.list.ContactEntry;
import com.prize.contacts.common.list.PrizeContactTileAdapter;
import com.android.contacts.common.list.ContactTileView;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import com.android.contacts.R;

public class PrizeGroupMemberTileAdapter extends PrizeContactTileAdapter {
	private String TAG = "ContactsGroup-PrizeGroupMemberTileAdapter";
	private Context mContext;

    public PrizeGroupMemberTileAdapter(Context context, ContactTileView.Listener listener, int numCols) {
        super(context, listener, numCols, DisplayType.GROUP_MEMBERS);
        Log.d(TAG, "[PrizeGroupMemberTileAdapter]");
        mContext = context;
    }

    @Override
    protected void bindColumnIndices() {
        mIdIndex = GroupMemberLoader.GroupDetailQuery.CONTACT_ID;
        mLookupIndex = GroupMemberLoader.GroupDetailQuery.CONTACT_LOOKUP_KEY;
        mPhotoUriIndex = GroupMemberLoader.GroupDetailQuery.CONTACT_PHOTO_URI;
        mNameIndex = GroupMemberLoader.GroupDetailQuery.CONTACT_DISPLAY_NAME_PRIMARY;
        mPresenceIndex = GroupMemberLoader.GroupDetailQuery.CONTACT_PRESENCE_STATUS;
        mStatusIndex = GroupMemberLoader.GroupDetailQuery.CONTACT_STATUS;
    }

    @Override
    protected void saveNumFrequentsFromCursor(Cursor cursor) {
        mNumFrequents = 0;
    }

    @Override
    public int getItemViewType(int position) {
        return ViewTypes.STARRED;
    }

    @Override
    protected int getDividerPosition(Cursor cursor) {
        // No divider
        return -1;
    }

    @Override
    public int getCount() {
    	Log.d(TAG, "[getCount]  mContactCursor == null? "+(mContactCursor == null));
    	if(mContactCursor != null){
    		Log.d(TAG, "[getCount]  mContactCursor.isClosed() = "+mContactCursor.isClosed());
    	}
        if (mContactCursor == null || mContactCursor.isClosed()) {
            return 0;
            
        }
        int count = getRowCount(mContactCursor.getCount()+1);
        Log.d(TAG, "[getCount]  count = "+count);
        return count;
    }

    @Override
    public ArrayList<ContactEntry> getItem(int position) {
        final ArrayList<ContactEntry> resultList = Lists.newArrayListWithCapacity(mColumnCount);
        int contactIndex = position * mColumnCount;
        
        for (int columnCounter = 0; columnCounter < mColumnCount; columnCounter++) {
            resultList.add(createContactEntryFromCursor(mContactCursor, contactIndex));
            contactIndex++;
        }
        
        return resultList;
    }

}