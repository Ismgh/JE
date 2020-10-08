package fr.aladlahcen.streamapp;
/* Alaeddine Hedhly et Lahcen Ait Bella*/

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;
import android.util.Log;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;


public class StreamBTClientAsync extends AsyncTask<String , Integer , Boolean>   {
    private BluetoothActivity app;
    private String destPath;

    byte[] BUFF = new byte[2048];
    BluetoothDevice mdevice;
    public StreamBTClientAsync(BluetoothActivity app, BluetoothDevice mdevice , String destPath) {
		this.destPath = destPath;
		this.app = app;
         this.mdevice = mdevice;
    }

    @Override
    protected Boolean doInBackground(String... params) {

        try {
            BluetoothSocket serverSocket = null;

            // Create a new listening server socket
			serverSocket = mdevice.createInsecureRfcommSocketToServiceRecord(MainActivity.uuid);
			serverSocket.connect();
			Log.d("BluetoothActivity", "after connect");
            InputStream is = serverSocket.getInputStream();
            ObjectInputStream ois = new ObjectInputStream(is);
            WiFiTransferModal  wifiinfo = (WiFiTransferModal) ois.readObject();
			
            File f = new File(destPath + File.separator+ wifiinfo.getFileName()+wifiinfo.getExtension());
            if(f.exists() && !f.isDirectory()) {
                f.delete();
            }
            FileOutputStream fos = new FileOutputStream( destPath + File.separator+ wifiinfo.getFileName()+wifiinfo.getExtension(), true);
			Log.d("StreamBTClient", "wifinfo="+wifiinfo.toString());
            int len = 0;
			int total = 0;
			boolean start = false;
			long startTime = System.currentTimeMillis();
            while((len = is.read(BUFF)) != -1){
              	fos.write(BUFF, 0, len);
				total += len;
				if(total > 512000 && !start) {
					long endTime = System.currentTimeMillis();
					Log.d("BluetoothActivity", "Start video : 512K ok ("+(endTime-startTime)+")");
					app.OnFileInfoAvailable(wifiinfo);
					start = true;
				}
            }
			Log.d("StreamBTClient", "over");
			
        } catch (IOException | ClassNotFoundException  e  ){
			Log.e("BluetoothActivity", e.toString());
        }


        return null;
    }
}
