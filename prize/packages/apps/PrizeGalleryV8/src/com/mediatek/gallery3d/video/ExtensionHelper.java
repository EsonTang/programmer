
package com.mediatek.gallery3d.video;

import android.content.Context;

import com.android.gallery3d.app.Log;
import com.mediatek.gallery3d.ext.IActivityHooker;
import com.mediatek.gallery3d.ext.IMovieExtension;
import com.mediatek.gallery3d.ext.IRewindAndForwardExtension;
import com.mediatek.gallery3d.ext.IServerTimeoutExtension;
import com.mediatek.gallery3d.ext.DefaultMovieExtension;
import com.mediatek.gallery3d.ext.DefaultRewindAndForwardExtension;
import com.mediatek.gallery3d.ext.DefaultServerTimeoutExtension;
import com.mediatek.galleryfeature.config.FeatureConfig;
import com.mediatek.common.MPlugin;

import java.util.ArrayList;
import java.util.List;

public class ExtensionHelper {
    private static final String TAG = "Gallery2/VideoPlayer/ExtensionHelper";
    private static final boolean LOG = true;

    private static List<IMovieExtension> sMovieExtensions;
    private static boolean sHasRewindAndForward;
    /// M: [FEATURE.ADD] Mutil-window @{
    private static String sContextString;
    /// @}
    /**
     * Get MovieExtension from plugin, otherwise use DefaultMovieExtension
     *
     * @param context
     */
    private static void ensureMovieExtension(final Context context) {
        Log.v(TAG, "ensureMovieExtension() sMovieExtensions "
                + sMovieExtensions);
        if (sMovieExtensions == null) {
            sMovieExtensions = new ArrayList<IMovieExtension>();
            boolean find = false;
            if (MtkVideoFeature.isMPluginExisted()) {
                final IMovieExtension ext = (IMovieExtension) MPlugin
                        .createInstance(IMovieExtension.class.getName(), context);
                Log.v(TAG, "ensureMovieExtension() ext = " + ext);
                if (ext != null) {
                    sMovieExtensions.add(ext);
                    find = true;
                }
            }
            if (!find) { // add default implemetation
                sMovieExtensions.add(new DefaultMovieExtension(context));
            }
        }
    }

    public static IActivityHooker getHooker(final Context context) {
        ensureMovieExtension(context);
        final ActivityHookerGroup group = new ActivityHookerGroup();
        getServerTimeoutExtension(context);
        getRewindAndForwardExtension(context);
        group.addHooker(new StopVideoHooker()); // add it for common feature.
        group.addHooker(new LoopVideoHooker()); // add it for common feature.
        group.addHooker(new TrimVideoHooker());
        group.addHooker(new NfcHooker());
        group.addHooker(new LetvHooker());
        if (MtkVideoFeature.isSlowMotionSupport() && sHasRewindAndForward) {
            group.addHooker(new SlowMotionHooker());
        }
        // add other feature in plugin app
        for (final IMovieExtension ext : sMovieExtensions) {
            final ArrayList<IActivityHooker> hookers = ext.getHookers(context);
            if (hookers != null) {
                for (int i = 0, size = hookers.size(); i < size; i++) {
                    IActivityHooker hooker = hookers.get(i);
                    group.addHooker(hooker);
                }
            }
        }

        if (mServerTimeoutExtension != null) {
            group.addHooker((IActivityHooker) mServerTimeoutExtension);
        }

        if (mRewindAndForwardExtension != null) {
            group.addHooker((IActivityHooker) mRewindAndForwardExtension);
        }

        if (MtkVideoFeature.isClearMotionMenuEnabled(context)) {
            group.addHooker(new ClearMotionHooker());
        }

        if (FeatureConfig.SUPPORT_PQ) {
            group.addHooker(new PqHooker());
        }

        // / M: [FEATURE.ADD] CTA @ {
        if (MtkVideoFeature.isSupportCTA()) {
            group.addHooker(new CTAHooker());
        }
        // / @}

        for (int i = 0, count = group.size(); i < count; i++) {
            if (LOG) {
                Log.v(TAG, "getHooker() [" + i + "]=" + group.getHooker(i));
            }
        }
        /// M: [FEATURE.ADD] Mutil-window @{
        sContextString = context.toString();
        /// @}
        return group;
    }

    private static IMovieDrmExtension sMovieDrmExtension;

    public static IMovieDrmExtension getMovieDrmExtension(final Context context) {
        if (sMovieDrmExtension == null) {
            /*
             * try { sMovieDrmExtension = (IMovieDrmExtension)
             * PluginManager.createPluginObject(
             * context.getApplicationContext(),
             * IMovieDrmExtension.class.getName()); } catch
             * (Plugin.ObjectCreationException e) { sMovieDrmExtension = new
             * MovieDrmExtension(); }
             */
            // should be modified for common feature
            if (MtkVideoFeature.isOmaDrmSupported()) {
                sMovieDrmExtension = new MovieDrmExtensionImpl();
            } else {
                sMovieDrmExtension = new DefaultMovieDrmExtension();
            }
        }
        return sMovieDrmExtension;
    }

    private static IServerTimeoutExtension mServerTimeoutExtension = null;

    public static IServerTimeoutExtension getServerTimeoutExtension(
            final Context context) {
        if (mServerTimeoutExtension != null) {
            return mServerTimeoutExtension;
        }
        ensureMovieExtension(context);
        for (final IMovieExtension ext : sMovieExtensions) {
            final IServerTimeoutExtension serverTimeout = ext
                    .getServerTimeoutExtension();
            if (serverTimeout != null) {
                mServerTimeoutExtension = serverTimeout;
                return serverTimeout;
            }
        }
        mServerTimeoutExtension = new DefaultServerTimeoutExtension();
        return mServerTimeoutExtension;
    }

    private static IRewindAndForwardExtension mRewindAndForwardExtension;

    public static IRewindAndForwardExtension getRewindAndForwardExtension (
            final Context context) {
        /// M: [FEATURE.ADD] Mutil-window @{
        // To avoid share one RewindandForwardExtension plugin in mutil-window
        // when two or more vieo player work together.
        String currentContextString = context.toString();
        Log.v(TAG,"getRewindAndForwardExtension() currentContextString= "
                + currentContextString + " sContextString= " + sContextString);
        /// @}
        if (mRewindAndForwardExtension != null &&
                currentContextString.equals(sContextString)) {
            return mRewindAndForwardExtension;
        }
        ensureMovieExtension(context);
        for (final IMovieExtension ext : sMovieExtensions) {
            final IRewindAndForwardExtension rewindAndForward = ext
                    .getRewindAndForwardExtension();
            if (rewindAndForward != null && rewindAndForward.getView() != null) {
                mRewindAndForwardExtension = rewindAndForward;
                sHasRewindAndForward = true;
                return rewindAndForward;
            }
        }
        return new DefaultRewindAndForwardExtension();
    }

    public static boolean hasRewindAndForward(final Context context) {
        return sHasRewindAndForward;
    }

    public static boolean shouldEnableCheckLongSleep(final Context context) {
        ensureMovieExtension(context);
        for (final IMovieExtension ext : sMovieExtensions) {
            return ext.shouldEnableCheckLongSleep();
        }
        return true;
    }
}
