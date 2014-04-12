package main.acceleraudio;



import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class Record extends ActionBarActivity implements SensorEventListener {

	private SensorManager mSensorManager;
	private Sensor mAccelerometer;
	private static byte[] recordX = new byte[1000];
	private static byte[] recordY = new byte[1000];
	private static byte[] recordZ = new byte[1000];
	private static int recordCount;
	private static boolean isStart;

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

		recordX[recordCount] = (byte) x;
		recordY[recordCount] = (byte) y;
		recordZ[recordCount] = (byte) z;
		recordCount++;

	}

	public void startRecord(View view){
		isStart = true;
		String result = "La registrazione è ora partita,"
				+ " premere pausa per fermare momentaneamente la registrazione o stop per fermarla";

		TextView textView = (TextView) findViewById(R.id.show_results);
		textView.setText(result);
	}

	public void pauseRecord(View view){
		isStart = false;
		String result = "La registrazione è ora in pausa,"
				+ " premi start per coninuare o stop per terminare e visualizzare i dati registrati";

		TextView textView = (TextView) findViewById(R.id.show_results);
		textView.setText(result);
	}

	public void stopRecord(View view){
		isStart = false;
		String result = "";
		for(int j = 0; j < recordCount; j++)
			result = result +"x=" + recordX[j] + " y=" + recordY[j] + " z=" + recordZ[j] +"\n";

		TextView textView = (TextView) findViewById(R.id.show_results);
		textView.setText(result);
	}

}
