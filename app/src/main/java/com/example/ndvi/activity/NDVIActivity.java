package com.example.ndvi.activity;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.example.ndvi.R;
import com.example.ndvi.control.AppSet;
import com.example.ndvi.customview.RegionNumberEditText;
import com.example.ndvi.util.GlideUtil;
import com.example.ndvi.util.pictureselector.PictureSelector;
import com.example.ndvi.util.pictureselector.bean.PictureMessage;
import com.orhanobut.logger.Logger;

import org.opencv.core.CvException;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.TermCriteria;
import org.opencv.ml.EM;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;

import baseLibrary.activity.ActivityUtil;
import baseLibrary.activity.BaseActivity;
import baseLibrary.toast.ToastUtil;
import baseLibrary.util.FileUtil;
import baseLibrary.util.ImageUtils;
import baseLibrary.util.NDVIJumpUtil;

import static com.example.ndvi.util.pictureselector.PictureSelectActivity.mSelectTime;


/**
 * 主页
 */
public class NDVIActivity extends BaseActivity implements View.OnClickListener, View.OnLongClickListener {

    private final String        TAG = "NDVIActivity";
    /** 设置宽高限制 */

    private       DecimalFormat df  = new DecimalFormat("0.00%");
    private       ImageView     imageViewOld, imageViewNew, imageViewNew2;

    /** 手动输入 */
    private RegionNumberEditText editText;

    /** 选择原图的Bitmap */
    private Bitmap mBasebitmap;
    /** NDVI数据源 */
    private double mNDVI[] = null;

    /** 图片，TXT文档保存地址 */
    private String path;
    /** button 生成NDVI  button2 生成NDVI植被覆盖  button3 导出TXT */
    private Button button, button2, button3;
    /** 原始图片 宽高 */
    private int mBitmapWidth = 0, mBitmapHeight = 0;
    /** 导出TXT成功标志 */
    private boolean        writeTxt          = false;
    /** NDVI图片存放地址 */
    private String         mNDVIpath         = "";
    /** NDVI覆盖度图片存放地址 */
    private String         mNDVIpathCoverage = "";
    /** 选择原图信息对象 */
    private PictureMessage message;
    /** 自动计算开关 */
    private Switch         mSwitchCrop;
    /** 自动计算标记 */
    private boolean        mAuto             = true;
    /** 显示NDVI图片 */
    private boolean        mShowNDVI         = false;
    /** 输出文本的线程 */
    private Thread         threadWriteToTxt;
    /** 手动输入的Editext父布局 */
    private RelativeLayout mInputRoot;
    /** 输出流 */
    private BufferedWriter mBwTxt;
    /** 自动手动计算 */
    private TextView       mTvAuto;
    /** 生成NDVI图片数据源 */
    private int[]          pixels;
    private Mat            labels;

    double maxValue = - 1;
    double minValue = 256;

    public static final String      ACTION_USB_PERMISSION = "com.example.ndvi.activity.USB_PERMISSION";
    /** USB 广播监听 */
    private             USBReceiver mUsbReceiver;

    public void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);//动作USB设备已连接 广播
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);//动作USB设备已连接 广播
        filter.addAction(UsbManager.ACTION_USB_ACCESSORY_ATTACHED);//动作USB设备已连接 广播
        filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);//动作USB设备已连接 广播
        filter.addAction("android.hardware.usb.action.USB_STATE");
        // filter.addAction(ACTION_USB_PERMISSION); //自定义广播
        mUsbReceiver = new USBReceiver();
        this.registerReceiver(mUsbReceiver, filter);
    }

    private class USBReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // 这里可以拿到插入的USB设备对象
            UsbDevice usbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

            if (intent.getAction() == null)
                return;
            switch (intent.getAction()) {

                case ACTION_USB_PERMISSION:

                    //允许权限申请
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (usbDevice != null) {
                            ToastUtil.showToast(NDVIActivity.this, "USB设备权限已获取");

                        } else {
                            ToastUtil.showToast(NDVIActivity.this, "没有插入USB设备");
                        }
                    } else {
                        ToastUtil.showToast(NDVIActivity.this, "未获取到USB设备权限");
                    }
                    break;
                case UsbManager.ACTION_USB_DEVICE_ATTACHED: // 插入USB设备

                    break;
                case UsbManager.ACTION_USB_DEVICE_DETACHED: // 拔出USB设备
                    closeApp();
                    break;
                default:
                    break;
            }
        }

    }

    private void closeApp() {
        Logger.d("NDVIActivity close");
        //退出程序
        new Handler().postDelayed(() -> {
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(0);
        }, 200);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerReceiver();
        checkPermission();
        this.setTitle("植被盖度测量系统");
        path = getSDPath() + "/NDVI/";
    }

    @Override
    public void initView() {
        setContentView(R.layout.activity_ndvi);

        mTvAuto = getView(R.id.tv_auto);
        imageViewOld = getView(R.id.ivold);
        imageViewOld.setOnClickListener(this);
        imageViewOld.setOnLongClickListener(this);
        imageViewNew = getView(R.id.ivnew);
        imageViewNew.setOnLongClickListener(this);
        imageViewNew2 = getView(R.id.ivnew2);
        imageViewNew2.setOnLongClickListener(this);
        editText = getView(R.id.edi);
        editText.setRegion(1, - 1);
        editText.setTextWatcher();
        button = getView(R.id.button);
        button.setOnClickListener(this);

        button2 = getView(R.id.button2);
        button2.setOnClickListener(this);
        button3 = getView(R.id.button3);
        button3.setOnClickListener(this);

        mSwitchCrop = findViewById(R.id.switchCrop);
        mInputRoot = findViewById(R.id.inputroot);
        mSwitchCrop.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if (isChecked) {
                mTvAuto.setText("自动计算");
                mInputRoot.setVisibility(View.INVISIBLE);
            } else {
                mTvAuto.setText("手动计算");
                mInputRoot.setVisibility(View.VISIBLE);
            }
            mAuto = isChecked;
        });
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            /**选择相册 与裁剪选择*/
            case R.id.ivold:
                PictureSelector.create(NDVIActivity.this, PictureSelector.SELECT_REQUEST_CODE).selectPicture();
                break;
            /**生成NDVI*/
            case R.id.button:
                try {
                    if (mBwTxt != null) {
                        mBwTxt.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (threadWriteToTxt != null) {
                    threadWriteToTxt = null;
                }
                /**计算NDVI数据*/
                new Thread(this::makeNDVIData).start();
                break;
            /**生成NDVI该盖度图*/
            case R.id.button2:
                new Thread(this::makeNDVIVegetationData).start();
                break;
            /**生成TXT文档*/
            case R.id.button3:
                makeTxtData();
                break;
            default:
                break;
        }
    }

    /** 查看图片详情 */
    @Override
    public boolean onLongClick(View v) {
        Bundle bundle = new Bundle();
        switch (v.getId()) {
            /**原图片*/
            case R.id.ivold:
                String path = "";
                if (message != null) {
                    if (message.isCrop()) {
                        path = message.getShowPath();
                    } else {
                        path = message.getBasePath();
                    }
                    bundle.putString("PICPATH", path);
                    NDVIJumpUtil.getInstance().startBaseActivity(context, PictureDetailsActivity.class, bundle, 0);

                }
                break;
            /**NDVI图片*/
            case R.id.ivnew:
                if (! mNDVIpath.equals("")) {
                    bundle.putString("PICPATH", mNDVIpath);
                    NDVIJumpUtil.getInstance().startBaseActivity(context, PictureDetailsActivity.class, bundle, 0);
                }
                break;
            /**NDVI图片覆盖图*/
            case R.id.ivnew2:
                if (! mNDVIpathCoverage.equals("")) {
                    bundle.putString("PICPATH", mNDVIpathCoverage);
                    NDVIJumpUtil.getInstance().startBaseActivity(context, PictureDetailsActivity.class, bundle, 0);
                }
                break;
            default:
                break;
        }
        return false;
    }

    /** 生成NDVI */
    private void makeNDVIData() {
        try {
            if (mBasebitmap == null && ! mShowNDVI) {
                showToast("请添加图片...");
                return;
            } else if (mShowNDVI) {
                showToast("NDVI图片已生成,请重新添加图片...");
            } else {
                runOnUiThread(this::showDialog);
                Logger.d("makeNDVI start  --  图片大小：mBitmapWidth=" + mBasebitmap.getWidth() + ",mBitmapHeight=" + mBasebitmap.getHeight() + ",mArrayColorLengh=" + mBasebitmap.getWidth() * mBasebitmap.getHeight());
                mBitmapWidth = mBasebitmap.getWidth();//pic.cols()
                mBitmapHeight = mBasebitmap.getHeight(); //pic.rows()

                mNDVI = new double[mBitmapWidth * mBitmapHeight];
                pixels = new int[mBitmapWidth * mBitmapHeight];//保存NDVI的图片数据的容器
                int count = 0;
                for (int i = 0; i < mBitmapHeight; i++) {
                    for (int j = 0; j < mBitmapWidth; j++) {

                        /**获得Bitmap 图片中每一个点的color颜色值*/
                        int color = mBasebitmap.getPixel(j, i);
                        /**取出各点的 R , G, B*/
                        double r = Color.red(color);
                        double nir = Color.blue(color);

                        double NDVI = ((nir - r) / (nir + r));
                        if (Double.isNaN(NDVI)) {
                            NDVI = - 1;
                        }
                        /**TXT文档数据源*/
                        mNDVI[count] = NDVI;
                        //   Log.d("NDVI", " NDVI    " + NDVI);
                        /**获取最大最小值*/
                        if (NDVI >= maxValue) {
                            maxValue = NDVI;
                        } else if ((NDVI < minValue)) {
                            minValue = NDVI;
                        }
                        count++;
                    }
                }
                Logger.d("maxValue   " + maxValue + "   minValue-------" + minValue);
                /**(0-255之间)图片灰度化 黑白图 去除黑色的部分*/
                for (int i = 0; i < mNDVI.length; i++) { //(mdvi-min)/(max-min)=(n-min)/(max-min)
                    /**NDVI*/
                    int newRGBColor = (int) (((mNDVI[i] - minValue) * 255) / (maxValue - minValue));
                    pixels[i] = Color.rgb(newRGBColor, newRGBColor, newRGBColor);
                }
                /**生成黑白图*/
                mBasebitmap = Bitmap.createBitmap(pixels, mBitmapWidth, mBitmapHeight, Bitmap.Config.RGB_565);
                /**保存NDVI*/
                if (mBasebitmap != null) {
                    savePhotoToSDCard(mBasebitmap, path, getNDVIname());
                    mNDVIpath = path + getNDVIname();
                }
                /**刷新UI*/
                runOnUiThread(() -> {
                    GlideUtil.showImageFile(NDVIActivity.this, mNDVIpath, imageViewNew, R.drawable.default_null, mBitmapWidth, mBitmapHeight);
                    writeTxt = false;
                    mShowNDVI = true;
                    dismissDialog();
                    Logger.d("makeNDVI end --- 图片保存成功并显示");
                });

            }
        } catch (OutOfMemoryError outOfMemoryError) {
            mBasebitmap = null;
            mShowNDVI = false;
            Logger.d("outOfMemoryError" + outOfMemoryError.getMessage());
            showToast("图片过大，生成NDVI图片失败,请重试...");
            runOnUiThread(() -> {
                imageViewOld.setImageResource(R.drawable.ico_add);
                dismissDialog();
            });
        }
    }

    /** 生成盖度图 */
    private void makeNDVIVegetationData() {
        try {
            /**必须是NDVI图片生成后 才能生成黑绿图*/
            if (mShowNDVI) {
                /**自动计算*/
                if (mAuto) {
                    runOnUiThread(this::showDialog);
                    Logger.d("------------------------------NDVIVEGEVTTION start");
                    Mat mMatData = new Mat();
                    /**加载灰度图（NDVI的黑白图）*/
                    Logger.d("------------------------------植被覆盖数据源 start"); /*覆盖度的数据源 start*/
                    for (int i = 0; i < mNDVI.length; i++) {
                        Mat mMat = new Mat(1, 1, CvType.CV_32FC1);
                        mMat.put(0, 0, mNDVI[i]);
                        mMatData.push_back(mMat);
                    }
                    Logger.d("------------------------------植被覆盖数据源 end");
                    Logger.d("------------------------------EM start0");
                    /**EM算法聚类分类*/
                    labels = new Mat();
                    EM em_model = EM.create();
                    em_model.setClustersNumber(2);
                    em_model.setCovarianceMatrixType(EM.COV_MAT_SPHERICAL);
                    em_model.setTermCriteria(new TermCriteria(TermCriteria.COUNT + TermCriteria.EPS, 100, 0.1));
                    em_model.trainEM(mMatData, new Mat(), labels, new Mat());
                    Logger.d("------------------------------EM end0");

                    double total0 = 0;
                    int length0 = 0;
                    double total1 = 0;
                    int length1 = 0;

                    for (int i = 0; i < mNDVI.length; i++) {
                        /*分类标识*/
                        int clusterIdx = (int) labels.get(i, 0)[0];
                        if (clusterIdx == 0) {
                            total0 += mNDVI[i];
                            length0++;
                        } else {
                            total1 += mNDVI[i];
                            length1++;
                        }
                    }

                    Logger.d(total0 + "   " + length0 + "   " + total1 + "   " + length1);

                    double mGreenIndex;
                    if (total0 / length0 >= total1 / length1) {
                        mGreenIndex = 0;
                    } else {
                        mGreenIndex = 1;
                    }
                    calculatingCoverage(mGreenIndex);
                    labels.release();
                    mMatData.release();
                } else { /**手动计算*/
                    runOnUiThread(this::showDialog);
                    /**获取输入值*/
                    if (editText.getText().toString().equals("")) {
                        editText.setText("0");
                    }
                    double mInputValue = Double.parseDouble(editText.getText().toString());
                    calculatingCoverage(mInputValue);
                }
            } else {
                showToast("请先生成NDVI图片...");
            }
        } catch (OutOfMemoryError outOfMemoryError) {
            outOfMemoryError.printStackTrace();
            Logger.d("------------------------------outOfMemoryError  " + outOfMemoryError.getMessage());
            showToast("图片过大，生成植被覆盖图片失败...");
        } catch (
                CvException cv) {
            Logger.d("计算发生错误, CvException " + cv.getMessage());
            showToast("计算发生错误," + cv.getMessage());
        } catch (Exception e) {
            Logger.d("计算发生错误, Exception " + e.getMessage());
            showToast("计算发生错误," + e.getMessage());
        }
    }

    /** 计算盖度 */
    private void calculatingCoverage(double threshold) {
        String format = "";
        /**所有颜色的数量*/
        double mGreenCount = 0;//绿色像素点的数量

        if (mAuto) {
            for (int i = 0; i < mNDVI.length; i++) {
                int clusterIdx = (int) labels.get(i, 0)[0];
                if (clusterIdx == threshold) {
                    /**0 是绿色*/
                    pixels[i] = Color.rgb(0, 255, 0);
                    mGreenCount++;
                } else {
                    /**1 是灰黑色*/
                    pixels[i] = Color.rgb(50, 50, 50);
                }
            }
        } else {
            for (int i = 0; i < mNDVI.length; i++) {
                if (mNDVI[i] > threshold) {
                    /**绿色*/
                    pixels[i] = Color.rgb(0, 255, 0);
                    mGreenCount++;
                } else {
                    /**灰黑色*/
                    pixels[i] = Color.rgb(50, 50, 50);
                }
            }
        }
        Logger.d("mGreenCount  " + mGreenCount + " all mPicCount---" + mNDVI.length);
        /**计算植被覆盖度*/
        format = df.format(mGreenCount / mNDVI.length);
        Bitmap mBitmap = Bitmap.createBitmap(pixels, mBitmapWidth, mBitmapHeight, Bitmap.Config.RGB_565);
        /**生成水印*/
        Bitmap bmpWater = ImageUtils.drawTextToRightBottom(NDVIActivity.this, mBitmap, "盖度:" + format + " 经:" + String.format("%.6f", message.getLongitude()) + " 纬:" + String.format("%.6f", message.getLatitude()) + " 时间:" + message.getPicTime(), mBitmap.getWidth() / 40, 10, 10);
        if (bmpWater != null) {
            String mName;
            if (mAuto) {
                mName = getNDVInameAutoNDVIVegetation();
            } else {
                mName = getNDVInameManualNDVIVegetation();
            }
            savePhotoToSDCard(bmpWater, path, mName);
            mNDVIpathCoverage = path + mName;
        }
        runOnUiThread(() -> {
            GlideUtil.showImageFile(NDVIActivity.this, mNDVIpathCoverage, imageViewNew2, R.drawable.default_null, mBitmapWidth, mBitmapHeight);
            dismissDialog();
            Logger.d("------------------------------NDVIVEGEVTTION end");
        });
    }

    /** NDVI图片名称 */
    private String getNDVIname() {
        return "NDVI_Gray" + mSelectTime + ".jpg";
    }

    /** 植被覆盖图片名称 自动 */
    private String getNDVInameAutoNDVIVegetation() {
        return "NDVI_Cov_Auto" + mSelectTime + ".jpg";
    }

    /** 植被覆盖图片名称 手动 */
    private String getNDVInameManualNDVIVegetation() {
        return "NDVI_Cov_Manual" + mSelectTime + ".jpg";
    }

    /** RGB文本 */
    private String getRGBTXT() {
        return "RGBColor" + mSelectTime + ".txt";
    }

    /** 到处txt文档 */
    private void makeTxtData() {
        if (mNDVI != null) {

            if (! writeTxt) {
                /**存入TXT*/
                if (threadWriteToTxt == null) {
                    dismissDialog();
                    button3.setClickable(false);
                    showDialog("TXT生成中，请稍等...");
                    threadWriteToTxt = new Thread(() -> {
                        writeTxt = false;
                        FileUtil.makeFilePath(path + "TXT/", getRGBTXT());
                        Logger.d("写入txt start   ");

                        try {
                            //创建一个字符缓冲输出流对象
                            mBwTxt = new BufferedWriter(new FileWriter(path + "TXT/" + getRGBTXT()));
                            //写数据
                            mBwTxt.write("Width:" + mBitmapWidth);
                            mBwTxt.write("Height:" + mBitmapHeight);
                            mBwTxt.write('\n');
                            for (int i = 0; i < mNDVI.length; i++) {
                                mBwTxt.write(String.valueOf(mNDVI[i]));
                                mBwTxt.write('\n');
                            }
                            //刷新流
                            mBwTxt.flush();
                            mBwTxt.close();

                            runOnUiThread(() -> {
                                button3.setClickable(true);
                                dismissDialog();
                                writeTxt = true;
                                Logger.d("写入txt end   ");
                            });
                        } catch (IOException e) {
                            e.printStackTrace();
                            runOnUiThread(() -> {
                                ToastUtil.showToast(NDVIActivity.this, "生成文档发生错误，请重试...");
                                dismissDialog();
                                writeTxt = false;
                                threadWriteToTxt = null;
                                button3.setClickable(true);
                                Logger.d("写入txt  异常  " + e.getMessage());
                            });
                        }
                    });
                    threadWriteToTxt.start();
                } else {
                    showToast("请生成NDVI图片");
                }
            } else {
                showToast("TXT已经生成");
            }
        } else {
            showToast("请添加图片");

        }
    }


    /**
     * 获取文件路径
     */
    public String getSDPath() {
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState()
                .equals(Environment.MEDIA_MOUNTED); //判断sd卡是否存在
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory();//获取跟目录
        }
        return sdDir.toString();
    }

    /**
     * Save image to the SD card
     */
    private void savePhotoToSDCard(Bitmap photoBitmap, String dirPath, String photoName) {

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
        Logger.d("保存图片到路径：---" + dirPath + " <photoName> " + photoName);

        Uri uri = null;
        ContentValues contentValues = new ContentValues();
        //设置文件名
        contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, photoName);
        //安卓系统环境版本在29以上时：
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/Pictures");
        }else {
            contentValues.put(MediaStore.Images.Media.DATA, dirPath);
        }
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/JPEG");
        uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        //最后通知图库更新
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);//扫描单个文件
        intent.setData(uri);//给图片的绝对路径
        this.sendBroadcast(intent);

        runOnUiThread(() -> ToastUtil.showToast(NDVIActivity.this, R.string.pic_save));
        return;
    }





    /**
     * 添加基础展示图片
     */
    private void addBasePic() {
        runOnUiThread(() -> {
            if (message.isCrop()) {
                Bitmap mBitmap = BitmapFactory.decodeFile(message.getShowPath());
                if (mBitmap.getWidth() > AppSet.WIDTH) {
                    int h0 = mBitmap.getHeight() * 1024 / mBitmap.getWidth();
                    mBasebitmap = Bitmap.createScaledBitmap(mBitmap, 1024, h0, true);
                } else {
                    mBasebitmap = mBitmap;
                }
                GlideUtil.showImageFile(NDVIActivity.this, message.getShowPath(), imageViewOld, R.drawable.ico_add, mBasebitmap.getWidth(), mBasebitmap.getHeight());

            } else {
                Logger.d("message.getBasePath()  "+message.getBasePath());
                Bitmap mBitmap = BitmapFactory.decodeFile(message.getBasePath());
                if (mBitmap.getWidth() > AppSet.WIDTH) {
                    int h0 = mBitmap.getHeight() * 1024 / mBitmap.getWidth();
                    mBasebitmap = Bitmap.createScaledBitmap(mBitmap, 1024, h0, true);
                } else {
                    mBasebitmap = mBitmap;
                }
                GlideUtil.showImageFile(NDVIActivity.this, message.getBasePath(), imageViewOld, R.drawable.ico_add, mBasebitmap.getWidth(), mBasebitmap.getHeight());
            }
            mShowNDVI = false;
            button.setClickable(true);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data1) {
        super.onActivityResult(requestCode, resultCode, data1);

        switch (requestCode) {
            case PictureSelector.SELECT_REQUEST_CODE:
                /*结果回调*/
                if (data1 != null) {
                    //   Logger.d("SELECT_REQUEST_CODE  start");
                    showDialog("请稍等...");
                    mBasebitmap = null;
                    button.setClickable(false);
                    message = (PictureMessage) data1.getSerializableExtra(PictureSelector.PICTURE_MSG);
                    addBasePic();
                    imageViewNew.setImageResource(R.drawable.default_null);
                    imageViewNew2.setImageResource(R.drawable.default_null);
                    dismissDialog();
                    //   Logger.d("SELECT_REQUEST_CODE  end");
                }
            default:
                break;
        }

    }

    /**
     * activity的回退事件
     */
    @Override
    public void onBackPressed() {
        if (! isExit) {
            isExit = true;
            ToastUtil.showToast(context, R.string.angin_back);
            mBasehandler.sendEmptyMessageDelayed(exitApp, 3000);
            Logger.d("System.exit(0)----1");
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CUPCAKE) {
                ActivityUtil.getInstance().AppExit(this);
            }
            Logger.d("System.exit(0)----2");
        }
    }

    /**
     * Drawable转换成一个Bitmap
     *
     * @param drawable drawable对象
     * @return
     */
    private Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap;
        bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(),
                drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    /** 等比缩放图片 */
    private Bitmap zoomImg(Bitmap bmp, int newWidth, int newHeight) {
        // 获得图片的宽高
        int width = bmp.getWidth();
        int height = bmp.getHeight();
        // 计算缩放比例
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // 取得想要缩放的matrix参数
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        // 得到新的图片
        return Bitmap.createBitmap(bmp, 0, 0, width, height, matrix, true);
    }


}


