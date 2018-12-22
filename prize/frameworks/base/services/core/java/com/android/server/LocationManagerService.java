/*
* Copyright (C) 2015 MediaTek Inc.
* Modification based on code covered by the mentioned copyright
* and/or permission notice(s).
*/
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

package com.android.server;

import android.content.pm.PackageManagerInternal;
import com.android.internal.content.PackageMonitor;
import com.android.internal.location.ProviderProperties;
import com.android.internal.location.ProviderRequest;
import com.android.internal.os.BackgroundThread;
import com.android.internal.util.ArrayUtils;
import com.android.server.location.ActivityRecognitionProxy;
import com.android.server.location.FlpHardwareProvider;
import com.android.server.location.FusedProxy;
import com.android.server.location.GeocoderProxy;
import com.android.server.location.GeofenceManager;
import com.android.server.location.GeofenceProxy;
import com.android.server.location.GnssLocationProvider;
import com.android.server.location.GnssMeasurementsProvider;
import com.android.server.location.GnssNavigationMessageProvider;
import com.android.server.location.LocationBlacklist;
import com.android.server.location.LocationFudger;
import com.android.server.location.LocationProviderInterface;
import com.android.server.location.LocationProviderProxy;
import com.android.server.location.LocationRequestStatistics;
import com.android.server.location.LocationRequestStatistics.PackageProviderKey;
import com.android.server.location.LocationRequestStatistics.PackageStatistics;
import com.android.server.location.MockProvider;
import com.android.server.location.PassiveProvider;

import android.app.AppOpsManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.pm.Signature;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.hardware.location.ActivityRecognitionHardware;
import android.location.Address;
import android.location.Criteria;
import android.location.GeocoderParams;
import android.location.Geofence;
import android.location.IGnssMeasurementsListener;
import android.location.IGnssStatusListener;
import android.location.IGnssStatusProvider;
import android.location.IGpsGeofenceHardware;
import android.location.IGnssNavigationMessageListener;
import android.location.ILocationListener;
import android.location.ILocationManager;
import android.location.INetInitiatedListener;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.location.LocationRequest;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.WorkSource;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.EventLog;
import android.util.Log;
import android.util.Slog;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

//MTK add
import android.os.SystemProperties;
import android.app.AlarmManager;
import com.mediatek.cta.CtaUtils;
//MTK end
/*-prize-lihuangyuan add for gps monitor-2018-06-22-start*/
import android.app.ActivityManager;
/*-prize-lihuangyuan add for gps monitor-2018-06-22-end*/

/**
 * The service class that manages LocationProviders and issues location
 * updates and alerts.
 */
public class LocationManagerService extends ILocationManager.Stub {
    private static final String TAG = "LocationManagerService";

    /// M: for build type check
    private static final boolean IS_USER_BUILD =
            "user".equals(Build.TYPE) || "userdebug".equals(Build.TYPE);

    private static final String PROP_FORCE_DEBUG_KEY = "persist.log.tag.tel_dbg";
    public static final boolean FORCE_DEBUG =
            (SystemProperties.getInt(PROP_FORCE_DEBUG_KEY, 0) == 1); /* STOPSHIP if true */

    public static final boolean D = IS_USER_BUILD ?
            (Log.isLoggable(TAG, Log.DEBUG) || FORCE_DEBUG) : true;

    private static final String WAKELOCK_KEY = TAG;

    // Location resolution level: no location data whatsoever
    private static final int RESOLUTION_LEVEL_NONE = 0;
    // Location resolution level: coarse location data only
    private static final int RESOLUTION_LEVEL_COARSE = 1;
    // Location resolution level: fine location data
    private static final int RESOLUTION_LEVEL_FINE = 2;

    private static final String ACCESS_MOCK_LOCATION =
            android.Manifest.permission.ACCESS_MOCK_LOCATION;
    private static final String ACCESS_LOCATION_EXTRA_COMMANDS =
            android.Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS;
    private static final String INSTALL_LOCATION_PROVIDER =
            android.Manifest.permission.INSTALL_LOCATION_PROVIDER;

    private static final String NETWORK_LOCATION_SERVICE_ACTION =
            "com.android.location.service.v3.NetworkLocationProvider";
    private static final String FUSED_LOCATION_SERVICE_ACTION =
            "com.android.location.service.FusedLocationProvider";

    private static final int MSG_LOCATION_CHANGED = 1;
    /*-prize-lihuangyuan add for gps monitor-2018-06-22-start*/
    private static final int MSG_REQUEST_GPS_PROVIDER = 2;
    private static final int MSG_REMOVE_GPS_PROVIDER = 3;
    /*-prize-lihuangyuan add for gps monitor-2018-06-22-end*/

    private static final long NANOS_PER_MILLI = 1000000L;

    // The maximum interval a location request can have and still be considered "high power".
    private static final long HIGH_POWER_INTERVAL_MS = 5 * 60 * 1000;

    // Location Providers may sometimes deliver location updates
    // slightly faster that requested - provide grace period so
    // we don't unnecessarily filter events that are otherwise on
    // time
    private static final int MAX_PROVIDER_SCHEDULING_JITTER_MS = 100;

    private static final LocationRequest DEFAULT_LOCATION_REQUEST = new LocationRequest();

    private final Context mContext;
    private final AppOpsManager mAppOps;

    // used internally for synchronization
    private final Object mLock = new Object();

    // --- fields below are final after systemRunning() ---
    private LocationFudger mLocationFudger;
    private GeofenceManager mGeofenceManager;
    private PackageManager mPackageManager;
    private PowerManager mPowerManager;
    private UserManager mUserManager;
    private GeocoderProxy mGeocodeProvider;
    private IGnssStatusProvider mGnssStatusProvider;
    private INetInitiatedListener mNetInitiatedListener;
    private LocationWorkerHandler mLocationHandler;
    private PassiveProvider mPassiveProvider;  // track passive provider for special cases
    private LocationBlacklist mBlacklist;
    private GnssMeasurementsProvider mGnssMeasurementsProvider;
    private GnssNavigationMessageProvider mGnssNavigationMessageProvider;
    private IGpsGeofenceHardware mGpsGeofenceProxy;

    // --- fields below are protected by mLock ---
    // Set of providers that are explicitly enabled
    // Only used by passive, fused & test.  Network & GPS are controlled separately, and not listed.
    private final Set<String> mEnabledProviders = new HashSet<String>();

    // Set of providers that are explicitly disabled
    private final Set<String> mDisabledProviders = new HashSet<String>();

    // Mock (test) providers
    private final HashMap<String, MockProvider> mMockProviders =
            new HashMap<String, MockProvider>();

    // all receivers
    private final HashMap<Object, Receiver> mReceivers = new HashMap<Object, Receiver>();

    // currently installed providers (with mocks replacing real providers)
    private final ArrayList<LocationProviderInterface> mProviders =
            new ArrayList<LocationProviderInterface>();

    // real providers, saved here when mocked out
    private final HashMap<String, LocationProviderInterface> mRealProviders =
            new HashMap<String, LocationProviderInterface>();

    // mapping from provider name to provider
    private final HashMap<String, LocationProviderInterface> mProvidersByName =
            new HashMap<String, LocationProviderInterface>();

    // mapping from provider name to all its UpdateRecords
    private final HashMap<String, ArrayList<UpdateRecord>> mRecordsByProvider =
            new HashMap<String, ArrayList<UpdateRecord>>();

    private final LocationRequestStatistics mRequestStatistics = new LocationRequestStatistics();

    // mapping from provider name to last known location
    private final HashMap<String, Location> mLastLocation = new HashMap<String, Location>();

    // same as mLastLocation, but is not updated faster than LocationFudger.FASTEST_INTERVAL_MS.
    // locations stored here are not fudged for coarse permissions.
    private final HashMap<String, Location> mLastLocationCoarseInterval =
            new HashMap<String, Location>();

    // all providers that operate over proxy, for authorizing incoming location
    private final ArrayList<LocationProviderProxy> mProxyProviders =
            new ArrayList<LocationProviderProxy>();

    // current active user on the device - other users are denied location data
    private int mCurrentUserId = UserHandle.USER_SYSTEM;
    private int[] mCurrentUserProfiles = new int[] { UserHandle.USER_SYSTEM };

    private GnssLocationProvider.GnssSystemInfoProvider mGnssSystemInfoProvider;

    //MTK add @prevent provider status error
    private boolean mProviderCheckAlarm = false;
    private PendingIntent mProviderCheckTimeoutIntent;
    private AlarmManager mAlarmManager;
    private static final int CHECK_PROVIDER_TIMER = 5 * 1000;

    // MTK add package whitelist to force using location source
    // [Test ID: 53653] ALFMS01120446 TMOUS_2017Q2_GID-MTRREQ-201603
    private static final String mWhitelistPackage = "com.mediatek.ims";
    private boolean mWhitelistWorkingMode = false;
    private static final String mWhitelistProvider = LocationManager.NETWORK_PROVIDER;
    //MTK end

    public LocationManagerService(Context context) {
        super();
        mContext = context;
        mAppOps = (AppOpsManager)context.getSystemService(Context.APP_OPS_SERVICE);

        // Let the package manager query which are the default location
        // providers as they get certain permissions granted by default.
        PackageManagerInternal packageManagerInternal = LocalServices.getService(
                PackageManagerInternal.class);
        packageManagerInternal.setLocationPackagesProvider(
                new PackageManagerInternal.PackagesProvider() {
                    @Override
                    public String[] getPackages(int userId) {
                        return mContext.getResources().getStringArray(
                                com.android.internal.R.array.config_locationProviderPackageNames);
                    }
                });

        if (D) Log.d(TAG, "Constructed");

        // most startup is deferred until systemRunning()
    }

    public void systemRunning() {
        synchronized (mLock) {
            if (D) Log.d(TAG, "systemRunning()");

            // fetch package manager
            mPackageManager = mContext.getPackageManager();

            // fetch power manager
            mPowerManager = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);

            // prepare worker thread
            mLocationHandler = new LocationWorkerHandler(BackgroundThread.get().getLooper());

            // prepare mLocationHandler's dependents
            mLocationFudger = new LocationFudger(mContext, mLocationHandler);
            mBlacklist = new LocationBlacklist(mContext, mLocationHandler);
            mBlacklist.init();
            mGeofenceManager = new GeofenceManager(mContext, mBlacklist);

            // Monitor for app ops mode changes.
            AppOpsManager.OnOpChangedListener callback
                    = new AppOpsManager.OnOpChangedInternalListener() {
                public void onOpChanged(int op, String packageName) {
                    synchronized (mLock) {
                        for (Receiver receiver : mReceivers.values()) {
                            receiver.updateMonitoring(true);
                        }
                        applyAllProviderRequirementsLocked();
                    }
                }
            };
            mAppOps.startWatchingMode(AppOpsManager.OP_COARSE_LOCATION, null, callback);

            PackageManager.OnPermissionsChangedListener permissionListener
                    = new PackageManager.OnPermissionsChangedListener() {
                @Override
                public void onPermissionsChanged(final int uid) {
                    synchronized (mLock) {
                        applyAllProviderRequirementsLocked();
                    }
                }
            };
            mPackageManager.addOnPermissionsChangeListener(permissionListener);

            mUserManager = (UserManager) mContext.getSystemService(Context.USER_SERVICE);
            updateUserProfiles(mCurrentUserId);

            // prepare providers
            loadProvidersLocked();
            updateProvidersLocked();
        }

        // listen for settings changes
        mContext.getContentResolver().registerContentObserver(
                Settings.Secure.getUriFor(Settings.Secure.LOCATION_PROVIDERS_ALLOWED), true,
                new ContentObserver(mLocationHandler) {
                    @Override
                    public void onChange(boolean selfChange) {
                        synchronized (mLock) {
                            // MTK add @prevent provider status error
                            triggerProviderCheck();
                            // MTK end
                            updateProvidersLocked();
                        }
                    }
                }, UserHandle.USER_ALL);
        mPackageMonitor.register(mContext, mLocationHandler.getLooper(), true);

        // listen for user change
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_USER_SWITCHED);
        intentFilter.addAction(Intent.ACTION_MANAGED_PROFILE_ADDED);
        intentFilter.addAction(Intent.ACTION_MANAGED_PROFILE_REMOVED);
        intentFilter.addAction(Intent.ACTION_SHUTDOWN);

        mContext.registerReceiverAsUser(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (Intent.ACTION_USER_SWITCHED.equals(action)) {
                    switchUser(intent.getIntExtra(Intent.EXTRA_USER_HANDLE, 0));
                } else if (Intent.ACTION_MANAGED_PROFILE_ADDED.equals(action)
                        || Intent.ACTION_MANAGED_PROFILE_REMOVED.equals(action)) {
                    updateUserProfiles(mCurrentUserId);
                } else if (Intent.ACTION_SHUTDOWN.equals(action)) {
                    // shutdown only if UserId indicates whole system, not just one user
                    if(D) Log.d(TAG, "Shutdown received with UserId: " + getSendingUserId());
                    if (getSendingUserId() == UserHandle.USER_ALL) {
                        shutdownComponents();
                    }
                }
            }
        }, UserHandle.ALL, intentFilter, null, mLocationHandler);

        // MTK add start ALPS00085457
        final String ACTION_BOOT_IPO = "android.intent.action.ACTION_BOOT_IPO";
        final String ACTION_SHUTDOWN_IPO = "android.intent.action.ACTION_SHUTDOWN_IPO";
        IntentFilter intentIPOFilter = new IntentFilter();
        if (SystemProperties.get("ro.mtk_ipo_support").equals("1")) {
            intentIPOFilter.addAction(ACTION_BOOT_IPO);
            intentIPOFilter.addAction(ACTION_SHUTDOWN_IPO);
            mContext.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                synchronized (mLock) {
                    String action = intent.getAction();
                    if (ACTION_SHUTDOWN_IPO.equals(action)) {
                        locationIPOremoveProviderLocked();
                    } else if (ACTION_BOOT_IPO.equals(action)) {
                        locationIPOcreateProviderLocked();
                        updateProvidersLocked();
                    }
                }
            }
        }, intentIPOFilter);
        }

        // MTK add @prevent provider status error
        final String ALARM_PVDCHECK = "com.mediatek.location.providercheck";
        IntentFilter intentPvdChkFilter = new IntentFilter();
        intentPvdChkFilter.addAction(ALARM_PVDCHECK);
        mContext.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (ALARM_PVDCHECK.equals(action)) {
                    synchronized (mLock) {
                        removeProviderCheck();
                        updateProvidersLocked();
                    }
                }
            }
        }, intentPvdChkFilter);
        mAlarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        mProviderCheckTimeoutIntent = PendingIntent.getBroadcast(mContext, 0,
                                    new Intent(ALARM_PVDCHECK), 0);

        // MTK add rebind NLP
        final String NLP_BIND_REQUEST = "com.mediatek.lbs.action.NLP_BIND_REQUEST";
        IntentFilter intentNlpFilter = new IntentFilter();
        intentNlpFilter.addAction(NLP_BIND_REQUEST);

        mContext.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equals(NLP_BIND_REQUEST)) {
                    boolean bindGmsPackage = intent.getBooleanExtra("bindGmsPackage", false);
                    Log.d(TAG, "received action NLP_BIND_REQUEST bindGmsPackage=" + bindGmsPackage);
                    synchronized (mLock) {
                        reBindNetworkProviderLock(bindGmsPackage);
                    }
                }
            }
        }, intentNlpFilter);
        // MTK add end
    }

    /**
     * Provides a way for components held by the {@link LocationManagerService} to clean-up
     * gracefully on system's shutdown.
     *
     * NOTES:
     * 1) Only provides a chance to clean-up on an opt-in basis. This guarantees back-compat
     * support for components that do not wish to handle such event.
     */
    private void shutdownComponents() {
        if(D) Log.d(TAG, "Shutting down components...");

        LocationProviderInterface gpsProvider = mProvidersByName.get(LocationManager.GPS_PROVIDER);
        if (gpsProvider != null && gpsProvider.isEnabled()) {
            gpsProvider.disable();
        }

        // it is needed to check if FLP HW provider is supported before accessing the instance, this
        // avoids an exception to be thrown by the singleton factory method
        if (FlpHardwareProvider.isSupported()) {
            FlpHardwareProvider flpHardwareProvider = FlpHardwareProvider.getInstance(mContext);
            flpHardwareProvider.cleanup();
        }
    }

    /**
     * Makes a list of userids that are related to the current user. This is
     * relevant when using managed profiles. Otherwise the list only contains
     * the current user.
     *
     * @param currentUserId the current user, who might have an alter-ego.
     */
    void updateUserProfiles(int currentUserId) {
        int[] profileIds = mUserManager.getProfileIdsWithDisabled(currentUserId);
        synchronized (mLock) {
            mCurrentUserProfiles = profileIds;
        }
    }

    /**
     * Checks if the specified userId matches any of the current foreground
     * users stored in mCurrentUserProfiles.
     */
    private boolean isCurrentProfile(int userId) {
        synchronized (mLock) {
            return ArrayUtils.contains(mCurrentUserProfiles, userId);
        }
    }

    private void ensureFallbackFusedProviderPresentLocked(ArrayList<String> pkgs) {
        PackageManager pm = mContext.getPackageManager();
        String systemPackageName = mContext.getPackageName();
        ArrayList<HashSet<Signature>> sigSets = ServiceWatcher.getSignatureSets(mContext, pkgs);

        List<ResolveInfo> rInfos = pm.queryIntentServicesAsUser(
                new Intent(FUSED_LOCATION_SERVICE_ACTION),
                PackageManager.GET_META_DATA, mCurrentUserId);
        for (ResolveInfo rInfo : rInfos) {
            String packageName = rInfo.serviceInfo.packageName;

            // Check that the signature is in the list of supported sigs. If it's not in
            // this list the standard provider binding logic won't bind to it.
            try {
                PackageInfo pInfo;
                pInfo = pm.getPackageInfo(packageName, PackageManager.GET_SIGNATURES);
                if (!ServiceWatcher.isSignatureMatch(pInfo.signatures, sigSets)) {
                    Log.w(TAG, packageName + " resolves service " + FUSED_LOCATION_SERVICE_ACTION +
                            ", but has wrong signature, ignoring");
                    continue;
                }
            } catch (NameNotFoundException e) {
                Log.e(TAG, "missing package: " + packageName);
                continue;
            }

            // Get the version info
            if (rInfo.serviceInfo.metaData == null) {
                Log.w(TAG, "Found fused provider without metadata: " + packageName);
                continue;
            }

            int version = rInfo.serviceInfo.metaData.getInt(
                    ServiceWatcher.EXTRA_SERVICE_VERSION, -1);
            if (version == 0) {
                // This should be the fallback fused location provider.

                // Make sure it's in the system partition.
                if ((rInfo.serviceInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                    if (D) Log.d(TAG, "Fallback candidate not in /system: " + packageName);
                    continue;
                }

                // Check that the fallback is signed the same as the OS
                // as a proxy for coreApp="true"
                if (pm.checkSignatures(systemPackageName, packageName)
                        != PackageManager.SIGNATURE_MATCH) {
                    if (D) Log.d(TAG, "Fallback candidate not signed the same as system: "
                            + packageName);
                    continue;
                }

                // Found a valid fallback.
                if (D) Log.d(TAG, "Found fallback provider: " + packageName);
                return;
            } else {
                if (D) Log.d(TAG, "Fallback candidate not version 0: " + packageName);
            }
        }
        return;

        /*throw new IllegalStateException("Unable to find a fused location provider that is in the "
                + "system partition with version 0 and signed with the platform certificate. "
                + "Such a package is needed to provide a default fused location provider in the "
                + "event that no other fused location provider has been installed or is currently "
                + "available. For example, coreOnly boot mode when decrypting the data "
                + "partition. The fallback must also be marked coreApp=\"true\" in the manifest");*/
    }

    private void loadProvidersLocked() {
        // create a passive location provider, which is always enabled
        PassiveProvider passiveProvider = new PassiveProvider(this);
        addProviderLocked(passiveProvider);
        mEnabledProviders.add(passiveProvider.getName());
        mPassiveProvider = passiveProvider;

        if (GnssLocationProvider.isSupported()) {
            // Create a gps location provider
            GnssLocationProvider gnssProvider = new GnssLocationProvider(mContext, this,
                    mLocationHandler.getLooper());
            mGnssSystemInfoProvider = gnssProvider.getGnssSystemInfoProvider();
            mGnssStatusProvider = gnssProvider.getGnssStatusProvider();
            mNetInitiatedListener = gnssProvider.getNetInitiatedListener();
            addProviderLocked(gnssProvider);
            mRealProviders.put(LocationManager.GPS_PROVIDER, gnssProvider);
            mGnssMeasurementsProvider = gnssProvider.getGnssMeasurementsProvider();
            mGnssNavigationMessageProvider = gnssProvider.getGnssNavigationMessageProvider();
            mGpsGeofenceProxy = gnssProvider.getGpsGeofenceProxy();
        }

        /*
        Load package name(s) containing location provider support.
        These packages can contain services implementing location providers:
        Geocoder Provider, Network Location Provider, and
        Fused Location Provider. They will each be searched for
        service components implementing these providers.
        The location framework also has support for installation
        of new location providers at run-time. The new package does not
        have to be explicitly listed here, however it must have a signature
        that matches the signature of at least one package on this list.
        */
        Resources resources = mContext.getResources();
        ArrayList<String> providerPackageNames = new ArrayList<String>();
        String[] pkgs = resources.getStringArray(
                com.android.internal.R.array.config_locationProviderPackageNames);
        if (D) Log.d(TAG, "certificates for location providers pulled from: " +
                Arrays.toString(pkgs));
        if (pkgs != null) providerPackageNames.addAll(Arrays.asList(pkgs));

        ensureFallbackFusedProviderPresentLocked(providerPackageNames);

        // bind to network provider
        ///M: Add vendor NLP package ResId and preferred ResId
        LocationProviderProxy networkProvider = LocationProviderProxy.createAndBind(
                mContext,
                LocationManager.NETWORK_PROVIDER,
                NETWORK_LOCATION_SERVICE_ACTION,
                com.android.internal.R.bool.config_enableNetworkLocationOverlay,
                com.android.internal.R.string.config_networkLocationProviderPackageName,
                com.android.internal.R.array.config_locationProviderPackageNames,
                com.mediatek.internal.R.array.config_cnLocationProviderPackageNames,
                0,
                mLocationHandler);
        if (networkProvider != null) {
            mRealProviders.put(LocationManager.NETWORK_PROVIDER, networkProvider);
            mProxyProviders.add(networkProvider);
            addProviderLocked(networkProvider);
        } else {
            Slog.w(TAG,  "no network location provider found");
        }

        // bind to fused provider
        LocationProviderProxy fusedLocationProvider = LocationProviderProxy.createAndBind(
                mContext,
                LocationManager.FUSED_PROVIDER,
                FUSED_LOCATION_SERVICE_ACTION,
                com.android.internal.R.bool.config_enableFusedLocationOverlay,
                com.android.internal.R.string.config_fusedLocationProviderPackageName,
                com.android.internal.R.array.config_locationProviderPackageNames,
                mLocationHandler);
        if (fusedLocationProvider != null) {
            addProviderLocked(fusedLocationProvider);
            mProxyProviders.add(fusedLocationProvider);
            mEnabledProviders.add(fusedLocationProvider.getName());
            mRealProviders.put(LocationManager.FUSED_PROVIDER, fusedLocationProvider);
        } else {
            Slog.e(TAG, "no fused location provider found",
                    new IllegalStateException("Location service needs a fused location provider"));
        }

        // bind to geocoder provider
        ///M: Add vendor NLP package ResId and preferred ResId
        mGeocodeProvider = GeocoderProxy.createAndBind(mContext,
                com.android.internal.R.bool.config_enableGeocoderOverlay,
                com.android.internal.R.string.config_geocoderProviderPackageName,
                com.android.internal.R.array.config_locationProviderPackageNames,
                com.mediatek.internal.R.array.config_cnLocationProviderPackageNames,
                0,
                mLocationHandler);
        if (mGeocodeProvider == null) {
            Slog.e(TAG,  "no geocoder provider found");
        }

        // bind to fused hardware provider if supported
        // in devices without support, requesting an instance of FlpHardwareProvider will raise an
        // exception, so make sure we only do that when supported
        FlpHardwareProvider flpHardwareProvider;
        if (FlpHardwareProvider.isSupported()) {
            flpHardwareProvider = FlpHardwareProvider.getInstance(mContext);
            FusedProxy fusedProxy = FusedProxy.createAndBind(
                    mContext,
                    mLocationHandler,
                    flpHardwareProvider.getLocationHardware(),
                    com.android.internal.R.bool.config_enableHardwareFlpOverlay,
                    com.android.internal.R.string.config_hardwareFlpPackageName,
                    com.android.internal.R.array.config_locationProviderPackageNames);
            if (fusedProxy == null) {
                Slog.d(TAG, "Unable to bind FusedProxy.");
            }
        } else {
            flpHardwareProvider = null;
            Slog.d(TAG, "FLP HAL not supported");
        }

        // bind to geofence provider
        GeofenceProxy provider = GeofenceProxy.createAndBind(
                mContext,com.android.internal.R.bool.config_enableGeofenceOverlay,
                com.android.internal.R.string.config_geofenceProviderPackageName,
                com.android.internal.R.array.config_locationProviderPackageNames,
                mLocationHandler,
                mGpsGeofenceProxy,
                flpHardwareProvider != null ? flpHardwareProvider.getGeofenceHardware() : null);
        if (provider == null) {
            Slog.d(TAG,  "Unable to bind FLP Geofence proxy.");
        }

        // bind to hardware activity recognition
        boolean activityRecognitionHardwareIsSupported = ActivityRecognitionHardware.isSupported();
        ActivityRecognitionHardware activityRecognitionHardware = null;
        if (activityRecognitionHardwareIsSupported) {
            activityRecognitionHardware = ActivityRecognitionHardware.getInstance(mContext);
        } else {
            Slog.d(TAG, "Hardware Activity-Recognition not supported.");
        }
        ActivityRecognitionProxy proxy = ActivityRecognitionProxy.createAndBind(
                mContext,
                mLocationHandler,
                activityRecognitionHardwareIsSupported,
                activityRecognitionHardware,
                com.android.internal.R.bool.config_enableActivityRecognitionHardwareOverlay,
                com.android.internal.R.string.config_activityRecognitionHardwarePackageName,
                com.android.internal.R.array.config_locationProviderPackageNames);
        if (proxy == null) {
            Slog.d(TAG, "Unable to bind ActivityRecognitionProxy.");
        }

        String[] testProviderStrings = resources.getStringArray(
                com.android.internal.R.array.config_testLocationProviders);
        for (String testProviderString : testProviderStrings) {
            String fragments[] = testProviderString.split(",");
            String name = fragments[0].trim();
            if (mProvidersByName.get(name) != null) {
                throw new IllegalArgumentException("Provider \"" + name + "\" already exists");
            }
            ProviderProperties properties = new ProviderProperties(
                    Boolean.parseBoolean(fragments[1]) /* requiresNetwork */,
                    Boolean.parseBoolean(fragments[2]) /* requiresSatellite */,
                    Boolean.parseBoolean(fragments[3]) /* requiresCell */,
                    Boolean.parseBoolean(fragments[4]) /* hasMonetaryCost */,
                    Boolean.parseBoolean(fragments[5]) /* supportsAltitude */,
                    Boolean.parseBoolean(fragments[6]) /* supportsSpeed */,
                    Boolean.parseBoolean(fragments[7]) /* supportsBearing */,
                    Integer.parseInt(fragments[8]) /* powerRequirement */,
                    Integer.parseInt(fragments[9]) /* accuracy */);
            addTestProviderLocked(name, properties);
        }
    }

    /** prize add by xiarui 2018-02-02 start **/
    @Override
    public List<String> getInUsePackagesList() {
        List<String> list = new ArrayList();

        HashMap<PackageProviderKey, PackageStatistics> statistics = mRequestStatistics.statistics;

        for (Map.Entry<PackageProviderKey, PackageStatistics> stat : statistics.entrySet()) {
            PackageProviderKey key = stat.getKey();
            PackageStatistics value = stat.getValue();
            if (value.isActive() && (key.providerName.equals(LocationManager.GPS_PROVIDER) || key.providerName.equals(LocationManager.NETWORK_PROVIDER))) {
                String name = key.packageName;
                if (!list.contains(name)) {
                    list.add(name);
                }
            }
        }
        Log.d(TAG, "get in use pkg:" + list);
        return list;
    }
    /** prize add by xiarui 2018-02-02 end **/

    //MTK add
    private void locationIPOremoveProviderLocked() {
        Log.d(TAG, "IPO shutdown for location");
        LocationProviderInterface realProvider = mRealProviders.get(LocationManager.NETWORK_PROVIDER);
        if (realProvider != null) {
            LocationProviderProxy networkProvider = (LocationProviderProxy) realProvider;
            networkProvider.unbind();
            mRealProviders.remove(LocationManager.NETWORK_PROVIDER);
            mProxyProviders.remove(networkProvider);
            removeProviderLocked(realProvider);
            realProvider = null;
        }

        realProvider = mRealProviders.get(LocationManager.FUSED_PROVIDER);
        if (realProvider != null) {
            LocationProviderProxy fusedProvider = (LocationProviderProxy) realProvider;
            fusedProvider.unbind();
            mRealProviders.remove(LocationManager.FUSED_PROVIDER);
            mProxyProviders.remove(fusedProvider);
            removeProviderLocked(realProvider);
            realProvider = null;
        }

        if (mGeocodeProvider != null) {
            mGeocodeProvider.unbind();
            mGeocodeProvider = null;
        }

        mLastLocation.clear();
        mLastLocationCoarseInterval.clear();
        updateProviderListenersLocked(LocationManager.GPS_PROVIDER, false);
    }

    private void locationIPOcreateProviderLocked() {
        Log.d(TAG, "IPO powerup for location");
        /*
        Load package name(s) containing location provider support.
        These packages can contain services implementing location providers:
        Geocoder Provider, Network Location Provider, and
        Fused Location Provider. They will each be searched for
        service components implementing these providers.
        The location framework also has support for installation
        of new location providers at run-time. The new package does not
        have to be explicitly listed here, however it must have a signature
        that matches the signature of at least one package on this list.
        */
        Resources resources = mContext.getResources();
        ArrayList<String> providerPackageNames = new ArrayList<String>();
        String[] pkgs = resources.getStringArray(
                com.android.internal.R.array.config_locationProviderPackageNames);
        if (D) Log.d(TAG, "certificates for location providers pulled from: " +
                Arrays.toString(pkgs));
        if (pkgs != null) providerPackageNames.addAll(Arrays.asList(pkgs));

        ensureFallbackFusedProviderPresentLocked(providerPackageNames);

        // bind to network provider
        LocationProviderProxy networkProvider = LocationProviderProxy.createAndBind(
                mContext,
                LocationManager.NETWORK_PROVIDER,
                NETWORK_LOCATION_SERVICE_ACTION,
                com.android.internal.R.bool.config_enableNetworkLocationOverlay,
                com.android.internal.R.string.config_networkLocationProviderPackageName,
                com.android.internal.R.array.config_locationProviderPackageNames,
                com.mediatek.internal.R.array.config_cnLocationProviderPackageNames,
                0,
                mLocationHandler);

        if (networkProvider != null) {
            mRealProviders.put(LocationManager.NETWORK_PROVIDER, networkProvider);
            mProxyProviders.add(networkProvider);
            addProviderLocked(networkProvider);
        } else {
            Slog.w(TAG,  "no network location provider found");
        }

        LocationProviderProxy fusedLocationProvider = LocationProviderProxy.createAndBind(
                mContext,
                LocationManager.FUSED_PROVIDER,
                FUSED_LOCATION_SERVICE_ACTION,
                com.android.internal.R.bool.config_enableFusedLocationOverlay,
                com.android.internal.R.string.config_fusedLocationProviderPackageName,
                com.android.internal.R.array.config_locationProviderPackageNames,
                mLocationHandler);
        if (fusedLocationProvider != null) {
            addProviderLocked(fusedLocationProvider);
            mProxyProviders.add(fusedLocationProvider);
            mEnabledProviders.add(fusedLocationProvider.getName());
            mRealProviders.put(LocationManager.FUSED_PROVIDER, fusedLocationProvider);
        } else {
            Slog.e(TAG, "no fused location provider found",
                    new IllegalStateException("Location service needs a fused location provider"));
        }

        // bind to geocoder provider
        mGeocodeProvider = GeocoderProxy.createAndBind(mContext,
                com.android.internal.R.bool.config_enableGeocoderOverlay,
                com.android.internal.R.string.config_geocoderProviderPackageName,
                com.android.internal.R.array.config_locationProviderPackageNames,
                com.mediatek.internal.R.array.config_cnLocationProviderPackageNames,
                0,
                mLocationHandler);
        if (mGeocodeProvider == null) {
            Slog.e(TAG,  "no geocoder provider found");
        }
    }


    // MTK add @prevent provider status error
    private void triggerProviderCheck() {
        if(D) Log.d(TAG, "triggerProviderCheck before: " + mProviderCheckAlarm + " to:true");
        mProviderCheckAlarm = true;
        mAlarmManager.cancel(mProviderCheckTimeoutIntent);
        mAlarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
        SystemClock.elapsedRealtime() + CHECK_PROVIDER_TIMER, mProviderCheckTimeoutIntent);
    }

    private void removeProviderCheck() {
        mProviderCheckAlarm = false;
        if(D) Log.d(TAG, "removeProviderCheck : " + mProviderCheckAlarm);
        mAlarmManager.cancel(mProviderCheckTimeoutIntent);
    }
    // MTK end

    /**
     * Called when the device's active user changes.
     * @param userId the new active user's UserId
     */
    private void switchUser(int userId) {
        if (mCurrentUserId == userId) {
            return;
        }
        mBlacklist.switchUser(userId);
        mLocationHandler.removeMessages(MSG_LOCATION_CHANGED);
        synchronized (mLock) {
            mLastLocation.clear();
            mLastLocationCoarseInterval.clear();
            for (LocationProviderInterface p : mProviders) {
                updateProviderListenersLocked(p.getName(), false);
            }
            mCurrentUserId = userId;
            updateUserProfiles(userId);
            updateProvidersLocked();
        }
    }

    /**
     * A wrapper class holding either an ILocationListener or a PendingIntent to receive
     * location updates.
     */
    private final class Receiver implements IBinder.DeathRecipient, PendingIntent.OnFinished {
        final int mUid;  // uid of receiver
        final int mPid;  // pid of receiver
        final String mPackageName;  // package name of receiver
        final int mAllowedResolutionLevel;  // resolution level allowed to receiver

        final ILocationListener mListener;
        final PendingIntent mPendingIntent;
        final WorkSource mWorkSource; // WorkSource for battery blame, or null to assign to caller.
        final boolean mHideFromAppOps; // True if AppOps should not monitor this receiver.
        final Object mKey;

        final HashMap<String,UpdateRecord> mUpdateRecords = new HashMap<String,UpdateRecord>();

        // True if app ops has started monitoring this receiver for locations.
        boolean mOpMonitoring;
        // True if app ops has started monitoring this receiver for high power (gps) locations.
        boolean mOpHighPowerMonitoring;
        int mPendingBroadcasts;
        PowerManager.WakeLock mWakeLock;

        Receiver(ILocationListener listener, PendingIntent intent, int pid, int uid,
                String packageName, WorkSource workSource, boolean hideFromAppOps) {
            mListener = listener;
            mPendingIntent = intent;
            if (listener != null) {
                mKey = listener.asBinder();
            } else {
                mKey = intent;
            }
            mAllowedResolutionLevel = getAllowedResolutionLevel(pid, uid);
            mUid = uid;
            mPid = pid;
            mPackageName = packageName;
            if (workSource != null && workSource.size() <= 0) {
                workSource = null;
            }
            mWorkSource = workSource;
            mHideFromAppOps = hideFromAppOps;

            updateMonitoring(true);

            // construct/configure wakelock
            mWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKELOCK_KEY);
            if (workSource == null) {
                workSource = new WorkSource(mUid, mPackageName);
            }
            mWakeLock.setWorkSource(workSource);
        }

        @Override
        public boolean equals(Object otherObj) {
            if (otherObj instanceof Receiver) {
                return mKey.equals(((Receiver)otherObj).mKey);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return mKey.hashCode();
        }

        @Override
        public String toString() {
            StringBuilder s = new StringBuilder();
            s.append("Reciever[");
            s.append(Integer.toHexString(System.identityHashCode(this)));
            if (mListener != null) {
                s.append(" listener");
            } else {
                s.append(" intent");
            }
            for (String p : mUpdateRecords.keySet()) {
                s.append(" ").append(mUpdateRecords.get(p).toString());
            }
            s.append("]");
            return s.toString();
        }

        /**
         * Update AppOp monitoring for this receiver.
         *
         * @param allow If true receiver is currently active, if false it's been removed.
         */
        public void updateMonitoring(boolean allow) {
            if (mHideFromAppOps) {
                return;
            }

            boolean requestingLocation = false;
            boolean requestingHighPowerLocation = false;
            if (allow) {
                // See if receiver has any enabled update records.  Also note if any update records
                // are high power (has a high power provider with an interval under a threshold).
                for (UpdateRecord updateRecord : mUpdateRecords.values()) {
                    if (isAllowedByCurrentUserSettingsLocked(updateRecord.mProvider)) {
                        requestingLocation = true;
                        LocationProviderInterface locationProvider
                                = mProvidersByName.get(updateRecord.mProvider);
                        ProviderProperties properties = locationProvider != null
                                ? locationProvider.getProperties() : null;
                        if (properties != null
                                && properties.mPowerRequirement == Criteria.POWER_HIGH
                                && updateRecord.mRequest.getInterval() < HIGH_POWER_INTERVAL_MS) {
                            requestingHighPowerLocation = true;
                            break;
                        }
                    }
                }
            }

            // First update monitoring of any location request (including high power).
            mOpMonitoring = updateMonitoring(
                    requestingLocation,
                    mOpMonitoring,
                    AppOpsManager.OP_MONITOR_LOCATION);

            // Now update monitoring of high power requests only.
            boolean wasHighPowerMonitoring = mOpHighPowerMonitoring;
            mOpHighPowerMonitoring = updateMonitoring(
                    requestingHighPowerLocation,
                    mOpHighPowerMonitoring,
                    AppOpsManager.OP_MONITOR_HIGH_POWER_LOCATION);
            if (mOpHighPowerMonitoring != wasHighPowerMonitoring) {
                // Send an intent to notify that a high power request has been added/removed.
                Intent intent = new Intent(LocationManager.HIGH_POWER_REQUEST_CHANGE_ACTION);
                mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
            }
        }

        /**
         * Update AppOps monitoring for a single location request and op type.
         *
         * @param allowMonitoring True if monitoring is allowed for this request/op.
         * @param currentlyMonitoring True if AppOps is currently monitoring this request/op.
         * @param op AppOps code for the op to update.
         * @return True if monitoring is on for this request/op after updating.
         */
        private boolean updateMonitoring(boolean allowMonitoring, boolean currentlyMonitoring,
                int op) {
            if (!currentlyMonitoring) {
                if (allowMonitoring) {
                    return mAppOps.startOpNoThrow(op, mUid, mPackageName)
                            == AppOpsManager.MODE_ALLOWED;
                }
            } else {
                if (!allowMonitoring || mAppOps.checkOpNoThrow(op, mUid, mPackageName)
                        != AppOpsManager.MODE_ALLOWED) {
                    mAppOps.finishOp(op, mUid, mPackageName);
                    return false;
                }
            }

            return currentlyMonitoring;
        }

        public boolean isListener() {
            return mListener != null;
        }

        public boolean isPendingIntent() {
            return mPendingIntent != null;
        }

        public ILocationListener getListener() {
            if (mListener != null) {
                return mListener;
            }
            throw new IllegalStateException("Request for non-existent listener");
        }

        public boolean callStatusChangedLocked(String provider, int status, Bundle extras) {
            if (mListener != null) {
                try {
                    synchronized (this) {
                        // synchronize to ensure incrementPendingBroadcastsLocked()
                        // is called before decrementPendingBroadcasts()
                        mListener.onStatusChanged(provider, status, extras);
                        // call this after broadcasting so we do not increment
                        // if we throw an exeption.
                        incrementPendingBroadcastsLocked();
                    }
                } catch (RemoteException e) {
                    return false;
                }
            } else {
                Intent statusChanged = new Intent();
                statusChanged.putExtras(new Bundle(extras));
                statusChanged.putExtra(LocationManager.KEY_STATUS_CHANGED, status);
                try {
                    synchronized (this) {
                        // synchronize to ensure incrementPendingBroadcastsLocked()
                        // is called before decrementPendingBroadcasts()
                        mPendingIntent.send(mContext, 0, statusChanged, this, mLocationHandler,
                                getResolutionPermission(mAllowedResolutionLevel));
                        // call this after broadcasting so we do not increment
                        // if we throw an exeption.
                        incrementPendingBroadcastsLocked();
                    }
                } catch (PendingIntent.CanceledException e) {
                    return false;
                }
            }
            return true;
        }

        public boolean callLocationChangedLocked(Location location) {
            Log.d(TAG,"callLocationChangedLocked location:"+location);
            if (mListener != null) {
                try {
                    synchronized (this) {
                        // synchronize to ensure incrementPendingBroadcastsLocked()
                        // is called before decrementPendingBroadcasts()
                        mListener.onLocationChanged(new Location(location));
                        // call this after broadcasting so we do not increment
                        // if we throw an exeption.
                        incrementPendingBroadcastsLocked();
                    }
                } catch (RemoteException e) {
                    return false;
                }
            } else {
                Intent locationChanged = new Intent();
                locationChanged.putExtra(LocationManager.KEY_LOCATION_CHANGED, new Location(location));
                try {
                    synchronized (this) {
                        // synchronize to ensure incrementPendingBroadcastsLocked()
                        // is called before decrementPendingBroadcasts()
                        mPendingIntent.send(mContext, 0, locationChanged, this, mLocationHandler,
                                getResolutionPermission(mAllowedResolutionLevel));
                        // call this after broadcasting so we do not increment
                        // if we throw an exeption.
                        incrementPendingBroadcastsLocked();
                    }
                } catch (PendingIntent.CanceledException e) {
                    return false;
                }
            }
            return true;
        }

        public boolean callProviderEnabledLocked(String provider, boolean enabled) {
            // First update AppOp monitoring.
            // An app may get/lose location access as providers are enabled/disabled.
            updateMonitoring(true);

            if (mListener != null) {
                try {
                    synchronized (this) {
                        // synchronize to ensure incrementPendingBroadcastsLocked()
                        // is called before decrementPendingBroadcasts()
                        if (enabled) {
                            mListener.onProviderEnabled(provider);
                        } else {
                            mListener.onProviderDisabled(provider);
                        }
                        // call this after broadcasting so we do not increment
                        // if we throw an exeption.
                        incrementPendingBroadcastsLocked();
                    }
                } catch (RemoteException e) {
                    return false;
                }
            } else {
                Intent providerIntent = new Intent();
                providerIntent.putExtra(LocationManager.KEY_PROVIDER_ENABLED, enabled);
                try {
                    synchronized (this) {
                        // synchronize to ensure incrementPendingBroadcastsLocked()
                        // is called before decrementPendingBroadcasts()
                        mPendingIntent.send(mContext, 0, providerIntent, this, mLocationHandler,
                                getResolutionPermission(mAllowedResolutionLevel));
                        // call this after broadcasting so we do not increment
                        // if we throw an exeption.
                        incrementPendingBroadcastsLocked();
                    }
                } catch (PendingIntent.CanceledException e) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public void binderDied() {
            if (D) Log.d(TAG, "Location listener died");

            synchronized (mLock) {
                removeUpdatesLocked(this);
            }
            synchronized (this) {
                clearPendingBroadcastsLocked();
            }
        }

        @Override
        public void onSendFinished(PendingIntent pendingIntent, Intent intent,
                int resultCode, String resultData, Bundle resultExtras) {
            synchronized (this) {
                decrementPendingBroadcastsLocked();
            }
        }

        // this must be called while synchronized by caller in a synchronized block
        // containing the sending of the broadcaset
        private void incrementPendingBroadcastsLocked() {
            if (mPendingBroadcasts++ == 0) {
                mWakeLock.acquire();
            }
        }

        private void decrementPendingBroadcastsLocked() {
            if (--mPendingBroadcasts == 0) {
                if (mWakeLock.isHeld()) {
                    mWakeLock.release();
                }
            }
        }

        public void clearPendingBroadcastsLocked() {
            if (mPendingBroadcasts > 0) {
                mPendingBroadcasts = 0;
                if (mWakeLock.isHeld()) {
                    mWakeLock.release();
                }
            }
        }
    }

    @Override
    public void locationCallbackFinished(ILocationListener listener) {
        //Do not use getReceiverLocked here as that will add the ILocationListener to
        //the receiver list if it is not found.  If it is not found then the
        //LocationListener was removed when it had a pending broadcast and should
        //not be added back.
        synchronized (mLock) {
            IBinder binder = listener.asBinder();
            Receiver receiver = mReceivers.get(binder);
            if (receiver != null) {
                synchronized (receiver) {
                    // so wakelock calls will succeed
                    long identity = Binder.clearCallingIdentity();
                    receiver.decrementPendingBroadcastsLocked();
                    Binder.restoreCallingIdentity(identity);
                }
            }
        }
    }

    /**
     * Returns the system information of the GNSS hardware.
     */
    @Override
    public int getGnssYearOfHardware() {
        if (mGnssNavigationMessageProvider != null) {
            return mGnssSystemInfoProvider.getGnssYearOfHardware();
        } else {
            return 0;
        }
    }

    private void addProviderLocked(LocationProviderInterface provider) {
        mProviders.add(provider);
        mProvidersByName.put(provider.getName(), provider);
    }

    private void removeProviderLocked(LocationProviderInterface provider) {
        provider.disable();
        mProviders.remove(provider);
        mProvidersByName.remove(provider.getName());
    }

    /**
     * Returns "true" if access to the specified location provider is allowed by the current
     * user's settings. Access to all location providers is forbidden to non-location-provider
     * processes belonging to background users.
     *
     * @param provider the name of the location provider
     * @return
     */
    private boolean isAllowedByCurrentUserSettingsLocked(String provider) {
        if (mEnabledProviders.contains(provider)) {
            return true;
        }
        if (mDisabledProviders.contains(provider)) {
            return false;
        }
        // Use system settings
        ContentResolver resolver = mContext.getContentResolver();

        return Settings.Secure.isLocationProviderEnabledForUser(resolver, provider, mCurrentUserId);
    }

    /**
     * Returns "true" if access to the specified location provider is allowed by the specified
     * user's settings. Access to all location providers is forbidden to non-location-provider
     * processes belonging to background users.
     *
     * @param provider the name of the location provider
     * @param uid the requestor's UID
     * @return
     */
    private boolean isAllowedByUserSettingsLocked(String provider, int uid) {
        if (!isCurrentProfile(UserHandle.getUserId(uid)) && !isUidALocationProvider(uid)) {
            return false;
        }
        return isAllowedByCurrentUserSettingsLocked(provider);
    }

    /**
     * Returns the permission string associated with the specified resolution level.
     *
     * @param resolutionLevel the resolution level
     * @return the permission string
     */
    private String getResolutionPermission(int resolutionLevel) {
        switch (resolutionLevel) {
            case RESOLUTION_LEVEL_FINE:
                return android.Manifest.permission.ACCESS_FINE_LOCATION;
            case RESOLUTION_LEVEL_COARSE:
                return android.Manifest.permission.ACCESS_COARSE_LOCATION;
            default:
                return null;
        }
    }

    /**
     * Returns the resolution level allowed to the given PID/UID pair.
     *
     * @param pid the PID
     * @param uid the UID
     * @return resolution level allowed to the pid/uid pair
     */
    private int getAllowedResolutionLevel(int pid, int uid) {
        if (mContext.checkPermission(android.Manifest.permission.ACCESS_FINE_LOCATION,
                pid, uid) == PackageManager.PERMISSION_GRANTED
                /// M: add to check AppOps permission
                && checkOp(pid, uid, AppOpsManager.OP_FINE_LOCATION)) {
            //Log.d(TAG, "getAllowedResolutionLevelByPermOnly(pid = " + pid + ", uid = " + uid +
            //      ") =  FINE LOCATION");
            return RESOLUTION_LEVEL_FINE;
        } else if (mContext.checkPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION,
                pid, uid) == PackageManager.PERMISSION_GRANTED
                /// M: add to check AppOps permission
                && checkOp(pid, uid, AppOpsManager.OP_COARSE_LOCATION)) {
            //Log.d(TAG, "getAllowedResolutionLevelByPermOnly(pid = " + pid + ", uid = " + uid +
            //      ") =  COARSE LOCATION");
            return RESOLUTION_LEVEL_COARSE;
        } else {
            return RESOLUTION_LEVEL_NONE;
        }
    }

    /// Mtk add
    private boolean checkOp(int pid, int uid, int op) {
        ///M: if CTA is not enabled, just return true to skip.
        if (!CtaUtils.isCtaSupported()) {
            return true;
        }

        boolean granted = false;
        String[] pkgs = mContext.getPackageManager().getPackagesForUid(uid);

        if (pkgs == null) {
            if (D) Log.d(TAG, "checkOp(pid = " + pid + ", uid = " + uid +
                    ") pkg == null, return false");
            return granted;
        }

        for (String pkg:pkgs) {
            if (mAppOps.checkOpNoThrow(op, uid, pkg) == AppOpsManager.MODE_ALLOWED) {
                //Log.d(TAG, "checkOp(pid = " + pid + ", uid = " + uid + ", pkg = " + pkg
                //    + " op = " + op + ") != Allowed.");
                granted = true;
                break;
            }
        }
        return granted;
    }
    /// Mtk end

    /**
     * Returns the resolution level allowed to the caller
     *
     * @return resolution level allowed to caller
     */
    private int getCallerAllowedResolutionLevel() {
        return getAllowedResolutionLevel(Binder.getCallingPid(), Binder.getCallingUid());
    }

    /**
     * Throw SecurityException if specified resolution level is insufficient to use geofences.
     *
     * @param allowedResolutionLevel resolution level allowed to caller
     */
    private void checkResolutionLevelIsSufficientForGeofenceUse(int allowedResolutionLevel) {
        if (allowedResolutionLevel < RESOLUTION_LEVEL_FINE) {
            throw new SecurityException("Geofence usage requires ACCESS_FINE_LOCATION permission");
        }
    }

    /**
     * Return the minimum resolution level required to use the specified location provider.
     *
     * @param provider the name of the location provider
     * @return minimum resolution level required for provider
     */
    private int getMinimumResolutionLevelForProviderUse(String provider) {
        if (LocationManager.GPS_PROVIDER.equals(provider) ||
                LocationManager.PASSIVE_PROVIDER.equals(provider)) {
            // gps and passive providers require FINE permission
            return RESOLUTION_LEVEL_FINE;
        } else if (LocationManager.NETWORK_PROVIDER.equals(provider) ||
                LocationManager.FUSED_PROVIDER.equals(provider)) {
            // network and fused providers are ok with COARSE or FINE
            return RESOLUTION_LEVEL_COARSE;
        } else {
            // mock providers
            LocationProviderInterface lp = mMockProviders.get(provider);
            if (lp != null) {
                ProviderProperties properties = lp.getProperties();
                if (properties != null) {
                    if (properties.mRequiresSatellite) {
                        // provider requiring satellites require FINE permission
                        return RESOLUTION_LEVEL_FINE;
                    } else if (properties.mRequiresNetwork || properties.mRequiresCell) {
                        // provider requiring network and or cell require COARSE or FINE
                        return RESOLUTION_LEVEL_COARSE;
                    }
                }
            }
        }
        return RESOLUTION_LEVEL_FINE; // if in doubt, require FINE
    }

    /**
     * Throw SecurityException if specified resolution level is insufficient to use the named
     * location provider.
     *
     * @param allowedResolutionLevel resolution level allowed to caller
     * @param providerName the name of the location provider
     */
    private void checkResolutionLevelIsSufficientForProviderUse(int allowedResolutionLevel,
            String providerName) {
        int requiredResolutionLevel = getMinimumResolutionLevelForProviderUse(providerName);
        if (allowedResolutionLevel < requiredResolutionLevel) {
            switch (requiredResolutionLevel) {
                case RESOLUTION_LEVEL_FINE:
                    throw new SecurityException("\"" + providerName + "\" location provider " +
                            "requires ACCESS_FINE_LOCATION permission.");
                case RESOLUTION_LEVEL_COARSE:
                    throw new SecurityException("\"" + providerName + "\" location provider " +
                            "requires ACCESS_COARSE_LOCATION or ACCESS_FINE_LOCATION permission.");
                default:
                    throw new SecurityException("Insufficient permission for \"" + providerName +
                            "\" location provider.");
            }
        }
    }

    /**
     * Throw SecurityException if WorkSource use is not allowed (i.e. can't blame other packages
     * for battery).
     */
    private void checkDeviceStatsAllowed() {
        mContext.enforceCallingOrSelfPermission(
                android.Manifest.permission.UPDATE_DEVICE_STATS, null);
    }

    private void checkUpdateAppOpsAllowed() {
        mContext.enforceCallingOrSelfPermission(
                android.Manifest.permission.UPDATE_APP_OPS_STATS, null);
    }

    public static int resolutionLevelToOp(int allowedResolutionLevel) {
        if (allowedResolutionLevel != RESOLUTION_LEVEL_NONE) {
            if (allowedResolutionLevel == RESOLUTION_LEVEL_COARSE) {
                return AppOpsManager.OP_COARSE_LOCATION;
            } else {
                return AppOpsManager.OP_FINE_LOCATION;
            }
        }
        return -1;
    }

    boolean reportLocationAccessNoThrow(
            int pid, int uid, String packageName, int allowedResolutionLevel) {
        int op = resolutionLevelToOp(allowedResolutionLevel);
        if (op >= 0) {
            if (mAppOps.noteOpNoThrow(op, uid, packageName) != AppOpsManager.MODE_ALLOWED) {
                /// Mtk add to check AppOps permission
                if (CtaUtils.isCtaSupported()) {
                    if (op == AppOpsManager.OP_FINE_LOCATION) {
                        if (mAppOps.noteOpNoThrow(AppOpsManager.OP_COARSE_LOCATION, uid,
                            packageName) != AppOpsManager.MODE_ALLOWED) {
                            if (D) Log.d(TAG, "reportLocationAccessNoThrow(op = OP_COARSE_LOCATION"
                                    + ", uid = " + uid + ", pkg = " + packageName + ") != ALLOWED");
                            return false;
                        }
                    } else {
                        if (D) Log.d(TAG, "reportLocationAccessNoThrow(op!=OP_COARSE_LOCATION)"
                                + " returns false");
                        return false;
                    }
                } else {
                    return false;
                }
                /// Mtk end
            }
        }

        if (getAllowedResolutionLevel(pid, uid) < allowedResolutionLevel) {
            /// Mtk added log
            if (D) Log.d(TAG, "reportLocationAccessNoThrow() - getAllowedResolutionLevel(pid=,"
                 + pid + "uid = " + uid + ") = " + getAllowedResolutionLevel(pid, uid) +
                " < allowedResolutionLevel = " + allowedResolutionLevel);
            return false;
        }

        return true;
    }

    boolean checkLocationAccess(int pid, int uid, String packageName, int allowedResolutionLevel) {
        int op = resolutionLevelToOp(allowedResolutionLevel);
        if (op >= 0) {
            if (mAppOps.checkOp(op, uid, packageName) != AppOpsManager.MODE_ALLOWED) {
                /// Mtk add to check AppOps permission
                if (CtaUtils.isCtaSupported()) {
                    if (op == AppOpsManager.OP_FINE_LOCATION) {
                        if (mAppOps.checkOp(AppOpsManager.OP_COARSE_LOCATION, uid, packageName)
                                != AppOpsManager.MODE_ALLOWED) {
                            if (D) Log.d(TAG, "checkLocationAccess(op = OP_COARSE_LOCATION" +
                                    " , uid = " + uid + ", pkg = " + packageName + ") != ALLOWED");
                            return false;
                        }
                    } else {
                        if (D) Log.d(TAG, "checkLocationAccess(op!=OP_COARSE_LOCATION)"
                                + " returns false");
                        return false;
                    }
                } else {
                    return false;
                }
                /// Mtk end
            }
        }

        if (getAllowedResolutionLevel(pid, uid) < allowedResolutionLevel) {
            /// Mtk added log
            if (D) Log.d(TAG, "checkLocationAccess() - getAllowedResolutionLevel(pid=," + pid
                + "uid = " + uid + ") = " + getAllowedResolutionLevel(pid, uid) +
                " < allowedResolutionLevel = " + allowedResolutionLevel);
            return false;
        }

        return true;
    }

    /**
     * Returns all providers by name, including passive, but excluding
     * fused, also including ones that are not permitted to
     * be accessed by the calling activity or are currently disabled.
     */
    @Override
    public List<String> getAllProviders() {
        ArrayList<String> out;
        synchronized (mLock) {
            out = new ArrayList<String>(mProviders.size());
            for (LocationProviderInterface provider : mProviders) {
                String name = provider.getName();
                if (LocationManager.FUSED_PROVIDER.equals(name)) {
                    continue;
                }
                out.add(name);
            }
        }

        if (D) Log.d(TAG, "getAllProviders()=" + out);
        return out;
    }

    /**
     * Return all providers by name, that match criteria and are optionally
     * enabled.
     * Can return passive provider, but never returns fused provider.
     */
    @Override
    public List<String> getProviders(Criteria criteria, boolean enabledOnly) {
        int allowedResolutionLevel = getCallerAllowedResolutionLevel();
        ArrayList<String> out;
        int uid = Binder.getCallingUid();;
        long identity = Binder.clearCallingIdentity();
        try {
            synchronized (mLock) {
                out = new ArrayList<String>(mProviders.size());
                for (LocationProviderInterface provider : mProviders) {
                    String name = provider.getName();
                    if (LocationManager.FUSED_PROVIDER.equals(name)) {
                        continue;
                    }
                    if (allowedResolutionLevel >= getMinimumResolutionLevelForProviderUse(name)) {
                        if (enabledOnly && !isAllowedByUserSettingsLocked(name, uid)) {
                            continue;
                        }
                        if (criteria != null && !LocationProvider.propertiesMeetCriteria(
                                name, provider.getProperties(), criteria)) {
                            continue;
                        }
                        out.add(name);
                    }
                }
            }
        } finally {
            Binder.restoreCallingIdentity(identity);
        }

        if (D) Log.d(TAG, "getProviders()=" + out);
        return out;
    }

    /**
     * Return the name of the best provider given a Criteria object.
     * This method has been deprecated from the public API,
     * and the whole LocationProvider (including #meetsCriteria)
     * has been deprecated as well. So this method now uses
     * some simplified logic.
     */
    @Override
    public String getBestProvider(Criteria criteria, boolean enabledOnly) {
        String result = null;

        List<String> providers = getProviders(criteria, enabledOnly);
        if (!providers.isEmpty()) {
            result = pickBest(providers);
            if (D) Log.d(TAG, "getBestProvider(" + criteria + ", " + enabledOnly + ")=" + result);
            return result;
        }
        providers = getProviders(null, enabledOnly);
        if (!providers.isEmpty()) {
            result = pickBest(providers);
            if (D) Log.d(TAG, "getBestProvider(" + criteria + ", " + enabledOnly + ")=" + result);
            return result;
        }

        if (D) Log.d(TAG, "getBestProvider(" + criteria + ", " + enabledOnly + ")=" + result);
        return null;
    }

    private String pickBest(List<String> providers) {
        if (providers.contains(LocationManager.GPS_PROVIDER)) {
            return LocationManager.GPS_PROVIDER;
        } else if (providers.contains(LocationManager.NETWORK_PROVIDER)) {
            return LocationManager.NETWORK_PROVIDER;
        } else {
            return providers.get(0);
        }
    }

    @Override
    public boolean providerMeetsCriteria(String provider, Criteria criteria) {
        LocationProviderInterface p = mProvidersByName.get(provider);
        if (p == null) {
            throw new IllegalArgumentException("provider=" + provider);
        }

        boolean result = LocationProvider.propertiesMeetCriteria(
                p.getName(), p.getProperties(), criteria);
        if (D) Log.d(TAG, "providerMeetsCriteria(" + provider + ", " + criteria + ")=" + result);
        return result;
    }

    private void updateProvidersLocked() {
        boolean changesMade = false;
        for (int i = mProviders.size() - 1; i >= 0; i--) {
            LocationProviderInterface p = mProviders.get(i);
            boolean isEnabled = p.isEnabled();
            String name = p.getName();
            boolean shouldBeEnabled = isAllowedByCurrentUserSettingsLocked(name);
            if (isEnabled && !shouldBeEnabled) {
                updateProviderListenersLocked(name, false);
                // If any provider has been disabled, clear all last locations for all providers.
                // This is to be on the safe side in case a provider has location derived from
                // this disabled provider.
                mLastLocation.clear();
                mLastLocationCoarseInterval.clear();
                changesMade = true;
            } else if (!isEnabled && shouldBeEnabled) {
                updateProviderListenersLocked(name, true);
                changesMade = true;
            // M: Exit whitelist only mode after provider enabled
            } else if (shouldBeEnabled && isWhitelistWorkingMode(name)) {
                mWhitelistWorkingMode = false;
                Log.d(TAG, "Exit whitelist only mode, skip provider enabling");
            }
            Log.d(TAG, "updateProvidersLocked provider:" + name + " changesMade: " + changesMade
                    + " isEnabled:" + isEnabled + " shouldBeEnabled:" + shouldBeEnabled);
        }
        if (changesMade) {
            mContext.sendBroadcastAsUser(new Intent(LocationManager.PROVIDERS_CHANGED_ACTION),
                    UserHandle.ALL);
            mContext.sendBroadcastAsUser(new Intent(LocationManager.MODE_CHANGED_ACTION),
                    UserHandle.ALL);
        }
    }

    private void updateProviderListenersLocked(String provider, boolean enabled) {
        int listeners = 0;

        LocationProviderInterface p = mProvidersByName.get(provider);
        if (p == null) return;

        ArrayList<Receiver> deadReceivers = null;

        ArrayList<UpdateRecord> records = mRecordsByProvider.get(provider);
        if (records != null) {
            final int N = records.size();
            for (int i = 0; i < N; i++) {
                UpdateRecord record = records.get(i);
                if (isCurrentProfile(UserHandle.getUserId(record.mReceiver.mUid))) {
                    // Sends a notification message to the receiver
                    if (!record.mReceiver.callProviderEnabledLocked(provider, enabled)) {
                        if (deadReceivers == null) {
                            deadReceivers = new ArrayList<Receiver>();
                        }
                        deadReceivers.add(record.mReceiver);
                    }
                    listeners++;
                }
            }
        }

        if (deadReceivers != null) {
            for (int i = deadReceivers.size() - 1; i >= 0; i--) {
                removeUpdatesLocked(deadReceivers.get(i));
            }
        }

        if (enabled) {
            p.enable();
            if (listeners > 0) {
                applyRequirementsLocked(provider);
            }
        } else {
            // M: Eenter whitelist only mode if whitelist package has registered
            if (isWhitelistFeatureSupport() && mWhitelistProvider.equals(provider)
                    && isProviderRecordsContainsWhilelistPackage(provider)) {
                Log.d(TAG, "Enter white list only mode, skip provider disabling");
                mWhitelistWorkingMode = true;
            } else {
                p.disable();
            }
        }
    }

    private void applyRequirementsLocked(String provider) {
        LocationProviderInterface p = mProvidersByName.get(provider);
        if (p == null) return;

        ArrayList<UpdateRecord> records = mRecordsByProvider.get(provider);
        WorkSource worksource = new WorkSource();
        ProviderRequest providerRequest = new ProviderRequest();

        if (records != null) {
            for (UpdateRecord record : records) {
                if (isCurrentProfile(UserHandle.getUserId(record.mReceiver.mUid))) {
                    if (checkLocationAccess(
                            record.mReceiver.mPid,
                            record.mReceiver.mUid,
                            record.mReceiver.mPackageName,
                            record.mReceiver.mAllowedResolutionLevel)) {
                        LocationRequest locationRequest = record.mRequest;
                        providerRequest.locationRequests.add(locationRequest);
                        if (locationRequest.getInterval() < providerRequest.interval) {
                            providerRequest.reportLocation = true;
                            providerRequest.interval = locationRequest.getInterval();
                        }
                    }
                }
            }

            if (providerRequest.reportLocation) {
                // calculate who to blame for power
                // This is somewhat arbitrary. We pick a threshold interval
                // that is slightly higher that the minimum interval, and
                // spread the blame across all applications with a request
                // under that threshold.
                long thresholdInterval = (providerRequest.interval + 1000) * 3 / 2;
                for (UpdateRecord record : records) {
                    if (isCurrentProfile(UserHandle.getUserId(record.mReceiver.mUid))) {
                        LocationRequest locationRequest = record.mRequest;

                        // Don't assign battery blame for update records whose
                        // client has no permission to receive location data.
                        if (!providerRequest.locationRequests.contains(locationRequest)) {
                            continue;
                        }

                        if (locationRequest.getInterval() <= thresholdInterval) {
                            if (record.mReceiver.mWorkSource != null
                                    && record.mReceiver.mWorkSource.size() > 0
                                    && record.mReceiver.mWorkSource.getName(0) != null) {
                                // Assign blame to another work source.
                                // Can only assign blame if the WorkSource contains names.
                                worksource.add(record.mReceiver.mWorkSource);
                            } else {
                                // Assign blame to caller.
                                worksource.add(
                                        record.mReceiver.mUid,
                                        record.mReceiver.mPackageName);
                            }
                        }
                    }
                }
            }
        }

        if (D) Log.d(TAG, "provider request: " + provider + " " + providerRequest);
        p.setRequest(providerRequest, worksource);
    }

    private class UpdateRecord {
        final String mProvider;
        final LocationRequest mRequest;
        final Receiver mReceiver;
        Location mLastFixBroadcast;
        long mLastStatusBroadcast;

        /**
         * Note: must be constructed with lock held.
         */
        UpdateRecord(String provider, LocationRequest request, Receiver receiver) {
            mProvider = provider;
            mRequest = request;
            mReceiver = receiver;

            ArrayList<UpdateRecord> records = mRecordsByProvider.get(provider);
            if (records == null) {
                records = new ArrayList<UpdateRecord>();
                mRecordsByProvider.put(provider, records);
            }
            if (!records.contains(this)) {
                /*-prize-lihuangyuan add for gps monitor-2018-06-22-start*/
                if(provider.equals("gps") && records.size() <= 0)
                {
                    Message m = Message.obtain(mLocationHandler, MSG_REQUEST_GPS_PROVIDER);
                    mLocationHandler.sendMessage(m);
                }
                /*-prize-lihuangyuan add for gps monitor-2018-06-22-end*/
                records.add(this);
            }

            // Update statistics for historical location requests by package/provider
            mRequestStatistics.startRequesting(
                    mReceiver.mPackageName, provider, request.getInterval());
        }

        /**
         * Method to be called when a record will no longer be used.
         */
        void disposeLocked(boolean removeReceiver) {
            mRequestStatistics.stopRequesting(mReceiver.mPackageName, mProvider);

            // remove from mRecordsByProvider
            ArrayList<UpdateRecord> globalRecords = mRecordsByProvider.get(this.mProvider);
            if (globalRecords != null) {
                globalRecords.remove(this);
                /*-prize-lihuangyuan add for gps monitor-2018-06-22-start*/
                if(mProvider.equals("gps") && globalRecords.size() <= 0)
                {
                    Message m = Message.obtain(mLocationHandler, MSG_REMOVE_GPS_PROVIDER);
                    mLocationHandler.sendMessage(m);
                }
                /*-prize-lihuangyuan add for gps monitor-2018-06-22-end*/
            }

            if (!removeReceiver) return;  // the caller will handle the rest

            // remove from Receiver#mUpdateRecords
            HashMap<String, UpdateRecord> receiverRecords = mReceiver.mUpdateRecords;
            if (receiverRecords != null) {
                receiverRecords.remove(this.mProvider);

                // and also remove the Receiver if it has no more update records
                if (removeReceiver && receiverRecords.size() == 0) {
                    removeUpdatesLocked(mReceiver);
                }
            }
        }

        @Override
        public String toString() {
            StringBuilder s = new StringBuilder();
            s.append("UpdateRecord[");
            s.append(mProvider);
            s.append(' ').append(mReceiver.mPackageName).append('(');
            s.append(mReceiver.mUid).append(')');
            s.append(' ').append(mRequest);
            s.append(']');
            return s.toString();
        }
    }

    private Receiver getReceiverLocked(ILocationListener listener, int pid, int uid,
            String packageName, WorkSource workSource, boolean hideFromAppOps) {
        IBinder binder = listener.asBinder();
        Receiver receiver = mReceivers.get(binder);
        if (receiver == null) {
            receiver = new Receiver(listener, null, pid, uid, packageName, workSource,
                    hideFromAppOps);
            try {
                receiver.getListener().asBinder().linkToDeath(receiver, 0);
            } catch (RemoteException e) {
                Slog.e(TAG, "linkToDeath failed:", e);
                return null;
            }
            mReceivers.put(binder, receiver);
        }
        return receiver;
    }

    private Receiver getReceiverLocked(PendingIntent intent, int pid, int uid, String packageName,
            WorkSource workSource, boolean hideFromAppOps) {
        Receiver receiver = mReceivers.get(intent);
        if (receiver == null) {
            receiver = new Receiver(null, intent, pid, uid, packageName, workSource,
                    hideFromAppOps);
            mReceivers.put(intent, receiver);
        }
        return receiver;
    }

    /**
     * Creates a LocationRequest based upon the supplied LocationRequest that to meets resolution
     * and consistency requirements.
     *
     * @param request the LocationRequest from which to create a sanitized version
     * @return a version of request that meets the given resolution and consistency requirements
     * @hide
     */
    private LocationRequest createSanitizedRequest(LocationRequest request, int resolutionLevel) {
        LocationRequest sanitizedRequest = new LocationRequest(request);
        if (resolutionLevel < RESOLUTION_LEVEL_FINE) {
            switch (sanitizedRequest.getQuality()) {
                case LocationRequest.ACCURACY_FINE:
                    sanitizedRequest.setQuality(LocationRequest.ACCURACY_BLOCK);
                    break;
                case LocationRequest.POWER_HIGH:
                    sanitizedRequest.setQuality(LocationRequest.POWER_LOW);
                    break;
            }
            // throttle
            if (sanitizedRequest.getInterval() < LocationFudger.FASTEST_INTERVAL_MS) {
                sanitizedRequest.setInterval(LocationFudger.FASTEST_INTERVAL_MS);
            }
            if (sanitizedRequest.getFastestInterval() < LocationFudger.FASTEST_INTERVAL_MS) {
                sanitizedRequest.setFastestInterval(LocationFudger.FASTEST_INTERVAL_MS);
            }
        }
        // make getFastestInterval() the minimum of interval and fastest interval
        if (sanitizedRequest.getFastestInterval() > sanitizedRequest.getInterval()) {
            request.setFastestInterval(request.getInterval());
        }
        return sanitizedRequest;
    }

    private void checkPackageName(String packageName) {
        if (packageName == null) {
            throw new SecurityException("invalid package name: " + packageName);
        }
        int uid = Binder.getCallingUid();
        String[] packages = mPackageManager.getPackagesForUid(uid);
        if (packages == null) {
            throw new SecurityException("invalid UID " + uid);
        }
        for (String pkg : packages) {
            if (packageName.equals(pkg)) return;
        }
        throw new SecurityException("invalid package name: " + packageName);
    }

    private void checkPendingIntent(PendingIntent intent) {
        if (intent == null) {
            throw new IllegalArgumentException("invalid pending intent: " + intent);
        }
    }

    private Receiver checkListenerOrIntentLocked(ILocationListener listener, PendingIntent intent,
            int pid, int uid, String packageName, WorkSource workSource, boolean hideFromAppOps) {
        if (intent == null && listener == null) {
            throw new IllegalArgumentException("need either listener or intent");
        } else if (intent != null && listener != null) {
            throw new IllegalArgumentException("cannot register both listener and intent");
        } else if (intent != null) {
            checkPendingIntent(intent);
            return getReceiverLocked(intent, pid, uid, packageName, workSource, hideFromAppOps);
        } else {
            return getReceiverLocked(listener, pid, uid, packageName, workSource, hideFromAppOps);
        }
    }

    @Override
    public void requestLocationUpdates(LocationRequest request, ILocationListener listener,
            PendingIntent intent, String packageName) {
        if (request == null) request = DEFAULT_LOCATION_REQUEST;
        checkPackageName(packageName);
        int allowedResolutionLevel = getCallerAllowedResolutionLevel();
        checkResolutionLevelIsSufficientForProviderUse(allowedResolutionLevel,
                request.getProvider());
        WorkSource workSource = request.getWorkSource();
        if (workSource != null && workSource.size() > 0) {
            checkDeviceStatsAllowed();
        }
        boolean hideFromAppOps = request.getHideFromAppOps();
        if (hideFromAppOps) {
            checkUpdateAppOpsAllowed();
        }
        LocationRequest sanitizedRequest = createSanitizedRequest(request, allowedResolutionLevel);

        final int pid = Binder.getCallingPid();
        final int uid = Binder.getCallingUid();

        // providers may use public location API's, need to clear identity
        long identity = Binder.clearCallingIdentity();
        try {
            // We don't check for MODE_IGNORED here; we will do that when we go to deliver
            // a location.
            checkLocationAccess(pid, uid, packageName, allowedResolutionLevel);

            synchronized (mLock) {
                Receiver recevier = checkListenerOrIntentLocked(listener, intent, pid, uid,
                        packageName, workSource, hideFromAppOps);
                /// M: recevier may be null
                if (recevier != null) {
                    requestLocationUpdatesLocked(sanitizedRequest, recevier, pid, uid, packageName);
                }
            }
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    private void requestLocationUpdatesLocked(LocationRequest request, Receiver receiver,
            int pid, int uid, String packageName) {
        // Figure out the provider. Either its explicitly request (legacy use cases), or
        // use the fused provider
        if (request == null) request = DEFAULT_LOCATION_REQUEST;
        String name = request.getProvider();
        if (name == null) {
            throw new IllegalArgumentException("provider name must not be null");
        }

        Log.d(TAG, "request " + Integer.toHexString(System.identityHashCode(receiver))
                + " " + name + " " + request + " from " + packageName + "(" + uid + ")");
        LocationProviderInterface provider = mProvidersByName.get(name);
        if (provider == null) {
            throw new IllegalArgumentException("provider doesn't exist: " + name);
        }

        UpdateRecord record = new UpdateRecord(name, request, receiver);
        UpdateRecord oldRecord = receiver.mUpdateRecords.put(name, record);
        if (oldRecord != null) {
            oldRecord.disposeLocked(false);
        }

        boolean isProviderEnabled = isAllowedByUserSettingsLocked(name, uid);
        if (isProviderEnabled) {
            applyRequirementsLocked(name);
        } else {
            // M: Whitelist pakcage request locations when provider is disabled
            if (isWhitelistFeatureSupport() && mWhitelistPackage.equals(packageName)
                    && mWhitelistProvider.equals(name)) {
                if (!mWhitelistWorkingMode) {
                    Log.d(TAG, "Enable provider first when first whitlist package requested");
                    provider.enable();
                    mContext.sendBroadcastAsUser(
                            new Intent(LocationManager.PROVIDERS_CHANGED_ACTION), UserHandle.ALL);
                    mContext.sendBroadcastAsUser(new Intent(LocationManager.MODE_CHANGED_ACTION),
                            UserHandle.ALL);
                }
                mWhitelistWorkingMode = true;
                applyRequirementsLocked(name);
            } else {
                // Notify the listener that updates are currently disabled
                receiver.callProviderEnabledLocked(name, false);
            }
        }
        // Update the monitoring here just in case multiple location requests were added to the
        // same receiver (this request may be high power and the initial might not have been).
        receiver.updateMonitoring(true);
    }

    @Override
    public void removeUpdates(ILocationListener listener, PendingIntent intent,
            String packageName) {
        checkPackageName(packageName);

        final int pid = Binder.getCallingPid();
        final int uid = Binder.getCallingUid();

        synchronized (mLock) {
            WorkSource workSource = null;
            boolean hideFromAppOps = false;
            Receiver receiver = checkListenerOrIntentLocked(listener, intent, pid, uid,
                    packageName, workSource, hideFromAppOps);

            // providers may use public location API's, need to clear identity
            long identity = Binder.clearCallingIdentity();
            try {
                /// M: receiver may be null
                if (receiver != null) {
                    removeUpdatesLocked(receiver);
                }
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }
    }

    private void removeUpdatesLocked(Receiver receiver) {
        Log.i(TAG, "remove " + Integer.toHexString(System.identityHashCode(receiver)));

        if (mReceivers.remove(receiver.mKey) != null && receiver.isListener()) {
            receiver.getListener().asBinder().unlinkToDeath(receiver, 0);
            synchronized (receiver) {
                receiver.clearPendingBroadcastsLocked();
            }
        }

        receiver.updateMonitoring(false);

        // Record which providers were associated with this listener
        HashSet<String> providers = new HashSet<String>();
        HashMap<String, UpdateRecord> oldRecords = receiver.mUpdateRecords;
        if (oldRecords != null) {
            // Call dispose() on the obsolete update records.
            for (UpdateRecord record : oldRecords.values()) {
                // Update statistics for historical location requests by package/provider
                record.disposeLocked(false);
            }
            // Accumulate providers
            providers.addAll(oldRecords.keySet());
        }

        // update provider
        for (String provider : providers) {
            // If provider is already disabled, don't need to do anything
            if (!isAllowedByCurrentUserSettingsLocked(provider)) {
                /// M: disable provider when exit whitelist working mode
                if (isWhitelistWorkingMode(provider)) {
                    if (disableProviderWhenNoWhitelistPackageRegistered(provider)) {
                        continue; // skip updating requirements
                    }
                } else {
                    continue;
                }
            }

            applyRequirementsLocked(provider);
        }
    }

    private void applyAllProviderRequirementsLocked() {
        for (LocationProviderInterface p : mProviders) {
            // If provider is already disabled, don't need to do anything
            if (!isAllowedByCurrentUserSettingsLocked(p.getName())) {
                continue;
            }

            applyRequirementsLocked(p.getName());
        }
    }

    @Override
    public Location getLastLocation(LocationRequest request, String packageName) {
        if (D) Log.d(TAG, "getLastLocation: " + request);
        if (request == null) request = DEFAULT_LOCATION_REQUEST;
        int allowedResolutionLevel = getCallerAllowedResolutionLevel();
        checkPackageName(packageName);
        checkResolutionLevelIsSufficientForProviderUse(allowedResolutionLevel,
                request.getProvider());
        // no need to sanitize this request, as only the provider name is used

        final int pid = Binder.getCallingPid();
        final int uid = Binder.getCallingUid();
        final long identity = Binder.clearCallingIdentity();
        try {
            if (mBlacklist.isBlacklisted(packageName)) {
                if (D) Log.d(TAG, "not returning last loc for blacklisted app: " +
                        packageName);
                return null;
            }

            if (!reportLocationAccessNoThrow(pid, uid, packageName, allowedResolutionLevel)) {
                if (D) Log.d(TAG, "not returning last loc for no op app: " +
                        packageName);
                return null;
            }

            synchronized (mLock) {
                // Figure out the provider. Either its explicitly request (deprecated API's),
                // or use the fused provider
                String name = request.getProvider();
                if (name == null) name = LocationManager.FUSED_PROVIDER;
                LocationProviderInterface provider = mProvidersByName.get(name);
                if (provider == null) return null;

                if (!isAllowedByUserSettingsLocked(name, uid)) return null;

                Location location;
                if (allowedResolutionLevel < RESOLUTION_LEVEL_FINE) {
                    // Make sure that an app with coarse permissions can't get frequent location
                    // updates by calling LocationManager.getLastKnownLocation repeatedly.
                    location = mLastLocationCoarseInterval.get(name);
                } else {
                    location = mLastLocation.get(name);
                }
                if (location == null) {
                    return null;
                }
                if (allowedResolutionLevel < RESOLUTION_LEVEL_FINE) {
                    Location noGPSLocation = location.getExtraLocation(Location.EXTRA_NO_GPS_LOCATION);
                    if (noGPSLocation != null) {
                        return new Location(mLocationFudger.getOrCreate(noGPSLocation));
                    }
                } else {
                    return new Location(location);
                }
            }
            return null;
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    @Override
    public void requestGeofence(LocationRequest request, Geofence geofence, PendingIntent intent,
            String packageName) {
        if (request == null) request = DEFAULT_LOCATION_REQUEST;
        int allowedResolutionLevel = getCallerAllowedResolutionLevel();
        checkResolutionLevelIsSufficientForGeofenceUse(allowedResolutionLevel);
        checkPendingIntent(intent);
        checkPackageName(packageName);
        checkResolutionLevelIsSufficientForProviderUse(allowedResolutionLevel,
                request.getProvider());
        LocationRequest sanitizedRequest = createSanitizedRequest(request, allowedResolutionLevel);

        if (D) Log.d(TAG, "requestGeofence: " + sanitizedRequest + " " + geofence + " " + intent);

        // geo-fence manager uses the public location API, need to clear identity
        int uid = Binder.getCallingUid();
        // TODO: http://b/23822629
        if (UserHandle.getUserId(uid) != UserHandle.USER_SYSTEM) {
            // temporary measure until geofences work for secondary users
            Log.w(TAG, "proximity alerts are currently available only to the primary user");
            return;
        }
        long identity = Binder.clearCallingIdentity();
        try {
            mGeofenceManager.addFence(sanitizedRequest, geofence, intent, allowedResolutionLevel,
                    uid, packageName);
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    @Override
    public void removeGeofence(Geofence geofence, PendingIntent intent, String packageName) {
        checkPendingIntent(intent);
        checkPackageName(packageName);

        if (D) Log.d(TAG, "removeGeofence: " + geofence + " " + intent);

        // geo-fence manager uses the public location API, need to clear identity
        long identity = Binder.clearCallingIdentity();
        try {
            mGeofenceManager.removeFence(geofence, intent);
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }


    @Override
    public boolean registerGnssStatusCallback(IGnssStatusListener callback, String packageName) {
        int allowedResolutionLevel = getCallerAllowedResolutionLevel();
        checkResolutionLevelIsSufficientForProviderUse(allowedResolutionLevel,
                LocationManager.GPS_PROVIDER);

        final int pid = Binder.getCallingPid();
        final int uid = Binder.getCallingUid();
        final long ident = Binder.clearCallingIdentity();
        try {
            if (!checkLocationAccess(pid, uid, packageName, allowedResolutionLevel)) {
                return false;
            }
        } finally {
            Binder.restoreCallingIdentity(ident);
        }

        if (mGnssStatusProvider == null) {
            return false;
        }

        try {
            mGnssStatusProvider.registerGnssStatusCallback(callback);
            /// M: add debug log to show listener packages
            Log.d(TAG, "registerGnssStatusCallback by package: " + packageName);
        } catch (RemoteException e) {
            Slog.e(TAG, "mGpsStatusProvider.registerGnssStatusCallback failed", e);
            return false;
        }
        return true;
    }

    @Override
    public void unregisterGnssStatusCallback(IGnssStatusListener callback) {
        synchronized (mLock) {
            try {
                mGnssStatusProvider.unregisterGnssStatusCallback(callback);
                /// M: add debug log trace gnss status callback
                Log.d(TAG, "unregisterGnssStatusCallback");
            } catch (Exception e) {
                Slog.e(TAG, "mGpsStatusProvider.unregisterGnssStatusCallback failed", e);
            }
        }
    }

    @Override
    public boolean addGnssMeasurementsListener(
            IGnssMeasurementsListener listener,
            String packageName) {
        int allowedResolutionLevel = getCallerAllowedResolutionLevel();
        checkResolutionLevelIsSufficientForProviderUse(
                allowedResolutionLevel,
                LocationManager.GPS_PROVIDER);

        int pid = Binder.getCallingPid();
        int uid = Binder.getCallingUid();
        long identity = Binder.clearCallingIdentity();
        boolean hasLocationAccess;
        try {
            hasLocationAccess = checkLocationAccess(pid, uid, packageName, allowedResolutionLevel);
        } finally {
            Binder.restoreCallingIdentity(identity);
        }

        if (!hasLocationAccess || mGnssMeasurementsProvider == null) {
            return false;
        }
        return mGnssMeasurementsProvider.addListener(listener);
    }

    @Override
    public void removeGnssMeasurementsListener(IGnssMeasurementsListener listener) {
        if (mGnssMeasurementsProvider != null) {
            mGnssMeasurementsProvider.removeListener(listener);
        }
    }

    @Override
    public boolean addGnssNavigationMessageListener(
            IGnssNavigationMessageListener listener,
            String packageName) {
        int allowedResolutionLevel = getCallerAllowedResolutionLevel();
        checkResolutionLevelIsSufficientForProviderUse(
                allowedResolutionLevel,
                LocationManager.GPS_PROVIDER);

        int pid = Binder.getCallingPid();
        int uid = Binder.getCallingUid();
        long identity = Binder.clearCallingIdentity();
        boolean hasLocationAccess;
        try {
            hasLocationAccess = checkLocationAccess(pid, uid, packageName, allowedResolutionLevel);
        } finally {
            Binder.restoreCallingIdentity(identity);
        }

        if (!hasLocationAccess || mGnssNavigationMessageProvider == null) {
            return false;
        }
        return mGnssNavigationMessageProvider.addListener(listener);
    }

    @Override
    public void removeGnssNavigationMessageListener(IGnssNavigationMessageListener listener) {
        if (mGnssNavigationMessageProvider != null) {
            mGnssNavigationMessageProvider.removeListener(listener);
        }
    }

    @Override
    public boolean sendExtraCommand(String provider, String command, Bundle extras) {
        if (provider == null) {
            // throw NullPointerException to remain compatible with previous implementation
            throw new NullPointerException();
        }
        checkResolutionLevelIsSufficientForProviderUse(getCallerAllowedResolutionLevel(),
                provider);

        // and check for ACCESS_LOCATION_EXTRA_COMMANDS
        if ((mContext.checkCallingOrSelfPermission(ACCESS_LOCATION_EXTRA_COMMANDS)
                != PackageManager.PERMISSION_GRANTED)) {
            throw new SecurityException("Requires ACCESS_LOCATION_EXTRA_COMMANDS permission");
        }

        synchronized (mLock) {
            LocationProviderInterface p = mProvidersByName.get(provider);
            if (p == null) return false;

            return p.sendExtraCommand(command, extras);
        }
    }

    @Override
    public boolean sendNiResponse(int notifId, int userResponse) {
        if (Binder.getCallingUid() != Process.myUid()) {
            throw new SecurityException(
                    "calling sendNiResponse from outside of the system is not allowed");
        }
        try {
            return mNetInitiatedListener.sendNiResponse(notifId, userResponse);
        } catch (RemoteException e) {
            Slog.e(TAG, "RemoteException in LocationManagerService.sendNiResponse");
            return false;
        }
    }

    /**
     * @return null if the provider does not exist
     * @throws SecurityException if the provider is not allowed to be
     * accessed by the caller
     */
    @Override
    public ProviderProperties getProviderProperties(String provider) {
        if (mProvidersByName.get(provider) == null) {
            return null;
        }

        checkResolutionLevelIsSufficientForProviderUse(getCallerAllowedResolutionLevel(),
                provider);

        LocationProviderInterface p;
        synchronized (mLock) {
            p = mProvidersByName.get(provider);
        }

        if (p == null) return null;
        return p.getProperties();
    }

    /**
     * @return null if the provider does not exist
     * @throws SecurityException if the provider is not allowed to be
     * accessed by the caller
     */
    @Override
    public String getNetworkProviderPackage() {
        LocationProviderInterface p;
        synchronized (mLock) {
            if (mProvidersByName.get(LocationManager.NETWORK_PROVIDER) == null) {
                return null;
            }
            p = mProvidersByName.get(LocationManager.NETWORK_PROVIDER);
        }

        if (p instanceof LocationProviderProxy) {
            return ((LocationProviderProxy) p).getConnectedPackageName();
        }
        return null;
    }

    @Override
    public boolean isProviderEnabled(String provider) {
        // Fused provider is accessed indirectly via criteria rather than the provider-based APIs,
        // so we discourage its use
        if (LocationManager.FUSED_PROVIDER.equals(provider)) return false;

        int uid = Binder.getCallingUid();
        long identity = Binder.clearCallingIdentity();
        try {
            synchronized (mLock) {
                LocationProviderInterface p = mProvidersByName.get(provider);
                if (p == null) return false;

                /// M: return true if the provider is in whitelist working mode
                if (isWhitelistWorkingMode(provider) && isUidALocationProvider(uid)) {
                    return true;
                } else {
                    return isAllowedByUserSettingsLocked(provider, uid);
                }
            }
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    /**
     * Returns "true" if the UID belongs to a bound location provider.
     *
     * @param uid the uid
     * @return true if uid belongs to a bound location provider
     */
    private boolean isUidALocationProvider(int uid) {
        if (uid == Process.SYSTEM_UID) {
            return true;
        }
        if (mGeocodeProvider != null) {
            if (doesUidHavePackage(uid, mGeocodeProvider.getConnectedPackageName())) return true;
        }
        for (LocationProviderProxy proxy : mProxyProviders) {
            if (doesUidHavePackage(uid, proxy.getConnectedPackageName())) return true;
        }
        return false;
    }

    private void checkCallerIsProvider() {
        if (mContext.checkCallingOrSelfPermission(INSTALL_LOCATION_PROVIDER)
                == PackageManager.PERMISSION_GRANTED) {
            return;
        }

        // Previously we only used the INSTALL_LOCATION_PROVIDER
        // check. But that is system or signature
        // protection level which is not flexible enough for
        // providers installed oustide the system image. So
        // also allow providers with a UID matching the
        // currently bound package name

        if (isUidALocationProvider(Binder.getCallingUid())) {
            return;
        }

        throw new SecurityException("need INSTALL_LOCATION_PROVIDER permission, " +
                "or UID of a currently bound location provider");
    }

    /**
     * Returns true if the given package belongs to the given uid.
     */
    private boolean doesUidHavePackage(int uid, String packageName) {
        if (packageName == null) {
            return false;
        }
        String[] packageNames = mPackageManager.getPackagesForUid(uid);
        if (packageNames == null) {
            return false;
        }
        for (String name : packageNames) {
            if (packageName.equals(name)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void reportLocation(Location location, boolean passive) {
        checkCallerIsProvider();

        if (!location.isComplete()) {
            Log.w(TAG, "Dropping incomplete location: " + location);
            return;
        }

        mLocationHandler.removeMessages(MSG_LOCATION_CHANGED, location);
        Message m = Message.obtain(mLocationHandler, MSG_LOCATION_CHANGED, location);
        m.arg1 = (passive ? 1 : 0);
        mLocationHandler.sendMessageAtFrontOfQueue(m);
    }


    private static boolean shouldBroadcastSafe(
            Location loc, Location lastLoc, UpdateRecord record, long now) {
        // Always broadcast the first update
        if (lastLoc == null) {
            return true;
        }

        // Check whether sufficient time has passed
        long minTime = record.mRequest.getFastestInterval();
        long delta = (loc.getElapsedRealtimeNanos() - lastLoc.getElapsedRealtimeNanos())
                / NANOS_PER_MILLI;
        if (delta < minTime - MAX_PROVIDER_SCHEDULING_JITTER_MS) {
            return false;
        }

        // Check whether sufficient distance has been traveled
        double minDistance = record.mRequest.getSmallestDisplacement();
        if (minDistance > 0.0) {
            if (loc.distanceTo(lastLoc) <= minDistance) {
                return false;
            }
        }

        // Check whether sufficient number of udpates is left
        if (record.mRequest.getNumUpdates() <= 0) {
            return false;
        }

        // Check whether the expiry date has passed
        if (record.mRequest.getExpireAt() < now) {
            return false;
        }

        return true;
    }

    private void handleLocationChangedLocked(Location location, boolean passive) {
        if (D) {
            Log.d(TAG, "incoming location: " + location);
        } else {
            /// M: do not log location info for user's privacy
            Log.d(TAG, "incoming location from: " + location.getProvider());
        }

        long now = SystemClock.elapsedRealtime();
        String provider = (passive ? LocationManager.PASSIVE_PROVIDER : location.getProvider());

        // Skip if the provider is unknown.
        LocationProviderInterface p = mProvidersByName.get(provider);
        if (p == null) return;

        /*-prize-lihuangyuan add for gps monitor-2018-06-22-start*/
        synchronized (mCheckGpsLocationRun)
        {
            if(provider.equals(LocationManager.GPS_PROVIDER))
            {
                if (D)Log.d(TAG,"handleLocationChangedLocked mLastLocation:"+mCheckLastLocation);
                if (D)Log.d(TAG,"handleLocationChangedLocked mCurLocation:"+location);
                
                mCheckLastLocation = mCheckCurLocation;
                mCheckCurLocation = location;  
            }
        }
        /*-prize-lihuangyuan add for gps monitor-2018-06-22-end*/
        
        // Update last known locations
        Location noGPSLocation = location.getExtraLocation(Location.EXTRA_NO_GPS_LOCATION);
        Location lastNoGPSLocation = null;
        Location lastLocation = mLastLocation.get(provider);
        if (lastLocation == null) {
            lastLocation = new Location(provider);
            mLastLocation.put(provider, lastLocation);
        } else {
            lastNoGPSLocation = lastLocation.getExtraLocation(Location.EXTRA_NO_GPS_LOCATION);
            if (noGPSLocation == null && lastNoGPSLocation != null) {
                // New location has no no-GPS location: adopt last no-GPS location. This is set
                // directly into location because we do not want to notify COARSE clients.
                location.setExtraLocation(Location.EXTRA_NO_GPS_LOCATION, lastNoGPSLocation);
            }
        }
        lastLocation.set(location);

        // Update last known coarse interval location if enough time has passed.
        Location lastLocationCoarseInterval = mLastLocationCoarseInterval.get(provider);
        if (lastLocationCoarseInterval == null) {
            lastLocationCoarseInterval = new Location(location);
            mLastLocationCoarseInterval.put(provider, lastLocationCoarseInterval);
        }
        long timeDiffNanos = location.getElapsedRealtimeNanos()
                - lastLocationCoarseInterval.getElapsedRealtimeNanos();
        if (timeDiffNanos > LocationFudger.FASTEST_INTERVAL_MS * NANOS_PER_MILLI) {
            lastLocationCoarseInterval.set(location);
        }
        // Don't ever return a coarse location that is more recent than the allowed update
        // interval (i.e. don't allow an app to keep registering and unregistering for
        // location updates to overcome the minimum interval).
        noGPSLocation =
                lastLocationCoarseInterval.getExtraLocation(Location.EXTRA_NO_GPS_LOCATION);

        // Skip if there are no UpdateRecords for this provider.
        ArrayList<UpdateRecord> records = mRecordsByProvider.get(provider);
        if (records == null || records.size() == 0) return;

        // Fetch coarse location
        Location coarseLocation = null;
        if (noGPSLocation != null) {
            coarseLocation = mLocationFudger.getOrCreate(noGPSLocation);
        ///MTK added coarseLocation by lastLocationCoarse
        } else if (CtaUtils.isCtaSupported()) {
            coarseLocation = mLocationFudger.getOrCreate(lastLocationCoarseInterval);
        /// Mtk end
        }

        // Fetch latest status update time
        long newStatusUpdateTime = p.getStatusUpdateTime();

        // Get latest status
        Bundle extras = new Bundle();
        int status = p.getStatus(extras);

        ArrayList<Receiver> deadReceivers = null;
        ArrayList<UpdateRecord> deadUpdateRecords = null;

        // Broadcast location or status to all listeners
        for (UpdateRecord r : records) {
            Receiver receiver = r.mReceiver;
            boolean receiverDead = false;

            int receiverUserId = UserHandle.getUserId(receiver.mUid);
            if (!isCurrentProfile(receiverUserId) && !isUidALocationProvider(receiver.mUid)) {
                if (D) {
                    Log.d(TAG, "skipping loc update for background user " + receiverUserId +
                            " (current user: " + mCurrentUserId + ", app: " +
                            receiver.mPackageName + ")");
                }
                continue;
            }

            if (mBlacklist.isBlacklisted(receiver.mPackageName)) {
                if (D) Log.d(TAG, "skipping loc update for blacklisted app: " +
                        receiver.mPackageName);
                continue;
            }

            if (!reportLocationAccessNoThrow(receiver.mPid, receiver.mUid, receiver.mPackageName,
                    receiver.mAllowedResolutionLevel)) {
                if (D) Log.d(TAG, "skipping loc update for no op app: " +
                        receiver.mPackageName);
                continue;
            }

            // M: Skip notifying receivers which are not in whitelist
            if (isWhitelistWorkingMode(provider)
                    && !mWhitelistPackage.equals(receiver.mPackageName)) {
                continue;
            }

            Location notifyLocation = null;
            if (receiver.mAllowedResolutionLevel < RESOLUTION_LEVEL_FINE) {
                notifyLocation = coarseLocation;  // use coarse location
            } else {
                notifyLocation = lastLocation;  // use fine location
            }
            if (notifyLocation != null) {
                Location lastLoc = r.mLastFixBroadcast;
                if ((lastLoc == null) || shouldBroadcastSafe(notifyLocation, lastLoc, r, now)) {
                    if (lastLoc == null) {
                        lastLoc = new Location(notifyLocation);
                        r.mLastFixBroadcast = lastLoc;
                    } else {
                        lastLoc.set(notifyLocation);
                    }
                    if (!receiver.callLocationChangedLocked(notifyLocation)) {
                        Slog.w(TAG, "RemoteException calling onLocationChanged on " + receiver);
                        receiverDead = true;
                    }
                    r.mRequest.decrementNumUpdates();
                }
            }

            long prevStatusUpdateTime = r.mLastStatusBroadcast;
            if ((newStatusUpdateTime > prevStatusUpdateTime) &&
                    (prevStatusUpdateTime != 0 || status != LocationProvider.AVAILABLE)) {

                r.mLastStatusBroadcast = newStatusUpdateTime;
                if (!receiver.callStatusChangedLocked(provider, status, extras)) {
                    receiverDead = true;
                    Slog.w(TAG, "RemoteException calling onStatusChanged on " + receiver);
                }
            }

            // track expired records
            if (r.mRequest.getNumUpdates() <= 0 || r.mRequest.getExpireAt() < now) {
                if (deadUpdateRecords == null) {
                    deadUpdateRecords = new ArrayList<UpdateRecord>();
                }
                deadUpdateRecords.add(r);
            }
            // track dead receivers
            if (receiverDead) {
                if (deadReceivers == null) {
                    deadReceivers = new ArrayList<Receiver>();
                }
                if (!deadReceivers.contains(receiver)) {
                    deadReceivers.add(receiver);
                }
            }
        }

        // remove dead records and receivers outside the loop
        if (deadReceivers != null) {
            for (Receiver receiver : deadReceivers) {
                removeUpdatesLocked(receiver);
            }
        }
        if (deadUpdateRecords != null) {
            for (UpdateRecord r : deadUpdateRecords) {
                r.disposeLocked(true);
            }
            applyRequirementsLocked(provider);
        }
    }

    private class LocationWorkerHandler extends Handler {
        public LocationWorkerHandler(Looper looper) {
            super(looper, null, true);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_LOCATION_CHANGED:
                    handleLocationChanged((Location) msg.obj, msg.arg1 == 1);
                    break;
                /*-prize-lihuangyuan add for gps monitor-2018-06-22-start*/
                case MSG_REQUEST_GPS_PROVIDER:
                    handleRequestGpsProvider();
                    break;
                case MSG_REMOVE_GPS_PROVIDER:
                    hanleRemoveGpsProvider();
                    break;
                /*-prize-lihuangyuan add for gps monitor-2018-06-22-end*/
            }
        }
    }
    /*-prize-lihuangyuan add for gps monitor-2018-06-22-start*/
    private boolean mStartCheckGpsAppKill = false;
    private boolean mIsRegisterScreen = false; 
    public static final int CHECK_GPS_APP_KILL_INTERVAL = 1*60*1000;
    public static final int CHECK_GPS_LOCATION_INTERVAL = 10*1000;
    private void handleRequestGpsProvider()
    {
        Log.d(TAG,"handleRequestGpsProvider++++++++++");
        mStartCheckGpsAppKill =  true;
        mLocationHandler.removeCallbacks(mCheckGpsAppKillRun);
        mLocationHandler.postDelayed(mCheckGpsAppKillRun,CHECK_GPS_APP_KILL_INTERVAL);
        if(!mIsRegisterScreen)
        {
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_SCREEN_OFF);
            filter.addAction(Intent.ACTION_SCREEN_ON);
            mContext.registerReceiver(mScreenReceiver, filter);
            mIsRegisterScreen =  true;
        }
    }
    private void hanleRemoveGpsProvider()
    {
        Log.d(TAG,"hanleRemoveGpsProvider++++++++++");
        mStartCheckGpsAppKill = false;
        mLocationHandler.removeCallbacks(mCheckGpsAppKillRun);
        if(mIsRegisterScreen)
        {
            mContext.unregisterReceiver(mScreenReceiver);
            mIsRegisterScreen =  false;
            stopCheckGpsLocation();
        }
    }
    private BroadcastReceiver mScreenReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            if (Intent.ACTION_SCREEN_OFF.equals(action))
            {
                Log.d(TAG,"ACTION_SCREEN_OFF");
                startCheckGpsLocation();
            }
            else if (Intent.ACTION_SCREEN_ON.equals(action))
            {
                Log.d(TAG,"ACTION_SCREEN_ON");
                stopCheckGpsLocation();
            }
        }
    };
    private boolean mIsCheckGpsLocaton = false;
    private Location mCheckLastLocation;
    private Location mCheckCurLocation;
    private long      mCheckGpsLocationStartTime = 0;
    private int         mCheckGpsLocationChangedTimes = 0;
    private void startCheckGpsLocation()
    {
        Log.d(TAG,"startCheckGpsLocation++++++++++");
        synchronized (mCheckGpsLocationRun)
        {
            mCheckLastLocation = null;
            mCheckCurLocation = null;
            mCheckGpsLocationChangedTimes =  0;
            mCheckGpsLocationStartTime = SystemClock.elapsedRealtime();
        }
        mIsCheckGpsLocaton = true;
        mLocationHandler.removeCallbacks(mCheckGpsLocationRun);
        mLocationHandler.postDelayed(mCheckGpsLocationRun,CHECK_GPS_LOCATION_INTERVAL);
    }
    private void stopCheckGpsLocation()
    {
        Log.d(TAG,"stopCheckGpsLocation++++++++++");
        mIsCheckGpsLocaton = false;
        mLocationHandler.removeCallbacks(mCheckGpsLocationRun);
    }
    private Runnable mCheckGpsLocationRun = new Runnable()
    {
        public void run()
        {
            //check gps provider
            /*List<String> usegpsList = getInUseGpsPackagesList();
            if(usegpsList.size() <= 0)
            {
                stopCheckGpsLocation();
                return;
            }*/
            
            //check the location change
            if(mCheckCurLocation != null && mCheckLastLocation != null 
                && (mCheckCurLocation.getLongitude() != mCheckLastLocation.getLongitude()
                      ||mCheckCurLocation.getLatitude() != mCheckLastLocation.getLatitude()))
            {                
                mCheckGpsLocationChangedTimes ++;
                //mCheckCurLocation =  null;
                Log.d(TAG,"mCheckGpsLocationRun mCheckGpsLocationChangedTimes:"+mCheckGpsLocationChangedTimes);
            }
            else
            {
                Log.d(TAG,"mCheckGpsLocationRun mCurLocation:"+mCheckCurLocation+",mLastLocation:"+mCheckLastLocation);
            }

            if(SystemClock.elapsedRealtime() - mCheckGpsLocationStartTime > 10*60*1000)
            {
                if(mCheckGpsLocationChangedTimes <= 3)
                {
                    List<String> usegpsList = getInUseGpsPackagesList();
                    ActivityManager amgr = (ActivityManager)mContext.getSystemService(Context.ACTIVITY_SERVICE);
                    List<ActivityManager.RunningTaskInfo> runningTask = amgr.getRunningTasks(1);
                    ActivityManager.RunningTaskInfo toptask = runningTask.size() > 0 ? runningTask.get(0):null;
                    String toptaskPkg = toptask != null ? toptask.baseActivity.getPackageName():null;
                    
                    Log.d(TAG,"mCheckGpsLocationRun forstop the gps apps"+",toptaskPkg:"+toptaskPkg);
                    for(int i=0;i<usegpsList.size();i++)
                    {
                        if(!usegpsList.get(i).equals(toptaskPkg))
                        {
                            forceStopPackage(usegpsList.get(i));
                        }                        
                    }
                }
                else
                {
                    
                }
                stopCheckGpsLocation();                
                return;
            }
            if(mIsCheckGpsLocaton)
            {
                mLocationHandler.postDelayed(mCheckGpsLocationRun,CHECK_GPS_LOCATION_INTERVAL);
            }
        }
    };
    private Runnable mCheckGpsAppKillRun = new Runnable()
    {
        public void run()
        {
            //check gps package
            ActivityManager amgr = (ActivityManager)mContext.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningTaskInfo> runningTask = amgr.getRunningTasks(50);
            ActivityManager.RunningTaskInfo toptask = runningTask.size() > 0 ? runningTask.get(0):null;
            String toptaskPkg = toptask != null ? toptask.baseActivity.getPackageName():null;
            List<String> usegpsList = getInUseGpsPackagesList();
            Log.d(TAG,"mCheckGpsAppKillRun toptaskPkg:"+toptaskPkg);

            if(usegpsList.size() <= 0)
            {
                hanleRemoveGpsProvider();
                return;
            }
            
            for(int i=0;i<usegpsList.size();i++)
            {
                String pkgname = usegpsList.get(i);
                if(!pkgname.equals(toptaskPkg))
                {
                    for(int j=0;j<runningTask.size();j++)
                    {
                        ActivityManager.RunningTaskInfo taskinfo = runningTask.get(j);
                        if(pkgname.equals(taskinfo.baseActivity.getPackageName()))
                        {
                            long curtime = System.currentTimeMillis();
                            long backruntime = curtime - taskinfo.lastActiveTime;
                            //if(backruntime > 60*60*1000)continue;
                            Log.d(TAG,"mCheckGpsAppKillRun pkgname:"+pkgname+",backruntime:"+backruntime+",taskinfo.lastActiveTime:"+taskinfo.lastActiveTime);
                            if(backruntime > 2*60*1000)
                            {
                                prizeReleaseGps(pkgname);
                            }
                        }                        
                    }
                }                
            }            
            if(mStartCheckGpsAppKill)
            {
                mLocationHandler.postDelayed(mCheckGpsAppKillRun,CHECK_GPS_APP_KILL_INTERVAL);
            }
        }
    };
    public static final String[] gps_whitelist = new String[]{
        "com.baidu.BaiduMap",
        "com.autonavi.minimap",
        "com.baidu.navi",
        "com.sogou.map.android.maps",
        "com.sdu.didi.psnger",//didi
        "com.didapinche.booking",//dida       
        
    };
    private void prizeReleaseGps(String pkgname)
    {
        //check whitelist
        Log.d(TAG,"prizeReleaseGps pkgname:"+pkgname);        
        for(int i=0;i<gps_whitelist.length;i++)
        {
            if(pkgname.equals(gps_whitelist[i]))
            {
                return;
            }
        }   

        synchronized (mLock) 
        {
                ArrayList<Receiver> deadReceivers = null;
                for (Receiver receiver : mReceivers.values()) 
                {
                    if (receiver.mPackageName.equals(pkgname)) 
                    {
                        if (deadReceivers == null) 
                        {
                            deadReceivers = new ArrayList<Receiver>();
                        }
                        deadReceivers.add(receiver);
                    }
                }

                // perform removal outside of mReceivers loop
                if (deadReceivers != null) 
                {
                    for (Receiver receiver : deadReceivers) 
                    {
                        removeUpdatesLocked(receiver);
                    }
                }
        }
        
    }
    private void forceStopPackage(String pkgname)
    {
        Log.d(TAG,"forceStopPackage "+pkgname); 
        ActivityManager amgr = (ActivityManager)mContext.getSystemService(Context.ACTIVITY_SERVICE);
        amgr.forceStopPackage(pkgname);
    }
    public List<String> getInUseGpsPackagesList() {
        List<String> list = new ArrayList();

        HashMap<PackageProviderKey, PackageStatistics> statistics = mRequestStatistics.statistics;

        for (Map.Entry<PackageProviderKey, PackageStatistics> stat : statistics.entrySet()) {
            PackageProviderKey key = stat.getKey();
            PackageStatistics value = stat.getValue();
            if (value.isActive() && (key.providerName.equals(LocationManager.GPS_PROVIDER))) {
                String name = key.packageName;
                if (!list.contains(name)) {
                    list.add(name);
                }
            }
        }
        Log.d(TAG, "get in use gps pkg:" + list);
        return list;
    }
    /*-prize-lihuangyuan add for gps monitor-2018-06-22-end*/
    private boolean isMockProvider(String provider) {
        synchronized (mLock) {
            return mMockProviders.containsKey(provider);
        }
    }

    private void handleLocationChanged(Location location, boolean passive) {
        // create a working copy of the incoming Location so that the service can modify it without
        // disturbing the caller's copy
        Location myLocation = new Location(location);
        String provider = myLocation.getProvider();

        // set "isFromMockProvider" bit if location came from a mock provider. we do not clear this
        // bit if location did not come from a mock provider because passive/fused providers can
        // forward locations from mock providers, and should not grant them legitimacy in doing so.
        if (!myLocation.isFromMockProvider() && isMockProvider(provider)) {
            myLocation.setIsFromMockProvider(true);
        }

        synchronized (mLock) {
            if (isAllowedByCurrentUserSettingsLocked(provider)) {
                if (!passive) {
                    // notify passive provider of the new location
                    mPassiveProvider.updateLocation(myLocation);
                }
                handleLocationChangedLocked(myLocation, passive);
            /// allow whitelist package listeners to receive location changes
            } else if (isWhitelistWorkingMode(provider)) {
                handleLocationChangedLocked(myLocation, passive);
            }
        }
    }

    private final PackageMonitor mPackageMonitor = new PackageMonitor() {
        @Override
        public void onPackageDisappeared(String packageName, int reason) {
            // remove all receivers associated with this package name
            synchronized (mLock) {
                ArrayList<Receiver> deadReceivers = null;

                for (Receiver receiver : mReceivers.values()) {
                    if (receiver.mPackageName.equals(packageName)) {
                        if (deadReceivers == null) {
                            deadReceivers = new ArrayList<Receiver>();
                        }
                        deadReceivers.add(receiver);
                    }
                }

                // perform removal outside of mReceivers loop
                if (deadReceivers != null) {
                    for (Receiver receiver : deadReceivers) {
                        removeUpdatesLocked(receiver);
                    }
                }
            }
        }
    };

    // Geocoder

    @Override
    public boolean geocoderIsPresent() {
        // M: Really check the geocoder service is presented or not.
        return (mGeocodeProvider != null && mGeocodeProvider.isServiceBinded());
    }

    @Override
    public String getFromLocation(double latitude, double longitude, int maxResults,
            GeocoderParams params, List<Address> addrs) {
        ///M: check disable geocoder system property first
        boolean isDisableGeocoder = SystemProperties
            .get("persist.sys.mtk.disable.moms", "0").equals("1");
        if (mGeocodeProvider != null && !isDisableGeocoder) {
            return mGeocodeProvider.getFromLocation(latitude, longitude, maxResults,
                    params, addrs);
        }
        return null;
    }


    @Override
    public String getFromLocationName(String locationName,
            double lowerLeftLatitude, double lowerLeftLongitude,
            double upperRightLatitude, double upperRightLongitude, int maxResults,
            GeocoderParams params, List<Address> addrs) {
        ///M: check disable geocoder system property first
        boolean isDisableGeocoder = SystemProperties
                .get("persist.sys.mtk.disable.moms", "0").equals("1");
        if (mGeocodeProvider != null && !isDisableGeocoder) {
            return mGeocodeProvider.getFromLocationName(locationName, lowerLeftLatitude,
                    lowerLeftLongitude, upperRightLatitude, upperRightLongitude,
                    maxResults, params, addrs);
        }
        return null;
    }

    // Mock Providers

    private boolean canCallerAccessMockLocation(String opPackageName) {
        return mAppOps.noteOp(AppOpsManager.OP_MOCK_LOCATION, Binder.getCallingUid(),
                opPackageName) == AppOpsManager.MODE_ALLOWED;
    }

    @Override
    public void addTestProvider(String name, ProviderProperties properties, String opPackageName) {
        if (!canCallerAccessMockLocation(opPackageName)) {
            return;
        }

        if (LocationManager.PASSIVE_PROVIDER.equals(name)) {
            throw new IllegalArgumentException("Cannot mock the passive location provider");
        }

        long identity = Binder.clearCallingIdentity();
        synchronized (mLock) {
            // remove the real provider if we are replacing GPS or network provider
            if (LocationManager.GPS_PROVIDER.equals(name)
                    || LocationManager.NETWORK_PROVIDER.equals(name)
                    || LocationManager.FUSED_PROVIDER.equals(name)) {
                LocationProviderInterface p = mProvidersByName.get(name);
                if (p != null) {
                    removeProviderLocked(p);
                }
            }
            addTestProviderLocked(name, properties);
            updateProvidersLocked();
        }
        Binder.restoreCallingIdentity(identity);
    }

    private void addTestProviderLocked(String name, ProviderProperties properties) {
        if (mProvidersByName.get(name) != null) {
            throw new IllegalArgumentException("Provider \"" + name + "\" already exists");
        }
        MockProvider provider = new MockProvider(name, this, properties);
        addProviderLocked(provider);
        mMockProviders.put(name, provider);
        mLastLocation.put(name, null);
        mLastLocationCoarseInterval.put(name, null);
    }

    @Override
    public void removeTestProvider(String provider, String opPackageName) {
        if (!canCallerAccessMockLocation(opPackageName)) {
            return;
        }

        synchronized (mLock) {

            // These methods can't be called after removing the test provider, so first make sure
            // we don't leave anything dangling.
            clearTestProviderEnabled(provider, opPackageName);
            clearTestProviderLocation(provider, opPackageName);
            clearTestProviderStatus(provider, opPackageName);

            MockProvider mockProvider = mMockProviders.remove(provider);
            if (mockProvider == null) {
                throw new IllegalArgumentException("Provider \"" + provider + "\" unknown");
            }
            long identity = Binder.clearCallingIdentity();
            removeProviderLocked(mProvidersByName.get(provider));

            // reinstate real provider if available
            LocationProviderInterface realProvider = mRealProviders.get(provider);
            if (realProvider != null) {
                addProviderLocked(realProvider);
            }
            mLastLocation.put(provider, null);
            mLastLocationCoarseInterval.put(provider, null);
            updateProvidersLocked();
            Binder.restoreCallingIdentity(identity);
        }
    }

    @Override
    public void setTestProviderLocation(String provider, Location loc, String opPackageName) {
        if (!canCallerAccessMockLocation(opPackageName)) {
            return;
        }

        synchronized (mLock) {
            MockProvider mockProvider = mMockProviders.get(provider);
            if (mockProvider == null) {
                throw new IllegalArgumentException("Provider \"" + provider + "\" unknown");
            }

            // Ensure that the location is marked as being mock. There's some logic to do this in
            // handleLocationChanged(), but it fails if loc has the wrong provider (bug 33091107).
            Location mock = new Location(loc);
            mock.setIsFromMockProvider(true);

            if (!TextUtils.isEmpty(loc.getProvider()) && !provider.equals(loc.getProvider())) {
                // The location has an explicit provider that is different from the mock provider
                // name. The caller may be trying to fool us via bug 33091107.
                EventLog.writeEvent(0x534e4554, "33091107", Binder.getCallingUid(),
                        provider + "!=" + loc.getProvider());
            }

            // clear calling identity so INSTALL_LOCATION_PROVIDER permission is not required
            long identity = Binder.clearCallingIdentity();
            mockProvider.setLocation(mock);
            Binder.restoreCallingIdentity(identity);
        }
    }

    @Override
    public void clearTestProviderLocation(String provider, String opPackageName) {
        if (!canCallerAccessMockLocation(opPackageName)) {
            return;
        }

        synchronized (mLock) {
            MockProvider mockProvider = mMockProviders.get(provider);
            if (mockProvider == null) {
                throw new IllegalArgumentException("Provider \"" + provider + "\" unknown");
            }
            mockProvider.clearLocation();
        }
    }

    @Override
    public void setTestProviderEnabled(String provider, boolean enabled, String opPackageName) {
        if (!canCallerAccessMockLocation(opPackageName)) {
            return;
        }

        synchronized (mLock) {
            MockProvider mockProvider = mMockProviders.get(provider);
            if (mockProvider == null) {
                throw new IllegalArgumentException("Provider \"" + provider + "\" unknown");
            }
            long identity = Binder.clearCallingIdentity();
            if (enabled) {
                mockProvider.enable();
                mEnabledProviders.add(provider);
                mDisabledProviders.remove(provider);
            } else {
                mockProvider.disable();
                mEnabledProviders.remove(provider);
                mDisabledProviders.add(provider);
            }
            updateProvidersLocked();
            Binder.restoreCallingIdentity(identity);
        }
    }

    @Override
    public void clearTestProviderEnabled(String provider, String opPackageName) {
        if (!canCallerAccessMockLocation(opPackageName)) {
            return;
        }

        synchronized (mLock) {
            MockProvider mockProvider = mMockProviders.get(provider);
            if (mockProvider == null) {
                throw new IllegalArgumentException("Provider \"" + provider + "\" unknown");
            }
            long identity = Binder.clearCallingIdentity();
            mEnabledProviders.remove(provider);
            mDisabledProviders.remove(provider);
            updateProvidersLocked();
            Binder.restoreCallingIdentity(identity);
        }
    }

    @Override
    public void setTestProviderStatus(String provider, int status, Bundle extras, long updateTime,
            String opPackageName) {
        if (!canCallerAccessMockLocation(opPackageName)) {
            return;
        }

        synchronized (mLock) {
            MockProvider mockProvider = mMockProviders.get(provider);
            if (mockProvider == null) {
                throw new IllegalArgumentException("Provider \"" + provider + "\" unknown");
            }
            mockProvider.setStatus(status, extras, updateTime);
        }
    }

    @Override
    public void clearTestProviderStatus(String provider, String opPackageName) {
        if (!canCallerAccessMockLocation(opPackageName)) {
            return;
        }

        synchronized (mLock) {
            MockProvider mockProvider = mMockProviders.get(provider);
            if (mockProvider == null) {
                throw new IllegalArgumentException("Provider \"" + provider + "\" unknown");
            }
            mockProvider.clearStatus();
        }
    }

    private void log(String log) {
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Slog.d(TAG, log);
        }
    }

    @Override
    protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (mContext.checkCallingOrSelfPermission(android.Manifest.permission.DUMP)
                != PackageManager.PERMISSION_GRANTED) {
            pw.println("Permission Denial: can't dump LocationManagerService from from pid="
                    + Binder.getCallingPid()
                    + ", uid=" + Binder.getCallingUid());
            return;
        }

        synchronized (mLock) {
            pw.println("Current Location Manager state:");
            pw.println("  Location Listeners:");
            for (Receiver receiver : mReceivers.values()) {
                pw.println("    " + receiver);
            }
            pw.println("  Active Records by Provider:");
            for (Map.Entry<String, ArrayList<UpdateRecord>> entry : mRecordsByProvider.entrySet()) {
                pw.println("    " + entry.getKey() + ":");
                for (UpdateRecord record : entry.getValue()) {
                    pw.println("      " + record);
                }
            }
            pw.println("  Historical Records by Provider:");
            for (Map.Entry<PackageProviderKey, PackageStatistics> entry
                    : mRequestStatistics.statistics.entrySet()) {
                PackageProviderKey key = entry.getKey();
                PackageStatistics stats = entry.getValue();
                pw.println("    " + key.packageName + ": " + key.providerName + ": " + stats);
            }
            pw.println("  Last Known Locations:");
            for (Map.Entry<String, Location> entry : mLastLocation.entrySet()) {
                String provider = entry.getKey();
                Location location = entry.getValue();
                pw.println("    " + provider + ": " + location);
            }

            pw.println("  Last Known Locations Coarse Intervals:");
            for (Map.Entry<String, Location> entry : mLastLocationCoarseInterval.entrySet()) {
                String provider = entry.getKey();
                Location location = entry.getValue();
                pw.println("    " + provider + ": " + location);
            }

            mGeofenceManager.dump(pw);

            if (mEnabledProviders.size() > 0) {
                pw.println("  Enabled Providers:");
                for (String i : mEnabledProviders) {
                    pw.println("    " + i);
                }

            }
            if (mDisabledProviders.size() > 0) {
                pw.println("  Disabled Providers:");
                for (String i : mDisabledProviders) {
                    pw.println("    " + i);
                }
            }
            pw.append("  ");
            mBlacklist.dump(pw);
            if (mMockProviders.size() > 0) {
                pw.println("  Mock Providers:");
                for (Map.Entry<String, MockProvider> i : mMockProviders.entrySet()) {
                    i.getValue().dump(pw, "      ");
                }
            }

            pw.append("  fudger: ");
            mLocationFudger.dump(fd, pw,  args);

            if (args.length > 0 && "short".equals(args[0])) {
                return;
            }
            for (LocationProviderInterface provider: mProviders) {
                pw.print(provider.getName() + " Internal State");
                if (provider instanceof LocationProviderProxy) {
                    LocationProviderProxy proxy = (LocationProviderProxy) provider;
                    pw.print(" (" + proxy.getConnectedPackageName() + ")");
                }
                pw.println(":");
                provider.dump(fd, pw, args);
            }
        }
    }

    /// Mtk add start
    private void reBindNetworkProviderLock(boolean bindGmsPackage) {
        Log.d(TAG, "reBindNetworkProviderLock bindGmsPackage: " + bindGmsPackage);

        LocationProviderProxy previousNlp =
                (LocationProviderProxy) mRealProviders.get(LocationManager.NETWORK_PROVIDER);
        if (previousNlp != null) {
            Log.d(TAG, "binded NLP package name: " + previousNlp.getConnectedPackageName());
        } else {
            Log.d(TAG, "currently there is no NLP provided.");
            return;
        }

        // try to bind to new network provider
        int preferPackageNamesId = 0;
        if (bindGmsPackage) {
            preferPackageNamesId = 0;
        } else {
            preferPackageNamesId =
                    com.mediatek.internal.R.array.config_cnLocationProviderPackageNames;
        }

        // bind to network provider
        LocationProviderProxy networkProvider = LocationProviderProxy.createAndBind(
                mContext,
                LocationManager.NETWORK_PROVIDER,
                NETWORK_LOCATION_SERVICE_ACTION,
                com.android.internal.R.bool.config_enableNetworkLocationOverlay,
                com.android.internal.R.string.config_networkLocationProviderPackageName,
                com.android.internal.R.array.config_locationProviderPackageNames,
                com.mediatek.internal.R.array.config_cnLocationProviderPackageNames,
                preferPackageNamesId,
                mLocationHandler);

        if (networkProvider != null) {
            Log.d(TAG, "Successfully bind package:" + networkProvider.getConnectedPackageName());

            //unbind previous network provider
            if (previousNlp != null) {
                previousNlp.unbind();
                mRealProviders.remove(LocationManager.NETWORK_PROVIDER);
                mProxyProviders.remove(previousNlp);
                removeProviderLocked(previousNlp);
            }
            mRealProviders.put(LocationManager.NETWORK_PROVIDER, networkProvider);
            mProxyProviders.add(networkProvider);
            addProviderLocked(networkProvider);
            updateProvidersLocked();

             // M: try to rebind geocoder provider
            if (mGeocodeProvider != null) {
                mGeocodeProvider.unbind();
            }
            mGeocodeProvider = GeocoderProxy.createAndBind(mContext,
                    com.android.internal.R.bool.config_enableGeocoderOverlay,
                    com.android.internal.R.string.config_geocoderProviderPackageName,
                    com.android.internal.R.array.config_locationProviderPackageNames,
                    com.mediatek.internal.R.array.config_cnLocationProviderPackageNames,
                    preferPackageNamesId,
                    mLocationHandler);
        } else {
            Log.d(TAG, "Failed to bind specified package service");
        }
    }

    private boolean isWhitelistFeatureSupport() {
        String optr = SystemProperties.get("persist.operator.optr");
        if (optr != null && optr.equals("OP08") ) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isWhitelistWorkingMode(String provider) {
        boolean ret = false;
        if (mWhitelistWorkingMode && mWhitelistProvider.equals(provider)) {
            ret = true;
        }
        return ret;
    }

    private boolean isProviderRecordsContainsWhilelistPackage(String provider) {
        ArrayList<UpdateRecord> records = mRecordsByProvider.get(provider);
        boolean contained = false;
        if (records != null) {
            for (UpdateRecord record : records) {
                if (mWhitelistPackage.equals(record.mReceiver.mPackageName)) {
                    contained = true;
                    break;
                }
            }
        }
        Log.d(TAG, "isProviderRecordsContainsWhilelistPackage: " + contained);
        return contained;
    }

    // return true if all whistlist packages are removed and provider is disabled
    private boolean disableProviderWhenNoWhitelistPackageRegistered(String provider) {
        boolean disabledProvider = false;
        if (!isProviderRecordsContainsWhilelistPackage(provider)) {
            mWhitelistWorkingMode = false;
            LocationProviderInterface p = mProvidersByName.get(provider);
            if (p == null) {
                throw new IllegalArgumentException("provider doesn't exist: " + provider);
            } else {
                Log.d(TAG, "disable provider when no whitelist package registered");
                p.disable();
                disabledProvider = true;
                mContext.sendBroadcastAsUser(new Intent(LocationManager.PROVIDERS_CHANGED_ACTION),
                        UserHandle.ALL);
                mContext.sendBroadcastAsUser(new Intent(LocationManager.MODE_CHANGED_ACTION),
                        UserHandle.ALL);
            }
        }
        return disabledProvider;
    }
    // Mtk added end
}