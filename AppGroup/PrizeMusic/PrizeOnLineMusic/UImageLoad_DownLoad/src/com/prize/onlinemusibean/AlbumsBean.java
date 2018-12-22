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

import com.prize.app.beans.HomeAdBean;

/**
 * 
 **
 * 新碟上架(专辑)对象
 * 
 * @author longbaoxiu
 * @version V1.0
 */
public class AlbumsBean implements Serializable {

	/** 用一句话描述这个变量表示什么 */
	private static final long serialVersionUID = -1347952112592733143L;
	/** 专辑ID, BIGINT类型 */
	public String album_name;
	/** 专辑名称 */
	public int album_id;
	/** 专辑LOGO */
	public String album_logo;
	/** 艺人id, BIGINT类型 */
	public int artist_id;
	public String artist_name;
	public String artist_logo;
	/** 发布时间戳 */
	public int gmt_publish;
	public int song_count;
	/** 评分 */
	public String grade;
	/** 专辑类型 */
	public String album_category;
	public String company;
	public String language;
	public int cd_count;
	public String description;
}
