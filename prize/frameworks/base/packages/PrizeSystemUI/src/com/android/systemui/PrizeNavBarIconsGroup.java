/**
 * Nav bar color customized feature.
 * created by prize-linkh-2017.07.10
 *
 */
package com.android.systemui;

public final class PrizeNavBarIconsGroup {
    public static final int[][] RECENTS_ICONS = {
         // portrait
        {
            R.drawable.ic_sysbar_recent_white_prize, // white style.
            R.drawable.ic_sysbar_recent_gray_prize // gray style.
        },
         // landscape
        {
            R.drawable.ic_sysbar_recent_land_white_prize, // white style.
            R.drawable.ic_sysbar_recent_land_gray_prize // gray style.
        },
    };
    public static final int[][] RECENTS_ICONS_WITH_DOCKED = {
         // portrait
        {
            R.drawable.ic_sysbar_recent_docked_white_prize, // white style.
            R.drawable.ic_sysbar_recent_docked_gray_prize // gray style.
        },
         // landscape
        {
            R.drawable.ic_sysbar_recent_docked_land_white_prize, // white style.
            R.drawable.ic_sysbar_recent_docked_land_gray_prize // gray style.
        },
    };    
    public static final int[][] HOME_ICONS = {
         // portrait
        {
            R.drawable.ic_sysbar_home_white_prize, // white style.
            R.drawable.ic_sysbar_home_gray_prize // gray style.
        },
         // landscape
        {
            R.drawable.ic_sysbar_home_land_white_prize, // white style.
            R.drawable.ic_sysbar_home_land_gray_prize // gray style.
        },
    };
    public static final int[][] BACK_ICONS = {
         // portrait
        {
            R.drawable.ic_sysbar_back_white_prize, // white style.
            R.drawable.ic_sysbar_back_gray_prize // gray style.
        },
         // landscape
        {
            R.drawable.ic_sysbar_back_land_white_prize, // white style.
            R.drawable.ic_sysbar_back_land_gray_prize // gray style.
        },
    };
    public static final int[][] BACK_IME_ICONS = {
         // portrait
        {
            R.drawable.ic_sysbar_back_ime_white_prize, // white style.
            R.drawable.ic_sysbar_back_ime_gray_prize // gray style.
        },
         // landscape
        {
            R.drawable.ic_sysbar_back_ime_land_white_prize, // white style.
            R.drawable.ic_sysbar_back_ime_land_gray_prize // gray style.
        },
    };
    public static final int[][] MENU_ICONS = {
         // portrait
        {
            R.drawable.ic_sysbar_menu_white_prize, // white style.
            R.drawable.ic_sysbar_menu_gray_prize // gray style.
        },
         // landscape
        {
            R.drawable.ic_sysbar_menu_land_white_prize, // white style.
            R.drawable.ic_sysbar_menu_land_gray_prize // gray style.
        },
    }; 
    public static final int[][] IME_ICONS = {
         // portrait
        {
            R.drawable.ic_ime_switcher_default, // white style.
            R.drawable.ic_ime_switcher_default_gray_prize // gray style.
        },
         // landscape
        {
            R.drawable.ic_ime_switcher_default, // white style.
            R.drawable.ic_ime_switcher_default_gray_prize // gray style.
        },
    }; 
    public static final int[][] HIDE_ICONS = {
         // portrait
        {
            R.drawable.ic_hide_navbar_white_prize, // white style.
            R.drawable.ic_hide_navbar_gray_prize // gray style.
        },
         // landscape
        {
            R.drawable.ic_hide_navbar_land_white_prize, // white style.
            R.drawable.ic_hide_navbar_land_gray_prize // gray style.
        },
    };
    public static final int[][] HIDE_ICONS_LAND_ALT = {
         // portrait
        {
            R.drawable.ic_hide_navbar_white_prize, // white style.
            R.drawable.ic_hide_navbar_gray_prize // gray style.
        },
         // landscape
        {
            R.drawable.ic_hide_navbar_270_land_white_prize, // white style.
            R.drawable.ic_hide_navbar_270_land_gray_prize // gray style.
        },
    };

    public static int[][] getIconsGroup(int id) {
        return getIconsGroup(id, false/*docked*/, false/*back alt*/, false);
    }

    public static int[][] getIconsGroup(int id, boolean isDocked, boolean isBackAlt, boolean isHideLandAlt) {
        int[][] iconsGroup = null;
        
        switch(id) {
        case R.id.recent_apps:
            if (isDocked) {
                iconsGroup = RECENTS_ICONS_WITH_DOCKED;
            } else {
                iconsGroup = RECENTS_ICONS;
            }
            break;
        case R.id.home:
            iconsGroup = HOME_ICONS;
            break;
        case R.id.back:
            if (isBackAlt) {
                iconsGroup = BACK_IME_ICONS;
            } else {
                iconsGroup = BACK_ICONS;
            }             
            break;
        case R.id.menu:
            iconsGroup = MENU_ICONS;
            break;
        case R.id.ime_switcher:
            iconsGroup = IME_ICONS;
            break;
        case R.id.hide:
            if (isHideLandAlt) {
                iconsGroup = HIDE_ICONS_LAND_ALT;
            } else {
                iconsGroup = HIDE_ICONS;
            }
            break;
        
        }

        return iconsGroup;
    }

    public static int getIcon(int id, int orientation, int style) {
        return getIcon(id, orientation, style, false/*docked*/, false/*back alt*/, false);
    }

    public static int getIcon(int id, int orientation, int style, boolean isDocked, 
                boolean isBackAlt, boolean isHideLandAlt) {
        int icon = 0;
        int[][] iconsGroup = getIconsGroup(id, isDocked, isBackAlt, isHideLandAlt);
        if (iconsGroup != null) {
            icon = iconsGroup[orientation][style];
        }

        return icon;
    }
}
