package com.gectcr.ece.design.tutorial.networktest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.net.InetAddress;

public class PingActivity extends AppCompatActivity {

    private String _outData;
    private static final String OUT_DATA_KEY = "out.stream.ui";
    private String _inData;
    private static final String IN_DATA_KEY = "in.stream.ui";
    private TextView _connectionStatus;
    private TextView _outStreamUI;
    private TextView _inStreamUI;
    private TextView _outCurrentUI;
    private static final String OUT_CURRENT_KEY = "out.curr.ui";
    private TextView _inCurrentUI;
    private static final String IN_CURRENT_KEY = "in.curr.ui";
    Handler _updateHandler;
    HandlerThread _updateThread;

    public static final String TAG = "PingActivity";

    private NetworkManager _networkManager;
    private ServerConnection _server;
    private ClientConnection _client;
    private Integer _mode;
    private int _port;
    private InetAddress _address;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ping);
        String in = "0", out = "0";

        _connectionStatus = (TextView) findViewById(R.id.connectionStatus);
        _outStreamUI = (TextView) findViewById(R.id.outData);
        _inStreamUI = (TextView) findViewById(R.id.inData);
        _outCurrentUI = (TextView) findViewById(R.id.outCurrent);
        _inCurrentUI = (TextView) findViewById(R.id.inCurrent);

        if (savedInstanceState == null) {
           _outData = "Out : ";
           _inData = "In : ";
        } else {
            _outData = savedInstanceState.getString(OUT_DATA_KEY);
            _inData = savedInstanceState.getString(IN_DATA_KEY);
            in = savedInstanceState.getString(IN_CURRENT_KEY);
            out = savedInstanceState.getString(OUT_CURRENT_KEY);
        }
        _inCurrentUI.setText(in);
        _outCurrentUI.setText(out);

        Intent intent = getIntent();
        _mode = intent.getIntExtra(LauncherActivity.CONNECTION_MODEL, 0);
        if (_mode.equals(LauncherActivity.CLIENT_CONNECTION)) {
            _port = intent.getIntExtra(DiscoverActivity.PORT_KEY, -1);
            _address = (InetAddress) intent.getSerializableExtra(DiscoverActivity.ADDRESS_KEY);
        }
        _updateThread = new HandlerThread("UpdateHandler");
        _updateThread.start();
        _updateHandler = new Handler(_updateThread.getLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                String seq = msg.getData().getString(ClientConnection.REC_MSG_KEY);
                for( int i=0; i < seq.length(); ++i) {
                    pingMe((int)seq.charAt(i) - (int)'0');
                }
            }
        };
    }

    @Override
    protected void onStart() {


        if (_mode.equals(LauncherActivity.SERVER_CONNECTION)) {
            _networkManager = new NetworkManager(this, null);
            _server = new ServerConnection(_updateHandler);
            synchronized (_server) {
                try {
                   while(!_server._isServerAcquired) {
                       _server.wait();
                   }
                } catch (InterruptedException e) {
                    Log.d(TAG, "server interrupt for some reason");
                }
            }
            _connectionStatus.setText("Server Set up");
            _networkManager.registerService(_port);
            synchronized (_networkManager._waitOnForRegister) {
                try {
                    while (!_networkManager.isRegistered()) {
                        _networkManager._waitOnForRegister.wait();
                    }
                } catch (InterruptedException e) {
                    Log.d(TAG, "server interrupt for some reason");
                }
                _connectionStatus.setText("NSD registered as " + _networkManager.getRegisteredName());
            }
            synchronized (_networkManager._waitOnForRegister) {
                try {
                    while (!_server._isServerAcquired) {
                        _server._waitForClient.wait();
                    }
                } catch (InterruptedException e) {
                    Log.d(TAG, "server interrupt for some reason");
                }
                _connectionStatus.setText("NSD registered as " + _networkManager.getRegisteredName()
                        + "connected to " + _server.getHost());
            }
        } else if (_mode.equals(LauncherActivity.CLIENT_CONNECTION)) {
            _client = new ClientConnection(_port,_address, _updateHandler);
            _connectionStatus.setText("Connection Succes to " + _client.getHost());

        }
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "Now stopping");
        if (_networkManager != null) {
           _networkManager.tearDown();
           _networkManager = null;
        }
        if (_client !=null ) {
            _client.tearDown();
            _client = null;
        }
        if (_server != null) {
            _server.tearDown();
            _server = null;
        }

        super.onStop();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {

        outState.putString(OUT_DATA_KEY, _outData);
        outState.putString(IN_DATA_KEY, _inData);
        outState.putString(IN_CURRENT_KEY, (String)_inCurrentUI.getText());
        outState.putString(OUT_CURRENT_KEY, (String)_outCurrentUI.getText());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "Being destroyed.");
        super.onDestroy();
    }

    public void clickPingHigh(View view) {
        pingNow(1);
    }

    public void clickPingLow(View view) {
        pingNow(0);
    }

    private void pingNow(int var) {

        if(_outData.length() == 8)  {
            _outData = _outData.substring(1);
        }
        _outData = _outData + String.valueOf(var);

        ((TextView)findViewById(R.id.outData)).setText("Out: " + _outData);

        ((TextView)findViewById(R.id.outCurrent)).setText(String.valueOf(var));
        Log.i(TAG,"pinging with " + String.valueOf(var));

        if(_mode.equals(LauncherActivity.CLIENT_CONNECTION)) {
            _client.sendMessage(var);
        } else if (_mode.equals(LauncherActivity.SERVER_CONNECTION)) {
            _server.sendMessage(var);
        }

    }

    public void pingMe(int var) {

        if(_inData.length() == 8) {
            _inData = _inData.substring(1);
        }
        _inData = _inData + String.valueOf(var);

        ((TextView)findViewById(R.id.inData)).setText(" In: " + _inData);

        ((TextView)findViewById(R.id.inCurrent)).setText(String.valueOf(var));
        Log.i(TAG,"pinged with " + String.valueOf(var));
    }
}
