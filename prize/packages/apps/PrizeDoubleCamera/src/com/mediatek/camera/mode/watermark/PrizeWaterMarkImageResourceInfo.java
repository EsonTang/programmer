package com.mediatek.camera.mode.watermark;

public class PrizeWaterMarkImageResourceInfo {
	
	private int rid = -1;
	
	public static final String DBSQL_RIDNAME = "rid";
	
	private int mid = -1;
	
	public static final String DBSQL_MIDNAME = "mid";
	
	private String imageresourcepath ="";
	
	public static final String DBSQL_RPATHNAME = "rpath";
	
	private float x = 0.0f;
	
	public static final String DBSQL_XPOSTIONNAME = "x";
	
	private float y = 0.0f;
	
	public static final String DBSQL_YPOSTIONNAME = "y";
	
	private int imagetype = 0;
	
	public static final String DBSQL_IMAGETYPENAME = "image_type";
	
	public void setDataAlbumId(int rid){
		this.rid = rid;
	}
	
	public int getDataAlbumId(){
		return this.rid;
	}
	
	public void setDataId(int mid){
		this.mid = mid;
	}
	
	public int getDataId(){
		return this.mid;
	}
	
	public void setImageDataPath(String imageresourcepath){
		this.imageresourcepath = imageresourcepath;
	}
	
	public String getImageDataPath(){
		return this.imageresourcepath;
	}
	
	public void setImageXPostion(float x){
		this.x = x;
	}
	
	public float getImageXPostion(){
		return this.x;
	}
	
	public void setImageYPostion(float y){
		this.y = y;
	}
	
	public float getImageYPostion(){
		return this.y;
	}
	
	public void setImageType(int imagetype){
		this.imagetype = imagetype;
	}
	
	public int getImageType(){
		return this.imagetype;
	}
	
}
