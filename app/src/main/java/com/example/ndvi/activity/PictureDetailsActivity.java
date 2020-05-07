package com.example.ndvi.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;

import com.bm.library.PhotoView;
import com.example.ndvi.R;
import com.example.ndvi.util.GlideUtil;

import baseLibrary.activity.BaseActivity;

/**
 * 查看图片详情
 */
public class PictureDetailsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.see_pic);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);// 显示导航按钮
            actionBar.setHomeAsUpIndicator(R.drawable.ico_back);// 设置导航按钮图标
            actionBar.setTitle(R.string.back);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // 导航按钮
            case android.R.id.home:
                finishActivity();
                break;
            // 其他ToolBar按钮
            default:
                break;
        }
        return true;
    }

    @Override
    public void initView() {
        setContentView(R.layout.activity_picture_details);
        PhotoView mPhotoView = getView(R.id.PhotoView);
        // 启用图片缩放功能
        mPhotoView.enable();
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        if (bundle != null)
            if (intent.getFlags() == 0) {
                GlideUtil.showImageFile(PictureDetailsActivity.this, (String) bundle.get("PICPATH"), mPhotoView, R.drawable.default_null);
            }
    }

    @Override
    public void onBackPressed() {
        finishActivity();
    }
}
