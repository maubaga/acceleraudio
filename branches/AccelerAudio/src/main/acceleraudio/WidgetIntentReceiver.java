package main.acceleraudio;

import static main.acceleraudio.DBOpenHelper.DATA_SIZE;
import static main.acceleraudio.DBOpenHelper.FIRST_DATE;
import static main.acceleraudio.DBOpenHelper.FIRST_TIME;
import static main.acceleraudio.DBOpenHelper.NAME;
import static main.acceleraudio.DBOpenHelper.TABLE;
import static main.acceleraudio.DBOpenHelper.UPSAMPL;
import static main.acceleraudio.DBOpenHelper.X_CHECK;
import static main.acceleraudio.DBOpenHelper.X_VALUES;
import static main.acceleraudio.DBOpenHelper.Y_CHECK;
import static main.acceleraudio.DBOpenHelper.Y_VALUES;
import static main.acceleraudio.DBOpenHelper.Z_CHECK;
import static main.acceleraudio.DBOpenHelper.Z_VALUES;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.SystemClock;
import android.widget.RemoteViews;
import android.widget.Toast;

public class WidgetIntentReceiver extends BroadcastReceiver {
	// our actions for our buttons
	public static final String ACTION_WIDGET_START = "main.acceleraudio.widget.START";
	public static final String ACTION_WIDGET_STOP = "main.acceleraudio.widget.STOP";

	private byte[] x, y, z;
	private int size;

	private boolean pref_cbX;
	private boolean pref_cbY;
	private boolean pref_cbZ;
	private int rate; 
	private int pref_upsampl;


	//all file parameters
	private final byte BITS_PER_SAMPLE = 8; // 8, 16...
	private final byte NUM_OF_CHANNEL = 1; // 1 = mono, 2 = stereo
	private final int FREQUENCY = 8000; // 8000, 44100...


	@Override
	public void onReceive(Context context, Intent intent) {
		if(intent.getAction().equals(ACTION_WIDGET_START)){
			startFromWidget(context);
		}
		if(intent.getAction().equals(ACTION_WIDGET_STOP)){
			stopFromWidget(context);
		}

		if(intent.getAction().equals(RecordService.STOP_SERVICE)){ //The recording is over!
			x = intent.getByteArrayExtra(RecordService.X_VALUE);
			y = intent.getByteArrayExtra(RecordService.Y_VALUE);
			z = intent.getByteArrayExtra(RecordService.Z_VALUE);
			size = intent.getIntExtra(RecordService.SIZE, 0);
			String name = intent.getStringExtra(RecordService.SESSION_NAME);

			SharedPreferences preferences = context.getSharedPreferences("Session_Preferences", Context.MODE_PRIVATE); 
			pref_cbX = preferences.getBoolean("cBoxSelectX", true);
			pref_cbY = preferences.getBoolean("cBoxSelectY", true);
			pref_cbZ = preferences.getBoolean("cBoxSelectZ", true);
			pref_upsampl = preferences.getInt("sbUpsampling", 100);
			rate = preferences.getInt("sbRate", 100);

			addTrack(context, name); 


			//Update layout of the Widgets
			updateWidgetOnStop(context);

		}
	}


	private void startFromWidget(Context context) {
		//Get max duration and rate from the preferences
		SharedPreferences preferences = context.getSharedPreferences("Session_Preferences", Context.MODE_PRIVATE);  //TODO prendere le stringhe dalle costanti
		String pref_maxRec = preferences.getString(PrefActivity.KEY_MAX_REC, context.getResources().getString(R.string.duration1));
		int rate = preferences.getInt(PrefActivity.KEY_RATE, 50);
		long maxRecordTime = 0;
		if (context.getResources().getString(R.string.duration1).equals(pref_maxRec))
			maxRecordTime = 30 * 1000;
		if (context.getResources().getString(R.string.duration2).equals(pref_maxRec))
			maxRecordTime = 60 * 1000;
		if (context.getResources().getString(R.string.duration3).equals(pref_maxRec))
			maxRecordTime = 120 * 1000;
		if (context.getResources().getString(R.string.duration4).equals(pref_maxRec))
			maxRecordTime = 300 * 1000;

		//Select the name to give to the session
		String folder = context.getApplicationContext().getFilesDir().getPath() + "/";
		int fileIndex = 1;
		String name = "Widget";
		while(true){
			File outputFile = new File(folder + name + "-" + fileIndex +".wav");
			if (!outputFile.exists())
				break;
			else
				fileIndex++;
		}

		//Start the background recording
		Intent intent = new Intent(context, RecordService.class);
		intent.setAction(RecordService.START);
		intent.putExtra(RecordService.MAX_RECORD_TIME, maxRecordTime); 
		intent.putExtra(RecordService.RATE, rate);
		intent.putExtra(RecordService.SESSION_NAME, name + "-" + fileIndex); 
		context.startService(intent);
		Toast.makeText(context,"Registrazione in background iniziata", Toast.LENGTH_SHORT).show();

		//Update layout of the widgets
		updateWidgetOnStart(context);
	}


	private void stopFromWidget(Context context) {
		//This intent stop the background recording and notify to return the values
		Intent intent = new Intent(context, RecordService.class);
		intent.setAction(RecordService.STOP);
		context.startService(intent);
		Toast.makeText(context,"Registrazione finita", Toast.LENGTH_SHORT).show();
	}

	private void updateWidgetOnStart(Context context) {		
		//Start the little widget
		RemoteViews littleRemoteViews = new RemoteViews(context.getPackageName(), R.layout.little_widget);
		littleRemoteViews.setChronometer(R.id.chronometer, SystemClock.elapsedRealtime(), null, true);
		littleRemoteViews.setViewVisibility(R.id.start_button, 8);
		littleRemoteViews.setViewVisibility(R.id.stop_button, 0);

		//Start the big widget
		RemoteViews bigRemoteViews = new RemoteViews(context.getPackageName(), R.layout.big_widget);
		bigRemoteViews.setChronometer(R.id.chronometer, SystemClock.elapsedRealtime(), null, true);
		bigRemoteViews.setViewVisibility(R.id.start_button, 8);
		bigRemoteViews.setViewVisibility(R.id.widget_text_view, 8);
		bigRemoteViews.setViewVisibility(R.id.widget_prefereces, 8);
		bigRemoteViews.setViewVisibility(R.id.stop_button, 0);

		//Refresh the stop listener.
		littleRemoteViews.setOnClickPendingIntent(R.id.stop_button, LittleWidgetProvider.stopButtonPendingIntent(context));
		bigRemoteViews.setOnClickPendingIntent(R.id.stop_button, BigWidgetProvider.stopButtonPendingIntent(context));

		//Update the widgets
		LittleWidgetProvider.pushWidgetUpdate(context.getApplicationContext(), littleRemoteViews);
		BigWidgetProvider.pushWidgetUpdate(context.getApplicationContext(), bigRemoteViews);

	}

	/**
	 * This method update the layout of the two widget when the recording is over. It refresh the last song store in the database, so this method can be call every time that the database is update, for example for deleting, modification or duplication of records.
	 * @param context The context where the method is call.
	 */
	public static void updateWidgetOnStop(Context context) {	
		//Stop the little widget
		RemoteViews littleRemoteViews = new RemoteViews(context.getPackageName(), R.layout.little_widget);
		littleRemoteViews.setChronometer(R.id.chronometer, SystemClock.elapsedRealtime(), null, false);
		littleRemoteViews.setTextViewText(R.id.chronometer, context.getResources().getString(R.string.initial_time));
		littleRemoteViews.setViewVisibility(R.id.start_button, 0);
		littleRemoteViews.setViewVisibility(R.id.stop_button, 8);

		//Stop the big widget
		RemoteViews bigRemoteViews = new RemoteViews(context.getPackageName(), R.layout.big_widget);
		bigRemoteViews.setChronometer(R.id.chronometer, SystemClock.elapsedRealtime(), null, false);
		bigRemoteViews.setTextViewText(R.id.chronometer, context.getResources().getString(R.string.initial_time));
		bigRemoteViews.setViewVisibility(R.id.start_button, 0);
		bigRemoteViews.setViewVisibility(R.id.widget_text_view, 0);
		bigRemoteViews.setViewVisibility(R.id.widget_prefereces, 0);
		bigRemoteViews.setViewVisibility(R.id.stop_button, 8);
		
		BigWidgetProvider.updateLastSong(bigRemoteViews, context);

		//REMEMBER TO ALWAYS REFRESH YOUR BUTTON CLICK LISTENERS!!!
		littleRemoteViews.setOnClickPendingIntent(R.id.start_button, LittleWidgetProvider.startButtonPendingIntent(context));
		bigRemoteViews.setOnClickPendingIntent(R.id.start_button, BigWidgetProvider.startButtonPendingIntent(context));
		bigRemoteViews.setOnClickPendingIntent(R.id.widget_prefereces, BigWidgetProvider.preferencesPendingIntent(context));

		LittleWidgetProvider.pushWidgetUpdate(context.getApplicationContext(), littleRemoteViews);
		BigWidgetProvider.pushWidgetUpdate(context.getApplicationContext(), bigRemoteViews);
	}


	//******************************START CREATE HERE********************************************\\

	public static boolean saveImage(Context context, String imageName, Bitmap session_image){
		FileOutputStream out;
		String file = imageName + ".png"; //name of the .png file
		try {
			out = context.openFileOutput(file, Context.MODE_PRIVATE);
			session_image.compress(Bitmap.CompressFormat.PNG, 90, out);
			out.close();
			return true;
		} 
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	//https://ccrma.stanford.edu/courses/422/projects/WaveFormat/
	//write the .wav file
	public boolean createWavFile(Context context, String songName){

		try {

			String file = songName + ".wav"; //name of the .wav file

			//create the data to add
			final int UPSAMPLING = pref_upsampl;
			byte[] dataAdded = new byte[size * UPSAMPLING];

			int num_axes = 0;
			if(pref_cbX)
				num_axes++;
			if(pref_cbY)
				num_axes++;
			if(pref_cbZ)
				num_axes++;

			for(int i = 0; i < size; i++){

				int sum_axes = 0;

				if(pref_cbX)
					sum_axes += x[i];
				if(pref_cbY)
					sum_axes += y[i];
				if(pref_cbZ)
					sum_axes += z[i];

				for(int j = i * UPSAMPLING; j < (i + 1) * UPSAMPLING; j++)
					dataAdded[j] = (byte)(sum_axes / num_axes);
			}	


			FileOutputStream fOut = context.openFileOutput(file,Context.MODE_PRIVATE);

			long totalAudioLen = dataAdded.length * NUM_OF_CHANNEL * (BITS_PER_SAMPLE / 8);
			long chunkSize = 36 + (dataAdded.length * NUM_OF_CHANNEL * (BITS_PER_SAMPLE / 8));
			long byteRate = FREQUENCY * NUM_OF_CHANNEL * (BITS_PER_SAMPLE / 8);

			WriteWaveFileHeader(fOut, totalAudioLen, 
					chunkSize, 
					FREQUENCY, NUM_OF_CHANNEL, 
					byteRate);

			for(int u = 0; u < 10; u++) //TODO ho una media di 10!
				averageArray(dataAdded); //loop this method for more average!

			fOut.write(dataAdded);
			fOut.close();

			return true;

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}


	//define the header of the .wav file. DON'T CHANGE IT!
	private void WriteWaveFileHeader(
			FileOutputStream out, long totalAudioLen,
			long totalDataLen, long longSampleRate, int channels,
			long byteRate) throws IOException {

		byte[] headerBuffer = new byte[44];

		headerBuffer[0] = 'R';  // RIFF/WAVE header
		headerBuffer[1] = 'I';
		headerBuffer[2] = 'F';
		headerBuffer[3] = 'F';
		headerBuffer[4] = (byte) (totalDataLen & 0xff);
		headerBuffer[5] = (byte) ((totalDataLen >> 8) & 0xff);
		headerBuffer[6] = (byte) ((totalDataLen >> 16) & 0xff);
		headerBuffer[7] = (byte) ((totalDataLen >> 24) & 0xff);
		headerBuffer[8] = 'W';
		headerBuffer[9] = 'A';
		headerBuffer[10] = 'V';
		headerBuffer[11] = 'E';
		headerBuffer[12] = 'f';  // 'fmt ' chunk
		headerBuffer[13] = 'm';
		headerBuffer[14] = 't';
		headerBuffer[15] = ' ';
		headerBuffer[16] = 16;  // 4 bytes: size of 'fmt ' chunk
		headerBuffer[17] = 0;
		headerBuffer[18] = 0;
		headerBuffer[19] = 0;
		headerBuffer[20] = 1;  // format = 1
		headerBuffer[21] = 0;
		headerBuffer[22] = (byte) channels;
		headerBuffer[23] = 0;
		headerBuffer[24] = (byte) (longSampleRate & 0xff);
		headerBuffer[25] = (byte) ((longSampleRate >> 8) & 0xff);
		headerBuffer[26] = (byte) ((longSampleRate >> 16) & 0xff);
		headerBuffer[27] = (byte) ((longSampleRate >> 24) & 0xff);
		headerBuffer[28] = (byte) (byteRate & 0xff);
		headerBuffer[29] = (byte) ((byteRate >> 8) & 0xff);
		headerBuffer[30] = (byte) ((byteRate >> 16) & 0xff);
		headerBuffer[31] = (byte) ((byteRate >> 24) & 0xff);
		headerBuffer[32] = (byte) (2 * 16 / 8);  // block align
		headerBuffer[33] = 0;
		headerBuffer[34] = BITS_PER_SAMPLE;  // bits per sample
		headerBuffer[35] = 0;
		headerBuffer[36] = 'd';
		headerBuffer[37] = 'a';
		headerBuffer[38] = 't';
		headerBuffer[39] = 'a';
		headerBuffer[40] = (byte) (totalAudioLen & 0xff);
		headerBuffer[41] = (byte) ((totalAudioLen >> 8) & 0xff);
		headerBuffer[42] = (byte) ((totalAudioLen >> 16) & 0xff);
		headerBuffer[43] = (byte) ((totalAudioLen >> 24) & 0xff);

		out.write(headerBuffer, 0, 44);
	}

	private void averageArray(byte[] byteArray){
		for (int i = 1; i < byteArray.length - 1; i++){
			byteArray[i] = (byte) ((byteArray[i - 1] + byteArray[i] + byteArray[i + 1]) /  3);
		}
	}

	public static Bitmap createImage() {
		//creo l'immagine
		Bitmap bmp = Bitmap.createBitmap(10, 10, Bitmap.Config.RGB_565); // this creates a MUTABLE bitmap
		long value = System.currentTimeMillis();

		//le do i valori
		int color = (int) value;
		int[] indexes = new int[4];
		for(int i = 0; i < indexes.length; i++)
			indexes[i] = (int)((value >> 2 * i) & 0xff) % (bmp.getWidth() / 2);

		for(int i = 0; i < indexes.length; i++){
			for(int j = i; j < indexes.length; j++){
				bmp.setPixel(indexes[i], indexes[j], color);
				bmp.setPixel(indexes[i], bmp.getHeight() - indexes[j] - 1, color);
				bmp.setPixel(bmp.getWidth() - indexes[i] -1, indexes[j], color);
				bmp.setPixel(bmp.getWidth() - indexes[i] -1, bmp.getHeight() - indexes[j] - 1, color);
			}
		}
		return bmp;
	}

	public boolean addTrack(Context context, String session_name){
		Bitmap session_image = createImage();
		boolean isSaved = saveImage(context, session_name, session_image);
		boolean isCreated = createWavFile(context, session_name);

		if(isSaved && isCreated){

			int duration = size * pref_upsampl * 1000 / FREQUENCY; //duration of the sound in milliSeconds

			//Get data and time
			String date = AccelerAudioUtilities.getCurrentDate();
			String time = AccelerAudioUtilities.getCurrentTime();

			//database
			DBOpenHelper openHelper = new DBOpenHelper(context);
			SQLiteDatabase db = openHelper.getWritableDatabase();
			db.delete(TABLE, NAME + "='" + session_name + "'", null);
			ContentValues values = new ContentValues();
			values.put(DBOpenHelper.NAME, session_name);
			values.put(DBOpenHelper.FIRST_DATE, date);
			values.put(DBOpenHelper.FIRST_TIME, time);
			values.put(DBOpenHelper.LAST_MODIFY_DATE, date);
			values.put(DBOpenHelper.LAST_MODIFY_TIME, time);
			values.put(DBOpenHelper.DURATION, duration);   
			values.put(DBOpenHelper.RATE, rate);       
			values.put(DBOpenHelper.UPSAMPL, pref_upsampl);       //add seekbar value
			values.put(DBOpenHelper.X_CHECK, pref_cbX);
			values.put(DBOpenHelper.Y_CHECK, pref_cbY);
			values.put(DBOpenHelper.Z_CHECK, pref_cbZ);
			values.put(DBOpenHelper.X_VALUES, x);                //add the three byte array to the database
			values.put(DBOpenHelper.Y_VALUES, y);
			values.put(DBOpenHelper.Z_VALUES, z);
			values.put(DBOpenHelper.DATA_SIZE, size);            //add the number samples to the database
			db.insert(DBOpenHelper.TABLE, null, values);

			Intent modifyIntent = new Intent(context, ModifyActivity.class);
			modifyIntent.putExtra(FIRST_DATE, date);
			modifyIntent.putExtra(FIRST_TIME, time);
			modifyIntent.putExtra(NAME, session_name);
			modifyIntent.putExtra(X_VALUES, x);
			modifyIntent.putExtra(Y_VALUES, y);
			modifyIntent.putExtra(Z_VALUES, z);
			modifyIntent.putExtra(DATA_SIZE, size);
			modifyIntent.putExtra(X_CHECK, pref_cbX);
			modifyIntent.putExtra(Y_CHECK, pref_cbY);
			modifyIntent.putExtra(Z_CHECK, pref_cbZ);
			modifyIntent.putExtra(UPSAMPL, pref_upsampl);
			modifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(modifyIntent);
			return true;
		}
		else{
			Toast.makeText(context,"Errore di creazione file", Toast.LENGTH_LONG).show();
			return false;
		}
	}



}
