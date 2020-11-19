package com.harithmanoj.design.tutorial.network_ping;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.nfc.Tag;
import android.util.Log;

public class NSDNetworkManager {

    Context context;

    NsdManager manager;
    NsdManager.DiscoveryListener discoveryListener;
    NsdManager.RegistrationListener registrationListener;
    NsdManager.ResolveListener resolveListener;

    public static final String SERVICE = "_http._tcp.";

    public static final String TAG = "NSDNetworkManager";
    public String serviceName = "NSDPing";

    NsdServiceInfo serviceInfo;

    public NSDNetworkManager(Context cntxt) {
        context = cntxt;
        manager = (NsdManager)context.getSystemService(Context.NSD_SERVICE);
    }

    public void initializeNsd() {
        initializeResolveListener();
    }

    public void initializeDiscoveryListener(){
        discoveryListener = new NsdManager.DiscoveryListener() {
            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
            }

            @Override
            public void onDiscoveryStarted(String serviceType) {
                Log.d(TAG, "Service Discovery Started");
            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
                Log.i(TAG, "Service discovery stopped");
            }

            @Override
            public void onServiceFound(NsdServiceInfo argserviceInfo) {
                Log.d(TAG, "Service discovery success" + argserviceInfo);
                if (!argserviceInfo.getServiceType().equals(SERVICE)) {
                    Log.d(TAG, "Unknown Service Type: " + argserviceInfo.getServiceType());
                } else if (argserviceInfo.getServiceName().equals(serviceName)) {
                    Log.d(TAG, "Same machine: " + serviceName);
                } else if (argserviceInfo.getServiceName().contains(serviceName)){
                    manager.resolveService(argserviceInfo, resolveListener);
                }
            }

            @Override
            public void onServiceLost(NsdServiceInfo argserviceInfo) {
                Log.e(TAG, "service lost" + argserviceInfo);
                if (serviceInfo == argserviceInfo) {
                    serviceInfo = null;
                }
            }
        };

    }

    public void initializeResolveListener() {
        resolveListener = new NsdManager.ResolveListener() {
            @Override
            public void onResolveFailed(NsdServiceInfo argserviceInfo, int errorCode) {
                Log.e(TAG, "Resolve failed" + errorCode);
            }

            @Override
            public void onServiceResolved(NsdServiceInfo argserviceInfo) {
                Log.e(TAG, "Resolve Succeeded. " + argserviceInfo);

                if (serviceInfo.getServiceName().equals(serviceName)) {
                    Log.d(TAG, "Same IP.");
                    return;
                }
                serviceInfo = argserviceInfo;
            }
        };
    }
    public void initializeRegistrationListener() {
        registrationListener = new NsdManager.RegistrationListener() {

            @Override
            public void onServiceRegistered(NsdServiceInfo NsdServiceInfo) {
                serviceName = NsdServiceInfo.getServiceName();
                Log.d(TAG, "Service registered: " + serviceName);
            }

            @Override
            public void onRegistrationFailed(NsdServiceInfo arg0, int arg1) {
                Log.d(TAG, "Service registration failed: " + arg1);
            }

            @Override
            public void onServiceUnregistered(NsdServiceInfo arg0) {
                Log.d(TAG, "Service unregistered: " + arg0.getServiceName());
            }

            @Override
            public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                Log.d(TAG, "Service unregistration failed: " + errorCode);
            }

        };
    }

    public void registerService(int port) {
        tearDown();  // Cancel any previous registration request
        initializeRegistrationListener();
        NsdServiceInfo tserviceInfo  = new NsdServiceInfo();
        tserviceInfo.setPort(port);
        tserviceInfo.setServiceName(serviceName);
        tserviceInfo.setServiceType(SERVICE);

        manager.registerService(
                tserviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener);

    }

    public void discoverServices() {
        stopDiscovery();  // Cancel any existing discovery request
        initializeDiscoveryListener();
        manager.discoverServices(
                SERVICE, NsdManager.PROTOCOL_DNS_SD, discoveryListener);
    }

    public void stopDiscovery() {
        if (discoveryListener != null) {
            try {
                manager.stopServiceDiscovery(discoveryListener);
            } finally {
            }
            discoveryListener = null;
        }
    }

    public NsdServiceInfo getChosenServiceInfo() {
        return serviceInfo;
    }

    public void tearDown() {
        if (registrationListener != null) {
            try {
                manager.unregisterService(registrationListener);
            } finally {
            }
            registrationListener = null;
        }
    }
}
