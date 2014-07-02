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
		//Get the layout of the LittleWidget
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.little_widget);
		remoteViews.setOnClickPendingIntent(R.id.start_button, startButtonPendingIntent(context));
		remoteViews.setOnClickPendingIntent(R.id.stop_button, stopButtonPendingIntent(context));
		remoteViews.setOnClickPendingIntent(R.id.chronometer, chronometerPendingIntent(context));		
						
		pushWidgetUpdate(context, remoteViews);
	}

	/**
	 * Get a PendigIntent for start button that start a recording.
	 * @param context The context where the method is call.
	 * @return A PendingIntet to associate with the start button.
	 */
	public static PendingIntent startButtonPendingIntent(Context context) {
		Intent intent = new Intent();
	    intent.setAction(WidgetIntentReceiver.ACTION_WIDGET_START);
	    return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	}
	
	/**
	 * Get a PendigIntent for stop button that stop the recording.
	 * @param context The context where the method is call.
	 * @return A PendingIntet to associate with the stop button.
	 */
	public static PendingIntent stopButtonPendingIntent(Context context) {
		Intent intent = new Intent();
	    intent.setAction(WidgetIntentReceiver.ACTION_WIDGET_STOP);
	    return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	}

	/**
	 * Get a PendigIntent for preferences chronometer that that start MainActivity.
	 * @param context The context where the method is call.
	 * @return A PendingIntet to associate with the chronometer.
	 */
	public static PendingIntent chronometerPendingIntent(Context context) {
		Intent intent = new Intent(context, MainActivity.class);
        return PendingIntent.getActivity(context, 0, intent, Intent.FLAG_ACTIVITY_NEW_TASK);
	}
	
	
	/**
	 * This method update the RemoteViews give as param.
	 * @param context The context where the method is call.
	 * @param remoteViews RemoteViews to update.
	 */
	public static void pushWidgetUpdate(Context context, RemoteViews remoteViews) {
		ComponentName myWidget = new ComponentName(context, LittleWidgetProvider.class);
	    AppWidgetManager manager = AppWidgetManager.getInstance(context);
	    manager.updateAppWidget(myWidget, remoteViews);		
	}
}