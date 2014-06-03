package main.acceleraudio;

import android.content.BroadcastReceiver;
import android.content.Context;
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
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class RecordActivity extends ActionBarActivity{

	private long timeStop = 0;
	private static long maxRecordTime;
	private static int rate;
	private Chronometer chrono;
	private boolean isStart = false;

	private BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent.getIntExtra(RecordService.INTENT_TYPE, -1) == RecordService.STOP_SERVICE){ //The recording is over!
				byte[] x = intent.getByteArrayExtra(RecordService.X_VALUE);
				byte[] y = intent.getByteArrayExtra(RecordService.Y_VALUE);
				byte[] z = intent.getByteArrayExtra(RecordService.Z_VALUE);
				int size = intent.getIntExtra(RecordService.SIZE, 0);

				//				String results = "";
				//				for (int i = 0; i < size; i++)
				//					results = results +" X = " + x[i] + "   Y = " + y[i] + "  Z = " + z[i] +"\n";
				//				TextView textView = (TextView) findViewById(R.id.show_results);
				//				textView.setText(results);

				startCreateActivity(x, y, z, size);	
				finish();
			}
			if(intent.getIntExtra(RecordService.INTENT_TYPE, -1) == RecordService.SENSOR_CHANGE){ //The values of the sensor are changed
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

				//				x_bar_height = Math.abs(Integer.parseInt(xTextView.getText().toString()));
				//				y_bar_height = Integer.parseInt(yTextView.getText().toString());
				//				z_bar_height = Integer.parseInt(zTextView.getText().toString());

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
		//This intent delete the background recording
		Intent intent = new Intent(this, RecordService.class);
		intent.setAction(RecordService.CANCEL);
		startService(intent);
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
			String pref_maxRec = preferences.getString("eTextMaxRec", getResources().getString(R.string.duration1));
			String pref_rate = preferences.getString("eTextSampleRate", getResources().getString(R.string.sample_rate1));
			
			if (getResources().getString(R.string.duration1).equals(pref_maxRec))
				maxRecordTime = 30 * 1000;
			if (getResources().getString(R.string.duration2).equals(pref_maxRec))
				maxRecordTime = 60 * 1000;
			if (getResources().getString(R.string.duration3).equals(pref_maxRec))
				maxRecordTime = 120 * 1000;
			if (getResources().getString(R.string.duration4).equals(pref_maxRec))
				maxRecordTime = 300 * 1000;
			
			if (getResources().getString(R.string.sample_rate1).equals(pref_rate))
				rate = 1;
			if (getResources().getString(R.string.sample_rate2).equals(pref_rate))
				rate = 2;
			if (getResources().getString(R.string.sample_rate4).equals(pref_rate))
				rate = 4;
			if (getResources().getString(R.string.sample_rate6).equals(pref_rate))
				rate = 6;
			if (getResources().getString(R.string.sample_rate8).equals(pref_rate))
				rate = 8;
			
			return rootView;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		registerReceiver(receiver, new IntentFilter(RecordService.NOTIFICATION));
		//The recording is already stopped in background, this permits to change activity
		if(isStart && (SystemClock.elapsedRealtime() - chrono.getBase() >= maxRecordTime))
		stopRecord(null);
	}
	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(receiver);
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
		startService(intent);
		isStart = true;

		//This is only for graphical changes
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

	private void startCreateActivity(byte[] x, byte[] y, byte[] z, int size){
		Intent createIntent = new Intent(this, CreateActivity.class);
		createIntent.putExtra(RecordService.X_VALUE, x);
		createIntent.putExtra(RecordService.Y_VALUE, y);
		createIntent.putExtra(RecordService.Z_VALUE, z);
		createIntent.putExtra(RecordService.SIZE, size);

		startActivity(createIntent);
	}


}
