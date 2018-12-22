/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.server.am;

import android.content.ComponentName;
import android.os.Binder;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.Slog;
import android.util.SparseArray;
import com.android.internal.os.TransferPipe;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
/* app multi instances feature. prize-linkh-20151119 */
import com.mediatek.common.prizeoption.PrizeOption;
import android.util.PrizeAppInstanceUtils;
//end...

/**
 * Keeps track of content providers by authority (name) and class. It separates the mapping by
 * user and ones that are not user-specific (system providers).
 */
public final class ProviderMap {

    private static final String TAG = "ProviderMap";

    private static final boolean DBG = false;

    private final ActivityManagerService mAm;

    private final HashMap<String, ContentProviderRecord> mSingletonByName
            = new HashMap<String, ContentProviderRecord>();
    private final HashMap<ComponentName, ContentProviderRecord> mSingletonByClass
            = new HashMap<ComponentName, ContentProviderRecord>();

    private final SparseArray<HashMap<String, ContentProviderRecord>> mProvidersByNamePerUser
            = new SparseArray<HashMap<String, ContentProviderRecord>>();
    private final SparseArray<HashMap<ComponentName, ContentProviderRecord>> mProvidersByClassPerUser
            = new SparseArray<HashMap<ComponentName, ContentProviderRecord>>();

    ProviderMap(ActivityManagerService am) {
        mAm = am;
    }

    ContentProviderRecord getProviderByName(String name) {
        return getProviderByName(name, -1);
    }

    ContentProviderRecord getProviderByName(String name, int userId) {
        if (DBG) {
            Slog.i(TAG, "getProviderByName: " + name + " , callingUid = " + Binder.getCallingUid());
        }
        // Try to find it in the global list
        ContentProviderRecord record = mSingletonByName.get(name);
        if (record != null) {
            return record;
        }

        // Check the current user's list
        return getProvidersByName(userId).get(name);
    }

    ContentProviderRecord getProviderByClass(ComponentName name) {
        return getProviderByClass(name, -1);
    }

    ContentProviderRecord getProviderByClass(ComponentName name, int userId) {
        if (DBG) {
            Slog.i(TAG, "getProviderByClass: " + name + ", callingUid = " + Binder.getCallingUid());
        }
        // Try to find it in the global list
        ContentProviderRecord record = mSingletonByClass.get(name);
        if (record != null) {
            return record;
        }

        // Check the current user's list
        return getProvidersByClass(userId).get(name);
    }

    void putProviderByName(String name, ContentProviderRecord record) {
        if (DBG) {
            Slog.i(TAG, "putProviderByName: " + name + " , callingUid = " + Binder.getCallingUid()
                + ", record uid = " + record.appInfo.uid);
        }
        if (record.singleton) {
            mSingletonByName.put(name, record);
        } else {
            final int userId = UserHandle.getUserId(record.appInfo.uid);
            getProvidersByName(userId).put(name, record);
        }
    }

    void putProviderByClass(ComponentName name, ContentProviderRecord record) {
        if (DBG) {
            Slog.i(TAG, "putProviderByClass: " + name + " , callingUid = " + Binder.getCallingUid()
                + ", record uid = " + record.appInfo.uid);
        }
        if (record.singleton) {
            mSingletonByClass.put(name, record);
        } else {
            final int userId = UserHandle.getUserId(record.appInfo.uid);
            getProvidersByClass(userId).put(name, record);
        }
    }

    void removeProviderByName(String name, int userId) {
        if (mSingletonByName.containsKey(name)) {
            if (DBG)
                Slog.i(TAG, "Removing from globalByName name=" + name);
            mSingletonByName.remove(name);
        } else {
            if (userId < 0) throw new IllegalArgumentException("Bad user " + userId);
            if (DBG)
                Slog.i(TAG,
                        "Removing from providersByName name=" + name + " user=" + userId);
            HashMap<String, ContentProviderRecord> map = getProvidersByName(userId);
            // map returned by getProvidersByName wouldn't be null
            map.remove(name);
            if (map.size() == 0) {
                mProvidersByNamePerUser.remove(userId);
            }
        }
    }

    void removeProviderByClass(ComponentName name, int userId) {
        if (mSingletonByClass.containsKey(name)) {
            if (DBG)
                Slog.i(TAG, "Removing from globalByClass name=" + name);
            mSingletonByClass.remove(name);
        } else {
            if (userId < 0) throw new IllegalArgumentException("Bad user " + userId);
            if (DBG)
                Slog.i(TAG,
                        "Removing from providersByClass name=" + name + " user=" + userId);
            HashMap<ComponentName, ContentProviderRecord> map = getProvidersByClass(userId);
            // map returned by getProvidersByClass wouldn't be null
            map.remove(name);
            if (map.size() == 0) {
                mProvidersByClassPerUser.remove(userId);
            }
        }
    }

    private HashMap<String, ContentProviderRecord> getProvidersByName(int userId) {
        if (userId < 0) throw new IllegalArgumentException("Bad user " + userId);
        final HashMap<String, ContentProviderRecord> map = mProvidersByNamePerUser.get(userId);
        if (map == null) {
            HashMap<String, ContentProviderRecord> newMap = new HashMap<String, ContentProviderRecord>();
            mProvidersByNamePerUser.put(userId, newMap);
            return newMap;
        } else {
            return map;
        }
    }

    HashMap<ComponentName, ContentProviderRecord> getProvidersByClass(int userId) {
        if (userId < 0) throw new IllegalArgumentException("Bad user " + userId);
        final HashMap<ComponentName, ContentProviderRecord> map
                = mProvidersByClassPerUser.get(userId);
        if (map == null) {
            HashMap<ComponentName, ContentProviderRecord> newMap
                    = new HashMap<ComponentName, ContentProviderRecord>();
            mProvidersByClassPerUser.put(userId, newMap);
            return newMap;
        } else {
            return map;
        }
    }

    private boolean collectPackageProvidersLocked(String packageName,
            Set<String> filterByClasses, boolean doit, boolean evenPersistent,
            HashMap<ComponentName, ContentProviderRecord> providers,
            ArrayList<ContentProviderRecord> result) {
        boolean didSomething = false;
        for (ContentProviderRecord provider : providers.values()) {
            final boolean sameComponent = packageName == null
                    || (provider.info.packageName.equals(packageName)
                        && (filterByClasses == null
                            || filterByClasses.contains(provider.name.getClassName())));
            if (sameComponent
                    && (provider.proc == null || evenPersistent || !provider.proc.persistent)) {
                if (!doit) {
                    return true;
                }
                didSomething = true;
                result.add(provider);
            }
        }
        return didSomething;
    }

    boolean collectPackageProvidersLocked(String packageName, Set<String> filterByClasses,
            boolean doit, boolean evenPersistent, int userId,
            ArrayList<ContentProviderRecord> result, int appInst/* app multi instances feature. prize-linkh-20151225 */) {
        /* app multi instances feature. prize-linkh-20151119 */
        if(DBG_APP_INST) {
            mAm.logAppInstancesInfo(ActivityManagerService.LOG_CONTENT_TAG,
                "collectPackageProvidersLocked(): name=" + packageName + ", filterByClasses=" + filterByClasses +
                ", userId=" + userId + ", doit=" + doit + ", appInst=" + appInst + ", this=" + this);
        } //end...
        
        boolean didSomething = false;
        if (userId == UserHandle.USER_ALL || userId == UserHandle.USER_SYSTEM) {
            /* app multi instances feature. prize-linkh-20151225 */
            if(PrizeOption.PRIZE_APP_MULTI_INSTANCES) {
                //Warning: we don't remove the original data indepently.
                if(appInst == PrizeAppInstanceUtils.ALL_APP_INSTANCES) {
                    didSomething = collectPackageProvidersLocked(packageName, filterByClasses,
                            doit, evenPersistent, mSingletonByClass, result);
                }
            } else { //end...  
                didSomething = collectPackageProvidersLocked(packageName, filterByClasses,
                        doit, evenPersistent, mSingletonByClass, result);
            }
        }
        if (!doit && didSomething) {
            return true;
        }

        /* app multi instances feature. prize-linkh-20151119 */
        //this is a stoping package action. we need to add the data of other app instances that have the same package.
        if(PrizeOption.PRIZE_APP_MULTI_INSTANCES) {
            for(int i = 0, N = mSingletonByClassForAppInst.size(); i < N; ++i) {
                int aii = mSingletonByClassForAppInst.keyAt(i);
                if(appInst != PrizeAppInstanceUtils.ALL_APP_INSTANCES
                    && appInst != PrizeAppInstanceUtils.ALL_APP_INSTANCES_EXCEPT_ORIGINAL 
                      && appInst != aii) {
                    continue;
                }
                
                didSomething |= collectPackageProvidersLocked(packageName, filterByClasses, doit,
                    evenPersistent, mSingletonByClassForAppInst.valueAt(i), result);
                if(DBG_APP_INST) {
                    mAm.logAppInstancesInfo(ActivityManagerService.LOG_CONTENT_TAG,
                        "collectting for SingletonByClassForAppInst, didSomething=" + didSomething);
                }
            }
            
            if (!doit && didSomething) {
                return true;
            }            
        } //end...
        
        if (userId == UserHandle.USER_ALL) {
            for (int i = 0; i < mProvidersByClassPerUser.size(); i++) {
                /* app multi instances feature. prize-linkh-20151225 */
                if(PrizeOption.PRIZE_APP_MULTI_INSTANCES && appInst != PrizeAppInstanceUtils.ALL_APP_INSTANCES) {
                    break;
                } //end... 
                if (collectPackageProvidersLocked(packageName, filterByClasses,
                        doit, evenPersistent, mProvidersByClassPerUser.valueAt(i), result)) {
                    if (!doit) {
                        return true;
                    }
                    didSomething = true;
                }
            }

            /* app multi instances feature. prize-linkh-20151119 */
            //this is a stoping package action. we need to add the data of other app instances that have the same package.
            if(PrizeOption.PRIZE_APP_MULTI_INSTANCES) {
                for(int i = 0, N = mProvidersByClassPerUserForAppInst.size(); i < N; ++i) { // the key is app instance index.
                    int aii = mProvidersByClassPerUserForAppInst.keyAt(i);
                    if(appInst != PrizeAppInstanceUtils.ALL_APP_INSTANCES
                        && appInst != PrizeAppInstanceUtils.ALL_APP_INSTANCES_EXCEPT_ORIGINAL 
                          && appInst != aii) {
                        continue;
                    }
                    
                    SparseArray<HashMap<ComponentName, ContentProviderRecord>> array = mProvidersByClassPerUserForAppInst.valueAt(i);
                    for(int j = 0, P =  array.size(); j < P; ++j) { //the kye is user id.
                        if (collectPackageProvidersLocked(packageName, filterByClasses, doit, evenPersistent,
                                array.valueAt(j), result)) {
                            if (!doit) {
                                return true;
                            }
                            didSomething = true;
                        }
                    }
                }        
            } //end...               
        } else {
            HashMap<ComponentName, ContentProviderRecord> items
                    = getProvidersByClass(userId);
            /* app multi instances feature. prize-linkh-20151225 */
            if(PrizeOption.PRIZE_APP_MULTI_INSTANCES && appInst != PrizeAppInstanceUtils.ALL_APP_INSTANCES) {
                items = null;
            } //end...            
            if (items != null) {
                didSomething |= collectPackageProvidersLocked(packageName, filterByClasses,
                        doit, evenPersistent, items, result);
            }

            /* app multi instances feature. prize-linkh-20151119 */
            //this is a stoping package action. we need to add the data of other app instances that have the same package.
            if(PrizeOption.PRIZE_APP_MULTI_INSTANCES) {
                for(int i = 0, N = mProvidersByClassPerUserForAppInst.size(); i < N; ++i) { // the key is app instance index.
                    int aii = mProvidersByClassPerUserForAppInst.keyAt(i);
                    if(appInst != PrizeAppInstanceUtils.ALL_APP_INSTANCES
                        && appInst != PrizeAppInstanceUtils.ALL_APP_INSTANCES_EXCEPT_ORIGINAL 
                          && appInst != aii) {
                        continue;
                    }
                    
                    items = getProvidersByClass(userId, aii);
                    didSomething |= collectPackageProvidersLocked(packageName, filterByClasses, doit,
                            evenPersistent, items, result);
                }        
            } //end...              
        }
        return didSomething;
    }

    private boolean dumpProvidersByClassLocked(PrintWriter pw, boolean dumpAll, String dumpPackage,
            String header, boolean needSep, HashMap<ComponentName, ContentProviderRecord> map) {
        Iterator<Map.Entry<ComponentName, ContentProviderRecord>> it = map.entrySet().iterator();
        boolean written = false;
        while (it.hasNext()) {
            Map.Entry<ComponentName, ContentProviderRecord> e = it.next();
            ContentProviderRecord r = e.getValue();
            if (dumpPackage != null && !dumpPackage.equals(r.appInfo.packageName)) {
                continue;
            }
            if (needSep) {
                pw.println("");
                needSep = false;
            }
            if (header != null) {
                pw.println(header);
                header = null;
            }
            written = true;
            pw.print("  * ");
            pw.println(r);
            r.dump(pw, "    ", dumpAll);
        }
        return written;
    }

    private boolean dumpProvidersByNameLocked(PrintWriter pw, String dumpPackage,
            String header, boolean needSep, HashMap<String, ContentProviderRecord> map) {
        Iterator<Map.Entry<String, ContentProviderRecord>> it = map.entrySet().iterator();
        boolean written = false;
        while (it.hasNext()) {
            Map.Entry<String, ContentProviderRecord> e = it.next();
            ContentProviderRecord r = e.getValue();
            if (dumpPackage != null && !dumpPackage.equals(r.appInfo.packageName)) {
                continue;
            }
            if (needSep) {
                pw.println("");
                needSep = false;
            }
            if (header != null) {
                pw.println(header);
                header = null;
            }
            written = true;
            pw.print("  ");
            pw.print(e.getKey());
            pw.print(": ");
            pw.println(r.toShortString());
        }
        return written;
    }

    boolean dumpProvidersLocked(PrintWriter pw, boolean dumpAll, String dumpPackage) {
        boolean needSep = false;

        if (mSingletonByClass.size() > 0) {
            needSep |= dumpProvidersByClassLocked(pw, dumpAll, dumpPackage,
                    "  Published single-user content providers (by class):", needSep,
                    mSingletonByClass);
        }

        /* app multi instances feature. prize-linkh-20151217 */        
        if(ActivityManagerService.DUMP_APP_INST && mSingletonByClassForAppInst.size() > 0) {
            for (int i = 0; i < mSingletonByClassForAppInst.size(); i++) {
                HashMap<ComponentName, ContentProviderRecord> map = mSingletonByClassForAppInst.valueAt(i);
                needSep |= dumpProvidersByClassLocked(pw, dumpAll, dumpPackage,
                        "  Published single-user content providers (by class) for AppInst " + 
                        mSingletonByClassForAppInst.keyAt(i) + " :", needSep, map);
            }
        } //end...
        
        for (int i = 0; i < mProvidersByClassPerUser.size(); i++) {
            HashMap<ComponentName, ContentProviderRecord> map = mProvidersByClassPerUser.valueAt(i);
            needSep |= dumpProvidersByClassLocked(pw, dumpAll, dumpPackage,
                    "  Published user " + mProvidersByClassPerUser.keyAt(i)
                            + " content providers (by class):", needSep, map);
        }

        /* app multi instances feature. prize-linkh-20151217 */        
        if(ActivityManagerService.DUMP_APP_INST && mProvidersByClassPerUserForAppInst.size() > 0) {
            for (int i = 0; i < mProvidersByClassPerUserForAppInst.size(); i++) {
                SparseArray<HashMap<ComponentName, ContentProviderRecord>> outer = mProvidersByClassPerUserForAppInst.valueAt(i);
                for(int j = 0; j < outer.size(); ++j) {
                    HashMap<ComponentName, ContentProviderRecord> map = outer.valueAt(j);
                    needSep |= dumpProvidersByClassLocked(pw, dumpAll, dumpPackage,
                            "  Published user " + outer.keyAt(j)
                                    + " content providers (by class) for AppInst " +
                                    mProvidersByClassPerUserForAppInst.keyAt(i) + " :", needSep, map);
                }
            }
        } //end...
        
        if (dumpAll) {
            needSep |= dumpProvidersByNameLocked(pw, dumpPackage,
                    "  Single-user authority to provider mappings:", needSep, mSingletonByName);
            
            /* app multi instances feature. prize-linkh-20151217 */        
            if(ActivityManagerService.DUMP_APP_INST && mSingletonByNameForAppInst.size() > 0) {
                for(int i = 0; i < mSingletonByNameForAppInst.size(); ++i) {
                    needSep |= dumpProvidersByNameLocked(pw, dumpPackage,
                            "  Single-user authority to provider mappings for AppInst " +
                            mSingletonByNameForAppInst.keyAt(i) + " :", needSep, mSingletonByNameForAppInst.valueAt(i));
                }
            } //end...
            
            for (int i = 0; i < mProvidersByNamePerUser.size(); i++) {
                needSep |= dumpProvidersByNameLocked(pw, dumpPackage,
                        "  User " + mProvidersByNamePerUser.keyAt(i)
                                + " authority to provider mappings:", needSep,
                        mProvidersByNamePerUser.valueAt(i));
            }

            /* app multi instances feature. prize-linkh-20151217 */        
            if(ActivityManagerService.DUMP_APP_INST && mProvidersByNamePerUserForAppInst.size() > 0) {
                for (int i = 0; i < mProvidersByNamePerUserForAppInst.size(); i++) {
                    SparseArray<HashMap<String, ContentProviderRecord>> outer = mProvidersByNamePerUserForAppInst.valueAt(i);
                    for(int j = 0; j < outer.size(); ++j) {
                        HashMap<String, ContentProviderRecord> map = outer.valueAt(j);
                        needSep |= dumpProvidersByNameLocked(pw, dumpPackage,
                                "  User " + outer.keyAt(i)
                                    + " authority to provider mappings for AppInst " + mProvidersByNamePerUserForAppInst.keyAt(i) + " :", 
                                    needSep, map);
                    }
                }
            }  //end... 
            
        }
        return needSep;
    }

    protected boolean dumpProvider(FileDescriptor fd, PrintWriter pw, String name, String[] args,
            int opti, boolean dumpAll) {
        ArrayList<ContentProviderRecord> allProviders = new ArrayList<ContentProviderRecord>();
        ArrayList<ContentProviderRecord> providers = new ArrayList<ContentProviderRecord>();

        synchronized (mAm) {
            allProviders.addAll(mSingletonByClass.values());
            for (int i=0; i<mProvidersByClassPerUser.size(); i++) {
                allProviders.addAll(mProvidersByClassPerUser.valueAt(i).values());
            }

            /* app multi instances feature. prize-linkh-20151217 */        
            if(ActivityManagerService.DUMP_APP_INST) {
                for(int i = 0; i < mSingletonByClassForAppInst.size(); ++i) {
                    allProviders.addAll(mSingletonByClassForAppInst.valueAt(i).values());
                }

                for(int i = 0; i < mProvidersByClassPerUserForAppInst.size(); ++i) {
                    SparseArray<HashMap<ComponentName, ContentProviderRecord>> outer = mProvidersByClassPerUserForAppInst.valueAt(i);
                    for(int j = 0; j < outer.size(); ++j) {
                        allProviders.addAll(outer.valueAt(i).values());
                    }
                }
            } //end...
            
            if ("all".equals(name)) {
                providers.addAll(allProviders);
            } else {
                ComponentName componentName = name != null
                        ? ComponentName.unflattenFromString(name) : null;
                int objectId = 0;
                if (componentName == null) {
                    // Not a '/' separated full component name; maybe an object ID?
                    try {
                        objectId = Integer.parseInt(name, 16);
                        name = null;
                        componentName = null;
                    } catch (RuntimeException e) {
                    }
                }

                for (int i=0; i<allProviders.size(); i++) {
                    ContentProviderRecord r1 = allProviders.get(i);
                    if (componentName != null) {
                        if (r1.name.equals(componentName)) {
                            providers.add(r1);
                        }
                    } else if (name != null) {
                        if (r1.name.flattenToString().contains(name)) {
                            providers.add(r1);
                        }
                    } else if (System.identityHashCode(r1) == objectId) {
                        providers.add(r1);
                    }
                }
            }
        }

        if (providers.size() <= 0) {
            return false;
        }

        boolean needSep = false;
        for (int i=0; i<providers.size(); i++) {
            if (needSep) {
                pw.println();
            }
            needSep = true;
            dumpProvider("", fd, pw, providers.get(i), args, dumpAll);
        }
        return true;
    }

    /**
     * Invokes IApplicationThread.dumpProvider() on the thread of the specified provider if
     * there is a thread associated with the provider.
     */
    private void dumpProvider(String prefix, FileDescriptor fd, PrintWriter pw,
            final ContentProviderRecord r, String[] args, boolean dumpAll) {
        String innerPrefix = prefix + "  ";
        synchronized (mAm) {
            pw.print(prefix); pw.print("PROVIDER ");
                    pw.print(r);
                    pw.print(" pid=");
                    if (r.proc != null) pw.println(r.proc.pid);
                    else pw.println("(not running)");
            if (dumpAll) {
                r.dump(pw, innerPrefix, true);
            }
        }
        if (r.proc != null && r.proc.thread != null) {
            pw.println("    Client:");
            pw.flush();
            try {
                TransferPipe tp = new TransferPipe();
                try {
                    r.proc.thread.dumpProvider(
                            tp.getWriteFd().getFileDescriptor(), r.provider.asBinder(), args);
                    tp.setBufferPrefix("      ");
                    // Short timeout, since blocking here can
                    // deadlock with the application.
                    tp.go(fd, 2000);
                } finally {
                    tp.kill();
                }
            } catch (IOException ex) {
                pw.println("      Failure while dumping the provider: " + ex);
            } catch (RemoteException ex) {
                pw.println("      Got a RemoteException while dumping the service");
            }
        }
    }

    /* app multi instances feature. prize-linkh-20151119 */
    private final static boolean mSupportAppInstances = com.mediatek.common.prizeoption.PrizeOption.PRIZE_APP_MULTI_INSTANCES;
    
    //the key of sparse array is app instance index.
    private final SparseArray<HashMap<String, ContentProviderRecord>> mSingletonByNameForAppInst
            = new SparseArray<HashMap<String, ContentProviderRecord>>();
    //the key of sparse array is app instance index.
    private final SparseArray<HashMap<ComponentName, ContentProviderRecord>> mSingletonByClassForAppInst
            = new SparseArray<HashMap<ComponentName, ContentProviderRecord>>();
    
    //the key of outer sparse array is app instance index, that of inner is user id.(appInst > 0)
    private final SparseArray<SparseArray<HashMap<String, ContentProviderRecord>>> mProvidersByNamePerUserForAppInst
            = new SparseArray<SparseArray<HashMap<String, ContentProviderRecord>>>();    
    //the key of outer sparse array is app instance index, that of inner's is user id.(appInst > 0)
    private final SparseArray<SparseArray<HashMap<ComponentName, ContentProviderRecord>>> mProvidersByClassPerUserForAppInst
            = new SparseArray<SparseArray<HashMap<ComponentName, ContentProviderRecord>>>();
    private final static boolean DBG_APP_INST = ActivityManagerService.ENABLE_LOG_CONTENT;
    
    ContentProviderRecord getProviderByName(int appInst, String name) {
        return getProviderByName(name, -1, appInst);
    }    
    
    ContentProviderRecord getProviderByName(String name, int userId, int appInst) {
        if(DBG_APP_INST) {
            mAm.logAppInstancesInfo(ActivityManagerService.LOG_CONTENT_TAG,
                "getProviderByName: " + name + ", callingUid = " + Binder.getCallingUid() + ", appInst=" + appInst);
        }
        
        if(mSupportAppInstances && appInst > 0) {            
            // Try to find it in the global list
            HashMap<String, ContentProviderRecord> map = mSingletonByNameForAppInst.get(appInst);
            if (map != null && map.get(name) != null) {
                return map.get(name);
            }
            
            // Check the current user's list
            return getProvidersByName(userId, appInst).get(name);
        }

        return getProviderByName(name, userId);

    }

    ContentProviderRecord getProviderByClass(int appInst, ComponentName name) {
        return getProviderByClass(name, -1, appInst);
    }
    
    ContentProviderRecord getProviderByClass(ComponentName name, int userId, int appInst) {
        if(DBG_APP_INST) {
            mAm.logAppInstancesInfo(ActivityManagerService.LOG_CONTENT_TAG,
                "getProviderByClass: " + name + ", callingUid = " + Binder.getCallingUid() + ", appInst=" + appInst);
        }


        if(mSupportAppInstances && appInst > 0) {
            // Try to find it in the global list
            HashMap<ComponentName, ContentProviderRecord> map = mSingletonByClassForAppInst.get(appInst);
            if (map != null && map.get(name) != null) {
                return map.get(name);
            }
            
            // Check the current user's list
            return getProvidersByClass(userId, appInst).get(name);
        }
        
        return getProviderByClass(name, userId);
    }

    void putProviderByName(String name, ContentProviderRecord record, int appInst) {
        if (DBG_APP_INST) {
            mAm.logAppInstancesInfo(ActivityManagerService.LOG_CONTENT_TAG, 
                "putProviderByName: " + name + " , callingUid = " + Binder.getCallingUid()
                + ", record uid = " + record.appInfo.uid + ", appInst=" + appInst);
        }
        
        if(mSupportAppInstances && appInst > 0) {
            if (record.singleton) {
                HashMap<String, ContentProviderRecord> map = mSingletonByNameForAppInst.get(appInst);
                if(map == null) {
                    map = new HashMap<String, ContentProviderRecord>();
                    mSingletonByNameForAppInst.put(appInst, map);
                }
                
                map.put(name, record);
            } else {
                final int userId = UserHandle.getUserId(record.appInfo.uid);
                getProvidersByName(userId, appInst).put(name, record);
            }

            return;
        }        

        putProviderByName(name, record);
    }

    void putProviderByClass(ComponentName name, ContentProviderRecord record, int appInst) {
        if (DBG_APP_INST) {
            mAm.logAppInstancesInfo(ActivityManagerService.LOG_CONTENT_TAG, 
                "putProviderByClass: " + name + " , callingUid = " + Binder.getCallingUid()
                + ", record uid = " + record.appInfo.uid + ", appInst=" + appInst);
        }


        if(mSupportAppInstances && appInst > 0) {
            if (record.singleton) {
                HashMap<ComponentName, ContentProviderRecord> map = mSingletonByClassForAppInst.get(appInst);
                if(map == null) {
                    map = new HashMap<ComponentName, ContentProviderRecord>();
                    mSingletonByClassForAppInst.put(appInst, map);
                }
                
                map.put(name, record);
            } else {
                final int userId = UserHandle.getUserId(record.appInfo.uid);
                getProvidersByClass(userId, appInst).put(name, record);
            }

            return;
        }        

        putProviderByClass(name, record);
    }

    void removeProviderByName(String name, int userId, int appInst) {
        if(mSupportAppInstances && appInst > 0) {
            HashMap<String, ContentProviderRecord> singletonMap = mSingletonByNameForAppInst.get(appInst);
            if (singletonMap != null && singletonMap.containsKey(name)) {
                if (DBG_APP_INST) {
                    mAm.logAppInstancesInfo(ActivityManagerService.LOG_CONTENT_TAG, 
                        "Removing from globalByName name=" + name + ", appInst=" + appInst);
                }
                singletonMap.remove(name);
                if(singletonMap.size() == 0) {
                    mSingletonByNameForAppInst.remove(appInst);
                }
            } else {
                if (userId < 0) throw new IllegalArgumentException("Bad user " + userId);
                if (DBG_APP_INST) {
                    mAm.logAppInstancesInfo(ActivityManagerService.LOG_CONTENT_TAG, 
                            "Removing from providersByName name=" + name + " user=" + userId + ", appInst=" + appInst);
                }
                HashMap<String, ContentProviderRecord> map = getProvidersByName(userId, appInst);
                // map returned by getProvidersByName wouldn't be null
                map.remove(name);
                if (map.size() == 0) {
                    mProvidersByNamePerUserForAppInst.get(appInst).remove(userId);
                    mProvidersByNamePerUserForAppInst.remove(appInst);
                }
            }

            return;
        }     

        removeProviderByName(name, userId);
    }

    void removeProviderByClass(ComponentName name, int userId, int appInst) {
        if(mSupportAppInstances && appInst > 0) {
            HashMap<ComponentName, ContentProviderRecord> singletonMap = mSingletonByClassForAppInst.get(appInst);
            if (singletonMap != null && singletonMap.containsKey(name)) {
                if (DBG_APP_INST) {
                    mAm.logAppInstancesInfo(ActivityManagerService.LOG_CONTENT_TAG, 
                        "Removing from globalByClass name=" + name + ", appInst=" + appInst);
                }

                singletonMap.remove(name);
                if(singletonMap.size() == 0) {
                    mSingletonByClassForAppInst.remove(appInst);
                }
            } else {
                if (userId < 0) throw new IllegalArgumentException("Bad user " + userId);
                if (DBG_APP_INST) {
                    mAm.logAppInstancesInfo(ActivityManagerService.LOG_CONTENT_TAG, 
                            "Removing from providersByClass name=" + name + " user=" + userId + ", appInst=" + appInst);
                }

                HashMap<ComponentName, ContentProviderRecord> map = getProvidersByClass(userId, appInst);
                // map returned by getProvidersByName wouldn't be null
                map.remove(name);
                if (map.size() == 0) {
                    mProvidersByClassPerUserForAppInst.get(appInst).remove(userId);
                    mProvidersByClassPerUserForAppInst.remove(appInst);
                }
            }

            return;
        }        

        removeProviderByClass(name, userId);
    }

    private HashMap<String, ContentProviderRecord> getProvidersByName(int userId, int appInst) {
        if (userId < 0) throw new IllegalArgumentException("Bad user " + userId);
        if (appInst <= 0) throw new IllegalArgumentException("invalid app instance index " + appInst);
        
        SparseArray<HashMap<String, ContentProviderRecord>> array = mProvidersByNamePerUserForAppInst.get(appInst);
        if(array == null) {
            array = new SparseArray<HashMap<String, ContentProviderRecord>>();
            mProvidersByNamePerUserForAppInst.put(appInst, array);
        }

        HashMap<String, ContentProviderRecord> map = array.get(userId);
        if (map == null) {
            map = new HashMap<String, ContentProviderRecord>();
            array.put(userId, map);
        }

        return map;
    }

    private HashMap<ComponentName, ContentProviderRecord> getProvidersByClass(int userId, int appInst) {       
        if (userId < 0) throw new IllegalArgumentException("Bad user " + userId);
        if (appInst <= 0) throw new IllegalArgumentException("invalid app instance index " + appInst);

        SparseArray<HashMap<ComponentName, ContentProviderRecord>> array = mProvidersByClassPerUserForAppInst.get(appInst);
        if(array == null) {
            array = new SparseArray<HashMap<ComponentName, ContentProviderRecord>>();
            mProvidersByClassPerUserForAppInst.put(appInst, array);
        }

        HashMap<ComponentName, ContentProviderRecord> map = array.get(userId);
        if (map == null) {
            map = new HashMap<ComponentName, ContentProviderRecord>();
            array.put(userId, map);
        }

        return map;
    }    
    //end............
}
