
/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：Widget控件更新广播发送类
 *当前版本：V1.0
 *作	者：朱道鹏
 *完成日期：2015-04-17
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
package com.android.notepad;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 **
 * 类描述：Widget控件更新广播发送类
 * @author 朱道鹏
 * @version V1.0
 */
public class WidgetUpdateReceiver extends BroadcastReceiver{
	public static final String UPDATE_ACTION = "notepad.widget.UPDATE";
	public static final String IMG_REQUEST = "img_request";
	public static final String NOTE_REQUEST = "note_request";
	public static final String IMG_EDIT = "img_edit";
	public static final String NOTE_EDIT = "note_edit";
	public static final String IMG_FILENAME = "img_filename";
	public static final String NOTE_CONTENT = "note_content";

	public void onReceive(Context context,Intent intent){ 
	}

	
	 /**
	 * 方法描述：Widget控件图片更新
	 * @param Context
	 * @return void
	 * @see WidgetUpdateReceiver#sendImgUpdate
	 */
	public static void sendImgUpdate(Context context){
		Intent update_intent = new Intent(UPDATE_ACTION);
		update_intent.putExtra("img_request", true);
		context.sendBroadcast(update_intent);
	}

	
	/**
	 * 方法描述：Widget控件便签事件更新
	 * @param Context
	 * @return void
	 * @see WidgetUpdateReceiver#sendNoteUpdate
	 */
	public static void sendNoteUpdate(Context context){
		Intent update_intent = new Intent(UPDATE_ACTION);
		update_intent.putExtra("note_request", true);
		context.sendBroadcast(update_intent);
	}

}
