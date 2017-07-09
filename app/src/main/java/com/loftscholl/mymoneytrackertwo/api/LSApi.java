package com.loftscholl.mymoneytrackertwo.api;

import com.loftscholl.mymoneytrackertwo.Item;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Created by Гео on 28.06.2017.
 */

public interface LSApi {

    @Headers("Content-Type: application/json")
    @GET("items")
    Call<List<Item>> items(@Query("type") String type);

    @POST("items/add")
    Call<AddResult> add(@Query("name") String name,
                        @Query("price") int price, @Query("type")
                                String type);

    @POST("items/remove")
    Call<Result> remove(@Query("id") int id);

    @GET("balance")
    Call<BalanceResult> balance();

    @GET("auth")
    Call<AuthResult> auth(@Query("social_user_id") String socialUserId);

}
