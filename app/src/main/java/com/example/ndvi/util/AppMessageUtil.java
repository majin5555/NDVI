package com.example.ndvi.util;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;

import com.example.ndvi.NDVIApp;
import com.example.ndvi.bean.AppInfo;

import java.util.Date;
import java.util.List;

import baseLibrary.util.DateUtils;

import static com.example.ndvi.control.AppSet.USEAPP;

/**
 * @author zaws-majin
 * @version :1
 * @CreateDate 2018年8月7日
 * @description :获取app信息工具类
 */
public class AppMessageUtil {
    private static AppMessageUtil nUtil;


    private AppMessageUtil() {
    }

    /**
     * 单一实例
     */
    public static AppMessageUtil getInstance() {
        if (nUtil == null) {
            synchronized (AppMessageUtil.class) {
                if (nUtil == null) {
                    nUtil = new AppMessageUtil();
                }
            }
        }
        return nUtil;
    }


    /** 获取Apk信息 */
    public   String getAppMessage() {
        // 获取已经安装的所有应用, PackageInfo　系统类，包含应用信息
        List<PackageInfo> packages = NDVIApp.getInstance().getPackageManager().getInstalledPackages(0);
        for (int i = 0; i < packages.size(); i++) {
            PackageInfo packageInfo = packages.get(i);
            if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) { //非系统应用

                if (packageInfo.applicationInfo.loadLabel(NDVIApp.getInstance().getPackageManager()).toString().equals("NDVI")) {
                    AppInfo appInfo = new AppInfo(); // AppInfo 自定义类，包含应用信息
                    appInfo.setFirstInstallTime(packageInfo.firstInstallTime);//首次应用装时间
                    /**获取首次安装APP的date时间*/
                    Date date = DateUtils.parseServerTime(DateUtils.stampToDate(packageInfo.firstInstallTime));
                    /**计算App有效时间*/
                    String EffectiveTime = DateUtils.addTimeYear(date, USEAPP);
                    appInfo.setEffectiveTime(DateUtils.dateTimeStamp(EffectiveTime));//有效运行时间
                    appInfo.setLastUpdateTime(packageInfo.lastUpdateTime); // 应用最后一次更新时间
                    appInfo.setAppName(packageInfo.applicationInfo.loadLabel(NDVIApp.getInstance().getPackageManager()).toString());//获取应用名称
                    appInfo.setPackageName(packageInfo.packageName); //获取应用包名，可用于卸载和启动应用
                    appInfo.setVersionName(packageInfo.versionName);//获取应用版本名
                    appInfo.setVersionCode(packageInfo.versionCode);//获取应用版本号
                    return  packageInfo.versionName+"_"+packageInfo.versionCode;
                }
            } else { // 系统应用

            }
        }
        return "";
    }


}