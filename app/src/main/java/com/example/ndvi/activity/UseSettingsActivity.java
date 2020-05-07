package com.example.ndvi.activity;

import android.os.Bundle;
import android.view.View;

import com.example.ndvi.R;

import baseLibrary.activity.BaseActivity;

/**
 * 使用权限设置
 */
public class UseSettingsActivity extends BaseActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void initView() {
        setContentView(R.layout.activity_use_settings);
        getView(R.id.btn_sure).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        /**
         * 获取现在的系统时间
         *
         *
         * */
    }


}
