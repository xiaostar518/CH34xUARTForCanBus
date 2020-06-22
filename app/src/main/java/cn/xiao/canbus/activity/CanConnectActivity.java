package cn.xiao.canbus.activity;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import cn.wch.ch34xuartdriver.CH34xUARTDriver;
import cn.xiao.canbus.R;
import cn.xiao.canbus.application.MyApp;
import cn.xiao.canbus.util.CommonMethod;

public class CanConnectActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String TAG = "CanConnectActivity";

    private static final String ACTION_USB_PERMISSION = "cn.xiao.canbus.USB_PERMISSION";

    private boolean driverRunningState;
    private Handler handler;
    private int retval;

    private Button benOpen;

    public byte[] writeBuffer;
    public byte[] readBuffer;


    private Button btnClose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_can_connect);
        // 保持常亮的屏幕的状态
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        MyApp.driver = new CH34xUARTDriver((UsbManager) getSystemService(Context.USB_SERVICE), this, ACTION_USB_PERMISSION);

        initView();
        initListener();

        checkUsbHostAvailable();

        handler = new Handler() {

            public void handleMessage(Message msg) {
//                readText.setText((String) msg.obj);
//				readText.append((String) msg.obj);
            }
        };
    }

    //处理界面
    private void initView() {
        benOpen = (Button) findViewById(R.id.btn_open);
        btnClose = findViewById(R.id.btn_close);
    }

    private void initListener() {
        //打开流程主要步骤为ResumeUsbList，UartInit
        benOpen.setOnClickListener(this);

        btnClose.setOnClickListener(this);
    }

    private void checkUsbHostAvailable() {
        // 判断系统是否支持USB HOST
        if (!MyApp.driver.UsbFeatureSupported()) {
            Dialog dialog = new AlertDialog.Builder(this)
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
        }
    }

    public void onResume() {
        super.onResume();
        if (!MyApp.driver.isConnected()) {
            int retval = MyApp.driver.ResumeUsbPermission();
            if (retval == 0) {
            } else if (retval == -2) {
                Toast.makeText(CanConnectActivity.this, "获取权限失败!", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_open:
                if (!driverRunningState) {
                    retval = MyApp.driver.ResumeUsbList();
                    // ResumeUsbList方法用于枚举CH34X设备以及打开相关设备
                    if (retval == -1) {
                        Toast.makeText(CanConnectActivity.this, "打开设备失败!", Toast.LENGTH_SHORT).show();
                        MyApp.driver.CloseDevice();
                    } else if (retval == 0) {
                        if (!MyApp.driver.UartInit()) {//对串口设备进行初始化操作
                            Toast.makeText(CanConnectActivity.this, "设备初始化失败!", Toast.LENGTH_SHORT).show();
                            Toast.makeText(CanConnectActivity.this, "打开" + "设备失败!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(CanConnectActivity.this, "打开设备成功!", Toast.LENGTH_SHORT).show();
                            driverRunningState = true;
                            benOpen.setText("Close");
                            new CanBusReadThread().start();//开启读线程读取串口接收的数据
                        }
                    } else {
                        android.app.AlertDialog.Builder builder = new AlertDialog.Builder(this);
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
                break;
            case R.id.btn_close:
                if (driverRunningState) {
                    benOpen.setText("Open");
                    driverRunningState = false;
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    MyApp.driver.CloseDevice();
                }
                break;
            default:
                break;
        }
    }

    private class CanBusReadThread extends Thread {
        public void run() {
            byte[] buffer = new byte[4096];

            while (true) {
                Log.d(TAG, "driverRunningState = " + driverRunningState);
                Message msg = Message.obtain();
                if (!driverRunningState) {
                    break;
                }
                int length = MyApp.driver.ReadData(buffer, 4096);
                if (length > 0) {
                    String recv = CommonMethod.bytesToHex(buffer, length);

                    Log.d(TAG, "recv = " + recv);
                    msg.obj = recv;
                    handler.sendMessage(msg);
                }
            }
        }
    }

}