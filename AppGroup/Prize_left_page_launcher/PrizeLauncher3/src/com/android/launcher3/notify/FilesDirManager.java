package com.android.launcher3.notify;

import java.io.File;

import android.app.Application;
import android.content.Context;

/**
 * 数据文件管理
 * @author liuyd
 *
 */
public class FilesDirManager {
    public static final String POCKET_CUTIS = "cutis";
    public static final String TAG = "FilesDirManager";
    public static final String POCKET_LOADING = "loading";
//    public static final String POCKET_MAINFRAME = "mainframe";
//    /**
//     * 首页的数据存放目录
//     */
//    public static final String POCKET_FIRSTPAGE = "firstpage";
//    public static final String POCKET_APP = "app";
//    public static final String POCKET_GAME = "game";

    public static final String POCKET_CONTENT = "content";
    public static final String POCKET_NIFTY = "nifty";

    public static final String POCKET_RPC = "rpc";

    public static File getFile(Context context, String s) {
        return new File(context.getFilesDir(), s);
    }

    public static File getFile(String s) {
//        assert s != null;
        return new File(cRoot, s);
    }

    public static void configure(Application app) {
        cRoot = app.getFilesDir();
    }
    public static void reinitialize(Application application) {
        if (cRoot == null) {
            configure(application);
        }
    }
    private static File cRoot;

    private FilesDirManager() {
        
    }
}
