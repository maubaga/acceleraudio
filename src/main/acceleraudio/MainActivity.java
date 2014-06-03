package main.acceleraudio;

import static main.acceleraudio.DBOpenHelper.DATA_SIZE;
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

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity {

	private boolean resumeHasRun = false;
	private DBOpenHelper dbHelper;
	private static String folder;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_main);
		//		if (savedInstanceState == null) {
		//			getSupportFragmentManager().beginTransaction()
		//					.add(R.id.container, new PlaceholderFragment()).commit();
		//		}
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
		String[] FROM = { NAME, LAST_MODIFY_DATE, LAST_MODIFY_TIME, FIRST_DATE, FIRST_TIME, RATE, UPSAMPL, X_CHECK, Y_CHECK, Z_CHECK };
		String ORDER_BY = NAME + " ASC";
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.query(TABLE, FROM, null, null, null, null, ORDER_BY);
		startManagingCursor(cursor);
		//TODO Trovare un metodo alternativo che non sia deprecato
		return cursor;
	}
	
	private Cursor getArraysData(String songName) {
		// Get the three arrays from blob fields in the data base and the dimension of the arrays
		String[] FROM = { X_VALUES, Y_VALUES, Z_VALUES, DATA_SIZE, FIRST_DATE, FIRST_TIME};
		String WHERE = NAME + "= '" + songName + "'";
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.query(TABLE, FROM, WHERE, null, null, null, null);
		startManagingCursor(cursor);
		//TODO Trovare un metodo alternativo che non sia deprecato
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
					(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, getResources().getDisplayMetrics()), 
					(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, getResources().getDisplayMetrics()));
			params.gravity = Gravity.CENTER_VERTICAL;
			img.setImageURI(Uri.parse(folder + cursor.getString(cursor.getColumnIndex(NAME)) + ".png"));
			img.setLayoutParams(params);

			TextView name = new TextView(this);
			LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(
					(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 120, getResources().getDisplayMetrics()), 
					LayoutParams.WRAP_CONTENT);
			params2.gravity = Gravity.CENTER_VERTICAL;
			name.setPadding((int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics()), 0, 0, 0);
			name.setTextSize(16);
			name.setText(cursor.getString(cursor.getColumnIndex(NAME)));
			name.setLayoutParams(params2);

			TextView date = new TextView(this);
			LinearLayout.LayoutParams params3 = new LinearLayout.LayoutParams(
					(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 120, getResources().getDisplayMetrics()), 
					LayoutParams.WRAP_CONTENT);
			params3.gravity = Gravity.CENTER_VERTICAL;
			date.setPadding((int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics()), 0, 0, 0);
			date.setTextSize(16);
			date.setText(cursor.getString(cursor.getColumnIndex(LAST_MODIFY_DATE)));
			date.setLayoutParams(params3);

			//Session's name
			final String session_name = name.getText().toString();

			//First and last date
			String first_date = cursor.getString(cursor.getColumnIndex(FIRST_DATE));
			String first_time = cursor.getString(cursor.getColumnIndex(FIRST_TIME));
			String last_date = cursor.getString(cursor.getColumnIndex(LAST_MODIFY_DATE));
			String last_time = cursor.getString(cursor.getColumnIndex(LAST_MODIFY_TIME));
			final String first_time_date = first_date + " " + first_time;
			final String last_time_date = last_date + " " + last_time;

			//Rate value
			final String rate = cursor.getString(cursor.getColumnIndex(RATE));

			//Upsampling value
			final String upsampl = cursor.getString(cursor.getColumnIndex(UPSAMPL));

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
			play.setLayoutParams(new LayoutParams((int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, getResources().getDisplayMetrics()), (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, getResources().getDisplayMetrics())));
			play.setImageResource(R.drawable.media_play);
			play.setScaleType(ScaleType.FIT_CENTER);
			play.setBackgroundResource(R.drawable.selector_colors);
			play.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					startSession(arg0, session_name);
				}

			});
			
			
			//Text of the Details window
			final String message = "Nome: " + session_name + "\n\nData creazione: " + first_time_date + 
					"\n\nUltima modifica: " + last_time_date + "\n\nAssi utilizzati: " + used_axes +
					"\n\nCampionamento: " + rate + " Campioni/s" + "\n\nInterpolazione: " + upsampl;
			session.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					detailsView(arg0, message);
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

	private void startSession(View view, String name){

		Intent intent = new Intent(this, PlayActivity.class);
		intent.putExtra("session_name", name);
		startActivity(intent);

	}

	private void detailsView(View v, String message){

		new AlertDialog.Builder(MainActivity.this)
		.setTitle(R.string.details)
		.setMessage(message)
		.setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) { 
				return;
			}
		})
		.show();

	}

	private void contextMenu(View v, String session_name){

		final String name = session_name;

		new AlertDialog.Builder(MainActivity.this).setTitle(session_name).setItems(R.array.context_menu,
				new DialogInterface.OnClickListener() {		

			@Override
			public void onClick(DialogInterface dialog, int which) {
				byte[] x,y,z;
				int size;
				Cursor cursor;
				
				switch(which){

				case 0: //TODO pass the preferences from the database
					cursor = getArraysData(name);
					cursor.moveToFirst();
					x = cursor.getBlob(cursor.getColumnIndex(X_VALUES));
					y = cursor.getBlob(cursor.getColumnIndex(Y_VALUES));
					z = cursor.getBlob(cursor.getColumnIndex(Z_VALUES));
					size = cursor.getInt(cursor.getColumnIndex(DATA_SIZE));
					String data = cursor.getString(cursor.getColumnIndex(FIRST_DATE));
					String time = cursor.getString(cursor.getColumnIndex(FIRST_TIME));
					startModifyActivity(name, data, time, x, y, z, size);

					break;

				case 1: //TODO now duplicate make a new file with the same arrays
					cursor = getArraysData(name);
					cursor.moveToFirst();
					x = cursor.getBlob(cursor.getColumnIndex(X_VALUES));
					y = cursor.getBlob(cursor.getColumnIndex(Y_VALUES));
					z = cursor.getBlob(cursor.getColumnIndex(Z_VALUES));
					size = cursor.getInt(cursor.getColumnIndex(DATA_SIZE));
					startCreateActivity(x, y, z, size);
					break;

				case 2:       //delete button
					new AlertDialog.Builder(MainActivity.this)
					.setTitle(name)
					.setMessage(R.string.confirm_delete)
					.setIcon(null)
					.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) { 
							deleteSession(name); //here i delete the session
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

	private void deleteSession(String session_name){

		SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.delete(TABLE, NAME + "='" + session_name + "'", null);

		File dir = getFilesDir();
		File image = new File(dir, session_name + ".png");
		File audio = new File(dir, session_name + ".wav");
		image.delete();
		audio.delete();

		showSessions(getSessions());

	}

	@Override
	protected void onResume(){

		super.onResume();
		if (!resumeHasRun) {
			resumeHasRun = true;
			return;
		} else{

			folder = getApplicationContext().getFilesDir().getPath() + "/";

			dbHelper = new DBOpenHelper(this);
			Cursor cursor = getSessions();
			showSessions(cursor);

		}	
	}
	
	private void startModifyActivity(String session_name, String data, String time, byte[] x, byte[] y, byte[] z, int size){
		Intent modifyIntent = new Intent(this, ModifyActivity.class);
		modifyIntent.putExtra(FIRST_DATE, data);
		modifyIntent.putExtra(FIRST_TIME, time);
		modifyIntent.putExtra(NAME, session_name);
		modifyIntent.putExtra(X_VALUES, x);
		modifyIntent.putExtra(Y_VALUES, y);
		modifyIntent.putExtra(Z_VALUES, z);
		modifyIntent.putExtra(DATA_SIZE, size);

		startActivity(modifyIntent);
	}
	
	private void startCreateActivity(byte[] x, byte[] y, byte[] z, int size){
		Intent createIntent = new Intent(this, CreateActivity.class);
		createIntent.putExtra(RecordService.X_VALUE, x);
		createIntent.putExtra(RecordService.Y_VALUE, y);
		createIntent.putExtra(RecordService.Z_VALUE, z);
		createIntent.putExtra(RecordService.SIZE, size);

		startActivity(createIntent);
	}


	//	/**
	//	 * A placeholder fragment containing a simple view.
	//	 */
	//	public static class PlaceholderFragment extends Fragment {
	//
	//		public PlaceholderFragment() {
	//		}
	//
	//		@Override
	//		public View onCreateView(LayoutInflater inflater, ViewGroup container,
	//				Bundle savedInstanceState) {
	//			View rootView = inflater.inflate(R.layout.fragment_main, container,
	//					false);
	//			return rootView;
	//		}
	//	}

}
