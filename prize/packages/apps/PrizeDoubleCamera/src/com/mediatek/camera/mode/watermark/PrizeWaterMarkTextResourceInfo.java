package com.mediatek.camera.mode.watermark;

import android.widget.TextView;

public class PrizeWaterMarkTextResourceInfo {
	
	private int rid = -1;
	
	public static final String DBSQL_TEXTALBUMDATAID = "rid";
	
	private int mid = -1;
	
	public static final String DBSQL_TEXTDATAID = "mid";
	
	private String mInitString ="";
	
	public static final String DBSQL_TEXTINITSTRING = "word";
	
	private String mShowString;
	
	private int msize = 0;
	
	public static final String DBSQL_TEXTSIZE = "text_size";
	
	/**jugdy text isn't Vertical*/
	private boolean text_v = false;
	
	public static final String DBSQL_TEXT_V = "text_v";
	
	private String textColor = "#ffffff";
	
	public static final String DBSQL_TEXTCOLOR = "text_color";
	
	private int wType = 0;//(0:普通，1：时间，2：地点)
	
	public static final String DBSQL_TEXTTYPE = "wtype";
	
	private int TextXPostion = 0;
	
	public static final String DBSQL_TEXTXPOSTION = "x";
	
	private int TextYPostion = 0;
	
	public static final String DBSQL_TEXTYPOSTION = "y";
	
	private int limits = 0;

	public static final String DBSQL_TEXTLIMITS ="text_limit";
	
	private String Time_String_type = "";
	
	public static final String DBSQL_TEXTTIMETYPE = "t_time";
	
	private int TextViewAlign =0;
	
	public static final String DBSQL_TEXTVIEWALIGN = "align";
	
	private TextView mTextView = null;

	public void setTextView(final TextView mTextView){
		this.mTextView = mTextView;
	}
	
	public TextView getTextView(){
		return this.mTextView;
	}
	
	public void setAlbumDataId(int rid){
		this.rid = rid;
	}
	
	public int getAlbumDataId(){
		return this.rid;
	}
	
	public void setTextDataId(int mid){
		this.mid = mid;
	}
	
	public int getTextDataId(){
		return this.mid;
	}
	
	public void setInitString(String mInitString){
		this.mInitString = mInitString;
		this.mShowString = mInitString;
	}
	
	public String getInitString(){
		return this.mInitString;
	}
	
	public void setShowString(String mShowString){
		/*prize-xuchunming-20171211-bugid:44931-start*/
		if(mShowString!=null){
		/*prize-xuchunming-20171211-bugid:44931-end*/
			this.mShowString = mShowString;
		}else{
			this.mShowString = this.mInitString;
		}	
	}
	
	public String getShowString(){
		return this.mShowString;
	}
	
	public void setTextSize(int msize){
		this.msize = msize;
	}
	
	public int getTextSize(){
		return this.msize;
	}
	
	public void setTextIsVertical(boolean text_v){
		this.text_v = text_v;
	}
	
	public boolean getTextIsVertical(){
		return this.text_v;
	}
	
	public void setTextColor(String textColor){
		this.textColor = textColor;
	}
	
	public String getTextColor(){
		return this.textColor;
	}
	
	public void setTextWtype(int wType){
		this.wType = wType;
	}
	
	public int getTextWtype(){
		return this.wType;
	}
	
	public void setTextXPostion(int TextXPostion){
		this.TextXPostion = TextXPostion;
	}
	
	public int getTextXPostion(){
		return this.TextXPostion;
	}
	
	public void  setTextYPostion(int TextYPostion){
		this.TextYPostion = TextYPostion;
	}
	
	public int getTextYPostion(){
		return this.TextYPostion;
	}
	
	public void setTextLimitPostion(int limits){
		this.limits = limits;
	}
	
	public int getTextLimitPostion(){
		return this.limits;
	}
	
	public void setTimeTextType(String Time_String_type){
		this.Time_String_type = Time_String_type;
	}
	
	public String getTimeTextType(){
		return this.Time_String_type;
	}
	
	public void setTextViewAlign(int TextViewAlign){
		this.TextViewAlign = TextViewAlign;
	}
	
	public int getTextViewAlign(){
		return this.TextViewAlign;
	}
	
	
}
