/*
* Copyright (C) 2014 MediaTek Inc.
* Modification based on code covered by the mentioned copyright
* and/or permission notice(s).
*/
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

package com.android.gallery3d.app;

import android.view.View;

public interface ControllerOverlay {

  interface Listener {
    void onPlayPause();
    void onSeekStart();
    void onSeekMove(int time);
    void onSeekEnd(int time, int trimStartTime, int trimEndTime);
    void onShown();
    void onHidden();
    void onReplay();
    boolean powerSavingNeedShowController();
    /*PRIZE- supports suspension function -wanzhijuan-2015-4-13-start*/
    /**
     *
     * method description: switch to the last video
     * @param parameter name Description
     * @return return type description
     *  class / @see class name complete complete class # method name
     */
    void onPlayPre();
    /**
     * 
     * method description: switch to next video
	 * @param parameter name Description
	 * @return return type specification
	 * / class / @see class name complete complete class # method name
     */
    void onPlayNext();
    
    /**
     * 
     * method description:Touch screen to adjust progress start
     * @param  parameter name Description
     * @return return type description
     * @see / class / @see class name complete complete class # method name
     */
    void onSlideStart();
    /**
     * 
     * method description:Touch screen to adjust progress doing
     * @param The parameter name time indicates the time to adjust the schedule.
     * @return return type description
     * @see / class / @see class name complete complete class # method name
     */
    void onSlideMove(int time);
    /**
     * 
     * Method description: touch screen to adjust the progress of the end
	 * @param parameter name time note to drag to the time
	 * @param trimStartTime parameter name imitation interface callback method and schedule drag
	 * @param trimEndTime parameter name imitation interface callback method and schedule drag
	 * @return return type description
	 * / class / @see class name complete complete class # method name
     */
    void onSlideEnd(int time, int trimStartTime, int trimEndTime);
    void onLockScreen(boolean isLock);
    /*PRIZE-Support suspension function-wanzhijuan-2015-4-13-end*/
  }

  void setListener(Listener listener);

  void setCanReplay(boolean canReplay);

  /**
   * @return The overlay view that should be added to the player.
   */
  View getView();

  void show();

  void showPlaying();

  void showPaused();

  void showEnded();

  void showLoading();

  void showErrorMessage(String message);
  void setDragTimes(int position);
  void setTimes(int currentTime, int totalTime,
          int trimStartTime, int trimEndTime);
}
