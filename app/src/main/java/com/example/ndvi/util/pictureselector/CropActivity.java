package com.example.ndvi.util.pictureselector;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;

import com.example.ndvi.R;
import com.example.ndvi.util.AppFolderConfig;
import com.example.ndvi.util.pictureselector.photocropper.CropImageView;
import com.example.ndvi.util.pictureselector.photocropper.CropListener;
import com.orhanobut.logger.Logger;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import baseLibrary.activity.BaseActivity;

import static com.example.ndvi.util.pictureselector.PictureSelectActivity.mSelectTime;


public class CropActivity extends BaseActivity {

    Bitmap mBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setTitle("图片裁剪");
    }

    @Override
    public void initView() {
        setContentView(R.layout.activity_crop);
        final CropImageView cropImageView = findViewById(R.id.crop_image_view);
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        mBitmap = BitmapFactory.decodeFile((String) bundle.get("basePicPath"));
        cropImageView.setImageBitmap(mBitmap);


        final CropListener listener = bitmap -> {
            /**图片保存本地*/
            String mCrop = "PH_Crop_" + mSelectTime + ".jpg";
            savePhotoToSDCard(bitmap, AppFolderConfig.getInstance().getPH(), mCrop);
            intent.putExtra("CropName", mCrop);
            setResult(RESULT_OK, intent);
            finishActivity();
        };

        /**裁剪*/
        findViewById(R.id.btn_crop).setOnClickListener(v -> cropImageView.crop(listener, false));
        /**旋转*/
        findViewById(R.id.btn_rotate).setOnClickListener(v -> rotateBitmap());
    }

    private void rotateBitmap() {
        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        mBitmap = Bitmap.createBitmap(mBitmap, 0, 0, mBitmap.getWidth(), mBitmap.getHeight(), matrix, false);
        ((CropImageView) findViewById(R.id.crop_image_view)).setImageBitmap(mBitmap);
    }

    /**
     * Save image to the SD card
     */
    public void savePhotoToSDCard(Bitmap photoBitmap, String dirPath, String photoName) {

        File dir = new File(dirPath);
        if (! dir.exists()) {
            dir.mkdirs();
        }
        File photoFile = new File(dirPath, photoName);
        BufferedOutputStream bos = null;
        try {
            bos = new BufferedOutputStream(new FileOutputStream(photoFile));
            photoBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            bos.flush();
            bos.close();
            photoBitmap.recycle();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Logger.d("------------------------------FileNotFoundException");
        } catch (IOException e) {
            e.printStackTrace();
            Logger.d("------------------------------IOException");
        }
        Logger.d("保存裁剪图片到路径：---" + dirPath + photoName);
        try {
            MediaStore.Images.Media.insertImage(this.getContentResolver(), dirPath, photoName, null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        //最后通知图库更新
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);//扫描单个文件
        intent.setData(Uri.fromFile(photoFile));//给图片的绝对路径
        this.sendBroadcast(intent);
        return;
    }
}
