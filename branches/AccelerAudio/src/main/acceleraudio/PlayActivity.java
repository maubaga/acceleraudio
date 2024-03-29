package main.acceleraudio;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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
	private static ImageButton loop;
	private static SeekBar soundProgress;
	private static TextView text_time_passed;
	private boolean isLoop = true;
	private static boolean isOnPause = false;
	private static int duration;

	private BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent.getAction().equals(PlayerService.NOTIFICATION)){
				if(!isLoop)
					stop(null);
			}
			if(intent.getAction().equals(PlayerService.CHANGE)){
				int currentProgress = intent.getIntExtra(PlayerService.CURRENT_PROGRESS, 0);
				soundProgress.setProgress(currentProgress);                    // Update the seekbar.
				text_time_passed.setText(secondToTime(currentProgress));       // Update my "chronometer".
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
			isLoop = savedInstanceState.getBoolean("is_loop_on");
		}


		intent = getIntent();
		appFileDirectory = getApplicationContext().getFilesDir().getPath() + "/"; // "/data/data/main.acceleraudio/files/".
		session_name = intent.getStringExtra(SESSION_NAME);
		isAutoplayEnabled = intent.getBooleanExtra(AUTOPLAY, false);
		duration = intent.getIntExtra(DURATION, -1);
		
		if(!isAutoplayEnabled){
			intent.putExtra(AUTOPLAY, true);
			isOnPause = true;
		}			
	}

	@Override
	protected void onStart() {
		super.onStart();

		text_time_passed.setText(secondToTime(soundProgress.getProgress()));

		if(isAutoplayEnabled && !isOnPause){
			play(null);
		}

		soundProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){ 

			@Override 
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {  // Update the current time.
				text_time_passed.setText(secondToTime(progress)); 
			} 

			@Override 
			public void onStartTrackingTouch(SeekBar seekBar) { 
				// No need to use this.
			} 

			@Override 
			public void onStopTrackingTouch(SeekBar seekBar) {  
				seekTo(soundProgress.getProgress());					   
			} 
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		registerReceiver(receiver, new IntentFilter(PlayerService.NOTIFICATION));
		registerReceiver(receiver, new IntentFilter(PlayerService.CHANGE));
		if(isLoop)
			loop.setImageResource(R.drawable.loop);
		else
			loop.setImageResource(R.drawable.noloop);
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(receiver);
		WidgetIntentReceiver.updateWidgetOnStop(this); // Update the widget.
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

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		// Save isLoop value in saveInstanceState bundle.
		savedInstanceState.putBoolean("is_loop_on", isLoop);
		super.onSaveInstanceState(savedInstanceState);
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

			loop = (ImageButton) rootView.findViewById(R.id.loop);

			ImageView imageView = (ImageView) rootView.findViewById(R.id.thumbnail);
			imageView.setImageURI(Uri.parse(appFileDirectory + session_name + ".png"));

			TextView name = (TextView) rootView.findViewById(R.id.session_name);
			name.setText(session_name);

			TextView duration_text = (TextView) rootView.findViewById(R.id.duration);
			duration_text.setText(secondToTime(duration));

			text_time_passed = (TextView) rootView.findViewById(R.id.text_time_passed);

			soundProgress = (SeekBar) rootView.findViewById(R.id.progress_song);
			soundProgress.setMax(duration);


			return rootView;
		}
	}


	/**
	 * This method is called when the button "Play" is pressed. It starts the PlayService.
	 * @param view the button pressed.
	 */
	public void play(View view){

		ImageButton play = (ImageButton)findViewById(R.id.play);
		ImageButton pause = (ImageButton)findViewById(R.id.pause);
		play.setVisibility(View.GONE);
		pause.setVisibility(View.VISIBLE);

		isOnPause = false;

		// Start the background play.
		Intent startIntent = new Intent(getApplicationContext(),PlayerService.class); 
		startIntent.setAction(PlayerService.PLAY_START);
		startIntent.putExtra(PlayerService.SONG_TO_PLAY, appFileDirectory + session_name + ".wav");
		startService(startIntent);

	}
	
	/**
	 * This method is called when the button "Pause" is pressed. It pauses the song in the PlayService.
	 * @param view the button pressed.
	 */
	public void pause(View view) {

		ImageButton play = (ImageButton)findViewById(R.id.play);
		ImageButton pause = (ImageButton)findViewById(R.id.pause);
		play.setVisibility(View.VISIBLE);
		pause.setVisibility(View.GONE);

		isOnPause = true;

		// Pause the song in background.
		Intent pauseIntent = new Intent(getApplicationContext(),PlayerService.class); 
		pauseIntent.setAction(PlayerService.PLAY_PAUSE);
		startService(pauseIntent);
	}
	
	/**
	 * This method is called when the button "Stop" is pressed. It stops the PlayService.
	 * @param view the button pressed.
	 */
	public void stop(View view) {

		ImageButton play = (ImageButton)findViewById(R.id.play);
		ImageButton pause = (ImageButton)findViewById(R.id.pause);
		play.setVisibility(View.VISIBLE);
		pause.setVisibility(View.GONE);
		
		isOnPause = false;

		// Stop the song in background.
		Intent stopIntent = new Intent(getApplicationContext(), PlayerService.class); 
		stopService(stopIntent);

		finish();
	}
	
	/**
	 * This method is called when the button "Loop" is pressed. It permits to loop or not the song in the PlayService.
	 * @param view the button pressed.
	 */
	public void setLoop(View view) {

		if(isLoop){
			isLoop = false;
			loop.setImageResource(R.drawable.noloop);

		} else{
			isLoop = true;
			loop.setImageResource(R.drawable.loop);			
		}

		// Notify the PlayService that the loop button is pressed.
		Intent loopIntent = new Intent(getApplicationContext(), PlayerService.class); 
		loopIntent.setAction(PlayerService.SET_LOOP);
		startService(loopIntent);
	}


	/**
	 * Pass the time in milliseconds to the PlayService.
	 * @param milliseconds time to seek in milliseconds.
	 */
	private void seekTo(int milliseconds){
		Intent seekIntent = new Intent(getApplicationContext(),PlayerService.class);
		seekIntent.setAction(PlayerService.SEEK_TO);
		seekIntent.putExtra(PlayerService.TIME_TO_SEEK, milliseconds);
		startService(seekIntent);
	}


	/**
	 * This method convert milliseconds into minutes and seconds in the format mm:ss.
	 * @param totalMilliSeconds the milliseconds to convert.
	 * @return A String that represent the time in format mm:ss.
	 */
	public static String secondToTime(int totalMilliSeconds){
		int totalSeconds = totalMilliSeconds / 1000; // Convert from milliseconds to seconds.
		int minutes = totalSeconds / 60;             // Get the minutes.
		int seconds = totalSeconds % 60;    		 // Get the seconds.
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
