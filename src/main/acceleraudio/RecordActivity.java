package main.acceleraudio;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

	long timeStop = 0;

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
			}
			if(intent.getIntExtra(RecordService.INTENT_TYPE, -1) == RecordService.SENSOR_CHANGE){ //The values of the sensor are changed
				float x = intent.getFloatExtra(RecordService.X_VALUE, 99);
				float y = intent.getFloatExtra(RecordService.Y_VALUE, 99);
				float z = intent.getFloatExtra(RecordService.Z_VALUE, 99);

				TextView xTextView = (TextView) findViewById(R.id.x_axis);
				TextView yTextView = (TextView) findViewById(R.id.y_axis);
				TextView zTextView = (TextView) findViewById(R.id.z_axis);				
				LinearLayout bars = (LinearLayout)findViewById(R.id.bars);
				View x_bar1 = (View)findViewById(R.id.x_bar1);
				View x_bar2 = (View)findViewById(R.id.x_bar2);
				View y_bar1 = (View)findViewById(R.id.y_bar1);
				View y_bar2 = (View)findViewById(R.id.y_bar2);
				View z_bar1 = (View)findViewById(R.id.z_bar1);
				View z_bar2 = (View)findViewById(R.id.z_bar2);

				xTextView.setText(x + "");
				yTextView.setText(y + "");
				zTextView.setText(z + "");

				int x_bar_height = Math.abs((int)x)*15;
				int y_bar_height = Math.abs((int)y)*15;
				int z_bar_height = Math.abs((int)z)*15;

				//				x_bar_height = Math.abs(Integer.parseInt(xTextView.getText().toString()));
				//				y_bar_height = Integer.parseInt(yTextView.getText().toString());
				//				z_bar_height = Integer.parseInt(zTextView.getText().toString());

				bars.setVisibility(View.VISIBLE);
				x_bar1.setLayoutParams(new LinearLayout.LayoutParams(150, x_bar_height));
				x_bar2.setLayoutParams(new LinearLayout.LayoutParams(150, x_bar_height));
				y_bar1.setLayoutParams(new LinearLayout.LayoutParams(150, y_bar_height));
				y_bar2.setLayoutParams(new LinearLayout.LayoutParams(150, y_bar_height));
				z_bar1.setLayoutParams(new LinearLayout.LayoutParams(150, z_bar_height));
				z_bar2.setLayoutParams(new LinearLayout.LayoutParams(150, z_bar_height));

			}

		}
	};


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
			return rootView;
		}
	}


	@Override
	protected void onResume() {
		super.onResume();
		registerReceiver(receiver, new IntentFilter(RecordService.NOTIFICATION));
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
		startService(intent);

		//This is only for graphical changes
		ScrollView scroll = (ScrollView)findViewById(R.id.scroll);
		LinearLayout mic = (LinearLayout)findViewById(R.id.mic);
		LinearLayout buttons = (LinearLayout)findViewById(R.id.buttons);
		ImageButton play = (ImageButton)findViewById(R.id.play);
		ImageButton pause = (ImageButton)findViewById(R.id.pause);
		Chronometer chrono = (Chronometer)findViewById(R.id.chrono);

		chrono.setVisibility(View.VISIBLE);
		chrono.setBase(SystemClock.elapsedRealtime() + timeStop);
		chrono.start();

		scroll.setVisibility(View.GONE);
		mic.setVisibility(View.GONE);
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
		Chronometer chrono = (Chronometer)findViewById(R.id.chrono);

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
		Chronometer chrono = (Chronometer)findViewById(R.id.chrono);
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
		Intent playIntent = new Intent(this, CreateActivity.class);
		playIntent.putExtra(RecordService.X_VALUE, x);
		playIntent.putExtra(RecordService.Y_VALUE, y);
		playIntent.putExtra(RecordService.Z_VALUE, z);
		playIntent.putExtra(RecordService.SIZE, size);

		startActivity(playIntent);
	}


}
