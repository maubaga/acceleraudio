/**
 * 
 * @author Michele Lissandrin
 */

package main.acceleraudio;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import android.os.Build;

public class PrefActivity extends ActionBarActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pref);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.pref, menu);
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
			View rootView = inflater.inflate(R.layout.fragment_pref, container,
					false);
			
			//recupero gli stati dei componenti utilizzando SharedPreferences
			SharedPreferences preferences = this.getActivity().getPreferences(MODE_PRIVATE); 
			boolean pref_cbX = preferences.getBoolean("cBoxSelectX", false);
			boolean pref_cbY = preferences.getBoolean("cBoxSelectY", false);
			boolean pref_cbZ = preferences.getBoolean("cBoxSelectZ", false);
			String pref_rate = preferences.getString("eTextSampleRate", null);
			String pref_maxRec = preferences.getString("eTextMaxRec", null);
			String pref_upsampl = preferences.getString("eTextUpsampling", null);
			
			//assegno i valori degli stati ai relativi componenti
			CheckBox cbX = (CheckBox)rootView.findViewById(R.id.checkBoxX); 
			cbX.setChecked(pref_cbX);
			CheckBox cbY = (CheckBox)rootView.findViewById(R.id.checkBoxY); 
			cbY.setChecked(pref_cbY);
			CheckBox cbZ = (CheckBox)rootView.findViewById(R.id.checkBoxZ); 
			cbZ.setChecked(pref_cbZ);
			EditText et_rate = (EditText)rootView.findViewById(R.id.sampleRate);
			et_rate.setText(pref_rate);
			EditText et_maxRec = (EditText)rootView.findViewById(R.id.max_rec);
			et_maxRec.setText(pref_maxRec);
			EditText et_upsampl = (EditText)rootView.findViewById(R.id.v_upsamping);
			et_upsampl.setText(pref_upsampl);
			
			return rootView;
		}
	}
	
	protected void onPause(){
		super.onPause();
		
		//utilizzo SharedPreferece e il relativo editor per salvare lo stato dei componenti
		SharedPreferences preferences = getPreferences(MODE_PRIVATE);
		SharedPreferences.Editor editor = preferences.edit();
		
		//stato dei checkbox relativi agli assi selezionati
		CheckBox cbX = (CheckBox)findViewById(R.id.checkBoxX); 
		boolean pref_cbX = cbX.isChecked();
		CheckBox cbY = (CheckBox)findViewById(R.id.checkBoxY); 
		boolean pref_cbY = cbY.isChecked();
		CheckBox cbZ = (CheckBox)findViewById(R.id.checkBoxZ); 
		boolean pref_cbZ = cbZ.isChecked();
		
		//stato del box di testo numerico relativo alla freq. di campionamento
		EditText et_rate = (EditText)findViewById(R.id.sampleRate);
		String pref_rate = et_rate.getText().toString();
		
		//stato del box di testo numerico relativo alla massima durata della registrazione
		EditText et_maxRec = (EditText)findViewById(R.id.max_rec);
		String pref_maxRec = et_maxRec.getText().toString();
		
		//stato del box di testo numerico relativo al parametro di interpolazione
		EditText et_upsampl = (EditText)findViewById(R.id.v_upsamping);
		String pref_upsampl = et_upsampl.getText().toString();
		
		//salvo lo stato
		editor.putBoolean("cBoxSelectX", pref_cbX);
		editor.putBoolean("cBoxSelectY", pref_cbY);
		editor.putBoolean("cBoxSelectZ", pref_cbZ);
		editor.putString("eTextSampleRate", pref_rate);
		editor.putString("eTextMaxRec", pref_maxRec);
		editor.putString("eTextUpsampling", pref_upsampl);
		
		editor.commit();
	}

}
