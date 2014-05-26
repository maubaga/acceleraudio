package main.acceleraudio;

import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class PlayActivity extends ActionBarActivity {
	MediaPlayer mp;
	static Intent intent;
	static String session_name;
	static String appFileDirectory;

	FileOutputStream fout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_play);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
			.add(R.id.container, new PlaceholderFragment()).commit();
		}

		intent = getIntent();
		appFileDirectory = getApplicationContext().getFilesDir().getPath() + "/"; // "/data/data/main.acceleraudio/files/"
		session_name = intent.getStringExtra("session_name");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.play, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			Intent intent = new Intent(this, PrefActivity.class);
			startActivity(intent);
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_play, container,
					false);
			
			ImageView imageView = (ImageView) rootView.findViewById(R.id.thumbnail);
			imageView.setImageURI(Uri.parse(appFileDirectory + session_name + ".png"));
			
			TextView textView = (TextView) rootView.findViewById(R.id.session_name);
			textView.setText(session_name);
			
			return rootView;
		}
	}


	//read the .wav file
	public void play(View view){
		if(mp != null)
			mp.stop();

		try{
			mp = new MediaPlayer();
			try {
				mp.setDataSource(appFileDirectory + session_name + ".wav");
				mp.prepare();
				mp.start();
				Button buttonStop = (Button) findViewById(R.id.stop_music);
				buttonStop.setVisibility(View.VISIBLE);
			} catch (IOException e) {
				Toast.makeText(getBaseContext(),"prepare failed",
						Toast.LENGTH_SHORT).show();
			}


		}catch(Exception e){
			Toast.makeText(getBaseContext(),e.toString(),
					Toast.LENGTH_SHORT).show();
		}
	}
	
	public void stop(View view) {
		mp.stop();
		mp = null;
		Button buttonStop = (Button) findViewById(R.id.stop_music);
		buttonStop.setVisibility(View.INVISIBLE);
	}

}
