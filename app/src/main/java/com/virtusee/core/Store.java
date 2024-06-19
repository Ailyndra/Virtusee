package com.virtusee.core;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.LocationServices;
import com.virtusee.adapter.StoreAdapter;
import com.virtusee.contentprovider.StoreContentProvider;
import com.virtusee.db.StoreTable;
import com.virtusee.geofence.GeofenceHelper;
import com.virtusee.helper.AuthHelper;
import com.virtusee.helper.DateHelper;
import com.virtusee.helper.GpsHelper;
import com.virtusee.helper.PermissionHelper;
import com.virtusee.helper.PrefHelper_;
import com.virtusee.listener.GpsListener;
import com.virtusee.listener.MyRecyclerListener;
import com.virtusee.listener.StoreListener;
import com.virtusee.listener.StoreTagListener;
import com.virtusee.recycleradapter.StoreTagRecyclerAdapter;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.util.ArrayList;
import java.util.HashMap;


@EFragment(R.layout.store)
public class Store extends Fragment implements SearchView.OnQueryTextListener, LoaderManager.LoaderCallbacks<Cursor>, StoreListener, MyRecyclerListener, StoreTagListener, GpsListener {

    private static final int LOADER_ID = 2;
    private static final int LOADER_TAG_ID = 21;
    private static final int LOADER_CIN_ID = 123;
    private static final int SCAN_TO_ENTER = 1000;

    private SearchView mSearchView;
    private String mCurFilter;
    private StoreAdapter storeAdapter;
    private LinearLayoutManager mLinearLayoutManager;
    private HashMap<String, Boolean> tag = new HashMap<String, Boolean>();
    private String storeId = "all";
    private Cursor onClickCursor;

    private LocationManager locationManager;
    private Location currentLocation;
    private GeofencingClient geofencingClient;
    private GeofenceHelper geofenceHelper;

    @ViewById
    LinearLayout storeViewCont;

    @ViewById
    RecyclerView storeTagView;

    @ViewById
    ListView storeView;

    @Bean
    AuthHelper authHelper;

    @Bean
    DateHelper dateHelper;

    @Bean
    StoreTagRecyclerAdapter storeTagRecyclerAdapter;

    @Pref
    PrefHelper_ myPrefs;

    @Bean
    GpsHelper gpsHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        geofencingClient = LocationServices.getGeofencingClient(getActivity().getApplicationContext());
        geofenceHelper = new GeofenceHelper(getActivity().getApplicationContext());

        return null;
    }

    @AfterInject
    public void initInject() {
        dateHelper.init(getActivity());
        gpsHelper.init(getActivity(), this);
    }

    @AfterViews
    void initList() {
        setupRecyclerView();
        setupAdapter();

        Log.e("view", "initstore");
    }


    private void setupRecyclerView() {
        mLinearLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
        storeTagView.setLayoutManager(mLinearLayoutManager);
    }

    private void setupAdapter() {

        getActivity().getLoaderManager().initLoader(LOADER_CIN_ID, null, this);

        // Set an Adapter to the ListView
        getActivity().getLoaderManager().initLoader(LOADER_ID, null, this);
        storeAdapter = new StoreAdapter(getActivity(), this);
        storeView.setAdapter(storeAdapter);


        // --- set tag adapter --//
        getLoaderManager().initLoader(LOADER_TAG_ID, null, this);
        storeTagRecyclerAdapter.setOnRecyclerItemClickedListener(this);
        storeTagRecyclerAdapter.setTagClickedListener(this);
        storeTagView.setAdapter(storeTagRecyclerAdapter);
    }

    @ItemClick
    void storeViewItemClicked(Cursor storeCursor) {
        onClickCursor = storeCursor;
        //showMessage(storeCursor.getString(storeCursor.getColumnIndex(StoreTable.COLUMN_STORE_ID)));
//        String domain = authHelper.getDomain();
//        if (domain.equalsIgnoreCase("ulamsari")) {
//            String shorts = storeCursor.getString(storeCursor.getColumnIndexOrThrow(StoreTable.COLUMN_SHORT));
//            if (shorts.contains("JTG")) {
//                Intent intent = new Intent(getActivity(), BarcodeScanner.class);
//                startActivityForResult(intent, SCAN_TO_ENTER);
//                return;
//            }
//        }

        String latitude = storeCursor.getString(storeCursor.getColumnIndexOrThrow(StoreTable.COLUMN_LAT));
        String longitude = storeCursor.getString(storeCursor.getColumnIndexOrThrow(StoreTable.COLUMN_LONG));

        if(latitude.equals("") && longitude.equals("")) {
            Form_.intent(this).idStore(storeCursor.getString(storeCursor.getColumnIndexOrThrow(StoreTable.COLUMN_STORE_ID))).start();
            return;
        }

        Location storeLocation = new Location("");
        try {
            storeLocation.setLatitude(Double.parseDouble(latitude));
            storeLocation.setLongitude(Double.parseDouble(longitude));
        } catch (Exception e) {
            e.printStackTrace();
        }

        Form_.intent(this).idStore(storeCursor.getString(storeCursor.getColumnIndexOrThrow(StoreTable.COLUMN_STORE_ID))).storeLocation(storeLocation).start();

        //DEBUG
        /*Uri gmmIntentUri = Uri.parse("google.navigation:q=" + currentLocation.getLatitude() + "," + currentLocation.getLongitude());
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");

        try {
            if (mapIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                startActivity(mapIntent);
            }
        } catch (NullPointerException e) {
            showMessage("Couldn't open map");
        }*/
    }

    @OnActivityResult(SCAN_TO_ENTER)
    public void enterPlace(int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            String idPlace = onClickCursor.getString(onClickCursor.getColumnIndexOrThrow(StoreTable.COLUMN_SHORT));
            String barcode = data.getStringExtra("barcode");
            if (idPlace.equalsIgnoreCase(barcode))
                Form_.intent(this).idStore(onClickCursor.getString(onClickCursor.getColumnIndexOrThrow(StoreTable.COLUMN_STORE_ID))).start();
            else
                Toast.makeText(getActivity(), "Invalid barcode", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu,inflater);

        menu.clear();
        getActivity().getMenuInflater().inflate(R.menu.main, menu);
        MenuItem searchItem = menu.findItem(R.id.searchView);

        mSearchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        if (mSearchView != null) {
            mSearchView.setOnQueryTextListener(this);
        }
    }


    @UiThread
    public void showMessage(String message) {
        Toast.makeText(getActivity().getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        mCurFilter = !TextUtils.isEmpty(query) ? query : null;
        doSearch();
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if(TextUtils.isEmpty(newText) && !TextUtils.isEmpty(mCurFilter) ) {
            mCurFilter = null;
            doSearch();
        }
        return false;
    }

    public void doSearch(){
        try{
            getActivity().getLoaderManager().restartLoader(LOADER_ID, null, this);
        } catch (Exception e){}
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader loader;
        switch(id){
            case LOADER_ID:
                loader = LoadPlaces(args);
            break;
            case LOADER_TAG_ID:
                loader = LoadTag(args);
            break;
            case LOADER_CIN_ID:
                loader = LoadCin(args);
            break;
            default:
                loader = null;
                break;
        }
        return loader;
    }

    private CursorLoader LoadCin(Bundle args){
        Uri baseUri = Uri.parse(StoreContentProvider.CONTENT_URI + "/cin");
        CursorLoader cursorLoader = new CursorLoader(getActivity(),
                baseUri , null, null, null, null);

        return cursorLoader;
    }

    private CursorLoader LoadPlaces(Bundle args){
        if(storeId==null) return  null;

        String[] projection = { StoreTable.COLUMN_ID, StoreTable.COLUMN_STORE_NAME , StoreTable.COLUMN_STORE_CODE, StoreTable.COLUMN_STORE_ID, StoreTable.COLUMN_DAY,StoreTable.COLUMN_WEEK ,StoreTable.COLUMN_ATTR,StoreTable.COLUMN_LONG,StoreTable.COLUMN_LAT,StoreTable.COLUMN_LOCKED,StoreTable.COLUMN_LOCKED_WHEN,StoreTable.COLUMN_SHORT };
        Uri baseUri;
        boolean isTag = !tag.isEmpty();
        String tagstr = "";

        if(storeId.equals("all")) {
            if (isTag) {
                for (String tagid : tag.keySet()) {
                    tagstr += "," + DatabaseUtils.sqlEscapeString(tagid);
                }
                tagstr = tagstr.substring(1);
            }

            if (mCurFilter != null) {
                if (isTag) {
                    baseUri = Uri.parse(StoreContentProvider.CONTENT_URI + "/tag/" + Uri.encode(tagstr) + "/q/" + Uri.encode(mCurFilter));
                } else {
                    baseUri = Uri.parse(StoreContentProvider.CONTENT_URI + "/q/" + Uri.encode(mCurFilter));
                }
            } else {
                if (isTag) {
                    baseUri = Uri.parse(StoreContentProvider.CONTENT_URI + "/tag/" + Uri.encode(tagstr));
                } else {
                    baseUri = StoreContentProvider.CONTENT_URI;
                }
            }
        } else {
            baseUri = Uri.parse(StoreContentProvider.CONTENT_URI + "/" + Uri.encode(storeId));
        }

        Log.e("loader", baseUri.toString());
        CursorLoader cursorLoader = new CursorLoader(getActivity(),
                baseUri, projection, null, null, null);

        return cursorLoader;
    }

    private CursorLoader LoadTag(Bundle args){
        if(storeId==null) return  null;
        if(!storeId.equals("all")) return null;

        Uri baseTagUri = Uri.parse(StoreContentProvider.CONTENT_URI + "/tagall");
        CursorLoader cursorTagLoader = new CursorLoader(getActivity(),
                baseTagUri , null, null, null, null);

        return cursorTagLoader;
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(data==null) return;
        int lid = loader.getId();

        switch(lid){
            case LOADER_ID:
                storeAdapter.swapCursor(data);
                break;
            case LOADER_CIN_ID:
                int totCin = 0;
                totCin = data.getCount();
                data.moveToFirst();

                if(totCin==0) storeId = "all";
                else {
                    storeId = data.getString(data.getColumnIndexOrThrow(StoreTable.COLUMN_ID));
//                    Form_.intent(this).idStore(storeId).start();
                }
                try{
                    if(getActivity().getLoaderManager().getLoader(LOADER_ID)!=null) getActivity().getLoaderManager().restartLoader(LOADER_ID, null, this);
                    if(getActivity().getLoaderManager().getLoader(LOADER_TAG_ID)!=null) getActivity().getLoaderManager().restartLoader(LOADER_TAG_ID, null, this);
                } catch (NullPointerException e){ }
                break;

            case LOADER_TAG_ID:
                int totTag = 0;
                if(data!=null) totTag = data.getCount();

                storeTagRecyclerAdapter.swapCursor(data);
                if(totTag==0) {
                    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                    params.setMargins(0,0,0,0);
                    storeViewCont.setLayoutParams(params);
                }
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // TODO Auto-generated method stub
        int lid = loader.getId();
        if(lid==LOADER_ID) storeAdapter.swapCursor(null);
        else if(lid==LOADER_TAG_ID) storeTagRecyclerAdapter.swapCursor(null);
    }

    @Override
    public void onStart() {
        super.onStart();
        gpsHelper.connect();
    }

    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first
        gpsHelper.checkGPSOn();
        boolean isLogged = authHelper.isLogged();
        //isLogged = false;
        if(!isLogged) {
            getActivity().finish();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        gpsHelper.disconnect();
    }

    @Override
    public void readMoreClick(String content) {
    }


    @Override
    public void onRecyclerItemClicked(Cursor cursor) {
    }

    @Override
    public void onRecyclerLongClicked(Cursor cursor) {

    }

    @Override
    public void tagClick(Button btnTag, String content) {
        int istag = 0;
        String tagstr = btnTag.getTag().toString();
        if(!tagstr.equals("")) istag = Integer.parseInt(tagstr);

        if(istag==1){
            tag.put(content,true);
        } else {
            tag.remove(content);
        }
        doSearch();
    }

    @Override
    public void onGpsSet(Location location) {
        if(location != null) {
            currentLocation = location;
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
}



