/*
* created. prize-linkh-20150909
*/

package com.android.systemui.statusbar;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.Icon;
import android.util.Log;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.Context;
import android.content.Intent;
import java.util.List;
import android.graphics.drawable.Drawable;
import android.app.Notification;
import com.android.systemui.R;
import android.view.View;
import android.widget.ImageView;
import android.widget.RemoteViews;    
import android.graphics.PorterDuff;
import android.service.notification.StatusBarNotification;
import com.android.internal.statusbar.StatusBarIcon;
import android.util.PrizeAppInstanceUtils;
import com.mediatek.common.prizeoption.PrizeOption;


public final class PrizeNotificaionIconUtil {
    private static final String TAG = "PrizeNotificaionIconUtil";
    private static final boolean DEBUG = false;
    public static final boolean GET_APP_ICON_FIRST = false;
    private Context mContext;
    private static PrizeNotificaionIconUtil mInstance = null;
    private static final int ADB_NOTIFICATION_ICON = R.drawable.stat_sys_adb_prize;
    
    private PrizeNotificaionIconUtil(Context context) {
        mContext = context;
    }
    
    public static PrizeNotificaionIconUtil getInstance(Context context) {
        if(mInstance == null) {
            mInstance = new PrizeNotificaionIconUtil(context);
        }
        
        return mInstance;
    }
    
    public static int getLaunchIcon(Context context, String pkg) {
        return getLaunchIcon(context, pkg, GET_APP_ICON_FIRST);
    }    
    
    public static int getLaunchIcon(Context context, String pkg, final boolean getAppIconFirst) {
        
        int iconId = 0;
        int appIcon = 0;

        if(DEBUG) {
            Log.d(TAG, "getLaunchIcon(): pkg=" + pkg + ", getAppIconFirst=" + getAppIconFirst);
        }
        
        if("android".equals(pkg)) {
            //Ignore the android pkg.
            return iconId;
        }
        
        if(pkg != null) {
            ApplicationInfo appInfo = null;        
            PackageManager pm = context.getPackageManager();
            
            // Try to get applicaion info and get app icon if available.
            // if failed, we retrieve the launch activity icon instead. 
            try {
                 //Maybe i need to set some flags to improve the speed of retrieving package info.
                appInfo = pm.getApplicationInfo(pkg, 0);
            } catch(NameNotFoundException e) {
            }
            
            if(DEBUG) {
                Log.d(TAG, "getLaunchIcon(): appInfo=" + appInfo);
            }
            
            if(appInfo != null && appInfo.icon > 0) {
                appIcon = appInfo.icon;
                if(DEBUG) {
                    Log.d(TAG, "getLaunchIcon(): found app icon--" +  Integer.toHexString(appIcon));
                }                
            }

            if(getAppIconFirst) {
                iconId = appIcon;
            }
            
            if(iconId <= 0) {
                // if getAppIconFirst variable is false, or  the app icon is invalid,
                // we retrieve the activity launch icon.
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                intent.setPackage(pkg);
                
                List<ResolveInfo> list = pm.queryIntentActivities(intent, PackageManager.GET_ACTIVITIES);
                if(list != null  && list.size() == 1) {
                    //Good. we only find out a activity with launch icon. Try to use it.
                    // If we find out two more icons, we can't figure out which to use and will
                    // ignore them.
                    ResolveInfo ri = list.get(0);
                    if(DEBUG) {
                        Log.d(TAG, "getLaunchIcon(): ri.activityInfo=" + ri.activityInfo);
                    }                                    
                    if(ri.activityInfo != null) {
                        iconId = ri.activityInfo.icon;
                        if(DEBUG) {
                            Log.d(TAG, "getLaunchIcon(): found launch icon--" + Integer.toHexString(iconId));                    
                        }
                    }
                }

                if(!getAppIconFirst && iconId <= 0 && appIcon > 0) {
                    //No suitable launch icon to choose. Use the app icon if available.
                    iconId = appIcon;
                }

            }
        }
        
        return iconId;
    }

    public static Drawable getApplicationIcon(Context context, String pkg) {
        Drawable d = null;
        try {
            d = context.getPackageManager().getApplicationIcon(pkg);            
        } catch(NameNotFoundException e) {
        }

        if(DEBUG) {
            Log.d(TAG, "getApplicationIcon(): drawable-" + d);   
        }        
        return d;
    }
    
    public static void useLaunchIconIfNecessary(Context context, String pkg, StatusBarIconView iconView) {
        if("android".equals(pkg)) {
            if(DEBUG) {
                Log.d(TAG, "useLaunchIconIfNecessary(). system app - return");
            }
            //Ignore the android notification.
            return;
        }
        
        if(pkg != null && iconView != null) {
            int iconId = getLaunchIcon(context, pkg);
            if(iconId > 0) {
                if(DEBUG) {
                    Log.d(TAG, "useLaunchIconIfNecessary(). set prio icon-" + Integer.toHexString(iconId));
                }                  
                iconView.mPriorIcon = iconId;
                iconView.mPriorDrawable = null;
            } else {
                //Ok. we can't find the best icon.
                // Should we use the default icon that android system provides or use that app provides?
                try {
                    iconView.mPriorDrawable = context.getPackageManager().getApplicationIcon(pkg);
                    if(DEBUG) {
                        Log.d(TAG, "useLaunchIconIfNecessary(). set prio drawable-" + iconView.mPriorDrawable);               
                    }                    
                } catch(NameNotFoundException e) {
                    iconView.mPriorDrawable = null;
                }
             }                
         }
    } 
        
    public static Drawable getLaunchIconIfAvailable(StatusBarIconView iconView) {
        Drawable d = null;
        if(iconView == null) {
            return d;
        }
        if(DEBUG) {
            Log.d(TAG, "getLaunchIconIfAvailable(). iconView=" + iconView);
            Log.d(TAG, "mPriorIcon=" + Integer.toHexString(iconView.mPriorIcon) + ", priorDrawable=" + iconView.mPriorDrawable);
        }           
        if(iconView.mPriorIcon > 0 || iconView.mPriorDrawable != null) {
            if(iconView.mPriorDrawable != null) {
				if(DEBUG) {
	                Log.d(TAG, "getLaunchIconIfAvailable(). iconView.mPriorDrawable=" + iconView.mPriorDrawable);			
				}
                d = iconView.mPriorDrawable;
            } else {
				if(DEBUG) {
            		Log.d(TAG, "getLaunchIconIfAvailable(). iconView.getIcon=");
				}
                d = iconView.getIcon();
            }
        }
        if(DEBUG) {
            Log.d(TAG, "getLaunchIconIfAvailable(). d=" + d);
        }          
        return d;
    }
    
    public static boolean shouldIgnoreReplacing(Notification n) {
        boolean ignore = false;
        if(n != null && (n.flags & Notification.FLAG_KEEP_NOTIFICATION_ICON) != 0) {
            ignore = true;
        }
        if(DEBUG) {
            Log.d(TAG, "shouldIgnoreReplacing().N=" + n + ", ignore=" + ignore);
        }           
        return ignore;
    }

    public static int getSpecialNotificationIcon(Context context, StatusBarNotification sbn) {
        int icon = 0;
        if(sbn == null || context == null) {
            return icon;
        }
        
        final Notification n = sbn.getNotification(); 
        if(DEBUG) {
            Log.d(TAG, "getSpecialNotificationIcon(). n.getSmallIcon().getResId()=" + Integer.toHexString(n.getSmallIcon().getResId()));
        }          
        if(n.getSmallIcon().getResId() == com.android.internal.R.drawable.stat_sys_adb) {
            icon = ADB_NOTIFICATION_ICON;
        }
        
        /* app multi instances feature. prize-linkh-20151229 */
        if(PrizeOption.PRIZE_APP_MULTI_INSTANCES && icon <= 0) {
            PrizeAppInstanceUtils appInstanceUtils = PrizeAppInstanceUtils.getInstance(context);
            icon = appInstanceUtils.getIconIdForAppInst(sbn.getPackageName(), sbn.getAppInstanceIndex());
        }

        return icon;
    }
    
    // Some icons can't be convient to replace, for example, adb notification icon is a vector drawable,  
    // it's located in framework-res. when we use bitmap drawable to represent this icon, it makes
    // trouble. If we add these bitmap drawables in framework-res, the resource IDs will shift. If we modify the codes
    // that refrence this icon, we will modify more files. To make thing easy, i add this method. we 
    // replace them with SystemUI icons.
    public static Drawable getSpecialNotificationDrawable(Context context, StatusBarNotification sbn) {
        if(DEBUG) {
            Log.d(TAG, "getSpecialNotificationDrawable(). sbn=" + sbn);
        }         
        if(sbn == null || context == null) {
            return null;
        }
        
        Drawable d = null;
        int icon = getSpecialNotificationIcon(context, sbn);
        if(icon > 0) {
            d = context.getResources().getDrawable(icon);
        }
        if(DEBUG) {
            Log.d(TAG, "getSpecialNotificationDrawable(). d=" + d);
        }        

        return d;
    }

    public static boolean replaceNotificationIcon(Context context, NotificationData.Entry entry, View contentView,  int iconId, boolean isHeadsUp) {
        if(DEBUG) {
            Log.d(TAG, "replaceNotificationIcon()--isHeadsUp: "+ isHeadsUp);
        }
        boolean res = false;
        if(context == null || contentView == null || entry == null || iconId < 0) {
            return false;
        }

        Drawable specialDrawable = getSpecialNotificationDrawable(context, entry.notification);
        Drawable iconDrawable = null;
        if(isHeadsUp && entry.icon == null) {
            //The head-up notification doesn't have a status bar icon view, we can't 
            // get launch icon from it. So we must create a new status bar icon view.
            final StatusBarNotification sbn = entry.notification;
            final Notification n = sbn.getNotification();
            int icon_Id = getLaunchIcon(context, sbn.getPackageName());
            if(icon_Id > 0) {
                final StatusBarIcon ic = new StatusBarIcon(sbn.getPackageName(),
                            sbn.getUser(),
                            icon_Id,
                            n.iconLevel,
                            n.number,
                            n.tickerText);
                iconDrawable = StatusBarIconView.getIcon(context, ic);
            } else {
                try {
                    iconDrawable = context.getPackageManager().getApplicationIcon(sbn.getPackageName());
                } catch(NameNotFoundException e) {}
            }         
        } else {
            iconDrawable = getLaunchIconIfAvailable(entry.icon);    
        }

        View v = contentView.findViewById(iconId);
        if(v != null && v instanceof ImageView) {
            if(specialDrawable != null) {                
                res = true;
                ((ImageView)v).setImageDrawable(specialDrawable);
            } else if(iconDrawable != null) {            
                res = true;
                ((ImageView)v).setImageDrawable(iconDrawable);
            }
        }
        if(DEBUG) {
            Log.d(TAG, "replaceNotificationIcon()--res: "+ res);
        }
        return res;
    }

    /* prize add by xiarui for bug55086 2018-04-25 start */
    public static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = Bitmap
                .createBitmap(
                        drawable.getIntrinsicWidth(),
                        drawable.getIntrinsicHeight(),
                        drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                                : Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public static Icon getNewIcon(Context context, NotificationData.Entry entry) {
        Icon icon = null;
        if(context == null || entry == null) {
            return null;
        }
        int specialNotificationIcon = getSpecialNotificationIcon(context, entry.notification);
        Drawable iconDrawable = getLaunchIconIfAvailable(entry.icon);
        if (specialNotificationIcon > 0) {
            icon = Icon.createWithResource(context, specialNotificationIcon);
        } else if (iconDrawable != null) {
            Bitmap bitmap = drawableToBitmap(iconDrawable);
            icon = Icon.createWithBitmap(bitmap);
        }
        return icon;
    }
    /* prize add by xiarui for bug55086 2018-04-25 end */

    public static boolean replaceNotificationIcon(Context context, NotificationData.Entry entry, View contentView,  int iconId) {
        return replaceNotificationIcon(context, entry, contentView, iconId, false);
    }
    
    public static boolean clearNotificationIconColorFilter(StatusBarNotification sbn, RemoteViews contentView) {
        if(DEBUG) {
            Log.d(TAG, "clearNotificationIconColorFilter()");
        }
        boolean clear = true;
        if(sbn == null || contentView == null) {
            return false;
        }
        
        if(sbn.getNotification().largeIcon == null) {
            contentView.setDrawableParameters(com.android.internal.R.id.icon, false, -1,
                    0x00000000,
                    PorterDuff.Mode.SRC_ATOP, -1);
        } else {
            contentView.setDrawableParameters(com.android.internal.R.id.right_icon, false, -1,
                    0x00000000,
                    PorterDuff.Mode.SRC_ATOP, -1);
            contentView.setDrawableParameters(com.android.internal.R.id.icon, false, -1,
                    0x00000000,
                    PorterDuff.Mode.SRC_ATOP, -1);
        }

        return clear;
    }
    
}

