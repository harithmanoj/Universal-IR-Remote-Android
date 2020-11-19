package com.harithmanoj.design.tutorial.network_ping;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {

    private String outData;
    private String inData;
    private static final String TAG = "MainActivity";


    NSDNetworkManager network;
    private Handler updateHandler;

    Connection connection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        outData = inData = "";
        Log.d(TAG,"create MainActivity");

        updateHandler = new Handler() {
            @Override
            public void handleMessage(@NonNull Message msg) {
                String chatLine = msg.getData().getString("msg");
                addChatLine(chatLine);
            }
        };
    }

    public void clickAdvertise(View v) {
        // Register service
        if(connection.getLocalPort() > -1) {
            network.registerService(connection.getLocalPort());
        } else {
            Log.d(TAG, "ServerSocket isn't bound.");
        }
    }

    public void clickDiscover(View v) {
        network.discoverServices();
    }

    public void clickConnect(View v) {
        NsdServiceInfo service = network.getChosenServiceInfo();
        if (service != null) {
            Log.d(TAG, "Connecting.");
            connection.connectToServer(service.getHost(),
                    service.getPort());
        } else {
            Log.d(TAG, "No service to connect to!");
        }
    }

    public void addChatLine(String line) {
        try {
            pingMe(Integer.parseInt(line));
        }
        catch (NumberFormatException e)
        {
            Log.e(TAG, "numberformat exception; " + e.getLocalizedMessage());
        }
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "Starting.");
        connection = new Connection(updateHandler);

        network = new NSDNetworkManager(this);
        network.initializeNsd();
        super.onStart();
    }


    @Override
    protected void onPause() {
        Log.d(TAG, "Pausing.");
        if (network != null) {
            network.stopDiscovery();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "Resuming.");
        super.onResume();
        if (network != null) {
            network.discoverServices();
        }
    }

    public void clickPingHigh(View view) {
        pingNow(1);
    }

    public void clickPingLow(View view) {
        pingNow(0);
    }

    private void pingNow(int var) {

        if(outData.length() == 8)  {
            outData = outData.substring(1);
        }
        outData = outData + String.valueOf(var);

        ((TextView)findViewById(R.id.outData)).setText("Out: " + outData);

        ((TextView)findViewById(R.id.outCurrent)).setText(String.valueOf(var));
        Log.i(TAG,"pinging with " + String.valueOf(var));
        connection.sendMessage(String.valueOf(var));
    }

    public void pingMe(int var) {

        if(inData.length() == 8) {
            inData = inData.substring(1);
        }
        inData = inData + String.valueOf(var);

        ((TextView)findViewById(R.id.inData)).setText(" In: " + inData);

        ((TextView)findViewById(R.id.inCurrent)).setText(String.valueOf(var));
        Log.i(TAG,"pinged with " + String.valueOf(var));
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "Being stopped.");
        network.tearDown();
        connection.tearDown();
        network = null;
        connection = null;
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "Being destroyed.");
        super.onDestroy();
    }
}