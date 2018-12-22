package com.android.dialer.prize.tmsdkcallmark;

import android.content.Context;
import android.database.Cursor;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.android.dialer.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import tmsdk.common.creator.ManagerCreatorC;
import tmsdk.common.module.numbermarker.INumQueryRetListener;
import tmsdk.common.module.numbermarker.NumMarkerManager;
import tmsdk.common.module.numbermarker.NumQueryRet;
import tmsdk.common.module.numbermarker.NumQueryReq;

/**
 * Created by wangzhong on 2017/5/5.
 */
public class CallMarkManager {

    public static final String TAG = "TMSDK_Call_Mark";
    private static final boolean DEBUG = false;

    public static final int MESSAGE_WHAT_CLOUD_FETCH = 101;
    public static final int MESSAGE_WHAT_DBCACHE_FETCH = 102;
	//prize add by lijimeng TMSDK_Call_Mark 20180906-start
    public static final int PRIZE_MESSAGE_WHAT_CLOUD_FETCH = 103;
	//prize add by lijimeng TMSDK_Call_Mark 20180906-end
    public static final String DB_CACHE_TAG_FLAG_HAS_DATA = "hasdata";
    public static final String DB_CACHE_TAG_FLAG_NO_DATA = "nodata";

    public static final String DEFAULT_MARK_TYPE = "prizecallmark";

    private ExecutorService fixedThreadPool = null;
    private static CallMarkManager callMarkManager;

    private Context mContext = null;
    private ICallMarkCacheDao mICallMarkCacheDao = null;
    private Map<String, TextView> mTVMaps = new HashMap<String, TextView>();
    private NumMarkerManager mNumMarkerManager = null;

    private TextView mTagTextView;

    private void showLog(String log) {
        if (DEBUG) {
            Log.d(TAG, ":::::::::: " + log);
        }
    }

    private CallMarkManager() {
        if (null == fixedThreadPool) {
            fixedThreadPool = Executors.newFixedThreadPool(6);
        }
    }

    public static CallMarkManager getInstance() {
        if (null == callMarkManager) {
            callMarkManager = new CallMarkManager();
        }
        return callMarkManager;
    }

    public boolean isSuportTMSDKCallMark() {
        if ("1".equals(android.os.SystemProperties.get("ro.prize_callmark_support", "0"))) {
            return true;
        }
        return false;
    }

    public void setCallMarkTagView(TextView view) {
        this.mTagTextView = view;
    }

    public void setCallMark(Context context, TextView view, String number, ICallMarkCacheDao iCallMarkCacheDao) {
        this.mContext = context;
        this.mICallMarkCacheDao = iCallMarkCacheDao;
        /*view.setTag(R.id.prize_tag_number, number);*/
        setTagNumber(view, number);
        view.setVisibility(View.GONE);
        if (null != mTagTextView) mTagTextView.setVisibility(View.GONE);
        mTVMaps.put(number, view);

        //getDBCacheFetchNumberInfo(getCallMarkCache(), number, view);
        getTMSDKFetchNumberInfo(view, number);
    }

    private void initNumMarkerManager() {
        if (null == mNumMarkerManager) {
            mNumMarkerManager = ManagerCreatorC.getManager(NumMarkerManager.class);
        }
    }
    //prize-modify-by-lijimeng-private to public-20180829-start
    public void getTMSDKFetchNumberInfo(TextView view, String number) {
        NumQueryRet item = getLocalFetchNumberInfo(number);
        //prize-modify-by-lijimeng-TMSDK_Call_Mark-20180829-start
        //if (null != item) {
        if (null != item && view != null) {
            showLog("TMSDK LocalFetchNumberInfo ::::: item.toString() : " + item.toString());
            setTextTargetTextView(true, mNumMarkerManager.getTagName(item.tagType), item.number, view);
        } else {
            getCloudFetchNumberInfo(number, view);
        }
        //prize-modify-by-lijimeng-TMSDK_Call_Mark-20180829-end
    }
    //prize-modify-by-lijimeng-TMSDK_Call_Mark-20180829-end
    private NumQueryRet getLocalFetchNumberInfo(String number) {
        initNumMarkerManager();
        return mNumMarkerManager.localFetchNumberInfo(number);
    }

    private void getCloudFetchNumberInfo(final String number, final TextView view) {
        initNumMarkerManager();
        fixedThreadPool.execute(new Thread() {
            @Override
            public void run() {
                showLog("TMSDK CloudFetchNumberInfo ::::: number : " + number);
                List<NumQueryReq> numbers = new ArrayList<NumQueryReq>();
                // 18706206046
                /* NumQueryReq.TYPE_Common, NumQueryReq.TYPE_Calling, NumQueryReq.TYPE_Called */
                NumQueryReq queryInfo = new NumQueryReq(number, NumQueryReq.TYPE_Common);
                numbers.add(queryInfo);
                /*queryInfo = new NumQueryReq("01062671188", NumQueryReq.TYPE_Called);
                numbers.add(queryInfo);*/

                mNumMarkerManager.cloudFetchNumberInfo(numbers, new INumQueryRetListener() {
                    @Override
                    public void onResult(int arg0, java.util.List<NumQueryRet> arg1) {
                        showLog("TMSDK CloudFetchNumberInfo ::::: onResult()");
                        /*view.setTag(R.id.prize_tag_data, arg1);*/
						//prize-modify-by-lijimeng-TMSDK_Call_Mark-20180829-start
                        /*setTagData(view, arg1);
                        mHandler.obtainMessage(MESSAGE_WHAT_CLOUD_FETCH, arg0, 0, view).sendToTarget();*/
                        if (view != null) {
                            setTagData(view, arg1);
                            mHandler.obtainMessage(MESSAGE_WHAT_CLOUD_FETCH, arg0, 0, view).sendToTarget();
                        } else {
                            if (arg1 != null && arg1.size() > 0) {
                                Message msg = Message.obtain();
                                msg.what = PRIZE_MESSAGE_WHAT_CLOUD_FETCH;
                                msg.obj = arg1;
                                mHandler.sendMessage(msg);
                            }else {
                                showLog("TMSDK CloudFetchNumberInfo ::::: onResult() == null");
                            }
                        }
                        //prize-modify-by-lijimeng-TMSDK_Call_Mark-20180829-end
                    }
                });
            }
        });
    }

    private void getDBCacheFetchNumberInfo(final Cursor cursor, final String number, final TextView view) {
        fixedThreadPool.execute(new Thread() {
            @Override
            public void run() {
                if (null != cursor && cursor.getCount() > 1) {
                    while (cursor.moveToNext()) {
                        String cPhoneNumber = cursor.getString(cursor.getColumnIndex(
                                com.android.dialer.prize.tmsdkcallmark.CallMarkCacheSQLiteOpenHelper.TABLE_COLUMN_PHONE_NUMBER));
                        //showLog(":::::DBCache has data!!   cursor cPhoneNumber : " + cPhoneNumber + ",   number : " + number);
                        if (cPhoneNumber.equals(number)) {
                            showLog(":::::DBCache find data!!   cursor cPhoneNumber : " + cPhoneNumber + ",   number : " + number);
                            /*view.setVisibility(View.VISIBLE);
                            view.setText(cursor.getString(cursor.getColumnIndex(
                                    com.android.dialer.prize.tmsdkcallmark.CallMarkCacheSQLiteOpenHelper.TABLE_COLUMN_TYPE)));*/

                            /*view.setTag(R.id.prize_tag_number, number);
                            view.setTag(R.id.prize_tag_data, cursor.getString(cursor.getColumnIndex(
                                    com.android.dialer.prize.tmsdkcallmark.CallMarkCacheSQLiteOpenHelper.TABLE_COLUMN_TYPE)));
                            view.setTag(R.id.prize_tag_flag, DB_CACHE_TAG_FLAG_HAS_DATA);*/
                            setTagNumber(view, number);
                            setTagData(view, cursor.getString(cursor.getColumnIndex(
                                    com.android.dialer.prize.tmsdkcallmark.CallMarkCacheSQLiteOpenHelper.TABLE_COLUMN_TYPE)));
                            setTagFlag(view, DB_CACHE_TAG_FLAG_HAS_DATA);
                            mHandler.obtainMessage(MESSAGE_WHAT_DBCACHE_FETCH, 0, 0, view).sendToTarget();
                            cursor.moveToPosition(-1);
                            return;
                        }
                    }
                    cursor.moveToPosition(-1);
                }
                /*view.setTag(R.id.prize_tag_number, number);
                view.setTag(R.id.prize_tag_flag, DB_CACHE_TAG_FLAG_NO_DATA);*/
                setTagNumber(view, number);
                setTagFlag(view, DB_CACHE_TAG_FLAG_NO_DATA);
                mHandler.obtainMessage(MESSAGE_WHAT_DBCACHE_FETCH, 0, 0, view).sendToTarget();
            }
        });
    }

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_WHAT_CLOUD_FETCH:
                    processCloudFetchResult(msg);
                    break;
                case MESSAGE_WHAT_DBCACHE_FETCH:
                    processDBCacheFetchResult(msg);
                    break;
				//prize-modify-by-lijimeng-TMSDK_Call_Mark-20180829-start
                case PRIZE_MESSAGE_WHAT_CLOUD_FETCH:
                    prizeProcessCloudFetchResult(msg);
                    break;
				//prize-modify-by-lijimeng-TMSDK_Call_Mark-20180829-end
            }
        }
    };

    //prize-add-by-lijimeng- TMSDK_Call_Mark-20180829-start
    private void prizeProcessCloudFetchResult(Message msg) {
        List<NumQueryRet> retList = (List<NumQueryRet>) msg.obj;
        StringBuilder strBuilder = new StringBuilder();
        if (retList != null) {
            for (NumQueryRet item : retList) {
                strBuilder.append(disCloudResult(item) + "\n");
            }
            if (retList.size() > 0) {
                String type = mNumMarkerManager.getTagName(retList.get(0).tagType);
                String number = retList.get(0).number;
                showLog("prizeProcessCloudFetchResult TMSDK CloudFetchNumberInfo ::::: processCloudFetchResult  mark : "
                        + type + "   number::: " + number);
                if (null == type) {
                    return;
                }
                if (null != type && type.equals("")) {
                    return;
                }
                updateCallMarkCache(number, type);
            }
        }
        showLog("prizeProcessCloudFetchResult TMSDK CloudFetchNumberInfo ALL ::::: strBuilder.toString() : " + strBuilder.toString());
    }

    //prize-add-by-lijimeng- TMSDK_Call_Mark-20180829-end
    private void processDBCacheFetchResult(Message msg) {
        TextView tv = (TextView) msg.obj;
        if (null == tv) {
            return;
        }
        /*if (tv.getTag(R.id.prize_tag_flag).equals(DB_CACHE_TAG_FLAG_NO_DATA)) {
            showLog(":::::DBCache not find data!!   number : " + tv.getTag(R.id.prize_tag_number));
            getTMSDKFetchNumberInfo(tv, tv.getTag(R.id.prize_tag_number).toString());*/
        String num = getTagNumber(tv);
        if (getTagFlag(tv).equals(DB_CACHE_TAG_FLAG_NO_DATA)) {
            showLog(":::::DBCache not find data!!   number : " + num);
            getTMSDKFetchNumberInfo(tv, num);
        } else {
            /*String tag = tv.getTag(R.id.prize_tag_data).toString();*/
            String tag = getTagData(tv).toString();
            if (null == tag) {
                /*getTMSDKFetchNumberInfo(tv, tv.getTag(R.id.prize_tag_number).toString());*/
                getTMSDKFetchNumberInfo(tv, num);
            }
            if (tag.equals(DEFAULT_MARK_TYPE)) {
                tv.setVisibility(View.GONE);
                tv.setText("");
            } else {
                /*tv.setVisibility(View.VISIBLE);
                tv.setText(tag);*/
                /*setTextTargetTextView(true, tag, tv.getTag(R.id.prize_tag_number).toString());*/
                setTextTargetTextView(true, tag, num);
            }
        }
    }

    private void processCloudFetchResult(Message msg) {
        showLog("TMSDK CloudFetchNumberInfo ::::: processCloudFetchResult");
        TextView tv = (TextView) msg.obj;
        if (null == tv) {
            return;
        }
        /*if (!(tv.getTag(R.id.prize_tag_data) instanceof List)) {*/
        if (!(getTagData(tv) instanceof List)) {
            return;
        }
        /*List<NumQueryRet> retList = (List<NumQueryRet>) tv.getTag(R.id.prize_tag_data);*/
        List<NumQueryRet> retList = (List<NumQueryRet>) getTagData(tv);
        StringBuilder strBuilder = new StringBuilder(mContext.getResources().getString(R.string.prize_call_mark_cloud_fetch) + "(" + msg.arg1 + ")\n");
        if (retList != null) {
            for (NumQueryRet item : retList) {
                strBuilder.append(disCloudResult(item) + "\n");
            }
            if (retList.size() > 0) {
                showLog("TMSDK CloudFetchNumberInfo ::::: processCloudFetchResult  mark : " + mNumMarkerManager.getTagName(retList.get(0).tagType));
                //setTextTargetTextView(true, mNumMarkerManager.getTagName(retList.get(0).tagType), retList.get(0).number, tv);
                setTextTargetTextView(true, mNumMarkerManager.getTagName(retList.get(0).tagType), retList.get(0).number);
            }
        }
        showLog("TMSDK CloudFetchNumberInfo ALL ::::: strBuilder.toString() : " + strBuilder.toString());
    }

    private String disCloudResult(NumQueryRet nResult) {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("property:");
        strBuilder.append(nResult.property);
        if (nResult.property == NumQueryRet.PROP_Tag) {
            strBuilder.append(mContext.getResources().getString(R.string.prize_call_mark_prop_tag) + "\n");
        } else if (nResult.property == NumQueryRet.PROP_Yellow) {
            strBuilder.append(mContext.getResources().getString(R.string.prize_call_mark_prop_yellow) + "\n");
        } else if (nResult.property == NumQueryRet.PROP_Tag_Yellow) {
            strBuilder.append(mContext.getResources().getString(R.string.prize_call_mark_prop_tag_yellow) + "\n");
        } else {
            strBuilder.append("\n");
        }
        strBuilder.append(mContext.getResources().getString(R.string.prize_call_mark_number) + "[" + nResult.number + "]\n");
        strBuilder.append(mContext.getResources().getString(R.string.prize_call_mark_name) + "[" + nResult.name + "]\n");
        strBuilder.append(mContext.getResources().getString(R.string.prize_call_mark_tagtype) + "[" + nResult.tagType + "=" + mNumMarkerManager.getTagName(nResult.tagType) + "]\n");//tagtype mNumMarkerManager.getTagName()
        strBuilder.append(mContext.getResources().getString(R.string.prize_call_mark_tagcount) + "[" + nResult.tagCount + "]\n");
        strBuilder.append(mContext.getResources().getString(R.string.prize_call_mark_warning) + "[" + nResult.warning + "]\n");
        strBuilder.append("usedFor:");
        strBuilder.append(nResult.usedFor);
        if (nResult.usedFor == NumQueryRet.USED_FOR_Common) {
            strBuilder.append(mContext.getResources().getString(R.string.prize_call_mark_used_for_common) + "\n");
        } else if (nResult.usedFor == NumQueryRet.USED_FOR_Calling) {
            strBuilder.append(mContext.getResources().getString(R.string.prize_call_mark_used_for_calling) + "\n");
        } else if (nResult.usedFor == NumQueryRet.USED_FOR_Called) {
            strBuilder.append(mContext.getResources().getString(R.string.prize_call_mark_used_for_called) + "\n");
        } else {
            strBuilder.append("\n");
        }
        strBuilder.append(mContext.getResources().getString(R.string.prize_call_mark_location) + "[" + nResult.location + "]\n");
        strBuilder.append(mContext.getResources().getString(R.string.prize_call_mark_eoperator) + "[" + nResult.eOperator + "]\n");

        return strBuilder.toString();
    }

    private void setTextTargetTextView(boolean isVisible, String text, String number, TextView view) {
        if (null == view || null == text) {
            return;
        }
        if (null != text && text.equals("")) {
            return;
        }
        /*if (number.equals(view.getTag(R.id.prize_tag_number))) {*/
        if (number.equals(getTagNumber(view))) {
            view.setText(text);
            view.setVisibility(isVisible ? View.VISIBLE : View.GONE);
            if (null != mTagTextView) mTagTextView.setVisibility(isVisible ? View.VISIBLE : View.GONE);
            updateCallMarkCache(number, text);
        }
    }

    private void setTextTargetTextView(boolean isVisible, String text, String number) {
        showLog("TMSDK CloudFetchNumberInfo DISPLAY ::::: mTVMaps.size() : " + mTVMaps.size());
        if (null == text) {
            return;
        }
        if (null != text && text.equals("")) {
            return;
        }
        /*if (null != mTVMaps && mTVMaps.containsKey(number) && null != mTVMaps.get(number) && (mTVMaps.get(number).getTag(R.id.prize_tag_number)).equals(number)) {*/
        if (null != mTVMaps && mTVMaps.containsKey(number) && null != mTVMaps.get(number) && (getTagNumber(mTVMaps.get(number))).equals(number)) {
            showLog("TMSDK CloudFetchNumberInfo DISPLAY ::::: number : " + number);
            mTVMaps.get(number).setText(text);
            mTVMaps.get(number).setVisibility(isVisible ? View.VISIBLE : View.GONE);
            if (null != mTagTextView) mTagTextView.setVisibility(isVisible ? View.VISIBLE : View.GONE);
            updateCallMarkCache(number, text);
        }
        //updateCallMarkCache(number, DEFAULT_MARK_TYPE);
    }

    private void updateCallMarkCache(String phoneNumber, String type) {
        if (null != mICallMarkCacheDao) {
            mICallMarkCacheDao.updateCallMarkCache(phoneNumber, type);
        }
    }

    private Cursor getCallMarkCache() {
        if (null != mICallMarkCacheDao) {
            return mICallMarkCacheDao.getCallMarkCache();
        }
        return null;
    }

    /**
     * Number
     * @param view
     * @param number
     */
    private void setTagNumber(TextView view, String number) {
        if (null == view || null == number) {
            showLog("TMSDK setTagNumber ::::: fail!");
            return;
        }
        view.setTag(R.id.prize_tag_number, number);
    }

    private String getTagNumber(TextView view) {
        if (null == view) {
            showLog("TMSDK getTagNumber ::::: view == null!");
            return "";
        }
        return view.getTag(R.id.prize_tag_number).toString();
    }

    /**
     * Data
     * @param view
     * @param data
     */
    private void setTagData(TextView view, Object data) {
        if (null == view || null == data) {
            showLog("TMSDK setTagData ::::: fail!");
            return;
        }
        view.setTag(R.id.prize_tag_data, data);
    }

    private Object getTagData(TextView view) {
        if (null == view) {
            showLog("TMSDK getTagData ::::: view == null!");
            return "";
        }
        return view.getTag(R.id.prize_tag_data);
    }

    /**
     * Flag
     * @param view
     * @param flag
     */
    private void setTagFlag(TextView view, String flag) {
        if (null == view || null == flag) {
            showLog("TMSDK setTagFlag ::::: fail!");
            return;
        }
        view.setTag(R.id.prize_tag_flag, flag);
    }

    private String getTagFlag(TextView view) {
        if (null == view) {
            showLog("TMSDK getTagFlag ::::: view == null!");
            return "";
        }
        return view.getTag(R.id.prize_tag_flag).toString();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // YellowPage
    ////////////////////////////////////////////////////////////////////////////////////////////////
    public void setYellowPage(Context context, TextView view, String number) {
        this.mContext = context;
        getTMSDKYellowPageInfo(view, number);
    }

    public String getYellowPage(Context context, String number) {
        NumQueryRet item = getLocalYellowPageInfo(number);
        String text = "";
        if (null != item) {
            text = item.name;
        }
        return text;
    }

    private void getTMSDKYellowPageInfo(TextView view, String number) {
        NumQueryRet item = getLocalYellowPageInfo(number);
        if (null != item && null != view) {
            showLog("TMSDK LocalYellowPageInfo ::::: item.toString() : " + item.toString());
            String text = item.name;
            if (null == text || (null != text && text.equals(""))) {
                return;
            }
            showLog("TMSDK LocalYellowPageInfo ::::: text : " + text);
            view.setText(text);
            view.setVisibility(View.VISIBLE);
        }
    }

    private NumQueryRet getLocalYellowPageInfo(String number) {
        initNumMarkerManager();
        return mNumMarkerManager.localYellowPageInfo(number);
    }

}
