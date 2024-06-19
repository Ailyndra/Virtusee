package com.virtusee.core;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.text.InputType;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.dantsu.escposprinter.connection.DeviceConnection;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections;
import com.dantsu.escposprinter.textparser.PrinterTextParserImg;
import com.google.gson.Gson;
import com.virtusee.contentprovider.AnswerContentProvider;
import com.virtusee.contentprovider.FormContentProvider;
import com.virtusee.contentprovider.StoreContentProvider;
import com.virtusee.db.AnswerTable;
import com.virtusee.db.FormTable;
import com.virtusee.db.StoreTable;
import com.virtusee.helper.AuthHelper;
import com.virtusee.helper.DateHelper;
import com.virtusee.helper.FileHelper;
import com.virtusee.helper.ImageHelper;
import com.virtusee.helper.PermissionHelper;
import com.virtusee.helper.PrefHelper_;
import com.virtusee.model.AnswerContentModel;
import com.virtusee.model.ComboModel;
import com.virtusee.model.QuestionModel;
import com.virtusee.printer.AsyncBluetoothEscPosPrint;
import com.virtusee.printer.AsyncEscPosPrint;
import com.virtusee.printer.AsyncEscPosPrinter;
import com.virtusee.services.SyncServ;
import com.virtusee.view.FormAudioView;
import com.virtusee.view.FormAudioView_;
import com.virtusee.view.FormBarcodeView;
import com.virtusee.view.FormBarcodeView_;
import com.virtusee.view.FormDateView;
import com.virtusee.view.FormDateView_;
import com.virtusee.view.FormEditView;
import com.virtusee.view.FormEditView_;
import com.virtusee.view.FormGroupView;
import com.virtusee.view.FormImgButView;
import com.virtusee.view.FormImgButView_;
import com.virtusee.view.FormMultipleChoiceView;
import com.virtusee.view.FormMultipleChoiceView_;
import com.virtusee.view.FormTextView;
import com.virtusee.view.FormTextView_;
import com.virtusee.view.FormTtdView;
import com.virtusee.view.FormTtdView_;
import com.virtusee.view.FormYesNoView;
import com.virtusee.view.FormYesNoView_;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@EActivity
public class FormHist extends AppCompatActivity {
    @ViewById
    LinearLayout formDetContainer;

    @ViewById
    TextView formDetGpsStatus;

    @Extra
    String idAnswer;

    @Pref
    PrefHelper_ myPrefs;

    @Bean
    DateHelper dateHelper;


    @Bean
    AuthHelper authHelper;

    private QuestionModel[] model;
    private HashMap<String, String> answerMap = new HashMap<String, String>();

    private String content;
    private String idForm;
    private String placeName;

    private static String imgPath;

    private static final int FORM_TYPE_TEXT = 0;
    private static final int FORM_TYPE_LONGTEXT = 1;
    private static final int FORM_TYPE_YESNO = 2;
    private static final int FORM_TYPE_SELECTION = 3;
    private static final int FORM_TYPE_NUMERIC = 4;
    private static final int FORM_TYPE_NUMERIC_SUM = 5;
    private static final int FORM_TYPE_PHOTO = 6;
    private static final int FORM_TYPE_MULTIPLE_CHOICE = 7;
    private static final int FORM_TYPE_DATE = 8;
    private static final int FORM_TYPE_SECTIONHEADER = 9;
    private static final int FORM_TYPE_TTD = 10;
    private static final int FORM_TYPE_MULTIPLE_ITEM = 11;
    private static final int FORM_TYPE_BARCODE = 12;
    private static final int FORM_TYPE_AUDIO = 13;
    private static final int FORM_TYPE_MULTIPLICATION = 14;
    private static final int FORM_TYPE_MANUAL_MULTI = 15;

    private static int EL_ID = 10000;

    private ImageView lastView = null;


    private MediaPlayer mediaPlayer;
    private String mCurrentAudioPath;

    private Uri fileUri;

    private String idUsr;
    private boolean isUpdateClicked = false;


    @Override
    public void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);

        setContentView(R.layout.formdet);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        verifyStoragePermissions(this);

        Log.e("checkview", "home created");

    }

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    /**
     * Checks if the app has permission to write to device storage
     *
     * If the app does not has permission then the user will be prompted to grant permissions
     *
     * @param activity
     */
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    @AfterViews
    protected void init() {
        Log.e("checkview", "home init");
//        Log.e("checkextra",content);
        idUsr = authHelper.getUserid();
        fillData();
    }

    @AfterInject
    public void initInject() {
        Log.e("inject", "check inject");
        dateHelper.init(this);
    }


    @Background
    void fillData() {
        /* --- sticky --
         * 0 --> daily
         * 1 --> continuous
         * 2 --> sticky
         */
        if (idAnswer == null || idAnswer.equals("")) return;
        if (!answerMap.isEmpty()) answerMap.clear();

        String idStore = null;
        String longitude = null;
        String latitude = null;
        String acc = null;

        Uri todayUri = Uri.parse(AnswerContentProvider.CONTENT_URI + "/" + idAnswer);
        //Uri todayUri = Uri.parse(AnswerContentProvider.CONTENT_URI + "/123123");
        String[] projection = {AnswerTable.COLUMN_ID, AnswerTable.COLUMN_ID_STORE, AnswerTable.COLUMN_ID_FORM, AnswerTable.COLUMN_CONTENT, AnswerTable.COLUMN_GPS_ACCURACY, AnswerTable.COLUMN_LAT, AnswerTable.COLUMN_LONG};
        Cursor cursor = getContentResolver().query(todayUri, projection, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            String anscontent = cursor.getString(cursor.getColumnIndexOrThrow(AnswerTable.COLUMN_CONTENT));
            Gson gson = new Gson();
            AnswerContentModel[] answerData = gson.fromJson(anscontent, AnswerContentModel[].class);

            if (answerData != null) {
                for (AnswerContentModel am : answerData) {
                    answerMap.put(am.idQuestion, am.answer);
                }
            }

            idStore = cursor.getString(cursor.getColumnIndexOrThrow(AnswerTable.COLUMN_ID_STORE));
            acc = cursor.getString(cursor.getColumnIndexOrThrow(AnswerTable.COLUMN_GPS_ACCURACY));
            longitude = cursor.getString(cursor.getColumnIndexOrThrow(AnswerTable.COLUMN_LONG));
            latitude = cursor.getString(cursor.getColumnIndexOrThrow(AnswerTable.COLUMN_LAT));
            idForm = cursor.getString(cursor.getColumnIndexOrThrow(AnswerTable.COLUMN_ID_FORM));
            cursor.close();
        }

        content = getFormContent(idForm);
        parseContent(content);
        Uri uri = Uri.parse(StoreContentProvider.CONTENT_URI + "/" + idStore);
        Cursor storeCursor = getContentResolver().query(uri, new String[]{StoreTable.COLUMN_STORE_NAME}, null, null, null);
        if (storeCursor.moveToFirst()) {
            placeName = storeCursor.getString(storeCursor.getColumnIndexOrThrow(StoreTable.COLUMN_STORE_NAME));
        }
        setLocation(latitude, longitude, acc);
    }

    @UiThread
    void setLocation(String latitude, String longitude, String acc) {
        String msg;

        if (latitude == null || latitude.equals("")) msg = "Not Set";
        else if (longitude == null || longitude.equals("")) msg = "Not Set";
        else msg = latitude + ", " + longitude + " (" + acc + " m)";
        formDetGpsStatus.setText(msg);
    }

    String getFormContent(String formid) {
        String formcontent = null;

        Uri formUri = Uri.parse(FormContentProvider.CONTENT_URI + "/" + formid);
        String[] projection = {FormTable.COLUMN_ID, FormTable.COLUMN_FORM_CONTENT};
        Cursor cursor = getContentResolver().query(formUri, projection, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            formcontent = cursor.getString(cursor.getColumnIndexOrThrow(FormTable.COLUMN_FORM_CONTENT));
            cursor.close();
        }

        return formcontent;
    }

    @Background
    void parseContent(String content) {
        if (content == null) return;

        Gson gson = new Gson();

        if (model == null) {
            //Log.e("checkmodel","model gen");
            model = gson.fromJson(content, QuestionModel[].class);
            //Log.e("checkmodel","model mari");
        }

        generateForm();
    }

    private boolean ada_group(List<Integer> hay, int needle) {
        return hay.contains(Integer.valueOf(needle));
    }

    @UiThread
    void generateForm() {
        if (model == null) return;
        List<Integer> groupArr = new ArrayList<Integer>();
        groupArr.add(Integer.valueOf(0));

        Log.e("checkinit", "Form generated");
        boolean hasAnswer = !answerMap.isEmpty();
        boolean groupvis = true;

        for (QuestionModel qm : model) {

            FormTextView form = FormTextView_.build(this);
            form.bind(qm.question);
            if (qm.type == FORM_TYPE_SECTIONHEADER) {
                form.setHeader();
            }

            groupvis = ada_group(groupArr, qm.group);
            if (!groupvis) form.setInVisible();

            formDetContainer.addView(form);

            switch (qm.type) {
                case FORM_TYPE_TEXT:
                    FormEditView iform0 = FormEditView_.build(this);
                    iform0.setSingleLine();
                    iform0.setQuestionId(idForm, qm.id, qm.type, qm.group);
                    iform0.setId(++EL_ID);
                    iform0.setReadonly();

                    if (hasAnswer) iform0.setVal(answerMap.get(qm.id));
                    if (!groupvis) iform0.setInVisible();

                    formDetContainer.addView(iform0);
                    break;

                case FORM_TYPE_LONGTEXT:
                    FormEditView iform1 = FormEditView_.build(this);
                    iform1.setMultiLine();
                    iform1.setQuestionId(idForm, qm.id, qm.type, qm.group);
                    iform1.setId(++EL_ID);
                    iform1.setReadonly();

                    if (hasAnswer) iform1.setVal(answerMap.get(qm.id));
                    if (!groupvis) iform1.setInVisible();

                    formDetContainer.addView(iform1);
                    break;

                case FORM_TYPE_YESNO:
                    FormYesNoView iform2 = FormYesNoView_.build(this);
                    iform2.setQuestionId(idForm, qm.id, qm.type, qm.group);
                    iform2.setId(++EL_ID);
                    if (hasAnswer) iform2.setVal(answerMap.get(qm.id));
                    iform2.setReadonly();
                    if (!groupvis) iform2.setInVisible();
                    if (qm.selval != "" && hasAnswer) {
                        ComboModel cmbModel2 = new ComboModel(qm.selval);
                        int t2 = cmbModel2.GetTarget(answerMap.get(qm.id));
                        groupArr.add(Integer.valueOf(t2));
                    }

                    formDetContainer.addView(iform2);
                    break;

                case FORM_TYPE_SELECTION:
                    FormMultipleChoiceView iform3 = FormMultipleChoiceView_.build(this);
                    iform3.setId(++EL_ID);
                    iform3.setQuestionId(idForm, qm.id, qm.type, qm.group);
                    iform3.setReadonly();
                    if (!groupvis) iform3.setInVisible();

                    if (qm.selval != "" && hasAnswer) {
                        ComboModel cmbModel3 = new ComboModel(qm.selval);
                        int t3 = cmbModel3.GetTarget(answerMap.get(qm.id));
                        groupArr.add(Integer.valueOf(t3));
                        iform3.setVal(answerMap.get(qm.id), t3);
                    }

                    formDetContainer.addView(iform3);
                    break;

                case FORM_TYPE_NUMERIC:
                case FORM_TYPE_MULTIPLICATION:
                case FORM_TYPE_MANUAL_MULTI:
                case FORM_TYPE_NUMERIC_SUM:
                    FormEditView iform4 = FormEditView_.build(this);
                    iform4.setNumeric();
                    iform4.setQuestionId(idForm, qm.id, qm.type, qm.group);
                    iform4.setId(++EL_ID);
                    iform4.setReadonly();
                    if (hasAnswer) iform4.setVal(answerMap.get(qm.id));
                    if (!groupvis) iform4.setInVisible();

                    formDetContainer.addView(iform4);
                    break;

                case FORM_TYPE_PHOTO:
                    FormImgButView iform6 = FormImgButView_.build(this);
                    iform6.setQuestionId(idForm, qm.id, qm.type, qm.group);
                    iform6.setId(++EL_ID);

                    if (hasAnswer && answerMap.get(qm.id) != null && !answerMap.get(qm.id).isEmpty()) {
                        thumbPhoto(iform6.getImage(), answerMap.get(qm.id), "photo");
                    }
                    if (!groupvis) iform6.setInVisible();

                    formDetContainer.addView(iform6);
                    break;

                case FORM_TYPE_MULTIPLE_ITEM:
                case FORM_TYPE_MULTIPLE_CHOICE:
                    FormMultipleChoiceView iform7 = FormMultipleChoiceView_.build(this);

                    iform7.setId(++EL_ID);
                    iform7.setQuestionId(idForm, qm.id, qm.type, qm.group);
                    if (hasAnswer) iform7.setVal(answerMap.get(qm.id), 0);
                    iform7.setReadonly();
                    if (!groupvis) iform7.setInVisible();

                    formDetContainer.addView(iform7);
                    break;

                case FORM_TYPE_DATE:
                    FormDateView iform8 = FormDateView_.build(this);

                    iform8.setId(++EL_ID);
                    iform8.setQuestionId(idForm, qm.id, qm.type, qm.group);
                    if (hasAnswer) iform8.setVal(answerMap.get(qm.id));
                    iform8.setReadonly();
                    if (!groupvis) iform8.setInVisible();

                    formDetContainer.addView(iform8);
                    break;

                case FORM_TYPE_TTD:
                    FormTtdView iform10 = FormTtdView_.build(this);
                    iform10.setQuestionId(idForm, qm.id, qm.type, qm.group);
                    iform10.setId(++EL_ID);

                    if (hasAnswer && answerMap.get(qm.id) != null && !answerMap.get(qm.id).isEmpty()) {
                        thumbPhoto(iform10.getImage(), answerMap.get(qm.id), "ttd");
                    }
                    if (!groupvis) iform10.setInVisible();

                    formDetContainer.addView(iform10);
                    break;
                case FORM_TYPE_BARCODE:
                    FormBarcodeView iform11 = FormBarcodeView_.build(this);
                    iform11.setId(++EL_ID);
                    iform11.setQuestionId(idForm, qm.id, qm.type, qm.group);
                    iform11.setMultiLine();
                    iform11.setRequired(qm.required);
                    iform11.setReadonly();

                    if (hasAnswer) iform11.setVal(answerMap.get(qm.id));
                    if (!groupvis) iform11.setInVisible();
                    formDetContainer.addView(iform11);
                    break;
                case FORM_TYPE_AUDIO:
                    FormAudioView iform12 = FormAudioView_.build(this);
                    iform12.onPlayClick(formPlayItem_OCL);
                    iform12.setQuestionId(idForm, qm.id, qm.type, qm.group);
                    iform12.setId(++EL_ID);
                    iform12.setRequired(qm.required);

                    if (hasAnswer && answerMap.get(qm.id) != null && !answerMap.get(qm.id).isEmpty()) {
                        mCurrentAudioPath = answerMap.get(qm.id);
                    }

                    if (!groupvis) iform12.setInVisible();
                    formDetContainer.addView(iform12);
                    break;
            }
        }
    }

    private void onPlay(boolean start) {
        if (start) {
            startPlaying();
        } else {
            stopPlaying();
        }
    }

    private void startPlaying() {
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(mCurrentAudioPath);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            Log.e("FormDet", "prepare() failed");
        }
    }

    private void stopPlaying() {
        mediaPlayer.release();
        mediaPlayer = null;
    }

    private final View.OnClickListener formPlayItem_OCL = new View.OnClickListener() {
        private boolean mStartPlaying = true;

        @Override
        public void onClick(View v) {
            ImageView button = findViewById(v.getId());

            if (mCurrentAudioPath == null) {
                showMessage("Audio masih kosong");
                return;
            }

            onPlay(mStartPlaying);
            if (mStartPlaying) {
                button.setImageDrawable(ContextCompat.getDrawable(FormHist.this, R.drawable.stop));
            } else {
                button.setImageDrawable(ContextCompat.getDrawable(FormHist.this, R.drawable.play));
            }
            mStartPlaying = !mStartPlaying;
        }
    };

    @UiThread
    void thumbPhoto(ImageView v, String path, String type) {

        if (v == null) {
            showMessage("Failed to capture image! Please try again.");
            return;
        }


        int thumbWidth = 256;
        int thumbHeight = 256;


        switch (getApplicationContext().getResources().getDisplayMetrics().densityDpi) {
            case DisplayMetrics.DENSITY_LOW:
                thumbWidth = 96;
                thumbHeight = 96;
                break;
            case DisplayMetrics.DENSITY_MEDIUM:
                thumbWidth = 128;
                thumbHeight = 128;
                break;
            case DisplayMetrics.DENSITY_HIGH:
                thumbWidth = 256;
                thumbHeight = 256;
                break;
            case DisplayMetrics.DENSITY_XHIGH:
                thumbWidth = 384;
                thumbHeight = 384;
                break;
            case DisplayMetrics.DENSITY_XXHIGH:
                thumbWidth = 512;
                thumbHeight = 512;
                break;
            case DisplayMetrics.DENSITY_XXXHIGH:
                thumbWidth = 768;
                thumbHeight = 768;
                break;
            default:
                thumbWidth = 256;
                thumbHeight = 256;
                break;
        }

        if (type.equals("ttd")) thumbWidth = thumbHeight / 4 * 6;

        Bitmap thumbBitmap = ImageHelper.createThumb(path, thumbWidth, thumbHeight);

        if (thumbBitmap == null) {
            showMessage("Failed to capture image! Please try again.");
            return;
        }

        v.setImageBitmap(thumbBitmap);
        v.setTag(R.id.TAG_PATH, path);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.form, menu);

        menu.findItem(R.id.abs_form_print).setVisible(areAllTrue());
        menu.findItem(R.id.abs_form_save).setTitle(R.string.action_update);
        menu.findItem(R.id.abs_form_save).setTitleCondensed(getString(R.string.action_update));
        return true;
    }

    public boolean areAllTrue() {
        for(QuestionModel qm : model) if(qm.type == 14 || qm.type == 15) return true;
        return false;
    }

    @OptionsItem(R.id.abs_form_save)
    void updateFormClick() {
        if (isUpdateClicked) return;

        isUpdateClicked = true;

        updateForm();
        finish();
    }

    @OptionsItem(R.id.abs_form_print)
    void printFormClick() {
        createCustomDialog();
    }

    private ImageView logoPrinter;

    private void createCustomDialog() {
        // Create an alert dialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Inflate the custom layout for the dialog
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.custom_dialog_layout, null);

        // Get references to the views in the layout
        EditText editText1 = view.findViewById(R.id.editText1);
        EditText editText2 = view.findViewById(R.id.editText2);
        logoPrinter = view.findViewById(R.id.logoPrinter);
        Button openGalleryButton = view.findViewById(R.id.openGalleryButton);

        String alamat = myPrefs.printerAlamat().get();
        editText1.setText("Jl. Penjaringan Sari YKP Pandugo 2 No.1, Penjaringan Sari, Kec. Rungkut, Surabaya, Jawa Timur 602972");
        if (!TextUtils.isEmpty(alamat)) editText1.setText(alamat);

        String contact = myPrefs.printerContact().get();
        editText2.setText("031 8700 688 - info@virtusee.com");
        if (!TextUtils.isEmpty(contact)) editText2.setText(contact);

        if (!TextUtils.isEmpty(myPrefs.printerLogo().get())) Glide.with(this).load(new File(myPrefs.printerLogo().get())).into(logoPrinter);

        // Set the custom layout to the dialog builder
        builder.setView(view);

        // Set the positive button (optional)
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Handle user interaction with the dialog, if needed
                myPrefs.printerAlamat().put(editText1.getText().toString());
                myPrefs.printerContact().put(editText2.getText().toString());
                // Do something with the entered text here
                browseBluetoothDevice();
            }
        });

        // Set the negative button (optional)
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Handle cancel button click (e.g., dismiss the dialog)
                dialog.dismiss();
            }
        });

        openGalleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open the phone gallery using an Intent
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 1); // Request code for handling the result
            }
        });

        // Create and show the dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public String getRealPathFromURI(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
        return cursor.getString(idx);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Handle the result from the phone gallery
        if (requestCode == 1 && resultCode == RESULT_OK) {
            // Get the selected image URI from the intent
            Uri selectedImageUri = data.getData();
            copyImageToAppStorage(selectedImageUri);
            logoPrinter.setImageURI(selectedImageUri);
            // Do something with the selected image, e.g., display it in an ImageView
        }
    }

    private boolean isUriFromApp(Uri uri) {
        return uri.getAuthority().equals(BuildConfig.APPLICATION_ID + ".provider.fileprovider");
    }

    private void copyImageToAppStorage(Uri imageUri) {
        try {
            // Create a destination file within your app's "images" directory
            String fileName = UUID.randomUUID().toString() + ".jpg"; // Unique filename
            File destinationFile = FileHelper.getPrivateImageFile(this, "123");

            // Open input and output streams
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            OutputStream outputStream = new FileOutputStream(destinationFile);

            // Copy data in chunks
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            // Close streams
            inputStream.close();
            outputStream.close();

            // Get the FileProvider URI for the copied image
            myPrefs.printerLogo().put(destinationFile.getAbsolutePath());
            Log.d("Test", "destinationFile: " + destinationFile.getAbsolutePath());
//            Uri copiedImageUri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider.fileprovider", destinationFile);

            // (handle copied image URI here)

        } catch (IOException e) {
            e.printStackTrace();
            // Handle copying error
        }
    }

    private void updateForm() {
        ArrayList<String> photos = new ArrayList<>();

        int childcount = formDetContainer.getChildCount();
        for (int i = 0; i < childcount; i++) {
            FormGroupView v = (FormGroupView) formDetContainer.getChildAt(i);
            if (!v.isInputable()) continue;
            if (v.isImage() && !v.getVal().equals("")) photos.add(v.getVal());
        }

        SyncServ.enqueuePostUpdate(this, authHelper.getUserid(), authHelper.getFormattedUsername(), authHelper.getPassword(), idAnswer, photos);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("checkdestroy", "Destroy");
//        this.saveForm();

        if (lastView != null) {
            Log.e("checkimgdestroy", "Destroy");
            Bitmap dd = ((BitmapDrawable) lastView.getDrawable()).getBitmap();
            //if(dd!=null) dd.recycle();
            lastView.setImageDrawable(null);
            lastView = null;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.e("checkpause", "Pause");
    }

    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.e("savedinstance", "dfdfdf");
        super.onSaveInstanceState(outState);
    }

    @SuppressWarnings("unchecked")
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        // Always call the superclass so it can restore the view hierarchy
        super.onRestoreInstanceState(savedInstanceState);
    }

    @UiThread
    public void showMessage(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        onBackPressed();
        return true;
    }

    public interface OnBluetoothPermissionsGranted {
        void onPermissionsGranted();
    }

    public static final int PERMISSION_BLUETOOTH = 1;
    public static final int PERMISSION_BLUETOOTH_ADMIN = 2;
    public static final int PERMISSION_BLUETOOTH_CONNECT = 3;
    public static final int PERMISSION_BLUETOOTH_SCAN = 4;

    public OnBluetoothPermissionsGranted onBluetoothPermissionsGranted;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            switch (requestCode) {
                case FormHist.PERMISSION_BLUETOOTH:
                case FormHist.PERMISSION_BLUETOOTH_ADMIN:
                case FormHist.PERMISSION_BLUETOOTH_CONNECT:
                case FormHist.PERMISSION_BLUETOOTH_SCAN:
                    this.checkBluetoothPermissions(this.onBluetoothPermissionsGranted);
                    break;
            }
        }
    }

    public void checkBluetoothPermissions(OnBluetoothPermissionsGranted onBluetoothPermissionsGranted) {
        this.onBluetoothPermissionsGranted = onBluetoothPermissionsGranted;
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.S && ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.BLUETOOTH}, FormHist.PERMISSION_BLUETOOTH);
        } else if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.S && ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.BLUETOOTH_ADMIN}, FormHist.PERMISSION_BLUETOOTH_ADMIN);
        } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S && ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.BLUETOOTH_CONNECT}, FormHist.PERMISSION_BLUETOOTH_CONNECT);
        } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S && ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_SCAN}, FormHist.PERMISSION_BLUETOOTH_SCAN);
        } else {
            this.onBluetoothPermissionsGranted.onPermissionsGranted();
        }
    }

    public void browseBluetoothDevice() {
        this.checkBluetoothPermissions(() -> {
            final BluetoothConnection[] bluetoothDevicesList = (new BluetoothPrintersConnections()).getList();

            if (bluetoothDevicesList != null) {
                final String[] items = new String[bluetoothDevicesList.length];
                int i = 0;
                for (BluetoothConnection device : bluetoothDevicesList) {
                    items[i++] = device.getDevice().getName();
                }

                AlertDialog.Builder alertDialog = new AlertDialog.Builder(FormHist.this);
                alertDialog.setTitle("Bluetooth printer selection");
                alertDialog.setItems(
                        items,
                        (dialogInterface, i1) -> {
                            this.checkBluetoothPermissions(() -> {
                                new AsyncBluetoothEscPosPrint(
                                        this,
                                        new AsyncEscPosPrint.OnPrintFinished() {
                                            @Override
                                            public void onError(AsyncEscPosPrinter asyncEscPosPrinter, int codeException) {
                                                Log.e("Async.OnPrintFinished", "AsyncEscPosPrint.OnPrintFinished : An error occurred !");
                                            }

                                            @Override
                                            public void onSuccess(AsyncEscPosPrinter asyncEscPosPrinter) {
                                                Log.i("Async.OnPrintFinished", "AsyncEscPosPrint.OnPrintFinished : Print is finished !");
                                            }
                                        }).execute(this.getAsyncEscPosPrinter(bluetoothDevicesList[i1]));
                            });
                        }
                );

                if (items.length == 0) {
                    alertDialog.setMessage("No printer found\nPair with bluetooth printer");
                }

                AlertDialog alert = alertDialog.create();
                alert.show();
            }
        });
    }

    public AsyncEscPosPrinter getAsyncEscPosPrinter(DeviceConnection printerConnection) {
        AsyncEscPosPrinter printer = new AsyncEscPosPrinter(printerConnection, 203, 48f, 32);
        String username = myPrefs.username().get();
        String domain = myPrefs.domain().get();
        StringBuilder text = new StringBuilder();
        StringBuilder pembayaran = new StringBuilder();
        long totalHarga = 0;
        int formMulti = 0;
        boolean printNext = false;

        for (int i = 0; i < model.length; i++) {
            boolean isMultiplication = formMulti != 0;
            QuestionModel currentModel = model[i];
            if (answerMap.get(currentModel.id) != null && answerMap.get(currentModel.id).equals("")) continue;
            Log.d("TEST", currentModel.toString());

           if (currentModel.type == 15) {
                text.append("[L]")
                        .append(currentModel.question)
                        .append("[R]")
                        .append(answerMap.get(currentModel.id));
                formMulti = 2;
           } else if (currentModel.type == 14) {
               String[] selval = model[i].selval.split("#####");
               int harga = Integer.parseInt(selval[0]);
               text.append("[L]")
                       .append(model[i].question)
                       .append("[R]")
                       .append(answerMap.get(model[i].id))
                       .append("[R]")
                       .append(harga);
               formMulti = 1;
           } else if (currentModel.type == 4) {
               if (currentModel.question.equals("TOTAL HARGA")) {
                   totalHarga = Long.parseLong(answerMap.get(currentModel.id));
               }

               if (isMultiplication) {
                   text.append("[R]")
                           .append(String.format("%,d", Long.parseLong(answerMap.get(currentModel.id))));

                   if (formMulti == 1) {
                       text.append("\n");
                   }
                   formMulti--;
               }

               if (printNext) {
                   pembayaran.append("[R]")
                           .append(String.format("%,d", Long.parseLong(answerMap.get(currentModel.id))))
                           .append("\n");
                   printNext = false;
               }
           } else if (currentModel.type == 3) {
               switch (answerMap.get(currentModel.id)) {
                   case "Cash":
                   case "PPN":
                       printNext = true;
                       break;
               }
           }
        }

        String alamat = myPrefs.printerAlamat().getOr("Jl. Penjaringan Sari YKP Pandugo 2 No.1, Penjaringan Sari, Kec. Rungkut, Surabaya, Jawa Timur 602972");
        String contact = myPrefs.printerContact().getOr("031 8700 688 - info@virtusee.com");
        String image = "[C]<img>" + PrinterTextParserImg.bitmapToHexadecimalString(printer, this.getApplicationContext().getResources().getDrawableForDensity(R.drawable.logo_with_text, DisplayMetrics.DENSITY_LOW))+"</img>\n";
        Bitmap bitmap = BitmapFactory.decodeFile(new File(myPrefs.printerLogo().get()).getAbsolutePath());
        Log.d("TEST", "path: " + new File(myPrefs.printerLogo().get()).getAbsolutePath());
        if (!TextUtils.isEmpty(myPrefs.printerLogo().get())) {
            image = "[C]<img>" + PrinterTextParserImg.bitmapToHexadecimalString(printer, bitmap)+"</img>\n";
        }
        return printer.addTextToPrint(
                image +
                        "[L]\n" +
                        "[L]" + alamat +
                        "[L]\n" +
//                        "[C]================================\n" +
                        "[C]===== " + placeName + " =====\n" +
                        text +
                        "[L][R]---------------\n" +
                        "[L]Total [R]" + String.format(Locale.getDefault(), "%,d", totalHarga) + "\n" +
                        pembayaran +
                        "[L]\n" +
                        "[C]" + username + " - " + domain + "\n" +
                        "[C]" + contact
        );
    }
}
