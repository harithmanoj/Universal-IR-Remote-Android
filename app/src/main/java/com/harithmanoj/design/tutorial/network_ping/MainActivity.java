package com.harithmanoj.design.tutorial.network_ping;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void pingNow() {
        Toast.makeText(getBaseContext(), "Pinging the pair now!!!", Toast.LENGTH_SHORT).show();
    }
}