package com.prize.music.admanager.statistics.db.utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import com.prize.music.admanager.presenter.JLog;
import com.prize.music.admanager.statistics.listener.ISaveLister;
import com.prize.music.admanager.statistics.model.Event;
import com.prize.music.admanager.statistics.model.KeyValueBean;
import com.prize.music.admanager.statistics.utils.SharedPreferencesHelper;

import android.content.Context;

/**
 * DataConstruct
 * Created by tamic on 2016-04-11.
 */
public class DataConstruct {

    private static Event                   event     = null;
    private static ArrayList<KeyValueBean> parameter = new ArrayList<>();
    private static String                  TAG       = "DataConstruct";

    private DataConstruct() {
    }


    /**
     * initEvent
     * @param event_name
     */
    public static void initEvent(String event_name) {
        event = new Event();
        event.event_name=event_name;
        event.action_time=System.currentTimeMillis();
    }

    /**
     * onEvent
     *
     * @param p
     */
    public static void onEvent(Properties p, Context mContext, ISaveLister iSaveLister) {
        showKeysAndValues(p,mContext);
        if (event == null) {
            throw new RuntimeException("you must call initEvent before onEvent!");
        }
        event.parameter=parameter;
        storeEvent(iSaveLister);
    }
    /**
     * storeEvent
     */
    private static void storeEvent(ISaveLister iSaveLister){
        if(event == null){
            return;
        }
        StaticsAgent.storeObject(event,iSaveLister);
    }

    /**
     * @param properties
     */
    private static ArrayList<KeyValueBean> showKeysAndValues(Properties properties,Context mContext) {
        parameter.clear();
        Iterator<Map.Entry<Object, Object>> it = properties.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Object, Object> entry = it.next();
            String businessName = (String) entry.getKey();
            String businessValue = (String) entry.getValue();
            parameter.add(new KeyValueBean(businessName, businessValue));
        }
        long time= System.currentTimeMillis()+ SharedPreferencesHelper.getTimeDifference(0);

        JLog.i("PRIZE2016", "DataConstruct-showKeysAndValues-time=" + dateFormater.format(new Date(time))+"--"+SharedPreferencesHelper.getTimeDifference(0));
        parameter.add(new KeyValueBean("time_stamp", time+""));
        return parameter;
    }

    static SimpleDateFormat dateFormater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
}
