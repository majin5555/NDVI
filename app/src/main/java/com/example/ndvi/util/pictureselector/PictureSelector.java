package com.example.ndvi.util.pictureselector;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.Fragment;

import java.lang.ref.WeakReference;

/**
 * Author       majin
 */
public class PictureSelector {

    public static final int    SELECT_REQUEST_CODE = 1000;//选择图片请求码
    public static final String PICTURE_MSG         = "image_msg";//图片信息

    private       int                     mRequestCode;
    private final WeakReference<Activity> mActivity;
    private final WeakReference<Fragment> mFragment;

    public static PictureSelector create(Activity activity, int requestCode) {
        return new PictureSelector(activity, requestCode);
    }

    public static PictureSelector create(Fragment fragment, int requestCode) {
        return new PictureSelector(fragment, requestCode);
    }

    private PictureSelector(Activity activity, int requestCode) {
        this(activity, (Fragment) null, requestCode);
    }

    private PictureSelector(Fragment fragment, int requestCode) {
        this(fragment.getActivity(), fragment, requestCode);
    }

    private PictureSelector(Activity activity, Fragment fragment, int requestCode) {
        this.mActivity = new WeakReference(activity);
        this.mFragment = new WeakReference(fragment);
        this.mRequestCode = requestCode;
    }

    /**
     * 选择图片
     */
    public void selectPicture() {
        Activity activity = this.mActivity.get();
        Fragment fragment = this.mFragment.get();
        Intent intent = new Intent(activity, PictureSelectActivity.class);

        if (fragment != null) {
            fragment.startActivityForResult(intent, mRequestCode);
        } else {
            activity.startActivityForResult(intent, mRequestCode);
        }
    }
}

