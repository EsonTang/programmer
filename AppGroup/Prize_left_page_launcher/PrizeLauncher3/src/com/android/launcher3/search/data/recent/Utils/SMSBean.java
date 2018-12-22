
/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：短信 
 *当前版本：V1.0
 *作	者：zhouerlong
 *完成日期：2015-8-5
 *********************************************/

package com.android.launcher3.search.data.recent.Utils;

public class SMSBean {

	private String thread_id;
	private String msg_count;
	/**
	 * 短信内容
	 */
	private String msg_snippet;
	/**
	 * 短信标题或者号码
	 */
	private String address;
	/**
	 * 时间
	 */
	private Long date;
	/**
	 * 是否可读
	 */
	private String read;

	public SMSBean(String threadId, String msgCount, String msgSnippet) {
		thread_id = threadId;
		msg_count = msgCount;
		msg_snippet = msgSnippet;
	}

	public SMSBean() {
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public Long getDate() {
		return date;
	}

	public void setDate(Long date) {
		this.date = date;
	}

	public String getRead() {
		return read;
	}

	public void setRead(String read) {
		this.read = read;
	}

	public String getThread_id() {
		return thread_id;
	}

	public void setThread_id(String threadId) {
		thread_id = threadId;
	}

	public String getMsg_count() {
		return msg_count;
	}

	public void setMsg_count(String msgCount) {
		msg_count = msgCount;
	}

	public String getMsg_snippet() {
		return msg_snippet;
	}

	public void setMsg_snippet(String msgSnippet) {
		msg_snippet = msgSnippet;
	}
}
