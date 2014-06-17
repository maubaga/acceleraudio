package main.acceleraudio;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.SeekBar;
import android.widget.TextView;

public class PlayActivity extends ActionBarActivity {
	public static final String AUTOPLAY = "autoplay";
	public static final String DURATION = "session_duration";
	public static final String SESSION_NAME = "session_name";
	private static Intent intent;
	private static String session_name;
	private static boolean isAutoplayEnabled;
	private static String appFileDirectory;
	private long chrono_time = 0;
	private static Chronometer chrono;
	private static ImageButton loop;
	private static SeekBar soundProgress;
	private boolean isLoop = true;
	private static int duration;

	private BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent.getAction().equals(PlayerService.NOTIFICATION)){
				chrono.setBase(SystemClock.elapsedRealtime());
				chrono_time = 0;
				if(!isLoop)
					stop(null);
				soundProgress.setProgress(0);
			}
			if(intent.getAction().equals(PlayerService.CHANGE)){
				soundProgress.setProgress(intent.getIntExtra("current_progress", -1));
			}
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
			isLoop = savedInstanceState.getBoolean("is_loop_on");
		}
	

		intent = getIntent();
		appFileDirectory = getApplicationContext().getFilesDir().getPath() + "/"; // "/data/data/main.acceleraudio/files/"
		session_name = intent.getStringExtra(SESSION_NAME);
		isAutoplayEnabled = intent.getBooleanExtra(AUTOPLAY, false);
		duration = intent.getIntExtra(DURATION, -1);
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
			loop = (ImageButton) rootView.findViewById(R.id.loop);

			ImageView imageView = (ImageView) rootView.findViewById(R.id.thumbnail);
			imageView.setImageURI(Uri.parse(appFileDirectory + session_name + ".png"));
			
			TextView name = (TextView) rootView.findViewById(R.id.session_name);
			name.setText(session_name);
			
			TextView duration_text = (TextView) rootView.findViewById(R.id.duration);
			duration_text.setText(secondToTime(duration));
			
			soundProgress = (SeekBar) rootView.findViewById(R.id.progress_song);

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
		finish();
	}
	
	public void setLoop(View view) {

		if(isLoop){
			isLoop = false;
			loop.setImageResource(R.drawable.noloop);
			//background
			Intent loopIntent = new Intent(getApplicationContext(),PlayerService.class); 
			loopIntent.setAction(PlayerService.SET_LOOP);
			startService(loopIntent);
			
		} else{
			isLoop = true;
			loop.setImageResource(R.drawable.loop2);
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		
		if(isAutoplayEnabled){
			play(null);
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		registerReceiver(receiver, new IntentFilter(PlayerService.NOTIFICATION));
		if(isLoop)
			loop.setImageResource(R.drawable.loop2);
		else
			loop.setImageResource(R.drawable.noloop);
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
	    savedInstanceState.putBoolean("is_loop_on", isLoop);
	    super.onSaveInstanceState(savedInstanceState);
	}
	
	
	
	public static String secondToTime(int totalSeconds){
		int minutes = totalSeconds / 60;
		int seconds = totalSeconds % 60;
		String secondString = "";
		String minuteString = "";
		
		if(seconds < 10)
			secondString = "0" + seconds;
		else 
			secondString = seconds +"";
		
		if(minutes < 10)
			minuteString = "0" + minutes;
		else 
			minuteString = minutes +"";
		
		return minuteString + ":" + secondString;
	}
	
 
	
}
