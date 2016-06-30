package me.yokeyword.sample;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import me.yokeyword.sample.demo_flow.MainActivity;

/**
 * Created by YoKeyword on 16/6/5.
 */
public class EnterActivity extends AppCompatActivity {
    private Toolbar mToolBar;
    private TextView mTvBtnFlow, mTvBtnWechat, mTvBtnZhihu;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter);

        initView();
    }

    private void initView() {
        mToolBar = (Toolbar) findViewById(R.id.toolbar);
        mTvBtnFlow = (TextView) findViewById(R.id.tv_btn_flow);
        mTvBtnWechat = (TextView) findViewById(R.id.tv_btn_wechat);
        mTvBtnZhihu = (TextView) findViewById(R.id.tv_btn_zhihu);

        setSupportActionBar(mToolBar);

        mTvBtnFlow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(EnterActivity.this, MainActivity.class));
            }
        });

        mTvBtnWechat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(EnterActivity.this, me.yokeyword.sample.demo_wechat.MainActivity.class));
            }
        });

        mTvBtnZhihu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(EnterActivity.this, me.yokeyword.sample.demo_zhihu.MainActivity.class));
            }
        });
    }
}
