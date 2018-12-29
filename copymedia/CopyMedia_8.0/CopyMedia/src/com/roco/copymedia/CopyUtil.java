package com.roco.copymedia;

import java.io.File;
import java.io.IOException;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.os.SystemProperties;
import android.util.Log;

public class CopyUtil {

	public static void debug(String msg){
		android.util.Log.d("xxczy","roco=>"+msg);
	}
	
	public static String getRealPath(File file){
		try {
			debug("getRealPath getCanonicalPath="+file.getCanonicalPath()+" absPath="+file.getAbsolutePath());
			if(!file.getCanonicalPath().equals(file.getAbsolutePath())){
				return getRealPath(new File(file.getCanonicalPath()));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return file.getAbsolutePath();
	}
	
	public static void doCopy(final Context ctx , final String SRC, final String DST){

		boolean copyOk = CopyJni.isCopyDown();
		
		debug("doCopyStuff copy "+ copyOk);
		if(!copyOk){
			new Thread(){
				public void run() {

					File src = new File(SRC);
					File dst =  new File(DST);

					String dstPath = dst.getAbsolutePath();
					String dstCanoinacal = dstPath;
					debug("dstPath="+dstPath);
				
					dstCanoinacal = getRealPath(dst);
					debug("dstCanoinacal="+dstCanoinacal);
					dst = new File(dstCanoinacal);
					
					boolean ret = true;
				
					if(!dst.exists()){
						ret = dst.mkdirs();
					}
	
					if(!ret){
						debug("mkdir error return now "+dstCanoinacal);
						return;
					}
					
					CopyJni.doSomething(src, dst);

					CopyJni.copyDown(ctx, dstPath);
				};
			}.start();
		}
	}

	public static void updateMedia(Context context, String filename){  
		MediaScannerConnection.scanFile(context,  
                  new String[] { filename }, null,  
                  new MediaScannerConnection.OnScanCompletedListener() {  
              public void onScanCompleted(String path, Uri uri) {  
                  debug("Scanned " + path + ":");  
                  debug("-> uri=" + uri);  
              }  
         });  
    } 


	
}
