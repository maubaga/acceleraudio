package main.acceleraudio;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.VideoView;

public class RecordActivity extends ActionBarActivity{

	long timeStop = 0;

	private BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent.getIntExtra(RecordService.INTENT_TYPE, -1) == RecordService.STOP_SERVICE){ //Mi notifica che la registrazione � terminata
				byte[] x = intent.getByteArrayExtra(RecordService.X_VALUE);
				byte[] y = intent.getByteArrayExtra(RecordService.Y_VALUE);
				byte[] z = intent.getByteArrayExtra(RecordService.Z_VALUE);
				int size = intent.getIntExtra(RecordService.SIZE, 0);
				String results = "";
				for (int i = 0; i < size; i++)
					results = results +" X = " + x[i] + "   Y = " + y[i] + "  Z = " + z[i] +"\n";
				TextView textView = (TextView) findViewById(R.id.show_results);
				textView.setText(results);
			}
			if(intent.getIntExtra(RecordService.INTENT_TYPE, -1) == RecordService.SENSOR_CHANGE){ //Mi notifica che i valori del sensore sono cambiati
				float x = intent.getFloatExtra(RecordService.X_VALUE, 99);
				float y = intent.getFloatExtra(RecordService.Y_VALUE, 99);
				float z = intent.getFloatExtra(RecordService.Z_VALUE, 99);
				
				TextView xTextView = (TextView) findViewById(R.id.x_axis);
				TextView yTextView = (TextView) findViewById(R.id.y_axis);
				TextView zTextView = (TextView) findViewById(R.id.z_axis);

				xTextView.setText(x + "");
				yTextView.setText(y + "");
				zTextView.setText(z + "");
				
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
		//Questo intent fa partire la registrazione in background
		Intent intent = new Intent(this, RecordService.class);
		intent.setAction(RecordService.START);
		startService(intent);

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

		//Questo intent notifica al servizio di mettere in pausa la registrazione
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
		//Questo intent notifica al servizio di cessare la registrazione e di restituire il risultato
		Intent intent = new Intent(this, RecordService.class);
		intent.setAction(RecordService.STOP);
		startService(intent);

		ScrollView scroll = (ScrollView)findViewById(R.id.scroll);
		ImageButton play = (ImageButton)findViewById(R.id.play);
		ImageButton pause = (ImageButton)findViewById(R.id.pause);
		Chronometer chrono = (Chronometer)findViewById(R.id.chrono);

		chrono.setVisibility(View.GONE);
		chrono.setBase(SystemClock.elapsedRealtime());
		chrono.stop();
		timeStop = 0;

		play.setVisibility(View.VISIBLE);
		pause.setVisibility(View.GONE);		
		scroll.setVisibility(View.VISIBLE);

	}

}