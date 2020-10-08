package com.example.javaembarquee;

import android.Manifest;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.Bundle;
import android.net.wifi.WifiManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.view.View;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private long downloadId;
    WifiP2pManager manager;
    WifiP2pManager.Channel channel;
    BroadcastReceiver receiver;
    IntentFilter intentFilter;
    WifiP2pManager.PeerListListener myPeerListListener;
    WifiManager w;
    List<WifiP2pDevice> peers=new ArrayList<WifiP2pDevice>();
    String[] deviceNameArray;
    WifiP2pDevice[] deviceArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        registerReceiver(onDownloadComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        //verifie if wifi is enable
        w=(WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        FloatingActionButton fab2 = findViewById(R.id.fab2);
        //fab2.setVisibility(View.GONE);
        //serveur initialisation
        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);
        receiver = new WiFiDirectBroadcastReceiver(manager, channel, this);
        intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        final FloatingActionButton fab = findViewById(R.id.fab);
        fab.setVisibility(View.GONE);
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //stream server
                /*w.setWifiEnabled(true);
                if (w.isWifiEnabled()) {
                    Toast.makeText(MainActivity.this, "wifi is on", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(MainActivity.this, "wifi is off", Toast.LENGTH_SHORT).show();
                }*/
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
                        @Override
                        public void onSuccess() {

                            Toast.makeText(MainActivity.this, "Devices discovered", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(int reasonCode) {
                            Toast.makeText(MainActivity.this, "Devices not discoverd", Toast.LENGTH_SHORT).show();
                        }
                    });
                    return;
                }
                /*Snackbar.make(view, "the video is being downloaded please be patient", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/
            }
        });
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                beginDownload();
                fab.setVisibility(View.GONE);
                Snackbar.make(view, "the video is being downloaded please be patient", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    private void beginDownload(){
        File file = new File(getExternalFilesDir(null),"test");
        DownloadManager.Request request=null;
        //verifier si la version d'android est plus gande que naugat
        if(android.os.Build.VERSION.SDK_INT>= android.os.Build.VERSION_CODES.N){
            request= new DownloadManager.Request(Uri.parse("https://ia800201.us.archive.org/22/items/ksnn_compilation_master_the_internet/ksnn_compilation_master_the_internet_512kb.mp4"))
                    .setTitle("Test")
                    .setDescription("en-cour de telechargement")
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                    .setDestinationUri(Uri.fromFile(file))
                    .setRequiresCharging(false)
                    .setAllowedOverMetered(true)
                    .setAllowedOverRoaming(true);
            //
        }
        else{
            request= new DownloadManager.Request(Uri.parse("https://ia800201.us.archive.org/22/items/ksnn_compilation_master_the_internet/ksnn_compilation_master_the_internet_512kb.mp4"))
                    .setTitle("Test")
                    .setDescription("en-cour de telechargement")
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                    .setDestinationUri(Uri.fromFile(file))
                    .setAllowedOverRoaming(true);
        }
        DownloadManager downloadManager = (DownloadManager)getSystemService(DOWNLOAD_SERVICE);
        downloadId= downloadManager.enqueue(request);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    //showing download complete message
    private BroadcastReceiver onDownloadComplete =new BroadcastReceiver(){
        @Override
        public  void onReceive(Context context, Intent intent){
            long id=intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID,-1);
            FloatingActionButton fab2 = findViewById(R.id.fab2);
            if(downloadId==id){
                Toast.makeText(MainActivity.this,"Download Complete",Toast.LENGTH_SHORT).show();
                fab2.setVisibility(View.VISIBLE);
            }
        }
    };
    WifiP2pManager.PeerListListener peerListListener=new WifiP2pManager.PeerListListener(){
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peerl) {
        if(!peerl.getDeviceList().equals(peers)){
            peers.clear();
            peers.addAll(peerl.getDeviceList());

            deviceNameArray=new String[peerl.getDeviceList().size()];
            deviceArray = new  WifiP2pDevice[peerl.getDeviceList().size()];
            int index =0;
            for(WifiP2pDevice device:peerl.getDeviceList()){
                deviceNameArray[index]=device.deviceName;
                deviceArray[index]=device;
                index++;
            }
            //ArrayAdapter<String> adapter =new ArrayAdapter<String>(getApplicationContext())
        }
            Toast.makeText(MainActivity.this,"number of peers : "+peers.size(),Toast.LENGTH_SHORT).show();
        }
    };
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver, intentFilter);
    }
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }
    @Override
    protected  void onDestroy(){
        super.onDestroy();
        unregisterReceiver((onDownloadComplete));
    }
}