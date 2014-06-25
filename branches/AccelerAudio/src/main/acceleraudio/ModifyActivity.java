/**
 * @author Mauro Bagatella
 */
package main.acceleraudio;

import static main.acceleraudio.DBOpenHelper.DATA_SIZE;
import static main.acceleraudio.DBOpenHelper.FIRST_DATE;
import static main.acceleraudio.DBOpenHelper.FIRST_TIME;
import static main.acceleraudio.DBOpenHelper.NAME;
import static main.acceleraudio.DBOpenHelper.UPSAMPL;
import static main.acceleraudio.DBOpenHelper.X_CHECK;
import static main.acceleraudio.DBOpenHelper.X_VALUES;
import static main.acceleraudio.DBOpenHelper.Y_CHECK;
import static main.acceleraudio.DBOpenHelper.Y_VALUES;
import static main.acceleraudio.DBOpenHelper.Z_CHECK;
import static main.acceleraudio.DBOpenHelper.Z_VALUES;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class ModifyActivity extends ActionBarActivity {

	private static EditText nameEditText;
	private static ImageView imageView;
	private static String session_name;
	private static String date;
	private static String time;
	private static TextView first_date;
	private static TextView last_date;
	private static CheckBox x_axis;
	private static CheckBox y_axis;
	private static CheckBox z_axis;
	private static SeekBar et_upsampl;

	private static boolean xCheck;
	private static boolean yCheck;
	private static boolean zCheck;

	private static int seekValue;
	private static int seekbar_value;

	MediaPlayer mp;

	private static Intent intent;
	private static String oldSessionName, appFileDirectory, firstData, firstTime;
	private static byte[] x, y, z;
	private static int size;

	//all file parameters
	private byte bits_per_sample = 8; // 8, 16...
	private byte num_channels = 1; // 1 = mono, 2 = stereo
	private int sample_rate = 8000; // 8000, 44100...

	//database
	private DBOpenHelper openHelper;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
			.add(R.id.container, new PlaceholderFragment()).commit();
		}



		appFileDirectory = getApplicationContext().getFilesDir().getPath() + "/";

		intent = getIntent();
		oldSessionName = intent.getStringExtra(NAME);
		x = intent.getByteArrayExtra(X_VALUES);
		y = intent.getByteArrayExtra(Y_VALUES);
		z = intent.getByteArrayExtra(Z_VALUES);
		size = intent.getIntExtra(DATA_SIZE, 0);
		firstData = intent.getStringExtra(FIRST_DATE);
		firstTime = intent.getStringExtra(FIRST_TIME);
		xCheck = intent.getBooleanExtra(X_CHECK, true);
		yCheck = intent.getBooleanExtra(Y_CHECK, true);
		zCheck = intent.getBooleanExtra(Z_CHECK, true);
		seekValue = intent.getIntExtra(UPSAMPL, 50);
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

			nameEditText = (EditText)rootView.findViewById(R.id.name);
			imageView = (ImageView) rootView.findViewById(R.id.imageView);
			first_date = (TextView)rootView.findViewById(R.id.first_date);
			last_date = (TextView)rootView.findViewById(R.id.last_date);
			x_axis = (CheckBox)rootView.findViewById(R.id.x);
			y_axis = (CheckBox)rootView.findViewById(R.id.y);
			z_axis = (CheckBox)rootView.findViewById(R.id.z);


			nameEditText.setText(oldSessionName);
			final Calendar c = Calendar.getInstance();
			int yy = c.get(Calendar.YEAR);
			int mm = c.get(Calendar.MONTH);
			int dd = c.get(Calendar.DAY_OF_MONTH);
			int hh = c.get(Calendar.HOUR_OF_DAY);
			int mn = c.get(Calendar.MINUTE);
			String months = "";
			String days = "";
			String minutes = "";

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

			date = yy + "-" + months + "-" + days;
			time = hh + ":" + minutes;

			first_date.setText(MainActivity.dateConverter(firstData) + " " + firstTime);
			last_date.setText(MainActivity.dateConverter(date) + " " + time);

			imageView.setImageURI(Uri.parse(appFileDirectory + oldSessionName + ".png"));

			x_axis.setChecked(xCheck);
			y_axis.setChecked(yCheck);
			z_axis.setChecked(zCheck);

			//Checking if at least one axes is selected
			x_axis.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){ 

				@Override 
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) { 
					if(!x_axis.isChecked() && !y_axis.isChecked() && !z_axis.isChecked()){

						Toast.makeText(getActivity(),"Devi selezionare almeno un asse", Toast.LENGTH_SHORT).show();
						buttonView.setChecked(true);

					}
				} 
			});

			y_axis.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){ 

				@Override 
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) { 
					if(!x_axis.isChecked() && !y_axis.isChecked() && !z_axis.isChecked()){

						Toast.makeText(getActivity(),"Devi selezionare almeno un asse", Toast.LENGTH_SHORT).show();
						buttonView.setChecked(true);

					}
				} 
			});

			z_axis.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){ 

				@Override 
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) { 
					if(!x_axis.isChecked() && !y_axis.isChecked() && !z_axis.isChecked()){

						Toast.makeText(getActivity(),"Devi selezionare almeno un asse", Toast.LENGTH_SHORT).show();
						buttonView.setChecked(true);

					}
				} 
			});

			final TextView tvProgress=(TextView)rootView.findViewById(R.id.progress_seekbar);
			et_upsampl = (SeekBar)rootView.findViewById(R.id.v_upsamping);
			et_upsampl.setProgress(seekValue);
			tvProgress.setText(String.valueOf(seekValue));
			seekbar_value = seekValue;

			et_upsampl.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){ 

				@Override 
				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) { 
					tvProgress.setText(String.valueOf(progress + 1)); 

					seekbar_value = progress + 1;
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

			session_name = nameEditText.getText().toString();
			//Check if a name is given
			if(session_name.equals("")){
				Toast.makeText(this,"Inserisci un nome per la sessione", Toast.LENGTH_SHORT).show();
				return false;
			}

			if(session_name.contains("  ")){
				Toast.makeText(this,"Non puoi inserire spazi consecutivi nel nome", Toast.LENGTH_SHORT).show();
				return false;
			}

			if(session_name.substring(0, 1).equals(" ")){
				Toast.makeText(this,"Il nome non può iniziare con uno spazio", Toast.LENGTH_LONG).show();
				return false;
			}

			if(session_name.length() > 12){
				session_name = session_name.substring(0, 12);
			}
			
			stopPreview(null); //if the preview still playing I stop it
			
			File fileCheck = new File(getApplicationContext().getFilesDir().getPath() + "/" + session_name + ".wav");
			if(fileCheck.exists() && !session_name.equals(oldSessionName)){

				Toast.makeText(this, session_name + " esiste già!", Toast.LENGTH_SHORT).show();
				return false;

			}

			boolean isCreated = createWavFile(session_name, x, y, z, size);

			if(isCreated){

				int duration = size * (et_upsampl.getProgress() + 1) * 1000 / sample_rate; //duration of the sound in milliSeconds
				
				openHelper = new DBOpenHelper(this);
				SQLiteDatabase db = openHelper.getWritableDatabase();

				ContentValues values = new ContentValues();
				values.put(DBOpenHelper.NAME, session_name);
				values.put(DBOpenHelper.LAST_MODIFY_DATE, date);
				values.put(DBOpenHelper.LAST_MODIFY_TIME, time);
				values.put(DBOpenHelper.DURATION, duration); 
				values.put(DBOpenHelper.UPSAMPL, et_upsampl.getProgress() + 1);     //add seekbar value
				values.put(DBOpenHelper.X_CHECK, x_axis.isChecked());
				values.put(DBOpenHelper.Y_CHECK, y_axis.isChecked());
				values.put(DBOpenHelper.Z_CHECK, z_axis.isChecked());
				db.update(DBOpenHelper.TABLE, values, NAME +"= '" + oldSessionName + "'",null);

				if(!oldSessionName.equals(session_name)){
					File dir = getFilesDir();
					File image = new File(dir, oldSessionName + ".png");
					File audio = new File(dir, oldSessionName + ".wav");
					image.renameTo(new File(dir, session_name + ".png"));
					audio.delete();
				}

				finish();
			}
			else{
				Toast.makeText(this,"Errore di creazione file", Toast.LENGTH_LONG).show();
				return false;
			}

			return true;
		}

		if (id == R.id.action_settings) {
			Intent intent = new Intent(this, PrefActivity.class);
			startActivity(intent);
		}
		return super.onOptionsItemSelected(item);
	}


	//https://ccrma.stanford.edu/courses/422/projects/WaveFormat/
	//write the .wav file
	private boolean createWavFile(String songName, byte[] x, byte[] y, byte[] z, int size){

		try {

			String file = songName + ".wav"; //name of the .wav file

			//create the data to add
			final int UPSAMPLING = seekbar_value;
			byte[] dataAdded = new byte[size * UPSAMPLING];

			int num_axes = 0;
			if(x_axis.isChecked())
				num_axes++;
			if(y_axis.isChecked())
				num_axes++;
			if(z_axis.isChecked())
				num_axes++;

			for(int i = 0; i < size; i++){

				int sum_axes = 0;

				if(x_axis.isChecked())
					sum_axes += x[i];
				if(y_axis.isChecked())
					sum_axes += y[i];
				if(z_axis.isChecked())
					sum_axes += z[i];

				for(int j = i * UPSAMPLING; j < (i + 1) * UPSAMPLING; j++)
					dataAdded[j] = (byte)(sum_axes / num_axes);
			}	

			FileOutputStream fOut = openFileOutput(file,MODE_PRIVATE);

			long totalAudioLen = dataAdded.length * num_channels * (bits_per_sample / 8);
			long chunkSize = 36 + (dataAdded.length * num_channels * (bits_per_sample / 8));
			long byteRate = sample_rate * num_channels * (bits_per_sample / 8);

			WriteWaveFileHeader(fOut, totalAudioLen, 
					chunkSize, 
					sample_rate, num_channels, 
					byteRate);

			for(int u = 0; u < 10; u++) //TODO ho una media di 10!
				averageArray(dataAdded); //loop this method for more average!

			fOut.write(dataAdded);
			fOut.close();
			//			Toast.makeText(getBaseContext(),"Il file è stato creato!",
			//					Toast.LENGTH_SHORT).show();

			return true;

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}


	//define the header of the .wav file. DON'T CHANGE IT!
	private void WriteWaveFileHeader(
			FileOutputStream out, long totalAudioLen,
			long totalDataLen, long longSampleRate, int channels,
			long byteRate) throws IOException {

		byte[] headerBuffer = new byte[44];

		headerBuffer[0] = 'R';  // RIFF/WAVE header
		headerBuffer[1] = 'I';
		headerBuffer[2] = 'F';
		headerBuffer[3] = 'F';
		headerBuffer[4] = (byte) (totalDataLen & 0xff);
		headerBuffer[5] = (byte) ((totalDataLen >> 8) & 0xff);
		headerBuffer[6] = (byte) ((totalDataLen >> 16) & 0xff);
		headerBuffer[7] = (byte) ((totalDataLen >> 24) & 0xff);
		headerBuffer[8] = 'W';
		headerBuffer[9] = 'A';
		headerBuffer[10] = 'V';
		headerBuffer[11] = 'E';
		headerBuffer[12] = 'f';  // 'fmt ' chunk
		headerBuffer[13] = 'm';
		headerBuffer[14] = 't';
		headerBuffer[15] = ' ';
		headerBuffer[16] = 16;  // 4 bytes: size of 'fmt ' chunk
		headerBuffer[17] = 0;
		headerBuffer[18] = 0;
		headerBuffer[19] = 0;
		headerBuffer[20] = 1;  // format = 1
		headerBuffer[21] = 0;
		headerBuffer[22] = (byte) channels;
		headerBuffer[23] = 0;
		headerBuffer[24] = (byte) (longSampleRate & 0xff);
		headerBuffer[25] = (byte) ((longSampleRate >> 8) & 0xff);
		headerBuffer[26] = (byte) ((longSampleRate >> 16) & 0xff);
		headerBuffer[27] = (byte) ((longSampleRate >> 24) & 0xff);
		headerBuffer[28] = (byte) (byteRate & 0xff);
		headerBuffer[29] = (byte) ((byteRate >> 8) & 0xff);
		headerBuffer[30] = (byte) ((byteRate >> 16) & 0xff);
		headerBuffer[31] = (byte) ((byteRate >> 24) & 0xff);
		headerBuffer[32] = (byte) (2 * 16 / 8);  // block align
		headerBuffer[33] = 0;
		headerBuffer[34] = bits_per_sample;  // bits per sample
		headerBuffer[35] = 0;
		headerBuffer[36] = 'd';
		headerBuffer[37] = 'a';
		headerBuffer[38] = 't';
		headerBuffer[39] = 'a';
		headerBuffer[40] = (byte) (totalAudioLen & 0xff);
		headerBuffer[41] = (byte) ((totalAudioLen >> 8) & 0xff);
		headerBuffer[42] = (byte) ((totalAudioLen >> 16) & 0xff);
		headerBuffer[43] = (byte) ((totalAudioLen >> 24) & 0xff);

		out.write(headerBuffer, 0, 44);
	}

	private void averageArray(byte[] byteArray){
		for (int i = 1; i < byteArray.length - 1; i++){
			byteArray[i] = (byte) ((byteArray[i - 1] + byteArray[i] + byteArray[i + 1]) /  3);
		}
	}


	/**
	 * This method is called when the button "Prova" is pressed and it create a temporary wav file and play it
	 * @param view the button pressed
	 */
	public void startPreview(View view){

		//create temporary files
		boolean isCreated = createWavFile("Temp", x, y, z, size);
		if(!isCreated){
			Toast.makeText(getBaseContext(),"File non creato.", Toast.LENGTH_SHORT).show();
			return;
		}


		String appFileDirectory = getApplicationContext().getFilesDir().getPath() + "/";
		if(mp != null){
			mp.stop();
			mp = null;
		}

		try{
			mp = new MediaPlayer();
			try {

				Button play = (Button)findViewById(R.id.start_bt);
				Button stop = (Button)findViewById(R.id.stop_bt);

				play.setVisibility(View.GONE);
				stop.setVisibility(View.VISIBLE);

				mp.setDataSource(appFileDirectory + "Temp.wav");
				mp.prepare();
				mp.start();
				mp.setOnCompletionListener(new OnCompletionListener() {

					@Override
					public void onCompletion(MediaPlayer m) {
						stopPreview(null);
					}
				});
			} catch (IOException e) {
				Toast.makeText(getBaseContext(),"prepare failed", Toast.LENGTH_SHORT).show();
			}
		}catch(Exception e){
			Toast.makeText(getBaseContext(),e.toString(), Toast.LENGTH_SHORT).show();
		}
	}

	public void stopPreview(View view){
		try{
			if(mp != null){

				Button play = (Button)findViewById(R.id.start_bt);
				Button stop = (Button)findViewById(R.id.stop_bt);

				play.setVisibility(View.VISIBLE);
				stop.setVisibility(View.GONE);

				mp.stop();
				mp = null;

			}
		}catch(Exception e){
			Toast.makeText(getBaseContext(),e.toString(), Toast.LENGTH_SHORT).show();
		}
	}
}
