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
 * 电台分类对象
 * 
 * @author longbaoxiu
 * @version V1.0
 */
public class RadioCategorieBean implements Serializable {

	/** 用一句话描述这个变量表示什么 */
	private static final long serialVersionUID = -1347952112592733143L;
	/** 电台 分类id */
	public int category_id;
	/** 电台logo */
	public String radio_logo;
	/** 电台分类 */
	public String category_name;
	/** 电台类型，原创为original，其他为none */
	public String category_type;
}
