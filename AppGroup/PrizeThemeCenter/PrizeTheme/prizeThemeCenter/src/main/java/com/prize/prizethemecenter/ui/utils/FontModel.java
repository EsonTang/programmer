package com.prize.prizethemecenter.ui.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Xml;

import com.prize.app.util.JLog;
import com.prize.app.util.MD5Util;
import com.prize.app.util.PreferencesUtils;
import com.prize.prizethemecenter.MainApplication;
import com.prize.prizethemecenter.bean.table.LocalFontTable;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class FontModel {

    private Context mContext;
    private static String  Tag="FontModel";
    private ArrayList<File> mFiles;
    private File mFile;
    public FontModel(Context pContext) {
        this.mContext = pContext;
    }

    public void loadLocalFont(String path) {
        BultInsertFontTask wallTask = new BultInsertFontTask();
        wallTask.execute(path);
//        if (!PreferencesUtils.getBoolean(mContext,Constant.IS_PARSE)) {
//        }
        JLog.d(Tag,!PreferencesUtils.getBoolean(mContext,Constant.IS_PARSE)+"");
    }


    private List<LocalFontTable> findFontFromConfig(Context context, String path) {

        InputStream is = null;
        String name = null;
        String id = null;
        String md5 = null;
        ArrayList<LocalFontTable> fonts = new ArrayList<LocalFontTable>();
        try {
            String cpath = path + "config.xml";
            is = context.getResources().getAssets().open(cpath);
            if (is == null) return null;
            XmlPullParser xpp = Xml.newPullParser();
            xpp.setInput(is, "UTF-8");
            int eventType = xpp.getEventType();//0、开始文档 1、结束文档 2、开始标志 3、终止标签 4、内容
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    // 判断当前事件是否为文档开始事件
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    // 判断当前事件是否为标签元素开始事件
                    case XmlPullParser.START_TAG:
                        if (xpp.getName().equals("item-info")) {
                            name = null;
                            id = null;
                        } else if (xpp.getName().equals("name")) {
                            eventType = xpp.next();
                            name = xpp.getText();
                            if (null != name && name.split(";").length > 1) {
                                name = name.split(";")[0];
                            }
                        } else if (xpp.getName().equals("id")) {
                            eventType = xpp.next();
                            id = xpp.getText();
                        }else if (xpp.getName().equals("md5")) {
                            eventType = xpp.next();
                            md5 = xpp.getText();
                        }
                        break;
                    // 判断当前事件是否为标签元素结束事件
                    case XmlPullParser.END_TAG:
                        if (xpp.getName().equals("item-info")) {
                            if (id != null) {
                                LocalFontTable font = new LocalFontTable();
                                String fontPath = path + id + "/" + id + ".ttf";
                                String iconPath = path + id + "/" + id + ".jpg";
                                boolean ft = context.getResources().getAssets().open(fontPath) != null;
                                boolean ic = context.getResources().getAssets().open(fontPath) != null;
                                if (ft && ic) {
//                                    font.key = id;
                                    font.localFontId = id;
                                    font.preview_path = iconPath;
                                    font.title = name;
                                    font.isSelected = false;
                                    font.md5 = md5;
                                    font.path = fontPath;
                                    fonts.add(font);
                                }
                                saveInSdCard(mContext,fontPath,font);
                            }
                        }
                        break;
                }
                // 进入下一个元素并触发相应事件
                eventType = xpp.next();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return fonts;
    }

    /**
     * 保存到sb卡内
     * @param pContext
     * @param path
     * @param pFontTable
     * @throws IOException
     */
    public  void saveInSdCard(Context pContext, String path, LocalFontTable pFontTable) throws IOException {
        mFiles = new ArrayList<>();
        File rt = new File(FileUtils.getExternalFontStoragePath());
        if(!rt.exists()) {
            rt.mkdirs();
        }
        File rootFile = new File(FileUtils.getExternalFontStoragePath(),Constant.LOCAL_FONT_FILE_NAME);
        //如不存在文件夹，则新建文件夹
        if (!rootFile.exists())
            rootFile.mkdirs();
        //在文件夹下加入获取的文件
        String dir = path.substring(0,path.lastIndexOf("/"));
        File dirs = new File(rootFile, dir);
        if(!dirs.exists()) {
            dirs.mkdirs();
        }
        mFile = new File(rootFile, path);
        pFontTable.path = mFile.getPath();
        if (!mFile.exists()){
            InputStream in = pContext.getResources().getAssets().open(path);
            try {
                //文件输出流
                FileOutputStream out = new FileOutputStream(mFile);
                int len = -1;
                byte[] buf =new byte[1024*10];
                while ((len= in.read(buf))!= -1){
                    out.write(buf, 0, len);
                }
                if (out != null) {
                    out.flush();
                    out.close();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    class BultInsertFontTask extends AsyncTask<String, Void, Void> {


        private List<LocalFontTable> list;

        public List<LocalFontTable> getList() {
            return list;
        }

        public void setList(List<LocalFontTable> pList) {
            list = pList;
        }

        @Override
        protected Void doInBackground(String... params) {
            String param = params[0];
            try {
                list = findFontFromConfig(mContext, param);
                MainApplication.getDbManager().delete(LocalFontTable.class);
                MainApplication.getDbManager().save(list);
                for (int i = 0; i < list.size(); i++) {
                    JLog.d("bian","md5"+list.get(i).getMd5() +"md51"+ MD5Util.getFileMD5String(mFile));
                    if (!list.get(i).getMd5().equals( MD5Util.getFileMD5String(mFile))){
                        FileUtils.DeleteFile(mFile);
                        saveInSdCard(mContext,list.get(i).getPath(),list.get(i));
                    }
                }
            } catch (Exception pE) {
                pE.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
        }
    }

}
