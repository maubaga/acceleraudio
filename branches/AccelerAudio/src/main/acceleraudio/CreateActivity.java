package main.acceleraudio;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

import android.content.Intent;
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
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class CreateActivity extends ActionBarActivity {

	static EditText name;
	static String session_name;
	static TextView first_time;
	static TextView last_time;
	static CheckBox x_axis;
	static CheckBox y_axis;
	static CheckBox z_axis;
	static SeekBar et_upsampl;

	static boolean pref_cbX;
	static boolean pref_cbY;
	static boolean pref_cbZ;

	static int pref_upsampl;
	static int seekbar_value;

	private String file;

	static Intent intent;
	static byte[] x, y , z;
	static int size;

	//all file parameters
	private byte bits_per_sample = 8; // 8, 16...
	private byte num_channels = 1; // 1 = mono, 2 = stereo
	private long sample_rate = 8000; // 8000, 44100...

	FileOutputStream fout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
			.add(R.id.container, new PlaceholderFragment()).commit();
		}

		SharedPreferences preferences = this.getSharedPreferences("prova", MODE_PRIVATE); 
		pref_cbX = preferences.getBoolean("cBoxSelectX", false);
		pref_cbY = preferences.getBoolean("cBoxSelectY", false);
		pref_cbZ = preferences.getBoolean("cBoxSelectZ", false);
		pref_upsampl = preferences.getInt("sbUpsampling", 0);

		intent = getIntent();
		x = intent.getByteArrayExtra(RecordService.X_VALUE);
		y = intent.getByteArrayExtra(RecordService.Y_VALUE);
		z = intent.getByteArrayExtra(RecordService.Z_VALUE);
		size = intent.getIntExtra(RecordService.SIZE, 0);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.create, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		if (id == R.id.action_accept) {
			create(x, y, z, size);
		}

		if (id == R.id.action_settings) {
			Intent intent = new Intent(this, PrefActivity.class);
			startActivity(intent);
		}
		return super.onOptionsItemSelected(item);
	}

	//https://ccrma.stanford.edu/courses/422/projects/WaveFormat/
	//write the .wav file
	public void create(byte[] x, byte[] y, byte[] z, int size ){

		try {

			if(!x_axis.isChecked()){

				for(int i = 0; i < x.length; i++){

					x[i] = 0;

				}

			}

			if(!y_axis.isChecked()){

				for(int i = 0; i < y.length; i++){

					y[i] = 0;

				}

			}
			
			if(!z_axis.isChecked()){

				for(int i = 0; i < z.length; i++){

					z[i] = 0;

				}

			}

			//Save the name of the session
			session_name = name.getText().toString();

			file = session_name + ".wav"; //name of the .wav file

			//create the data to add
			final int UPSAMPLING = seekbar_value;
			byte[] dataAdded = new byte[size * UPSAMPLING];
			for(int i = 0; i < size; i++)
				for(int j = i * UPSAMPLING; j < (i + 1) * UPSAMPLING; j++)
					dataAdded[j] = (byte)((x[i] + y[i] + z[i]) / 3); //TODO trovare un modo più intelligente per usare i valori

			FileOutputStream fOut = openFileOutput(file,MODE_PRIVATE);

			long totalAudioLen = dataAdded.length * num_channels * (bits_per_sample / 8);
			long chunkSize = 36 + (dataAdded.length * num_channels * (bits_per_sample / 8));
			long byteRate = sample_rate * num_channels * (bits_per_sample / 8);

			WriteWaveFileHeader(fOut, totalAudioLen, 
					chunkSize, 
					sample_rate, num_channels, 
					byteRate);

			averageArray(dataAdded); //loop this method for more average!

			fOut.write(dataAdded);
			fOut.close();
			//				Toast.makeText(getBaseContext(),"Il file è stato creato!",
			//						Toast.LENGTH_SHORT).show();

			Intent createIntent = new Intent(this, PlayActivity.class);
			createIntent.putExtra("session_name", session_name);

			startActivity(createIntent);

		} catch (Exception e) {
			e.printStackTrace();
		}

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
			View rootView = inflater.inflate(R.layout.fragment_create,
					container, false);

			name = (EditText)rootView.findViewById(R.id.name);
			first_time = (TextView)rootView.findViewById(R.id.first_time);
			last_time = (TextView)rootView.findViewById(R.id.last_time);
			x_axis = (CheckBox)rootView.findViewById(R.id.x);
			y_axis = (CheckBox)rootView.findViewById(R.id.y);
			z_axis = (CheckBox)rootView.findViewById(R.id.z);

			final Calendar c = Calendar.getInstance();
			int yy = c.get(Calendar.YEAR);
			int mm = c.get(Calendar.MONTH);
			int dd = c.get(Calendar.DAY_OF_MONTH);

			first_time.setText(dd + "/" + (mm + 1) + "/" + yy);
			last_time.setText(dd + "/" + (mm + 1) + "/" + yy);

			x_axis.setChecked(pref_cbX);
			y_axis.setChecked(pref_cbY);
			z_axis.setChecked(pref_cbZ);

			final TextView tvProgress=(TextView)rootView.findViewById(R.id.progress_seekbar);
			et_upsampl = (SeekBar)rootView.findViewById(R.id.v_upsamping);
			et_upsampl.setProgress(pref_upsampl);
			tvProgress.setText(String.valueOf(pref_upsampl));
			seekbar_value = pref_upsampl;

			et_upsampl.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){ 

				@Override 
				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) { 
					tvProgress.setText(String.valueOf(progress)); 

					seekbar_value = progress;
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

	//define the header of the .wav file. DON'T CHANGE IT!
	private void WriteWaveFileHeader(
			FileOutputStream out, long totalAudioLen,
			long totalDataLen, long longSampleRate, int channels,
			long byteRate) throws IOException {

		byte[] header = new byte[44];

		header[0] = 'R';  // RIFF/WAVE header
		header[1] = 'I';
		header[2] = 'F';
		header[3] = 'F';
		header[4] = (byte) (totalDataLen & 0xff);
		header[5] = (byte) ((totalDataLen >> 8) & 0xff);
		header[6] = (byte) ((totalDataLen >> 16) & 0xff);
		header[7] = (byte) ((totalDataLen >> 24) & 0xff);
		header[8] = 'W';
		header[9] = 'A';
		header[10] = 'V';
		header[11] = 'E';
		header[12] = 'f';  // 'fmt ' chunk
		header[13] = 'm';
		header[14] = 't';
		header[15] = ' ';
		header[16] = 16;  // 4 bytes: size of 'fmt ' chunk
		header[17] = 0;
		header[18] = 0;
		header[19] = 0;
		header[20] = 1;  // format = 1
		header[21] = 0;
		header[22] = (byte) channels;
		header[23] = 0;
		header[24] = (byte) (longSampleRate & 0xff);
		header[25] = (byte) ((longSampleRate >> 8) & 0xff);
		header[26] = (byte) ((longSampleRate >> 16) & 0xff);
		header[27] = (byte) ((longSampleRate >> 24) & 0xff);
		header[28] = (byte) (byteRate & 0xff);
		header[29] = (byte) ((byteRate >> 8) & 0xff);
		header[30] = (byte) ((byteRate >> 16) & 0xff);
		header[31] = (byte) ((byteRate >> 24) & 0xff);
		header[32] = (byte) (2 * 16 / 8);  // block align
		header[33] = 0;
		header[34] = bits_per_sample;  // bits per sample
		header[35] = 0;
		header[36] = 'd';
		header[37] = 'a';
		header[38] = 't';
		header[39] = 'a';
		header[40] = (byte) (totalAudioLen & 0xff);
		header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
		header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
		header[43] = (byte) ((totalAudioLen >> 24) & 0xff);

		out.write(header, 0, 44);
	}

	private void averageArray(byte[] byteArray){
		for (int i = 1; i < byteArray.length - 1; i++){
			byteArray[i] = (byte) ((byteArray[i - 1] + byteArray[i] + byteArray[i + 1]) /  3);
		}
	}

}
