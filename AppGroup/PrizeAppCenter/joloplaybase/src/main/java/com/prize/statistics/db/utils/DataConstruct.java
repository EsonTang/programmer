package com.prize.statistics.db.utils;

import com.prize.app.constants.Constants;
import com.prize.app.util.JLog;
import com.prize.app.util.SharedPreferencesHelper;
import com.prize.statistics.listener.ISaveLister;
import com.prize.statistics.model.Event;
import com.prize.statistics.model.ExposureBean;
import com.prize.statistics.model.ExposureEvent;
import com.prize.statistics.model.KeyValueBean;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * DataConstruct
 */
public class DataConstruct {

    //    private static String TAG = "DataConstruct";
    private static Event event = null;
    private static ExposureEvent mExposureEvent = null;
    private static ArrayList<KeyValueBean> parameter = new ArrayList<>();

    private DataConstruct() {
    }


    /**
     * initEvent
     *
     * @param event_name 事件名称
     */
    public static void initEvent(String event_name) {
        event = new Event();
        event.event_name = event_name;
        event.action_time = System.currentTimeMillis();
    }

    /**
     * initEvent
     *
     * @param event_name 事件名称
     */
    public static void initExposureEvent(String event_name) {
        mExposureEvent = new ExposureEvent();
        mExposureEvent.event_name = event_name;
        mExposureEvent.action_time = System.currentTimeMillis();
        if (event_name.equals(Constants.EVEN_NAME_NEWDOWNLOAD) || event_name.equals(Constants.EVEN_NAME_NEWEXPOSURE)) {
//            Date date = new Date(mExposureEvent.action_time);
            mExposureEvent.action_time = mExposureEvent.action_time/1000;
        }
    }

    /**
     * onEvent
     *
     * @param p           Properties
     * @param iSaveLister ISaveLister
     */
    public static void onEvent(Properties p, ISaveLister iSaveLister) {
        showKeysAndValues(p);
        if (event == null) {
            throw new RuntimeException("you must call initEvent before onEvent!");
        }
        event.parameter = parameter;
        storeEvent(iSaveLister);
    }

    /**
     * onEvent
     *
     * @param p           List<ExposureBean>
     * @param iSaveLister ISaveLister
     */
    public static void onExposure(List<ExposureBean> p, ISaveLister iSaveLister) {
        if (mExposureEvent == null) {
            throw new RuntimeException("you must call initEvent before onEvent!");
        }
        mExposureEvent.parameter = p;
        storeExposureEvent(iSaveLister);
    }

    /* onEvent
    *
    * @param p List<ExposureBean>
    * @param iSaveLister ISaveLister
    */
    public static void onNewDown(ExposureBean p, ISaveLister iSaveLister) {
        if (mExposureEvent == null) {
            throw new RuntimeException("you must call initEvent before onEvent!");
        }
        List<ExposureBean> list = new ArrayList<>();
        list.add(p);
        mExposureEvent.parameter = list;
        storeExposureEvent(iSaveLister);
    }

    /**
     * storeEvent
     */
    private static void storeEvent(ISaveLister iSaveLister) {
        if (event == null) {
            return;
        }
        StaticsAgent.storeObject(event, iSaveLister);
    }

    /**
     * storeEvent
     */
    private static void storeExposureEvent(ISaveLister iSaveLister) {
        if (mExposureEvent == null) {
            return;
        }
        StaticsAgent.storeObject(mExposureEvent, iSaveLister);
    }

    /**
     * @param properties Properties
     */
    private static ArrayList<KeyValueBean> showKeysAndValues(Properties properties) {
        parameter.clear();
        Iterator<Map.Entry<Object, Object>> it = properties.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Object, Object> entry = it.next();
            String businessName = (String) entry.getKey();
            String businessValue = (String) entry.getValue();
            parameter.add(new KeyValueBean(businessName, businessValue));
        }
        long time = System.currentTimeMillis() + SharedPreferencesHelper.getTimeDifference(0);
        if (JLog.isDebug) {
            JLog.i("PRIZE2016", "DataConstruct-showKeysAndValues-time=" + dateFormater.format(new Date(time)) + "--" + SharedPreferencesHelper.getTimeDifference(0));
        }
        parameter.add(new KeyValueBean("time_stamp", time + ""));
        return parameter;
    }

    static SimpleDateFormat dateFormater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
}
