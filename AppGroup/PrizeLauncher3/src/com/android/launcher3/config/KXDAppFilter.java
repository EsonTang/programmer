package com.android.launcher3.config;

import android.content.ComponentName;
import android.text.TextUtils;

import com.android.launcher3.AppFilter;

/**
 * Filter out the special apps.
 * @author bxc @2014-06-12
 *
 */
public class KXDAppFilter extends AppFilter {

    private static final String [][] FILTER_OUT_COMPONENT =
        {
          // {pkgName, clsName}
          {"com.example.afasense", null /*"com.example.afasense/.AfaSense"*/},
        };

    @Override
    public boolean shouldShowApp(ComponentName app) {
        for (String [] comp : FILTER_OUT_COMPONENT) {
            String pkgName = comp[0];
            String clsName = comp[1];
            if (TextUtils.isEmpty(pkgName)) {
                continue;
            }
            if (pkgName.equals(app.getPackageName())) {
                if (TextUtils.isEmpty(clsName)
                        || TextUtils.isEmpty(app.getClassName())
                        || clsName.equals(app.getClassName())) {
                    return false;
                }
            }
        }
        return true;
    }
}
