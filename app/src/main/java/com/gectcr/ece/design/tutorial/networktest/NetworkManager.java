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

    private void initialiseResolveListener() {
        _resolveListener = new NsdManager.ResolveListener() {
            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                Log.e(TAG, "resolve failed with " + errorCode);
            }

            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {
                Log.i(TAG, "Resolved " + serviceInfo.getServiceName() + " "
                        + serviceInfo.getServiceType());

                if(serviceInfo.getServiceName().equals(_registeredName)) {
                    Log.e(TAG, "Same IP");
                    return;
                }

                _selectedServiceInfo = serviceInfo;
            }
        };
    }

    public void initializeRegistrationListener() {
        _registrationListener = new NsdManager.RegistrationListener() {
            @Override
            public void onRegistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                Log.e(TAG, "Registration failed with " + errorCode);
            }

            @Override
            public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                Log.e(TAG, "UnRegistration failed with " + errorCode);
            }

            @Override
            public void onServiceRegistered(NsdServiceInfo serviceInfo) {
                _registeredName = serviceInfo.getServiceName();
                Log.d(TAG, "Service Registered as " + _registeredName);
                _isRegistered = true;
            }

            @Override
            public void onServiceUnregistered(NsdServiceInfo serviceInfo) {
                Log.d(TAG, "Service " + serviceInfo.getServiceName() + " unregistered");
                _isRegistered = false;
            }
        };
    }

    public void tearDown() {
        if (_registrationListener != null) {
            try {
                _nsdManager.unregisterService(_registrationListener);
            } finally {

            }
            _registrationListener = null;
        }
    }

    public void registerService(int port) {
        tearDown();
        initializeRegistrationListener();
        NsdServiceInfo serviceInfo = new NsdServiceInfo();
        serviceInfo.setPort(port);
        serviceInfo.setServiceName(_registeredName);
        serviceInfo.setServiceType(SERVICE_TYPE);

        _nsdManager.registerService(
                serviceInfo, NsdManager.PROTOCOL_DNS_SD, _registrationListener
        );
    }
}
