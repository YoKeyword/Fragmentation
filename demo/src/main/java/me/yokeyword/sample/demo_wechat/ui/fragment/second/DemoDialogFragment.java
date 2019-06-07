package me.yokeyword.sample.demo_wechat.ui.fragment.second;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import me.yokeyword.fragmentation.ISupportActivity;
import me.yokeyword.sample.R;

/**
 * 使用DialogFragment时，需要重写show()，入Fragmentation的事务队列
 *
 * Dialog是基于Window （Activity也是Window），普通Fragment的视图一般基于View，这样会导致Dialog永远会浮在最顶层
 *
 * 可以考虑自定义半透明View的Fragment，从视觉上模拟Dialog
 *
 * Created by YoKey on 19/6/7.
 */

public class DemoDialogFragment extends DialogFragment {

    public static DemoDialogFragment newInstance() {
        return new DemoDialogFragment();
    }

    protected FragmentActivity _mActivity;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        _mActivity = (FragmentActivity) activity;
    }

    /**
     * Enqueue the Fragmentation Queue.
     *
     * 如果是SupportFragment打开，可以不用复写该方法， 放到post()中show亦可
     */
    @Override
    public void show(final FragmentManager manager, final String tag) {
        if (_mActivity instanceof ISupportActivity) {
            ((ISupportActivity) _mActivity).getSupportDelegate().post(new Runnable() {
                @Override
                public void run() {
                    DemoDialogFragment.super.show(manager, tag);
                }
            });
            return;
        }

        super.show(manager, tag);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.wechat_fragment_dialog, container, false);
        view.findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        return view;
    }
}
