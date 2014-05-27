package main.acceleraudio;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class RecordService extends IntentService  implements SensorEventListener {

	public static final String START = "start";
	public static final String STOP = "stop";
	public static final String CANCEL = "cancel";
	public static final String PAUSE = "pause";
	public static final String X_VALUE = "x_value";
	public static final String Y_VALUE = "y_value";
	public static final String Z_VALUE = "z_value";
	public static final String SIZE = "size";
	public static final String NOTIFICATION = "main.acceleraudio.receiver";
	public static final String INTENT_TYPE = "intent_type";
	public static final int STOP_SERVICE  = 1;
	public static final int SENSOR_CHANGE = 0;

	private static SensorManager mSensorManager;
	private Sensor mAccelerometer;
	private static RecordContainer record;
	private static boolean isStart = false;
	private static boolean isOnPause = false;

	public RecordService() {
		super("MyBackgroundService");
	}

	// will be called asynchronously by Android
	@Override
	protected void onHandleIntent(Intent intent) {
		if (START.equals(intent.getAction())){ //the button start is pressed.
			if (isOnPause){                     //isn't the first time, i don't need to create the sensorManager and the RecordContainer
				isStart = true;
				isOnPause = false;
			}
			else{                               //is the first time than I have to create the sensorManager and the RecordContainer
				record = new RecordContainer();

				mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
				mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
				mSensorManager.registerListener(this, mAccelerometer , SensorManager.SENSOR_DELAY_NORMAL);

				isStart = true;
			}			
		}
		if(PAUSE.equals(intent.getAction())){
			isStart = false;
			isOnPause = true;
		}
		if (STOP.equals(intent.getAction())){
			isStart = false;
			mSensorManager.unregisterListener(this);
			publishFinishResults(); 
		}
		if (CANCEL.equals(intent.getAction())){
			isStart = false;
			if(mSensorManager != null)
				mSensorManager.unregisterListener(this);
			record = null; 
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// can be safely ignored for this demo
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (!isStart)
			return;

		synchronized (this) {
			if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
			{
				float x = event.values[0];
				float y = event.values[1];
				float z = event.values[2];

				publishResults(x, y, z);
				record.add((byte) (x * 10), (byte) (y * 10), (byte) (z * 10));
			}
		}		

	}

	/**
	 * Called when the button stop is pressed. This method send the value of the three array and the number of samples.
	 */
	private void publishFinishResults() {		
		Intent intent = new Intent(NOTIFICATION);
		intent.putExtra(INTENT_TYPE, STOP_SERVICE);
		intent.putExtra(X_VALUE, record.getXarray());
		intent.putExtra(Y_VALUE, record.getYarray());
		intent.putExtra(Z_VALUE, record.getZarray());
		intent.putExtra(SIZE, record.getSize());
		sendBroadcast(intent);
	}

	/**
	 * This method send the three value of accelerometer.
	 * @param x the value of x axis.
	 * @param y the value of y axis.
	 * @param z the value of z axis.
	 */
	private void publishResults(float x, float y, float z) {
		Intent intent = new Intent(NOTIFICATION);
		intent.putExtra(INTENT_TYPE, SENSOR_CHANGE);
		intent.putExtra(X_VALUE, x);
		intent.putExtra(Y_VALUE, y);
		intent.putExtra(Z_VALUE, z);
		sendBroadcast(intent);
	}
} 
