package main.acceleraudio;

import java.io.IOException;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

public class PlayerService extends Service{
	public static String PLAY_START = "play_start"; 
	public static String PLAY_PAUSE = "play_pause";
	public static String PLAY_STOP = "play_stop";
	public static String SET_LOOP = "set_loop";
	public static String PATH = "path_directory";
	public static String NOTIFICATION = "main.acceleraudio.playerservice";
	public static String CHANGE = "main.acceleraudio.change";
	private String sessionToPlay;
	private String sessionInPlayNow;
	private MediaPlayer myPlayer = null; 
	private boolean isPlaying = false;
	private boolean isLoop = true;
	private int pos = 0;

	@Override 
	public IBinder onBind(Intent intent) 
	{ 
		return null; // Clients can not bind to this service 
	} 

	@Override 
	public int onStartCommand(Intent intent, int flags, int startId) 
	{ 
		if(PLAY_START.equals(intent.getAction())) {
			sessionToPlay = intent.getStringExtra(PATH);
			play(); 
		}
		if(PLAY_PAUSE.equals(intent.getAction())){
			pause();
		}
		if(PLAY_STOP.equals(intent.getAction())){
			stop();
		}
		
		if(SET_LOOP.equals(intent.getAction())){
			setLoop();
		}
		return Service.START_STICKY; 
	}

	private void play() { 
		if(isPlaying && sessionToPlay.equals(sessionInPlayNow))
			return;
		
		if(isPlaying && !sessionToPlay.equals(sessionInPlayNow))
			stop();
		
		try {
			isPlaying = true;
			myPlayer = new MediaPlayer(); 
			myPlayer.setDataSource(sessionToPlay);
			myPlayer.prepare();
			myPlayer.seekTo(pos);
			myPlayer.start();
			myPlayer.setOnCompletionListener(new OnCompletionListener() {				
				@Override
				public void onCompletion(MediaPlayer m) {
					Intent intent = new Intent(NOTIFICATION);
					pos = 0;
					isPlaying = false;
					sendBroadcast(intent);	
					if(isLoop)
						play();
					else
						stopSelf();	
				}
			});
			sessionInPlayNow = sessionToPlay.toString();
			
			//Thread to update the seekbar
			new Thread(
				    new Runnable() {
				        @Override
				        public void run() {
				            while(myPlayer != null && myPlayer.isPlaying()) {
				                 // Updating progress bar
				                 int progress =  myPlayer.getCurrentPosition();
				                 Intent changeProgress = new Intent(CHANGE);
				                 changeProgress.putExtra("current_progress", progress);
				                 sendBroadcast(changeProgress);
				            	
				                        try {
				                            // Sleep for 5 seconds
				                            Thread.sleep(100);
				                        } catch (InterruptedException e) {
				                            Log.d("Sleep seekbar", "sleep failure");
				                        }
				            }

				        }
				    }
				// Starts the thread by calling the run() method in its Runnable
				).start();

		} catch (IOException e) {
			Toast.makeText(getBaseContext(),"prepare failed",
					Toast.LENGTH_SHORT).show();
		}
		
		// Runs this service in the foreground, 
		// supplying the ongoing notification to be shown to the user
		Intent intent = new Intent(this, PlayActivity.class); 
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP); 
		intent.putExtra(PlayActivity.SESSION_NAME, sessionInPlayNow.substring(35, sessionInPlayNow.length()-4));
		intent.putExtra(PlayActivity.AUTOPLAY, true);  //the song starts automatically
		intent.putExtra(PlayActivity.DURATION, myPlayer.getDuration() / 1000);
		PendingIntent pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT); 
		Notification notification = new NotificationCompat.Builder(getApplicationContext())
		.setContentTitle("Stai ascoltando: "+sessionInPlayNow.substring(35, sessionInPlayNow.length()-4))
		.setContentText("Premi per fermare la riproduzione.")
        .setSmallIcon(R.drawable.abc_ic_go)
		.setContentIntent(pi) // Required on Gingerbread and below 
		.build();
		final int notificationID = 1; // An ID for this notification unique within the app 
		startForeground(notificationID, notification);
		Log.d("notification", "create notification");
		
	}
	
	private void pause() { 
		if (isPlaying) { 
			isPlaying = false; 
			myPlayer.pause();
			pos = myPlayer.getCurrentPosition();
			
		} 
	} 

	private void stop() { 
		if (isPlaying) 
		{ 
			isPlaying = false; 
			if (myPlayer != null) { 
				myPlayer.release(); 
				myPlayer = null;
				pos = 0;
			} 
		} 
	} 
	
	private void setLoop() { 
		if(isLoop){
			isLoop = false;
			
		} else{
			isLoop = true;
		} 
	} 

	@Override 
	public void onDestroy() {
		stop();
	}
	

}
