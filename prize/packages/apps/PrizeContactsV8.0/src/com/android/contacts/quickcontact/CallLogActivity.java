/**
 * CallLogActivity.java [V 1.0.0]
 * classes : com.android.contacts.quickcontact.CallLogActivity
 * huangliemin Create at 2016-7-18 8:58:30
 */
package com.android.contacts.quickcontact;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.android.contacts.interactions.CallLogInteraction;
import com.android.contacts.interactions.CallLogInteractionsLoader;
import com.android.contacts.interactions.ContactInteraction;
import com.android.contacts.quickcontact.ExpandingEntryCardView.Entry;

import com.android.contacts.R;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Trace;
import android.provider.CallLog;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import com.mediatek.telecom.TelecomManagerEx;
/*prize-add-hpf-2018-2-26-start*/
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
/*prize-add-hpf-2018-2-26-end*/

/**
 * com.android.contacts.quickcontact.CallLogActivity
 * @author huangliemin <br/>
 * create at 2016-7-18 8:58:30
 */
public class CallLogActivity extends Activity{
	
	private static final String KEY_LOADER_EXTRA_PHONES =
			"KEY_LOADER_EXTRA_PHONES";
	
	private AsyncTask<Void, Void, Void> mRecentDataTask;
	private List<String> mDeleteList;
	private ListView mListView;
	private CallLogAdapter mAdapter;
	private ImageButton mDeleteButton;
	private TelephonyManager mTelephonyManager;
	private TelecomManager mTelecomManager;
	private LinearLayout mCallRecordsDeleteLayout;//prize-add-huangpengfei-2016-9-19
	private TextView mContactsCallRecordsDeleteText;//prize-add-huangpengfei-2016-9-19
	
	private BroadcastReceiver mSimReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent.getAction().equals("android.intent.action.SIM_STATE_CHANGED")) {
				if(mAdapter!=null) {
					mAdapter.notifyDataSetChanged();
				}
			}
		}
		
	};
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.prize_more_calllog_layout);
		
		mTelephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
		mTelecomManager = (TelecomManager)getSystemService(Context.TELECOM_SERVICE);
		mListView = (ListView)findViewById(R.id.list);

		// prize add by zhaojian for bug 52444 20180316 start
		mListView.setVisibility(View.INVISIBLE);
		// prize add by zhaojian for bug 52444 20180316 end

		/*prize-add-huangpengfei-2016-9-19-start*/
		mCallRecordsDeleteLayout = (LinearLayout)findViewById(R.id.quickcontact_callrecords_delete_layout);//prize-add-huangpengfei-2016-9-19
		mContactsCallRecordsDeleteText = (TextView)findViewById(R.id.prize_contacts_callrecords_delete_text);
		/*prize-add-huangpengfei-2016-9-19-end*/
		mDeleteButton = (ImageButton)findViewById(R.id.quickcontact_callrecords_delete);
		mCallRecordsDeleteLayout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				/*prize-change-hpf-2018-3-16-start*/
				//prizeCallRecentsDelete();
				prizeShowBottomDialog();
				/*prize-change-hpf-2018-3-16-end*/
			}
		});
		mAdapter = new CallLogAdapter(this);
		mListView.setAdapter(mAdapter);
		mListView.setFooterDividersEnabled(true);
		/*prize-add fix bug[52412]-hpf-2018-3-12-start*/
		mAdapter.setOnDataChangeListener(new OnDataChangeListener() {

			@Override
			public void onDataChange(List<Entry> datalist) {
				if(datalist == null){
					mListView.setVisibility(View.INVISIBLE);
				}else{
					mListView.setVisibility(View.VISIBLE);
				}
			}
		});
		/*prize-add fix bug[52412]-hpf-2018-3-12-end*/
		Log.i("logtest", "callLog: "+(getIntent().getStringArrayExtra(KEY_LOADER_EXTRA_PHONES)!=null ? getIntent().getStringArrayExtra(KEY_LOADER_EXTRA_PHONES).toString() : null));
		if(getIntent().getStringArrayExtra(KEY_LOADER_EXTRA_PHONES)!=null) {
			final Bundle phonesExtraBundle = new Bundle();
			phonesExtraBundle.putStringArray(KEY_LOADER_EXTRA_PHONES, getIntent().getStringArrayExtra(KEY_LOADER_EXTRA_PHONES));
			getLoaderManager().initLoader(0, phonesExtraBundle, mLoaderInteractionsCallbacks);
		}
		
		ActionBar actionBar = getActionBar();
		if(actionBar!=null) {
			/*prize-change-huangliemin-2016-7-22-start*/
			/*
			actionbar.setDisplayHomeAsUpEnabled(true);
			actionbar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_TITLE);
			*/
			actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        	actionBar.setDisplayShowCustomEnabled(true);
        	actionBar.setDisplayHomeAsUpEnabled(false);
        	actionBar.setDisplayShowTitleEnabled(false);
        	actionBar.setDisplayUseLogoEnabled(false);
        	actionBar.setCustomView(R.layout.prize_custom_center_actionbar_2016_7_21);
        	TextView TitleText = (TextView)actionBar.getCustomView().findViewById(R.id.title);
        	TitleText.setText(R.string.prize_contacts_calllog_title);
        	ImageButton BackButton = (ImageButton)actionBar.getCustomView().findViewById(R.id.back_button);
        	BackButton.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					onBackPressed();
				}
			});
        	
        	IntentFilter filter = new IntentFilter("android.intent.action.SIM_STATE_CHANGED");
        	registerReceiver(mSimReceiver, filter);
			/*prize-change-huangliemin-2016-7-22-end*/
		}
	}
	
	@Override
	protected void onDestroy() {
		if(mSimReceiver!=null) {
			unregisterReceiver(mSimReceiver);
		}
		super.onDestroy();
	}



	@Override
	protected void onStart() {
		super.onStart();
	}
	
	private void prizeCallRecentsDelete(){
        AlertDialog.Builder builder = new AlertDialog.Builder(CallLogActivity.this);
        builder.setMessage(getResources().getString(R.string.prize_clear_call_log_tips))
			.setTitle(getResources().getString(R.string.prize_recents_delete_prompt))
			.setPositiveButton(getResources().getString(R.string.prize_recents_delete_ok), new DialogInterface.OnClickListener() {
            
            @Override
            public void onClick(DialogInterface dialog, int which) {
               if(mDeleteList !=null && mDeleteList.size()>0){//PRIZE-add-yuandailin-2016-5-20
                for(int i=0;i<mDeleteList.size();i++){
					getContentResolver().delete(CallLog.Calls.CONTENT_URI,"number=?",new String[]{mDeleteList.get(i)});
				 }
               	}
				
               mDeleteButton.setEnabled(false);
               mContactsCallRecordsDeleteText.setEnabled(false);
               mCallRecordsDeleteLayout.setEnabled(false);
               //mDeleteButton.setClickable(false);//prize-remove-huangpengfei-2016-9-19
               mCallRecordsDeleteLayout.setClickable(false);//prize-add-huangpengfei-2016-9-19
               mAdapter.setData(null);
               mAdapter.notifyDataSetChanged();
            }
        }).setNegativeButton(getResources().getString(R.string.prize_recents_delete_cancel), new DialogInterface.OnClickListener() {
            
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();

    }
	
	/*prize-add-hpf-2018-3-16-start*/
	private AlertDialog mDialog;
 	private void prizeShowBottomDialog() {
 		View rootView = View.inflate(this, R.layout.prize_contacts_delete_dialog, null);
 		TextView delete = (TextView)rootView.findViewById(R.id.delete_contact);
 		View cancel = rootView.findViewById(R.id.cancel_btn);
 		delete.setText(R.string.prize_clear_call_log);
 		delete.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				if(mDeleteList !=null && mDeleteList.size()>0){
 	                for(int i=0;i<mDeleteList.size();i++){
 						getContentResolver().delete(CallLog.Calls.CONTENT_URI,"number=?",new String[]{mDeleteList.get(i)});
 					 }
 	             }
 					
               mDeleteButton.setEnabled(false);
               mContactsCallRecordsDeleteText.setEnabled(false);
               mCallRecordsDeleteLayout.setEnabled(false);
               mCallRecordsDeleteLayout.setClickable(false);
               mAdapter.setData(null);
               mAdapter.notifyDataSetChanged();
               mDialog.dismiss();
 			}
 			
 		});
 		
 		cancel.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				mDialog.dismiss();
 			}
 		});
 		mDialog = new AlertDialog.Builder(this).setView(rootView).create();
 		Window dialogWindow = mDialog.getWindow();
 		dialogWindow.getDecorView().setPadding(0, 0, 0, 0);
 		dialogWindow.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
 				WindowManager.LayoutParams.FLAG_FULLSCREEN);
 		dialogWindow.setBackgroundDrawableResource(android.R.color.transparent);
 		WindowManager.LayoutParams mParams = dialogWindow.getAttributes();
 		mParams.width = WindowManager.LayoutParams.MATCH_PARENT;
 		mParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
 		mParams.gravity = Gravity.BOTTOM;
 		dialogWindow.setAttributes(mParams);
 		dialogWindow.setWindowAnimations(R.style.GetDialogBottomMenuAnimation);
 		mDialog.show();
 	}
 	/*prize-add-hpf-2018-3-16-end*/
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId() == android.R.id.home) {
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (mRecentDataTask != null) {
            mRecentDataTask.cancel(/* mayInterruptIfRunning = */ false);
        }
	}



	private void bindRecentData(List<ContactInteraction> data) {
		Log.i("logtest", "data: "+(data==null ? null : data.size()));
        final List<ContactInteraction> allInteractions = new ArrayList<>();
        final List<Entry> interactionsWrapper = new ArrayList<>();

        // Serialize mRecentLoaderResults into a single list. This should be done on the main
        // thread to avoid races against mRecentLoaderResults edits.
        allInteractions.addAll(data);

        mRecentDataTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                Trace.beginSection("sort recent loader results");

                // Sort the interactions by most recent
                Collections.sort(allInteractions, new Comparator<ContactInteraction>() {
                    @Override
                    public int compare(ContactInteraction a, ContactInteraction b) {
                        if (a == null && b == null) {
                            return 0;
                        }
                        if (a == null) {
                            return 1;
                        }
                        if (b == null) {
                            return -1;
                        }
                        if (a.getInteractionDate() > b.getInteractionDate()) {
                            return -1;
                        }
                        if (a.getInteractionDate() == b.getInteractionDate()) {
                            return 0;
                        }
                        return 1;
                    }
                });

                Trace.endSection();
                Trace.beginSection("contactInteractionsToEntries");

                // Wrap each interaction in its own list so that an icon is displayed for each entry
                for (Entry contactInteraction : contactInteractionsToEntries(allInteractions)) {
                    interactionsWrapper.add(contactInteraction);
                }

                Trace.endSection();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                Trace.beginSection("initialize recents card");
                Log.i("logtest", "size: "+allInteractions.size());

                if (allInteractions.size() > 0) {
                    if(mAdapter!=null) {
                    	mAdapter.setData(interactionsWrapper);
                    }
					mDeleteList = new ArrayList<String>();
					for(int i=0;i<interactionsWrapper.size();i++){
						mDeleteList.add(interactionsWrapper.get(i).getHeader());
					}	
        
                } else {
                   mAdapter.setData(null);

                }
                
                mAdapter.notifyDataSetChanged();

				// prize add by zhaojian for bug 52444 20180316 start
				mListView.setVisibility(View.VISIBLE);
				// prize add by zhaojian for bug 52444 20180316 end

                Trace.endSection();
                mRecentDataTask = null;
            }
        };
        mRecentDataTask.execute();
    }
	
	
	private List<Entry> contactInteractionsToEntries(List<ContactInteraction> interactions) {
        final List<Entry> entries = new ArrayList<>();
        for (ContactInteraction interaction : interactions) {
            if (interaction == null) {
                continue;
            }
	
			if(interaction instanceof CallLogInteraction){
				CallLogInteraction pinteraction = (CallLogInteraction)interaction;
				StringBuffer s = new StringBuffer();
				if(pinteraction.getType() == 1){
				    s.append(getResources().getString(R.string.prize_incall));
				}else if(pinteraction.getType() == 2){
				    s.append(getResources().getString(R.string.prize_outcall));
				}else{
				    s.append(getResources().getString(R.string.prize_callnotice));
				}
				int i = Integer.valueOf(pinteraction.getDuration().toString());
                int i1 = i / 3600;
                int i2 = (i % 3600) / 60;
                int i3 = i % 60;
                if (i1 != 0) {
                    s.append(i1 + getResources().getString(R.string.prize_hours));
                }
                if (i2 != 0) {
                    s.append(i2 + getResources().getString(R.string.prize_minutes));
                }
                s.append(i3 + getResources().getString(R.string.prize_seconds));
                
                
            entries.add(new Entry(/* id = */ -1,
            		/*mainIcon = */interaction.getIcon(this),
            		/*header = */interaction.getViewHeader(this),
            		/*subHeader = */interaction.getViewBody(this),
            		/*subHeaderIcon = */interaction.getBodyIcon(this),
            		/*text = */interaction.getViewFooter(this),
            		/*textIcon = */interaction.getFooterIcon(this),
                    /* M: add sim icon @ { */
            		/*simIcon = */interaction.getSimIcon(this),
            		/*simName = */interaction.getSimName(this),
                    /* @ } */
            		/*primaryContentDescription = */interaction.getContentDescription(this),
            		/*intent = */interaction.getIntent(),
                    /* alternateIcon = */ null,
                    /* alternateIntent = */ null,
                    /* alternateContentDescription = */ null,
                    /* shouldApplyColor = */ true,
                    /* isEditable = */ false,
                    /* EntryContextMenuInfo = */ null,
                    /* thirdIcon = */ null,
                    /* thirdIntent = */ null,
                    /* thirdContentDescription = */ null,
                    /* thirdAction = */ 0,
                    /* thirdExtras = */ null,
                    /*iconResourceId = */interaction.getIconResourceId(),
                    /*callDuration = */s.toString()));
				}else{
				entries.add(new Entry(/* id = */ -1,
						/*mainIcon = */interaction.getIcon(this),
						/*header = */interaction.getViewHeader(this),
						/*subHeader = */interaction.getViewBody(this),
						/*subHeaderIcon = */interaction.getBodyIcon(this),
						/*text = */interaction.getViewFooter(this),
						/*textIcon = */interaction.getFooterIcon(this),
                    /* M: add sim icon @ { */
						/*simIcon = */interaction.getSimIcon(this),
						/*simName = */interaction.getSimName(this),
                    /* @ } */
						/*primaryContentDescription = */interaction.getContentDescription(this),
						/*intent = */interaction.getIntent(),
                    /* alternateIcon = */ null,
                    /* alternateIntent = */ null,
                    /* alternateContentDescription = */ null,
                    /* shouldApplyColor = */ true,
                    /* isEditable = */ false,
                    /* EntryContextMenuInfo = */ null,
                    /* thirdIcon = */ null,
                    /* thirdIntent = */ null,
                    /* thirdContentDescription = */ null,
                    /* thirdAction = */ 0,
                    /* thirdExtras = */ null,
                    /*iconResourceId = */interaction.getIconResourceId(),
					/*callDuration = */ null));	
				}

        }
        return entries;
    }
	/*prize-add fix bug[52412]-hpf-2018-3-12-start*/
	public interface OnDataChangeListener{
		void onDataChange(List<Entry> datalist);
	}
	/*prize-add fix bug[52412]-hpf-2018-3-12-end*/
	class CallLogAdapter extends BaseAdapter{
		private Context mContext;
		private List<Entry> DataList;
		private OnDataChangeListener mOnDataChangeListener;//prize-add fix bug[52412]-hpf-2018-3-12
		
		public CallLogAdapter(Context context) {
			mContext = context;
		}
		/*prize-add fix bug[52412]-hpf-2018-3-12-start*/
		public void setOnDataChangeListener(OnDataChangeListener onDataChangeListener){
			this.mOnDataChangeListener = onDataChangeListener;
		}
		/*prize-add fix bug[52412]-hpf-2018-3-12-end*/
		public void setData(List<Entry> datalist) {
			DataList = datalist;
			if(mOnDataChangeListener != null)
			mOnDataChangeListener.onDataChange(datalist);
		}

		
		@Override
		public int getCount() {
			Log.i("logtest", "count: "+(DataList==null ? null : DataList.size()));
			if(DataList!=null) {
				return DataList.size();
			} else {
				return 0;
			}
		}

		
		@Override
		public Entry getItem(int position) {
			if(DataList!=null) {
				return DataList.get(position);
			} else {
				return null;
			}
		}

		
		@Override
		public long getItemId(int position) {
			return position;
		}

		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder mViewHolder;
			if(convertView == null) {
				convertView = LayoutInflater.from(mContext).inflate(R.layout.prize_calllog_item_view_huangliemin_2016_7_20, null);
				mViewHolder = new ViewHolder();
				mViewHolder.mDate = (TextView)convertView.findViewById(R.id.date);
				mViewHolder.mIcon = (ImageView)convertView.findViewById(R.id.style_icon);
				mViewHolder.mSimIcon = (ImageView)convertView.findViewById(R.id.sim_icon);
				mViewHolder.mtext = (TextView)convertView.findViewById(R.id.style_text);
				convertView.setTag(mViewHolder);
			} else {
				mViewHolder = (ViewHolder)convertView.getTag();
			}
			
			Entry dataitem = DataList.get(position);
			PhoneAccountHandle mPhoneAccountHandle = dataitem.getIntent().getParcelableExtra(TelecomManagerEx.EXTRA_SUGGESTED_PHONE_ACCOUNT_HANDLE);
			List<SubscriptionInfo> mSubInfoList = SubscriptionManager.from(mContext).getActiveSubscriptionInfoList();
			int simcount = 0;
			if(mSubInfoList!=null) {
				simcount = mSubInfoList.size();
			}
			if(mPhoneAccountHandle!=null && mPhoneAccountHandle.getId()!=null && simcount > 1) {
				int defaultSubId = mTelephonyManager.getSubIdForPhoneAccount(mTelecomManager.getPhoneAccount(mPhoneAccountHandle));
				int slotId = SubscriptionManager.getSlotId(defaultSubId);
				if(slotId == 0) {
					mViewHolder.mSimIcon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.prize_sim_1));
					mViewHolder.mSimIcon.setVisibility(View.VISIBLE);
				} else if (slotId == 1) {
					mViewHolder.mSimIcon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.prize_sim_2));
					mViewHolder.mSimIcon.setVisibility(View.VISIBLE);
				} else {
					mViewHolder.mSimIcon.setVisibility(View.GONE);
				}
			} else {
				mViewHolder.mSimIcon.setVisibility(View.GONE);
			}
			mViewHolder.mDate.setText(dataitem.getText());
			mViewHolder.mIcon.setImageDrawable(dataitem.getTextIcon());
			if(dataitem.getCallDuration() != null) {
				mViewHolder.mtext.setText(dataitem.getCallDuration());
			} else {
				mViewHolder.mtext.setText(mContext.getResources().getString(R.string.prize_callnotice) + 0 + getResources().getString(R.string.prize_seconds));
			}
			return convertView;
		}
	}
	
	class ViewHolder {
		TextView mDate;
		ImageView mIcon;
		ImageView mSimIcon;
		TextView mtext;
	}
	
	private final LoaderCallbacks<List<ContactInteraction>> mLoaderInteractionsCallbacks = 
			new LoaderCallbacks<List<ContactInteraction>>() {

				@Override
				public Loader<List<ContactInteraction>> onCreateLoader(int id,
						Bundle args) {
					Loader<List<ContactInteraction>> loader = new CallLogInteractionsLoader(CallLogActivity.this,
							args.getStringArray(KEY_LOADER_EXTRA_PHONES),
							Integer.MAX_VALUE);
					return loader;
				}

				@Override
				public void onLoadFinished(
						Loader<List<ContactInteraction>> loader,
						List<ContactInteraction> data) {
					bindRecentData(data);
					
				}

				@Override
				public void onLoaderReset(
						Loader<List<ContactInteraction>> loader) {
					
				}
			};
}
