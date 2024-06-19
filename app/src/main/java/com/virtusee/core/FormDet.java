package com.virtusee.core;

import static androidx.core.content.FileProvider.getUriForFile;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.gson.Gson;
import com.virtusee.contentprovider.AnswerContentProvider;
import com.virtusee.contentprovider.AudioContentProvider;
import com.virtusee.contentprovider.MasterContentProvider;
import com.virtusee.contentprovider.PhotoContentProvider;
import com.virtusee.db.AnswerTable;
import com.virtusee.db.MasterTable;
import com.virtusee.db.VSDbHelper;
import com.virtusee.helper.AbsenHelper;
import com.virtusee.helper.AuthHelper;
import com.virtusee.helper.CurrencyHelper;
import com.virtusee.helper.DateHelper;
import com.virtusee.helper.FileHelper;
import com.virtusee.helper.GpsHelper;
import com.virtusee.helper.ImageHelper;
import com.virtusee.helper.PermissionHelper;
import com.virtusee.helper.PrefHelper_;
import com.virtusee.listener.DialogListener;
import com.virtusee.listener.GpsListener;
import com.virtusee.model.AnswerContentModel;
import com.virtusee.model.AnswerGroupModel;
import com.virtusee.model.ComboModel;
import com.virtusee.model.MasterModel;
import com.virtusee.model.QuestionModel;
import com.virtusee.model.StoreAbsenModel;
import com.virtusee.receiver.WdayReceiver;
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
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.zip.Adler32;
import java.util.zip.CheckedInputStream;

@EActivity
public class FormDet extends AppCompatActivity implements GpsListener, DialogListener {
    @ViewById
    LinearLayout formDetContainer;

    @ViewById
    TextView formDetGpsStatus;

    @Extra
    String content;

    @Extra
    String idForm;

    @Extra
    String formTitle;

    @Extra
    String idStore;

    @Extra
    int sticky;

    @Extra
    int mandatory;

    @Pref
    PrefHelper_ myPrefs;

    @Bean
    AbsenHelper absenHelper;

    @Bean
    GpsHelper gpsHelper;

    @Bean
    DateHelper dateHelper;

    @Bean
    AuthHelper authHelper;

    private Bundle navData;
    private QuestionModel[] model;
    private HashMap<Integer, Integer> nodeMap = new HashMap<>();
    private List<AnswerGroupModel> node = new ArrayList<>();
    private HashMap<String, String> answerMap = new HashMap<>();
    private HashMap<Integer, List<FormGroupView>> groupMap = new HashMap<>();
    private String answerId;
    private boolean isValid = true;
    private HashMap<String, String> masterMap = new HashMap<>();
    private HashMap<String, String> beforeMasterMap = new HashMap<>();
    private HashMap<String, String> idMasterMap = new HashMap<>();
    private HashMap<String, String> typeMasterMap = new HashMap<>();
    private HashMap<Integer, Integer> totalMap = new HashMap<>();

    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    private static final int CAPTURE_IMAGE_PREVIEW_REQUEST_CODE = 200;
    public static final int CAPTURE_BARCODE_REQUEST_CODE = 300;
    private static final int CAPTURE_TTD_CODE = 500;
    private static String imgPath;
    private static int IMG_TARGET_WIDTH = 800;
    private static int IMG_TARGET_HEIGHT = 600;
    private static int IMG_TARGET_COMPRESS = 100;

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
    private static final int FORM_TYPE_CURRENCY = 16;

    private static int EL_ID = 10000;
    private static final int ALARM_ID = 123;

    private ImageView lastView = null;

    private Uri fileUri, audioUri;

    private MediaRecorder mediaRecorder;
    private MediaPlayer mediaPlayer;

    private boolean isSaveClicked = false;
    private Location currentLocation, lastLocation;
    private String idUsr;
    private String entertime;

    private boolean doubleBackToExitPressedOnce;
    private Handler mHandler = new Handler();
    private String mCurrentPhotoPath, mCurrentAudioPath;
    private String masterBefore;

    private EditText editTextHarga = null;
    private EditText editTextPPN = null;

    private EditText provinceMaster = null;
    private EditText districtMaster = null;
    private EditText regencyMaster = null;
    private EditText villageMaster = null;

    private final Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            doubleBackToExitPressedOnce = false;
        }
    };

    @Override
    public void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);

        setContentView(R.layout.formdet);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        //lastView = null;
        //if(fileUri==null) fileUri = FileHelper.getOutputMediaFileUri(FileHelper.MEDIA_TYPE_IMAGE);
        // Setup the location update Pending Intents
        Log.e("checkview", "home created");

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.form, menu);
        return true;
    }

    //@SuppressLint("SimpleDateFormat")
    @OptionsItem(R.id.abs_form_save)
    void saveFormClick() {
        this.isSaveClicked = true;

        saveForm();
        if (isValid) this.finish();
    }

    @Override
    public void OnDialogOkay(int type) {

    }

    @Override
    public void OnDialogCancel(int type) {

    }

    @AfterViews
    protected void init() {
        Log.e("checkview", "home init");
//        Log.e("checkextra",content);
        idUsr = authHelper.getUserid();
        fillData();
        parseContent(content);
        try {
            getSupportActionBar().setTitle(formTitle);
        } catch (NullPointerException e) {
        }

        /*

        node.add(new AnswerGroupModel(0));
        node.add(node.get(0).addChild(new AnswerGroupModel(1)));
        node.add(node.get(0).addChild(new AnswerGroupModel(2)));
        node.add(node.get(0).addChild(new AnswerGroupModel(3)));
        node.add(node.get(1).addChild(new AnswerGroupModel(4)));
        node.add(node.get(1).addChild(new AnswerGroupModel(9)));
        node.add(node.get(2).addChild(new AnswerGroupModel(7)));
        node.add(node.get(3).addChild(new AnswerGroupModel(6)));
        node.add(node.get(7).addChild(new AnswerGroupModel(10)));
        AnswerGroupModel tree[] = new AnswerGroupModel[6];

        tree[0] = new AnswerGroupModel(0);
        tree[1] = tree[0].addChild(new AnswerGroupModel(1));
        tree[2] = tree[0].addChild(new AnswerGroupModel(2));
        tree[3] = tree[1].addChild(new AnswerGroupModel(3));
        tree[4] = tree[2].addChild(new AnswerGroupModel(4));
        tree[5] = tree[2].addChild(new AnswerGroupModel(5));
*/


        // travTree(node.get(0),"   ");
    }

    private void travTree(AnswerGroupModel node) {
//        Log.e("tree",appender + String.valueOf(node.getData()));
        if (node.getData() > 0) set_group_invisible(node.getData());

        for (AnswerGroupModel child : node.getChildren()) {
            travTree(child);
        }
    }


    @AfterInject
    public void initInject() {
        Log.e("inject", "check inject");
        gpsHelper.init(this, this);
        dateHelper.init(this);
        absenHelper.init(this);
        entertime = dateHelper.getCurrentTimestamp();
    }


    @Background
    void fillData() {
        /* --- sticky --
         * 0 --> daily
         * 1 --> continuous
         * 2 --> sticky
         */
        if (!answerMap.isEmpty()) answerMap.clear();

        Uri todayUri;
        switch (sticky) {
            case 1:
                // -- continuos : always insert new record
                return;
            case 2:
                // -- sticky : always update old record
                todayUri = Uri.parse(AnswerContentProvider.CONTENT_URI + "/store/" + idStore + "/form/" + idForm + "/sticky");
                break;
            default:
                // -- daily : daily update
                todayUri = Uri.parse(AnswerContentProvider.CONTENT_URI + "/store/" + idStore + "/form/" + idForm + "/today");
                break;
        }

        //Uri todayUri = Uri.parse(AnswerContentProvider.CONTENT_URI + "/123123");
        String[] projection = {AnswerTable.COLUMN_ID, AnswerTable.COLUMN_CONTENT};
        Cursor cursor = getContentResolver().query(todayUri, projection, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            answerId = cursor.getString(cursor.getColumnIndexOrThrow(AnswerTable.COLUMN_ID));
            String content = cursor.getString(cursor.getColumnIndexOrThrow(AnswerTable.COLUMN_CONTENT));
            Gson gson = new Gson();
            AnswerContentModel[] answerData = gson.fromJson(content, AnswerContentModel[].class);

            if (answerData != null) {
                for (AnswerContentModel am : answerData) {
                    answerMap.put(am.idQuestion, am.answer);
                }
            }
            cursor.close();
        }
    }

    @Background
    void parseContent(String content) {
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

    private void add_groupmap(int group, FormGroupView frm) {
        Integer ig = Integer.valueOf(group);
        if (!groupMap.containsKey(ig)) groupMap.put(ig, new ArrayList<>());
        groupMap.get(ig).add(frm);
    }

    @UiThread
    void generateForm() {
        if (model == null) return;

        List<Integer> groupArr = new ArrayList<Integer>();
        groupArr.add(Integer.valueOf(0));

        Log.e("checkinit", "Form generated");
        boolean hasAnswer = !answerMap.isEmpty();
        boolean groupvis = true;

        String imgClick = null;
        if (this.navData != null) imgClick = this.navData.getString("imgClick");

        nodeMap.put(0, 0);
        node.add(new AnswerGroupModel(0));


        /*
        node.add(node.get(0).addChild(new AnswerGroupModel(1)));
        node.add(node.get(0).addChild(new AnswerGroupModel(2)));
        node.add(node.get(0).addChild(new AnswerGroupModel(3)));
        node.add(node.get(1).addChild(new AnswerGroupModel(4)));
        node.add(node.get(1).addChild(new AnswerGroupModel(9)));
        node.add(node.get(2).addChild(new AnswerGroupModel(7)));
        node.add(node.get(3).addChild(new AnswerGroupModel(6)));
        node.add(node.get(7).addChild(new AnswerGroupModel(10)));
        */

        for (QuestionModel qm : model) {

            FormTextView form = FormTextView_.build(this);
            form.bind(qm.question);

            if (qm.type == FORM_TYPE_SECTIONHEADER) {
                form.setHeader();
            }

            groupvis = ada_group(groupArr, qm.group);
            if (!groupvis) form.setInVisible();

            formDetContainer.addView(form);
            add_groupmap(qm.group, form);

            switch (qm.type) {
                case FORM_TYPE_TEXT:
                    FormEditView iform0 = FormEditView_.build(this);
                    iform0.setSingleLine();
                    iform0.setQuestionId(idForm, qm.id, qm.type, qm.group);
                    iform0.setRequired(qm.required);
                    iform0.setId(++EL_ID);

                    if (hasAnswer) iform0.setVal(answerMap.get(qm.id));
                    if (!groupvis) iform0.setInVisible();
                    formDetContainer.addView(iform0);
                    add_groupmap(qm.group, iform0);
                    break;

                case FORM_TYPE_LONGTEXT:
                    FormEditView iform1 = FormEditView_.build(this);
                    iform1.setMultiLine();
                    iform1.setQuestionId(idForm, qm.id, qm.type, qm.group);
                    iform1.setId(++EL_ID);
                    iform1.setRequired(qm.required);
                    if (hasAnswer) iform1.setVal(answerMap.get(qm.id));
                    if (!groupvis) iform1.setInVisible();
                    formDetContainer.addView(iform1);
                    add_groupmap(qm.group, iform1);
                    break;

                case FORM_TYPE_YESNO:
                    FormYesNoView iform2 = FormYesNoView_.build(this);
                    iform2.setQuestionId(idForm, qm.id, qm.type, qm.group);
                    iform2.setId(++EL_ID);
                    iform2.setRequired(qm.required);
                    if (hasAnswer) iform2.setVal(answerMap.get(qm.id));
                    if (qm.selval != "") {
                        ComboModel cmbModel2 = new ComboModel(qm.selval);
                        iform2.onClick(new formYesNo_OCL(iform2, cmbModel2.target));
                        addNode(cmbModel2.target, qm.group);
                        if (hasAnswer) {
                            int t2 = cmbModel2.GetTarget(answerMap.get(qm.id));
                            groupArr.add(Integer.valueOf(t2));
                        }
                    }
                    if (!groupvis) iform2.setInVisible();
                    formDetContainer.addView(iform2);
                    add_groupmap(qm.group, iform2);
                    break;

                case FORM_TYPE_SELECTION:
                    FormMultipleChoiceView iform3 = FormMultipleChoiceView_.build(this);

                    iform3.setId(++EL_ID);
                    iform3.setQuestionId(idForm, qm.id, qm.type, qm.group);
                    iform3.setRequired(qm.required);
                    iform3.setIcon();
                    if (qm.selval != "") {
                        ComboModel cmbModel3 = new ComboModel(qm.selval);
                        iform3.onClick(new formMultipleChoiceItem_OCL(qm.question, cmbModel3.val, cmbModel3.target, true));
                        addNode(cmbModel3.target, qm.group);
                        if (hasAnswer) {
                            int t3 = cmbModel3.GetTarget(answerMap.get(qm.id));
                            groupArr.add(Integer.valueOf(t3));
                            iform3.setVal(answerMap.get(qm.id), t3);
                        }
                    }

                    if (!groupvis) iform3.setInVisible();
                    formDetContainer.addView(iform3);
                    add_groupmap(qm.group, iform3);
                    break;

                case FORM_TYPE_NUMERIC:
                case FORM_TYPE_NUMERIC_SUM:
                    FormEditView iform4 = FormEditView_.build(this);
                    iform4.setNumeric();
                    iform4.setQuestionId(idForm, qm.id, qm.type, qm.group);
                    iform4.setId(++EL_ID);
                    iform4.setRequired(qm.required);

                    if (hasAnswer) iform4.setVal(answerMap.get(qm.id));
                    if (!groupvis) iform4.setInVisible();
                    formDetContainer.addView(iform4);
                    add_groupmap(qm.group, iform4);

                    if (qm.question.equals("TOTAL HARGA")) {
                        editTextHarga = findViewById(EL_ID);
                    } else if (qm.question.equals("PPN")) {
                        editTextPPN = findViewById(EL_ID);
                    }

                    break;

                case FORM_TYPE_CURRENCY:
                    FormEditView iform16 = FormEditView_.build(this);
                    iform16.setNumeric();
                    iform16.setQuestionId(idForm, qm.id, qm.type, qm.group);
                    iform16.setId(++EL_ID);
                    iform16.setRequired(qm.required);
                    EditText editText = iform16.getEditText();
                    editText.addTextChangedListener(new CurrencyHelper(editText));

                    if (hasAnswer) iform16.setVal(answerMap.get(qm.id));
                    if (!groupvis) iform16.setInVisible();
                    formDetContainer.addView(iform16);
                    add_groupmap(qm.group, iform16);
                    break;

                case FORM_TYPE_MULTIPLICATION:
                    FormEditView iform14 = FormEditView_.build(this);
                    iform14.setNumeric();
                    iform14.setQuestionId(idForm, qm.id, qm.type, qm.group);
                    iform14.setId(++EL_ID);
                    iform14.setRequired(qm.required);

                    if (hasAnswer) iform14.setVal(answerMap.get(qm.id));
                    if (!groupvis) iform14.setInVisible();
                    formDetContainer.addView(iform14);
                    add_groupmap(qm.group, iform14);

                    String[] selval = qm.selval.split("#####");
                    int harga = Integer.parseInt(selval[0]);
                    iform14.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                        }

                        @Override
                        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                        }

                        @Override
                        public void afterTextChanged(Editable editable) {
                            int id = iform14.idForm();
                            EditText nextQuestion = findViewById(id+1);
                            String qty = iform14.getVal();
                            if (TextUtils.isEmpty(qty)) {
                                nextQuestion.setText("");
                            } else {
                                int total = Integer.parseInt(qty) * harga;
                                nextQuestion.setText(String.valueOf(total));
                                totalMap.put(id+1, total);
                                hitungTotal();
                            }
                        }
                    });

                    break;

                case FORM_TYPE_MANUAL_MULTI:
                    FormEditView iform15 = FormEditView_.build(this);
                    iform15.setNumeric();
                    iform15.setQuestionId(idForm, qm.id, qm.type, qm.group);
                    iform15.setId(++EL_ID);
                    iform15.setRequired(qm.required);

                    if (hasAnswer) iform15.setVal(answerMap.get(qm.id));
                    if (!groupvis) iform15.setInVisible();
                    formDetContainer.addView(iform15);
                    add_groupmap(qm.group, iform15);

                    iform15.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                        }

                        @Override
                        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                        }

                        int total = 0;

                        @Override
                        public void afterTextChanged(Editable editable) {
                            int id = iform15.idForm();
                            EditText hargaQuestion = findViewById(id+1);
                            String validationHarga = hargaQuestion.getText().toString();
                            int harga = Integer.parseInt(validationHarga.equals("") ? "0" : validationHarga);
                            EditText totalQuestion = findViewById(id+2);
                            String qty = iform15.getVal();
                            if (TextUtils.isEmpty(qty)) {
                                totalQuestion.setText("");
                            } else {
                                total = Integer.parseInt(qty) * harga;
                                totalQuestion.setText(String.valueOf(total));
                                totalMap.put(id+2, total);
                                hitungTotal();
                            }
                            hargaQuestion.addTextChangedListener(new TextWatcher() {
                                @Override
                                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                                }

                                @Override
                                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                                }

                                @Override
                                public void afterTextChanged(Editable editable) {
                                    String validationHarga = hargaQuestion.getText().toString();
                                    int harga = Integer.parseInt(validationHarga.equals("") ? "0" : validationHarga);
                                    if (TextUtils.isEmpty(qty)) {
                                        totalQuestion.setText("");
                                    } else {
                                        total = Integer.parseInt(qty) * harga;
                                        totalQuestion.setText(String.valueOf(total));
                                        totalMap.put(id+2, total);
                                        hitungTotal();
                                    }
                                }
                            });
                        }
                    });

                    break;

                case FORM_TYPE_PHOTO:
                    if (qm.photo_type != null && qm.photo_type.equalsIgnoreCase("high")) {
                        IMG_TARGET_COMPRESS = 100;
                        IMG_TARGET_WIDTH = 1280;
                        IMG_TARGET_HEIGHT = 720;
                    } else {
                        IMG_TARGET_COMPRESS = 80;
                        IMG_TARGET_WIDTH = 800;
                        IMG_TARGET_HEIGHT = 600;
                    }

                    FormImgButView iform6 = FormImgButView_.build(this);
                    iform6.onClick(formImgButItem_OCL);
                    iform6.setQuestionId(idForm, qm.id, qm.type, qm.group);
                    iform6.setId(++EL_ID);
                    iform6.setRequired(qm.required);

                    if (imgClick != null && qm.id.equals(imgClick)) {
                        lastView = iform6.getImage();
                        Log.e("lastview", "last view generated");
                    }

                    if (hasAnswer && answerMap.get(qm.id) != null && !answerMap.get(qm.id).isEmpty()) {
                        thumbPhoto(iform6.getImage(), answerMap.get(qm.id), "photo");
                    }
                    if (!groupvis) iform6.setInVisible();
                    formDetContainer.addView(iform6);
                    add_groupmap(qm.group, iform6);
                    break;

                case FORM_TYPE_MULTIPLE_CHOICE:
                    FormMultipleChoiceView iform7 = FormMultipleChoiceView_.build(this);

                    iform7.setId(++EL_ID);
                    iform7.setQuestionId(idForm, qm.id, qm.type, qm.group);
                    iform7.setRequired(qm.required);
                    iform7.setIcon();
                    if (hasAnswer) iform7.setVal(answerMap.get(qm.id), 0);

                    if (qm.selval != "") {
                        ComboModel cmbModel7 = new ComboModel(qm.selval);
                        iform7.onClick(new formMultipleChoiceItem_OCL(qm.question, cmbModel7.val, null, false));
                    }

                    if (!groupvis) iform7.setInVisible();
                    formDetContainer.addView(iform7);
                    add_groupmap(qm.group, iform7);
                    break;

                case FORM_TYPE_DATE:
                    FormDateView iform8 = FormDateView_.build(this);

                    iform8.setId(++EL_ID);
                    iform8.setQuestionId(idForm, qm.id, qm.type, qm.group);
                    iform8.setRequired(qm.required);
                    iform8.setIcon();
                    if (hasAnswer) iform8.setVal(answerMap.get(qm.id));
                    iform8.onClick(new formDateItem_OCL(qm.question));

                    if (!groupvis) iform8.setInVisible();
                    formDetContainer.addView(iform8);
                    add_groupmap(qm.group, iform8);
                    break;


                case FORM_TYPE_TTD:
                    FormTtdView iform10 = FormTtdView_.build(this);
                    iform10.onClick(formTtdItem_OCL);
                    iform10.setQuestionId(idForm, qm.id, qm.type, qm.group);
                    iform10.setId(++EL_ID);
                    iform10.setRequired(qm.required);

                    if (imgClick != null && qm.id.equals(imgClick)) {
                        lastView = iform10.getImage();
                        Log.e("lastview", "last view generated");
                    }

                    if (hasAnswer && answerMap.get(qm.id) != null && !answerMap.get(qm.id).isEmpty()) {
                        thumbPhoto(iform10.getImage(), answerMap.get(qm.id), "ttd");
                    }
                    if (!groupvis) iform10.setInVisible();
                    formDetContainer.addView(iform10);
                    add_groupmap(qm.group, iform10);
                    break;

                case FORM_TYPE_MULTIPLE_ITEM:
                    FormMultipleChoiceView iform11 = FormMultipleChoiceView_.build(this);

                    iform11.setId(++EL_ID);
                    iform11.setQuestionId(idForm, qm.id, qm.type, qm.group);
                    iform11.onClick(new formMultipleItem_OCL(qm.question, qm.master_type, qm.id));
                    iform11.setRequired(qm.required);
                    iform11.setIcon();

                    if (hasAnswer) iform11.setVal(answerMap.get(qm.id), 0);

                    if (!groupvis) iform11.setInVisible();
                    formDetContainer.addView(iform11);
                    add_groupmap(qm.group, iform11);

                    Log.d("TEST", "masterType: " + qm.master_type);
                    if (qm.master_type.equalsIgnoreCase("province")) provinceMaster = findViewById(EL_ID);
                    if (qm.master_type.equalsIgnoreCase("district")) districtMaster = findViewById(iform11.idForm());
                    if (qm.master_type.equalsIgnoreCase("regency")) regencyMaster = findViewById(iform11.idForm());
                    if (qm.master_type.equalsIgnoreCase("village")) villageMaster = findViewById(iform11.idForm());

                    break;

                case FORM_TYPE_BARCODE:
                    int id = ++EL_ID;
                    FormBarcodeView iform12 = FormBarcodeView_.build(this);
                    iform12.setOnClickListener(new formBarcodeItem_OCL(id));
                    iform12.setId(id);
                    iform12.setQuestionId(idForm, qm.id, qm.type, qm.group);
                    iform12.setMultiLine();
                    iform12.setRequired(qm.required);

                    if (hasAnswer) iform12.setVal(answerMap.get(qm.id));
                    if (!groupvis) iform12.setInVisible();
                    formDetContainer.addView(iform12);
                    add_groupmap(qm.group, iform12);
                    break;

                case FORM_TYPE_AUDIO:
                    FormAudioView iform13 = FormAudioView_.build(this);
                    iform13.onMicClick(formMicItem_OCL);
                    iform13.onPlayClick(formPlayItem_OCL);
                    iform13.setQuestionId(idForm, qm.id, qm.type, qm.group);
                    iform13.setId(++EL_ID);
                    iform13.setRequired(qm.required);

                    if (hasAnswer && answerMap.get(qm.id) != null && !answerMap.get(qm.id).isEmpty()) {
                        mCurrentAudioPath = answerMap.get(qm.id);
                    }

                    if (!groupvis) iform13.setInVisible();
                    formDetContainer.addView(iform13);
                    add_groupmap(qm.group, iform13);
                    break;

            }
        }
    }

    private void addNode(int[] target, int group) {
        int g = nodeMap.get(Integer.valueOf(group)).intValue();
        int lastn = node.size();
        for (int i = 0, n = target.length; i < n; i++) {
            if (target[i] == 0) continue;
            node.add(node.get(g).addChild(new AnswerGroupModel(target[i])));
            nodeMap.put(Integer.valueOf(target[i]), Integer.valueOf(lastn));
            lastn++;
        }
    }

    private void hitungTotal() {
        if (editTextHarga == null) return;
        int totalHarga = 0;
        for (int harga : totalMap.values()) {
            totalHarga += harga;
        }
        editTextHarga.setText(""+totalHarga);
        if (editTextPPN == null) return;
        int ppn = (int) (totalHarga * 0.11);
        editTextPPN.setText(""+ppn);
    }

    @Override
    public void onGpsSet(Location location) {
        String msg = "Searching for GPS";
        if (location != null) {
            lastLocation = location;
            msg = location.getLatitude() + ", " + location.getLongitude() + " (" + String.valueOf(location.getAccuracy()) + " m)";
            Geocoder geocoder;
            List<Address> addresses;
            geocoder = new Geocoder(this, Locale.getDefault());

            if (provinceMaster != null) {
                try {
                    addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                String district = addresses.get(0).getLocality();
                String province = addresses.get(0).getAdminArea();
                String regency = addresses.get(0).getSubAdminArea();
                String village = addresses.get(0).getSubLocality();

                setMaster(provinceMaster, province);
                setMaster(districtMaster, district);
                setMaster(regencyMaster, regency);
                setMaster(villageMaster, village);
            }
        }
        formDetGpsStatus.setText(msg);
    }

    private void setMaster(EditText master, String text) {
        if (master == null) return;

        master.setText(text.toUpperCase());
        master.setTag(R.id.TAG_MC_ID, text.toUpperCase());
    }

    @Override
    public void onGpsMock() {
        String msg = "FAKE GPS! Coordinate will not be captured";
        formDetGpsStatus.setText(msg);
    }

    @Override
    public void onGpsOff() {
        String msg = "GPS is turned off";
        formDetGpsStatus.setText(msg);
    }

    @Override
    public void onGpsSearch() {
        String msg = "Searching for GPS";
        formDetGpsStatus.setText(msg);
    }

    private void set_group_visible(int targetgroup) {
        List<FormGroupView> fgv = groupMap.get(Integer.valueOf(targetgroup));
        if (fgv == null) return;

        for (FormGroupView form : fgv) {
            form.setVisible();
        }
    }

    private void set_group_invisible(int targetgroup) {
        List<FormGroupView> fgv = groupMap.get(Integer.valueOf(targetgroup));
        if (fgv == null) return;

        for (FormGroupView form : fgv) {
            form.setInVisible();
        }
    }

    private void disp_group(int targetgroup, int prevgroup) {
        if (prevgroup > 0) {
            set_group_invisible(prevgroup);
            int g = nodeMap.get(Integer.valueOf(prevgroup));
            travTree(node.get(g));
        }
        if (targetgroup > 0) set_group_visible(targetgroup);
    }

    private class formYesNo_OCL implements RadioGroup.OnCheckedChangeListener {
        FormYesNoView v;
        int[] target;

        public formYesNo_OCL(FormYesNoView v, int[] target) {
            this.v = v;
            this.target = target;
        }

        @Override
        public void onCheckedChanged(RadioGroup radioGroup, int i) {
            RadioButton rb = (RadioButton) findViewById(i);

            int tag = Integer.parseInt(rb.getTag().toString());
            int togtag = (tag == 1) ? 0 : 1;
            int act = this.target[tag];
            int prevact = this.target[togtag];
            disp_group(act, prevact);
            this.v.unSetError();
        }
    }

    private class formMultipleChoiceItem_OCL implements OnClickListener {
        String title;
        String[] cs;
        int[] target;
        boolean isSingle;

        public formMultipleChoiceItem_OCL(String title, String[] cs, int[] target, boolean isSingle) {
            this.title = title;
            this.cs = cs;
            this.target = target;
            this.isSingle = isSingle;
        }

        public void onClick(final View v) {
            int btnid = v.getId();
            String answer = "";
            if (v.getTag(R.id.TAG_MC_ID) != null) answer = v.getTag(R.id.TAG_MC_ID).toString();

            boolean[] isSelected = new boolean[this.cs.length];
            Arrays.fill(isSelected, false);

            if (answer != "") {
                List<String> arrAns = Arrays.asList(answer.split("~@~"));

                for (int i = 0, n = this.cs.length; i < n; i++) {
                    isSelected[i] = arrAns.contains(this.cs[i]);
                }
            }

            DialogFragment newFragment = MultipleChoiceDialog.newInstance(this.title, this.cs, this.target, this.isSingle, isSelected, btnid);
            newFragment.show(getFragmentManager(), "mchoicedialog_" + Integer.toString(btnid));
        }
    }


    private class formDateItem_OCL implements OnClickListener {
        String title;

        public formDateItem_OCL(String title) {
            this.title = title;
        }

        public void onClick(final View v) {
            int btnid = v.getId();
            int dd = 0;
            int mm = 0;
            int yy = 0;
            if (v.getTag(R.id.TAG_DATE_DD) != null)
                dd = Integer.valueOf(v.getTag(R.id.TAG_DATE_DD).toString());
            if (v.getTag(R.id.TAG_DATE_MM) != null)
                mm = Integer.valueOf(v.getTag(R.id.TAG_DATE_MM).toString());
            if (v.getTag(R.id.TAG_DATE_YY) != null)
                yy = Integer.valueOf(v.getTag(R.id.TAG_DATE_YY).toString());

            DialogFragment newFragment = DatePickerFragment.newInstance(this.title, btnid, dd, mm, yy);
            newFragment.show(getFragmentManager(), "datepickdialog_" + Integer.toString(btnid));
        }
    }

    private class formMultipleItem_OCL implements OnClickListener {
        String title;
        String type;
        String idQuestion;

        public formMultipleItem_OCL(String title, String type, String idQuestion) {
            this.title = title;
            this.type = type;
            this.idQuestion = idQuestion;
        }

        public void onClick(final View v) {
            int btnid = v.getId();

            String parent = masterMap.get(type);
            String beforeParent = beforeMasterMap.get(type);

            if (!type.equals(masterBefore))
                if (masterBefore != null && !masterBefore.equals(beforeParent)) parent = null;

            Uri uri;
            if (parent == null)
                uri = Uri.parse(MasterContentProvider.CONTENT_URI + "/type/" + type);
            else
                uri = Uri.parse(MasterContentProvider.CONTENT_URI + "/type/" + type + "/parent_id/" + parent);

            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            ArrayList<MasterModel> masters = new ArrayList<>();
            if (cursor != null && cursor.moveToFirst()) {
                for (int i = 0; i < cursor.getCount(); i++) {
                    String _id = cursor.getString(cursor.getColumnIndexOrThrow(MasterTable.COLUMN_ID));
                    String name = cursor.getString(cursor.getColumnIndexOrThrow(MasterTable.COLUMN_NAME));
                    String type = cursor.getString(cursor.getColumnIndexOrThrow(MasterTable.COLUMN_TYPE));
                    String parent_id = cursor.getString(cursor.getColumnIndexOrThrow(MasterTable.COLUMN_PARENT_ID));
                    String next = cursor.getString(cursor.getColumnIndexOrThrow(MasterTable.COLUMN_GOTO));

                    MasterModel master = new MasterModel(_id, name, type, parent_id, next);
                    masters.add(master);

                    cursor.moveToNext();
                }
                cursor.close();
            }

            DialogFragment newFragment = MultipleItemDialog.newInstance(this.title, masters, btnid, idQuestion);
            newFragment.show(getFragmentManager(), "mitemdialog_" + Integer.toString(btnid));
        }
    }

    private final OnClickListener formTtdItem_OCL = new OnClickListener() {
        public void onClick(final View v) {
            lastView = (ImageView) v;
            navData.putString("imgClick", lastView.getTag(R.id.TAG_QUESTION_ID).toString());

            String ttp = "";

            if (lastView.getTag(R.id.TAG_PATH) != null)
                ttp = lastView.getTag(R.id.TAG_PATH).toString();

            startTtd(ttp);

        }
    };

    private final OnClickListener formImgButItem_OCL = new OnClickListener() {
        public void onClick(final View v) {
            lastView = (ImageView) v;
            navData.putString("imgClick", lastView.getTag(R.id.TAG_QUESTION_ID).toString());
            if (lastView.getTag(R.id.TAG_PATH) == null) startCamera();
//            if (lastView.getTag(R.id.TAG_PATH) == null) {
//                File mediaFile = FileHelper.getPrivateImageFile(FormDet.this, idUsr);
//                if (mediaFile != null) {
//                    fileUri = getUriForFile(FormDet.this, "com.virtusee.core.provider.fileprovider", mediaFile);
//                    mCurrentPhotoPath = mediaFile.getAbsolutePath();
//                }
//
//                if (fileUri == null) {
//                    showMessage("Can't create image folder!");
//                    return;
//                }
//
//                FormDet.this.navData.putString("fileUri", fileUri.toString());
//                FormDet.this.navData.putString("mCurrentPhotoPath", mCurrentPhotoPath);
//
//                Camera_.intent(FormDet.this).mPhotoPath(mCurrentPhotoPath).startForResult(CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
//            }
            else startPreview(lastView.getTag(R.id.TAG_PATH).toString());

        }
    };

    private void onRecord(boolean start) {
        if (start) {
            startRecording();
        } else {
            stopRecording();
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

    private void startRecording() {
        File mediaFile = FileHelper.getPrivateAudioFile(this, idUsr);
        if (mediaFile != null) {
            audioUri = getUriForFile(this, "com.virtusee.core.provider.fileprovider", mediaFile);
            mCurrentAudioPath = mediaFile.getAbsolutePath();
        }

        if (audioUri == null) {
            showMessage("Can't create audio folder!");
            return;
        }

        this.navData.putString("audioUri", audioUri.toString());
        this.navData.putString("mCurrentAudioPath", mCurrentAudioPath);

        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setOutputFile(mCurrentAudioPath);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mediaRecorder.prepare();
        } catch (IOException e) {
            Log.e("FormDet", "prepare() failed");
        }

        mediaRecorder.start();
    }

    private void stopRecording() {
        mediaRecorder.stop();
        mediaRecorder.release();
        mediaRecorder = null;
    }

    private final OnClickListener formMicItem_OCL = new OnClickListener() {
        private boolean mStartRecording = true;

        @Override
        public void onClick(View v) {
            ImageView button = findViewById(v.getId());

            onRecord(mStartRecording);
            if (mStartRecording) {
                button.setImageDrawable(ContextCompat.getDrawable(FormDet.this, R.drawable.stop));
            } else {
                button.setImageDrawable(ContextCompat.getDrawable(FormDet.this, R.drawable.mic));
                button.setTag(R.id.TAG_PATH, mCurrentAudioPath);
            }
            mStartRecording = !mStartRecording;
        }
    };

    private final OnClickListener formPlayItem_OCL = new OnClickListener() {
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
                button.setImageDrawable(ContextCompat.getDrawable(FormDet.this, R.drawable.stop));
            } else {
                button.setImageDrawable(ContextCompat.getDrawable(FormDet.this, R.drawable.play));
            }
            mStartPlaying = !mStartPlaying;
        }
    };

    private class formBarcodeItem_OCL implements OnClickListener {
        int editid;

        public formBarcodeItem_OCL(int editid) {
            this.editid = editid;
        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(FormDet.this, BarcodeScanner.class);
            intent.putExtra("editid", editid);
            startActivityForResult(intent, CAPTURE_BARCODE_REQUEST_CODE);
        }
    }

    private void startCamera() {
        File mediaFile = FileHelper.getPrivateImageFile(this, idUsr);
        if (mediaFile != null) {
            fileUri = getUriForFile(this, "com.virtusee.core.provider.fileprovider", mediaFile);
            mCurrentPhotoPath = mediaFile.getAbsolutePath();
        }

        if (fileUri == null) {
            showMessage("Can't create image folder!");
            return;
        }

        this.navData.putString("fileUri", fileUri.toString());
        this.navData.putString("mCurrentPhotoPath", mCurrentPhotoPath);

        // create Intent to take a picture and return control to the calling application
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (intent.resolveActivity(getPackageManager()) == null) {
            showMessage("Can not start camera!");
            return;
        }


        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file name

        final PackageManager packageManager = getPackageManager();

        List<ApplicationInfo> list = packageManager.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);
        for (int n = 0; n < list.size(); n++) {
            if ((list.get(n).flags & ApplicationInfo.FLAG_SYSTEM) == 1) {
                if (list.get(n).loadLabel(packageManager).toString().equalsIgnoreCase("Camera")) {
                    intent.setPackage(list.get(n).packageName);
                    break;
                }
            }
        }

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
            intent.setClipData(ClipData.newRawUri("", fileUri));
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        // start the image capture Intent
        startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
    }

    private void startPreview(String path) {
        Log.e("path", path);
        Photo_.intent(this).imgPath(path).startForResult(CAPTURE_IMAGE_PREVIEW_REQUEST_CODE);
    }


    private void startTtd(String path) {
        Log.e("ssspath", path);
        TTD_.intent(this).imgPath(path).startForResult(CAPTURE_TTD_CODE);
    }


    @OnActivityResult(CAPTURE_TTD_CODE)
    void onTtdResult(int resultCode, Intent data) {
        if (data == null) return;
        if (data.getStringExtra("ttdPath") == null) return;

        try {
            String ttd_path = data.getStringExtra("ttdPath");
            thumbPhoto(lastView, ttd_path, "ttd");
        } catch (Exception e) {
        }
    }

    @OnActivityResult(CAPTURE_IMAGE_PREVIEW_REQUEST_CODE)
    void onPreviewResult(int resultCode, Intent data) {
        if (data == null) return;
        if (data.getStringExtra("result") == null) return;

        try {
            if (data.getStringExtra("result").equals("delete")) {
                Log.e("img", "camera delete");
                showMessage("Deleting photo...");
                if (lastView != null) {
                    detachPhoto();
                } else {
                    detachPhotoDelayed();
                }
            }
        } catch (Exception e) {
        }
    }

    @OnActivityResult(CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE)
    void onCameraResult(int resultCode, Intent data) {
        Log.d("TEST", "resultCode: " + resultCode);
        if (resultCode == RESULT_OK) {
            showMessage("Resizing...");

            // --- start resize image
            compressPhoto();

        } else if (resultCode == RESULT_CANCELED) {
            // User cancelled the image capture
        } else {
            // Image capture failed, advise user
            showMessage("Photo capture failed!");
        }

    }

    @OnActivityResult(CAPTURE_BARCODE_REQUEST_CODE)
    void onBarcodeResult(int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            String barcode = data.getStringExtra("barcode");
            int editid = data.getIntExtra("editid", 0);
            Log.d("FormDet", "onBarcodeREsult: " + barcode + " " + editid);

            EditText formBarcodeView = findViewById(editid);
            formBarcodeView.setText(barcode);
        }
    }

    @UiThread(delay = 2000)
    void detachPhotoDelayed() {
        detachPhoto();
    }

    @UiThread
    void detachPhoto() {
        Log.e("img", "detaching");
        if (lastView != null) {
            Log.e("img", "lastview delete");
            Bitmap dd = ((BitmapDrawable) lastView.getDrawable()).getBitmap();
            //if(dd!=null) dd.recycle();
            lastView.setTag(R.id.TAG_PATH, null);
            lastView.setImageBitmap(null);
            lastView.setBackgroundResource(R.drawable.camera);
        }
    }


    @Background(delay = 1000)
    void compressPhoto() {
        //int thumbWidth=0,thumbHeight=0;
        Bitmap scaledBitmap;

        if (fileUri == null) {
            Log.e("img", "fileuri kosong");
            return;
        }

        File imgFile = new File(mCurrentPhotoPath);

        Log.d("img", "IMGSIZE : " + IMG_TARGET_WIDTH + "x" + IMG_TARGET_HEIGHT);
        try {
            scaledBitmap = ImageHelper.decodeAndScaleBitmap(imgFile, mCurrentPhotoPath, IMG_TARGET_WIDTH, IMG_TARGET_HEIGHT);
        } catch (OutOfMemoryError | IOException e1) {
            scaledBitmap = null;
            e1.printStackTrace();
        }

        if (scaledBitmap != null) {
            try {
                if (imgFile.exists()) {
                    Log.e("fileexist", imgFile.getAbsolutePath());
                    imgFile.delete();
                }

                FileOutputStream out = new FileOutputStream(imgFile);
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, IMG_TARGET_COMPRESS, out);
                out.flush();
                out.close();
	/*
				if(scaledBitmap.getWidth() > scaledBitmap.getHeight()){
					thumbWidth = 80;
					thumbHeight = 60;
				} else {
					thumbWidth = 60;
					thumbHeight = 80;
				}
	*/
                //scaledBitmap.recycle();

            } catch (Exception e) {
                e.printStackTrace();
            } catch (OutOfMemoryError e1) {
            }
        }

        Log.e("img", "fileuri tiddak kosong");
        thumbPhoto(lastView, mCurrentPhotoPath, "photo");

    }

    void onDateOkay(int btnid, int dd, int mm, int yy) {
        EditText myButton = (EditText) findViewById(btnid);
        String hasil = String.format("%02d", dd) + '-' + String.format("%02d", mm + 1) + '-' + String.format("%04d", yy);

        myButton.setTag(R.id.TAG_DATE_ID, hasil);
        myButton.setTag(R.id.TAG_DATE_DD, dd);
        myButton.setTag(R.id.TAG_DATE_MM, mm + 1);
        myButton.setTag(R.id.TAG_DATE_YY, yy);

        GregorianCalendar dateConverted = new GregorianCalendar(yy, mm, dd);
        DateFormat fmt = DateFormat.getDateInstance();
        myButton.setText(fmt.format(dateConverted.getTime()));
        if (!hasil.isEmpty()) myButton.setError(null);
    }

    void onSingleChoiceOkay(int btnid, String[] cs, int[] target, int mSelectedItem) {
        EditText myButton = (EditText) findViewById(btnid);
        String hasil;
        int act;
        try {
            hasil = cs[mSelectedItem];
            act = target[mSelectedItem];
        } catch (ArrayIndexOutOfBoundsException e) {
            return;
        }


        String btnText = hasil;
        String btnTag = hasil;

        int prevact = 0;
        try {
            prevact = Integer.parseInt(myButton.getTag(R.id.TAG_MC_GROUP).toString());
            Log.e("prevact", myButton.getTag(R.id.TAG_MC_GROUP).toString());
        } catch (NullPointerException e) {
            prevact = 0;
        } catch (NumberFormatException e) {
            prevact = 0;
        }

        myButton.setText(btnText);
        myButton.setTag(R.id.TAG_MC_ID, btnTag);
        if (!btnTag.isEmpty()) myButton.setError(null);
        disp_group(act, prevact);
        myButton.setTag(R.id.TAG_MC_GROUP, act);
    }

    void onMultipleChoiceOkay(int btnid, String[] cs, ArrayList<Integer> mSelectedItems) {
        EditText myButton = findViewById(btnid);
        String[] hasil = new String[mSelectedItems.size()];

        for (int i = 0; i < mSelectedItems.size(); i++) {
            hasil[i] = cs[mSelectedItems.get(i)];
        }

        String btnText = FileHelper.implode(hasil, ",");
        String btnTag = FileHelper.implode(hasil, "~@~");

        myButton.setText(btnText);
        myButton.setTag(R.id.TAG_MC_ID, btnTag);
        if (!btnTag.isEmpty()) myButton.setError(null);
    }

    void onMultipleItemOkay(int btnid, MasterModel mSelectedItems, String idQuestion) {
        EditText myButton = findViewById(btnid);
        idMasterMap.put(idQuestion, mSelectedItems._id);
        typeMasterMap.put(idQuestion, mSelectedItems.type);

        if (!mSelectedItems.next.equals("")) {
            masterMap.put(mSelectedItems.next, mSelectedItems._id);
            beforeMasterMap.put(mSelectedItems.next, mSelectedItems.type);
            masterBefore = mSelectedItems.type;
            try {
                EditText childButton = findViewById(++btnid);
                childButton.setText("");
            } catch (ClassCastException e) {
                e.printStackTrace();
            }
        } else {
            masterMap = new HashMap<>();
            beforeMasterMap = new HashMap<>();
            masterBefore = null;
        }

        myButton.setText(mSelectedItems.name);
        myButton.setTag(R.id.TAG_MC_ID, mSelectedItems.name);
    }

    @UiThread
    void thumbPhoto(ImageView v, String path, String type) {

        if (v == null) {
            showMessage("Image View Failed to capture image! Please try again.");
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
//            File imgFile = new File(path);
//            try {
//                Bitmap scaledBitmap = ImageHelper.decodeAndScaleBitmap(imgFile, path, thumbWidth, thumbHeight);
//                v.setImageBitmap(scaledBitmap);
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
            showMessage("Failed to capture image! Please try again.");
            return;
        }

        v.setImageBitmap(thumbBitmap);
        v.setTag(R.id.TAG_PATH, path);
        v.setColorFilter(null);

    }

    void bundleForm() {
        if (!answerMap.isEmpty()) answerMap.clear();

        int childcount = formDetContainer.getChildCount();
        for (int i = 0; i < childcount; i++) {
            FormGroupView v = (FormGroupView) formDetContainer.getChildAt(i);
            if (!v.isInputable()) continue;
            answerMap.put(v.getIdQuestion(), v.getVal());
        }

        this.navData.putSerializable("bundleAnswer", answerMap);

        lastLocation = gpsHelper.getLastLocation();
        this.navData.putParcelable("lastLocation", lastLocation);
        this.navData.putString("entertime", entertime);

        Log.e("bundleform", "answer bundled");
    }

    @SuppressLint("SimpleDateFormat")
    void saveForm() {
        isValid = true;

        List<AnswerContentModel> answerContent = new ArrayList<>();
        String answerContentJson = "";
        List<String> photos = new ArrayList<>();
        List<String> audio = new ArrayList<>();
        String longitude = "", latitude = "", gpsTime = "", gpsAccuracy = "";
        boolean formValidate;

        int childcount = formDetContainer.getChildCount();
        for (int i = 0; i < childcount; i++) {
            FormGroupView v = (FormGroupView) formDetContainer.getChildAt(i);
            if (!v.isInputable()) continue;

            answerContent.add(new AnswerContentModel(v.getIdQuestion(), v.getVal(), v.getType(), idMasterMap.get(v.getIdQuestion()), typeMasterMap.get(v.getIdQuestion())));

            if (v.isImage() && !v.getVal().equals("")) photos.add(v.getVal());
            if (v.isAudio() && !v.getVal().equals("")) {
                audio.add(v.getVal());
                Log.d("FormDet", "saveForm: audio =  " + v.getVal());
            }

            formValidate = v.validate();
            if (!formValidate) isValid = false;
        }

        if (!isValid) return;

        if (answerContent != null) {
            Gson gson = new Gson();
            answerContentJson = gson.toJson(answerContent);
        }

        String whenupdate = dateHelper.getCurrentTimestamp();
        Log.e("answer", whenupdate);

        currentLocation = gpsHelper.getLastLocation();
        if (currentLocation == null && lastLocation != null) currentLocation = lastLocation;

        ArrayList<String> unileverDomains = new ArrayList<>();
        unileverDomains.add("ulipc");
        unileverDomains.add("ulib35a");
        unileverDomains.add("uliam");
        unileverDomains.add("quam3ce");
        unileverDomains.add("uliprosespc");
        unileverDomains.add("ulipw");
        unileverDomains.add("ampw");
        unileverDomains.add("unileverpc");
        unileverDomains.add("unileverpw");

        if (currentLocation != null) {
            longitude = Double.toString(currentLocation.getLongitude());
            latitude = Double.toString(currentLocation.getLatitude());
            gpsTime = Long.toString(currentLocation.getTime());
            gpsAccuracy = Float.toString(currentLocation.getAccuracy());

            myPrefs.lastLocationForm().put(longitude + "," + latitude);
        } else {
            String domain = myPrefs.domain().get();
            if (!unileverDomains.contains(domain)) {
                showAlertLocationEmpty();
                return;
            }
        }

        if (currentLocation.isFromMockProvider()) {
            showAlertLocationEmpty();
            return;
        }

        String project = authHelper.getDomain();
        //String idusr = authHelper.getUserid();

        ContentValues values = new ContentValues();
        values.put(AnswerTable.COLUMN_ID_FORM, idForm);
        values.put(AnswerTable.COLUMN_ID_STORE, idStore);
        values.put(AnswerTable.COLUMN_CONTENT, answerContentJson);
        values.put(AnswerTable.COLUMN_WHEN, whenupdate);
        values.put(AnswerTable.COLUMN_LAST_SYNC, 0);
        values.put(AnswerTable.COLUMN_LONG, longitude);
        values.put(AnswerTable.COLUMN_LAT, latitude);
        values.put(AnswerTable.COLUMN_GPS_TIME, gpsTime);
        values.put(AnswerTable.COLUMN_GPS_ACCURACY, gpsAccuracy);
        values.put(AnswerTable.COLUMN_PROJECT, project);
        values.put(AnswerTable.COLUMN_USER, idUsr);
        values.put(AnswerTable.COLUMN_ENTER, entertime);
        values.put(AnswerTable.COLUMN_APP_VERSION, BuildConfig.VERSION_NAME);
        values.put(AnswerTable.COLUMN_DEVICE_NAME, getDeviceName());

        if (answerId == null) {
            Uri insuri = getContentResolver().insert(AnswerContentProvider.CONTENT_URI, values);
            answerId = insuri.getLastPathSegment();
        } else {
            getContentResolver().update(Uri.parse(AnswerContentProvider.CONTENT_URI + "/" + answerId), values, null, null);
            getContentResolver().delete(Uri.parse(PhotoContentProvider.CONTENT_URI + "/answer/" + answerId), null, null);
            getContentResolver().delete(Uri.parse(AudioContentProvider.CONTENT_URI + "/answer/" + answerId), null, null);
        }


        if (photos != null) {
            saveImageLog(answerId, photos);
        }
        if (audio != null) {
            saveAudioLog(answerId, audio);
        }

        checkCin();
        if (mandatory < 2) absenHelper.CheckMandatory(idStore);

        SyncServ.enqueuePostAll(this, authHelper.getUserid(), authHelper.getFormattedUsername(), authHelper.getPassword());
    }

    public void showAlertLocationEmpty() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Terdeteksi Lokasi GPS Error")
                .setMessage(getString(R.string.warn_empty_location))
                .setPositiveButton("Ok", (dialogInterface, i) -> dialogInterface.dismiss())
                .show();
        isValid = false;
    }

    public String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.toLowerCase().startsWith(manufacturer.toLowerCase())) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }


    private String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

    private void checkCin() {
        String oStore;
        int placeStatus;

        currentLocation = gpsHelper.getLastLocation();
        if (currentLocation == null && lastLocation != null) currentLocation = lastLocation;

        StoreAbsenModel storeAbsenModel = absenHelper.GetStatus();
        int wdayStatus = absenHelper.GetStatusWday();

        if (storeAbsenModel.idStore.equals("")) {
            // -- belum ada data cin --//
            oStore = "";
            placeStatus = 0;
        } else if (storeAbsenModel.idStore.equals(idStore)) {
            // -- ada data cin di toko ini --
            oStore = "";
            placeStatus = storeAbsenModel.status;
        } else {
            if (storeAbsenModel.status == 1) oStore = storeAbsenModel.idStore;
            else oStore = "";
            placeStatus = 0;
        }

        if (placeStatus != 1 && !oStore.equals("")) {
            // -- jika ada toko lain belum cout, auto cout di toko lain --//
            absenHelper.SaveAbsen(2, oStore, currentLocation);
        }

        if (placeStatus != 1) {
            // -- auto cin place
            absenHelper.SaveAbsen(1, idStore, currentLocation);
        }

        if (wdayStatus != 1) {
            // -- auto wday start if haven't
            absenHelper.SaveAbsen(1, "wday", currentLocation);
            setWdayLimit();
        }
    }

    private void setWdayLimit() {

        Intent notificationIntent = new Intent(this, WdayReceiver.class);
        PendingIntent pendingIntent;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            pendingIntent = PendingIntent.getBroadcast(this, ALARM_ID, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        } else {
            pendingIntent = PendingIntent.getBroadcast(this, ALARM_ID, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR_OF_DAY, 18);

        AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pendingIntent);
    }

    public void saveImageLog(String answerId, List<String> photos) {
        if (photos == null) return;

        String whenupdate = dateHelper.getCurrentTimestamp();
        String sql = "INSERT INTO photo(id_answer,img,csum,whenupdate,lastsync) VALUES (?,?,?,?,?);";
        SQLiteDatabase db = VSDbHelper.getInstance(getApplicationContext()).getWritableDatabase();
        SQLiteStatement statement = db.compileStatement(sql);
        db.beginTransaction();
        Log.e("imagerest", "start images");

        for (String photo : photos) {

            File p = new File(photo);
            String csum = "";
            try {
                FileInputStream fileInputStream = new FileInputStream(p);
                CheckedInputStream chksum = new CheckedInputStream(fileInputStream, new Adler32());
                while (chksum.read() != -1) {
                    // Read file in completely
                }
                csum = String.valueOf(chksum.getChecksum().getValue());

            } catch (FileNotFoundException e) {

            } catch (IOException e) {
                e.printStackTrace();
            }
            statement.clearBindings();
            statement.bindString(1, answerId);
            statement.bindString(2, photo);
            statement.bindString(3, csum);
            statement.bindString(4, whenupdate);
            statement.bindString(5, "0");
            Log.e("imagerest", photo + "--" + csum);

            statement.execute();
        }

        db.setTransactionSuccessful();
        db.endTransaction();
    }

    public void saveAudioLog(String answerId, List<String> audios) {
        if (audios == null) return;

        String whenupdate = dateHelper.getCurrentTimestamp();
        String sql = "INSERT INTO audio(id_answer,audio,csum,whenupdate,lastsync) VALUES (?,?,?,?,?);";
        SQLiteDatabase db = VSDbHelper.getInstance(getApplicationContext()).getWritableDatabase();
        SQLiteStatement statement = db.compileStatement(sql);
        db.beginTransaction();
        Log.e("audiorest", "start audio");

        for (String audio : audios) {

            File p = new File(audio);
            String csum = "";
            try {
                FileInputStream fileInputStream = new FileInputStream(p);
                CheckedInputStream chksum = new CheckedInputStream(fileInputStream, new Adler32());
                while (chksum.read() != -1) {
                    // Read file in completely
                }
                csum = String.valueOf(chksum.getChecksum().getValue());

            } catch (FileNotFoundException e) {

            } catch (IOException e) {
                e.printStackTrace();
            }
            statement.clearBindings();
            statement.bindString(1, answerId);
            statement.bindString(2, audio);
            statement.bindString(3, csum);
            statement.bindString(4, whenupdate);
            statement.bindString(5, "0");
            Log.e("audiorest", audio + "--" + csum);

            statement.execute();
        }

        db.setTransactionSuccessful();
        db.endTransaction();
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

        if (mHandler != null) {
            mHandler.removeCallbacks(mRunnable);
        }

        if (!this.isSaveClicked) {
            // delete image from file //
        }


    }

    @Override
    public void onPause() {
//    	gpsHelper.disconnect();

        super.onPause();
        Log.e("checkpause", "Pause");
        lastLocation = gpsHelper.getLastLocation();

        if (fileUri != null) Log.e("checkuri", "Pause: " + fileUri.toString());
    }

    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first
        if (this.navData.getString("fileUri") != null)
            fileUri = Uri.parse(this.navData.getString("fileUri"));
        if (this.navData.getString("audioUri") != null)
            audioUri = Uri.parse(this.navData.getString("audioUri"));

        if (this.navData.getString("mCurrentPhotoPath") != null)
            mCurrentPhotoPath = this.navData.getString("mCurrentPhotoPath");
        if (this.navData.getString("mCurrentAudioPath") != null)
            mCurrentAudioPath = this.navData.getString("mCurrentAudioPath");

        this.doubleBackToExitPressedOnce = false;

        PermissionHelper.request_multi(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.RECORD_AUDIO}, PermissionHelper.REQUEST_CHECK_PERM);

        gpsHelper.checkGPSOn();

        if (fileUri == null) {
            File mediaFile = FileHelper.getPrivateImageFile(this, idUsr);
            if (mediaFile != null) {
                fileUri = getUriForFile(this, "com.virtusee.core.provider.fileprovider", mediaFile);
                mCurrentPhotoPath = mediaFile.getAbsolutePath();
                Log.e("checkurigen", "New file created: " + fileUri.toString());
            }
        }
        if (audioUri == null) {
            File mediaFile = FileHelper.getPrivateAudioFile(this, idUsr);
            if (mediaFile != null) {
                fileUri = getUriForFile(this, "com.virtusee.core.provider.fileprovider", mediaFile);
                mCurrentAudioPath = mediaFile.getAbsolutePath();
                Log.e("checkurigen", "New file created: " + fileUri.toString());
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        this.showMessage("Tap Back again to exit without saving");

//	    if(mHandler==null) mHandler = new Handler();
        mHandler.postDelayed(mRunnable, 2000);
    }

    @Override
    protected void onStart() {
        super.onStart();
        gpsHelper.connect();

        if (this.navData == null) {
            Log.e("oncreate", "navdata created");
            this.navData = getIntent().getExtras();
            //imgPath = FileHelper.getImageFolder();
            //this.navData.putString("imgPath", imgPath);
        }
    }

    @Override
    protected void onStop() {
        gpsHelper.disconnect();
        super.onStop();
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        bundleForm();
        outState.putAll(this.navData);
//        outState.putString("entertime",entertime);
        Log.e("savedinstance", "dfdfdf");
        super.onSaveInstanceState(outState);
    }

    @SuppressWarnings("unchecked")
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        // Always call the superclass so it can restore the view hierarchy
        super.onRestoreInstanceState(savedInstanceState);

        this.navData = (Bundle) savedInstanceState.clone();
        //imgPath = this.navData.getString("imgPath");
        if (this.navData.getString("fileUri") != null)
            fileUri = Uri.parse(this.navData.getString("fileUri"));
        if (this.navData.getString("audioUri") != null)
            audioUri = Uri.parse(this.navData.getString("audioUri"));
        if (this.navData.getSerializable("bundleAnswer") != null) {
            answerMap = (HashMap<String, String>) this.navData.getSerializable("bundleAnswer");
            Log.e("oncreate", "answermap loaded");
        }
        lastLocation = this.navData.getParcelable("lastLocation");
        if (lastLocation != null) {
            gpsHelper.setStartLoc(lastLocation);
            onGpsSet(lastLocation);
        }

        if (this.navData.getString("entertime") != null)
            entertime = this.navData.getString("entertime");
        if (this.navData.getString("mCurrentPhotoPath") != null)
            mCurrentPhotoPath = this.navData.getString("mCurrentPhotoPath");
        if (this.navData.getString("mCurrentAudioPath") != null)
            mCurrentAudioPath = this.navData.getString("mCurrentAudioPath");
    }

    @UiThread
    public void showMessage(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    public static class MultipleChoiceDialog extends DialogFragment {

        public static MultipleChoiceDialog newInstance(String title, String[] cs, int[] target, boolean isSingle, boolean[] isSelected, int btnid) {
            MultipleChoiceDialog frag = new MultipleChoiceDialog();
            Bundle args = new Bundle();
            args.putString("title", title);
            args.putStringArray("choice", cs);
            args.putIntArray("target", target);
            args.putBoolean("isSingle", isSingle);
            args.putBooleanArray("selecteditem", isSelected);
            args.putInt("btn", btnid);
            frag.setArguments(args);
            return frag;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            // Set the dialog title
            final String title = getArguments().getString("title");
            final String[] cs = getArguments().getStringArray("choice");
            final int[] target = getArguments().getIntArray("target");
            final int btnid = getArguments().getInt("btn");
            boolean[] isSelectedArray = getArguments().getBooleanArray("selecteditem");
            boolean isSingle = getArguments().getBoolean("isSingle");

            // Specify the list array, the items to be selected by default (null for none),
            // and the listener through which to receive callbacks when items are selected

            if (isSingle) {
                return _build_single(title, cs, target, isSelectedArray, btnid);
            } else {
                return _build_multiple(title, cs, isSelectedArray, btnid);
            }
        }

        private AlertDialog _build_multiple(String title, String[] cs, boolean[] isSelectedArray, int btnid) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            final ArrayList<Integer> mSelectedItems = new ArrayList<Integer>();  // Where we track the selected items
            final int _btnid = btnid;
            final String[] _cs = cs;

            builder.setTitle(title);

            for (int i = 0, n = isSelectedArray.length; i < n; i++) {
                if (isSelectedArray[i]) mSelectedItems.add(Integer.valueOf(i));
            }

            builder.setMultiChoiceItems(cs, isSelectedArray,
                    new DialogInterface.OnMultiChoiceClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which,
                                            boolean isChecked) {
                            if (isChecked) {
                                mSelectedItems.add(which);
                            } else if (mSelectedItems.contains(which)) {
                                mSelectedItems.remove(Integer.valueOf(which));
                            }
                        }
                    });

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    ((FormDet) getActivity()).onMultipleChoiceOkay(_btnid, _cs, mSelectedItems);
                }
            });

            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            });

            return builder.create();
        }


        private AlertDialog _build_single(String title, String[] cs, int[] target, boolean[] isSelectedArray, int btnid) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            final int _btnid = btnid;
            final String[] _cs = cs;
            final int[] _target = target;
            int selected = -1;

            builder.setTitle(title);

            for (int i = 0, n = isSelectedArray.length; i < n; i++) {
                if (isSelectedArray[i]) {
                    selected = i;
                }
            }

            final int mSelectedItem = selected;

            builder.setSingleChoiceItems(cs, mSelectedItem, null);

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    ListView lw = ((AlertDialog) dialog).getListView();
                    ((FormDet) getActivity()).onSingleChoiceOkay(_btnid, _cs, _target, lw.getCheckedItemPosition());
                }
            });

            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            });

            return builder.create();
        }
    }


    public static class MultipleItemDialog extends DialogFragment {

        public static MultipleItemDialog newInstance(String title, ArrayList<MasterModel> masters, int btnid, String idQuestion) {
            MultipleItemDialog frag = new MultipleItemDialog();
            Bundle args = new Bundle();
            args.putString("title", title);
            args.putParcelableArrayList("masters", masters);
            args.putInt("btn", btnid);
            args.putString("idQuestion", idQuestion);
            frag.setArguments(args);
            return frag;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            // Set the dialog title
            final String title = getArguments().getString("title");
            final ArrayList<MasterModel> masters = getArguments().getParcelableArrayList("masters");
            final int btnid = getArguments().getInt("btn");
            final String idQuestion = getArguments().getString("idQuestion");

            // Specify the list array, the items to be selected by default (null for none),
            // and the listener through which to receive callbacks when items are selected

            return _build(title, masters, btnid, idQuestion);
        }


        private AlertDialog _build(String title, final ArrayList<MasterModel> masters, int btnid, final String idQuestion) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            final int _btnid = btnid;

            builder.setTitle(title);

            final String[] cs = new String[masters.size()];
            for (int i = 0; i < masters.size(); i++) {
                cs[i] = masters.get(i).name;
            }

            final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.select_dialog_item, cs);

            View view = LayoutInflater.from(getActivity()).inflate(R.layout.custom_search_choice, null);
            builder.setView(view);

            EditText searchText = view.findViewById(R.id.search);
            searchText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    arrayAdapter.getFilter().filter(editable.toString());
                }
            });

            ListView listView = view.findViewById(R.id.list_item);
            listView.setAdapter(arrayAdapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                    String selected = (String) adapterView.getItemAtPosition(position);
                    MasterModel master = new MasterModel();
                    for (int i = 0; i < masters.size(); i++) {
                        if (selected.equalsIgnoreCase(masters.get(i).name)) master = masters.get(i);
                    }
                    ((FormDet) getActivity()).onMultipleItemOkay(_btnid, master, idQuestion);
                    getDialog().dismiss();
                }
            });

//            builder.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    ((FormDet) getActivity()).onMultipleItemOkay(_btnid, masters.get(which), idQuestion);
//                    dialog.dismiss();
//                }
//            });

            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            });

            return builder.create();
        }
    }

    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {
        int btnid;

        public static DatePickerFragment newInstance(String title, int btnid, int dd, int mm, int yy) {
            DatePickerFragment frag = new DatePickerFragment();
            Bundle args = new Bundle();
            args.putString("title", title);
            args.putInt("btn", btnid);
            args.putInt("dd", dd);
            args.putInt("mm", mm);
            args.putInt("yy", yy);
            frag.setArguments(args);
            return frag;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();

            this.btnid = getArguments().getInt("btn");
            int year = getArguments().getInt("yy");
            int month = getArguments().getInt("mm") - 1;
            int day = getArguments().getInt("dd");

            if (year == 0) year = c.get(Calendar.YEAR);
            if (month == -1) month = c.get(Calendar.MONTH);
            if (day == 0) day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            DatePickerDialog dpd = new DatePickerDialog(getActivity(), this, year, month, day);
            dpd.setTitle(getArguments().getString("title"));
            return dpd;
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            // Do something with the date chosen by the user
            ((FormDet) getActivity()).onDateOkay(this.btnid, day, month, year);

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        onBackPressed();
        return true;
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
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionHelper.result(this, requestCode, permissions, grantResults);
    }

}