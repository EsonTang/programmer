/**
 * PrizeGroupBrowseListAdapter.java [V 1.0.0]
 * classes : com.android.contacts.prize.PrizeGroupBrowseListAdapter
 * huangliemin Create at 2016-7-5 3:07:23
 */
package com.android.contacts.prize;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.android.contacts.GroupListLoader;
import com.android.contacts.group.GroupListItem;
import com.android.contacts.prize.PrizeGroupMemberTileAdapter;
import com.mediatek.contacts.group.SimGroupUtils;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Rect;
import android.net.Uri;
import android.provider.Contacts.Groups;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
//import android.widget.ListView;
import com.android.contacts.prize.PrizeGridListView;
import android.widget.TextView;

import com.google.common.base.Objects;
import com.android.contacts.R;
import com.android.contacts.activities.GroupEditorActivity;
import com.android.contacts.common.model.AccountTypeManager;
import com.android.contacts.common.model.account.AccountWithDataSet;
import com.mediatek.contacts.model.AccountWithDataSetEx;
import com.mediatek.contacts.simcontact.SubInfoUtils;

import com.android.contacts.common.list.ContactTileView;
import com.android.contacts.common.ContactPhotoManager;
import com.android.contacts.common.list.ContactTileAdapter;
import com.android.contacts.common.util.ImplicitIntentsUtil;

/**
 * com.android.contacts.prize.PrizeGroupBrowseListAdapter
 * @author huangliemin <br/>
 * create at 2016-7-5 3:07:23
 */
public class PrizeGroupBrowseListAdapter extends BaseExpandableListAdapter {
	
	private static final String TAG = "ContactsGroup-PrizeGroupBrowseListAdapter";
	private final Context mContext;
	private final LayoutInflater mLayoutInflater;
	
	private Cursor mCursor;
	
	private ArrayList<String> mAccountNameList = new ArrayList<String>();
	private HashMap<String, Integer> mAccountGroupMembers = new HashMap<String, Integer>();
	
	/*prize-add-huangliemin-for-child-view-2016-7-6-start*/
	private HashMap<Integer, ContactTileAdapter> mChildDataMap = new HashMap<Integer, ContactTileAdapter>();
	private static int ViewHeight;
	private static int lastIndex = -1;
	/*prize-add-huangliemin-for-child-view-2016-7-6-end*/
	public PrizeGroupMemberTileAdapter mMemberAdapter = null;//prize-add for dido os8.0-hpf-2018-1-8
	
	public PrizeGroupBrowseListAdapter(Context context) {
		mContext = context;
		mLayoutInflater = LayoutInflater.from(context);
	}
	
	public void setCursor(Cursor cursor) {
		Log.i(TAG, "setCursor");
		mCursor = cursor;
		SimGroupUtils.initGroupsByAllAccount(mCursor, mAccountNameList, mAccountGroupMembers);
		notifyDataSetChanged();
	}

	/* (non-Javadoc)
	 * @see android.widget.ExpandableListAdapter#getGroupCount()
	 */
	@Override
	public int getGroupCount() {
		Log.i(TAG, "getGroupCount: "+((mCursor == null || mCursor.isClosed()) ? 0 : mCursor.getCount()));
		return (mCursor == null || mCursor.isClosed()) ? 0 : mCursor.getCount();
	}

	/* (non-Javadoc)
	 * @see android.widget.ExpandableListAdapter#getChildrenCount(int)
	 */
	@Override
	public int getChildrenCount(int groupPosition) {
		return 1;//getGroup(groupPosition).getMemberCount();
	}

	/* (non-Javadoc)
	 * @see android.widget.ExpandableListAdapter#getGroup(int)
	 */
	@Override
	public GroupListItem getGroup(int groupPosition) {
		Log.d(TAG, "[getGroup]   groupPosition = "+groupPosition);
		if(mCursor == null || mCursor.isClosed() || !mCursor.moveToPosition(groupPosition)) {
			Log.e(TAG, "mCursor: "+mCursor+" , position: "+groupPosition);
			return null;
		}
		
		String accountName = mCursor.getString(GroupListLoader.ACCOUNT_NAME);
        String accountType = mCursor.getString(GroupListLoader.ACCOUNT_TYPE);
        String dataSet = mCursor.getString(GroupListLoader.DATA_SET);
        long groupId = mCursor.getLong(GroupListLoader.GROUP_ID);
        String title = mCursor.getString(GroupListLoader.TITLE);
        int memberCount = mCursor.getInt(GroupListLoader.MEMBER_COUNT);
        int accountGroupMemberCount = mAccountGroupMembers.get(accountName);
        
     // Figure out if this is the first group for this account name / account type pair by
        // checking the previous entry. This is to determine whether or not we need to display an
        // account header in this item.
        int previousIndex = groupPosition - 1;
        boolean isFirstGroupInAccount = true;
        if (previousIndex >= 0 && mCursor.moveToPosition(previousIndex)) {
            String previousGroupAccountName = mCursor.getString(GroupListLoader.ACCOUNT_NAME);
            String previousGroupAccountType = mCursor.getString(GroupListLoader.ACCOUNT_TYPE);
            String previousGroupDataSet = mCursor.getString(GroupListLoader.DATA_SET);

            if (accountName.equals(previousGroupAccountName) &&
                    accountType.equals(previousGroupAccountType) &&
                    Objects.equal(dataSet, previousGroupDataSet)) {
                isFirstGroupInAccount = false;
            }
        }

        return new GroupListItem(accountName, accountType, dataSet, groupId, title,
                isFirstGroupInAccount, memberCount, accountGroupMemberCount);
	}

	/* (non-Javadoc)
	 * @see android.widget.ExpandableListAdapter#getChild(int, int)
	 */
	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return null;
	}

	/* (non-Javadoc)
	 * @see android.widget.ExpandableListAdapter#getGroupId(int)
	 */
	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	/* (non-Javadoc)
	 * @see android.widget.ExpandableListAdapter#getChildId(int, int)
	 */
	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return 0;
	}

	/* (non-Javadoc)
	 * @see android.widget.ExpandableListAdapter#hasStableIds()
	 */
	@Override
	public boolean hasStableIds() {
		return false;
	}

	/* (non-Javadoc)
	 * @see android.widget.ExpandableListAdapter#getGroupView(int, boolean, android.view.View, android.view.ViewGroup)
	 */
	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		GroupListItem entry = getGroup(groupPosition);
		View result;
		GroupListItemViewCache viewCache;
		if(convertView != null) {
			result = convertView;
			viewCache = (GroupListItemViewCache) result.getTag();
		} else {
			result = mLayoutInflater.inflate(getGroupListItemLayout(), parent, false);
			viewCache = new GroupListItemViewCache(result);
			result.setTag(viewCache);
		}
		
		/*prize-add for dido os8.0-hpf-2017-8-7-start*/
		if(isExpanded){
			result.findViewById(R.id.prize_group_divider).setVisibility(View.GONE);
		}else{
			result.findViewById(R.id.prize_group_divider).setVisibility(View.VISIBLE);
		}
		/*prize-add for dido os8.0-hpf-2017-8-7-end*/
		
		String memberCountString = mContext.getResources().getQuantityString(
				R.plurals.prize_group_list_num_contacts_in_group, entry.getMemberCount(),
				entry.getMemberCount());
		viewCache.groupTitle.setText(entry.getTitle());
		viewCache.groupMemberCount.setText(memberCountString);
		return result;
	}

	/* (non-Javadoc)
	 * @see android.widget.ExpandableListAdapter#getChildView(int, int, boolean, android.view.View, android.view.ViewGroup)
	 */
	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		View result;
		ChildListItemViewCache viewCache;
		/*prize-change for dido os8.0-hpf-2018-1-8-start*/
		//ContactTileAdapter MemberAdapter;
		if(convertView != null) {
			result = convertView;
			viewCache = (ChildListItemViewCache) result.getTag();
		} else { 
			result = mLayoutInflater.inflate(getChildListItemLayout(), parent, false);
			viewCache= new ChildListItemViewCache(result);
			result.setTag(viewCache);
		}
		int columnCount = mContext.getResources().getInteger(R.integer.contact_tile_column_count);
		int group_id =(int)(getGroup(groupPosition).getGroupId());
		mMemberAdapter = new PrizeGroupMemberTileAdapter(mContext, mContactTileListener, columnCount);
		configurePhotoLoader(mMemberAdapter, viewCache.childList);
		viewCache.childList.setAdapter(mMemberAdapter);
		if(mOnCursorRequestListener != null){
			Log.d(TAG, "getChildView  111");
			mOnCursorRequestListener.onCursorRequest(group_id);
		}
		/*if(mChildDataMap!=null && !mChildDataMap.containsKey(group_id)) {
			MemberAdapter = new PrizeGroupMemberTileAdapter(mContext, mContactTileListener, columnCount);
			configurePhotoLoader(MemberAdapter, viewCache.childList);
			viewCache.childList.setAdapter(MemberAdapter);
			//MemberAdapter.setContactCursor(null);
			setChildData(group_id, MemberAdapter);
			if(mOnCursorRequestListener != null){
				Log.d(TAG, "getChildView  111");
				mOnCursorRequestListener.onCursorRequest(group_id);
			}
		} else {
			MemberAdapter = (PrizeGroupMemberTileAdapter)viewCache.childList.getAdapter();
			//MemberAdapter.setContactCursor(null);
			if(MemberAdapter == null){
				MemberAdapter = new PrizeGroupMemberTileAdapter(mContext, mContactTileListener, columnCount);
				viewCache.childList.setAdapter(MemberAdapter);
				setChildData(group_id, MemberAdapter);
				if(mOnCursorRequestListener != null){
					Log.d(TAG, "getChildView  222");
					mOnCursorRequestListener.onCursorRequest(group_id);
				}
			}
		}*/
		/*prize-change for dido os8.0-hpf-2018-1-8-end*/
		
		return result;
	}
	
	//prize-add-huangliemin-for-child-view-2016-7-6-start
	public void setFragmentViewHeight(int height) {
		ViewHeight = height;
	}
	
	private void setChildData(int groupId, ContactTileAdapter adapter) {
		mChildDataMap.put(groupId, adapter);
	}
	
	public void setLastGroupPosition(int groupPosition) {
		lastIndex = groupPosition;
	}
	
	public ContactTileAdapter getChildData(int groupId) {
		Log.d(TAG,"[getChildData] groupId = "+groupId+
				"   mChildDataMap = "+mChildDataMap);
		if(mChildDataMap.get(groupId)!=null) {
			return mChildDataMap.get(groupId);
		}
		
		return null;
	}
	
	private final ContactTileView.Listener mContactTileListener = 
			new ContactTileView.Listener() {
		
		@Override
        public void onContactSelected(Uri contactUri, Rect targetRect) {
            //mListener.onContactSelected(contactUri);
			Log.i("logtest", "contactUri: "+contactUri);
			if(contactUri!=null) {
				ViewContact(contactUri);
			} else {
				if(lastIndex!=-1) {
					GroupListItem item = getGroup(lastIndex);
					EditGroup(item.getGroupId(), item.getMemberCount());
				}
			}
        }

        @Override
        public void onCallNumberDirectly(String phoneNumber) {
            // No need to call phone number directly from People app.
            Log.w(TAG, "unexpected invocation of onCallNumberDirectly()");
        }

        @Override
        public int getApproximateTileWidth() {
            //return getView().getWidth() / mAdapter.getColumnCount();
        	int columnCount = mContext.getResources().getInteger(R.integer.contact_tile_column_count);
        	return ViewHeight/columnCount;
        }
	};
	
	public void ViewContact(Uri contactUri) {
		Intent intent = new Intent(Intent.ACTION_VIEW, contactUri);
		ImplicitIntentsUtil.startActivityInApp(mContext, intent);
	}
	
	public void EditGroup(long groupId, int groupnums) {
		
		final Intent intent = new Intent(mContext, GroupEditorActivity.class);
		/*
		int msubId = Integer.parseInt(groupUri.getLastPathSegment().toString());
		String grpId = groupUri.getPathSegments().get(1).toString();
		*/
		Uri uri = Uri.parse("content://com.android.contacts/groups").buildUpon()
				.appendPath(String.valueOf(groupId)).build();
		intent.setData(uri);
		intent.setAction(Intent.ACTION_EDIT);
		//intent.putExtra("SIM_ID", msubId);
		intent.putExtra("GROUP_NUMS", groupnums);
		mContext.startActivity(intent);
	}
	
	private void configurePhotoLoader(ContactTileAdapter adapter, PrizeGridListView listview) {
		if(mContext!=null) {
			ContactPhotoManager mPhotoManager = ContactPhotoManager.getInstance(mContext);
			if(listview!=null) {
				listview.setOnScrollListener(new GridViewScrollListener(mPhotoManager));
			}
			if(adapter!=null) {
				adapter.setPhotoLoader(mPhotoManager);
			}
		}
	}
	
	class GridViewScrollListener implements AbsListView.OnScrollListener {
		private ContactPhotoManager mPhotoManager;
		public GridViewScrollListener (ContactPhotoManager photoManager) {
			mPhotoManager = photoManager;
		}

		/* (non-Javadoc)
		 * @see android.widget.AbsListView.OnScrollListener#onScrollStateChanged(android.widget.AbsListView, int)
		 */
		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			if(scrollState == OnScrollListener.SCROLL_STATE_FLING) {
				mPhotoManager.pause();
			} else {
				mPhotoManager.resume();
			}
		}

		/* (non-Javadoc)
		 * @see android.widget.AbsListView.OnScrollListener#onScroll(android.widget.AbsListView, int, int, int)
		 */
		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {
			
		}
		
	}
	//prize-add-huangliemin-2016-7-6-for-child-view-end

	/* (non-Javadoc)
	 * @see android.widget.ExpandableListAdapter#isChildSelectable(int, int)
	 */
	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return false;
	}

	public static class GroupListItemViewCache {
		public final TextView groupTitle;
		public final TextView groupMemberCount;
		
		public GroupListItemViewCache(View view) {
			groupTitle = (TextView) view.findViewById(R.id.label);
			groupMemberCount = (TextView) view.findViewById(R.id.count);
		}
	}
	
	public static class ChildListItemViewCache {
		public final PrizeGridListView childList;
		
		public ChildListItemViewCache(View view) {
			childList = (PrizeGridListView) view.findViewById(android.R.id.list);
		}
	}
	
	protected int getGroupListItemLayout() {
		return R.layout.prize_group_browse_list_group_item_2016_7_5;
	}
	
	protected int getChildListItemLayout() {
		return R.layout.prize_group_detail_fragment_2016_7_6;
	}
	
	//prize-add-huangliemin-for-get-groupuri-2016-7-6-start
	public Uri getGroupUri(int groupPosition){
		GroupListItem entry  = getGroup(groupPosition);
		Uri groupUri = getGroupUriFromIdAndAccountInfo(entry.getGroupId(), entry.getAccountName(),
				entry.getAccountType());
		return groupUri;
	}
	
	private Uri getGroupUriFromIdAndAccountInfo(long groupId, String accountName,
			String accountType) {
		Uri retUri = ContentUris.withAppendedId(Groups.CONTENT_URI, groupId);
		if(accountName!=null && accountType!=null) {
			retUri = groupUriWithAccountInfo(retUri, accountName, accountType);
		}
		
		return retUri;
	}
	
	// The following lines are provided and maintained by Mediatek Inc.
    private Uri groupUriWithAccountInfo(final Uri groupUri, String accountName,
            String accountType) {
        if (groupUri == null) {
            return groupUri;
        }

        Uri retUri = groupUri;

        AccountWithDataSet account = null;
        final List<AccountWithDataSet> accounts = AccountTypeManager.getInstance(mContext)
                .getGroupWritableAccounts();
        int i = 0;
        int subId = SubInfoUtils.getInvalidSubId();
        for (AccountWithDataSet ac : accounts) {
            if (ac.name.equals(accountName) && ac.type.equals(accountType)) {
                account = accounts.get(i);
                if (account instanceof AccountWithDataSetEx) {
                    subId = ((AccountWithDataSetEx) account).getSubId();
                }
            }
            i++;
        }
        retUri = groupUri.buildUpon().appendPath(String.valueOf(subId)).appendPath(accountName)
                .appendPath(accountType).build();

        return retUri;
    }
    //prize-add-huangliemin-for-get-groupuri-2016-7-6-end
    
    /*prize-add for dido os8.0-hpf-2017-9-1-start*/
    private OnCursorRequestListener mOnCursorRequestListener;
    public interface OnCursorRequestListener{
    	void onCursorRequest(int groupId);
    }
    public void setOnCursorRequestListener(OnCursorRequestListener onCursorRequestListener){
    	mOnCursorRequestListener = onCursorRequestListener;
    }
    /*prize-add for dido os8.0-hpf-2017-9-1-end*/
}
