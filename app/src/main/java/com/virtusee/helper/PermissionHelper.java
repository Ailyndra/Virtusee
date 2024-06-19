package com.virtusee.helper;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;

import java.util.ArrayList;
import java.util.List;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

public class PermissionHelper {
    public static final int REQUEST_CHECK_GPS = 1;
    public static final int REQUEST_CHECK_CAMERA = 2;
    public static final int REQUEST_CHECK_PERM = 3;
    public static final int REQUEST_CHECK_STORAGE = 4;

    public static boolean request(Activity act, String access, int reqcode){
        if (ContextCompat.checkSelfPermission(act, access) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(act, new String[]{access}, reqcode);
            return false;
        }
        return true;
    }

    public static void request_multi(Activity act, String[] access, int reqcode){
        List<String> accessList = new ArrayList<String>();
        for(String acc: access)
        {
            if (ContextCompat.checkSelfPermission(act,acc) != PackageManager.PERMISSION_GRANTED) {
                accessList.add(acc);
            }
        }

        if(accessList.size()==0) return;

        String[] req_access = new String[accessList.size()];
        accessList.toArray(req_access);
        ActivityCompat.requestPermissions(act, req_access, reqcode);
    }

    public static void result(Activity act,int requestCode, String permissions[], int[] grantResults){
        // If request is cancelled, the result arrays are empty.
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // permission was granted, yay! Do the
            // contacts-related task you need to do.
        } else {
            act.finish();
        }
        return;
    }
}
