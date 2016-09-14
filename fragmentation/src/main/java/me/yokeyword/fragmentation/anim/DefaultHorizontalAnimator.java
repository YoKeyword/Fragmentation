package me.yokeyword.fragmentation.anim;

import android.os.Parcel;
import android.os.Parcelable;

import me.yokeyword.fragmentation.R;

/**
 * Created by YoKeyword on 16/2/5.
 */
public class DefaultHorizontalAnimator extends FragmentAnimator implements Parcelable{

    public DefaultHorizontalAnimator() {
        enter = R.anim.h_fragment_enter;
        exit = R.anim.h_fragment_exit;
        popEnter = R.anim.h_fragment_pop_enter;
        popExit = R.anim.h_fragment_pop_exit;
    }

    protected DefaultHorizontalAnimator(Parcel in) {
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

    public static final Creator<DefaultHorizontalAnimator> CREATOR = new Creator<DefaultHorizontalAnimator>() {
        @Override
        public DefaultHorizontalAnimator createFromParcel(Parcel in) {
            return new DefaultHorizontalAnimator(in);
        }

        @Override
        public DefaultHorizontalAnimator[] newArray(int size) {
            return new DefaultHorizontalAnimator[size];
        }
    };
}
