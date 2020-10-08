package fr.aladlahcen.streamapp;
/* Alaeddine Hedhly et Lahcen Ait Bella*/

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

	public static final String TAG = "MainActivity";
	public static UUID uuid = UUID.fromString("fb36491d-7c21-40ef-9f67-a63237b5bbea");
	String folderPath;
    ListView filelist;
    ArrayAdapter<String>adapter;
    SharedPreferences settings;


	// Boolean telling us whether a download is in progress, so we don't trigger overlapping
	// downloads with consecutive button clicks.
	private boolean mDownloading = false;

	private ProgressDialog pDialog;

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        settings = getSharedPreferences("Settings", Context.MODE_PRIVATE);

        folderPath =  settings.getString("serverDir", Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath());
        filelist = (ListView) findViewById(R.id.FileList);

        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1);
        filelist.setAdapter(adapter);
        filelist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent myintent = new Intent(MainActivity.this,VideoPlayerActivity.class);
                myintent.putExtra("VideoPath" , folderPath + File.separator+ adapter.getItem(position) );
                startActivity(myintent);
            }
        });
		filelist.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
				if (BluetoothAdapter.getDefaultAdapter().getScanMode() !=
						BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
					Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
					discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
					startActivity(discoverableIntent);
				}
				String filename = folderPath + File.separator+ adapter.getItem(position);
				new StreamBTServerAsync( filename, getApplicationContext() ).execute();
				new StreamWIFIServerAsync(8888, filename, getApplicationContext() ).execute();
				Toast.makeText(MainActivity.this, "stream server waiting  " , Toast.LENGTH_SHORT).show();
				return true;
			}
		});

		pDialog = new ProgressDialog(this);
		pDialog.setMessage("Downloading... Please wait...");
		pDialog.setIndeterminate(false);
		pDialog.setCancelable(true);
		pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public void onResume(){
        super.onResume();
        folderPath =  settings.getString("serverDir", Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath());
        refreshFileView();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.fetch_action:
				startDownloadVideo();
                return true;

            case R.id.action_settings:
                startSettings();
                return true;
            case R.id.action_wifidirect:
                startWifiDirect();
                return true;

			case R.id.action_bt:
				startActivity(new Intent(this, BluetoothActivity.class));
				return true;
        }
        return false;
    }

    private void startDownloadVideo() {
		new DownloadFileFromURL().execute("https://ia800201.us.archive.org/22/items/ksnn_compilation_master_the_internet/ksnn_compilation_master_the_internet_512kb.mp4");
	}

    private void startSettings() {
        startActivity(new Intent(this, VideoStreamSettingsActivity.class));
    }

    private void startWifiDirect() {
        startActivity(new Intent(this, WifiDirectActivity.class));
    }

    private void refreshFileView() {
        adapter.clear();
        File folder=new File(folderPath);
        for (final File fileEntry : folder.listFiles()) {
            if (! fileEntry.isDirectory())
                adapter.add(fileEntry.getName());
		}
	}

	private class DownloadFileFromURL extends AsyncTask<String, Integer, String> {
		String filepath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();

		/**
		 * Before starting background thread
		 * */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			Log.d(TAG, "Start downloading");
			pDialog.show();
		}

		/**
		 * Downloading file in background thread
		 * */
		@Override
		protected String doInBackground(String... f_url) {
			int count;
			try {
				filepath += File.separator + f_url[0].substring(f_url[0].lastIndexOf('/') + 1);
				File downloadFile = new File(filepath);
				if(downloadFile.exists()) {
					downloadFile.delete();
				}
				Log.d(MainActivity.TAG, filepath);

				URL url = new URL(f_url[0]);

				URLConnection conection = url.openConnection();
				conection.connect();
				// getting file length
				int fileLength = conection.getContentLength();
				pDialog.setMax(fileLength/1024);
				pDialog.setProgress(0);

				// input stream to read file - with 8k buffer
				InputStream input = new BufferedInputStream(url.openStream(), 8192);

				// Output stream to write file
				OutputStream output = new FileOutputStream(filepath);
				byte data[] = new byte[1024];

				int total = 0;
				while ((count = input.read(data)) != -1) {
					total += count;
					publishProgress(total/1024);

					// writing data to file
					output.write(data, 0, count);
				}

				// flushing output
				output.flush();

				// closing streams
				output.close();
				input.close();

			} catch (Exception e) {
				Log.e("Error: ", e.getMessage());
			}

			return null;
		}

		@Override
		protected void onProgressUpdate(Integer... progress) {
			pDialog.setProgress(progress[0]);
		}

		/**
		 * After completing background task
		 * **/
		@Override
		protected void onPostExecute(String file_url) {
			System.out.println("Downloaded");
			Toast.makeText(MainActivity.this, "Downloaded to "+filepath, Toast.LENGTH_SHORT).show();

			MainActivity.this.refreshFileView();
			pDialog.dismiss();
			pDialog.setMax(0);
			pDialog.setProgress(0);
		}

	}
}
