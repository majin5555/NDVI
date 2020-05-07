package com.example.ndvi.util.pictureselector;

import java.io.File;

/**
 * Author       wildma
 * Github       https://github.com/wildma
 * Date         2018/6/10
 * Desc	        ${常量}
 */
public class Constant {
    public static final String APP_NAME = "PH";
    public static final String BASE_DIR = APP_NAME + File.separator;//PictureSelector/
    public static final String DIR_ROOT = FileUtils.getRootPath() + "/NDVI/" + File.separator + Constant.BASE_DIR;

    public static final int CANCEL = 0;//取消
    public static final int CAMERA = 1;//拍照
    public static final int ALBUM  = 2;//相册


}

