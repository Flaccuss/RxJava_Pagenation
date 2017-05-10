package com.app.androidkt.rxapp.datasource.remot.git;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by brijesh on 25/4/17.
 */

public class User implements Parcelable {
    public int id;
    public String login;
    public String avatar_url;


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeString(this.login);
        dest.writeString(this.avatar_url);
    }

    public User() {
    }

    protected User(Parcel in) {
        this.id = in.readInt();
        this.login = in.readString();
        this.avatar_url = in.readString();
    }

    public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
        @Override
        public User createFromParcel(Parcel source) {
            return new User(source);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };
}
