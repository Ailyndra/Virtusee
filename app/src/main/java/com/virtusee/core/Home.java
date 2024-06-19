package com.virtusee.core;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Fragment;
import android.app.LoaderManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.virtusee.adapter.StoreAdapter;
import com.virtusee.contentprovider.AnswerContentProvider;
import com.virtusee.contentprovider.HistoryContentProvider;
import com.virtusee.db.AnswerTable;
import com.virtusee.helper.AbsenHelper;
import com.virtusee.helper.AuthHelper;
import com.virtusee.helper.DateHelper;
import com.virtusee.helper.GpsHelper;
import com.virtusee.helper.PrefHelper_;
import com.virtusee.helper.UtilHelper;
import com.virtusee.listener.DialogListener;
import com.virtusee.listener.GpsListener;
import com.virtusee.listener.HidingScrollListener;
import com.virtusee.listener.MyRecyclerListener;
import com.virtusee.receiver.WdayReceiver;
import com.virtusee.recycleradapter.ActivityRecyclerAdapter;
import com.virtusee.restful.DataRest;
import com.virtusee.restful.SessionRest;
import com.virtusee.retrofit.VSRetrofitListener;
import com.virtusee.services.CleanerServ;
import com.virtusee.services.SyncServ;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.util.Calendar;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;


@EFragment(R.layout.home)
public class Home extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,GpsListener,DialogListener,MyRecyclerListener,VSRetrofitListener {

    private static final int LOADER_ID = 5;
    private static final int LOADER_TOKO_ID = 51;
    private static final int LOADER_FORM_ID = 52;
    private static final int ALARM_ID = 123;    

    private SearchView mSearchView;
    private String mCurFilter;
    private StoreAdapter storeAdapter;
    private Context ctx;
    private int absenstatus;
    private Location lastLocation;
    private int mToolbarHeight;
    private Activity act;

    private BroadcastReceiver wdayReceiver;

    @ViewById
    RecyclerView activityView;

    @Bean
    ActivityRecyclerAdapter activityRecyclerAdapter;

    @Bean
    GpsHelper gpsHelper;

    @Bean
    AuthHelper authHelper;

    @Bean
    AbsenHelper absenHelper;

    @ViewById
    ImageButton btnWdayStart;

    @ViewById
    CardView absen_layout;

    @ViewById
    ImageButton btnWdayStop;


    @ViewById
    TextView txtStoreCount;

    @ViewById
    TextView txtFormCount;

    @ViewById
    ImageButton btnWdayResume;


    @ViewById
    ImageButton btnWdayPause;

    @ViewById
    TextView txtWdayInfo;

    @Bean
    DateHelper dateHelper;

    @Pref
    PrefHelper_ myPrefs;

    SwipeRefreshLayout refreshLayout;

    @Bean
    DataRest dataRest;

    @Bean
    SessionRest sessionRest;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        if(savedInstanceState!=null) lastLocation = savedInstanceState.getParcelable("lastLocation");

        if(act==null) act = getActivity();
        if(ctx==null) ctx = act.getApplicationContext();
        return null;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        refreshLayout = view.findViewById(R.id.refreshLayout);
        refreshLayout.setOnRefreshListener(() -> {
            sessionRest.checkSess(ctx, this);
        });
    }

    @AfterInject
    public void initInject() {
        if(act==null) act = getActivity();
        if(ctx==null) ctx = act.getApplicationContext();

        dateHelper.init(act);
        absenHelper.init(act);
        gpsHelper.init(act, this);
        dataRest.init(this);

        if (lastLocation != null && gpsHelper!=null){
            gpsHelper.setStartLoc(lastLocation);
            onGpsSet(lastLocation);
        }

    }

    @AfterViews
    void initList() {
        if(act==null) act = getActivity();
        if(ctx==null) ctx = act.getApplicationContext();

        // Set an Adapter to the ListView
        setupRecyclerView();
        setupAdapter();
        Log.e("view", "initstore");
    }

    private void setupRecyclerView() {
        Toolbar toolbar = (Toolbar) act.findViewById(R.id.toolbar);
        mToolbarHeight = toolbar.getHeight()+50;
        activityView.setLayoutManager(new LinearLayoutManager(ctx));
        activityView.addOnScrollListener(new HidingScrollListener() {
            @Override
            public void onShow() {
                absen_layout.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).start();
            }

            @Override
            public void onHide() {
                absen_layout.animate().translationY(mToolbarHeight).setInterpolator(new AccelerateInterpolator(2)).start();
            }
        });
    }

    private void setupAdapter(){
        activityRecyclerAdapter.setOnRecyclerItemClickedListener(this);
        getLoaderManager().initLoader(LOADER_ID, null, this);
        getLoaderManager().initLoader(LOADER_TOKO_ID, null, this);
        getLoaderManager().initLoader(LOADER_FORM_ID, null, this);
        activityView.setAdapter(activityRecyclerAdapter);
    }


    private void initWorkday(){
        SyncServ.enqueuePostAll(getActivity().getApplicationContext(),authHelper.getUserid(),authHelper.getFormattedUsername(),authHelper.getPassword());
        absenstatus = absenHelper.GetStatusWday();
        switch (absenstatus){
            case 0:
                // -- haven't cin --//
                disp_wdayFirst();
                break;
            case 1:
                // -- status cin --//
                disp_wdayStart();
                break;
            case 3:
                // -- status pause --//
                disp_wdayPause();
                break;
            case 2:
                // -- status stop --//
                disp_wdayStop();
                break;
        }
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu,inflater);

        menu.clear();
        act.getMenuInflater().inflate(R.menu.main, menu);
        MenuItem searchItem = menu.findItem(R.id.searchView);

    }

    private void disp_wdayFirst(){
        btnWdayStart.setVisibility(View.VISIBLE);
        btnWdayStop.setVisibility(View.GONE);
        btnWdayPause.setVisibility(View.GONE);
        btnWdayResume.setVisibility(View.GONE);
        txtWdayInfo.setText("Welcome. Click button to start the day!");
    }

    private void disp_wdayStart() {
        btnWdayStart.setVisibility(View.GONE);
        btnWdayStop.setVisibility(View.VISIBLE);
        btnWdayPause.setVisibility(View.VISIBLE);
        btnWdayResume.setVisibility(View.GONE);
        txtWdayInfo.setText("Workday started!");
    }

    private void disp_wdayPause() {
        btnWdayResume.setVisibility(View.VISIBLE);
        btnWdayStop.setVisibility(View.VISIBLE);
        btnWdayPause.setVisibility(View.GONE);
        btnWdayStart.setVisibility(View.GONE);
        txtWdayInfo.setText("Workday paused!");
    }

    private void disp_wdayResume() {
        btnWdayResume.setVisibility(View.GONE);
        btnWdayPause.setVisibility(View.VISIBLE);
        btnWdayStart.setVisibility(View.GONE);
        btnWdayStop.setVisibility(View.VISIBLE);
        txtWdayInfo.setText("Workday resumed!");
    }

    private void disp_wdayStop() {
        btnWdayResume.setVisibility(View.GONE);
        btnWdayStop.setVisibility(View.GONE);
        btnWdayPause.setVisibility(View.GONE);
        btnWdayStart.setVisibility(View.GONE);
        txtWdayInfo.setText("Workday stopped!");
    }


    @Click(R.id.btnWdayStart)
    void wdayStart() {
        UtilHelper.Alert(act,this,"Start Workday?","",android.R.drawable.ic_dialog_alert,1);
    }

    @Click(R.id.btnWdayPause)
    void wdayPause() {
        UtilHelper.Alert(act,this,"Pause Workday?","",android.R.drawable.ic_dialog_alert,3);
        boolean ok = saveWday(3);
        if(ok) disp_wdayPause();
    }

    @Click(R.id.btnWdayStop)
    void wdayStop() {
        String domain = myPrefs.domain().get();
        Log.d("Home", "domain: " + domain);
        if(!domain.equals("storehunter") && !domain.equals("aqua")) {
            stopDay();
            return;
        }

        Uri dtUri = Uri.parse(AnswerContentProvider.CONTENT_URI + "/count");
        Cursor cursor = getActivity().getContentResolver().query(dtUri, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int tot = cursor.getInt(cursor.getColumnIndex("total"));
            int tot_checkout = myPrefs.total_checkout().get();
            if (tot > tot_checkout) {
                stopDay();
                return;
            }
        }

        Uri uri = null;
        if (domain.equals("storehunter")) uri = Uri.parse(AnswerContentProvider.CONTENT_URI + "/form/5d5553c0d1439936012e70af/today");
        else if (domain.equals("aqua")) uri = Uri.parse(AnswerContentProvider.CONTENT_URI + "/form/6332557b80ab887da98b4567/today");
        Cursor cursorForm = getActivity().getContentResolver().query(uri, null, null, null, null);
        if(cursorForm != null && cursorForm.getCount() != 0) {
            stopDay();
            return;
        }

        UtilHelper.Alert(getActivity(), this, "Kuota belum terpenuhi", "", android.R.drawable.ic_dialog_alert, 0);
    }

    private void stopDay() {
        UtilHelper.Alert(act,this,"End Workday?","",android.R.drawable.ic_dialog_alert,2);
        boolean ok = saveWday(2);
        if(ok) disp_wdayStop();
    }

    @Click(R.id.btnWdayResume)
    void wdayResume() {
        UtilHelper.Alert(act,this,"Resume Workday?","",android.R.drawable.ic_dialog_alert,4);
        boolean ok = saveWday(1);
        if(ok) disp_wdayResume();
    }

    private boolean saveWday(int status) {
        Location currentLocation = gpsHelper.getLastLocation();
        if(currentLocation == null && lastLocation!=null) currentLocation = lastLocation;

        absenHelper.SaveAbsen(status,currentLocation);
        SyncServ.enqueuePostAll(getActivity().getApplicationContext(),authHelper.getUserid(),authHelper.getFormattedUsername(),authHelper.getPassword());
//        resetLoader();
        return true;
    }


    @UiThread
    public void showMessage(String message) {
        Toast.makeText(ctx, message, Toast.LENGTH_SHORT).show();
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader loader;
        Uri baseUri;

        switch(id){
            case LOADER_ID:
                baseUri = Uri.parse(HistoryContentProvider.CONTENT_URI + "/activity/30");
                loader = new CursorLoader(act,
                        baseUri, null, null, null, null);
                break;
            case LOADER_TOKO_ID:
                baseUri = Uri.parse(HistoryContentProvider.CONTENT_URI + "/stats/toko");
                loader = new CursorLoader(act,
                        baseUri, null, null, null, null);
                break;
            case LOADER_FORM_ID:
                baseUri = Uri.parse(HistoryContentProvider.CONTENT_URI + "/stats/form");
                loader = new CursorLoader(act,
                        baseUri, null, null, null, null);
                break;
            default:
                loader = null;
                break;
        }
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(data==null) return;
        int lid = loader.getId();

        switch(lid){
            case LOADER_ID:
                activityRecyclerAdapter.swapCursor(data);
                break;
            case LOADER_TOKO_ID:
                data.moveToFirst();
                txtStoreCount.setText(data.getString(data.getColumnIndexOrThrow("total")));
                break;
            case LOADER_FORM_ID:
                data.moveToFirst();
                txtFormCount.setText(data.getString(data.getColumnIndexOrThrow("total")));
                break;
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        activityRecyclerAdapter.swapCursor(null);
    }

    @UiThread
    public void resetLoader(){
        act.getLoaderManager().restartLoader(LOADER_ID, null, this);
    }


    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first
        boolean isLogged = authHelper.isLogged();
        if(!isLogged) {
            act.finish();
        }

        gpsHelper.checkGPSOn();

        IntentFilter intentFilter = new IntentFilter("com.virtusee.receiver.WDAY");


        wdayReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int id = intent.getIntExtra("actionid",0);
                if(id==42) disp_wdayStop();
            }
        };

        act.registerReceiver(wdayReceiver, intentFilter);
        initWorkday();
    }


    @Override
    public void onPause(){
        super.onPause();
        lastLocation = gpsHelper.getLastLocation();
        act.unregisterReceiver(wdayReceiver);
    }


    @Override
    public void onStart() {
        super.onStart();
        gpsHelper.connect();
    }


    @Override
    public void onStop() {
        gpsHelper.disconnect();
        super.onStop();
    }

    @Override
    public void onGpsSet(Location location) {
        if (location != null) {
            lastLocation = location;
        }

    }

    @Override
    public void onGpsMock() {

    }

    @Override
    public void onGpsOff() {

    }

    @Override
    public void onGpsSearch() {

    }



    @Override
    public void onSaveInstanceState(Bundle outState) {
        lastLocation = gpsHelper.getLastLocation();
        outState.putParcelable("lastLocation",lastLocation);

        super.onSaveInstanceState(outState);
    }


    @Override
    public void OnDialogOkay(int type) {
        boolean ok;

        switch (type){
            case 1:
                ok = saveWday(1);
                if(ok){
                    setWdayLimit(10);
                    disp_wdayStart();
                }
                break;
            case 2:
                ok = saveWday(2);
                if(ok){
                    cancelWdayLimit();
                    disp_wdayStop();
                }
                break;
            case 3:
                ok = saveWday(3);
                if(ok) disp_wdayPause();
                break;
            case 4:
                ok = saveWday(1);
                if(ok) disp_wdayStart();
                break;
            default:
                break;
        }
    }

    @Override
    public void OnDialogCancel(int type) {

    }

    private void setWdayLimit(int hrs) {

        Intent notificationIntent = new Intent(ctx, WdayReceiver.class);
        PendingIntent pendingIntent = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            pendingIntent = PendingIntent.getBroadcast(ctx, ALARM_ID, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        } else {
            pendingIntent = PendingIntent.getBroadcast(ctx, ALARM_ID, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR, hrs);

        AlarmManager alarmManager = (AlarmManager)ctx.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pendingIntent);
    }

    private void cancelWdayLimit(){

        Intent notificationIntent = new Intent(ctx, WdayReceiver.class);
        PendingIntent pendingIntent = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            pendingIntent = PendingIntent.getBroadcast(ctx, ALARM_ID, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        } else {
            pendingIntent = PendingIntent.getBroadcast(ctx, ALARM_ID, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        AlarmManager am = (AlarmManager)ctx.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pendingIntent);
    }

    @Override
    public void onRecyclerItemClicked(Cursor cursor) {
        int id = cursor.getInt(cursor.getColumnIndex(AnswerTable.COLUMN_ID));
        if(id>=0) FormHist_.intent(this).idAnswer(cursor.getString(cursor.getColumnIndex(AnswerTable.COLUMN_ID))).start();
    }

    @Override
    public void onRecyclerLongClicked(Cursor cursor) {

    }

    @Background
    @Override
    public void onSuccess(String code) {
        if (code.equals("sess")) {
            dataRest.getData(ctx);
            CleanerServ.enqueueWork(ctx);
            refreshLayout.setRefreshing(false);
        }
    }

    @Override
    public void onFailure(String code) {
        Toast.makeText(ctx, "Failed", Toast.LENGTH_SHORT).show();
        refreshLayout.setRefreshing(false);
    }
}



