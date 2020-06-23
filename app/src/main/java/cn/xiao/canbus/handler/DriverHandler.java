package cn.xiao.canbus.handler;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import cn.xiao.canbus.activity.CanConnectActivity;
import cn.xiao.canbus.thread.CanBusReadThread;

public class DriverHandler extends Handler {

    private static final String TAG = "DriverHandler";
    private static DriverHandler mDriverHandler;
    private CanConnectActivity mCanConnectActivity;

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

    public void setActivity(CanConnectActivity canConnectActivity) {
        this.mCanConnectActivity = canConnectActivity;
    }

    public void setCanBusReadThreadState(boolean driverRunningState) {
        CanBusReadThread.getInstance().setDriverRunningState(driverRunningState);
        if (driverRunningState) {
            CanBusReadThread.getInstance().start();
        } else {

        }
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        switch (msg.what) {
            case CAN_BUS_RECEIVE_MESSAGE:
                String canMessage = (String) msg.obj;
                Log.d(TAG, "canMessage = " + canMessage);
                break;
            default:
                break;
        }
    }
}
