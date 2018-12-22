package com.mediatek.camera.mode.watermark;

import java.util.ArrayList;

public class PrizeWaterMarkAlbum {

	private int _albumid = 0;
	
	public static final String DBSQL_ALBUMIDNAME = "_id" ;
	
	private String _albumname = "";
	
	public static final String DBSQL_ALBUMNAME = "tname";
	
	private ArrayList<PrizeWaterMarkThumbInfo> WaterMarkThumbList = null;
	
	public void setAlbumid(int _albumid){
		this._albumid = _albumid;
	}
	
	public int getAlbumId(){
		return _albumid;
	}
	
	public void setAlbumName(String _albumname){
		this._albumname = _albumname;
	}
	
	public String getAlbumName(){
		return this._albumname;
	}
	
	public void setWaterMarkThumbList(ArrayList<PrizeWaterMarkThumbInfo> WaterMarkThumbList){
		this.WaterMarkThumbList = WaterMarkThumbList;
	}
	
	public ArrayList<PrizeWaterMarkThumbInfo> getWaterMarkThumbList(){
		return this.WaterMarkThumbList;
	}
	
}
