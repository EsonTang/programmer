/**
 * Nav bar color customized feature. prize-linkh-2017.07.11
 */
package com.android.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;

public class PrizeNavBarColorUtil {
    public static final int PRE_WHITE_COLOR = 0xffededed;
    public static final int PRE_BLACK_COLOR = 0xff000000;
    public static final int PRE_GRAY_BLUE_COLOR = 0xff95b3d0;
    
    public static final int PRE_PINK_COLOR = 0xfff4dade;
    public static final int PRE_BROWN_COLOR = 0xffceb891;
    public static final int PRE_GRAY_COLOR = 0xffb1b1b1;
    
    public static final int[] WHITE_COLOR_IMAGE_RESOURCE = {
        R.drawable.default_white, /* normal state */
        R.drawable.default_white_selected /* selected state */
    };
    public static final int[] BLACK_COLOR_IMAGE_RESOURCE = {
        R.drawable.black,
        R.drawable.black_selected
    };
    public static final int[] GRAY_BLUE_COLOR_IMAGE_RESOURCE = {
        R.drawable.gray_blue,
        R.drawable.gray_blue_selected
    };
    public static final int[] PINK_COLOR_IMAGE_RESOURCE = {
        R.drawable.pink,
        R.drawable.pink_selected
    };
    public static final int[] BROWN_COLOR_IMAGE_RESOURCE = {
        R.drawable.brown,
        R.drawable.brown_selected
    };
    public static final int[] GRAY_COLOR_IMAGE_RESOURCE = {
        R.drawable.gray,
        R.drawable.gray_selected
    };    
    public static final int[] CUSTEOM_COLOR_IMAGE_RESOURCE = {
        R.drawable.custom,
        R.drawable.custom_selected
    }; 

    // used for shared preference.
    public static final int DEFAULT_WHITE_COLOR_IDX = 0;
    public static final int BLACK_COLOR_IDX = 1;
    public static final int GRAY_BLUE_COLOR_IDX = 2;
    public static final int PINK_COLOR_IDX = 3;
    public static final int BROWN_COLOR_IDX = 4;
    public static final int GRAY_COLOR_IDX = 5;
    public static final int CUSTOM_COLOR_IDX = 6;

    public static final String PREF_FILE_NAME = "NavBarColorPref";
    public static final String PREF_KEY_NAV_BAR_COLOR = "navBarColor";

    // used for activity result.
    public static final int REQUEST_CODE = 100;
    
    public static int getColorIndex(int id) {
        int idx = -1;
        
        switch(id) {
        case R.id.default_white_color:
            idx = DEFAULT_WHITE_COLOR_IDX;
            break;
        case R.id.black_color:
            idx = BLACK_COLOR_IDX;
            break;
        case R.id.gray_blue_color:
            idx = GRAY_BLUE_COLOR_IDX;           
            break;
        case R.id.pink_color:
            idx = PINK_COLOR_IDX;
            break;
        case R.id.brown_color:
            idx = BROWN_COLOR_IDX;
            break;
        case R.id.gray_color:
            idx = GRAY_COLOR_IDX;
            break;
        case R.id.custom_color:
            idx = CUSTOM_COLOR_IDX;
            break;
        }  

        return idx;
    }

    public static int getColorViewId(int colorIndex) {
        int id = 0;
        
        switch(colorIndex) {
        case DEFAULT_WHITE_COLOR_IDX:
            id = R.id.default_white_color;
            break;
        case BLACK_COLOR_IDX:
            id = R.id.black_color;
            break;
        case GRAY_BLUE_COLOR_IDX:
            id = R.id.gray_blue_color;           
            break;
        case PINK_COLOR_IDX:
            id = R.id.pink_color;
            break;
        case BROWN_COLOR_IDX:
            id = R.id.brown_color;
            break;
        case GRAY_COLOR_IDX:
            id = R.id.gray_color;
            break;
        case CUSTOM_COLOR_IDX:
            id = R.id.custom_color;
            break;
        }  

        return id;
    }    

    /*
    public static int readColorIndexFromSharedPref(Context context) {

        final SharedPreferences sp = context.getSharedPreferences(
                    PREF_FILE_NAME, Context.MODE_PRIVATE);
        final int colorIndex = sp.getInt(PREF_KEY_NAV_BAR_COLOR, 0);
        return colorIndex;
    }

    public static void writeColorIndexToSharedPref(Context context, int colorIndex) {

        final SharedPreferences sp = context.getSharedPreferences(
                    PREF_FILE_NAME, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sp.edit(); 
        editor.putInt(PREF_KEY_NAV_BAR_COLOR, colorIndex);
        editor.apply();
    }
    */
    public static int readColorIndexFromSettings(Context context) {
        return Settings.System.getInt(context.getContentResolver(), 
                "prize_nav_bar_color_index", 0);
    }

    public static void writeColorIndexToSettings(Context context, int colorIndex) {
        Settings.System.putInt(context.getContentResolver(), "prize_nav_bar_color_index", colorIndex);
    }

    public static int[] getColorImageResources(int id) {
        int[] icons = null;
        
        switch(id) {
        case R.id.default_white_color:
            icons = WHITE_COLOR_IMAGE_RESOURCE;
            break;
        case R.id.black_color:
            icons = BLACK_COLOR_IMAGE_RESOURCE;
            break;
        case R.id.gray_blue_color:
            icons = GRAY_BLUE_COLOR_IMAGE_RESOURCE;           
            break;
        case R.id.pink_color:
            icons = PINK_COLOR_IMAGE_RESOURCE;
            break;
        case R.id.brown_color:
            icons = BROWN_COLOR_IMAGE_RESOURCE;
            break;
        case R.id.gray_color:
            icons = GRAY_COLOR_IMAGE_RESOURCE;
            break;
        case R.id.custom_color:
            icons = CUSTEOM_COLOR_IMAGE_RESOURCE;
            break;
        }

        return icons;
    }

    public static int getColorImageResource(int id, boolean selected) {
        int[] icons = getColorImageResources(id);
        if (icons == null) {
            return 0;
        }
        
        return selected ? icons[1] : icons[0];
    }

    public static int getColor(int id) {
        int color = 0xff000000; // black color
        
        switch(id) {
        case R.id.default_white_color:
            color = PRE_WHITE_COLOR;
            break;
        case R.id.black_color:
            color = PRE_BLACK_COLOR;
            break;
        case R.id.gray_blue_color:
            color = PRE_GRAY_BLUE_COLOR;
            break;
        case R.id.pink_color:
            color = PRE_PINK_COLOR;
            break;
        case R.id.brown_color:
            color = PRE_BROWN_COLOR;
            break;
        case R.id.gray_color:
            color = PRE_GRAY_COLOR;
            break;
        }

        return color;
    }    
}

