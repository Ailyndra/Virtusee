package com.virtusee.core;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.virtusee.helper.AuthHelper;
import com.virtusee.helper.PermissionHelper;
import com.virtusee.helper.PrefHelper_;
import com.virtusee.listener.GpsListener;
import com.virtusee.restful.AuthRest;
import com.virtusee.retrofit.VSRetrofitListener;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;

import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;

@EActivity
public class Login extends Activity implements VSRetrofitListener,GpsListener {
    @ViewById
    CoordinatorLayout loginLayout;

    @ViewById
    EditText username;

    @ViewById
    EditText password;

    @ViewById
    EditText domain;

    @ViewById
    Button btnLogin;

    @Bean
    AuthRest authRest;

    @Bean
    AuthHelper authHelper;


    @Pref
    PrefHelper_ myPrefs;

    private String dom;
    private String usr;
    private String pass;
    private String loginText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
    }


    @UiThread
    public void showMessage(String message) {
        Snackbar mySnackbar = Snackbar.make(loginLayout, message, Snackbar.LENGTH_LONG);

        View snackView = mySnackbar.getView();
        mySnackbar.getView().setBackgroundColor(Color.rgb(255, 255, 255));
        TextView tv = (TextView) snackView.findViewById(com.google.android.material.R.id.snackbar_text);
        tv.setTextColor(Color.BLACK);
        mySnackbar.show();
    }


    @Click(R.id.btnLogin)
    void myButtonLogin() {

        usr = username.getText().toString();
        dom = domain.getText().toString();
        pass = password.getText().toString();
        EditText focusView = null;
        boolean cancel = false;

        if (TextUtils.isEmpty(pass)) {
            password.setError(getString(R.string.error_field_required));
            cancel = true;
            focusView = password;
        }

        if (TextUtils.isEmpty(usr)) {
            username.setError(getString(R.string.error_field_required));
            cancel = true;
            focusView = username;
        }

        if (TextUtils.isEmpty(dom)) {
            domain.setError(getString(R.string.error_field_required));
            cancel = true;
            focusView = domain;
        }


        if (cancel) {
            focusView.requestFocus();
        } else {
            Log.d("Login", "Login");
            setButtonLoginState(false);
            authRest.checkAuth(this,dom, usr, pass, this);
        }
    }

    private void setButtonLoginState(boolean enabled) {
        String loading = "Please wait...";
        if (enabled) {
            btnLogin.setEnabled(true);
            btnLogin.setText(loginText);
        } else {
            if (loginText == null) loginText = btnLogin.getText().toString();
            btnLogin.setEnabled(false);
            btnLogin.setText(loading);
        }
    }

    @Override
    public void onSuccess(String code) {
        Splash_.intent(this).start();
        this.finish();
    }

    @Override
    public void onFailure(String code) {
        showMessage("Login failed");
        setButtonLoginState(true);
    }

    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            new AlertDialog.Builder(this)
                    .setIcon(R.drawable.ic_place)
                    .setTitle("Akses Lokasi")
                    .setMessage("Virtusee mengumpulkan data lokasi untuk mengaktifkan lokasi saat mengisi formulir dan kegiatan absensi pada latar belakang secara real-time.")
                    .setPositiveButton("Setuju", (dialog, which) -> {
                        PermissionHelper.request_multi(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.CAMERA}, PermissionHelper.REQUEST_CHECK_PERM);
                    })
                    .setNegativeButton("Tolak", ((dialog, which) -> finish()))
                    .setCancelable(false)
                    .show();
        }
    }


    @Override
    public void onGpsSet(Location location) {

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
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        PermissionHelper.result(this,requestCode, permissions, grantResults);
    }

}

