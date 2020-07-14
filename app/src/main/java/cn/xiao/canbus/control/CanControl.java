package cn.xiao.canbus.control;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.util.Log;
import android.widget.Toast;

import cn.xiao.canbus.R;
import cn.xiao.canbus.activity.CanBaseActivity;
import cn.xiao.canbus.application.MyApp;
import cn.xiao.canbus.handler.DriverHandler;
import cn.xiao.canbus.util.Common;
import cn.xiao.canbus.util.CommonMethod;

public class CanControl {
    private static final String TAG = "CanControl";

    private static CanControl mCanControl;
    private DriverHandler mDriverHandler;
    private CanBaseActivity mCanBaseActivity;
    private boolean driverRunningState = false;

    public static int baudRate = 460800;
    public static byte dataBit = 8;
    public static byte stopBit = 1;
    public static byte parity = 0;
    public static byte flowControl = 0;

    private CanControl() {
    }

    public static CanControl getInstance() {
        if (mCanControl == null) {
            mCanControl = new CanControl();
        }
        return mCanControl;
    }

    public void setMainActivity(CanBaseActivity canBaseActivity) {
        this.mCanBaseActivity = canBaseActivity;
    }

    public void setHandler(DriverHandler driverHandler) {
        this.mDriverHandler = driverHandler;
    }

    public void checkUsbHostAvailable() {
        // 判断系统是否支持USB HOST
        if (!MyApp.driver.UsbFeatureSupported()) {
            if (mCanBaseActivity != null) {
                Dialog dialog = new AlertDialog.Builder(mCanBaseActivity)
                        .setTitle("提示")
                        .setMessage("您的手机不支持USB HOST，请更换其他手机再试！")
                        .setPositiveButton("确认",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface arg0, int arg1) {
                                        System.exit(0);
                                    }
                                }).create();
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();
            } else {
                Log.d(TAG, "您的手机不支持USB HOST，请更换其他手机再试!");
                System.exit(0);
            }

        }
    }

    public void resumeUsbPermission() {
        if (!MyApp.driver.isConnected()) {
            int retval = MyApp.driver.ResumeUsbPermission();
            if (retval == 0) {

            } else if (retval == -2) {
                if (mCanBaseActivity != null) {
                    Toast.makeText(mCanBaseActivity, "获取权限失败!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public void setCanConfig(int baudRate, byte dataBit, byte stopBit, byte parity, byte flowControl) {
        if (MyApp.driver.SetConfig(baudRate, dataBit, stopBit, parity,//配置串口波特率，函数说明可参照编程手册
                flowControl)) {
            Log.d(TAG, "串口设置成功!");
        } else {
            Log.d(TAG, "串口设置失败!");
        }
    }

    public void setDriverState(boolean driverState) {
        if (driverState && !driverRunningState) {
            int retval = MyApp.driver.ResumeUsbList();
            // ResumeUsbList方法用于枚举CH34X设备以及打开相关设备
            if (Common.DRIVER_STATE_CODE_ERROR == retval) {
                if (mCanBaseActivity != null) {
                    Toast.makeText(mCanBaseActivity, "打开设备失败!", Toast.LENGTH_SHORT).show();
                }
                MyApp.driver.CloseDevice();
            } else if (Common.DRIVER_STATE_CODE_SUCCESS == retval) {
                if (!MyApp.driver.UartInit()) {//对串口设备进行初始化操作
                    if (mCanBaseActivity != null) {
                        Toast.makeText(mCanBaseActivity, "设备初始化失败!", Toast.LENGTH_SHORT).show();
                        Toast.makeText(mCanBaseActivity, "打开" + "设备失败!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    if (mCanBaseActivity != null) {
                        Toast.makeText(mCanBaseActivity, "打开设备成功!", Toast.LENGTH_SHORT).show();
                    }
                    //开启读线程读取串口接收的数据
                    mDriverHandler.setCanBusReadThreadState(driverRunningState = true);

                    mCanBaseActivity.callbackDriverState(driverRunningState);
                }
            } else {
                if (mCanBaseActivity != null) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(mCanBaseActivity);
                    builder.setIcon(R.drawable.icon);
                    builder.setTitle("未授权限");
                    builder.setMessage("确认退出吗？");
                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
//								MainFragmentActivity.this.finish();
                            System.exit(0);
                        }
                    });
                    builder.setNegativeButton("返回", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    builder.show();
                }
            }
        } else if (!driverState && driverRunningState) {
            closeDriver();
            mCanBaseActivity.callbackDriverState(driverRunningState);
        }
    }

    public boolean getDriverState() {
        return driverRunningState;
    }

    public void receiveMessage(String receiveText) {
        Log.d(TAG, "ReceiveText = " + receiveText);

        mCanBaseActivity.receiveCanMessage(receiveText);
    }


    public void sendMessage(String sendText) {
        Log.d(TAG, "sendText = " + sendText);
        byte[] to_send = CommonMethod.hexToByteArray(sendText);

        //写数据，第一个参数为需要发送的字节数组，第二个参数为需要发送的字节长度，返回实际发送的字节长度
        int retval = MyApp.driver.WriteData(to_send, to_send.length);
        if (retval < 0) {
            Log.d(TAG, "CAN信号发送失败");
            if (mCanBaseActivity != null) {
                Toast.makeText(mCanBaseActivity, "CAN信号发送失败!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void closeDriver() {
        mDriverHandler.setCanBusReadThreadState(driverRunningState = false);
        mDriverHandler.removeCallbacksAndMessages(null);

        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        MyApp.driver.CloseDevice();
    }
}
