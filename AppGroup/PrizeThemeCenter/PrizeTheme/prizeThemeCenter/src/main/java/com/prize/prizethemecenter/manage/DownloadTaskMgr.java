package com.prize.prizethemecenter.manage;

import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.prize.app.BaseApplication;
import com.prize.app.beans.ClientInfo;
import com.prize.app.util.JLog;
import com.prize.prizethemecenter.bean.SingleThemeItemBean;
import com.prize.prizethemecenter.ui.utils.DBUtils;
import com.prize.prizethemecenter.ui.utils.FileUtils;

import org.xutils.ex.DbException;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Administrator on 2016/12/22.
 */
public class DownloadTaskMgr {
    private static final String TAG = "DownloadTaskMgr";
    /** 下载的游戏 */
    private static HashMap<String, DownloadTask> loadGametasks;
    /** 下载模块的状态监听，通下载线程交互 */
    private DownloadState loadListener;
    /** 下载模块的handler，和UI交互 **/
    private Handler loadHandler;
    private HashSet<UIDownLoadListener> uiListners;
    private HashSet<UIDownLoadListener> loopListners; // 专门用户循环

    private long lastRefreshUI = 0;
    private static DownloadTaskMgr instance;
    private static boolean isScreenOn = true;

    private DownloadTaskMgr() {
        // 不允许外部实例化
        JLog.i(TAG, "DownloadTaskMgr-构造函数");
        initDownloadAppMode();
    }



    public static DownloadTaskMgr getInstance() {
        if (instance == null) {
            synchronized (DownloadTaskMgr.class) {
                if (instance == null) {
                    instance = new DownloadTaskMgr();
                }
            }
        }
        return instance;
    }

    /**
     * 初始化下载模块,UI线程调用
     */
    private void initDownloadAppMode() {
        loadHandler = new Handler(BaseApplication.curContext.getMainLooper()) {
            public void handleMessage(Message msg) {
                loadHandler.removeMessages(DownloadState.STATE_DOWNLOAD_UPDATE_PROGRESS); // 避免过度刷屏
                notifyUIDownloadState(msg.what, msg.arg1,msg.arg2);
            }
        };
        loadListener = new DownloadState() {
            @Override
            public void onDownloadState(int state, String theme_id, int errorCode) {
                DownloadTask task = null;
                synchronized (loadGametasks){
                    task = loadGametasks.get(theme_id);
                    if (null == task) {
                        return;
                    }
                    task.gameDownloadState = state;
                    if (DownloadState.STATE_DOWNLOAD_SUCESS == state){
                        task.resetDownloadRunnable();// 移除下载任务线程，否则pause全部的时候，状态会被置成暂停
                        if(checkDownloadSucess(task.loadGame, task.type)){
                            DBUtils.updateState(String.valueOf(theme_id),state);
                            JLog.i(TAG, "SUCESS-state=" + state);
                            //下载成功记录增加到表中
                            DBUtils.addDownloadComlete(task);
                        }else{
                            task.gameDownloadState = DownloadState.STATE_DOWNLOAD_ERROR;
                            startDownload(task.loadGame,task.isBackgroundTask(),task.type);
                            return;
                        }
                    }else if((DownloadState.STATE_DOWNLOAD_PAUSE == state)
                            || (DownloadState.STATE_DOWNLOAD_ERROR == state)){
                        task.resetDownloadRunnable();
                        DBUtils.updateState(theme_id+"",state);
                        if ((DownloadState.ERROR_CODE_TIME_OUT == errorCode)
                                || (DownloadState.ERROR_CODE_HTTP == errorCode)
                                || (DownloadState.ERROR_CODE_URL_ERROR == errorCode)) {
                            // 如果是超时，或者网络连接失败，重试
                            task.gameDownloadState = state;
                            startDownload(task.loadGame,task.isBackgroundTask(),task.type);
                            return;
                        }
                        if (task.isBackgroundTask()) {
                            // 后台任务被暂停，不继续后台任务下载.原因： 可能是因为正常任务把后台任务中止的。故不再启动
                        } else {
                            // 正常任务暂停后，继续后台任务的下载
//                            continueBackgroundDownload(task.type);
                        }
                    }
                }
                if (task != null)
                    notifyDLTaskUIMsgToHandler(state, Integer.parseInt(theme_id), errorCode,task.isBackgroundTask());
            }

            @Override
            public void updateDownloadProgress(int theme_id, int downloadFileSize, int downloadPosition, int downloadSpeed) {
                DownloadTask task = loadGametasks.get(String.valueOf(theme_id));
                JLog.d(TAG,"theme_id"+theme_id);
                if (null == task) {
                    return;
                }
                // 判断刷新的频率，防止过度刷屏
                long current = System.currentTimeMillis();
                if ((current - lastRefreshUI) < 1000) {
                    // 如果900ms内已经刷新
                }else{
                    if(!task.isBackgroundTask()){
                        notifyDLTaskUIMsgToHandler(
                                DownloadState.STATE_DOWNLOAD_UPDATE_PROGRESS,
                                theme_id, DownloadState.ERROR_NONE,
                                task.isBackgroundTask());
                    }else{
                        notifyDLTaskUIMsgToHandler(
                                DownloadState.STATE_DOWNLOAD_UPDATE_PROGRESS,
                                theme_id, DownloadState.ERROR_NONE,
                                task.isBackgroundTask());
                    }
                }

                task.setDownloadSize(downloadFileSize, downloadPosition);
                task.setDownloadSpeed(downloadSpeed);
                DBUtils.updateDownloadSize(theme_id+"",downloadFileSize,downloadPosition);
            }
        };
        loadGametasks = new HashMap<>();
        initDownloadGameTaskFromDB();
    }



    /**
     * 初始化下载的数据
     */
    private void initDownloadGameTaskFromDB() {
        ThreadPoolManager.getDownloadPool().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if (loadGametasks == null) {
                        loadGametasks = new HashMap<>();
                    }
                    loadGametasks.putAll(DBUtils.findDownloadTasks());
                    notifyDLTaskUIMsgToHandler( DownloadState.STATE_DOWNLOAD_MODE_INIT, -1, 0, 5000); // 5s后执行，需要等UI准备好
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }


    /**
     * 下载接口
     *
     * @param game
     * @param force
     * @return false: 继续下载， true： 新下载
     */
    public Boolean startDownload(SingleThemeItemBean.ItemsBean loadGame, boolean isBackground,int type) {
        boolean isNewDownload = false;
        if (null == loadGame || loadGame.getId() ==null) {
            return isNewDownload;
        }
        int theme_id = Integer.parseInt(loadGame.getId());
        int checkResult = checkNetAndSpace();
        if (DownloadState.ERROR_NONE == checkResult) {

        }else {
            // 通知错误
            notifyDLTaskUIMsgToHandler(DownloadState.STATE_DOWNLOAD_ERROR,Integer.parseInt(loadGame.getId()+type), checkResult, isBackground);
            return false;
        }
        synchronized (loadGametasks){
            DownloadTask loadGameTask = loadGametasks.get(String.valueOf(theme_id) + type);
            if(null == loadGameTask){
                FileUtils.deleteTmpDownloadFile(loadGame,type);
                loadGameTask = new DownloadTask(loadGame,type);
                loadGametasks.put(String.valueOf(loadGame.getId())+type,loadGameTask);
                isNewDownload = true;
                loadGameTask.setBackgroundTaskFlag(isNewDownload);// 新增任务时，初始化
                //数据库操作
                DBUtils.saveOrUpdate(loadGameTask,type);
            }else{
                if(loadGameTask.isBackgroundTask() && !isBackground){
                    loadGameTask.setBackgroundTaskFlag(false);
                    //数据库操作
                    DBUtils.saveOrUpdate(loadGameTask, type);
                }
                //continue download.
                if((DownloadState.STATE_DOWNLOAD_ERROR == loadGameTask.gameDownloadState)){
                    loadGameTask.resetTask(loadGame,type); //替换下载游戏信息
                    DBUtils.saveOrUpdate(loadGameTask, type);
                }else if(DownloadState.STATE_DOWNLOAD_SUCESS == loadGameTask.gameDownloadState){
                    if(FileUtils.isFileExists(loadGame,type)){

                        return isNewDownload;
                    }else {
                        loadGameTask.gameDownloadState = DownloadState.STATE_DOWNLOAD_ERROR;
                    }
                }
            }
            String url = DBUtils.getAppDownUrl(loadGame.getId()+type);
            if (!TextUtils.isEmpty(url)) {
                loadGameTask.loadGame.setDownload_url(url);
            }
            startDownloadTask(loadGameTask, type);
            return  isNewDownload;
        }
    }

    private void continueBackgroundDownload(int type) {
        if (ClientInfo.networkType != ClientInfo.WIFI) {
            // 非WIFI网络，直接退出
            return;
        }
        if (null == loadGametasks || 0 == loadGametasks.size()) {
            return;
        }
        int checkResult = checkNetAndSpace();
        if (DownloadState.ERROR_NONE == checkResult) {

        } else {
            // 通知错误
            return;
        }
        synchronized (loadGametasks){
            Iterator<Map.Entry<String, DownloadTask>> ite = loadGametasks.entrySet().iterator();
            Map.Entry<String, DownloadTask> entity = null;
            DownloadTask task = null;
            while (ite.hasNext()) {
                entity = ite.next();
                task = entity.getValue();
                if ((DownloadState.STATE_DOWNLOAD_SUCESS != task.gameDownloadState)
                        && task.isBackgroundTask()) {
                    startDownloadTask(task,type);
                }
            }
        }
    }

    /**
     * 启动下载
     *
     * @param task
     */
    private void startDownloadTask(DownloadTask task,int type) {
        if (null == task) {
            return;
        }

        if (!task.isBackgroundTask()) {
            task.startTask(loadListener, type);
        } else if (task.isBackgroundTask()) {
            task.startTask(loadListener,type);
        }
    }

    private int checkNetAndSpace() {
        int netType = ClientInfo.networkType;
        int errorCode = DownloadState.ERROR_NONE;

        if (netType == ClientInfo.NONET) {
            // 通知网络错误
            errorCode = DownloadState.ERROR_CODE_NO_NET;
        } else if ((netType != ClientInfo.WIFI)
                && BaseApplication.isDownloadWIFIOnly()) {
            // 通知网络设置
            // errorCode = DownloadState.ERROR_CODE_NOT_WIFI;
        } else if (!Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED)) {
            // 通知无SD卡
            errorCode = DownloadState.ERROR_CODE_NO_SDCARD;
        }
        return errorCode;
    }

    /**
     * 只通知UI,不做其他逻辑处理
     *
     * @param state
     *            下载状态
     * @param errorCode
     *            错误码
     * @param theme_id
     *            包名
     */
    private void notifyUIDownloadState(int state, int errorCode,int theme_id) {
        if (null == uiListners || 0 == uiListners.size()) {
            return;
        }
        if (null == loopListners) {
            loopListners = new HashSet<>();
        }
        loopListners.clear();
        loopListners.addAll(uiListners);
        for (UIDownLoadListener listener : loopListners) {
            listener.handleDownloadState(state, errorCode, theme_id);
        }
        // 最后通知的时间
        lastRefreshUI = System.currentTimeMillis();
        loopListners.clear();

    }


    private boolean checkDownloadSucess(SingleThemeItemBean.ItemsBean ItemsBean,int type){
        if (null == ItemsBean) {
            return false;
        }
        DownloadTask task = loadGametasks.get(ItemsBean.getId()+type);
        if (null == task) {
            return false;
        }
        try{
            // 文件重命名
            String gameCode = String.valueOf(ItemsBean.getId());
            if (renameDownloadFile(task.loadGame,type)){
                if(task.isBackgroundTask()){
                    DBUtils.updateState(ItemsBean.getId(),DownloadState.STATE_DOWNLOAD_SUCESS);
                }
                return true;
            }else{
                return false;
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    /**
     * 将临时文件重命名
     *
     * @param ItemsBean
     * @return
     * @throws IOException
     */
    private boolean renameDownloadFile(SingleThemeItemBean.ItemsBean ItemsBean,int type) throws IOException {
        File oldfile = new File(FileUtils.getDownloadTempPath(FileUtils.getTempFileName(ItemsBean),type));
        File newfile = new File(FileUtils.getDownloadPath(ItemsBean.getId(),type));

        if (newfile.exists()) {
            newfile.delete();
        }
        if (oldfile.exists()) {
            oldfile.renameTo(newfile);
            return true;
        } else {
            // 文件意外删除了
            return false;
        }
    }

    public DownloadTask getDownloadTask(String theme_id) {
        return loadGametasks.get(theme_id);
    }

    public boolean DownloadTaskCotain(String theme_id) {
        return (loadGametasks.get(theme_id) != null);
    }

    /**
     * 提供 appManagerCenter，静默安装的时候，状态变化刷新
     */
    public void notifyRefreshUI(int state,String song_id) {
        notifyDLTaskUIMsgToHandler(state, Integer.parseInt(song_id), 0, false);
    }

    private void notifyDLTaskUIMsgToHandler(int state, int song_id, int errorCode, boolean isBackground) {
        if (isBackground) {
            notifyDLTaskUIMsgToHandler(state, song_id, errorCode, 0);
        } else {
            JLog.d(TAG,"song_id="+song_id+"state"+state);
            notifyDLTaskUIMsgToHandler(state, song_id, errorCode, 0);
        }
    }

    /**
     * 通知task的状态给Handler，发送给UI的监听.只通知UI,不做其他逻辑处理
     *
     * @param state
     * @param song_id
     * @param errorCode
     */
    private synchronized void notifyDLTaskUIMsgToHandler(int state,int song_id, int errorCode, long delayMillis) {
        Message msg = Message.obtain();
        msg.what = state;
        msg.arg1 = errorCode;
        msg.arg2 = song_id;
        loadHandler.sendMessageDelayed(msg, delayMillis);
    }

    /**
     * 停止所有下载
     */
    public void pauseAllDownload() {
        if (null == loadGametasks || 0 == loadGametasks.size()) {
            return;
        }
        synchronized (loadGametasks) {
            Iterator<Map.Entry<String, DownloadTask>> ite = loadGametasks
                    .entrySet().iterator();
            Map.Entry<String, DownloadTask> entity = null;
            DownloadTask task = null;
            while (ite.hasNext()) {
                entity = ite.next();
                task = entity.getValue();
                pauseDownloadTask(task, task.isUserPause());
            }
        }
    }

    /**
     *
     * @param loadGameTask
     * @param isUser
     */
    public void pauseDownloadTask(SingleThemeItemBean.ItemsBean ItemsBean, boolean isUser,int type) {
        if (null == ItemsBean) {
            return;
        }
        DownloadTask loadGameTask = loadGametasks.get(ItemsBean.getId()+type);
        pauseDownloadTask(loadGameTask, isUser);
    }

    /**
     *
     * @param loadGameTask
     * @param isUser
     */
    private void pauseDownloadTask(DownloadTask loadGameTask, boolean isUser) {
        if (null == loadGameTask) {
            return;
        }
        loadGameTask.pauseTask(isUser);
        DBUtils.updateLoadFlag(loadGameTask);
    }

    /**
     * 判断是否有正在下载的任务
     */
    public boolean hasDownloadingTask() {
        if (null == loadGametasks || 0 == loadGametasks.size()) {
            return false;
        }
        synchronized (loadGametasks) {
            Iterator<Map.Entry<String, DownloadTask>> ite = loadGametasks
                    .entrySet().iterator();
            Map.Entry<String, DownloadTask> entity = null;
            DownloadTask task = null;
            while (ite.hasNext()) {
                entity = ite.next();
                task = entity.getValue();
                if (task.isBackgroundTask()) {
                    // 后台任务，不计算
                } else {
                    if (task.taskIsLoading()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public int getDownloadProgress(String song_id) {
        DownloadTask task = loadGametasks.get(song_id);
        if (null == task) {
            return 0;
        }
        return task.progress;
    }

    public void clearTask(String song_id) {

        synchronized (loadGametasks) {
            DownloadTask task = loadGametasks.get(song_id);
            if (null != task) {
                task.onDestory();
                loadGametasks.remove(song_id);
            }
        }
    }

    public void setUIDownloadListener(UIDownLoadListener refreshHandle) {
        if (null == uiListners) {
            uiListners = new HashSet<>();
        }
        uiListners.add(refreshHandle);
    }

    public void removeUIDownloadListener(UIDownLoadListener refreshHandle) {
        if (null == uiListners) {
            return;
        }
        uiListners.remove(refreshHandle);
    }

    /**
     * 继续所有下载
     */
    public void continueAllDownload() {
        if (null == loadGametasks || 0 == loadGametasks.size()) {
            return;
        }

        int checkResult = checkNetAndSpace();
        if (DownloadState.ERROR_NONE == checkResult) {

        } else {
            // 通知错误
            return;
        }

        synchronized (loadGametasks) {
            Iterator<Map.Entry<String, DownloadTask>> ite = loadGametasks
                    .entrySet().iterator();
            Map.Entry<String, DownloadTask> entity = null;
            DownloadTask task = null;
            while (ite.hasNext()) {
                entity = ite.next();
                task = entity.getValue();
                if (DownloadState.STATE_DOWNLOAD_SUCESS != task.gameDownloadState
                        && DownloadState.STATE_DOWNLOAD_INSTALLED != task.gameDownloadState) {
                    // 只要还没下载完成，或者安装的，都恢复下载
                    if (task.isUserPause()) {

                    }
//                    else if (task.isBackgroundTask()) {
//                        // 后台任务不启动继续下载，由外部控制
//                    }
                    else {
                        startDownloadTask(task,task.type);
                    }
                }
            }
        }
    }


    public void cancelDownload(SingleThemeItemBean.ItemsBean game,int type) {
        if (null == game || null == game.getId()) {
            return;
        }
        DownloadTask loadGameTask = removeTask(game.getId()+type);
        if (null != loadGameTask) {
            // stop task
            loadGameTask.cancelTask(type);
            notifyDLTaskUIMsgToHandler(DownloadState.STATE_DOWNLOAD_CANCEL,Integer.parseInt(game.getId()+game.getType()), 0, loadGameTask.isBackgroundTask());
        }
    }

    private DownloadTask removeTask(String id) {
        if (null == id) {
            return null;
        }
        DownloadTask task = null;
        synchronized (loadGametasks){
            task = loadGametasks.remove(id);
            if (task == null)
                return null;
            DBUtils.deleteDownloadById(id);
            return task;
        }
    }

    public int getDownloadSpeed(String theme_id){
        DownloadTask task = loadGametasks.get(theme_id);
        if(task == null) return 0;
        return task.downloadSpeed;
    }

    public  void setDownlaadTaskState(String theme_id,int type){
        if(loadGametasks !=null &&  theme_id != null){
            Iterator<Map.Entry<String, DownloadTask>> ite = loadGametasks.entrySet().iterator();
            Map.Entry<String, DownloadTask> entity = null;
            DownloadTask task = null;
            while (ite.hasNext()) {
                entity = ite.next();
                JLog.d(TAG,"entity="+entity );
                task = entity.getValue();
                if (DownloadState.STATE_DOWNLOAD_INSTALLED == task.gameDownloadState && task.type == type) {
                    task.setGameDownloadState(DownloadState.STATE_DOWNLOAD_SUCESS);
                }
                if(entity.getKey().equals(theme_id+type)){
                    task.setGameDownloadState(DownloadState.STATE_DOWNLOAD_INSTALLED);
                }
            }
        }
    }

    /**
     * 刷新主界面
     * @param type 123
     */
    public  void setDownloadTaskState(int type){
        if(loadGametasks !=null){
            Iterator<Map.Entry<String, DownloadTask>> ite = loadGametasks.entrySet().iterator();
            Map.Entry<String, DownloadTask> entity = null;
            DownloadTask task = null;
            while (ite.hasNext()) {
                entity = ite.next();
                JLog.d(TAG,"entity="+entity );
                task = entity.getValue();
                if (DownloadState.STATE_DOWNLOAD_INSTALLED == task.gameDownloadState && task.type == type) {
                    task.setGameDownloadState(DownloadState.STATE_DOWNLOAD_SUCESS);
                }
            }
        }
    }

}
