package com.virtusee.core;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.virtusee.helper.AuthHelper;
import com.virtusee.helper.DateHelper;
import com.virtusee.helper.FileHelper;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import androidx.appcompat.widget.SearchView;


@EFragment(R.layout.syslog)
public class Syslog extends Fragment {

	private static final int LOADER_ID = 3;

    private SearchView mSearchView;
    private String mCurFilter;
    private String logcontent;
    private Menu refreshMenu;
    private boolean refreshClicked = false;

    @ViewById
    TextView txtLog;

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
        return null;
    }

	@AfterViews
	void initList() {

        getLog();

        Log.e("view", "initstore");
	}


    public void getLog(){
        String filename = "virtusee.err.log";
        logcontent = "";
        FileInputStream fIn;
        try {
            fIn = getActivity().getApplicationContext().openFileInput(filename);
        } catch (FileNotFoundException e){
            fIn = null;
        }

        if(fIn!=null){
            try {
                logcontent = FileHelper.convertStreamToString(fIn);
                txtLog.setText(logcontent);
            } catch (IOException ieo){}
        }
    }



	@UiThread
	public void showMessage(String message) {
		Toast.makeText(getActivity().getApplicationContext(), message, Toast.LENGTH_SHORT).show();
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

}


