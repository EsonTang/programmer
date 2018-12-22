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

    private static final String LABEL_PROCESS = "ProtectProcessList";
    private static final String LABEL_PACKAGE = "PackageFilterList";
    private static final String LABEL_MORNING = "MorningKillFilterList";//"DeepClearProcessFilterList";
    private static final String LABEL_SCREEN_OFF = "ScreenOffKillFilterList";//"LearnClearPkgFilterList";
    private static final String LABEL_CALLED = "CalledKillFilterList";//"OnlyBeKillPkgList";
    private static final String LABEL_SERVICE = "ProtectServiceInfo";
    private static final String LABEL_DOZE_MODE = "DozeModeFilterList";//"ThirdAppFilterList";
    private static final String LABEL_FORCESTOP = "ForceStopFilterList";
    private static final String LABEL_MUSIC = "MusicFilterList";
    private static final String LABEL_MAP = "MapFilterList";
    private static final String LABEL_3MIN_BLACK_LIST = "3MinBlackList";
    private static final String LABEL_REGION_PROCESS = "RegionProcessInfoExp";
    private static final String LABEL_REGION_PACKAGE = "RegionPackagesInfoExp";

    private static final String PREF_UPDATE_FINISHED = "update_finished";
    private static final String PREF_NAME_PROCESS = "preference_process_filter_list";
    private static final String PREF_NAME_PACKAGE = "preference_package_filter_list";
    private static final String PREF_NAME_MORNING_CLEAR = "preference_morning_filter_list";
    private static final String PREF_NAME_SCREEN_OFF = "preference_screenoff_filter_list";
    private static final String PREF_NAME_CALLED_PACKAGE = "preference_called_filter_list";
    private static final String PREF_NAME_SERVICE = "preference_service_filter_list";
    private static final String PREF_NAME_DOZE_MODE = "preference_doze_filter_list";
    private static final String PREF_NAME_FORCESTOP = "preference_forcestop_filter_list";
    private static final String PREF_NAME_MUSIC = "preference_music_filter_list";
    private static final String PREF_NAME_MAP = "preference_map_filter_list";
    private static final String PREF_NAME_3MIN_BLACK_LIST = "preference_3min_black_list";
    private static final String PREF_NAME_REGION_PROCESS = "preference_region_process_list_exp";
    private static final String PREF_NAME_REGION_PACKAGE = "preference_region_packages_list_exp";

    public static final String CLOUD_LIST_PATH = "/data/system/cloudlist/";
    public static final String CLOUD_FILTER_FILE = "screenofkill.xml";

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
        ArrayList<String> morningFilterList = new ArrayList();
        ArrayList<String> screenOffFilterList = new ArrayList();
        ArrayList<String> calledKillPkgList = new ArrayList();
        ArrayList<String> protectServiceInfo = new ArrayList();
        ArrayList<String> dozePkgFilterList = new ArrayList();
        ArrayList<String> forceStopFilterPkgList = new ArrayList();
        ArrayList<String> musicFilterPkgList = new ArrayList();
        ArrayList<String> mapFilterPkgList = new ArrayList();
        ArrayList<String> threeMinBlackList = new ArrayList();
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
                    } else if (LABEL_MORNING.equals(name)) {
                        name = newPullParser.nextText();
                        if (name != null && !name.isEmpty()) {
                            morningFilterList.add(name);
                        }
                    } else if (LABEL_SCREEN_OFF.equals(name)) {
                        name = newPullParser.nextText();
                        if (name != null && !name.isEmpty()) {
                            screenOffFilterList.add(name);
                        }
                    } else if (LABEL_CALLED.equals(name)) {
                        name = newPullParser.nextText();
                        if (name != null && !name.isEmpty()) {
                            calledKillPkgList.add(name);
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
                    } else if (LABEL_DOZE_MODE.equals(name)) {
                        name = newPullParser.nextText();
                        if (name != null && !name.isEmpty()) {
                            dozePkgFilterList.add(name);
                        }
                    } else if (LABEL_FORCESTOP.equals(name)) {
                        name = newPullParser.nextText();
                        if (name != null && !name.isEmpty()) {
                            forceStopFilterPkgList.add(name);
                        }
                    } else if (LABEL_MUSIC.equals(name)) {
                        name = newPullParser.nextText();
                        if (name != null && !name.isEmpty()) {
                            musicFilterPkgList.add(name);
                        }
                    } else if (LABEL_MAP.equals(name)) {
                        name = newPullParser.nextText();
                        if (name != null && !name.isEmpty()) {
                            mapFilterPkgList.add(name);
                        }
                    } else if (LABEL_3MIN_BLACK_LIST.equals(name)) {
                        name = newPullParser.nextText();
                        if (name != null && !name.isEmpty()) {
                            threeMinBlackList.add(name);
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
        filterList.morningClearFilterList = morningFilterList;
        filterList.screenOffClearPkgList = screenOffFilterList;
        filterList.calledKillPkgList = calledKillPkgList;
        filterList.serviceFilterList = protectServiceInfo;
        filterList.dozePkgFilterList = dozePkgFilterList;
        filterList.forceStopFilterPkgList = forceStopFilterPkgList;
        filterList.musicFilterList = musicFilterPkgList;
        filterList.mapFilterList = mapFilterPkgList;
        filterList.threeMinBlackList = threeMinBlackList;
        filterList.regionProcessListExp = regionProcessInfoExp;
        filterList.regionPackagesListExp = regionPackagesInfoExp;
        return filterList;
    }

    public void initSysClearAppFilterList(Context context) {

        PrizeClearUtil.setUpdateFilterListRunning(true);

        File file = new File(CLOUD_LIST_PATH, CLOUD_FILTER_FILE);
        FilterList filterList = getFilterListFormXml(file);
        if (filterList.processFilterList == null) {
            filterList.processFilterList = new ArrayList(PrizeClearUtil.mProcessFilterList);
        } else {
            filterList.processFilterList.addAll(PrizeClearUtil.mProcessFilterList);
        }
        if (filterList.packageFilterList == null) {
            filterList.packageFilterList = new ArrayList(PrizeClearUtil.mPackageFilterList);
        } else {
            filterList.packageFilterList.addAll(PrizeClearUtil.mPackageFilterList);
        }
        if (filterList.morningClearFilterList == null) {
            filterList.morningClearFilterList = new ArrayList(PrizeClearUtil.mMorningClearProcessList);
        } else {
            filterList.morningClearFilterList.addAll(PrizeClearUtil.mMorningClearProcessList);
        }
        if (filterList.screenOffClearPkgList == null) {
            filterList.screenOffClearPkgList = new ArrayList(PrizeClearUtil.mScreenOffClearPkgList);
        } else {
            filterList.screenOffClearPkgList.addAll(PrizeClearUtil.mScreenOffClearPkgList);
        }
        if (filterList.calledKillPkgList == null) {
            filterList.calledKillPkgList = new ArrayList(PrizeClearUtil.mCalledKillPkgList);
        } else {
            filterList.calledKillPkgList.addAll(PrizeClearUtil.mCalledKillPkgList);
        }
        if (filterList.serviceFilterList == null) {
            filterList.serviceFilterList = new ArrayList(PrizeClearUtil.mServiceFilterList);
        } else {
            filterList.serviceFilterList.addAll(PrizeClearUtil.mServiceFilterList);
        }
        if (filterList.dozePkgFilterList == null) {
            filterList.dozePkgFilterList = new ArrayList(PrizeClearUtil.mDozePkgFilterList);
        } else {
            filterList.dozePkgFilterList.addAll(PrizeClearUtil.mDozePkgFilterList);
        }
        if (filterList.forceStopFilterPkgList == null) {
            filterList.forceStopFilterPkgList = new ArrayList(PrizeClearUtil.mForceStopFilterPkgList);
        } else {
            filterList.forceStopFilterPkgList.addAll(PrizeClearUtil.mForceStopFilterPkgList);
        }
        if (filterList.musicFilterList == null) {
            filterList.musicFilterList = new ArrayList(PrizeClearUtil.mMusicPkgList);
        } else {
            filterList.musicFilterList.addAll(PrizeClearUtil.mMusicPkgList);
        }
        if (filterList.mapFilterList == null) {
            filterList.mapFilterList = new ArrayList(PrizeClearUtil.mMapFilterList);
        } else {
            filterList.mapFilterList.addAll(PrizeClearUtil.mMapFilterList);
        }
        if (filterList.threeMinBlackList == null) {
            filterList.threeMinBlackList = new ArrayList(PrizeClearUtil.m3MinBlackList);
        } else {
            filterList.threeMinBlackList.addAll(PrizeClearUtil.m3MinBlackList);
        }

        initPreference(context, filterList);

        PrizeClearUtil.setUpdateFilterListRunning(false);
    }

    private void initPreference(Context context, FilterList filterList) {
        if (filterList != null) {
            editSharedPreferences(context, filterList.processFilterList, PREF_NAME_PROCESS, Context.MODE_PRIVATE);
            editSharedPreferences(context, filterList.packageFilterList, PREF_NAME_PACKAGE, Context.MODE_PRIVATE);
            editSharedPreferences(context, filterList.morningClearFilterList, PREF_NAME_MORNING_CLEAR, Context.MODE_PRIVATE);
            editSharedPreferences(context, filterList.screenOffClearPkgList, PREF_NAME_SCREEN_OFF, Context.MODE_PRIVATE);
            editSharedPreferences(context, filterList.calledKillPkgList, PREF_NAME_CALLED_PACKAGE, Context.MODE_PRIVATE);
            editSharedPreferences(context, filterList.serviceFilterList, PREF_NAME_SERVICE, Context.MODE_PRIVATE);
            editSharedPreferences(context, filterList.dozePkgFilterList, PREF_NAME_DOZE_MODE, Context.MODE_PRIVATE);
            editSharedPreferences(context, filterList.forceStopFilterPkgList, PREF_NAME_FORCESTOP, Context.MODE_PRIVATE);
            editSharedPreferences(context, filterList.musicFilterList, PREF_NAME_MUSIC, Context.MODE_PRIVATE);
            editSharedPreferences(context, filterList.mapFilterList, PREF_NAME_MAP, Context.MODE_PRIVATE);
            editSharedPreferences(context, filterList.threeMinBlackList, PREF_NAME_3MIN_BLACK_LIST, Context.MODE_PRIVATE);
            editSharedPreferences(context, filterList.regionProcessListExp, PREF_NAME_REGION_PROCESS, Context.MODE_PRIVATE);
            editSharedPreferences(context, filterList.regionPackagesListExp, PREF_NAME_REGION_PACKAGE, Context.MODE_PRIVATE);
        }
    }

    public ArrayList<String> getFilterListFromSP(Context context, int filterType) {
        //isExp = context.getPackageManager().hasSystemFeature("prize.version.exp");
        //LogUtils.d(TAG, "getFilterListFromSP isExp:" + isExp);
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
            case PrizeClearUtil.TYPE_PROCESS:
                tempList = new ArrayList(PrizeClearUtil.mProcessFilterList);
                break;
            case PrizeClearUtil.TYPE_PACKAGE:
                tempList = new ArrayList(PrizeClearUtil.mPackageFilterList);
                break;
            case PrizeClearUtil.TYPE_MORNING:
                tempList = new ArrayList(PrizeClearUtil.mMorningClearProcessList);
                break;
            case PrizeClearUtil.TYPE_SCREEN_OFF:
                tempList = new ArrayList(PrizeClearUtil.mScreenOffClearPkgList);
                break;
            case PrizeClearUtil.TYPE_CALLED:
                tempList = new ArrayList(PrizeClearUtil.mCalledKillPkgList);
                break;
            case PrizeClearUtil.TYPE_SERVICE:
                tempList = new ArrayList(PrizeClearUtil.mServiceFilterList);
                break;
            case PrizeClearUtil.TYPE_DOZE:
                tempList = new ArrayList(PrizeClearUtil.mDozePkgFilterList);
                break;
            case PrizeClearUtil.TYPE_FORCE_STOP:
                tempList = new ArrayList(PrizeClearUtil.mForceStopFilterPkgList);
                break;
            case PrizeClearUtil.TYPE_MUSIC:
                tempList = new ArrayList(PrizeClearUtil.mMusicPkgList);
                break;
            case PrizeClearUtil.TYPE_MAP:
                tempList = new ArrayList(PrizeClearUtil.mMapFilterList);
                break;
            case PrizeClearUtil.TYPE_3MIN_BLACK_LIST:
                tempList = new ArrayList(PrizeClearUtil.m3MinBlackList);
                break;
        }
        if (isExp) {
            switch (type) {
                case PrizeClearUtil.TYPE_PROCESS:
                    tempList.addAll(PrizeClearUtil.mProcessFilterList_Exp);
                    break;
                case PrizeClearUtil.TYPE_PACKAGE:
                    tempList.addAll(PrizeClearUtil.mPackageFilterList_Exp);
                    break;
                case PrizeClearUtil.TYPE_MORNING:
                    tempList.addAll(PrizeClearUtil.mMorningClearProcessList_Exp);
                    break;
                case PrizeClearUtil.TYPE_SCREEN_OFF:
                    tempList.addAll(PrizeClearUtil.mScreenOffClearPkgList_Exp);
                    break;
            }
            LogUtils.d(TAG, "[EXP]tempList Size: " + tempList.size());
        }
        return tempList;
    }

    private SharedPreferences getFilterListSP(Context context, int type) {
        switch (type) {
            case PrizeClearUtil.TYPE_PROCESS:
                return context.getSharedPreferences(PREF_NAME_PROCESS, Context.MODE_PRIVATE);
            case PrizeClearUtil.TYPE_PACKAGE:
                return context.getSharedPreferences(PREF_NAME_PACKAGE, Context.MODE_PRIVATE);
            case PrizeClearUtil.TYPE_MORNING:
                return context.getSharedPreferences(PREF_NAME_MORNING_CLEAR, Context.MODE_PRIVATE);
            case PrizeClearUtil.TYPE_SCREEN_OFF:
                return context.getSharedPreferences(PREF_NAME_SCREEN_OFF, Context.MODE_PRIVATE);
            case PrizeClearUtil.TYPE_CALLED:
                return context.getSharedPreferences(PREF_NAME_CALLED_PACKAGE, Context.MODE_PRIVATE);
            case PrizeClearUtil.TYPE_SERVICE:
                return context.getSharedPreferences(PREF_NAME_SERVICE, Context.MODE_PRIVATE);
            case PrizeClearUtil.TYPE_DOZE:
                return context.getSharedPreferences(PREF_NAME_DOZE_MODE, Context.MODE_PRIVATE);
            case PrizeClearUtil.TYPE_FORCE_STOP:
                return context.getSharedPreferences(PREF_NAME_FORCESTOP, Context.MODE_PRIVATE);
            case PrizeClearUtil.TYPE_MUSIC:
                return context.getSharedPreferences(PREF_NAME_MUSIC, Context.MODE_PRIVATE);
            case PrizeClearUtil.TYPE_MAP:
                return context.getSharedPreferences(PREF_NAME_MAP, Context.MODE_PRIVATE);
            case PrizeClearUtil.TYPE_3MIN_BLACK_LIST:
                return context.getSharedPreferences(PREF_NAME_3MIN_BLACK_LIST, Context.MODE_PRIVATE);
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
        ArrayList<String> filterList = getFilterListFromSP(context, PrizeClearUtil.TYPE_SERVICE);
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
                    } else {
                        return 1;
                    }
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
        public ArrayList<String> morningClearFilterList;
        public ArrayList<String> screenOffClearPkgList;
        public ArrayList<String> calledKillPkgList;
        public ArrayList<String> serviceFilterList;
        public ArrayList<String> dozePkgFilterList;
        public ArrayList<String> forceStopFilterPkgList;
        public ArrayList<String> musicFilterList;
        public ArrayList<String> mapFilterList;
        public ArrayList<String> threeMinBlackList;
        public ArrayList<String> regionProcessListExp;
        public ArrayList<String> regionPackagesListExp;
    }

}