package com.virtusee.restful;

import android.content.Context;

import com.virtusee.helper.AuthHelper;
import com.virtusee.helper.ConnHelper;
import com.virtusee.helper.NotifHelper;
import com.virtusee.retrofit.VSRetrofitAdapter;
import com.virtusee.retrofit.VSRetrofitApi;
import com.virtusee.retrofit.VSRetrofitListener;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


@EBean
public class SessionRest implements Callback<String> {


	@Bean
	AuthHelper authHelper;

	@Bean
	NotifHelper notifHelper;

	private VSRetrofitListener vsRetrofitListener;
	private String domain;
	private String username;
	private String password;

    public void checkSess(Context context,VSRetrofitListener lst){

        vsRetrofitListener = lst;

        if(!ConnHelper.hasConnection(context)) {
            if(vsRetrofitListener != null) vsRetrofitListener.onFailure("noconn");
            return;
        }

        VSRetrofitApi.Auth authApi = VSRetrofitAdapter.init(VSRetrofitApi.Auth.class,authHelper.getFormattedUsername(),authHelper.getPassword());
        Call<String> call = authApi.getSess();
        call.enqueue(this);

        return;
    }


    @Override
    public void onResponse(Call<String> call, Response<String> response) {
        if(response.isSuccessful()) {
            String s = response.body();

            String[] rrs = s.split("~");
            if (rrs[1].equals("sukses")) {
                vsRetrofitListener.onSuccess("sess");
            } else {
                vsRetrofitListener.onFailure("sess");
            }
        } else {
            vsRetrofitListener.onSuccess("sess");
        }
	}

	@Override
    public void onFailure(Call<String> call, Throwable t) {
        vsRetrofitListener.onSuccess("sess");
	}
}
