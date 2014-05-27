/**
 * 
 * @author Michele Lissandrin, Matteo Franzosi
 */

package main.acceleraudio;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class PrefActivity extends ActionBarActivity {
	
	static CheckBox cbX;
	static CheckBox cbY;
	static CheckBox cbZ;
	static TextView sample_rate_values;
	static String rate_value;
	static TextView max_duration_values;
	static String duration_value;

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
			
//			recupero gli stati dei componenti utilizzando SharedPreferences
			Context context = getActivity();
			SharedPreferences preferences = context.getSharedPreferences("Session_Preferences", Context.MODE_PRIVATE); 
			boolean pref_cbX = preferences.getBoolean("cBoxSelectX", true);
			boolean pref_cbY = preferences.getBoolean("cBoxSelectY", true);
			boolean pref_cbZ = preferences.getBoolean("cBoxSelectZ", true);
			String pref_rate = preferences.getString("eTextSampleRate", getResources().getString(R.string.sample_rate1));
			String pref_maxRec = preferences.getString("eTextMaxRec", getResources().getString(R.string.duration1));
			int pref_upsampl = preferences.getInt("sbUpsampling", 100);
			
			//assegno i valori degli stati ai relativi componenti
			cbX = (CheckBox)rootView.findViewById(R.id.checkBoxX); 
			cbX.setChecked(pref_cbX);
			cbY = (CheckBox)rootView.findViewById(R.id.checkBoxY); 
			cbY.setChecked(pref_cbY);
			cbZ = (CheckBox)rootView.findViewById(R.id.checkBoxZ); 
			cbZ.setChecked(pref_cbZ);
			
			//Checking if at least one axes is selected
			cbX.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){ 

				   @Override 
				   public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) { 
					   if(!cbX.isChecked() && !cbY.isChecked() && !cbZ.isChecked()){
						   
						   Toast.makeText(getActivity(),"Devi selezionare almeno un asse", Toast.LENGTH_SHORT).show();
						   buttonView.setChecked(true);
						   
					   }
				   } 
				       });
			
			cbY.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){ 

				   @Override 
				   public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) { 
					   if(!cbX.isChecked() && !cbY.isChecked() && !cbZ.isChecked()){
						   
						   Toast.makeText(getActivity(),"Devi selezionare almeno un asse", Toast.LENGTH_SHORT).show();
						   buttonView.setChecked(true);
						   
					   }
				   } 
				       });
			
			cbZ.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){ 

				   @Override 
				   public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) { 
					   if(!cbX.isChecked() && !cbY.isChecked() && !cbZ.isChecked()){
						   
						   Toast.makeText(getActivity(),"Devi selezionare almeno un asse", Toast.LENGTH_SHORT).show();
						   buttonView.setChecked(true);
						   
					   }
				   } 
				       });
			
//			EditText et_rate = (EditText)rootView.findViewById(R.id.max_rec);
//			et_rate.setText(pref_rate);
			
			sample_rate_values = (TextView)rootView.findViewById(R.id.sample_rate_value);
			sample_rate_values.setText(pref_rate);
			
			max_duration_values = (TextView)rootView.findViewById(R.id.max_duration_value);
			max_duration_values.setText(pref_maxRec);
			
			final TextView tvProgress=(TextView)rootView.findViewById(R.id.progress_seekbar);
			SeekBar et_upsampl = (SeekBar)rootView.findViewById(R.id.v_upsamping);
			et_upsampl.setProgress(pref_upsampl);
			tvProgress.setText(String.valueOf(pref_upsampl)); 
			
			et_upsampl.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){ 

				   @Override 
				   public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) { 
					   tvProgress.setText(String.valueOf(progress + 1)); 
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
		SharedPreferences preferences = getSharedPreferences("Session_Preferences", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = preferences.edit();
		
		//stato dei checkbox relativi agli assi selezionati
		CheckBox cbX = (CheckBox)findViewById(R.id.checkBoxX); 
		boolean pref_cbX = cbX.isChecked();
		CheckBox cbY = (CheckBox)findViewById(R.id.checkBoxY); 
		boolean pref_cbY = cbY.isChecked();
		CheckBox cbZ = (CheckBox)findViewById(R.id.checkBoxZ); 
		boolean pref_cbZ = cbZ.isChecked();
		
		//stato del box di testo numerico relativo alla freq. di campionamento
		String pref_rate = rate_value;
		
		//stato del box di testo numerico relativo alla massima durata della registrazione
		String pref_maxRec = duration_value;
		
		//stato del box di testo numerico relativo al parametro di interpolazione
		TextView tvProgress=(TextView)findViewById(R.id.progress_seekbar);
		int seekbar_value = Integer.parseInt(tvProgress.getText().toString());
		int pref_upsampl = (seekbar_value);
		
		//salvo lo stato
		editor.putBoolean("cBoxSelectX", pref_cbX);
		editor.putBoolean("cBoxSelectY", pref_cbY);
		editor.putBoolean("cBoxSelectZ", pref_cbZ);
		editor.putString("eTextSampleRate", pref_rate);
		editor.putString("eTextMaxRec", pref_maxRec);
		editor.putInt("sbUpsampling", pref_upsampl);
		
		editor.commit();
	}
	
	public void sampleRateChooser(View view){
		
		new AlertDialog.Builder(this).setTitle(R.string.sample_rate).setItems(R.array.rates,
				new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						
						sample_rate_values = (TextView)findViewById(R.id.sample_rate_value);
						
						switch(which){
						
						case 0:
							rate_value = getResources().getString(R.string.sample_rate1);
							sample_rate_values.setText(R.string.sample_rate1);
							break;
							
						case 1:
							rate_value = getResources().getString(R.string.sample_rate2);
							sample_rate_values.setText(R.string.sample_rate2);
							break;
							
						case 2:
							rate_value = getResources().getString(R.string.sample_rate4);
							sample_rate_values.setText(R.string.sample_rate4);
							break;
							
						case 3:
							rate_value = getResources().getString(R.string.sample_rate6);
							sample_rate_values.setText(R.string.sample_rate6);
							break;
							
						case 4:
							rate_value = getResources().getString(R.string.sample_rate8);
							sample_rate_values.setText(R.string.sample_rate8);
							break;
						
						}
						
					}
				}).show();
		
	}
	
public void durationChooser(View view){
		
		new AlertDialog.Builder(this).setTitle(R.string.max_duration).setItems(R.array.durations,
				new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						
						max_duration_values = (TextView)findViewById(R.id.max_duration_value);
						
						switch(which){
						
						case 0:
							duration_value = getResources().getString(R.string.duration1);
							max_duration_values.setText(R.string.duration1);
							break;
							
						case 1:
							duration_value = getResources().getString(R.string.duration2);
							max_duration_values.setText(R.string.duration2);
							break;
							
						case 2:
							duration_value = getResources().getString(R.string.duration3);
							max_duration_values.setText(R.string.duration3);
							break;
							
						case 3:
							duration_value = getResources().getString(R.string.duration4);
							max_duration_values.setText(R.string.duration4);
							break;
						
						}
						
					}
				}).show();
		
	}

}
