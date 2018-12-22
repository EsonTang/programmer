/*
 * Copyright (C) 2007 The Android Open Source Project
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

#ifndef ANDROID_SURFACE_FLINGER_H
#define ANDROID_SURFACE_FLINGER_H

#include <stdint.h>
#include <sys/types.h>

#include <EGL/egl.h>
#include <GLES/gl.h>

#include <cutils/compiler.h>

#include <utils/Atomic.h>
#include <utils/Errors.h>
#include <utils/KeyedVector.h>
#include <utils/RefBase.h>
#include <utils/SortedVector.h>
#include <utils/threads.h>

#include <binder/BinderService.h>
#include <binder/IMemory.h>

#include <ui/PixelFormat.h>

#include <gui/ISurfaceComposer.h>
#include <gui/ISurfaceComposerClient.h>

#include <hardware/hwcomposer_defs.h>

#include <private/gui/LayerState.h>

#include "Barrier.h"
#include "MessageQueue.h"
#include "DisplayDevice.h"

#include "DisplayHardware/HWComposer.h"

namespace android {

// ---------------------------------------------------------------------------

class Client;
class DisplayEventConnection;
class EventThread;
class IGraphicBufferAlloc;
class Layer;
class LayerBase;
class LayerBaseClient;
class LayerDim;
class LayerScreenshot;
class SurfaceTextureClient;

// ---------------------------------------------------------------------------

enum {
    eTransactionNeeded        = 0x01,
    eTraversalNeeded          = 0x02,
    eDisplayTransactionNeeded = 0x04,
    eTransactionMask          = 0x07
};

class SurfaceFlinger : public BinderService<SurfaceFlinger>,
                       public BnSurfaceComposer,
                       private IBinder::DeathRecipient,
                       private Thread,
                       private HWComposer::EventHandler
{
public:
    static char const* getServiceName() {
        return "SurfaceFlinger";
    }

    SurfaceFlinger();

    enum {
        EVENT_VSYNC = HWC_EVENT_VSYNC
    };

    // post an asynchronous message to the main thread
    status_t postMessageAsync(const sp<MessageBase>& msg, nsecs_t reltime = 0,
        uint32_t flags = 0);

    // post a synchronous message to the main thread
    status_t postMessageSync(const sp<MessageBase>& msg, nsecs_t reltime = 0,
        uint32_t flags = 0);

    // force full composition on all displays
    void repaintEverything();

    // renders content on given display to a texture. thread-safe version.
    status_t renderScreenToTexture(uint32_t layerStack, GLuint* textureName,
        GLfloat* uOut, GLfloat* vOut);

    // renders content on given display to a texture, w/o acquiring main lock
    status_t renderScreenToTextureLocked(uint32_t layerStack, GLuint* textureName,
        GLfloat* uOut, GLfloat* vOut);

    // returns the default Display
    sp<const DisplayDevice> getDefaultDisplayDevice() const {
        return getDisplayDevice(mDefaultDisplays[DisplayDevice::DISPLAY_PRIMARY]);
    }

    // utility function to delete a texture on the main thread
    void deleteTextureAsync(GLuint texture);

    // allocate a h/w composer display id
    int32_t allocateHwcDisplayId(DisplayDevice::DisplayType type);

    // enable/disable h/w composer event
    // TODO: this should be made accessible only to EventThread
    void eventControl(int disp, int event, int enabled);

    // called on the main thread by MessageQueue when an internal message
    // is received
    // TODO: this should be made accessible only to MessageQueue
    void onMessageReceived(int32_t what);

    // for debugging only
    // TODO: this should be made accessible only to HWComposer
    const Vector< sp<LayerBase> >& getLayerSortedByZForHwcDisplay(int disp);

private:
    friend class Client;
    friend class DisplayEventConnection;
    friend class LayerBase;
    friend class LayerBaseClient;
    friend class Layer;
    friend class LayerScreenshot;

    // We're reference counted, never destroy SurfaceFlinger directly
    virtual ~SurfaceFlinger();

    /* ------------------------------------------------------------------------
     * Internal data structures
     */

    class LayerVector : public SortedVector<sp<LayerBase> > {
    public:
        LayerVector();
        LayerVector(const LayerVector& rhs);
        virtual int do_compare(const void* lhs, const void* rhs) const;
    };

    struct DisplayDeviceState {
        DisplayDeviceState();
        DisplayDeviceState(DisplayDevice::DisplayType type);
        bool isValid() const { return type >= 0; }
        bool isMainDisplay() const { return type == DisplayDevice::DISPLAY_PRIMARY; }
        bool isVirtualDisplay() const { return type >= DisplayDevice::DISPLAY_VIRTUAL; }
        DisplayDevice::DisplayType type;
        sp<ISurfaceTexture> surface;
        uint32_t layerStack;
        Rect viewport;
        Rect frame;
        uint8_t orientation;
        String8 displayName;
        bool isSecure;
    };

    struct State {
        LayerVector layersSortedByZ;
        DefaultKeyedVector< wp<IBinder>, DisplayDeviceState> displays;
    };

    /* ------------------------------------------------------------------------
     * IBinder interface
     */
    virtual status_t onTransact(uint32_t code, const Parcel& data,
        Parcel* reply, uint32_t flags);
    virtual status_t dump(int fd, const Vector<String16>& args);

    /* ------------------------------------------------------------------------
     * ISurfaceComposer interface
     */
    virtual sp<ISurfaceComposerClient> createConnection();
    virtual sp<IGraphicBufferAlloc> createGraphicBufferAlloc();
    virtual sp<IBinder> createDisplay(const String8& displayName, bool secure);
    virtual sp<IBinder> getBuiltInDisplay(int32_t id);
    virtual void setTransactionState(const Vector<ComposerState>& state,
            const Vector<DisplayState>& displays, uint32_t flags);
    virtual void bootFinished();
    virtual bool authenticateSurfaceTexture(
        const sp<ISurfaceTexture>& surface) const;
    virtual sp<IDisplayEventConnection> createDisplayEventConnection();
    virtual status_t captureScreen(const sp<IBinder>& display, sp<IMemoryHeap>* heap,
        uint32_t* width, uint32_t* height, PixelFormat* format,
        uint32_t reqWidth, uint32_t reqHeight, uint32_t minLayerZ,
        uint32_t maxLayerZ);
    // called when screen needs to turn off
    virtual void blank(const sp<IBinder>& display);
    // called when screen is turning back on
    virtual void unblank(const sp<IBinder>& display);
    virtual status_t getDisplayInfo(const sp<IBinder>& display, DisplayInfo* info);

    /* ------------------------------------------------------------------------
     * DeathRecipient interface
     */
    virtual void binderDied(const wp<IBinder>& who);

    /* ------------------------------------------------------------------------
     * Thread interface
     */
    virtual bool threadLoop();
    virtual status_t readyToRun();
    virtual void onFirstRef();

    /* ------------------------------------------------------------------------
     * HWComposer::EventHandler interface
     */
    virtual void onVSyncReceived(int type, nsecs_t timestamp);
    virtual void onHotplugReceived(int disp, bool connected);

    /* ------------------------------------------------------------------------
     * Message handling
     */
    void waitForEvent();
    void signalTransaction();
    void signalLayerUpdate();
    void signalRefresh();

    // called on the main thread in response to initializeDisplays()
    void onInitializeDisplays();
    // called on the main thread in response to blank()
    void onScreenReleased(const sp<const DisplayDevice>& hw);
    // called on the main thread in response to unblank()
    void onScreenAcquired(const sp<const DisplayDevice>& hw);

    void handleMessageTransaction();
    void handleMessageInvalidate();
    void handleMessageRefresh();

    void handleTransaction(uint32_t transactionFlags);
    void handleTransactionLocked(uint32_t transactionFlags);

    /* handlePageFilp: this is were we latch a new buffer
     * if available and compute the dirty region.
     */
    void handlePageFlip();

    /* ------------------------------------------------------------------------
     * Transactions
     */
    uint32_t getTransactionFlags(uint32_t flags);
    uint32_t peekTransactionFlags(uint32_t flags);
    uint32_t setTransactionFlags(uint32_t flags);
    void commitTransaction();
    uint32_t setClientStateLocked(const sp<Client>& client,
        const layer_state_t& s);
    uint32_t setDisplayStateLocked(const DisplayState& s);

    /* ------------------------------------------------------------------------
     * Layer management
     */
    sp<ISurface> createLayer(ISurfaceComposerClient::surface_data_t* params,
            const String8& name, const sp<Client>& client,
            uint32_t w, uint32_t h, PixelFormat format, uint32_t flags);

    sp<Layer> createNormalLayer(const sp<Client>& client,
            uint32_t w, uint32_t h, uint32_t flags, PixelFormat& format);

    sp<LayerDim> createDimLayer(const sp<Client>& client,
            uint32_t w, uint32_t h, uint32_t flags);

    sp<LayerScreenshot> createScreenshotLayer(const sp<Client>& client,
            uint32_t w, uint32_t h, uint32_t flags);

    // called in response to the window-manager calling
    // ISurfaceComposerClient::destroySurface()
    // The specified layer is first placed in a purgatory list
    // until all references from the client are released.
    status_t onLayerRemoved(const sp<Client>& client, SurfaceID sid);

    // called when all clients have released all their references to
    // this layer meaning it is entirely safe to destroy all
    // resources associated to this layer.
    status_t onLayerDestroyed(const wp<LayerBaseClient>& layer);

    // remove a layer from SurfaceFlinger immediately
    status_t removeLayer(const sp<LayerBase>& layer);

    // add a layer to SurfaceFlinger
    ssize_t addClientLayer(const sp<Client>& client,
        const sp<LayerBaseClient>& lbc);

    status_t removeLayer_l(const sp<LayerBase>& layer);
    status_t purgatorizeLayer_l(const sp<LayerBase>& layer);

    /* ------------------------------------------------------------------------
     * Boot animation, on/off animations and screen capture
     */

    void startBootAnim();

    status_t captureScreenImplLocked(const sp<IBinder>& display, sp<IMemoryHeap>* heap,
        uint32_t* width, uint32_t* height, PixelFormat* format,
        uint32_t reqWidth, uint32_t reqHeight, uint32_t minLayerZ,
        uint32_t maxLayerZ);

    /* ------------------------------------------------------------------------
     * EGL
     */
    static status_t selectConfigForAttribute(EGLDisplay dpy,
        EGLint const* attrs, EGLint attribute, EGLint value, EGLConfig* outConfig);
    static EGLConfig selectEGLConfig(EGLDisplay disp, EGLint visualId);
    static EGLContext createGLContext(EGLDisplay disp, EGLConfig config);
    void initializeGL(EGLDisplay display);
    uint32_t getMaxTextureSize() const;
    uint32_t getMaxViewportDims() const;

    /* ------------------------------------------------------------------------
     * Display and layer stack management
     */
    // called when starting, or restarting after system_server death
    void initializeDisplays();

    // NOTE: can only be called from the main thread or with mStateLock held
    sp<const DisplayDevice> getDisplayDevice(const wp<IBinder>& dpy) const {
        return mDisplays.valueFor(dpy);
    }

    // NOTE: can only be called from the main thread or with mStateLock held
    sp<DisplayDevice> getDisplayDevice(const wp<IBinder>& dpy) {
        return mDisplays.valueFor(dpy);
    }

    // mark a region of a layer stack dirty. this updates the dirty
    // region of all screens presenting this layer stack.
    void invalidateLayerStack(uint32_t layerStack, const Region& dirty);

    /* ------------------------------------------------------------------------
     * H/W composer
     */

    HWComposer& getHwComposer() const { return *mHwc; }

    /* ------------------------------------------------------------------------
     * Compositing
     */
    void invalidateHwcGeometry();
    static void computeVisibleRegions(
            const LayerVector& currentLayers, uint32_t layerStack,
            Region& dirtyRegion, Region& opaqueRegion);

    void preComposition();
    void postComposition();
    void rebuildLayerStacks();
    void setUpHWComposer();
    void doComposition();
    void doDebugFlashRegions();
    void doDisplayComposition(const sp<const DisplayDevice>& hw,
            const Region& dirtyRegion);
    void doComposeSurfaces(const sp<const DisplayDevice>& hw,
            const Region& dirty);

    void postFramebuffer();
    void drawWormhole(const sp<const DisplayDevice>& hw,
            const Region& region) const;
    GLuint getProtectedTexName() const {
        return mProtectedTexName;
    }

    /* ------------------------------------------------------------------------
     * Display management
     */


    /* ------------------------------------------------------------------------
     * Debugging & dumpsys
     */
    void listLayersLocked(const Vector<String16>& args, size_t& index,
        String8& result, char* buffer, size_t SIZE) const;
    void dumpStatsLocked(const Vector<String16>& args, size_t& index,
        String8& result, char* buffer, size_t SIZE) const;
    void clearStatsLocked(const Vector<String16>& args, size_t& index,
        String8& result, char* buffer, size_t SIZE) const;
    void dumpAllLocked(String8& result, char* buffer, size_t SIZE) const;
    bool startDdmConnection();
    static void appendSfConfigString(String8& result);

    /* ------------------------------------------------------------------------
     * Attributes
     */

    // access must be protected by mStateLock
    mutable Mutex mStateLock;
    State mCurrentState;
    volatile int32_t mTransactionFlags;
    Condition mTransactionCV;
    SortedVector<sp<LayerBase> > mLayerPurgatory;
    bool mTransactionPending;
    bool mAnimTransactionPending;
    Vector<sp<LayerBase> > mLayersPendingRemoval;

    // protected by mStateLock (but we could use another lock)
    bool mLayersRemoved;

    // access must be protected by mInvalidateLock
    volatile int32_t mRepaintEverything;

    // constant members (no synchronization needed for access)
    HWComposer* mHwc;
    GLuint mProtectedTexName;
    nsecs_t mBootTime;
    sp<EventThread> mEventThread;
    GLint mMaxViewportDims[2];
    GLint mMaxTextureSize;
    EGLContext mEGLContext;
    EGLConfig mEGLConfig;
    EGLDisplay mEGLDisplay;
    sp<IBinder> mDefaultDisplays[DisplayDevice::NUM_DISPLAY_TYPES];

    // Can only accessed from the main thread, these members
    // don't need synchronization
    State mDrawingState;
    bool mVisibleRegionsDirty;
    bool mHwWorkListDirty;

    // this may only be written from the main thread with mStateLock held
    // it may be read from other threads with mStateLock held
    DefaultKeyedVector< wp<IBinder>, sp<DisplayDevice> > mDisplays;

    // don't use a lock for these, we don't care
    int mDebugRegion;
    int mDebugDDMS;
    int mDebugDisableHWC;
    int mDebugDisableTransformHint;
    volatile nsecs_t mDebugInSwapBuffers;
    nsecs_t mLastSwapBufferTime;
    volatile nsecs_t mDebugInTransaction;
    nsecs_t mLastTransactionTime;
    bool mBootFinished;

    // these are thread safe
    mutable MessageQueue mEventQueue;
    mutable Barrier mReadyToRunBarrier;

    // protected by mDestroyedLayerLock;
    mutable Mutex mDestroyedLayerLock;
    Vector<LayerBase const *> mDestroyedLayers;

    /* ------------------------------------------------------------------------
     * Feature prototyping
     */

    sp<IBinder> mExtDisplayToken;
};

// ---------------------------------------------------------------------------
}; // namespace android

#endif // ANDROID_SURFACE_FLINGER_H
