package com.virtusee.restful;

import android.content.Context;
import android.util.Log;

import com.virtusee.helper.AuthHelper;
import com.virtusee.helper.ConnHelper;
import com.virtusee.helper.NotifHelper;
import com.virtusee.model.AuthModel;
import com.virtusee.retrofit.VSRetrofitAdapter;
import com.virtusee.retrofit.VSRetrofitApi;
import com.virtusee.retrofit.VSRetrofitListener;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


@EBean
public class AuthRest implements Callback<AuthModel> {

	@Bean
	AuthHelper authHelper;

	@Bean
	NotifHelper notifHelper;


	private VSRetrofitListener vsRetrofitListener;
	private String domain;
	private String username;
	private String password;


    public void checkAuth(Context context,String dom, String user, String pass, VSRetrofitListener lst){

        vsRetrofitListener = lst;
        this.domain = dom;
        this.username = user;
        this.password = pass;

        if(!ConnHelper.hasConnection(context)) {
            if(vsRetrofitListener != null) vsRetrofitListener.onFailure("auth");
            return;
        }

        VSRetrofitApi.Auth authApi = VSRetrofitAdapter.init(VSRetrofitApi.Auth.class,authHelper.getAppId(),dom,user,pass);
        Call<AuthModel> call = authApi.getAuth();
        call.enqueue(this);

        return;
    }


    public void getProfile(Context context,VSRetrofitListener lst) {
        vsRetrofitListener = lst;

        if(!ConnHelper.hasConnection(context)) {
            if(vsRetrofitListener != null) vsRetrofitListener.onFailure("noconn");
            return;
        }

        VSRetrofitApi.Auth authApi = VSRetrofitAdapter.init(VSRetrofitApi.Auth.class,authHelper.getFormattedUsername(),authHelper.getPassword());

        Call<AuthModel> call = authApi.getProfile();
        call.enqueue(this);

        return;
    }



    @Override
    public void onResponse(Call<AuthModel> call, Response<AuthModel> response) {
        Log.e("login",response.message());
        if(response.isSuccessful()) {
            AuthModel s = response.body();
            authHelper.setCredentials(this.domain, s._id, this.username, this.password,s.fullname,s.company);
            vsRetrofitListener.onSuccess("auth");
        } else {
            VSRetrofitAdapter.reset();
            vsRetrofitListener.onFailure("auth");
        }
    }

    @Override
    public void onFailure(Call<AuthModel> call, Throwable t) {
        Log.e("login",t.getMessage());
        VSRetrofitAdapter.reset();
        vsRetrofitListener.onFailure("auth");
    }

}
