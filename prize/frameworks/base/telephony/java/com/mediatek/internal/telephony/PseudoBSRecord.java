/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mediatek.internal.telephony;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Immutable cell information from a point in time.
 */
public class PseudoBSRecord implements Parcelable {

    public int mBsType;

    public int mBsPlmn;

    public int mBsLac;

    public int mBsCellId;

    public int mBsArfcn;

    public int mBsBsic;

    /** @hide */
    protected PseudoBSRecord() {
        this.mBsType = 0;
        this.mBsPlmn = 0;
        this.mBsLac = 0;
        this.mBsCellId = 0;
        this.mBsArfcn = 0;
        this.mBsBsic = 0;
    }

    /** @hide */
    protected PseudoBSRecord(PseudoBSRecord bs) {
        this.mBsType = bs.mBsType;
        this.mBsPlmn = bs.mBsPlmn;
        this.mBsLac = bs.mBsLac;
        this.mBsCellId = bs.mBsCellId;
        this.mBsArfcn = bs.mBsArfcn;
        this.mBsBsic = bs.mBsBsic;
    }

    public PseudoBSRecord(int type, int plmn, int lac, int cid, int arfcn, int bsic) {
        this.mBsType = type;
        this.mBsPlmn = plmn;
        this.mBsLac = lac;
        this.mBsCellId = cid;
        this.mBsArfcn = arfcn;
        this.mBsBsic = bsic;
    }


    @Override
    public int hashCode() {
        return mBsType + mBsPlmn + mBsLac + mBsCellId + mBsArfcn + mBsBsic;
    }

    private static boolean equalsHandlesNulls (Object a, Object b) {
        return (a == null) ? (b == null) : a.equals (b);
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (this == other) {
            return true;
        }
        PseudoBSRecord o;
        try {
            o = (PseudoBSRecord) other;
        } catch (ClassCastException ex) {
            return false;
        }

        return mBsType == o.mBsType
            && equalsHandlesNulls(mBsPlmn, o.mBsPlmn)
            && equalsHandlesNulls(mBsLac, o.mBsLac)
            && equalsHandlesNulls(mBsCellId, o.mBsCellId)
            && mBsArfcn == o.mBsArfcn
            && mBsBsic == o.mBsBsic;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(" mBsType=").append(mBsType);
        sb.append(" mBsPlmn=").append(mBsPlmn);
        sb.append(" mBsLac=").append(mBsLac);
        sb.append(" mBsCellId=").append(mBsCellId);
        sb.append(" mBsArfcn=").append(mBsArfcn);
        sb.append(" mBsBsic=").append(mBsBsic);
        return sb.toString();
    }

    /**
     * Implement the Parcelable interface
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /** Implement the Parcelable interface */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mBsType);
        dest.writeInt(mBsPlmn);
        dest.writeInt(mBsLac);
        dest.writeInt(mBsCellId);
        dest.writeInt(mBsArfcn);
        dest.writeInt(mBsBsic);
    }

    /**
     * Used by child classes for parceling
     *
     * @hide
     */
    protected PseudoBSRecord(Parcel in) {
        mBsType = in.readInt();
        mBsPlmn = in.readInt();
        mBsLac = in.readInt();
        mBsCellId = in.readInt();
        mBsArfcn = in.readInt();
        mBsBsic = in.readInt();
    }

    public int getType() {
        return this.mBsType;
    }

    public int getPlmn() {
        return this.mBsPlmn;
    }

    public int getLac() {
        return this.mBsLac;
    }

    public int getCi() {
        return this.mBsCellId;
    }

    public int getArfcn() {
        return this.mBsArfcn;
    }

    public int getBsic() {
        return this.mBsBsic;
    }

    /** Implement the Parcelable interface */
    public static final Creator<PseudoBSRecord> CREATOR = new Creator<PseudoBSRecord>() {
        @Override
        public PseudoBSRecord createFromParcel(Parcel in) {
            return new PseudoBSRecord(in);
        }

        @Override
        public PseudoBSRecord[] newArray(int size) {
            return new PseudoBSRecord[size];
        }
    };
}
