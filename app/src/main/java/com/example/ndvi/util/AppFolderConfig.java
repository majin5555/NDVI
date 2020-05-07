package com.example.ndvi.util;

import android.os.Environment;

import java.io.File;

/**
 * @author znws-majin
 * @version :1
 * @CreateDate 2019年8月05日
 * @description :文件夹配置管理
 */
public class AppFolderConfig {

    // Singleton

    private static AppFolderConfig instance;

    private AppFolderConfig() {

    }

    //写成静态单利
    public static AppFolderConfig getInstance() {

        if (instance == null) {

            instance = new AppFolderConfig();
        }
        return instance;
    }


    // Const Config            Environment.getExternalStorageDirectory()
    private String envConfig = Environment.getExternalStorageDirectory().getAbsolutePath();
    private String pathRoot  = envConfig + File.separator + "NDVI" + File.separator;

    private String PH;
    private String TXT;

    public String getPH() {
        return PH == null ? "" : PH;
    }

    public String getTXT() {
        return TXT == null ? "" : TXT;
    }

    public void initConfig() {

        // App Init
        File folder = null;
        folder = new File(this.pathRoot);
        if (! folder.exists()) {
            folder.mkdir();
        }


        folder = new File(pathRoot);
        if (! folder.exists()) {
            folder.mkdir();
        }

        initPH();
        initTXT();

    }

    public void initPH() {
        File folder = null;
        this.PH = this.pathRoot + "PH" + File.separator;
        folder = new File(this.PH);
        if (! folder.exists()) {
            folder.mkdir();
        }
    }

    public void initTXT() {
        File folder = null;
        this.TXT = this.pathRoot + "TXT" + File.separator;
        folder = new File(this.TXT);
        if (! folder.exists()) {
            folder.mkdir();
        }
    }


}
