/**
 * PrizeGroupBrowseListFragment.java [V 1.0.0]
 * classes : com.android.contacts.prize.PrizeGroupBrowseListFragment
 * huangliemin Create at 2016-7-5 11:33:26
 */
package com.android.contacts.prize;

import java.io.InputStream;
import java.util.List;

import com.android.contacts.GroupListLoader;
import com.android.contacts.GroupMemberLoader;
import com.android.contacts.GroupMetaDataLoader;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Groups;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Profile;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ExpandableListView.OnGroupCollapseListener;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.contacts.R;
import com.android.contacts.activities.GroupDetailActivity;
import com.android.contacts.group.GroupListItem;
import com.android.contacts.prize.PrizeGroupBrowseListAdapter;
import com.mediatek.contacts.activities.GroupBrowseActivity.AccountCategoryInfo;
import com.mediatek.contacts.simcontact.SubInfoUtils;
import com.android.contacts.editor.ContactEditorFragment;

/*prize-add-create-account-huangliemin-2016-7-14-start*/
import com.mediatek.contacts.model.AccountWithDataSetEx;
import com.android.contacts.common.model.account.AccountWithDataSet;
import com.android.contacts.common.model.AccountTypeManager;
import com.mediatek.contacts.ExtensionManager;
import com.android.contacts.common.util.ImplicitIntentsUtil;
/*prize-add-create-account-huangliemin-2016-7-14-end*/
import android.view.inputmethod.InputMethodManager;//prize-add-hpf-2018-1-5

//prize add by zhaojian for bug 53044 20180319 start
import android.content.res.ColorStateList;
//prize add by zhaojian for bug 53044 20180319 end

/**
 * com.android.contacts.prize.PrizeGroupBrowseListFragment
 * @author huangliemin <br/>
 * create at 2016-7-5 11:33:26
 */
public class PrizeGroupBrowseListFragment extends Fragment {
	private static final String TAG = "ContactsGroup-PrizeGroupBrowseListFragment";
	private static final int LOADER_GROUPS = -2;//prize-change-(-2)-huangliemin-2016-7-6
	private static final int LOADER_PROFILE = -4;//prize-add-huangliemin-2016-7-6
	private final String LAST_POSITION = "last_position";
	
	private Context mContext;
    private Cursor mGroupListCursor;
    
    private PrizeGroupBrowseListAdapter mAdapter;
    private View mRootView;
    private ExpandableListView mListView;
    
    //prize-add-huangliemin-2016-7-14-start
    //private ImageButton mNewGroupButton;//prize-remove-huangpengfei-2016-9-12
    private Dialog mDialog;
    private EditText mDialogEdit;
    public String ProfileLookUpUri;
    //prize-add-huangliemin-2016-7-14-end
    
    private LinearLayout mNewGroupLayout;//prize-add-huangpengfei-2016-9-12
    
    private int lastindex = -1;//prize-add-huangliemin-2016-7-6
    
    /*prize-add-huangliemin-2016-7-26-start*/
    private ImageView MyInfoPhoto;
    private TextView MyInfoTitle;
    /*prize-add-huangliemin-2016-7-26-end*/
    
    public PrizeGroupBrowseListFragment(){
    	
    }

	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d(TAG, "[onCreateView]");
		mRootView = inflater.inflate(R.layout.prize_group_browse_list_fragment, null);
		mAdapter = new PrizeGroupBrowseListAdapter(mContext);
		mListView = (ExpandableListView) mRootView.findViewById(R.id.list);
		mListView.setAdapter(mAdapter);
		/*prize-add for dido os 8.0-2017-9-12-start*/
		final InputMethodManager imm = (InputMethodManager) mContext.getSystemService(
                Context.INPUT_METHOD_SERVICE);
		mAdapter.setOnCursorRequestListener(new PrizeGroupBrowseListAdapter.OnCursorRequestListener(){
			
			@Override
			public void onCursorRequest(int groupId){
				startGroupMembersLoader(groupId);
			}
		});
		/*prize-add for dido os 8.0-2017-9-12-end*/
		/*prize-add-huangliemin-2016-7-26-start*/
        MyInfoPhoto = (ImageView)mRootView.findViewById(R.id.myinfo_photo);
        MyInfoTitle = (TextView)mRootView.findViewById(R.id.myinfo_title);
        MyInfoPhoto.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startProfileActivity();
			}
		});
        MyInfoTitle.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startProfileActivity();
			}
		});
        /*prize-add-huangliemin-2016-7-26-end*/
        
        /*prize-add-huangpengfei-2016-9-12-start*/
        mNewGroupLayout =  (LinearLayout)mRootView.findViewById(R.id.prize_new_group_layout);
        mNewGroupLayout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mDialog = createNewGroupDialog();
				if(mDialog!=null && !mDialog.isShowing()) {
					mDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
						
						@Override
						public void onDismiss(DialogInterface dialog) {
                            if(getActivity() != null) {
                                InputMethodManager imm = (InputMethodManager) getActivity().
                                    getSystemService(Context.INPUT_METHOD_SERVICE);
                            }
							if(imm != null){
								imm.prizeHideSoftInput(InputMethodManager.HIDE_NOT_ALWAYS,null); 
							}
						}
					});
					mDialog.show();
					if (imm != null && mDialogEdit != null) {
						mDialogEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
							
							@Override
							public void onFocusChange(View v, boolean hasFocus) {
								if(hasFocus){
									new Handler().postDelayed(new Runnable() {
										
										@Override
										public void run() {
											if(imm.showSoftInput(v, 0)){
								            	Log.d(TAG, "[onFocusChange] showSoftInput true");
								            }else{
								            	Log.d(TAG, "[onFocusChange] showSoftInput false");
								            }
										}
									}, 200);
								}
							}
						});
						mDialogEdit.requestFocus();
			        }
				}
			}
		});
        /*prize-add-huangpengfei-2016-9-12-end*/
    
		mListView.setOnGroupCollapseListener(new OnGroupCollapseListener() {
			
			@Override
			public void onGroupCollapse(int groupPosition) {
				Log.d(TAG, "[onGroupCollapse] groupPosition : "+groupPosition);
				if(lastindex == groupPosition) {
					lastindex = -1;
				}
			}
		});
		
		mListView.setOnGroupExpandListener(new OnGroupExpandListener() {
			
			@Override
			public void onGroupExpand(int groupPosition) {
				/*
				Uri gourpUri = mAdapter.getGroupUri(groupPosition);
				onViewGroupAction(gourpUri);
				*/
				Log.d(TAG, "[onGroupExpand]  groupPosition: "+groupPosition);
				
				if(lastindex!=-1) {
					mListView.collapseGroup(lastindex);
				}
				
				mAdapter.setLastGroupPosition(groupPosition);
				mAdapter.notifyDataSetChanged();
				/*prize-remove-for dido os8.0-hpf-2017-9-14-start*/
				/*GroupListItem item = mAdapter.getGroup(groupPosition);
				if (item != null) {
					startGroupMembersLoader((int) item.getGroupId());
					if (mAdapter.getChildData((int) item.getGroupId()) != null) {
						mAdapter.getChildData((int) item.getGroupId()).setContactCursor(null);
					}
				}*/
				/*prize-remove-for dido os8.0-hpf-2017-9-14-end*/
				lastindex = groupPosition;
				
			}
		});
		getLoaderManager().restartLoader(LOADER_GROUPS, null, mGroupLoaderListener);
		return mRootView;
	}
	
	/*prize-add-show-dialog-huangliemin-2016-7-14-start*/
	public Dialog createNewGroupDialog() {
		AlertDialog.Builder mBuilder = new AlertDialog.Builder(mContext);
		mBuilder.setTitle(getResources().getString(R.string.prize_new_group_dialog_title));
		mBuilder.setPositiveButton(android.R.string.ok, new GroupDialogListener(mContext));
		mBuilder.setNegativeButton(android.R.string.cancel,null);
		View mDialogContent = LayoutInflater.from(mContext).inflate(R.layout.prize_group_dialog_content, null);
		mDialogEdit = (EditText)mDialogContent.findViewById(R.id.group_dialog_edit);
		mBuilder.setView(mDialogContent);
		return mBuilder.create();
	}
	
	
	
	class GroupDialogListener implements DialogInterface.OnClickListener {
		private Context mDialogContext;
		
		public GroupDialogListener(Context context) {
			mDialogContext = context;
		}

		
		@Override
		public void onClick(DialogInterface dialog, int which) {
			if(mDialogEdit!=null) {
				String groupname= mDialogEdit.getText().toString().trim();
				if(!TextUtils.isEmpty(groupname)) {
					boolean isSuccess = createGroup(groupname);
					if(!isSuccess) {
						Toast.makeText(mDialogContext, R.string.prize_new_group_name_toast_2, Toast.LENGTH_LONG).show();
					}
				} else {
					Toast.makeText(mDialogContext, R.string.prize_new_group_name_toast, Toast.LENGTH_LONG).show();
				}
			}
		}
	}
  /*prize-add-huangliemin-2016-7-26-start*/
	
	public void startProfileActivity() {
		Log.d(TAG, "[startProfileActivity]");
		if(ExtensionManager.getInstance().getRcsExtension()
				.addRcsProfileEntryListener(null, true)) {
			return;
		}
		
		if(ProfileLookUpUri == null || TextUtils.isEmpty(ProfileLookUpUri)) {
			Intent intent = new Intent(Intent.ACTION_INSERT, Contacts.CONTENT_URI);
			intent.putExtra(ContactEditorFragment.INTENT_EXTRA_NEW_LOCAL_PROFILE, true);
			ImplicitIntentsUtil.startActivityInApp(getActivity(), intent);
		} else {
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(ProfileLookUpUri));
			intent.putExtra("personal", true);//prize-add-hpf-2018-1-6
			ImplicitIntentsUtil.startActivityInApp(getActivity(), intent);
		}
	}
	/*prize-add-huangliemin-2016-7-26-end*/
	
	public boolean createGroup(String groupname) {
		AccountTypeManager accountTypes = AccountTypeManager.getInstance(getActivity());
		List<AccountWithDataSet> accounts = accountTypes.getAccounts(true);
		AccountWithDataSet PhoneAccount = null;
		
		for(AccountWithDataSet account : accounts) {
			if(AccountWithDataSetEx.isLocalPhone(account.type)) {
				PhoneAccount = account;
			}
		}
		
		if(PhoneAccount == null) {
			return false;
		}
		ContentResolver resolver = getActivity().getContentResolver();
		Cursor groupCursor = resolver.query(Groups.CONTENT_URI, new String[] {Groups.TITLE, Groups.DELETED}, null, null, null);
		if(groupCursor != null && groupCursor.moveToFirst()){
			do {
				String title =groupCursor.getString(groupCursor.getColumnIndex(Groups.TITLE));
				int deleted =groupCursor.getInt(groupCursor.getColumnIndex(Groups.DELETED));
				Log.e(TAG, "getColumnname = " + title);
				if (title.equals(groupname) && deleted == 0) {
					Toast.makeText(getActivity(), R.string.prize_repeat_group_name_toast, Toast.LENGTH_LONG).show();
					return false;
				}
			}while(groupCursor.moveToNext());
		}
		
		ContentValues values = new ContentValues();
		values.put(Groups.ACCOUNT_NAME, PhoneAccount.name);
		values.put(Groups.ACCOUNT_TYPE, PhoneAccount.type);
		values.put(Groups.DATA_SET, PhoneAccount.dataSet);
		values.put(Groups.TITLE, groupname);
		
		Uri groupUri = resolver.insert(Groups.CONTENT_URI, values);
		return (groupUri != null);
	}
	/*prize-add-show-dialog-huangliemin-2016-7-14-end*/

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mContext = activity;
	}

	/* (non-Javadoc)
	 * @see android.app.Fragment#onResume()
	 */
	@Override
	public void onResume() {
		super.onResume();
		Log.d(TAG, "[onResume]   lastindex = "+lastindex);
		getLoaderManager().restartLoader(LOADER_PROFILE, null, mProfileLoaderListener);
		/*prize-remove-hpf-2018-1-8-start*/
		//mAdapter.setLastGroupPosition(lastindex);
		//mAdapter.notifyDataSetChanged();
		/*prize-remove-hpf-2018-1-8-end*
//		
//		Log.i("logtest", "onresume: "+lastindex);
//		if(lastindex != -1) {
//			/*
//			int groupId = (int)(mAdapter.getGroup(lastindex).getGroupId());
//			startGroupMembersLoader(groupId);*/
//			
//			//mListView.expandGroup(lastindex);
//		}
	}

	/* (non-Javadoc)
	 * @see android.app.Fragment#onSaveInstanceState(android.os.Bundle)
	 */
	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putInt(LAST_POSITION, lastindex);
		super.onSaveInstanceState(outState);
	}
	
	/*
	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		if(savedInstanceState!=null) {
			lastindex = savedInstanceState.getInt(LAST_POSITION);
		}
	}
	*/
	
	


	/* (non-Javadoc)
	 * @see android.app.Fragment#onStart()
	 */
	@Override
	public void onStart() {
		/*
         * Bug Fix by Mediatek Begin. Original Android's code:
         * getLoaderManager().initLoader(LOADER_GROUPS, null,
         * mGroupLoaderListener); CR ID: ALPS00379788 Descriptions: restart
         * loader after unlock screen
         */
		Log.d(TAG, "[onStart]");
		mAdapter.setFragmentViewHeight(getView().getHeight());
        //getLoaderManager().restartLoader(LOADER_GROUPS, null, mGroupLoaderListener);
		super.onStart();
	}

	@Override
	public void onDetach() {
		super.onDetach();
		Log.d(TAG, "[onDetach]");
		if(mDialog!=null && mDialog.isShowing()) {
			mDialog.dismiss();
		}
		mContext = null;
	}
	
	/*prize-add-huangliemin-2016-7-26-start*/
	private final LoaderManager.LoaderCallbacks<Cursor> mProfileLoaderListener = 
			new LoaderCallbacks<Cursor>() {

				@Override
				public Loader<Cursor> onCreateLoader(int id, Bundle args) {
					Log.d(TAG,"[onCreateLoader--mProfileLoaderListener]");
					return new CursorLoader(mContext, Profile.CONTENT_URI, null, null, null, null);
				}

				@Override
				public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
					Log.d(TAG,"[onLoadFinished--mProfileLoaderListener]");
					if(data!=null && !data.isClosed() && data.getCount()>0 && data.moveToFirst()) { //prize add !data.isClosed() for bug 54802 by zhaojian 20180411
						
						/*prize change fix bug-id:20298 huangpengfei-2016-8-22-start*/
						Uri photoUri = null;
						String strPhotoUri = data.getString(data.getColumnIndex(Profile.PHOTO_URI));
						if(strPhotoUri != null){
							photoUri = Uri.parse(strPhotoUri);
						}
						/*prize change fix bug-id:20298 huangpengfei-2016-8-22-end*/
						if(photoUri!=null) {
							/*
							MyInfoPhoto.setImageURI(photoUri);
							*/
							try{
								InputStream input = mContext.getContentResolver().openInputStream(photoUri);
								Bitmap bitmap = BitmapFactory.decodeStream(input);
								input.close();
								bitmap = new CreateCircleBitmap().getCircleBitmap(bitmap, mContext.getResources().getDimensionPixelSize(R.dimen.prize_photo_ration));
								MyInfoPhoto.setImageBitmap(bitmap);
							} catch (Exception e) {
								e.printStackTrace();
							}
							
						} else {
							MyInfoPhoto.setImageDrawable(mContext.getResources().getDrawable(R.drawable.person_white_prize));
						}
						
						String ProfileName = data.getString(data.getColumnIndex(Profile.DISPLAY_NAME));
						if(ProfileName!=null) {
							MyInfoTitle.setText(ProfileName);
						} else {
							MyInfoTitle.setText(R.string.prize_set_my_info);
						}
						
						ProfileLookUpUri = Contacts.CONTENT_LOOKUP_URI+"/"+data.getString(data.getColumnIndex(Profile.LOOKUP_KEY))+"/"
								+data.getLong(data.getColumnIndex(Profile._ID));
					} else {
						MyInfoPhoto.setImageDrawable(mContext.getResources().getDrawable(R.drawable.person_white_prize));
						MyInfoTitle.setText(R.string.prize_set_my_info);

						// prize add for bug 42792 by zhaojian 20171129 start
						ProfileLookUpUri = null;
						// prize add for bug 42792 by zhaojian 20171129 end
					}
				}

				@Override
				public void onLoaderReset(Loader<Cursor> loader) {
					
				}
			};
	/*prize-add-huangliemin-2016-7-26-end*/
	
	/**
     * The listener for the group meta data loader for all groups.
     */
    private final LoaderManager.LoaderCallbacks<Cursor> mGroupLoaderListener =
            new LoaderCallbacks<Cursor>() {

        @Override
        public CursorLoader onCreateLoader(int id, Bundle args) {
            /*
             * Bug Fix by Mediatek Begin. Original Android's code: CR ID:
             * ALPS00115673 Descriptions: add wait cursor
             */
            Log.d(TAG, "[onCreateLoader--mGroupLoaderListener]");
            //mWaitCursorView.startWaitCursor();


            /*
             * Bug Fix by Mediatek End.
             */

            //mEmptyView.setText(null);
            return new GroupListLoader(mContext);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
          ///M:check whether the fragment still in Activity@{
            if (!isAdded()) {
                Log.w(TAG, "[onLoadFinished],This Fragment is not add to the Activity now");
                if (data != null) {
                    data.close();
                }
                return;
            }
            ///@}

            /*
             * Bug Fix by Mediatek Begin. Original Android's code: CR ID:
             * ALPS00115673 Descriptions: add wait cursor
             */
            //mWaitCursorView.stopWaitCursor();

            /*
             * Bug Fix by Mediatek End.
             */
            mGroupListCursor = data;

			//prize add by zhaojian for bug 53044 20180319 start
			TextView editButton = (TextView)getActivity().findViewById(R.id.select_all);
			LinearLayout editButtonContainer = (LinearLayout)getActivity().findViewById(R.id.select_all_container);
			if(!data.isClosed() && data.getCount() != 0) {
				ColorStateList csl = getResources().getColorStateList(R.drawable.prize_selector_actionbar_text_btn);
				editButton.setTextColor(csl);
				editButtonContainer.setEnabled(true);
				editButton.setEnabled(true);
			}else {
				ColorStateList csl = getResources().getColorStateList(R.color.prize_button_text_dark_color);
				editButton.setTextColor(csl);
				editButtonContainer.setEnabled(false);
				editButton.setEnabled(false);
			}
			//prize add by zhaojian for bug 53044 20180319 end

            bindGroupList();
        }

        public void onLoaderReset(Loader<Cursor> loader) {
        }
    };
    
    private void bindGroupList() {
    	//setAddAccountsVisibility(!ContactsUtils.areGroupWritableAccountsAvailable(mContext));
        if (mGroupListCursor == null) {
            return;
        }
        mAdapter.setCursor(mGroupListCursor);
    }
	
    protected ExpandableListView getListView() {
        return mListView;
    }
	
    public void onViewGroupAction(Uri groupUri) {
            int simId = -1;
            int subId = SubInfoUtils.getInvalidSubId();
    ///M: For move to other group feature.
            int count = /*mGroupsFragment.getAccountGroupMemberCount();*/0;
            String accountType = "";
            String accountName = "";
            Log.d(TAG, "groupUri" + groupUri.toString());
            List uriList = groupUri.getPathSegments();
            Uri newGroupUri = ContactsContract.AUTHORITY_URI.buildUpon()
                    .appendPath(uriList.get(0).toString())
                    .appendPath(uriList.get(1).toString()).build();
            if (uriList.size() > 2) {
                subId = Integer.parseInt(uriList.get(2).toString());
                Log.i(TAG, "people subId-----------" + subId);
            }
            if (uriList.size() > 3) {
                accountType = uriList.get(3).toString();
            }
            if (uriList.size() > 4) {
                accountName = uriList.get(4).toString();
            }
            Log.d(TAG, "newUri-----------" + newGroupUri);
            Log.d(TAG, "accountType-----------" + accountType);
            Log.d(TAG, "accountName-----------" + accountName);
            Intent intent = new Intent(getActivity(), GroupDetailActivity.class);
            intent.setData(newGroupUri);
            intent.putExtra("AccountCategory", new AccountCategoryInfo(accountType, subId,
                    accountName, count));
            startActivity(intent);
    }
    
    
    public static class AccountCategoryInfo implements Parcelable {

        public String mAccountCategory;
        public int mSubId;
        public String mSimName;
        ///M: For move to other group feature.
        public int mAccountGroupMemberCount;

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            out.writeString(mAccountCategory);
            out.writeInt(mSubId);
            out.writeString(mSimName);
            ///M: For move to other group feature.
            out.writeInt(mAccountGroupMemberCount);
        }

        public static final Parcelable.Creator<AccountCategoryInfo> CREATOR =
                new Parcelable.Creator<AccountCategoryInfo>() {
            public AccountCategoryInfo createFromParcel(Parcel in) {
                return new AccountCategoryInfo(in);
            }

            public AccountCategoryInfo[] newArray(int size) {
                return new AccountCategoryInfo[size];
            }
        };

        private AccountCategoryInfo(Parcel in) {
            mAccountCategory = in.readString();
            mSubId = in.readInt();
            mSimName = in.readString();
            ///M: For move to other group feature.
            mAccountGroupMemberCount = in.readInt();
        }

        public AccountCategoryInfo(String accountCategory, int subId, String simName, int count) {
            mAccountCategory = accountCategory;
            mSubId = subId;
            mSimName = simName;
            ///M: For move to other group feature.
            mAccountGroupMemberCount = count;
        }
    }
    
    /*prize-add-load-group-huangliemin-2016-7-5-start*/
    /*
    public void loadGroup(Uri groupUri) {
    	startGroupMetadataLoader();
    }
    
    private void startGroupMetadataLoader() {
    	getLoaderManager().restartLoader(-1, null, mGroupMetadataLoaderListener);
    }
    */
    /**
     * Start the loader to retrieve the list of group members.
     */
    private void startGroupMembersLoader(int groupId) {
        if (!isAdded()) {
            Log.w(TAG, "#startGroupMembersLoader(),the fragment is not attach to  Activity.");
            return;
        }
        Log.d(TAG, "[startGroupMembersLoader] startGroup: "+groupId);

        getLoaderManager().restartLoader(groupId, null, mGroupMemberListLoaderListener);
    }
    
//    private final LoaderManager.LoaderCallbacks<Cursor> mGroupMetadataLoaderListener =
//    		new LoaderCallbacks<Cursor>() {
//
//				@Override
//				public Loader<Cursor> onCreateLoader(int id, Bundle args) {
//					// TODO Auto-generated method stub
//					Log.i(TAG, "onCreateLoader");
//					return new GroupMetaDataLoader(mContext, /*groupUri*/null);//prize-huangliemin
//				}
//
//				@Override
//				public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
//					// TODO Auto-generated method stub
//					if(data == null || data.isClosed()) {
//						Log.e(TAG, "Failed to load group metadata");
//						return;
//					}
//					
//					if(!isAdded()) {
//						Log.w(TAG, "onLoadFinished(),This Fragment is not add to the Activity now.");
//						if (data != null) {
//		                    data.close();
//		                }
//		                return;
//					}
//					
//					/**
//		             * M: fix bug for ALPS00336957 je happen when press back key from sms
//		             */
//		            if (null != data) {
//		                data.moveToPosition(-1);
//		                if (data.moveToNext()) {
//		                    boolean deleted = data.getInt(GroupMetaDataLoader.DELETED) == 1;
//		                    if (!deleted) {
//		                        //bindGroupMetaData(data);
//
//		                        // Retrieve the list of members
//		                        ///M: in onLoadFinished() can't call restart loader directly,
//		                        ///so we should use a handler to avoid Fragment commit failure.
//		                        
//		                        Handler restartLoaderHandler = new Handler() {
//		                            @Override
//		                            public void handleMessage(Message msg) {
//		                                Log.d(TAG, "[handleMessage] to restart group memeber loader");
//		                                //startGroupMembersLoader();
//		                            }
//		                        };
//		                        restartLoaderHandler.sendEmptyMessage(0);
//		                        return;
//		                    }
//		                }
//		            }
//					
//				}
//
//				@Override
//				public void onLoaderReset(Loader<Cursor> loader) {
//					// TODO Auto-generated method stub
//					
//				}
//    	
//    };
    
    /**
     * The listener for the group members list loader
     */
    private final LoaderManager.LoaderCallbacks<Cursor> mGroupMemberListLoaderListener =
            new LoaderCallbacks<Cursor>() {

        @Override
        public CursorLoader onCreateLoader(int id, Bundle args) {
        	Log.d(TAG, "[onCreateLoader--mGroupMemberListLoaderListener]  oncreate id: "+id);
            return GroupMemberLoader.constructLoaderForGroupDetailQuery(mContext, id);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            if (data == null || data.isClosed()) {
                Log.e(TAG, "[onLoadFinished] Failed to load group members");
                return;
            }
            /// M:check whether the fragment still in Activity@{
            if (!isAdded()) {
                Log.w(TAG, "onLoadFinished,This Fragment is not add to the Activity.");
                if (data != null) {
                    data.close();
                }
                return;
            }
            Log.d(TAG, "[onLoadFinished]: "+(mAdapter.getChildData(loader.getId())!=null));
            /*prize-add for dido os8.0-hpf-2017-9-14-start*/
            /*if (mAdapter.getChildData(loader.getId()) != null) {
            	mAdapter.getChildData(loader.getId()).setContactCursor(data);
            }*/
            if(mAdapter.mMemberAdapter != null){
            	mAdapter.mMemberAdapter.setContactCursor(data);
            }
            /*prize-add for dido os8.0-hpf-2017-9-14-end*/
            /// @}

        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {}
    };
    /*prize-add-load-group-huangliemin-2016-7-5-end*/

}
