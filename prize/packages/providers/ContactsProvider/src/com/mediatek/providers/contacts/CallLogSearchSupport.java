/*
 * Copyright (C) 2009 The Android Open Source Project
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

package com.mediatek.providers.contacts;

import android.app.SearchManager;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.CallLog.Calls;
import android.provider.ContactsContract.CommonDataKinds;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.text.format.Time;
import android.util.Log;

import com.android.providers.contacts.ContactsDatabaseHelper.Tables;
import com.android.providers.contacts.ContactsProvider2;
import com.android.providers.contacts.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

/**
 * Support for global search integration for Contacts.
 */
public class CallLogSearchSupport {
    private static final String TAG = "CallLogSearchSupport";

    private static final String[] SEARCH_SUGGESTIONS_BASED_ON_NAME_COLUMNS = {
            "_id", SearchManager.SUGGEST_COLUMN_TEXT_1, SearchManager.SUGGEST_COLUMN_TEXT_2,
            SearchManager.SUGGEST_COLUMN_ICON_1, SearchManager.SUGGEST_COLUMN_ICON_2,
            SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID, SearchManager.SUGGEST_COLUMN_SHORTCUT_ID
    };

    private final Context mContext;

    /**
     * M: Constructor.
     * @param context
     */
    public CallLogSearchSupport(Context context) {
        mContext = context;
    }

    /**
     * M: SearchSuggestionQuery.
     */
    private interface SearchSuggestionQuery {
        public static final String TABLE = Tables.CALLS;
        public static final String[] COLUMNS = {
            Calls._ID,
            Calls.NUMBER,
            Calls.DATE,
            Calls.TYPE,
            Calls.CACHED_NUMBER_TYPE,
            Calls.CACHED_NAME,
            Calls.CACHED_PHOTO_URI
         };

        public static final int CALLS_ID = 0;
        public static final int CALLS_NUMBER = 1;
        public static final int CALLS_DATE = 2;
        public static final int CALLS_TYPE = 3;
        public static final int CALLS_NUMBER_TYPE = 4;
        public static final int DISPLAY_NAME = 5;
        public static final int PHOTO_URI = 6;
    }

    /**
     * M: SearchSuggestion.
     */
    private static class SearchSuggestion {
        long mCallsId;
        String mNumber;
        long mDate;
        int mType;
        int mCallNumberLabel;
        String mSortKey;
        int mIsVTCall;
        boolean mProcessed;
        String mText1;
        String mText2;
        String mIcon1;
        String mIcon2;
        String mPhotoUri;

        public SearchSuggestion(long callsId) {
            this.mCallsId = callsId;
        }

        private void process() {
            if (mProcessed) {
                return;
            }
            Log.i(TAG, "mPhotoUri : " + mPhotoUri);
            if (null != mPhotoUri) {
                mIcon1 = mPhotoUri.toString();
            } else {
                mIcon1 = String.valueOf(com.android.internal.R.drawable.ic_contact_picture);
            }
            // Set the icon
            switch (mType) {
                /**M: ALPS00818980, vtCall initial value is -1,  0 & -1 for voiceCall,
                 * 1 for vtCall */
                case Calls.INCOMING_TYPE:
                    if (mIsVTCall <= 0) {
                        mIcon2 = String.valueOf(R.drawable.mtk_incoming_search);
                    } else {
                        mIcon2 = String.valueOf(R.drawable.mtk_video_incoming_search);
                    }
                    break;
                case Calls.OUTGOING_TYPE:
                    if (mIsVTCall <= 0) {
                        mIcon2 = String.valueOf(R.drawable.mtk_outing_search);
                    } else {
                        mIcon2 = String.valueOf(R.drawable.mtk_video_outing_search);
                    }
                    break;
                case Calls.MISSED_TYPE:
                    if (mIsVTCall <= 0) {
                        mIcon2 = String.valueOf(R.drawable.mtk_missed_search);
                    } else {
                        mIcon2 = String.valueOf(R.drawable.mtk_video_missed_search);
                    }
                    break;
                /// M: ALPS022855666, for AutoReject icon@{
                case Calls.AUTO_REJECT_TYPE:
                    mIcon2 = String.valueOf(R.drawable.mtk_autoreject_search);
                    break;
                /// @}
            }
            mProcessed = true;
        }

        /**
         * Returns key for sorting search suggestions.
         * <p>
         * TODO: switch to new sort key
         */
        public String getSortKey() {
            if (mSortKey == null) {
                process();
                mSortKey = Long.toString(mDate);
            }
            return mSortKey;
        }

        @SuppressWarnings({
            "unchecked"
        })
        public ArrayList asList(String[] projection) {
            process();

            ArrayList<Object> list = new ArrayList<Object>();
            if (projection == null) {
                list.add(mCallsId);
                list.add(mText1);
                list.add(mText2);
                list.add(mIcon1);
                list.add(mIcon2);
                list.add(mCallsId);
                list.add(mCallsId);
            } else {
                for (int i = 0; i < projection.length; i++) {
                    addColumnValue(list, projection[i]);
                }
            }
            return list;
        }

        private void addColumnValue(ArrayList<Object> list, String column) {
            if ("_id".equals(column)) {
                list.add(mCallsId);
            } else if (SearchManager.SUGGEST_COLUMN_TEXT_1.equals(column)) {
                list.add(mText1);
            } else if (SearchManager.SUGGEST_COLUMN_TEXT_2.equals(column)) {
                list.add(mText2);
            } else if (SearchManager.SUGGEST_COLUMN_ICON_1.equals(column)) {
                list.add(mIcon1);
            } else if (SearchManager.SUGGEST_COLUMN_ICON_2.equals(column)) {
                list.add(mIcon2);
            } else if (SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID.equals(column)) {
                list.add(mCallsId);
            } else if (SearchManager.SUGGEST_COLUMN_SHORTCUT_ID.equals(column)) {
                list.add(mCallsId);
            } else {
                throw new IllegalArgumentException("Invalid column name: " + column);
            }
        }

        private String getIcon(int slotId, int isSdn) {
            Log.i(TAG, " [getIcon] slotId is " + slotId + " isSdn : " + isSdn);
            return String.valueOf(com.android.internal.R.drawable.ic_contact_picture);
        }
    }

    public Cursor handleSearchSuggestionsQuery(SQLiteDatabase db, Uri uri, String limit) {
        if (uri.getPathSegments().size() <= 1) {
            return null;
        }

        final String searchClause = uri.getLastPathSegment();
        return buildCursorForSearchSuggestions(db, searchClause, limit);
    }

    private Cursor buildCursorForSearchSuggestions(SQLiteDatabase db, String filter, String limit) {
        return buildCursorForSearchSuggestions(db, null, null, filter, limit);
    }

    private Cursor buildCursorForSearchSuggestions(SQLiteDatabase db, String[] projection,
            String selection, String filter, String limit) {
        ArrayList<SearchSuggestion> suggestionList = new ArrayList<SearchSuggestion>();
        HashMap<Long, SearchSuggestion> suggestionMap = new HashMap<Long, SearchSuggestion>();

        final boolean haveFilter = !TextUtils.isEmpty(filter);

        String table = SearchSuggestionQuery.TABLE;

        String where = null;
        if (!TextUtils.isEmpty(selection) && !"null".equals(selection)) {
            where = selection;
        }
        if (haveFilter) {
            //TODO: check nomalizeName
            String nomalizeName = filter;//NameNormalizer.normalize(filter);
            /// M: keep this logic same with CallogManager's logCall, just strip separators with
            /// phone number before query happen, don't normalize it totally.
            /// Otherwise, some non-separator char in calllog can't be searched
            /// like "*" "#", etc. @{
            String number = filter;
            if (isPhoneNumber(filter)) {
                number = PhoneNumberUtils.stripSeparators(filter);
            }
            /// @}
            if (TextUtils.isEmpty(where)) {
                where = Tables.CALLS + "." + Calls.NUMBER + " GLOB '*" + number + "*' " + "OR ("
                        + Calls.CACHED_NAME + " GLOB '*" + nomalizeName + "*')";
            } else {
                where += " AND " + Tables.CALLS + "." + Calls.NUMBER + " GLOB '*" + number + "*' "
                        + "OR (" + Calls.CACHED_NAME + " GLOB '*" + nomalizeName + "*')";
            }
        }
        // M: fix CR:ALPS01753330,add tables.calls to solve ambiguous _id columns problems.
        Cursor c = db.query(false, table, SearchSuggestionQuery.COLUMNS, where, null, Tables.CALLS
                + "." + Calls._ID, null, null, limit);
        Log.d(TAG,
                "[buildCursorForSearchSuggestions] where = " + where + "; Count =" + c.getCount());
        try {
            while (c.moveToNext()) {
                long callsId = c.getLong(SearchSuggestionQuery.CALLS_ID);
                SearchSuggestion suggestion = suggestionMap.get(callsId);
                if (suggestion == null) {
                    suggestion = new SearchSuggestion(callsId);
                    suggestionMap.put(callsId, suggestion);
                }
                suggestion.mDate = c.getLong(SearchSuggestionQuery.CALLS_DATE);
                Time time = new Time();
                time.set(suggestion.mDate);
                String number = c.getString(SearchSuggestionQuery.CALLS_NUMBER);
                String name = c.getString(SearchSuggestionQuery.DISPLAY_NAME);
                if (TextUtils.isEmpty(name)) {
                    Resources r = mContext.getResources();
                    suggestion.mText1 = r.getString(R.string.unknown);
                    suggestion.mText2 = number;
                } else {
                    suggestion.mText1 = name;
                    // M: fix CR:ALPS01676096,substitute CACHED_NUMBER_TYPE
                    // for Data2 to ensure query the latest data.
                    int type = c.getInt(SearchSuggestionQuery.CALLS_NUMBER_TYPE);
                    String label = (String) CommonDataKinds.Phone.getTypeLabel(mContext
                            .getResources(), type, null);
                    suggestion.mText2 = label + " " + number;
                }
                suggestion.mType = c.getInt(SearchSuggestionQuery.CALLS_TYPE);
                suggestion.mPhotoUri = c.getString(SearchSuggestionQuery.PHOTO_URI);
                suggestionList.add(suggestion);
            }
        } catch (Exception e) {
            Log.e(TAG, "[buildCursorForSearchSuggestions] catched exception !!!");
            e.printStackTrace();
        } finally {
            c.close();
        }

        Collections.sort(suggestionList, new Comparator<SearchSuggestion>() {
            public int compare(SearchSuggestion row1, SearchSuggestion row2) {
                return row1.getSortKey().compareTo(row2.getSortKey());
            }
        });

        MatrixCursor retCursor = new MatrixCursor(projection != null ? projection
                : SEARCH_SUGGESTIONS_BASED_ON_NAME_COLUMNS);

        for (int i = 0; i < suggestionList.size(); i++) {
            retCursor.addRow(suggestionList.get(i).asList(projection));
        }
        Log.i(TAG, "[buildCursorForSearchSuggestions] retCursor = " + retCursor.getCount());
        return retCursor;
    }

    public Cursor handleSearchShortcutRefresh(SQLiteDatabase db, String[] projection,
            String callId, String filter) {

        return buildCursorForSearchSuggestions(db, null, Tables.CALLS + "." + Calls._ID + "="
                + callId, null, null);
    }

        /**
         * M: checking query string whether matching a phone number
         * @param query
         * @return ture if query match a phone number
         */
        public boolean isPhoneNumber(String query) {
            if (TextUtils.isEmpty(query)) {
                return false;
            }
            // Assume a phone number if it has at least 1 digit.
            return ContactsProvider2.countPhoneNumberDigits(query) > 0;
        }

}
