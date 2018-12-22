/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：
 *当前版本：1.0.0
 *作	者：yiyi
 *完成日期：2015年7月23日
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
 ...
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
 *********************************************/
package com.prize.cloud.db;

import android.content.Context;

import com.lidroid.xutils.DbUtils;

/**
 * 初始化数据库操作，生成数据库操作对象
 * 
 * @author yiyi
 * @version 1.0.0
 */
public class DbManager {
	public static DbManager mInstance = new DbManager();
	private volatile DbUtils db;
	private static final String DB_NAME = "pcloud.db";

	private DbManager() {
	}

	public static DbManager getInstance() {
		return mInstance;
	}

	/**
	 * 创建db，并生成db操作对象
	 * 
	 * @param ctx
	 */
	public void createDb(Context ctx) {
		synchronized (DbManager.class) {
			if (db == null)
				db = createFinalDb(ctx);
		}
	}

	/**
	 * 获取数据库操作对象，以此对象对数据库进行直接的增删改查
	 * @return
	 */
	public DbUtils getDb() {
		return db;
	}

	private static DbUtils createFinalDb(Context context) {
		DbUtils db = DbUtils.create(context, DB_NAME);

		return db;
	}

}
