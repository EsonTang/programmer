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
import java.util.Arrays;

/**
 * 
 **
 * 歌单bean
 * 
 * @author longbaoxiu
 * @version V1.0
 */
public class CollectBean implements Serializable {

	/** 用一句话描述这个变量表示什么 */
	private static final long serialVersionUID = 8121410557540665057L;
	/** 精选集ID, BIGINT类型 */
	public int list_id;
	/** 精选集名称 */
	public String collect_name;
	/** 精选集LOGO, 没有为空 */
	public String collect_logo;
	/** 歌曲总数 */
	public int song_count;
	/** 用户昵称 */
	public String user_name;
	/** 头像 */
	public String author_avatar;
	/** 播放次数 */
	public int play_count;
	/** 用户id, BIGINT类型 */
	public int user_id;
	/** 创建时间 */
	public int gmt_create;
	/** 描述信息 */
	public String description;
	/** 精选集标签 元素类型为string */
	public String[] tag_array;
	@Override
	public String toString() {
		return "CollectBean [list_id=" + list_id + ", collect_name="
				+ collect_name + ", collect_logo=" + collect_logo
				+ ", song_count=" + song_count + ", user_name=" + user_name
				+ ", author_avatar=" + author_avatar + ", play_count="
				+ play_count + ", user_id=" + user_id + ", gmt_create="
				+ gmt_create + ", description=" + description + ", tag_array="
				+ Arrays.toString(tag_array) + "]";
	}

}
