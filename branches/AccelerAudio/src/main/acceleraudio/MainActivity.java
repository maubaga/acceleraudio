/**
 * @author Matteo Franzosi
 */
package main.acceleraudio;

import static main.acceleraudio.DBOpenHelper.DATA_SIZE;
import static main.acceleraudio.DBOpenHelper.DURATION;
import static main.acceleraudio.DBOpenHelper.FIRST_DATE;
import static main.acceleraudio.DBOpenHelper.FIRST_TIME;
import static main.acceleraudio.DBOpenHelper.LAST_MODIFY_DATE;
import static main.acceleraudio.DBOpenHelper.LAST_MODIFY_TIME;
import static main.acceleraudio.DBOpenHelper.NAME;
import static main.acceleraudio.DBOpenHelper.RATE;
import static main.acceleraudio.DBOpenHelper.TABLE;
import static main.acceleraudio.DBOpenHelper.UPSAMPL;
import static main.acceleraudio.DBOpenHelper.X_CHECK;
import static main.acceleraudio.DBOpenHelper.X_VALUES;
import static main.acceleraudio.DBOpenHelper.Y_CHECK;
import static main.acceleraudio.DBOpenHelper.Y_VALUES;
import static main.acceleraudio.DBOpenHelper.Z_CHECK;
import static main.acceleraudio.DBOpenHelper.Z_VALUES;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {

	private DBOpenHelper dbHelper;
	private static String folder;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
			.add(R.id.container, new PlaceholderFragment()).commit();
		}

	}

	@Override
	protected void onPause() {
		super.onPause();
		dbHelper.close(); // Close the database when it's not used.
	}

	@Override
	protected void onResume(){
		super.onResume();
		// Reopen the database.
		folder = getApplicationContext().getFilesDir().getPath() + "/";
		dbHelper = new DBOpenHelper(this);
		Cursor cursor = getSessions();
		showSessions(cursor);
	}

	@Override 
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			Intent intent = new Intent(this, PrefActivity.class);
			startActivity(intent);			
		}

		if (id == R.id.action_new) {
			boolean isRecordingStart = AccelerAudioUtilities.isMyServiceRunning(this, RecordService.class);
			if (isRecordingStart){ // Check if the recording is already start.
				Toast.makeText(this, getResources().getString(R.string.rec_already_start_widget), Toast.LENGTH_SHORT).show();
				return false;
			}
			Intent intent = new Intent(this, RecordActivity.class);
			startActivity(intent); 
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Get the details of all the songs in the database.
	 * @return A Cursor that contains the details.
	 */
	private Cursor getSessions() {
		String[] select = { NAME, LAST_MODIFY_DATE, LAST_MODIFY_TIME, FIRST_DATE, FIRST_TIME, RATE, UPSAMPL, X_CHECK, Y_CHECK, Z_CHECK, DURATION};
		String order_by = NAME + " ASC";
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.query(TABLE, select, null, null, null, null, order_by);
		return cursor;
	}

	/**
	 * Get the details of the songs from the database and all the data that build it.
	 * @param songName Name of the song (without extension like .wav).
	 * @return A Cursor that contains the details.
	 */
	private Cursor getArraysData(String songName) {
		// Get the three arrays from blob fields in the data base and the dimensions of the arrays.
		String[] select = {X_CHECK, Y_CHECK, Z_CHECK, UPSAMPL, X_VALUES, Y_VALUES, Z_VALUES, DATA_SIZE, FIRST_DATE, FIRST_TIME, RATE, DURATION};
		String where = NAME + "= '" + songName + "'";
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.query(TABLE, select, where, null, null, null, null);
		return cursor;
	}

	/**
	 * Show all the songs recorded; Get the data from the database and show it in a list with an image and a button that permits to play the song; 
	 * More options and details are allow thanks to a long press in the track.
	 * @param cursor A Cursor that contain the tracks to show and their details.
	 */
	private void showSessions(Cursor cursor) {

		TextView empty_db = (TextView)findViewById(R.id.empty_db);
		LinearLayout index = (LinearLayout)findViewById(R.id.index);
		View index_line = (View)findViewById(R.id.index_line);
		LinearLayout main_container = (LinearLayout)findViewById(R.id.main_container);
		main_container.removeAllViews(); // If there are some old Views, remove them.
		// Display a message if the database is empty.
		if(cursor.getCount() == 0){

			index.setVisibility(View.GONE);
			index_line.setVisibility(View.GONE);
			empty_db.setVisibility(View.VISIBLE);

		} else{

			index.setVisibility(View.VISIBLE);
			index_line.setVisibility(View.VISIBLE);
			empty_db.setVisibility(View.GONE);

		}
		// Create the LinearLayouts and fill them with the data.
		for(int i = 0; i < cursor.getCount(); i++){

			cursor.moveToPosition(i);
			// The container of a session.
			LinearLayout session = new LinearLayout(this);
			session.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, getResources().getDisplayMetrics())));
			session.setOrientation(LinearLayout.HORIZONTAL);
			session.setClickable(true);
			session.setBackgroundResource(R.drawable.selector_colors);
			// The session's image.
			ImageView img = new ImageView(this);
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
					0, (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, getResources().getDisplayMetrics()), 20);
			params.gravity = Gravity.CENTER_VERTICAL;
			img.setImageURI(Uri.parse(folder + cursor.getString(cursor.getColumnIndex(NAME)) + ".png"));
			img.setLayoutParams(params);

			// The session's name.
			TextView name = new TextView(this);
			LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(
					0, LayoutParams.WRAP_CONTENT, 55);
			params2.gravity = Gravity.CENTER_VERTICAL;
			name.setPadding((int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics()), 0, 0, 0);
			name.setTextSize(16);
			name.setMaxLines(1);
			name.setText(cursor.getString(cursor.getColumnIndex(NAME)));
			name.setLayoutParams(params2);

			// Get the session's name; The play button needs it.
			final String session_name = name.getText().toString();

			// The date on which the session has been modified for the last time.
			TextView date = new TextView(this);
			LinearLayout.LayoutParams params3 = new LinearLayout.LayoutParams(
					0, LayoutParams.WRAP_CONTENT, 45);
			params3.gravity = Gravity.CENTER_VERTICAL;
			date.setPadding((int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics()), 0, 0, 0);
			date.setTextSize(16);
			date.setText(AccelerAudioUtilities.dateConverter(cursor.getString(cursor.getColumnIndex(LAST_MODIFY_DATE))));
			date.setLayoutParams(params3);

			ImageButton play = new ImageButton(this);
			LinearLayout.LayoutParams params4 = new LinearLayout.LayoutParams(
					0, (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, getResources().getDisplayMetrics()), 20);
			play.setImageResource(R.drawable.media_play_main);
			play.setPadding(5, 5, 5, 5);
			play.setScaleType(ScaleType.FIT_CENTER);
			play.setLayoutParams(params4);
			play.setBackgroundResource(R.drawable.selector_colors);
			play.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					startSession(session_name);
				}

			});

			// Get all the data for the details window;
			// The session's dates;
			String first_date = AccelerAudioUtilities.dateConverter(cursor.getString(cursor.getColumnIndex(FIRST_DATE)));
			String first_time = cursor.getString(cursor.getColumnIndex(FIRST_TIME));
			String last_date = AccelerAudioUtilities.dateConverter(cursor.getString(cursor.getColumnIndex(LAST_MODIFY_DATE)));
			String last_time = cursor.getString(cursor.getColumnIndex(LAST_MODIFY_TIME));
			final String first_time_date = first_date + " " + first_time;
			final String last_time_date = last_date + " " + last_time;
			
			// The session's rate value.
			final String rate = cursor.getString(cursor.getColumnIndex(RATE)); 
			
			// The session's upsampling value.
			final String upsampl = cursor.getString(cursor.getColumnIndex(UPSAMPL)); 
			
			// The session's length.
			final String duration = PlayActivity.secondToTime(cursor.getInt(cursor.getColumnIndex(DURATION)));

			// The axes used by the session.
			String temp_used_axes = "";
			boolean second_axis = false;
			if(cursor.getString(cursor.getColumnIndex(X_CHECK)).equals("1")){

				second_axis = true;
				temp_used_axes = "X";

			}
			if(cursor.getString(cursor.getColumnIndex(Y_CHECK)).equals("1")){
				if(second_axis)
					temp_used_axes += " , ";

				second_axis = true;
				temp_used_axes += "Y";
			}
			if(cursor.getString(cursor.getColumnIndex(Z_CHECK)).equals("1")){
				if(second_axis)
					temp_used_axes += " , ";

				temp_used_axes += "Z";
			}			
			final String used_axes = temp_used_axes;

			// Text of the details window.
			final String message = 
					getResources().getString(R.string.detail_name) + " " + session_name +
					getResources().getString(R.string.detail_length) + " " + duration +
					getResources().getString(R.string.detail_first_time) + " " + first_time_date +
					getResources().getString(R.string.detail_last_time) + " " + last_time_date +
					getResources().getString(R.string.detail_axes) + " " + used_axes +
					getResources().getString(R.string.detail_rate) + " " + rate + " " + getResources().getString(R.string.detail_rate_value) +
					getResources().getString(R.string.detail_upsample) + " " + upsampl;
			
			// Show the details window when a session is pressed.
			session.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					detailsView(message);
				}
			});

			// Show the context menu when a session is long pressed.
			session.setOnLongClickListener(new OnLongClickListener() { 
				@Override
				public boolean onLongClick(View v) {
					contextMenu(v, session_name);
					return true;
				}
			});

			// A simple line between the sessions.
			View line = new View(this);
			line.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics())));
			line.setBackgroundColor(0x227a7a7a);

			// Add the Views to the containers.
			session.addView(img);
			session.addView(name);
			session.addView(date);
			session.addView(play);
			main_container.addView(session);
			main_container.addView(line);

		}

	}



	/**
	 * AlertDialog that contains the details of the session.
	 * @param message The details to view in string form.
	 */
	private void detailsView(String message){

		AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
		.setTitle(R.string.details)
		.setMessage(message)
		.setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) { 
				return;
			}
		})
		.show();

		TextView textView = (TextView) dialog.findViewById(android.R.id.message);
		textView.setTextSize(16);

	}

	/**
	 * Contextual menu that appears when a session is long pressed.
	 * @param v The view which was long pressed;
	 * @param session_name The name of the song.
	 */
	private void contextMenu(View v, String session_name){

		final String name = session_name;

		new AlertDialog.Builder(MainActivity.this).setTitle(session_name).setItems(R.array.context_menu,
				new DialogInterface.OnClickListener() {		

			@Override
			public void onClick(DialogInterface dialog, int which) {


				switch(which){

				case 0: // Rename button.
					renameSession(name);
					break;

				case 1: // Modify button.
					modifySession(name);
					break;

				case 2: // Duplicate button.
					duplicateSession(name);
					showSessions(getSessions());
					break;

				case 3: // Delete button.
					// Show a confirmation window.
					new AlertDialog.Builder(MainActivity.this)
					.setTitle(name)
					.setMessage(R.string.confirm_delete)
					.setIcon(null)
					.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) { 
							deleteSession(name); // Delete the session.
							showSessions(getSessions()); // Update the Views with the updated database.
						}
					})
					.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) { 
							return; // Do nothing.
						}
					})
					.show();
					break;
				}
			}
		}).show();
	}

	/**
	 * Play the session calling the PlayActivity.
	 * @param name The name of the session to play.
	 */
	private void startSession(String name){
		String[] SELECT = { NAME, DURATION};
		String WHERE = NAME + " = '" + name + "'";
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.query(TABLE, SELECT, WHERE, null, null, null, null);
		cursor.moveToFirst();
		int duration = cursor.getInt(cursor.getColumnIndex(DURATION));
		Intent playIntent = new Intent(this, PlayActivity.class);
		playIntent.putExtra(PlayActivity.DURATION, duration);
		playIntent.putExtra(PlayActivity.SESSION_NAME, name);
		playIntent.putExtra(PlayActivity.AUTOPLAY, true);  // The song starts automatically.
		startActivity(playIntent);
	}

	/**
	 * This method is called when the "Rinomina" button is pressed and it deletes the song.
	 * @param new_name The name of the song to delete.
	 */
	private void renameSession(String new_name){

		boolean isPlaying = AccelerAudioUtilities.isMyServiceRunning(this, PlayerService.class);
		if (isPlaying){ // Check if something is playing.
			String sessionInPlay = PlayerService.getSessionInPlay();
			if(new_name.equals(sessionInPlay)){ // Check if the song to rename is playing now.
				// Stop the song in background.
				Intent stopIntent = new Intent(getApplicationContext(), PlayerService.class); 
				stopService(stopIntent);
			}
		}

		final String name = new_name;

		final EditText input = new EditText(MainActivity.this);  
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.MATCH_PARENT);
		input.setLayoutParams(lp);
		input.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
		input.setText(name);

		new AlertDialog.Builder(MainActivity.this)
		.setTitle(R.string.rename)
		.setView(input)
		.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) { 

				String new_name = input.getText().toString();

				if(new_name.equals("")){
					Toast.makeText(MainActivity.this, getResources().getString(R.string.name_error1), Toast.LENGTH_SHORT).show();
					return;
				}

				if(new_name.contains("  ")){
					Toast.makeText(MainActivity.this, getResources().getString(R.string.name_error2), Toast.LENGTH_SHORT).show();
					return;
				}

				if(new_name.contains("/")){
					Toast.makeText(MainActivity.this, getResources().getString(R.string.name_error3), Toast.LENGTH_SHORT).show();
					return;
				}	

				if(new_name.substring(0, 1).equals(" ")){
					Toast.makeText(MainActivity.this, getResources().getString(R.string.name_error4), Toast.LENGTH_LONG).show();
					return;
				}

				File fileCheck = new File(getApplicationContext().getFilesDir().getPath() + "/" + new_name + ".wav");
				if(fileCheck.exists() && !new_name.equals(name)){

					Toast.makeText(MainActivity.this, new_name + " " + getResources().getString(R.string.name_error5), Toast.LENGTH_SHORT).show();
					return;

				} else{

					dbHelper = new DBOpenHelper(MainActivity.this);
					SQLiteDatabase db = dbHelper.getWritableDatabase();

					ContentValues values = new ContentValues();
					values.put(DBOpenHelper.NAME, new_name);
					db.update(DBOpenHelper.TABLE, values, NAME +"= '" + name + "'",null);
					File dir = getFilesDir();
					File image = new File(dir, name + ".png");
					File audio = new File(dir, name + ".wav");
					image.renameTo(new File(dir, new_name + ".png"));
					audio.renameTo(new File(dir, new_name + ".wav"));
					showSessions(getSessions());

					WidgetIntentReceiver.updateWidgetOnStop(getApplicationContext()); // Update the widget with the last song.

				}

			}
		})
		.setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) { 
				return;
			}
		})
		.show();

	}

	/**
	 * This method is called when the "Elimina" button is pressed and it deletes the song.
	 * @param session_name The name of the song to delete.
	 */
	private void deleteSession(String session_name){

		boolean isPlaying = AccelerAudioUtilities.isMyServiceRunning(this, PlayerService.class);
		if (isPlaying){ // Check if something is playing.
			String sessionInPlay = PlayerService.getSessionInPlay();
			if(session_name.equals(sessionInPlay)){ // Check if the song to delete is playing now.
				// Stop the song in background.
				Intent stopIntent = new Intent(getApplicationContext(), PlayerService.class); 
				stopService(stopIntent);
			}
		}

		SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.delete(TABLE, NAME + "='" + session_name + "'", null);

		File dir = getFilesDir();
		File image = new File(dir, session_name + ".png");
		File audio = new File(dir, session_name + ".wav");
		image.delete();
		audio.delete();

		WidgetIntentReceiver.updateWidgetOnStop(this); // Update the widget with the last song.
	}


	/**
	 * This method is called when the "Modifica" button is pressed and it calls ModifyActivity.
	 * @param name The name of the session to modify.
	 */
	private void modifySession(String name){
		byte[] x,y,z;
		int size, seekValue;
		Cursor cursor;
		boolean xCheck, yCheck, zCheck;

		cursor = getArraysData(name);
		cursor.moveToFirst();
		x = cursor.getBlob(cursor.getColumnIndex(X_VALUES));
		y = cursor.getBlob(cursor.getColumnIndex(Y_VALUES));
		z = cursor.getBlob(cursor.getColumnIndex(Z_VALUES));
		size = cursor.getInt(cursor.getColumnIndex(DATA_SIZE));
		String data = cursor.getString(cursor.getColumnIndex(FIRST_DATE));
		String time = cursor.getString(cursor.getColumnIndex(FIRST_TIME));
		xCheck = intToBoolean(cursor.getInt(cursor.getColumnIndex(X_CHECK)));
		yCheck = intToBoolean(cursor.getInt(cursor.getColumnIndex(Y_CHECK)));
		zCheck = intToBoolean(cursor.getInt(cursor.getColumnIndex(Z_CHECK)));
		seekValue = cursor.getInt(cursor.getColumnIndex(UPSAMPL));

		Intent modifyIntent = new Intent(this, ModifyActivity.class);
		modifyIntent.putExtra(FIRST_DATE, data);
		modifyIntent.putExtra(FIRST_TIME, time);
		modifyIntent.putExtra(NAME,name);
		modifyIntent.putExtra(X_VALUES, x);
		modifyIntent.putExtra(Y_VALUES, y);
		modifyIntent.putExtra(Z_VALUES, z);
		modifyIntent.putExtra(DATA_SIZE, size);
		modifyIntent.putExtra(X_CHECK, xCheck);
		modifyIntent.putExtra(Y_CHECK, yCheck);
		modifyIntent.putExtra(Z_CHECK, zCheck);
		modifyIntent.putExtra(UPSAMPL, seekValue);

		startActivity(modifyIntent);
	}


	/**
	 * Duplicates the song and the image associated with it. Return true if the duplication is successful, 
	 * else return false if an error is occurred.
	 * @param name The name of the song (without ".wav")
	 * @return true if duplication is successful, false otherwise.
	 */
	private boolean duplicateSession(String name){
		byte[] x,y,z;
		int size, seekValue, rate, duration;
		Cursor cursor;
		boolean xCheck, yCheck, zCheck;
		int fileIndex = 2;

		// Read information from the database.
		cursor = getArraysData(name);
		cursor.moveToFirst();
		x = cursor.getBlob(cursor.getColumnIndex(X_VALUES));
		y = cursor.getBlob(cursor.getColumnIndex(Y_VALUES));
		z = cursor.getBlob(cursor.getColumnIndex(Z_VALUES));
		size = cursor.getInt(cursor.getColumnIndex(DATA_SIZE));
		xCheck = intToBoolean(cursor.getInt(cursor.getColumnIndex(X_CHECK)));
		yCheck = intToBoolean(cursor.getInt(cursor.getColumnIndex(Y_CHECK)));
		zCheck = intToBoolean(cursor.getInt(cursor.getColumnIndex(Z_CHECK)));
		seekValue = cursor.getInt(cursor.getColumnIndex(UPSAMPL));
		rate = cursor.getInt(cursor.getColumnIndex(RATE));
		duration = cursor.getInt(cursor.getColumnIndex(DURATION));

		try{
			// Duplicate the song.
			File inputFile = new File(folder + name + ".wav");
			FileInputStream input = new FileInputStream(inputFile);

			while(true){
				File outputFile = new File(folder + name + "-" + fileIndex +".wav");
				if (!outputFile.exists())
					break;
				else
					fileIndex++;
			}
			FileOutputStream output = openFileOutput(name + "-" + fileIndex +".wav", MODE_PRIVATE);

			byte[] buf = new byte[(int)inputFile.length()]; // Numbers of bytes of the song + 44 bytes for the headers.
			int len;
			while ((len = input.read(buf)) > 0) {
				output.write(buf, 0, len);
			}


			input.close();
			output.close();

			// Create a new image.
			AccelerAudioUtilities.saveImage(this, name + "-" + fileIndex, AccelerAudioUtilities.createImage());

		}
		catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(this, getResources().getString(R.string.duplicate_error), Toast.LENGTH_SHORT).show();
			return false;
		}


		// Get date and time.
		String date = AccelerAudioUtilities.getCurrentDate();
		String time = AccelerAudioUtilities.getCurrentTime();

		// Insert the data in the database.
		DBOpenHelper oh = new DBOpenHelper(getApplicationContext());
		SQLiteDatabase db = oh.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(DBOpenHelper.NAME, name + "-" + fileIndex);
		values.put(DBOpenHelper.FIRST_DATE, date);
		values.put(DBOpenHelper.FIRST_TIME, time);
		values.put(DBOpenHelper.LAST_MODIFY_DATE, date);
		values.put(DBOpenHelper.LAST_MODIFY_TIME, time);
		values.put(DBOpenHelper.RATE, rate);     
		values.put(DBOpenHelper.DURATION, duration);     
		values.put(DBOpenHelper.UPSAMPL, seekValue);       // Add the seekbar value.
		values.put(DBOpenHelper.X_CHECK, xCheck);
		values.put(DBOpenHelper.Y_CHECK, yCheck);
		values.put(DBOpenHelper.Z_CHECK, zCheck);
		values.put(DBOpenHelper.X_VALUES, x);          // Add the three byte arrays to the database.
		values.put(DBOpenHelper.Y_VALUES, y);
		values.put(DBOpenHelper.Z_VALUES, z);
		values.put(DBOpenHelper.DATA_SIZE, size);        // Add the number of samples to the database.
		db.insert(DBOpenHelper.TABLE, null, values);

		WidgetIntentReceiver.updateWidgetOnStop(this); // Update the widget with the last song.

		return true;
	}

	/**
	 * Convert from integer to boolean (0 = false, any other numbers = true)
	 * @param bool The integer to convert.
	 * @return true if the parameter is different from 0, else otherwise.
	 */
	private boolean intToBoolean(int bool){
		if(bool != 0)
			return true;
		else
			return false;
	}


	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			return rootView;
		}
	}
}
