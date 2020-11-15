package com.harithmanoj.design.tutorial.network_ping;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

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
        Toast.makeText(getApplicationContext(), "Pinging the pair with "
                + String.valueOf(var) + " now !!!", Toast.LENGTH_SHORT).show();
        TextView tv = (TextView)findViewById(R.id.textView);
        CharSequence text = tv.getText();
        tv.setText(text + String.valueOf(var));
    }


}