package main.acceleraudio;

import main.acceleraudio.R;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class LittleWidgetProvider extends AppWidgetProvider {

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {		
		// Get the layout of the LittleWidget.
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.little_widget);
		remoteViews.setOnClickPendingIntent(R.id.start_button, startButtonPendingIntent(context));
		remoteViews.setOnClickPendingIntent(R.id.stop_button, stopButtonPendingIntent(context));
		remoteViews.setOnClickPendingIntent(R.id.little_chronometer, chronometerPendingIntent(context));		
						
		pushWidgetUpdate(context, remoteViews);
	}

	/**
	 * Get a PendigIntent for start button that starts a recording.
	 * @param context The Context where the method is called.
	 * @return A PendingIntet to associate with the start button.
	 */
	public static PendingIntent startButtonPendingIntent(Context context) {
		Intent intent = new Intent();
	    intent.setAction(WidgetIntentReceiver.ACTION_WIDGET_START);
	    return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	}
	
	/**
	 * Get a PendigIntent for stop button that stops the recording.
	 * @param context The Context where the method is called.
	 * @return A PendingIntet to associate with the stop button.
	 */
	public static PendingIntent stopButtonPendingIntent(Context context) {
		Intent intent = new Intent();
	    intent.setAction(WidgetIntentReceiver.ACTION_WIDGET_STOP);
	    return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	}

	/**
	 * Get a PendigIntent for the chronometer that starts MainActivity.
	 * @param context The Context where the method is called.
	 * @return A PendingIntet to associate with the chronometer.
	 */
	public static PendingIntent chronometerPendingIntent(Context context) {
		Intent intent = new Intent(context, MainActivity.class);
        return PendingIntent.getActivity(context, 0, intent, 0);
	}
	
	
	/**
	 * Update the RemoteViews given as param.
	 * @param context The Context where the method is called.
	 * @param remoteViews RemoteViews to update.
	 */
	public static void pushWidgetUpdate(Context context, RemoteViews remoteViews) {
		ComponentName myWidget = new ComponentName(context, LittleWidgetProvider.class);
	    AppWidgetManager manager = AppWidgetManager.getInstance(context);
	    manager.updateAppWidget(myWidget, remoteViews);		
	}
}