/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：
 *当前版本：1.0.0
 *作	者：yiyi
 *完成日期：2015年7月23日
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
package com.prize.cloud.bean;

import java.util.Map;

import org.codehaus.jackson.type.TypeReference;

import com.prize.cloud.util.GObjectMapper;

/**
 * 服务器下发json 
 * 其实就是一个bean类
 * @author yiyi
 * @version 1.9 changed by   
 * 
 */
public class Respond {
	private int code;
	private String msg;
	private Object data;

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	public <T> T convert(TypeReference<T> typeRef) {
		return GObjectMapper.get().convertValue(data, typeRef);
	}

	/**
	 * JSON TO CLASS
	 * @param clazz 转换目标
	 * @return 转换目标
	 */
	public <T> T convert(Class<T> clazz) {
		if (clazz == Integer.class || clazz == String.class) {
			Map<String, Object> dataMap = (Map<String, Object>) data;
			for (Object s : dataMap.values()) {
				return (T) s;
			}
		}
		return GObjectMapper.get().convertValue(data, clazz);
	}

	@Override
	public String toString() {
		return "Respond [code=" + code + ", msg=" + msg + ", data=" + data
				+ "]";
	}
}
