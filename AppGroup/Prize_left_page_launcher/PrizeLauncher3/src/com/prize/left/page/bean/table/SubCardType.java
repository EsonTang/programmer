package com.prize.left.page.bean.table;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

/**
 * 频道类型表(卡片子类)
 * @author fanjunchen
 *
 */
@Table(name = "t_sub_cardType")
public class SubCardType implements ITable {

    @Column(name = "id", isId = true)
    private int id;
    /**频道编码*/
    @Column(name = "code")
    public int code;
    /**频道名称*/
    @Column(name = "name")
    public String name;
    /**新闻卡片类型*/
    @Column(name = "newsType")
    public int newsType;

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
                ", newsType='" + newsType + '\'' +
                '}';
    }
    
    @Override
	public String getTableName() {
		return "t_sub_cardType";
	}
}
