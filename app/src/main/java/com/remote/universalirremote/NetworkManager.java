//
//
//        Copyright (C) 2020  Contributors (in contributors file)
//
//        This program is free software: you can redistribute it and/or modify
//        it under the terms of the GNU General Public License as published by
//        the Free Software Foundation, either version 3 of the License, or
//        (at your option) any later version.
//
//        This program is distributed in the hope that it will be useful,
//        but WITHOUT ANY WARRANTY; without even the implied warranty of
//        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//        GNU General Public License for more details.
//
//        You should have received a copy of the GNU General Public License
//        along with this program.  If not, see <https://www.gnu.org/licenses/>.
//
package com.remote.universalirremote;


import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.util.concurrent.CopyOnWriteArrayList;


//        Class to discover and resolve services of same type in the local area network.
//        Uses android.os.nsd.NsdManager for implementation.
//
//        Usage:
//
//        1) To Discover Services and resolve connection :
//            * Construct with context and a discovery handler.
//            * Select a service from list of services ( getChosenServiceInfo() )
//            * call resolveServices with selected service
//
//        2) To Ensure selected service remains visible :
//            * Construct with context and a discovery handler.
//            * start discovery
//            * pass service to setChosenServiceInfo
//            * if it returns false, the service is no longer visible
//            * discovery handler will be notified of all lost services
//            * if service is present now and later lost, handle it in the discoveryHandler
//                            ( graceful exit or crash and burn or ignore)


public class NetworkManager {


    // Nsd API
    private NsdManager _nsdManager;

    private NsdManager.ResolveListener _resolveListener;
    private NsdManager.DiscoveryListener _discoveryListener;

    public static final String SERVICE_TYPE = "_http._tcp.";
    public static final String TAG = "NetworkManager";

    // Keys for Bundle passed in _discoveryHandler
    public static final String DISCOVERED_SERVICE_NAME = "disc.serv.name";

    // Key and result of Bundle passed in _discoveryHandler to identify if service lost or found.
    public static final String DISCOVER_OP = "disc.op";
    public static final int DISCOVER_NEW = 1;
    public static final int DISCOVER_LOST = 2;
    public static final int DISCOVER_REFRESH = 3;

    // Resolved service information.
    private NsdServiceInfo _selectedServiceInfo;

    // Handler discovery / loss of service on the network
    protected Handler _discoveryHandler;

    // List of all discovered services
    private CopyOnWriteArrayList<NsdServiceInfo> _discoveredServices;

    // Has service been resolved?
    public boolean _isResolved;

    // Object to wait on
    public final Object _waitForResolution = new Object();


    // Constructor, pass context and handler to handle services found or lost.
    public NetworkManager(Context context, Handler discovery) {
        _nsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
        _selectedServiceInfo = null;
        _discoveryHandler = discovery;
        _discoveredServices = new CopyOnWriteArrayList<>();
    }

    // get list of all discovered services
    public CopyOnWriteArrayList<NsdServiceInfo> getDiscoveredServices() {
        return _discoveredServices;
    }

    // Returns service info of a service with name given, null if not found.
    // if discovery is stopped, start discovery and try again.
    public NsdServiceInfo getDiscoveredServices(String name ) {

        for (NsdServiceInfo i : _discoveredServices ) {
            if (i.getServiceName().equals(name)) {
                return i;
            }
        }
        return null;
    }


    // Get Service info of chosen service
    public NsdServiceInfo getChosenServiceInfo() {
        return _selectedServiceInfo;
    }

    // Set service info of previously chosen service
    // returns false if input is null.
    // sets info but returns false if it is not present in list of services
    // start discover and try again to check with current discovered services
    public boolean setChosenServiceInfo( NsdServiceInfo service ) {
        if (service == null)
            return false;
        if (!_discoveredServices.contains(service))
            return false;
        _selectedServiceInfo = service;
        return true;
    }

    // initialise discovery listener ( push messages to handler, logging)
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
				if(_discoveredServices.contains(serviceInfo))
					return;
                _discoveredServices.add(serviceInfo);
                if ((_discoveryHandler != null)) {
                    Bundle msgBundle = new Bundle();
                    msgBundle.putInt(DISCOVER_OP, DISCOVER_NEW);
                    msgBundle.putString(DISCOVERED_SERVICE_NAME, serviceInfo.getServiceName());
                    Message msg = new Message();
                    msg.setData(msgBundle);
                    _discoveryHandler.sendMessage(msg);

                }
            }

            @Override
            public void onServiceLost(NsdServiceInfo serviceInfo) {
                Log.i(TAG, serviceInfo.getServiceName()
                        + " " + serviceInfo.getServiceType() + " lost");
				if(!_discoveredServices.contains(serviceInfo))
					return;
                _discoveredServices.remove(serviceInfo);
                if ((_discoveryHandler != null)) {
                    Bundle msgBundle = new Bundle();
                    msgBundle.putInt(DISCOVER_OP, DISCOVER_LOST);
                    msgBundle.putString(DISCOVERED_SERVICE_NAME, serviceInfo.getServiceName());
                    Message msg = new Message();
                    msg.setData(msgBundle);
                    _discoveryHandler.sendMessage(msg);
                }
            }
        };
    }

    // initialise resolve listener (set selected service info)
    private void initialiseResolveListener() {
        _resolveListener = new NsdManager.ResolveListener() {
            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                synchronized (_waitForResolution) {
                    _isResolved = true;
                    _selectedServiceInfo = null;
                    _waitForResolution.notifyAll();
                }
                Log.e(TAG, "resolve failed with " + errorCode);
            }

            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {
                Log.i(TAG, "Resolved " + serviceInfo.getServiceName() + " "
                        + serviceInfo.getServiceType());
                synchronized (_waitForResolution) {
                    _isResolved = true;
                    _selectedServiceInfo = serviceInfo;
                    _waitForResolution.notifyAll();
                }
            }
        };
    }

    // stop discovery
    public void stopDiscover() {
        if ((_discoveryListener != null) && (_nsdManager != null)) {
            _nsdManager.stopServiceDiscovery(_discoveryListener);
            _discoveryListener = null;
        }
    }

    // start discovery aborts if null _nsdManager
    public void discoverServices() {
        stopDiscover();
        if (_nsdManager != null ) {
            initialiseDiscoveryListener();
            _nsdManager.discoverServices(SERVICE_TYPE,
                    NsdManager.PROTOCOL_DNS_SD, _discoveryListener);
        }
    }

    // resolve service passed.
    // returns false if _nsdManager is null
    // returns false if service is not found in list of discovered services
    // (start discovery to refresh list if discovery is not running)
    public boolean resolveServices(NsdServiceInfo service) {
        if(_nsdManager == null)
            return false;

        if (!_discoveredServices.contains(service)) {
            Log.e(TAG," The service " + service.getServiceName() + " is no longer visible");
            return false;
        }

        if (_resolveListener == null)
            initialiseResolveListener();
        _isResolved = false;
        _nsdManager.resolveService(service, _resolveListener);
        return true;
    }


}
