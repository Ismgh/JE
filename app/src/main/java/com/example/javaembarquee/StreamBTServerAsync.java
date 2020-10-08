package fr.aladlahcen.streamapp;
/* Alaeddine Hedhly et Lahcen Ait Bella*/

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.ContentResolver;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;


public class StreamBTServerAsync extends AsyncTask<String , Integer , Boolean> {

    byte buf[]  = new byte[1024];
    int len;
        private Context context;
        private TextView statusText;
        private String path;
        private int port;
    public final static  String TAG = "StreamBTServerAsync";

        public StreamBTServerAsync(String  path , Context context) {
            super();
            this.context = context;
            len = 0;
            this.path = path;


        }

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                ContentResolver cr = context.getContentResolver();

                /**
                 * Create a server socket and wait for client connections. This
                 * call blocks until a connection is accepted from a client
                 */
                BluetoothServerSocket serverSocket = null;

                // Create a new listening server socket
                try {

                    serverSocket = BluetoothAdapter.getDefaultAdapter().listenUsingRfcommWithServiceRecord("videostream", MainActivity.uuid);

                } catch (IOException e) {
                    Log.e(TAG, "Socket Type: "  + "listen() failed", e);
                }

                BluetoothSocket client = serverSocket.accept();

                WiFiTransferModal wifiinfo = new WiFiTransferModal();

                /**
                 * If this code is reached, a client has connected and transferred data
                 * Save the input stream from the client as a JPEG file
                 */
                File f  = new File((path));

                InputStream is =  new FileInputStream((path));

                wifiinfo.setFileLength(f.length());
                wifiinfo.setFileName(f.getName());
                wifiinfo.setExtension("");

                OutputStream os = client.getOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(os);
                oos.writeObject(wifiinfo);
                 while ((len = is.read(buf)) != -1) {
                     os.write(buf, 0, len);
                }

                is.close();
                os.close();
                serverSocket.close();

            } catch (IOException e) {
                Log.e(fr.aladlahcen.streamapp.StreamBTServerAsync.TAG, e.getMessage());
                return null;
            }
            return true;
        }

        /**
         * Start activity that can handle the JPEG image
         */
        @Override
        protected void onPostExecute(Boolean result) {
            if (result != null) {
                Log.d(fr.aladlahcen.streamapp.StreamBTServerAsync.TAG,"File sent:" + result);
            }
        }


}

