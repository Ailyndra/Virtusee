package com.virtusee.retrofit;

import android.text.TextUtils;

import com.virtusee.helper.InterceptorHelper;

import java.util.concurrent.TimeUnit;

import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by winata on 8/24/15.
 */

public class VSRetrofitAdapter {

    private static final String RESTURL = "https://apis.virtusee.com/v3/index.php/api/";
    private static OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
    private static Retrofit.Builder builder =
            new Retrofit.Builder()
                    .baseUrl(RESTURL)
                    .addConverterFactory(GsonConverterFactory.create());

    private static Retrofit retrofit = builder.build();
    public static boolean awal = false;


    public static <S> S init(Class<S> apiClass) {
        return init(apiClass, null);
    }

    public static <S> S init(Class<S> apiClass, String appid, String domain, String username, String password) {
        if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(password)) {
            String formatted  = appid + "####" + domain + "//" + username;
            String authToken = Credentials.basic(formatted, password);
            return init(apiClass, authToken);
        }

        return init(apiClass, null);
    }


    public static <S> S init(Class<S> apiClass,  String formatted, String password) {
        if (!TextUtils.isEmpty(formatted) && !TextUtils.isEmpty(password)) {
            String authToken = Credentials.basic(formatted, password);
            return init(apiClass, authToken);
        }

        return init(apiClass, null);
    }

    public static <S> S init( Class<S> apiClass, final String authToken) {
        if (!TextUtils.isEmpty(authToken)) {
            if (!awal ) {
                InterceptorHelper interceptor =
                        new InterceptorHelper(authToken);

                HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
                logging.setLevel(HttpLoggingInterceptor.Level.BODY);

                httpClient.addInterceptor(interceptor);
                httpClient.addInterceptor(logging);
                httpClient.connectTimeout(600, TimeUnit.SECONDS);
                httpClient.readTimeout(600,TimeUnit.SECONDS).build();

                builder.client(httpClient.build());
                retrofit = builder.build();
                awal = true;

            }
        }
        return retrofit.create(apiClass);
    }

    public static void reset(){
        awal = false;
        try {
            if (httpClient.interceptors().get(0).getClass().getSimpleName().equals("InterceptorHelper"))
                httpClient.interceptors().remove(0);
        } catch (Exception e){
        }
    }
}
