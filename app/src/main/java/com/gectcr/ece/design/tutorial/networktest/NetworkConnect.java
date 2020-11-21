package com.gectcr.ece.design.tutorial.networktest;

import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class NetworkConnect {
    // asynchronous task execution handler (interface class)
    private Handler _updateHandler;

    private static final String TAG = "NetworkConnect";

    private Socket _socket;

    private synchronized void setSocket(Socket socket) {
        Log.d(TAG, "setSocket called");

        if(socket == null) {
            Log.d(TAG, "setting null socket");
        }
        if(_socket != null) {
            if(_socket.isConnected()) {
                try {
                    _socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

        _socket = socket;
    }

    private Socket getSocket() {
        return _socket;
    }

    private int _port = -1;

    public void setLocalPort(int port) {
        _port = port;
    }

    public int getLocalPort() {
        return _port;
    }

    private class PingServer {

        class ServerThread implements Runnable {

            @Override
            public void run() {
                try {
                    _serverSocket = new ServerSocket(0);
                    setLocalPort(_serverSocket.getLocalPort());

                    while (!Thread.currentThread().isInterrupted()) {
                        Log.d(TAG, "Server Socket created, awaiting connection");
                        setSocket(_serverSocket.accept());
                        Log.d(TAG, "connected");

                        if(_pingClient == null) {
                            int port = _socket.getPort();
                            InetAddress address = _socket.getInetAddress();
                            connectToServer(address, port);
                        }
                    }
                } catch (IOException e) {
                    Log.e(TAG, "error creating Server Socket " + e);
                    e.printStackTrace();
                }
            }
        }

        ServerSocket _serverSocket = null;
        Thread _thread = null;

        public PingServer(Handler handler) {
            _thread = new Thread( new ServerThread() );
            _thread.start();
        }

        public void tearDown() {
            _thread.interrupt();
            try {
                _serverSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Error while closing serrver socket " + e);
            }
        }
    }

    private class PingClient {




}
