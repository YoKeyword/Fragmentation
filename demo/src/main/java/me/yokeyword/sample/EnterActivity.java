package me.yokeyword.sample;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import me.yokeyword.sample.demo_flow.MainActivity;

/**
 * Created by YoKeyword on 16/6/5.
 */
public class EnterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter);

        initView();
    }

    private void initView() {
        Toolbar toolBar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolBar);

        findViewById(R.id.btn_flow).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(EnterActivity.this, MainActivity.class));
            }
        });

        findViewById(R.id.btn_wechat).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(EnterActivity.this, me.yokeyword.sample.demo_wechat.MainActivity.class));
            }
        });

        findViewById(R.id.btn_zhihu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(EnterActivity.this, me.yokeyword.sample.demo_zhihu.MainActivity.class));
            }
        });
    }
}
