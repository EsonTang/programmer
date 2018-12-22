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
 * MediaTek Inc. (C) 2015. All rights reserved.
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
package com.mediatek.galleryfeature.stereo.freeview3d;

import android.content.Context;

import com.mediatek.galleryframework.base.ComboLayer;
import com.mediatek.galleryframework.base.ExtItem;
import com.mediatek.galleryframework.base.Layer;
import com.mediatek.galleryframework.base.MediaData;
import com.mediatek.galleryframework.base.MediaMember;
import com.mediatek.galleryframework.base.Player;
import com.mediatek.galleryframework.base.ThumbType;
import com.mediatek.galleryframework.gl.GLIdleExecuter;

import java.util.ArrayList;

/**
 * Define for depth image.
 */
public class DepthImageMember extends MediaMember {
    private Layer mLayer;
    private GLIdleExecuter mGLExecuter;

    /**
     * Constructor.
     * @param context the context is used for create layer.
     * @param exe the exe is used for create layer.
     */
    public DepthImageMember(Context context, GLIdleExecuter exe) {
        super(context);
        mGLExecuter = exe;
    }

    @Override
    public boolean isMatching(MediaData md) {
        return (md.depth_image == 1);
    }

    @Override
    public ExtItem getItem(MediaData md) {
        return new DepthImageItem(md);
    }

    @Override
    public MediaData.MediaType getMediaType() {
        return MediaData.MediaType.DEPTH_IMAGE;
    }

    @Override
    public Player getPlayer(MediaData md, ThumbType type) {
        if (type == ThumbType.MIDDLE) {
            return new FreeView3DPlayer(mContext, md,
                    Player.OutputType.TEXTURE, type, mGLExecuter);
        }
        return null;
    }

    @Override
    public Layer getLayer() {
        if (mLayer == null) {
            mLayer = new FreeView3DLayer();
        }
        return mLayer;
    }
}