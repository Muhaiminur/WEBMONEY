package com.misfit.webmoney.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class IssueDate {

    @SerializedName("mm")
    @Expose
    public String mm;
    @SerializedName("en")
    @Expose
    public String en;

    public String getMm() {
        return mm;
    }

    public void setMm(String mm) {
        this.mm = mm;
    }

    public String getEn() {
        return en;
    }

    public void setEn(String en) {
        this.en = en;
    }

    @Override
    public String toString() {
        return "IssueDate{" +
                "mm='" + mm + '\'' +
                ", en='" + en + '\'' +
                '}';
    }
}