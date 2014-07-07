package mau.baga.battery;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.widget.RemoteViews;

public class BatteryWidgetProvider extends AppWidgetProvider {

	// our actions for our buttons
	public static final String ACTION_WIDGET_START = "mau.baga.chronometer.START";
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		
		//Definisco il layout del widget e mi salvo la sua istanza in remoteViews
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_chronometer);
		remoteViews.setOnClickPendingIntent(R.id.level, startButtonPendingIntent(context));
		
		IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		Intent batteryStatus = context.registerReceiver(null, ifilter);
		int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
		int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

		float batteryPct = level / (float)scale * 100;
		remoteViews.setTextViewText(R.id.level, batteryPct + "%");
		
		pushWidgetUpdate(context, remoteViews);
	}

	public static PendingIntent startButtonPendingIntent(Context context) {
		Intent intent = new Intent();
	    intent.setAction(ACTION_WIDGET_START);
	    return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	}

	public static void pushWidgetUpdate(Context context, RemoteViews remoteViews) {
		ComponentName myWidget = new ComponentName(context, BatteryWidgetProvider.class);
	    AppWidgetManager manager = AppWidgetManager.getInstance(context);
	    manager.updateAppWidget(myWidget, remoteViews);		
	}
}