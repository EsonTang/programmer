package com.prize.app.database.dao;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.prize.app.beans.AppInstallFaile;
import com.prize.app.beans.EarnPoints360Bean;
import com.prize.app.beans.EarnPointsBean;
import com.prize.app.beans.PushEven;
import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.app.util.GsonParseUtils;
import com.prize.app.util.JLog;
import com.prize.statistics.model.TcNote;

import org.xutils.DbManager;
import org.xutils.ex.DbException;
import org.xutils.x;

import java.util.ArrayList;
import java.util.List;

import static com.prize.statistics.model.StatisConstant.PRIZE_STAT_DB_NAME;
import static com.prize.statistics.model.StatisConstant.STATICS_DBVERSIONCODE;

/**
 * longbaoxiu
 * 2017/1/17.11:21
 */

public class XutilsDAO {
    private static Context sContext;
    private static DbManager sDbManager;

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

    public static PushEven getDataPushEven() {
        try {
            PushEven even = sDbManager.findFirst(PushEven.class);
            if (even == null) {
                return null;
            }
            return even;
        } catch (DbException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static void storeData(PushEven event) {
        JLog.i("PRIZE", "storeData-Event=" + event.operationType + "--" + event.operationType);
        try {
            if (null != sDbManager) {
                sDbManager.save(event);
            } else {
                init(sContext);
                sDbManager.save(event);
            }
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    public static void storeEarnPointsBean(String packageName) {
        try {
            if (null != sDbManager) {
                EarnPointsBean list = sDbManager.selector(EarnPointsBean.class).where("packageName", "=", packageName).findFirst();
                if (list != null)
                    return;
                EarnPointsBean event = new EarnPointsBean();
                event.packageName = packageName;
                event.timeStamp = System.currentTimeMillis();
                sDbManager.save(event);
            } else {
                init(sContext);
                EarnPointsBean list = sDbManager.selector(EarnPointsBean.class).where("packageName", "=", packageName).findFirst();
                if (list != null)
                    return;
                EarnPointsBean event = new EarnPointsBean();
                event.packageName = packageName;
                event.timeStamp = System.currentTimeMillis();
                sDbManager.save(event);
            }
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    public static void storeEarnPoints360Bean(AppsItemBean bean) {
        if (bean == null || TextUtils.isEmpty(bean.backParams)) return;
        try {
            if (null != sDbManager) {
                EarnPoints360Bean event = new EarnPoints360Bean();
                event.packageName = bean.packageName;
                event.gson = new Gson().toJson(bean);
                event.timeStamp = System.currentTimeMillis();
                sDbManager.save(event);
            } else {
                init(sContext);
                EarnPoints360Bean event = new EarnPoints360Bean();
                event.packageName = bean.packageName;
                event.gson = new Gson().toJson(bean);
                event.timeStamp = System.currentTimeMillis();
                sDbManager.save(event);
            }
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    /**
     * 找寻360点击的数据
     *
     * @return List<AppsItemBean>
     */
    public static List<AppsItemBean> findPoints360Bean() {

        try {
            List<AppsItemBean> targetList = new ArrayList<AppsItemBean>();
            delete360Bean((System.currentTimeMillis() - (20 * 60 * 1000)));
            if (null != sDbManager) {
                List<EarnPoints360Bean> list = sDbManager.selector(EarnPoints360Bean.class).findAll();
                if (list == null) return null;
                EarnPoints360Bean bean;
                AppsItemBean appItem;
                for (int i = 0; i < list.size(); i++) {
                    bean = list.get(i);
                    if (bean != null && !TextUtils.isEmpty(bean.gson)) {
                        appItem = GsonParseUtils.parseSingleBean(bean.gson, AppsItemBean.class);
                        if (appItem != null) {
                            appItem.backParams = null;
                            targetList.add(appItem);
                        }
                    }
                }
                return targetList;
            } else {
                init(sContext);
                List<EarnPoints360Bean> list = sDbManager.selector(EarnPoints360Bean.class).findAll();
                if (list == null) return null;
                EarnPoints360Bean bean;
                AppsItemBean appItem;
                for (int i = 0; i < list.size(); i++) {
                    bean = list.get(i);
                    if (bean != null && !TextUtils.isEmpty(bean.gson)) {
                        appItem = GsonParseUtils.parseSingleBean(bean.gson, AppsItemBean.class);
                        if (appItem != null) {
                            targetList.add(appItem);
                        }
                    }
                }
                return targetList;
            }
        } catch (DbException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 删除过期的360点击的数据
     */
    private static void delete360Bean(long timeStamp) {

        try {
            if (null != sDbManager) {
                List<EarnPoints360Bean> list = sDbManager.selector(EarnPoints360Bean.class).where("timeStamp", "<", timeStamp).findAll();
                if (list != null && list.size() > 0) {
                    sDbManager.delete(list);
                }
            } else {
                init(sContext);
                List<EarnPoints360Bean> list = sDbManager.selector(EarnPoints360Bean.class).where("timeStamp", "<", timeStamp).findAll();
                if (list != null && list.size() > 0) {
                    sDbManager.delete(list);
                }
            }
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除已经领取积分的360点击的数据
     */
    public static void deletegettedPoints360Bean(String packageName) {

        try {
            if (null != sDbManager) {
                List<EarnPoints360Bean> list = sDbManager.selector(EarnPoints360Bean.class).where("packageName", "=", packageName).findAll();
                if (list != null && list.size() > 0) {
                    sDbManager.delete(list);
                }
            } else {
                init(sContext);
                List<EarnPoints360Bean> list = sDbManager.selector(EarnPoints360Bean.class).where("packageName", "=", packageName).findAll();
                if (list != null && list.size() > 0) {
                    sDbManager.delete(list);
                }
            }
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除数据
     *
     * @param timeStamp 时间戳
     */
    public static void deletEarnPointsBeanData(long timeStamp) {
        try {
            if (null != sDbManager) {
                List<EarnPointsBean> list = sDbManager.selector(EarnPointsBean.class).where("timeStamp", "<", timeStamp).findAll();
                if (list != null && list.size() > 0) {
                    sDbManager.delete(list);
                }
            } else {
                init(sContext);
                List<EarnPointsBean> list = sDbManager.selector(EarnPointsBean.class).where("timeStamp", "<", timeStamp).findAll();
                if (list != null && list.size() > 0) {
                    sDbManager.delete(list);
                }
            }
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    /**
     * 是否存在数据已经领取积分了
     *
     * @param packageName 包名
     */
    public static boolean isExistsEarnPointsBeanData(String packageName) {
        try {
            if (TextUtils.isEmpty(packageName))
                return false;
            if (null != sDbManager) {
                EarnPointsBean list = sDbManager.selector(EarnPointsBean.class).where("packageName", "=", packageName).findFirst();
                return (list != null);
            } else {
                init(sContext);
                EarnPointsBean list = sDbManager.selector(EarnPointsBean.class).where("packageName", "=", packageName).findFirst();
                return (list != null);
            }
        } catch (DbException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 删除数据
     *
     * @param timeStamp 时间戳
     */
    public static void deletData(long timeStamp) {
        try {
            if (null != sDbManager) {
                PushEven list = sDbManager.selector(PushEven.class).where("timeStamp", "=", timeStamp).findFirst();
                sDbManager.delete(list);
            } else {
                init(sContext);
                PushEven list = sDbManager.selector(PushEven.class).where("timeStamp", "=", timeStamp).findFirst();
                sDbManager.delete(list);
            }
        } catch (DbException e) {
            e.printStackTrace();

        }
    }

    /**
     * 失败了保存一次
     *
     * @param appsItemBean AppsItemBean
     */
    public static void storeAppInstallFaile(AppsItemBean appsItemBean) {
        try {
            if (appsItemBean == null || TextUtils.isEmpty(appsItemBean.packageName))
                return;
            if (null != sDbManager) {
                AppInstallFaile bean = sDbManager.selector(AppInstallFaile.class).where("packageName", "=", appsItemBean.packageName).and("versionCode", "=", appsItemBean.versionCode).findFirst();
                if (JLog.isDebug) {
                    JLog.i("XutilsDAO", "storeAppInstallFaile-bean:" + bean);
                }
                if (bean != null) {
                    if (JLog.isDebug) {
                        if ("com.prize.music".equals(appsItemBean.packageName)) {
                            JLog.i("XutilsDAO", "音乐下载失败第2次");
                        }
                    }
                    if (bean.installCount != 2) {
                        bean.installCount = 2;
                        sDbManager.update(bean, "installCount");
                    }
                } else {
                    if (JLog.isDebug) {
                        if ("com.prize.music".equals(appsItemBean.packageName)) {
                            JLog.i("XutilsDAO", "音乐下载失败了第1次");
                        }
                    }
                    AppInstallFaile originalBean = new AppInstallFaile();
                    originalBean.packageName = appsItemBean.packageName;
                    originalBean.versionCode = appsItemBean.versionCode;
                    originalBean.installCount = 1;
                    originalBean.timeStamp = System.currentTimeMillis();
                    sDbManager.save(originalBean);
                }
            } else {
                init(sContext);
                AppInstallFaile bean = sDbManager.selector(AppInstallFaile.class).where("packageName", "=", appsItemBean.packageName).and("versionCode", "=", appsItemBean.versionCode).findFirst();
                if (bean != null) {
                    if (bean.installCount != 2) {
                        bean.installCount = 2;
                        sDbManager.update(bean, "installCount");
                    } else {
                        AppInstallFaile originalBean = new AppInstallFaile();
                        originalBean.packageName = appsItemBean.packageName;
                        originalBean.versionCode = appsItemBean.versionCode;
                        originalBean.installCount = 1;
                        originalBean.timeStamp = System.currentTimeMillis();
                        sDbManager.save(originalBean);
                    }
                }
            }
        } catch (DbException e) {
            e.printStackTrace();
            JLog.i("XutilsDAO", "storeAppInstallFaile-DbException:" + e.getMessage());

        }
    }

    /**
     * 是否已经安装失败了2次
     *
     * @param appsItemBean AppsItemBean
     */
    public static boolean isAppInstallFaile(AppsItemBean appsItemBean) {
        try {
            if (null != sDbManager) {
                AppInstallFaile bean = sDbManager.selector(AppInstallFaile.class).where("packageName", "=", appsItemBean.packageName).and("versionCode", "=", appsItemBean.versionCode).and("installCount", "=", 2).findFirst();
                if (JLog.isDebug) {
                    JLog.i("XutilsDAO", "isAppInstallFaile-bean:" + bean);
                }
                return bean != null;
            } else {
                init(sContext);
                AppInstallFaile bean = sDbManager.selector(AppInstallFaile.class).where("packageName", "=", appsItemBean.packageName).and("versionCode", "=", appsItemBean.versionCode).and("installCount", "=", 2).findFirst();
                return bean != null;
            }
        } catch (DbException e) {
            e.printStackTrace();
            JLog.i("XutilsDAO", "isAppInstallFaile-DbException:" + e.getMessage());
        }
        return false;
    }
}
