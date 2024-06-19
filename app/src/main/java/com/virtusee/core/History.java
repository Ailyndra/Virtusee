package com.virtusee.core;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.virtusee.adapter.HistoryAdapter;
import com.virtusee.contentprovider.HistoryContentProvider;
import com.virtusee.db.AnswerTable;
import com.virtusee.db.FormTable;
import com.virtusee.db.StoreTable;
import com.virtusee.helper.AuthHelper;
import com.virtusee.helper.DateHelper;
import com.virtusee.retrofit.VSRetrofitListener;
import com.virtusee.services.SyncServ;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import androidx.appcompat.widget.SearchView;


@EFragment(R.layout.history)
public class History extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> ,VSRetrofitListener {

	private static final int LOADER_ID = 3;

    private SearchView mSearchView;
    private String mCurFilter;
    private String logcontent;
    private Menu refreshMenu;
    private boolean refreshClicked = false;

    @ViewById
    ListView historyView;

	@Bean
    AuthHelper authHelper;

    @Bean
    HistoryAdapter historyAdapter;

	@Bean
	DateHelper dateHelper;

    @AfterInject
	public void initInject() {
		dateHelper.init(getActivity());
	}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return null;
    }

	@AfterViews
	void initList() {
        // Set an Adapter to the ListView
        getActivity().getLoaderManager().initLoader(LOADER_ID, null, this);
        historyView.setAdapter(historyAdapter);

        Log.e("view", "initstore");
	}


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu,inflater);
        refreshMenu = menu;

        menu.clear();
        getActivity().getMenuInflater().inflate(R.menu.history, menu);
    }


    //@SuppressLint("SimpleDateFormat")
    @OptionsItem(R.id.menu_refresh)
    void menuRefreshClick() {
        if(refreshClicked) return;

        refreshClicked = true;
        startRefreshAnim();

        SyncServ.enqueuePostAll(getActivity().getApplicationContext(),authHelper.getUserid(),authHelper.getFormattedUsername(),authHelper.getPassword());
    }

    public void startRefreshAnim(){

        MenuItem refreshItem = refreshMenu.findItem(R.id.menu_refresh);

        LayoutInflater inflater = (LayoutInflater)getActivity().getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ImageView iv = (ImageView)inflater.inflate(R.layout.iv_refresh, null);
        Animation rotation = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.rotate_refresh);
        rotation.setRepeatCount(Animation.INFINITE);
        iv.startAnimation(rotation);
        refreshItem.setActionView(iv);

    }

    public void stopRefreshAnim()
    {
        Log.e("loader", "stop anim");
        MenuItem refreshItem = refreshMenu.findItem(R.id.menu_refresh);

        // Get our refresh item from the menu
        if(refreshItem.getActionView()!=null)
        {
            // Remove the animation.
            refreshItem.getActionView().clearAnimation();
            refreshItem.setActionView(null);
        }
    }



    @ItemClick
    void historyViewItemClicked(Cursor histCursor) {
    	FormHist_.intent(this).idAnswer(histCursor.getString(histCursor.getColumnIndex(AnswerTable.COLUMN_ID))).start();
    }



    @Override
    public void onSuccess(String code) {
        resetLoader();
        stopRefreshAnim();
        refreshClicked = false;
    }

    @Override
    public void onFailure(String code) {
        refreshClicked = false;
        stopRefreshAnim();
    }

	@UiThread
	public void showMessage(String message) {
		Toast.makeText(getActivity().getApplicationContext(), message, Toast.LENGTH_SHORT).show();
	}

    
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		if(id==LOADER_ID){
			String[] projection = { AnswerTable.TABLE+"."+AnswerTable.COLUMN_ID, AnswerTable.COLUMN_ID_FORM, AnswerTable.COLUMN_ID_STORE,StoreTable.COLUMN_STORE_NAME, FormTable.COLUMN_FORM_TITLE,AnswerTable.COLUMN_WHEN,AnswerTable.COLUMN_LAST_SYNC};
			Uri baseUri;

            baseUri = HistoryContentProvider.CONTENT_URI;
//			Log.e("loader", baseUri.toString());
			CursorLoader cursorLoader = new CursorLoader(getActivity(),
					baseUri, projection, null, null, null);

			return cursorLoader;
		}
		else return null;
	}
	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		historyAdapter.swapCursor(data);
		Log.e("loader", "loader finish");
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		// TODO Auto-generated method stub
		historyAdapter.swapCursor(null);
	}
	
	@UiThread
	public void resetLoader(){
		getActivity().getLoaderManager().restartLoader(LOADER_ID, null, this);
	}


	@Override
	public void onResume() {
	    super.onResume();  // Always call the superclass method first
		boolean isLogged = authHelper.isLogged();
		//isLogged = false;
		if(!isLogged) {
			getActivity().finish();
		}

        resetLoader();
	}

    @Override
    public void onDestroy(){
        super.onDestroy();
        stopRefreshAnim();
    }

}


