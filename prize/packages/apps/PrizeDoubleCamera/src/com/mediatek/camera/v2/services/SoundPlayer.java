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
 * limitations under the License.
 */

package com.mediatek.camera.v2.services;

import java.util.HashMap;
import android.content.Context;
import android.media.SoundPool;
import android.util.Log;
import android.util.SparseIntArray;

/**
 * Loads a plays custom sounds. For playing system-standard sounds for various
 * camera actions, please refer to {@link SoundClips}.
 */
public class SoundPlayer implements SoundPool.OnLoadCompleteListener {
    private static final String TAG = "CAM_API2/SoundPlayer";
    private final Context mAppContext;
    private final SoundPool mSoundPool;
    /** ID returned by load() should be non-zero. */
    private static final int UNLOAD_SOUND_ID = 0;
    /** Keeps a mapping from sound resource ID to sound ID */
    private final SparseIntArray mResourceToSoundId = new SparseIntArray();
    private final HashMap<Integer, Boolean> mSoundIDReadyMap = new HashMap<Integer, Boolean>();
    private int mSoundIDToPlay;
    private float mVolume;

    /**
     * Construct a new sound player.
     */
    public SoundPlayer(Context appContext) {
        mAppContext = appContext;
        final int audioType = SoundClips.getAudioTypeForSoundPool();
        mSoundPool = new SoundPool(1 /* max streams */, audioType, 0 /* quality */);
        mSoundIDToPlay = UNLOAD_SOUND_ID;
        mSoundPool.setOnLoadCompleteListener(SoundPlayer.this);
    }

    /**
     * Load the sound from a resource.
     */
    private void loadSound(int resourceId) {
        int soundId = mSoundPool.load(mAppContext, resourceId, 1/* priority */);
        mResourceToSoundId.put(resourceId, soundId);
    }

    /**
     * Play the sound with the given resource. The resource has to be loaded
     * before it can be played.
     */
    public void play(int resourceId, float volume) {
        mSoundIDToPlay = mResourceToSoundId.get(resourceId, UNLOAD_SOUND_ID);
        mVolume = volume;
        if (mSoundIDToPlay == UNLOAD_SOUND_ID) {
            loadSound(resourceId);
            mSoundIDToPlay = mResourceToSoundId.get(resourceId);
        } else if (!mSoundIDReadyMap.get(mSoundIDToPlay)) {
            Log.w(TAG, "sound id " + mSoundIDToPlay + " is in loading and not ready yet");
        } else {
            mSoundPool
                    .play(mSoundIDToPlay, volume, volume,
                            0 /* priority */, 0 /* loop */, 1 /* rate */);
        }
    }

    /**
     * Unload the given sound if it's not needed anymore to release memory.
     */
    private void unloadSound(int resourceId) {
        Integer soundId = mResourceToSoundId.get(resourceId);
        if (soundId == null) {
            throw new IllegalStateException("Sound not loaded. Must call #loadSound first.");
        }
        mSoundPool.unload(soundId);
    }

    /**
     * Unload the all sound if it's not needed anymore to release memory.
     */
    public void unloadSound() {
        int resourceId = 0;
        int resourceSize = mResourceToSoundId.size();
        for (int i = 0; i < resourceSize; i++) {
            resourceId = mResourceToSoundId.keyAt(i);
            unloadSound(resourceId);
        }
        mResourceToSoundId.clear();
    }

    /**
     * Call this if you don't need the SoundPlayer anymore. All memory will be
     * released and the object cannot be re-used.
     */
    public void release() {
        mSoundPool.release();
    }

    @Override
    public void onLoadComplete(SoundPool pool, int soundID, int status) {
        if (status != 0) {
            Log.e(TAG, "onLoadComplete : " + soundID + " load failed , status is " + status);
            return;
        }
        Log.d(TAG, "onLoadComplete : " + soundID + " load success");
        mSoundIDReadyMap.put(soundID, true);
        if (soundID == mSoundIDToPlay) {
            mSoundIDToPlay = UNLOAD_SOUND_ID;
            mSoundPool.play(soundID, mVolume, mVolume, 0, 0, 1);
        }
    }
}
