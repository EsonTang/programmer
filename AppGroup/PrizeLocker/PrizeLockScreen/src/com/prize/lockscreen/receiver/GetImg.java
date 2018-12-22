package com.prize.lockscreen.receiver;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.os.AsyncTask;

import com.prize.ext.res.ResHelper;
import com.prize.lockscreen.application.LockScreenApplication;

/***
 * 获取图片拷贝到指定位置, 是用来与主题变化处理图片
 * @author fanjunchen
 *
 */
public class GetImg extends AsyncTask<Void, Void, Void> {

	/**类型， 1表示str为包名， 2表示str为文件路径*/
	private int type;
	
	private String str;
	
	private Context ctx;
	
	public GetImg(int t, String name, Context ctx) {
		this.ctx = ctx;
		type = t;
		str = name;
	}
	/***
	 * 用于监听设置中某个URI变化得到路径用的构造器
	 * @param name
	 * @param ctx
	 */
	public GetImg(String name, Context ctx) {
		this.ctx = ctx;
		type = 2;
		str = name;
	}
	@Override
	protected Void doInBackground(Void... params) {
		// TODO Auto-generated method stub
		byte[] buffer = new byte[1024];
		FileOutputStream out = null;
		InputStream in = null;
		try {
			if (type == 1) {
				in = ResHelper.getInstance(ctx).getLockScreenStream();
				out = new FileOutputStream(LockScreenApplication.IMG_PATH);
				int len = 0;
				while((len = in.read(buffer)) !=-1) {
					out.write(buffer, 0, len);
				}
				out.flush();
				out.close();
			}
			else if (type == 2) { // 若是这种情况就还得判断一下是否为节日壁纸, 增加对节日壁纸的兼容
				in = new FileInputStream(str);
				out = new FileOutputStream(LockScreenApplication.IMG_PATH);
				int len = 0;
				while((len = in.read(buffer)) !=-1) {
					out.write(buffer, 0, len);
				}
				out.flush();
				out.close();
			}
			LockScreenApplication.forceGetBgImg();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (in != null)
					in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				if (out != null)
					out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
}
