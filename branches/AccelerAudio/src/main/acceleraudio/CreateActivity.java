/**
 * @author Mauro Bagatella
 */
package main.acceleraudio;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class CreateActivity extends ActionBarActivity {

	static EditText name;
	static ImageView imageView;
	static Bitmap session_image;
	static String session_name;
	static String date;
	static String time;
	static TextView first_date;
	static TextView last_date;
	static CheckBox x_axis;
	static CheckBox y_axis;
	static CheckBox z_axis;
	static SeekBar et_upsampl;

	static boolean pref_cbX;
	static boolean pref_cbY;
	static boolean pref_cbZ;

	static int pref_upsampl;
	static int seekbar_value;
	static int num_axes;



	static Intent intent;
	static byte[] x, y , z;
	static int size;

	//all file parameters
	private byte bits_per_sample = 8; // 8, 16...
	private byte num_channels = 1; // 1 = mono, 2 = stereo
	private long sample_rate = 8000; // 8000, 44100...
	
	//database
	private DBOpenHelper oh;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
			.add(R.id.container, new PlaceholderFragment()).commit();
		}

		SharedPreferences preferences = this.getSharedPreferences("prova", MODE_PRIVATE); 
		pref_cbX = preferences.getBoolean("cBoxSelectX", true);
		pref_cbY = preferences.getBoolean("cBoxSelectY", true);
		pref_cbZ = preferences.getBoolean("cBoxSelectZ", true);
		pref_upsampl = preferences.getInt("sbUpsampling", 100);



		intent = getIntent();
		x = intent.getByteArrayExtra(RecordService.X_VALUE);
		y = intent.getByteArrayExtra(RecordService.Y_VALUE);
		z = intent.getByteArrayExtra(RecordService.Z_VALUE);
		size = intent.getIntExtra(RecordService.SIZE, 0);
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
			imageView = (ImageView) rootView.findViewById(R.id.imageView);
			first_date = (TextView)rootView.findViewById(R.id.first_date);
			last_date = (TextView)rootView.findViewById(R.id.last_date);
			x_axis = (CheckBox)rootView.findViewById(R.id.x);
			y_axis = (CheckBox)rootView.findViewById(R.id.y);
			z_axis = (CheckBox)rootView.findViewById(R.id.z);

			final Calendar c = Calendar.getInstance();
			int yy = c.get(Calendar.YEAR);
			int mm = c.get(Calendar.MONTH);
			int dd = c.get(Calendar.DAY_OF_MONTH);
			int hh = c.get(Calendar.HOUR_OF_DAY);
			int mn = c.get(Calendar.MINUTE);
//			int ss = c.get(Calendar.SECOND);
			String minutes = "";
			
			if(mn < 10){
				
				minutes = "0" + mn; 
				
			} else{
				
				minutes = "" + mn;
				
			}
			
			date = dd + "/" + (mm + 1) + "/" + yy;
			time = hh + ":" + minutes;

			first_date.setText(date + " " + time);
			last_date.setText(date + " " + time);

			session_image = createImage();
			imageView.setImageBitmap(session_image);

			x_axis.setChecked(pref_cbX);
			y_axis.setChecked(pref_cbY);
			z_axis.setChecked(pref_cbZ);
			
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
			et_upsampl.setProgress(pref_upsampl);
			tvProgress.setText(String.valueOf(pref_upsampl));
			seekbar_value = pref_upsampl;

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

			session_name = name.getText().toString();
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

			num_axes = 0;
			if(x_axis.isChecked())
				num_axes++;
			if(y_axis.isChecked())
				num_axes++;
			if(z_axis.isChecked())
				num_axes++;
			//Check if at least one axis is selected
			if(num_axes == 0){
				Toast.makeText(this,"Devi selezionare almeno un asse!", Toast.LENGTH_LONG).show();
				return false;
			}


			boolean isSaved = saveImage();
			boolean isCreated = createWavFile(x, y, z, size);
			
			if(isSaved && isCreated){
				Intent createIntent = new Intent(this, PlayActivity.class);
				createIntent.putExtra("session_name", session_name);
				
				oh = new DBOpenHelper(this);
				SQLiteDatabase db = oh.getWritableDatabase();
				ContentValues values = new ContentValues();
				values.put(DBOpenHelper.NAME, session_name);
				values.put(DBOpenHelper.FIRST_DATE, date);
				values.put(DBOpenHelper.FIRST_TIME, time);
				values.put(DBOpenHelper.LAST_MODIFY_DATE, date);
				values.put(DBOpenHelper.LAST_MODIFY_TIME, time);
				values.put(DBOpenHelper.RATE, -1);
				values.put(DBOpenHelper.UPSAMPL, et_upsampl.getProgress() +1 );
				values.put(DBOpenHelper.X_CHECK, x_axis.isChecked());
				values.put(DBOpenHelper.Y_CHECK, y_axis.isChecked());
				values.put(DBOpenHelper.Z_CHECK, z_axis.isChecked());
				db.insert(DBOpenHelper.TABLE, null, values);
				//TODO verificare che la riga sia presente effettivamente nel database
				
				startActivity(createIntent);			
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

	private boolean saveImage(){
		FileOutputStream out;
		String file = session_name + ".png"; //name of the .png file
		try {
			out = openFileOutput(file, MODE_PRIVATE);
			session_image.compress(Bitmap.CompressFormat.PNG, 90, out);
//			Toast toast=Toast.makeText(this,"Immagine creta",Toast.LENGTH_LONG);
//			toast.show();
			out.close();
			return true;
		} 
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	//https://ccrma.stanford.edu/courses/422/projects/WaveFormat/
	//write the .wav file
	private boolean createWavFile(byte[] x, byte[] y, byte[] z, int size ){

		try {

			String file = session_name + ".wav"; //name of the .wav file

			//create the data to add
			final int UPSAMPLING = seekbar_value;
			byte[] dataAdded = new byte[size * UPSAMPLING];

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

	private static Bitmap createImage() {
		//creo l'immagine
		Bitmap bmp = Bitmap.createBitmap(10, 10, Bitmap.Config.RGB_565); // this creates a MUTABLE bitmap
		long value = System.currentTimeMillis();

		//le do i valori
		int color = (int) value;
		int[] indexes = new int[4];
		for(int i = 0; i < indexes.length; i++)
			indexes[i] = (int)((value >> 2 * i) & 0xff) % (bmp.getWidth() / 2);

		for(int i = 0; i < indexes.length; i++){
			for(int j = i; j < indexes.length; j++){
				bmp.setPixel(indexes[i], indexes[j], color);
				bmp.setPixel(indexes[i], bmp.getHeight() - indexes[j] - 1, color);
				bmp.setPixel(bmp.getWidth() - indexes[i] -1, indexes[j], color);
				bmp.setPixel(bmp.getWidth() - indexes[i] -1, bmp.getHeight() - indexes[j] - 1, color);
			}
		}
		return bmp;
	}

}
