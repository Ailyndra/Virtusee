package com.virtusee.core;

import android.Manifest;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;
import com.virtusee.db.PhotoTable;
import com.virtusee.db.VSDbHelper;
import com.virtusee.helper.AuthHelper;
import com.virtusee.helper.DateHelper;
import com.virtusee.helper.FileHelper;
import com.virtusee.helper.InboxHelper;
import com.virtusee.helper.PermissionHelper;
import com.virtusee.helper.PrefHelper_;
import com.virtusee.helper.TokenHelper;
import com.virtusee.receiver.ConnectivityChangedReceiver;
import com.virtusee.restful.AuthRest;
import com.virtusee.restful.DataRest;
import com.virtusee.restful.MasterRest;
import com.virtusee.restful.SessionRest;
import com.virtusee.retrofit.VSRetrofitListener;
import com.virtusee.services.CleanerServ;
import com.virtusee.services.SyncServ;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;

@EActivity
public class Splash extends AppCompatActivity implements VSRetrofitListener {

	@Pref
    PrefHelper_ myPrefs;

    @Bean
    AuthRest authRest;

    @Bean
    SessionRest sessionRest;

    @Bean
    DataRest dataRest;

    @Bean
    MasterRest masterRest;

	@Bean
    AuthHelper authHelper;

	@Bean
	DateHelper dateHelper;


    @ViewById
    TextView splashTitle;


//	private static final int currver = 2;
private Bundle navData;
	private int isauto = 0;
    private SQLiteDatabase db;
    private ConnectivityChangedReceiver connectivityChangedReceiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);

        // Handle possible data accompanying notification message.
        // [START handle_data_extras]
        if (getIntent().getExtras() != null) {
            this.navData = getIntent().getExtras();
            /*
            for (String key : getIntent().getExtras().keySet()) {
                Object value = getIntent().getExtras().get(key);
                this.navData.putString(key, value.toString());
            }
            */
        }
        // [END handle_data_extras]
    }
    
	@AfterViews
	void initSession() {
		Log.e("tesdulu", "startview");

		boolean isLogged = authHelper.isLogged();
        String appid = authHelper.getAppId();
        if(appid.equals("")) authHelper.genAppId(this);

        splashTitle.setText(authHelper.getDomain());

        if(!isLogged) {
			Login_.intent(this).start();
			this.finish();
		} else {
            String fullname = authHelper.getFullname();
            if(fullname.equals("")) authRest.getProfile(this,this);
            sessionRest.checkSess(this,this);
		}


	}

	@AfterInject
	void checkSession(){
		Log.e("tesdulu", "startinject");
		isauto = dateHelper.init(this);

		dataRest.init(this);
		masterRest.init(this);
    }


    private void initFirebase(){
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener( this, instanceIdResult -> {
//            long lastsync = TokenHelper.GetLastTokenUpdate(getApplicationContext());
//            String savedToken = TokenHelper.GetToken(getApplicationContext());
            String token = instanceIdResult.getToken();
            TokenHelper.PutToken(getApplicationContext(),token);
            SyncServ.enqueuePostToken(getApplicationContext(),token,authHelper.getUserid(),authHelper.getFormattedUsername(),authHelper.getPassword());
        });
    }

    private void undoSyncImages(){
        final ContentValues values = new ContentValues();
        values.putNull(PhotoTable.COLUMN_LAST_SYNC);
        db = VSDbHelper.getInstance(getApplicationContext()).getWritableDatabase();
        db.update("photo",values,null,null);
        FileHelper.setLog(this, this.getClass().getSimpleName() +" update photo");
    }


	public void initSync(){
        long ri = myPrefs.resendImage().get();
        String appdomain = authHelper.getDomain();
        Log.e("sess","init sync");
        dataRest.getData(this);
        if(ri!=210726 && appdomain.equals("pnginstore")) {
            SyncServ.enqueueResend(this,authHelper.getUserid(),authHelper.getFormattedUsername(),authHelper.getPassword());

            String source = FileHelper.getPrivateImageFolder(this).getAbsolutePath();
            String destination = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath();
            copyFileOrDirectory(source, destination);

            myPrefs.resendImage().put((long) 210726);
        }


        SyncServ.enqueuePostAll(this,authHelper.getUserid(),authHelper.getFormattedUsername(),authHelper.getPassword());
        CleanerServ.enqueueWork(this);
	}

	public void initMaster() {
        masterRest.getMaster(this);
        masterRest.getAbsensi(this);
    }

	@UiThread(delay=1000)
	public void startMain() {
        String isi = "";
        String att = "";

        if (this.navData!=null && this.navData.getString("google.message_id") != null){
            if(this.navData.getString("content") != null) isi = this.navData.getString("content");
            if(this.navData.getString("att") != null) att = this.navData.getString("att");
            InboxHelper.SaveToDb(this,isi,att);
            Drawer_.intent(this).fcmId(this.navData.getString("google.message_id")).start();
        } else {
            Drawer_.intent(this).start();
        }

		this.finish();
    }

	@Override
	public void onSuccess(String code) {
        AsyncTask.execute(() -> {
            if(code.equals("sess")){
                initMaster();
            } else if(code.equals("master")) {
                initSync();
            } else {
                startMain();
                initFirebase();
            }
        });
	}

	@Override
	public void onFailure(String code) {
//        Log.e("sess","failure " + code);
        if(code.equals("sess")){
            db = VSDbHelper.getInstance(getApplicationContext()).getWritableDatabase();
            authHelper.clearCredentials();
            Toast.makeText(getApplicationContext(), "Duplicate account login detected. Logging out automatically!", Toast.LENGTH_LONG).show();

            db.beginTransaction();
            db.delete("form", null, null);
            db.delete("form_tag", null, null);
            db.delete("store", null, null);
            db.delete("store_tag", null, null);
            db.delete("photo", null, null);
            db.delete("answer", null, null);
            db.delete("inbox", null, null);
            db.delete("master", null, null);
            db.setTransactionSuccessful();
            db.endTransaction();

            FileHelper.setLog(this, this.getClass().getSimpleName() +" delete");

            this.finish();
        } else {
            if(!code.equals("noconn")) Toast.makeText(getApplicationContext(), "Sync failed", Toast.LENGTH_SHORT).show();
            startMain();
        }
	}



    @Override
    public void onSaveInstanceState(Bundle outState) {
        if(this.navData!=null) outState.putAll(this.navData);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        this.navData = (Bundle) savedInstanceState.clone();
    }

    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first
        PermissionHelper.request(this, Manifest.permission.ACCESS_FINE_LOCATION,PermissionHelper.REQUEST_CHECK_GPS);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionHelper.result(this, requestCode, permissions, grantResults);
    }

    private void copyFileOrDirectory(String srcDir, String dstDir) {
        try {
            File src = new File(srcDir);
            File dst = new File(dstDir, src.getName());

            if (src.isDirectory()) {

                String files[] = src.list();
                int filesLength = files.length;
                for (int i = 0; i < filesLength; i++) {
                    String src1 = (new File(src, files[i]).getPath());
                    String dst1 = dst.getPath();
                    copyFileOrDirectory(src1, dst1);

                }
            } else {
                copyFile(src, dst);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void copyFile(File sourceFile, File destFile) throws IOException {
        if (!destFile.getParentFile().exists())
            destFile.getParentFile().mkdirs();

        if (!destFile.exists()) {
            destFile.createNewFile();
        }

        FileChannel source = null;
        FileChannel destination = null;

        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        } finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }
    }
}



