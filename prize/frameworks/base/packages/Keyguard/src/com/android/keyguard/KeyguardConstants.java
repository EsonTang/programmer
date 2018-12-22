/*
 * Copyright (C) 2014 The Android Open Source Project
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
 * limitations under the License
 */

package com.android.keyguard;

import android.os.SystemProperties;

/**
 * Defines constants for the Keyguard.
 */
public class KeyguardConstants {

   private static final boolean IS_ENG_BUILD = SystemProperties.get("ro.build.type").equals("eng");

    /**
     * Turns on debugging information for the whole Keyguard. This is very verbose and should only
     * be used temporarily for debugging.
     */
    public static final boolean DEBUG = IS_ENG_BUILD;
    public static final boolean DEBUG_SIM_STATES = IS_ENG_BUILD;
    public static final boolean DEBUG_FP_WAKELOCK = true;
}
