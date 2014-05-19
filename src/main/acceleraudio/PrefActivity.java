/**
 * 
 * @author Michele Lissandrin
 */

package main.acceleraudio;

import main.acceleraudio.R.array;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

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

		@SuppressWarnings("rawtypes")
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_pref, container,
					false);
			
//			recupero gli stati dei componenti utilizzando SharedPreferences
			Context context = getActivity();
			SharedPreferences preferences = context.getSharedPreferences("prova", Context.MODE_PRIVATE); 
			boolean pref_cbX = preferences.getBoolean("cBoxSelectX", false);
			boolean pref_cbY = preferences.getBoolean("cBoxSelectY", false);
			boolean pref_cbZ = preferences.getBoolean("cBoxSelectZ", false);
			String pref_rate = preferences.getString("eTextSampleRate", null);
			String pref_maxRec = preferences.getString("eTextMaxRec", null);
			int pref_upsampl = preferences.getInt("sbUpsampling", 0);
			
			//assegno i valori degli stati ai relativi componenti
			CheckBox cbX = (CheckBox)rootView.findViewById(R.id.checkBoxX); 
			cbX.setChecked(pref_cbX);
			CheckBox cbY = (CheckBox)rootView.findViewById(R.id.checkBoxY); 
			cbY.setChecked(pref_cbY);
			CheckBox cbZ = (CheckBox)rootView.findViewById(R.id.checkBoxZ); 
			cbZ.setChecked(pref_cbZ);
			
//			EditText et_rate = (EditText)rootView.findViewById(R.id.max_rec);
//			et_rate.setText(pref_rate);
			
			Spinner s1 = (Spinner)rootView.findViewById(R.id.sample_rate);
			ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(getActivity().getBaseContext(), array.sample_values, 
					android.R.layout.simple_spinner_item);
			adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			s1.setAdapter(adapter1);
			
			@SuppressWarnings("unchecked")
			ArrayAdapter<CharSequence> myAdap1 = (ArrayAdapter) s1.getAdapter(); //cast to an ArrayAdapter

			int spinner1Position = myAdap1.getPosition(pref_rate);

			//set the default according to value
			s1.setSelection(spinner1Position);
			
			Spinner s2 = (Spinner)rootView.findViewById(R.id.duration);
			ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(getActivity().getBaseContext(), array.rec_values, 
					android.R.layout.simple_spinner_item);
			adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			s2.setAdapter(adapter2);
			
			@SuppressWarnings("unchecked")
			ArrayAdapter<CharSequence> myAdap2 = (ArrayAdapter) s2.getAdapter(); //cast to an ArrayAdapter

			int spinner2Position = myAdap2.getPosition(pref_maxRec);

			//set the default according to value
			s2.setSelection(spinner2Position);
			
			final TextView tvProgress=(TextView)rootView.findViewById(R.id.progress_seekbar);
			SeekBar et_upsampl = (SeekBar)rootView.findViewById(R.id.v_upsamping);
			et_upsampl.setProgress(pref_upsampl);
			tvProgress.setText(String.valueOf(pref_upsampl)); 
			
			et_upsampl.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){ 

				   @Override 
				   public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) { 
					   tvProgress.setText(String.valueOf(progress)); 
				   } 

				   @Override 
				   public void onStartTrackingTouch(SeekBar seekBar) { 
					   //no need to use this
				   } 

				   @Override 
				   public void onStopTrackingTouch(SeekBar seekBar) {
					   //no need to use this
				   } 
				       });
			
			return rootView;
		}
	}
	
	protected void onPause(){
		super.onPause();
		
		//utilizzo SharedPreferece e il relativo editor per salvare lo stato dei componenti
		SharedPreferences preferences = getSharedPreferences("prova", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = preferences.edit();
		
		//stato dei checkbox relativi agli assi selezionati
		CheckBox cbX = (CheckBox)findViewById(R.id.checkBoxX); 
		boolean pref_cbX = cbX.isChecked();
		CheckBox cbY = (CheckBox)findViewById(R.id.checkBoxY); 
		boolean pref_cbY = cbY.isChecked();
		CheckBox cbZ = (CheckBox)findViewById(R.id.checkBoxZ); 
		boolean pref_cbZ = cbZ.isChecked();
		
		//stato del box di testo numerico relativo alla freq. di campionamento
		Spinner s1 = (Spinner)findViewById(R.id.sample_rate);
		String pref_rate = s1.getSelectedItem().toString();
		
		//stato del box di testo numerico relativo alla massima durata della registrazione
		Spinner s2 = (Spinner)findViewById(R.id.duration);
		String pref_maxRec = s2.getSelectedItem().toString();
		
		//stato del box di testo numerico relativo al parametro di interpolazione
		SeekBar et_upsampl = (SeekBar)findViewById(R.id.v_upsamping);
		int pref_upsampl = et_upsampl.getProgress();
		
		//salvo lo stato
		editor.putBoolean("cBoxSelectX", pref_cbX);
		editor.putBoolean("cBoxSelectY", pref_cbY);
		editor.putBoolean("cBoxSelectZ", pref_cbZ);
		editor.putString("eTextSampleRate", pref_rate);
		editor.putString("eTextMaxRec", pref_maxRec);
		editor.putInt("sbUpsampling", pref_upsampl);
		
		editor.commit();
	}

}
