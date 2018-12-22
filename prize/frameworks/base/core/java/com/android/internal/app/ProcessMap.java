/*
 * Copyright (C) 2006 The Android Open Source Project
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

package com.android.internal.app;

import android.util.ArrayMap;
import android.util.SparseArray;

public class ProcessMap<E> {
    final ArrayMap<String, SparseArray<E>> mMap
            = new ArrayMap<String, SparseArray<E>>();
    
    public E get(String name, int uid) {
        SparseArray<E> uids = mMap.get(name);
        if (uids == null) return null;
        return uids.get(uid);
    }
    
    public E put(String name, int uid, E value) {
        SparseArray<E> uids = mMap.get(name);
        if (uids == null) {
            uids = new SparseArray<E>(2);
            mMap.put(name, uids);
        }
        uids.put(uid, value);
        return value;
    }
    
    public E remove(String name, int uid) {
        SparseArray<E> uids = mMap.get(name);
        if (uids != null) {
            final E old = uids.removeReturnOld(uid);
            if (uids.size() == 0) {
                mMap.remove(name);
            }
            return old;
        }
        return null;
    }
    
    public ArrayMap<String, SparseArray<E>> getMap() {
        return mMap;
    }

    public int size() {
        return mMap.size();
    }

    /* app multi instances feature. prize-linkh-20151026 */
    // the key of outer SparseArray is app instance index(appInst > 0).
    private SparseArray<ArrayMap<String, SparseArray<E>>> mMapForAppInstances = new SparseArray<ArrayMap<String, SparseArray<E>>>(2);        
    private final boolean mSupportAppInstances = com.mediatek.common.prizeoption.PrizeOption.PRIZE_APP_MULTI_INSTANCES;

    private int ensureValidAppInstanceIndex(int index) {
        if(index < 0) {
            index = 0;
        }

        return index;
    }
    
    public E get(int appInstanceIndex, String name, int uid) {
        if(mSupportAppInstances && appInstanceIndex > 0) {
            //appInstanceIndex = ensureValidAppInstanceIndex(appInstanceIndex);
            final ArrayMap<String, SparseArray<E>> map = mMapForAppInstances.get(appInstanceIndex);
            if(map == null) {
                return null;
            } else {
                SparseArray<E> uids = map.get(name);
                if (uids == null) return null;
                return uids.get(uid);
            }
        } else {
            return get(name, uid);
        }     
    }
    
    public E put(int appInstanceIndex, String name, int uid, E value) {
        if(mSupportAppInstances && appInstanceIndex > 0) {
             //appInstanceIndex = ensureValidAppInstanceIndex(appInstanceIndex);
             ArrayMap<String, SparseArray<E>> map = mMapForAppInstances.get(appInstanceIndex);
             if(map == null) {
                 map = new ArrayMap<String, SparseArray<E>>();
                 mMapForAppInstances.put(appInstanceIndex, map);               
             }
    
             SparseArray<E> uids = map.get(name);
             if (uids == null) {
                 uids = new SparseArray<E>(2);
                 map.put(name, uids);
             }
             uids.put(uid, value);
             return value;
        } else {
            return put(name, uid, value);
        }    
     }
     
     public E remove(int appInstanceIndex, String name, int uid) {
         if(mSupportAppInstances && appInstanceIndex > 0) {
             //appInstanceIndex = ensureValidAppInstanceIndex(appInstanceIndex);
             final ArrayMap<String, SparseArray<E>> map = mMapForAppInstances.get(appInstanceIndex);
             if(map != null) {
                 SparseArray<E> uids = map.get(name);
                 if (uids != null) {
                     final E old = uids.removeReturnOld(uid);
                     if (uids.size() == 0) {
                         map.remove(name);
                     }
                     return old;
                 }
             }
             
             return null;
         } else {
             return remove(name, uid);
         }    
     }

    /* Note: Not include orignal app data. */
    public int getAppInstancesCount() {
        return mMapForAppInstances.size();
    }
    
    public SparseArray<ArrayMap<String, SparseArray<E>>> getMapForAppInstances() {
        return mMapForAppInstances;
    }
    
    public ArrayMap<String, SparseArray<E>> getMap(int appInstanceIndex) {
        if(mSupportAppInstances && appInstanceIndex > 0) {
            //appInstanceIndex = ensureValidAppInstanceIndex(appInstanceIndex);
            ArrayMap<String, SparseArray<E>> map = mMapForAppInstances.get(appInstanceIndex);
            if(map == null) {
                //make sure the caller can get non-null map!!
                map = new ArrayMap<String, SparseArray<E>>();
                mMapForAppInstances.put(appInstanceIndex, map);
            }
            
            return map;
        } else {
            return getMap();
        }         
    }

    public int size(int appInstanceIndex) {
        if(mSupportAppInstances && appInstanceIndex > 0) {
            final ArrayMap<String, SparseArray<E>> map = mMapForAppInstances.get(appInstanceIndex);
            if(map == null) {
                return 0;
            }
            
            return map.size();
        } else {
            return mMap.size();
        }   
    }    
    /***************************** end **********************/
    
}
