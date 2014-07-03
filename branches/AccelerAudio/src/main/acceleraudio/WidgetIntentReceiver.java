package main.acceleraudio;

import static main.acceleraudio.DBOpenHelper.DATA_SIZE;
import static main.acceleraudio.DBOpenHelper.FIRST_DATE;
import static main.acceleraudio.DBOpenHelper.FIRST_TIME;
import static main.acceleraudio.DBOpenHelper.NAME;
import static main.acceleraudio.DBOpenHelper.UPSAMPL;
import static main.acceleraudio.DBOpenHelper.X_CHECK;
import static main.acceleraudio.DBOpenHelper.X_VALUES;
import static main.acceleraudio.DBOpenHelper.Y_CHECK;
import static main.acceleraudio.DBOpenHelper.Y_VALUES;
import static main.acceleraudio.DBOpenHelper.Z_CHECK;
import static main.acceleraudio.DBOpenHelper.Z_VALUES;

import java.io.File;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

public class WidgetIntentReceiver extends BroadcastReceiver {
	// our actions for our buttons
	public static final String ACTION_WIDGET_START = "main.acceleraudio.widget.START";
	public static final String ACTION_WIDGET_STOP = "main.acceleraudio.widget.STOP";


	@Override
	public void onReceive(Context context, Intent intent) {
		if(intent.getAction().equals(ACTION_WIDGET_START)){
			boolean isRecordingStart = AccelerAudioUtilities.isMyServiceRunning(context, RecordService.class);
			if (isRecordingStart){
				Toast.makeText(context, context.getResources().getString(R.string.rec_already_start), Toast.LENGTH_SHORT).show();
				return;
			}
			startFromWidget(context);
		}
		if(intent.getAction().equals(ACTION_WIDGET_STOP)){
			stopFromWidget(context);
		}

		if(intent.getAction().equals(RecordService.STOP_SERVICE)){ // The recording is over!
			byte[] x = intent.getByteArrayExtra(RecordService.X_VALUE);
			byte[] y = intent.getByteArrayExtra(RecordService.Y_VALUE);
			byte[] z = intent.getByteArrayExtra(RecordService.Z_VALUE);
			int size = intent.getIntExtra(RecordService.SIZE, 0);
			String name = intent.getStringExtra(RecordService.SESSION_NAME);

			SharedPreferences preferences = context.getSharedPreferences(PrefActivity.KEY_PREFERENCE, Context.MODE_PRIVATE);
			boolean pref_cbX = preferences.getBoolean(PrefActivity.KEY_SELECT_X, true);
			boolean pref_cbY = preferences.getBoolean(PrefActivity.KEY_SELECT_Y, true);
			boolean pref_cbZ = preferences.getBoolean(PrefActivity.KEY_SELECT_Z, true);
			int pref_upsampl = preferences.getInt(PrefActivity.KEY_UPSAMPL, 100);
			int rate = preferences.getInt(PrefActivity.KEY_RATE, 100);

			// Create the song.
			SongCreator songCreator = new SongCreator(x, y, z, size);
			songCreator.setRate(rate);
			songCreator.setUpsample(pref_upsampl);
			songCreator.setAxes(pref_cbX, pref_cbY, pref_cbZ);
			boolean isCreated = songCreator.createNewSession(context, name); 

			if(isCreated){
				// Update the layout of the Widgets.
				updateWidgetOnStop(context);
				
				// Start Modify Activity.
				Intent modifyIntent = new Intent(context, ModifyActivity.class);
				modifyIntent.putExtra(FIRST_DATE, AccelerAudioUtilities.getCurrentDate());
				modifyIntent.putExtra(FIRST_TIME, AccelerAudioUtilities.getCurrentTime());
				modifyIntent.putExtra(NAME, name);
				modifyIntent.putExtra(X_VALUES, x);
				modifyIntent.putExtra(Y_VALUES, y);
				modifyIntent.putExtra(Z_VALUES, z);
				modifyIntent.putExtra(DATA_SIZE, size);
				modifyIntent.putExtra(X_CHECK, pref_cbX);
				modifyIntent.putExtra(Y_CHECK, pref_cbY);
				modifyIntent.putExtra(Z_CHECK, pref_cbZ);
				modifyIntent.putExtra(UPSAMPL, pref_upsampl);
				modifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(modifyIntent);
			}
		}
	}


	private void startFromWidget(Context context) {
		// Get max duration and rate from the preferences.
		SharedPreferences preferences = context.getSharedPreferences(PrefActivity.KEY_PREFERENCE, Context.MODE_PRIVATE);
		String pref_maxRec = preferences.getString(PrefActivity.KEY_MAX_REC, context.getResources().getString(R.string.duration1));
		int rate = preferences.getInt(PrefActivity.KEY_RATE, 100);
		long maxRecordTime = 0;
		if (context.getResources().getString(R.string.duration1).equals(pref_maxRec))
			maxRecordTime = 30 * 1000;
		if (context.getResources().getString(R.string.duration2).equals(pref_maxRec))
			maxRecordTime = 60 * 1000;
		if (context.getResources().getString(R.string.duration3).equals(pref_maxRec))
			maxRecordTime = 120 * 1000;
		if (context.getResources().getString(R.string.duration4).equals(pref_maxRec))
			maxRecordTime = 300 * 1000;

		// Select the name to give to the session.
		String folder = context.getApplicationContext().getFilesDir().getPath() + "/";
		int fileIndex = 1;
		String name = "Widget";
		while(true){
			File outputFile = new File(folder + name + "-" + fileIndex +".wav");
			if (!outputFile.exists())
				break;
			else
				fileIndex++;
		}

		// Start the background recording.
		Intent intent = new Intent(context, RecordService.class);
		intent.setAction(RecordService.START);
		intent.putExtra(RecordService.MAX_RECORD_TIME, maxRecordTime); 
		intent.putExtra(RecordService.RATE, rate);
		intent.putExtra(RecordService.SESSION_NAME, name + "-" + fileIndex); 
		context.startService(intent);
		Toast.makeText(context,"Registrazione in background iniziata", Toast.LENGTH_SHORT).show();

		// Update the layout of the widgets.
		updateWidgetOnStart(context);
	}


	private void stopFromWidget(Context context) {
		// Stop the background recording and notify to return the values.
		Intent intent = new Intent(context, RecordService.class);
		intent.setAction(RecordService.STOP);
		context.startService(intent);
		Toast.makeText(context,"Registrazione finita", Toast.LENGTH_SHORT).show();
	}

	private void updateWidgetOnStart(Context context) {		
		// Start the little widget.
		RemoteViews littleRemoteViews = new RemoteViews(context.getPackageName(), R.layout.little_widget);
		littleRemoteViews.setChronometer(R.id.little_chronometer, SystemClock.elapsedRealtime(), null, true);
		littleRemoteViews.setViewVisibility(R.id.start_button, View.GONE);
		littleRemoteViews.setViewVisibility(R.id.stop_button, View.VISIBLE);

		// Start the big widget.
		RemoteViews bigRemoteViews = new RemoteViews(context.getPackageName(), R.layout.big_widget);
		bigRemoteViews.setChronometer(R.id.big_chronometer, SystemClock.elapsedRealtime(), null, true);
		bigRemoteViews.setViewPadding(R.id.big_chronometer, 80, 0, 0, 0);
		bigRemoteViews.setViewVisibility(R.id.start_button, View.GONE);
		bigRemoteViews.setViewVisibility(R.id.widget_text_view, View.GONE);
		bigRemoteViews.setViewVisibility(R.id.widget_prefereces, View.GONE);
		bigRemoteViews.setViewVisibility(R.id.stop_button, View.VISIBLE);

		// Refresh the stop listener.
		littleRemoteViews.setOnClickPendingIntent(R.id.stop_button, LittleWidgetProvider.stopButtonPendingIntent(context));
		bigRemoteViews.setOnClickPendingIntent(R.id.stop_button, BigWidgetProvider.stopButtonPendingIntent(context));

		// Update the widgets.
		LittleWidgetProvider.pushWidgetUpdate(context.getApplicationContext(), littleRemoteViews);
		BigWidgetProvider.pushWidgetUpdate(context.getApplicationContext(), bigRemoteViews);
	}

	/**
	 * This method update the layout of the two widget when the recording is over. It refresh the last song store in the database, so this method can be call every time that the database is update, for example for deleting, modification or duplication of records.
	 * @param context The context where the method is call.
	 */
	public static void updateWidgetOnStop(Context context) {	
		// Stop the little widget.
		RemoteViews littleRemoteViews = new RemoteViews(context.getPackageName(), R.layout.little_widget);
		littleRemoteViews.setChronometer(R.id.little_chronometer, SystemClock.elapsedRealtime(), null, false);
		littleRemoteViews.setTextViewText(R.id.little_chronometer, context.getResources().getString(R.string.initial_time));
		littleRemoteViews.setViewVisibility(R.id.start_button, View.VISIBLE);
		littleRemoteViews.setViewVisibility(R.id.stop_button, View.GONE);

		// Stop the big widget.
		RemoteViews bigRemoteViews = new RemoteViews(context.getPackageName(), R.layout.big_widget);
		bigRemoteViews.setChronometer(R.id.big_chronometer, SystemClock.elapsedRealtime(), null, false);
		bigRemoteViews.setTextViewText(R.id.big_chronometer, context.getResources().getString(R.string.initial_time));
		bigRemoteViews.setViewPadding(R.id.big_chronometer, 0, 0, 0, 0);
		bigRemoteViews.setViewVisibility(R.id.start_button, View.VISIBLE);
		bigRemoteViews.setViewVisibility(R.id.widget_text_view, View.VISIBLE);
		bigRemoteViews.setViewVisibility(R.id.widget_prefereces, View.VISIBLE);
		bigRemoteViews.setViewVisibility(R.id.stop_button, View.GONE);

		BigWidgetProvider.updateLastSong(bigRemoteViews, context);

		// Refresh the start and preference listener.
		littleRemoteViews.setOnClickPendingIntent(R.id.start_button, LittleWidgetProvider.startButtonPendingIntent(context));
		littleRemoteViews.setOnClickPendingIntent(R.id.little_chronometer, LittleWidgetProvider.chronometerPendingIntent(context));
		bigRemoteViews.setOnClickPendingIntent(R.id.start_button, BigWidgetProvider.startButtonPendingIntent(context));
		bigRemoteViews.setOnClickPendingIntent(R.id.widget_prefereces, BigWidgetProvider.preferencesPendingIntent(context));
		bigRemoteViews.setOnClickPendingIntent(R.id.big_chronometer, BigWidgetProvider.chronometerPendingIntent(context));

		// Update the widgets.
		LittleWidgetProvider.pushWidgetUpdate(context.getApplicationContext(), littleRemoteViews);
		BigWidgetProvider.pushWidgetUpdate(context.getApplicationContext(), bigRemoteViews);
	}
}
