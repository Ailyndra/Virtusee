package com.virtusee.services;

import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.virtusee.db.VSDbHelper;
import com.virtusee.helper.FileHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.core.app.JobIntentService;

/**
 * Created by winata on 5/25/17.
 */

public class CleanerServ extends JobIntentService {

    private static final String TAG = JobService.class.getSimpleName();
    private static final int JOB_ID = 2;
    private SQLiteDatabase db;
    private Context context;

    /**
     * Convenience method for enqueuing work in to this service.
     */
    public static void enqueueWork(Context context) {
        Intent intent = new Intent(context, CleanerServ.class);
        enqueueWork(context, CleanerServ.class, JOB_ID, intent);
    }

    public void clearPastData() {
        context = getApplicationContext();

        try {
            if (db == null) db = VSDbHelper.getInstance(context).getWritableDatabase();
        } catch (Exception e){ return; }

        clearAnswer();
        clearPhotos();
        clearLog();
    }

    public void clearLog() {
        try {
            long length = new File(getFilesDir().getAbsolutePath() + "/virtusee.err.log").length();
            if (length > (50 * 1000)) FileHelper.clearLog(context);
        } catch (Exception e){}
    }

    public void clearAnswer(){
        String sql = "delete FROM `answer` where id_form in (select _id from form where sticky < 2) and (lastsync > 0 or lastsync is not null or lastsync <> '') and date(whenupdate) <= date('now','-14 day')";
        db.rawQuery(sql,null);

        FileHelper.setLog(this, this.getClass().getSimpleName() +" clearAnswer");
    }

    public void clearPhotos() {
        List<String> photos = new ArrayList<String>();

        String sql = "SELECT a.img FROM `photo` a left join answer b on a.id_answer = b._id where b._id is null and a.lastsync > 0";
        Cursor cursor = db.rawQuery(sql, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String img = cursor.getString(cursor.getColumnIndexOrThrow("img"));
                photos.add(img);
            } while (cursor.moveToNext());
        }
        cursor.close();

        sql = "delete FROM `photo` where lastsync > 0 and id_answer not in (select _id from answer)";
        db.rawQuery(sql, null);

        if (photos.isEmpty()) return;

        for (String photo : photos) {
            File file = new File(photo);

            if(!file.exists()) continue;
            file.delete();
        }

        FileHelper.setLog(this, this.getClass().getSimpleName() +" clearPhotos");
    }

    @Override
    protected void onHandleWork(@Nullable Intent intent) {
        clearPastData();
    }
}
