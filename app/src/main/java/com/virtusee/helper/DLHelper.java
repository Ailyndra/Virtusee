package com.virtusee.helper;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import static android.content.Context.DOWNLOAD_SERVICE;

public class DLHelper {

    public static void downloadfile(Context ctx, String url, String filename){
        Log.e("dl",url);
        DownloadManager.Request r = new DownloadManager.Request(Uri.parse(url));

        r.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename);

        r.allowScanningByMediaScanner();

        r.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

        DownloadManager dm = (DownloadManager) ctx.getSystemService(DOWNLOAD_SERVICE);
        dm.enqueue(r);
    }

}
