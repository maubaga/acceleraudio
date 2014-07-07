package mau.baga.battery;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.BatteryManager;
import android.widget.RemoteViews;

public class BatteryWidgetIntentReceiver extends BroadcastReceiver {


	@Override
	public void onReceive(Context context, Intent intent) {
		if(intent.getAction().equals(BatteryWidgetProvider.ACTION_WIDGET_START)){
			startChronometer(context);
		}
	}

	private void startChronometer(Context context) {
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_chronometer);

		IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		Intent batteryStatus = context.registerReceiver(null, ifilter);
		int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
		int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

		float batteryPct = (int)(level / (float)scale * 100);
		remoteViews.setTextViewText(R.id.level, batteryPct + "%");

		if(batteryPct < 15){
			remoteViews.setTextColor(R.id.level, Color.RED);
		}
		else{
			if(batteryPct < 50){
				remoteViews.setTextColor(R.id.level, Color.YELLOW);
			}
			else{
				remoteViews.setTextColor(R.id.level, Color.GREEN);
			}
		}

		//REMEMBER TO ALWAYS REFRESH YOUR BUTTON CLICK LISTENERS!!!
		remoteViews.setOnClickPendingIntent(R.id.level, BatteryWidgetProvider.startButtonPendingIntent(context));

		BatteryWidgetProvider.pushWidgetUpdate(context.getApplicationContext(), remoteViews);
	}
}
