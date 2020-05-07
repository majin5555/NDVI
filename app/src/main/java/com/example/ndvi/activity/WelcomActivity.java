package com.example.ndvi.activity;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.baselibrary.R;
import com.example.ndvi.libaums.UsbMassStorageDevice;
import com.example.ndvi.libaums.fs.FileSystem;
import com.example.ndvi.libaums.fs.UsbFile;
import com.example.ndvi.libaums.fs.UsbFileInputStream;
import com.example.ndvi.libaums.partition.Partition;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;

import baseLibrary.activity.BaseActivity;
import baseLibrary.toast.ToastUtil;
import baseLibrary.util.NDVIJumpUtil;


/** 欢迎页面 */
public class WelcomActivity extends BaseActivity {
    protected final int goMain = - 1;

    protected final int EQUALS = - 2;

    private final String TAG = "WelcomActivity";

    private              UsbMassStorageDevice[] storageDevices;
    private              UsbFile                cFolder;
    //自定义U盘读写权限
    public static final  String                 ACTION_USB_PERMISSION = "com.example.ndvi.activity.USB_PERMISSION";
    private final static String                 U_DISK_FILE_NAME      = "pass.txt";
    private              StringBuffer           stringBuffer          = new StringBuffer();

    /** USB 广播监听 */
    private USBReceiver mUsbReceiver;

    @SuppressLint("HandlerLeak")
    protected Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case goMain:
                    //                                        registerReceiver();
                    //                                        redUDiskDevsList();
                    NDVIJumpUtil.getInstance().startBaseActivity(context, NDVIActivity.class);
                    finishActivity();
                    break;
                case EQUALS:
                    //                    String str = (String) msg.obj;
                    //                    if (str.equals(USEKAY)) {
                    //                        NDVIJumpUtil.getInstance().startBaseActivity(context, NDVIActivity.class);
                    //                        finishActivity();
                    //                    } else {
                    //                        finishActivity();
                    //                    }
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcom);

        ImageView im = new ImageView(this);
        im.setScaleType(ImageView.ScaleType.CENTER_CROP);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT
                , ViewGroup.LayoutParams.MATCH_PARENT);
        im.setLayoutParams(params);
        im.setBackgroundResource(R.drawable.welcome);
        setContentView(im);
        mHandler.sendEmptyMessageDelayed(goMain, 3000);
    }

    public void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);//动作USB设备已连接 广播
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);//动作USB设备已连接 广播
        filter.addAction(UsbManager.ACTION_USB_ACCESSORY_ATTACHED);//动作USB设备已连接 广播
        filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);//动作USB设备已连接 广播
        filter.addAction("android.hardware.usb.action.USB_STATE");
        filter.addAction(ACTION_USB_PERMISSION); //自定义广播
        mUsbReceiver = new USBReceiver();
        this.registerReceiver(mUsbReceiver, filter);
    }

    @Override
    public void initView() {

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
                            ToastUtil.showToast(WelcomActivity.this, "USB设备权限已获取");
                            redUDiskDevsList();
                        } else {
                            ToastUtil.showToast(WelcomActivity.this, "没有插入USB设备");
                        }
                    } else {
                        ToastUtil.showToast(WelcomActivity.this, "未获取到USB设备权限");
                    }
                    break;
                case UsbManager.ACTION_USB_DEVICE_ATTACHED: // 插入USB设备
                    ToastUtil.showToast(WelcomActivity.this, "插入USB设备");
                    //接收到U盘插入广播，尝试读取U盘设备数据
                    redUDiskDevsList();
                    break;
                case UsbManager.ACTION_USB_DEVICE_DETACHED: // 拔出USB设备
                    ToastUtil.showToast(WelcomActivity.this, "拔出USB设备");
                    break;
                default:
                    //    ToastUtil.showToast(NDVIActivity.this, "action= " + intent.getAction());
                    break;
            }
        }

    }

    /**
     * @description U盘设备读取
     * @author majjin
     * @time 2019/8/5 17:20
     */
    private void redUDiskDevsList() {
        //设备管理器
        UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        //获取U盘存储设备
        storageDevices = UsbMassStorageDevice.getMassStorageDevices(this);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        //一般手机只有1个OTG插口
        for (UsbMassStorageDevice device : storageDevices) {
            //读取设备是否有权限
            if (usbManager.hasPermission(device.getUsbDevice())) {
                ToastUtil.showToast(WelcomActivity.this, "有权限");
                readDevice(device);

                HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
                Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();

                while (deviceIterator.hasNext()) {
                    UsbDevice device1 = deviceIterator.next();
                    ToastUtil.showToast(WelcomActivity.this, "device-----" + device1.getProductId());
                }


                //                StorageManager storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
                //                try {
                //                    Class storeManagerClazz = Class.forName("android.os.storage.StorageManager");
                //
                //                    Method getVolumesMethod = storeManagerClazz.getMethod("getVolumes");
                //
                //                    List<?> volumeInfos = (List<?>) getVolumesMethod.invoke(storageManager);
                //
                //                    Class volumeInfoClazz = Class.forName("android.os.storage.VolumeInfo");
                //
                //                    Method getTypeMethod = volumeInfoClazz.getMethod("getType");
                //                    Method getFsUuidMethod = volumeInfoClazz.getMethod("getFsUuid");
                //                    ToastUtil.showToast(WelcomActivity.this, " getFsUuidMethod getName----" + getFsUuidMethod.getName());
                //
                //
                //                    Field fsTypeField = volumeInfoClazz.getDeclaredField("fsType");
                //                    Field fsLabelField = volumeInfoClazz.getDeclaredField("fsLabel");
                //                    Field pathField = volumeInfoClazz.getDeclaredField("path");
                //                    Field internalPath = volumeInfoClazz.getDeclaredField("internalPath");
                //
                //                    if (volumeInfos != null) {
                //                        for (Object volumeInfo : volumeInfos) {
                //                            String uuid = (String) getFsUuidMethod.invoke(volumeInfo);
                //                           // ToastUtil.showToast(WelcomActivity.this, " uuid" + uuid);
                //
                //                            if (uuid != null) {
                //                                String fsTypeString = (String) fsTypeField.get(volumeInfo);//U盘类型
                //                                String fsLabelString = (String) fsLabelField.get(volumeInfo);//U盘名称
                //                                String pathString = (String) pathField.get(volumeInfo);//U盘路径
                //                                String internalPathString = (String) internalPath.get(volumeInfo);//U盘路径
                //                                StatFs statFs = new StatFs(internalPathString);
                //                                long avaibleSize = statFs.getAvailableBytes();
                //                                long totalSize = statFs.getTotalBytes();
                //                                ToastUtil.showToast(WelcomActivity.this, "U盘类型" + fsTypeString + " U盘名称" + fsLabelString + " U盘路径" + pathString + " uuid" + uuid);
                //                            }
                //                        }
                //                    }
                //                } catch (Exception e) {
                //                    e.printStackTrace();
                //                }

            } else {
                ToastUtil.showToast(WelcomActivity.this, "没有权限，进行申请");
                //没有权限，进行申请
                usbManager.requestPermission(device.getUsbDevice(), pendingIntent);
            }
        }
        if (storageDevices.length == 0) {
            ToastUtil.showToast(WelcomActivity.this, "请插入可用的USB设备");
        }

    }


    private void readDevice(UsbMassStorageDevice device) {
        try {
            device.init();//初始化
            //设备分区
            Partition partition = device.getPartitions().get(0);
            String volumeLabel1 = partition.getVolumeLabel();
            ToastUtil.showToast(WelcomActivity.this, "获取到设备的标识" + volumeLabel1);

            //文件系统
            FileSystem currentFs = partition.getFileSystem();
            String volumeLabel = currentFs.getVolumeLabel();//可以获取到设备的标识

            // ToastUtil.showToast(WelcomActivity.this, "获取到ACTION_MEDIA_MOUNTED设备的标识" + volumeLabel + "   currentFs.getType() " + currentFs.getType());
            //通过FileSystem可以获取当前U盘的一些存储信息，包括剩余空间大小，容量等等
            Log.e("Capacity: ", currentFs.getCapacity() + "");
            Log.e("Occupied Space: ", currentFs.getOccupiedSpace() + "");
            Log.e("Free Space: ", currentFs.getFreeSpace() + "");
            Log.e("Chunk size: ", currentFs.getChunkSize() + "");

            currentFs.getType();
            //   ToastUtil.showToast(WelcomActivity.this, "可用空间：" + currentFs.getFreeSpace());
            cFolder = currentFs.getRootDirectory();//设置当前文件对象为根目录
            readFromUDisk();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void readFromUDisk() {
        UsbFile[] usbFiles = new UsbFile[0];
        try {
            usbFiles = cFolder.listFiles();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (null != usbFiles && usbFiles.length > 0) {

            for (UsbFile usbFile : usbFiles) {
                stringBuffer.append(", " + usbFile.getName());
                if (usbFile.getName().equals(U_DISK_FILE_NAME)) {
                    readTxtFromUDisk(usbFile);
                }
            }
        }
    }

    private void readTxtFromUDisk(UsbFile usbFile) {
        UsbFile descFile = usbFile;
        //读取文件内容
        InputStream is = new UsbFileInputStream(descFile);
        //读取秘钥中的数据进行匹配
        StringBuilder sb = new StringBuilder();
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(is));
            String read;
            while ((read = bufferedReader.readLine()) != null) {
                sb.append(read);
            }
            Message msg = Message.obtain();
            msg.what = EQUALS;
            msg.obj = sb.toString();
            mHandler.sendMessage(msg);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // unregisterReceiver();
    }

    public void unregisterReceiver() {
        this.unregisterReceiver(mUsbReceiver);
    }

}
