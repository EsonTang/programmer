package com.mediatek.galleryfeature.platform;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import com.mediatek.gallery3d.ext.IActivityHooker;
import com.mediatek.galleryframework.base.MediaData;
import com.mediatek.galleryframework.base.Work;

/**
 * Making a bridge which connect google packages and mediatek packages.
 * In mediatek feature, we can not access google packages directly for easy
 * migration purpose, so google packages implement this
 * interface(PlatformImpl.java) and register it, then mediatek feature could
 * access google packages by PlatformImpl.
 */
public class PlatformHelper {
    private static final String TAG = "MtkGallery2/PlatformHelper";

    private static Platform sPlatform;

    public static void setPlatform(Platform platform) {
        sPlatform = platform;
    }

    /**
     * Check if the photo is out of decoding spec.
     * @param fileSize size of this photo, in bytes
     * @param width    width of this photo
     * @param height   height of this photo
     * @param mimeType mime type of this photo
     * @return         true if out of spec, otherwise false
     */
    public static boolean isOutOfDecodeSpec(long fileSize, int width, int height, String mimeType) {
        if (sPlatform != null) {
            return sPlatform.isOutOfDecodeSpec(fileSize, width, height, mimeType);
        } else {
            return false;
        }
    }
	
	/**
     * Enter ContainerPage like ContinusShot.
     * @param activity   gallery activity
     * @param data       related data of current media item
     * @param getContent flag indicating if launched by getting Content
     * @param bundleData useful information carried with bundle
     */
    public static void enterContainerPage(Activity activity, MediaData data,
            boolean getContent, Bundle bundleData) {
        if (sPlatform != null) {
            sPlatform.enterContainerPage(activity, data, getContent, bundleData);
        }
    }

    /**
     * Switch to ContainerPage from other page.
     * @param activity   gallery activity
     * @param data       related data of current media item
     * @param getContent flag indicating if launched by getting Content
     * @param bundleData useful information carried with bundle
     */
    public static void switchToContainerPage(Activity activity, MediaData data,
            boolean getContent, Bundle bundleData) {
        if (sPlatform != null) {
            sPlatform.switchToContainerPage(activity, data, getContent, bundleData);
        }
    }
	
	/**
     * Enter ContainerPage like ContinusShot.
     * @param activity   gallery activity
     * @param data       related data of current media item
     * @param getContent flag indicating if launched by getting Content
     * @param bundleData useful information carried with bundle
     */
    public static void enterContainerSavePage(Activity activity, MediaData data,
            boolean getContent, Bundle bundleData) {
        if (sPlatform != null) {
            sPlatform.enterContainerSavePage(activity, data, getContent, bundleData);
        }
    }

    /**
     * Switch to ContainerPage from other page.
     * @param activity   gallery activity
     * @param data       related data of current media item
     * @param getContent flag indicating if launched by getting Content
     * @param bundleData useful information carried with bundle
     */
    public static void switchToContainerSavePage(Activity activity, MediaData data,
            boolean getContent, Bundle bundleData) {
        if (sPlatform != null) {
            sPlatform.switchToContainerSavePage(activity, data, getContent, bundleData);
        }
    }

    /**
     * Submit job to Gallery thread pool.
     * @param work job that has been wrapped as work
     */
    public static void submitJob(Work work) {
        if (sPlatform != null) {
            sPlatform.submitJob(work);
        }
    }
}