package com.virtusee.core;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.virtusee.contentprovider.InboxContentProvider;
import com.virtusee.db.InboxTable;
import com.virtusee.db.VSDbHelper;
import com.virtusee.helper.AbsenHelper;
import com.virtusee.helper.AuthHelper;
import com.virtusee.helper.DLHelper;
import com.virtusee.helper.DateHelper;
import com.virtusee.helper.FileHelper;
import com.virtusee.helper.PermissionHelper;
import com.virtusee.helper.UtilHelper;
import com.virtusee.listener.DialogListener;
import com.virtusee.listener.MyRecyclerListener;
import com.virtusee.recycleradapter.InboxRecyclerAdapter;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static android.content.Context.DOWNLOAD_SERVICE;


@EFragment(R.layout.inbox)
public class Inbox extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,MyRecyclerListener, DialogListener {
    InboxDownloadListener callback;

    public void setInboxDownloadListener(InboxDownloadListener callback) {
        this.callback = callback;
    }

    // This interface can be implemented by the Activity, parent Fragment,
    // or a separate test implementation.
    public interface InboxDownloadListener {
        public void onDownload(String url, String filename);
    }

    private static final int LOADER_ID = 9;

    private Context ctx;
    private int absenstatus;
    private Activity act;
    private BroadcastReceiver inboxReceiver;
    private LinearLayoutManager lm;
    private boolean isStorageGranted;

    @ViewById
    RecyclerView inboxView;

    @Bean
    InboxRecyclerAdapter inboxRecyclerAdapter;

    @Bean
    AuthHelper authHelper;

    @Bean
    AbsenHelper absenHelper;

    @Bean
    DateHelper dateHelper;

    private String url;
    private String filename;
    private int inboxid;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        if(act==null) act = getActivity();
        if(ctx==null) ctx = act.getApplicationContext();
        return null;
    }


    @AfterInject
    public void initInject() {
        if(act==null) act = getActivity();
        if(ctx==null) ctx = act.getApplicationContext();

        dateHelper.init(act);
        absenHelper.init(act);
    }

    @AfterViews
    void initList() {
        if(act==null) act = getActivity();
        if(ctx==null) ctx = act.getApplicationContext();

        // Set an Adapter to the ListView
        setupRecyclerView();
        setupAdapter();
        Log.e("view", "initinbox");
    }

    private void setupRecyclerView() {
        lm = new LinearLayoutManager(ctx);
        lm.setStackFromEnd(true);
        inboxView.setLayoutManager(lm);
    }

    private void setupAdapter(){
        inboxRecyclerAdapter.setOnRecyclerItemClickedListener(this);
        getLoaderManager().initLoader(LOADER_ID, null, this);
        inboxView.setAdapter(inboxRecyclerAdapter);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu,inflater);

        menu.clear();
        act.getMenuInflater().inflate(R.menu.main, menu);
        MenuItem searchItem = menu.findItem(R.id.searchView);

    }


    @UiThread
    public void showMessage(String message) {
        Toast.makeText(ctx, message, Toast.LENGTH_SHORT).show();
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if(id==LOADER_ID){
            Uri baseUri;

            baseUri = InboxContentProvider.CONTENT_URI;
            CursorLoader cursorLoader = new CursorLoader(act,
                    baseUri, null, null, null, null);

            return cursorLoader;
        }
        else return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        inboxRecyclerAdapter.swapCursor(data);
        lm.scrollToPosition(data.getCount()-1);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        inboxRecyclerAdapter.swapCursor(null);
    }

    @UiThread
    public void resetLoader(){
        act.getLoaderManager().restartLoader(LOADER_ID, null, this);
    }


    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first
        boolean isLogged = authHelper.isLogged();

        IntentFilter intentFilter = new IntentFilter("com.virtusee.receiver.INBOX");


        inboxReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int id = intent.getIntExtra("actionid",0);
                if(id==10) resetLoader();
            }
        };

        act.registerReceiver(inboxReceiver, intentFilter);


        if(!isLogged) {
            act.finish();
        }

    }


    @Override
    public void onPause(){
        super.onPause();
        act.unregisterReceiver(inboxReceiver);
    }


    @Override
    public void onStart() {
        super.onStart();
    }


    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    private void logdownload(int id){
        String sql;
        SQLiteDatabase db= VSDbHelper.getInstance(ctx).getWritableDatabase();
        SQLiteStatement statement;
        Date d = new Date();
        long now = d.getTime()/1000;

        sql = "update inbox set last_download = ? where _id = ?;";
        statement = db.compileStatement(sql);
        db.beginTransaction();

        statement.bindString(1, Long.toString(now));
        statement.bindLong(2, id);
        statement.execute();

        db.setTransactionSuccessful();
        db.endTransaction();

        FileHelper.setLog(getActivity().getApplicationContext(), this.getClass().getSimpleName() +" inbox");
    }


    @Override
    public void onRecyclerItemClicked(Cursor cursor) {
        inboxid = cursor.getInt(cursor.getColumnIndex(InboxTable.COLUMN_ID));
        url = cursor.getString(cursor.getColumnIndex(InboxTable.COLUMN_URL));
        filename = cursor.getString(cursor.getColumnIndex(InboxTable.COLUMN_CONTENT));
        long lastdownload = cursor.getInt(cursor.getColumnIndex(InboxTable.COLUMN_DOWNLOAD));

        if(url.equals("")) return;

        isStorageGranted = PermissionHelper.request(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE,PermissionHelper.REQUEST_CHECK_STORAGE);
        if(!isStorageGranted) {
            callback.onDownload(url,filename);
            return;
        }

        if(lastdownload==0) {
            DLHelper.downloadfile(getActivity().getApplicationContext(),url,filename);
            logdownload(inboxid);
        } else {
            String submsg = "";
            Log.e("dl",String.valueOf(lastdownload));
            Date ddate = new Date(lastdownload*1000);
            DateFormat formatter = new SimpleDateFormat("EEEE, d MMM yyyy HH:mm");
            submsg = "Previous download was on "+formatter.format(ddate);

            UtilHelper.Alert(act,this,"Redownload file?",submsg,android.R.drawable.ic_dialog_alert);
        }
    }

    @Override
    public void onRecyclerLongClicked(Cursor cursor) {

    }

    @Override
    public void OnDialogOkay(int type) {
        if(url!=null && filename!=null) {
            DLHelper.downloadfile(getActivity().getApplicationContext(),url,filename);
            logdownload(inboxid);
        }
    }

    @Override
    public void OnDialogCancel(int type) {

    }
}



