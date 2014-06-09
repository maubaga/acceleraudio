package main.acceleraudio;

import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.widget.Chronometer;
import android.widget.Toast;

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
	public static final String MAX_RECORD_TIME = "max_record_time";
	public static final String RATE = "rate";
	public static final int STOP_SERVICE  = 1;
	public static final int SENSOR_CHANGE = 0;

	private static SensorManager mSensorManager;
	private Sensor mAccelerometer;
	private static RecordContainer record;
	private static boolean isStart = false;
	private static boolean isOnPause = false;
	private static long maxRecordTime;
	private static int rate;
	private static Chronometer chrono;
	private long timeStop = 0;

	public RecordService() {
		super("RecordService");
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

				rate = intent.getIntExtra(RATE, SensorManager.SENSOR_DELAY_NORMAL);
				mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
				mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
				mSensorManager.registerListener(this, mAccelerometer , 1000/rate);
				
				
				
				maxRecordTime = intent.getLongExtra(MAX_RECORD_TIME, 30000); //default time 30 seconds
				chrono = new Chronometer(getApplicationContext());
				chrono.setBase(SystemClock.elapsedRealtime() + timeStop);
				chrono.start();
				isStart = true;
				
				//notifica
				Intent iNotif = new Intent(this, RecordActivity.class); 
				iNotif.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP); 
				PendingIntent pi = PendingIntent.getActivity(this, 0, iNotif, 0); 
				Notification notification = new NotificationCompat.Builder(getApplicationContext())
				.setContentTitle("Stai registrando")
		        .setSmallIcon(R.drawable.ic_action_mic)
				.setContentIntent(pi) 
				.build();
				final int notificationID = 102584; //ID for this notification 
				startForeground(notificationID, notification);
			}			
		}
		if(PAUSE.equals(intent.getAction())){
			isStart = false;
			isOnPause = true;
			
			chrono.stop();
			timeStop = chrono.getBase() - SystemClock.elapsedRealtime();
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
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// can be safely ignored for this demo
	}

	@Override
	public void onSensorChanged(SensorEvent event) {

		synchronized (this) {
			if (!isStart)
				return;
			
			checkMaxDuration(); //control if I reach the max duration of the recording
			
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
		intent.putExtra(SIZE, record.getSize());
		sendBroadcast(intent);
	}
	
	private void checkMaxDuration(){
		if(SystemClock.elapsedRealtime() - chrono.getBase() >= maxRecordTime){
			isStart = false;
			mSensorManager.unregisterListener(this);
			publishFinishResults();
			Toast.makeText(this,"Raggiunta la durata massima raggiunta di " + (maxRecordTime / 1000) + " secondi.", Toast.LENGTH_LONG).show();
		}
	}
} 
