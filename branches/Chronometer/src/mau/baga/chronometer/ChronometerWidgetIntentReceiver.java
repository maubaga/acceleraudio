package mau.baga.chronometer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.SystemClock;
import android.widget.RemoteViews;

public class ChronometerWidgetIntentReceiver extends BroadcastReceiver {


	@Override
	public void onReceive(Context context, Intent intent) {
		if(intent.getAction().equals(ChronometerWidgetProvider.ACTION_WIDGET_START)){
			startChronometer(context);
		}
		if(intent.getAction().equals(ChronometerWidgetProvider.ACTION_WIDGET_STOP)){
			stopChronometer(context);
		}
	}

	private void startChronometer(Context context) {
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_chronometer);
		remoteViews.setTextColor(R.id.chronometer, Color.GREEN);
		remoteViews.setChronometer(R.id.chronometer, SystemClock.elapsedRealtime(), null, true);
		
		//REMEMBER TO ALWAYS REFRESH YOUR BUTTON CLICK LISTENERS!!!
		remoteViews.setOnClickPendingIntent(R.id.start_button, ChronometerWidgetProvider.startButtonPendingIntent(context));
		remoteViews.setOnClickPendingIntent(R.id.stop_button, ChronometerWidgetProvider.stopButtonPendingIntent(context));
		
		ChronometerWidgetProvider.pushWidgetUpdate(context.getApplicationContext(), remoteViews);
	}
	
	private void stopChronometer(Context context) {
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_chronometer);
		remoteViews.setTextColor(R.id.chronometer, Color.RED);
		remoteViews.setChronometer(R.id.chronometer, SystemClock.elapsedRealtime() , null, false);
		
		//REMEMBER TO ALWAYS REFRESH YOUR BUTTON CLICK LISTENERS!!!
		remoteViews.setOnClickPendingIntent(R.id.start_button, ChronometerWidgetProvider.startButtonPendingIntent(context));
		remoteViews.setOnClickPendingIntent(R.id.stop_button, ChronometerWidgetProvider.stopButtonPendingIntent(context));

		ChronometerWidgetProvider.pushWidgetUpdate(context.getApplicationContext(), remoteViews);
	}



}
