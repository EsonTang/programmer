/*
* Copyright (C) 2014 MediaTek Inc.
* Modification based on code covered by the mentioned copyright
* and/or permission notice(s).
*/
/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.providers.downloads;

import static com.android.providers.downloads.Constants.DEBUG;

import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MatrixCursor.RowBuilder;
import android.graphics.Point;
import android.net.Uri;
import android.os.Binder;
import android.os.CancellationSignal;
import android.os.Environment;
import android.os.FileObserver;
import android.os.FileUtils;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.provider.DocumentsContract.Document;
import android.provider.DocumentsContract.Root;
import android.provider.DocumentsProvider;
import android.provider.Downloads;
import android.support.provider.DocumentArchiveHelper;
import android.provider.Downloads;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import com.mediatek.omadrm.OmaDrmStore;
import android.drm.DrmManagerClient;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.NumberFormat;

import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;

import libcore.io.IoUtils;

/**
 * Presents a {@link DocumentsContract} view of {@link DownloadManager}
 * contents.
 */
public class DownloadStorageProvider extends DocumentsProvider {
    private static final String TAG = "DownloadStorageProvider";
    private static final boolean DEBUG = false;

    private static final String AUTHORITY = Constants.STORAGE_AUTHORITY;
    private static final String DOC_ID_ROOT = Constants.STORAGE_ROOT_ID;
    private static final String XTAG_DRM = "DownloadManager/DRM";

    private static final String[] DEFAULT_ROOT_PROJECTION = new String[] {
            Root.COLUMN_ROOT_ID, Root.COLUMN_FLAGS, Root.COLUMN_ICON,
            Root.COLUMN_TITLE, Root.COLUMN_DOCUMENT_ID,
    };

    private static final String[] DEFAULT_DOCUMENT_PROJECTION = new String[] {
            Document.COLUMN_DOCUMENT_ID, Document.COLUMN_MIME_TYPE, Document.COLUMN_DISPLAY_NAME,
            Document.COLUMN_SUMMARY, Document.COLUMN_LAST_MODIFIED, Document.COLUMN_FLAGS,
            Document.COLUMN_SIZE,
            /// M: add to support drm .@{
            MediaStore.MediaColumns.DATA, MediaStore.MediaColumns.IS_DRM,
            MediaStore.MediaColumns.DRM_METHOD,
            /// @}
    };

    private DownloadManager mDm;
    private DocumentArchiveHelper mArchiveHelper;
    /// M : add to support drm. @{
    private static DrmManagerClient mDrmClient;
    /// @}

    @Override
    public boolean onCreate() {
        mDm = (DownloadManager) getContext().getSystemService(Context.DOWNLOAD_SERVICE);
        mDm.setAccessAllDownloads(true);
        mDm.setAccessFilename(true);
        mArchiveHelper = new DocumentArchiveHelper(this, ':');

        return true;
    }

    private static String[] resolveRootProjection(String[] projection) {
        return projection != null ? projection : DEFAULT_ROOT_PROJECTION;
    }

    private static String[] resolveDocumentProjection(String[] projection) {
        return projection != null ? projection : DEFAULT_DOCUMENT_PROJECTION;
    }

    private void copyNotificationUri(MatrixCursor result, Cursor cursor) {
        result.setNotificationUri(getContext().getContentResolver(), cursor.getNotificationUri());
    }

    static void onDownloadProviderDelete(Context context, long id) {
        final Uri uri = DocumentsContract.buildDocumentUri(AUTHORITY, Long.toString(id));
        context.revokeUriPermission(uri, ~0);
    }

    @Override
    public Cursor queryRoots(String[] projection) throws FileNotFoundException {
        final MatrixCursor result = new MatrixCursor(resolveRootProjection(projection));
        final RowBuilder row = result.newRow();
        row.add(Root.COLUMN_ROOT_ID, DOC_ID_ROOT);
        row.add(Root.COLUMN_FLAGS,
                Root.FLAG_LOCAL_ONLY | Root.FLAG_SUPPORTS_RECENTS | Root.FLAG_SUPPORTS_CREATE);
        row.add(Root.COLUMN_ICON, R.mipmap.ic_launcher_download);
        row.add(Root.COLUMN_TITLE, getContext().getString(R.string.root_downloads));
        row.add(Root.COLUMN_DOCUMENT_ID, DOC_ID_ROOT);
        return result;
    }

    @Override
    public String createDocument(String docId, String mimeType, String displayName)
            throws FileNotFoundException {
        displayName = FileUtils.buildValidFatFilename(displayName);

        if (DEBUG) Log.d(Constants.DL_ENHANCE,
            "DownloadStorageProvider:createDocument(), docId: " + docId +
                       ", mimeType: " + mimeType + ",displayName: " + displayName);

        if (Document.MIME_TYPE_DIR.equals(mimeType)) {
            throw new FileNotFoundException("Directory creation not supported");
        }

        final File parent = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS);
        parent.mkdirs();

        // Delegate to real provider
        final long token = Binder.clearCallingIdentity();
        try {
            final File file = FileUtils.buildUniqueFile(parent, mimeType, displayName);

            try {
                if (!file.createNewFile()) {
                    throw new IllegalStateException("Failed to touch " + file);
                }
            } catch (IOException e) {
                throw new IllegalStateException("Failed to touch " + file + ": " + e);
            }

            return Long.toString(mDm.addCompletedDownload(
                    file.getName(), file.getName(), true, mimeType, file.getAbsolutePath(), 0L,
                    false, true));
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    @Override
    public void deleteDocument(String docId) throws FileNotFoundException {
        // Delegate to real provider
        final long token = Binder.clearCallingIdentity();
        try {
            if (mDm.remove(Long.parseLong(docId)) != 1) {
                throw new IllegalStateException("Failed to delete " + docId);
            }
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    @Override
    public String renameDocument(String documentId, String displayName)
            throws FileNotFoundException {
        displayName = FileUtils.buildValidFatFilename(displayName);

        final long token = Binder.clearCallingIdentity();
        try {
            final long id = Long.parseLong(documentId);

            if (!mDm.rename(getContext(), id, displayName)) {
                throw new IllegalStateException(
                        "Failed to rename to " + displayName + " in downloadsManager");
            }
        } finally {
            Binder.restoreCallingIdentity(token);
        }
        return null;
    }

    @Override
    public Cursor queryDocument(String docId, String[] projection) throws FileNotFoundException {
        if (mArchiveHelper.isArchivedDocument(docId)) {
            return mArchiveHelper.queryDocument(docId, projection);
        }

        final DownloadsCursor result =
                new DownloadsCursor(projection, getContext().getContentResolver());

        if (DOC_ID_ROOT.equals(docId)) {
            includeDefaultDocument(result);
        } else {
            // Delegate to real provider
            final long token = Binder.clearCallingIdentity();
            Cursor cursor = null;
            try {
                cursor = mDm.query(new Query().setFilterById(Long.parseLong(docId)));
                copyNotificationUri(result, cursor);
                if (cursor.moveToFirst()) {
                    // We don't know if this queryDocument() call is from Downloads (manage)
                    // or Files. Safely assume it's Files.
                    includeDownloadFromCursor(result, cursor);
                }
            } finally {
                IoUtils.closeQuietly(cursor);
                Binder.restoreCallingIdentity(token);
            }
        }

        result.start();
        return result;
    }

    @Override
    public Cursor queryChildDocuments(String docId, String[] projection, String sortOrder)
            throws FileNotFoundException {
        if (mArchiveHelper.isArchivedDocument(docId) ||
                mArchiveHelper.isSupportedArchiveType(getDocumentType(docId))) {
            return mArchiveHelper.queryChildDocuments(docId, projection, sortOrder);
        }

        final DownloadsCursor result =
                new DownloadsCursor(projection, getContext().getContentResolver());

        // Delegate to real provider
        final long token = Binder.clearCallingIdentity();
        Cursor cursor = null;
        try {
            cursor = mDm.query(new DownloadManager.Query().setOnlyIncludeVisibleInDownloadsUi(true)
                    .setFilterByStatus(DownloadManager.STATUS_SUCCESSFUL));
            copyNotificationUri(result, cursor);
            while (cursor.moveToNext()) {
                includeDownloadFromCursor(result, cursor);
            }
        } finally {
            IoUtils.closeQuietly(cursor);
            Binder.restoreCallingIdentity(token);
        }

        result.start();
        return result;
    }

    @Override
    public Cursor queryChildDocumentsForManage(
            String parentDocumentId, String[] projection, String sortOrder)
            throws FileNotFoundException {
        if (mArchiveHelper.isArchivedDocument(parentDocumentId)) {
            return mArchiveHelper.queryDocument(parentDocumentId, projection);
        }

        final DownloadsCursor result =
                new DownloadsCursor(projection, getContext().getContentResolver());

        // Delegate to real provider
        final long token = Binder.clearCallingIdentity();
        Cursor cursor = null;
        try {
            cursor = mDm.query(
                    new DownloadManager.Query().setOnlyIncludeVisibleInDownloadsUi(true));
            copyNotificationUri(result, cursor);
            while (cursor.moveToNext()) {
                includeDownloadFromCursor(result, cursor);
            }
        } finally {
            IoUtils.closeQuietly(cursor);
            Binder.restoreCallingIdentity(token);
        }

        result.start();
        return result;
    }

    @Override
    public Cursor queryRecentDocuments(String rootId, String[] projection)
            throws FileNotFoundException {

        final DownloadsCursor result =
                new DownloadsCursor(projection, getContext().getContentResolver());

        // Delegate to real provider
        final long token = Binder.clearCallingIdentity();
        Cursor cursor = null;
        try {
            cursor = mDm.query(new DownloadManager.Query().setOnlyIncludeVisibleInDownloadsUi(true)
                    .setFilterByStatus(DownloadManager.STATUS_SUCCESSFUL));
            copyNotificationUri(result, cursor);
            while (cursor.moveToNext() && result.getCount() < 12) {
                final String mimeType = cursor.getString(
                        cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_MEDIA_TYPE));
                final String uri = cursor.getString(
                        cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_MEDIAPROVIDER_URI));

                // Skip images that have been inserted into the MediaStore so we
                // don't duplicate them in the recents list.
                if (mimeType == null
                        || (mimeType.startsWith("image/") && !TextUtils.isEmpty(uri)) || Helpers.isMtkDRMFile(mimeType)) {
                    continue;
                }

                includeDownloadFromCursor(result, cursor);
            }
        } finally {
            IoUtils.closeQuietly(cursor);
            Binder.restoreCallingIdentity(token);
        }

        result.start();
        return result;
    }

    @Override
    public ParcelFileDescriptor openDocument(String docId, String mode, CancellationSignal signal)
            throws FileNotFoundException {
        if (mArchiveHelper.isArchivedDocument(docId)) {
            return mArchiveHelper.openDocument(docId, mode, signal);
        }

        // Delegate to real provider
        final long token = Binder.clearCallingIdentity();
        try {
            final long id = Long.parseLong(docId);
            final ContentResolver resolver = getContext().getContentResolver();
            return resolver.openFileDescriptor(mDm.getDownloadUri(id), mode, signal);
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    @Override
    public AssetFileDescriptor openDocumentThumbnail(
            String docId, Point sizeHint, CancellationSignal signal) throws FileNotFoundException {
        // TODO: extend ExifInterface to support fds
        final ParcelFileDescriptor pfd = openDocument(docId, "r", signal);
        return new AssetFileDescriptor(pfd, 0, AssetFileDescriptor.UNKNOWN_LENGTH);
    }

    private void includeDefaultDocument(MatrixCursor result) {
        final RowBuilder row = result.newRow();
        row.add(Document.COLUMN_DOCUMENT_ID, DOC_ID_ROOT);
        row.add(Document.COLUMN_MIME_TYPE, Document.MIME_TYPE_DIR);
        row.add(Document.COLUMN_FLAGS,
                Document.FLAG_DIR_PREFERS_LAST_MODIFIED | Document.FLAG_DIR_SUPPORTS_CREATE);
    }

    /**
     * Adds the entry from the cursor to the result only if the entry is valid. That is,
     * if the file exists in the file system.
     */
    private void includeDownloadFromCursor(MatrixCursor result, Cursor cursor) {
        final long id = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_ID));
        final String docId = String.valueOf(id);

        final String displayName = cursor.getString(
                cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TITLE));
        String summary = cursor.getString(
                cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_DESCRIPTION));
        String mimeType = cursor.getString(
                cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_MEDIA_TYPE));
        if (mimeType == null) {
            // Provide fake MIME type so it's openable
            mimeType = "vnd.android.document/file";
        }
        Long size = cursor.getLong(
                cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
        if (size == -1) {
            size = null;
        }
        String localFilePath = cursor.getString(
                cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_LOCAL_FILENAME));

        int extraFlags = Document.FLAG_PARTIAL;
        final int status = cursor.getInt(
                cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS));
        switch (status) {
            case DownloadManager.STATUS_SUCCESSFUL:
                // Verify that the document still exists in external storage. This is necessary
                // because files can be deleted from the file system without their entry being
                // removed from DownloadsManager.
                if (localFilePath == null || !new File(localFilePath).exists()) {
                    try {
                        deleteDocument(docId);
                    }
                    catch (FileNotFoundException e) {
                        Log.e(Constants.DL_ENHANCE,
                            "Delete from DownloadDB failed for non-existent file");
                    }
                    return;
                }
                extraFlags = Document.FLAG_SUPPORTS_RENAME;  // only successful is non-partial
                break;
            case DownloadManager.STATUS_PAUSED:
                summary = getContext().getString(R.string.download_queued);
                break;
            case DownloadManager.STATUS_PENDING:
                summary = getContext().getString(R.string.download_queued);
                break;
            case DownloadManager.STATUS_RUNNING:
                final long progress = cursor.getLong(cursor.getColumnIndexOrThrow(
                        DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                if (size != null) {
                    String percent =
                            NumberFormat.getPercentInstance().format((double) progress / size);
                    summary = getContext().getString(R.string.download_running_percent, percent);
                } else {
                    summary = getContext().getString(R.string.download_running);
                }
                break;
            case DownloadManager.STATUS_FAILED:
                /// M : add OMA DL error string. @{
                summary = getContext().getString(R.string.download_error);
                if (Downloads.Impl.OMA_DOWNLOAD_SUPPORT) {
                     int reasonColumnId = cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_REASON);
                     int failedReason = cursor.getInt(reasonColumnId);
                     if (failedReason == DownloadManager.ERROR_INSUFFICIENT_SPACE) {
                         summary = getContext().getString(R.string.download_error_insufficient_memory);
                     } else if (failedReason == Downloads.Impl.STATUS_BAD_REQUEST) {
                         summary = getContext().getString(R.string.download_error_invalid_descriptor);
                     } else if (failedReason == Downloads.Impl.OMADL_STATUS_ERROR_INVALID_DDVERSION) {
                         summary = getContext().getString(R.string.download_error_invalid_ddversion);
                     } else if (failedReason == Downloads.Impl.OMADL_STATUS_ERROR_ATTRIBUTE_MISMATCH) {
                         summary = getContext().getString(R.string.download_error_attribute_mismatch);
                     }
                }
                break;
                /// @}
            default:
                summary = getContext().getString(R.string.download_error);
                break;
        }

        /// M: get origin mimetype of drm file. @{
        int isDrm = 0;
        int drmMethod = 0;
        if (Helpers.isMtkDRMFile(mimeType)) {
            isDrm = 1;
        }

        if (Constants.MTK_DRM_ENABLED && null != mimeType) {
             if (mimeType.equals(OmaDrmStore.DrmObjectMimeType.MIME_TYPE_DRM_CONTENT) ||
                     mimeType.equals(OmaDrmStore.DrmObjectMimeType.MIME_TYPE_DRM_MESSAGE)) {
                 String localUri = cursor.getString(cursor.getColumnIndexOrThrow(
                                 DownloadManager.COLUMN_LOCAL_URI));
                 if (localUri != null) {
                     if (mDrmClient == null) {
                         mDrmClient = new DrmManagerClient(getContext());
                     }

                     String path = Uri.parse(localUri).getPath();
                     String oriMimeType = mDrmClient.getOriginalMimeType(path);
                     drmMethod = getMethod(mDrmClient, path);
                     if (null != oriMimeType && !oriMimeType.isEmpty()) {
                         mimeType = oriMimeType;
                         if (DEBUG) Log.d(XTAG_DRM,
                            "DownloadStorageProvider: includeDownloadFromCursor a DRM file path:"
                                 + path + " ,original MimeType is:" + mimeType +
                                 ", docId: " + docId);
                     }
                 }
             }
        }
        /// @}
        int flags = Document.FLAG_SUPPORTS_DELETE | Document.FLAG_SUPPORTS_WRITE | extraFlags;
        final long progress = cursor.getLong(cursor.getColumnIndexOrThrow(
                DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
        final int destination = cursor.getInt(
                cursor.getColumnIndexOrThrow(Downloads.Impl.COLUMN_DESTINATION));
        if (mimeType.startsWith("image/")) {
            if ((size != null && progress == size) ||
                    destination == Downloads.Impl.DESTINATION_NON_DOWNLOADMANAGER_DOWNLOAD) {
                flags |= Document.FLAG_SUPPORTS_THUMBNAIL;
            }
        }

        if (mArchiveHelper.isSupportedArchiveType(mimeType)) {
            flags |= Document.FLAG_ARCHIVE;
        }

        final long lastModified = cursor.getLong(
                cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_LAST_MODIFIED_TIMESTAMP));

        final RowBuilder row = result.newRow();
        row.add(Document.COLUMN_DOCUMENT_ID, docId);
        row.add(Document.COLUMN_DISPLAY_NAME, displayName);
        row.add(Document.COLUMN_SUMMARY, summary);
        row.add(Document.COLUMN_SIZE, size);
        row.add(Document.COLUMN_MIME_TYPE, mimeType);
        row.add(Document.COLUMN_FLAGS, flags);
        // Incomplete downloads get a null timestamp.  This prevents thrashy UI when a bunch of
        // active downloads get sorted by mod time.
        if (status != DownloadManager.STATUS_RUNNING) {
            row.add(Document.COLUMN_LAST_MODIFIED, lastModified);
        }

        if (localFilePath != null) {
            row.add(DocumentArchiveHelper.COLUMN_LOCAL_FILE_PATH, localFilePath);
        }
        /// M: add to support drm. @{
        if (Constants.MTK_DRM_ENABLED && (isDrm == 1)) {
            String localPath = cursor
                    .getString(cursor
                            .getColumnIndexOrThrow(DownloadManager.COLUMN_LOCAL_FILENAME));

            if (localPath != null && localPath.length() != 0) {
                Uri mediaFilesUri = MediaStore.Files.getContentUri("external");
                String whereClause = MediaStore.MediaColumns.DATA + " = ?";

                Cursor drmCursor = null;
                try {
                    drmCursor = getContext().getContentResolver().query(
                            mediaFilesUri,
                            new String[] { MediaStore.MediaColumns.IS_DRM},
                            whereClause, new String[] { localPath }, null);

                    if (drmCursor != null && drmCursor.moveToFirst()) {
                        isDrm = drmCursor
                                .getInt(drmCursor
                                        .getColumnIndex(MediaStore.MediaColumns.IS_DRM));
                    }
                } catch (IllegalStateException e) {
                    Log.e(Constants.DL_ENHANCE,
                                    "DownloadStorageProvider:includeDownloadFromCursor query media occur error");
                } finally {
                    if (drmCursor != null) {
                        drmCursor.close();
                    }
                }
                if (!new File(localPath).exists()) {
                    isDrm = 1;
                    drmMethod = -1;
                }
            }

        if (drmMethod == 0) {
                isDrm = 0;
            }
            row.add(MediaStore.MediaColumns.DATA, localPath);
            row.add(MediaStore.MediaColumns.IS_DRM, isDrm);
            row.add(MediaStore.MediaColumns.DRM_METHOD, drmMethod);
            Log.e(Constants.DL_ENHANCE, "localPath: " + localPath
                    + ", isDrm: " + isDrm + ", drmMethod: "
                    + drmMethod + ", mimeType: " + mimeType + ", docId: " + docId);
        }
        /// @}
    }

    /**
     * A MatrixCursor that spins up a file observer when the first instance is
     * started ({@link #start()}, and stops the file observer when the last instance
     * closed ({@link #close()}. When file changes are observed, a content change
     * notification is sent on the Downloads content URI.
     *
     * <p>This is necessary as other processes, like ExternalStorageProvider,
     * can access and modify files directly (without sending operations
     * through DownloadStorageProvider).
     *
     * <p>Without this, contents accessible by one a Downloads cursor instance
     * (like the Downloads root in Files app) can become state.
     */
    private static final class DownloadsCursor extends MatrixCursor {

        private static final Object mLock = new Object();
        @GuardedBy("mLock")
        private static int mOpenCursorCount = 0;
        @GuardedBy("mLock")
        private static @Nullable ContentChangedRelay mFileWatcher;

        private final ContentResolver mResolver;

        DownloadsCursor(String[] projection, ContentResolver resolver) {
            super(resolveDocumentProjection(projection));
            mResolver = resolver;
        }

        void start() {
            synchronized (mLock) {
                if (mOpenCursorCount++ == 0) {
                    mFileWatcher = new ContentChangedRelay(mResolver);
                    mFileWatcher.startWatching();
                }
            }
        }

        @Override
        public void close() {
            super.close();
            synchronized (mLock) {
                if (--mOpenCursorCount == 0) {
                    mFileWatcher.stopWatching();
                    mFileWatcher = null;
                }
            }
        }
    }

    /**
     * A file observer that notifies on the Downloads content URI(s) when
     * files change on disk.
     */
    private static class ContentChangedRelay extends FileObserver {
        private static final int NOTIFY_EVENTS = ATTRIB | CLOSE_WRITE | MOVED_FROM | MOVED_TO
                | CREATE | DELETE | DELETE_SELF | MOVE_SELF;

        private static final String DOWNLOADS_PATH =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                        .getAbsolutePath();
        private final ContentResolver mResolver;

        public ContentChangedRelay(ContentResolver resolver) {
            super(DOWNLOADS_PATH, NOTIFY_EVENTS);
            mResolver = resolver;
        }

        @Override
        public void startWatching() {
            super.startWatching();
            if (DEBUG) Log.d(TAG, "Started watching for file changes in: " + DOWNLOADS_PATH);
        }

        @Override
        public void stopWatching() {
            super.stopWatching();
            if (DEBUG) Log.d(TAG, "Stopped watching for file changes in: " + DOWNLOADS_PATH);
        }

        @Override
        public void onEvent(int event, String path) {
            if ((event & NOTIFY_EVENTS) != 0) {
                if (DEBUG) Log.v(TAG, "Change detected at path: " + DOWNLOADS_PATH);
                mResolver.notifyChange(Downloads.Impl.ALL_DOWNLOADS_CONTENT_URI, null, false);
                mResolver.notifyChange(Downloads.Impl.CONTENT_URI, null, false);
            }
        }
    }

    /// M: Add for get DRM method. @{
    private int getMethod(DrmManagerClient client, String path) {
        int method = 0; // not a drm file
        ContentValues metadata = client.getMetadata(path);
        if (metadata != null && metadata.containsKey(OmaDrmStore.MetadatasColumns.DRM_METHOD)) {
            method = metadata.getAsInteger(OmaDrmStore.MetadatasColumns.DRM_METHOD);
        }
        return method;
    }
    /// @}
}
