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
@Table(name = "t_net_navi")
public class NetNaviTable implements ITable, Serializable {

    /**网址导航编码*/
    @Column(name = "code", isId = true)
    private String code;
    
    public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}
	/**图片URL*/
    @Column(name = "iconUrl")
    public String iconUrl;
    /**网址导航 名称*/
    @Column(name = "name")
    public String name;
    /**类型*/
    @Column(name = "downUrl")
    public String downUrl;
    /**类型排序号*/
    @Column(name = "_sort")
    public int sort;
    /**可用状态 1:可用, 否则不可用, 这个字段主要是用来与网络同步*/
    @Column(name = "status")
    public int status = 1;
    @Column(name = "cid")
    public int cid;
    public String getId() {
        return code;
    }

    public void setId(String id) {
        this.code = id;
    }

    @Override
    public String toString() {
        return "CardType {" +
                "code=" + getId() +
                ", name='" + name + '\'' +
                ", sort='" + sort + '\'' +
                ", downUrl='" + downUrl + '\'' +
                ", iconUrl='" + iconUrl + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
    
    @Override
	public String getTableName() {
		return "t_net_navi";
	}
    
    public void toContentValues() {
    	
    }
}
