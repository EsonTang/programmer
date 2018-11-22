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

package android.os;

import libcore.io.ErrnoException;
import libcore.io.Libcore;
import libcore.io.StructStatVfs;

/**
 * Retrieve overall information about the space on a filesystem. This is a
 * wrapper for Unix statvfs().
 */
public class StatFs {
    private StructStatVfs mStat;
    private float JTY_ROM_MULTIPLE = 1.0F;
    /**
     * Construct a new StatFs for looking at the stats of the filesystem at
     * {@code path}. Upon construction, the stat of the file system will be
     * performed, and the values retrieved available from the methods on this
     * class.
     *
     * @param path path in the desired file system to stat.
     */
    public StatFs(String path) {
	JTY_ROM_MULTIPLE = 1.0F;
	if (SystemProperties.get("ro.mtk_shared_sdcard").equals("1")){
		//JTY_ROM_MULTIPLE = Float.parseFloat(SystemProperties.get("ro.jty.fat.expandNum", "1"));	
	}else{

		if ("/data".equals(path)){
			//JTY_ROM_MULTIPLE = Float.parseFloat(SystemProperties.get("ro.jty.data.expandNum", "1")); 
		}else if (("/storage/sdcard0".equals(path)) || ("/mnt/sdcard".equals(path)) || ("/storage/emulated/legacy".equals(path))){
			//JTY_ROM_MULTIPLE = Float.parseFloat(SystemProperties.get("ro.jty.fat.expandNum", "1"));
		}
	}
        mStat = doStat(path);
    }

    private static StructStatVfs doStat(String path) {
        try {
            return Libcore.os.statvfs(path);
        } catch (ErrnoException e) {
            throw new IllegalArgumentException("Invalid path: " + path, e);
        }
    }

    /**
     * Perform a restat of the file system referenced by this object. This is
     * the same as re-constructing the object with the same file system path,
     * and the new stat values are available upon return.
     */
    public void restat(String path) {
        mStat = doStat(path);
    }

    /**
     * @deprecated Use {@link #getBlockSizeLong()} instead.
     */
    @Deprecated
    public int getBlockSize() {
        return (int) mStat.f_bsize;
    }

    /**
     * The size, in bytes, of a block on the file system. This corresponds to
     * the Unix {@code statvfs.f_bsize} field.
     */
    public long getBlockSizeLong() {
        return mStat.f_bsize;
    }

    /**
     * @deprecated Use {@link #getBlockCountLong()} instead.
     */
    @Deprecated
    public int getBlockCount() {
        return (int)(mStat.f_blocks * JTY_ROM_MULTIPLE);
    }

    /**
     * The total number of blocks on the file system. This corresponds to the
     * Unix {@code statvfs.f_blocks} field.
     */
    public long getBlockCountLong() {
        return (long)(mStat.f_blocks * JTY_ROM_MULTIPLE);
    }

    /**
     * @deprecated Use {@link #getFreeBlocksLong()} instead.
     */
    @Deprecated
    public int getFreeBlocks() {
        return (int) ( mStat.f_bfree* JTY_ROM_MULTIPLE);
    }

    /**
     * The total number of blocks that are free on the file system, including
     * reserved blocks (that are not available to normal applications). This
     * corresponds to the Unix {@code statvfs.f_bfree} field. Most applications
     * will want to use {@link #getAvailableBlocks()} instead.
     */
    public long getFreeBlocksLong() {
        return (long) ( mStat.f_bfree* JTY_ROM_MULTIPLE);
    }

    /**
     * The number of bytes that are free on the file system, including reserved
     * blocks (that are not available to normal applications). Most applications
     * will want to use {@link #getAvailableBytes()} instead.
     */
    public long getFreeBytes() {
        return (long) (mStat.f_bfree * mStat.f_bsize * JTY_ROM_MULTIPLE);
    }

    /**
     * @deprecated Use {@link #getAvailableBlocksLong()} instead.
     */
    @Deprecated
    public int getAvailableBlocks() {
        //return (int)( mStat.f_bavail* JTY_ROM_MULTIPLE);
	  return (int) (mStat.f_blocks* JTY_ROM_MULTIPLE) - (int)(mStat.f_blocks -mStat.f_bavail);
    }

    /**
     * The number of blocks that are free on the file system and available to
     * applications. This corresponds to the Unix {@code statvfs.f_bavail} field.
     */
    public long getAvailableBlocksLong() {
        //return (long) (mStat.f_bavail* JTY_ROM_MULTIPLE);
	return (long) (mStat.f_blocks  * JTY_ROM_MULTIPLE) -(mStat.f_blocks -mStat.f_bavail) ;
    }

    /**
     * The number of bytes that are free on the file system and available to
     * applications.
     */
    public long getAvailableBytes() {
        return (long) (mStat.f_blocks * mStat.f_bsize * JTY_ROM_MULTIPLE) -(mStat.f_blocks -mStat.f_bavail) * mStat.f_bsize ;
    }

    /**
     * The total number of bytes supported by the file system.
     */
    public long getTotalBytes() {
        return (long) (mStat.f_blocks * mStat.f_bsize * JTY_ROM_MULTIPLE);
    }
}
