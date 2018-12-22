/************************************************************************
 * <p>Title: Fingerprint.java</p>
 * <p>Description: </p>
 * <p>Copyright (C), 1997-2014, Shenzhen Goodix Technology Co.,Ltd.</p>
 * <p>Company: Goodix</p>
 * @author  peng.hu
 * @date    2014-5-20
 * @version  1.0
 ************************************************************************/
package com.goodix.util;

import android.os.Parcelable;
import android.os.Parcel;

/**
 * <p>
 * Title: Fingerprint.java
 * </p>
 * <p>
 * Description: ָ�����ݣ�֧��AIDL
 * </p>
 */
public class Fingerprint implements Parcelable
{
    
    /**
     * @Fields key : �ؼ�����ָ�ƿ���Ψһ��ʾ��ֻ�ṩREAD
     */
    private final int key;
    
    
    /**
     * @Fields description : ָ����Ϣ����
     */
    public String description;
    
    /**
     * @Fields name : ָ�����ݵ�����
     */
    public String name;
    
    /**
     * @Fields uri : ָ�����ݴ洢λ��
     */
    public String uri;
    
    // ָ��Ψһ��ʾ��һ�����洢��ʹ��
    public Fingerprint(int key, String name, String description, String uri)
    {
        // TODO Auto-generated constructor stub
        this.key = key;
        this.description = description;
        this.name = name;
        this.uri = uri;
    }
    
    // δָ��Ψһ��ʾ����ʱ��֪��Ψһ��ʾʱʹ��
    public Fingerprint(String name, String description, String uri)
    {
        // TODO Auto-generated constructor stub
        this.key = -1;
        this.description = description;
        this.name = name;
        this.uri = uri;
    }
    
    private Fingerprint(Parcel in)
    {
        this.key = in.readInt();
        this.name = in.readString();
        this.description = in.readString();
        this.uri = in.readString();
    }
    
    public int describeContents()
    {
        return 0;
    }
    
    public void writeToParcel(Parcel out, int flags)
    {
        out.writeInt(this.key);
        out.writeString(this.name);
        out.writeString(this.description);
        out.writeString(this.uri);
    }
    
    public static final Parcelable.Creator<Fingerprint> CREATOR = new Parcelable.Creator<Fingerprint>()
    {
        public Fingerprint createFromParcel(Parcel in)
        {
            return new Fingerprint(in);
        }
        
        public Fingerprint[] newArray(int size)
        {
            return new Fingerprint[size];
        }
    };
    
    public void setFingerprint(String name, String description, String uri)
    {
        this.description = description;
        this.name = name;
        this.uri = uri;
    }
    
    @Override
    public String toString()
    {
        return String.format(
                "Fingerprint : key = %d , name = %s ,des = %s , uri = %s",
                this.key, this.name, this.description, this.uri);
    }
    
    public static final void assignTo(Fingerprint dst,Fingerprint src)
    {
        dst.name = src.name;
        dst.description = src.description;
        dst.uri = src.uri;
    }
    
    public Fingerprint getFingerprint()
    {
        return this;
    }
    
    public void setName(String name)
    {
        this.name = name;
    }
    
    public String getName()
    {
        return this.name;
    }
    
    public void setDescription(String description)
    {
        this.description = description;
    }
    
    public String getDescription()
    {
        return this.description;
    }
    
    public void setUri(String uri)
    {
        this.uri = uri;
    }
    
    public String getUri()
    {
        return this.uri;
    }
    
    public int getKey()
    {
        return this.key;
    }
}