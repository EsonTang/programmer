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
 *	    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package com.android.contacts.common.vcard;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.provider.ContactsContract.RawContacts;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.android.contacts.common.R;
import com.android.vcard.VCardEntry;

import com.mediatek.contacts.util.Log;
import com.mediatek.contacts.util.MtkToast;

import java.text.NumberFormat;

import static android.R.id.message;

/*prize-add for custom progressbar -hpf-2017-12-27-start*/
import com.android.contacts.common.prize.PrizeCirclePercentView.OnFinishListener;
import com.android.contacts.common.prize.PrizeCirclePercentView;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.app.AlertDialog;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.content.DialogInterface;
/*prize-add for custom progressbar -hpf-2017-12-27-start*/

public class NotificationImportExportListener implements VCardImportExportListener,
        Handler.Callback {
    /** The tag used by vCard-related notifications. */
    /* package */ static final String DEFAULT_NOTIFICATION_TAG = "VCardServiceProgress";
    /**
     * The tag used by vCard-related failure notifications.
     * <p>
     * Use a different tag from {@link #DEFAULT_NOTIFICATION_TAG} so that failures do not get
     * replaced by other notifications and vice-versa.
     */
    public static final String FAILURE_NOTIFICATION_TAG = "VCardServiceFailure";

    private final NotificationManager mNotificationManager;
    private final Activity mContext;
    private final Handler mHandler;

    /// M: ALPS03308908, can't display finished notification
    private int mPercentage;
    
    /*prize-add for custom progressbar -hpf-2017-12-27-start*/
    private PrizeCirclePercentView mPg;
    private TextView mPgTvTitle;
    private AlertDialog mProgressDialog;
    private AlertDialog mProgressDialogWaitting;
    private ProgressBar mWaittingProgressBar;
    private int mDialogWidth = 0;
    private String mDialogTitle;
    private String mJobFinishStr;
    private String mJobCancelStr;
    private boolean mIsCancelJob = false;
    private boolean mIsFinishJob = false;
	private int count;
	private int mCurrentCount;
	private boolean mIsImportModle = false;
    private final static String TAG = "NotificationImportExportListener";
    
    private final static int CASE_TOAST = 0;
    private final static int CASE_SHOW_DIALOG = 1;
    private final static int CASE_UPDATE_PROGRESS = 2;
    private final static int CASE_HIDE_DIALOG = 3;
    /*prize-add for custom progressbar -hpf-2017-12-27-end*/

    public NotificationImportExportListener(Activity activity) {
        mContext = activity;
        mNotificationManager = (NotificationManager) activity.getSystemService(
                Context.NOTIFICATION_SERVICE);
        mHandler = new Handler(this);
        /* prize-add for custom progressbar -hpf-2017-12-27-start */
        mDialogWidth = (int) mContext.getResources().getDimension(R.dimen.prize_process_dialog_width);
        /*prize-add for custom progressbar -hpf-2017-12-27-end*/
    }

    @Override
    public boolean handleMessage(Message msg) {
    	
    	/*prize-change for custom progressbar -hpf-2017-12-27-start*/
    	switch(msg.what){
    	
    		case CASE_TOAST:
    			Log.d(TAG, "[handleMessage]case CASE_TOAST");
    			String text = (String) msg.obj;
    	        /// M: To modify the toast last a long time issue.
    	        MtkToast.toast(mContext, text, Toast.LENGTH_LONG);
    			break;
    			
	    	case CASE_SHOW_DIALOG:
	    		Log.d(TAG, "[handleMessage]case CASE_SHOW_DIALOG");
	    		showProgressDialog();
	    		break;
	    		
	    	case CASE_UPDATE_PROGRESS:
	    		Log.d(TAG, "[handleMessage]case CASE_UPDATE_PROGRESS");
	    		float currentCount = msg.arg1;
	    		float totalCount = msg.arg2;
	    		float percent = currentCount/totalCount;
	    		if(mPg != null && mPg.getVisibility() == View.VISIBLE){
	    			mPg.setPercent(percent);
	    		}
	    		break;
	    	
	    	case CASE_HIDE_DIALOG:
	    		Log.d(TAG, "[handleMessage]case CASE_HIDE_DIALOG");
	    		hideProgressDialog();
	    		hideWaittingDialog();
	    		mContext.finish();
	    		break;
	    	
    		
	    	default:
    	
    	}
    	/*prize-change for custom progressbar -hpf-2017-12-27-end*/
        
        return true;
    }

    @Override
    public void onImportProcessed(ImportRequest request, int jobId, int sequence) {
        // prize-delete-bug 37801 -by zhaojian 20171017 start
//        // Show a notification about the status
//        final String displayName;
//        final String message;
//        if (request.displayName != null) {
//            displayName = request.displayName;
//            message = mContext.getString(R.string.vcard_import_will_start_message, displayName);
//        } else {
//            displayName = mContext.getString(R.string.vcard_unknown_filename);
//            message = mContext.getString(
//                    R.string.vcard_import_will_start_message_with_default_name);
//        }
//
//        // We just want to show notification for the first vCard.
//        if (sequence == 0) {
//            // TODO: Ideally we should detect the current status of import/export and
//            // show "started" when we can import right now and show "will start" when
//            // we cannot.
//            mHandler.obtainMessage(0, message).sendToTarget();
//        }
//
//        final Notification notification = constructProgressNotification(mContext,
//                VCardService.TYPE_IMPORT, message, message, jobId, displayName, -1, 0);
//        Log.d(DEFAULT_NOTIFICATION_TAG, "[onImportProcessed] displayName:" + request.displayName
//                + ",jobId: " + jobId);
//        mNotificationManager.notify(DEFAULT_NOTIFICATION_TAG, jobId, notification);
        // prize-delete-bug 37801 -by zhaojian 20171017 end
    	
    	/*prize-add for custom progressbar -hpf-2017-12-27-start*/
    	count ++;
    	Log.d(TAG, "[onImportProcessed] count = "+count);
    	mIsImportModle = true;
    	if(count == 1){
    		mDialogTitle = mContext.getString(R.string.prize_importing);
    		mJobFinishStr = mContext.getString(R.string.prize_vcard_import_finished_message);
    		mJobCancelStr = mContext.getString(R.string.prize_vcard_import_cancel_message);
    		Message msg = mHandler.obtainMessage();
        	msg.what = CASE_SHOW_DIALOG;
        	mHandler.sendMessage(msg);
    	}
    	/*prize-add for custom progressbar -hpf-2017-12-27-end*/
    }

    @Override
    public void onImportParsed(ImportRequest request, int jobId, VCardEntry entry, int currentCount,
            int totalCount) {
        // prize-delete-bug 37801 -by zhaojian 20171017 start
//        if (entry.isIgnorable()) {
//            return;
//        }
		/// M: ALPS03308908, can't display finished notification
        // @{
 //       int percentage = (int )(((double )currentCount)/totalCount * 100);
 //       if (percentage == mPercentage) {
 //           return;
 //       }
 //      mPercentage = percentage;
        // }@
//
//        final String totalCountString = String.valueOf(totalCount);
//        final String tickerText =
//                mContext.getString(R.string.progress_notifier_message,
//                        String.valueOf(currentCount),
//                        totalCountString,
//                        entry.getDisplayName());
//        final String description = mContext.getString(R.string.importing_vcard_description,
//                entry.getDisplayName());
//
//        final Notification notification = constructProgressNotification(
//                mContext.getApplicationContext(), VCardService.TYPE_IMPORT, description, tickerText,
//                jobId, request.displayName, totalCount, currentCount);
//        mNotificationManager.notify(DEFAULT_NOTIFICATION_TAG, jobId, notification);
        // prize-delete-bug 37801 -by zhaojian 20171017 end
    	
    	/*prize-add for custom progressbar -hpf-2017-12-27-start*/
    	if(currentCount == totalCount){
    		mIsFinishJob = true;
    	}
    	mCurrentCount = currentCount;
    	Message msg = mHandler.obtainMessage();
    	msg.what = CASE_UPDATE_PROGRESS;
    	msg.arg1 = currentCount;
    	msg.arg2 = totalCount;
    	mHandler.sendMessage(msg);
    	/*prize-add for custom progressbar -hpf-2017-12-27-end*/

    }

    @Override
    public void onImportFinished(ImportRequest request, int jobId, Uri createdUri) {
        // prize-delete-bug 37801 -by zhaojian 20171017 start
//		  mPercentage = 0;
//        final String description = mContext.getString(R.string.importing_vcard_finished_title,
//                request.displayName);
//        final Intent intent;
//        if (createdUri != null) {
//            final long rawContactId = ContentUris.parseId(createdUri);
//            final Uri contactUri = RawContacts.getContactLookupUri(
//                    mContext.getContentResolver(), ContentUris.withAppendedId(
//                            RawContacts.CONTENT_URI, rawContactId));
//            intent = new Intent(Intent.ACTION_VIEW, contactUri);
//        } else {
//            intent = new Intent(Intent.ACTION_VIEW);
//            intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
//        }
//        intent.setPackage(mContext.getPackageName());
//        final Notification notification =
//                /// M:
//                NotificationImportExportListener.constructFinishNotification(
//                        VCardService.TYPE_IMPORT, mContext, description, null, intent);
//        mNotificationManager.notify(NotificationImportExportListener.DEFAULT_NOTIFICATION_TAG,
//                jobId, notification);
        // prize-delete-bug 37801 -by zhaojian 20171017 end
    }

    @Override
    public void onImportFailed(ImportRequest request) {
        // TODO: a little unkind to show Toast in this case, which is shown just a moment.
        // Ideally we should show some persistent something users can notice more easily.
//		mPercentage = 0;
        // prize modify for bug 41637 by zhaojian 20171104 start
        /*mHandler.obtainMessage(0,
                mContext.getString(R.string.vcard_import_request_rejected_message)).sendToTarget();*/      
    	String message = mContext.getString(R.string.vcard_import_request_rejected_message);
        mHandler.obtainMessage(CASE_TOAST, message).sendToTarget();
        // prize modify for bug 41637 by zhaojian 20171104 end
    }

    @Override
    public void onImportCanceled(ImportRequest request, int jobId) {
        // prize modify for bug 41637 by zhaojian 20171104 start
//        mPercentage = 0;
		final String description = mContext.getString(R.string.importing_vcard_canceled_title,
                request.displayName);               
        /*final Notification notification =
                NotificationImportExportListener.constructCancelNotification(mContext, description);
        Log.d(DEFAULT_NOTIFICATION_TAG, "[onImportCanceled] displayName:" + request.displayName
                + ",jobId: " + jobId);
        mNotificationManager.notify(NotificationImportExportListener.DEFAULT_NOTIFICATION_TAG,
                jobId, notification);*/
		//mHandler.obtainMessage(CASE_TOAST, description).sendToTarget();
        // prize modify for bug 41637 by zhaojian 20171104 end
		
		/*prize-add for custom progressbar -hpf-2017-12-27-start*/
		Log.d(TAG, "[onImportCanceled]");
    	mIsCancelJob = true;
    	/*prize-add for custom progressbar -hpf-2017-12-27-end*/
    }

    @Override
    public void onExportProcessed(ExportRequest request, int jobId) {
        /// M: ALPS02689253. export tip show vcf file name @{
        /* mtk no use
        final String displayName = ExportVCardActivity.getOpenableUriDisplayName(mContext,
                request.destUri);
        final String message = mContext.getString(R.string.contacts_export_will_start_message);
        */
        //prize delete by zhaojian for bug 37801 20171016 start
//        final String displayName = request.destUri.getLastPathSegment();
//        final String message = mContext.getString(R.string.vcard_export_will_start_message,
//                displayName);
//        /// @}
//
//        mHandler.obtainMessage(0, message).sendToTarget();
//        final Notification notification =
//                NotificationImportExportListener.constructProgressNotification(mContext,
//                        VCardService.TYPE_EXPORT, message, message, jobId, displayName, -1, 0);
//        mNotificationManager.notify(DEFAULT_NOTIFICATION_TAG, jobId, notification);
        //prize delete by zhaojian for bug 37801 20171016 end
    	
    	/*prize-add for custom progressbar -hpf-2017-12-27-start*/
    	Log.d(TAG, "[onExportProcessed]");
    	mIsImportModle = false;
    	mDialogTitle = mContext.getString(R.string.prize_exporting);
    	mJobFinishStr = mContext.getString(R.string.prize_vcard_export_finished_message);
    	mJobCancelStr = mContext.getString(R.string.prize_vcard_export_cancel_message);
    	Message msg = mHandler.obtainMessage();
    	msg.what = CASE_SHOW_DIALOG;
    	mHandler.sendMessage(msg);
    	/*prize-add for custom progressbar -hpf-2017-12-27-end*/
    }

    @Override
    public void onExportFailed(ExportRequest request) {
        // prize modify for bug 41637 by zhaojian 20171104 start
//        mHandler.obtainMessage(0,
//                mContext.getString(R.string.vcard_export_request_rejected_message)).sendToTarget();
        final String message = mContext.getString(R.string.vcard_export_request_rejected_message);
        mHandler.obtainMessage(CASE_TOAST, message).sendToTarget();
        // prize modify for bug 41637 by zhaojian 20171104 end
    }

    @Override
    public void onCancelRequest(CancelRequest request, int type) {
        final String description = type == VCardService.TYPE_IMPORT ?
                mContext.getString(R.string.importing_vcard_canceled_title, request.displayName) :
                mContext.getString(R.string.exporting_vcard_canceled_title, request.displayName);
        // prize modify for bug 41637 by zhaojian 20171104 start
        //final Notification notification = constructCancelNotification(mContext, description);
        //mNotificationManager.notify(DEFAULT_NOTIFICATION_TAG, request.jobId, notification);
        mHandler.obtainMessage(CASE_TOAST, description).sendToTarget();
        // prize modify for bug 41637 by zhaojian 20171104 end
    }

    /**
     * Constructs a {@link Notification} showing the current status of import/export.
     * Users can cancel the process with the Notification.
     *
     * @param context
     * @param type import/export
     * @param description Content of the Notification.
     * @param tickerText
     * @param jobId
     * @param displayName Name to be shown to the Notification (e.g. "finished importing XXXX").
     * Typycally a file name.
     * @param totalCount The number of vCard entries to be imported. Used to show progress bar.
     * -1 lets the system show the progress bar with "indeterminate" state.
     * @param currentCount The index of current vCard. Used to show progress bar.
     */
    /* package */ static Notification constructProgressNotification(
            Context context, int type, String description, String tickerText,
            int jobId, String displayName, int totalCount, int currentCount) {
        // Note: We cannot use extra values here (like setIntExtra()), as PendingIntent doesn't
        // preserve them across multiple Notifications. PendingIntent preserves the first extras
        // (when flag is not set), or update them when PendingIntent#getActivity() is called
        // (See PendingIntent#FLAG_UPDATE_CURRENT). In either case, we cannot preserve extras as we
        // expect (for each vCard import/export request).
        //
        // We use query parameter in Uri instead.
        // Scheme and Authority is arbitorary, assuming CancelActivity never refers them.
        final Intent intent = new Intent(context, CancelActivity.class);
        final Uri uri = (new Uri.Builder())
                .scheme("invalidscheme")
                .authority("invalidauthority")
                .appendQueryParameter(CancelActivity.JOB_ID, String.valueOf(jobId))
                .appendQueryParameter(CancelActivity.DISPLAY_NAME, displayName)
                .appendQueryParameter(CancelActivity.TYPE, String.valueOf(type)).build();
        intent.setData(uri);

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setOngoing(true)
                .setProgress(totalCount, currentCount, totalCount == - 1)
                /// M: @{
                /*.setTicker(tickerText)*/
                .setContentTitle(description)
                /*.setColor(context.getResources().getColor(R.color.dialtacts_theme_color))*///prize remove fix bug-id:20612-huangpengfei-2016-8-22 
                .setSmallIcon(
                        type == VCardService.TYPE_IMPORT ? android.R.drawable.stat_sys_download_done
                                : R.drawable.mtk_stat_sys_upload_done)
                /// @}
                .setContentIntent(PendingIntent.getActivity(context, 0, intent, 0));
        if (totalCount > 0) {
            String percentage =
                    NumberFormat.getPercentInstance().format((double) currentCount / totalCount);
            builder.setContentText(percentage);
        }
        return builder.getNotification();
    }

    /**
     * Constructs a Notification telling users the process is canceled.
     *
     * @param context
     * @param description Content of the Notification
     */
    /* package */ static Notification constructCancelNotification(
            Context context, String description) {
        return new NotificationCompat.Builder(context)
                .setAutoCancel(true)
                .setSmallIcon(android.R.drawable.stat_notify_error)
                .setColor(context.getResources().getColor(R.color.dialtacts_theme_color))
                .setContentTitle(description)
                .setContentText(description)
                // Launch an intent that won't resolve to anything. Restrict the intent to this
                // app to make sure that no other app can steal this pending-intent b/19296918.
                .setContentIntent(PendingIntent
                        .getActivity(context, 0, new Intent(context.getPackageName(), null), 0))
                .getNotification();
    }

    /**
     * Constructs a Notification telling users the process is finished.
     *
     * @param context
     * @param description Content of the Notification
     * @param intent Intent to be launched when the Notification is clicked. Can be null.
     */
    /* package */ static Notification constructFinishNotification(
            Context context, String title, String description, Intent intent) {
        return constructFinishNotificationWithFlags(context, title, description, intent, 0);
    }

    /**
     * @param flags use FLAG_ACTIVITY_NEW_TASK to set it as new task, to get rid of cached files.
     */
    /* package */ static Notification constructFinishNotificationWithFlags(
            Context context, String title, String description, Intent intent, int flags) {
        return new NotificationCompat.Builder(context)
                .setAutoCancel(true)
                .setColor(context.getResources().getColor(R.color.dialtacts_theme_color))
                .setSmallIcon(android.R.drawable.stat_sys_download_done)
                .setContentTitle(title)
                .setContentText(description)
                // If no intent provided, include an intent that won't resolve to anything.
                // Restrict the intent to this app to make sure that no other app can steal this
                // pending-intent b/19296918.
                .setContentIntent(PendingIntent.getActivity(context, 0,
                        (intent != null ? intent : new Intent(context.getPackageName(), null)),
                        flags))
                .getNotification();
    }
    /// M: @{
    /**
     * Constructs a Notification telling users the process is finished.
     *
     * @param context
     * @param description Content of the Notification
     * @param intent Intent to be launched when the Notification is clicked. Can be null.
     */
    /* package */ static Notification constructFinishNotification(int type,
            Context context, String title, String description, Intent intent) {
        return constructFinishNotificationWithFlags(type, context, title, description, intent, 0);
    }

    /**
     * @param flags use FLAG_ACTIVITY_NEW_TASK to set it as new task, to get rid of cached files.
     */
    /* package */ static Notification constructFinishNotificationWithFlags(int type,
            Context context, String title, String description, Intent intent, int flags) {
        return new NotificationCompat.Builder(context)
                .setAutoCancel(true)
                .setColor(context.getResources().getColor(R.color.dialtacts_theme_color))
                .setSmallIcon(
                        type == VCardService.TYPE_IMPORT ? android.R.drawable.stat_sys_download_done
                                : R.drawable.mtk_stat_sys_upload_done)
                .setContentTitle(title)
                .setContentText(description)
                 // If no intent provided, include an intent that won't resolve to anything.
                // Restrict the intent to this app to make sure that no other app can steal this
                // pending-intent b/19296918.
                .setContentIntent(PendingIntent.getActivity(context, 0,
                        (intent != null ? intent : new Intent(context.getPackageName(), null)),
                        flags))
                .getNotification();
    }
    /// @}
    /**
     * Constructs a Notification telling the vCard import has failed.
     *
     * @param context
     * @param reason The reason why the import has failed. Shown in description field.
     */
    /* package */ static Notification constructImportFailureNotification(
            Context context, String reason) {
        return new NotificationCompat.Builder(context)
                .setAutoCancel(true)
                .setColor(context.getResources().getColor(R.color.dialtacts_theme_color))
                .setSmallIcon(android.R.drawable.stat_notify_error)
                .setContentTitle(context.getString(R.string.vcard_import_failed))
                .setContentText(reason)
                // Launch an intent that won't resolve to anything. Restrict the intent to this
                // app to make sure that no other app can steal this pending-intent b/19296918.
                .setContentIntent(PendingIntent
                        .getActivity(context, 0, new Intent(context.getPackageName(), null), 0))
                .getNotification();
    }

	@Override
	public void onComplete() {
		//mContext.finish();		
		/*prize-add for custom progressbar -hpf-2017-12-27-start*/
		Log.d(TAG, "[onComplete]  mIsCancelJob = " + mIsCancelJob
				+ "  mIsFinishJob = " + mIsFinishJob
				+ "  mIsImportModle = " + mIsImportModle);
		if(mIsCancelJob){
			mHandler.obtainMessage(CASE_TOAST, mJobCancelStr).sendToTarget();
		}
		if(mIsFinishJob){
			if(mIsImportModle) mJobFinishStr += mCurrentCount;
			mHandler.obtainMessage(CASE_TOAST, mJobFinishStr).sendToTarget();
		}
		Message msg = mHandler.obtainMessage();
		msg.what = CASE_HIDE_DIALOG;
		mHandler.sendMessage(msg);
		/*prize-add for custom progressbar -hpf-2017-12-27-end*/
	}

	
	/*prize-add for custom progressbar -hpf-2017-12-27-start*/
	public void onExportParsed(int currentCount,int totalCount) {
		if(currentCount == totalCount){
    		mIsFinishJob = true;
    	}
    	Message msg = mHandler.obtainMessage();
    	msg.what = CASE_UPDATE_PROGRESS;
    	msg.arg1 = currentCount;
    	msg.arg2 = totalCount;
    	mHandler.sendMessage(msg);
	}
	
	public void doCancelExport(){
		mIsCancelJob = true;
	}
	
	private void showProgressDialog() {
		Log.d(TAG, "[showProgressDialog]");
		hideWaittingDialog();
		if(!isActivityRunning(mContext)) return;
		View rootView = View.inflate(mContext, R.layout.prize_contacts_progress_dialog, null);
		mPg = (PrizeCirclePercentView) rootView.findViewById(R.id.custom_progressBar);
		mPg.setOnFinishListener(new OnFinishListener() {
			
			@Override
			public void onFinish() {
				count --;
				Log.d(TAG, "[onFinish]  count = "+count);
				if(count <= 0){
					showWaittingDialog();
				}else{
					Message msg = mHandler.obtainMessage();
			    	msg.what = CASE_UPDATE_PROGRESS;
			    	msg.arg1 = 0;
			    	msg.arg2 = 100;
			    	mHandler.sendMessage(msg);
				}
			}
		});
		mPgTvTitle = (TextView) rootView.findViewById(R.id.tv_title);
		mPgTvTitle.setText(mDialogTitle);
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setView(rootView);
		builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (mOnJobCancelListener != null) {
					mOnJobCancelListener.onImportExportCancel();
					showWaittingDialog();
				}
			}
		});
		mProgressDialog = builder.create();
		mProgressDialog.setCanceledOnTouchOutside(false);
		//mProgressDialog.getWindow().setType((WindowManager.LayoutParams.TYPE_SYSTEM_ALERT));
		mProgressDialog.setCancelable(false);
		mProgressDialog.show();
		Window dialogWindow = mProgressDialog.getWindow();
		WindowManager.LayoutParams p = dialogWindow.getAttributes();
		p.width = mDialogWidth;
		dialogWindow.setAttributes(p);

	}
	private void showWaittingDialog(){
		Log.d(TAG, "[showWaittingDialog]");
		hideProgressDialog();
		if(!isActivityRunning(mContext)) return;
		View rootView = View.inflate(mContext, R.layout.prize_contacts_progress_dialog_waitting, null);
		mWaittingProgressBar = (ProgressBar) rootView.findViewById(R.id.pg_waitting);
		TextView waittigTitle = (TextView) rootView.findViewById(R.id.tv_waitting_title);
		waittigTitle.setText(mContext.getResources().getString(R.string.prize_neatening));
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setView(rootView);
		builder.setNegativeButton(R.string.prize_neaten_background, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				mContext.finish();
			}
		});
		mProgressDialogWaitting = builder.create();
		mProgressDialogWaitting.setCanceledOnTouchOutside(false);
		//mProgressDialogWaitting.getWindow().setType((WindowManager.LayoutParams.TYPE_SYSTEM_ALERT));
		mProgressDialogWaitting.setCancelable(false);
		// prize add if-judge for bug 54802 by zhaojian 20180413 start
		if(ImportVCardActivity.sIsActivityActive || ExportVCardActivity.sIsActivityActive) {
			mProgressDialogWaitting.show();
			Window dialogWindow = mProgressDialogWaitting.getWindow();
			WindowManager.LayoutParams p = dialogWindow.getAttributes();
			p.width = mDialogWidth;
			dialogWindow.setAttributes(p);
		}
		// prize add if-judge for bug 54802 by zhaojian 20180413 end
	}
	
	private void hideWaittingDialog() {
		if (mProgressDialogWaitting != null){
			Log.d(TAG, "[hideWaittingDialog]");
			mProgressDialogWaitting.cancel();
		}
	}

	private void hideProgressDialog() {
		if (mProgressDialog != null){
			Log.d(TAG, "[hideProgressDialog]");
			mProgressDialog.cancel();
		}
	}
	
	private OnImportExportCancelListener mOnJobCancelListener;
	public interface OnImportExportCancelListener{
		void onImportExportCancel();
	}
	
	public void setOnImportCancelListener(OnImportExportCancelListener onJobCancelListener){
		mOnJobCancelListener = onJobCancelListener;
	}
	
	private boolean isActivityRunning(Activity activity) {
		String packageName = activity.getLocalClassName();
		Log.d(TAG, "[isActivityRunning]  packageName = " + packageName);
		if("common.vcard.ExportVCardActivity".equals(packageName)
				|| "common.vcard.ImportVCardActivity".equals(packageName)){
			Log.d(TAG, "[isActivityRunning] return true");
			return true;
		}
		
		Log.d(TAG, "[isActivityRunning] return false");
		return false;
	}
	
	/*prize-add for custom progressbar -hpf-2017-12-27-end*/
    
}
