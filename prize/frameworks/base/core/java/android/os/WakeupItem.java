package android.os;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class WakeupItem implements Parcelable
{
	//State that indicates we disallow to launch/send service/broadcast be launching/sending
    public static final int STATE_DISALLOW = 0;    
    //State that indicates we allow to launch/send service/broadcast be launching/sending
    public static final int STATE_ALLOW = 1;
    //State that indicates we allow to launch/send service/broadcast be launching/sending if its process has already running.    
    public static final int STATE_MAY_DISALLOW = 2;
    //State that indicates we can't find the component to detemine whether this service/broadcast can start/send.
    public static final int STATE_NOT_FOUND_COMPONENT = 3;
    
	public String targetPkg;
	public String classname;
	public String action;
	public String callerpkg;
	public int state;
	
	public WakeupItem()
	{
		
	}
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		if (targetPkg == null) {
            dest.writeInt(0);
        } else {
            dest.writeInt(1);
            dest.writeString(targetPkg);
        }
		
		if (classname == null) {
            dest.writeInt(0);
        } else {
            dest.writeInt(1);
            dest.writeString(classname);
        }
		
		if (action == null) {
            dest.writeInt(0);
        } else {
            dest.writeInt(1);
            dest.writeString(action);
        }
		
		if (callerpkg == null) {
            dest.writeInt(0);
        } else {
            dest.writeInt(1);
            dest.writeString(callerpkg);
        }
		dest.writeInt(state);
		
	}
	public void readFromParcel(Parcel source) {
		targetPkg = source.readInt() > 0 ? source.readString() : null;
		classname = source.readInt() > 0 ? source.readString() : null;
		action = source.readInt() > 0 ? source.readString() : null;
		callerpkg = source.readInt() > 0 ? source.readString() : null;
		state = source.readInt();            
    }
	private WakeupItem(Parcel source) {
        readFromParcel(source);
    }
    public static final Creator<WakeupItem> CREATOR
            = new Creator<WakeupItem>() {
        public WakeupItem createFromParcel(Parcel source) {
            return new WakeupItem(source);
        }
        public WakeupItem[] newArray(int size) {
            return new WakeupItem[size];
        }
    };
}
