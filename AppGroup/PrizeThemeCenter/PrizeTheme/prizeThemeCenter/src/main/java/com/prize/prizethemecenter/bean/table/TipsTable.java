package com.prize.prizethemecenter.bean.table;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

import java.io.Serializable;

/**
 * 提示词TABLE
 * @author pengy
 *
 */
@SuppressWarnings("serial")
@Table(name = "table_tips")
public class TipsTable implements ITable, Serializable {

    /***/
    @Column(name = "id", isId = true)
    public int id;
	/**提示语*/
    @Column(name = "tip")
    public String tip;

    @Override
	public String getTableName() {
        return "table_tips";
	}
}
