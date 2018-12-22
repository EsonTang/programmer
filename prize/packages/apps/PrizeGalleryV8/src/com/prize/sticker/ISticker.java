/***
 * function:watermark
 * author: wanzhijuan
 * date:   2016-1-21
 */
package com.prize.sticker;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Paint;

public interface ISticker {
	
	void setScale(float scale);

    float[] getDstPoints();

    float getScale();

    Bitmap getBitmap(boolean isSave);

    boolean isFocus();

    Matrix getMatrix();

    Paint getEdgePaint();

    float[] getSrcPoints();

    void setBitmap(Bitmap bitmap);

    void setFocus(boolean focus);
    
    Object getTouchResource(float x, float y);

    void replaceWord(String newWord);
    void replaceAddress(String address, double longitude, double latitude);
    boolean isInWatemark(float x, float y);
    
    void invertColor();
    
    boolean isBlackColor();
    
    void doSymmetric();
    
    boolean isSymmetric();
    
    boolean isAddr();
    
    boolean isSticker();
    
    boolean isTextColorChange();
    
    boolean isInvertColor();
    
}
