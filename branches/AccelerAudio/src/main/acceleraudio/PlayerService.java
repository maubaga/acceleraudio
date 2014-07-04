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
	public final static String PLAY_START = "play_start"; 
	public final static String PLAY_PAUSE = "play_pause";
	public final static String PLAY_STOP = "play_stop";
	public final static String SEEK_TO = "seek_to";
	public final static String TIME_TO_SEEK = "time_to_seek";
	public final static String SET_LOOP = "set_loop";
	public final static String SONG_TO_PLAY = "song_to_play";
	public final static String NOTIFICATION = "main.acceleraudio.playerservice";
	public final static String CHANGE = "main.acceleraudio.change";
	public final static String CURRENT_PROGRESS = "current_progress";
	private String sessionToPlay;
	private static String sessionInPlayNow;
	private MediaPlayer myPlayer = null; 
	private boolean isPlaying = false;
	private boolean isLoop = true;
	private int position = 0;

	@Override 
	public IBinder onBind(Intent intent) 
	{ 
		return null; // Clients can not bind to this service.
	} 

	@Override 
	public int onStartCommand(Intent intent, int flags, int startId) 
	{ 
		if(PLAY_START.equals(intent.getAction())) {
			sessionToPlay = intent.getStringExtra(SONG_TO_PLAY);
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

		if(SEEK_TO.equals(intent.getAction())) {
			int timeToSeek = intent.getIntExtra(TIME_TO_SEEK, 0);
			seekTo(timeToSeek); 
		}

		return Service.START_STICKY; 
	}
	
	/**
	 * This method is called when the button "Play" is pressed. It starts the song.
	 */
	private void play() { 
		if(isPlaying && sessionToPlay.equals(sessionInPlayNow))
			return;

		if(!sessionToPlay.equals(sessionInPlayNow))
			stop();

		try {
			myPlayer = new MediaPlayer(); 
			myPlayer.setDataSource(sessionToPlay);
			myPlayer.prepare();
			myPlayer.seekTo(position);
			myPlayer.start();
			myPlayer.setOnCompletionListener(new OnCompletionListener() {				
				@Override
				public void onCompletion(MediaPlayer m) {
					Intent intent = new Intent(NOTIFICATION);
					position = 0;
					isPlaying = false;
					sendBroadcast(intent);	
					if(isLoop)
						play();
					else
						stopSelf();	
				}
			});
			isPlaying = true;
			sessionInPlayNow = sessionToPlay.toString();

			// Thread to update the seekbar.
			new Thread(
					new Runnable() {
						@Override
						public void run() {
							while(myPlayer != null && isPlaying) {
								// Updating progress bar.
								int progress =  myPlayer.getCurrentPosition();
								Intent changeProgress = new Intent(CHANGE);
								changeProgress.putExtra(CURRENT_PROGRESS, progress);
								sendBroadcast(changeProgress);

								try {
									// Sleep for 0.1 seconds.
									Thread.sleep(100);
								} catch (InterruptedException e) {
									Log.d("Sleep seekbar", "sleep failure");
								}
							}
						}
					}
					// Starts the thread by calling the run() method in its Runnable.
					).start();

		} catch (IOException e) {
			Toast.makeText(getBaseContext(),"Errore nell'esecuzione della traccia: " + sessionToPlay,
					Toast.LENGTH_SHORT).show();
		}

		// Runs this service in the foreground;
		// Supply the ongoing notification.
		Intent intent = new Intent(this, PlayActivity.class); 
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP); 
		intent.putExtra(PlayActivity.SESSION_NAME, sessionInPlayNow.substring(35, sessionInPlayNow.length()-4));
		intent.putExtra(PlayActivity.AUTOPLAY, true);  // The song starts automatically.
		intent.putExtra(PlayActivity.DURATION, myPlayer.getDuration());
		PendingIntent pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT); 
		Notification notification = new NotificationCompat.Builder(getApplicationContext())
		.setContentTitle(getResources().getString(R.string.play_notification_title) + " " + sessionInPlayNow.substring(35, sessionInPlayNow.length()-4))
		.setContentText(getResources().getString(R.string.play_notification_text))
		.setSmallIcon(R.drawable.abc_ic_go)
		.setContentIntent(pi) // Required on Gingerbread and below.
		.build();
		final int notificationID = 1; // An ID for this notification unique within the app.
		startForeground(notificationID, notification);

	}
	
	/**
	 * This method is called when the button "Pause" is pressed. It pauses the song in the song.
	 */
	private void pause() { 
		if (isPlaying) { 
			isPlaying = false; 
			myPlayer.pause();
			position = myPlayer.getCurrentPosition();

		} 
	} 
	
	/**
	 * This method is called when the button "Stop" is pressed. It stops the song.
	 */
	private void stop() { 
		if (isPlaying) 
			isPlaying = false; 
		
		if (myPlayer != null) { 
			myPlayer.release(); 
			myPlayer = null;
		} 
		
		position = 0;
	} 

	/**
	 * This update the current position of the song.
	 * @param milliseconds time to seek in milliseconds.
	 */
	private void seekTo(int milliseconds) { 
		position = milliseconds;
		if (myPlayer != null)
			myPlayer.seekTo(position);
	} 
	
	/**
	 * This method is called when the button "Loop" is pressed. It permits to loop or not the song in the PlayService.
	 */
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
	
	/**
	 * Get the name of the session playing now (without .wav).
	 * @return the name of the session, null if nothig is in play.
	 */
	public static String getSessionInPlay(){
		if(sessionInPlayNow == null)
			return null;
		return sessionInPlayNow.substring(35, sessionInPlayNow.length()-4);
	}


}
