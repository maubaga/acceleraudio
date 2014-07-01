package main.acceleraudio;

import java.io.File;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class RecordActivity extends ActionBarActivity{

	private long timeStop = 0;
	private static long baseChronometer = 0;
	private static long maxRecordTime;
	private static int rate;
	private Chronometer chrono;
	private static boolean isStart = false;
	private String session_name;

	private BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent.getAction().equals(RecordService.STOP_SERVICE)){ //The recording is over!
				isStart = false;
				finish();
			}

			if(intent.getAction().equals(RecordService.SENSOR_CHANGE)){ //The values of the sensor are changed
				float x = intent.getFloatExtra(RecordService.X_VALUE, 99);
				float y = intent.getFloatExtra(RecordService.Y_VALUE, 99);
				float z = intent.getFloatExtra(RecordService.Z_VALUE, 99);
				int size = intent.getIntExtra(RecordService.SIZE, 0);

				TextView xTextView = (TextView) findViewById(R.id.x_axis);
				TextView yTextView = (TextView) findViewById(R.id.y_axis);
				TextView zTextView = (TextView) findViewById(R.id.z_axis);				
				View x_bar1 = (View)findViewById(R.id.x_bar1);
				View x_bar2 = (View)findViewById(R.id.x_bar2);
				View y_bar1 = (View)findViewById(R.id.y_bar1);
				View y_bar2 = (View)findViewById(R.id.y_bar2);
				View z_bar1 = (View)findViewById(R.id.z_bar1);
				View z_bar2 = (View)findViewById(R.id.z_bar2);
				TextView notes = (TextView) findViewById(R.id.note);

				xTextView.setText(x + "");
				yTextView.setText(y + "");
				zTextView.setText(z + "");

				int x_bar_height = Math.abs((int) x) * 10;
				int y_bar_height = Math.abs((int) y) * 10;
				int z_bar_height = Math.abs((int) z) * 10;

				x_bar1.setLayoutParams(new LinearLayout.LayoutParams(50, x_bar_height));
				x_bar2.setLayoutParams(new LinearLayout.LayoutParams(50, x_bar_height));
				y_bar1.setLayoutParams(new LinearLayout.LayoutParams(50, y_bar_height));
				y_bar2.setLayoutParams(new LinearLayout.LayoutParams(50, y_bar_height));
				z_bar1.setLayoutParams(new LinearLayout.LayoutParams(50, z_bar_height));
				z_bar2.setLayoutParams(new LinearLayout.LayoutParams(50, z_bar_height));			
				notes.setText(size + "");
			}

		}
	};


	@Override
	public void onBackPressed() {		
		if(isStart){
			isStart = false;
			//This intent delete the background recording
			Intent intent = new Intent(this, RecordService.class);
			intent.setAction(RecordService.CANCEL);
			startService(intent);
			Toast.makeText(this,"Registrazione annullata", Toast.LENGTH_SHORT).show();
		}
		finish(); 
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_record);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
			.add(R.id.container, new PlaceholderFragment()).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.record, menu);
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
			View rootView = inflater.inflate(R.layout.fragment_record,
					container, false);

			Context context = getActivity();
			SharedPreferences preferences = context.getSharedPreferences("Session_Preferences", Context.MODE_PRIVATE); 
			String pref_maxRec = preferences.getString(PrefActivity.KEY_MAX_REC, getResources().getString(R.string.duration1));
			rate = preferences.getInt(PrefActivity.KEY_RATE, 50);

			if (getResources().getString(R.string.duration1).equals(pref_maxRec))
				maxRecordTime = 30 * 1000;
			if (getResources().getString(R.string.duration2).equals(pref_maxRec))
				maxRecordTime = 60 * 1000;
			if (getResources().getString(R.string.duration3).equals(pref_maxRec))
				maxRecordTime = 120 * 1000;
			if (getResources().getString(R.string.duration4).equals(pref_maxRec))
				maxRecordTime = 300 * 1000;
		
			return rootView;
		}
	}
	
	

	@Override
	protected void onResume() {
		super.onResume();
		registerReceiver(receiver, new IntentFilter(RecordService.SENSOR_CHANGE));
		registerReceiver(receiver, new IntentFilter(RecordService.STOP_SERVICE));
		//The recording is already stopped in background, this permits to change activity
		chrono = (Chronometer)findViewById(R.id.chrono);
		chrono.setBase(baseChronometer);
		chrono.start();
		if(isStart && (SystemClock.elapsedRealtime() - chrono.getBase() >= maxRecordTime))
			finish();
		
		if(isStart){
			//This is only for graphical changes
			EditText start_name = (EditText)findViewById(R.id.name);
			TextView start_hint = (TextView)findViewById(R.id.hint);
			ScrollView scroll = (ScrollView)findViewById(R.id.scroll);
			LinearLayout mic = (LinearLayout)findViewById(R.id.mic);
			LinearLayout axis_container = (LinearLayout)findViewById(R.id.axis_container);
			LinearLayout bars = (LinearLayout)findViewById(R.id.bars);
			LinearLayout notes_container = (LinearLayout)findViewById(R.id.notes);
			LinearLayout buttons = (LinearLayout)findViewById(R.id.buttons);
			ImageButton play = (ImageButton)findViewById(R.id.play);
			ImageButton pause = (ImageButton)findViewById(R.id.pause);
			chrono.setVisibility(View.VISIBLE);

			start_name.setVisibility(View.GONE);
			start_hint.setVisibility(View.GONE);
			scroll.setVisibility(View.GONE);
			mic.setVisibility(View.GONE);
			bars.setVisibility(View.VISIBLE);
			axis_container.setVisibility(View.VISIBLE);
			notes_container.setVisibility(View.VISIBLE);
			buttons.setVisibility(View.VISIBLE);		
			play.setVisibility(View.GONE);
			pause.setVisibility(View.VISIBLE);
		}
			
	}
	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(receiver);
	}




	public void preStartRecord(View view){
		EditText name = (EditText) findViewById(R.id.name);
		session_name = name.getText().toString();
		//Check if a name is given
		if(session_name.equals("")){
			Toast.makeText(this,"Inserisci un nome per la sessione", Toast.LENGTH_SHORT).show();
			return;
		}

		if(session_name.substring(0, 1).equals(" ")){
			Toast.makeText(this,"Il nome non può iniziare con uno spazio", Toast.LENGTH_LONG).show();
			return;
		}

		if(session_name.contains("  ")){
			Toast.makeText(this,"Non puoi inserire spazi consecutivi nel nome", Toast.LENGTH_SHORT).show();
			return;
		}	

		File fileCheck = new File(getApplicationContext().getFilesDir().getPath() + "/" + session_name + ".wav");
		if(fileCheck.exists()){

			new AlertDialog.Builder(this)
			.setTitle(session_name + " è già presente")
			.setMessage("Vuoi sovrascrivere il file?")
			.setIcon(null)
			.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) { 
					startRecord(null);
				}
			})
			.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) { 
					return;
				}
			})
			.show();

		}
		else{
			startRecord(view);
		}


	}


	/**
	 * A method called when the button play is pressed, it start the data storing in the object Record
	 * @param view
	 */
	public void startRecord(View view){

		final int rotation = ((WindowManager) this.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
		switch (rotation) {
		case Surface.ROTATION_0:
			setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); 
			break;
		case Surface.ROTATION_90:
			setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); 
			break;
		case Surface.ROTATION_270:
			setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE); 
			break;
		}

		//This starts the background recording
		Intent intent = new Intent(this, RecordService.class);
		intent.setAction(RecordService.START);
		intent.putExtra(RecordService.MAX_RECORD_TIME, maxRecordTime);
		intent.putExtra(RecordService.RATE, rate);
		intent.putExtra(RecordService.SESSION_NAME, session_name);
		startService(intent);
		isStart = true;

		//This is only for graphical changes
		EditText start_name = (EditText)findViewById(R.id.name);
		TextView start_hint = (TextView)findViewById(R.id.hint);
		ScrollView scroll = (ScrollView)findViewById(R.id.scroll);
		LinearLayout mic = (LinearLayout)findViewById(R.id.mic);
		LinearLayout axis_container = (LinearLayout)findViewById(R.id.axis_container);
		LinearLayout bars = (LinearLayout)findViewById(R.id.bars);
		LinearLayout notes_container = (LinearLayout)findViewById(R.id.notes);
		LinearLayout buttons = (LinearLayout)findViewById(R.id.buttons);
		ImageButton play = (ImageButton)findViewById(R.id.play);
		ImageButton pause = (ImageButton)findViewById(R.id.pause);
		chrono = (Chronometer)findViewById(R.id.chrono);
		chrono.setVisibility(View.VISIBLE);
		chrono.setBase(SystemClock.elapsedRealtime() + timeStop);
		chrono.start();
		
		baseChronometer = chrono.getBase();

		start_name.setVisibility(View.GONE);
		start_hint.setVisibility(View.GONE);
		scroll.setVisibility(View.GONE);
		mic.setVisibility(View.GONE);
		bars.setVisibility(View.VISIBLE);
		axis_container.setVisibility(View.VISIBLE);
		notes_container.setVisibility(View.VISIBLE);
		buttons.setVisibility(View.VISIBLE);		
		play.setVisibility(View.GONE);
		pause.setVisibility(View.VISIBLE);

	}

	/**
	 * A method called when the button pause is pressed, this method stop temporarily the data storing in the object Record
	 * @param view
	 */
	public void pauseRecord(View view){

		//This intent put the background recording on pause
		Intent intent = new Intent(this, RecordService.class);
		intent.setAction(RecordService.PAUSE);
		startService(intent);

		ImageButton play = (ImageButton)findViewById(R.id.play);
		ImageButton pause = (ImageButton)findViewById(R.id.pause);
		chrono = (Chronometer)findViewById(R.id.chrono);

		chrono.stop();
		timeStop = chrono.getBase() - SystemClock.elapsedRealtime();

		play.setVisibility(View.VISIBLE);
		pause.setVisibility(View.GONE);
	}
	
	

	/**
	 * A method called when the button stop is pressed, this method show the data stored in the object Record
	 * @param view the button pressed
	 */
	public void stopRecord(View view){	

		ScrollView scroll = (ScrollView)findViewById(R.id.scroll);
		ImageButton play = (ImageButton)findViewById(R.id.play);
		ImageButton pause = (ImageButton)findViewById(R.id.pause);
		chrono = (Chronometer)findViewById(R.id.chrono);
		LinearLayout bars = (LinearLayout)findViewById(R.id.bars);

		chrono.setVisibility(View.GONE);
		chrono.setBase(SystemClock.elapsedRealtime());
		chrono.stop();
		timeStop = 0;

		bars.setVisibility(View.GONE);
		play.setVisibility(View.VISIBLE);
		pause.setVisibility(View.GONE);		
		scroll.setVisibility(View.VISIBLE);

		//This intent stop the background recording and notify to return the values
		Intent intent = new Intent(this, RecordService.class);
		intent.setAction(RecordService.STOP);
		startService(intent);
	}
	
	

}
