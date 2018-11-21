
package com.bnadev.tourism.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class TourismList {

    @SerializedName("nama_pariwisata")
    @Expose
    private String namaPariwisata;
    @SerializedName("alamat_pariwisata")
    @Expose
    private String alamatPariwisata;
    @SerializedName("detail_pariwisata")
    @Expose
    private String detailPariwisata;
    @SerializedName("gambar_pariwisata")
    @Expose
    private String gambarPariwisata;

    public String getNamaPariwisata() {
        return namaPariwisata;
    }

    public void setNamaPariwisata(String namaPariwisata) {
        this.namaPariwisata = namaPariwisata;
    }

    public String getAlamatPariwisata() {
        return alamatPariwisata;
    }

    public void setAlamatPariwisata(String alamatPariwisata) {
        this.alamatPariwisata = alamatPariwisata;
    }

    public String getDetailPariwisata() {
        return detailPariwisata;
    }

    public void setDetailPariwisata(String detailPariwisata) {
        this.detailPariwisata = detailPariwisata;
    }

    public String getGambarPariwisata() {
        return gambarPariwisata;
    }

    public void setGambarPariwisata(String gambarPariwisata) {
        this.gambarPariwisata = gambarPariwisata;
    }

}
