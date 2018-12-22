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
package com.prize.mix;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import com.android.camera.Log;
import android.util.Size;
import android.view.Surface;

import com.android.camera.Util;

import com.mediatek.camera.mode.pip.pipwrapping.PIPOperator.PIPCustomization;

import java.util.HashMap;
import java.util.Map;
import com.mediatek.camera.mode.pip.pipwrapping.AnimationRect;
import com.mediatek.camera.mode.pip.pipwrapping.EglCore;
import com.mediatek.camera.mode.pip.pipwrapping.TopGraphicRenderer;
import com.mediatek.camera.mode.pip.pipwrapping.WindowSurface;
import com.mediatek.camera.mode.pip.pipwrapping.GLUtil;

/**
 * Pip renderer manager.
 */
public class PrizeRendererManager {
    private static final String TAG = PrizeRendererManager.class.getSimpleName();
    private static final String TOP = "pip_top";
    private final Activity mActivity;

    private int mCurrentOrientation;
    private AnimationRect mPreviewTopGraphicRect = null;

    private HandlerThread mCaptureEglThread;
    private CaptureRendererHandler mCaptureEglHandler;

    private Bitmap mWbBitmap;

    public PrizeRendererManager(Activity activity) {
        mActivity = activity;
    }

    public void unInit() {
        if (mCaptureEglHandler != null) {
            doReleaseAndQuitThread(mCaptureEglHandler, mCaptureEglThread);
            mCaptureEglHandler = null;
            mCaptureEglThread = null;
        }
        Log.i(TAG, "[unInit]-");
    }

    /**
     * update pip template resource.
     * Note: if resource id is the same with previous, call this function has no use.
     * @param backResId bottom graphic template
     * @param frontResId top graphic template
     * @param highlightResId top graphic highlight template
     * @param editBtnResId top graphic edit template
     */
    public void updateResource(Bitmap wbBitmap) {
    	releaseBitmap();
        mWbBitmap = Bitmap.createBitmap(wbBitmap, 0, 0, wbBitmap.getWidth(), wbBitmap.getHeight());
    }
    
    private void releaseBitmap() {
    	if (mWbBitmap != null && mWbBitmap.isRecycled()) {
    		mWbBitmap.recycle();
    	}
    }

    /**
     * create two surface textures, switch pip by needSwitchPIP
     * <p>
     * By default, bottom surface texture is drawn in bottom graphic.
     * top surface texture is drawn in top graphic.
     */
    public void setUpSurfaceTextures() {
        Log.i(TAG, "[setUpSurfaceTextures]-");
        boolean needUpdate = false;
        // press home key exit pip and resume again, template update action will not happen
        // here call update template for this case.
        // update template should not block ui thread
        Log.i(TAG, "[setUpSurfaceTextures]-");
    }

    /**
     * update top graphic's position.
     * @param topGraphic the top grapihc's position
     */
    public void updateTopGraphic(AnimationRect topGraphic) {
        Log.i(TAG, "updateTopGraphic");
        mPreviewTopGraphicRect = topGraphic;
    }

    /**
     * when G-sensor's orientation changed, should update it to PIPOperator.
     * @param newOrientation G-sensor's new orientation
     */
    public void updateGSensorOrientation(int newOrientation) {
        Log.i(TAG, "updateOrientation newOrientation = " + newOrientation);
        mCurrentOrientation = newOrientation;
    }

    public SurfaceTexture getTopCapSt() {
        return mCaptureEglHandler == null ? null : mCaptureEglHandler.getTopSt();
    }

    /**
     *
     * @return pixel format that this egl can output.
     */
    public int initCapture(int[] inputFormats) {
        checkAndCreateCaptureGLThread(inputFormats);
        return mCaptureEglHandler.getPixelFormat();
    }

    /**
     * @param surface the surface used for taking picture.
     */
    public void setCaptureSurface(Surface surface) {
        if (mCaptureEglHandler != null) {
            mCaptureEglHandler.obtainMessage(
                    CaptureRendererHandler.MSG_SETUP_CAPTURE_SURFACE, surface).sendToTarget();
            waitDone(mCaptureEglHandler);
        }
    }
    
    /**
    *
    * @param topCaptureSize top picture size
    */
    public void setCaptureSize(Size topCaptureSize) {
        if (mCaptureEglHandler != null) {
            Map<String, Size> pictureSizeMap = new HashMap<String, Size>();
            pictureSizeMap.put(TOP, topCaptureSize);
            mCaptureEglHandler.obtainMessage(
                   CaptureRendererHandler.MSG_SETUP_PICTURE_TEXTURES,
                       pictureSizeMap).sendToTarget();
            waitDone(mCaptureEglHandler);
        }
    }

    /**
     * Set the jpeg's rotation received from Capture SurfaceTexture.
     * @param isBottomCam is bottom jpeg's rotation.
     * @param rotation received from surface texture's jpeg rotation.
     */
    public void setJpegRotation(int rotation) {
        if (mCaptureEglHandler != null) {
            mCaptureEglHandler.setJpegRotation(rotation);
        }
    }

    public void unInitCapture() {
        if (mCaptureEglHandler != null) {
            doReleaseAndQuitThread(mCaptureEglHandler, mCaptureEglThread);
            mCaptureEglHandler = null;
            mCaptureEglThread = null;
        }
        releaseBitmap();
    }

    private void checkAndCreateCaptureGLThread(int[] formats) {
        if (mCaptureEglHandler == null) {
            mCaptureEglThread = new HandlerThread("Pip-CaptureGLThread");
            mCaptureEglThread.start();
            Looper looper = mCaptureEglThread.getLooper();
            if (looper == null) {
                throw new RuntimeException("why looper is null?");
            }
            mCaptureEglHandler = new CaptureRendererHandler(looper);
            mCaptureEglHandler.obtainMessage(CaptureRendererHandler.MSG_INIT, formats).sendToTarget();
            waitDone(mCaptureEglHandler);
        }
    }

    private void doReleaseAndQuitThread(Handler handler, HandlerThread thread) {
        handler.removeCallbacksAndMessages(null);
        handler.obtainMessage(CaptureRendererHandler.MSG_RELEASE).sendToTarget();
        waitDone(handler);
        Looper looper = thread.getLooper();
        if (looper != null) {
            looper.quit();
        }
    }

    /**
     * Handler used for taking picture.
     *
     */
    private class CaptureRendererHandler extends Handler {
    	public static final int MSG_INIT = 0;
        public static final int MSG_RELEASE = 1;
        public static final int MSG_UPDATE_TEMPLATE = 2;
        public static final int MSG_UPDATE_RENDERER_SIZE = 3;
        public static final int MSG_SETUP_VIDEO_RENDER = 4;
        public static final int MSG_SWITCH_PIP = 6;
        public static final int MSG_SETUP_PIP_TEXTURES = 7;
        public static final int MSG_NEW_BOTTOM_FRAME_ARRIVED = 8;
        public static final int MSG_NEW_TOP_FRAME_ARRIVED = 9;
        public static final int MSG_PREVIEW_SURFACE_DESTORY = 10;
        public static final int MSG_TAKE_VSS = 11;
        public static final int MSG_INIT_SCREEN_RENDERER = 13;
        public static final int MSG_SET_PREVIEW_SURFACE = 14;
        public static final int MSG_SET_RECORDING_SURFACE = 15;
        public static final int MSG_COUNT = 16;
        private static final int MSG_SETUP_PICTURE_TEXTURES = MSG_COUNT;
        private static final int MSG_SETUP_CAPTURE_SURFACE = MSG_COUNT + 1;
        private static final int MSG_CAPTURE_FRAME_AVAILABLE = MSG_COUNT + 2;

        private final HandlerThread mFrameListener = new HandlerThread("PIP-STFrameListener");
        private Handler mSurfaceTextureHandler;

        private PrizeSurfaceTextureWrapper mBottomCapSt = null;
        private PrizeBottomGraphicRenderer mBottomRenderer;
        private PrizeGraphicRenderer mTopRenderer;

        private WindowSurface mCapEglSurface = null;
        private int mTopJpegRotation = 0;
        protected EglCore mEglCore;

        public CaptureRendererHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            Log.i(TAG, "handleMessage:" + msg.what);
            switch (msg.what) {
            case MSG_SETUP_PICTURE_TEXTURES:
                @SuppressWarnings("unchecked")
                Map<String, Size> pictureSizeMap = (HashMap<String, Size>) msg.obj;
                setUpTexturesForCapture(pictureSizeMap.get(TOP));
                break;
            case MSG_SETUP_CAPTURE_SURFACE:
                if (mEglCore != null) {
                    Surface captureSurface = (Surface) msg.obj;
                    mCapEglSurface = new WindowSurface(mEglCore, captureSurface);
                    mCapEglSurface.makeCurrent();
                }
                mTopJpegRotation = 0;
                break;
            case MSG_CAPTURE_FRAME_AVAILABLE:
                PrizeSurfaceTextureWrapper stPicWrapper = (PrizeSurfaceTextureWrapper) msg.obj;
                if (stPicWrapper != null) {
                	stPicWrapper.updateTexImage();
                    tryTakePicutre();
                }
                break;
            case MSG_INIT:
                int[] formats = (int[])msg.obj;
                mEglCore = new EglCore(
                        null,
                        EglCore.FLAG_TRY_GLES3 | EglCore.FLAG_RECORDABLE,
                        formats);
                break;
            case MSG_RELEASE:
                releaseRenderer();
                unInitEglCore();
                break;
            default:
                break;
            }
        }
        
        protected void unInitEglCore() {
            Log.i(TAG, "[release]+");
            if (mEglCore != null) {
                mEglCore.release();
                mEglCore = null;
            }
            Log.i(TAG, "[release]-");
        }
        
        public int getPixelFormat() {
            return mEglCore.getPixelFormat();
        }

        public SurfaceTexture getTopSt() {
            return mBottomCapSt.getSurfaceTexture();
        }

        public void setJpegRotation(int rotation) {
            mTopJpegRotation = rotation;
        }

        private void setUpTexturesForCapture(Size tPictureSize) {
            Log.i(TAG, "[setUpTexturesForCapture]+");
            if (!mFrameListener.isAlive()) {
                mFrameListener.start();
                mSurfaceTextureHandler = new Handler(mFrameListener.getLooper());
            }

            if (mBottomCapSt == null) {
                mBottomCapSt = new PrizeSurfaceTextureWrapper();
                mBottomCapSt.setDefaultBufferSize(tPictureSize.getWidth(), tPictureSize.getHeight());
                mBottomCapSt.setOnFrameAvailableListener(
                        mTopCamFrameAvailableListener, mSurfaceTextureHandler);
            }
            mBottomCapSt.setDefaultBufferSize(tPictureSize.getWidth(), tPictureSize.getHeight());
            if (mBottomRenderer == null) {
                mBottomRenderer = new PrizeBottomGraphicRenderer(mActivity);
            }
            initTopRenderer();

            Log.i(TAG, "[setUpTexturesForCapture]-");
        }
        
        private void tryTakePicutre() {
            if (mBottomCapSt != null && mBottomCapSt.getBufferTimeStamp() > 0) {
                Log.i(TAG, "[tryTakePicutre]+");
                mBottomRenderer.setRendererSize(
                		mBottomCapSt.getWidth(), mBottomCapSt.getHeight(), true);
                mTopRenderer.initTemplateTexture(mWbBitmap);
                mTopRenderer.setRendererSize(mBottomCapSt.getWidth(), mBottomCapSt.getHeight());
                mPreviewTopGraphicRect.setCurrentScaleValue((Math.min(mBottomCapSt.getWidth(), mBottomCapSt.getHeight()) * 1.0f) / Math.min(mPreviewTopGraphicRect.getPreviewWidth(), mPreviewTopGraphicRect.getPreviewHeight()));
                AnimationRect pictureTopGraphicRect = mPreviewTopGraphicRect.copy();
                Log.i(TAG, "[tryTakePicutre] pictureTopGraphicRect=" + pictureTopGraphicRect);
//                pictureTopGraphicRect.changeCooridnateSystem(
//                		mBottomCapSt.getWidth(),
//                		mBottomCapSt.getHeight(), 360 - mCurrentOrientation);
                Log.i(TAG, "[tryTakePicutre] after pictureTopGraphicRect=" + pictureTopGraphicRect);
                boolean bottomIsMainCamera = Util.bottomGraphicIsMainCamera(mActivity);
                /*prize-xuchunming-20170420-add frontcamera mirror switch-start*/
                boolean bottomNeedMirror = false
                        && PIPCustomization.SUB_CAMERA_NEED_HORIZONTAL_FLIP;
                /*prize-xuchunming-20170420-add frontcamera mirror switch-end*/
                boolean topNeedMirror = bottomIsMainCamera
                        && PIPCustomization.SUB_CAMERA_NEED_HORIZONTAL_FLIP;

                // enable blend, in order to get a transparent background
                GLES20.glViewport(0, 0, mBottomCapSt.getWidth(), mBottomCapSt.getHeight());
                GLES20.glEnable(GLES20.GL_BLEND);
                GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
                mBottomRenderer.draw(
                		mBottomCapSt.getTextureId(),
                        GLUtil.createIdentityMtx(), // OES Texture
                        getTexMatrixByRotation(mTopJpegRotation), // texture rotate
                        bottomNeedMirror); //need flip
                mTopRenderer.draw(
                        mBottomCapSt.getTextureId(),
                        GLUtil.createIdentityMtx(), // OES Texture
                        getTexMatrixByRotation(mTopJpegRotation), // texture rotate
                        pictureTopGraphicRect.copy(),
                        mCurrentOrientation > 0 ? -mCurrentOrientation : -1,
                        topNeedMirror); //need flip
                mCapEglSurface.swapBuffers();
                // Be careful, Surface Texture's release should always happen
                // before make nothing current.
                doReleaseCaptureSt();
                mCapEglSurface.makeNothingCurrent();
                mCapEglSurface.releaseEglSurface();
                mCapEglSurface = null;
                Log.i(TAG, "[tryTakePicutre]-");
            }
        }

        private float[] getTexMatrixByRotation(int rotation) {
            float[] texRotateMtxByOrientation = GLUtil.createIdentityMtx();
            android.opengl.Matrix.translateM(texRotateMtxByOrientation, 0,
                    texRotateMtxByOrientation, 0, .5f, .5f, 0);
            android.opengl.Matrix.rotateM(texRotateMtxByOrientation, 0,
                    -rotation, 0, 0, 1);
            android.opengl.Matrix.translateM(texRotateMtxByOrientation, 0, -.5f, -.5f, 0);
            return texRotateMtxByOrientation;
        }

        private SurfaceTexture.OnFrameAvailableListener
            mTopCamFrameAvailableListener = new SurfaceTexture.OnFrameAvailableListener() {
                @Override
                public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                    obtainMessage(MSG_CAPTURE_FRAME_AVAILABLE, mBottomCapSt).sendToTarget();
                }
        };
        
        private void initTopRenderer() {
        	if (mTopRenderer == null) {
            	mTopRenderer = new PrizeGraphicRenderer(mActivity);
            }
        }
        
        private void releaseTopRenderer() {
        	if (mTopRenderer != null) {
            	mTopRenderer.release();
            	mTopRenderer = null;
            }
        }

        private void releaseRenderer() {
            if (mBottomRenderer != null) {
                mBottomRenderer.release();
                mBottomRenderer = null;
            }
            releaseTopRenderer();
            doReleaseCaptureSt();
            if (mSurfaceTextureHandler != null) {
                mFrameListener.quitSafely();
                mSurfaceTextureHandler = null;
            }
        }

        private void doReleaseCaptureSt() {

            if (mBottomCapSt != null) {
                mBottomCapSt.release();
                mBottomCapSt = null;
            }
        }
    }

    private boolean waitDone(Handler handler) {
        final Object waitDoneLock = new Object();
        final Runnable unlockRunnable = new Runnable() {
            @Override
            public void run() {
                synchronized (waitDoneLock) {
                    waitDoneLock.notifyAll();
                }
            }
        };
        synchronized (waitDoneLock) {
            handler.post(unlockRunnable);
            try {
                waitDoneLock.wait();
            } catch (InterruptedException ex) {
                Log.i(TAG, "waitDone interrupted");
                return false;
            }
        }
        return true;
    }
	
}
