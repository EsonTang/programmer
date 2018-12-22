package com.prize.left.page.bean.table;

import java.io.Serializable;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

/**
 * 网址导航类型表
 * @author zhouerlong
 *
 */
@SuppressWarnings("serial")
@Table(name = "t_Recommd")
public class RecommdTable implements ITable, Serializable {
	
/*	  "id": 352,
      "name": "QQ阅读",
      "packageName": "com.qq.reader",
      "type": "应用",
      "categoryName": "资讯阅读",
      "downloadUrl": "http://192.168.1.148:8080/appstore/appinfo/download?appId=352",
      "iconUrl": "http://p17.qhimg.com/t01c6d62f4a3769a64a.png"*/
	
    /***/
    @Column(name = "id", isId = true)
    private String id;
    /**名称*/
    @Column(name = "name")
    public String name;

    /**名称*/
    @Column(name = "packageName")
    public String packageName;
    
    /**类型*/
    @Column(name = "type")
    public String type;

    /**分类名*/
    @Column(name = "categoryName")
    public String categoryName;

    /**分类名*/
    @Column(name = "downloadUrl")
    public String downloadUrl;

    /**分类名*/
    @Column(name = "iconUrl")
    public String iconUrl;

	@Override
	public String getTableName() {
		return "t_Recommd";
	}


	public void toContentValues() {
    	
    }
}
