package com.prize.prizenavigation;

import android.app.Application;
import android.content.Context;
import android.os.Handler;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.prize.prizenavigation.bean.NaviDatas;
import com.prize.prizenavigation.receiver.ScreenListener;
import com.prize.prizenavigation.utils.IConstants;
import com.umeng.analytics.MobclickAgent;
import com.zhy.http.okhttp.OkHttpUtils;

import org.xutils.DbManager;
import org.xutils.db.DbManagerImpl;
import org.xutils.db.table.TableEntity;
import org.xutils.ex.DbException;
import org.xutils.x;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

/**
 * Created by liukun on 2017/3/1.
 */
public class NavigationApplication extends Application {
    // 记录当前 Context
    private static Context mContext;
    private static Handler handler;
    private static int mainThreadId;


    /**
     * 单例数据库管理类
     */
    private static DbManager xDbManager = null;

    private DbManager.DaoConfig daoConfig = new DbManager.DaoConfig()
            .setDbName(IConstants.DB_NAEM)
            .setDbVersion(IConstants.DB_VERSION)
            .setDbUpgradeListener(new DbManager.DbUpgradeListener() {
                @Override
                public void onUpgrade(DbManager db, int oldVersion, int newVersion) {

                }
            });

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        handler = new Handler();
        mainThreadId = android.os.Process.myTid();
//        initImageLoader();
        Fresco.initialize(mContext);
//        queryUserId();
        initOkhttp();
        initXutils();
        initDb();
        initUmeng(mContext);

        registerScreenLister();
    }

    public static Context getContext() {
        return mContext;
    }

    public static Handler getHandler() {
        return handler;
    }

    public static int getMainThreadId() {
        return mainThreadId;
    }

//    private void initImageLoader() {
////        ImageLoaderConfiguration configuration = ImageLoaderConfiguration
////                .createDefault(this);
//        //Initialize ImageLoader with configuration.
//        ImageLoaderConfiguration configuration = new ImageLoaderConfiguration
//                .Builder(getContext())
//                .threadPriority(Thread.NORM_PRIORITY - 2)
//                .denyCacheImageMultipleSizesInMemory()
//                .diskCacheFileNameGenerator(new Md5FileNameGenerator())
//                .diskCacheSize(50 * 1024 * 1024)
////				.discCacheFileCount(100) //缓存的文件数量
//                // 50 Mb
//                .tasksProcessingOrder(QueueProcessingType.LIFO)
//                // .writeDebugLogs() // Remove for release app
//                .build();
//        ImageLoader.getInstance().init(configuration);
//    }

    private void initOkhttp() {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                // .addInterceptor(new LoggerInterceptor("TAG"))
                .connectTimeout(10000L, TimeUnit.MILLISECONDS)
                .readTimeout(10000L, TimeUnit.MILLISECONDS).build();
        OkHttpUtils.initClient(okHttpClient);
    }

    /**
     * 单例数据库管理类
     */
    public static DbManager getDbManager() {
        return xDbManager;
    }

    /**
     * 初始化xutils
     */
    private void initXutils() {
        x.Ext.init(this);
        try {
            xDbManager = x.getDb(daoConfig);
        } catch (Exception e) {
            // TODO: handle exception
        }
        x.Ext.setDebug(IConstants.ISDEBUG);
    }

    /***
     * 初始化数据库
     */
    private void initDb() {
        createTableIsNeed();
    }

    /**
     * 创建表
     */
    private void createTableIsNeed() {
        DbManagerImpl impl = (DbManagerImpl) xDbManager;
        try {
            TableEntity<NaviDatas.ListBean> table = TableEntity.get(xDbManager, NaviDatas.ListBean.class);
            impl.createTableIfNotExist(table);
            table = null;
        } catch (DbException e) {
            e.printStackTrace();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

//    /**
//     * 方法描述：查询是否登录云账号 返回userId
//     *
//     * @return void 返回userId 或者unkouwn
//     */
//    public static String queryUserId() {
//
////		Person p  = Utils.getPersonalInfo(MainApplication.curContext);
////		if(p!=null){
////			return  p.getUserId();
////		}
////		return null;
//        ContentResolver resolver = getContext().getContentResolver();
//        Uri uri = null;
//        uri = Uri.parse("content://com.prize.appcenter.provider.appstore/table_person");
//        String userId = null;
//        try {
//            Cursor cs = resolver.query(uri, null, null, null, null);
//            if (cs != null && cs.moveToFirst()) {
//                userId = cs.getString(cs.getColumnIndex("userId"));
//            }
//            if (cs != null) {
//                cs.close();
//            }
//            if (TextUtils.isEmpty(userId))
//                return "";
//        } catch (Exception e) {
//            return "";
//        }
//        ClientInfo.getInstance().setUserId(userId);
//        return userId;
//    }

    /**
     * umeng初始化
     *
     * @param context
     */
    private void initUmeng(Context context) {
        MobclickAgent.setDebugMode(IConstants.ISDEBUG);
        // SDK在统计Fragment时，需要关闭Activity自带的页面统计，
        // 然后在每个页面中重新集成页面统计的代码(包括调用了 onResume 和 onPause 的Activity)。
        MobclickAgent.openActivityDurationTrack(false);
        //捕获程序崩溃日志
        MobclickAgent.setCatchUncaughtExceptions(true);
        MobclickAgent.setScenarioType(context, MobclickAgent.EScenarioType.E_UM_NORMAL);
    }

    /**
     * 方法描述：注册锁屏监听者
     */
    private void registerScreenLister() {
        ScreenListener screenListener = new ScreenListener(this);
        screenListener.begin(new ScreenListener.ScreenStateListener() {

            @Override
            public void onUserPresent() {

            }

            @Override
            public void onScreenOn() {

            }

            // 锁屏并且电量高于30%时才会调用
            @Override
            public void onScreenOff() {
            }

            @Override
            public void onScreenOffNoRLLevel() {
            }
        });
    }
}
