package com.virtusee.core;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.res.ColorStateList;
import android.provider.MediaStore; // tambahan untuk open camera 
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.virtusee.contentprovider.AnswerContentProvider;
import com.virtusee.contentprovider.FormContentProvider;
import com.virtusee.contentprovider.StoreContentProvider;
import com.virtusee.db.FormTable;
import com.virtusee.db.StoreTable;
import com.virtusee.helper.AbsenHelper;
import com.virtusee.helper.AuthHelper;
import com.virtusee.helper.DateHelper;
import com.virtusee.helper.GpsHelper;
import com.virtusee.helper.PermissionHelper;
import com.virtusee.helper.PrefHelper_;
import com.virtusee.helper.UtilHelper;
import com.virtusee.listener.DialogListener;
import com.virtusee.listener.FormTagListener;
import com.virtusee.listener.GpsListener;
import com.virtusee.listener.MyRecyclerListener;
import com.virtusee.model.StoreAbsenModel;
import com.virtusee.receiver.ConnectivityChangedReceiver;
import com.virtusee.receiver.ConnectivityChangedReceiver_;
import com.virtusee.recycleradapter.FormRecyclerAdapter;
import com.virtusee.recycleradapter.FormTagRecyclerAdapter;
import com.virtusee.services.SyncServ;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@EActivity
public class Form extends AppCompatActivity implements SearchView.OnQueryTextListener, LoaderManager.LoaderCallbacks<Cursor>, MyRecyclerListener, FormTagListener, GpsListener, DialogListener {

    private static final int LOADER_ID = 2;
    private static final int LOADER_TAG_ID = 22;
    private SearchView mSearchView;
    private String mCurFilter;
    private int placeStatus = 0;
    private LinearLayoutManager mLinearLayoutManager;
    private Toolbar toolbar;
    private ConnectivityChangedReceiver connectivityChangedReceiver;
    private Location currentLocation, lastLocation;
    private String attr = "";
    private String oStore;
    private int wdayStatus;
    //private String storeTags = "";
    private String lastCIN = "";
    private HashMap<String, Boolean> tag = new HashMap<String, Boolean>();
    private String latitude, longitude;

    @Bean
    GpsHelper gpsHelper;

    @Bean
    AbsenHelper absenHelper;

    @Bean
    FormRecyclerAdapter formRecyclerAdapter;

    @ViewById
    TextView txtTag;

    @ViewById
    TextView txtForm;

    @ViewById
    RecyclerView formTagView;

    @ViewById
    RecyclerView formView;

    @ViewById
    Chronometer chronometer;

    @ViewById
    TextView txtStoreReadmore;


    @ViewById
    FloatingActionButton fabForm, fabDirection;

    @Extra
    String idStore;

    @Extra
    Location storeLocation;

    @Bean
    AuthHelper authHelper;

    @Bean
    DateHelper dateHelper;

    @Pref
    PrefHelper_ myPrefs;

    @Bean
    FormTagRecyclerAdapter formTagRecyclerAdapter;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.form);
        toolbar = (Toolbar) findViewById(R.id.toolbarForm);
        setSupportActionBar(toolbar);

        try {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Form");
        } catch (NullPointerException e) {
        }

        dispStore();
        registerReceiver();
//        tes64();
    }


    public void registerReceiver() {
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        connectivityChangedReceiver = new ConnectivityChangedReceiver_();
        registerReceiver(connectivityChangedReceiver, filter);
    }


    @AfterInject
    public void initInject() {
        dateHelper.init(this);
        gpsHelper.init(this, this);
        absenHelper.init(this);
    }

    @AfterViews
    void initList() {


        // Set an Adapter to the ListView

        Log.e("view", "initview");

        setupRecyclerView();
        setupAdapter();
    }

    private void setupRecyclerView() {

        mLinearLayoutManager = new LinearLayoutManager(this);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(formView.getContext(),
                mLinearLayoutManager.getOrientation());
        formView.addItemDecoration(dividerItemDecoration);
        formView.setLayoutManager(mLinearLayoutManager);

        LinearLayoutManager mTagLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        formTagView.setLayoutManager(mTagLayoutManager);

    }

    private void setupAdapter() {
        /*
        SQLiteDatabase db = VSDbHelper.getInstance(this).getReadableDatabase();
        String sql = "select * from store_tag where id_store = " + DatabaseUtils.sqlEscapeString(idStore);
        Cursor cursor = db.rawQuery(sql,null);

        storeTags += DatabaseUtils.sqlEscapeString(idStore);
        if( cursor != null && cursor.moveToFirst() ){
            do {
                storeTags += "," + DatabaseUtils.sqlEscapeString((cursor.getString(cursor.getColumnIndexOrThrow("tag"))));
            } while (cursor.moveToNext());
        }
        cursor.close();
*/

        formRecyclerAdapter.setOnRecyclerItemClickedListener(this);
        getLoaderManager().initLoader(LOADER_ID, null, this);
        formView.setAdapter(formRecyclerAdapter);


        // --- set tag adapter --//
        getLoaderManager().initLoader(LOADER_TAG_ID, null, this);
        formTagRecyclerAdapter.setOnRecyclerItemClickedListener(this);
        formTagRecyclerAdapter.setTagClickedListener(this);
        formTagView.setAdapter(formTagRecyclerAdapter);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem searchItem = menu.findItem(R.id.searchView);

        mSearchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        if (mSearchView != null) {
            mSearchView.setOnQueryTextListener(this);
        }
        return true;
    }

    private void dispStore() {
        Uri dtUri = Uri.parse(StoreContentProvider.CONTENT_URI + "/" + idStore);
        Cursor cursor = getContentResolver().query(dtUri, null, null, null, null);
        String nama = "Form";
        String desc = "";

        TextView txtStoreDesc = (TextView) findViewById(R.id.txtStoreDesc);

        try {
            if (cursor != null && cursor.moveToFirst()) {
                nama = cursor.getString(cursor.getColumnIndexOrThrow(StoreTable.COLUMN_STORE_NAME));
                desc = cursor.getString(cursor.getColumnIndexOrThrow(StoreTable.COLUMN_STORE_CODE));
                attr = cursor.getString(cursor.getColumnIndexOrThrow(StoreTable.COLUMN_ATTR));
                longitude = cursor.getString(cursor.getColumnIndexOrThrow(StoreTable.COLUMN_LONG));
                latitude = cursor.getString(cursor.getColumnIndexOrThrow(StoreTable.COLUMN_LAT));
            }
        } catch (Exception e) {
        } finally {
            if (cursor != null) cursor.close();
        }

        try {
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle(nama);
            txtStoreDesc.setText(desc);
            if (attr.equals("")) {
                txtStoreReadmore.setVisibility(View.INVISIBLE);
            } else {
                txtStoreReadmore.setVisibility(View.VISIBLE);
            }
        } catch (NullPointerException e) {
        }

    }

    private void initCin() {
        StoreAbsenModel storeAbsenModel = absenHelper.GetStatus();
        wdayStatus = absenHelper.GetStatusWday();
        if (storeAbsenModel.idStore.equals("")) {
            oStore = "";
            placeStatus = 0;
            lastCIN = "";
        } else if (storeAbsenModel.idStore.equals(idStore)) {
            oStore = "";
            placeStatus = storeAbsenModel.status;
            lastCIN = storeAbsenModel.whenupdate;
        } else {
            if (storeAbsenModel.status == 1) oStore = storeAbsenModel.idStore;
            else oStore = "";
            placeStatus = 0;
            lastCIN = "";
        }
        disp_cin();
    }

    private void disp_cin() {
        int icon = R.drawable.ic_alarm;
        int iconDirection = R.drawable.ic_direction;
        int color = R.color.Black;
        int textcolor = R.color.Black;

        switch (placeStatus) {
            case 0:
            case 2:
                icon = R.drawable.ic_alarm;
                color = R.color.Orange;
                textcolor = R.color.White;
                break;
            case 1:
                icon = R.drawable.ic_alarm_off;
                color = R.color.Red;
                textcolor = R.color.White;

                break;
        }

        try {
            fabForm.setImageResource(icon);
            fabForm.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, color)));
            fabForm.setColorFilter(ContextCompat.getColor(this, textcolor));

            fabDirection.setImageResource(iconDirection);
            fabDirection.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, color)));
            fabDirection.setColorFilter(ContextCompat.getColor(this, textcolor));

            if (placeStatus == 1 && !lastCIN.equals("")) {
                chronometer.setBase(dateHelper.chronoBase(lastCIN));
                chronometer.start();
                chronometer.setVisibility(View.VISIBLE);
            } else {
                chronometer.setVisibility(View.GONE);
            }


        } catch (Exception e) {
            Log.e("form", e.getMessage());
        }

    }

    @Click(R.id.txtStoreReadmore)
    void readMore() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Form.ReadmoreDialog frag = Form.ReadmoreDialog.newInstance(attr);
        frag.show(ft, "txn_tag");

    }


    @Click(R.id.fabForm)
    void placesCin() {
        // Membuka aplikasi kamera
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    // menambahkan variabel ini di luar metode untuk kode permintaan kamera
    private static final int REQUEST_IMAGE_CAPTURE = 1;

    // Override metode ini untuk menangani hasil dari kamera
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            // Ambil gambar dari Intent
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            // melakukan sesuatu dengan bitmap, misalnya menampilkannya di ImageView
            ImageView imageView = findViewById(R.id.yourImageViewId);
            imageView.setImageBitmap(imageBitmap);
        }
    }


    // @Click(R.id.fabForm)
    // void placesCin() {
    //     int status = 0;
    //     String msg;

    //     if (placeStatus == 1) {
    //         status = 2;
    //         msg = "Check out from this place?";
    //     } else {
    //         status = 1;
    //         msg = "Check in to this place?";
    //     }

    //     UtilHelper.Alert(this, this, msg, "", android.R.drawable.ic_dialog_alert, status);
    // }

    @Click(R.id.fabDirection)
    void direction() {
        if (!longitude.equals("") && !latitude.equals("")) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Open Google Maps?")
                    .setCancelable(true)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                            Uri gmmIntentUri = Uri.parse("google.navigation:q=" + latitude + "," + longitude);
                            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                            mapIntent.setPackage("com.google.android.apps.maps");

                            if (mapIntent.resolveActivity(getPackageManager()) == null) {
                                showMessage("Can not start map!");
                                return;
                            }

                            startActivity(mapIntent);
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                            dialog.cancel();
                        }
                    });
            final AlertDialog alert = builder.create();
            alert.show();
        } else {
            showMessage("Couldn't find Store location");
        }
    }

    private boolean cinPlaces(int status, String store) {
        placeStatus = status;
        Location currentLocation = gpsHelper.getLastLocation();
        if (currentLocation == null && lastLocation != null) currentLocation = lastLocation;

        absenHelper.SaveAbsen(status, store, currentLocation);
        lastCIN = dateHelper.getCurrentTimestamp();
        SyncServ.enqueuePostAll(this, authHelper.getUserid(), authHelper.getFormattedUsername(), authHelper.getPassword());
        return true;
    }

    @UiThread
    public void showMessage(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == LOADER_ID) {
            String[] projection = {FormTable.COLUMN_ID, FormTable.COLUMN_FORM_TITLE, FormTable.COLUMN_FORM_CONTENT, FormTable.COLUMN_FORM_STICKY, FormTable.COLUMN_FORM_ID, FormTable.COLUMN_FORM_MANDATORY};
            Uri baseUri;
            boolean isTag = !tag.isEmpty();
            String tagstr = "";

            if (isTag) {
                for (String tagid : tag.keySet()) {
                    tagstr += "," + DatabaseUtils.sqlEscapeString(tagid);
                }
                tagstr = tagstr.substring(1);
            }

            if (mCurFilter != null) {
                if (isTag) {
                    baseUri = Uri.parse(FormContentProvider.CONTENT_URI + "/tag/" + Uri.encode(tagstr) + "/t/" + Uri.encode(idStore) + "/q/" + Uri.encode(mCurFilter));
                } else {
                    baseUri = Uri.parse(FormContentProvider.CONTENT_URI + "/t/" + Uri.encode(idStore) + "/q/" + Uri.encode(mCurFilter));
                }
            } else {
                if (isTag) {
                    baseUri = Uri.parse(FormContentProvider.CONTENT_URI + "/tag/" + Uri.encode(tagstr) + "/t/" + Uri.encode(idStore));
                } else {
                    baseUri = Uri.parse(FormContentProvider.CONTENT_URI + "/t/" + Uri.encode(idStore));
                }
            }

			Log.e("form",baseUri.toString());

            CursorLoader cursorLoader = new CursorLoader(this, baseUri, projection, null, null, null);
            return cursorLoader;
        } else if (id == LOADER_TAG_ID) {

            Uri baseTagUri = Uri.parse(FormContentProvider.CONTENT_URI + "/tagall/" + Uri.encode(idStore));
            CursorLoader cursorTagLoader = new CursorLoader(this,
                    baseTagUri, null, null, null, null);

            Log.e("form",baseTagUri.toString());
            return cursorTagLoader;
        } else return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        int lid = loader.getId();
        if (lid == LOADER_ID) formRecyclerAdapter.swapCursor(data);
        else if (lid == LOADER_TAG_ID) {
            if (data == null || data.getCount() == 0) {
                setTagInvisible();
            } else {
                setTagVisible();
            }
            formTagRecyclerAdapter.swapCursor(data);
        }
    }


    private void setTagInvisible() {
        txtTag.setVisibility(View.GONE);
        txtForm.setTextColor(ContextCompat.getColor(this, R.color.vsblue));
        txtForm.setBackgroundColor(ContextCompat.getColor(this, R.color.White));
    }

    private void setTagVisible() {
        txtTag.setVisibility(View.VISIBLE);
        txtForm.setTextColor(ContextCompat.getColor(this, R.color.White));
        txtForm.setBackgroundColor(ContextCompat.getColor(this, R.color.vsblue));
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // TODO Auto-generated method stub
        int lid = loader.getId();
        if (lid == LOADER_ID) formRecyclerAdapter.swapCursor(null);
        else if (lid == LOADER_TAG_ID) formTagRecyclerAdapter.swapCursor(null);
    }

    @UiThread
    public void resetLoader() {
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }


    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first
        boolean isLogged = authHelper.isLogged();
        //isLogged = false;
        if (!isLogged) {
            this.finish();
        }
        PermissionHelper.request(this, Manifest.permission.ACCESS_FINE_LOCATION, PermissionHelper.REQUEST_CHECK_GPS);

        gpsHelper.checkGPSOn();

        initCin();
    }


    public void tes64() {

        //encode image to base64 string
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ab_logo);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        String imageString = Base64.encodeToString(imageBytes, Base64.DEFAULT);

        Log.e("imgtes", imageString);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        mCurFilter = !TextUtils.isEmpty(query) ? query : null;
        doSearch();
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if (TextUtils.isEmpty(newText) && !TextUtils.isEmpty(mCurFilter)) {
            mCurFilter = null;
            doSearch();
        }
        return false;
    }

    public void doSearch() {
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        try {
            switch (menuItem.getItemId()) {
                case android.R.id.home:
                    onBackPressed();
                    return true;
                default:
                    return super.onOptionsItemSelected(menuItem);
            }
        } catch (RuntimeException e) {
            return super.onOptionsItemSelected(menuItem);
        }
    }


    @Override
    public void onRecyclerItemClicked(Cursor formCursor) {
        String domain = myPrefs.domain().get();
        String id = formCursor.getString(formCursor.getColumnIndex(FormTable.COLUMN_ID));
        if (domain.equals("aqua") && id.equals("6332566780ab88f8a38b4567")) {
            Uri dtUri = Uri.parse(AnswerContentProvider.CONTENT_URI + "/count");
            Cursor cursor = getContentResolver().query(dtUri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int tot = cursor.getInt(cursor.getColumnIndex("total"));
                int tot_checkout = myPrefs.total_checkout().get();
                if (tot > tot_checkout) {
                    goToFormDet(formCursor);
                    return;
                }
            }

            Uri uri = Uri.parse(AnswerContentProvider.CONTENT_URI + "/form/6332557b80ab887da98b4567/today");
            Cursor cursorForm = getContentResolver().query(uri, null, null, null, null);
            if(cursorForm != null && cursorForm.getCount() != 0) {
                goToFormDet(formCursor);
            } else {
                Toast.makeText(this, "Kuota belum terpenuhi", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        String proximity = myPrefs.proximity().get();
        if (proximity.equals("0") || proximity.equals("")) {
            goToFormDet(formCursor);
            return;
        }

        if (storeLocation != null && (storeLocation.getLatitude() != 0 && storeLocation.getLongitude() != 0)) {
            int radius = myPrefs.radius().get();
            double distance = distance(storeLocation.getLatitude(), storeLocation.getLongitude(), lastLocation.getLatitude(), lastLocation.getLongitude()); //Get distance in miles
            distance = distance / 0.62137;
            double meter = distance * 1000;
            if (meter > radius) {
                if (meter < 1000) showMessage("Store distance is " + (int) meter + " m");
                else showMessage(String.format(Locale.getDefault(), "Store distance is %.2f km", meter / 1000));
                return;
            }
        }

        goToFormDet(formCursor);
    }

    private void goToFormDet(Cursor formCursor) {
        FormDet_.intent(this).idStore(idStore).idForm(formCursor.getString(formCursor.getColumnIndex(FormTable.COLUMN_FORM_ID))).formTitle(formCursor.getString(formCursor.getColumnIndex(FormTable.COLUMN_FORM_TITLE))).content(formCursor.getString(formCursor.getColumnIndex(FormTable.COLUMN_FORM_CONTENT))).sticky(formCursor.getInt(formCursor.getColumnIndex(FormTable.COLUMN_FORM_STICKY))).mandatory(formCursor.getInt(formCursor.getColumnIndex(FormTable.COLUMN_FORM_MANDATORY))).start();
    }

    private double distance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1))
                * Math.sin(deg2rad(lat2))
                + Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2))
                * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        return (dist);
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }

    @Override
    public void onRecyclerLongClicked(Cursor cursor) {

    }

    @Override
    public void onPause() {
        super.onPause();
        lastLocation = gpsHelper.getLastLocation();
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
    public void onDestroy() {
        super.onDestroy();
        if (connectivityChangedReceiver != null) unregisterReceiver(connectivityChangedReceiver);
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
        outState.putParcelable("lastLocation", lastLocation);

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        lastLocation = savedInstanceState.getParcelable("lastLocation");

        if (lastLocation != null && gpsHelper != null) {
            gpsHelper.setStartLoc(lastLocation);
            onGpsSet(lastLocation);
        }
    }

    @Override
    public void OnDialogOkay(int type) {
        if (type == 1 && wdayStatus != 1) {
            // -- auto wday start if haven't
            cinPlaces(1, "wday");
        }

        if (type == 1 && !oStore.equals("")) {
            // -- jika ada toko lain belum cout, auto cout di toko lain --//
            cinPlaces(2, oStore);
            oStore = "";
        }

        cinPlaces(type, idStore);
        disp_cin();
    }

    @Override
    public void OnDialogCancel(int type) {

    }

    @Override
    public void tagClick(Button btnTag, String content) {
        int istag = 0;
        String tagstr = btnTag.getTag().toString();
        if (!tagstr.equals("")) istag = Integer.parseInt(tagstr);

        if (istag == 1) {
            tag.put(content, true);
        } else {
            tag.remove(content);
        }
        doSearch();
    }


    public static class ReadmoreDialog extends DialogFragment implements Html.ImageGetter {
        private String mcontent;

        public static Form.ReadmoreDialog newInstance(String content) {
            Form.ReadmoreDialog f = new Form.ReadmoreDialog();

            // Supply num input as an argument.
            Bundle args = new Bundle();
            args.putString("content", content);
            f.setArguments(args);
            Log.e("fragment", "new");

            return f;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mcontent = getArguments().getString("content");
            //setStyle(DialogFragment.STYLE_NORMAL, R.style.fullpopup);
            setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_DeviceDefault_Light_NoActionBar_TranslucentDecor);
            Log.e("fragment", "create");
        }

        @Override
        public void onStart() {
            super.onStart();
            Log.e("fragment", "start");
            Dialog d = getDialog();
            if (d != null) {
                int width = ViewGroup.LayoutParams.MATCH_PARENT;
                int height = ViewGroup.LayoutParams.MATCH_PARENT;
                d.getWindow().setLayout(width, height);
            }
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            Log.e("fragment", "view");
            View root = inflater.inflate(R.layout.store_attr, container, false);

            /*Toolbar actionBar = root.findViewById(R.id.dialogtoolbar);
            if (actionBar != null) {
                final Form.ReadmoreDialog toolbarwindow = this;
                actionBar.setNavigationOnClickListener(v -> toolbarwindow.dismiss());
            }*/

            TextView name = root.findViewById(R.id.storeattr);
            ImageView img = root.findViewById(R.id.storeimg);

            String imageUrl = null;
            if (extractUrls(mcontent).size() != 0) imageUrl = extractUrls(mcontent).get(0);
            if (imageUrl != null) {
                img.setVisibility(View.VISIBLE);
                Glide.with(root.getContext())
                        .load(imageUrl.substring(0, imageUrl.length() - 8))
                        .centerCrop()
                        .skipMemoryCache(true)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .into(img);
                mcontent = mcontent.replace(imageUrl+">", "");
            }

            Spanned fh;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                fh = Html.fromHtml(mcontent, Html.FROM_HTML_MODE_LEGACY, this, null);
            } else {
                fh = Html.fromHtml(mcontent, this, null);
            }
            name.setText(fh);
            name.setMovementMethod(LinkMovementMethod.getInstance());

            return root;
        }

        @Override
        public Drawable getDrawable(String s) {
            byte[] data = Base64.decode(s, Base64.DEFAULT);
            Bitmap bitmap2 = BitmapFactory.decodeByteArray(data, 0, data.length);
            Drawable image = new BitmapDrawable(getResources(), bitmap2);
            image.setBounds(0, 0, 0 + image.getIntrinsicWidth(), 0 + image.getIntrinsicHeight());
            return image;
        }

        public static List<String> extractUrls(String text) {
            List<String> containedUrls = new ArrayList<>();
            String urlRegex = "((https?|ftp|gopher|telnet|file):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)";
            Pattern pattern = Pattern.compile(urlRegex, Pattern.CASE_INSENSITIVE);
            Matcher urlMatcher = pattern.matcher(text);

            while (urlMatcher.find()) {
                containedUrls.add(text.substring(urlMatcher.start(0),
                        urlMatcher.end(0)));
            }

            return containedUrls;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //final LocationSettingsStates states = LocationSettingsStates.fromIntent(data);
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GpsHelper.REQUEST_CHECK_SETTINGS) {// All required changes were successfully made
            if (resultCode == Activity.RESULT_CANCELED) {// The user was asked to change settings, but chose not to
                finish();
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionHelper.result(this, requestCode, permissions, grantResults);
    }
}





