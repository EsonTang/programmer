/*
 * Copyright Statement:
 *
 *   This software/firmware and related documentation ("MediaTek Software") are
 *   protected under relevant copyright laws. The information contained herein is
 *   confidential and proprietary to MediaTek Inc. and/or its licensors. Without
 *   the prior written permission of MediaTek inc. and/or its licensors, any
 *   reproduction, modification, use or disclosure of MediaTek Software, and
 *   information contained herein, in whole or in part, shall be strictly
 *   prohibited.
 *
 *   MediaTek Inc. (C) 2016. All rights reserved.
 *
 *   BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 *   THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 *   RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER
 *   ON AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL
 *   WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 *   WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 *   NONINFRINGEMENT. NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH
 *   RESPECT TO THE SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY,
 *   INCORPORATED IN, OR SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES
 *   TO LOOK ONLY TO SUCH THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO.
 *   RECEIVER EXPRESSLY ACKNOWLEDGES THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO
 *   OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES CONTAINED IN MEDIATEK
 *   SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE
 *   RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 *   STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S
 *   ENTIRE AND CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE
 *   RELEASED HEREUNDER WILL BE, AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE
 *   MEDIATEK SOFTWARE AT ISSUE, OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE
 *   CHARGE PAID BY RECEIVER TO MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 *   The following software/firmware and/or related documentation ("MediaTek
 *   Software") have been modified by MediaTek Inc. All revisions are subject to
 *   any receiver's applicable license agreements with MediaTek Inc.
 */
package com.prize.setting;

import android.content.ContentResolver;

import android.content.ContentUris;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.CameraProfile;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Files.FileColumns;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Images.ImageColumns;
import android.provider.MediaStore.Video;
import com.android.camera.Log;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Locale;

import com.android.prize.CreateCirleBitmap;

import android.os.SystemProperties;
/**
 * create bitmap for image or video.
 */
public class BitmapCreator {

	public static final  String TAG = "BitmapCreator";

    /**
     * create bitmap from the YUV data.
     * @param yuvData
     *            the YUV data.
     * @param targetWidth
     *            the view width where the bitmap shows.
     * @param yuvWidth
     *            the width of YUV image.
     * @param yuvHeight
     *            the height of YUV image.
     * @param orientation
     *            the orientation of YUV image.
     * @param imageFormat
     *            the image format of YUV image, must be NV21 OR YUY2.
     * @return the bitmap decode from YUV data.
     */
    public static Bitmap createBitmapFromYuv(byte[] yuvData, int imageFormat,
            int yuvWidth, int yuvHeight, int targetWidth, int orientation) {
        Log.d(TAG, "[createBitmapFromYuv] yuvData = " + yuvData
                + ", yuvWidth = " + yuvWidth + ", yuvHeight = " + yuvHeight
                + ", orientation = " + orientation + ", imageFormat = "
                + imageFormat);
        if (isNeedDumpYuv()) {
            dumpYuv("/sdcard/postView.yuv", yuvData);
        }
        if (yuvData != null) {
            byte[] jpeg = covertYuvDataToJpeg(yuvData, imageFormat, yuvWidth,
                    yuvHeight);
            int ratio = (int) Math.ceil((double) Math.min(yuvWidth, yuvHeight)
                    / targetWidth);
            int inSampleSize = Integer.highestOneBit(ratio);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = inSampleSize;
            try {
                Bitmap bitmap = BitmapFactory.decodeByteArray(jpeg, 0,
                        jpeg.length, options);
                Log.d(TAG, "[createBitmapFromYuv] end");
                return CreateCirleBitmap.getCircleBitmap(rotateBitmap(bitmap, orientation), targetWidth);
            } catch (OutOfMemoryError e) {
                Log.e(TAG, "createBitmapFromYuv fail", e);
                return null;
            }
        }
        return null;
    }

    private static boolean isNeedDumpYuv() {
        boolean enable = SystemProperties.getInt(
                "debug.thumbnailFromYuv.enable", 0) == 1 ? true : false;
        Log.d(TAG, "[isNeedDumpYuv] return :" + enable);
        return enable;
    }
    
    private static void dumpYuv(String filePath, byte[] data) {
        FileOutputStream out = null;
        try {
            Log.d(TAG, "[dumpYuv] begin");
            out = new FileOutputStream(filePath);
            out.write(data);
            out.close();
        } catch (IOException e) {
            Log.e(TAG, "[dumpYuv]Failed to write image,ex:", e);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    Log.e(TAG, "[dumpYuv]IOException:", e);
                }
            }
        }
        Log.d(TAG, "[dumpYuv] end");
    }

    /**
     * Encode YUV to jpeg, and crop it.
     * @param data the yuv data.
     * @param imageFormat the yuv format.
     * @param yuvWidth the yuv width.
     * @param yuvHeight the yuv height.
     * @return the jpeg data.
     */
    public static byte[] covertYuvDataToJpeg(byte[] data, int imageFormat,
            int yuvWidth, int yuvHeight) {
        byte[] jpeg;
        Rect rect = new Rect(0, 0, yuvWidth, yuvHeight);
        YuvImage yuvImg = new YuvImage(data, imageFormat, yuvWidth, yuvHeight,
                null);
        ByteArrayOutputStream outputstream = new ByteArrayOutputStream();
        int jpegQuality = CameraProfile
                .getJpegEncodingQualityParameter(CameraProfile.QUALITY_HIGH);
        yuvImg.compressToJpeg(rect, jpegQuality, outputstream);
        jpeg = outputstream.toByteArray();
        return jpeg;
    }

    private static Bitmap rotateBitmap(Bitmap bitmap, int orientation) {
        if (orientation != 0) {
            // We only rotate the thumbnail once even if we get OOM.
            Matrix m = new Matrix();
            m.setRotate(orientation, bitmap.getWidth() / 2,
                    bitmap.getHeight() / 2);
            try {
                Bitmap rotated = Bitmap.createBitmap(bitmap, 0, 0,
                        bitmap.getWidth(), bitmap.getHeight(), m, true);
                return rotated;
            } catch (IllegalArgumentException t) {
                Log.w(TAG, "Failed to rotate bitmap", t);
            }
        }
        return bitmap;
    }
}
