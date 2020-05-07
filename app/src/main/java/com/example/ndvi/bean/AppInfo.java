package com.example.ndvi.bean;

/**
 * @author: mj
 * @date: 2019/6/27$
 * @desc: app信息
 */
public class AppInfo {

    private String AppName;
    private String PackageName;
    private String VersionName;
    private int    VersionCode;
    private long   FirstInstallTime;//首次安装时间
    private long   EffectiveTime;//有效使用时间
    private long   LastUpdateTime;//最后安转时间


    public String getAppName() {
        return AppName == null ? "" : AppName;
    }

    public void setAppName(String appName) {
        AppName = appName;
    }

    public String getPackageName() {
        return PackageName == null ? "" : PackageName;
    }

    public void setPackageName(String packageName) {
        PackageName = packageName;
    }

    public String getVersionName() {
        return VersionName == null ? "" : VersionName;
    }

    public void setVersionName(String versionName) {
        VersionName = versionName;
    }

    public int getVersionCode() {
        return VersionCode;
    }

    public void setVersionCode(int versionCode) {
        VersionCode = versionCode;
    }

    public long getFirstInstallTime() {
        return FirstInstallTime;
    }

    public void setFirstInstallTime(long firstInstallTime) {
        FirstInstallTime = firstInstallTime;
    }

    public long getEffectiveTime() {
        return EffectiveTime;
    }

    public void setEffectiveTime(long effectiveTime) {
        EffectiveTime = effectiveTime;
    }

    public long getLastUpdateTime() {
        return LastUpdateTime;
    }

    public void setLastUpdateTime(long lastUpdateTime) {
        LastUpdateTime = lastUpdateTime;
    }


    @Override
    public String toString() {
        return "AppInfo{" +
                "AppName='" + AppName + '\'' +
                ", PackageName='" + PackageName + '\'' +
                ", VersionName='" + VersionName + '\'' +
                ", VersionCode=" + VersionCode +
                ", FirstInstallTime=" + FirstInstallTime +
                ", EffectiveTime=" + EffectiveTime +
                ", LastUpdateTime=" + LastUpdateTime +
                '}';
    }
}
