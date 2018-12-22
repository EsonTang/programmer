package com.mediatek.camera.mode.watermark;

import java.util.ArrayList;

public class PrizeWaterMarkThumbInfo {
	
	private int _thumbid = 0;
	
	public static final String DBSQL_THUMBIDNAME = "_id";
	
	private int _albumid = 0;
	
	public static final String DBSQL_THUMALBUMIDNAME = "t_id";
	
	private String mThumbPathString = "";
	
	public static final String DBSQL_THUMPATHNAME = "thumbpath";
	
	private boolean isEnableEdit = false;
	
	public static final String DBSQL_ISEDITNAME = "sticker";
	
	private float m_width = 0.0f;
	
	public static final String DBSQL_WIDTHNAME ="m_width";
	
	private float m_height = 0.0f;
	
	public static final String DBSQL_HEIGHTNAME = "m_height";
	
	private boolean isColorChange = false;
	
	public static final String DBSQL_COLORCHANGENAME = "color_change";
	
	private float x_postion = 0.0f;
	
	public static final String DBSQL_XPOSTIONNAME = "x_postion";
	
	private float y_postion = 0.0f;
	
	public static final String DBSQL_YPOSTIONNAME = "y_postion";
	
	private ArrayList<PrizeWaterMarkImageResourceInfo> mImageResourceList = null;
	
	private ArrayList<PrizeWaterMarkTextResourceInfo> mTextResourceList = null;
	
	public void setThumbID(int _thumbid){
		this._thumbid = _thumbid;
	}
	
	public int getThumbID(){
		return this._thumbid;
	}
	
	public void setAlbumId(int _albumid){
		this._albumid = _albumid;
	}
	
	public int getAlbumId(){
		return this._albumid;
	}
	
	public void setThumbPathString(String mThumbPathString){
		this.mThumbPathString = mThumbPathString;
	}
	
	public String getThumbPathString(){
		return this.mThumbPathString;
	}
	
	public void setIsEdit(boolean isEnableEdit){
		this.isEnableEdit = isEnableEdit;
	}
	
	public boolean getIsEdit(){
		return this.isEnableEdit;
	}
	
	public void setImageResourceList(ArrayList<PrizeWaterMarkImageResourceInfo> mImageResourceList){
		this.mImageResourceList = mImageResourceList;
	}
	
	public ArrayList<PrizeWaterMarkImageResourceInfo> getImageResourceList(){
		return this.mImageResourceList;
	}

	public void setTextResourceList(ArrayList<PrizeWaterMarkTextResourceInfo> mTextResourceList){
		this.mTextResourceList = mTextResourceList;
	}
	
	public ArrayList<PrizeWaterMarkTextResourceInfo> getTextResourceList(){
		return this.mTextResourceList;
	}
	
	public void setMWidth( float m_width){
		this.m_width  = m_width;
	}
	
	public float getMWidth(){
		return this.m_width;
	}
	
	public void setMHeight(float m_height){
		this.m_height = m_height;
	}
	
	public float getMHeight(){
		return this.m_height;
	}
	
	public void setColorChange(boolean isColorChange){
		this.isColorChange = isColorChange;
	}
	
	public boolean getColorChange(){
		return this.isColorChange;
	}
	
	public void setXpostion(float x_postion){
		this.x_postion = x_postion;
	}
	
	public float  getXpostion (){
		return this.x_postion;
	}
	
	public void setYpostion(float y_postion){
		this.y_postion = y_postion;
	}
	
	public float getYpostion(){
		return this.y_postion;
	}
	
}
