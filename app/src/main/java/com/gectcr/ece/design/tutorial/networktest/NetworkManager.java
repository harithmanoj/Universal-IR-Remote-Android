package com.gectcr.ece.design.tutorial.networktest;


import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;


import java.util.concurrent.CopyOnWriteArrayList;

public class NetworkManager {

    // Application info
    Context _context;

    // Manage all NSD functionality
    NsdManager _nsdManager;

    // Callback interfaces
    NsdManager.ResolveListener _resolveListener;
    NsdManager.DiscoveryListener _discoveryListener;
    NsdManager.RegistrationListener _registrationListener;

    public static final String SERVICE_TYPE = "_http._tcp.";
    public static final String TAG = "NetworkManager";

    public String _serviceName = "NSDPing";

    NsdServiceInfo _serviceInfo;
    boolean _isResolved;
    boolean _isRegistered;

    static Handler _discoveryHandler;

    CopyOnWriteArrayList<NsdServiceInfo> _discoveredServices;

    // constructor
    public NetworkManager(Context aContext, Handler discoveryhandler) {
        _context = aContext;
        _nsdManager = (NsdManager) _context.getSystemService(Context.NSD_SERVICE);
        _isResolved = false;
        _isRegistered = false;
        _discoveryHandler = discoveryhandler;
    }

    public void initializeDiscoveryListener()
    {
        _discoveryListener = new NsdManager.DiscoveryListener() {
            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery start failed error code: " + errorCode);
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery stop failed error code: " + errorCode);
            }

            @Override
            public void onDiscoveryStarted(String serviceType) {
                Log.d(TAG, "service Discovery started");
            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
                Log.i(TAG, "service Discovery stopped");
            }

            @Override
            public void onServiceFound(NsdServiceInfo serviceInfo) {
                Log.d(TAG, "Servics found: " + serviceInfo);

                if(!serviceInfo.getServiceType().equals(SERVICE_TYPE)) {
                    Log.d(TAG, "Incompatible service type " + serviceInfo.getServiceType());
                } else if (serviceInfo.getServiceName().equals(_serviceName)) {
                    Log.d(TAG, "Same Machine: " + _serviceName);
                } else if(!_discoveredServices.contains(serviceInfo)) {
                    _discoveredServices.add(serviceInfo);
                    CharSequence[] allServices = new CharSequence[_discoveredServices.size()];

                    for (int i = 0; i < _discoveredServices.size(); ++i) {
                        allServices[i] = _discoveredServices.get(i).getServiceName() + " " + _discoveredServices.get(i).getServiceType();
                    }

                    Bundle msgBundle = new Bundle();
                    msgBundle.putCharSequenceArray("services", allServices);
                    Message message = new Message();
                    message.setData(msgBundle);
                    _discoveryHandler.sendMessage(message);
                }
            }

            @Override
            public void onServiceLost(NsdServiceInfo serviceInfo) {
                _discoveredServices.remove(serviceInfo);
                if(_serviceInfo == serviceInfo)
                    _serviceInfo = null;
            }
        };
    }

    public void initializeResolveListener()
    {
        _resolveListener = new NsdManager.ResolveListener() {
            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                Log.e(TAG, "Resolve failed with " + errorCode);
            }

            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {
                Log.d(TAG, "Resolve successfull " + serviceInfo);

                if(serviceInfo.getServiceName().equals(_serviceName)) {
                    Log.e(TAG, "Same IP");
                    _isResolved = false;
                    return;
                }
                _serviceInfo = serviceInfo;
                _isResolved = true;
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
                _serviceName = serviceInfo.getServiceName();
                Log.d(TAG, "Service Registered as " + _serviceName);
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
        serviceInfo.setServiceName(_serviceName);
        serviceInfo.setServiceType(SERVICE_TYPE);

        _nsdManager.registerService(
                serviceInfo, NsdManager.PROTOCOL_DNS_SD, _registrationListener
        );
    }

    public void stopDiscovery() {
        if(_discoveryListener != null) {
            try {
                _nsdManager.stopServiceDiscovery(_discoveryListener);
            } finally {

            }
            _discoveryListener = null;
        }
    }

    public void discoverServices() {
        stopDiscovery();
        initializeDiscoveryListener();
        _nsdManager.discoverServices(
                SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, _discoveryListener
        );
    }

    public NsdServiceInfo getChosenServiceInfo() {
        return _serviceInfo;
    }

    public void resolveService(NsdServiceInfo service) {
        _nsdManager.resolveService(service,_resolveListener);
    }
}
