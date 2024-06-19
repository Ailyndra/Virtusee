package com.virtusee.services;

import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.net.Uri;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.JobIntentService;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.virtusee.contentprovider.AnswerContentProvider;
import com.virtusee.contentprovider.AudioContentProvider;
import com.virtusee.contentprovider.PhotoContentProvider;
import com.virtusee.db.AnswerTable;
import com.virtusee.db.VSDbHelper;
import com.virtusee.helper.ConnHelper;
import com.virtusee.helper.DateHelper;
import com.virtusee.helper.FileHelper;
import com.virtusee.helper.TokenHelper;
import com.virtusee.model.AnswerModel;
import com.virtusee.model.AnswerResponse;
import com.virtusee.model.AnswersModel;
import com.virtusee.model.TokenModel;
import com.virtusee.retrofit.VSRetrofitAdapter;
import com.virtusee.retrofit.VSRetrofitApi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.zip.Adler32;
import java.util.zip.CheckedInputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by winata on 5/25/17.
 */

public class SyncServ extends JobIntentService {
    private static final String TAG = JobService.class.getSimpleName();
    private static final int JOB_ID = 3;

    private static final String ACTION_POST_TOKEN = "SyncServ.postFirebaseToken";
    private static final String ACTION_POST_ANSWER = "SyncServ.postAll";
    private static final String ACTION_POST_RESEND = "SyncServ.resendPost";
    private static final String ACTION_POST_UPDATE = "SyncServ.postUpdate";

    private SQLiteDatabase db;
    private String idAnswer;
    private List<String> answerId = new ArrayList<String>();


    public static void enqueuePostToken(Context context, String token, String idusr, String username, String password) {
        Intent intent = new Intent(context, SyncServ.class);
        intent.setAction(ACTION_POST_TOKEN);
        intent.putExtra("token", token);
        intent.putExtra("idusr", idusr);
        intent.putExtra("username", username);
        intent.putExtra("password", password);
        enqueueWork(context, SyncServ.class, JOB_ID, intent);
    }

    public static void enqueuePostAll(Context context, String idusr, String username, String password) {
        Intent intent = new Intent(context, JobService.class);
        intent.setAction(ACTION_POST_ANSWER);
        intent.putExtra("idusr", idusr);
        intent.putExtra("username", username);
        intent.putExtra("password", password);
        enqueueWork(context, SyncServ.class, JOB_ID, intent);
    }

    public static void enqueuePostUpdate(Context context, String idusr, String username, String password, String idAnswer, ArrayList<String> photos) {
        Intent intent = new Intent(context, JobService.class);
        intent.setAction(ACTION_POST_UPDATE);
        intent.putExtra("idusr", idusr);
        intent.putExtra("username", username);
        intent.putExtra("password", password);
        intent.putExtra("idAnswer", idAnswer);
        intent.putStringArrayListExtra("photos", photos);
        enqueueWork(context, SyncServ.class, JOB_ID, intent);
    }

    public static void enqueueResend(Context context, String idusr, String username, String password) {
        Intent intent = new Intent(context, JobService.class);
        intent.setAction(ACTION_POST_RESEND);
        intent.putExtra("idusr", idusr);
        intent.putExtra("username", username);
        intent.putExtra("password", password);
        enqueueWork(context, SyncServ.class, JOB_ID, intent);
    }


    public void postFirebaseToken(Context context, String token, String idusr, String username, String password) {
        String android_id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        TokenModel tokenModel = new TokenModel(idusr, android_id, token);
        VSRetrofitApi.Auth authApi = VSRetrofitAdapter.init(VSRetrofitApi.Auth.class, username, password);
        if (idusr == null || idusr.equals("")) return;

        Call<String> call = authApi.sendToken(tokenModel);
        try {
            String res = call.execute().body();
            if (res.equals("success")) {
                TokenHelper.PutLastTokenUpdate(context);
            }
        } catch (IOException e) {
        } catch (NullPointerException e) {
        }

    }

    private void resendPost(Context context, String idusr, String username, String password) {
        String lmsg = "Start Repost...";
        Log.e("repost", lmsg);

        ArrayList<AnswerModel> answerModel = new ArrayList<AnswerModel>();

        Uri dtUri = Uri.parse(AnswerContentProvider.CONTENT_URI + "/all");

        Log.d(TAG, "sendAllData: " + idusr);

        try {

            Cursor cursor = context.getContentResolver().query(dtUri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    answerId.add(cursor.getString(cursor.getColumnIndexOrThrow(AnswerTable.COLUMN_ID)));
                    answerModel.add(new AnswerModel(
                            cursor.getString(cursor.getColumnIndexOrThrow(AnswerTable.COLUMN_ID)),
                            cursor.getString(cursor.getColumnIndexOrThrow(AnswerTable.COLUMN_ID_FORM)),
                            cursor.getString(cursor.getColumnIndexOrThrow(AnswerTable.COLUMN_ID_STORE)),
                            idusr,
                            cursor.getString(cursor.getColumnIndexOrThrow(AnswerTable.COLUMN_CONTENT)),
                            cursor.getString(cursor.getColumnIndexOrThrow(AnswerTable.COLUMN_WHEN)),
                            cursor.getString(cursor.getColumnIndexOrThrow(AnswerTable.COLUMN_LONG)),
                            cursor.getString(cursor.getColumnIndexOrThrow(AnswerTable.COLUMN_LAT)),
                            cursor.getString(cursor.getColumnIndexOrThrow(AnswerTable.COLUMN_GPS_TIME)),
                            cursor.getString(cursor.getColumnIndexOrThrow(AnswerTable.COLUMN_GPS_ACCURACY)),
                            cursor.getString(cursor.getColumnIndexOrThrow(AnswerTable.COLUMN_ENTER)),
                            cursor.getString(cursor.getColumnIndexOrThrow(AnswerTable.COLUMN_LAST_SYNC)),
                            cursor.getString(cursor.getColumnIndexOrThrow(AnswerTable.COLUMN_APP_VERSION)),
                            cursor.getString(cursor.getColumnIndexOrThrow(AnswerTable.COLUMN_DEVICE_NAME))
                    ));
                } while (cursor.moveToNext());
            }
            cursor.close();

            if (!answerModel.isEmpty()) {
                for (AnswerModel model : answerModel) {
                    ArrayList<String> photos = new ArrayList<>();
                    if (!model.answerContent.equals("1")) {
                        JsonElement content = new JsonParser().parse(model.answerContent);
                        JsonArray answerArray = content.getAsJsonArray();

                        for (JsonElement answerArr : answerArray) {
                            JsonObject object = answerArr.getAsJsonObject();
                            if (object.get("type").getAsString().equals("6")) {
                                photos.add(object.get("answer").getAsString());
                            }
                        }
                    }
                    saveImageLog(model.id, photos);
                }

                lmsg += "Uploading answer...";
                Log.e("repost", lmsg);
            } else {
                lmsg += "No Data available...";
                Log.e("repost", lmsg);
            }
        } catch (Exception e) {
            lmsg += e.getMessage();
            FileHelper.setLog(context, lmsg);
        }
    }

    private void saveImageLog(String answerId, List<String> photos) {
        if (photos == null) return;

        DateHelper dateHelper = new DateHelper();

        String whenupdate = dateHelper.getCurrentTimestamp();
        String sql = "INSERT OR REPLACE INTO photo(id_answer,img,csum,whenupdate,lastsync) VALUES (?,?,?,?,?);";
        SQLiteDatabase db = VSDbHelper.getInstance(getApplicationContext()).getWritableDatabase();
        SQLiteStatement statement = db.compileStatement(sql);
        db.beginTransaction();
        Log.e("imagerest", "start images");

        for (String photo : photos) {
            db.delete("photo", "img =?", new String[]{photo});

            File p = new File(photo);
            String csum = "";
            try {
                FileInputStream fileInputStream = new FileInputStream(p);
                CheckedInputStream chksum = new CheckedInputStream(fileInputStream, new Adler32());
                while (chksum.read() != -1) {
                    // Read file in completely
                }
                csum = String.valueOf(chksum.getChecksum().getValue());

            } catch (FileNotFoundException e) {

            } catch (IOException e) {
                e.printStackTrace();
            }
            statement.clearBindings();
            statement.bindString(1, answerId);
            statement.bindString(2, photo);
            statement.bindString(3, csum);
            statement.bindString(4, whenupdate);
            statement.bindString(5, "0");
            Log.e("imagerest", photo + "--" + csum);

            statement.execute();
        }

        db.setTransactionSuccessful();
        db.endTransaction();
    }

    public void postUpdate(Context context, String idusr, String username, String password, String idAnswer, List<String> photos) {
        Log.e("repost", "sync");

        if (!ConnHelper.hasConnection(context)) {
            FileHelper.setLog(context, "No Connection");
            return;
        }

        db = VSDbHelper.getInstance(context).getWritableDatabase();

        saveImageLog(idAnswer, photos);

        sendAllImage(context, username, password);
    }

    public void postAll(Context context, String idusr, String username, String password) {
        Log.e("repost", "sync");

        if (!ConnHelper.hasConnection(context)) {
            FileHelper.setLog(context, "No Connection");
            return;
        }

        db = VSDbHelper.getInstance(context).getWritableDatabase();

        sendAllData(context, idusr, username, password);
        sendAllImage(context, username, password);
        sendAllAudio(context, username, password);
    }


    public void sendAllData(Context context, String idusr, String username, String password) {
        String lmsg = "Start Syncing...";
        Log.e("repost", lmsg);

        ArrayList<AnswerModel> answerModel = new ArrayList<>();

        Uri dtUri = Uri.parse(AnswerContentProvider.CONTENT_URI + "/notsync");

        Log.d(TAG, "sendAllData: " + idusr);

        try {
            Cursor cursor = context.getContentResolver().query(dtUri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    answerId.add(cursor.getString(cursor.getColumnIndexOrThrow(AnswerTable.COLUMN_ID)));
                    answerModel.add(new AnswerModel(
                            cursor.getString(cursor.getColumnIndexOrThrow(AnswerTable.COLUMN_ID)),
                            cursor.getString(cursor.getColumnIndexOrThrow(AnswerTable.COLUMN_ID_FORM)),
                            cursor.getString(cursor.getColumnIndexOrThrow(AnswerTable.COLUMN_ID_STORE)),
                            idusr,
                            cursor.getString(cursor.getColumnIndexOrThrow(AnswerTable.COLUMN_CONTENT)),
                            cursor.getString(cursor.getColumnIndexOrThrow(AnswerTable.COLUMN_WHEN)),
                            cursor.getString(cursor.getColumnIndexOrThrow(AnswerTable.COLUMN_LONG)),
                            cursor.getString(cursor.getColumnIndexOrThrow(AnswerTable.COLUMN_LAT)),
                            cursor.getString(cursor.getColumnIndexOrThrow(AnswerTable.COLUMN_GPS_TIME)),
                            cursor.getString(cursor.getColumnIndexOrThrow(AnswerTable.COLUMN_GPS_ACCURACY)),
                            cursor.getString(cursor.getColumnIndexOrThrow(AnswerTable.COLUMN_ENTER)),
                            cursor.getString(cursor.getColumnIndexOrThrow(AnswerTable.COLUMN_LAST_SYNC)),
                            cursor.getString(cursor.getColumnIndexOrThrow(AnswerTable.COLUMN_APP_VERSION)),
                            cursor.getString(cursor.getColumnIndexOrThrow(AnswerTable.COLUMN_DEVICE_NAME))
                    ));
                } while (cursor.moveToNext());
            }
            cursor.close();

            if (!answerModel.isEmpty()) {
                AnswersModel answersModel = new AnswersModel(answerModel);

                VSRetrofitApi.Answer answerApi = VSRetrofitAdapter.init(VSRetrofitApi.Answer.class, username, password);

                Call<AnswerResponse> call = answerApi.sendAnswerAll2(answersModel);
                AnswerResponse res = call.execute().body();

                onDataSuccess(context, res);
                Log.d("Service", "res : " + Arrays.toString(res.idAnswer));

                lmsg += "Uploading answer...";
                Log.e("repost", lmsg);
            } else {
                lmsg += "No Data available...";
                Log.e("repost", lmsg);
            }
        } catch (Exception e) {
            lmsg += e.getMessage();
            FileHelper.setLog(context, lmsg);
        }
    }

    public void sendAllImage(Context context, String username, String password) {
        List<String> photos = new ArrayList<>();
        List<String> idPhoto = new ArrayList<>();

        Uri dtUri = Uri.parse(PhotoContentProvider.CONTENT_URI + "/notsync");

        String[] projection = {"_id", "img", "csum", "lastsync"};
        Cursor cursor = context.getContentResolver().query(dtUri, projection, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String img = cursor.getString(cursor.getColumnIndexOrThrow("img"));
                String csum = cursor.getString(cursor.getColumnIndexOrThrow("csum"));
                if (img.equals("")) continue;

                File p = new File(img);
                String csum2 = "";

                try {
                    FileInputStream fileInputStream = new FileInputStream(p);
                    CheckedInputStream chksum = new CheckedInputStream(fileInputStream, new Adler32());
                    while (chksum.read() != -1) {
                        // Read file in completely
                    }
                    csum2 = String.valueOf(chksum.getChecksum().getValue());

                    if (csum.equals(csum2)) {
                        photos.add(img);
                        idPhoto.add(cursor.getString(cursor.getColumnIndexOrThrow("_id")));
                    } else {
                        FileHelper.setLog(context, "Invalid Checksum for" + img);
                    }
                } catch (Exception e) {
                     FileHelper.setLog(context,e.getMessage() );
                }
            } while (cursor.moveToNext());
        }
        cursor.close();

        if (photos.isEmpty()) return;
        sendImage(context, null, photos, idPhoto, username, password);
    }

    public void sendImage(Context context, String answerId, List<String> photos, List<String> idPhoto, String username, String password) {
        idAnswer = answerId;

        RequestBody description = createPartFromString("Images");

        VSRetrofitApi.File photoApi = VSRetrofitAdapter.init(VSRetrofitApi.File.class, username, password);

        int idx = -1;
        for (String photo : photos) {
            idx++;

            MultipartBody.Part pp = prepareFilePart("file", photo, "image");
            if (pp == null) {
                FileHelper.setLog(context, "No Physical Image found for " + photo);
                continue;
            }

            try {
                Log.e("imageserv", photo);

                Call<String> call = photoApi.sendImage(description, pp);

                //Before
//                String tasks = call.execute().body();
//                if(tasks != null) onImageSuccess(idPhoto.get(idx));

                //After
                int finalIdx = idx;
                call.enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        if (response.body() != null) {
                            onImageSuccess(idPhoto.get(finalIdx));
                        } else {
                            FileHelper.setLog(context, "Null response when send image");
                        }
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {
                        FileHelper.setLog(context, t.getMessage());
                    }
                });
            } catch (Exception | OutOfMemoryError e) {
                FileHelper.setLog(context, e.getMessage());
            }
        }
    }

    public void sendAllAudio(Context context, String username, String password) {

        long lastSync = 0;
        List<String> audios = new ArrayList<String>();
        List<String> idAudio = new ArrayList<String>();


        Uri dtUri = Uri.parse(AudioContentProvider.CONTENT_URI + "/notsync");


        String[] projection = {"_id", "audio", "csum", "lastsync"};
        Cursor cursor = context.getContentResolver().query(dtUri, projection, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String audio = cursor.getString(cursor.getColumnIndexOrThrow("audio"));
                String csum = cursor.getString(cursor.getColumnIndexOrThrow("csum"));
                if (audio.equals("")) continue;

                File p = new File(audio);
                String csum2 = "";

                try {
                    FileInputStream fileInputStream = new FileInputStream(p);
                    CheckedInputStream chksum = new CheckedInputStream(fileInputStream, new Adler32());
                    while (chksum.read() != -1) {
                        // Read file in completely
                    }
                    csum2 = String.valueOf(chksum.getChecksum().getValue());

                    if (csum.equals(csum2)) {
                        audios.add(audio);
                        idAudio.add(cursor.getString(cursor.getColumnIndexOrThrow("_id")));
                    } else {
                        FileHelper.setLog(context, "Invalid Checksum for" + audio);
                    }
                } catch (FileNotFoundException e) {
                    // FileHelper.setLog(context,e.getMessage() );
                } catch (IOException e) {
                    FileHelper.setLog(context, e.getMessage());
                } catch (Exception e) {
                    FileHelper.setLog(context, e.getMessage());
                }


            } while (cursor.moveToNext());
        }
        cursor.close();


        if (audios.isEmpty()) return;
        sendAudio(context, null, audios, idAudio, username, password);
    }


    public void sendAudio(Context context, String answerId, List<String> audios, List<String> idAudio, String username, String password) {

        idAnswer = answerId;

        RequestBody description = createPartFromString("Audios");

        VSRetrofitApi.File audioApi = VSRetrofitAdapter.init(VSRetrofitApi.File.class, username, password);

        int idx = -1;
        for (String audio : audios) {
            idx++;

            MultipartBody.Part pp = prepareFilePart("file", audio, "audio");

            if (pp == null) {
                FileHelper.setLog(context, "No Physical Audio found for " + audio);
                continue;
            }

            try {
                Log.e("audioserv", audio);

                Call<String> call = audioApi.sendAudio(description, pp);
                String tasks = call.execute().body();

                onAudioSuccess(idAudio.get(idx));

            } catch (Exception e) {
                FileHelper.setLog(context, e.getMessage());

            } catch (OutOfMemoryError e1) {
                FileHelper.setLog(context, e1.getMessage());
            }

        }
    }

    @NonNull
    private RequestBody createPartFromString(String descriptionString) {
        return RequestBody.create(
                okhttp3.MultipartBody.FORM, descriptionString);
    }

    private MultipartBody.Part prepareFilePart(String partName, String fileUri, String type) {
        File file = new File(fileUri);

        if (!file.exists()) return null;

        RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), file);
        if (type.equals("audio"))
            requestFile = RequestBody.create(MediaType.parse("audio/*"), file);
        return MultipartBody.Part.createFormData(partName, file.getName(), requestFile);
    }

    private void onImageSuccess(String idphoto) {
        Date d = new Date();
        long now = d.getTime() / 1000;

        String sql = "update photo set lastsync = ? where _id = ?";
        SQLiteStatement statement = db.compileStatement(sql);

        db.beginTransaction();
        statement.bindString(1, Long.toString(now));
        statement.bindString(2, idphoto);
        statement.execute();
        db.setTransactionSuccessful();
        db.endTransaction();

        FileHelper.setLog(this, this.getClass().getSimpleName() + " photo");
    }

    private void onAudioSuccess(String idaudio) {
        Date d = new Date();
        long now = d.getTime() / 1000;

        String sql = "update audio set lastsync = ? where _id = ?";
        SQLiteStatement statement = db.compileStatement(sql);

        db.beginTransaction();
        statement.bindString(1, Long.toString(now));
        statement.bindString(2, idaudio);
        statement.execute();
        db.setTransactionSuccessful();
        db.endTransaction();

        FileHelper.setLog(this, this.getClass().getSimpleName() + " audio");
    }

    private void onDataSuccess(Context context, AnswerResponse s) {
        if (s == null) {
            FileHelper.setLog(context, "Null Response when sync answer");
            return;
        }

        if (s.err) {
            FileHelper.setLog(context, s.errmsg);
        }

        String sql;
        SQLiteStatement statement;
        Date d = new Date();
        long now = d.getTime() / 1000;

        if (s.idAnswer != null) {

            sql = "update answer set lastsync = ? where _id = ?;";
            statement = db.compileStatement(sql);
            db.beginTransaction();

            for (String id : s.idAnswer) {
                statement.clearBindings();
                statement.bindString(1, Long.toString(now));
                statement.bindString(2, id);
                statement.execute();
            }

            db.setTransactionSuccessful();
            db.endTransaction();

            FileHelper.setLog(this, this.getClass().getSimpleName() + " answer");
        }

    }

    @Override
    protected void onHandleWork(@Nullable Intent intent) {
        if (intent.getAction() == null) return;

        switch (intent.getAction()) {
            case ACTION_POST_TOKEN:
                postFirebaseToken(this, intent.getStringExtra("token"), intent.getStringExtra("idusr"), intent.getStringExtra("username"), intent.getStringExtra("password"));
                break;
            case ACTION_POST_ANSWER:
                postAll(this, intent.getStringExtra("idusr"), intent.getStringExtra("username"), intent.getStringExtra("password"));
                break;
            case ACTION_POST_RESEND:
                resendPost(this, intent.getStringExtra("idusr"), intent.getStringExtra("username"), intent.getStringExtra("password"));
                break;
            case ACTION_POST_UPDATE:
                postUpdate(this, intent.getStringExtra("idusr"), intent.getStringExtra("username"), intent.getStringExtra("password"), intent.getStringExtra("idAnswer"), intent.getStringArrayListExtra("photos"));
        }

    }
}
