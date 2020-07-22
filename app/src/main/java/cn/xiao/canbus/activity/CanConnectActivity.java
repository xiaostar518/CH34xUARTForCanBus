package cn.xiao.canbus.activity;


import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import cn.xiao.canbus.R;


public class CanConnectActivity extends CanBaseActivity implements View.OnClickListener {

    public static final String TAG = "CanConnectActivity";

    private Button btnOpenDevice;

    private Button btnClose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_can_connect);

        initView();
        initListener();
    }

    //处理界面
    private void initView() {
        btnOpenDevice = (Button) findViewById(R.id.btn_open_device);
        btnClose = findViewById(R.id.btn_quit);
    }

    private void initListener() {
        //打开流程主要步骤为ResumeUsbList，UartInit
        btnOpenDevice.setOnClickListener(this);

        btnClose.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_open_device:
                setDriverState(true);
                break;
            case R.id.btn_quit:
                setDriverState(false);
                System.exit(0);
                break;
            default:
                break;
        }
    }

    @Override
    public void callbackDriverState(boolean driverRunningState) {
        if (driverRunningState) {
            btnOpenDevice.setText(getText(R.string.close));
        } else {
            btnOpenDevice.setText(getText(R.string.open));
        }
    }
}