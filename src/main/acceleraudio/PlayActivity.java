package main.acceleraudio;

import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

public class PlayActivity extends ActionBarActivity {
	static Intent intent;
	static byte[] x, y , z;
	static int size;
	
	private String file = "song.wav"; //name of the .wav file

	//all file parameters
	private byte bits_per_sample = 8; // 8, 16...
	private byte num_channels = 1; // 1 = mono, 2 = stereo
	private long sample_rate = 8000; // 8000, 44100...

	FileOutputStream fout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		intent = getIntent();
		x = intent.getByteArrayExtra(RecordService.X_VALUE);
		y = intent.getByteArrayExtra(RecordService.Y_VALUE);
		z = intent.getByteArrayExtra(RecordService.Z_VALUE);
		size = intent.getIntExtra(RecordService.SIZE, 0);
		create(x, y, z, size);
		setContentView(R.layout.activity_play);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
			.add(R.id.container, new PlaceholderFragment()).commit();
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.play, menu);
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
			View rootView = inflater.inflate(R.layout.fragment_play, container,
					false);

			TextView textView = (TextView) rootView.findViewById(R.id.show_results);

			
			String results = "";
			for (int i = 0; i < size; i++)
				results = results +" X = " + x[i] + "   Y = " + y[i] + "  Z = " + z[i] +"\n";
			textView.setText(results);

			return rootView;
		}
	}
	
	//https://ccrma.stanford.edu/courses/422/projects/WaveFormat/
		//write the .wav file
		public void create(byte[] x, byte[] y, byte[] z, int size ){

			try {

				//create the data to add
				final int UPSAMPLING = 100;
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
				Toast.makeText(getBaseContext(),"Il file è stato creato!",
						Toast.LENGTH_SHORT).show();
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
		
		
		//read the .wav file
		public void read(View view){
			try{
				MediaPlayer mp = new MediaPlayer();
				try {
					mp.setDataSource("/data/data/main.acceleraudio/files/" + file);
					mp.prepare();
					mp.start();
				} catch (IOException e) {
					Toast.makeText(getBaseContext(),"prepare failed",
							Toast.LENGTH_SHORT).show();
				}


			}catch(Exception e){
				Toast.makeText(getBaseContext(),e.toString(),
						Toast.LENGTH_SHORT).show();
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
