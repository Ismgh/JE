package fr.aladlahcen.streamapp;
/* Alaeddine Hedhly et Lahcen Ait Bella*/

import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.InetSocketAddress;
import java.net.Socket;


public class StreamWIFIClientAsync extends AsyncTask<String , Integer , Boolean>   {
     private Socket socket;
    private String host;
    private int port;
    private WifiDirectActivity app;
    private String destPath;
    private int timeout;
    byte[] BUFF = new byte[512];
	
    public StreamWIFIClientAsync(WifiDirectActivity app, String destPath , String host, int port , int timeout){
        this.socket  = new Socket();
        this.host = host;
        this.port = port;
        this.timeout = timeout;
		this.destPath = destPath;
		this.app = app;
    }

    @Override
    protected Boolean doInBackground(String... params) {

        try {
			Log.d("WifiDirectActivity", host+":"+port);
            socket.bind(null);
            socket.connect((new InetSocketAddress(host, port)), timeout);
			Log.d("WifiDirectActivity", "after connect");
            InputStream  is = socket.getInputStream();
            ObjectInputStream ois = new ObjectInputStream(is);
            WiFiTransferModal  wifiinfo = (WiFiTransferModal) ois.readObject();
			Log.d("WifiDirectActivity", "after callback");
            File f = new File(destPath + File.separator+ wifiinfo.getFileName()+wifiinfo.getExtension());
            if(f.exists() && !f.isDirectory()) {
                f.delete();
            }
            FileOutputStream fos = new FileOutputStream( destPath + File.separator+ wifiinfo.getFileName()+wifiinfo.getExtension(), true);
            int len;
			int total = 0;
			boolean start = false;
			long startTime = System.currentTimeMillis();
            while((len = is.read(BUFF)) != -1){
              	fos.write(BUFF, 0, len);
				total += len;
				if(total > 1000000 && !start) {
					// Start after receiving 1Mo
					long endTime = System.currentTimeMillis();
					Log.d("BluetoothActivity", "Start video : 1Mo ok ("+(endTime-startTime)+")");
					app.OnFileInfoAvailable(wifiinfo);
					start = true;
				}
            }
            socket.close();
			
        } catch (IOException | ClassNotFoundException  e  ){
			Log.d("WifiDirectActivity", e.toString());
        }


        return null;
    }
}
