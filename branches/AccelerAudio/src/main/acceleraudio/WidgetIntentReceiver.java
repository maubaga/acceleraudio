package main.acceleraudio;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.SystemClock;
import android.widget.RemoteViews;
import android.widget.Toast;

public class WidgetIntentReceiver extends BroadcastReceiver {



	@Override
	public void onReceive(Context context, Intent intent) {
		if(intent.getAction().equals(LittleWidgetProvider.ACTION_WIDGET_START)){
			startChronometer(context);
		}
		if(intent.getAction().equals(LittleWidgetProvider.ACTION_WIDGET_STOP)){
			stopChronometer(context);
		}

		if(intent.getAction().equals(RecordService.STOP_SERVICE)){ //The recording is over!
			byte[] x = intent.getByteArrayExtra(RecordService.X_VALUE);
			byte[] y = intent.getByteArrayExtra(RecordService.Y_VALUE);
			byte[] z = intent.getByteArrayExtra(RecordService.Z_VALUE);
			int size = intent.getIntExtra(RecordService.SIZE, 0);

			Intent createIntent = new Intent(context, CreateActivity.class);
			createIntent.putExtra(RecordService.X_VALUE, x);
			createIntent.putExtra(RecordService.Y_VALUE, y);
			createIntent.putExtra(RecordService.Z_VALUE, z);
			createIntent.putExtra(RecordService.SIZE, size);
			createIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(createIntent);
			
			
			//Stop the chronometer in the widget
			RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.little_widget);
			remoteViews.setChronometer(R.id.chronometer, SystemClock.elapsedRealtime() , null, false);
			LittleWidgetProvider.pushWidgetUpdate(context.getApplicationContext(), remoteViews);
		}
	}
	private void startChronometer(Context context) {
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.little_widget);
		remoteViews.setTextColor(R.id.chronometer, Color.GREEN);
		remoteViews.setChronometer(R.id.chronometer, SystemClock.elapsedRealtime(), null, true);
		
		SharedPreferences preferences = context.getSharedPreferences("Session_Preferences", Context.MODE_PRIVATE);  //TODO prendere le stringhe dalle costanti
		String pref_maxRec = preferences.getString("eTextMaxRec", context.getResources().getString(R.string.duration1));
		int rate = preferences.getInt("sbRate", 50);
		long maxRecordTime = 0;
		if (context.getResources().getString(R.string.duration1).equals(pref_maxRec))
			maxRecordTime = 30 * 1000;
		if (context.getResources().getString(R.string.duration2).equals(pref_maxRec))
			maxRecordTime = 60 * 1000;
		if (context.getResources().getString(R.string.duration3).equals(pref_maxRec))
			maxRecordTime = 120 * 1000;
		if (context.getResources().getString(R.string.duration4).equals(pref_maxRec))
			maxRecordTime = 300 * 1000;
		
		Intent intent = new Intent(context, RecordService.class);
		intent.setAction(RecordService.START);
		intent.putExtra(RecordService.MAX_RECORD_TIME, maxRecordTime); 
		intent.putExtra(RecordService.RATE, rate);
		context.startService(intent);
		Toast.makeText(context,"Registrazione in background iniziata", Toast.LENGTH_SHORT).show();

		//REMEMBER TO ALWAYS REFRESH YOUR BUTTON CLICK LISTENERS!!!
		remoteViews.setOnClickPendingIntent(R.id.start_button, LittleWidgetProvider.startButtonPendingIntent(context));
		remoteViews.setOnClickPendingIntent(R.id.stop_button, LittleWidgetProvider.stopButtonPendingIntent(context));

		LittleWidgetProvider.pushWidgetUpdate(context.getApplicationContext(), remoteViews);
	}

	private void stopChronometer(Context context) {
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.little_widget);
		remoteViews.setTextColor(R.id.chronometer, Color.MAGENTA);
		remoteViews.setChronometer(R.id.chronometer, SystemClock.elapsedRealtime() , null, false);

		//This intent stop the background recording and notify to return the values
		Intent intent = new Intent(context, RecordService.class);
		intent.setAction(RecordService.STOP);
		context.startService(intent);
		Toast.makeText(context,"Registrazione finita", Toast.LENGTH_SHORT).show();
		
		//REMEMBER TO ALWAYS REFRESH YOUR BUTTON CLICK LISTENERS!!!
		remoteViews.setOnClickPendingIntent(R.id.start_button, LittleWidgetProvider.startButtonPendingIntent(context));
		remoteViews.setOnClickPendingIntent(R.id.stop_button, LittleWidgetProvider.stopButtonPendingIntent(context));

		LittleWidgetProvider.pushWidgetUpdate(context.getApplicationContext(), remoteViews);
	}



}
