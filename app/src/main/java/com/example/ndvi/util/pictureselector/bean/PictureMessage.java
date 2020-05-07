package com.example.ndvi.util.pictureselector.bean;

import java.io.Serializable;

/**
 * @author: mj
 * @date: 2019/6/29$
 * @desc:
 */
public class PictureMessage implements Serializable {

    private String basePath;//原始图片（用于获取图片信息）
    private String showPath;//展示图片的地址

    private boolean isCrop;//true 裁剪  false 不猜

    private String picTime = "";
    private double longitude;//经度
    private double latitude;//纬度

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }


    public String getPicTime() {
        return picTime == null ? "" : picTime;
    }

    public void setPicTime(String picTime) {
        this.picTime = picTime;
    }


    public String getBasePath() {
        return basePath == null ? "" : basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public String getShowPath() {
        return showPath == null ? "" : showPath;
    }

    public void setShowPath(String showPath) {
        this.showPath = showPath;
    }

    public boolean isCrop() {
        return isCrop;
    }

    public void setCrop(boolean crop) {
        isCrop = crop;
    }

}
