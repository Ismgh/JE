package fr.aladlahcen.streamapp;

/* Alaeddine Hedhly et Lahcen Ait Bella*/

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

public class BluetoothActivity extends AppCompatActivity {
	
	public final static String TAG = "BluetoothActivity";
	
	String path = "/storage/emulated/0/Download/video.mp4";
	
	ArrayList<BluetoothDevice> deviceAL;
	ListView deviceList;
	ArrayAdapter<String> deviceAdapter;
	
	BluetoothAdapter mBtAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bluetooth);
		
		deviceAL = new ArrayList<>();
		
		mBtAdapter = BluetoothAdapter.getDefaultAdapter();
		if (!mBtAdapter.isEnabled()) {
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivity(enableBtIntent);
		}
		
		deviceList = (ListView) findViewById(R.id.deviceList);
		deviceAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
		deviceList.setAdapter(deviceAdapter);
		deviceList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
				BluetoothDevice device = deviceAL.get(i);
				SharedPreferences settings = getSharedPreferences("Settings", Context.MODE_PRIVATE);

				String folderPath =  settings.getString("serverDir", Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath());
				new StreamBTClientAsync(BluetoothActivity.this , device ,folderPath).execute();

				Log.d(TAG, device.getAddress());
			}
		});
		
		BroadcastReceiver mReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				
				String action = intent.getAction();
				
				if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
					//discovery starts, we can show progress dialog or perform other tasks
					Toast.makeText(BluetoothActivity.this, "Discovery started", Toast.LENGTH_SHORT).show();
					Log.d(TAG, "Discovery started");
				} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
					//discovery finishes, dismis progress dialog
					Toast.makeText(BluetoothActivity.this, "Discovery finished", Toast.LENGTH_SHORT).show();
					Log.d(TAG, "Discovery finished");
				} else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
					//bluetooth device found
					BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
					
					deviceAdapter.add(device.getName()+"\n"+device.getAddress());
					deviceAL.add(device);
				}
			}
		};
		
		IntentFilter filter = new IntentFilter();
		
		filter.addAction(BluetoothDevice.ACTION_FOUND);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		registerReceiver(mReceiver, filter);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		startSearch();
	}
	
	@Override
	public void onDestroy() {
//		unregisterReceiver(mReceiver);
		
		super.onDestroy();
	}
	
	public void startSearch() {
		if(Build.VERSION.SDK_INT > 23) {
			int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
			permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
			if (permissionCheck != 0) {
				
				this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001); //Any number
			}
		}
		else{
			Log.d(TAG, "checkBTPermissions: No need to check permissions. SDK version < LOLLIPOP.");
		}
		
		deviceAdapter.clear();
		deviceAL.clear();
		
		if (mBtAdapter.isDiscovering()) {
			Log.d(TAG, "isDiscovering, cancelling");
			mBtAdapter.cancelDiscovery();
		}
		mBtAdapter.startDiscovery();
		Log.d(TAG, "startDiscovery");
	}

	public void OnFileInfoAvailable(WiFiTransferModal wifiinfo) {
		SharedPreferences settings = getSharedPreferences("Settings", Context.MODE_PRIVATE);

		String folderPath =  settings.getString("serverDir", Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath());
//
//		try {
//			Thread.sleep(5000);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
		
		Intent myintent = new Intent(BluetoothActivity.this, VideoPlayerActivity.class);
		myintent.putExtra("VideoPath" , folderPath + File.separator+ wifiinfo.getFileName());
		startActivity(myintent);

	}
}
