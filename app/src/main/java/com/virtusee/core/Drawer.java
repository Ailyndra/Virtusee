package com.virtusee.core;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.virtusee.adapter.DrawerAdapter;
import com.virtusee.helper.AuthHelper;
import com.virtusee.helper.DLHelper;
import com.virtusee.helper.GpsHelper;
import com.virtusee.helper.PermissionHelper;
import com.virtusee.receiver.ConnectivityChangedReceiver;
import com.virtusee.receiver.ConnectivityChangedReceiver_;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.UiThread;


@EActivity
public class Drawer extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        Inbox.InboxDownloadListener {

    private static final int LOADER_ID = 2;

    private Bundle navData;
    private SearchView mSearchView;
    private String mCurFilter;
    private DrawerAdapter drawerAdapter;
    private int selectedNav = -1;
    private DrawerLayout drawer;
    private ConnectivityChangedReceiver connectivityChangedReceiver;
    private String inboxUrl;
    private String inboxFilename;

    @Extra
    String fcmId;

    @Extra
    String content;

    @Extra
    String att;

    @Bean
    AuthHelper authHelper;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.drawer);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        TextView tvPrivacyPolicy = findViewById(R.id.privacy_policy);
        tvPrivacyPolicy.setOnClickListener(v -> {
            Uri uri = Uri.parse("https://virtusee.com/privacy");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        });

        initNavView();
        registerReceiver();

        if (savedInstanceState == null) selectItem(0, true);
        else if (fcmId != null) selectItem(4, true);

    }

    private void initNavView() {

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View mHeaderView = navigationView.getHeaderView(0);

        TextView lbl_user = (TextView) mHeaderView.findViewById(R.id.lbl_user);
        TextView lbl_company = (TextView) mHeaderView.findViewById(R.id.lbl_company);

        try {
            lbl_user.setText(authHelper.getFullname());
            lbl_company.setText(authHelper.getDomain());
        } catch (NullPointerException e) {
        }

    }

    public void registerReceiver() {
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        connectivityChangedReceiver = new ConnectivityChangedReceiver_();
        registerReceiver(connectivityChangedReceiver, filter);
    }


    @AfterInject
    public void initInject() {
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        return true;
    }

    @UiThread
    public void showMessage(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first
        Log.e("drawer resume", "yes");
        boolean isLogged = authHelper.isLogged();
        //isLogged = false;
        if (!isLogged) {
            this.finish();
        }

        PermissionHelper.request(this, Manifest.permission.ACCESS_FINE_LOCATION, PermissionHelper.REQUEST_CHECK_GPS);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    void selectItem(int position) {
        selectItem(position, false);
    }

    @UiThread
    void selectItem(int position, boolean init) {
        if (position == selectedNav) return;

        Fragment fragment = null;
        String fragTitle = "";

        switch (position) {
            case 0:
                fragment = new Home_();
                fragTitle = "Activity";
                break;
            case 1:
                fragment = new Store_();
                fragTitle = "Places";
                break;
            case 2:
                fragment = new Syslog_();
                fragTitle = "System Log";
                break;
            case 3:
                fragment = new Myaccount_();
                fragTitle = "My Account";
                break;
            case 4:
                fragment = new Inbox_();
                fragTitle = "Inbox";
                break;

            default:
                break;
        }

        if (fragment == null) return;

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        if (init) {
            transaction.add(R.id.content_frame, fragment);
        } else {
            transaction.replace(R.id.content_frame, fragment);
        }
        transaction.commitAllowingStateLoss();

        try {
            getSupportActionBar().setTitle(fragTitle);
        } catch (NullPointerException e) {
        }
        drawer.closeDrawer(GravityCompat.START);

        selectedNav = position;
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_main) {
            selectItem(0);
            // Handle the camera action
        } else if (id == R.id.nav_places) {
            selectItem(1);

        } else if (id == R.id.nav_inbox) {
            selectItem(4);

        } else if (id == R.id.nav_log) {
            selectItem(2);

        } else if (id == R.id.nav_account) {
            selectItem(3);
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (connectivityChangedReceiver != null) unregisterReceiver(connectivityChangedReceiver);
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        /*
        if(intent.getStringExtra("frm")!=null)
        {
            if(selectedNav==4) {
                Inbox_ fragment = (Inbox_) getFragmentManager().findFragmentById(R.id.content_frame);
                fragment.resetLoader();
            }
        }
        */
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //final LocationSettingsStates states = LocationSettingsStates.fromIntent(data);
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case GpsHelper.REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK: {
                        // All required changes were successfully made
                        break;
                    }
                    case Activity.RESULT_CANCELED: {
                        // The user was asked to change settings, but chose not to
                        finish();
                        break;
                    }
                    default: {
                        break;
                    }
                }
                break;
        }
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        if (fragment instanceof Inbox) {
            Inbox inbox = (Inbox) fragment;
            inbox.setInboxDownloadListener(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
//        Log.e("permission","ini dari drawer");
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != PermissionHelper.REQUEST_CHECK_STORAGE) {
            PermissionHelper.result(this, requestCode, permissions, grantResults);
        } else {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (inboxUrl != null && inboxFilename != null) {
                    DLHelper.downloadfile(this, inboxUrl, inboxFilename);
                }
            }
        }
    }

    @Override
    public void onDownload(String url, String filename) {
        inboxUrl = url;
        inboxFilename = filename;
    }
}


