package com.virtusee.model;

import com.google.gson.annotations.SerializedName;

import java.util.Arrays;

public class DataModel {
    @SerializedName("store")
    public StoreModel[] store;

    @SerializedName("form")
    public FormModel[] form;

    @SerializedName("schedule")
    public ScheduleModel[] schedule;

    @SerializedName("deldata")
    public DeldataModel[] deldata;

    @SerializedName("master")
    public MasterModel[] master;

    @SerializedName("prj")
    public PrjModel prj;

    @Override
    public String toString() {
        return "DataModel{" +
                "store=" + Arrays.toString(store) +
                ", form=" + Arrays.toString(form) +
                ", schedule=" + Arrays.toString(schedule) +
                ", deldata=" + Arrays.toString(deldata) +
                ", master=" + Arrays.toString(master) +
                '}';
    }
}
