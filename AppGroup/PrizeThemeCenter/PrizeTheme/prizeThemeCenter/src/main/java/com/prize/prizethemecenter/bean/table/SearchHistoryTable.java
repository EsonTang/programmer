package com.prize.prizethemecenter.bean.table;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

import java.io.Serializable;

/**
 * @author pengy  搜索记录
 */
@SuppressWarnings("serial")
@Table(name = "table_history")
public class SearchHistoryTable implements ITable, Serializable {

    /***/
    @Column(name = "id", isId = true)
    public int id;
	/**记录关键字*/
    @Column(name = "word")
    public String word;
    /**大图片URL*/
    @Column(name = "timestamp")
    public long timestamp;
    @Column(name = "type")
    public String type;

    @Override
	public String getTableName() {
        return "table_history";
	}
}
