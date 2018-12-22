package com.prize.left.page.bean.table;


import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

/**
 * 卡片类型触发表
 * @author fanjunchen
 *
 */
@Table(name = "t_card_trigger")
public class CardTriggerTable implements ITable {

    @Column(name = "id", isId = true)
    private int id;
    /**卡片类型*/
    @Column(name = "cardType")
    public int cardType;
    /**开始时间*/
    @Column(name = "startTime")
    public String start;
    /**结束时间*/
    @Column(name = "endTime")
    public String end;
    /**需要访问的URL或包名*/
    @Column(name = "_url")
    public String url;
    /**需要访问的类名*/
    @Column(name = "cls")
    public String cls;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "CardTriggerTable {" +
                "id=" + getId() +
                ", cardType='" + cardType + '\'' +
                ", start='" + start + '\'' +
                ", end='" + end + '\'' +
                ", cls='" + cls + '\'' +
                ", url='" + url + '\'' +
                '}';
    }

	@Override
	public String getTableName() {
		return "t_card_trigger";
	}
}
