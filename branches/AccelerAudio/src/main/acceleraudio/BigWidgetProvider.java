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
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {		
		//Get the layout of the BigWidget
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.big_widget);
		remoteViews.setOnClickPendingIntent(R.id.start_button, startButtonPendingIntent(context));
		remoteViews.setOnClickPendingIntent(R.id.stop_button, stopButtonPendingIntent(context));
		remoteViews.setOnClickPendingIntent(R.id.chronometer, chronometerPendingIntent(context));		
		remoteViews.setOnClickPendingIntent(R.id.widget_prefereces, preferencesPendingIntent(context));
		
		updateLastSong(remoteViews, context);
						
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
	 * Get a PendigIntent for preferences button that that start PrefActivity.
	 * @param context The context where the method is call.
	 * @return A PendingIntet to associate with the preferences button.
	 */
	public static PendingIntent preferencesPendingIntent(Context context) {
		Intent prefIntent = new Intent(context, PrefActivity.class);
	    return PendingIntent.getActivity(context, 0, prefIntent, Intent.FLAG_ACTIVITY_NEW_TASK);
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
	 * This method get the last song from the database and show it in a TextView. Then set a Intent on it that start PlayActivity.
	 * @param remoteViews RemoteView that contain the TextView.
	 * @param context The context where the method is call.
	 */
	public static void updateLastSong(RemoteViews remoteViews, Context context) {
		//Get the last song from the database
		DBOpenHelper dbHelper = new DBOpenHelper(context);
		String[] SELECT = {NAME, LAST_MODIFY_DATE, LAST_MODIFY_TIME, DURATION}; 
		String ORDER_BY = LAST_MODIFY_DATE + " DESC, " + LAST_MODIFY_TIME + " DESC";
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.query(TABLE, SELECT, null, null, null, null, ORDER_BY);
		if(cursor.getCount() == 0){
			remoteViews.setTextViewText(R.id.widget_text_view, "Premi start per iniziare.");
		} else{
			cursor.moveToFirst();
			String name = cursor.getString(cursor.getColumnIndex(NAME));
			remoteViews.setTextViewText(R.id.widget_text_view, name);
			int duration = cursor.getInt(cursor.getColumnIndex(DURATION));
			Intent playIntent = new Intent(context, PlayActivity.class);
			playIntent.putExtra(PlayActivity.DURATION, duration);
			playIntent.putExtra(PlayActivity.SESSION_NAME, name);
			playIntent.putExtra(PlayActivity.AUTOPLAY, true);  //the song starts automatically
			PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, playIntent, Intent.FLAG_ACTIVITY_NEW_TASK);
			remoteViews.setOnClickPendingIntent(R.id.widget_text_view, pendingIntent);
		}		
	}

	/**
	 * This method update the RemoteViews give as param.
	 * @param context The context where the method is call.
	 * @param remoteViews RemoteViews to update.
	 */
	public static void pushWidgetUpdate(Context context, RemoteViews remoteViews) {
		ComponentName myWidget = new ComponentName(context, BigWidgetProvider.class);
	    AppWidgetManager manager = AppWidgetManager.getInstance(context);
	    manager.updateAppWidget(myWidget, remoteViews);		
	}
}