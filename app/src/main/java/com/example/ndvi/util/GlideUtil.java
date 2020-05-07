package com.example.ndvi.util;

import android.app.Activity;
import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import baseLibrary.util.StringUtil;

/**
 * @author djh-zy
 * @ClassName: GlideUtil.java
 * @Date 2015年4月24日 上午10:00:30
 * @Description: 异步图片加载显示工具类
 */
public class GlideUtil {


    public static void showImage(Context context, String url, ImageView imageView, int placeholder, boolean isresize) {
        if (! showNullImage(context, url, imageView, placeholder)) return;
        Glide.with(context)
                .asBitmap()
                .load(StringUtil.isImageUrl(url))
                .apply(new RequestOptions()
                        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                        .placeholder(placeholder)
                        .error(placeholder)
                        .centerCrop())
                .into(imageView);
    }


    private static boolean showNullImage(Context context, String url, ImageView imageView, int placeholder) {
        if (context == null || imageView == null)
            return false;
        if (StringUtil.isEmpty(url)) {
            Glide.with(context)
                    .load(placeholder).into(imageView);
            return false;
        }
        return true;
    }

    //不变形
    public static void showNoneImage(Context context, String url, ImageView imageView, int placeholder) {
        if (! showNullImage(context, url, imageView, placeholder)) return;
        Glide.with(context)
                .asBitmap()
                .load(StringUtil.isImageUrl(url))
                .apply(new RequestOptions()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(placeholder)
                        .error(placeholder))
                .into(imageView);
    }

    //不变形
    public static void showNoneImage(Context context, String url, ImageView imageView) {
        Glide.with(context)
                .asBitmap()
                .load(StringUtil.isImageUrl(url))
                .apply(new RequestOptions()
                        .diskCacheStrategy(DiskCacheStrategy.ALL))
                .into(imageView);
    }


    //显示本地图片
    public static void showImageFile(Context context, String filePath, ImageView imageView, int placeholder) {
        if (! showNullImage(context, filePath, imageView, placeholder)) return;
        Glide.with(context)
                .asBitmap()
                .load(filePath)
                .skipMemoryCache(true)
                .apply(new RequestOptions()
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .placeholder(placeholder)
                        .error(placeholder)
                ) //  .centerCrop()
                .into(imageView);
    }

    //显示本地图片
    public static void showImageFile(Context context, String filePath, ImageView imageView, int placeholder, int width, int height) {
        if (! showNullImage(context, filePath, imageView, placeholder)) return;
        Glide.with(context)
                .asBitmap()
                .override(width, height)
                .load(filePath)
                .skipMemoryCache(true)
                .apply(new RequestOptions()
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .placeholder(placeholder)
                        .error(placeholder)
                ) //  .centerCrop()
                .into(imageView);
    }

    //显示Gif动画
    public static void showGifImage(Activity activity, String url, ImageView image, int placeholder) {
        if (! showNullImage(activity, url, image, placeholder)) return;
        Glide.with(activity)
                .load(StringUtil.isImageUrl(url))
                .apply(new RequestOptions()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(placeholder)
                        .error(placeholder))
                .into(image);
    }

}


