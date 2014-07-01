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

//class that manages the preferences of the app
public class PrefActivity extends ActionBarActivity {
	
	static CheckBox cbX;
	static CheckBox cbY;
	static CheckBox cbZ;
	static TextView sample_rate_values;
	static String rate_value;
	static TextView max_duration_values;
	static String duration_value;
	
	//keys for the values contained in SharedPreferences
	static final String KEY_SELECT_X="cBoxSelectX";
	static final String KEY_SELECT_Y="cBoxSelectY";
	static final String KEY_SELECT_Z="cBoxSelectZ";
	static final String KEY_RATE="sbRate";
	static final String KEY_MAX_REC="eTextMaxRec";
	static final String KEY_UPSAMPL="sbUpsampling";

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
			
			//recovery states of the components using SharedPreferences
			Context context = getActivity();
			SharedPreferences preferences = context.getSharedPreferences("Session_Preferences", Context.MODE_PRIVATE);
			boolean pref_cbX = preferences.getBoolean(KEY_SELECT_X, true);
			boolean pref_cbY = preferences.getBoolean(KEY_SELECT_Y, true);
			boolean pref_cbZ = preferences.getBoolean(KEY_SELECT_Z, true);
			int pref_rate = preferences.getInt(KEY_RATE, 100);
			String pref_maxRec = preferences.getString(KEY_MAX_REC, getResources().getString(R.string.duration1));
			int pref_upsampl = preferences.getInt(KEY_UPSAMPL, 100);
			
			//assignment of the values of the states related to the components
			cbX = (CheckBox)rootView.findViewById(R.id.checkBoxX); 
			cbX.setChecked(pref_cbX);
			cbY = (CheckBox)rootView.findViewById(R.id.checkBoxY); 
			cbY.setChecked(pref_cbY);
			cbZ = (CheckBox)rootView.findViewById(R.id.checkBoxZ); 
			cbZ.setChecked(pref_cbZ);
			
			//checking if, at least, one axes is selected
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
			
			//assignment of the values of the states related to the components
			max_duration_values = (TextView)rootView.findViewById(R.id.max_duration_value);
			max_duration_values.setText(pref_maxRec);
			
			final TextView tvProgress_rate=(TextView)rootView.findViewById(R.id.progress_seekbar_rate);
			SeekBar sb_rate = (SeekBar)rootView.findViewById(R.id.v_sample_rate);
			sb_rate.setProgress(pref_rate);
			tvProgress_rate.setText(String.valueOf(pref_rate)); 
			
			//Sets a listener to receive notifications of changes to the 
			//SeekBar's progress level, relative to sample-rate
			sb_rate.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){ 

				   @Override 
				   public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) { 
					   tvProgress_rate.setText(String.valueOf(progress + 1)); 
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
			
			//assignment of the values of the states related to the components
			final TextView tvProgress=(TextView)rootView.findViewById(R.id.progress_seekbar);
			SeekBar sb_upsampl = (SeekBar)rootView.findViewById(R.id.v_upsamping);
			sb_upsampl.setProgress(pref_upsampl);
			tvProgress.setText(String.valueOf(pref_upsampl)); 
			
			//Sets a listener to receive notifications of changes to the 
			//SeekBar's progress level, relative to upsampling
			sb_upsampl.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){ 

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
		
		//make use of SharedPrefereces and its editor to save the state of the components
		SharedPreferences preferences = getSharedPreferences("Session_Preferences", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = preferences.edit();
		
		//state of CheckBoxes relative to the selected axes
		CheckBox cbX = (CheckBox)findViewById(R.id.checkBoxX); 
		boolean pref_cbX = cbX.isChecked();
		CheckBox cbY = (CheckBox)findViewById(R.id.checkBoxY); 
		boolean pref_cbY = cbY.isChecked();
		CheckBox cbZ = (CheckBox)findViewById(R.id.checkBoxZ); 
		boolean pref_cbZ = cbZ.isChecked();
		
		//state of TextBox relative to the sample-rate
		TextView tvProgress_rate=(TextView)findViewById(R.id.progress_seekbar_rate);
		int pref_rate = Integer.parseInt(tvProgress_rate.getText().toString());
		
		//state of TextBox relative to the max duration of record
		String pref_maxRec = duration_value;
		
		//state of TextBox relative to the upsampling
		TextView tvProgress=(TextView)findViewById(R.id.progress_seekbar);
		int pref_upsampl = Integer.parseInt(tvProgress.getText().toString());
		
		//save state with Editor
		editor.putBoolean(KEY_SELECT_X, pref_cbX);
		editor.putBoolean(KEY_SELECT_Y, pref_cbY);
		editor.putBoolean(KEY_SELECT_Z, pref_cbZ);
		editor.putInt(KEY_RATE, pref_rate);
		editor.putString(KEY_MAX_REC, pref_maxRec);
		editor.putInt(KEY_UPSAMPL, pref_upsampl);
	
		editor.commit();
	}
	
//a pop-up for choose the duration of record
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
