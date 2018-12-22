/***
 * function:watermark
 * author: wanzhijuan
 * date:   2016-1-21
 */
package com.prize.sticker;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;

public class WatermarkBean implements Parcelable, Cloneable {

	private int mThumbId;
	private String mType;
	private long mID;
	private boolean mIsSticker;
	private boolean mIsColorChange;
	private int mTextColor;
	private List<IWatermarkResource> mWatermarkResources = new ArrayList<IWatermarkResource>();
	
	public static final Parcelable.Creator<WatermarkBean> CREATOR = new Creator<WatermarkBean>() {

		@Override
		public WatermarkBean createFromParcel(Parcel source) {
			return new WatermarkBean(source);
		}

		@Override
		public WatermarkBean[] newArray(int size) {
			return new WatermarkBean[size];
		}
		
	};
	
	@Override  
    public Object clone(){  
		WatermarkBean wb = null;  
        try {  
            wb = (WatermarkBean) super.clone();  
        } catch (CloneNotSupportedException e){  
            e.printStackTrace();  
        }  
        return wb;  
    }  
	
	public WatermarkBean(Parcel in) {  
		mThumbId = in.readInt();
		mType = in.readString();
		mID = in.readLong();
		mIsSticker = in.readInt() == 1;
		mIsColorChange = in.readInt() == 1;
		mTextColor = in.readInt();
		int size = in.readInt();
		Parcelable parcelable;
		for (int i = 0; i < size; i++) {
			int type = in.readInt();
			if (type == IWatermarkResource.TYPE_PHOTO) {
				parcelable = in.readParcelable(PhotoResource.class.getClassLoader());
			} else {
				parcelable = in.readParcelable(WordResource.class.getClassLoader());
			}
			if (parcelable != null) {
				mWatermarkResources.add((IWatermarkResource) parcelable);
			}
		}
    }  
	
	public WatermarkBean(List<IWatermarkResource> watermarkResources) {
		if (watermarkResources != null && watermarkResources.size() > 0) {
			mWatermarkResources.addAll(watermarkResources);
		}
	}
	
	public WatermarkBean(int thumbId, String type, long id, List<IWatermarkResource> watermarkResources) {
		this(true, false, thumbId, type, id, watermarkResources);
	}
	
	public WatermarkBean(boolean isSticker, boolean isColorChange, int thumbId, String type, long id, List<IWatermarkResource> watermarkResources) {
		this(Color.BLACK, isSticker, isColorChange, thumbId, type, id, watermarkResources);
	}
	
	public WatermarkBean(int textColor, boolean isSticker, boolean isColorChange, int thumbId, String type, long id, List<IWatermarkResource> watermarkResources) {
		mTextColor = textColor;
		mIsSticker = isSticker;
		mIsColorChange = isColorChange;
		mThumbId = thumbId;
		mType = type;
		mID = id;
		if (watermarkResources != null && watermarkResources.size() > 0) {
			mWatermarkResources.addAll(watermarkResources);
		}
	}
	
	public int getThumbId() {
		return mThumbId;
	}

	public String getType() {
		return mType;
	}

	public void setType(String type) {
		this.mType = type;
	}

	public long getID() {
		return mID;
	}

	public boolean isSticker() {
		return mIsSticker;
	}

	public boolean isColorChange() {
		return mIsColorChange;
	}

	public List<IWatermarkResource> getWatermarkResources() {
		return mWatermarkResources;
	}
	
	public int getTextColor() {
		return mTextColor;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(mThumbId);
		dest.writeString(mType);
		dest.writeLong(mID);
		dest.writeInt(mIsSticker ? 1 : 0);
		dest.writeInt(mIsColorChange ? 1 : 0);
		dest.writeInt(mTextColor);
		int size = mWatermarkResources.size();
		dest.writeInt(size);
		for (int i = 0; i < size; i++) {
			mWatermarkResources.get(i).writeParcelable(dest, flags);
		}
	}

	@Override
	public String toString() {
		return "WatermarkBean [mThumbId=" + mThumbId + ", mType=" + mType
				+ ", mID=" + mID + ", mIsSticker=" + mIsSticker
				+ ", mIsColorChange=" + mIsColorChange + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (mID ^ (mID >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		WatermarkBean other = (WatermarkBean) obj;
		if (mID != other.mID)
			return false;
		return true;
	}
}
