package com.harithmanoj.design.tutorial.network_ping;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {

    private String outData;
    private String inData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void pingHigh(View view) {
        pingNow(1);
    }

    public void pingLow(View view) {
        pingNow(0);
    }

    public void pingNow(int var) {


        if(outData.length() == 8)  {
            outData.substring(1);
        }
        outData = outData + String.valueOf(var);

        ((TextView)findViewById(R.id.outData)).setText("Out: " + outData);

        ((TextView)findViewById(R.id.outCurrent)).setText(String.valueOf(var));
    }

}