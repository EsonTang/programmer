package com.prize.smartcleaner;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.INetworkStatsService;
import android.net.INetworkStatsService.Stub;
import android.net.INetworkStatsSession;
import android.net.NetworkInfo;
import android.net.NetworkStats;
import android.net.NetworkStats.Entry;
import android.net.NetworkTemplate;
import android.net.TrafficStats;
import android.os.RemoteException;
import android.os.ServiceManager;
import com.prize.smartcleaner.utils.LogUtils;
import java.util.ArrayList;

/**
 * Created by xiarui on 2018/1/24.
 */
public class TrafficUtil {

    public static final String TAG = "TrafficUtil";

    public static NetworkStats getNetworkStatsInfo(Context context, int type) {
        INetworkStatsSession openSession = null;
        INetworkStatsService mNetworkStats = Stub.asInterface(ServiceManager.getService(Context.NETWORK_STATS_SERVICE));
        if (mNetworkStats == null) {
            return null;
        }
        LogUtils.d(TAG, "get network data!  ");
        try {
            mNetworkStats.forceUpdate();
            openSession = mNetworkStats.openSession();
            if (type == 0) {
                try {
                    NetworkStats summaryForAllUid;
                    NetworkTemplate buildTemplateWifi = NetworkTemplate.buildTemplateWifi();
                    long start = System.currentTimeMillis() - 1000;
                    long end = System.currentTimeMillis();
                    if (openSession != null) {
                        summaryForAllUid = openSession.getSummaryForAllUid(buildTemplateWifi, start, end, false);
                    } else {
                        summaryForAllUid = null;
                    }
                    if (openSession != null) {
                        TrafficStats.closeQuietly(openSession);
                    }
                    return summaryForAllUid;
                } catch (RemoteException e) {
                    if (openSession != null) {
                        TrafficStats.closeQuietly(openSession);
                    }
                    e.printStackTrace();
                    return null;

                }
            }
            LogUtils.d(TAG, "no wifi connected!");
            if (openSession != null) {
                TrafficStats.closeQuietly(openSession);
            }
            return null;
        } catch (RemoteException e) {
            if (openSession != null) {
                TrafficStats.closeQuietly(openSession);
            }
            return null;
        }
    }

    public static ArrayList<String> getUseNetPackages(Context context,
                                                      NetworkStats networkStats,
                                                      NetworkStats networkStats2,
                                                      int type,
                                                      int timeSlice) {
        if (networkStats == null || networkStats2 == null) {
            return null;
        }
        int flowLimit;
        ArrayList<String> resultList = new ArrayList();
        ArrayList<Integer> uidOfStats1 = new ArrayList();
        ArrayList<Long> capacityOfStats1 = new ArrayList();
        ArrayList<Integer> uidOfStats2 = new ArrayList();
        ArrayList<Long> capacityOfStats2 = new ArrayList();
        PackageManager packageManager = context.getPackageManager();
        if (type == 1) {
            flowLimit = 20;
        } else if (type == 0) {
            flowLimit = 50;
        } else {
            LogUtils.d(TAG, "wrong connection parameter!");
            return null;
        }
        int size = networkStats.size();
        int size2 = networkStats2.size();
        for (int i = 0; i < size; i++) {
            Entry entry = networkStats.getValues(i, null);
            long capacity = (entry.rxBytes / 1024) + (entry.txBytes / 1024);
            uidOfStats1.add(Integer.valueOf(entry.uid));
            capacityOfStats1.add(Long.valueOf(capacity));
        }
        for (int i = 0; i < uidOfStats1.size() - 1; i++) {
            int before = ((Integer) uidOfStats1.get(i)).intValue();
            int j = i + 1;
            while (j < uidOfStats1.size()) {
                if (before == ((Integer) uidOfStats1.get(j)).intValue()) {
                    capacityOfStats1.set(i, Long.valueOf(((Long) capacityOfStats1.get(i)).longValue() + ((Long) capacityOfStats1.get(j)).longValue()));
                    uidOfStats1.remove(j);
                    capacityOfStats1.remove(j);
                    j--;
                }
                j++;
            }
        }
        for (int i = 0; i < size2; i++) {
            Entry entry = networkStats2.getValues(i, null);
            long capacity = (entry.rxBytes / 1024) + (entry.txBytes / 1024);
            uidOfStats2.add(Integer.valueOf(entry.uid));
            capacityOfStats2.add(Long.valueOf(capacity));
        }
        for (int i = 0; i < uidOfStats2.size() - 1; i++) {
            int before = ((Integer) uidOfStats2.get(i)).intValue();
            int j = i + 1;
            while (j < uidOfStats2.size()) {
                if (before == ((Integer) uidOfStats2.get(j)).intValue()) {
                    capacityOfStats2.set(i, Long.valueOf(((Long) capacityOfStats2.get(i)).longValue() + ((Long) capacityOfStats2.get(j)).longValue()));
                    uidOfStats2.remove(j);
                    capacityOfStats2.remove(j);
                    j--;
                }
                j++;
            }
        }
        for (int i = 0; i < uidOfStats1.size(); i++) {
            int uid1 = ((Integer) uidOfStats1.get(i)).intValue();
            long capacity1 = ((Long) capacityOfStats1.get(i)).longValue();
            if (uid1 > 0 && uid1 != 1000) {
                int j = 0;
                while (j < uidOfStats2.size()) {
                    if (uid1 == ((Integer) uidOfStats2.get(j)).intValue()) {
                        long capacity5s = ((Long) capacityOfStats2.get(j)).longValue() - capacity1;
                        if (capacity5s > ((long) flowLimit)) {
                            String[] packagesForUid = packageManager.getPackagesForUid(uid1);
                            if (timeSlice != 0) {
                                LogUtils.d(TAG, "using network!  uid: " + uid1 + " data:("+ timeSlice + "s) " + capacity5s + "K");
                            } else {
                                LogUtils.d(TAG, "using network!  uid: " + uid1 + " data:(5s) " + capacity5s + "K");
                            }
                            if (packagesForUid != null) {
                                for (String uid : packagesForUid) {
                                    resultList.add(uid);
                                }
                            }
                        }
                    }
                    j++;

                }
            }
        }
        return resultList;
    }

    public static ArrayList<String> getUsingNetPackages(Context context, int timeSlice) {
        ArrayList<String> resultList;
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connMgr.getActiveNetworkInfo();
        if (activeNetworkInfo == null || !activeNetworkInfo.isAvailable()) {
            LogUtils.i(TAG, "network is unavailable !!");
            return null;
        } else {
            NetworkInfo.State state = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
            if (state == NetworkInfo.State.CONNECTED || state == NetworkInfo.State.CONNECTING) {
                LogUtils.d(TAG, "wifi connected!  ");
                NetworkStats before = getNetworkStatsInfo(context, 0);
                if (before == null) {
                    return null;
                }
                try {
                    if (timeSlice != 0) {
                        Thread.sleep(timeSlice * 1000);
                    } else {
                        Thread.sleep(5000);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                NetworkStats after = getNetworkStatsInfo(context, 0);
                if (after == null) {
                    return null;
                }
                resultList = getUseNetPackages(context, before, after, 0, timeSlice);
            } else {
                resultList = null;
            }
        }

        if (resultList != null) {
            int size = resultList.size();
            if (size == 0) {
                LogUtils.i(TAG, "no pkg use net !!");
            } else {
                for (int i = 0; i < size; i++) {
                    LogUtils.i(TAG, "in use net pkg : [ " + i + " ] " + resultList.get(i));
                }
            }
        } else {
            LogUtils.i(TAG, "no pkg use net !!");
        }
        return resultList;
    }
}