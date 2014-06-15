package main.acceleraudio;

import main.acceleraudio.R;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class BigWidgetProvider extends AppWidgetProvider {

	// our actions for our buttons
	public static final String ACTION_WIDGET_START = "mau.baga.chronometer.START";
	public static final String ACTION_WIDGET_STOP = "mau.baga.chronometer.STOP";

	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		
		//Definisco il layout del widget e mi salvo la sua istanza in remoteViews
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_chronometer);
		remoteViews.setOnClickPendingIntent(R.id.start_button, startButtonPendingIntent(context));
		remoteViews.setOnClickPendingIntent(R.id.stop_button, stopButtonPendingIntent(context));
		
		Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
		remoteViews.setOnClickPendingIntent(R.id.chronometer, pendingIntent);
		
		pushWidgetUpdate(context, remoteViews);
	}

	public static PendingIntent startButtonPendingIntent(Context context) {
		Intent intent = new Intent();
	    intent.setAction(ACTION_WIDGET_START);
	    return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	}
	
	public static PendingIntent stopButtonPendingIntent(Context context) {
		Intent intent = new Intent();
	    intent.setAction(ACTION_WIDGET_STOP);
	    return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	}

	public static void pushWidgetUpdate(Context context, RemoteViews remoteViews) {
		ComponentName myWidget = new ComponentName(context, BigWidgetProvider.class);
	    AppWidgetManager manager = AppWidgetManager.getInstance(context);
	    manager.updateAppWidget(myWidget, remoteViews);		
	}
}