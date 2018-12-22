package com.prize.left.page.bean.table;

import java.io.Serializable;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

/**
 * 自定义卡片类型表
 * @author fanjunchen
 *
 */
@SuppressWarnings("serial")
@Table(name = "t_big_cardType")
public class BigCardType implements ITable, Serializable {

    @Column(name = "id", isId = true)
    private int id;
    /**大类编码*/
    @Column(name = "uitype")
    public String uitype;
    /**类型名称*/
    @Column(name = "name")
    public String name;
    /**类型排序号*/
    @Column(name = "_sort")
    public int sort;
    /**可用状态 1:可用, 否则不需要*/
    @Column(name = "status")
    public int status = 1;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    
	@Override
	public String toString() {
		return "BigCardType [id=" + id + ", uitype=" + uitype + ", name="
				+ name + ", sort=" + sort + ", status=" + status + "]";
	}

	@Override
	public String getTableName() {
		return "t_big_cardType";
	}
}
