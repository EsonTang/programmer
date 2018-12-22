package com.android.settings.face.utils;

import com.android.settings.face.FaceBean;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

import android.util.Xml;
/**
 * Created by Administrator on 2017/12/20.
 */

public class FaceXmlData {

    public static final String TAG_CHECK_ROOT_DATA = "checkrootdata";
    public static final String TAG_CHECK_ROOT_TIME = "facedata";

    public static final String ATTR_YEAR = "face_show_ani";
    public static final String ATTR_MONTH = "face_name";
    public static final String ATTR_DAY = "faceSize";

    public static final String FACEBEAN_PHTH = "/data/system/users/faceid/facebean.xml";

    public static boolean writeCheckRootDataFile(FaceBean checkrootdata, String savepath) {
        boolean ret = false;
        File file = new File(savepath);
        FileOutputStream fstr = null;
        BufferedOutputStream str = null;
        try {
            fstr = new FileOutputStream(file);
            str = new BufferedOutputStream(fstr);

            final XmlSerializer serializer = new FastXmlSerializer();
            serializer.setOutput(str, "utf-8");

            serializer.startDocument(null, true);
            serializer.startTag(null, TAG_CHECK_ROOT_DATA);

            serializer.startTag(null, TAG_CHECK_ROOT_TIME);
            serializer.attribute(null, ATTR_YEAR, "" + checkrootdata.getFace_show_ani());
            serializer.attribute(null, ATTR_MONTH, "" + checkrootdata.getFace_name());
            serializer.attribute(null, ATTR_DAY, "" + checkrootdata.getFaceSize());
            serializer.endTag(null, TAG_CHECK_ROOT_TIME);

            serializer.endTag(null, TAG_CHECK_ROOT_DATA);
            serializer.endDocument();

            str.flush();
            sync(fstr);
            str.close();
            str = null;
            ret = true;
        } catch (Exception e) {
            ret = false;
        } finally {
            try {
                if (str != null) {
                    str.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

    public static FaceBean readCheckRootDataFile(String filepath) {
        FaceBean checkrootdata = new FaceBean(true, "人脸 1", 0);
        FileInputStream str = null;
        try {
            str = new FileInputStream(filepath);
            final XmlPullParser parser = Xml.newPullParser();
            parser.setInput(str, null);

            int type;
            while ((type = parser.next()) != XmlPullParser.START_TAG && type != XmlPullParser.END_DOCUMENT) {
                ;
            }

            //checkrootdata
            if (type != XmlPullParser.START_TAG) {
                str.close();
                return checkrootdata;
            }

            int outerDepth = parser.getDepth();
            while ((type = parser.next()) != XmlPullParser.END_DOCUMENT
                    && (type != XmlPullParser.END_TAG || parser.getDepth() > outerDepth)) {
                if (type == XmlPullParser.END_TAG || type == XmlPullParser.TEXT) {
                    continue;
                }
                String tagName = parser.getName();
                if (tagName.equals(TAG_CHECK_ROOT_TIME)) {
                    String strtemp = parser.getAttributeValue(null, ATTR_YEAR);
                    try {
                        checkrootdata.face_show_ani = Boolean.parseBoolean(strtemp);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    strtemp = parser.getAttributeValue(null, ATTR_MONTH);
                    try {
                        checkrootdata.face_name = strtemp;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    strtemp = parser.getAttributeValue(null, ATTR_DAY);
                    try {
                        checkrootdata.faceSize = Integer.parseInt(strtemp);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
        } finally {
            try {
                if (str != null) {
                    str.close();
                }
            } catch (IOException e) {

            }
        }

        return checkrootdata;
    }

    public static boolean sync(FileOutputStream stream) {
        try {
            if (stream != null) {
                stream.getFD().sync();
            }
            return true;
        } catch (IOException e) {
        }
        return false;
    }
}
