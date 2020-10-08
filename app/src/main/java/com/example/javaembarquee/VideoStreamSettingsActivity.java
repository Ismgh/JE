package fr.aladlahcen.streamapp;
/* Alaeddine Hedhly et Lahcen Ait Bella*/

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import net.rdrei.android.dirchooser.DirectoryChooserActivity;
import net.rdrei.android.dirchooser.DirectoryChooserConfig;

public class VideoStreamSettingsActivity extends AppCompatActivity {
	private static final String TAG = "VideoStreamSettingsAct";
	static final int SELECT_DIR_REQUEST_CODE = 0;
	
	Switch enableServerSwitch;
	Button chooseDirButton;
	TextView serverDirTextView;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_stream_settings);
	
		final SharedPreferences settings = getSharedPreferences("Settings", Context.MODE_PRIVATE);
	
		// Switch to turn on/off the server
		enableServerSwitch = (Switch) findViewById(R.id.enableServerSwitch);
		enableServerSwitch.setChecked(settings.getBoolean("enableServer", false));
		enableServerSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
//				SharedPreferences settings = getSharedPreferences("Settings", Context.MODE_PRIVATE);
				SharedPreferences.Editor editor = settings.edit();
				editor.putBoolean("enableServer", b);
				editor.apply();
				
				// Start server
			}
		});
	
		// Button to change the server directory
		serverDirTextView = (TextView) findViewById(R.id.directoryTextView);
		chooseDirButton = (Button) findViewById(R.id.chooseDirButton);
	
		final String defaultDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
		// We get the saved serverDirTextView, or a default one
		if(! settings.contains("serverDir")) {
			serverDirTextView.setText(defaultDir);
			SharedPreferences.Editor editor = settings.edit();
			editor.putString("serverDir", defaultDir);
			editor.apply();
		}
		else {
			serverDirTextView.setText(settings.getString("serverDir", defaultDir));
		}
	
		chooseDirButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
//				Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
//				intent.addCategory(Intent.CATEGORY_OPENABLE);
//				intent.setType(DocumentsContract.Document.MIME_TYPE_DIR);
//				startActivityForResult(intent, SELECT_DIR_REQUEST_CODE);
				
				final Intent chooserIntent = new Intent(VideoStreamSettingsActivity.this, DirectoryChooserActivity.class);
				
				final DirectoryChooserConfig config = DirectoryChooserConfig.builder()
						.newDirectoryName("nouveau dossier")
						.allowReadOnlyDirectory(true)
						.allowNewDirectoryNameModification(true)
						.build();
				
				chooserIntent.putExtra(DirectoryChooserActivity.EXTRA_CONFIG, config);

				// REQUEST_DIRECTORY is a constant integer to identify the request, e.g. 0
				startActivityForResult(chooserIntent, SELECT_DIR_REQUEST_CODE);
			}
		});
    }
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		switch(requestCode) {
			case SELECT_DIR_REQUEST_CODE:
				if (resultCode == DirectoryChooserActivity.RESULT_CODE_DIR_SELECTED) {
					String selectedDir = data.getStringExtra(DirectoryChooserActivity.RESULT_SELECTED_DIR);
					Log.d(TAG, "Location as : "+selectedDir);
					// Changes the server directory
					SharedPreferences settings = getSharedPreferences("Settings", Context.MODE_PRIVATE);
					SharedPreferences.Editor editor = settings.edit();
					editor.putString("serverDir", selectedDir);
					serverDirTextView.setText(selectedDir);
					editor.apply();
				}
				break;
		}
	}
}
