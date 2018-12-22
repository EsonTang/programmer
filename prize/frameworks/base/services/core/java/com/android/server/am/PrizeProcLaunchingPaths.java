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
import android.util.PrizeGlobalTag;
/*-prize add by lihuangyuan,for whitelist -2017-05-06-start-*/
import android.os.WakeupItem;
import java.util.ArrayList;
/*-prize add by lihuangyuan,for whitelist -2017-05-06-end-*/

final class PrizeProcLaunchingPaths {
    private static final String TAG = PrizeGlobalTag.PROC_MGR_PR + "/path";

    private static final Boolean DBG = PrizeProcessManager.DBG;
    // activity logs
    private static final Boolean DBG_ACT = DBG && PrizeProcessManager.DBG_ACT;
    // broadcast logs
    private static final Boolean DBG_BR = DBG && PrizeProcessManager.DBG_BR;
    // service logs
    private static final Boolean DBG_SR = DBG && PrizeProcessManager.DBG_SR;
    // content provider logs
    private static final Boolean DBG_PR = DBG && PrizeProcessManager.DBG_PR;

    private static final int ODD_PRIME_NUMBER = 37;        
    private static final int HASH_CODE_NUM = 23;
    
    private Context mContext;
    
    private final ArrayMap<Integer, ProviderPath> mProviderPathsMap = 
        new ArrayMap<Integer, ProviderPath>();
    private final ArrayList<ProviderPath> mMatchClassHeadPathDataAry = 
        new ArrayList<ProviderPath>();

    public PrizeProcLaunchingPaths(Context context) {
        mContext = context;
    }
    /*-prize add by lihuangyuan,for whitelist -2017-05-06-start-*/
    public void loadProviderData(WakeupItem[] classactionary)
    {
    	    if(classactionary == null)return;
	    Slog.d("whitelist","before loadProviderData mapsieze:"+mProviderPathsMap.size());
	    synchronized (mProviderPathsMap) 
	    {
		mProviderPathsMap.erase();
              mMatchClassHeadPathDataAry.clear();
	    }
    	    for(int i=0;i<classactionary.length;i++)
    	    {
    	        WakeupItem wakeupitem = classactionary[i];
		 if(wakeupitem == null )continue;             

		synchronized (mProviderPathsMap) 
		{
		       ProviderPath path = new ProviderPath(wakeupitem.callerpkg, wakeupitem.targetPkg,wakeupitem.classname, wakeupitem.state) ;
                     if((path.state == PrizeProcessManager.STATE_MATCH_CLASS_DENIED
                        ||path.state == PrizeProcessManager.STATE_MATCH_CLASS_DENIED_IF_NOT_RUNNING)
                        && path.targetClass != null)
                    {
                        path.state -= PrizeProcessManager.STATE_MATCH_CLASS_START;
                        mMatchClassHeadPathDataAry.add(path);
                    }                    
                    else
                    {
			    mProviderPathsMap.put(path.hashKey, path);
                    }
		}						
		Slog.d("whitelist","put providerpathsdata pkg:"+wakeupitem.targetPkg
			+"/"+wakeupitem.classname
			//+"/"+wakeupitem.action
			+"/"+wakeupitem.callerpkg
			+"/"+wakeupitem.state);
    	    }	
	    Slog.d("whitelist","after loadProviderData mapsieze:"+mProviderPathsMap.size());
    }
    /*-prize add by lihuangyuan,for whitelist -2017-05-06-end-*/
    public void loadPathData() {
        if (DBG) {
            Slog.d(TAG, "loadPathData()...");
        }
        loadProviderPathData();
    }

    public void clearPathData() {
        if (DBG) {
            Slog.d(TAG, "clearPathData()...");
        }
        clearProviderPathData();
    }

    public void loadProviderPathData() {
        if (DBG_PR) {
            Slog.d(TAG, "loadProviderPathData()...");
        }

        ProviderPath path = new ProviderPath("com.tencent.android.tpush.XGPushProvider");
        mProviderPathsMap.put(path.hashKey, path);
        
        path = new ProviderPath("com.tencent.mid.api.MidProvider");
        mProviderPathsMap.put(path.hashKey, path);
    }

    public void clearProviderPathData() {
        if (DBG_PR) {
            Slog.d(TAG, "clearProviderPathData()...");
        }
        mProviderPathsMap.clear();
    }

    public ArrayMap<Integer, ProviderPath> getProviderPathMap() {
        return mProviderPathsMap;
    }

    public static int makeKey(String targetClass) {
        return makeKey(null, null, targetClass);
    }        

    public static int makeKey(ComponentName targetCN) {
        return makeKey(null, targetCN.getPackageName(), targetCN.getClassName());
    } 

    public static int makeKey(String callerPkg, String targetPkg, String targetClass) {
        int hash = HASH_CODE_NUM;
        if (callerPkg != null) {
            hash = (ODD_PRIME_NUMBER*hash) + callerPkg.hashCode();
        }
        if (targetPkg != null) {
            hash = (ODD_PRIME_NUMBER*hash) + targetPkg.hashCode();
        }
        if (targetClass != null) {
            hash = (ODD_PRIME_NUMBER*hash) + targetClass.hashCode();
        }

        if (DBG) {
            StringBuilder sb = new StringBuilder();
            sb.append("makeKey() callerPkg=").append(callerPkg)
              .append(", targetPkg=").append(targetPkg)
              .append(", targetClass=").append(targetClass)
              .append(", hash=0x").append(Integer.toHexString(hash));
            Slog.d(TAG, sb.toString());
        }
        
        return hash;
    }    

    public ProviderPath getProviderPath(String targetPkg, String targetClass) {
        return getProviderPath(null, targetPkg, targetClass);
    }

    public ProviderPath getProviderPath(String callerPkg, String targetPkg, String targetClass) {
        int key = makeKey(callerPkg, targetPkg, targetClass);
        ProviderPath path = mProviderPathsMap.get(key);
        if (DBG_PR) {
            Slog.d(TAG, "getProviderPath() callerPkg=" + callerPkg + ", targetPkg=" + targetPkg
                    + ", targetClass=" + targetClass + ", path=" + path);
        }
        return path;
    }
    public ProviderPath getServicePathDataMatchClass(String targetClass)
    {
       synchronized (mProviderPathsMap) 
	{
            for(int i=0;i<mMatchClassHeadPathDataAry.size();i++)
            {
                ProviderPath item = mMatchClassHeadPathDataAry.get(i);
                if(targetClass.startsWith(item.targetClass))
                {
                    return item;
                }
            }
       }
       return null;
    }
    /*
    final class ActivityPath extends Path {

    }

    final class BroadcastPath extends Path {

    }

    final class ServicePath extends Path {

    }
    */

    final class ProviderPath extends Path {
        public ProviderPath(String targetClass) {
            this(targetClass, PrizeProcessManager.STATE_DENIED_IF_NOT_RUNNING);
        }
            
        public ProviderPath(String targetClass, int state) {
            this(null, null, targetClass, state);
        }

        public ProviderPath(ComponentName targetCN, int state) {
            this(null, targetCN.getPackageName(), targetCN.getClassName(), state);
        }

        public ProviderPath(String callerPkg, String targetPkg, String targetClass, int state) {
            super(callerPkg, targetPkg, targetClass, state);
        }

        @Override
        public int genKey() {
            return makeKey(callerPkg, targetPkg, targetClass);
        }

        @Override
        public String toString() {
            return "ProviderPath{" + callerPkg + "|" + targetPkg + "|" + targetClass
                     + "|" + state + "|0x" + Integer.toHexString(hashKey) + "}";
        }
    }

    abstract class Path {
        final String callerPkg;
        final String targetPkg;        
        final String targetClass;
        final int hashKey;
        int state;

        public Path(String targetClass, int state) {
            this(null, null, targetClass, state);
        }

        public Path(ComponentName targetCN, int state) {
            this(null, targetCN.getPackageName(), targetCN.getClassName(), state);
        }

        public Path(String callerPkg, String targetPkg, String targetClass, int state) {
            this.callerPkg = callerPkg;
            this.targetPkg = targetPkg;
            this.targetClass = targetClass;
            this.state = state;
            this.hashKey = genKey();
        }

        abstract int genKey();
    }
}

