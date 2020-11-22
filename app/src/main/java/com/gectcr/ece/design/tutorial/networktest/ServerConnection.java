package com.gectcr.ece.design.tutorial.networktest;

import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerConnection {
    public Object _waitForClient = new Object();
    private int _connectionPort = -1;
    private Handler _updateHandler;
    private ClientConnection _communicationManager;
    private ServerSocket _serverSocket;
    boolean _isServerAcquired = false;
    private InetAddress _connectionAddress;

    private Thread _serverThread;

    public ServerConnection(Handler update) {
        _serverThread = new Thread(new ServerAcquireThread());
        _serverThread.start();
    }

    public void tearDown() {
        _serverThread.interrupt();
        try {
            _serverSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Error while closing server socket ", e);
        }
    }

    public static final String TAG = "ServerCon";

    class ServerAcquireThread implements Runnable {
        @Override
        public void run() {
            try {
                Log.d(TAG, "Server thread acquiring port");

                synchronized (this) {
                    _serverSocket = new ServerSocket(0);
                    _connectionPort = _serverSocket.getLocalPort();
                    this.notifyAll();
                }
                while(!Thread.currentThread().isInterrupted()) {
                    Socket socket;
                    synchronized (_waitForClient) {
                        Log.d(TAG, "Server socket created, waiting client");
                        socket = _serverSocket.accept();
                        _isServerAcquired = true;
                        Log.d(TAG, "connected to " + socket.getInetAddress().getHostName());
                        _waitForClient.notifyAll();
                        _connectionAddress = socket.getInetAddress();
                    }
                    if (_communicationManager == null){
                        _communicationManager = new ClientConnection(socket.getPort(),
                                socket.getInetAddress(), _updateHandler);
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, "error creating Server Socket " , e);
                e.printStackTrace();
            }

        }
    }

    public String getHost() {
        return _connectionAddress.getHostName();
    }

    public void sendMessage(int var) {
        _communicationManager.sendMessage(var);
    }
}
