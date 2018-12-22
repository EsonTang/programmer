package com.mediatek.galleryfeature.drm;

import android.content.Intent;
import android.provider.MediaStore.Files.FileColumns;

import com.mediatek.galleryframework.base.MediaFilter;
import com.mediatek.galleryframework.base.MediaFilter.IFilter;
import com.mediatek.omadrm.OmaDrmStore;

public class DrmFilter implements IFilter {
    private static int INVALID_DRM_LEVEL = -1;

    public void setFlagFromIntent(Intent intent, MediaFilter filter) {
        filter.setFlagDisable(MediaFilter.INCLUDE_DRM_ALL);
        if (intent == null) {
            return;
        }
        String action = intent.getAction();
        int drmLevel =
                intent.getIntExtra(OmaDrmStore.DrmIntentExtra.EXTRA_DRM_LEVEL,
                        INVALID_DRM_LEVEL);
        if (drmLevel == INVALID_DRM_LEVEL) {
            intent.putExtra(OmaDrmStore.DrmIntentExtra.EXTRA_DRM_LEVEL,
                    OmaDrmStore.DrmIntentExtra.LEVEL_ALL);
            drmLevel = OmaDrmStore.DrmIntentExtra.LEVEL_ALL;
        }
        if (drmLevel != INVALID_DRM_LEVEL) {
            if (OmaDrmStore.DrmIntentExtra.LEVEL_FL == drmLevel) {
                filter.setFlagEnable(MediaFilter.INCLUDE_DRM_FL);
            } else if (OmaDrmStore.DrmIntentExtra.LEVEL_SD == drmLevel) {
                filter.setFlagEnable(MediaFilter.INCLUDE_DRM_SD);
            } else if (OmaDrmStore.DrmIntentExtra.LEVEL_ALL == drmLevel) {
                filter.setFlagEnable(MediaFilter.INCLUDE_DRM_ALL);
            }
        }
    }

    public void setDefaultFlag(MediaFilter filter) {
        filter.setFlagEnable(MediaFilter.INCLUDE_DRM_ALL);
    }

    public String getWhereClauseForImage(int flag, int bucketID) {
        return getWhereClauseInternal(flag);
    }

    public String getWhereClauseForVideo(int flag, int bucketID) {
        return getWhereClauseInternal(flag);
    }

    public String getWhereClause(int flag, int bucketID) {
        return getWhereClauseInternal(flag);
    }

    public String getDeleteWhereClauseForImage(int flag, int bucketID) {
        return getWhereClauseInternal(flag);
    }

    public String getDeleteWhereClauseForVideo(int flag, int bucketID) {
        return getWhereClauseInternal(flag);
    }

    private String getWhereClauseInternal(int flag) {
        String noDrmClause = FileColumns.IS_DRM + "=0 OR " + FileColumns.IS_DRM + " IS NULL";
        if ((flag & MediaFilter.INCLUDE_DRM_ALL) == 0) {
            return noDrmClause;
        }
        String whereClause = null;
        if ((flag & MediaFilter.INCLUDE_DRM_FL) != 0) {
            whereClause =
                    MediaFilter.OR(whereClause, FileColumns.DRM_METHOD + "="
                            + OmaDrmStore.Method.FL);
        }
        if ((flag & MediaFilter.INCLUDE_DRM_CD) != 0) {
            whereClause =
                    MediaFilter.OR(whereClause, FileColumns.DRM_METHOD + "="
                            + OmaDrmStore.Method.CD);
        }
        if ((flag & MediaFilter.INCLUDE_DRM_SD) != 0) {
            whereClause =
                    MediaFilter.OR(whereClause, FileColumns.DRM_METHOD + "="
                            + OmaDrmStore.Method.SD);
        }
        if ((flag & MediaFilter.INCLUDE_DRM_FLSD) != 0) {
            whereClause =
                    MediaFilter.OR(whereClause, FileColumns.DRM_METHOD + "="
                            + OmaDrmStore.Method.FLSD);
        }
        if (whereClause != null) {
            whereClause = MediaFilter.AND(FileColumns.IS_DRM + "=1", whereClause);
        }
        whereClause = MediaFilter.OR(noDrmClause, whereClause);
        return whereClause;
    }

    public String convertFlagToString(int flag) {
        StringBuilder sb = new StringBuilder();
        if ((flag & MediaFilter.INCLUDE_DRM_FL) != 0) {
            sb.append("INCLUDE_DRM_FL, ");
        }
        if ((flag & MediaFilter.INCLUDE_DRM_CD) != 0) {
            sb.append("INCLUDE_DRM_CD, ");
        }
        if ((flag & MediaFilter.INCLUDE_DRM_SD) != 0) {
            sb.append("INCLUDE_DRM_SD, ");
        }
        if ((flag & MediaFilter.INCLUDE_DRM_FLSD) != 0) {
            sb.append("INCLUDE_DRM_FLSD, ");
        }
        return sb.toString();
    }
}
