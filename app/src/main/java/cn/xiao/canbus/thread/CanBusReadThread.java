package cn.xiao.canbus.thread;

import android.os.Message;
import android.util.Log;

import cn.xiao.canbus.application.MyApp;
import cn.xiao.canbus.handler.DriverHandler;
import cn.xiao.canbus.util.CommonMethod;

public class CanBusReadThread extends Thread {
    public static final String TAG = "CanBusReadThread";

    private static CanBusReadThread mCanBusReadThread;
    private static DriverHandler mDriverHandler;

    private boolean driverRunningState;

    public static CanBusReadThread getInstance() {
        if (mCanBusReadThread == null) {
            mCanBusReadThread = new CanBusReadThread();
        }
        return mCanBusReadThread;
    }

    private CanBusReadThread() {
    }

    public void setDriverRunningState(boolean driverRunningState) {
        this.driverRunningState = driverRunningState;
    }

    public void setDriverHandler(DriverHandler driverHandler) {
        this.mDriverHandler = driverHandler;
    }



    public void run() {
        byte[] buffer = new byte[4096];

        Log.d(TAG, "driverRunningState = " + driverRunningState);
        while (driverRunningState) {
            Log.d(TAG, "CanBusReadThread looping");

            int length = MyApp.driver.ReadData(buffer, 4096);
            if (length > 0) {
                String receiveMessage = CommonMethod.bytesToHex(buffer, length);
                Log.d(TAG, "receiveMessage = " + receiveMessage);
                Message message = Message.obtain();
                message.what = DriverHandler.CAN_BUS_RECEIVE_MESSAGE;
                message.obj = "receiveMessage";
                mDriverHandler.sendMessage(message);
            }
        }
        Log.d(TAG, "driverRunningState = false");
        mCanBusReadThread = null;
    }
}
