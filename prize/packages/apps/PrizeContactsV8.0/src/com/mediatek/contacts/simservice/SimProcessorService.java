/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 *
 * MediaTek Inc. (C) 2010. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER ON
 * AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL WARRANTIES,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NONINFRINGEMENT.
 * NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH RESPECT TO THE
 * SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY, INCORPORATED IN, OR
 * SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES TO LOOK ONLY TO SUCH
 * THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO. RECEIVER EXPRESSLY ACKNOWLEDGES
 * THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES
 * CONTAINED IN MEDIATEK SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK
 * SOFTWARE RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S ENTIRE AND
 * CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE RELEASED HEREUNDER WILL BE,
 * AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE MEDIATEK SOFTWARE AT ISSUE,
 * OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE CHARGE PAID BY RECEIVER TO
 * MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek Software")
 * have been modified by MediaTek Inc. All revisions are subject to any receiver's
 * applicable license agreements with MediaTek Inc.
 */
package com.mediatek.contacts.simservice;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.android.contacts.common.vcard.ProcessorBase;

import com.mediatek.contacts.simservice.SimProcessorManager.ProcessorManagerListener;
import com.mediatek.contacts.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/*PRIZE-add default group -zhangzhonghao-2015-5-20 -start*/
import java.util.ArrayList;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.net.Uri;
import com.android.contacts.common.model.account.AccountType;
import android.database.Cursor;
import android.content.OperationApplicationException;
import android.provider.ContactsContract.Groups;
import com.android.contacts.R;
/*PRIZE-add default group -zhangzhonghao-2015-5-20 -end*/
public class SimProcessorService extends Service {
    private final static String TAG = "SIMProcessorService";

    private static final int CORE_POOL_SIZE = 2;
    private static final int MAX_POOL_SIZE = 10;
    private static final int KEEP_ALIVE_TIME = 10; // 10 seconds
    public static final String EXTRA_CALLBACK_INTENT = "callbackIntent";

    private SimProcessorManager mProcessorManager;
    private AtomicInteger mNumber = new AtomicInteger();
    private final ExecutorService mExecutorService = createThreadPool(CORE_POOL_SIZE);

	/*PRIZE-add default group -zhangzhonghao-2015-5-20 -start*/
	private static boolean sIsRunningNumberCheck = false;
	/*PRIZE-add default group -zhangzhonghao-2015-5-20 -end*/

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "[onCreate]...");
        mProcessorManager = new SimProcessorManager(this, mListener);
    }

		/*PRIZE-add default group -zhangzhonghao-2015-5-20 -start*/

	    private void addDefaultGroup() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "isRunningNumberCheck before:"
                        + sIsRunningNumberCheck);
                if (sIsRunningNumberCheck) {
                    return;
                }
                sIsRunningNumberCheck = true;
                Log.i(TAG, "isRunningNumberCheck after:"
                        + sIsRunningNumberCheck);
                Uri uri = Groups.CONTENT_URI;
                Cursor groupCursor = getContentResolver().query(uri,
                        new String[] { Groups._ID, Groups.TITLE },
                        Groups.DELETED + "=0", null, null);
                try {
                    if (groupCursor != null && groupCursor.getCount() > 0) {
                        return;
                    } else {
                        String defaultGroups[] = { getResources().getString(R.string.prize_worker) , 
													getResources().getString(R.string.prize_family),
                                					getResources().getString(R.string.prize_friends), 
                                					getResources().getString(R.string.prize_schoolmate)/*,
                                					getResources().getString(R.string.prize_VIP)*/ };//prize-delete-huangliemin-2016-7-29
                        final ArrayList<ContentProviderOperation> operationList = new ArrayList<ContentProviderOperation>();
                        ContentProviderOperation.Builder builder = ContentProviderOperation
                                .newInsert(uri);
                        ContentValues groupValues = new ContentValues();
                        for (int i = 0; i < defaultGroups.length; i++) {
                            groupValues.put(Groups.ACCOUNT_NAME,
                                    AccountType.ACCOUNT_NAME_LOCAL_PHONE);
                            groupValues.put(Groups.ACCOUNT_TYPE,
                                    AccountType.ACCOUNT_TYPE_LOCAL_PHONE);
                            groupValues.put(Groups.TITLE, defaultGroups[i]);
                            groupValues.put(Groups.GROUP_IS_READ_ONLY, 1);
                            builder.withValues(groupValues);
                            operationList.add(builder.build());
                            try {
                                getContentResolver().applyBatch(
                                        ContactsContract.AUTHORITY,
                                        operationList);
                                groupValues.clear();
                                operationList.clear();
                            } catch (RemoteException e) {
                                Log.e(TAG,
                                        String.format("%s:%s", e.toString(),
                                                e.getMessage()));
                            }catch (OperationApplicationException e){
								Log.e(TAG,String.format("%s:%s",e.toString(),e.getMessage()));
							}
                        }
                    }
                } finally {
                    if (groupCursor != null) {
                        groupCursor.close();
                    }
                }
                Log.i(TAG, "isRunningNumberCheck insert:"
                        + sIsRunningNumberCheck);
                sIsRunningNumberCheck = false;
            }
        }).start();
    }

	/*PRIZE-add default group -zhangzhonghao-2015-5-20 -end*/
	

    @Override
    public int onStartCommand(Intent intent, int flags, int id) {
        super.onStartCommand(intent, flags, id);
		/*PRIZE-add default group -zhangzhonghao-2015-5-20 -start*/
		addDefaultGroup();
		/*PRIZE-add default group -zhangzhonghao-2015-5-20 -end*/
        processIntent(intent);
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "[onDestroy]...");
    }

    private void processIntent(Intent intent) {
        if (intent == null) {
            Log.w(TAG, "[processIntent] intent is null.");
            return;
        }
        int subId = intent.getIntExtra(SimServiceUtils.SERVICE_SUBSCRIPTION_KEY, 0);
        int workType = intent.getIntExtra(SimServiceUtils.SERVICE_WORK_TYPE, -1);

        mProcessorManager.handleProcessor(getApplicationContext(), subId, workType, intent);
    }

    private SimProcessorManager.ProcessorManagerListener mListener =
            new ProcessorManagerListener() {
        @Override
        public void addProcessor(long scheduleTime, ProcessorBase processor) {
            if (processor != null) {
                try {
                    mExecutorService.execute(processor);
                } catch (RejectedExecutionException e) {
                    Log.e(TAG, "[addProcessor] RejectedExecutionException: " + e.toString());
                }
            }
        }

        @Override
        public void onAllProcessorsFinished() {
            Log.d(TAG, "[onAllProcessorsFinished]...");
            stopSelf();
            mExecutorService.shutdown();
        }
    };

    private ExecutorService createThreadPool(int initPoolSize) {
        return new ThreadPoolExecutor(initPoolSize, MAX_POOL_SIZE, KEEP_ALIVE_TIME,
                TimeUnit.SECONDS,
                new SynchronousQueue<Runnable>(), new ThreadFactory() {
                    @Override
                    public Thread newThread(Runnable r) {
                        String threadName = "SIM Service - " + mNumber.getAndIncrement();
                        Log.d(TAG, "[createThreadPool]thread name:" + threadName);
                        return new Thread(r, threadName);
                    }
                });
    }
}
