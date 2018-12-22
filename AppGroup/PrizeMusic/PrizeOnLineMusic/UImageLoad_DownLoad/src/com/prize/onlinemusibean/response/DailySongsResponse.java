/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：
 *当前版本：
 *作	者：
 *完成日期：
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

package com.prize.onlinemusibean.response;

import java.io.Serializable;
import java.util.ArrayList;

import com.prize.app.beans.HomeAdBean;
import com.prize.onlinemusibean.SongDetailInfo;
import com.prize.onlinemusibean.RecomendRankBean;

/**
 * 
 **
 * 今日歌单data后的返回对象
 * 
 * @author longbaoxiu
 * @version V1.0
 */
public class DailySongsResponse implements Serializable {

	/** 用一句话描述这个变量表示什么 */
	private static final long serialVersionUID = -1347952112592733143L;
	public ArrayList<SongDetailInfo> songs;
	/** 当天日期 **/
	public int day;
	public String title;
	public String logo;
	public int total;
}
