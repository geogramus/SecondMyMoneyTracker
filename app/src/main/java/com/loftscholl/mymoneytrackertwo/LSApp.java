package com.loftscholl.mymoneytrackertwo;

import android.app.Application;
import android.text.TextUtils;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.loftscholl.mymoneytrackertwo.api.LSApi;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Гео on 29.06.2017.
 */

public class LSApp extends Application {
    private static final String PREFERENCES_SESSION = "session";
    private static final String KEY_AUTH_TOKEN = "auth-token";
    private LSApi api;

    @Override
    public void onCreate() {
        super.onCreate();
        Gson gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .setDateFormat("yyyy-MM-dd HH:mm:ss")
                .create();
        final OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new HttpLoggingInterceptor().setLevel(BuildConfig.DEBUG ? HttpLoggingInterceptor.Level.HEADERS : HttpLoggingInterceptor.Level.NONE))
                .addInterceptor(new AuthInterceptor())
                .build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://android.loftschool.com/basic/v1/")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(client)
                .build();
        api = retrofit.create(LSApi.class);
    }

    public LSApi api() {
        return api;
    }

    public String getAuthToken() {
        return getSharedPreferences(PREFERENCES_SESSION, MODE_PRIVATE).getString(KEY_AUTH_TOKEN, "");
    }

    public void setAuthToken(String authToken) {
        getSharedPreferences(PREFERENCES_SESSION, MODE_PRIVATE).edit().putString(KEY_AUTH_TOKEN, authToken).apply();
    }

    public boolean isLoggedIn() {
        return !TextUtils.isEmpty(getAuthToken());
    }

    private class AuthInterceptor implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request originalRequest = chain.request();
            HttpUrl url = originalRequest.url().newBuilder().addQueryParameter("auth-token", getAuthToken()).build();
            return chain.proceed(originalRequest.newBuilder().url(url).build());
        }
    }
}
