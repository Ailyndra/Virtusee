package com.virtusee.retrofit;


import com.virtusee.model.AnswerModel;
import com.virtusee.model.AnswerResponse;
import com.virtusee.model.AnswersModel;
import com.virtusee.model.AuthModel;
import com.virtusee.model.DataModel;
import com.virtusee.model.TokenModel;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;


public class VSRetrofitApi {

	public interface Data {
		@GET("fetch")
        Call<DataModel> getData();

		@GET("fetch/last/{lastsync}")
        Call<DataModel> getDataPart(@Path("lastsync") String lastsync);

		@GET("load_master")
        Call<DataModel> getMaster();

        @GET("load_absensi_ulipc")
        Call<DataModel> getAbsensi();
	}

	public interface Auth {
        @GET("auth")
        Call<AuthModel> getAuth();

        @GET("checksess")
        Call<String> getSess();

        @GET("profile")
        Call<AuthModel> getProfile();

        @POST("token")
        Call<String> sendToken(@Body TokenModel tokenModel);

    }


	public interface Answer {
		@POST("answer")
        Call<String> sendAnswer(@Body AnswerModel answerModel);

		@POST("answerretrofit")
        Call<String> sendAnswerAll(@Body AnswersModel answersModel);

        @POST("answerretrofit2")
        Call<AnswerResponse> sendAnswerAll2(@Body AnswersModel answersModel);
    }

	public interface File {
        @Multipart
		@POST("image")
        Call<String> sendImage( @Part("name") RequestBody name, @Part MultipartBody.Part attachments);

        @Multipart
        @POST("audio")
        Call<String> sendAudio( @Part("name") RequestBody name, @Part MultipartBody.Part attachments);
	}
}
