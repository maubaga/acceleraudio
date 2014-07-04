package main.acceleraudio;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
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
	public static final String SESSION_NAME = "session_name";
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
	private long timeStart = 0;
	private long timeStop = 0;
	private final int aSecond = 1000000;
	private String session_name;

	@Override 
	public IBinder onBind(Intent intent) 
	{ 
		return null; // Clients can not bind to this service.
	} 

	@Override 
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (START.equals(intent.getAction())){ // The start button is pressed.
			if (isOnPause){                     // If it isn't the first time, I don't need to create the sensorManager and the RecordContainer.
				timeStart = SystemClock.elapsedRealtime() - timeStop;
				
				isStart = true;
				isOnPause = false;
			}
			else{                               // If it's the first time than I have to create the sensorManager and the RecordContainer.
				record = new RecordContainer();


				maxRecordTime = intent.getLongExtra(MAX_RECORD_TIME, 30000); // Default time 30 seconds.
				session_name = intent.getStringExtra(SESSION_NAME);
				rate = intent.getIntExtra(RATE, 50);
				
				mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
				mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
				mSensorManager.registerListener(this, mAccelerometer , aSecond/rate);
				
				timeStart = SystemClock.elapsedRealtime();
				isStart = true;
				
				displayNotification();
			}			
		}
		if(PAUSE.equals(intent.getAction())){
			isStart = false;
			isOnPause = true;
			
			timeStop = SystemClock.elapsedRealtime() - timeStart;
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
		// No need to use this.
	}

	@Override
	public void onSensorChanged(SensorEvent event) {

		synchronized (this) {
			if (!isStart)
				return;
			
			checkMaxDuration(); // Check if I reach the max duration of the recording.
			
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
	 * Called when the stop button is pressed. Send the value of the three arrays and the number of samples.
	 */
	private void publishFinishResults() {		
		Intent intent = new Intent(STOP_SERVICE);
		intent.putExtra(X_VALUE, record.getXarray());
		intent.putExtra(Y_VALUE, record.getYarray());
		intent.putExtra(Z_VALUE, record.getZarray());
		intent.putExtra(SIZE, record.getSize());
		intent.putExtra(SESSION_NAME, session_name);
		sendBroadcast(intent);
	}

	/**
	 * Send the three values of the accelerometer.
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
	 * Check if the max duration set in the preference is reached. If the time is over it stops the recording.
	 */
	private void checkMaxDuration(){
		if(SystemClock.elapsedRealtime() - timeStart >= maxRecordTime){
			isStart = false;
			mSensorManager.unregisterListener(this);
			publishFinishResults();
			Toast.makeText(this, getResources().getString(R.string.length_error_part1) + " " + (maxRecordTime / 1000) +
					" " + getResources().getString(R.string.length_error_part2), Toast.LENGTH_LONG).show();
			stopSelf();
		}
	}
	
	/**
	 * Display a simple notification that shows that the recording is running in background.
	 */
	private void displayNotification(){	
		Intent intent = new Intent();
	    intent.setAction(WidgetIntentReceiver.ACTION_WIDGET_STOP);
	    PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	    
		Notification notification = new NotificationCompat.Builder(getApplicationContext())
		.setContentTitle(getResources().getString(R.string.rec_notification_title))
		.setContentText(getResources().getString(R.string.rec_notification_text))
        .setSmallIcon(R.drawable.abc_ic_voice_search)
        .setContentIntent(pendingIntent)
		.build();
		final int notificationID = 2; // An ID for this notification unique within the app.
		startForeground(notificationID, notification);
	}
} 
