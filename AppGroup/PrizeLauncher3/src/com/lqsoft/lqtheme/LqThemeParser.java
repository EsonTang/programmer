package com.lqsoft.lqtheme;

import android.content.Context;



public class LqThemeParser {
    // ===========================================================
    // Constants
    // ===========================================================

    // ===========================================================
    // Fields
    // ===========================================================

    private ThemeParserBaseAdapter parserBaseAdapter;
    // ===========================================================
    // Constructors
    // ===========================================================

    // ===========================================================
    // Getter & Setter
    // ===========================================================

    public void setAdapter(ThemeParserBaseAdapter adapter){
        parserBaseAdapter = adapter;
    }
    
    public int getThemeType(String themeFilePath) {
        if(parserBaseAdapter == null){
            return 0;
        }
        return parserBaseAdapter.getThemeType(themeFilePath);
    }
    
    public String getApplyThemeFilePath(Context context,String themeFilePath) {
        if(parserBaseAdapter == null){
            return null;
        }
        return parserBaseAdapter.getApplyThemeFilePath(context,themeFilePath);
    }
    
    // ===========================================================
    // Methods for/from SuperClass/Interfaces
    // ===========================================================

    // ===========================================================
    // Methods
    // ===========================================================

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================


}
