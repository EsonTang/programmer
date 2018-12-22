package com.android.prize.salesstatis.util;

import android.os.Parcel;
import android.os.Parcelable;

public class PhoneStute implements Parcelable {
	public Double latitude = 0.0;
	public Double longitude = 0.0;
	public String position=null;
	public PhoneStute(){
    }
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeString(position);
        out.writeDouble(latitude);;
        out.writeDouble(longitude);;
		
	}
	
	public static final Parcelable.Creator<PhoneStute> CREATOR = new Creator<PhoneStute>() {
        @Override
        public PhoneStute[] newArray(int size)
        {
            return new PhoneStute[size];
        }
        
        @Override
        public PhoneStute createFromParcel(Parcel in)
        {
            return new PhoneStute(in);
        }
    };
    
	
	public PhoneStute(Parcel in) {
		latitude = in.readDouble();
		longitude = in.readDouble();
		position = in.readString();
    }
}
