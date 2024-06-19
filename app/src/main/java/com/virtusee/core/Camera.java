package com.virtusee.core;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;

@EActivity
public class Camera extends AppCompatActivity {

    @Extra
    String mPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        Bundle bundle = new Bundle();
        bundle.putString("mPhotoPath", mPhotoPath);
        Fragment cameraFragment = Camera2BasicFragment.newInstance();
        cameraFragment.setArguments(bundle);
        if (null == savedInstanceState) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, cameraFragment)
                    .commit();
        }
    }
}