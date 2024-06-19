package com.virtusee.core;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amulyakhare.textdrawable.TextDrawable;
import com.google.firebase.iid.FirebaseInstanceId;
import com.virtusee.db.VSDbHelper;
import com.virtusee.helper.AuthHelper;
import com.virtusee.helper.DateHelper;
import com.virtusee.helper.FileHelper;
import com.virtusee.helper.TokenHelper;
import com.virtusee.helper.UtilHelper;
import com.virtusee.listener.DialogListener;
import com.virtusee.retrofit.VSRetrofitAdapter;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import androidx.appcompat.widget.SearchView;

import java.io.IOException;


@EFragment(R.layout.myaccount)
public class Myaccount extends Fragment implements DialogListener {

	private static final int LOADER_ID = 3;

    private SearchView mSearchView;
    private String mCurFilter;
    private String logcontent;
    private Menu refreshMenu;
    private boolean refreshClicked = false;

    private Context ctx;
    
    @ViewById
    ImageView icon_image;

    @ViewById
    TextView txtAccountName;

    @ViewById
    TextView txtAccountCompany;

    @ViewById
    TextView txtVersion;

	@Bean
    AuthHelper authHelper;

	@Bean
	DateHelper dateHelper;

    @AfterInject
	public void initInject() {
		dateHelper.init(getActivity());
	}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(false);
        if(ctx==null) ctx = getActivity().getApplicationContext();
        
        return null;
    }

	@AfterViews
	void initList() {
        if(ctx==null) ctx = getActivity().getApplicationContext();

        txtAccountName.setText(authHelper.getFullname());
        txtAccountCompany.setText(authHelper.getUsername());
        try {
            String versionName = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0).versionName;
            txtVersion.setText("Virtusee version "+versionName);
        } catch (PackageManager.NameNotFoundException e) {

        }


        TextDrawable textDrawable = TextDrawable.builder().buildRound(createInitials(authHelper.getFullname()), getResources().getColor(R.color.vsblue));
        icon_image.setImageDrawable(textDrawable);

        Log.e("view", "initstore");
	}


	private String createInitials(String fullName){
        String[] parts = fullName.trim().split(" ");
        String res = "";
        if(fullName==null || fullName.equals("")) return "";

        if(parts!=null) {
            res = String.valueOf(parts[0].charAt(0));
            if(parts.length>1){
                if (parts[1] != null && !parts[1].equals(""))
                    res = res + String.valueOf(parts[1].charAt(0));
            }
        } else {
            res = String.valueOf(fullName.charAt(0));
        }
        return res.toUpperCase();
    }


    @Click(R.id.btnLogout)
    void logout() {
        UtilHelper.Alert(getActivity(),this,"Logout?","You'll lose all data and media!",android.R.drawable.ic_dialog_alert);
    }

	@UiThread
	public void showMessage(String message) {
		Toast.makeText(ctx, message, Toast.LENGTH_SHORT).show();
	}


	@Override
	public void onResume() {
	    super.onResume();  // Always call the superclass method first
		boolean isLogged = authHelper.isLogged();
		//isLogged = false;
		if(!isLogged) {
			getActivity().finish();
		}
	}

    @Override
    public void onDestroy(){
        super.onDestroy();
    }

    @Override
    public void OnDialogOkay(int type) {
        SQLiteDatabase db = VSDbHelper.getInstance(ctx).getWritableDatabase();

        db.beginTransaction();

        db.delete("form", null, null);
        db.delete("form_tag", null, null);
        db.delete("form_tag_own", null, null);
        db.delete("store", null, null);
        db.delete("store_tag", null, null);
        db.delete("photo", null, null);
        db.delete("answer", null, null);
        db.delete("inbox", null, null);
        db.delete("master", null, null);

        db.setTransactionSuccessful();
        db.endTransaction();

        AsyncTask.execute(() -> {
            try {
                FirebaseInstanceId.getInstance().deleteInstanceId();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        authHelper.clearCredentials();
        FileHelper.clearLog(getActivity().getApplicationContext());

        // -- delete photos --//
        FileHelper.clearAllPhotos(getActivity().getApplicationContext());

        FileHelper.setLog(getActivity().getApplicationContext(), this.getClass().getSimpleName() +" delete");

        VSRetrofitAdapter.reset();
        getActivity().finish();
    }

    @Override
    public void OnDialogCancel(int type) {

    }
}


