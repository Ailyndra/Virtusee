package com.virtusee.model;

import android.os.Parcel;
import android.os.Parcelable;

public class MasterModel implements Parcelable {
    public String _id;
    public String name;
    public String type;
    public String parent_id;
    public String next;

    public MasterModel() {

    }

    public MasterModel(String _id, String name, String type, String parent_id, String next) {
        this._id = _id;
        this.name = name;
        this.type = type;
        this.parent_id = parent_id;
        this.next = next;
    }

    protected MasterModel(Parcel in) {
        _id = in.readString();
        name = in.readString();
        type = in.readString();
        parent_id = in.readString();
        next = in.readString();
    }

    public static final Creator<MasterModel> CREATOR = new Creator<MasterModel>() {
        @Override
        public MasterModel createFromParcel(Parcel in) {
            return new MasterModel(in);
        }

        @Override
        public MasterModel[] newArray(int size) {
            return new MasterModel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(_id);
        dest.writeString(name);
        dest.writeString(type);
        dest.writeString(parent_id);
        dest.writeString(next);
    }
}
