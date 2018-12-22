/*
* created for status bar style. prize-linkh-20150829
*/

package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.util.Log;
import java.util.HashMap;
import android.util.SparseIntArray;
import android.app.StatusBarManager;

public final class PrizeStatusIconCategory {
    private HashMap<Integer, SparseIntArray> mIconsCategory = new HashMap<Integer, SparseIntArray>();
    private Context mContext;
    
    public PrizeStatusIconCategory(Context context) {
        mContext = context;
    }

    public void addIcons(int orginIconId, int style, int styleIconId) {
        SparseIntArray styleIcons = mIconsCategory.get(orginIconId);
        if(styleIcons == null) {
            styleIcons = new SparseIntArray(StatusBarManager.STATUS_BAR_INVERSE_TOTAL);
            mIconsCategory.put(orginIconId, styleIcons);
        }
        
        styleIcons.put(style, styleIconId);
        
    }
    
    public int getIcon(int orginIconId, int style) {
        int icon = 0;
        SparseIntArray styleIcons = mIconsCategory.get(orginIconId);
        if(styleIcons != null) {
            icon = styleIcons.get(style);
        }
        
        return icon;
    }    
}
