package fr.aladlahcen.streamapp;
/* Alaeddine Hedhly et Lahcen Ait Bella*/

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;

public class WifiDirectActivity extends AppCompatActivity implements WifiP2pManager.ConnectionInfoListener {
	private static final String TAG = "WifiDirectActivity";
	
	private final IntentFilter intentFilter = new IntentFilter();
	WifiP2pManager mManager;
	WifiP2pManager.Channel mChannel;
	WiFiDirectBroadcastReceiver receiver;

	private WifiP2pInfo info;
	
	ListView deviceList;
	private ArrayAdapter<String> deviceAdapter;
	WifiP2pConfig config;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_wifi_direct);
		
		//  Indicates a change in the Wi-Fi P2P status.
		intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
		
		// Indicates a change in the list of available peers.
		intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
		
		// Indicates the state of Wi-Fi P2P connectivity has changed.
		intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
		
		// Indicates this device's details have changed.
		intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
		
		mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
		mChannel = mManager.initialize(this, getMainLooper(), null);
		
		deviceList = (ListView) findViewById(R.id.deviceList);
		deviceAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
		deviceList.setAdapter(deviceAdapter);
		deviceList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
				String desc = (String) deviceList.getItemAtPosition(i);
				
				config = new WifiP2pConfig();
				config.deviceAddress = desc.split("\n")[1];
				mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {
					@Override
					public void onSuccess() {
						Log.d(TAG, "Connected to "+config.deviceAddress);
						Toast.makeText(WifiDirectActivity.this, "Connected to "+config.deviceAddress, Toast.LENGTH_SHORT).show();

						mManager.requestConnectionInfo(mChannel, WifiDirectActivity.this);
					}
					
					@Override
					public void onFailure(int i) {
						Log.d(TAG, "Failed to connect to "+config.deviceAddress);
					}
				});
				startSearch();
			}
		});
		
		mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
			@Override
			public void onSuccess() {
        		// Success
			}
			
			@Override
			public void onFailure(int reasonCode) {
				// Failure
			}
		});
	}
	
	/** register the BroadcastReceiver with the intent values to be matched */
	@Override
	public void onResume() {
		super.onResume();
		startSearch();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		unregisterReceiver(receiver);
	}
	
	public void startSearch() {
		deviceAdapter.clear();
		receiver = new WiFiDirectBroadcastReceiver(mManager, mChannel, deviceAdapter, this);
		registerReceiver(receiver, intentFilter);

	}

	public void OnFileInfoAvailable(WiFiTransferModal wifiinfo) {
		SharedPreferences settings = getSharedPreferences("Settings", Context.MODE_PRIVATE);

		String folderPath =  settings.getString("serverDir", Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath());
		Intent myintent = new Intent(WifiDirectActivity.this,VideoPlayerActivity.class);
		myintent.putExtra("VideoPath" , folderPath + File.separator+ wifiinfo.getFileName()+wifiinfo.getExtension() );
		startActivity(myintent);

	}

	@Override
	public void onConnectionInfoAvailable(WifiP2pInfo info) {
		SharedPreferences settings = getSharedPreferences("Settings", Context.MODE_PRIVATE);

		String folderPath =  settings.getString("serverDir", Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath());

//		new StreamBTClientAsync(this , folderPath, info.groupOwnerAddress.getHostAddress(), 8888, 5000).execute();
		new StreamWIFIClientAsync(this , folderPath, "192.168.0.20", 8888, 500000000).execute();

	}
}
