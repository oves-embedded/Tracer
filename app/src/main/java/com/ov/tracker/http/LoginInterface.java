package com.ov.tracker.http;

import com.ov.tracker.entity.http.LoginResult;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface LoginInterface {
    @POST("graphql")
    Call<LoginResult> login(@Body RequestBody requestBody);

}
