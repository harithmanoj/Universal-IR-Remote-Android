package com.remote.universalirremote;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

public class Ping extends AppCompatActivity {

    HttpClient _httpConnection;
    TextView _sentMessage;
    TextView _recievedMessage;
    int lines = 0;
    ArrayList<String> _sentList;
    ArrayList<String> _recList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ping);
        _sentMessage = (TextView) findViewById(R.id.txt_leftSent);
        _recievedMessage = (TextView) findViewById(R.id.txt_rightRecieved);
    }

    @Override
    protected void onStart() {
        Intent intent = getIntent();
        _httpConnection = new HttpClient((NsdServiceInfo)intent.getParcelableExtra(Constant.INT_SERVICE_KEY));
        _httpConnection.connect();
        super.onStart();
    }

    public void update() {
        StringBuilder recieved = new StringBuilder();
        for(String i : _recList) {
            recieved.append(i);
        }

        StringBuilder sent = new StringBuilder();
        for(String i : _sentList) {
            sent.append(i);
        }

        _recievedMessage.setText(recieved.toString());
        _sentMessage.setText(sent.toString());
    }

    public void addToSend(String msg) {
        if(lines == 24) {
            _sentList.remove(0);
            _recList.remove(0);
        } else {
            ++lines;
        }
        _sentList.add(msg);
        _recList.add("\n");

        if(lines == 24)
            update();
        else {
            _sentMessage.setText(_sentMessage.getText() + "\n" + msg);
            _recievedMessage.setText(_recievedMessage.getText() + "\n");
        }
    }

    public void addToRec(String msg) {
        if(lines == 24) {
            _sentList.remove(0);
            _recList.remove(0);
        } else {
            ++lines;
        }
        _recList.add(msg);
        _sentList.add("\n");
        if(lines == 24)
            update();
        else {
            _sentMessage.setText(_sentMessage.getText() + "\n" );
            _recievedMessage.setText(_recievedMessage.getText() + "\n"+ msg);
        }
    }

    public void clickSend(View view) {
        String msg = ((EditText) findViewById(R.id.text_sendMessage)).getText().toString();

        StringBuilder xmlMessage = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        xmlMessage.append("<message> " + msg + " </message>");

        try {
            String response = _httpConnection.transaction(
                    new HttpClient.Request(
                            xmlMessage.toString().getBytes(), "POST",
                            new HttpClient.Request.Property("Content-Type", "application/xml"),
                            new HttpClient.Request.Property("charset", "utf-8")
                    )
            );
            addToRec(response);
            addToSend(msg);
        } catch (IOException ex) {
            Log.e("PING", "error in http client", ex);
        }
    }

    public void clickGet(View view) {
        try {
            String response = _httpConnection.transaction(
                    new HttpClient.Request(
                            null, "GET",
                            new HttpClient.Request.Property("Content-Type", "application/xml"),
                            new HttpClient.Request.Property("charset", "utf-8")
                    )
            );
            addToRec(response);
        } catch (IOException ex) {
            Log.e("PING", "error in http client", ex);
        }
    }
}