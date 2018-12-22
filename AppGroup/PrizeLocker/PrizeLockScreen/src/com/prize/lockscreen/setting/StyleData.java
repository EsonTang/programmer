package com.prize.lockscreen.setting;

import android.os.Parcel;
import android.os.Parcelable;

public class StyleData implements Parcelable {

	
	public String name;
	/**样式类型 1默认**/
	public int styleType = 1;
	/**小图*/
	public int sImgResId = 0;
	/**原图*/
	public int imgResId = 0;
	/**需要应用的壁纸资源ID*/
	public int bgImgResId;
	
	public StyleData() {
	}

	@Override
	public int describeContents() {
		return 0;
	}
	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeString(name);
        out.writeInt(styleType);
        out.writeInt(sImgResId);
        out.writeInt(imgResId);
        out.writeInt(bgImgResId);
    }

    public static final Parcelable.Creator<StyleData> CREATOR
            = new Parcelable.Creator<StyleData>() {
        public StyleData createFromParcel(Parcel in) {
            return new StyleData(in);
        }

        public StyleData[] newArray(int size) {
            return new StyleData[size];
        }
    };
    
    private StyleData(Parcel in) {
    	name = in.readString();
    	styleType = in.readInt();
    	sImgResId = in.readInt();
    	imgResId = in.readInt();
    	bgImgResId = in.readInt();
    }
}
