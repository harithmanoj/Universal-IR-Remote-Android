package com.remote.universalirremote.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.view.View;

import com.remote.universalirremote.Constant;
import com.remote.universalirremote.R;
import com.remote.universalirremote.network.ACSend;
import com.remote.universalirremote.network.RawGet;
import com.remote.universalirremote.network.RawSend;

public class IRTest extends AppCompatActivity {
    private RawGet _irGet;
    private RawSend _irSend;
    private ACSend _acSend;

    private Handler _getHandler;
    private Handler _sendHandler;
    private Handler _acHandler;

    private HandlerThread _getHandlerThread;
    private HandlerThread _sendHandlerThread;
    private HandlerThread _acHandlerThread;

    private String _detectRaw;
    private int _detectProtocol;

    public static final String TAG = "IRTest";

    private boolean power = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_i_r_test);
    }

    @Override
    protected void onStart() {
        Intent intent = getIntent();

        NsdServiceInfo serviceInfo = (NsdServiceInfo)intent.getParcelableExtra(Constant.INT_SERVICE_KEY);



        _getHandlerThread = new HandlerThread("GetHandlerThread");
        _getHandlerThread.start();
        _getHandler = new Handler(_getHandlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                _detectRaw = msg.getData().getString(RawGet.RAW_KEY);
                _detectProtocol = msg.getData().getInt(RawGet.PROTOCOL_KEY);

                Log.i(TAG, "raw get handleMessage: " + _detectRaw + " | " + ((Integer)_detectProtocol).toString());
            }
        };
        _irGet = new RawGet(serviceInfo, _getHandler);

        _sendHandlerThread = new HandlerThread("SendHandlerThread");
        _sendHandlerThread.start();
        _sendHandler = new Handler(_sendHandlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                String resp = msg.getData().getString(RawSend.RESPONSE_KEY);
                Log.i(TAG, "raw send handleMessage: " + resp);
            }
        };
        _irSend = new RawSend(serviceInfo, _sendHandler);

        _acHandlerThread = new HandlerThread("AC Handler Thread");
        _acHandlerThread.start();
        _acHandler = new Handler(_acHandlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                String resp = msg.getData().getString(ACSend.RESPONSE_KEY);
                Log.i(TAG, "ac handleMessage: " + resp);
            }
        };
        _acSend = new ACSend(serviceInfo, _acHandler);

        super.onStart();
    }

    public void onGet(View view) {
        _irGet.getData(100);
        Log.i(TAG, "onSend: ");
    }

    public void onSend(View view) {
        if(_detectRaw == null)
            _irSend.sendData("null", "null");
        else
            _irSend.sendData(_detectRaw, "null");
        Log.i(TAG, "onSend: ");
    }

    public void onAC(View view) {
        _acSend.sendData(Constant.Protocols.LG, 1, power, Constant.AcMode.kCool, 25, true, Constant.AcFan.kAuto, Constant.AcSwingv.kAuto, Constant.AcSwingh.kAuto, true, true, true, true, false, true, true, -1, -1, "test");
        power = !power;
    }
}