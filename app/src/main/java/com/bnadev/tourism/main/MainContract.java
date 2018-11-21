package com.bnadev.tourism.main;

import android.content.Context;

import com.bnadev.tourism.model.TourismList;

import java.util.List;

public interface MainContract {

    interface View{
        void viewGoneTwo();

        void viewGoneThree();

        void getDataSuccess(String message, List<TourismList> tourismLists);

        void getDataFailure(String message);
    }

    interface Presenter{

        void getRetrofitObject(Context context);

    }

}
