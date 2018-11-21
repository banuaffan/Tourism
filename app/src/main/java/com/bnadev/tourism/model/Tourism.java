
package com.bnadev.tourism.model;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Tourism {

    @SerializedName("result")
    @Expose
    private String result;
    @SerializedName("data")
    @Expose
    private List<TourismList> data = null;

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public List<TourismList> getData() {
        return data;
    }

    public void setData(List<TourismList> data) {
        this.data = data;
    }

}
