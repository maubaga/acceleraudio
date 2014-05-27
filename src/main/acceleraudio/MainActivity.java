package main.acceleraudio;

import static android.provider.BaseColumns._ID;
import static main.acceleraudio.DBOpenHelper.LAST_MODIFY_DATE;
import static main.acceleraudio.DBOpenHelper.NAME;
import static main.acceleraudio.DBOpenHelper.TABLE;
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
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity {

	private boolean resumeHasRun = false;
	private DBOpenHelper dbHelper;
	static String folder;

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

	final static String IMG_NAME = folder + NAME + ".png";
	private static String[] FROM = { _ID, NAME, NAME, LAST_MODIFY_DATE };
	private static String ORDER_BY = NAME + " ASC";

	private Cursor getSessions() {
		// Get all of the notes from the database and create the item list
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.query(TABLE, FROM, null, null, null, null, ORDER_BY);
		startManagingCursor(cursor);

		return cursor;
	}

	private void showSessions(Cursor cursor) {

		LinearLayout main_container = (LinearLayout)findViewById(R.id.main_container);
		main_container.removeAllViews();

		for(int i = 0; i < cursor.getCount(); i++){

			cursor.moveToPosition(i);

			LinearLayout session = new LinearLayout(this);
			session.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, getResources().getDisplayMetrics())));
			session.setOrientation(LinearLayout.HORIZONTAL);

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
			final String session_name = name.getText().toString();

			TextView date = new TextView(this);
			LinearLayout.LayoutParams params3 = new LinearLayout.LayoutParams(
					(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 120, getResources().getDisplayMetrics()), 
					LayoutParams.WRAP_CONTENT);
			params3.gravity = Gravity.CENTER_VERTICAL;
			date.setPadding((int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics()), 0, 0, 0);
			date.setTextSize(16);
			date.setText(cursor.getString(cursor.getColumnIndex(LAST_MODIFY_DATE)));
			date.setLayoutParams(params3);

			ImageButton play = new ImageButton(this);
			play.setLayoutParams(new LayoutParams((int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, getResources().getDisplayMetrics()), (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, getResources().getDisplayMetrics())));
			play.setImageResource(R.drawable.media_play);
			play.setScaleType(ScaleType.FIT_CENTER);
			play.setOnClickListener(new OnClickListener() {
				 
				@Override
				public void onClick(View arg0) {
	 
				   startSession(arg0, session_name);
	 
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
