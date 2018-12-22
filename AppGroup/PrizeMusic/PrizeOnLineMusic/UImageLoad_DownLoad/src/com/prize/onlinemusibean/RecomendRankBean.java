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

package com.prize.onlinemusibean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 **
 * 推荐排行bean
 * 
 * @author longbaoxiu
 * @version V1.0
 */
public class RecomendRankBean implements Serializable {

	/** 用一句话描述这个变量表示什么 */
	private static final long serialVersionUID = -1347952112592733143L;
	public String title;
	public String logo;
	public String logo_middle;
	public String update_date;
	public String type;
	public String cycle_type;
	public ArrayList<SongDetailInfo> songs;

	@Override
	public String toString() {
		return "RecomendRankBean [title=" + title + ", logo=" + logo
				+ ", logo_middle=" + logo_middle + ", update_date="
				+ update_date + ", type=" + type + ", cycle_type=" + cycle_type
				+ "]";
	}

}
