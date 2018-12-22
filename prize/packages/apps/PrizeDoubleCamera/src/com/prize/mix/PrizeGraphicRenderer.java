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

import java.io.IOException;
import java.nio.FloatBuffer;

import android.app.Activity;
import android.graphics.Bitmap;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.Matrix;
import com.android.camera.Log;

import com.mediatek.camera.mode.pip.pipwrapping.AnimationRect;
import com.mediatek.camera.mode.pip.pipwrapping.GLUtil;
import com.mediatek.camera.mode.pip.pipwrapping.Renderer;

public class PrizeGraphicRenderer extends Renderer {
    private static final String TAG = "PrizeGraphicRenderer";

    // position
    private FloatBuffer mVtxBuf;
    private FloatBuffer mTexCoordBuf;
    // matrix
    private float[] mMVPMtx = GLUtil.createIdentityMtx();
    private float[] mMMtx = GLUtil.createIdentityMtx();
    private float[] mVMtx = GLUtil.createIdentityMtx(); // view
    private float[] mPMtx = GLUtil.createIdentityMtx(); // projection
    private int mBackTempTexId = -12345;

    private int mProgram = -1;
    private int maPositionHandle = -1;
    private int maTexCoordHandle = -1;
    private int muPosMtxHandle = -1;
    private int muTexRotateMtxHandle = -1;
    private int muSamplerHandle = -1;
	private int muTexMtxHandle = -1;

    final String vertexShader =
            "attribute vec4 aPosition;\n"
          + "attribute vec4 aTexCoord;\n"
          + "uniform   mat4 uPosMtx;\n"
          + "uniform   mat4   uTexMtx;\n"
          + "uniform   mat4 uTexRotateMtx;\n"
          + "varying   vec2 vTexCoord;\n"
          + "void main() {\n"
          + "  gl_Position = uPosMtx * aPosition;\n"
          + "  vTexCoord     = (uTexRotateMtx * uTexMtx * aTexCoord).xy;\n"
          + "}\n";
    final String fragmentShader =
            "precision mediump float;\n"
          + "uniform sampler2D uSampler;\n"
          + "varying vec2      vTexCoord;\n"
          + "void main() {\n"
          + "  gl_FragColor = texture2D(uSampler, vTexCoord);\n"
          + "}\n";

    public PrizeGraphicRenderer(Activity activity) {
        super(activity);
        Log.i(TAG, "TopGraphicRenderer");
        initProgram();
        mTexCoordBuf = createFloatBuffer(mTexCoordBuf, GLUtil.createTexCoord());
    }
    
    public void initTemplateTexture(Bitmap wbBitmap) {
        Log.i(TAG, "initTemplateTexture");
        if (mBackTempTexId > 0) {
            releaseBitmapTexture(mBackTempTexId);
            mBackTempTexId = -12345;
        }
        try {
            if (wbBitmap != null) {
                mBackTempTexId = initBitmapTexture(wbBitmap, false);
            }
        } catch (IOException e) {
            Log.e(TAG, "initBitmapTexture faile + " + e);
        }
    }

    @Override
    public void setRendererSize(int width, int height) {
        Log.i(TAG, "setRendererSize width = " + width + " height = " + height);
        resetMatrix();
        Matrix.orthoM(mPMtx, 0, 0, width, 0, height, -1, 1);
        // hand stands MVP matrix to match phone's coordinate system
        Matrix.translateM(mMMtx, 0, 0, height, 0);
        Matrix.scaleM(mMMtx, 0, mMMtx, 0, 1, -1, 1);

        Matrix.multiplyMM(mMVPMtx, 0, mMMtx, 0, mMVPMtx, 0);
        Matrix.multiplyMM(mMVPMtx, 0, mVMtx, 0, mMVPMtx, 0);
        Matrix.multiplyMM(mMVPMtx, 0, mPMtx, 0, mMVPMtx, 0);
        super.setRendererSize(width, height);
    }
    
    public void draw(int preTex, final float[] preTexMtx, final float[] texReverseRotateMtx,
            final AnimationRect topRect, int rotation, boolean needFlip) {
    	Log.i(TAG, "<draw> preTex=" + preTex + " topRect=" + topRect);
        if (preTex <= 0 || topRect == null) {
            return;
        }
        // copy AnimationRect
        AnimationRect animationRect = new AnimationRect();
        animationRect.setRendererSize(topRect.getPreviewWidth(), topRect.getPreviewHeight());
        animationRect.setCurrentScaleValue(topRect.getCurrentScaleValue());
        animationRect.setOriginalDistance(topRect.getOriginalDistance());
        animationRect.initialize(topRect.getRectF().left, topRect.getRectF().top,
                topRect.getRectF().right, topRect.getRectF().bottom);
        animationRect.setCurrrentRotationValue(topRect.getCurrrentRotationValue());
        // keep original centerX and centerY
        float centerX = animationRect.getRectF().centerX();
        float centerY = animationRect.getRectF().centerY();
        Log.i(TAG, "<draw> animationRect=" + animationRect + " centerX=" + centerX + " centerY=" + centerY);
        animationRect.scaleTranslate(animationRect.getCurrentScaleValue(), (animationRect.getCurrentScaleValue() - 1) * animationRect.getRectF().left,
        		(animationRect.getCurrentScaleValue() - 1) * animationRect.getRectF().top);
        GLUtil.checkGlError("TopGraphicRenderer draw start");
        Log.i(TAG, "<draw> after animationRect=" + animationRect);
        GLES20.glUseProgram(mProgram);
        // position
        mVtxBuf = createFloatBuffer(mVtxBuf, GLUtil.createTopRightRect(animationRect));
        mVtxBuf.position(0);
        GLES20.glVertexAttribPointer(maPositionHandle, 3, GLES20.GL_FLOAT, false, 4 * 3, mVtxBuf);
        mTexCoordBuf.position(0);
        GLES20.glVertexAttribPointer(maTexCoordHandle, 2, GLES20.GL_FLOAT, false, 4 * 2,
                mTexCoordBuf);
        GLES20.glEnableVertexAttribArray(maPositionHandle);
        GLES20.glEnableVertexAttribArray(maTexCoordHandle);
        // draw
        // matrix
        GLES20.glUniformMatrix4fv(muPosMtxHandle, 1, false, mMVPMtx, 0);
        GLES20.glUniformMatrix4fv(muTexRotateMtxHandle, 1, false, texReverseRotateMtx, 0);
        GLES20.glUniformMatrix4fv(muTexMtxHandle, 1, false,
                (preTexMtx == null) ? GLUtil.createIdentityMtx() : preTexMtx, 0);
        // sampler
        GLES20.glUniform1i(muSamplerHandle, 0);
        // texture
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture((preTexMtx == null) ? GLES20.GL_TEXTURE_2D
                : GLES11Ext.GL_TEXTURE_EXTERNAL_OES, preTex);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mBackTempTexId);
        // draw
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 6);
        GLUtil.checkGlError("TopGraphicRenderer draw end");
    }

    public FloatBuffer getVtxFloatBuffer() {
        return mVtxBuf;
    }

    @Override
    public void release() {
        if (mBackTempTexId > 0) {
            releaseBitmapTexture(mBackTempTexId);
            mBackTempTexId = -12345;
        }
    }

    private void initProgram() {
    	mProgram = GLUtil.createProgram(vertexShader, fragmentShader);
        maPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
        maTexCoordHandle = GLES20.glGetAttribLocation(mProgram, "aTexCoord");
        // matrix
        muTexRotateMtxHandle = GLES20.glGetUniformLocation(mProgram, "uTexRotateMtx");
        muPosMtxHandle = GLES20.glGetUniformLocation(mProgram, "uPosMtx");
        muTexMtxHandle = GLES20.glGetUniformLocation(mProgram, "uTexMtx");
        // sampler
        muSamplerHandle = GLES20.glGetUniformLocation(mProgram, "uSampler");
    }

    private void resetMatrix() {
        mMVPMtx = GLUtil.createIdentityMtx();
        mPMtx = GLUtil.createIdentityMtx();
        mVMtx = GLUtil.createIdentityMtx();
        mMMtx = GLUtil.createIdentityMtx();
    }
}
