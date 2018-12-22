package com.prize.music.helpers.utils;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.util.Log;

public class MainColor {
	private static final String TAG = "MainColor";
	private static final int threshold = 30; 
	
	public static int getMainColor(Bitmap bitmap){
		List<Integer> pixels = new ArrayList<Integer>();
		int width = bitmap.getWidth();  
        int height = bitmap.getHeight();  
        int sum= 0;  
        
        for (int i = 0; i < width; i++) {  
            for (int j = 0; j < height; j++) {  
                int pixel = bitmap.getPixel(i, j);   
                sum += pixel;
            }  
        }  
		
        int average = sum/(width * height);

        for (int i = 0; i < width; i++) {  
            for (int j = 0; j < height; j++) {  
                int pixel = bitmap.getPixel(i, j);   
                if(Math.abs(pixel - average) > threshold){
                	pixels.add(pixel);
                }
            }  
        }  
        
        int sum_pixels = 0;
        int sum_r = 0, sum_g = 0, sum_b = 0 ;
        for(int i : pixels){
        	sum_pixels += i;
        	
        	
        	
        	
//        	sum_r += (i & 0xff0000) >> 16;
//        	sum_g += (i & 0xff00) >> 8;
//        	sum_b += (i & 0xff);
        }
        
//        int color = sum_r/pixels.size() <<16 + sum_g/pixels.size() <<8 + sum_b/pixels.size();
//        color |= 0xff000000;
        
        
        
        if(pixels.size() != 0){
        	Log.d(TAG,"pixels.size() != 0 " + "average color = " + Integer.toHexString(average) + "main color = " + sum_pixels/(pixels.size()));
        	return sum_pixels/(pixels.size());
//        	return color;
        }else{
        	Log.d(TAG,"pixels.size() == 0 average color = " + Integer.toHexString(average));
        	return average;
        }
	}
	
	 public static Bitmap drawableToBitmap(Drawable drawable) {    
	       int width = drawable.getIntrinsicWidth();    
	       int height = drawable.getIntrinsicHeight();    
	       Bitmap bitmap = Bitmap.createBitmap(width, height, drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);    
	       Canvas canvas = new Canvas(bitmap);    
	       drawable.setBounds(0, 0, width, height);    
	       drawable.draw(canvas);    
	       return bitmap;    
	        
	    }  
	
	
	
}
