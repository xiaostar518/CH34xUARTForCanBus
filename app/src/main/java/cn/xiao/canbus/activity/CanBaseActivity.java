package cn.xiao.canbus.activity;

import android.app.Activity;
import android.content.Context;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.WindowManager;

import cn.wch.ch34xuartdriver.CH34xUARTDriver;
import cn.xiao.canbus.application.MyApp;
import cn.xiao.canbus.control.CanControl;
import cn.xiao.canbus.handler.DriverHandler;
import cn.xiao.canbus.util.Common;

public class CanBaseActivity extends Activity {

    protected DriverHandler mDriverHandler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 保持常亮的屏幕的状态
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        MyApp.driver = new CH34xUARTDriver((UsbManager) getSystemService(Context.USB_SERVICE), this, Common.ACTION_USB_PERMISSION);

        mDriverHandler = DriverHandler.getInstance();

        CanControl.getInstance().checkUsbHostAvailable();
        CanControl.getInstance().setMainActivity(this);
        CanControl.getInstance().setHandler(mDriverHandler);
    }

    @Override
    protected void onResume() {
        super.onResume();
        CanControl.getInstance().resumeUsbPermission();
    }

    public void callbackDriverState(boolean driverRunningState) {
    }

    public void receiveCanMessage(String receiveText) {
    }

    protected void setDriverState(boolean driverState) {
        CanControl.getInstance().setDriverState(driverState);
    }

    @Override
    protected void onDestroy() {
        CanControl.getInstance().closeDriver();
        super.onDestroy();
    }

}
