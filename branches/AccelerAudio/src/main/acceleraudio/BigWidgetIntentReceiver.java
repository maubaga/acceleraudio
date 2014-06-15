package main.acceleraudio;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.SystemClock;
import android.widget.RemoteViews;
import android.widget.Toast;

public class BigWidgetIntentReceiver extends BroadcastReceiver {



	@Override
	public void onReceive(Context context, Intent intent) {
		if(intent.getAction().equals(BigWidgetProvider.ACTION_WIDGET_START)){
			startChronometer(context);
		}
		if(intent.getAction().equals(BigWidgetProvider.ACTION_WIDGET_STOP)){
			stopChronometer(context);
		}

		if(intent.getIntExtra(RecordService.INTENT_TYPE, -1) == RecordService.STOP_SERVICE){ //The recording is over!
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
		}
	}
	private void startChronometer(Context context) {
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_chronometer);
		remoteViews.setTextColor(R.id.chronometer, Color.GREEN);
		remoteViews.setChronometer(R.id.chronometer, SystemClock.elapsedRealtime(), null, true);

		Intent intent = new Intent(context, RecordService.class);
		intent.setAction(RecordService.START);
		intent.putExtra(RecordService.MAX_RECORD_TIME, 30000); //TODO Get by preferences
		intent.putExtra(RecordService.RATE, 1);
		context.startService(intent);
		Toast.makeText(context,"Registrazione in background iniziata", Toast.LENGTH_SHORT).show();

		//REMEMBER TO ALWAYS REFRESH YOUR BUTTON CLICK LISTENERS!!!
		remoteViews.setOnClickPendingIntent(R.id.start_button, BigWidgetProvider.startButtonPendingIntent(context));
		remoteViews.setOnClickPendingIntent(R.id.stop_button, BigWidgetProvider.stopButtonPendingIntent(context));

		BigWidgetProvider.pushWidgetUpdate(context.getApplicationContext(), remoteViews);
	}

	private void stopChronometer(Context context) {
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_chronometer);
		remoteViews.setTextColor(R.id.chronometer, Color.MAGENTA);
		remoteViews.setChronometer(R.id.chronometer, SystemClock.elapsedRealtime() , null, false);

		//This intent stop the background recording and notify to return the values
		Intent intent = new Intent(context, RecordService.class);
		intent.setAction(RecordService.STOP);
		context.startService(intent);
		Toast.makeText(context,"Registrazione finita", Toast.LENGTH_SHORT).show();
		
		//REMEMBER TO ALWAYS REFRESH YOUR BUTTON CLICK LISTENERS!!!
		remoteViews.setOnClickPendingIntent(R.id.start_button, BigWidgetProvider.startButtonPendingIntent(context));
		remoteViews.setOnClickPendingIntent(R.id.stop_button, BigWidgetProvider.stopButtonPendingIntent(context));

		BigWidgetProvider.pushWidgetUpdate(context.getApplicationContext(), remoteViews);
	}



}
