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


package com.android.server.wm;

import android.os.SystemProperties;

/**
 * Common class for the various debug {@link android.util.Log} output configuration in the window
 * manager package.
 */
public class WindowManagerDebugConfig {
    // All output logs in the window manager package use the {@link #TAG_WM} string for tagging
    // their log output. This makes it easy to identify the origin of the log message when sifting
    // through a large amount of log output from multiple sources. However, it also makes trying
    // to figure-out the origin of a log message while debugging the window manager a little
    // painful. By setting this constant to true, log messages from the window manager package
    // will be tagged with their class names instead fot the generic tag.
    static final boolean TAG_WITH_CLASS_NAME = false;

    // Default log tag for the window manager package.
    static final String TAG_WM = "WindowManager";
    static final boolean enableAll = SystemProperties.getBoolean("debug.wms.surface", false);

    static boolean DEBUG_RESIZE = false || enableAll;
    static boolean DEBUG = false || enableAll;
    static boolean DEBUG_ADD_REMOVE = false || enableAll;
    static boolean DEBUG_FOCUS = false || enableAll;
    static boolean DEBUG_FOCUS_LIGHT = DEBUG_FOCUS || true;
    static boolean DEBUG_ANIM = false || enableAll;
    static boolean DEBUG_KEYGUARD = true;
    static boolean DEBUG_LAYOUT = false || enableAll;
    static boolean DEBUG_LAYERS = false || enableAll;
    static boolean DEBUG_INPUT = false || enableAll;
    static boolean DEBUG_INPUT_METHOD = false || enableAll;
    static boolean DEBUG_VISIBILITY = false || enableAll;
    static boolean DEBUG_WINDOW_MOVEMENT = false || enableAll;
    static boolean DEBUG_TOKEN_MOVEMENT = false || enableAll;
    static boolean DEBUG_ORIENTATION = false || enableAll;
    static boolean DEBUG_APP_ORIENTATION = false || enableAll;
    static boolean DEBUG_CONFIGURATION = false || enableAll;
    static boolean DEBUG_APP_TRANSITIONS = false || enableAll;
    static boolean DEBUG_STARTING_WINDOW = false || enableAll;
    static boolean DEBUG_WALLPAPER = false || enableAll;
    static boolean DEBUG_WALLPAPER_LIGHT = false || DEBUG_WALLPAPER;
    static boolean DEBUG_DRAG = false || enableAll;
    static boolean DEBUG_SCREEN_ON = true;
    static boolean DEBUG_SCREENSHOT = false || enableAll;
    static boolean DEBUG_BOOT = true;
    static boolean DEBUG_LAYOUT_REPEATS = false || enableAll;
    static boolean DEBUG_SURFACE_TRACE = false || enableAll;
    static boolean DEBUG_WINDOW_TRACE = false || enableAll;
    static boolean DEBUG_TASK_MOVEMENT = false || enableAll;
    static boolean DEBUG_TASK_POSITIONING = false || enableAll;
    static boolean DEBUG_STACK = false || enableAll;
    static boolean DEBUG_DISPLAY = true;
    static boolean DEBUG_POWER = false || enableAll;
    static boolean DEBUG_DIM_LAYER = false || enableAll;
    static boolean SHOW_SURFACE_ALLOC = false || enableAll;
    static boolean SHOW_TRANSACTIONS = false || enableAll;
    static boolean SHOW_VERBOSE_TRANSACTIONS = false && SHOW_TRANSACTIONS;
    static boolean SHOW_LIGHT_TRANSACTIONS = false || SHOW_TRANSACTIONS;
    static boolean SHOW_STACK_CRAWLS = false || enableAll;
    static boolean DEBUG_WINDOW_CROP = false || enableAll;

    static final String TAG_KEEP_SCREEN_ON = "DebugKeepScreenOn";
    static boolean DEBUG_KEEP_SCREEN_ON = false || enableAll;

    /// M: add more debug cofigurations
    static boolean DEBUG_WAKEUP = false || enableAll;
    static boolean localLOGV = false || enableAll;
}
