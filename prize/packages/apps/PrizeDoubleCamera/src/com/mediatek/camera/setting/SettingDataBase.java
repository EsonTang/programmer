/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein is
 * confidential and proprietary to MediaTek Inc. and/or its licensors. Without
 * the prior written permission of MediaTek inc. and/or its licensors, any
 * reproduction, modification, use or disclosure of MediaTek Software, and
 * information contained herein, in whole or in part, shall be strictly
 * prohibited.
 * 
 * MediaTek Inc. (C) 2014. All rights reserved.
 * 
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER
 * ON AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL
 * WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NONINFRINGEMENT. NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH
 * RESPECT TO THE SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY,
 * INCORPORATED IN, OR SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES
 * TO LOOK ONLY TO SUCH THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO.
 * RECEIVER EXPRESSLY ACKNOWLEDGES THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO
 * OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES CONTAINED IN MEDIATEK
 * SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE
 * RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S
 * ENTIRE AND CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE
 * RELEASED HEREUNDER WILL BE, AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE
 * MEDIATEK SOFTWARE AT ISSUE, OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE
 * CHARGE PAID BY RECEIVER TO MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek
 * Software") have been modified by MediaTek Inc. All revisions are subject to
 * any receiver's applicable license agreements with MediaTek Inc.
 */
package com.mediatek.camera.setting;

import android.graphics.Point;
import android.hardware.Camera.Parameters;
import android.media.CamcorderProfile;
import android.media.CameraProfile;
import android.text.TextUtils;

import com.android.camera.R;
//TODO:CamcorderProfileEx
import com.mediatek.camcorder.CamcorderProfileEx;
import com.mediatek.camera.ISettingRule.MappingFinder;
import com.mediatek.camera.util.Log;

import java.util.List;

import static com.baidu.location.b.g.S;

//TODO: need to be changed as xml
public class SettingDataBase {
    private static final String TAG = "SettingDataBase";
    
    public static final int UNKNOWN = -1;
   
    public static final int STATE_D0 = 100;// disable
    public static final int STATE_OFF = 101;// off
    public static final int STATE_E0 = 200;// enable
    public static final int STATE_R0 = 300;// reset value 0
    public static final int STATE_R1 = 301;// reset value 1
    public static final int STATE_R2 = 302;// reset value 2
    public static final int STATE_R3 = 303;// reset value 3
    public static final int STATE_R4 = 304;// reset value 4
    public static final int STATE_R5 = 305;// reset value 5
    public static final int STATE_R6 = 306;// reset value 6
    public static final int STATE_R7 = 307;// reset value 7
    
    private static final int[][] MATRIX_RESTRICTION_STATE = new int[SettingConstants.SETTING_COUNT][];
    static {
//              normal    hdr        facebeauty panorama  asd      photopip
//              video     videopip    smile     gesture   refecous watermark 
//          	prevideo  doublecamera gangyun_beauty gangyun_blur picturezoom lowlightshoot
//              shortprevideo portrait
        MATRIX_RESTRICTION_STATE[SettingConstants.ROW_SETTING_FLASH]                 = new int[]{
                STATE_E0, STATE_OFF, STATE_E0, STATE_R0, STATE_E0, STATE_E0,
                STATE_E0, STATE_R0, STATE_E0, STATE_E0, STATE_R0, STATE_E0,
                STATE_E0, STATE_R0, STATE_E0, STATE_R0, STATE_R0, STATE_R0,
                STATE_E0, STATE_E0};//0: flash
        MATRIX_RESTRICTION_STATE[SettingConstants.ROW_SETTING_DUAL_CAMERA]           = new int[]{
                STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_D0, STATE_E0,
                STATE_E0, STATE_E0, STATE_D0, STATE_E0, STATE_D0, STATE_E0,
                STATE_E0, STATE_E0, STATE_E0,STATE_E0 , STATE_E0, STATE_E0,
                STATE_E0, STATE_E0};//1: dual camera
        MATRIX_RESTRICTION_STATE[SettingConstants.ROW_SETTING_EXPOSURE]              = new int[]{
                STATE_E0, STATE_R0, STATE_E0, STATE_E0, STATE_E0, STATE_E0,
                STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0,
                STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0,
                STATE_E0, STATE_E0};//2: ev
        MATRIX_RESTRICTION_STATE[SettingConstants.ROW_SETTING_SCENCE_MODE]           = new int[]{
                STATE_E0, STATE_R1, STATE_R0, STATE_R0, STATE_R0, STATE_R0,
                STATE_E0, STATE_R0, STATE_E0, STATE_E0, STATE_E0, STATE_E0,
                STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0,
                STATE_E0, STATE_E0};//3: scence mode
        MATRIX_RESTRICTION_STATE[SettingConstants.ROW_SETTING_WHITE_BALANCE]         = new int[]{
                STATE_E0, STATE_R0, STATE_R0, STATE_E0, STATE_R0, STATE_E0,
                STATE_E0, STATE_E0, STATE_E0, STATE_R0, STATE_E0, STATE_E0,
                STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0,
                STATE_E0, STATE_E0};//4: white balance
        MATRIX_RESTRICTION_STATE[SettingConstants.ROW_SETTING_SLOW_MOTION]           = new int[]{
                STATE_E0, STATE_R0, STATE_R0, STATE_E0, STATE_R0, STATE_R0,
                STATE_D0, STATE_R0, STATE_E0, STATE_E0, STATE_R0, STATE_E0,
                STATE_D0, STATE_E0, STATE_D0, STATE_E0, STATE_E0, STATE_E0,
                STATE_D0, STATE_E0};//5: slow motion
        /*MATRIX_RESTRICTION_STATE[SettingConstants.ROW_SETTING_COLOR_EFFECT]          = new int[]{
                STATE_E0, STATE_E0, STATE_R0, STATE_R0, STATE_R0, STATE_R0,
                STATE_E0, STATE_R0, STATE_E0, STATE_E0, STATE_R0, STATE_E0
                ,STATE_E0,STATE_E0};//6: color effect*/
        MATRIX_RESTRICTION_STATE[SettingConstants.ROW_SETTING_SELF_TIMER]            = new int[]{
                STATE_E0, STATE_E0, STATE_E0, STATE_R0, STATE_E0, STATE_E0,
                STATE_R0, STATE_R0, STATE_R0, STATE_E0, STATE_E0, STATE_E0,
                STATE_D0, STATE_R0, STATE_D0, STATE_E0, STATE_E0, STATE_E0,
                STATE_D0, STATE_E0};//7: self timer
        MATRIX_RESTRICTION_STATE[SettingConstants.ROW_SETTING_ZSD]                   = new int[]{
        		STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_R0, STATE_R0,
                STATE_R0, STATE_R0, STATE_R0, STATE_R0, STATE_E0, STATE_E0,
                STATE_R0, STATE_R0, STATE_E0, STATE_R0, STATE_E0, STATE_E0,
                STATE_R0, STATE_E0};//8: zsd
        MATRIX_RESTRICTION_STATE[SettingConstants.ROW_SETTING_RECORD_LOCATION]       = new int[]{
                STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0,
                STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0,
                STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0,
                STATE_E0, STATE_E0};//10: geo tag
        MATRIX_RESTRICTION_STATE[SettingConstants.ROW_SETTING_PICTURE_RATIO]         = new int[]{
                STATE_E0, STATE_E0, STATE_E0, STATE_D0, STATE_E0, STATE_E0,
                STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0,
                STATE_D0, STATE_D0, STATE_D0, STATE_D0, STATE_E0, STATE_E0,
                STATE_D0, STATE_E0};//21: picture ratio
        MATRIX_RESTRICTION_STATE[SettingConstants.ROW_SETTING_PICTURE_SIZE]          = new int[]{
                STATE_E0, STATE_E0, STATE_E0, STATE_D0, STATE_E0, STATE_E0,
                STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0,
                STATE_D0, STATE_D0, STATE_D0, STATE_D0, STATE_E0, STATE_E0,
                STATE_D0, STATE_E0};//11: picture size
        MATRIX_RESTRICTION_STATE[SettingConstants.ROW_SETTING_ISO]                   = new int[]{
                STATE_E0, STATE_R0, STATE_E0, STATE_E0, STATE_E0, STATE_E0,
                STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0,
                STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0,
                STATE_E0, STATE_E0};//12: ISO
        MATRIX_RESTRICTION_STATE[SettingConstants.ROW_SETTING_AIS]                   = new int[]{
                STATE_E0, STATE_R0, STATE_E0, STATE_R0, STATE_R0, STATE_R0,
                STATE_R0, STATE_R0, STATE_R0, STATE_R0, STATE_R0, STATE_E0,
                STATE_R0, STATE_E0, STATE_R0, STATE_E0, STATE_R0, STATE_R0,
                STATE_R0, STATE_E0};//1//ais
        MATRIX_RESTRICTION_STATE[SettingConstants.ROW_SETTING_ANTI_FLICKER]          = new int[]{
                STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0,
                STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0,
                STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0,
                STATE_E0, STATE_E0};//14: Anti flicker
        MATRIX_RESTRICTION_STATE[SettingConstants.ROW_SETTING_VIDEO_STABLE]          = new int[]{
                STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_R0,
                STATE_E0, STATE_R0, STATE_E0, STATE_E0, STATE_E0, STATE_E0,
                STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0,
                STATE_E0, STATE_E0};//15: video stable
        MATRIX_RESTRICTION_STATE[SettingConstants.ROW_SETTING_MICROPHONE]            = new int[]{
                STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0,
                STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0,
                STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0,
                STATE_E0, STATE_E0};//16 microphone
        MATRIX_RESTRICTION_STATE[SettingConstants.ROW_SETTING_AUDIO_MODE]            = new int[]{
                STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_R0,
                STATE_E0, STATE_R0, STATE_E0, STATE_E0, STATE_E0, STATE_E0,
                STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0,
                STATE_E0, STATE_E0};//17 audio mode
        MATRIX_RESTRICTION_STATE[SettingConstants.ROW_SETTING_TIME_LAPSE]            = new int[]{
                STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_R0,
                STATE_E0, STATE_R0, STATE_E0, STATE_E0, STATE_E0, STATE_E0,
                STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0,
                STATE_E0, STATE_E0};//18 timelapse
        MATRIX_RESTRICTION_STATE[SettingConstants.ROW_SETTING_VIDEO_QUALITY]         = new int[]{
                STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0,
                STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0,
                STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0,
                STATE_E0, STATE_E0};//20 video quality
        MATRIX_RESTRICTION_STATE[SettingConstants.ROW_SETTING_SLOW_MOTION_VIDEO_QUALITY]    = new int[]{
                STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0,
                STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0,
                STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0,
                STATE_E0, STATE_E0};//20 video quality
        MATRIX_RESTRICTION_STATE[SettingConstants.ROW_SETTING_VOICE]                 = new int[]{
                STATE_E0, STATE_E0, STATE_E0, STATE_R0, STATE_E0, STATE_E0,
                STATE_E0, STATE_R0, STATE_E0, STATE_E0, STATE_E0, STATE_E0,
                STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0,
                STATE_E0, STATE_E0};//22 voice
        MATRIX_RESTRICTION_STATE[SettingConstants.ROW_SETTING_3DNR]                  = new int[]{
                STATE_E0, STATE_E0, STATE_R0, STATE_E0, STATE_E0, STATE_R0,
                STATE_E0, STATE_R0, STATE_E0, STATE_E0, STATE_E0, STATE_E0,
                STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0,
                STATE_E0, STATE_E0};//23: 3DNR
        MATRIX_RESTRICTION_STATE[SettingConstants.ROW_SETTING_SHARPNESS]             = new int[]{
                STATE_E0, STATE_R0, STATE_E0, STATE_E0, STATE_E0, STATE_E0,
                STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0,
                STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0,
                STATE_E0, STATE_E0};//30: image adjustment sharpness
        MATRIX_RESTRICTION_STATE[SettingConstants.ROW_SETTING_HUE]                   = new int[]{
                STATE_E0, STATE_R0, STATE_E0, STATE_E0, STATE_E0, STATE_E0,
                STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0,
                STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0,
                STATE_E0, STATE_E0};//31: image adjustment hue
        MATRIX_RESTRICTION_STATE[SettingConstants.ROW_SETTING_SATURATION]            = new int[]{
                STATE_E0, STATE_R0, STATE_E0, STATE_E0, STATE_E0, STATE_E0,
                STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0,
                STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0,
                STATE_E0, STATE_E0};//32: image adjustment saturation
        MATRIX_RESTRICTION_STATE[SettingConstants.ROW_SETTING_BRIGHTNESS]            = new int[]{
                STATE_E0, STATE_R0, STATE_E0, STATE_E0, STATE_E0, STATE_E0,
                STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0,
                STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0,
                STATE_E0, STATE_E0};//33: image adjustment brightness
        MATRIX_RESTRICTION_STATE[SettingConstants.ROW_SETTING_CONTRAST]              = new int[]{
                STATE_E0, STATE_R0, STATE_E0, STATE_E0, STATE_E0, STATE_E0,
                STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0,
                STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0,
                STATE_E0, STATE_E0};//34: image adjustment contrast
        MATRIX_RESTRICTION_STATE[SettingConstants.ROW_SETTING_FAST_AF]               = new int[]{
                STATE_E0, STATE_R0, STATE_E0, STATE_E0, STATE_E0, STATE_E0,
                STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0,
                STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0,
                STATE_E0, STATE_E0};//33: Fast AF
        MATRIX_RESTRICTION_STATE[SettingConstants.ROW_SETTING_DISTANCE]              = new int[]{
                STATE_E0, STATE_R0, STATE_E0, STATE_E0, STATE_E0, STATE_E0,
                STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0,
                STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0,
                STATE_E0, STATE_E0};//34: Distance info
        MATRIX_RESTRICTION_STATE[SettingConstants.ROW_SETTING_CAMERA_MODE]           = new int[]{
                STATE_R0, STATE_R0, STATE_R0, STATE_R0, STATE_R0, STATE_R0,
                STATE_R1, STATE_R1, STATE_R0, STATE_R0, STATE_R0, STATE_R0,
                STATE_R0, STATE_R0, STATE_R0, STATE_R0, STATE_R0, STATE_R0,
                STATE_R0, STATE_R0};//21 camera mode
        MATRIX_RESTRICTION_STATE[SettingConstants.ROW_SETTING_CAPTURE_MODE]          = new int[]{
                STATE_R0, STATE_R0, STATE_R2, STATE_R6, STATE_R3, STATE_R0,
                STATE_R0, STATE_R0, STATE_E0, STATE_E0, STATE_E0, STATE_R0,
                STATE_R0, STATE_R0, STATE_R0, STATE_R0, STATE_E0, STATE_E0,
                STATE_R0, STATE_R0};//22: capture mode
        MATRIX_RESTRICTION_STATE[SettingConstants.ROW_SETTING_CONTINUOUS_NUM]        = new int[]{
                STATE_R0, STATE_D0, STATE_R0, STATE_R0, STATE_R0, STATE_R0,
                STATE_R0, STATE_R0, STATE_D0, STATE_D0, STATE_R0, STATE_D0,
                STATE_R0, STATE_R0, STATE_R0, STATE_R0, STATE_E0, STATE_E0,
                STATE_R0, STATE_R0};
        //23: initial continuous number,
        //it is different from 6 for here is real number, 6 is enable/disable state.
        MATRIX_RESTRICTION_STATE[SettingConstants.ROW_SETTING_RECORDING_HINT]        = new int[]{
                STATE_R0, STATE_E0, STATE_R0, STATE_R0, STATE_R0, STATE_R0,
                STATE_R1, STATE_R0, STATE_E0, STATE_E0, STATE_R0, STATE_R0,
                STATE_R1, STATE_R0, STATE_R0, STATE_R0, STATE_E0, STATE_E0,
                STATE_R1, STATE_R0};//24 recording hint
        MATRIX_RESTRICTION_STATE[SettingConstants.ROW_SETTING_JPEG_QUALITY]          = new int[]{
                STATE_R0, STATE_R0, STATE_R0, STATE_R1, STATE_R0, STATE_R0,
                STATE_R0, STATE_R0, STATE_E0, STATE_E0, STATE_R0, STATE_R0,
                STATE_R0, STATE_R0, STATE_R0, STATE_R0, STATE_E0, STATE_E0,
                STATE_R0, STATE_R0};//25 jpeg quality
        MATRIX_RESTRICTION_STATE[SettingConstants.ROW_SETTING_FACEBEAUTY_SMOOTH]     = new int[]{
                STATE_E0, STATE_D0, STATE_E0, STATE_D0, STATE_D0, STATE_D0,
                STATE_D0, STATE_D0, STATE_E0, STATE_E0, STATE_D0, STATE_E0,
                STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0,
                STATE_E0, STATE_E0};//47: face beauty smooth
        MATRIX_RESTRICTION_STATE[SettingConstants.ROW_SETTING_FACEBEAUTY_SKIN_COLOR] = new int[]{
                STATE_E0, STATE_D0, STATE_E0, STATE_D0, STATE_D0, STATE_D0,
                STATE_D0, STATE_D0, STATE_E0, STATE_E0, STATE_D0, STATE_E0,
                STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0,
                STATE_E0, STATE_E0};//48: face beauty skin-color
        MATRIX_RESTRICTION_STATE[SettingConstants.ROW_SETTING_FACEBEAUTY_SHARP]      = new int[]{
                STATE_E0, STATE_D0, STATE_E0, STATE_D0, STATE_D0, STATE_D0,
                STATE_D0, STATE_D0, STATE_E0, STATE_E0, STATE_D0, STATE_E0,
                STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0,
                STATE_E0, STATE_E0};//49: face beauty sharp
        MATRIX_RESTRICTION_STATE[SettingConstants.ROW_SETTING_FACEBEAUTY_SLIM]       = new int[]{
                STATE_E0, STATE_D0, STATE_E0, STATE_D0, STATE_D0, STATE_D0,
                STATE_D0, STATE_D0, STATE_E0, STATE_E0, STATE_D0, STATE_E0,
                STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0,
                STATE_E0, STATE_E0};//49: face beauty sharp
        MATRIX_RESTRICTION_STATE[SettingConstants.ROW_SETTING_FACEBEAUTY_BIG_EYES]   = new int[]{
                STATE_E0, STATE_D0, STATE_E0, STATE_D0, STATE_D0, STATE_D0,
                STATE_D0, STATE_D0, STATE_E0, STATE_E0, STATE_D0, STATE_E0,
                STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0,
                STATE_E0, STATE_E0};//49: face beauty sharp
        MATRIX_RESTRICTION_STATE[SettingConstants.ROW_SETTING_CAMERA_FACE_DETECT]    = new int[]{
                STATE_E0, STATE_E0, STATE_R1, STATE_R0, STATE_R1, STATE_E0,
                STATE_R0, STATE_R0, STATE_R1, STATE_R1, STATE_E0, STATE_E0,
                STATE_E0, STATE_E0, STATE_E0, STATE_R0, STATE_E0, STATE_E0,
                STATE_E0, STATE_E0};//50: face detection
        MATRIX_RESTRICTION_STATE[SettingConstants.ROW_SETTING_MULTI_FACE_MODE]       = new int[]{
                STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_D0,
                STATE_E0, STATE_D0, STATE_E0, STATE_E0, STATE_E0, STATE_E0,
                STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0,
                STATE_E0, STATE_E0};//55: multi-face mode
        MATRIX_RESTRICTION_STATE[SettingConstants.ROW_SETTING_HDR]                   = new int[]{
                STATE_E0, STATE_E0, STATE_R0, STATE_R0, STATE_R0, STATE_R0,
                STATE_R0, STATE_R0, STATE_R0, STATE_R0, STATE_R0, STATE_E0,
                STATE_R0, STATE_D0, STATE_R0, STATE_R0, STATE_R0, STATE_R0,
                STATE_R0, STATE_R0};//51: hdr
        MATRIX_RESTRICTION_STATE[SettingConstants.ROW_SETTING_SMILE_SHOT]            = new int[]{
                STATE_E0, STATE_R0, STATE_R0, STATE_R0, STATE_R0, STATE_E0,
                STATE_R0, STATE_R0, STATE_E0, STATE_R0, STATE_R0, STATE_E0,
                STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0,
                STATE_E0, STATE_E0};//52: smile shot
        MATRIX_RESTRICTION_STATE[SettingConstants.ROW_SETTING_ASD]                   = new int[]{
                STATE_E0, STATE_R0, STATE_R0, STATE_R0, STATE_E0, STATE_R0,
                STATE_R0, STATE_R0, STATE_R0, STATE_R0, STATE_R0, STATE_E0,
                STATE_E0, STATE_E0, STATE_E0, STATE_R0, STATE_R0, STATE_E0,
                STATE_E0, STATE_E0};//53: asd
		/* prize Camera mute wanzhijuan 2016-5-30 start */
        /*MATRIX_RESTRICTION_STATE[SettingConstants.ROW_SETTING_MUTE_RECORDING_SOUND]  = new int[]{
                STATE_R0, STATE_R0, STATE_R0, STATE_R0, STATE_R0, STATE_R0,
                STATE_R0, STATE_R0, STATE_E0, STATE_E0, STATE_R0};*///54 enable recording sound
		/* prize Camera mute wanzhijuan 2016-5-30 end */
        MATRIX_RESTRICTION_STATE[SettingConstants.ROW_SETTING_GESTURE_SHOT]          = new int[]{
                STATE_E0, STATE_R0, STATE_E0, STATE_R0, STATE_R0, STATE_E0,
                STATE_R0, STATE_R0, STATE_R0, STATE_E0, STATE_R0, STATE_E0,
                STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0,
                STATE_E0, STATE_E0};//55 gesture shot
        MATRIX_RESTRICTION_STATE[SettingConstants.ROW_SETTING_HEARTBEAT_MONITOR]     = new int[]{
                STATE_R0, STATE_R1, STATE_R1, STATE_R1, STATE_R1, STATE_R1,
                STATE_R1, STATE_R1, STATE_R1, STATE_R1, STATE_R1, STATE_R0,
                STATE_R0, STATE_R0, STATE_R0, STATE_R0, STATE_E0, STATE_E0,
                STATE_R0, STATE_R0};//55 gesture shot
        MATRIX_RESTRICTION_STATE[SettingConstants.ROW_SETTING_DNG]                   = new int[]{
                STATE_E0, STATE_R0, STATE_R0, STATE_R0, STATE_E0, STATE_R0,
                STATE_R0, STATE_R0, STATE_E0, STATE_E0, STATE_R0, STATE_E0,
                STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0,
                STATE_E0, STATE_E0};
		/* PRIZE-add setting items-wanzhijuan-2016-05-03-start */
		MATRIX_RESTRICTION_STATE[SettingConstants.ROW_SETTING_CAMERA_MUTE] = new int[] {
				STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0,
				STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0,
				STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0,
				STATE_E0, STATE_E0};// 52
		MATRIX_RESTRICTION_STATE[SettingConstants.ROW_SETTING_GRIDE] = new int[] {
				STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0,
				STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0,
				STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0 ,
				STATE_E0, STATE_E0};// 53
		MATRIX_RESTRICTION_STATE[SettingConstants.ROW_SETTING_TOUCHSHUTTER] = new int[] {
				STATE_E0, STATE_E0, STATE_E0, STATE_D0, STATE_D0, STATE_D0,
				STATE_D0, STATE_D0, STATE_E0, STATE_E0, STATE_E0, STATE_E0,
				STATE_D0, STATE_D0, STATE_D0, STATE_D0, STATE_E0, STATE_E0,
				STATE_D0, STATE_E0};// 54
		/* PRIZE-add setting items-wanzhijuan-2016-05-03-end */
		MATRIX_RESTRICTION_STATE[SettingConstants.ROW_SETTING_CONBIN_MANAGER] = new int[] {
				STATE_E0, STATE_E0, STATE_D0, STATE_D0, STATE_D0, STATE_D0,
				STATE_D0, STATE_D0, STATE_D0, STATE_D0, STATE_D0, STATE_E0,
				STATE_D0, STATE_D0, STATE_D0, STATE_D0, STATE_D0, STATE_D0,
				STATE_D0, STATE_D0};// 55
        /*PRIZE-add for flip setting-liufan-2017-05-24-start*/
        MATRIX_RESTRICTION_STATE[SettingConstants.ROW_SETTING_PRIZE_FLIP] = new int[] {
                STATE_E0, STATE_R1, STATE_E0, STATE_R1, STATE_R1, STATE_R1,
                STATE_R1, STATE_R1, STATE_R1, STATE_R1, STATE_R1, STATE_R1,
                STATE_R1, STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0,
                STATE_R1, STATE_E0};// 56
        /*PRIZE-add for flip setting-liufan-2017-05-24-end*/
        
        /*arc add start*/
        MATRIX_RESTRICTION_STATE[SettingConstants.ROW_SETTING_ARC_PICSELFIE_ENABLE] = new int[] {
                STATE_E0, STATE_R0, STATE_R0, STATE_R0, STATE_R0, STATE_R0,
                STATE_R0, STATE_R0, STATE_R0, STATE_R0, STATE_R0, STATE_R0,
                STATE_R0, STATE_R0, STATE_R0, STATE_R0, STATE_R0, STATE_R0,
                STATE_R0, STATE_E0};// 56
        /*arc add end*/

        /*add koobee watermark*/
        MATRIX_RESTRICTION_STATE[SettingConstants.ROW_SETTING_KOOBEE_WATERMARK]       = new int[]{
                STATE_E0, STATE_E0, STATE_E0, STATE_R0, STATE_E0, STATE_E0,
                STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_R0,
                STATE_R0, STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0,
                STATE_R0, STATE_E0};
        /*prize-xuchunming-20171018-change picturezoom UI interaction-start*/
        MATRIX_RESTRICTION_STATE[SettingConstants.ROW_SETTING_ARC_DISPLAY_ENABLE]     = new int[]{
                STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0,
                STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0,
                STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0,
                STATE_E0, STATE_E0};
        /*prize-xuchunming-20171018-change picturezoom UI interaction-end*/
    }
    
    private static final int[] RESTRCTION_SETTING_INDEX = new int[] {
        SettingConstants.ROW_SETTING_NORMAL,
        SettingConstants.ROW_SETTING_HDR,
        SettingConstants.ROW_SETTING_FACE_BEAUTY,
        SettingConstants.ROW_SETTING_PANORAMA,
        SettingConstants.ROW_SETTING_ASD,
        SettingConstants.ROW_SETTING_PHOTO_PIP,
        SettingConstants.ROW_SETTING_VIDEO,
        SettingConstants.ROW_SETTING_VIDEO_PIP,
        SettingConstants.ROW_SETTING_SMILE_SHOT,
        SettingConstants.ROW_SETTING_GESTURE_SHOT,
        SettingConstants.ROW_SETTING_REFOCUS,
        SettingConstants.ROW_SETTING_WATERMARK,
        SettingConstants.ROW_SETTING_PREVIDEO,
        SettingConstants.ROW_SETTING_DOUBLE_CAMERA,
		//gangyun tech add begin
        SettingConstants.ROW_SETTING_GANGYUN_BEAUTY,
        SettingConstants.ROW_SETTING_GANGYUN_BOKEH, 
		//gangyun tech add end
        /*arc add start*/
        SettingConstants.ROW_SETTING_ARC_PICTUREZOOM_MODE,
        SettingConstants.ROW_SETTING_ARC_LOWLIGHTSHOOT_MODE,
        /*arc add end*/
        SettingConstants.ROW_SETTING_SHORT_PREVIDEO,
        SettingConstants.ROW_SETTING_PORTRAIT,
        
    };
    
    private static final String[][] RESET_STATE_VALUE = new String[SettingConstants.SETTING_COUNT][];
    static {
    	RESET_STATE_VALUE[SettingConstants.ROW_SETTING_FLASH]            = new String[]{Parameters.FLASH_MODE_OFF};//off
        RESET_STATE_VALUE[SettingConstants.ROW_SETTING_DUAL_CAMERA]      = new String[]{"0"};//none need check
        RESET_STATE_VALUE[SettingConstants.ROW_SETTING_EXPOSURE]         = new String[]{"0", "1"};//auto need check
        RESET_STATE_VALUE[SettingConstants.ROW_SETTING_SCENCE_MODE]      = new String[]{
                Parameters.SCENE_MODE_AUTO,
                ParametersHelper.KEY_SCENE_MODE_HDR,
        };//normal
        RESET_STATE_VALUE[SettingConstants.ROW_SETTING_WHITE_BALANCE]    = new String[]{
                Parameters.WHITE_BALANCE_AUTO,
                Parameters.WHITE_BALANCE_DAYLIGHT,
                Parameters.WHITE_BALANCE_INCANDESCENT
        };//auto
        //image properties
        RESET_STATE_VALUE[SettingConstants.ROW_SETTING_AIS]                   = new String[]{"off"};//
        RESET_STATE_VALUE[SettingConstants.ROW_SETTING_COLOR_EFFECT]          = new String[]{Parameters.EFFECT_NONE};//none
        RESET_STATE_VALUE[SettingConstants.ROW_SETTING_SELF_TIMER]            = new String[]{"0"};//none
        RESET_STATE_VALUE[SettingConstants.ROW_SETTING_ZSD]                   = new String[]{ParametersHelper.ZSD_MODE_OFF};//none
        RESET_STATE_VALUE[SettingConstants.ROW_SETTING_RECORD_LOCATION]       = null;//none
        RESET_STATE_VALUE[SettingConstants.ROW_SETTING_PICTURE_SIZE]          = null;//none need check
        RESET_STATE_VALUE[SettingConstants.ROW_SETTING_ISO]                   = new String[]{"auto"};//auto
        RESET_STATE_VALUE[SettingConstants.ROW_SETTING_ANTI_FLICKER]          = null;//none
        RESET_STATE_VALUE[SettingConstants.ROW_SETTING_VIDEO_STABLE]          = new String[]{"off"};
        RESET_STATE_VALUE[SettingConstants.ROW_SETTING_3DNR]                  = new String[]{"off"};
        RESET_STATE_VALUE[SettingConstants.ROW_SETTING_MICROPHONE]            = new String[]{"on","off"};
        RESET_STATE_VALUE[SettingConstants.ROW_SETTING_AUDIO_MODE]            = new String[]{"normal"};
        RESET_STATE_VALUE[SettingConstants.ROW_SETTING_TIME_LAPSE]            = new String[]{"0"};
        RESET_STATE_VALUE[SettingConstants.ROW_SETTING_VIDEO_QUALITY]         = new String[]{"9"};
        RESET_STATE_VALUE[SettingConstants.ROW_SETTING_SLOW_MOTION_VIDEO_QUALITY]    = new String[]{"21"};
        RESET_STATE_VALUE[SettingConstants.ROW_SETTING_SHARPNESS]             = new String[]{"middle", "low", "high"};
        RESET_STATE_VALUE[SettingConstants.ROW_SETTING_FAST_AF]               = new String[]{"off"};//middle
        RESET_STATE_VALUE[SettingConstants.ROW_SETTING_DISTANCE]              = new String[]{"off"};//middle
        RESET_STATE_VALUE[SettingConstants.ROW_SETTING_HUE]                   = new String[]{"middle"};//middle
        RESET_STATE_VALUE[SettingConstants.ROW_SETTING_SATURATION]            = new String[]{"middle", "low"};//middle
        RESET_STATE_VALUE[SettingConstants.ROW_SETTING_BRIGHTNESS]            = new String[]{"middle"};//middle
        RESET_STATE_VALUE[SettingConstants.ROW_SETTING_CONTRAST]              = new String[]{"middle"};//middle
        RESET_STATE_VALUE[SettingConstants.ROW_SETTING_FACEBEAUTY_SMOOTH]     = new String[]{"0"};//middle
        RESET_STATE_VALUE[SettingConstants.ROW_SETTING_FACEBEAUTY_SKIN_COLOR] = new String[]{"0"};//middle
        RESET_STATE_VALUE[SettingConstants.ROW_SETTING_FACEBEAUTY_SHARP]      = new String[]{"0"};//middle
        RESET_STATE_VALUE[SettingConstants.ROW_SETTING_CAMERA_FACE_DETECT]    = new String[]{"off", "on"};
        RESET_STATE_VALUE[SettingConstants.ROW_SETTING_MULTI_FACE_MODE]       = new String[]{"Single", "Multi","OFF"};
        RESET_STATE_VALUE[SettingConstants.ROW_SETTING_RECORDING_HINT]        = new String[]{Boolean.toString(false), Boolean.toString(true)};
        RESET_STATE_VALUE[SettingConstants.ROW_SETTING_CAPTURE_MODE]          = new String[]{
                Parameters.CAPTURE_MODE_NORMAL,
                Parameters.SCENE_MODE_HDR,//HDR from scence mode
                Parameters.CAPTURE_MODE_FB,
                Parameters.CAPTURE_MODE_ASD,
                //Parameters.CAPTURE_MODE_SMILE_SHOT,
                Parameters.CAPTURE_MODE_BEST_SHOT,
                Parameters.CAPTURE_MODE_EV_BRACKET_SHOT,
                "autorama"
        };
        RESET_STATE_VALUE[SettingConstants.ROW_SETTING_CONTINUOUS_NUM]        = new String[]{"40"};
        RESET_STATE_VALUE[SettingConstants.ROW_SETTING_JPEG_QUALITY]          = new String[]{
                Integer.toString(CameraProfile.QUALITY_HIGH),
                Integer.toString(CameraProfile.QUALITY_HIGH),//should be rechecked
        };
        RESET_STATE_VALUE[SettingConstants.ROW_SETTING_CAMERA_MODE]           = new String[]{
                Integer.toString(Parameters.CAMERA_MODE_MTK_PRV),
                Integer.toString(Parameters.CAMERA_MODE_MTK_VDO),
        };
        RESET_STATE_VALUE[SettingConstants.ROW_SETTING_VOICE]                = new String[] { "off" };// off
        RESET_STATE_VALUE[SettingConstants.ROW_SETTING_SLOW_MOTION]          = new String[]{"off","on"};
        RESET_STATE_VALUE[SettingConstants.ROW_SETTING_HDR]                  = new String[]{"off", "on"};
        RESET_STATE_VALUE[SettingConstants.ROW_SETTING_SMILE_SHOT]           = new String[]{"off", "on"};
        RESET_STATE_VALUE[SettingConstants.ROW_SETTING_ASD]                  = new String[]{"off", "on"};
        RESET_STATE_VALUE[SettingConstants.ROW_SETTING_GESTURE_SHOT]         = new String[]{"off", "on"};
        RESET_STATE_VALUE[SettingConstants.ROW_SETTING_MUTE_RECORDING_SOUND] = new String[]{"0", "1"};
        RESET_STATE_VALUE[SettingConstants.ROW_SETTING_HEARTBEAT_MONITOR]    = new String[]{"true", "false"};
        RESET_STATE_VALUE[SettingConstants.ROW_SETTING_DNG] = new String[]{"off", "on"};
		
		/*PRIZE-add setting items-wanzhijuan-2016-05-03-start*/
        RESET_STATE_VALUE[SettingConstants.ROW_SETTING_CAMERA_MUTE]         = new String[]{"off", "on"};
        RESET_STATE_VALUE[SettingConstants.ROW_SETTING_GRIDE]         = new String[]{"off", "on"};
        RESET_STATE_VALUE[SettingConstants.ROW_SETTING_TOUCHSHUTTER]         = new String[]{"off", "on"};
        /*PRIZE-add setting items-wanzhijuan-2016-05-03-end*/
		
		//gangyun tech add begin
	    RESET_STATE_VALUE[SettingConstants.ROW_SETTING_GANGYUN_BEAUTY]       = new String[]{"off", "on"};
	    RESET_STATE_VALUE[SettingConstants.ROW_SETTING_GANGYUN_BOKEH]        = new String[]{"off", "on"}; 
		//gangyun tech add end
        /*PRIZE-add for flip setting-liufan-2017-05-24-start*/
        RESET_STATE_VALUE[SettingConstants.ROW_SETTING_PRIZE_FLIP]         = new String[]{"off", "on"};
        /*PRIZE-add for flip setting-liufan-2017-05-24-end*/
        
        /*arc add start*/
        RESET_STATE_VALUE[SettingConstants.ROW_SETTING_ARC_PICTUREZOOM_MODE]     = new String[]{"off", "on"};
        RESET_STATE_VALUE[SettingConstants.ROW_SETTING_ARC_LOWLIGHTSHOOT_MODE]   = new String[]{"off", "on"};
        RESET_STATE_VALUE[SettingConstants.ROW_SETTING_ARC_PICSELFIE_ENABLE]     = new String[]{"off", "on"};
        
        /*arc add end*/
        RESET_STATE_VALUE[SettingConstants.ROW_SETTING_KOOBEE_WATERMARK]     = new String[]{"off", "on"};
        RESET_STATE_VALUE[SettingConstants.ROW_SETTING_ARC_FCAM_SUPERZOOM_ENABLE]     = new String[]{"off", "on"};
    }
    
    private static final int[] DEFAULT_VALUE_FOR_SETTING_ID = new int[SettingConstants.SETTING_COUNT];
    private static final String[] DEFAULT_VALUE_FOR_SETTING = new String[SettingConstants.SETTING_COUNT];
    
    static {
    	DEFAULT_VALUE_FOR_SETTING_ID[SettingConstants.ROW_SETTING_FLASH]                 = R.string.pref_camera_flashmode_default;
        DEFAULT_VALUE_FOR_SETTING_ID[SettingConstants.ROW_SETTING_DUAL_CAMERA]           = R.string.pref_camera_id_default;//need recheck
        DEFAULT_VALUE_FOR_SETTING_ID[SettingConstants.ROW_SETTING_EXPOSURE]              = R.string.pref_camera_exposure_default;
        DEFAULT_VALUE_FOR_SETTING_ID[SettingConstants.ROW_SETTING_SCENCE_MODE]           = R.string.pref_camera_scenemode_default;
        DEFAULT_VALUE_FOR_SETTING_ID[SettingConstants.ROW_SETTING_WHITE_BALANCE]         = R.string.pref_camera_whitebalance_default;
        DEFAULT_VALUE_FOR_SETTING_ID[SettingConstants.ROW_SETTING_COLOR_EFFECT]          = R.string.pref_camera_coloreffect_default;
        DEFAULT_VALUE_FOR_SETTING_ID[SettingConstants.ROW_SETTING_SELF_TIMER]            = R.string.pref_camera_selftimer_default;
        DEFAULT_VALUE_FOR_SETTING_ID[SettingConstants.ROW_SETTING_ZSD]                   = R.string.pref_camera_zsd_default;
        DEFAULT_VALUE_FOR_SETTING_ID[SettingConstants.ROW_SETTING_CONTINUOUS_NUM]        = R.string.pref_camera_continuous_number_default;
        DEFAULT_VALUE_FOR_SETTING_ID[SettingConstants.ROW_SETTING_RECORD_LOCATION]       = R.string.pref_camera_recordlocation_default;//need recheck
        DEFAULT_VALUE_FOR_SETTING_ID[SettingConstants.ROW_SETTING_PICTURE_SIZE]          = UNKNOWN;
        DEFAULT_VALUE_FOR_SETTING_ID[SettingConstants.ROW_SETTING_ISO]                   = R.string.pref_camera_iso_default;
        DEFAULT_VALUE_FOR_SETTING_ID[SettingConstants.ROW_SETTING_ANTI_FLICKER]          = R.string.pref_camera_antibanding_default;
        DEFAULT_VALUE_FOR_SETTING_ID[SettingConstants.ROW_SETTING_VIDEO_STABLE]          = R.string.pref_camera_eis_default;
        DEFAULT_VALUE_FOR_SETTING_ID[SettingConstants.ROW_SETTING_3DNR]                  = R.string.pref_camera_3dnr_default;
        DEFAULT_VALUE_FOR_SETTING_ID[SettingConstants.ROW_SETTING_MICROPHONE]            = R.string.pref_camera_recordaudio_default;
        DEFAULT_VALUE_FOR_SETTING_ID[SettingConstants.ROW_SETTING_AUDIO_MODE]            = R.string.pref_video_hd_recording_default;
        DEFAULT_VALUE_FOR_SETTING_ID[SettingConstants.ROW_SETTING_TIME_LAPSE]            = R.string.pref_video_time_lapse_frame_interval_default;
        DEFAULT_VALUE_FOR_SETTING_ID[SettingConstants.ROW_SETTING_VIDEO_QUALITY]         = R.string.pref_video_quality_default;
        DEFAULT_VALUE_FOR_SETTING_ID[SettingConstants.ROW_SETTING_SLOW_MOTION_VIDEO_QUALITY]  = R.string.pref_slow_motion_video_quality_default;
        DEFAULT_VALUE_FOR_SETTING_ID[SettingConstants.ROW_SETTING_STEREO_MODE]           = R.string.pref_stereo3d_mode_default;
        DEFAULT_VALUE_FOR_SETTING_ID[SettingConstants.ROW_SETTING_SHARPNESS]             = R.string.pref_camera_edge_default;
        DEFAULT_VALUE_FOR_SETTING_ID[SettingConstants.ROW_SETTING_HUE]                   = R.string.pref_camera_hue_default;
        DEFAULT_VALUE_FOR_SETTING_ID[SettingConstants.ROW_SETTING_SATURATION]            = R.string.pref_camera_saturation_default;
        DEFAULT_VALUE_FOR_SETTING_ID[SettingConstants.ROW_SETTING_BRIGHTNESS]            = R.string.pref_camera_brightness_default;
        DEFAULT_VALUE_FOR_SETTING_ID[SettingConstants.ROW_SETTING_CONTRAST]              = R.string.pref_camera_contrast_default;
        /*
         * if ("both".equals(FeatureSwitcher.featureAisMfllSupportType()) ||
         * "sqc".equals(FeatureSwitcher.featureAisMfllSupportType())) {
         * DEFAULT_VALUE_FOR_SETTING_ID[ROW_SETTING_AIS_MFLL] =
         * R.string.pref_camera_ais_default; } else {
         */
        DEFAULT_VALUE_FOR_SETTING_ID[SettingConstants.ROW_SETTING_AIS]                   = R.string.pref_camera_ais_default;
        // }
        DEFAULT_VALUE_FOR_SETTING_ID[SettingConstants.ROW_SETTING_PICTURE_RATIO]         = UNKNOWN;
        DEFAULT_VALUE_FOR_SETTING_ID[SettingConstants.ROW_SETTING_VOICE]                 = R.string.pref_voice_default;
        DEFAULT_VALUE_FOR_SETTING_ID[SettingConstants.ROW_SETTING_SLOW_MOTION]           = R.string.pref_slow_motion_default;
        DEFAULT_VALUE_FOR_SETTING_ID[SettingConstants.ROW_SETTING_FACEBEAUTY_SMOOTH]     = R.string.pref_facebeauty_smooth_default;
        DEFAULT_VALUE_FOR_SETTING_ID[SettingConstants.ROW_SETTING_FACEBEAUTY_SKIN_COLOR] = R.string.pref_facebeauty_skin_color_default;
        DEFAULT_VALUE_FOR_SETTING_ID[SettingConstants.ROW_SETTING_FACEBEAUTY_SHARP]      = R.string.pref_facebeauty_sharp_default;
        DEFAULT_VALUE_FOR_SETTING_ID[SettingConstants.ROW_SETTING_FACEBEAUTY_SLIM]       = R.string.pref_facebeauty_sharp_default;//can use the same shape value
        DEFAULT_VALUE_FOR_SETTING_ID[SettingConstants.ROW_SETTING_FACEBEAUTY_BIG_EYES]   = R.string.pref_facebeauty_big_eys_default;

        DEFAULT_VALUE_FOR_SETTING_ID[SettingConstants.ROW_SETTING_CAMERA_FACE_DETECT]    = R.string.pref_camera_face_detect_default;
        DEFAULT_VALUE_FOR_SETTING_ID[SettingConstants.ROW_SETTING_MULTI_FACE_MODE]       = UNKNOWN;
        DEFAULT_VALUE_FOR_SETTING_ID[SettingConstants.ROW_SETTING_SMILE_SHOT]            = R.string.pref_smile_shot_default;
        DEFAULT_VALUE_FOR_SETTING_ID[SettingConstants.ROW_SETTING_HDR]                   = R.string.pref_camera_hdr_default;
        DEFAULT_VALUE_FOR_SETTING_ID[SettingConstants.ROW_SETTING_ASD]                   = R.string.pref_asd_default;
        DEFAULT_VALUE_FOR_SETTING_ID[SettingConstants.ROW_SETTING_GESTURE_SHOT]          = R.string.pref_gesture_shot_default;
        DEFAULT_VALUE_FOR_SETTING_ID[SettingConstants.ROW_SETTING_DNG] = R.string.pref_dng_default;
		/*PRIZE-add setting items-wanzhijuan-2016-05-03-start*/
        DEFAULT_VALUE_FOR_SETTING_ID[SettingConstants.ROW_SETTING_CAMERA_MUTE]          = R.string.pref_camera_mute_default;
        DEFAULT_VALUE_FOR_SETTING_ID[SettingConstants.ROW_SETTING_GRIDE]          = R.string.pref_gride_default;
        DEFAULT_VALUE_FOR_SETTING_ID[SettingConstants.ROW_SETTING_TOUCHSHUTTER]          = R.string.pref_touchshutter_default;
        /*PRIZE-add setting items-wanzhijuan-2016-05-03-end*/
        /*PRIZE-add for flip setting-liufan-2017-05-24-start*/
        DEFAULT_VALUE_FOR_SETTING_ID[SettingConstants.ROW_SETTING_PRIZE_FLIP]          = R.string.setting_title_flip;
        /*PRIZE-add for flip setting-liufan-2017-05-24-end*/
        /* arc add start*/
        DEFAULT_VALUE_FOR_SETTING_ID[SettingConstants.ROW_SETTING_ARC_PICTUREZOOM_MODE]     = R.string.pref_picture_zoom_mode_title;
        DEFAULT_VALUE_FOR_SETTING_ID[SettingConstants.ROW_SETTING_ARC_LOWLIGHTSHOOT_MODE]   = R.string.pref_lowlight_shot_mode_title;
        /* arc add end*/
        /*add koobee watermark start*/
        DEFAULT_VALUE_FOR_SETTING_ID[SettingConstants.ROW_SETTING_KOOBEE_WATERMARK]   = R.string.pref_camera_koobeewatermark_default;
        /*add koobee watermark end*/
        DEFAULT_VALUE_FOR_SETTING_ID[SettingConstants.ROW_SETTING_ARC_FCAM_SUPERZOOM_ENABLE]   = R.string.pref_camera_hdr_default;
        DEFAULT_VALUE_FOR_SETTING_ID[SettingConstants.ROW_SETTING_PORTRAIT_MODE]   = R.string.pref_camera_coloreffect_default;
        DEFAULT_VALUE_FOR_SETTING_ID[SettingConstants.ROW_SETTING_PORTRAIT_SELECTED]   = R.string.pref_camera_coloreffect_portrait_default_value;
    }
    
    static {
        DEFAULT_VALUE_FOR_SETTING[SettingConstants.ROW_SETTING_CAMERA_MODE]              = Integer.toString(Parameters.CAMERA_MODE_MTK_PRV);
        DEFAULT_VALUE_FOR_SETTING[SettingConstants.ROW_SETTING_CAPTURE_MODE]             = Parameters.CAPTURE_MODE_NORMAL;
        DEFAULT_VALUE_FOR_SETTING[SettingConstants.ROW_SETTING_HEARTBEAT_MONITOR]        = "true";
        DEFAULT_VALUE_FOR_SETTING[SettingConstants.ROW_SETTING_CONTINUOUS_NUM]           = "40";
        DEFAULT_VALUE_FOR_SETTING[SettingConstants.ROW_SETTING_JPEG_QUALITY]             = Integer.toString(CameraProfile.QUALITY_HIGH);
        DEFAULT_VALUE_FOR_SETTING[SettingConstants.ROW_SETTING_RECORDING_HINT]           = Boolean.toString(false);
        DEFAULT_VALUE_FOR_SETTING[SettingConstants.ROW_SETTING_MUTE_RECORDING_SOUND]     = "off";
        DEFAULT_VALUE_FOR_SETTING[SettingConstants.ROW_SETTING_FACEBEAUTY_SLIM]          = "off";
        DEFAULT_VALUE_FOR_SETTING[SettingConstants.ROW_SETTING_FACEBEAUTY_BIG_EYES]      = "off";
        DEFAULT_VALUE_FOR_SETTING[SettingConstants.ROW_SETTING_FACE_BEAUTY]              = "off";
        DEFAULT_VALUE_FOR_SETTING[SettingConstants.ROW_SETTING_PANORAMA]                 = "off";
        DEFAULT_VALUE_FOR_SETTING[SettingConstants.ROW_SETTING_PHOTO_PIP]                = "off";
        DEFAULT_VALUE_FOR_SETTING[SettingConstants.ROW_SETTING_VIDEO_PIP]                = "off";
        DEFAULT_VALUE_FOR_SETTING[SettingConstants.ROW_SETTING_VIDEO]                    = "off";
        DEFAULT_VALUE_FOR_SETTING[SettingConstants.ROW_SETTING_OBJECT_TRACKING]          = "off";
		//gangyun tech add begin
	    DEFAULT_VALUE_FOR_SETTING[SettingConstants.ROW_SETTING_GANGYUN_BEAUTY]           = "off"; 
	    DEFAULT_VALUE_FOR_SETTING[SettingConstants.ROW_SETTING_GANGYUN_BOKEH]            = "off";
		//gangyun tech add end
    }
    
    public static final String PICTURE_RATIO_16_9 = "1.7778";
    private static final String PICTURE_RATIO_5_3 = "1.6667";
    private static final String PICTURE_RATIO_3_2 = "1.5";
	
	/*PRIZE-add setting items-wanzhijuan-2016-05-03-start*/ 
    public static final String PICTURE_RATIO_4_3 = "1.3333";
    public static final String PICTURE_RATIO_18_9 = "2.0";
    public static final String PICTURE_RATIO_19_9 = "2.0719";
    
    
    public static final double PICTURE_RATIO_16_9_D = 1.7778;
    public static final double PICTURE_RATIO_18_9_D = 2.0;
    public static final double PICTURE_RATIO_4_3_D = 1.3333;
    public static final double PICTURE_RATIO_19_9_D = 2.0719;
	/*PRIZE-add setting items-wanzhijuan-2016-05-03-end*/ 
    
    private static final String[] PICTURE_SIZE_4_3 = new String[] {
        "320x240",
        "640x480",
        "1024x768",
        "1280x960",
        "1600x1200",
        "2048x1536",
        "2560x1920",
        "3264x2448",
        "3600x2700",
        "3672x2754",
        "4096x3072",
        "4160x3120",
        "4608x3456",
        "5120x3840",
        "5312x3984",
        "4656x3496",
    };
    private static final String[] PICTURE_SIZE_16_9 = new String[] {
        "256x144",
        "320x180",
        "512x288",
        "640x360",
        "1280x720",
        "1600x912",
        "2048x1152",
        "2560x1440",
        "3328x1872",
        "4096x2304",
        "4608x2592",
        "5120x2880",
        "5280x2992",
        "5376x3024",
    };
    private static final String[] PICTURE_SIZE_5_3 = new String[] {
        "320x192",
        "400x240",
        "560x336",
        "640x384",
        "1280x768",
        "1600x960",
        "1840x1104",
        "2880x1728",
        "3600x2160",
    };
    private static final String[] PICTURE_SIZE_3_2 = new String[] {
        "1024x688",
        "1280x864",
        "1440x960",
        "2048x1360",
        "2560x1712"
    };
    
    /*prize-add-18:9 full screen-xiaoping-20180423-start*/
    private static final String[] PICTURE_SIZE_2_1 = new String[] {
            "4608x2304"
    };
    private static final String[] PICTURE_SIZE_19_9 = new String[] {
            "4608x2224"
    };
    /*prize-add-18:9 full screen-xiaoping-20180423-end*/
    
    private static final String[] PICTURE_SIZE = new String[] {
    	"256x144",
        "512x288",
        "1280x720",
        "1600x912",
        "2048x1152",
        "2560x1440",
        "3328x1872",
        "4096x2304",
        "4608x2592",
        "5120x2880",
        "320x240",
        "640x480",
        "1024x768",
        "1280x960",
        "1600x1200",
        "2048x1536",
        "2560x1920",
        "3264x2448",
        "3600x2700",
        "3672x2754",
        "4096x3072",
        "4160x3120",
        "4608x3456",
        "5120x3840",
        "4608x2592",
        "4096x3072",
        "4656x3496",
        "3072x1728",
        "4608x2304",
            "4608x2224"
    };
    
    public static final String[] VIDEO_QUALITY_WHITHOUT_FINE = new String[] {
        Integer.toString(CamcorderProfileEx.QUALITY_HIGH) ,
        Integer.toString(CamcorderProfileEx.QUALITY_MEDIUM),
        Integer.toString(CamcorderProfileEx.QUALITY_LOW),
     };
    
    /*PRIZE-add setting items-wanzhijuan-2016-05-03-start*/ 
    public static boolean is4Divide3(String ratio) {
    	if (TextUtils.isEmpty(ratio)) {
    		return false;
    	}
    	for (String str : PICTURE_SIZE_4_3) {
    		if (ratio.equals(str)) {
    			return true;
    		}
    	}
    	return false;
    }
    /*PRIZE-add setting items-wanzhijuan-2016-05-03-end*/ 
    
    /*prize-add-18:9 full screen-xiaoping-20180423-start*/

    public static String getPictureRatio(String ratio) {
        if (TextUtils.isEmpty(ratio)) {
            return "";
        }
        for (String str : PICTURE_SIZE_4_3) {
            if (ratio.equals(str)) {
                return PICTURE_RATIO_4_3;
            }
        }
        for (String str : PICTURE_SIZE_16_9) {
            if (ratio.equals(str)) {
                return PICTURE_RATIO_16_9;
            }
        }
        for (String str : PICTURE_SIZE_2_1) {
            if (ratio.equals(str)) {
                return PICTURE_RATIO_18_9;
            }
        }
        for (String str : PICTURE_SIZE_19_9) {
            if (ratio.equals(str)) {
                return PICTURE_RATIO_19_9;
            }
        }
        return "";
    }
    /*prize-add-18:9 full screen-xiaoping-20180423-end*/
    
    private static final String[] VIDEO_SUPPORT_SCENE_MODE = new String[] {
        Parameters.SCENE_MODE_AUTO,
        Parameters.SCENE_MODE_SUNSET,
        Parameters.SCENE_MODE_PARTY,
        Parameters.SCENE_MODE_PORTRAIT,
        Parameters.SCENE_MODE_LANDSCAPE,
        Parameters.SCENE_MODE_NIGHT_PORTRAIT,
        Parameters.SCENE_MODE_THEATRE,
        Parameters.SCENE_MODE_BEACH,
        Parameters.SCENE_MODE_SNOW,
        Parameters.SCENE_MODE_STEADYPHOTO,
        Parameters.SCENE_MODE_SPORTS,
        Parameters.SCENE_MODE_CANDLELIGHT
    };
 
    private static final Restriction[] RESTRICTIOINS = new Restriction[]{
        new Restriction(SettingConstants.ROW_SETTING_TIME_LAPSE) //should be checked.
            .setValues("1000", "1500", "2000", "2500", "3000", "5000", "10000")
            .setRestrictions(
                new Restriction(SettingConstants.ROW_SETTING_MICROPHONE)
                    .setEnable(false)
                    .setValues("off"),
                new Restriction(SettingConstants.ROW_SETTING_AUDIO_MODE)
                    .setEnable(false)
                    .setValues("normal")),
        
        new Restriction(SettingConstants.ROW_SETTING_SLOW_MOTION)
            .setValues("on")
            .setRestrictions(
                new Restriction(SettingConstants.ROW_SETTING_VIDEO_STABLE)
                    .setEnable(false)
                    .setValues("off"),
                new Restriction(SettingConstants.ROW_SETTING_TIME_LAPSE)
                    .setEnable(false)
                    .setValues("0")),
                    
        new Restriction(SettingConstants.ROW_SETTING_SLOW_MOTION)
            .setType(Restriction.TYPE_SETTING)
            .setValues("on")
            .setRestrictions(
                new Restriction(SettingConstants.ROW_SETTING_SCENCE_MODE)
                    .setEnable(false)
                    .setValues(Parameters.SCENE_MODE_AUTO),
                new Restriction(SettingConstants.ROW_SETTING_ASD)
                    .setEnable(false)
                    .setValues("off"),
                new Restriction(SettingConstants.ROW_SETTING_3DNR)
                    .setEnable(false)
                    .setValues("off"),
                new Restriction(SettingConstants.ROW_SETTING_HDR)
                    .setEnable(false)
                    .setValues("off")),

        new Restriction(SettingConstants.ROW_SETTING_FAST_AF)
              .setValues("on")
              .setRestrictions(
                  new Restriction(SettingConstants.ROW_SETTING_PICTURE_RATIO)
                       .setEnable(true)
                       .setValues(PICTURE_RATIO_16_9),
                  new Restriction(SettingConstants.ROW_SETTING_SLOW_MOTION)
                       .setEnable(false)
                       .setValues("off"),
                 new Restriction(SettingConstants.ROW_SETTING_HDR)
                       .setEnable(false)
                       .setValues("off")),
                  
       new Restriction(SettingConstants.ROW_SETTING_DISTANCE)
                .setValues("on")
                .setRestrictions(
                   new Restriction(SettingConstants.ROW_SETTING_PICTURE_RATIO)
                        .setEnable(true)
                        .setValues(PICTURE_RATIO_16_9),
                   new Restriction(SettingConstants.ROW_SETTING_SLOW_MOTION)
                        .setEnable(false)
                        .setValues("off"),
                  new Restriction(SettingConstants.ROW_SETTING_HDR)
                        .setEnable(false)
                        .setValues("off")),

        new Restriction(SettingConstants.ROW_SETTING_MICROPHONE) //should be checked.
            .setValues("off")
            .setRestrictions(
                new Restriction(SettingConstants.ROW_SETTING_AUDIO_MODE)
                    .setEnable(false)
                    .setValues("normal")),
            
        new Restriction(SettingConstants.ROW_SETTING_VIDEO_QUALITY)//23 means quality is Fine(4k2k)
            .setValues("23")
            .setRestrictions(
                new Restriction(SettingConstants.ROW_SETTING_3DNR)
                    .setEnable(false)
                    .setValues("off")),
                
        new Restriction(SettingConstants.ROW_SETTING_RECORDING_HINT)
            .setType(Restriction.TYPE_SETTING)
            .setValues(Boolean.toString(false))
            .setRestrictions(
                new Restriction(SettingConstants.ROW_SETTING_FLASH)
                    .setEnable(true)
                    .setMappingFinder(new FlashMappingFinder())
                    .setValues(Parameters.FLASH_MODE_AUTO, Parameters.FLASH_MODE_ON, Parameters.FLASH_MODE_OFF, Parameters.FLASH_MODE_TORCH)),
                
        new Restriction(SettingConstants.ROW_SETTING_RECORDING_HINT)
            .setType(Restriction.TYPE_SETTING)
            .setValues(Boolean.toString(true))
            .setRestrictions(
                new Restriction(SettingConstants.ROW_SETTING_FLASH)
                    .setEnable(true)
                    .setMappingFinder(new FlashMappingFinder())
                    .setValues(Parameters.FLASH_MODE_AUTO, Parameters.FLASH_MODE_ON, Parameters.FLASH_MODE_OFF, Parameters.FLASH_MODE_TORCH)), //prize-bug:16757 flash-mode jump to Torch mode when recording-pengcancan-20160601//
                    
        new Restriction(SettingConstants.ROW_SETTING_FLASH)
                    .setValues(Parameters.FLASH_MODE_AUTO, Parameters.FLASH_MODE_ON, Parameters.FLASH_MODE_TORCH)
                    .setRestrictions(
                        new Restriction(SettingConstants.ROW_SETTING_HDR)
                            .setEnable(true)
                            .setValues(SettingUtils.RESET_STATE_VALUE_OFF)),
           
        new Restriction(SettingConstants.ROW_SETTING_HDR)
            .setValues("on")
            .setRestrictions(
                new Restriction(SettingConstants.ROW_SETTING_VIDEO_QUALITY)
                    .setEnable(true)
                    .setValues(VIDEO_QUALITY_WHITHOUT_FINE)),
        
        new Restriction(SettingConstants.ROW_SETTING_FACE_BEAUTY)
            .setValues("on")
            .setRestrictions(
                new Restriction(SettingConstants.ROW_SETTING_VIDEO_QUALITY)
                    .setEnable(true)
                    .setValues(VIDEO_QUALITY_WHITHOUT_FINE)),
        /*PRIZE-modify Photo size function-wanzhijuan-2016-05-03-start*/             
        new Restriction(SettingConstants.ROW_SETTING_PICTURE_RATIO)
            .setType(Restriction.TYPE_SETTING)
            .setValues(PICTURE_RATIO_19_9,PICTURE_RATIO_18_9,PICTURE_RATIO_16_9, PICTURE_RATIO_4_3)
            .setRestrictions(
                new Restriction(SettingConstants.ROW_SETTING_PICTURE_SIZE)
                    .setEnable(true)
                    .setMappingFinder(new PictureSizeMappingFinder())
                    .setValues(PICTURE_SIZE)),
                    
        /*new Restriction(SettingConstants.ROW_SETTING_PICTURE_RATIO)
            .setType(Restriction.TYPE_SETTING)
            .setValues(PICTURE_RATIO_16_9)
            .setRestrictions(
                new Restriction(SettingConstants.ROW_SETTING_PICTURE_SIZE)
                    .setEnable(true)
                    .setMappingFinder(new PictureSizeMappingFinder())
                    .setValues(PICTURE_SIZE_16_9)),
                    
        new Restriction(SettingConstants.ROW_SETTING_PICTURE_RATIO)
            .setType(Restriction.TYPE_SETTING)
            .setValues(PICTURE_RATIO_5_3)
            .setRestrictions(
                new Restriction(SettingConstants.ROW_SETTING_PICTURE_SIZE)
                    .setEnable(true)
                    .setMappingFinder(new PictureSizeMappingFinder())
                    .setValues(PICTURE_SIZE_5_3)),
                    
        new Restriction(SettingConstants.ROW_SETTING_PICTURE_RATIO)
            .setType(Restriction.TYPE_SETTING)
            .setValues(PICTURE_RATIO_3_2)
            .setRestrictions(
                new Restriction(SettingConstants.ROW_SETTING_PICTURE_SIZE)
                    .setEnable(true)
                    .setMappingFinder(new PictureSizeMappingFinder())
                    .setValues(PICTURE_SIZE_3_2)),*/
        /*PRIZE-modify Photo size function-wanzhijuan-2016-05-03-end*/             
        new Restriction(SettingConstants.ROW_SETTING_SMILE_SHOT)
            .setValues("on")
            .setRestrictions(
                new Restriction(SettingConstants.ROW_SETTING_SCENCE_MODE)
                    .setEnable(true)
                    .setValues(Parameters.SCENE_MODE_AUTO, Parameters.SCENE_MODE_PORTRAIT)),
                
        new Restriction(SettingConstants.ROW_SETTING_VIDEO_QUALITY)
            .setValues(Integer.toString(CamcorderProfileEx.QUALITY_FINE_4K2K))
            .setRestrictions(
                new Restriction(SettingConstants.ROW_SETTING_3DNR)
                    .setEnable(false)
                    .setValues("off")),
                    
        new Restriction(SettingConstants.ROW_SETTING_VIDEO)
            .setValues("on")
            .setRestrictions(
                new Restriction(SettingConstants.ROW_SETTING_SCENCE_MODE)
                    .setEnable(true)
                    .setValues(VIDEO_SUPPORT_SCENE_MODE),
                new Restriction(SettingConstants.ROW_SETTING_HDR)
                    .setEnable(false)
                    .setValues("off")),
      
       new Restriction(SettingConstants.ROW_SETTING_PREVIDEO)
                    .setValues("on")
                    .setRestrictions(
                        new Restriction(SettingConstants.ROW_SETTING_SCENCE_MODE)
                            .setEnable(true)
                            .setValues(VIDEO_SUPPORT_SCENE_MODE)),              

        new Restriction(SettingConstants.ROW_SETTING_DNG)
            .setValues("on")
            .setRestrictions(
                new Restriction(SettingConstants.ROW_SETTING_HDR)
                    .setEnable(false)
                    .setValues("off")),

        new Restriction(SettingConstants.ROW_SETTING_CAPTURE_MODE)
        .setValues(Parameters.CAPTURE_MODE_NORMAL, Parameters.SCENE_MODE_HDR, 
                Parameters.CAPTURE_MODE_ASD, /*Parameters.CAPTURE_MODE_SMILE_SHOT,*/ "autorama")
        .setRestrictions(
            new Restriction(SettingConstants.ROW_SETTING_FACEBEAUTY_SMOOTH)
                .setValues("disable-value")),
                
        new Restriction(SettingConstants.ROW_SETTING_CAPTURE_MODE)
        .setValues(Parameters.CAPTURE_MODE_NORMAL, Parameters.SCENE_MODE_HDR, 
                Parameters.CAPTURE_MODE_ASD, /*Parameters.CAPTURE_MODE_SMILE_SHOT,*/ "autorama")
        .setRestrictions(
            new Restriction(SettingConstants.ROW_SETTING_FACEBEAUTY_SKIN_COLOR)
                .setValues("disable-value")),
                
        new Restriction(SettingConstants.ROW_SETTING_CAPTURE_MODE)
        .setValues(Parameters.CAPTURE_MODE_NORMAL, Parameters.SCENE_MODE_HDR, 
                Parameters.CAPTURE_MODE_ASD, /*Parameters.CAPTURE_MODE_SMILE_SHOT,*/ "autorama")
        .setRestrictions(
            new Restriction(SettingConstants.ROW_SETTING_FACEBEAUTY_SHARP)
                .setValues("disable-value")),
                
        new Restriction(SettingConstants.ROW_SETTING_CAPTURE_MODE)
        .setValues(Parameters.CAPTURE_MODE_NORMAL, Parameters.SCENE_MODE_HDR, 
                Parameters.CAPTURE_MODE_ASD, /*Parameters.CAPTURE_MODE_SMILE_SHOT,*/ "autorama")
        .setRestrictions(
            new Restriction(SettingConstants.ROW_SETTING_FACEBEAUTY_SLIM)
                .setValues("disable-value")),
                
        new Restriction(SettingConstants.ROW_SETTING_CAPTURE_MODE)
        .setValues(Parameters.CAPTURE_MODE_NORMAL, Parameters.SCENE_MODE_HDR, 
                Parameters.CAPTURE_MODE_ASD, /*Parameters.CAPTURE_MODE_SMILE_SHOT,*/ "autorama")
        .setRestrictions(
            new Restriction(SettingConstants.ROW_SETTING_FACEBEAUTY_BIG_EYES)
                .setValues("disable-value")),
        
        /*new Restriction(SettingConstants.ROW_SETTING_ARC_PICSELFIE_ENABLE)
        .setValues("on")
        .setRestrictions(
            new Restriction(SettingConstants.ROW_SETTING_ZSD)
                .setEnable(false)
                .setValues("off")),*/
        /*prize-add-bugid:41923 turn white balance off on arc picselfie-xiaoping-20171113-start*/
        new Restriction(SettingConstants.ROW_SETTING_ARC_PICSELFIE_ENABLE)
            .setValues("on")
            .setRestrictions(
                    new Restriction(SettingConstants.ROW_SETTING_WHITE_BALANCE)
                            .setValues("auto")
                            .setEnable(false)),
        /*prize-add-bugid:41923 turn white balance off on arc picselfie-xiaoping-20171113-end*/
            new Restriction(SettingConstants.ROW_SETTING_SHORT_PREVIDEO)
                    .setValues("on")
                    .setRestrictions(
                    new Restriction(SettingConstants.ROW_SETTING_SCENCE_MODE)
                            .setEnable(true)
                            .setValues(VIDEO_SUPPORT_SCENE_MODE)),  
    };
    
    private static final int[][] MATRIX_SCENE_STATE = new int[SettingConstants.SETTING_COUNT][];
    static {
                                                                                      //action  portrait    land     night    nightport theatre    beach      snow     sunset    steady  fireworks    spot     party    candle     auto     normal    hdr
        MATRIX_SCENE_STATE[SettingConstants.ROW_SETTING_EXPOSURE]        = new int[]{STATE_R0, STATE_R0, STATE_R0, STATE_R0, STATE_R0, STATE_R0, STATE_R1, STATE_R1, STATE_R0, STATE_R0, STATE_R0, STATE_R0, STATE_R0, STATE_R0, STATE_E0, STATE_E0, STATE_E0};//2: ev
        MATRIX_SCENE_STATE[SettingConstants.ROW_SETTING_SLOW_MOTION]     = new int[]{STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0};
        MATRIX_SCENE_STATE[SettingConstants.ROW_SETTING_WHITE_BALANCE]   = new int[]{STATE_R0, STATE_R0, STATE_R1, STATE_R0, STATE_R0, STATE_R0, STATE_R0, STATE_R0, STATE_R1, STATE_R0, STATE_R0, STATE_R0, STATE_R0, STATE_R2, STATE_E0, STATE_E0, STATE_E0};//4: white balance
        MATRIX_SCENE_STATE[SettingConstants.ROW_SETTING_ISO]             = new int[]{STATE_R0, STATE_R0, STATE_R0, STATE_R0, STATE_R0, STATE_R0, STATE_R0, STATE_R0, STATE_R0, STATE_R0, STATE_R0, STATE_R0, STATE_R0, STATE_R0, STATE_E0, STATE_E0, STATE_E0};//12: ISO
        MATRIX_SCENE_STATE[SettingConstants.ROW_SETTING_SHARPNESS]       = new int[]{STATE_R0, STATE_R1, STATE_R2, STATE_R1, STATE_R1, STATE_R2, STATE_R2, STATE_R2, STATE_R2, STATE_R0, STATE_R0, STATE_R0, STATE_R0, STATE_R0, STATE_E0, STATE_E0, STATE_E0};//32: image adjustment sharpness
        MATRIX_SCENE_STATE[SettingConstants.ROW_SETTING_HUE]             = new int[]{STATE_R0, STATE_R0, STATE_R0, STATE_R0, STATE_R0, STATE_R0, STATE_R0, STATE_R0, STATE_R0, STATE_R0, STATE_R0, STATE_R0, STATE_R0, STATE_R0, STATE_E0, STATE_E0, STATE_E0};//30: image adjustment hue
        MATRIX_SCENE_STATE[SettingConstants.ROW_SETTING_SATURATION]      = new int[]{STATE_R0, STATE_R0, STATE_R0, STATE_R0, STATE_R0, STATE_R1, STATE_R0, STATE_R0, STATE_R0, STATE_R0, STATE_R0, STATE_R0, STATE_R0, STATE_R0, STATE_E0, STATE_E0, STATE_E0};//33: image adjustment saturation
        MATRIX_SCENE_STATE[SettingConstants.ROW_SETTING_BRIGHTNESS]      = new int[]{STATE_R0, STATE_R0, STATE_R0, STATE_R0, STATE_R0, STATE_R0, STATE_R0, STATE_R0, STATE_R0, STATE_R0, STATE_R0, STATE_R0, STATE_R0, STATE_R0, STATE_E0, STATE_E0, STATE_E0};//34: image adjustment brightness
        MATRIX_SCENE_STATE[SettingConstants.ROW_SETTING_CONTRAST]        = new int[]{STATE_R0, STATE_R0, STATE_R0, STATE_R0, STATE_R0, STATE_R0, STATE_R0, STATE_R0, STATE_R0, STATE_R0, STATE_R0, STATE_R0, STATE_R0, STATE_R0, STATE_E0, STATE_E0, STATE_E0};//31: image adjustment contrast
        MATRIX_SCENE_STATE[SettingConstants.ROW_SETTING_FLASH]           = new int[]{STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_R0, STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_OFF};//0: flash
        MATRIX_SCENE_STATE[SettingConstants.ROW_SETTING_FAST_AF]         = new int[]{STATE_R0, STATE_R0, STATE_R0, STATE_R0, STATE_R0, STATE_R0, STATE_R0, STATE_R0, STATE_R0, STATE_R0, STATE_R0, STATE_R0, STATE_R0, STATE_R0, STATE_E0, STATE_E0, STATE_E0};//31: image adjustment contrast
        MATRIX_SCENE_STATE[SettingConstants.ROW_SETTING_DISTANCE]        = new int[]{STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_R0, STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0, STATE_E0};//0: flash
    }
    
    private static final String[] MATRIX_SCENE_COLUMN = {
        "action",
        "portrait",
        "landscape",
        "night",
        "night-portrait",
        "theatre",
        "beach",
        "snow",
        "sunset",
        "steadyphoto",
        "fireworks",
        "sports",
        "party",
        "candlelight",
        "auto",
        "normal",
        "hdr",
    };
    

    private static class FlashMappingFinder implements MappingFinder {
        @Override
        public String find(String current, List<String> supportedList) {
            String supported = current;
            if (supportedList != null && !supportedList.contains(current)) {
                if (Parameters.FLASH_MODE_ON.equals(current)) { //if cannot find on, it video mode, match torch
                    supported = Parameters.FLASH_MODE_TORCH;
                } else if (Parameters.FLASH_MODE_TORCH.equals(current)) { //if cannot find torch, it video mode, match on
                    supported = Parameters.FLASH_MODE_ON;
                }
            }
            if (!supportedList.contains(supported)) {
                supported = supportedList.get(0);
            }
            Log.i(TAG, "find(" + current + ") return " + supported);
            return supported;
        }
        @Override
        public int findIndex(String current, List<String> supportedList) {
            int index = UNKNOWN;
            if (supportedList != null) {
                String supported = find(current, supportedList);
                index = supportedList.indexOf(supported);
            }
            Log.d(TAG, "findIndex(" + current + ", " + supportedList + ") return " + index);
            return index;
        }
    }
    
    private static class PictureSizeMappingFinder implements MappingFinder {
        @Override
        public String find(String current, List<String> supportedList) {
            // Always choose the max picture, the picture size is in ascending order.
            if (supportedList.contains(current)) {
                return current;
            } else {
                return supportedList.get(supportedList.size() - 1);
            }
        }

        @Override
        public int findIndex(String current, List<String> supportedList) {
            int index = UNKNOWN;
            if (supportedList != null) {
                String supported = find(current, supportedList);
                index = supportedList.indexOf(supported);
            }
            Log.d(TAG, "findIndex(" + current + ", " + supportedList + ") return " + index);
            return index;
        }
    }
    
    public static String getSettingResetValue(int row, int state) {
        if (state == STATE_E0) {
            return null;
        }
        if (state == STATE_D0) {
            return SettingUtils.RESET_STATE_VALUE_DISABLE;
        }
        
        if(state == STATE_OFF){
        	return SettingUtils.RESET_STATE_VALUE_OFF;
        }
        if (state >= STATE_R0 && state <= STATE_R7) {
        	Log.d(TAG, "getSettingResetValue row:"+row+", state:"+state);
            return RESET_STATE_VALUE[row][state % 300];
        }
        
        return null;
    }
    
    public static int[][] getRestrictionMatrix() {
        return MATRIX_RESTRICTION_STATE;
    }
    
    public static int getSettingIndex(int column) {
        int settingIndex = -1;
        if (column < RESTRCTION_SETTING_INDEX.length) {
            settingIndex = RESTRCTION_SETTING_INDEX[column];
        }
        return settingIndex;
    }
    
    public static int getSettingColumn(int settingId) {
        int column = -1;
        for (int i = 0; i < RESTRCTION_SETTING_INDEX.length; i++) {
            if (settingId == RESTRCTION_SETTING_INDEX[i]) {
                column = i;
                break;
            }
        }
        return column;
    }
    
    public static int getDefaultSettingID(int settingIndex) {
        return DEFAULT_VALUE_FOR_SETTING_ID[settingIndex];
    }
    
    public static void setDefaultValue(int settingIndex, String value) {
        DEFAULT_VALUE_FOR_SETTING[settingIndex] = value;
    }
    
    public static String getDefaultValue(int settingIndex) {
        return DEFAULT_VALUE_FOR_SETTING[settingIndex];
    }
    
    public static String getSceneMode(int column) {
        return MATRIX_SCENE_COLUMN[column];
    }
    
    public static int[][] getSceneRestrictionMatrix() {
        return MATRIX_SCENE_STATE;
    }
    
    public static Restriction[] getRestrictions() {
        return RESTRICTIOINS;
    }
}
