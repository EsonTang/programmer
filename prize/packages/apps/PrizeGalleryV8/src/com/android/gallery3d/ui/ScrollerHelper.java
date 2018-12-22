/*
* Copyright (C) 2014 MediaTek Inc.
* Modification based on code covered by the mentioned copyright
* and/or permission notice(s).
*/
/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.android.gallery3d.ui;

import android.content.Context;
import android.view.ViewConfiguration;
import android.view.animation.DecelerateInterpolator;

import com.android.gallery3d.common.OverScroller;
import com.android.gallery3d.common.Utils;

public class ScrollerHelper {
    private OverScroller mScroller;
    private int mOverflingDistance;
    private boolean mOverflingEnabled;
    /** acceleration*/
    private final int G_A = 1000;

    public ScrollerHelper(Context context) {
        mScroller = new OverScroller(context);
        ViewConfiguration configuration = ViewConfiguration.get(context);
        mOverflingDistance = configuration.getScaledOverflingDistance();
    }

    public void setOverfling(boolean enabled) {
        mOverflingEnabled = enabled;
    }

    /**
     * Call this when you want to know the new location. The position will be
     * updated and can be obtained by getPosition(). Returns true if  the
     * animation is not yet finished.
     */
    public boolean advanceAnimation(long currentTimeMillis) {
        return mScroller.computeScrollOffset();
    }

    public boolean isFinished() {
        return mScroller.isFinished();
    }

    public void forceFinished() {
        mScroller.forceFinished(true);
    }

    public int getPosition() {
        /// M: [FEATURE.ADD] fancy layout @{
        if (mScrollMax != -1 && mScrollMin != -1 && mScroller.isOverScrolled()) {
            return Utils.clamp(mScroller.getCurrX(), mScrollMin, mScrollMax);
        }
        /// @}
        return mScroller.getCurrX();
    }

    public float getCurrVelocity() {
        return mScroller.getCurrVelocity();
    }

    public void setPosition(int position) {
        mScroller.startScroll(
                position, 0,    // startX, startY
                0, 0, 0);       // dx, dy, duration

        // This forces the scroller to reach the final position.
        mScroller.abortAnimation();
    }

    public void fling(int velocity, int min, int max) {
        /// M: [FEATURE.ADD] fancy layout @{
        mScrollMax = max;
        mScrollMin = min;
        mScroller.setMaxScrollLength(max);
        mScroller.setMinScrollLength(min);
        /// @}
        int currX = getPosition();
        mScroller.fling(
                currX, 0,      // startX, startY
                velocity, 0,   // velocityX, velocityY
                min, max,      // minX, maxX
                0, 0,          // minY, maxY
                mOverflingEnabled ? mOverflingDistance : 0, 0);
    }

    // Returns the distance that over the scroll limit.
    public int startScroll(int distance, int min, int max) {
        /// M: [FEATURE.ADD] fancy layout @{
        mScrollMax = max;
        mScrollMin = min;
        mScroller.setMaxScrollLength(max);
        mScroller.setMinScrollLength(min);
        /// @}
        int currPosition = mScroller.getCurrX();
        int finalPosition = mScroller.isFinished() ? currPosition :
                mScroller.getFinalX();
        int newPosition = Utils.clamp(finalPosition + distance, min, max);
        if (newPosition != currPosition) {
            mScroller.startScroll(
                currPosition, 0,                    // startX, startY
                newPosition - currPosition, 0, 0);  // dx, dy, duration
        }
        return finalPosition + distance - newPosition;
    }
    
    /// @prize fanjunchen 2015-04-23 {
    /***
     *    */
    public void setInterpolator() {
//    	DecelerateInterpolator d = new DecelerateInterpolator(0.8f);
//    	mScroller.setInterpolator(d);
    }
    /***
     *     * @param vel
     * @param max
     * @return
     */
    public int startY(int vel, int max) {
        /// M: [FEATURE.ADD] fancy layout @{
        mScrollMax = max;
        mScrollMin = 0;
        mScroller.setMaxScrollLengthY(max);
        mScroller.setMinScrollLengthY(mScrollMin);
//        mScroller.setMaxScrollLength(max);
//        mScroller.setMinScrollLength(min);
        /// @}
        
        int v = (int)Math.abs(vel);
        int time = 1000 * v / G_A; // 2*v/3;
		// gravity = 1500;
		int distance = v*v / (2 * G_A);
		if (vel > 0)
			distance =  -distance;
    	
        int currPosition = mScroller.getCurrY();
        int finalPosition = mScroller.isFinished() ? currPosition :
                mScroller.getFinalY();
        int newPosition = Utils.clamp(finalPosition + distance, 0, max);
        
        float dy = newPosition - currPosition;
        
        time = (int)(time * dy/distance);
        
        if (newPosition != currPosition) {
            mScroller.startScroll(
                0, currPosition,    // startX, startY
                0, (int)dy, time);  // dx, dy, duration
        }
        return finalPosition + distance - newPosition;
    }
    
    public int getPositionY() {
        /// M: [FEATURE.ADD] fancy layout @{
        if (mScrollMax != -1 && mScrollMin != -1 && mScroller.isOverScrolled()) {
            return Utils.clamp(mScroller.getCurrY(), mScrollMin, mScrollMax);
        }
        /// @}
        return mScroller.getCurrY();
    }
    
    public void setPositionY(int position) {
        mScroller.startScroll(
                0, position,    // startX, startY
                0, 0, 0);       // dx, dy, duration

        // This forces the scroller to reach the final position.
        mScroller.abortAnimation();
    }

    /** prize chenyao  2015 , 05 ,29 */
    public void flingY(int velocity, int min, int max) {
        /// M: [FEATURE.ADD] fancy layout @{
        mScrollMax = max;
        mScrollMin = min;
        mScroller.setMaxScrollLengthY(max);
        mScroller.setMinScrollLengthY(min);
//        mScroller.setMaxScrollLength(max);
//        mScroller.setMinScrollLength(min);
        /// @}
        int currY = getPositionY();
        mScroller.fling(
                0, currY,      // startX, startY
                0, velocity,   // velocityX, velocityY
                0, 0,      // minX, maxX
                min, max,          // minY, maxY
                0, mOverflingEnabled ? mOverflingDistance : 0);
    }
    
    
    
    /** prize chenyao  2015 , 05 ,29 */
    // Returns the distance that over the scroll limit.
    public int startScrollY(int distance, int min, int max) {
        /// M: [FEATURE.ADD] fancy layout @{
        mScrollMax = max;
        mScrollMin = min;
        mScroller.setMaxScrollLengthY(max);
        mScroller.setMinScrollLengthY(min);
//        mScroller.setMaxScrollLength(max);
//        mScroller.setMinScrollLength(min);
        /// @}
        int currPosition = mScroller.getCurrY();
        int finalPosition = mScroller.isFinished() ? currPosition :
                mScroller.getFinalY();
        int newPosition = Utils.clamp(finalPosition + distance, min, max);
        if (newPosition != currPosition) {
            mScroller.startScroll(
                0, currPosition,                    // startX, startY
                0, newPosition - currPosition, 0);  // dx, dy, duration
        }
        return finalPosition + distance - newPosition;
    }
    /// @prize }

//********************************************************************
//*                              MTK                                 *
//********************************************************************
    /// M: [FEATURE.ADD] fancy layout @{
    private int mScrollMax = -1;
    private int mScrollMin = -1;
    /// @}
}
