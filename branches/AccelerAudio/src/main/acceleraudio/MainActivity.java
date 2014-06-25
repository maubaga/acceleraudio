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
import java.util.Calendar;

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
		dbHelper.close();
	}

	@Override
	protected void onResume(){
		super.onResume();

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
			Intent intent = new Intent(this, RecordActivity.class);
			startActivity(intent); 
		}
		return super.onOptionsItemSelected(item);
	}



	private Cursor getSessions() {
		// Get all of the notes from the database and create the item list
		String[] select = { NAME, LAST_MODIFY_DATE, LAST_MODIFY_TIME, FIRST_DATE, FIRST_TIME, RATE, UPSAMPL, X_CHECK, Y_CHECK, Z_CHECK, DURATION};
		String order_by = NAME + " ASC";
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.query(TABLE, select, null, null, null, null, order_by);
		return cursor;
	}

	private Cursor getArraysData(String songName) {
		// Get the three arrays from blob fields in the data base and the dimension of the arrays
		String[] select = {X_CHECK, Y_CHECK, Z_CHECK, UPSAMPL, X_VALUES, Y_VALUES, Z_VALUES, DATA_SIZE, FIRST_DATE, FIRST_TIME, RATE, DURATION};
		String where = NAME + "= '" + songName + "'";
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.query(TABLE, select, where, null, null, null, null);
		return cursor;
	}

	private void showSessions(Cursor cursor) {

		TextView empty_db = (TextView)findViewById(R.id.empty_db);
		LinearLayout index = (LinearLayout)findViewById(R.id.index);
		View index_line = (View)findViewById(R.id.index_line);
		LinearLayout main_container = (LinearLayout)findViewById(R.id.main_container);
		main_container.removeAllViews();

		if(cursor.getCount() == 0){

			index.setVisibility(View.GONE);
			index_line.setVisibility(View.GONE);
			empty_db.setVisibility(View.VISIBLE);

		} else{

			index.setVisibility(View.VISIBLE);
			index_line.setVisibility(View.VISIBLE);
			empty_db.setVisibility(View.GONE);

		}

		for(int i = 0; i < cursor.getCount(); i++){

			cursor.moveToPosition(i);
			LinearLayout session = new LinearLayout(this);
			session.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, getResources().getDisplayMetrics())));
			session.setOrientation(LinearLayout.HORIZONTAL);
			session.setClickable(true);
			session.setBackgroundResource(R.drawable.selector_colors);

			ImageView img = new ImageView(this);
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
					0, (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, getResources().getDisplayMetrics()), 20);
			params.gravity = Gravity.CENTER_VERTICAL;
			img.setImageURI(Uri.parse(folder + cursor.getString(cursor.getColumnIndex(NAME)) + ".png"));
			img.setLayoutParams(params);

			TextView name = new TextView(this);
			LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(
					0, LayoutParams.WRAP_CONTENT, 55);
			params2.gravity = Gravity.CENTER_VERTICAL;
			name.setPadding((int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics()), 0, 0, 0);
			name.setTextSize(16);
			name.setMaxLines(1);
			name.setText(cursor.getString(cursor.getColumnIndex(NAME)));
			name.setLayoutParams(params2);

			TextView date = new TextView(this);
			LinearLayout.LayoutParams params3 = new LinearLayout.LayoutParams(
					0, LayoutParams.WRAP_CONTENT, 45);
			params3.gravity = Gravity.CENTER_VERTICAL;
			date.setPadding((int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics()), 0, 0, 0);
			date.setTextSize(16);
			date.setText(dateConverter(cursor.getString(cursor.getColumnIndex(LAST_MODIFY_DATE))));
			date.setLayoutParams(params3);

			//Session's name
			final String session_name = name.getText().toString();

			//First and last date
			String first_date = dateConverter(cursor.getString(cursor.getColumnIndex(FIRST_DATE)));
			String first_time = cursor.getString(cursor.getColumnIndex(FIRST_TIME));
			String last_date = dateConverter(cursor.getString(cursor.getColumnIndex(LAST_MODIFY_DATE)));
			String last_time = cursor.getString(cursor.getColumnIndex(LAST_MODIFY_TIME));
			final String first_time_date = first_date + " " + first_time;
			final String last_time_date = last_date + " " + last_time;
			final String rate = cursor.getString(cursor.getColumnIndex(RATE)); //Rate value			
			final String upsampl = cursor.getString(cursor.getColumnIndex(UPSAMPL)); //Upsampling value
			final String duration = PlayActivity.secondToTime(cursor.getInt(cursor.getColumnIndex(DURATION)));

			//Used axes
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



			ImageButton play = new ImageButton(this);
			LinearLayout.LayoutParams params4 = new LinearLayout.LayoutParams(
					0, (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, getResources().getDisplayMetrics()), 20);
			play.setImageResource(R.drawable.media_play);
			play.setScaleType(ScaleType.FIT_CENTER);
			play.setLayoutParams(params4);
			play.setBackgroundResource(R.drawable.selector_colors);
			play.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					startSession(session_name);
				}

			});


			//Text of the Details window
			final String message = "Nome: " + session_name + "\n\nData creazione: " + first_time_date + 
					"\n\nUltima modifica: " + last_time_date + "\n\nAssi utilizzati: " + used_axes +
					"\n\nCampionamento: " + rate + " Campioni/s" + "\n\nInterpolazione: " + upsampl + 
					"\n\nDurata: " + duration;
			session.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					detailsView(message);
				}
			});

			session.setOnLongClickListener(new OnLongClickListener() { 
				@Override
				public boolean onLongClick(View v) {
					contextMenu(v, session_name);
					return true;
				}
			});


			View line = new View(this);
			line.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics())));
			line.setBackgroundColor(0x227a7a7a);



			session.addView(img);
			session.addView(name);
			session.addView(date);
			session.addView(play);
			main_container.addView(session);
			main_container.addView(line);

		}

	}



	/**
	 * AlertDialog that contain the detail of the track.
	 * @param message The detail to view in string form.
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
	 * Contextual menu that appears when it is done long press on a track.
	 * @param v The view in which it was done long press;
	 * @param session_name The name of the song.
	 */
	private void contextMenu(View v, String session_name){

		final String name = session_name;

		new AlertDialog.Builder(MainActivity.this).setTitle(session_name).setItems(R.array.context_menu,
				new DialogInterface.OnClickListener() {		

			@Override
			public void onClick(DialogInterface dialog, int which) {


				switch(which){

				case 0: //rename button
					renameSession(name);
					break;

				case 1: //modify button
					modifySession(name);
					break;

				case 2: //duplicate button
					duplicateSession(name);
					showSessions(getSessions());
					break;

				case 3: //delete button
					new AlertDialog.Builder(MainActivity.this)
					.setTitle(name)
					.setMessage(R.string.confirm_delete)
					.setIcon(null)
					.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) { 
							deleteSession(name); //here i delete the session
							showSessions(getSessions());
						}
					})
					.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) { 
							return;
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
		String[] FROM = { NAME, DURATION};
		String WHERE = NAME + " = '" + name +"'";
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.query(TABLE, FROM, WHERE, null, null, null, null);
		cursor.moveToFirst();
		int duration = cursor.getInt(cursor.getColumnIndex(DURATION));
		Intent playIntent = new Intent(this, PlayActivity.class);
		playIntent.putExtra(PlayActivity.DURATION, duration);
		playIntent.putExtra(PlayActivity.SESSION_NAME, name);
		playIntent.putExtra(PlayActivity.AUTOPLAY, true);  //the song starts automatically
		startActivity(playIntent);
	}

	/**
	 * This method is called when the "Rinomina" button is pressed and it deletes the song.
	 * @param new_name The name of the song to delete.
	 */
	private void renameSession(String new_name){

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
					Toast.makeText(MainActivity.this,"Inserisci un nome per la sessione", Toast.LENGTH_SHORT).show();
					return;
				}

				if(new_name.contains("  ")){
					Toast.makeText(MainActivity.this,"Non puoi inserire spazi consecutivi nel nome", Toast.LENGTH_SHORT).show();
					return;
				}

				if(new_name.substring(0, 1).equals(" ")){
					Toast.makeText(MainActivity.this,"Il nome non pu� iniziare con uno spazio", Toast.LENGTH_LONG).show();
					return;
				}

				if(new_name.length() > 12){
					new_name = new_name.substring(0, 12);
				}

				File fileCheck = new File(getApplicationContext().getFilesDir().getPath() + "/" + new_name + ".wav");
				if(fileCheck.exists() && !new_name.equals(name)){

					Toast.makeText(MainActivity.this, new_name + " esiste gi�!", Toast.LENGTH_SHORT).show();
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

		SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.delete(TABLE, NAME + "='" + session_name + "'", null);

		File dir = getFilesDir();
		File image = new File(dir, session_name + ".png");
		File audio = new File(dir, session_name + ".wav");
		image.delete();
		audio.delete();

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
	 * This method duplicates the song and the image associated with it. Return true if duplication is successful, 
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

		//read information from the database
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
			//duplicate the song
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

			byte[] buf = new byte[(int)inputFile.length()]; //numbers of bytes of the song + 44 bytes for the headers
			int len;
			while ((len = input.read(buf)) > 0) {
				output.write(buf, 0, len);
			}


			input.close();
			output.close();

			//duplicate the image
			inputFile = new File(folder + name + ".png");
			input = new FileInputStream(inputFile);
			output = openFileOutput(name + "-" + fileIndex +".png", MODE_PRIVATE);


			buf = new byte[(int)inputFile.length()];
			while ((len = input.read(buf)) > 0) {
				output.write(buf, 0, len);
			}

			input.close();
			output.close();

		}
		catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(this, "Non ho copiato i file", Toast.LENGTH_SHORT).show();
			return false;
		}

		final Calendar c = Calendar.getInstance();
		int yy = c.get(Calendar.YEAR);
		int mm = c.get(Calendar.MONTH);
		int dd = c.get(Calendar.DAY_OF_MONTH);
		int hh = c.get(Calendar.HOUR_OF_DAY);
		int mn = c.get(Calendar.MINUTE);
		String months = "";
		String days = "";
		String minutes = "";
		String date = "";
		String time = "";

		if(dd < 10)
			days = "0" + dd; 
		else
			days = "" + dd;
		if(mm < 10)
			months = "0" + (mm + 1); 
		else
			months = "" + (mm + 1);
		if(mn < 10)
			minutes = "0" + mn; 
		else
			minutes = "" + mn;

		date = days + "/" + months + "/" + yy;
		time = hh + ":" + minutes;


		//insert the data in the database
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
		values.put(DBOpenHelper.UPSAMPL, seekValue);       //add seekbar value
		values.put(DBOpenHelper.X_CHECK, xCheck);
		values.put(DBOpenHelper.Y_CHECK, yCheck);
		values.put(DBOpenHelper.Z_CHECK, zCheck);
		values.put(DBOpenHelper.X_VALUES, x);          //add the three byte array to the database
		values.put(DBOpenHelper.Y_VALUES, y);
		values.put(DBOpenHelper.Z_VALUES, z);
		values.put(DBOpenHelper.DATA_SIZE, size);        //add the number samples to the database
		db.insert(DBOpenHelper.TABLE, null, values);

		return true;
	}

	/**
	 * Change from integer to boolean (0 = false, any other numbers = true)
	 * @param bool the integer to convert
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
	
	public static String dateConverter(String inputDate){
		String yy = inputDate.substring(0, 4);
		String mm = inputDate.substring(5, 7);
		String dd = inputDate.substring(8, 10);
		String outputDate = dd + "/" + mm + "/" + yy;
		return outputDate;
	}

}
