package main.acceleraudio;

import android.app.PendingIntent; 
import android.app.Service; 
import android.app.Notification; 
import android.support.v4.app.NotificationCompat; 
import android.content.Intent; 
import android.media.MediaPlayer; 
import android.net.Uri;
import android.os.IBinder; 

public class PlayerService extends Service{
	public static String PLAY_START = "play_start"; 
	public static String PLAY_STOP = "play_stop";
	public static String PATH;
	private MediaPlayer myPlayer = null; 
	private boolean isPlaying = false; 

	@Override 
	public IBinder onBind(Intent intent) 
	{ 
		return null; // Clients can not bind to this service 
	} 

	@Override 
	public int onStartCommand(Intent intent, int flags, int startId) 
	{ 
		if(intent.getBooleanExtra(PLAY_START, false)) play(); 
		return Service.START_STICKY; 
	}

	private void play() { 
		if(isPlaying)
			return; 

		isPlaying = true;
		myPlayer = MediaPlayer.create(this, Uri.parse(PATH)); 
		myPlayer.setLooping(true); 
		myPlayer.start(); 

		// Runs this service in the foreground, 
		// supplying the ongoing notification to be shown to the user 
		Intent intent = new Intent(this, PlayActivity.class); 
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP); 
		PendingIntent pi = PendingIntent.getActivity(this, 0, intent, 0); 
		Notification notification = new NotificationCompat.Builder(getApplicationContext())
		.setContentIntent(pi) // Required on Gingerbread and below 
		.build(); 
		final int notificationID = 1; // An ID for this notification unique within the app 
		startForeground(notificationID, notification);
	}

	private void stop() { 
		if (isPlaying) 
		{ 
			isPlaying = false; 
			if (myPlayer != null) { 
				myPlayer.release(); 
				myPlayer = null; 
			} 
			stopForeground(true); 
		} 
	} 

	@Override 
	public void onDestroy() {
		stop();
	}
}
