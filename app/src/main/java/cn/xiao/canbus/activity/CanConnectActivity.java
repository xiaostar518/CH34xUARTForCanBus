package cn.xiao.canbus.activity;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import cn.wch.ch34xuartdriver.CH34xUARTDriver;
import cn.xiao.canbus.R;
import cn.xiao.canbus.application.MyApp;
import cn.xiao.canbus.handler.DriverHandler;
import cn.xiao.canbus.util.Common;

public class CanConnectActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String TAG = "CanConnectActivity";

    private boolean driverRunningState;
    private int retval;

    private Button benOpen;

    public byte[] writeBuffer;
    public byte[] readBuffer;


    private Button btnClose;
    private DriverHandler mDriverHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_can_connect);
        // 保持常亮的屏幕的状态
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        MyApp.driver = new CH34xUARTDriver((UsbManager) getSystemService(Context.USB_SERVICE), this, Common.ACTION_USB_PERMISSION);

        initView();
        initListener();

        checkUsbHostAvailable();

        mDriverHandler = DriverHandler.getInstance();
        mDriverHandler.setActivity(this);
    }

    //处理界面
    private void initView() {
        benOpen = (Button) findViewById(R.id.btn_open);
        btnClose = findViewById(R.id.btn_quit);
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
                setDriverState(!driverRunningState);
                break;
            case R.id.btn_quit:
                setDriverState(false);
                System.exit(0);
                break;
            default:
                break;
        }
    }

    private void setDriverState(boolean driverState) {
        if (driverState && !driverRunningState) {
//                    mDriverHandler.setCanBusReadThreadState(driverRunningState = true);

            retval = MyApp.driver.ResumeUsbList();
            // ResumeUsbList方法用于枚举CH34X设备以及打开相关设备
            if (Common.DRIVER_STATE_CODE_ERROR == retval) {
                Toast.makeText(CanConnectActivity.this, "打开设备失败!", Toast.LENGTH_SHORT).show();
                MyApp.driver.CloseDevice();
            } else if (Common.DRIVER_STATE_CODE_SUCCESS == retval) {
                if (!MyApp.driver.UartInit()) {//对串口设备进行初始化操作
                    Toast.makeText(CanConnectActivity.this, "设备初始化失败!", Toast.LENGTH_SHORT).show();
                    Toast.makeText(CanConnectActivity.this, "打开" + "设备失败!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(CanConnectActivity.this, "打开设备成功!", Toast.LENGTH_SHORT).show();
                    //开启读线程读取串口接收的数据
                    mDriverHandler.setCanBusReadThreadState(driverRunningState = true);

                    benOpen.setText(getText(R.string.close));
                }
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
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
        } else if (!driverState && driverRunningState) {
            benOpen.setText(getText(R.string.open));
            closeDriver();
        }
    }

    @Override
    protected void onDestroy() {
        closeDriver();
        super.onDestroy();
    }

    private void closeDriver() {
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