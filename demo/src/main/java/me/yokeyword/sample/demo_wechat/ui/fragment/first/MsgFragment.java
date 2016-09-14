package me.yokeyword.sample.demo_wechat.ui.fragment.first;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import me.yokeyword.sample.R;
import me.yokeyword.sample.demo_wechat.adapter.MsgAdapter;
import me.yokeyword.sample.demo_wechat.base.BaseBackFragment;
import me.yokeyword.sample.demo_wechat.entity.Chat;
import me.yokeyword.sample.demo_wechat.entity.Msg;

/**
 * Created by YoKeyword on 16/6/30.
 */
public class MsgFragment extends BaseBackFragment {
    private static final String ARG_MSG = "arg_msg";

    private Toolbar mToolbar;
    private RecyclerView mRecy;
    private EditText mEtSend;
    private Button mBtnSend;

    private Chat mChat;
    private MsgAdapter mAdapter;

    public static MsgFragment newInstance(Chat msg) {

        Bundle args = new Bundle();
        args.putParcelable(ARG_MSG, msg);
        MsgFragment fragment = new MsgFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mChat = getArguments().getParcelable(ARG_MSG);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.wechat_fragment_tab_first_msg, container, false);
        initView(view);
        return attachToSwipeBack(view);
    }

    private void initView(View view) {
        mToolbar = (Toolbar) view.findViewById(R.id.toolbar);
        mBtnSend = (Button) view.findViewById(R.id.btn_send);
        mEtSend = (EditText) view.findViewById(R.id.et_send);
        mRecy = (RecyclerView) view.findViewById(R.id.recy);

        mToolbar.setTitle(mChat.name);
        initToolbarNav(mToolbar);
    }

    @Override
    protected void onEnterAnimationEnd(Bundle savedInstanceState) {
        super.onEnterAnimationEnd(savedInstanceState);
        // 入场动画结束后执行  优化,防动画卡顿

        _mActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        mRecy.setLayoutManager(new LinearLayoutManager(_mActivity));
        mRecy.setHasFixedSize(true);
        mAdapter = new MsgAdapter(_mActivity);
        mRecy.setAdapter(mAdapter);

        mBtnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str = mEtSend.getText().toString().trim();
                if (TextUtils.isEmpty(str)) return;

                mAdapter.addMsg(new Msg(str));
                mEtSend.setText("");
                mRecy.scrollToPosition(mAdapter.getItemCount() - 1);
            }
        });

        mAdapter.addMsg(new Msg(mChat.message));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mRecy.setAdapter(null);
        _mActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        hideSoftInput();
    }
}
