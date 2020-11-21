package com.gectcr.ece.design.tutorial.networktest;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class NetworkConnect {
    // asynchronous task execution handler (interface class)
    private Handler _updateHandler;
    private PingClient _pingClient;
    private PingServer _pingServer;

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

    public NetworkConnect(Handler handler) {
        _updateHandler = handler;
        _pingServer = new PingServer(handler);
    }

    public void tearDown() {
        _pingServer.tearDown();
        if(_pingClient != null) {
            _pingClient.tearDown();
        }
    }

    public void connectToServer(InetAddress address, int port) {
        _pingClient = new PingClient(address, port);
    }

    public void sendMessage(int bit) {
        if (_pingClient != null) {
            _pingClient.sendMessage(bit);
        }
    }

    public synchronized void updateMessages(String bit) {
        Log.e(TAG, "recieving message " + bit);

        Bundle messageBundle = new Bundle();
        messageBundle.putInt("bit", Integer.valueOf(bit));

        Message message = new Message();
        message.setData(messageBundle);
        _updateHandler.sendMessage(message);
    }

    private class PingServer {

        class ServerThread implements Runnable {

            @Override
            public void run() {
                try {
                    Log.d(TAG, "Server thread run function now setting port");
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
                    Log.e(TAG, "error creating Server Socket " , e);
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
                Log.e(TAG, "Error while closing serrver socket " , e);
            }
        }
    }

    private class PingClient {

        private InetAddress _clientAddress;
        private int _clientPort;

        private static final String CLIENT_TAG = "PingClient";

        private Thread _sendThread;
        private Thread _receiveThread;

        public PingClient (InetAddress address, int port) {
            Log.d(CLIENT_TAG, "construction");

            _clientAddress = address;
            _clientPort = port;

            _sendThread = new Thread(new SendingThread());
            _sendThread.start();
        }

        class SendingThread implements Runnable {
            BlockingQueue<Integer> _pingQueue;
            private int QUEUE_CAPACITY = 10;
            public SendingThread() {
                _pingQueue = new ArrayBlockingQueue<Integer>(10);
            }


            @Override
            public void run() {
                try {
                    if (getSocket() == null) {
                        setSocket(new Socket(_clientAddress, _clientPort));
                        Log.d(CLIENT_TAG, "client side socket init");

                    } else {
                        Log.d(CLIENT_TAG, "Socket already initialised");
                    }

                    _receiveThread = new Thread(new ReceivingThread());
                    _receiveThread.start();
                } catch (UnknownHostException e) {
                    Log.e(CLIENT_TAG, "Initialising socket failed with Unknown host", e);
                } catch (IOException e) {
                    Log.e(CLIENT_TAG, "Initialising Socket failed with IO exception", e);
                }

                while (true) {
                    try {
                        Integer bit = _pingQueue.take();
                        sendMessage(bit);
                    } catch (InterruptedException ie) {
                        Log.d(CLIENT_TAG, "Message sending loop interrupted, exiting", ie);
                    }
                }
            }
        }

        class ReceivingThread implements Runnable {


            @Override
            public void run() {
                BufferedReader input;

                try {
                    input = new BufferedReader(new InputStreamReader(_socket.getInputStream()));

                    while (!Thread.currentThread().isInterrupted() ) {
                        String msg = null;
                        msg = input.readLine();

                        if (msg != null) {
                            Log.d(CLIENT_TAG, "read from stream " + msg);
                            updateMessages(msg);
                        } else {
                            Log.d(CLIENT_TAG, "null msg?");
                            break;
                        }

                    }

                    input.close();
                } catch (IOException e) {
                    Log.e(CLIENT_TAG, "Server loop error ", e);
                }

            }
        }

        public void tearDown() {
            try {
                getSocket().close();
            } catch (IOException e) {
                Log.e(CLIENT_TAG, "error when closing ");
            }
        }

        public void sendMessage(Integer bit) {
            try {
                Socket socket = getSocket();

                if (socket == null) {
                    Log.e(CLIENT_TAG, "Socket is null");
                } else if (socket.getOutputStream() == null) {
                    Log.e(CLIENT_TAG, "Socket output stream is null");
                }

                PrintWriter out = new PrintWriter(
                        new BufferedWriter(
                                new OutputStreamWriter(getSocket().getOutputStream())
                        ), true
                );
                out.println(bit);
                out.flush();
            } catch (UnknownHostException e) {
                Log.e(CLIENT_TAG, "Unknown Host", e);
            } catch (IOException e) {
                Log.e(CLIENT_TAG, "I/O Exception", e);
            } catch (Exception e) {
                Log.e(CLIENT_TAG, "Error ", e);
            }
            Log.d(CLIENT_TAG, "Client sent message: " + bit.toString());
        }

    }


}
