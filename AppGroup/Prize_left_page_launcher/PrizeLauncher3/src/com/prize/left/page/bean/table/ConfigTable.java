package com.prize.left.page.bean.table;


import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

/**
 * 配置表
 * @author fanjunchen
 *
 */
@Table(name = "t_config")
public class ConfigTable implements ITable {

    @Column(name = "id", isId = true)
    private int id;
    /**账号*/
    @Column(name = "_key")
    public String key;
    /**显示名称*/
    @Column(name = "val")
    public String value;
    /**描述*/
    @Column(name = "_des")
    public String des;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "ConfigTable {" +
                "id=" + getId() +
                ", key='" + key + '\'' +
                ", value='" + value + '\'' +
                ", des='" + des + '\'' +
                '}';
    }
    
    @Override
	public String getTableName() {
		return "t_config";
	}
}
