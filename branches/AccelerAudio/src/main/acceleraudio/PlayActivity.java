package main.acceleraudio;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class PlayActivity extends ActionBarActivity {
	public static final String AUTOPLAY = "autoplay";
	private static Intent intent;
	private static String session_name;
	private static boolean isAutoplayEnabled;
	private static String appFileDirectory;
	private long chrono_time = 0;
	private static Chronometer chrono;

	private BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			chrono.setBase(SystemClock.elapsedRealtime());
			chrono_time = 0;
			play(null);

		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_play);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
			.add(R.id.container, new PlaceholderFragment()).commit();
		}
		else{
			chrono_time = savedInstanceState.getLong("chronometer_time");
			chrono.setBase(SystemClock.elapsedRealtime() + chrono_time);
		}
	

		intent = getIntent();
		appFileDirectory = getApplicationContext().getFilesDir().getPath() + "/"; // "/data/data/main.acceleraudio/files/"
		session_name = intent.getStringExtra("session_name");
		isAutoplayEnabled = intent.getBooleanExtra(AUTOPLAY, false);
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
			
			chrono = (Chronometer) rootView.findViewById(R.id.chrono);

			ImageView imageView = (ImageView) rootView.findViewById(R.id.thumbnail);
			imageView.setImageURI(Uri.parse(appFileDirectory + session_name + ".png"));

			TextView textView = (TextView) rootView.findViewById(R.id.session_name);			
			textView.setText(session_name.toUpperCase());

			return rootView;
		}
	}


	//read the .wav file
	public void play(View view){

		ImageButton play = (ImageButton)findViewById(R.id.play);
		ImageButton pause = (ImageButton)findViewById(R.id.pause);
		play.setVisibility(View.GONE);
		pause.setVisibility(View.VISIBLE);

		chrono.setBase(SystemClock.elapsedRealtime() + chrono_time);
		chrono.start();

		//background
		Intent startIntent = new Intent(getApplicationContext(),PlayerService.class); 
		startIntent.setAction(PlayerService.PLAY_START);
		startIntent.putExtra(PlayerService.PATH, appFileDirectory + session_name + ".wav");
		startService(startIntent);

	}

	public void pause(View view) {

		ImageButton play = (ImageButton)findViewById(R.id.play);
		ImageButton pause = (ImageButton)findViewById(R.id.pause);
		play.setVisibility(View.VISIBLE);
		pause.setVisibility(View.GONE);

		chrono.stop();
		chrono_time = chrono.getBase() - SystemClock.elapsedRealtime();
		Intent pauseIntent = new Intent(getApplicationContext(),PlayerService.class); 
		pauseIntent.setAction(PlayerService.PLAY_PAUSE);
		startService(pauseIntent);
	}

	public void stop(View view) {

		ImageButton play = (ImageButton)findViewById(R.id.play);
		ImageButton pause = (ImageButton)findViewById(R.id.pause);
		play.setVisibility(View.VISIBLE);
		pause.setVisibility(View.GONE);

		chrono.setBase(SystemClock.elapsedRealtime());
		chrono.stop();
		chrono_time = 0;

		//background
		Intent stopIntent = new Intent(getApplicationContext(), PlayerService.class); 
		stopService(stopIntent);
	}

	@Override
	protected void onStart() {
		super.onStart();
		
		if(isAutoplayEnabled)
			play(null);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		registerReceiver(receiver, new IntentFilter(PlayerService.NOTIFICATION));
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(receiver);
	}
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
	    // Save myVar's value in saveInstanceState bundle
		chrono_time = chrono.getBase() - SystemClock.elapsedRealtime();
	    savedInstanceState.putLong("chronometer_time", chrono_time);
	    super.onSaveInstanceState(savedInstanceState);
	}

}
