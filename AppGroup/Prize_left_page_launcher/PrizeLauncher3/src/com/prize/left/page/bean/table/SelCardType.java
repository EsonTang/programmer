package com.prize.left.page.bean.table;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

/**
 * 已选的卡片类型表
 * @author fanjunchen
 *
 */
@Table(name = "t_sel_cardType")
public class SelCardType implements ITable {

    @Column(name = "id", isId = true)
    private int id;
    /**类型编码*/
    @Column(name = "code")
    public int code;
    /**类型名称*/
    @Column(name = "name")
    public String name;
    /**类型排序号*/
    @Column(name = "_sort")
    public int sort = 99;
    /**是否可以删除*/
    @Column(name = "canDel")
    public boolean canDel = true;
    /**子类型编码*/
    @Column(name = "subCode")
    public int subCode = 0;
    /**类型编码*/
    @Column(name = "dataCode")
    public String dataCode;
    /**是否显示*/
    @Column(name = "status")
    public int status;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "SelCardType {" +
                "id=" + getId() +
                ", name='" + name + '\'' +
                ", code='" + code + '\'' +
                ", sort='" + sort + '\'' +
                ", canDel='" + canDel + '\'' +
                ", subCode='" + subCode + '\'' +
                '}';
    }
    
    @Override
	public String getTableName() {
		return "t_sel_cardType";
	}
}
