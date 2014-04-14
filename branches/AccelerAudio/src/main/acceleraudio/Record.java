package main.acceleraudio;


import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
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

public class Record extends ActionBarActivity implements SensorEventListener {

	private SensorManager mSensorManager;
	private Sensor mAccelerometer;
	private static byte[] recordX = new byte[1000];
	private static byte[] recordY = new byte[1000];
	private static byte[] recordZ = new byte[1000];
	private static int recordCount;
	private static boolean isStart;
	long timeStop = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_record);

		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mSensorManager.registerListener(this, mAccelerometer , SensorManager.SENSOR_DELAY_NORMAL);
		recordCount=0;
		isStart = false;

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
			return true;
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

	protected void onResume() {
		super.onResume();
		mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
	}

	protected void onPause() {
		super.onPause();
		mSensorManager.unregisterListener(this);
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// can be safely ignored for this demo
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		TextView tvX= (TextView)findViewById(R.id.x_axis);
		TextView tvY= (TextView)findViewById(R.id.y_axis);
		TextView tvZ= (TextView)findViewById(R.id.z_axis);
		float x = event.values[0];
		float y = event.values[1];
		float z = event.values[2];

		tvX.setText(Float.toString(x));
		tvY.setText(Float.toString(y));
		tvZ.setText(Float.toString(z));

		if (!isStart)
			return;

		try{
			recordX[recordCount] = (byte) (x * 10);
			recordY[recordCount] = (byte) (y * 10);
			recordZ[recordCount] = (byte) (z * 10);
			recordCount++;
		}
		catch (ArrayIndexOutOfBoundsException e){
			//The three array are too short, I need to resize them
			byte[] recordXtmp = new byte[recordX.length * 2];
			byte[] recordYtmp = new byte[recordX.length * 2];
			byte[] recordZtmp = new byte[recordX.length * 2];
			
			for (int i = 0; i < recordX.length; i++){
				recordXtmp[i] = recordX[i];
				recordYtmp[i] = recordY[i];
				recordZtmp[i] = recordZ[i];
			}
			recordX = recordXtmp;
			recordY = recordYtmp;
			recordZ = recordZtmp;
			
			//Now I have add the last value
			recordX[recordCount] = (byte) (x * 10);
			recordY[recordCount] = (byte) (y * 10);
			recordZ[recordCount] = (byte) (z * 10);
			recordCount++;
		}
		

	}

	public void startRecord(View view){
		isStart = true;
		
		ScrollView scroll = (ScrollView)findViewById(R.id.scroll);
		LinearLayout mic = (LinearLayout)findViewById(R.id.mic);
		LinearLayout buttons = (LinearLayout)findViewById(R.id.buttons);
		ImageButton play = (ImageButton)findViewById(R.id.play);
		ImageButton pause = (ImageButton)findViewById(R.id.pause);
		VideoView videoView = (VideoView)findViewById(R.id.bar);
		Chronometer chrono = (Chronometer)findViewById(R.id.chrono);
		
		chrono.setVisibility(View.VISIBLE);
		chrono.setBase(SystemClock.elapsedRealtime() + timeStop);
		chrono.start();
		
		
        MediaController mediaController = new MediaController(this);
         mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);

//        videoView.setVideoPath("/sdcard/Video/Techno_chicken.MP4");

        videoView.start();
		
        scroll.setVisibility(View.GONE);
		mic.setVisibility(View.GONE);
		buttons.setVisibility(View.VISIBLE);		
		play.setVisibility(View.GONE);
		pause.setVisibility(View.VISIBLE);
//		bars.setVisibility(View.VISIBLE);
	}

	public void pauseRecord(View view){
		isStart = false;
		
		ImageButton play = (ImageButton)findViewById(R.id.play);
		ImageButton pause = (ImageButton)findViewById(R.id.pause);
		Chronometer chrono = (Chronometer)findViewById(R.id.chrono);
	
		chrono.stop();
		timeStop = chrono.getBase() - SystemClock.elapsedRealtime();
		
		play.setVisibility(View.VISIBLE);
		pause.setVisibility(View.GONE);
	}

	public void stopRecord(View view){
		isStart = false;
		String result = "";
		for(int j = 0; j < recordCount; j++)
			result = result +"x= " + recordX[j] + "    y= " + recordY[j] + "    z= " + recordZ[j] +"\n";
		
		recordCount = 0; //Reset the count, so if I press play again the recording restart
		
		ScrollView scroll = (ScrollView)findViewById(R.id.scroll);
		TextView textView = (TextView) findViewById(R.id.show_results);		
		ImageButton play = (ImageButton)findViewById(R.id.play);
		ImageButton pause = (ImageButton)findViewById(R.id.pause);
		LinearLayout bars = (LinearLayout)findViewById(R.id.bars);
		Chronometer chrono = (Chronometer)findViewById(R.id.chrono);
		
		chrono.setVisibility(View.GONE);
		chrono.setBase(SystemClock.elapsedRealtime());
		chrono.stop();
		timeStop = 0;
		
		play.setVisibility(View.VISIBLE);
		pause.setVisibility(View.GONE);		
		scroll.setVisibility(View.VISIBLE);
		bars.setVisibility(View.GONE);
		
		textView.setText(result);
	}

}
