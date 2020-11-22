package com.gectcr.ece.design.tutorial.networktest;

import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class ClientConnection {

    int _connectionPort;
    InetAddress _connectionAddress;
    Handler _updateHandler;

    public static final String TAG = "ClientConnection";

    private Thread _sendThread;
    private Thread _receiveThread;

    private BlockingQueue<Integer> _pingQueue;
    private int QUEUE_CAPACITY = 30;

    public boolean sendMessage(int bit) {
        if(_pingQueue.size() == QUEUE_CAPACITY) {
            return false;
        } else {
            _pingQueue.add(bit);
            return true;
        }
    }

    class SendingThread implements Runnable {

        Socket _socket;
        public static final String SEND_TAG = "Client.SEND";

        public SendingThread(int port, InetAddress addr) {
            try {
                _socket = new Socket(addr, port);
                Log.i(TAG, "Sending socket set");
            } catch (UnknownHostException e) {
                Log.e(SEND_TAG, "Initialising socket failed with Unknown host", e);
            } catch(IOException e) {
                Log.e( TAG,"IO exception in socket", e);
            }
        }


        @Override
        public void run() {
            while(true) {

                String bits = new String("");

                try {
                    while(!_pingQueue.isEmpty()) {
                        bits = bits + _pingQueue.take().toString();
                    }
                } catch (InterruptedException e) {
                    Log.d(SEND_TAG, "Message sending loop interrupted, exiting", e);
                }

                try {
                    if (_socket == null) {
                        Log.e(SEND_TAG, "socket is null????wth");
                    } else if (_socket.getOutputStream() == null ) {
                        Log.e(SEND_TAG, "wth is output stream null");
                    } else {
                        PrintWriter out = new PrintWriter(
                                new BufferedWriter(
                                        new OutputStreamWriter(_socket.getOutputStream())),
                                true
                        );
                        out.println(bits);
                        out.flush();
                    }
                } catch (UnknownHostException e) {
                    Log.e(SEND_TAG, "Unknown Host", e);
                } catch (IOException e) {
                    Log.e(SEND_TAG, "I/O Exception", e);
                } catch (Exception e) {
                    Log.e(SEND_TAG, "Error ", e);
                }
                Log.d(SEND_TAG, "Client sent message: " + bits);
            }
        }
    }
}
