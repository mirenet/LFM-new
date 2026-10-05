package com.webhtml.app;

import android.app.DownloadManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import java.io.File;
import java.io.OutputStream;

public class DownloadHelper {
    private Context context;
    private String cachedBlobData = null;

    public DownloadHelper(Context context) {
        this.context = context;
    }

    // =========================================================
    // JAVASCRIPT BRIDGE (Zamenjuje AppBridge klasu)
    // =========================================================
    @JavascriptInterface
    public void cacheData(String data) {
        this.cachedBlobData = data;
    }

    // =========================================================
    // IZVRŠAVANJE PREUZIMANJA (Blob ili običan URL)
    // =========================================================
    public void executeDownloadTask(String fileName, String url, String mimetype, boolean isBlob) {
        final String finalFileName = fileName.replaceAll("[\\\\/:*?\"<>|]", "_");
        if (isBlob) {
            if (cachedBlobData != null && !cachedBlobData.equals("ERROR")) {
                saveBase64ToFile(cachedBlobData, finalFileName);
            } else {
                Toast.makeText(context, "Error: Blob data not ready or expired.", Toast.LENGTH_LONG).show();
            }
        } else {
            try {
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                request.setMimeType(mimetype);
                request.setTitle(finalFileName);
                request.setDescription("Downloading file...");
                request.allowScanningByMediaScanner();
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, finalFileName);
                
                DownloadManager dm = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
                if (dm != null) {
                    dm.enqueue(request);
                    Toast.makeText(context, "Download started...", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    // =========================================================
    // ČUVANJE BASE64 / BLOB FAJLOVA
    // =========================================================
    private void saveBase64ToFile(String base64Data, String name) {
        try {
            String base64Content = base64Data;
            if (base64Data.contains(",")) {
                base64Content = base64Data.split(",")[1];
            }
            byte[] decodedBytes = android.util.Base64.decode(base64Content, android.util.Base64.DEFAULT);

            OutputStream os = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentResolver resolver = context.getContentResolver();
                ContentValues contentValues = new ContentValues();
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "application/octet-stream");
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
                
                Uri uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues);
                if (uri != null) {
                    os = resolver.openOutputStream(uri);
                }
            } else {
                File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                if (!downloadsDir.exists()) downloadsDir.mkdirs();
                File file = new File(downloadsDir, name);
                os = new java.io.FileOutputStream(file);

                Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                scanIntent.setData(Uri.fromFile(file));
                context.sendBroadcast(scanIntent);
            }

            if (os != null) {
                os.write(decodedBytes);
                os.flush();
                os.close();
                Toast.makeText(context, "File saved: " + name, Toast.LENGTH_LONG).show();
            } else {
                throw new Exception("Could not open output stream.");
            }
        } catch (Exception e) {
            Toast.makeText(context, "Error saving file: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
