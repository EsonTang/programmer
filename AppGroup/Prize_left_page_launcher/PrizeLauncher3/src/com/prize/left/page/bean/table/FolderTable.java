package com.prize.left.page.bean.table;

import java.io.Serializable;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

/**
 * 网址导航类型表
 * @author fanjunchen
 *
 */
@SuppressWarnings("serial")
@Table(name = "t_folder")
public class FolderTable implements ITable, Serializable {

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

    /**分类Id*/
    @Column(name = "categoryId")
    public String categoryId;

	@Override
	public String getTableName() {
		return "t_folder";
	}
	
	
    
   



	@Override
	public String toString() {
		return "FolderTable [id=" + id + ", name=" + name + ", pkg=" + packageName
				+ ", type=" + type + ", categoryName=" + categoryName + "]";
	}







	public void toContentValues() {
    	
    }
}
