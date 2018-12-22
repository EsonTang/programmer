
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
/**
 *****************************************
 * 版权所有©2015,深圳市铂睿智恒科技有限公司
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
*********************************************
 */

package com.prize.weather.city;
/**
 **
 * 类描述：
 * @author 作者
 * @version 版本
 */
public class CityEntity {
	public int id;
	public int pid;
	public int cCode;
	public int dCode;
	public int type;
	public String name;
	public int isEnable;
	public int isHot;
	
	
	 /**
	 * 方法描述：
	 * @param 参数名 说明
	 * @return 返回类型 说明
	 * @see 类名/完整类名/完整类名#方法名
	 */
	
	public CityEntity() {
		super();
	}
	
	
	
	 /**
	 * 方法描述：
	 * @param 参数名 说明
	 * @return 返回类型 说明
	 * @see 类名/完整类名/完整类名#方法名
	 */
	
	public CityEntity(int id, int pid, int cCode, int dCode, int type,
			String name, int isEnable, int isHot) {
		super();
		this.id = id;
		this.pid = pid;
		this.cCode = cCode;
		this.dCode = dCode;
		this.type = type;
		this.name = name;
		this.isEnable = isEnable;
		this.isHot = isHot;
	}


	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getPid() {
		return pid;
	}
	public void setPid(int pid) {
		this.pid = pid;
	}
	public int getcCode() {
		return cCode;
	}
	public void setcCode(int cCode) {
		this.cCode = cCode;
	}
	public int getdCode() {
		return dCode;
	}
	public void setdCode(int dCode) {
		this.dCode = dCode;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}



	public int getIsEnable() {
		return isEnable;
	}



	public void setIsEnable(int isEnable) {
		this.isEnable = isEnable;
	}



	public int getIsHot() {
		return isHot;
	}



	public void setIsHot(int isHot) {
		this.isHot = isHot;
	}
	
	
	
}

