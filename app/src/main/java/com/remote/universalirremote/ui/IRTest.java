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
import com.remote.universalirremote.network.RawGet;
import com.remote.universalirremote.network.RawSend;

public class IRTest extends AppCompatActivity {
    private RawGet _irGet;
    private RawSend _irSend;

    private Handler _getHandler;
    private Handler _sendHandler;

    private HandlerThread _getHandlerThread;
    private HandlerThread _sendHandlerThread;

    private String _detectRaw;
    private int _detectProtocol;

    public static final String TAG = "IRTest";

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

                Log.i(TAG, "handleMessage: " + _detectRaw + " | " + ((Integer)_detectProtocol).toString());
            }
        };
        _irGet = new RawGet(serviceInfo, _getHandler);

        _sendHandlerThread = new HandlerThread("SendHandlerThread");
        _sendHandlerThread.start();
        _sendHandler = new Handler(_sendHandlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                String resp = msg.getData().getString(RawSend.RESPONSE_KEY);
                Log.i(TAG, "handleMessage: " + resp);
            }
        };
        _irSend = new RawSend(serviceInfo, _sendHandler);

        super.onStart();
    }

    public void onGet(View view) {
        _irGet.getData();
        Log.i(TAG, "onSend: ");
    }

    public void onSend(View view) {
        if(_detectRaw == null)
            _irSend.sendData("null");
        else
            _irSend.sendData(_detectRaw);
        Log.i(TAG, "onSend: ");
    }
}