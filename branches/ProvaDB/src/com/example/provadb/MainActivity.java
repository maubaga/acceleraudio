package com.example.provadb;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;

public class MainActivity extends ActionBarActivity {

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
			return true;
		}
		return super.onOptionsItemSelected(item);
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
	
	@Override
	protected void onStart(){
		super.onStart();
		
		MyOpenHelper prova=new MyOpenHelper(this);
		SQLiteDatabase db = prova.getWritableDatabase(); 
		ContentValues values = new ContentValues();
		values.put(prova.MATR, 1005523);
		values.put(prova.NAME, "Giovanni Rossi");
		db.insert(prova.TABLE, null, values);
		
		String[] colonne = {prova.MATR, prova.NAME};
		Cursor cr = db.query(prova.TABLE, colonne, null, null, null, null, null, null);
		
		cr.moveToFirst();
		for(int i=0; i<cr.getColumnCount(); i++){
			System.out.println(cr.getColumnName(i)+": "+cr.getInt(i)+";");
			i++;
			System.out.println(cr.getColumnName(i)+": "+cr.getString(i)+";");
		}
	}

}
