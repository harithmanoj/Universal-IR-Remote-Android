package com.gectcr.ece.design.tutorial.networktest;


import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
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

    CopyOnWriteArrayList<NsdServiceInfo> _discoveredServices;

    // constructor
    public NetworkManager(Context aContext) {
        _context = aContext;
        _nsdManager = (NsdManager) _context.getSystemService(Context.NSD_SERVICE);
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

                if(!serviceInfo.getServiceType().equals(SERVICE_TYPE))
                    Log.d(TAG, "Incompatible service type " + serviceInfo.getServiceType());
                else if (serviceInfo.getServiceName().equals(_serviceName))
                    Log.d(TAG, "Same Machine: " + _serviceName);
                else if(!_discoveredServices.contains(serviceInfo))
                    _discoveredServices.add(serviceInfo);
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
                
            }

            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {

            }
        }
    }
}
