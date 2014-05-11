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
	public static final String PAUSE = "pause";
	public static final String X_VALUE = "x_value";
	public static final String Y_VALUE = "y_value";
	public static final String Z_VALUE = "z_value";
	public static final String SIZE = "size";
	public static final String NOTIFICATION = "com.example.mybackgroundtry.receiver";

	private SensorManager mSensorManager;
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
		if (START.equals(intent.getAction())){
			if (isOnPause){                     //se era in pausa non devo ricreare tutto
				isStart = true;
				isOnPause = false;
			}
			else{                               //se non era in pausa allora è la prima volta che premo il pulsante play
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
			publishResults();
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

		float x = event.values[0];
		float y = event.values[1];
		float z = event.values[2];

		record.add((byte) (x * 10), (byte) (y * 10), (byte) (z * 10));
	}

	private void publishResults() {
		Intent intent = new Intent(NOTIFICATION);
		intent.putExtra(X_VALUE, record.getXarray());
		intent.putExtra(Y_VALUE, record.getYarray());
		intent.putExtra(Z_VALUE, record.getZarray());
		intent.putExtra(SIZE, record.getSize());
		sendBroadcast(intent);
	}
} 
