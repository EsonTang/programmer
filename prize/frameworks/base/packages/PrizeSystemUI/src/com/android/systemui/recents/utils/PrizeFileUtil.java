package com.android.systemui.recents.utils;

/**
 * Created by prize on 2018/1/11.
 */

import android.util.Log;
import android.util.Xml;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class PrizeFileUtil {

    private static final String TAG = "PrizeFileUtil";

    public static final String PATH = "/data/system/recenttask/";
    public static final String LOCKED = "locked_apps.xml";
    public static final String FORBID = "forbid_lock_apps.xml";
    public static final String FIXED = "fixed_lock_apps.xml";
    public static final String DEFAULT_LOCKED = "default_locked_apps.xml";
    public static final String BACKUP_LOCKED = "backup_locked_apps.xml";

    private static PrizeFileUtil mFileUtil = null;

    public static synchronized PrizeFileUtil getInstance() {
        if (mFileUtil == null) {
            mFileUtil = new PrizeFileUtil();
        }
        return mFileUtil;
    }

    private boolean createFile(String path, String fileName) {
        File file = new File(path);
        if (!file.exists()) {
            try {
                if (file.mkdirs()) {
                    Log.i(TAG, "initFile: create dir");
                } else {
                    Log.i(TAG, "initFile: failed create dir");
                }
            } catch (Exception e) {
                Log.i(TAG, "failed create dir " + e);
            }
        }
        File file2 = new File(path + fileName);
        if (file2.exists()) {
            return false;
        }
        try {
            if (file2.createNewFile()) {
                return true;
            }
            Log.i(TAG, "initFile: file.createNewFile() failed");
            return false;
        } catch (IOException e2) {
            Log.i(TAG, "failed create file " + e2);
            return false;
        }
    }

    private ArrayList<String> getInfoFromXmlPullParser(File file) {
        FileInputStream fileInputStream = null;
        ArrayList<String> arrayList = new ArrayList();

        try {
            fileInputStream = new FileInputStream(file);
            XmlPullParser newPullParser = Xml.newPullParser();
            newPullParser.setInput(fileInputStream, null);
            int next;
            do {
                next = newPullParser.next();
                if (next == 2) {
                    if ("p".equals(newPullParser.getName())) {
                        String attributeValue = newPullParser.getAttributeValue(null, "att");
                        if (attributeValue != null) {
                            arrayList.add(attributeValue);
                        }
                    }
                }
            } while (next != 1);
        } catch (FileNotFoundException e) {
            Log.i(TAG, "file not found exception " + e);
        } catch (XmlPullParserException e) {
            Log.i(TAG, "xml pull parser exception " + e);
        } catch (IOException e) {
            Log.i(TAG, "io exception " + e);
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    Log.i(TAG, "Failed to close state FileInputStream " + e);
                }
            }
        }
        return arrayList;
    }

    private void saveToXmlSerializer(File file, List list) {
        FileOutputStream fileOutputStream = null;
        Log.i(TAG, "saveToXmlSerializer");
        if (file != null) {
            try {
                fileOutputStream = new FileOutputStream(file);
                XmlSerializer newSerializer = Xml.newSerializer();
                newSerializer.setOutput(fileOutputStream, "UTF-8");
                newSerializer.startDocument(null, Boolean.valueOf(true));
                newSerializer.startTag(null, "gs");
                for (int i = 0; i < list.size(); i++) {
                    String str = (String) list.get(i);
                    if (str != null) {
                        newSerializer.startTag(null, "p");
                        newSerializer.attribute(null, "att", str);
                        newSerializer.endTag(null, "p");
                    }
                }
                newSerializer.endTag(null, "gs");
                newSerializer.endDocument();
                newSerializer.flush();
                Log.i(TAG, "save success");
            } catch (FileNotFoundException e) {
                Log.i(TAG, "file not found exception " + e);
            } catch (IOException e) {
                Log.i(TAG, "io exception " + e);
            } finally {
                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.close();
                    } catch (IOException e) {
                        Log.i(TAG, "failed close stream " + e);
                    }
                }
            }
        }
    }

    public void init() {
        File file = new File(PATH);
        if (!(file.exists() || file.mkdir())) {
            Log.i(TAG, "init failed mkdir");
        }
        createFile(PATH, LOCKED);
        createFile(PATH, FORBID);
        createFile(PATH, FIXED);
        createFile(PATH, DEFAULT_LOCKED);
        createFile(PATH, BACKUP_LOCKED);
    }

    public ArrayList<String> getInfoFromXml(String path, String fileName) {
        ArrayList<String> list;
        File file = new File(path, fileName);
        synchronized (mFileUtil) {
            list = getInfoFromXmlPullParser(file);
        }
        return list;
    }

    public void saveInfoToXml(String path, String fileName, List list) {
        if (path != null && fileName != null && list != null) {
            File file = new File(path, fileName);
            if (!file.exists()) {
                try {
                    if (!file.createNewFile()) {
                        Log.i(TAG, "saveListToFile: failed create file");
                    }
                } catch (IOException e) {
                    Log.i(TAG, "failed create file " + e);
                }
            }
            synchronized (mFileUtil) {
                saveToXmlSerializer(file, list);
            }
        }
    }
}