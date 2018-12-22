/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.android.server.telecom.tests;

import android.os.Handler;
import android.os.Message;
import com.android.internal.telephony.CallerInfo;
import com.android.internal.telephony.CallerInfoAsyncQuery;
import com.android.server.telecom.CallerInfoAsyncQueryFactory;
import com.android.server.telecom.Log;

import android.content.Context;

import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Controls a test {@link CallerInfoAsyncQueryFactory} to abstract away the asynchronous retrieval
 * of caller information from the Android contacts database.
 */
public class CallerInfoAsyncQueryFactoryFixture implements
        TestFixture<CallerInfoAsyncQueryFactory> {

    static class Request {
        int mToken;
        int mSubId;
        Object mCookie;
        /// M: auto reply
        boolean mReplied = false;
        CallerInfoAsyncQuery.OnQueryCompleteListener mListener;
        void reply() {
            replyWithCallerInfo(new CallerInfo());
        }

        void replyWithCallerInfo(CallerInfo callerInfo) {
            /// M: auto reply
            mReplied = true;
            mListener.onQueryComplete(mToken, mCookie, callerInfo);
        }
    }

    CallerInfoAsyncQueryFactory mCallerInfoAsyncQueryFactory = new CallerInfoAsyncQueryFactory() {
        @Override
        public CallerInfoAsyncQuery startQuery(int token, Context context, String number,
                CallerInfoAsyncQuery.OnQueryCompleteListener listener, Object cookie, int subId) {
            Log.d(CallerInfoAsyncQueryFactoryFixture.this, "[startQuery] stopped by" +
                    " CallerInfoAsyncQueryFactoryFixture," +
                    " listener: " + listener + ", CallStack: " + new Throwable().toString());
            Request r = new Request();
            r.mToken = token;
            r.mCookie = cookie;
            r.mSubId = subId;
            r.mListener = listener;
            mRequests.add(r);
            /**
             * M: Auto reply caller info query, avoid blocking the follow-up queries. @{
             */
            new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    if (mAutoReplyCallerInfo != null) {
                        for (Request r : mRequests) {
                            if (!r.mReplied) {
                                Log.d(CallerInfoAsyncQueryFactoryFixture.this, "auto reply" +
                                        " CallerInfo " + mAutoReplyCallerInfo +
                                        " to listener: " + r.mListener);
                                r.replyWithCallerInfo(mAutoReplyCallerInfo);
                            }
                        }
                    }
                }
            }.sendEmptyMessage(0);
            /** @} */
            return Mockito.mock(CallerInfoAsyncQuery.class);
        }
    };

    final List<Request> mRequests = Collections.synchronizedList(new ArrayList<Request>());

    public CallerInfoAsyncQueryFactoryFixture() throws Exception {
        Log.i(this, "Creating ...");
    }

    @Override
    public CallerInfoAsyncQueryFactory getTestDouble() {
        return mCallerInfoAsyncQueryFactory;
    }

    /**
     * M: For auto reply CallerInfo @{
     */
    private CallerInfo mAutoReplyCallerInfo;
    public void setAutoReplyCallerInfo(CallerInfo info) {
        Log.d(this, "[setAutoReplyCallerInfo] " + info);
        mAutoReplyCallerInfo = info;
        for (Request r : mRequests) {
            if (!r.mReplied) {
                Log.d(CallerInfoAsyncQueryFactoryFixture.this, "auto reply when configured" +
                        " CallerInfo " + mAutoReplyCallerInfo +
                        " to listener: " + r.mListener);
                r.replyWithCallerInfo(mAutoReplyCallerInfo);
            }
        }
    }
    public void clearAutoReplyCallerInfo() {
        Log.d(this, "[clearAutoReplyCallerInfo] ");
        mAutoReplyCallerInfo = null;
    }
    /** @} */
}
