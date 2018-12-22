package com.prize.left.page.bean.table;


import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

/**
 * 用户表
 * @author fanjunchen
 *
 */
@Table(name = "t_user")
public class UserTable implements ITable {

    @Column(name = "id", isId = true)
    private int id;
    /**账号*/
    @Column(name = "code")
    public int code;
    /**显示名称*/
    @Column(name = "name")
    public String name;
    /**头像URL*/
    @Column(name = "headUrl")
    public String headUrl;
    /**是否可以删除*/
    @Column(name = "sex")
    public String sex;

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
                ", headUrl='" + headUrl + '\'' +
                ", sex='" + sex + '\'' +
                '}';
    }
    
    @Override
	public String getTableName() {
		return "t_user";
	}
}
