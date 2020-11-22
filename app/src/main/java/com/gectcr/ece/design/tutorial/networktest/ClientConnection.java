package com.gectcr.ece.design.tutorial.networktest;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
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

    private int _connectionPort;
    private InetAddress _connectionAddress;
    private Handler _updateHandler;
    private Socket _socket;

    public static final String TAG = "ClientConnection";
    public static final String REC_MSG_KEY = "Client.rec.msg";

    private Thread _sendThread;
    private Thread _receiveThread;

    private BlockingQueue<Integer> _pingQueue;
    private int QUEUE_CAPACITY = 30;

    public void tearDown() {
        _receiveThread.interrupt();
        try {
            _socket.close();
        } catch (IOException e) {
            Log.e(TAG, "error when closing socket ", e);
        }
    }

    public String getHost() {
        return _connectionAddress.getHostName();
    }

    public ClientConnection(int port, InetAddress addr, Handler update) {
        _connectionAddress = addr;
        _connectionPort = port;
        _updateHandler = update;
        _pingQueue = new ArrayBlockingQueue<Integer>(QUEUE_CAPACITY);
        try {
            _socket = new Socket(_connectionAddress, _connectionPort);
            Log.i(TAG, "Sending socket set");
        } catch (UnknownHostException e) {
            Log.e(TAG, "Initialising socket failed with Unknown host", e);
        } catch(IOException e) {
            Log.e( TAG,"IO exception in socket", e);
        }
        _sendThread = new Thread(
                new SendingThread(_socket,_pingQueue));
        _sendThread.start();
        _receiveThread = new Thread(
                new RecievingThread(_socket,_updateHandler));
        _receiveThread.start();
    }

    public boolean sendMessage(int bit) {
        if(_pingQueue.size() == QUEUE_CAPACITY) {
            return false;
        } else {
            _pingQueue.add(bit);
            return true;
        }
    }

    static class RecievingThread implements Runnable {
        Socket _socket;
        public static final String REC_TAG = "Client.REC";
        Handler _updateHandler;

        public RecievingThread(Socket skt, Handler update) {
            _socket = skt;
            _updateHandler = update;

        }

        @Override
        public void run() {
            BufferedReader input;

            try {
                input = new BufferedReader(new InputStreamReader(_socket.getInputStream()));

                while (!Thread.currentThread().isInterrupted() ) {
                    String msg = null;
                    msg = input.readLine();

                    if (msg != null) {
                        Log.d(REC_TAG, "read from stream " + msg);
                        Bundle msgBundle = new Bundle();
                        msgBundle.putString(REC_MSG_KEY, msg);;
                        Message message = new Message();
                        message.setData(msgBundle);
                        _updateHandler.sendMessage(message);
                    } else {
                        Log.d(REC_TAG, "null msg?");
                        break;
                    }

                }

                input.close();
            } catch (IOException e) {
                Log.e(REC_TAG, "Server loop error ", e);
            }
        }
    }

    static class SendingThread implements Runnable {

        Socket _socket;
        public static final String SEND_TAG = "Client.SEND";
        BlockingQueue<Integer> _pingQueue;

        public SendingThread(Socket skt, BlockingQueue<Integer> queue) {
            _socket = skt;
            _pingQueue = queue;
        }


        @Override
        public void run() {
            while(true) {

                String bits = new String("");

                try {
                    if (_socket == null) {
                        Log.e(SEND_TAG, "socket is null????wth");
                        return;
                    } else if (_socket.getOutputStream() == null ) {
                        Log.e(SEND_TAG, "wth is output stream null");
                        return;
                    }
                } catch (UnknownHostException e) {
                    Log.e(SEND_TAG, "Unknown Host", e);
                } catch (IOException e) {
                    Log.e(SEND_TAG, "I/O Exception", e);
                } catch (Exception e) {
                    Log.e(SEND_TAG, "Error ", e);
                }

                try {
                    while(!_pingQueue.isEmpty()) {
                        bits = bits + _pingQueue.take().toString();
                    }
                } catch (InterruptedException e) {
                    Log.d(SEND_TAG, "Message sending loop interrupted, exiting", e);
                }
                PrintWriter out;
                try{
                    out = new PrintWriter(
                        new BufferedWriter(
                                new OutputStreamWriter(_socket.getOutputStream())),
                        true
                    );
                    out.println(bits);
                    out.flush();
                } catch (IOException e) {
                    Log.e(SEND_TAG, "I/O Exception", e);
                }

                Log.d(SEND_TAG, "Client sent message: " + bits);
            }
        }
    }
}
