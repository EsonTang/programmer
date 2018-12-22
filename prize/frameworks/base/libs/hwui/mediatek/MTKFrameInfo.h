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
 * MediaTek Inc. (C) 2010. All rights reserved.
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
#ifndef MTK_FRAMEINFO_H_
#define MTK_FRAMEINFO_H_

#include "utils/Macros.h"
#include "utils/Log.h"
#include "MTKFrameBudgetIndicator.h"

#include <cutils/compiler.h>
#include <utils/Timers.h>

#include <memory.h>
#include <string>

namespace android {
namespace uirenderer {

#define UI_THREAD_FRAME_INFO_SIZE 9

enum class FrameInfoIndex {
    Flags = 0,
    IntendedVsync,
    Vsync,
    OldestInputEvent,
    NewestInputEvent,
    HandleInputStart,
    AnimationStart,
    PerformTraversalsStart,
    DrawStart,
    // End of UI frame info

    SyncQueued,

    SyncStart,
    IssueDrawCommandsStart,
    SwapBuffers,
    FrameCompleted,

    DequeueBufferDuration,
    QueueBufferDuration,

    // Must be the last value!
    NumIndexes
};

extern const std::string FrameInfoNames[];

namespace FrameInfoFlags {
    enum {
        WindowLayoutChanged = 1 << 0,
        RTAnimation = 1 << 1,
        SurfaceCanvas = 1 << 2,
        SkippedFrame = 1 << 3,
    };
};

class ANDROID_API UiFrameInfoBuilder {
public:
    UiFrameInfoBuilder(int64_t* buffer) : mBuffer(buffer) {
        memset(mBuffer, 0, UI_THREAD_FRAME_INFO_SIZE * sizeof(int64_t));
    }

    UiFrameInfoBuilder& setVsync(nsecs_t vsyncTime, nsecs_t intendedVsync) {
        set(FrameInfoIndex::Vsync) = vsyncTime;
        set(FrameInfoIndex::IntendedVsync) = intendedVsync;
        // Pretend the other fields are all at vsync, too, so that naive
        // duration calculations end up being 0 instead of very large
        set(FrameInfoIndex::HandleInputStart) = vsyncTime;
        set(FrameInfoIndex::AnimationStart) = vsyncTime;
        set(FrameInfoIndex::PerformTraversalsStart) = vsyncTime;
        set(FrameInfoIndex::DrawStart) = vsyncTime;
        return *this;
    }

    UiFrameInfoBuilder& addFlag(int frameInfoFlag) {
        set(FrameInfoIndex::Flags) |= static_cast<uint64_t>(frameInfoFlag);
        return *this;
    }

private:
    inline int64_t& set(FrameInfoIndex index) {
        return mBuffer[static_cast<int>(index)];
    }

    int64_t* mBuffer;
};

class FrameInfo {
public:
    virtual ~FrameInfo(){};
    void importUiThreadInfo(int64_t* info);

    void markSyncStart() {
        set(FrameInfoIndex::SyncStart) = systemTime(CLOCK_MONOTONIC);
    }

    void markIssueDrawCommandsStart() {
        set(FrameInfoIndex::IssueDrawCommandsStart) = systemTime(CLOCK_MONOTONIC);
    }

    void markSwapBuffers() {
        set(FrameInfoIndex::SwapBuffers) = systemTime(CLOCK_MONOTONIC);
        notifySwapBuffers();
    }

    void markFrameCompleted() {
        set(FrameInfoIndex::FrameCompleted) = systemTime(CLOCK_MONOTONIC);
        notifyFrameComplete(totalDuration() - get(FrameInfoIndex::QueueBufferDuration) - get(FrameInfoIndex::DequeueBufferDuration));
    }

    void addFlag(int frameInfoFlag) {
        set(FrameInfoIndex::Flags) |= static_cast<uint64_t>(frameInfoFlag);
    }

    const int64_t* data() const {
        return mFrameInfo;
    }

    inline int64_t operator[](FrameInfoIndex index) const {
        return get(index);
    }

    inline int64_t operator[](int index) const {
        if (index < 0 || index >= static_cast<int>(FrameInfoIndex::NumIndexes)) return 0;
        return mFrameInfo[index];
    }

    inline int64_t duration(FrameInfoIndex start, FrameInfoIndex end) const {
        int64_t endtime = get(end);
        int64_t starttime = get(start);
        int64_t gap = endtime - starttime;
        gap = starttime > 0 ? gap : 0;
        if (end > FrameInfoIndex::SyncQueued &&
                start < FrameInfoIndex::SyncQueued) {
            // Need to subtract out the time spent in a stalled state
            // as this will be captured by the previous frame's info
            int64_t offset = get(FrameInfoIndex::SyncStart)
                    - get(FrameInfoIndex::SyncQueued);
            if (offset > 0) {
                gap -= offset;
            }
        }
        return gap > 0 ? gap : 0;
    }

    inline int64_t totalDuration() const {
        return duration(FrameInfoIndex::IntendedVsync, FrameInfoIndex::FrameCompleted);
    }

    inline int64_t& set(FrameInfoIndex index) {
        return mFrameInfo[static_cast<int>(index)];
    }

    inline int64_t get(FrameInfoIndex index) const {
        if (index == FrameInfoIndex::NumIndexes) return 0;
        return mFrameInfo[static_cast<int>(index)];
    }

private:
    int64_t mFrameInfo[static_cast<int>(FrameInfoIndex::NumIndexes)];
};

} /* namespace uirenderer */
} /* namespace android */

#endif /* MTK_FRAMEINFO_H_ */
