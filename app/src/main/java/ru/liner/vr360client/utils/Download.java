package ru.liner.vr360client.utils;

import android.app.DownloadManager;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.net.Uri;

import java.io.File;

/**
 * @author : "Line'R"
 * @mailto : serinity320@mail.com
 * @created : 01.05.2022, воскресенье
 **/
public class Download {
    private Context context;
    private DownloadManager downloadManager;
    private Callback callback;
    private long downloadID;
    private Worker downloadQue;
    private int downloadProgress = 0;
    private File destinationFile;

    public Download(Context context) {
        this.context = context;
        this.downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        this.downloadQue = new Worker() {
            @Override
            public void execute() {
                DownloadManager.Query query = new DownloadManager.Query();
                query.setFilterById(downloadID);
                Cursor cursor = downloadManager.query(query);
                cursor.moveToFirst();
                try {
                    final int bytesDownloaded = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                    final int bytesInTotal = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                    if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
                        callback.onFinished(destinationFile);
                        stop();
                    }
                    final int progressPercent = (int) ((bytesDownloaded * 100L) / bytesInTotal);
                    if (progressPercent != downloadProgress) {
                        downloadProgress = progressPercent;
                        callback.onDownloading(progressPercent, bytesDownloaded, bytesInTotal);
                    }
                } catch (CursorIndexOutOfBoundsException | ArithmeticException e) {
                    callback.onFailed(e);
                    stop();
                }
                cursor.close();
            }

            @Override
            public long delay() {
                return 16;
            }
        };
    }

    public void startDownload(String url, File destinationFile, Callback callback) {
        this.destinationFile = destinationFile;
        this.callback = callback;
        if (Networks.isNetworkAvailable(context)) {
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            request.setTitle(destinationFile.getName());
            request.setDescription("Fetching video");
            request.setVisibleInDownloadsUi(true);
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
            request.setDestinationUri(Uri.fromFile(destinationFile));
            downloadID = downloadManager.enqueue(request);
            this.callback.onStarted();
            downloadQue.start();
        } else {
            this.callback.onNetworkUnavailable();
        }
    }

    public void cancelDownload() {
        downloadManager.remove(downloadID);
        downloadQue.stop();
        callback.onCanceled();
    }

    public int getDownloadProgress() {
        return downloadProgress;
    }

    public interface Callback {
        void onStarted();

        void onCanceled();

        void onFinished(File file);

        void onDownloading(int progress, int downloadedBytes, int totalBytes);

        void onFailed(Exception e);

        void onNetworkUnavailable();
    }
}
