package com.prize.music.admanager.statistics.db.utils;

import java.util.ArrayList;
import java.util.List;

import org.xutils.DbManager;
import org.xutils.x;
import org.xutils.ex.DbException;

import com.google.gson.Gson;
import com.prize.music.admanager.presenter.JLog;
import com.prize.music.admanager.statistics.listener.ISaveLister;
import com.prize.music.admanager.statistics.model.DataBlock;
import com.prize.music.admanager.statistics.model.Event;
import com.prize.music.admanager.statistics.model.TcNote;
import com.prize.music.admanager.statistics.utils.GsonParseUtils;

import android.content.Context;
import android.text.TextUtils;


/**
 * Created by Tamic on 2016-03-17.
 */
public class StaticsAgent {
    private static final String TAG = "StaticsAgent";
    private static Context             mContext;
    private static TcNote              note;
    private static DbManager           sDbManager;
    private static DbManager.DaoConfig sDaoConfig;
    private static Context             sContext;
    private static int DBVERSIONCODE = 1;

    /**
     * storeEvent
     *
     * @param event
     */
    public static void storeEvent(Event event, ISaveLister iSaveLister) {
        if (event == null)
            throw new NullPointerException("eventString is null");
        storeData(event, iSaveLister);
    }

    /**
     * 初始化数据库
     *
     * @param var0
     */
    public static void init(Context var0) {
        sContext = var0;
        sDaoConfig = new DbManager.DaoConfig().setDbName("prize_stat_db").setDbVersion(DBVERSIONCODE).
                setDbUpgradeListener(new DbManager.DbUpgradeListener() {
                                         @Override
                                         public void onUpgrade(DbManager db, int oldVersion, int newVersion) {

                                         }
                                     }
                );
        sDbManager = x.getDb(sDaoConfig);
    }

    public static DataBlock getDataBlock() {
        DataBlock dataBlock = new DataBlock();
        try {
            List<TcNote> list = sDbManager.findAll(TcNote.class);
            if (list == null || list.size() <= 0) {
                return null;
            }
            Event event = new Event();
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


    public static void storeData(Event event, ISaveLister iSaveLister) {
        JLog.i("PRIZE", "storeData-Event=" + event.event_name);
        note = new TcNote();
        note.timeStamp = event.action_time;
        note.firstCloumn = new Gson().toJson(event);
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
        }
    }

    /**
     * 删除某个时间段的的数据（防止全部上传返回时，有数据插入）
     *
     * @param timeStamp 时间戳
     */
    public static void deletData(long timeStamp) {
        try {
            if (null != sDbManager) {
                List<TcNote> list = sDbManager.selector(TcNote.class).where("timeStamp", "<=", timeStamp).findAll();
                sDbManager.delete(list);
            } else {
                init(sContext);
                List<TcNote> list = sDbManager.selector(TcNote.class).where("timeStamp", "<=", timeStamp).findAll();
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
                sDbManager.delete(note);
            } else {
                init(sContext);
                TcNote note = sDbManager.selector(TcNote.class).where("timeStamp", "=", timeStamp).findFirst();
                sDbManager.delete(note);
            }
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    /**
     * 找寻数据
     *
     * @param timeStamp 时间戳
     */
    public static DataBlock findSingleData(long timeStamp) {
        DataBlock dataBlock = new DataBlock();
        try {
            if (null != sDbManager) {
                TcNote note = sDbManager.selector(TcNote.class).where("timeStamp", "=", timeStamp).findFirst();
                if (note == null) {
                    return null;
                }
                Event event = new Event();
                List<Event> eventList = new ArrayList<Event>();
                if (!TextUtils.isEmpty(note.firstCloumn)) {
                    event = GsonParseUtils.parseSingleBean(note.firstCloumn, Event.class);
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
     * @param obj
     */

    public static void storeObject(Object obj, ISaveLister iSaveLister) {
        if (obj instanceof Event) {
            storeEvent((Event) obj, iSaveLister);
        }
    }

}

