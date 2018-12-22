/*
 * Disallow inter-launching apps feature. prize-linkh-20160307
*/
package com.android.server.am;

import android.content.Context;
import android.app.IApplicationThread;
import android.os.IBinder;
import android.content.Intent;
import android.util.Slog;
import android.content.ComponentName;
import android.os.Build;
import android.os.SystemClock;
import android.util.ArrayMap;
/*-prize add by lihuangyuan,for whitelist -2017-05-06-start-*/
import android.os.WakeupItem;
import java.util.ArrayList;
/*-prize add by lihuangyuan,for whitelist -2017-05-06-end-*/
import android.util.PrizeGlobalTag;

final class PrizeInterLaunchingPaths {
    private static final String TAG = PrizeGlobalTag.PROC_MGR_SR + "/path"; //"PrizeInterLaunchingAppPolicy-paths"; //"PrizeInterLaunchingPaths";    
    private static final boolean DBG_INFO = ActivityManagerService.DBG_INTER_LAUNCHING_APPS;

    private static final int ODD_PRIME_NUMBER = 37;        
    private static final int HASH_CODE_NUM = 23;
    
    private Context mContext;
    
    private final ArrayMap<Integer, ServicePathData> mServicePathsDataMap = 
        new ArrayMap<Integer, ServicePathData>();
    private final ArrayList<ServicePathData> mMatchClassHeadPathDataAry = 
        new ArrayList<ServicePathData>();

    public PrizeInterLaunchingPaths(Context context) {
        mContext = context;
        
        long start = SystemClock.elapsedRealtime();        
	 synchronized (mServicePathsDataMap) 
	 {
        	initData();
	 }
        long spentTime = SystemClock.elapsedRealtime() - start;
        logInfo("initData(). time spent: " + spentTime + "ms");
    }
    /*-prize add by lihuangyuan,for whitelist -2017-05-06-start-*/
    public void loadData(WakeupItem[] classactionary)
    {
    	    if(classactionary == null)return;
	    Slog.d("whitelist","before loadData mapsieze:"+mServicePathsDataMap.size());
	    synchronized (mServicePathsDataMap) 
	    {
		mServicePathsDataMap.erase();
              mMatchClassHeadPathDataAry.clear();
	    }
    	    for(int i=0;i<classactionary.length;i++)
    	    {
    	        WakeupItem wakeupitem = classactionary[i];
		 if(wakeupitem == null )continue;
		 
	 	ServicePathData item = new ServicePathData(wakeupitem.targetPkg,wakeupitem.classname,wakeupitem.action,wakeupitem.callerpkg,wakeupitem.state);
		synchronized (mServicePathsDataMap) 
		{
                    if((item.interceptState == PrizeInterLaunchingAppPolicy.STATE_MATCH_CLASS_MAY_DISALLOW
                        ||item.interceptState == PrizeInterLaunchingAppPolicy.STATE_MATCH_CLASS_DISALLOW)
                        && item.targetClass != null)
                    {
                        item.interceptState -= PrizeInterLaunchingAppPolicy.STATE_MATCH_START;
                        mMatchClassHeadPathDataAry.add(item);
                    }                    
                    else
                    {
			    mServicePathsDataMap.put(item.key, item);
                    }
		}						
		Slog.d("whitelist","put servicepathsdata pkg:"+wakeupitem.targetPkg
			+"/"+wakeupitem.classname
			+"/"+wakeupitem.action
			+"/"+wakeupitem.callerpkg
			+"/"+wakeupitem.state);
    	    }	
	    Slog.d("whitelist","after loadData mapsieze:"+mServicePathsDataMap.size());
    }
    /*-prize add by lihuangyuan,for whitelist -2017-05-06-end-*/
    private void initData() {
        /********** Baidu **********/
        ServicePathData item = new ServicePathData("com.baidu.sapi2.share.ShareService", "baidu.intent.action.account.SHARE_SERVICE");
        item.interceptState = PrizeInterLaunchingAppPolicy.STATE_DISALLOW;
        mServicePathsDataMap.put(item.key, item);
        

        /******** Ali **********/
        //com.ali.money <= com.taobao.taobao
        item = new ServicePathData("com.ali.money.shield.service.TransmitService", "com.ali.money.shield.service.action.init");        
        mServicePathsDataMap.put(item.key, item);
        //com.taobao.trip  <= com.eg.android.AlipayGphone
        item = new ServicePathData("com.alipay.pushsdk.push.AppInfoRecvIntentService", "org.rome.sdk.IPP_CALL");
        mServicePathsDataMap.put(item.key, item);
        //com.autonavi.minimap <= com.taobao.mobile.dipei;com.taobao.movie.android;*
        item = new ServicePathData("com.amap.api.service.AMapService");
        //item.interceptState = PrizeInterLaunchingAppPolicy.STATE_DISALLOW; //??????????????????????????????????????????????        
        mServicePathsDataMap.put(item.key, item);
        //fm.xiami.main;com.sds.android.ttpod;*  <= fm.xiami.main;com.joyworks.shantu;*
        item = new ServicePathData("com.igexin.sdk.PushService");        
        mServicePathsDataMap.put(item.key, item);
        //com.taobao.taobao <= com.taobao.caipiao;com.eg.android.AlipayGphone;*
        item = new ServicePathData("com.taobao.accs.ChannelService", "org.agoo.android.intent.action.PING_V4");        
        mServicePathsDataMap.put(item.key, item);
        //com.tmall.wireless <= com.taobao.taobao
        item = new ServicePathData("com.taobao.accs.ChannelService", "com.taobao.accs.intent.action.SERVICE");        
        mServicePathsDataMap.put(item.key, item);
        //com.autonavi.minimap <= com.eg.android.AlipayGphone
        item = new ServicePathData("com.taobao.agoo.PushService");        
        mServicePathsDataMap.put(item.key, item);
        //com.taobao.caipiao;com.taobao.trip;* <= com.taobao.appcenter;com.taobao.caipiao;*
        item = new ServicePathData("com.taobao.agoo.PushService", "org.agoo.android.intent.action.PING_V4");        
        mServicePathsDataMap.put(item.key, item);
        //com.taobao.caipiao;com.taobao.trip <= com.taobao.appcenter;alicom.palm.android;*
        item = new ServicePathData("com.taobao.agoo.TaobaoMessageIntentReceiverService"/*, "org.android.agoo.client.ElectionReceiverService"*/);        
        mServicePathsDataMap.put(item.key, item);
        //com.eg.android.AlipayGphone;com.taobao.trip <= com.taobao.caipiao;com.taobao.trip
        item = new ServicePathData("com.taobao.android.sso.internal.PidGetterService");        
        mServicePathsDataMap.put(item.key, item);
        // com.tmall.wireless <= com.taobao.taobao
        item = new ServicePathData("com.tmall.wireless.TaobaoIntentService", "org.agoo.android.intent.action.RECEIVE");        
        mServicePathsDataMap.put(item.key, item);
        // com.UCMobile <= com.eg.android.AlipayGphone
        item = new ServicePathData("com.uc.base.push.PushFriendBridge", "com.UCMobile.intent.action.FRIEND");        
        mServicePathsDataMap.put(item.key, item);
        // com.youku.phone <= com.tmall.wireless;com.taobao.taobao;*
        item = new ServicePathData("com.youku.pushsdk.service.PushService", "com.youku.pushsdk.pushservice.FRIEND");        
        mServicePathsDataMap.put(item.key, item);
        //com.ss.android.article.news <= com.tmall.wireless;com.financial360.nicaifu;*
        item = new ServicePathData("com.umeng.message.UmengMessageIntentReceiverService"/*, "org.android.agoo.client.MessageReceiverService"*/);        
        mServicePathsDataMap.put(item.key, item);
        //com.eg.android.AlipayGphone <= com.taobao.mobile.dipei;com.taobao.taobao;*
        item = new ServicePathData("org.rome.android.ipp.binder.IppService");
        //item.interceptState = PrizeInterLaunchingAppPolicy.STATE_DISALLOW; //??????????????????????????????????????????????        
        mServicePathsDataMap.put(item.key, item);
        // com.UCMobile <= com.taobao.taobao
        item = new ServicePathData("com.UCMobile.TaobaoIntentService", "org.agoo.android.intent.action.RECEIVE");
        //item.shouldIntercept = false; //??????????????????????????????????????????????      
        mServicePathsDataMap.put(item.key, item);
        // com.UCMobile <= com.taobao.taobao;com.taobao.caipiao
        item = new ServicePathData("com.ucweb.message.UcwebMessageIntentReceiverService", "org.android.agoo.client.MessageReceiverService");        
        //item.shouldIntercept = false; //??????????????????????????????????????????????        
        mServicePathsDataMap.put(item.key, item);

        /******* Tencent ********/
        // com.tencent.qqlauncher <= com.tencent.launcher
        item = new ServicePathData("qrom.component.push.core.TCMService"/*, "qrom.compoent.tcm.action.start"*/);
        mServicePathsDataMap.put(item.key, item);
        //com.tencent.androidqqmail <= com.tencent.mtt
        item = new ServicePathData("com.tencent.qqmail.utilities.qmnetwork.service.QMWakeUpService");        
        mServicePathsDataMap.put(item.key, item);
        //com.tencent.gallerymanager <= com.tencent.qqpimsecure
        item = new ServicePathData("com.tencent.gallerymanager.service.PhotoGalleryService", "com.tencent.gallerymanager.service.PhotoGalleryService");        
        mServicePathsDataMap.put(item.key, item);
        //com.tencent.mtt <= com.tencent.mm
        item = new ServicePathData("com.tencent.mtt.sdk.BrowserSdkService", "com.tencent.mtt.ACTION_ACTIVE_PUSH");        
        mServicePathsDataMap.put(item.key, item);
        //com.tencent.mtt <= com.tencent.edu
        item = new ServicePathData("com.tencent.mtt.sdk.BrowserSdkService", "com.tencent.mtt.ACTION_INSTALL_X5");        
        mServicePathsDataMap.put(item.key, item);
        //com.tencent.qqpim <= com.tencent.qqpimsecure
        item = new ServicePathData("com.tencent.qqpim.service.share.QQPimShareService"/*, "com.tecnent.qqpim.CHANGENOTICE"*/);
        item.interceptState = PrizeInterLaunchingAppPolicy.STATE_MAY_DISALLOW;
        mServicePathsDataMap.put(item.key, item);
        // com.tencent.news <= android
        item = new ServicePathData("com.tencent.news.account.SyncService", "android.content.SyncAdapter");
        item.interceptState = PrizeInterLaunchingAppPolicy.STATE_MAY_DISALLOW;
        mServicePathsDataMap.put(item.key, item);

        /*************** JD ******************/        
        //com.wangyin.payment <= com.tencent.movieticket
        // remove it for Prize App Center. prize-linkh-20160801
        //item = new ServicePathData("com.tencent.android.tpush.rpc.XGRemoteService");
        //mServicePathsDataMap.put(item.key, item);

        /************ 360 **************/
        // com.qihoo.appstore <= com.qihoo.browser;com.qihoo.secstore
        item = new ServicePathData("com.qihoo.appstore.zhushouhelper.ZhushouHelperService", "com.qihoo.appstore.zhushouhelper.IZhushouHelperService");       
        item.interceptState = PrizeInterLaunchingAppPolicy.STATE_MAY_DISALLOW;
        mServicePathsDataMap.put(item.key, item);
        // com.qihoo.appstore <= com.qihoo360.mobilesafe;*
        item = new ServicePathData(null, "com.qihoo.appstore.QihooAlliance");
        item.interceptState = PrizeInterLaunchingAppPolicy.STATE_DISALLOW;
        mServicePathsDataMap.put(item.key, item);
        //com.qihoo.appstore <= com.qihoo.freewifi
        item = new ServicePathData("com.qihoo.core.CoreService", "com.qihoo.appstore.ACTION_DAEMON_CORE_SERVICE");
        item.interceptState = PrizeInterLaunchingAppPolicy.STATE_MAY_DISALLOW;
        mServicePathsDataMap.put(item.key, item);
        //com.qihoo.appstore <= com.qihoo360.mobilesafe
        item = new ServicePathData("com.qihoo.express.mini.service.DaemonCoreService", "android.intent.action.VIEW");       
        mServicePathsDataMap.put(item.key, item);
        //com.qihoo.browser <= com.qihoo.video
        item = new ServicePathData(null, "com.qihoo.browser.QihooAlliance");
        item.interceptState = PrizeInterLaunchingAppPolicy.STATE_DISALLOW;
        mServicePathsDataMap.put(item.key, item);
        // com.qihoo.browser <= com.qihoo.appstore;com.qihoo.video;*
        item = new ServicePathData("com.qihoo360.accounts.sso.svc.AccountService", "com.qihoo360.accounts.action.START_SERVICE");       
        mServicePathsDataMap.put(item.key, item);
        //com.qihoo.gameunion <= com.qihoo.video;com.qihoo.yunpan;*
        item = new ServicePathData(null, "com.qihoo.gameunion.QihooAlliance");
        item.interceptState = PrizeInterLaunchingAppPolicy.STATE_DISALLOW;
        mServicePathsDataMap.put(item.key, item);
        // com.qihoo.video <= com.qihoo360.mobilesafe;com.qihoo.yunpan;*
        item = new ServicePathData(null, "com.qihoo.video.QihooAlliance");
        item.interceptState = PrizeInterLaunchingAppPolicy.STATE_DISALLOW;
        mServicePathsDataMap.put(item.key, item);
        // com.qihoo360.mobilesafe <= com.qihoo.freewifi
        item = new ServicePathData(null, "com.qihoo360.mobilesafe.QihooAlliance");
        item.interceptState = PrizeInterLaunchingAppPolicy.STATE_DISALLOW;
        mServicePathsDataMap.put(item.key, item);
        // com.qihoo.cleandroid_cn <=  com.qihoo.antivirus
        item = new ServicePathData(null, "com.qihoo.cleandroid_cn.QihooAlliance");
        item.interceptState = PrizeInterLaunchingAppPolicy.STATE_DISALLOW;
        mServicePathsDataMap.put(item.key, item);        

        /************ xiaomi push sdk **************/
        item = new ServicePathData("com.xiaomi.mipush.sdk.PushMessageHandler");
        item.interceptState = PrizeInterLaunchingAppPolicy.STATE_DISALLOW;
        mServicePathsDataMap.put(item.key, item);

        /*360 browser push service */
        item = new ServicePathData("com.qihoo.browser.pushmanager.PushBrowserService");
        item.interceptState = PrizeInterLaunchingAppPolicy.STATE_DISALLOW;
        mServicePathsDataMap.put(item.key, item);        

        /* Letv push. 
        *  This service holds wakelock and doesn't relase it!!!
           So we temporarily prevent it from starting. */
        item = new ServicePathData("com.letv.android.client.push.LetvPushService");
        item.interceptState = PrizeInterLaunchingAppPolicy.STATE_DISALLOW;
        mServicePathsDataMap.put(item.key, item);            
    }

    private static void logInfo(String msg) {
        Slog.d(TAG, msg);
    }
    public ServicePathData getServicePathDataMatchClass(String targetClass)
    {
       synchronized (mServicePathsDataMap) 
	{
            for(int i=0;i<mMatchClassHeadPathDataAry.size();i++)
            {
                ServicePathData item = mMatchClassHeadPathDataAry.get(i);
                if(targetClass.startsWith(item.targetClass))
                {
                    Slog.d(TAG,"getServicePathDataMatchClass targetclass:"+targetClass+",match:"+item.targetClass+",state:"+item.interceptState);
                    return item;
                }
            }
       }
       return null;
    }
    public ServicePathData getServicePathData(String targetPkg, String targetClass, String action, String callerPkg) {
        int key = makeKey(targetPkg, targetClass, action, callerPkg);
	 synchronized (mServicePathsDataMap) 
	{
        return mServicePathsDataMap.get(key);
	}        
    }

    public ServicePathData getServicePathData(String targetClass, String action) {
        return getServicePathData(null, targetClass, action, null);
    }

    public ServicePathData getServicePathData(String targetClass) {
        return getServicePathData(null, targetClass, null, null);
    }

    public ServicePathData getServicePathData(ComponentName cn) {
        return getServicePathData(cn.getPackageName(), cn.getClassName(), null, null);
    }

    public ServicePathData getServicePathData(ComponentName cn, String action) {
        return getServicePathData(cn.getPackageName(), cn.getClassName(), action, null);
    }

    public int makeKey(String targetClass, String action) {
        return makeKey(null, targetClass, action, null);
    }

    public int makeKey(String targetClass) {
        return makeKey(null, targetClass, null, null);
    }        

    public int makeKey(String targetPkg, String targetClass, String action, String callerPkg) {
        int hash = HASH_CODE_NUM;
        if (targetPkg != null) {
            hash = (ODD_PRIME_NUMBER*hash) + targetPkg.hashCode();
        }
        if (targetClass != null) {
            hash = (ODD_PRIME_NUMBER*hash) + targetClass.hashCode();
        }
        if (action != null) {
            hash = (ODD_PRIME_NUMBER*hash) + action.hashCode();
        }
        if (callerPkg != null) {
            hash = (ODD_PRIME_NUMBER*hash) + callerPkg.hashCode();
        }

        return hash;
    }
    
    final class ServicePathData {
        public final String targetPkg;
        public final String targetClass;
        public final String action;        
        public final String callerPkg;
        public int interceptState;
        private final int key;

        public ServicePathData(
            String targetClass) {
            this(null, targetClass, null, null, PrizeInterLaunchingAppPolicy.INTERCEPT_POLICY_DEFAULT);
        }
        
        public ServicePathData(String targetClass, String action) {
            this(null, targetClass, action, null, PrizeInterLaunchingAppPolicy.INTERCEPT_POLICY_DEFAULT);
        }

        public ServicePathData(
            String targetPkg, String targetClass, String action) {
            this(targetPkg, targetClass, action, null, PrizeInterLaunchingAppPolicy.INTERCEPT_POLICY_DEFAULT);
        }
        
        public ServicePathData(
            String targetPkg, String targetClass, String action, String callerPkg) {
            this(targetPkg, targetClass, action, callerPkg, PrizeInterLaunchingAppPolicy.INTERCEPT_POLICY_DEFAULT);
        }

        public ServicePathData(
            String targetPkg, String targetClass, String action,
            String callerPkg, int state) {
            this.targetPkg = targetPkg;
            this.targetClass = targetClass;
            this.action = action;            
            this.callerPkg = callerPkg;
            this.interceptState = state;

            //Gen key
            this.key = makeKey();
            if(DBG_INFO) {
                logInfo("ServicePathData. key=0x" + Integer.toHexString(key));
            }
        }

        public int getKey() {
            return key;
        }

        public int makeKey() {
            return PrizeInterLaunchingPaths.this.makeKey(targetPkg, targetClass, action, callerPkg);
        }
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("ServicePathData { cn=");
            sb.append(targetPkg + "/" + targetClass);
            sb.append(", act=" + action);
            sb.append(", caller=" + callerPkg);
            sb.append(", interceptState=" + interceptState);
            sb.append(", key=" + key);
            return sb.toString();
        }
    }

    private final class BroadcastPathData {

    }    
}

