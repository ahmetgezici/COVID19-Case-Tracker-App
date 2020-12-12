package com.ahmetgezici.covid19app.Model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CovidData {

    @SerializedName("tarih")
    @Expose
    public String tarih;
    @SerializedName("gunluk_test")
    @Expose
    public String gunlukTest;
    @SerializedName("gunluk_vaka")
    @Expose
    public String gunlukVaka;
    @SerializedName("gunluk_hasta")
    @Expose
    public String gunlukHasta;
    @SerializedName("gunluk_vefat")
    @Expose
    public String gunlukVefat;
    @SerializedName("gunluk_iyilesen")
    @Expose
    public String gunlukIyilesen;
    @SerializedName("toplam_test")
    @Expose
    public String toplamTest;
    @SerializedName("toplam_hasta")
    @Expose
    public String toplamHasta;
    @SerializedName("toplam_vefat")
    @Expose
    public String toplamVefat;
    @SerializedName("toplam_iyilesen")
    @Expose
    public String toplamIyilesen;
    @SerializedName("toplam_yogun_bakim")
    @Expose
    public String toplamYogunBakim;
    @SerializedName("toplam_entube")
    @Expose
    public String toplamEntube;
    @SerializedName("hastalarda_zaturre_oran")
    @Expose
    public String hastalardaZaturreOran;
    @SerializedName("agir_hasta_sayisi")
    @Expose
    public String agirHastaSayisi;
    @SerializedName("yatak_doluluk_orani")
    @Expose
    public String yatakDolulukOrani;
    @SerializedName("eriskin_yogun_bakim_doluluk_orani")
    @Expose
    public String eriskinYogunBakimDolulukOrani;
    @SerializedName("ventilator_doluluk_orani")
    @Expose
    public String ventilatorDolulukOrani;
    @SerializedName("ortalama_filyasyon_suresi")
    @Expose
    public String ortalamaFilyasyonSuresi;
    @SerializedName("ortalama_temasli_tespit_suresi")
    @Expose
    public String ortalamaTemasliTespitSuresi;
    @SerializedName("filyasyon_orani")
    @Expose
    public String filyasyonOrani;

}