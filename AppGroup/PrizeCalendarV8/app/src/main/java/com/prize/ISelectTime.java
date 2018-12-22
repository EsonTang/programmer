
 /*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：新建事件界面的时间选择完成后的回调
 *当前版本：1.0
 *作	者：wanzhijuan
 *完成日期：2015-6-25
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

package com.prize;
public interface ISelectTime {
	void onSelectDone(boolean isStart, int year, int month, int day, int hour, int mins);
	void onSelectDone(int dialogtype, int year, int month, int day, int hour, int mins);
}

