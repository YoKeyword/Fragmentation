package me.yokeyword.fragmentation.helper;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Result 记录
 * Created by YoKeyword on 16/6/2.
 */
public class FragmentResultRecord implements Parcelable {
    public int requestCode;
    public int resultCode = 0;
    public Bundle resultBundle;

    public FragmentResultRecord() {
    }

    protected FragmentResultRecord(Parcel in) {
        requestCode = in.readInt();
        resultCode = in.readInt();
        resultBundle = in.readBundle();
    }

    public static final Creator<FragmentResultRecord> CREATOR = new Creator<FragmentResultRecord>() {
        @Override
        public FragmentResultRecord createFromParcel(Parcel in) {
            return new FragmentResultRecord(in);
        }

        @Override
        public FragmentResultRecord[] newArray(int size) {
            return new FragmentResultRecord[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(requestCode);
        dest.writeInt(resultCode);
        dest.writeBundle(resultBundle);
    }
}
