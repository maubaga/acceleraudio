package main.acceleraudio;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;

import android.content.Intent;
import android.media.MediaPlayer;
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
import android.widget.Toast;

public class PlayActivity extends ActionBarActivity {
	static MediaPlayer mp;
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
			textView.setText(session_name.toUpperCase());

			return rootView;
		}
	}

	private int time = 0;
	private long chrono_time = 0;
	//read the .wav file
	public void play(View view){
		if(mp != null)
			mp.stop();

		try{
			mp = new MediaPlayer();
			try {
				Chronometer chrono = (Chronometer)findViewById(R.id.chrono);
				ImageButton play = (ImageButton)findViewById(R.id.play);
				ImageButton pause = (ImageButton)findViewById(R.id.pause);
				play.setVisibility(View.GONE);
				pause.setVisibility(View.VISIBLE);
				mp.setDataSource(appFileDirectory + session_name + ".wav");
				mp.prepare();
				mp.seekTo(time);
				mp.start();
				chrono.setBase(SystemClock.elapsedRealtime() + chrono_time);
				chrono.start();

			} catch (IOException e) {
				Toast.makeText(getBaseContext(),"prepare failed",
						Toast.LENGTH_SHORT).show();
			}

		}catch(Exception e){
			Toast.makeText(getBaseContext(),e.toString(),
					Toast.LENGTH_SHORT).show();
		}
	}

	public void pause(View view) {

		Chronometer chrono = (Chronometer)findViewById(R.id.chrono);
		ImageButton play = (ImageButton)findViewById(R.id.play);
		ImageButton pause = (ImageButton)findViewById(R.id.pause);
		play.setVisibility(View.VISIBLE);
		pause.setVisibility(View.GONE);
		if(mp != null){

			mp.pause();
			time = mp.getCurrentPosition();
			chrono.stop();
			chrono_time = chrono.getBase() - SystemClock.elapsedRealtime();
			mp = null;

		}
	}

	public void stop(View view) {

		Chronometer chrono = (Chronometer)findViewById(R.id.chrono);
		ImageButton play = (ImageButton)findViewById(R.id.play);
		ImageButton pause = (ImageButton)findViewById(R.id.pause);
		play.setVisibility(View.VISIBLE);
		pause.setVisibility(View.GONE);
		if(mp != null){

			mp.stop();
			time = 0;
			chrono.setBase(SystemClock.elapsedRealtime());
			chrono.stop();
			chrono_time = 0;
			mp = null;

		}
	}

}
