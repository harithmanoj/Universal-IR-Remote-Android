/*

        Copyright (C) 2020  Contributors (in contributors file)

        This program is free software: you can redistribute it and/or modify
        it under the terms of the GNU General Public License as published by
        the Free Software Foundation, either version 3 of the License, or
        (at your option) any later version.

        This program is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
        GNU General Public License for more details.

        You should have received a copy of the GNU General Public License
        along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/
package com.remote.universalirremote;


import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Handler;

import java.util.concurrent.CopyOnWriteArrayList;

/*
        Class to discover and resolve services of same type in the local area network.
        Uses android.os.nsd.NsdManager for implementation.
 */
public class NetworkManager {

    // Context of the activity
    private Context _context;

    // Nsd API
    private NsdManager _nsdManager;

    private NsdManager.ResolveListener _resolveListener;
    private NsdManager.DiscoveryListener _discoveryListener;

    public static final String SERVICE_TYPE = "_http._tcp.";
    public static final String TAG = "NetworkManager";
    public static final String DISCOVERY_HANDLER_KEY = "disc_services";
    public static final String DISCOVERY_HANDLER_LOST_KEY = "lost_services";

    // Resolved service information.
    private NsdServiceInfo _selectedServiceInfo;

    // Handler discovery / loss of service on the network
    protected Handler _discoveryHandler;

    // List of all discovered services
    private CopyOnWriteArrayList<NsdServiceInfo> _discoveredServices;

    public NetworkManager(Context context, Handler discovery) {
        _context = context;
        _nsdManager = (NsdManager) _context.getSystemService(Context.NSD_SERVICE);
        _selectedServiceInfo = null;
        _discoveryHandler = discovery;
        _discoveredServices = new CopyOnWriteArrayList<NsdServiceInfo>();
    }

    public CopyOnWriteArrayList<NsdServiceInfo> getDiscoveredServices() {
        return _discoveredServices;
    }

    public NsdServiceInfo getDiscoveredService(String name, String serviceType ) {

        for (NsdServiceInfo i : _discoveredServices ) {
            if ((i.getServiceType() == serviceType) && (i.getServiceName() == name)) {
                return i;
            }
        }
        return null;
    }

    

}
