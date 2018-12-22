/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.DynamicLayout;
import android.text.Layout;
import android.text.Layout.Alignment;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.util.MathUtils;
import android.view.MotionEvent;
import android.view.View;

import com.android.internal.util.Preconditions;
import com.android.settings.R;

public class PrizeStorageRectView extends View {
	private static final String TAG = "PrizeStorageRectView";
	private int apps = -1;
    private int images = -1;
    private int videos = -1;
    private int audio = -1;
    private int other = -1;

    public PrizeStorageRectView(Context context) {
        this(context, null);
    }

    public PrizeStorageRectView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PrizeStorageRectView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    
    public void cleanAll(){
    	apps = -1;
        images = -1;
        videos = -1;
        audio = -1;
        other = -1;
    }
    
    public void setPercentage(int tilteRes,int percentage){
    	switch (tilteRes) {
	    	case R.string.storage_detail_apps: 
	    		apps = percentage;
	    		Log.d(TAG,"setPercentage() apps = "+apps);
	    		break;
	    	case R.string.storage_detail_images: 
	    		images = percentage;
	    		Log.d(TAG,"setPercentage() images = "+images);
	    		break;
	    	case R.string.storage_detail_videos: 
	    		videos = percentage;
	    		Log.d(TAG,"setPercentage() videos = "+videos);
	    		break;
	    	case R.string.storage_detail_audio: 
	    		audio = percentage;
	    		Log.d(TAG,"setPercentage() audio = "+audio);
	    		break;
	    	case R.string.storage_detail_other: 
	    		other = percentage;
	    		Log.d(TAG,"setPercentage() other = "+other);
	    		break;
			default:				
				break;
		}
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas); 
        drawRect(canvas);
    }
	
    private void drawRect(Canvas canvas){ 
    	Log.d(TAG,"---------- drawRect------------");
        Paint paint = new Paint();  
        paint.setColor(getContext().getResources().getColor(R.color.storage_detail_all));
    	
    	Rect rectf = new Rect();
    	getLocalVisibleRect(rectf);
    	
    	int left = rectf.left;
        int top = rectf.top;
        int right = rectf.right;
        int bottom = rectf.bottom;
        
        int with = getWidth();       
       
       canvas.drawRect(rectf, paint);
       
       Log.d(TAG,"drawRect(), left = "+left+", right = "+right+", top = "+top+", bottom = "+bottom);
       
       Log.d(TAG,"drawRect(), apps = "+apps+", images = "+images+", videos = "+videos+", audio = "+audio+", other = "+other);
       if(apps!=-1&&images!=-1&&videos!=-1&&audio!=-1&&other!=-1){
    	   Log.d(TAG,"----------postInvalidate drawRect------------");
    	   paint.setColor(getContext().getResources().getColor(R.color.storage_detail_apps));
           right = left + apps * with /100;
           rectf.set(left, top, right, bottom);
           canvas.drawRect(rectf, paint);
           Log.d(TAG,"drawRect() PostInvalidate DrawRect, left = "+left+", right = "+right+", top = "+top+", bottom = "+bottom);
           
           paint.setColor(getContext().getResources().getColor(R.color.storage_detail_images));
           left= right;
           right =left + images * with /100;
           rectf.set(left, top, right, bottom);
           canvas.drawRect(rectf, paint);
           Log.d(TAG,"drawRect() PostInvalidate DrawRect, left = "+left+", right = "+right+", top = "+top+", bottom = "+bottom);
           
           paint.setColor(getContext().getResources().getColor(R.color.storage_detail_videos));
           left= right;
           right =left + videos * with /100;
           rectf.set(left, top, right, bottom);
           canvas.drawRect(rectf, paint);
           Log.d(TAG,"drawRect() PostInvalidate DrawRect, left = "+left+", right = "+right+", top = "+top+", bottom = "+bottom);
           
           paint.setColor(getContext().getResources().getColor(R.color.storage_detail_audio));
           left= right;
           right =left + audio * with /100;
           rectf.set(left, top, right, bottom);
           canvas.drawRect(rectf, paint);
           Log.d(TAG,"drawRect() PostInvalidate DrawRect, left = "+left+", right = "+right+", top = "+top+", bottom = "+bottom);
           
           paint.setColor(getContext().getResources().getColor(R.color.storage_detail_other));
           left= right;
           right =left + other * with /100;
           rectf.set(left, top, right, bottom);
           canvas.drawRect(rectf, paint);
           Log.d(TAG,"drawRect() PostInvalidate DrawRect, left = "+left+", right = "+right+", top = "+top+", bottom = "+bottom);
       }                            
    }
}
