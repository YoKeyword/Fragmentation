package me.yokeyword.sample;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import me.yokeyword.sample.flow.MainActivity;

/**
 * Created by YoKeyword on 16/6/5.
 */
public class EnterActivity extends AppCompatActivity {
    private Toolbar mToolBar;
    private Button mBtnFlow, mBtnMultiple;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter);

        initView();
    }

    private void initView() {
        mToolBar = (Toolbar) findViewById(R.id.toolbar);
        mBtnFlow = (Button) findViewById(R.id.btn_flow);
        mBtnMultiple = (Button) findViewById(R.id.btn_multiple);

        setSupportActionBar(mToolBar);

        mBtnFlow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(EnterActivity.this, MainActivity.class));
            }
        });

        mBtnMultiple.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(EnterActivity.this, me.yokeyword.sample.multiple.MainActivity.class));
            }
        });
    }
}
