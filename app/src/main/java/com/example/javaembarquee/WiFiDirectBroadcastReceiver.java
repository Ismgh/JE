package fr.aladlahcen.streamapp;
/* Alaeddine Hedhly et Lahcen Ait Bella*/

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.Arrays;
import java.util.List;

public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {
	private final static String TAG = "WiFiDirectBroadcastRcvr";
	
	private WifiP2pManager mManager;
	private WifiP2pManager.Channel mChannel;
	private WifiDirectActivity mActivity;
	private ArrayAdapter<String> deviceAdapter;
	
	public WiFiDirectBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel, ArrayAdapter<String> deviceAdapter, WifiDirectActivity activity) {
		super();
		this.mManager = manager;
		this.mChannel = channel;
		this.deviceAdapter = deviceAdapter;
		this.mActivity = activity;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		WifiP2pManager.PeerListListener myPeerListListener = new WifiP2pManager.PeerListListener() {
			@Override
			public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {
				for(WifiP2pDevice device : wifiP2pDeviceList.getDeviceList()) {
//					Log.d(TAG, "device: "+device.toString());
					
					String deviceDesc = device.deviceName+"\n"+device.deviceAddress;
					deviceAdapter.add(deviceDesc);
				}
			}
		};
		String action = intent.getAction();
		
		if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
			// Check to see if Wi-Fi is enabled and notify appropriate activity
			int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
			if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
				// Wifi Direct is enabled
				Log.d(TAG, "WiFi Direct is enabled");
			} else {
				// Wi-Fi Direct is not enabled
				Log.d(TAG, "WiFi Direct is not enabled");
			}
		} else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
			// Call WifiP2pManager.requestPeers() to get a list of current peers
			if (mManager != null) {
				mManager.requestPeers(mChannel, myPeerListListener);
			}
		} else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
			// Respond to new connection or disconnections
		} else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
			// Respond to this device's wifi state changing
		}
	}
}