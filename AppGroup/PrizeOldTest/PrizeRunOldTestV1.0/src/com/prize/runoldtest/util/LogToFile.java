package com.prize.runoldtest.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Environment;
import android.util.Log;

public class LogToFile {
	 private static String logPath = null;//log日志存放路径
	 private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US);//日期格式;
	// private static Date date = new Date();//因为log日志是使用日期命名的，使用静态成员变量主要是为了在整个程序运行期间只存在一个.log文件中;
	 
	 private static String fileName ="";
	 private static String ddrtestfile ="DDR_test_result";
	 private static Context mcontext;
	
	
	
	 /**
    * 初始化，须在使用之前设置，最好在Application创建时调用
    *
    * @param context
    */
   public static void init(Context context) {
       logPath = getFilePath(context) + "/OldLogs";//获得文件储存路径,在后面加"/Logs"建立子文件夹
        fileName = logPath + "/log_" + dateFormat.format(new Date()) + ".txt";//log日志名，使用时间命名，保证不重复
        mcontext=context;
   }
   
   
   
   /**
    * 获得文件存储路径
    *
    * @return
    */
  /* @SuppressLint("NewApi")
	private static String getFilePath(Context context) {

       if (Environment.MEDIA_MOUNTED.equals(Environment.MEDIA_MOUNTED) || !Environment.isExternalStorageRemovable()) {//如果外部储存可用
           return context.getExternalFilesDir(null).getPath();//获得外部存储路径,默认路径为 /storage/emulated/0/Android/data/com.waka.workspace.logtofile/files/Logs/log_2016-03-14_16-15-09.log
       } else {
           return context.getFilesDir().getPath();//直接存在/data/data里，非root手机是看不到的
       }
   }*/
   
   
   

	 @SuppressLint("NewApi")
	private static String getFilePath(Context context) {
		 String path="";
		 
	     if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
	            // 创建一个文件夹对象，赋值为外部存储器的目录
	             File sdcardDir =Environment.getExternalStorageDirectory();
	           //得到一个路径，内容是sdcard的文件夹路径和名字
	              path=sdcardDir.getPath()+"/OldTest";
	             File path1 = new File(path);
	            if (!path1.exists()) {
	             //若不存在，创建目录，可以在应用启动的时候创建
	             path1.mkdirs();
	          //   setTitle("paht ok,path:"+path);
	           }
	            
	            }
		 
		return path;
		}
   
   
   
   
   
   public static void writeToFile(String msg) {

       if (null == logPath) {
           Log.e("writeToFile:", "logPath == null ，未初始化LogToFile");
           init(mcontext);
           return;
       }

      // String log = dateFormat.format(date) + " " + type + " " + tag + " " + msg + "\n";//log日志内容，可以自行定制

       //如果父路径不存在
       File file = new File(logPath);
       if (!file.exists()) {
           file.mkdirs();//创建父路径
       }

       FileOutputStream fos = null;//FileOutputStream会自动调用底层的close()方法，不用关闭
       BufferedWriter bw = null;
       try {

           fos = new FileOutputStream(ddrtestfile, true);//这里的第二个参数代表追加还是覆盖，true为追加，flase为覆盖
           bw = new BufferedWriter(new OutputStreamWriter(fos));
           bw.write(msg);

       } catch (FileNotFoundException e) {
           e.printStackTrace();
       } catch (IOException e) {
           e.printStackTrace();
       } finally {
           try {
               if (bw != null) {
                   bw.close();//关闭缓冲流
               }
           } catch (IOException e) {
               e.printStackTrace();
           }
       }

   }
   
   /**
    * 将log信息写入文件中
    *
    * @param type
    * @param tag
    * @param msg
    */
   public static void writeToFile(char type, String tag, String msg) {

       if (null == logPath) {
           Log.e("writeToFile:", "logPath == null ，未初始化LogToFile");
           init(mcontext);
           return;
       }

      Date mdate = new Date();
       String log = dateFormat.format(mdate) + " " + type + " " + tag + " " + msg + "\n";//log日志内容，可以自行定制

       //如果父路径不存在
       File file = new File(logPath);
       if (!file.exists()) {
           file.mkdirs();//创建父路径
       }

       FileOutputStream fos = null;//FileOutputStream会自动调用底层的close()方法，不用关闭
       BufferedWriter bw = null;
       try {

           fos = new FileOutputStream(fileName, true);//这里的第二个参数代表追加还是覆盖，true为追加，flase为覆盖
           bw = new BufferedWriter(new OutputStreamWriter(fos));
           bw.write(log);

       } catch (FileNotFoundException e) {
           e.printStackTrace();
       } catch (IOException e) {
           e.printStackTrace();
       } finally {
           try {
               if (bw != null) {
                   bw.close();//关闭缓冲流
               }
           } catch (IOException e) {
               e.printStackTrace();
           }
       }

   }
   
   
   public static final char VERBOSE = 'v';

   public static final char DEBUG = 'd';

   public static final char INFO = 'i';

   public static final char WARN = 'w';

   public static final char ERROR = 'e';

   public static void v(String tag, String msg) {
       writeToFile(VERBOSE, tag, msg);
   }

   public static void d(String tag, String msg) {
       writeToFile(DEBUG, tag, msg);
   }

   public static void i(String tag, String msg) {
       writeToFile(INFO, tag, msg);
   }

   public static void w(String tag, String msg) {
       writeToFile(WARN, tag, msg);
   }

   public static void e(String tag, String msg) {
       writeToFile(ERROR, tag, msg);
   }
   
   


}
