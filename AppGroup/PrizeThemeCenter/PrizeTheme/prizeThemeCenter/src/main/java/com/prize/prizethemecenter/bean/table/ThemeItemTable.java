package com.prize.prizethemecenter.bean.table;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

import java.io.Serializable;

/**
 * 首页热门主题TABLE
 * @author pengy
 *
 */
@SuppressWarnings("serial")
@Table(name = "table_themeItem")
public class ThemeItemTable implements ITable, Serializable {

    /***/
    @Column(name = "id", isId = true,autoGen = false)
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

    @Override
	public String getTableName() {
        return "table_themeItem";
	}

}
