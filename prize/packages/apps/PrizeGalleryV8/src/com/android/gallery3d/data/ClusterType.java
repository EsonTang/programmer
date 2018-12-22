package com.android.gallery3d.data;

/**
 * Created by Cony on 2017/7/20.
 */

public class ClusterType {
    public int mDatetaken;
    public int mCount;

    public ClusterType(int datetaken, int count) {
        mDatetaken = datetaken;
        mCount = count;
    }

    @Override
    public String toString() {
        return "[" + mDatetaken + "," + mCount + "]";
    }
}
