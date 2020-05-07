package com.example.ndvi;

import android.app.Application;
import android.content.res.Configuration;
import android.os.Environment;

import com.example.ndvi.util.AppFolderConfig;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.PrettyFormatStrategy;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;


import java.io.File;
import java.util.ArrayList;


/**
 * @author znws-majin
 * @version :1
 * @CreateDate 2018年8月07日
 * @description :自定义 Application
 */
public class NDVIApp extends Application {

    private static NDVIApp instance;

    public final static int APP_STATUS_KILLED = 0; // 表示应用是被杀死后在启动的
    public final static int APP_STATUS_NORMAL = 1; // 表示应用时正常的启动流程
    public static       int APP_STATUS        = APP_STATUS_KILLED; // 记录App的启动状态

    private ArrayList<String> mPathList;
    private ArrayList<String> mMsgIdList;

    @Override
    public void onCreate() {
        instance = this;
        Logger.addLogAdapter(new AndroidLogAdapter(PrettyFormatStrategy.newBuilder().tag("Logger").build()));
        /**初始化文件夹*/
        AppFolderConfig.getInstance().initConfig();

        initOpenCV();
        super.onCreate();
    }

    public static NDVIApp getInstance() {
        if (instance == null) {
            synchronized (NDVIApp.class) {
                if (instance == null) {
                    instance = new NDVIApp();
                }
            }
        }
        return instance;
    }

    /** 初始化openCV */
    private void initOpenCV() {
        //初始化
        if (! OpenCVLoader.initDebug()) {
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, mLoaderCallback);
        } else {
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    /***
     * opencv库 加载并初始化回调的函数
     */
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            super.onManagerConnected(status);
            switch (status) {
                case BaseLoaderCallback.SUCCESS:
                    Logger.d("opencv initialization successful");
                    break;
                default:
                    super.onManagerConnected(status);
                    Logger.d("opencv initialization failed");
                    break;
            }
        }
    };

    /**
     * 获取数据库的管理器
     * 通过管理器进行增删改查
     */
//    public DbManager getDbManager() {
//        DbManager.DaoConfig daoconfig = new DbManager.DaoConfig();
//        //默认在data/data/包名/database/数据库名称
//        daoconfig.setDbName("NDVI_MSG.db");
//        String path = getSDPath() + "/NDVI_DB";
//
//        File dir = new File(path);
//        if (! dir.exists()) {
//            Logger.d("文件夹不存在  ");
//            daoconfig.setDbDir(new File(path));
//            daoconfig.setDbVersion(1);//默认1
//        } else {
//            Logger.d("文件夹已经存在");
//        }
//
//        //通过manager进行增删改查
//        return x.getDb(daoconfig);
//    }

    /**
     * 获取文件路径
     */
    public String getSDPath() {
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState()
                .equals(android.os.Environment.MEDIA_MOUNTED); //判断sd卡是否存在
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory();//获取跟目录
        }
        return sdDir.toString();
    }

    public ArrayList<String> getPathList() {
        if (mPathList == null) {
            return new ArrayList<>();
        }
        return mPathList;
    }

    public void setPathList(ArrayList<String> mPathList) {
        this.mPathList = mPathList;
    }

    public ArrayList<String> getMsgIdList() {
        if (mMsgIdList == null) {
            return new ArrayList<>();
        }
        return mMsgIdList;
    }

    public void setMsgIdList(ArrayList<String> mMsgIdList) {
        this.mMsgIdList = mMsgIdList;
    }

    public void clearListRes() {
        if (mPathList != null) {
            mPathList.clear();
        }
        if (mMsgIdList != null) {
            mMsgIdList.clear();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //     ActivityUtil.getInstance().AppExit(this);
    }
}
