package com.bnadev.tourism.network;

import com.bnadev.tourism.model.Tourism;
import com.bnadev.tourism.model.TourismList;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiInterface {

    @GET("jsonBootcamp.php")
    Call<Tourism> getTourism();

}
