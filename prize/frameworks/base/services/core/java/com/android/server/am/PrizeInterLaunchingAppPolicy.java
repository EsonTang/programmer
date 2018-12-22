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
import com.android.server.am.PrizeInterLaunchingPaths.ServicePathData;
/*-prize add by lihuangyuan,for whitelist -2017-05-06-start-*/
import android.os.WakeupItem;
/*-prize add by lihuangyuan,for whitelist -2017-05-06-end-*/
import android.util.PrizeGlobalTag;
import android.app.AppGlobals;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;


final class PrizeInterLaunchingAppPolicy {
    private static final String TAG = PrizeGlobalTag.PROC_MGR_SR + "/policy"; //"PrizeInterLaunchingAppPolicy";    
    private static final boolean DBG_INFO = ActivityManagerService.DBG_INTER_LAUNCHING_APPS;
    
    private Context mContext;
    private PrizeInterLaunchingPaths mInterLaunchingPaths;

    //State that indicates we disallow to launch/send service/broadcast be launching/sending
    public static final int STATE_DISALLOW = 0;    
    //State that indicates we allow to launch/send service/broadcast be launching/sending
    public static final int STATE_ALLOW = 1;
    //State that indicates we allow to launch/send service/broadcast be launching/sending if its process has already running.    
    public static final int STATE_MAY_DISALLOW = 2;
    //State that indicates we can't find the component to detemine whether this service/broadcast can start/send.
    public static final int STATE_NOT_FOUND_COMPONENT = 3;

    public static final int STATE_MATCH_START = 10;
    //state that match class head and may disallow
    //for example,com.tencent.android.tpush.service match com.tencent.android.tpush.servicexxx
    public static final int STATE_MATCH_CLASS_MAY_DISALLOW = STATE_MATCH_START+STATE_MAY_DISALLOW;
    //state that match class head and  disallow
    //for example,com.tencent.android.tpush.service match com.tencent.android.tpush.servicexxx
    public static final int STATE_MATCH_CLASS_DISALLOW = STATE_MATCH_START+STATE_DISALLOW;

    public static final int INTERCEPT_POLICY_DEFAULT = PrizeInterLaunchingAppPolicy.STATE_DISALLOW;

    public PrizeInterLaunchingAppPolicy(Context context) {
        logInfo("contruct PrizeInterLaunchingAppPolicy...");

        mContext = context;
        mInterLaunchingPaths = new PrizeInterLaunchingPaths(context);
    }

    private static void logInfo(String msg) {
        Slog.d(TAG, msg);
    }
     /*-prize add by lihuangyuan,for whitelist -2017-05-06-start-*/
    public void loadData(WakeupItem[] classactionary)
    {
    	mInterLaunchingPaths.loadData(classactionary);
    }
    /*-prize add by lihuangyuan,for whitelist -2017-05-06-end-*/
    public int getStateForStartingService(IApplicationThread caller,
            Intent service, String resolvedType, int userId,boolean isscreenon) {
        if(DBG_INFO) {
            logInfo("getStateForStartingService: caller=" + caller 
                + ", s="+ service + ", type=" + resolvedType + ", userId=" + userId);
        }
        
        int state = STATE_ALLOW;
        if(service == null) {
            if(DBG_INFO) {
                logInfo("getStateForStartingService: Null intent. Always allow starting!");
            }            
            return STATE_ALLOW;
        } else if(service.getComponent() == null) {
            if(DBG_INFO) {
                logInfo("getStateForStartingService: Null component. Maybe disallow starting!");
            }            
            return STATE_NOT_FOUND_COMPONENT;
        }

        String action = service.getAction();
        ComponentName cn = service.getComponent();
        ServicePathData spd = null;

        //add by lihuangyuan,for account sync managed-2018-07-12-start
        if(action != null && action.equals("android.content.SyncAdapter"))
        {
            if(isscreenon)
            {
                return STATE_ALLOW;
            }
            else
            {
                if(cn != null)
                {
                    try
                    {
                        ApplicationInfo ai =  AppGlobals.getPackageManager().getApplicationInfo(cn.getPackageName(), PackageManager.GET_SHARED_LIBRARY_FILES, userId);
                        if(ai != null && ai.isSystemApp())
                        {
                            return STATE_ALLOW;
                        }                        
                     }
                    catch(Exception ex){}
                }
                Slog.d(TAG,"isscreenon:"+isscreenon+" block action:"+action+",cn:"+(cn==null?cn:cn.toString()));
                return STATE_DISALLOW;
            }
        }
        //add by lihuangyuan,for account sync managed-2018-07-12-end
        // 1.  pkg + class + action combination
        spd = mInterLaunchingPaths.getServicePathData(cn, action);
        // 2. class + action combination
        if(spd == null) {
            spd = mInterLaunchingPaths.getServicePathData(cn.getClassName(), action);
        }
        // 3. class
        if(spd == null) {
            //find match first
            spd = mInterLaunchingPaths.getServicePathDataMatchClass(cn.getClassName());
            if(spd == null)
            {
                spd = mInterLaunchingPaths.getServicePathData(cn.getClassName());
            }
        }
        // 4. action
        if(spd == null) {
            spd = mInterLaunchingPaths.getServicePathData(null, null, action, null);
        }
        
        if(spd != null) {
            state = spd.interceptState;
        }

        if(DBG_INFO) {
            logInfo("getStateForStartingService: spd=" + spd);
        }

        //special case.         
        if(spd == null) {
            // 1. add for 360 apps.
            if(action != null && action.endsWith(".QihooAlliance")) {
                if(DBG_INFO) {
                    logInfo("getStateForStartingService: special action " + action + "! Maybe disallow starting!");
                }
                state = STATE_MAY_DISALLOW;
            }
        }
        
        return state;

    }
    
    public int getStateForBindingService(IApplicationThread caller, IBinder token,
            Intent service, String resolvedType, int userId,boolean isscreenon) {
        if(DBG_INFO) {
            logInfo("getStateForBindingService: caller=" + caller + ", token=" + token
                + ", s="+ service + ", type=" + resolvedType + ", userId=" + userId);
        }
        
        int state = STATE_ALLOW;
        if(service == null) {
            if(DBG_INFO) {
                logInfo("getStateForBindingService: Null intent. Always allow starting!");
            }
            return STATE_ALLOW;
        } else if(service.getComponent() == null) {
            if(DBG_INFO) {
                logInfo("getStateForBindingService: Null component. Maybe disallow starting!");
            }
            return STATE_NOT_FOUND_COMPONENT;
        }


        String action = service.getAction();
        ComponentName cn = service.getComponent();
        ServicePathData spd = null;

        //add by lihuangyuan,for account sync managed-2018-07-12-start
        if(action != null && (action.equals("android.content.SyncAdapter")
            || action.equals("android.service.notification.NotificationListenerService")))
        {
            if(isscreenon)
            {
                return STATE_ALLOW;
            }
            else
            {
                if(cn != null)
                {
                    try
                    {
                        ApplicationInfo ai =  AppGlobals.getPackageManager().getApplicationInfo(cn.getPackageName(), PackageManager.GET_SHARED_LIBRARY_FILES, userId);
                        if(ai != null && ai.isSystemApp())
                        {
                            return STATE_ALLOW;
                        }                        
                     }
                    catch(Exception ex){}
                }
                return STATE_DISALLOW;
            }
        }
        //add by lihuangyuan,for account sync managed-2018-07-12-end
        
        spd = mInterLaunchingPaths.getServicePathData(cn, action);
        if(spd == null) {
            spd = mInterLaunchingPaths.getServicePathData(cn.getClassName(), action);
        }
        if(spd == null) {
            //find match first            
            spd = mInterLaunchingPaths.getServicePathDataMatchClass(cn.getClassName());
            if(spd == null)
            {
                spd = mInterLaunchingPaths.getServicePathData(cn.getClassName());
            }
        }

        if(spd != null) {
            state = spd.interceptState;
        }

        if(DBG_INFO) {
            logInfo("getStateForBindingService: spd=" + spd);
        }        
        return state;

    }
    
    public int getStateForSendBroadcast() {
        return STATE_ALLOW;
    }

    public int getStateForStartContentProvider() {
        return STATE_ALLOW;
    }

    public static String stateToString(int state) {
        String stateStr = null;
        switch(state) {
        case STATE_DISALLOW:
            stateStr = "Disallow";
            break;
        case STATE_ALLOW:
            stateStr = "Allow";            
            break;
        case STATE_MAY_DISALLOW:
            stateStr = "MayDisallow";            
            break;
        case STATE_NOT_FOUND_COMPONENT:
            stateStr = "NotFoundComponent";            
            break;
        default:
            stateStr = "unknown**" + state;            
            break;
        }
        return stateStr;
    }
}

