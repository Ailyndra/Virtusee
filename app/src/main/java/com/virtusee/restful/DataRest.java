package com.virtusee.restful;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteStatement;
import android.os.AsyncTask;
import android.util.Log;

import com.virtusee.db.VSDbHelper;
import com.virtusee.helper.AuthHelper;
import com.virtusee.helper.ConnHelper;
import com.virtusee.helper.FileHelper;
import com.virtusee.helper.NotifHelper;
import com.virtusee.helper.PrefHelper_;
import com.virtusee.model.DataModel;
import com.virtusee.model.DeldataModel;
import com.virtusee.model.FormModel;
import com.virtusee.model.PrjModel;
import com.virtusee.model.ScheduleModel;
import com.virtusee.model.StoreModel;
import com.virtusee.retrofit.VSRetrofitAdapter;
import com.virtusee.retrofit.VSRetrofitApi;
import com.virtusee.retrofit.VSRetrofitListener;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


@EBean
public class DataRest implements Callback<DataModel> {

	@RootContext
	Context context;


	@Bean
	NotifHelper notifHelper;

	@Bean
	AuthHelper authHelper;

	@Pref
	PrefHelper_ myPrefs;

	private VSRetrofitListener vsRetrofitListener;
    private SQLiteDatabase db;

	public void init(VSRetrofitListener lst){
		vsRetrofitListener = lst;
	}

	public void getData(Context context) {
		if (!ConnHelper.hasConnection(context)) {
			if(vsRetrofitListener != null) vsRetrofitListener.onFailure("noconn");
			return;
		}

        Date d = new Date();
		String lastSync = null;

		if (myPrefs.lastDownload().exists()) lastSync = myPrefs.lastDownload().get();

		Log.e("isi dl", "start dl");

        VSRetrofitApi.Data dataApi = VSRetrofitAdapter.init(VSRetrofitApi.Data .class,authHelper.getFormattedUsername(),authHelper.getPassword());

        Call<DataModel> call;

		if (lastSync == null) {
            Log.e("lastsync", "lastsyncnull");
			call = dataApi.getData();
		} else {
            Log.e("lastsync", lastSync);
			call = dataApi.getDataPart(lastSync);
		}

        call.enqueue(this);

        Log.e("isi dl", "end dl");

	}


	private void _del_data(DeldataModel[] deldata){
        db.beginTransaction();
        try {

            for (DeldataModel sm : deldata) {
                if (sm.tb.equals("store")) {
                    db.delete("store","_id=?",new String[]{sm.key});
                    db.delete("store_tag","id_store = ?",new String[]{sm.key});
                } else if (sm.tb.equals("form")) {
                    db.delete("form","_id=?",new String[]{sm.key});
                    db.delete("form_tag","id_form = ?",new String[]{sm.key});
                    db.delete("form_tag_own","id_form = ?",new String[]{sm.key});
                }
                Log.e("isi form", "delete");
            }
            db.setTransactionSuccessful();
        } catch (SQLiteException e){
            FileHelper.setLog(context,e.getMessage());
        } finally {
            db.endTransaction();
        }
    }

    private void _set_prj(PrjModel prj) {
        Log.d("DataRest", "_set_prj: " + prj.toString());
        myPrefs.proximity().put(prj.prj_proximity);
        myPrefs.radius().put(prj.prj_radius);
        myPrefs.reverse_proximity().put(prj.prj_reverse_proximity);
        myPrefs.total_checkout().put(prj.prj_total_checkout);
    }

	private void _set_places(StoreModel[] stores){
        String sql,sqltag;
        SQLiteStatement statement,statementtag;
        String project = authHelper.getDomain();

        db.beginTransaction();

        try {

            sql = "INSERT OR REPLACE INTO store(_id, store_id, store_code, store_name, project,day,week,longitude,latitude,attr,urut,short) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
            statement = db.compileStatement(sql);

            sqltag = "INSERT INTO store_tag(id_store, tag) VALUES (?,?)";
            statementtag = db.compileStatement(sqltag);

            Log.e("isi places", "start places");

            String day, week, attr, longitude, latitude;

            for (StoreModel sm : stores) {

                if (sm.day == null || sm.day.equals("")) day = "1111111";
                else day = sm.day;

                if (sm.week == null || sm.week.equals("")) week = "11";
                else week = sm.week;

                if (sm.attr == null) attr = "";
                else attr = sm.attr;

                if (sm.longitude == null) longitude = "";
                else longitude = sm.longitude;

                if (sm.latitude == null) latitude = "";
                else latitude = sm.latitude;

                statement.clearBindings();
                statement.bindString(1, sm._id);
                statement.bindString(2, sm._id);
                statement.bindString(3, sm.code);
                statement.bindString(4, sm.name);
                statement.bindString(5, project);
                statement.bindString(6, day);
                statement.bindString(7, week);
                statement.bindString(8, longitude);
                statement.bindString(9, latitude);
                statement.bindString(10, attr);
                statement.bindLong(11, 1);
                statement.bindString(12, sm.place_code);

                statement.execute();

                // -- insert tag --//
                db.delete("store_tag","id_store = ?",new String[]{sm._id});

                if(sm.tag!=null){
                    for (String tt : sm.tag) {
                        statementtag.clearBindings();
                        statementtag.bindString(1, sm._id);
                        statementtag.bindString(2, tt);

                        statementtag.execute();
                    }
                }
            }
            db.setTransactionSuccessful();
        } catch (SQLiteException e){
            FileHelper.setLog(context,e.getMessage());
        } finally {
            db.endTransaction();
        }

        Log.e("isi places", "endplaces");
    }

    private void _set_form(FormModel[] forms){
        String sql,sqltag;
        SQLiteStatement statement,statementtag,statementtag2;
        String project = authHelper.getDomain();

        db.beginTransaction();
        try {

            sql = "INSERT OR REPLACE INTO form(_id, form_id, title, desc, content, project, urut, sticky, mandatory) VALUES (?,?,?,?,?,?,?,?,?)";
            statement = db.compileStatement(sql);

            sqltag = "INSERT INTO form_tag(id_form, tag) VALUES (?,?)";
            statementtag = db.compileStatement(sqltag);

            sqltag = "INSERT INTO form_tag_own(id_form, tag) VALUES (?,?)";
            statementtag2 = db.compileStatement(sqltag);

            Log.e("isi form", "start form");

            for (FormModel sm : forms) {
                statement.clearBindings();
                statement.bindString(1, sm._id);
                statement.bindString(2, sm._id);
                statement.bindString(3, sm.title);
                statement.bindString(4, sm.desc);
                statement.bindString(5, sm.content);
                statement.bindString(6, project);
                statement.bindLong(7, sm.urut);
                statement.bindLong(8, sm.sticky);
                statement.bindLong(9, sm.mandatory);
                statement.execute();


                // -- insert tag --//
                db.delete("form_tag","id_form = ?",new String[]{sm._id});

                if(sm.tag!=null){
                    for (String tt : sm.tag) {
                        statementtag.clearBindings();
                        statementtag.bindString(1, sm._id);
                        statementtag.bindString(2, tt);

                        statementtag.execute();
                    }
                }

                // -- insert form tag --//
                db.delete("form_tag_own","id_form = ?",new String[]{sm._id});

                if(sm.form_tag!=null){
                    for (String tt : sm.form_tag) {
                        statementtag2.clearBindings();
                        statementtag2.bindString(1, sm._id);
                        statementtag2.bindString(2, tt);

                        statementtag2.execute();
                    }
                }
            }


            db.setTransactionSuccessful();
        } catch (SQLiteException e){
            FileHelper.setLog(context,e.getMessage());
        } finally {
            db.endTransaction();
        }

        Log.e("isi form", "end form");
    }

    private void _set_schedule(ScheduleModel[] schedules) {
        String sql;
        SQLiteStatement statement;


        db.beginTransaction();

        try {
            sql = "update store set day = ? where _id = ?";
            statement = db.compileStatement(sql);

            for (ScheduleModel sm : schedules) {
                statement.clearBindings();
                statement.bindString(1, sm.store);
                statement.bindString(2, sm.visits);

                statement.executeUpdateDelete();
            }

            db.setTransactionSuccessful();
        }  catch (Exception e){
            FileHelper.setLog(context,e.getMessage());
        } finally {
            db.endTransaction();
        }
        Log.e("isi places", "update visits");
    }


    @Override
    public void onResponse(Call<DataModel> call, Response<DataModel> response) {
        if(response.isSuccessful()) {
            if(db==null) db = VSDbHelper.getInstance(context).getWritableDatabase();
            DataModel dataModel = response.body();

            AsyncTask.execute(() -> {
                if (dataModel.deldata != null) _del_data(dataModel.deldata);
                if (dataModel.store != null) _set_places(dataModel.store);
                if (dataModel.form != null) _set_form(dataModel.form);
                if (dataModel.schedule != null) _set_schedule(dataModel.schedule);
                if (dataModel.prj != null) _set_prj(dataModel.prj);
            });

            Date d = new Date();
            long now = d.getTime() / 1000;
            myPrefs.lastDownload().put(Long.toString(now));

            if(vsRetrofitListener != null) vsRetrofitListener.onSuccess("data");
        } else {
            if(vsRetrofitListener!=null) vsRetrofitListener.onFailure("data");
        }

    }

    @Override
    public void onFailure(Call<DataModel> call, Throwable t) {
        Log.e("error", t.getMessage());
        FileHelper.setLog(context,t.getMessage());

        if(vsRetrofitListener!=null) vsRetrofitListener.onFailure("data");
    }


}