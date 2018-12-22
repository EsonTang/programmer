
/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：短信信息
 *当前版本：V1.0
 *作	者：zhouerlong
 *完成日期：2015-8-5
 *********************************************/
package com.android.launcher3.search.data;

/**
 * @author kxd
 *"_id", // 0
			"thread_id", // 1
			"address", // 2
			"person", // 3
			"date", // 4
			"body", // 5
			"read", // 6; 0:not read 1:read; default is 0
			"type", // 7; 0:all 1:inBox 2:sent 3:draft 4:outBox 5:failed
					// 6:queued
			"service_center" // 8
			
			
				public static final int ID = 0;
	public static final int DATE = 1;
	public static final int MESSAGE_COUNT = 2;
	public static final int RECIPIENT_IDS = 3;
	public static final int SNIPPET = 4;
	public static final int SNIPPET_CS = 5;
	public static final int READ = 6;
 */
public class MmsBean {
	public String date;
	public String address;
	public String person;
	public String snippet;
	public String body;
	public String read;
}
