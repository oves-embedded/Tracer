package com.ov.tracker.http;


import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitService {

    private static RetrofitService instance;
    private static OkHttpClient mOkHttpClient;
    private static final Boolean DEBUG = true;
    private static final int TIMEOUT_READ = 10;

    private RetrofitService(){}

    public static RetrofitService getInstance(){
        if(instance==null){
            synchronized (RetrofitService.class){
                if(instance==null){
                    instance=new RetrofitService();
                }
            }
        }
        return instance;
    }

    public  <T> T createInterface(Class<T> service) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(HttpConstants.BASE_HTTP_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(getOkHttpClient())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
        return retrofit.create(service);
    }

    private synchronized OkHttpClient getOkHttpClient() {
        if (mOkHttpClient != null) return mOkHttpClient;

        HttpLoggingInterceptor logInterceptor = new HttpLoggingInterceptor();
        //打印信息
        HttpLoggingInterceptor.Level level = DEBUG ? HttpLoggingInterceptor.Level.HEADERS : HttpLoggingInterceptor.Level.NONE;
        logInterceptor.setLevel(level);

        Interceptor interceptor = (new Interceptor() {    //添加拦截器 可以在请求头中添加 一些小是参数
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request original = chain.request();
                Request.Builder requestBuilder = original.newBuilder().header("Content-Type","application/json");
//                Request.Builder requestBuilder = original.newBuilder()
//                        .header("Authorization", Authorization)
//                        .addHeader("accountId", "1")
//                        .addHeader("RELEASE", versionsCode);
                Request request = requestBuilder.build();
                return chain.proceed(request);
            }
        });

        return mOkHttpClient = new OkHttpClient.Builder()
                .retryOnConnectionFailure(true)
                .addInterceptor(logInterceptor)
                .addInterceptor(interceptor)
                .readTimeout(TIMEOUT_READ, TimeUnit.SECONDS)
                .connectTimeout(TIMEOUT_READ, TimeUnit.SECONDS)
                .build();
    }
}
