package com.gectcr.ece.design.tutorial.networktest;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.util.concurrent.CopyOnWriteArrayList;

public class NetworkManager {

    private Context _context;

    private NsdManager _nsdManager;

    private NsdManager.RegistrationListener _registrationListener;
    private NsdManager.ResolveListener _resolveListener;
    private NsdManager.DiscoveryListener _discoveryListener;

    public static final String SERVICE_TYPE = "_http._tcp.";
    public static final String TAG = "NetworkManager";
    public static final String DISCOVERY_HANDLER_KEY = "disc_services";
    public static final String DISCOVERY_HANDLER_LOST_KEY = "lost_services";

    private  String _registeredName;
    private  boolean _isRegistered;

    private NsdServiceInfo _selectedServiceInfo;

    protected Handler _discoveryHandler;

    private CopyOnWriteArrayList<NsdServiceInfo> _discoveredServices;

    public CopyOnWriteArrayList<NsdServiceInfo> getDiscoveredServices() {
        return _discoveredServices;
    }

    public NetworkManager(Context context, Handler discoveryHandler) {
        _context = context;
        _nsdManager = (NsdManager) _context.getSystemService(Context.NSD_SERVICE);
        _isRegistered = false;
        _discoveryHandler = discoveryHandler;
        _registeredName = "Ping Server " + Build.MODEL;
        _discoveredServices = new CopyOnWriteArrayList<NsdServiceInfo>();
        _selectedServiceInfo = null;
    }


    public NsdServiceInfo getChosenServiceInfo() {
        return _selectedServiceInfo;
    }

    public boolean isRegistered() {
        return _isRegistered;
    }

    public String getRegisteredName() {
        return _registeredName;
    }

    private void initialiseDiscoveryListener() {
        _discoveryListener = new NsdManager.DiscoveryListener() {
            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery Start Failed " + errorCode);
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery Stop Failed " + errorCode);
            }

            @Override
            public void onDiscoveryStarted(String serviceType) {
                Log.e(TAG, "Discovery Started ");
            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
                Log.e(TAG, "Discovery Stopped ");
            }

            @Override
            public void onServiceFound(NsdServiceInfo serviceInfo) {
                Log.i(TAG, serviceInfo.getServiceName()
                        + " " + serviceInfo.getServiceType() + " found");
                _discoveredServices.add(serviceInfo);
                if(_discoveryHandler != null) {
                    Bundle msgBundle = new Bundle();
                    msgBundle.putString(DISCOVERY_HANDLER_KEY,
                            serviceInfo.getServiceName() + " " + serviceInfo.getServiceType());
                    Message msg = new Message();
                    msg.setData(msgBundle);
                    _discoveryHandler.sendMessage(msg);
                }
            }

            @Override
            public void onServiceLost(NsdServiceInfo serviceInfo) {
                Log.i(TAG, serviceInfo.getServiceName()
                        + " " + serviceInfo.getServiceType() + " lost");
                _discoveredServices.remove(serviceInfo);
                if(_discoveryHandler != null) {
                    Bundle msgBundle = new Bundle();
                    msgBundle.putString(DISCOVERY_HANDLER_LOST_KEY,
                            serviceInfo.getServiceName() + " " + serviceInfo.getServiceType());
                    Message msg = new Message();
                    msg.setData(msgBundle);
                    _discoveryHandler.sendMessage(msg);
                }
            }
        };
    }
}
