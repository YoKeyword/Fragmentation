package me.yokeyword.fragmentation.anim;


import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by YoKeyword on 16/2/15.
 */
public class DefaultNoAnimator extends FragmentAnimator implements Parcelable {
    public DefaultNoAnimator() {
        enter = 0;
        exit = 0;
        popEnter = 0;
        popExit = 0;
    }

    protected DefaultNoAnimator(Parcel in) {
        super(in);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<DefaultNoAnimator> CREATOR = new Creator<DefaultNoAnimator>() {
        @Override
        public DefaultNoAnimator createFromParcel(Parcel in) {
            return new DefaultNoAnimator(in);
        }

        @Override
        public DefaultNoAnimator[] newArray(int size) {
            return new DefaultNoAnimator[size];
        }
    };
}
