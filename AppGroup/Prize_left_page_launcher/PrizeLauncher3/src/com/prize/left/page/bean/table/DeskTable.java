package com.prize.left.page.bean.table;

import java.io.Serializable;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

/**
 * 网址导航类型表
 * @author fanjunchen
 *
 */
@SuppressWarnings("serial")
@Table(name = "t_desk")
public class DeskTable implements ITable, Serializable {

    /***/
    @Column(name = "id", isId = true)
    private String id;
	/**图片URL*/
    @Column(name = "iconUrl")
    public String iconUrl;
    /**名称*/
    @Column(name = "name")
    public String name;
    /**时间段*/
    @Column(name = "startTime")
    public String startTime;
    /**时间段*/
    @Column(name = "endTime")
    public String endTime;
    /**状态*/
    @Column(name = "status")
    public int status;
    
    @Override
	public String toString() {
		return "PushTable [id=" + id + ", iconUrl=" + iconUrl + ", name="
				+ name + ", startTime=" + startTime + ", endTime=" + endTime
				+ "]";
	}

	@Override
	public String getTableName() {
		return "t_desk";
	}
    
    public void toContentValues() {
    	
    }
}
