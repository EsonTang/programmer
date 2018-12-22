/*
* Copyright (C) 2014 MediaTek Inc.
* Modification based on code covered by the mentioned copyright
* and/or permission notice(s).
*/
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

package com.android.gallery3d.app;

import android.content.Context;
import android.content.res.Resources;

import com.android.gallery3d.R;
import com.android.gallery3d.ui.AlbumSetSlotRenderer;
import com.android.gallery3d.ui.PrizeLabelMaker;
import com.android.gallery3d.ui.SlotView;
import com.mediatek.gallery3d.layout.FancyHelper;
import com.android.gallery3d.ui.TimeLineSlotRenderer;
import com.android.gallery3d.ui.TimeLineSlotView;

/// M: [FEATURE.MODIFY] @{
/*final class Config {*/
/**
 * set as public for Container.
 */
public final class Config {
/// @}
    public static class AlbumSetPage {
        private static AlbumSetPage sInstance;

        public SlotView.Spec slotViewSpec;
        public AlbumSetSlotRenderer.LabelSpec labelSpec;
        public int paddingTop;
        public int paddingBottom;
        public int placeholderColor;

        public static synchronized AlbumSetPage get(Context context) {
            /// M: [FEATURE.MODIFY] fancy layout @{
            // 1. connect phone to smb, launch Gallery on smb, then plug out phone
            // 2. launch Gallery on phone, album labels will became extremely small @{
            /*
            if (sInstance == null) {
                sInstance = new AlbumSetPage(context);
            }
            */
            sInstance = new AlbumSetPage(context);
            /// @}
            return sInstance;
        }

        /// M: [FEATURE.ADD] Multi-window. @{
        private static final int COLS_LAND_MULTI_WINDOW = 2;
        private static final int COLS_PORT_MULTI_WINDOW = 2;

        /**
         * Get AlbumSetPage config in Multi-window mode.
         * @param context context
         * @return AlbumSetPage config
         */
        public static synchronized AlbumSetPage getConfigInMultiWindow(Context context) {
            AlbumSetPage config = new AlbumSetPage(context);
            config.slotViewSpec.colsLand = COLS_LAND_MULTI_WINDOW;
            config.slotViewSpec.colsPort = COLS_PORT_MULTI_WINDOW;
            return config;
        }
        /// @}

        private AlbumSetPage(Context context) {
            Resources r = context.getResources();

            placeholderColor = r.getColor(R.color.albumset_placeholder);

            slotViewSpec = new SlotView.Spec();
            slotViewSpec.rowsLand = r.getInteger(R.integer.albumset_rows_land);
            slotViewSpec.rowsPort = r.getInteger(R.integer.albumset_rows_port);
            /// M: [FEATURE.ADD] fancy layout @{
            if (FancyHelper.isFancyLayoutSupported()) {
                slotViewSpec.colsLand = FancyHelper.ALBUMSETPAGE_COL_LAND;
                slotViewSpec.colsPort = FancyHelper.ALBUMSETPAGE_COL_PORT;
            }
            else {
            	slotViewSpec.colsLand = r.getInteger(R.integer.albumset_cols_land);
                slotViewSpec.colsPort = r.getInteger(R.integer.albumset_cols_port);
            }
            /// @}
            slotViewSpec.slotGap = r.getDimensionPixelSize(R.dimen.albumset_slot_gap);
            slotViewSpec.slotGapFile = r.getDimensionPixelSize(R.dimen.albumset_slot_gap_file);
            slotViewSpec.slotHeightAdditional = 0;

            paddingTop = r.getDimensionPixelSize(R.dimen.gl_padding_top);
            paddingBottom = r.getDimensionPixelSize(R.dimen.albumset_padding_bottom);

            labelSpec = new AlbumSetSlotRenderer.LabelSpec();
            labelSpec.labelBackgroundHeight = r.getDimensionPixelSize(
                    R.dimen.albumset_label_background_height);
            labelSpec.labelCountTop = r.getDimensionPixelSize(
                    R.dimen.albumset_label_count_top);
            labelSpec.titleOffset = r.getDimensionPixelSize(
                    R.dimen.albumset_title_offset);
            labelSpec.countOffset = r.getDimensionPixelSize(
                    R.dimen.albumset_count_offset);
            labelSpec.titleFontSize = r.getDimensionPixelSize(
                    R.dimen.albumset_title_font_size);
            labelSpec.countFontSize = r.getDimensionPixelSize(
                    R.dimen.albumset_count_font_size);
            labelSpec.leftMargin = r.getDimensionPixelSize(
                    R.dimen.albumset_left_margin);
            /// M: [FEATURE.MODIFY] fancy layout @{
            /*
            labelSpec.titleRightMargin = r.getDimensionPixelSize(
                    R.dimen.albumset_title_right_margin);
            */
            if (FancyHelper.isFancyLayoutSupported()) {
                labelSpec.titleRightMargin = r.getDimensionPixelSize(
                        R.dimen.albumset_title_right_margin_fancy);
            } else {
                labelSpec.titleRightMargin = r.getDimensionPixelSize(
                        R.dimen.albumset_title_right_margin);
            }
            /// @}
            labelSpec.iconSize = r.getDimensionPixelSize(
                    R.dimen.albumset_icon_size);
            /// M: [FEATURE.MODIFY] fancy layout @{
            /*
            labelSpec.backgroundColor = r.getColor(
                    R.color.albumset_label_background);
            */
            if (FancyHelper.isFancyLayoutSupported()) {
                labelSpec.backgroundColor = r.getColor(
                        R.color.albumset_label_background_fancy);
            } else {
                labelSpec.backgroundColor = r.getColor(
                        R.color.albumset_label_background);
            }
            /// @}
            labelSpec.titleColor = r.getColor(R.color.albumset_label_title);
            labelSpec.countColor = r.getColor(R.color.albumset_label_count);
        }
    }
    
    public static class AlbumDialogPage {
        private static AlbumDialogPage sInstance;

        public SlotView.Spec slotViewSpec;
        public AlbumSetSlotRenderer.LabelSpec labelSpec;
        public int paddingTop;
        public int paddingBottom;
        public int placeholderColor;

        public static synchronized AlbumDialogPage get(Context context) {
            /// M: [FEATURE.MODIFY] fancy layout @{
            // 1. connect phone to smb, launch Gallery on smb, then plug out phone
            // 2. launch Gallery on phone, album labels will became extremely small @{
            /*
            if (sInstance == null) {
                sInstance = new AlbumDialogPage(context);
            }
            */
            sInstance = new AlbumDialogPage(context);
            /// @}
            return sInstance;
        }

        private AlbumDialogPage(Context context) {
            Resources r = context.getResources();

            placeholderColor = r.getColor(R.color.albumset_placeholder);

            slotViewSpec = new SlotView.Spec();
            /** prize add 2015-07-07 liudong start*/
            slotViewSpec.slotGapFile = r.getDimensionPixelSize(R.dimen.albumset_slot_gap_file);
            /** prize add 2015-07-07 liudong end*/
            slotViewSpec.rowsLand = r.getInteger(R.integer.albumset_rows_land);
            slotViewSpec.rowsPort = r.getInteger(R.integer.albumset_rows_port);
            /// M: [FEATURE.ADD] fancy layout @{
            if (FancyHelper.isFancyLayoutSupported()) {
                slotViewSpec.colsLand = FancyHelper.ALBUMSETPAGE_COL_LAND;
                slotViewSpec.colsPort = FancyHelper.ALBUMSETPAGE_COL_PORT;
            }
            /// @prize fanjunchen 2015-04-23 {
            else {
            	slotViewSpec.colsLand = r.getInteger(R.integer.albumset_cols_land);
                slotViewSpec.colsPort = r.getInteger(R.integer.albumset_cols_port);
            	//slotViewSpec.colsLand = FancyHelper.ALBUMSETPAGE_COL_LAND;
                //slotViewSpec.colsPort = FancyHelper.ALBUMSETPAGE_COL_PORT;
            }
            /// @prize }
            /// @}
            slotViewSpec.slotGap = r.getDimensionPixelSize(R.dimen.albumset_slot_gap);
            slotViewSpec.slotHeightAdditional = 0;

            paddingTop = r.getDimensionPixelSize(R.dimen.gl_padding_top);
            paddingBottom = r.getDimensionPixelSize(R.dimen.albumset_padding_bottom);

            labelSpec = new AlbumSetSlotRenderer.LabelSpec();
            labelSpec.labelBackgroundHeight = r.getDimensionPixelSize(
                    R.dimen.albumset_label_background_height);
            labelSpec.labelCountTop = r.getDimensionPixelSize(
                    R.dimen.albumset_label_count_top);
            labelSpec.titleOffset = r.getDimensionPixelSize(
                    R.dimen.albumset_title_offset);
            labelSpec.countOffset = r.getDimensionPixelSize(
                    R.dimen.albumset_count_offset);
            labelSpec.titleFontSize = r.getDimensionPixelSize(
                    R.dimen.albumset_title_font_size);
            labelSpec.countFontSize = r.getDimensionPixelSize(
                    R.dimen.albumset_count_font_size);
            labelSpec.leftMargin = r.getDimensionPixelSize(
                    R.dimen.albumset_left_margin);
            /// M: [FEATURE.MODIFY] fancy layout @{
            /*
            labelSpec.titleRightMargin = r.getDimensionPixelSize(
                    R.dimen.albumset_title_right_margin);
            */
            if (FancyHelper.isFancyLayoutSupported()) {
                labelSpec.titleRightMargin = r.getDimensionPixelSize(
                        R.dimen.albumset_title_right_margin_fancy);
            } else {
                labelSpec.titleRightMargin = r.getDimensionPixelSize(
                        R.dimen.albumset_title_right_margin);
            }
            /// @}
            labelSpec.iconSize = r.getDimensionPixelSize(
                    R.dimen.albumset_icon_size);
            /// M: [FEATURE.MODIFY] fancy layout @{
            /*
            labelSpec.backgroundColor = r.getColor(
                    R.color.albumset_label_background);
            */
            if (FancyHelper.isFancyLayoutSupported()) {
                labelSpec.backgroundColor = r.getColor(
                        R.color.albumset_label_background_fancy);
            } else {
                labelSpec.backgroundColor = r.getColor(
                        R.color.albumset_label_background);
            }
            /// @}
            labelSpec.titleColor = r.getColor(R.color.albumset_label_title);
            labelSpec.countColor = r.getColor(R.color.albumset_label_count);
        }
    }

    public static class AlbumPage {
        private static AlbumPage sInstance;

        public SlotView.Spec slotViewSpec;
        public int placeholderColor;
        public int paddingTop;

        public static synchronized AlbumPage get(Context context) {
            if (sInstance == null) {
                sInstance = new AlbumPage(context);
            }
            return sInstance;
        }

        private AlbumPage(Context context) {
            Resources r = context.getResources();

            placeholderColor = r.getColor(R.color.album_placeholder);

            slotViewSpec = new SlotView.Spec();
            slotViewSpec.rowsLand = r.getInteger(R.integer.album_rows_land);
            slotViewSpec.rowsPort = r.getInteger(R.integer.album_rows_port);
            /// M: [FEATURE.ADD] fancy layout @{
            if (FancyHelper.isFancyLayoutSupported()) {
                slotViewSpec.colsLand = FancyHelper.ALBUMPAGE_COL_LAND;
                slotViewSpec.colsPort = FancyHelper.ALBUMPAGE_COL_PORT;
            }
            else {
                slotViewSpec.colsLand = r.getInteger(R.integer.album_cols_land);
                slotViewSpec.colsPort = r.getInteger(R.integer.album_cols_port);
            }
            /// @}
            slotViewSpec.slotGap = r.getDimensionPixelSize(R.dimen.album_slot_gap);
            slotViewSpec.slotGapFile = slotViewSpec.slotGap;
            paddingTop = r.getDimensionPixelSize(R.dimen.gl_padding_top);
        }
    }

    public static class ManageCachePage extends AlbumSetPage {
        private static ManageCachePage sInstance;

        public final int cachePinSize;
        public final int cachePinMargin;

        public static synchronized ManageCachePage get(Context context) {
            if (sInstance == null) {
                sInstance = new ManageCachePage(context);
            }
            return sInstance;
        }

        public ManageCachePage(Context context) {
            super(context);
            Resources r = context.getResources();
            cachePinSize = r.getDimensionPixelSize(R.dimen.cache_pin_size);
            cachePinMargin = r.getDimensionPixelSize(R.dimen.cache_pin_margin);
        }
    }

    /// @prize fanjunchen 2015-05-16 {
    public static class PrizeAlbumPage {
        private static PrizeAlbumPage sInstance;

        public SlotView.Spec slotViewSpec;
        public int placeholderColor;
        
        public PrizeLabelMaker.LabelSpec labelSpec;

        public static synchronized PrizeAlbumPage get(Context context) {
            if (sInstance == null) {
                sInstance = new PrizeAlbumPage(context);
            }
            return sInstance;
        }

        private PrizeAlbumPage(Context context) {
            Resources r = context.getResources();

            placeholderColor = r.getColor(R.color.album_placeholder);

            slotViewSpec = new SlotView.Spec();
            
            slotViewSpec.rowsLand = r.getInteger(R.integer.prize_album_rows_land);
            slotViewSpec.rowsPort = r.getInteger(R.integer.prize_album_rows_port);
            /// M: [FEATURE.ADD] fancy layout @{
            if (FancyHelper.isFancyLayoutSupported()) {
                slotViewSpec.colsLand = FancyHelper.ALBUMPAGE_COL_LAND;
                slotViewSpec.colsPort = FancyHelper.ALBUMPAGE_COL_PORT;
            }
            /// @prize fanjunchen 2015-04-23 {
            else {
            	slotViewSpec.colsLand = 6;
                slotViewSpec.colsPort = 4;
            }
            /// @prize }
            /// @}
            slotViewSpec.slotGap = r.getDimensionPixelSize(R.dimen.album_slot_gap);
            
            labelSpec = new PrizeLabelMaker.LabelSpec();
            
            slotViewSpec.labelHeight = r.getDimensionPixelSize(
                    R.dimen.prize_label_background_height);
            labelSpec.labelHeight = slotViewSpec.labelHeight;
            
            labelSpec.titleOffset = r.getDimensionPixelSize(
                    R.dimen.prize_title_offset);
            labelSpec.countOffset = r.getDimensionPixelSize(
                    R.dimen.prize_count_offset);
            labelSpec.titleFontSize = r.getDimensionPixelSize(
                    R.dimen.prize_title_font_size);
            labelSpec.countFontSize = r.getDimensionPixelSize(
                    R.dimen.prize_count_font_size);
            labelSpec.leftMargin = r.getDimensionPixelSize(
                    R.dimen.prize_left_margin);
            labelSpec.titleRightMargin = r.getDimensionPixelSize(
                    R.dimen.prize_title_right_margin);
            labelSpec.iconSize = r.getDimensionPixelSize(
                    R.dimen.albumset_icon_size);
            labelSpec.backgroundColor = r.getColor(
                    R.color.prize_label_background);
            labelSpec.titleColor = r.getColor(R.color.prize_label_title);
            labelSpec.countColor = r.getColor(R.color.prize_label_count);
        }
    }
    /// @prize }

   public static class TimeLinePage {
        private static TimeLinePage sInstance;

        public TimeLineSlotView.Spec slotViewSpec;
        public TimeLineSlotRenderer.LabelSpec labelSpec;
        public int placeholderColor;
        public int paddingTop;

        public static synchronized TimeLinePage get(Context context) {
            if (sInstance == null) {
                sInstance = new TimeLinePage(context);
            }
            return sInstance;
        }
        private TimeLinePage(Context context) {
            Resources r = context.getResources();

            placeholderColor = r.getColor(R.color.album_placeholder);

            paddingTop = r.getDimensionPixelSize(R.dimen.gl_padding_top);
            slotViewSpec = new TimeLineSlotView.Spec();
            slotViewSpec.colsLand = r.getInteger(R.integer.album_cols_land);
            slotViewSpec.colsPort = r.getInteger(R.integer.album_cols_port);
            slotViewSpec.slotGapPort = r.getDimensionPixelSize(R.dimen.timeline_port_slot_gap);
            slotViewSpec.slotGapLand = r.getDimensionPixelSize(R.dimen.timeline_land_slot_gap);
            slotViewSpec.titleHeight = r.getDimensionPixelSize(R.dimen.timeline_title_height);

            labelSpec = new TimeLineSlotRenderer.LabelSpec();
            labelSpec.slotGapPort = slotViewSpec.slotGapPort;
            labelSpec.slotGapLand = slotViewSpec.slotGapLand;
            labelSpec.timeLineTitleHeight = r.getDimensionPixelSize(
                    R.dimen.timeline_title_height);
            labelSpec.timeLineTitlePaddingBottom = r.getDimensionPixelSize(
                    R.dimen.timeline_title_padding_bottom);
            labelSpec.timeLineTitleFontSize = r.getDimensionPixelSize(
                    R.dimen.timeline_title_time_font_size);
            labelSpec.timeLineNumberFontSize = r.getDimensionPixelSize(
                    R.dimen.timeline_title_count_font_size);
            labelSpec.timeLineLocationPaddingLeft = r.getDimensionPixelSize(
                    R.dimen.timeline_title_location_padding_left);
            labelSpec.timeLineLocationFontSize = r.getDimensionPixelSize(
                    R.dimen.timeline_title_location_font_size);
            labelSpec.timeLineTitleMarginLeft = r.getDimensionPixelSize(
                    R.dimen.timeline_title_margin_left);
            labelSpec.timeLineTitleMarginRight = r.getDimensionPixelSize(
                    R.dimen.timeline_title_margin_right);
            labelSpec.timeLineTimePadding = r.getDimensionPixelSize(
                    R.dimen.timeline_title_time_padding);
            labelSpec.timeLineCountPadding = r.getDimensionPixelSize(
                    R.dimen.timeline_title_count_padding);
            labelSpec.timeLineTitleTextColor = r.getColor(R.color.timeline_title_text_color);
            labelSpec.timeLineNumberTextColor = r.getColor(R.color.timeline_title_number_text_color);
            labelSpec.timeLineLocationTextColor = r.getColor(R.color.timeline_title_location_text_color);
            labelSpec.timeLineTitleBackgroundColor = r.getColor(R.color.timeline_title_background_color);
        }
    }
}

