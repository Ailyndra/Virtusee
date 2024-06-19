package com.virtusee.model;

import com.google.gson.annotations.SerializedName;

public class TesModel {

    @SerializedName("store")
    StoreModel[] storeModels;

    @SerializedName("form")
    FormModel[] formModels;

    @SerializedName("deldata")
    DeldataModel[] deldataModels;
}
	
