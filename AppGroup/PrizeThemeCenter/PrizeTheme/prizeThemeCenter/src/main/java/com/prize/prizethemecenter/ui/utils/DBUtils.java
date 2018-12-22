package com.prize.prizethemecenter.ui.utils;

import android.text.TextUtils;

import com.prize.prizethemecenter.MainApplication;
import com.prize.prizethemecenter.bean.DownloadInfo;
import com.prize.prizethemecenter.bean.LocalFontBean;
import com.prize.prizethemecenter.bean.SingleThemeItemBean;
import com.prize.prizethemecenter.bean.table.DownloadedTable;
import com.prize.prizethemecenter.bean.table.LocalFontTable;
import com.prize.prizethemecenter.bean.table.ThemeDetailTable;
import com.prize.prizethemecenter.manage.Download;
import com.prize.prizethemecenter.manage.DownloadState;
import com.prize.prizethemecenter.manage.DownloadTask;
import com.prize.prizethemecenter.manage.DownloadTaskMgr;

import org.xutils.ex.DbException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Administrator on 2016/11/9.
 */
public class DBUtils {


    /**
     * 重新下载 更新数据库
     *
     * @param info
     */
    public synchronized static void saveOrUpdate(DownloadInfo info) {
        if (info != null) {
            ThemeDetailTable themeDetailTable = null;
            try {
                themeDetailTable = MainApplication.getDbManager().selector(ThemeDetailTable.class).where("themeId", "==", info.themeID).findFirst();
                if (themeDetailTable != null) {
                    themeDetailTable.setStatus(info.currentState);
                    themeDetailTable.setDownload_progress(info.curentProgress);
                    if (info.currentState == DownloadState.STATE_DOWNLOAD_ERROR) {
                        MainApplication.getDbManager().delete(themeDetailTable);
                    } else {
                        MainApplication.getDbManager().update(themeDetailTable, "download_progress", "status");
                    }
                }
            } catch (DbException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 保存新数据库
     *
     * @param infos
     */
    public synchronized static void saveOrUpdateAll(List<DownloadInfo> infos) {
        if (infos == null || infos.size() == 0) return;
        for (DownloadInfo info : infos) {
            saveOrUpdate(info);
        }
    }

    /**
     * 查询已下载主题、壁纸、字体
     * 1,2,3
     *
     * @return
     * @throws DbException
     */
    public static List<DownloadInfo> findAllDownloadedTask(int typeId) throws DbException {
        List<ThemeDetailTable> type = MainApplication.getDbManager().selector(ThemeDetailTable.class).where("type", "==", typeId).and("status", ">=", 6).findAll();
        List<DownloadInfo> allDownloadTaskList = new ArrayList<>();
        if (type != null) {
            for (ThemeDetailTable b : type) {
                DownloadInfo info = toDownloadInfo(b);
                if (info != null)
                    allDownloadTaskList.add(info);
            }
            return allDownloadTaskList;
        }
        return null;
    }

    public static List<DownloadInfo> findAllDownloadTypeTask(int type) throws DbException {
        List<ThemeDetailTable> typelist = MainApplication.getDbManager().selector(ThemeDetailTable.class).where("type", "==", type).findAll();
        List<DownloadInfo> allDownloadTaskList = new ArrayList<>();
        if (typelist != null) {
            for (ThemeDetailTable b : typelist) {
                DownloadInfo info = toDownloadInfo(b);
                if (info != null)
                    allDownloadTaskList.add(info);
            }
            return allDownloadTaskList;
        }
        return null;
    }

    /*所有的文件下载*/
    public static List<DownloadInfo> findAllDownloadTask() {
        List<ThemeDetailTable> downloadInfoList = null;
        try {
            downloadInfoList = MainApplication.getDbManager().findAll(ThemeDetailTable.class);
            List<DownloadInfo> allDownloadTaskList = new ArrayList<>();
            if (downloadInfoList != null) {
                for (ThemeDetailTable b : downloadInfoList) {
                    DownloadInfo info = toDownloadInfo(b);
                    if (info != null)
                        allDownloadTaskList.add(info);
                }
                return allDownloadTaskList;
            }
        } catch (DbException e) {
            e.printStackTrace();
        }
        return null;
    }
 public static HashMap<String, DownloadTask > findDownloadTasks() throws DbException {
          HashMap<String,DownloadTask> tasks = new HashMap<>();
        List<ThemeDetailTable> downloadInfoList = MainApplication.getDbManager().findAll(ThemeDetailTable.class);
        if (downloadInfoList != null) {
            for (ThemeDetailTable b : downloadInfoList) {
                DownloadTask task = toDownloadTask(b);
                if (task != null) {
                    tasks.put(b.themeID,task);
                }
            }
            return tasks;
        }
        return null;
    }

    public static DownloadInfo findDownloadById(String themeID) {
        if (themeID != null) {
            try {
                ThemeDetailTable table = MainApplication.getDbManager().selector(ThemeDetailTable.class).where("themeId", "==", themeID).findFirst();
                DownloadInfo downloadInfo = toDownloadInfo(table);
                if (downloadInfo != null) return downloadInfo;
            } catch (DbException e) {
                e.printStackTrace();
            }

        }
        return null;
    }

    public static void deleteDownloadById(String themeID) {
        if (themeID != null) {
            try {
                DownloadTaskMgr.getInstance().clearTask(themeID);
                ThemeDetailTable table = MainApplication.getDbManager().selector(ThemeDetailTable.class).where("themeId", "==", themeID).findFirst();
                if (table != null) MainApplication.getDbManager().delete(table);
            } catch (DbException e) {
                e.printStackTrace();
            }
        }
    }

    public static void deleteDownloadTableById(String themeID) {
        if (themeID != null) {
            try {
                DownloadTaskMgr.getInstance().clearTask(themeID);
                DownloadedTable table = MainApplication.getDbManager().selector(DownloadedTable.class).where("themeId", "==", themeID).findFirst();
                if (table != null) MainApplication.getDbManager().delete(table);
            } catch (DbException e) {
                e.printStackTrace();
            }
        }
    }

    public static List<DownloadInfo> findFinishDownloadTask() {
        try {
            List<ThemeDetailTable> downLoadingTaskTable = MainApplication.getDbManager().selector(ThemeDetailTable.class).where("status", ">=", "6").findAll();
            List<DownloadInfo> downLoadingTaskList = new ArrayList<>();
            if (downLoadingTaskTable != null) {
                for (ThemeDetailTable b : downLoadingTaskTable) {
                    if (b != null) {
                        DownloadInfo info = toDownloadInfo(b);
                        if (info != null) downLoadingTaskList.add(info);
                    }
                }
                return downLoadingTaskList;
            }
        } catch (DbException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 查询正在下载的任务
     *
     * @return
     */
    public static List<DownloadInfo> findDownloadingTask() {
        try {
            List<ThemeDetailTable> downLoadingTaskTable = MainApplication.getDbManager().selector(ThemeDetailTable.class).where("status", "<", "6").findAll();
            List<DownloadInfo> downLoadingTaskList = new ArrayList<>();
            if (downLoadingTaskTable != null) {
                for (ThemeDetailTable b : downLoadingTaskTable) {
                    if (b != null) {
                        DownloadInfo info = toDownloadInfo(b);
                        if (info != null) downLoadingTaskList.add(info);
                    }
                }
                return downLoadingTaskList;
            }
        } catch (DbException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static DownloadInfo toDownloadInfo(ThemeDetailTable b) {
        DownloadInfo info = new DownloadInfo();
        if (b != null) {
            info.downloadUrl = b.getDownload_url();
            info.themeID = b.getThemeID();
            info.currentState = b.getStatus();
            info.title = b.getTitle();
            info.curentProgress = b.getDownload_progress();
            info.totaleSize = b.getTotal_size();
            info.type = b.type;
            info.thumbnail = b.thumbnail;
            info.isSelected = b.isSelect;
            info.wallType = b.wallType;
            return info;
        }
        return null;
    }


    public static DownloadTask toDownloadTask(ThemeDetailTable b) {

        SingleThemeItemBean.ItemsBean info  = new SingleThemeItemBean.ItemsBean();
        if (b != null) {
            info.download_url = b.getDownload_url();
            info.id = b.getThemeID().substring(0,b.getThemeID().length()-1);
            info.name = b.getTitle();
            info.md5 = b.md5;

            info.is_pay = String.valueOf(b.isPay);
            info.size=StringUtils.formatFileSize(b.total_size);


            info.setThumbnail(b.thumbnail);
            info.wallpaper_type = b.wallType;

            DownloadTask task = new DownloadTask(info,b.type);
            task.gameDownloadState = b.status;
            if(DownloadState.STATE_DOWNLOAD_WAIT ==task.gameDownloadState || DownloadState.STATE_DOWNLOAD_START_LOADING == task.gameDownloadState ){
                task.gameDownloadState = DownloadState.STATE_DOWNLOAD_PAUSE;
            }
            task.progress = (int)b.download_progress;

            return task;
        }

        return null;
    }

    /**
     * 根据主题，壁纸，字体ID 判断是否已下载
     *
     * @param itemId
     * @return
     */
    public static boolean isDownload(String itemId) {
        int state = 0;
        DownloadInfo info = DBUtils.findDownloadById(itemId);
        if (info != null) state = info.currentState;
        if (state == 6 || state == 7) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 判断本地下载的主题是否已经付过费
     *
     * @param itemId
     * @return
     */
    public static boolean isDownloadAndPay(String itemId) {
        try {
            ThemeDetailTable table = MainApplication.getDbManager().selector(ThemeDetailTable.class).where("themeId", "==", itemId).findFirst();
            if (table.isPay) {
                return true;
            } else {
                return false;
            }
        } catch (DbException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 下载完收费主题更新数据库
     */
    public synchronized static void updatePriceToThemeTable(SingleThemeItemBean.ItemsBean bean, boolean isPay) {
        try {
            ThemeDetailTable table = MainApplication.getDbManager().selector(ThemeDetailTable.class).where("themeId", "==", bean.getId()+1).findFirst();
            if (table != null) {
                table.setIsPay(isPay);
                MainApplication.getDbManager().update(table, "isPay");
            }
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    /**
     * 更新已下载数据库
     *
     * @param
     */
    public synchronized static void saveOrUpdateDownload(String typeID, int type) {
        if (typeID == null) return;
        try {
            List<DownloadInfo> list = null;
            list = findAllDownloadTypeTask(type);
            for (int i = 0; i < list.size(); i++) {
                int currentState = list.get(i).getCurrentState();
                if (currentState == 7) {
                    list.get(i).setCurrentState(6);
                }
                if (list.get(i).themeID.equals(typeID+type)) {
                    list.get(i).setCurrentState(7);
                }
            }
            saveOrUpdateAll(list);
        } catch (org.xutils.ex.DbException e) {
            e.printStackTrace();
        }
    }

    /**
     * 判断是否正在使用
     */
    public static boolean isTypeUsed(String themeID, int type) {
        if (themeID != null) {
            try {
                ThemeDetailTable table = MainApplication.getDbManager().selector(ThemeDetailTable.class).
                        where("themeId", "==", themeID).and("type", "=", type).findFirst();
                return table != null && table.getStatus() == 7 ? true : false;
            } catch (DbException e) {
                e.printStackTrace();
            }
        }
        return false;
    }


//
//    /**
//     * 更换正在使用的壁纸，主题，字体
//     * @param type  1.主题 2.壁纸 3.字体
//     * @param typeID  主题，壁纸，字体的ID
//     */
//    public static void ChangeUsedType(int type,String typeID){
//        try {
//            List<ThemeDetailTable> list  = MainApplication.getDbManager().selector(
//                    ThemeDetailTable.class).where("type", "==", type).findAll();
//            for(int i=0;i<list.size();i++){
//                boolean isSelected = list.get(i).isSelect;
//                if(isSelected){
//                    list.get(i).isSelect = false;
//                }
//                if(list.get(i).themeID.equals(typeID)){
//                    list.get(i).isSelect = true;
//                }
//            }
//            MainApplication.getDbManager().update(list);
//        } catch (DbException e) {
//            e.printStackTrace();
//        }
//    }

    /**
     * 取消正在使用的壁纸，主题，字体
     *
     * @param type     1.主题 2.壁纸 3.字体
     * @param //typeID 主题，壁纸，字体的ID
     */
    public static void CancellUsedType(int type) {
        try {
            List<ThemeDetailTable> list = MainApplication.getDbManager().selector(
                    ThemeDetailTable.class).where("type", "==", type).findAll();
            for (int i = 0; i < list.size(); i++) {
                boolean isSelected = list.get(i).isSelect;
                if (isSelected) {
                    list.get(i).isSelect = false;
                }

            }
            MainApplication.getDbManager().update(list);
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据ID查找数据库里面的名字
     *
     * @return
     */
    public static String findThemeNameByID(String itemId) {
        try {
            ThemeDetailTable table = MainApplication.getDbManager().selector(ThemeDetailTable.class).where("themeId", "==", itemId).and("type", "==", 1).findFirst();
            if (table != null) {
                return table.getTitle();
            }
        } catch (DbException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 根据ID查找数据库文件的大小
     *
     * @return
     */
    public static long findFifleSizeByID(String ID) {
        try {
            ThemeDetailTable table = MainApplication.getDbManager().selector(ThemeDetailTable.class).where("themeId", "==", ID).findFirst();
            if (table != null) {
                return table.getTotal_size();
            }
        } catch (DbException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 更新下载的地址
     */
    public static void updateDownUrl(String url, String pkg) {
        ThemeDetailTable table = null;
        try {
            table = MainApplication.getDbManager().selector(ThemeDetailTable.class).where("themeId", "==", pkg).findFirst();
            if (table != null) {
                table.setDownload_url(url);
                MainApplication.getDbManager().update(table, "download_url");
            }
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    /***
     * @param pkg
     * @return String
     * @see
     */
    public static final String getAppDownUrl(String pkg) {
        ThemeDetailTable table = null;
        try {
            table = MainApplication.getDbManager().selector(ThemeDetailTable.class).where("themeId", "==", pkg).findFirst();
            if (table != null)
                return table.getDownload_url();
        } catch (DbException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 修改下载状态，只需记录
     * STATE_DOWNLOAD_SUCESS，STATE_DOWNLOAD_ERROR，STATE_DOWNLOAD_PAUSE
     * ，恢复时，用于区别下载状态
     *
     * @param gameCode
     * @param state
     * @return
     */
    public static void updateState(String pkg, int state) {
        if ((DownloadState.STATE_DOWNLOAD_SUCESS == state)
                || (DownloadState.STATE_DOWNLOAD_ERROR == state)
                || (DownloadState.STATE_DOWNLOAD_PAUSE == state)) {
            ThemeDetailTable table = null;
            try {
                table = MainApplication.getDbManager().selector(ThemeDetailTable.class).where("themeId", "==", pkg).findFirst();
                if (table != null) {
                    table.setStatus(state);
                    MainApplication.getDbManager().update(table, "status");
                }
            } catch (DbException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 重新下载 更新数据库
     *
     * @param info
     */
    public synchronized static void saveOrUpdate(DownloadTask info, int type) {
        if (info == null || info.loadGame == null) return;
        SingleThemeItemBean.ItemsBean bean = info.loadGame;
        ThemeDetailTable table = DBUtils.toThemeTable(info, type);
        ThemeDetailTable isExit = null;
        try {
            isExit = MainApplication.getDbManager().selector(ThemeDetailTable.class).where("themeId", "==", bean.getId()+type)
                    .findFirst();
            if (isExit == null) {
                MainApplication.getDbManager().save(table);
            } else {
                DBUtils.updateThemeTable(info);
            }
        } catch (org.xutils.ex.DbException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param info
     * @param type
     * @return
     */
    public static ThemeDetailTable toThemeTable(DownloadTask info, int type) {
        if (info != null && info.loadGame != null) {
            SingleThemeItemBean.ItemsBean bean = info.loadGame;
            ThemeDetailTable table = new ThemeDetailTable();
            table.download_progress = info.progress;
            table.download_url = bean.getDownload_url();
            table.isPay = true;
            if(bean.is_pay !=null && bean.is_buy !=null && (bean.is_pay.equals("1") && bean.is_buy.equals("0"))){
                table.isPay = false;
            }
            table.type = type;
            table.themeID = bean.getId()+type;
            table.title = bean.getName();
            table.md5 = bean.getMd5();
            table.wallType = bean.getWallpaper_type();
            table.thumbnail = bean.getThumbnail();
            table.isSelect = false;
            table.status = info.gameDownloadState;
            return table;
        }
        return null;
    }

    public synchronized static void updateThemeTable(DownloadTask info) {
        if (info != null && info.loadGame != null) {
            SingleThemeItemBean.ItemsBean bean = info.loadGame;
            ThemeDetailTable themeDetailTable = null;
            try {
                themeDetailTable = MainApplication.getDbManager().selector(ThemeDetailTable.class).where("themeId", "==", bean.getId()+bean.getType()).findFirst();
                if (themeDetailTable != null) {
                    themeDetailTable.setStatus(info.gameDownloadState);
                    themeDetailTable.setDownload_progress(info.gameDownloadPostion);
                    if(bean.getMd5() != null) themeDetailTable.setMd5(bean.getMd5());
                    if (info.gameDownloadState == DownloadState.STATE_DOWNLOAD_ERROR) {
                        MainApplication.getDbManager().delete(themeDetailTable);
                    } else {
                        MainApplication.getDbManager().update(themeDetailTable, "download_progress", "status");
                    }
                }
            } catch (DbException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 更新下载进度
     *
     * @param theme_id
     * @param pos
     * @return
     */
    public synchronized static void updateDownloadSize(String theme_id, long totalSize, long pos) {
        ThemeDetailTable themeDetailTable = null;
        try {
            themeDetailTable = MainApplication.getDbManager().selector(ThemeDetailTable.class).where("themeId", "==", theme_id).findFirst();
            if (themeDetailTable != null) {
                themeDetailTable.setTotal_size(totalSize);
                themeDetailTable.setDownload_progress(pos * 100 / totalSize);
                MainApplication.getDbManager().update(themeDetailTable, "total_size", "download_progress");
            }
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    public static List<LocalFontBean> findAllLocalFontTask() throws DbException {
        List<LocalFontTable> typelist = MainApplication.getDbManager().selector(LocalFontTable.class).findAll();
        List<LocalFontBean> allLocalFontList = new ArrayList<>();
        if (typelist != null) {
            for (LocalFontTable table : typelist) {
                LocalFontBean info = toLocalFontTable(table);
                if (info != null)
                    allLocalFontList.add(info);
            }
            return allLocalFontList;
        }
        return null;
    }

    /**
     * 转成本地字体表
     *
     * @return
     */
    public static LocalFontBean toLocalFontTable(LocalFontTable pFontTable) {
        LocalFontBean vLocalFont = new LocalFontBean();
        vLocalFont.fontId = pFontTable.getLocalFontId();
        vLocalFont.fontPath = pFontTable.getPath();
        vLocalFont.name = pFontTable.getTitle();
        vLocalFont.isSelected = pFontTable.isSelected();
        vLocalFont.iconPath = pFontTable.getPreview_path();
        return vLocalFont;
    }

    public static void saveLocalTable(String localBeanId) throws DbException {
        List<LocalFontTable> typelist = MainApplication.getDbManager().selector(LocalFontTable.class).findAll();
        if (typelist != null) {
            for (LocalFontTable table : typelist) {
                if (localBeanId.equals(table.getLocalFontId().trim())) {
                    table.setSelected(true);
                }else {
                    table.setSelected(false);
                }
                MainApplication.getDbManager().delete(LocalFontTable.class);
                MainApplication.getDbManager().save(typelist);
            }
        }
    }

    public static void updateLocalFontTable() throws DbException {
        LocalFontTable table = MainApplication.getDbManager().selector(LocalFontTable.class).where("isSelected", "==", true).findFirst();
        if (table != null) {
            if (table.isSelected()) {
                table.setSelected(false);
            }
            MainApplication.getDbManager().update(table,"isSelected");
        }
    }

    /**
     * 取消已应用状态
     *
     * @param type
     */
    public static void cancelLoadedState(int type) {
        try {
            List<ThemeDetailTable> downLoadingTaskTable = MainApplication.getDbManager().selector(ThemeDetailTable.class).where("type", "==", type).and("status", "==", 7).findAll();
            if (downLoadingTaskTable != null) {
                for (ThemeDetailTable b : downLoadingTaskTable) {
                    if (b != null) {
                        if (b.getStatus() == 7)
                            b.setStatus(6);
                    }
                    MainApplication.getDbManager().update(b, "status");
                }
            }

        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    /**
     * 更新已应用状态
     *
     * @param type
     */
    public static void updateLoadedState(int type,String themeId) {
        try {
            List<ThemeDetailTable> downLoadingTaskTable = MainApplication.getDbManager().selector(ThemeDetailTable.class).where("type", "==", type).and("status", ">=", 6).findAll();
            if (downLoadingTaskTable != null) {
                for (ThemeDetailTable b : downLoadingTaskTable) {
                    if (b != null) {
                        if (b.themeID.equals(themeId)){
                            b.setStatus(7);
                        }else {
                            b.setStatus(6);
                        }
                    }
                    MainApplication.getDbManager().update(b, "status");
                }
            }
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    public static void updateLoadFlag(DownloadTask info) {
        if (info != null && info.loadGame != null) {
            SingleThemeItemBean.ItemsBean bean = info.loadGame;
            ThemeDetailTable themeDetailTable = null;
            try {
                themeDetailTable = MainApplication.getDbManager().selector(ThemeDetailTable.class).where("themeId", "==", bean.getId()+info.type).findFirst();
                if (themeDetailTable != null) {
                    themeDetailTable.setLoadFlag(info.loadFlag);
                    MainApplication.getDbManager().update(themeDetailTable, "loadFlag");
                }
            } catch (DbException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 下载成功增加到下载记录的表中
     */
    public synchronized static void addDownloadComlete(DownloadTask task) {
        if (task == null || task.loadGame == null) return;
        SingleThemeItemBean.ItemsBean bean = task.loadGame;
        DownloadedTable table = toDownloadTable(task);
        if (table != null) {
            try {
                MainApplication.getDbManager().saveOrUpdate(table);
            } catch (DbException e) {
                e.printStackTrace();
            }
        }
    }


    public static DownloadedTable toDownloadTable(DownloadTask info) {
        if (info != null && info.loadGame != null) {
            SingleThemeItemBean.ItemsBean bean = info.loadGame;
            DownloadedTable table = new DownloadedTable();
            table.type = info.type;
            table.themeID = bean.getId()+info.type;
            table.wallType = bean.getWallpaper_type();
            table.thumbnail = bean.getThumbnail();
            table.title = bean.getName();
            table.total_size = info.gameDownloadPostion;
            return table;
        }
        return null;
    }


    public static List<SingleThemeItemBean.ItemsBean> findDownloadCompleteTask() {
        try {
            List<SingleThemeItemBean.ItemsBean> lists = new ArrayList<>();
            List<DownloadedTable> tables = MainApplication.getDbManager().findAll(DownloadedTable.class);
            for (DownloadedTable table : tables) {
                SingleThemeItemBean.ItemsBean itemsBean = new SingleThemeItemBean.ItemsBean();
                DownloadInfo info = toDownloadInfo(table);
                if (info != null && !TextUtils.isEmpty(info.getTitle())) {
                    SingleThemeItemBean.ItemsBean bean = new SingleThemeItemBean.ItemsBean();
                    bean.setId(info.getThemeID().substring(0,info.getThemeID().length()-1));
                    bean.setDownload_url(info.getDownloadUrl());
                    bean.setThumbnail(info.getThumbnail());
                    bean.setName(info.getTitle());
                    bean.setSize(StringUtils.formatFileSize(info.getTotaleSize()));
                    bean.setType(info.type);
                    lists.add(bean);
                }
            }
            Collections.reverse(lists);
//            if(lists!=null && lists.size()>20){
//                lists = lists.subList(0, 20);
//            }
            return lists;
        } catch (DbException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static DownloadInfo toDownloadInfo(DownloadedTable b) {
        DownloadInfo info = new DownloadInfo();
        if (b != null) {
            info.themeID = b.getThemeID();
            info.title = b.getTitle();
            info.totaleSize = b.getTotal_size();
            info.type = b.type;
            info.thumbnail = b.thumbnail;
            info.wallType = b.wallType;
            return info;
        }
        return null;
    }

    public static void deleteDownloadById(String themeID, int type) {
        if (themeID != null) {
            try {
                DownloadedTable table = MainApplication.getDbManager().selector(DownloadedTable.class).where("themeId", "==", themeID+type).findFirst();
                if (table != null) MainApplication.getDbManager().delete(table);
            } catch (DbException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     *
     */

    public static void deleteDownloadTable(){
        try {
            MainApplication.getDbManager().delete(DownloadedTable.class);
        } catch (DbException e) {
            e.printStackTrace();
        }
    }
}
