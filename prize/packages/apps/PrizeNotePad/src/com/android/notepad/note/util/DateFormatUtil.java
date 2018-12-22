
/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：日期格式化工具
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

package com.android.notepad.note.util;

import java.sql.Date;
import java.text.SimpleDateFormat;

import com.android.notepad.R;

import android.annotation.SuppressLint;
import android.content.Context;

/**
 **
 * 类描述：日期格式化工具类
 * @author 朱道鹏
 * @version V1.0
 */
public class DateFormatUtil {

	/**
	 * 方法描述：获取传入参数的星期几字符
	 * @param Context int                   weekDay   每周中的第几天 0-6
	 * @return String  星期日  星期一  .....
	 * @see DateFormatUtil#getWeekDayString
	 */
	public static String getWeekDayString(Context context, int weekDay){
		String weekDayString = null;
		switch (weekDay) {
		case 0:
			weekDayString = context.getResources().getString(R.string.sunday);
			break;
		case 1:
			weekDayString = context.getResources().getString(R.string.monday);
			break;
		case 2:
			weekDayString = context.getResources().getString(R.string.tuesday);
			break;
		case 3:
			weekDayString = context.getResources().getString(R.string.wednesday);
			break;
		case 4:
			weekDayString = context.getResources().getString(R.string.thursday);
			break;
		case 5:
			weekDayString = context.getResources().getString(R.string.friday);
			break;
		case 6:
			weekDayString = context.getResources().getString(R.string.saturday);
			break;
		default:
			weekDayString = "传入参数小于0或者大于6";
			break;
		}
		return weekDayString;
	}


	/**
	 * 方法描述：将传入的时分格式化成上午或下午时间
	 * @param mIsTwentyFourHourSystem 
	 * @param Context int int
	 * @return String
	 * @see DateFormatUtil#getAmOrPmAndTimeString
	 */
	public static String getAmOrPmAndTimeString(Context context, int hour, int minute,boolean mIsTwentyFourHourSystem){
		StringBuffer buf = new StringBuffer();
		String amOrPm = "";
		if(hour>=0&&hour<=23 && minute>=0 && minute<=59){
			if(!mIsTwentyFourHourSystem){
				if(hour>=12){
					amOrPm = context.getResources().getString(R.string.pm);
				}else{
					amOrPm = context.getResources().getString(R.string.am);
				}
			}
			if(!mIsTwentyFourHourSystem && hour >= 13){
				hour = hour - 12;
			}
			buf.append(amOrPm);
			if(hour < 10){
				buf.append("0").append(hour).append(":");
			}else{
				buf.append(hour).append(":");
			}
			if(minute < 10){
				buf.append("0").append(minute);
			}else{
				buf.append(minute);
			}
		}else{
			buf.append("传入的参数有误");
		}
		return buf.toString();
	}

	/**
	 * 方法描述：将传入的毫秒值格式化成yyyy-MM-dd或者今天
	 * @param Context long
	 * @return String
	 * @see DateFormatUtil#getDateInfoString
	 */
	@SuppressLint("SimpleDateFormat") 
	public static String getDateInfoString(Context context, long longTime){
		String formatDate = null;
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		Date date = new Date(longTime);
		String inPutDate = formatter.format(date);
		date = new Date(System.currentTimeMillis());
		String currentDate = formatter.format(date);
		if(currentDate.equals(inPutDate)){
			formatDate = context.getResources().getString(R.string.today);
		}else{
			formatDate = inPutDate;
		}
		return formatDate;
	}
}

