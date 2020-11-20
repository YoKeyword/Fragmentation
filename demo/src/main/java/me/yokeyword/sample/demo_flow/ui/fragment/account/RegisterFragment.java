package me.yokeyword.sample.demo_flow.ui.fragment.account;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import me.yokeyword.sample.R;
import me.yokeyword.sample.demo_flow.base.BaseBackFragment;

/**
 * Created by YoKeyword on 16/2/14.
 */
public class RegisterFragment extends BaseBackFragment {
    private EditText mEtAccount, mEtPassword, mEtPasswordConfirm;
    private Button mBtnRegister;
    private LoginFragment.OnLoginSuccessListener mOnLoginSuccessListener;

    public static RegisterFragment newInstance() {

        Bundle args = new Bundle();

        RegisterFragment fragment = new RegisterFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof LoginFragment.OnLoginSuccessListener) {
            mOnLoginSuccessListener = (LoginFragment.OnLoginSuccessListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnLoginSuccessListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);
        initView(view);
        return view;
    }

    private void initView(View view) {
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        mEtAccount = (EditText) view.findViewById(R.id.et_account);
        mEtPassword = (EditText) view.findViewById(R.id.et_password);
        mEtPasswordConfirm = (EditText) view.findViewById(R.id.et_password_confirm);
        mBtnRegister = (Button) view.findViewById(R.id.btn_register);

        showSoftInput(mEtAccount);

        toolbar.setTitle(R.string.register);
        initToolbarNav(toolbar);

        mBtnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String strAccount = mEtAccount.getText().toString();
                String strPassword = mEtPassword.getText().toString();
                String strPasswordConfirm = mEtPasswordConfirm.getText().toString();
                if (TextUtils.isEmpty(strAccount.trim())) {
                    Toast.makeText(_mActivity, R.string.error_username, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(strPassword.trim()) || TextUtils.isEmpty(strPasswordConfirm.trim())) {
                    Toast.makeText(_mActivity, R.string.error_pwd, Toast.LENGTH_SHORT).show();
                    return;
                }

                // 注册成功
                mOnLoginSuccessListener.onLoginSuccess(strAccount);
                popTo(LoginFragment.class, true);
            }
        });
    }

    @Override
    public void onSupportInvisible() {
        super.onSupportInvisible();
        hideSoftInput();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mOnLoginSuccessListener = null;
    }
}
