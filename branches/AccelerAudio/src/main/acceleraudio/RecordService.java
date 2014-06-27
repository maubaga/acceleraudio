package main.acceleraudio;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.widget.Chronometer;
import android.widget.Toast;

public class RecordService extends Service  implements SensorEventListener {

	public static final String START = "start";
	public static final String STOP = "stop";
	public static final String CANCEL = "cancel";
	public static final String PAUSE = "pause";
	public static final String X_VALUE = "x_value";
	public static final String Y_VALUE = "y_value";
	public static final String Z_VALUE = "z_value";
	public static final String SIZE = "size";
	public static final String STOP_SERVICE  = "main.acceleraudio.stop_service";
	public static final String SENSOR_CHANGE = "main.acceleraudio.sensor_change";
	public static final String MAX_RECORD_TIME = "max_record_time";
	public static final String RATE = "rate";

	private static SensorManager mSensorManager;
	private Sensor mAccelerometer;
	private static RecordContainer record;
	private static boolean isStart = false;
	private static boolean isOnPause = false;
	private static long maxRecordTime;
	private static int rate;
	private static Chronometer chrono;
	private long timeStop = 0;
	private final int aSecond = 1000000;

	@Override 
	public IBinder onBind(Intent intent) 
	{ 
		return null; // Clients can not bind to this service 
	} 

	@Override 
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (START.equals(intent.getAction())){ //the button start is pressed.
			if (isOnPause){                     //isn't the first time, i don't need to create the sensorManager and the RecordContainer
				isStart = true;
				isOnPause = false;
			}
			else{                               //is the first time than I have to create the sensorManager and the RecordContainer
				record = new RecordContainer();

				SharedPreferences preferences = getSharedPreferences("Session_Preferences", Context.MODE_PRIVATE);
				rate = preferences.getInt("sbRate", 1);
				mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
				mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
				mSensorManager.registerListener(this, mAccelerometer , aSecond/rate);
				
				
				
				maxRecordTime = intent.getLongExtra(MAX_RECORD_TIME, 30000); //default time 30 seconds
				chrono = new Chronometer(getApplicationContext());
				chrono.setBase(SystemClock.elapsedRealtime() + timeStop);
				chrono.start();
				isStart = true;
				
				displayNotification();
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
			stopSelf();
		}
		if (CANCEL.equals(intent.getAction())){
			isStart = false;
			if(mSensorManager != null)
				mSensorManager.unregisterListener(this);
			stopSelf();
		}
		return Service.START_STICKY; 
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
		Intent intent = new Intent(STOP_SERVICE);
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
		Intent intent = new Intent(SENSOR_CHANGE);
		intent.putExtra(X_VALUE, x);
		intent.putExtra(Y_VALUE, y);
		intent.putExtra(Z_VALUE, z);
		intent.putExtra(SIZE, record.getSize());
		sendBroadcast(intent);
	}
	
	/**
	 * This method check if the max duration set in the preference is reached. If the time is over it stop the recording.
	 */
	private void checkMaxDuration(){
		if(SystemClock.elapsedRealtime() - chrono.getBase() >= maxRecordTime){
			isStart = false;
			mSensorManager.unregisterListener(this);
			publishFinishResults();
			Toast.makeText(this,"Raggiunta la durata massima raggiunta di " + (maxRecordTime / 1000) + " secondi.", Toast.LENGTH_LONG).show();
			stopSelf();
		}
	}
	
	/**
	 * Display a simple notification that show that the recording is running in background.
	 */
	private void displayNotification(){
//		Notification notification = new NotificationCompat.Builder(getApplicationContext())
//		.setContentTitle("Registrazione AccelerAudio")
//		.setContentText("Premi per fermare la registrazione.")
//        .setSmallIcon(R.drawable.abc_ic_voice_search)
//		.build();
//		
//		NotificationManager mNotificationManager =
//		    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//		mNotificationManager.notify(2, notification);

		
		Notification notification = new NotificationCompat.Builder(getApplicationContext())
		.setContentTitle("Registrazione AccelerAudio")
		.setContentText("Premi per fermare la registrazione.")
        .setSmallIcon(R.drawable.abc_ic_voice_search)
		.build();
		final int notificationID = 2; // An ID for this notification unique within the app 
		startForeground(notificationID, notification);
	}
} 
