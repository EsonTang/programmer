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

import com.prize.onlinemusibean.CollectBean;
import com.prize.onlinemusibean.SongDetailInfo;

/**
 * 
 **
 * 精选集详情data后的返回对象
 * 
 * @author longbaoxiu
 * @version V1.0
 */
public class CollectDetailResponse implements Serializable {

	/** 用一句话描述这个变量表示什么 */
	private static final long serialVersionUID = -1347952112592733143L;
	public int list_id;
	public String collect_name;
	public String collect_logo;

	public int song_count;
	public String user_name;
	public String author_avatar;
	public int play_count;
	public int user_id;
	/** 创建时间 */
	public int gmt_create;
	public String description;
	/** 精选集标签 元素类型为string */
	public String[] tag_array;
	/** 精选集标签 元素类型为string */
	public String[] tags;
	public ArrayList<SongDetailInfo> songs;
}
