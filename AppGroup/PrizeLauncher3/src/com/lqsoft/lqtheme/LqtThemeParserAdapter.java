package com.lqsoft.lqtheme;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.Context;



public class LqtThemeParserAdapter extends ThemeParserBaseAdapter{


    // ===========================================================
    // Constants
    // ===========================================================

    // ===========================================================
    // Fields
    // ===========================================================

    // ===========================================================
    // Constructors
    // ===========================================================

    // ===========================================================
    // Getter & Setter
    // ===========================================================

    // ===========================================================
    // Methods for/from SuperClass/Interfaces
    // ===========================================================
    @Override
    public int getThemeType(String themeFilePath) {
        // TODO Auto-generated method stub
        return LoadXml(themeFilePath);
    }

    @Override
    public String getApplyThemeFilePath(Context context,String themeFilePath) {
        String result = parserApplyThemePath(themeFilePath);
        return result;
    
    }

    private String parserApplyThemePath(String themeFilePath) {
        //long costTime = System.currentTimeMillis();
        String applyThemePath = "";
        try {
            File file = new File(themeFilePath);
            // 获得zip信息
            String themeFileDir = file.getParent();
            @SuppressWarnings("resource")
            ZipFile zipFile = new ZipFile(themeFilePath);           
            @SuppressWarnings("unchecked")
            Enumeration<ZipEntry> enu = (Enumeration<ZipEntry>) zipFile.entries();
            //Log.i("lqtTheme", "[OLThemePickerActivity][getLqtThemeFilePath][0]cost = "+(System.currentTimeMillis() -costTime));
            while (enu.hasMoreElements()) {
                ZipEntry zipElement = (ZipEntry) enu.nextElement();
                String fileName = zipElement.getName();
                if (fileName != null && fileName.contains(".jar")) {// 是否为主题文件
                    //Log.i("lqtTheme", "[OLThemePickerActivity][getLqtThemeFilePath][1]cost = "+(System.currentTimeMillis() -costTime));
                    InputStream read = zipFile.getInputStream(zipElement);
                    applyThemePath = unZipFile(zipElement, read,themeFileDir);
                   // Log.i("lqtTheme", "[OLThemePickerActivity][getLqtThemeFilePath][2]cost = "+(System.currentTimeMillis() -costTime));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return applyThemePath;
    }
    
    // ===========================================================
    // Methods
    // ===========================================================
    private InputStream getConfigFileInputStream(String themeFilePath){
        ZipFile zipFile = null;
        InputStream input = null;
        String configFile = ThemeParserBaseAdapter.LQ_THEME_TYPE_CONFIG_XML;

        try {
            zipFile = new ZipFile(themeFilePath);

            input = readStream(zipFile, configFile);
            
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return input;
    }
    
    private InputStream readStream(ZipFile zipFile, String fileName) {
        InputStream input = null;
        final ZipEntry entry = zipFile.getEntry(fileName);
        if (entry != null && !entry.isDirectory()) {
            try {
                input = zipFile.getInputStream(entry);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return input;
    }
    
    private int LoadXml(String themeFilePath) {
        int result = 3;
        InputStream inputStream=getConfigFileInputStream(themeFilePath);
        if(inputStream==null){
            return result;
        }
        XmlPullParserFactory pullFactory;
        try {
            pullFactory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = pullFactory.newPullParser();
            parser.setInput(inputStream, "UTF-8");

            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                case XmlPullParser.START_DOCUMENT:
                    break;
                case XmlPullParser.START_TAG:
                    if (parser.getName().equals("lq")) {
                        final int count=parser.getAttributeCount();
                        for (int i = 0; i < count; i++) {
                            String attName = parser.getAttributeName(i);
                            if(attName.equals("themetype")){
                                result =Integer.parseInt(parser.getAttributeValue(i));
                            }
                        }
                    }
                    break;
                case XmlPullParser.END_TAG:
                    break;
                default:
                    break;
                }
                eventType = parser.next();
            }

        } catch(Exception e) {
            e.printStackTrace();
        }
        return result;
    }
    
    public String unZipFile(ZipEntry ze, InputStream read, String themeFileDir)
            throws FileNotFoundException, IOException {
        int count = 0, bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        String themeFilePath = "";

        String fileName = ze.getName();
        themeFilePath = themeFileDir + "/" + fileName;
        File file = new File(themeFilePath);
        if (!file.exists()) {
            File rootDirectoryFile = new File(file.getParent());
            // 创建目录
            if (!rootDirectoryFile.exists()) {
                rootDirectoryFile.mkdirs();
            }
        } else {
            return themeFilePath;
        }
        // 写入文件
        bos = new BufferedOutputStream(new FileOutputStream(file));
        bis = new BufferedInputStream(read);
        while ((count = bis.read(buffer, 0, bufferSize)) != -1) {
            bos.write(buffer, 0, count);
        }
        // 要注意IO流关闭的先后顺序
        bos.flush();
        bos.close();
        read.close();

        return themeFilePath;
    }
    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================
}
