package cn.xiao.canbus.handler;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import cn.xiao.canbus.activity.CanConnectActivity;
import cn.xiao.canbus.control.CanControl;
import cn.xiao.canbus.thread.CanBusReadThread;

public class DriverHandler extends Handler {

    private static final String TAG = "DriverHandler";
    private static DriverHandler mDriverHandler;

    public static final int CAN_BUS_RECEIVE_MESSAGE = 1100;

    public static DriverHandler getInstance() {
        if (mDriverHandler == null) {
            mDriverHandler = new DriverHandler();
            CanBusReadThread.getInstance().setDriverHandler(mDriverHandler);
        }
        return mDriverHandler;
    }

    private DriverHandler() {

    }

    public void setCanBusReadThreadState(boolean driverRunningState) {
        CanBusReadThread.getInstance().setDriverRunningState(driverRunningState);
        if (driverRunningState) {
            CanControl.getInstance().setCanConfig(CanControl.baudRate, CanControl.dataBit, CanControl.stopBit, CanControl.parity, CanControl.flowControl);
            CanBusReadThread.getInstance().start();
        } else {

        }
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        switch (msg.what) {
            case CAN_BUS_RECEIVE_MESSAGE:
                String canReceiveMessage = (String) msg.obj;
                Log.d(TAG, "canReceiveMessage = " + canReceiveMessage);
                CanControl.getInstance().receiveMessage(canReceiveMessage);
                break;

            default:
                break;
        }
    }
}
