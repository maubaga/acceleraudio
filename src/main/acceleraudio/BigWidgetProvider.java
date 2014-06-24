package main.acceleraudio;

import static main.acceleraudio.DBOpenHelper.DURATION;
import static main.acceleraudio.DBOpenHelper.LAST_MODIFY_DATE;
import static main.acceleraudio.DBOpenHelper.LAST_MODIFY_TIME;
import static main.acceleraudio.DBOpenHelper.NAME;
import static main.acceleraudio.DBOpenHelper.TABLE;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.RemoteViews;

public class BigWidgetProvider extends AppWidgetProvider {

	// our actions for our buttons
	public static final String ACTION_WIDGET_START = "mau.baga.chronometer.START";
	public static final String ACTION_WIDGET_STOP = "mau.baga.chronometer.STOP";

	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		
		//Definisco il layout del widget e mi salvo la sua istanza in remoteViews
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.big_widget);
		remoteViews.setOnClickPendingIntent(R.id.start_button, startButtonPendingIntent(context));
		remoteViews.setOnClickPendingIntent(R.id.stop_button, stopButtonPendingIntent(context));
		
		//Accedo al database
		DBOpenHelper dbHelper = new DBOpenHelper(context);
		String[] SELECT = {NAME, LAST_MODIFY_DATE, LAST_MODIFY_TIME, DURATION}; 
		String ORDER_BY = LAST_MODIFY_DATE + ", " + LAST_MODIFY_TIME + " DESC";
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.query(TABLE, SELECT, null, null, null, null, ORDER_BY);
		if(cursor.getCount() == 0){
			remoteViews.setTextViewText(R.id.widget_text_view, "Non sono ancora presenti tracce. Premi su start per iniziare.");
		} else{
			cursor.moveToFirst();
			String name = cursor.getString(cursor.getColumnIndex(NAME));
			remoteViews.setTextViewText(R.id.widget_text_view, name);
			int duration = cursor.getInt(cursor.getColumnIndex(DURATION));
			Intent playIntent = new Intent(context, PlayActivity.class);
			playIntent.putExtra(PlayActivity.DURATION, duration);
			playIntent.putExtra(PlayActivity.SESSION_NAME, name);
			playIntent.putExtra(PlayActivity.AUTOPLAY, true);  //the song starts automatically
			PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, playIntent, 0);
			remoteViews.setOnClickPendingIntent(R.id.widget_text_view, pendingIntent);
		}
		
		
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