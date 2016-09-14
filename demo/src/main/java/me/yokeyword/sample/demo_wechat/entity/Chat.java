package me.yokeyword.sample.demo_wechat.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by YoKeyword on 16/6/30.
 */
public class Chat implements Parcelable {
    public String name;
    public String message;
    public long time;
    public int avatar;

    public Chat() {
    }

    protected Chat(Parcel in) {
        name = in.readString();
        message = in.readString();
        time = in.readLong();
        avatar = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(message);
        dest.writeLong(time);
        dest.writeInt(avatar);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Chat> CREATOR = new Creator<Chat>() {
        @Override
        public Chat createFromParcel(Parcel in) {
            return new Chat(in);
        }

        @Override
        public Chat[] newArray(int size) {
            return new Chat[size];
        }
    };
}
