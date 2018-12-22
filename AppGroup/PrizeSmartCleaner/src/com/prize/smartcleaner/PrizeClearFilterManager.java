package com.prize.smartcleaner;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.util.Xml;

import com.prize.smartcleaner.utils.LogUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import com.prize.smartcleaner.utils.PrizeClearUtil;
import com.prize.smartcleaner.bean.ServiceInfo;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 * Created by xiarui on 2018/1/16.
 */

public class PrizeClearFilterManager {

    public static final String TAG = "PrizeClearFilterManager";

    private static final String LABEL_PROCESS = "ProcessFilterList";
    private static final String LABEL_PACKAGE = "PackageFilterList";
    private static final String LABEL_DEEP_CLEAR = "DeepClearProcessFilterList";
    private static final String LABEL_LEARN_CLEAR = "LearnClearPkgFilterList";
    private static final String LABEL_ONLY_KILL_PACKAGE = "OnlyBeKillPkgList";
    private static final String LABEL_SERVICE = "ProtectServiceInfo";
    private static final String LABEL_THIRD = "ThirdAppFilterList";
    private static final String LABEL_FORCESTOP = "ForceStopFilterList";
    private static final String LABEL_REGION_PROCESS = "RegionProcessInfoExp";
    private static final String LABEL_REGION_PACKAGE = "RegionPackagesInfoExp";

    private static final String PREF_UPDATE_FINISHED = "update_finished";
    private static final String PREF_NAME_PROCESS = "preference_process_filter_list";
    private static final String PREF_NAME_PACKAGE = "preference_package_filter_list";
    private static final String PREF_NAME_DEEP_CLEAR = "preference_deep_clear_process_list";
    private static final String PREF_NAME_LEARN_CLEAR = "preference_learn_clear_pkg_list";
    private static final String PREF_NAME_ONLY_KILL_PACKAGE = "preference_only_kill_pkg_list";
    private static final String PREF_NAME_SERVICE = "preference_service_filter_list";
    private static final String PREF_NAME_THIRD = "preference_third_filter_list";
    private static final String PREF_NAME_FORCESTOP = "preference_forcestop_filter_list";
    private static final String PREF_NAME_REGION_PROCESS = "preference_region_process_list_exp";
    private static final String PREF_NAME_REGION_PACKAGE = "preference_region_packages_list_exp";

    public static final String PATH = "/data/system/recenttask/";
    public static final String FILTER_LIST = "sys_clear_appfilter_list.xml";

    public boolean isExp = false;
    private static PrizeClearFilterManager mClearFilterMgr = null;
    private static ArrayList<ServiceInfo> mServiceList = null;

    public static synchronized PrizeClearFilterManager getInstance() {
        synchronized (PrizeClearFilterManager.class) {
            if (mClearFilterMgr == null) {
                mClearFilterMgr = new PrizeClearFilterManager();
            }
        }
        return mClearFilterMgr;
    }

    private FilterList getFilterListFormXml(File file) {
        FileInputStream fileInputStream = null;
        FilterList filterList = new FilterList();
        ArrayList<String> processFilterList = new ArrayList();
        ArrayList<String> packageFilterList = new ArrayList();
        ArrayList<String> deepClearProcessFilterList = new ArrayList();
        ArrayList<String> learnClearPkgFilterList = new ArrayList();
        ArrayList<String> onlyBeKillPkgList = new ArrayList();
        ArrayList<String> protectServiceInfo = new ArrayList();
        ArrayList<String> thirdPkgFilterList = new ArrayList();
        ArrayList<String> forceStopFilterPkgList = new ArrayList();
        ArrayList<String> regionProcessInfoExp = new ArrayList();
        ArrayList<String> regionPackagesInfoExp = new ArrayList();
        try {
            fileInputStream = new FileInputStream(file);
            XmlPullParser newPullParser = Xml.newPullParser();//XmlPullParserFactory.newInstance().newPullParser();
            newPullParser.setInput(fileInputStream, null);
            //newPullParser.nextTag();
            int next;
            do {
                next = newPullParser.next();
                if (next == XmlPullParser.START_TAG) {
                    String name = newPullParser.getName();
                    if (LABEL_PROCESS.equals(name)) {
                        name = newPullParser.nextText();
                        if (name != null && !name.isEmpty()) {
                            processFilterList.add(name);
                        }
                    } else if (LABEL_PACKAGE.equals(name)) {
                        name = newPullParser.nextText();
                        if (name != null && !name.isEmpty()) {
                            packageFilterList.add(name);
                        }
                    } else if (LABEL_DEEP_CLEAR.equals(name)) {
                        name = newPullParser.nextText();
                        if (name != null && !name.isEmpty()) {
                            deepClearProcessFilterList.add(name);
                        }
                    } else if (LABEL_LEARN_CLEAR.equals(name)) {
                        name = newPullParser.nextText();
                        if (name != null && !name.isEmpty()) {
                            learnClearPkgFilterList.add(name);
                        }
                    } else if (LABEL_ONLY_KILL_PACKAGE.equals(name)) {
                        name = newPullParser.nextText();
                        if (name != null && !name.isEmpty()) {
                            onlyBeKillPkgList.add(name);
                        }
                    } else if (LABEL_SERVICE.equals(name)) {
                        name = newPullParser.nextText();
                        if (name != null && !name.isEmpty()) {
                            protectServiceInfo.add(name);
                        }
                    } else if (LABEL_REGION_PROCESS.equals(name)) {
                        name = newPullParser.nextText();
                        if (name != null && !name.isEmpty()) {
                            regionProcessInfoExp.add(name);
                        }
                    } else if (LABEL_REGION_PACKAGE.equals(name)) {
                        name = newPullParser.nextText();
                        if (name != null && !name.isEmpty()) {
                            regionPackagesInfoExp.add(name);
                        }
                    } else if (LABEL_THIRD.equals(name)) {
                        name = newPullParser.nextText();
                        if (name != null && !name.isEmpty()) {
                            thirdPkgFilterList.add(name);
                        }
                    } else if (LABEL_FORCESTOP.equals(name)) {
                        name = newPullParser.nextText();
                        if (name != null && !name.isEmpty()) {
                            forceStopFilterPkgList.add(name);
                        }
                    }
                }
            } while (next != XmlPullParser.END_DOCUMENT);
        } catch (FileNotFoundException e) {
            Log.i(TAG, "file not found exception " + e);
        } catch (XmlPullParserException e) {
            Log.i(TAG, "xml pull parser exception " + e);
        } catch (IOException e) {
            Log.i(TAG, "io exception " + e);
        }  finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    Log.i(TAG, "Failed to close state FileInputStream " + e);
                }
            }
        }
        filterList.processFilterList = processFilterList;
        filterList.packageFilterList = packageFilterList;
        filterList.deepClearProcessList = deepClearProcessFilterList;
        filterList.learnClearPkgList = learnClearPkgFilterList;
        filterList.onlyKillPkgList = onlyBeKillPkgList;
        filterList.serviceFilterList = protectServiceInfo;
        filterList.thirdPkgFilterList = thirdPkgFilterList;
        filterList.forceStopFilterPkgList = forceStopFilterPkgList;
        filterList.regionProcessListExp = regionProcessInfoExp;
        filterList.regionPackagesListExp = regionPackagesInfoExp;
        return filterList;
    }

    public void initSysClearAppFilterList(Context context) {
        File file = new File(PATH, FILTER_LIST);
        FilterList filterList = getFilterListFormXml(file);
        isExp = context.getPackageManager().hasSystemFeature("prize.version.exp");
        LogUtils.d(TAG, "updateClearFilterList isExp: " + isExp);
        if (filterList.processFilterList == null || filterList.processFilterList.isEmpty()) {
            filterList.processFilterList = new ArrayList(PrizeClearUtil.mProcessFilterList);
            if (isExp) {
                filterList.processFilterList.addAll(PrizeClearUtil.mProcessFilterList_Exp);
                LogUtils.d(TAG, "localList.processFilterList Size: " + filterList.processFilterList.size());
            }
        }
        if (filterList.packageFilterList == null || filterList.packageFilterList.isEmpty()) {
            filterList.packageFilterList = new ArrayList(PrizeClearUtil.mPackageFilterList);
            if (isExp) {
                filterList.packageFilterList.addAll(PrizeClearUtil.mPackageFilterList_Exp);
                LogUtils.d(TAG, "localList.packageFilterList Size: " + filterList.packageFilterList.size());
            }
        }
        if (filterList.deepClearProcessList == null || filterList.deepClearProcessList.isEmpty()) {
            filterList.deepClearProcessList = new ArrayList(PrizeClearUtil.mDeepClearProcessList);
            if (isExp) {
                filterList.deepClearProcessList.addAll(PrizeClearUtil.mDeepClearProcessList_Exp);
                LogUtils.d(TAG, "localList.deepClearProcessList Size: " + filterList.deepClearProcessList.size());
            }
        }
        if (filterList.learnClearPkgList == null || filterList.learnClearPkgList.isEmpty()) {
            filterList.learnClearPkgList = new ArrayList(PrizeClearUtil.mLearnClearPkgList);
            if (isExp) {
                filterList.learnClearPkgList.addAll(PrizeClearUtil.mLearnClearPkgList_Exp);
                LogUtils.d(TAG, "localList.learnClearPkgList Size: " + filterList.learnClearPkgList.size());
            }
        }
        if (filterList.onlyKillPkgList == null || filterList.onlyKillPkgList.isEmpty()) {
            filterList.onlyKillPkgList = new ArrayList(PrizeClearUtil.mOnlyKillPkgList);
        }
        if (filterList.serviceFilterList == null || filterList.serviceFilterList.isEmpty()) {
            filterList.serviceFilterList = new ArrayList(PrizeClearUtil.mServiceFilterList);
        }
        if (filterList.thirdPkgFilterList == null || filterList.thirdPkgFilterList.isEmpty()) {
            filterList.thirdPkgFilterList = new ArrayList(PrizeClearUtil.mThirdPkgFilterList);
        }
        if (filterList.forceStopFilterPkgList == null || filterList.forceStopFilterPkgList.isEmpty()) {
            filterList.forceStopFilterPkgList = new ArrayList(PrizeClearUtil.mForceStopFilterPkgList);
        }

        initPreference(context, filterList);
    }

    private void initPreference(Context context, FilterList filterList) {
        if (filterList != null) {
            ArrayList<String> processFilterList = filterList.processFilterList;
            ArrayList<String> packageFilterList = filterList.packageFilterList;
            ArrayList<String> deepClearProcessList = filterList.deepClearProcessList;
            ArrayList<String> learnClearPkgList = filterList.learnClearPkgList;
            ArrayList<String> onlyKillPkgList = filterList.onlyKillPkgList;
            ArrayList<String> serviceFilterList = filterList.serviceFilterList;
            ArrayList<String> thirdFilterList = filterList.thirdPkgFilterList;
            ArrayList<String> forceStopFilterPkgList = filterList.forceStopFilterPkgList;
            ArrayList<String> regionProcessListExp = filterList.regionProcessListExp;
            ArrayList<String> regionPackagesListExp = filterList.regionPackagesListExp;
            editSharedPreferences(context, processFilterList, PREF_NAME_PROCESS, Context.MODE_PRIVATE);
            editSharedPreferences(context, packageFilterList, PREF_NAME_PACKAGE, Context.MODE_PRIVATE);
            editSharedPreferences(context, deepClearProcessList, PREF_NAME_DEEP_CLEAR, Context.MODE_PRIVATE);
            editSharedPreferences(context, learnClearPkgList, PREF_NAME_LEARN_CLEAR, Context.MODE_PRIVATE);
            editSharedPreferences(context, onlyKillPkgList, PREF_NAME_ONLY_KILL_PACKAGE, Context.MODE_PRIVATE);
            editSharedPreferences(context, serviceFilterList, PREF_NAME_SERVICE, Context.MODE_PRIVATE);
            editSharedPreferences(context, thirdFilterList, PREF_NAME_THIRD, Context.MODE_PRIVATE);
            editSharedPreferences(context, forceStopFilterPkgList, PREF_NAME_FORCESTOP, Context.MODE_PRIVATE);
            editSharedPreferences(context, regionProcessListExp, PREF_NAME_REGION_PROCESS, Context.MODE_PRIVATE);
            editSharedPreferences(context, regionPackagesListExp, PREF_NAME_REGION_PACKAGE, Context.MODE_PRIVATE);
        }
    }

    public ArrayList<String> getFilterListFromSP(Context context, int filterType) {
        isExp = context.getPackageManager().hasSystemFeature("prize.version.exp");
        LogUtils.d(TAG, "getFilterListFromSP isExp:" + isExp);
        ArrayList<String> filterList = getFilterList(filterType);
        SharedPreferences sp = getFilterListSP(context, filterType);
        if (sp == null || !sp.getBoolean(PREF_UPDATE_FINISHED, false)) {
            return filterList;
        }
        ArrayList<String> arrayList = new ArrayList();

        Set<Entry<String, String>> entrySet = ((HashMap) sp.getAll()).entrySet();
        for (Entry<String, String> key : entrySet) {
            String str = key.getKey();
            if (!str.equals(PREF_UPDATE_FINISHED)) {
                arrayList.add(str);
            }
        }
        if (arrayList.isEmpty()) {
            return filterList;
        }
        return arrayList;
    }

    private ArrayList<String> getFilterList(int type) {
        ArrayList<String> tempList = new ArrayList();
        switch (type) {
            case PrizeClearUtil.PROCESS:
                tempList = new ArrayList(PrizeClearUtil.mProcessFilterList);
                break;
            case PrizeClearUtil.PACKAGE:
                tempList = new ArrayList(PrizeClearUtil.mPackageFilterList);
                break;
            case PrizeClearUtil.DEEP_CLEAR:
                tempList = new ArrayList(PrizeClearUtil.mDeepClearProcessList);
                break;
            case PrizeClearUtil.LEARN:
                tempList = new ArrayList(PrizeClearUtil.mLearnClearPkgList);
                break;
            case PrizeClearUtil.ONLY_KILL_PKG:
                tempList = new ArrayList(PrizeClearUtil.mOnlyKillPkgList);
                break;
            case PrizeClearUtil.SERVICE:
                tempList = new ArrayList(PrizeClearUtil.mServiceFilterList);
                break;
            case PrizeClearUtil.THIRD_WHITE_LIST:
                tempList = new ArrayList(PrizeClearUtil.mThirdPkgFilterList);
                break;
            case PrizeClearUtil.FORCE_STOP:
                tempList = new ArrayList(PrizeClearUtil.mForceStopFilterPkgList);
                break;
        }
        if (isExp) {
            switch (type) {
                case PrizeClearUtil.PROCESS:
                    tempList.addAll(PrizeClearUtil.mProcessFilterList_Exp);
                    break;
                case PrizeClearUtil.PACKAGE:
                    tempList.addAll(PrizeClearUtil.mPackageFilterList_Exp);
                    break;
                case PrizeClearUtil.DEEP_CLEAR:
                    tempList.addAll(PrizeClearUtil.mDeepClearProcessList_Exp);
                    break;
                case PrizeClearUtil.LEARN:
                    tempList.addAll(PrizeClearUtil.mLearnClearPkgList_Exp);
                    break;
            }
            LogUtils.d(TAG, "[EXP]tempList Size: " + tempList.size());
        }
        return tempList;
    }

    private SharedPreferences getFilterListSP(Context context, int type) {
        switch (type) {
            case PrizeClearUtil.PROCESS:
                return context.getSharedPreferences(PREF_NAME_PROCESS, Context.MODE_PRIVATE);
            case PrizeClearUtil.PACKAGE:
                return context.getSharedPreferences(PREF_NAME_PACKAGE, Context.MODE_PRIVATE);
            case PrizeClearUtil.DEEP_CLEAR:
                return context.getSharedPreferences(PREF_NAME_DEEP_CLEAR, Context.MODE_PRIVATE);
            case PrizeClearUtil.LEARN:
                return context.getSharedPreferences(PREF_NAME_LEARN_CLEAR, Context.MODE_PRIVATE);
            case PrizeClearUtil.ONLY_KILL_PKG:
                return context.getSharedPreferences(PREF_NAME_ONLY_KILL_PACKAGE, Context.MODE_PRIVATE);
            case PrizeClearUtil.SERVICE:
                return context.getSharedPreferences(PREF_NAME_SERVICE, Context.MODE_PRIVATE);
            case PrizeClearUtil.THIRD_WHITE_LIST:
                return context.getSharedPreferences(PREF_NAME_THIRD, Context.MODE_PRIVATE);
            case PrizeClearUtil.FORCE_STOP:
                return context.getSharedPreferences(PREF_NAME_FORCESTOP, Context.MODE_PRIVATE);
            default:
                return null;
        }
    }

    private void editSharedPreferences(Context context, ArrayList<String> list, String name, int mode) {
        if (list != null && !list.isEmpty() && name != null && !name.equals("")) {
            SharedPreferences.Editor edit = context.getSharedPreferences(name, mode).edit();
            edit.clear();
            edit.commit();
            edit.putBoolean(PREF_UPDATE_FINISHED, false);
            edit.commit();
            int size = list.size();
            for (int i = 0; i < size; i++) {
                edit.putBoolean((String)list.get(i), true);
            }
            edit.commit();
            edit.putBoolean(PREF_UPDATE_FINISHED, true);
            edit.commit();
        }
    }


    private ArrayList<ServiceInfo> getLocalServiceList(Context context) {
        ArrayList<ServiceInfo> serviceInfos = new ArrayList();
        ArrayList<String> filterList = getFilterListFromSP(context, PrizeClearUtil.SERVICE);
        if (filterList != null) {
            Iterator it = filterList.iterator();
            while (it.hasNext()) {
                String str = (String) it.next();
                ArrayList<String> serviceList = new ArrayList();
                if (str.contains("#")) {
                    String[] split = str.split("#");
                    String pkg = split[0];
                    int length = split.length;
                    if (length >= 2) {
                        int killType = 0;
                        for (int i = 1; i < length; i++) {
                            String service = split[i];
                            if (service != null && service.equals("NONE")) {
                                killType = 1;
                                break;
                            }
                            serviceList.add(service);
                        }
                        serviceInfos.add(new ServiceInfo(pkg, serviceList, killType));
                    }
                }
            }
        }
        return serviceInfos;
    }


    public int getKillType(Context context, String processName, String pkgName, boolean isPersistent) {
        if (isPersistent) {
            return 1;
        }
        synchronized (mClearFilterMgr) {
            if (mServiceList == null) {
                mServiceList = getLocalServiceList(context);
            }
            Iterator it = mServiceList.iterator();
            while (it.hasNext()) {
                ServiceInfo serviceInfo = (ServiceInfo) it.next();
                if (serviceInfo.killType == 1) {
                    if (serviceInfo.pkg.equals(pkgName)) {
                        return 1;
                    }
                } else if (serviceInfo.killType == 0 && serviceInfo.pkg.equals(pkgName)) {
                    if (serviceInfo.serviceList.contains(processName)) {
                        return 0;
                    }
                    return 1;
                }
            }
            return 2;
        }
    }

    public boolean isFilterService(Context context, String pkgName) {
        if (pkgName == null || pkgName.isEmpty()) {
            return false;
        }
        synchronized (mClearFilterMgr) {
            if (mServiceList == null) {
                mServiceList = getLocalServiceList(context);
            }
            Iterator it = mServiceList.iterator();
            while (it.hasNext()) {
                if (((ServiceInfo) it.next()).pkg.equals(pkgName)) {
                    return true;
                }
            }
            return false;
        }
    }

    class FilterList {
        public ArrayList<String> processFilterList;
        public ArrayList<String> packageFilterList;
        public ArrayList<String> deepClearProcessList;
        public ArrayList<String> learnClearPkgList;
        public ArrayList<String> onlyKillPkgList;
        public ArrayList<String> serviceFilterList;
        public ArrayList<String> thirdPkgFilterList;
        public ArrayList<String> forceStopFilterPkgList;
        public ArrayList<String> regionProcessListExp;
        public ArrayList<String> regionPackagesListExp;
    }

}