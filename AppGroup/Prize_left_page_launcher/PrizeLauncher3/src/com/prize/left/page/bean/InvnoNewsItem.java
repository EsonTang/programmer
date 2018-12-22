package com.prize.left.page.bean;

import java.io.Serializable;
import java.util.List;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

import com.prize.left.page.bean.table.ITable;

@SuppressWarnings("serial")
@Table(name = "table_news")
public class InvnoNewsItem implements Serializable,ITable {
	@Column(name="id",isId=true)
	public int id;
	/** 图片地址 */
	@Column(name="imageUrl")
	public String imageUrl;
	/** 新闻标题 */
	@Column(name="title")
	public String title;
	/** 新闻来源 */
	@Column(name="src")
	public String src;
	/** 新闻地址 */
	@Column(name="surl")
	public String surl;
	/** 发布时间 */
	@Column(name="time")
	public String time;
	
	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getSrc() {
		return src;
	}

	public void setSrc(String src) {
		this.src = src;
	}

	public String getSurl() {
		return surl;
	}

	public void setSurl(String surl) {
		this.surl = surl;
	}
	public InvnoNewsItem() {
	}

	@Override
	public String getTableName() {
		// TODO Auto-generated method stub
		return "table_news";
	}

	@Override
	public String toString() {
		return "InvnoNewsItem [id=" + id + ", imageUrl=" + imageUrl
				+ ", title=" + title + ", src=" + src + ", surl=" + surl
				+ ", time=" + time + "]";
	}
	
}
