package com.virtusee.restful;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.AsyncTask;
import android.util.Log;

import com.virtusee.db.VSDbHelper;
import com.virtusee.helper.AuthHelper;
import com.virtusee.helper.ConnHelper;
import com.virtusee.helper.FileHelper;
import com.virtusee.helper.PrefHelper_;
import com.virtusee.model.DataModel;
import com.virtusee.model.MasterModel;
import com.virtusee.retrofit.VSRetrofitAdapter;
import com.virtusee.retrofit.VSRetrofitApi;
import com.virtusee.retrofit.VSRetrofitListener;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@EBean
public class MasterRest implements Callback<DataModel> {
    @RootContext
    Context context;

    @Bean
    AuthHelper authHelper;

    @Pref
    PrefHelper_ myPrefs;

    private VSRetrofitListener vsRetrofitListener;
    private SQLiteDatabase db;

    public void init(VSRetrofitListener lst){
        vsRetrofitListener = lst;
    }

    public void getMaster(Context context) {
        if (!ConnHelper.hasConnection(context)) {
            if(vsRetrofitListener != null) vsRetrofitListener.onFailure("noconn");
            return;
        }

        Log.e("isi dl", "start master");

        VSRetrofitApi.Data dataApi = VSRetrofitAdapter.init(VSRetrofitApi.Data .class,authHelper.getFormattedUsername(),authHelper.getPassword());

        Call<DataModel> master;

        SQLiteDatabase database = VSDbHelper.getInstance(context).getReadableDatabase();
        Cursor c = database.query("master", null, null, null, null, null, null);
        if (c.getCount() == 0) {
            Log.e("lastsync", "lastsyncnull");
            master = dataApi.getMaster();
            master.enqueue(this);
        } else {
            if(vsRetrofitListener != null) vsRetrofitListener.onSuccess("master");
        }
        c.close();
        database.close();

        Log.e("isi dl", "end master");

    }

    public void getAbsensi(Context context) {
        if (!ConnHelper.hasConnection(context)) {
            if(vsRetrofitListener != null) vsRetrofitListener.onFailure("noconn");
            return;
        }

        Log.e("isi dl", "start absensi");

        VSRetrofitApi.Data dataApi = VSRetrofitAdapter.init(VSRetrofitApi.Data .class,authHelper.getFormattedUsername(),authHelper.getPassword());

        Call<DataModel> master;

        master = dataApi.getAbsensi();
        master.enqueue(this);

        Log.e("isi dl", "end master");
    }

    private void _set_master(MasterModel[] masters) {
        String sql;
        SQLiteStatement statement;
        String project = authHelper.getDomain();

        db.beginTransaction();

        try {
            sql = "INSERT OR REPLACE INTO master(_id,name,type,parent_id,goto,project) VALUES (?,?,?,?,?,?)";
            statement = db.compileStatement(sql);

            for(MasterModel ms : masters) {
                String parent_id;
                if(ms.parent_id == null) parent_id = "";
                else parent_id = ms.parent_id;

                String next;
                if(ms.next == null) next = "";
                else next = ms.next;

                statement.clearBindings();
                statement.bindString(1, ms._id);
                statement.bindString(2, ms.name);
                statement.bindString(3, ms.type);
                statement.bindString(4, parent_id);
                statement.bindString(5, next);
                statement.bindString(6, project);
                statement.execute();
            }

            db.setTransactionSuccessful();
        } catch (Exception e) {
            FileHelper.setLog(context, e.getMessage());
        } finally {
            db.endTransaction();
        }
        Log.e("isi master", "end master");
    }

    @Override
    public void onResponse(Call<DataModel> call, Response<DataModel> response) {
        Log.e("response", response.toString());
        if(response.isSuccessful()) {
            if(db==null) db = VSDbHelper.getInstance(context).getWritableDatabase();
            DataModel dataModel = response.body();

            if (dataModel.master != null) {
                AsyncTask.execute(() -> _set_master(dataModel.master));
            }

            if(vsRetrofitListener != null) vsRetrofitListener.onSuccess("master");
        } else {
            if(vsRetrofitListener!=null) vsRetrofitListener.onFailure("master");
        }
    }

    @Override
    public void onFailure(Call<DataModel> call, Throwable t) {
        Log.e("error", t.getMessage());
        FileHelper.setLog(context,t.getMessage());

        if(vsRetrofitListener!=null) vsRetrofitListener.onFailure("master");
    }
}
