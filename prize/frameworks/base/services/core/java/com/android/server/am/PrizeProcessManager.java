/**
 * Created by prize-linkh-20180316
 *
 */

package com.android.server.am;

import android.util.Slog;
import android.content.Context;
import android.util.PrizeGlobalTag;
import android.content.ComponentName;
import android.os.UserHandle;
/*-prize add by lihuangyuan,for whitelist -2017-05-06-start-*/
import android.os.WakeupItem;
/*-prize add by lihuangyuan,for whitelist -2017-05-06-end-*/

/**
 *
 *
 */
final class PrizeProcessManager {
    private static final String TAG = PrizeGlobalTag.PROC_MGR;
    static final Boolean DBG = false;
    // activity logs
    static final Boolean DBG_ACT = DBG && true;
    // broadcast logs
    static final Boolean DBG_BR = DBG && true;
    // service logs
    static final Boolean DBG_SR = DBG && true;
    // content provider logs
    static final Boolean DBG_PR = DBG && true;

    // Intercepted States.
    // State that indicates start request(activity/broadcast/service/provider) can be granted.
    public static final int STATE_GRANTED = 0;
    // State that indicates start request(activity/broadcast/service/provider) should be denied.
    public static final int STATE_DENIED = 1; 
    // State that indicates if the host process has already been running, then start request can 
    // be granted. If not, start request will be denied.
    public static final int STATE_DENIED_IF_NOT_RUNNING = 2;
    // State that indicates the actual component hasn't been figured out and is delayed to intercept until
    // it's found.
    //public static final int STATE_NOT_FOUND_COMPONENT = 3;
    public static final int STATE_MATCH_CLASS_START = 10;
    //state that match class head and defined
    public static final int STATE_MATCH_CLASS_DENIED = STATE_MATCH_CLASS_START+STATE_DENIED;
    //state that match class head and defined if not running
    public static final int STATE_MATCH_CLASS_DENIED_IF_NOT_RUNNING = STATE_DENIED_IF_NOT_RUNNING+STATE_DENIED;

    // default policy that intercepts starting activity request.
    public static final int DEFAULT_INTERCEPTED_ACT_POLICY = STATE_DENIED_IF_NOT_RUNNING;
    // default policy that intercepts sending broadcast request.
    public static final int DEFAULT_INTERCEPTED_BR_POLICY = STATE_DENIED_IF_NOT_RUNNING;
    // default policy that intercepts starting service request.
    public static final int DEFAULT_INTERCEPTED_SR_POLICY = STATE_DENIED_IF_NOT_RUNNING;
    // default policy that intercepts starting provider request.
    public static final int DEFAULT_INTERCEPTED_PR_POLICY = STATE_DENIED_IF_NOT_RUNNING;

    private static PrizeProcessManager mInstance;
    private final ActivityManagerService mAm;
    private final Context mContext;
    private final PrizeProcLaunchingPaths mProcLaunchingPaths;
    
    private PrizeProcessManager(ActivityManagerService am, Context context) {
        mAm = am;
        mContext = context;
        mProcLaunchingPaths = new PrizeProcLaunchingPaths(context);
        mProcLaunchingPaths.loadPathData();
        
        initAll();
    }
    /*-prize add by lihuangyuan,for whitelist -2017-05-06-start-*/
    public void loadProviderData(WakeupItem[] classactionary)
    {
    	mProcLaunchingPaths.loadProviderData(classactionary);
    }
    /*-prize add by lihuangyuan,for whitelist -2017-05-06-end-*/
    public static synchronized PrizeProcessManager getInstance(ActivityManagerService am, Context context) {
        if (mInstance == null) {
            mInstance = new PrizeProcessManager(am, context);
        }

        return mInstance;
    }

    private void initAll() {
        if (DBG) {
            Slog.d(TAG, "initAll()..");
        }
    }

    public int getStateForStartingProvider(int callingPid, int callingUid, ContentProviderRecord cpr) {
        if (DBG_PR) {
            Slog.d(PrizeGlobalTag.PROC_MGR_PR, "getStateForStartingProvider() callingPid=" + callingPid + ", callingUid=" + callingUid
                    + ", cpr=" + cpr);
        }

        if (cpr == null || cpr.name == null) {
            if (DBG_PR) {
                Slog.d(PrizeGlobalTag.PROC_MGR_PR, "getStateForStartingProvider() invalid record. Granted!");
            }
            return STATE_GRANTED;
        }
           
        // 1. Check the uid of starting provider.
        // If callingUid is same with it, then we let it to start.
        if (callingUid == cpr.uid) {
            if (DBG_PR) {
                Slog.d(PrizeGlobalTag.PROC_MGR_PR, "getStateForStartingProvider() Same uid. Granted!");
            }            
            return STATE_GRANTED;
        } else if (!UserHandle.isApp(callingUid)) {
            if (DBG_PR) {
                Slog.d(PrizeGlobalTag.PROC_MGR_PR, "getStateForStartingProvider() Calling uid isn't app id. Granted!");
            }
            return STATE_GRANTED;
        } else if (cpr.appInfo.isSystemApp() && (!cpr.appInfo.isPrebuiltThirdApp())) {
            if (DBG_PR) {
                Slog.d(PrizeGlobalTag.PROC_MGR_PR, "getStateForStartingProvider() target is system app. Granted!");
            }
            return STATE_GRANTED;
        }

        // 2. Check the component of starting provider.
        final ComponentName cn = cpr.name;
        
        // 2.1 Try to retrieve with a full component.
        PrizeProcLaunchingPaths.ProviderPath path = mProcLaunchingPaths.getProviderPath(
                    cn.getPackageName(), cn.getClassName());
        if (path != null) {
            if (DBG_PR) {
                Slog.d(PrizeGlobalTag.PROC_MGR_PR, "Found path with full component. path=" + path);
            }
            return path.state;
        }

        // 2.2 Full Component Not Matched. Fall back to retrieve with class name.
        path = mProcLaunchingPaths.getServicePathDataMatchClass(cn.getClassName());
        if (path != null) {
            if (DBG_PR) {
                Slog.d(PrizeGlobalTag.PROC_MGR_PR, "Found path with class name. path=" + path);
            }            
            return path.state;
        }
        path = mProcLaunchingPaths.getProviderPath(null, cn.getClassName());
        if (path != null) {
            if (DBG_PR) {
                Slog.d(PrizeGlobalTag.PROC_MGR_PR, "Found path with class name. path=" + path);
            }            
            return path.state;
        }

        if (DBG_PR) {
            Slog.d(PrizeGlobalTag.PROC_MGR_PR, "getStateForStartingProvider() Nothing found. Granted!");
        }
        return STATE_GRANTED;
    }

    public static String stateToString(int state) {
        String stateStr = null;
        switch(state) {
        case STATE_GRANTED:
            stateStr = "Granted";
            break;
        case STATE_DENIED:
            stateStr = "Denied";            
            break;
        case STATE_DENIED_IF_NOT_RUNNING:
            stateStr = "DeniedIfNotRunning";
            break;
        //case STATE_NOT_FOUND_COMPONENT:
          //  stateStr = "NotFoundCN";            
            //break;
        default:
            stateStr = "unknown**" + state;            
            break;
        }
        return stateStr;
    }    
}
