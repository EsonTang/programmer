package com.prize.statistics.db.utils;

import android.content.Context;
import android.database.sqlite.SQLiteFullException;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.prize.app.constants.Constants;
import com.prize.app.util.GsonParseUtils;
import com.prize.app.util.JLog;
import com.prize.statistics.listener.ISaveLister;
import com.prize.statistics.model.DataBlock;
import com.prize.statistics.model.Event;
import com.prize.statistics.model.ExposureDataBlock;
import com.prize.statistics.model.ExposureEvent;
import com.prize.statistics.model.TcNote;

import org.xutils.DbManager;
import org.xutils.ex.DbException;
import org.xutils.x;

import java.util.ArrayList;
import java.util.List;

import static com.prize.statistics.model.StatisConstant.PRIZE_STAT_DB_NAME;
import static com.prize.statistics.model.StatisConstant.STATICS_DBVERSIONCODE;

public class StaticsAgent {
    private static final String TAG = "StaticsAgent";
    private static TcNote note;
    private static DbManager sDbManager;
    private static Context sContext;


    /**
     * storeEvent
     *
     * @param event       Event
     * @param iSaveLister ISaveLister
     */
    private static void storeEvent(Event event, ISaveLister iSaveLister) {
        if (event == null)
            throw new NullPointerException("eventString is null");
        storeData(event, iSaveLister);
    }

    /**
     * storeEvent
     *
     * @param event       ExposureEvent
     * @param iSaveLister ISaveLister
     */
    private static void storeExposureEvent(ExposureEvent event, ISaveLister iSaveLister) {
        if (event == null)
            throw new NullPointerException("eventString is null");
        storeExposureData(event, iSaveLister);
    }

    /**
     * 初始化数据库
     *
     * @param var0 Context
     */
    public static void init(Context var0) {
        sContext = var0;
        DbManager.DaoConfig sDaoConfig = new DbManager.DaoConfig().setDbName(PRIZE_STAT_DB_NAME).setDbVersion(STATICS_DBVERSIONCODE).
                setDbUpgradeListener(new DbManager.DbUpgradeListener() {
                                         @Override
                                         public void onUpgrade(DbManager db, int oldVersion, int newVersion) {
                                             try {
                                                 if (JLog.isDebug) {
                                                     JLog.i("PRIZE2016", "StaticsAgent-init-oldVersion=" + oldVersion + "---newVersion=" + newVersion);
                                                 }
                                                 if (newVersion > oldVersion) {
                                                     db.dropTable(TcNote.class);
                                                 }
                                             } catch (DbException e) {
                                                 e.printStackTrace();
                                             }
                                         }
                                     }
                );
        sDbManager = x.getDb(sDaoConfig);
    }

    public static DataBlock getDataBlock() {
        DataBlock dataBlock = new DataBlock();
        long timeStamp = System.currentTimeMillis();
        try {
            List<TcNote> list = sDbManager.selector(TcNote.class).where("timeStamp",
                    "<=", timeStamp).and("firstCloumn", "!=", "null").findAll();
            if (list == null || list.size() <= 0) {
                return null;
            }
            Event event;
            List<Event> eventList = new ArrayList<Event>();
            for (int i = 0; i < list.size(); i++) {
                if (!TextUtils.isEmpty(list.get(i).firstCloumn)) {
                    event = GsonParseUtils.parseSingleBean(list.get(i).firstCloumn, Event.class);
                    eventList.add(event);
                }
                dataBlock.events = eventList;
            }
            return dataBlock;
        } catch (DbException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取旧版本的曝光数据
     *
     * @return ExposureDataBlock
     */
    public static ExposureDataBlock getExposureDataBlock() {
        ExposureDataBlock dataBlock = new ExposureDataBlock();
        long timeStamp = System.currentTimeMillis();
        try {
            List<TcNote> list = sDbManager.selector(TcNote.class).where("timeStamp", "<=", timeStamp).and("secondCloumn", "!=", "null").findAll();
            if (list == null || list.size() <= 0) {
                return null;
            }
            ExposureEvent event;
            List<ExposureEvent> eventList = new ArrayList<ExposureEvent>();
            for (int i = 0; i < list.size(); i++) {
                if (!TextUtils.isEmpty(list.get(i).secondCloumn)) {
                    event = GsonParseUtils.parseSingleBean(list.get(i).secondCloumn, ExposureEvent.class);
                    eventList.add(event);
                }
                dataBlock.events = eventList;
            }
            return dataBlock;
        } catch (DbException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取新版本的曝光数据
     *
     * @return ExposureDataBlock
     */
    public static ExposureDataBlock getNewExposureDataBlock() {
        ExposureDataBlock dataBlock = new ExposureDataBlock();
        long timeStamp = System.currentTimeMillis();
        try {
            List<TcNote> list = sDbManager.selector(TcNote.class).where("timeStamp", "<=", timeStamp).and("fourthCloumn", "!=", "null").findAll();
            if (list == null || list.size() <= 0) {
                return null;
            }
            ExposureEvent event;
            List<ExposureEvent> eventList = new ArrayList<ExposureEvent>();
            for (int i = 0; i < list.size(); i++) {
                if (!TextUtils.isEmpty(list.get(i).fourthCloumn)) {
                    event = GsonParseUtils.parseSingleBean(list.get(i).fourthCloumn, ExposureEvent.class);
                    eventList.add(event);
                }
                dataBlock.events = eventList;
            }
            return dataBlock;
        } catch (DbException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static DataBlock getDownBlock() {
        DataBlock dataBlock = new DataBlock();
        long timeStamp = System.currentTimeMillis();
        try {
            List<TcNote> list = sDbManager.selector(TcNote.class).where("timeStamp", "<=", timeStamp).and("thirdCloumn", "!=", "null").findAll();
            if (list == null || list.size() <= 0) {
                return null;
            }
            Event event;
            List<Event> eventList = new ArrayList<Event>();
            for (int i = 0; i < list.size(); i++) {
                if (!TextUtils.isEmpty(list.get(i).thirdCloumn)) {
                    event = GsonParseUtils.parseSingleBean(list.get(i).thirdCloumn, Event.class);
                    eventList.add(event);
                }
                dataBlock.events = eventList;
            }
            return dataBlock;
        } catch (DbException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ExposureDataBlock getNewDownBlock() {
        ExposureDataBlock dataBlock = new ExposureDataBlock();
        long timeStamp = System.currentTimeMillis();
        try {
            List<TcNote> list = sDbManager.selector(TcNote.class).where("timeStamp", "<=", timeStamp).and("fifthCloumn", "!=", "null").findAll();
            if (list == null || list.size() <= 0) {
                return null;
            }
            ExposureEvent event;
            List<ExposureEvent> eventList = new ArrayList<ExposureEvent>();
            for (int i = 0; i < list.size(); i++) {
                if (!TextUtils.isEmpty(list.get(i).fifthCloumn)) {
                    event = GsonParseUtils.parseSingleBean(list.get(i).fifthCloumn, ExposureEvent.class);
                    eventList.add(event);
                }
                dataBlock.events = eventList;
            }
            return dataBlock;
        } catch (DbException e) {
            JLog.i(TAG, "getNewDownBlock-e=" + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }


//    /**
//     * @param event
//     */
//    private static void showKeysAndValues(Event event) {
//        Iterator<KeyValueBean> it= event.getParameter().iterator();
//        while (it.hasNext()) {
//            KeyValueBean entry = it.next();
//            String businessName = entry.getName();
//            String businessValue = entry.getValue();
//            if("time_stamp".equals(businessName)){
//                Log.i("PRIZE","---businessValue="+businessValue);
//                Log.i("PRIZE",DateUtil.getDateString(Long.parseLong(businessValue), "yyyy-MM-dd HH:mm:ss"));
//
//            }
//        }
//    }


    private static void storeData(Event event, ISaveLister iSaveLister) {
        note = new TcNote();
        note.timeStamp = event.action_time;
        if (Constants.EVEN_NAME_DOWNLOAD.equals(event.event_name) || Constants.EVEN_NAME_UPDATE.equals(event.event_name)
                || Constants.EVEN_NAME_BACKPARAMS.equals(event.event_name)) {
            note.thirdCloumn = new Gson().toJson(event);
        } else if (Constants.EVEN_NAME_NEWDOWNLOAD.equals(event.event_name)) {
            note.fifthCloumn = new Gson().toJson(event);
        } else {
            note.firstCloumn = new Gson().toJson(event);
        }
        try {
            if (null != sDbManager) {
                sDbManager.save(note);
                if (iSaveLister != null) {
                    iSaveLister.saveOk(note);
                }
            } else {
                init(sContext);
                sDbManager.save(note);
                if (iSaveLister != null) {
                    iSaveLister.saveOk(note);
                }
            }
        } catch (DbException e) {
            e.printStackTrace();
        } catch (SQLiteFullException e) {
            e.printStackTrace();
            Log.i(TAG, "SQLiteFullException" + e.getMessage());
        } catch (IllegalStateException e) {
            e.printStackTrace();
            Log.i(TAG, "IllegalStateException" + e.getMessage());
        }
    }

    /**
     * 存储曝光数据转化为String插入到数据库对应的Cloumn中
     *
     * @param event       ExposureEvent
     * @param iSaveLister ISaveLister
     */
    private static void storeExposureData(ExposureEvent event, ISaveLister iSaveLister) {
        if (JLog.isDebug&&event.event_name.equals("exposure")) {
            JLog.i("PRIZE2016", "StaticsAgent-storeExposureData-360数据曝光：" + event);
        }
        note = new TcNote();
        note.timeStamp = event.action_time;
        if (Constants.EVEN_NAME_NEWEXPOSURE.equals(event.event_name)) {
            note.fourthCloumn = new Gson().toJson(event);
        } else if (Constants.EVEN_NAME_NEWDOWNLOAD.equals(event.event_name)) {
            note.fifthCloumn = new Gson().toJson(event);
        } else {
            note.secondCloumn = new Gson().toJson(event);
        }
        try {
            if (null != sDbManager) {
                sDbManager.save(note);
                if (iSaveLister != null) {
                    iSaveLister.saveOk(note);
                }
            } else {
                init(sContext);
                sDbManager.save(note);
                if (iSaveLister != null) {
                    iSaveLister.saveOk(note);
                }
            }
        } catch (DbException e) {
            JLog.i("PRIZE2016", "storeExposureData-DbException=" + e.getMessage());
            e.printStackTrace();
        } catch (SQLiteFullException e) {

            e.printStackTrace();
        } catch (IllegalStateException e) {
            JLog.i("PRIZE2016", "storeExposureData-IllegalStateException=" + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 删除某个时间段的的数据（防止全部上传返回时，有数据插入）
     *
     * @param timeStamp 时间戳
     */
    public static void deletData(long timeStamp, Object o) {
        try {
            if (null != sDbManager) {
                List<TcNote> list = new ArrayList<>();
                if (o instanceof ExposureDataBlock) {
                    list = sDbManager.selector(TcNote.class).where("timeStamp", "<=", timeStamp).and("secondCloumn", "!=", "").findAll();
                } else if (o instanceof DataBlock) {
                    list = sDbManager.selector(TcNote.class).where("timeStamp", "<=", timeStamp).and("firstCloumn", "!=", "").findAll();

                }
                if (list == null)
                    return;
                if (JLog.isDebug) {
                    if (o instanceof ExposureDataBlock) {
                        JLog.i("PRIZE2016", "StaticsAgent-deletData曝光数据大小-list=" + list.size());
                    } else if (o instanceof DataBlock) {
                        JLog.i("PRIZE2016", "StaticsAgent-deletData事件数据大小-list=" + list.size());
                    }
                }
                sDbManager.delete(list);
            } else {
                init(sContext);
                List<TcNote> list = new ArrayList<>();
                if (o instanceof ExposureDataBlock) {
                    list = sDbManager.selector(TcNote.class).where("timeStamp", "<=", timeStamp).and("secondCloumn", "!=", "").findAll();
                } else if (o instanceof DataBlock) {
                    list = sDbManager.selector(TcNote.class).where("timeStamp", "<=", timeStamp).and("firstCloumn", "!=", "").findAll();

                }
                if (list == null)
                    return;
                sDbManager.delete(list);
            }
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除某个时间段的的数据新版的统计（防止全部上传返回时，有数据插入）3.2add
     *
     * @param timeStamp 时间戳
     * @param type      type=1：曝光；Type=2:下载
     */
    public static void deletNewStaticsRecord(long timeStamp, int type) {
        try {
            if (null != sDbManager) {
                List<TcNote> list = new ArrayList<>();
                if (type == 1) {
                    list = sDbManager.selector(TcNote.class).where("timeStamp", "<=", timeStamp).and("fourthCloumn", "!=", "").findAll();
                } else if (type == 2) {
                    list = sDbManager.selector(TcNote.class).where("timeStamp", "<=", timeStamp).and("fifthCloumn", "!=", "").findAll();

                }
                if (list == null)
                    return;
                if (JLog.isDebug) {
                    if (type == 1) {
                        JLog.i("PRIZE2016", "StaticsAgent-deletNewStaticsRecord曝光数据大小ExposureDataBlock-list=" + list.size());
                    } else if (type == 2) {
                        JLog.i("PRIZE2016", "StaticsAgent-deletNewStaticsRecord下载事件数据大小-DataBlock-list=" + list.size());
                    }
                }
                sDbManager.delete(list);
            } else {
                init(sContext);
                List<TcNote> list = new ArrayList<>();
                if (type == 1) {
                    list = sDbManager.selector(TcNote.class).where("timeStamp", "<=", timeStamp).and("fourthCloumn", "!=", "").findAll();
                } else if (type == 2) {
                    list = sDbManager.selector(TcNote.class).where("timeStamp", "<=", timeStamp).and("fifthCloumn", "!=", "").findAll();

                }
                if (list == null)
                    return;
                sDbManager.delete(list);
            }
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除某个时间段本地已上传的下载数据（防止全部上传返回时，有数据插入）
     *
     * @param timeStamp 时间戳
     */
    public static void deletDBdownData(long timeStamp) {
        try {
            if (null != sDbManager) {
                List<TcNote> list;
                list = sDbManager.selector(TcNote.class).where("timeStamp", "<=", timeStamp).and("thirdCloumn", "!=", "").findAll();
                if (list == null)
                    return;
                sDbManager.delete(list);
            } else {
                init(sContext);
                List<TcNote> list;
                list = sDbManager.selector(TcNote.class).where("timeStamp", "<=", timeStamp).and("thirdCloumn", "!=", "").findAll();
                if (list == null)
                    return;
                sDbManager.delete(list);
            }
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除单条数据
     *
     * @param timeStamp 时间戳
     */
    public static void deletSingleData(long timeStamp) {
        try {
            if (null != sDbManager) {
                TcNote note = sDbManager.selector(TcNote.class).where("timeStamp", "=", timeStamp).findFirst();
                if (note == null)
                    return;
                sDbManager.delete(note);
            } else {
                init(sContext);
                TcNote note = sDbManager.selector(TcNote.class).where("timeStamp", "=", timeStamp).findFirst();
                if (note == null)
                    return;
                sDbManager.delete(note);
            }
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    /**
     * 找寻360数据
     *
     * @param timeStamp 时间戳
     */
    public static DataBlock find360ClickData(long timeStamp) {
        DataBlock dataBlock = new DataBlock();
        try {
            if (null != sDbManager) {
                TcNote note = sDbManager.selector(TcNote.class).where("timeStamp", "=", timeStamp).findFirst();
                if (note == null) {
                    return null;
                }
                Event event;
                List<Event> eventList = new ArrayList<Event>();
                if (!TextUtils.isEmpty(note.thirdCloumn)) {
                    event = GsonParseUtils.parseSingleBean(note.thirdCloumn, Event.class);
                    eventList.add(event);
                }
                dataBlock.events = eventList;
                return dataBlock;
            }
        } catch (DbException e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }

    /**
     * 找寻单条的下载更新数据
     *
     * @param timeStamp 时间戳
     */
    public static DataBlock findSingleDownData(long timeStamp) {
        DataBlock dataBlock = new DataBlock();
        try {
            if (null != sDbManager) {
                TcNote note = sDbManager.selector(TcNote.class).where("timeStamp", "=", timeStamp).findFirst();
                if (note == null) {
                    return null;
                }
                Event event;
                List<Event> eventList = new ArrayList<Event>();
                if (!TextUtils.isEmpty(note.thirdCloumn)) {
                    event = GsonParseUtils.parseSingleBean(note.thirdCloumn, Event.class);
                    eventList.add(event);
                }
                dataBlock.events = eventList;
                return dataBlock;
            }
        } catch (DbException e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }

    /**
     * 找寻数据
     *
     * @param timeStamp 时间戳
     */
    public static ExposureDataBlock findSingleExousureData(long timeStamp) {
        ExposureDataBlock dataBlock = new ExposureDataBlock();
        try {
            if (null != sDbManager) {
                TcNote note = sDbManager.selector(TcNote.class).where("timeStamp", "=", timeStamp).findFirst();
                if (note == null) {
                    return null;
                }
                ExposureEvent event;
                List<ExposureEvent> eventList = new ArrayList<ExposureEvent>();
                if (!TextUtils.isEmpty(note.secondCloumn)) {
                    event = GsonParseUtils.parseSingleBean(note.secondCloumn, ExposureEvent.class);
                    eventList.add(event);
                }
                dataBlock.events = eventList;
                return dataBlock;
            }
        } catch (DbException e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }

    /**
     * storeObject
     *
     * @param obj         Object
     * @param iSaveLister ISaveLister
     */

    static void storeObject(Object obj, ISaveLister iSaveLister) {
        if (obj instanceof Event) {
            storeEvent((Event) obj, iSaveLister);
        }
        if (obj instanceof ExposureEvent) {
            storeExposureEvent((ExposureEvent) obj, iSaveLister);
        }
    }

}

