package com.prize.prizethemecenter.bean.table;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

import java.io.Serializable;

/**
 * 字体表
 * @author
 */
@SuppressWarnings("serial")
@Table(name = "table_font")
public class FontTable implements ITable, Serializable {

    /***/
    @Column(name = "id", isId = true)
    public int id;
	/**标题Title*/
    @Column(name = "title")
    public String title;
    /**价格Price*/
    @Column(name = "price")
    public String price;
    /**图片URL*/
    @Column(name = "ad_pictrue")
    public String ad_pictrue;

    @Column(name="isBuy")
    public boolean isBuy;

    @Override
	public String getTableName() {
        return "table_font";
	}

}
