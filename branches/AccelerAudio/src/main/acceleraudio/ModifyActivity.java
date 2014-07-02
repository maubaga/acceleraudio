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
import java.io.IOException;

import android.content.Intent;
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


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_modify);

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

	@Override
	protected void onResume() {
		super.onResume();
		boolean isPlaying = AccelerAudioUtilities.isMyServiceRunning(this, PlayerService.class);
		if (isPlaying){ // Check if is in play something.
			// Stop the song in background.
			Intent stopIntent = new Intent(getApplicationContext(), PlayerService.class); 
			stopService(stopIntent);
		}
	}

	@Override
	public void onBackPressed() {
		stopPreview(null);
		finish(); 
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
			View rootView = inflater.inflate(R.layout.fragment_modify,
					container, false);

			nameEditText = (EditText)rootView.findViewById(R.id.name);
			imageView = (ImageView) rootView.findViewById(R.id.imageView);
			first_date = (TextView)rootView.findViewById(R.id.first_date);
			last_date = (TextView)rootView.findViewById(R.id.last_date);
			x_axis = (CheckBox)rootView.findViewById(R.id.x);
			y_axis = (CheckBox)rootView.findViewById(R.id.y);
			z_axis = (CheckBox)rootView.findViewById(R.id.z);


			nameEditText.setText(oldSessionName);

			date = AccelerAudioUtilities.getCurrentDate();
			time = AccelerAudioUtilities.getCurrentTime();

			first_date.setText(AccelerAudioUtilities.dateConverter(firstData) + " " + firstTime);
			last_date.setText(AccelerAudioUtilities.dateConverter(date) + " " + time);

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

			stopPreview(null); //if the preview still playing I stop it

			File fileCheck = new File(getApplicationContext().getFilesDir().getPath() + "/" + session_name + ".wav");
			if(fileCheck.exists() && !session_name.equals(oldSessionName)){

				Toast.makeText(this, session_name + " esiste già!", Toast.LENGTH_SHORT).show();
				return false;

			}

			SongCreator songCreator = new SongCreator(x, y, z, size);
			songCreator.setUpsample(seekbar_value);
			songCreator.setAxes( x_axis.isChecked(),  y_axis.isChecked(),  z_axis.isChecked());
			boolean isModify = songCreator.modifySession(this, session_name, oldSessionName);

			if(isModify){
				WidgetIntentReceiver.updateWidgetOnStop(this); // Update the widget with the last song.

				int duration = size * seekbar_value * 1000 / SongCreator.FREQUENCY; // Compute the duration of the song.
				// Start PlayActivity
				Intent playIntent = new Intent(this, PlayActivity.class);
				playIntent.putExtra("session_name", session_name);
				playIntent.putExtra(PlayActivity.AUTOPLAY, false); // The song doesn't start automatically.
				playIntent.putExtra(PlayActivity.DURATION, duration);
				startActivity(playIntent);				
			}
			return true;
		}

		if (id == R.id.action_settings) {
			Intent intent = new Intent(this, PrefActivity.class);
			startActivity(intent);
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * This method is called when the button "Anteprima" is pressed and it create a temporary wav file and play it.
	 * @param view the button pressed.
	 */
	public void startPreview(View view){

		//create temporary files
		SongCreator songCreator = new SongCreator(x, y, z, size);
		songCreator.setUpsample(seekbar_value);
		songCreator.setAxes( x_axis.isChecked(),  y_axis.isChecked(),  z_axis.isChecked());
		boolean isCreated = songCreator.createWavFile(this, "Temp"); // Create only the song, without the image and without insert the data on the database.

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


	/**
	 * Method called when the button "Stop" is pressed. Stop the preview of the song.
	 * @param view The button pressed.
	 */
	public void stopPreview(View view){
		try{
			if(mp != null){
				Button play = (Button)findViewById(R.id.start_bt);
				Button stop = (Button)findViewById(R.id.stop_bt);

				play.setVisibility(View.VISIBLE);
				stop.setVisibility(View.GONE);

				mp.stop();
				mp = null;
				
				File dir = getFilesDir();
				File temp = new File(dir, "Temp.wav"); // Delete the temoraney file.
				temp.delete();
			}
		}catch(Exception e){
			Toast.makeText(getBaseContext(),e.toString(), Toast.LENGTH_SHORT).show();
		}
	}
}
