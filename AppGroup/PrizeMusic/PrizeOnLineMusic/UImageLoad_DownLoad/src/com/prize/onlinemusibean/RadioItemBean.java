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

/**
 * 
 **
 * 电台对象
 * 
 * @author longbaoxiu
 * @version V1.0
 */
public class RadioItemBean implements Serializable {

	/** 用一句话描述这个变量表示什么 */
	private static final long serialVersionUID = -1347952112592733143L;
	/** 电台 id */
	public int radio_id;
	/** 电台播放数 */
	public int play_count;
	/** 电台logo */
	public String radio_logo;
	/** 电台名 */
	public String radio_name;
	/** 描述信息 */
	public String desc;
	/** 电台类型，原创为original，其他为none */
	public String category_type;
	public int type;

	@Override
	public String toString() {
		return "RadioItemBean [radio_id=" + radio_id + ", play_count="
				+ play_count + ", radio_logo=" + radio_logo + ", radio_name="
				+ radio_name + ", desc=" + desc + ", category_type="
				+ category_type + "]";
	}

}
