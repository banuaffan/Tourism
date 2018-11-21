package com.bnadev.tourism.main;

import android.content.Context;

import com.bnadev.tourism.adapter.TourismAdapter;
import com.bnadev.tourism.model.Tourism;
import com.bnadev.tourism.model.TourismList;
import com.bnadev.tourism.network.ApiClientTourism;
import com.bnadev.tourism.network.ApiInterface;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainPresenter implements MainContract.Presenter {

    private MainContract.View mView;
    private TourismAdapter mAdapter;
    private List<TourismList> tourismLists;

    public MainPresenter(MainContract.View view) {
        mView = view;
    }


    @Override
    public void getRetrofitObject(Context context) {

        ApiInterface apiInterface = ApiClientTourism.getClient().create(ApiInterface.class);

        Call<Tourism> call = apiInterface.getTourism();
        call.enqueue(new Callback<Tourism>() {
            @Override
            public void onResponse(Call<Tourism> call, Response<Tourism> response) {

                mView.viewGoneTwo();

                int statusCode = response.code();

                tourismLists = response.body().getData();

                mView.getDataSuccess("Number of Tourisms = " + tourismLists.size(), tourismLists);

            }

            @Override
            public void onFailure(Call<Tourism> call, Throwable t) {

                mView.viewGoneThree();

                mView.getDataFailure(t.getMessage());
            }
        });

    }
}
