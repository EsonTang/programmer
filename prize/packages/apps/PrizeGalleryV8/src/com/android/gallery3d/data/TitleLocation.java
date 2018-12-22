package com.android.gallery3d.data;

import android.text.TextUtils;

import com.android.gallery3d.util.LogUtil;

import java.util.ArrayList;

/**
 * Created by Cony on 2017/8/14.
 */

public class TitleLocation {

    protected static final String TAG = "TitleLocation";
    public String mProvince;
    public int mCountryCount;
    public int mProvinceCount;
    public int mCityCount;
    public int mDistrictCount;
    public int mLocationCount;
    public boolean mHasAbroad;
    public boolean mInCn;

    public ArrayList<TitleLocation> mChildren = new ArrayList<>();


    public TitleLocation generate(String where, String country, String province, String city, String district, String street) {
        LogUtil.i(TAG, "generate country=" + country + " province=" + province + " city=" + city + " district=" + district + " street=" + street);

        TitleLocation countryLocation = hasChild(country);
        if (countryLocation == null) {
            countryLocation = new TitleLocation(country);
            countryLocation.mInCn = where.equals("1");
            if (!mHasAbroad && !countryLocation.mInCn) {
                mHasAbroad = true;
            }
            addChild(countryLocation);
            mCountryCount++;
        }

        TitleLocation provinceLocation = countryLocation.hasChild(province);
        if (provinceLocation == null) {
            provinceLocation = new TitleLocation(province);
            countryLocation.addChild(provinceLocation);
            mProvinceCount++;
        }

        TitleLocation cityLocation = provinceLocation.hasChild(city);
        if (cityLocation == null) {
            cityLocation = new TitleLocation(city);
            provinceLocation.addChild(cityLocation);
            mCityCount++;
        }


        TitleLocation districtLocation = cityLocation.hasChild(district);
        if (districtLocation == null) {
            districtLocation = new TitleLocation(district);
            cityLocation.addChild(districtLocation);
            mDistrictCount++;
        }

        TitleLocation streetLocation = districtLocation.hasChild(street);
        if (streetLocation == null) {
            streetLocation = new TitleLocation(street);
            districtLocation.addChild(streetLocation);
            mLocationCount++;
        }
        return this;
    }

    public TitleLocation(String province) {
        mProvince = province;
    }

    private void addChild(TitleLocation children) {
        mChildren.add(children);
    }

    private TitleLocation hasChild(String children) {
        for (TitleLocation titleLocation : mChildren) {
            if (titleLocation.mProvince.equals(children)) {
                return titleLocation;
            }
        }
        return null;
    }

    public static TitleLocation parseAddr(TitleLocation root, String addr) {
        if (!TextUtils.isEmpty(addr)) {
            String[] addrs = addr.split(":");
            if (addrs != null && addrs.length > 1) {
                String detailDivision = addrs[1];
                String[] locations = detailDivision.split(",");
                if (locations != null && locations.length == 6) {
                    return root.generate(locations[0], locations[1], locations[2], locations[3], locations[4], locations[5]);
                }
            }else if(addrs != null && addrs.length == 1){
                if(root.mHasAbroad == false) root.mHasAbroad = true;
				String localString = addrs[0];
				if(localString != null){
					if(localString.startsWith("中国")){
						localString = localString.substring(2);
					}
					root.addChild(new TitleLocation(localString));
				}
            }
        }
        return root;
    }
}
