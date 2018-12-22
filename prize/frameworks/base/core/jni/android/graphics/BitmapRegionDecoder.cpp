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

#define LOG_TAG "BitmapRegionDecoder"

#include "SkBitmap.h"
#include "SkBitmapRegionDecoder.h"
#include "SkData.h"
#include "SkImageEncoder.h"
#include "GraphicsJNI.h"
#include "SkUtils.h"
#include "SkTemplates.h"
#include "SkPixelRef.h"
#include "SkStream.h"
#include "BitmapFactory.h"
//#include "AutoDecodeCancel.h"
#include "CreateJavaOutputStreamAdaptor.h"
#include "Utils.h"
#include "JNIHelp.h"

#include "core_jni_helpers.h"
#include "android_util_Binder.h"
#include "android_nio_utils.h"
#include "CreateJavaOutputStreamAdaptor.h"

#include <binder/Parcel.h>
#include <jni.h>
#include <androidfw/Asset.h>
#include <sys/stat.h>
#include <cutils/log.h>

using namespace android;

class SkBitmapRegionDecoder_MTK : public SkBitmapRegionDecoder {
public:

    SkBitmapRegionDecoder_MTK(int width, int height, SkImageDecoder *decoder)
        : SkBitmapRegionDecoder(width, height)
        , fDecoder(decoder)
    {}

    ~SkBitmapRegionDecoder_MTK() {
        if (fDecoder) delete fDecoder;
    }

    bool decodeRegion(SkBitmap* bitmap, const SkIRect& rect,
                           SkColorType pref, int sampleSize, void* dc) {
    fDecoder->setSampleSize(sampleSize);
#ifdef MTK_SKIA_MULTI_THREAD_JPEG_REGION
    #ifdef MTK_IMAGE_DC_SUPPORT
    if (fDecoder->getFormat() == SkImageDecoder::kJPEG_Format)
        return fDecoder->decodeSubset(bitmap, rect, pref, sampleSize, dc);
    else
        return fDecoder->decodeSubset(bitmap, rect, pref);
    #else
    if (fDecoder->getFormat() == SkImageDecoder::kJPEG_Format)
        return fDecoder->decodeSubset(bitmap, rect, pref, sampleSize, NULL);
    else
        return fDecoder->decodeSubset(bitmap, rect, pref);
    #endif
#else
    return fDecoder->decodeSubset(bitmap, rect, pref);
#endif
    }

    bool decodeRegion(SkBitmap* bitmap, SkBRDAllocator* allocator,
                              const SkIRect& desiredSubset, int sampleSize,
                              SkColorType colorType, bool requireUnpremul)
    { return false; }

    bool conversionSupported(SkColorType colorType) { return false; }

    SkEncodedFormat getEncodedFormat() { return kJPEG_SkEncodedFormat;}

#ifdef MTK_IMAGE_ENABLE_PQ_FOR_JPEG
    void setPostProcFlag(int flag) { flag = flag;}
#endif

    SkImageDecoder* getDecoder() const { return fDecoder; }

    void regionDecodeLock() {
      /* SkDebugf("SKIA_REGION: wait regionDecodeLock!!\n");0 */
      fRegionDecodeMutex.acquire();
      /* SkDebugf("SKIA_REGION: get regionDecodeLock!!\n"); */
      return;
    }
    void regionDecodeUnlock() {
      /* SkDebugf("SKIA_REGION: release regionDecodeUnlock!!\n"); */
      fRegionDecodeMutex.release();
      return;
    }

private:
    SkImageDecoder* fDecoder;
    SkMutex fRegionDecodeMutex;
};

void MTK_getProcessCmdline(char* acProcName, int iSize)
{
    long pid = getpid();
    char procPath[128];
    snprintf(procPath, 128, "/proc/%ld/cmdline", pid);
    FILE * file = fopen(procPath, "r");
    if (file)
    {
       fgets(acProcName, iSize - 1, file);
       fclose(file);
    }
}

extern "C" int MTK_CheckAppName(const char* acAppName)
{
    char appName[128];
    MTK_getProcessCmdline(appName, sizeof(appName));
    /// MTK_LOGD("appName=%s, acAppName=%s", appName, acAppName);
    if (strstr(appName, acAppName))
    {
        /// MTK_LOGD("1");
        return 1;
    }
    /// MTK_LOGD("0");
    return 0;
}

// Takes ownership of the SkStreamRewindable. For consistency, deletes stream even
// when returning null.
static jobject createBitmapRegionDecoder(JNIEnv* env, SkStreamRewindable* stream) {
    if (MTK_CheckAppName("com.android.gallery3d"))
    {
        SkImageDecoder* decoder = SkImageDecoder::Factory(stream);
        int width, height;
        if (NULL == decoder) {
            delete stream;
            doThrowIOE(env, "Image format not supported");
            return nullObjectReturn("SkImageDecoder::Factory returned null");
        }

        JavaPixelAllocator *javaAllocator = new JavaPixelAllocator(env);
        decoder->setAllocator(javaAllocator);
        javaAllocator->unref();

        // This call passes ownership of stream to the decoder, or deletes on failure.
        if (!decoder->buildTileIndex(stream, &width, &height)) {
            char msg[100];
            snprintf(msg, sizeof(msg), "Image failed to decode using %s decoder",
                    decoder->getFormatName());
            doThrowIOE(env, msg);
            delete decoder;
            return nullObjectReturn("decoder->buildTileIndex returned false");
        }

        SkBitmapRegionDecoder_MTK *bm = new SkBitmapRegionDecoder_MTK(width, height, decoder);
        ALOGD("createBitmapRegionDecoder width(%d), height(%d)", width, height);
        return GraphicsJNI::createBitmapRegionDecoder(env, bm);
    }
    else
    {
        SkAutoTDelete<SkBitmapRegionDecoder> brd(
                SkBitmapRegionDecoder::Create(stream, SkBitmapRegionDecoder::kAndroidCodec_Strategy));
        if (NULL == brd) {
            doThrowIOE(env, "Image format not supported");
            return nullObjectReturn("CreateBitmapRegionDecoder returned null");
        }

        return GraphicsJNI::createBitmapRegionDecoder(env, brd.detach());
    }
}

static jobject nativeNewInstanceFromByteArray(JNIEnv* env, jobject, jbyteArray byteArray,
                                     jint offset, jint length, jboolean isShareable) {
    /*  If isShareable we could decide to just wrap the java array and
        share it, but that means adding a globalref to the java array object
        For now we just always copy the array's data if isShareable.
     */
    AutoJavaByteArray ar(env, byteArray);
    SkMemoryStream* stream = new SkMemoryStream(ar.ptr() + offset, length, true);

    // the decoder owns the stream.
    jobject brd = createBitmapRegionDecoder(env, stream);
    return brd;
}

static jobject nativeNewInstanceFromFileDescriptor(JNIEnv* env, jobject clazz,
                                          jobject fileDescriptor, jboolean isShareable) {
    NPE_CHECK_RETURN_ZERO(env, fileDescriptor);

    jint descriptor = jniGetFDFromFileDescriptor(env, fileDescriptor);

    struct stat fdStat;
    if (fstat(descriptor, &fdStat) == -1) {
        doThrowIOE(env, "broken file descriptor");
        return nullObjectReturn("fstat return -1");
    }

    SkAutoTUnref<SkData> data(SkData::NewFromFD(descriptor));
    SkMemoryStream* stream = new SkMemoryStream(data);

    // the decoder owns the stream.
    jobject brd = createBitmapRegionDecoder(env, stream);
    return brd;
}

static jobject nativeNewInstanceFromStream(JNIEnv* env, jobject clazz,
                                  jobject is,       // InputStream
                                  jbyteArray storage, // byte[]
                                  jboolean isShareable) {
    jobject brd = NULL;
    // for now we don't allow shareable with java inputstreams
    SkStreamRewindable* stream = CopyJavaInputStream(env, is, storage);

    if (stream) {
        // the decoder owns the stream.
        brd = createBitmapRegionDecoder(env, stream);
    }
    return brd;
}

static jobject nativeNewInstanceFromAsset(JNIEnv* env, jobject clazz,
                                 jlong native_asset, // Asset
                                 jboolean isShareable) {
    Asset* asset = reinterpret_cast<Asset*>(native_asset);
    SkMemoryStream* stream = CopyAssetToStream(asset);
    if (NULL == stream) {
        return NULL;
    }

    // the decoder owns the stream.
    jobject brd = createBitmapRegionDecoder(env, stream);
    return brd;
}

/*
 * nine patch not supported
 *
 * purgeable not supported
 * reportSizeToVM not supported
 */

jstring getMimeTypeString(JNIEnv* env, SkImageDecoder::Format format) {
    static const struct {
        SkImageDecoder::Format fFormat;
        const char*            fMimeType;
    } gMimeTypes[] = {
        { SkImageDecoder::kBMP_Format,  "image/bmp" },
        { SkImageDecoder::kGIF_Format,  "image/gif" },
        { SkImageDecoder::kICO_Format,  "image/x-ico" },
        { SkImageDecoder::kJPEG_Format, "image/jpeg" },
        { SkImageDecoder::kPNG_Format,  "image/png" },
        { SkImageDecoder::kWEBP_Format, "image/webp" },
        { SkImageDecoder::kWBMP_Format, "image/vnd.wap.wbmp" }
    };

    const char* cstr = nullptr;
    for (size_t i = 0; i < SK_ARRAY_COUNT(gMimeTypes); i++) {
        if (gMimeTypes[i].fFormat == format) {
            cstr = gMimeTypes[i].fMimeType;
            break;
        }
    }

    jstring jstr = nullptr;
    if (cstr != nullptr) {
        // NOTE: Caller should env->ExceptionCheck() for OOM
        // (can't check for nullptr as it's a valid return value)
        jstr = env->NewStringUTF(cstr);
    }
    return jstr;
}

#define MTK_BRD_MULTI_THREAD
static jobject nativeDecodeRegion(JNIEnv* env, jobject, jlong brdHandle,
                                jint start_x, jint start_y, jint width, jint height, jobject options) {
    if (MTK_CheckAppName("com.android.gallery3d"))
    {
        SkBitmapRegionDecoder_MTK *brd = reinterpret_cast<SkBitmapRegionDecoder_MTK*>(brdHandle);
        jobject tileBitmap = NULL;
        SkImageDecoder *decoder = brd->getDecoder();
        int sampleSize = 1;
        int postproc = 0;
        int postprocflag = 0;
    #ifdef MTK_IMAGE_DC_SUPPORT
        void* dc = NULL;
        bool dcflag = false;
        jint* pdynamicCon = NULL;
        jintArray dynamicCon;
        jsize size = 0;
    #endif

        SkColorType prefColorType = kUnknown_SkColorType;
        bool doDither = true;
        bool preferQualityOverSpeed = false;
        bool requireUnpremultiplied = false;

    #ifdef MTK_BRD_MULTI_THREAD
        jobject ret = NULL;
        brd->regionDecodeLock();
    #endif

        ALOGD("nativeDecodeRegion +");

        if (NULL != options) {
            sampleSize = env->GetIntField(options, gOptions_sampleSizeFieldID);
            // initialize these, in case we fail later on
            env->SetIntField(options, gOptions_widthFieldID, -1);
            env->SetIntField(options, gOptions_heightFieldID, -1);
            env->SetObjectField(options, gOptions_mimeFieldID, 0);

            jobject jconfig = env->GetObjectField(options, gOptions_configFieldID);
            prefColorType = GraphicsJNI::getNativeBitmapColorType(env, jconfig);
            doDither = env->GetBooleanField(options, gOptions_ditherFieldID);
            postproc = env->GetBooleanField(options, gOptions_postprocFieldID);
            postprocflag = env->GetIntField(options, gOptions_postprocflagFieldID);

    #ifdef MTK_IMAGE_DC_SUPPORT
            dcflag = env->GetBooleanField(options, gOptions_dynamicConflagFieldID);
            dynamicCon = (jintArray)env->GetObjectField(options, gOptions_dynamicConFieldID);
            pdynamicCon = env->GetIntArrayElements(dynamicCon, NULL);
            size = env->GetArrayLength(dynamicCon);
    #endif

            preferQualityOverSpeed = env->GetBooleanField(options,
                    gOptions_preferQualityOverSpeedFieldID);
            // Get the bitmap for re-use if it exists.
            tileBitmap = env->GetObjectField(options, gOptions_bitmapFieldID);
            requireUnpremultiplied = !env->GetBooleanField(options, gOptions_premultipliedFieldID);
        }
    #ifdef MTK_BRD_MULTI_THREAD
        if(tileBitmap != NULL && decoder->isAllowMultiThreadRegionDecode()){
          brd->regionDecodeUnlock();
        }
        SkDebugf("nativeDecodeRegion: options %x, tileBitmap %x!!\n", options, tileBitmap);
    #endif
        decoder->setPostProcFlag((postproc | (postprocflag << 4)));

    #ifdef MTK_IMAGE_DC_SUPPORT
        if (dcflag == true) {
            dc = (void*)pdynamicCon;
            int len = (int)size;
            decoder->setDynamicCon(dc, len);
        } else {
            dc = NULL;
            decoder->setDynamicCon(dc, 0);
        }
        ALOGD("nativeDecodeRegion dcflag %d, dc %p", dcflag, dc);
    #endif

        decoder->setDitherImage(doDither);
        decoder->setPreferQualityOverSpeed(preferQualityOverSpeed);
        decoder->setRequireUnpremultipliedColors(requireUnpremultiplied);
        //AutoDecoderCancel adc(options, decoder);

        // To fix the race condition in case "requestCancelDecode"
        // happens earlier than AutoDecoderCancel object is added
        // to the gAutoDecoderCancelMutex linked list.
        if (NULL != options && env->GetBooleanField(options, gOptions_mCancelID)) {
          #ifdef MTK_BRD_MULTI_THREAD
            brd->regionDecodeUnlock();
          #endif
          return nullObjectReturn("gOptions_mCancelID");
        }

        SkIRect region;
        region.fLeft = start_x;
        region.fTop = start_y;
        region.fRight = start_x + width;
        region.fBottom = start_y + height;
        SkBitmap bitmap;

        if (tileBitmap != NULL) {
            // Re-use bitmap.
            GraphicsJNI::getSkBitmap(env, tileBitmap, &bitmap);
        }

        #ifdef MTK_IMAGE_DC_SUPPORT
        if (!brd->decodeRegion(&bitmap, region, prefColorType, sampleSize, dc))
        #else
        if (!brd->decodeRegion(&bitmap, region, prefColorType, sampleSize, NULL))
        #endif
        {
          #ifdef MTK_BRD_MULTI_THREAD
            brd->regionDecodeUnlock();
          #endif
          return nullObjectReturn("decoder->decodeRegion returned false");
        }

    #if 0 //mtk skia multi thread jpeg region decode support
        if (!brd->decodeRegion(&bitmap, region, prefColorType, sampleSize)) {
          return nullObjectReturn("decoder->decodeRegion returned false");
        }
    #endif

        // update options (if any)
        if (NULL != options) {
            env->SetIntField(options, gOptions_widthFieldID, bitmap.width());
            env->SetIntField(options, gOptions_heightFieldID, bitmap.height());
            // TODO: set the mimeType field with the data from the codec.
            // but how to reuse a set of strings, rather than allocating new one
            // each time?
            env->SetObjectField(options, gOptions_mimeFieldID,
                                getMimeTypeString(env, decoder->getFormat()));
        }

        if (tileBitmap != NULL) {
          #ifdef MTK_BRD_MULTI_THREAD
            brd->regionDecodeUnlock();
          #endif
          bitmap.notifyPixelsChanged();
          return tileBitmap;
        }

        JavaPixelAllocator* allocator = (JavaPixelAllocator*) decoder->getAllocator();

        int bitmapCreateFlags = 0;
        if (!requireUnpremultiplied) bitmapCreateFlags |= GraphicsJNI::kBitmapCreateFlag_Premultiplied;

    #ifdef MTK_BRD_MULTI_THREAD
        ret =  GraphicsJNI::createBitmap(env, allocator->getStorageObjAndReset(),bitmapCreateFlags);
        brd->regionDecodeUnlock();
        return ret;
    #else
        return GraphicsJNI::createBitmap(env, allocator->getStorageObjAndReset(),bitmapCreateFlags);
    #endif
    }
    else
    {
        // Set default options.
        int sampleSize = 1;
        SkColorType colorType = kN32_SkColorType;
        bool requireUnpremul = false;
        jobject javaBitmap = NULL;

#ifdef MTK_IMAGE_ENABLE_PQ_FOR_JPEG
        int postproc = 0;
        int postprocflag = 0;
#endif

        // Update the default options with any options supplied by the client.
        if (NULL != options) {
            sampleSize = env->GetIntField(options, gOptions_sampleSizeFieldID);
            jobject jconfig = env->GetObjectField(options, gOptions_configFieldID);
            colorType = GraphicsJNI::getNativeBitmapColorType(env, jconfig);
            requireUnpremul = !env->GetBooleanField(options, gOptions_premultipliedFieldID);
            javaBitmap = env->GetObjectField(options, gOptions_bitmapFieldID);
    #ifdef MTK_IMAGE_ENABLE_PQ_FOR_JPEG
            postproc = env->GetBooleanField(options, gOptions_postprocFieldID);
            postprocflag = env->GetIntField(options, gOptions_postprocflagFieldID);
    #endif
            // The Java options of ditherMode and preferQualityOverSpeed are deprecated.  We will
            // ignore the values of these fields.

            // Initialize these fields to indicate a failure.  If the decode succeeds, we
            // will update them later on.
            env->SetIntField(options, gOptions_widthFieldID, -1);
            env->SetIntField(options, gOptions_heightFieldID, -1);
            env->SetObjectField(options, gOptions_mimeFieldID, 0);
        }

        // Recycle a bitmap if possible.
        android::Bitmap* recycledBitmap = nullptr;
        size_t recycledBytes = 0;
        if (javaBitmap) {
            recycledBitmap = GraphicsJNI::getBitmap(env, javaBitmap);
            if (recycledBitmap->peekAtPixelRef()->isImmutable()) {
                ALOGW("Warning: Reusing an immutable bitmap as an image decoder target.");
            }
            recycledBytes = GraphicsJNI::getBitmapAllocationByteCount(env, javaBitmap);
        }

        // Set up the pixel allocator
        SkBRDAllocator* allocator = nullptr;
        RecyclingClippingPixelAllocator recycleAlloc(recycledBitmap, recycledBytes);
        JavaPixelAllocator javaAlloc(env);
        if (javaBitmap) {
            allocator = &recycleAlloc;
            // We are required to match the color type of the recycled bitmap.
            colorType = recycledBitmap->info().colorType();
        } else {
            allocator = &javaAlloc;
        }

        // Decode the region.
        SkIRect subset = SkIRect::MakeXYWH(start_x, start_y, width, height);
        SkBitmapRegionDecoder* brd =
                reinterpret_cast<SkBitmapRegionDecoder*>(brdHandle);

#ifdef MTK_IMAGE_ENABLE_PQ_FOR_JPEG
        if (brd->getEncodedFormat() == SkEncodedFormat::kJPEG_SkEncodedFormat) {
            brd->setPostProcFlag(postproc | (postprocflag << 4));
        }
#endif

        SkBitmap bitmap;
        if (!brd->decodeRegion(&bitmap, allocator, subset, sampleSize, colorType, requireUnpremul)) {
            return nullObjectReturn("Failed to decode region.");
        }

        // If the client provided options, indicate that the decode was successful.
        if (NULL != options) {
            env->SetIntField(options, gOptions_widthFieldID, bitmap.width());
            env->SetIntField(options, gOptions_heightFieldID, bitmap.height());
            env->SetObjectField(options, gOptions_mimeFieldID,
                    encodedFormatToString(env, brd->getEncodedFormat()));
            if (env->ExceptionCheck()) {
                return nullObjectReturn("OOM in encodedFormatToString()");
            }
        }

        // If we may have reused a bitmap, we need to indicate that the pixels have changed.
        if (javaBitmap) {
            recycleAlloc.copyIfNecessary();
            return javaBitmap;
        }

        int bitmapCreateFlags = 0;
        if (!requireUnpremul) {
            bitmapCreateFlags |= GraphicsJNI::kBitmapCreateFlag_Premultiplied;
        }
        return GraphicsJNI::createBitmap(env, javaAlloc.getStorageObjAndReset(), bitmapCreateFlags);
    }
}

static jint nativeGetHeight(JNIEnv* env, jobject, jlong brdHandle) {
    SkBitmapRegionDecoder *brd = reinterpret_cast<SkBitmapRegionDecoder*>(brdHandle);
    return static_cast<jint>(brd->height());
}

static jint nativeGetWidth(JNIEnv* env, jobject, jlong brdHandle) {
    SkBitmapRegionDecoder *brd = reinterpret_cast<SkBitmapRegionDecoder*>(brdHandle);
    return static_cast<jint>(brd->width());
}

static void nativeClean(JNIEnv* env, jobject, jlong brdHandle) {
    SkBitmapRegionDecoder *brd = reinterpret_cast<SkBitmapRegionDecoder*>(brdHandle);
    delete brd;
}

///////////////////////////////////////////////////////////////////////////////

static JNINativeMethod gBitmapRegionDecoderMethods[] = {
    {   "nativeDecodeRegion",
        "(JIIIILandroid/graphics/BitmapFactory$Options;)Landroid/graphics/Bitmap;",
        (void*)nativeDecodeRegion},

    {   "nativeGetHeight", "(J)I", (void*)nativeGetHeight},

    {   "nativeGetWidth", "(J)I", (void*)nativeGetWidth},

    {   "nativeClean", "(J)V", (void*)nativeClean},

    {   "nativeNewInstance",
        "([BIIZ)Landroid/graphics/BitmapRegionDecoder;",
        (void*)nativeNewInstanceFromByteArray
    },

    {   "nativeNewInstance",
        "(Ljava/io/InputStream;[BZ)Landroid/graphics/BitmapRegionDecoder;",
        (void*)nativeNewInstanceFromStream
    },

    {   "nativeNewInstance",
        "(Ljava/io/FileDescriptor;Z)Landroid/graphics/BitmapRegionDecoder;",
        (void*)nativeNewInstanceFromFileDescriptor
    },

    {   "nativeNewInstance",
        "(JZ)Landroid/graphics/BitmapRegionDecoder;",
        (void*)nativeNewInstanceFromAsset
    },
};

int register_android_graphics_BitmapRegionDecoder(JNIEnv* env)
{
    return android::RegisterMethodsOrDie(env, "android/graphics/BitmapRegionDecoder",
            gBitmapRegionDecoderMethods, NELEM(gBitmapRegionDecoderMethods));
}
