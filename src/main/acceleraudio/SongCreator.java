package main.acceleraudio;

import static main.acceleraudio.DBOpenHelper.NAME;
import static main.acceleraudio.DBOpenHelper.TABLE;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.os.StatFs;
import android.widget.Toast;

public class SongCreator {
	//all file parameters
	public static final byte BITS_PER_SAMPLE = 8; // 8, 16...
	public static final byte NUM_OF_CHANNEL = 1; // 1 = mono, 2 = stereo
	public static final int FREQUENCY = 8000; // 8000, 44100...

	private byte[] x, y, z;
	private int size;

	private boolean useX;
	private boolean useY;
	private boolean useZ;
	private int rate; 
	private int upsampl;

	/**
	 * The Class constructor. Uses all three axes and sets the values rate and upsample to the maximum (100).
	 * The arrays mustn't be null.
	 * @param x The x array.
	 * @param y The y array.
	 * @param z The z array.
	 * @param size The numer of element in the arrays.
	 */
	public SongCreator(byte[] x, byte[] y, byte[] z, int size){
		if(x == null || y == null || z == null)
			throw new IllegalArgumentException("The arrays mustn't be null!");
		this.x = x;
		this.y = y;
		this.z = z;
		this.size = size;

		setRate(100);
		setUpsample(100);

		setAxes(true, true, true);
	}

	/**
	 * Set the rate, it must be 0 < rate <= 100.
	 * @param rate The rate to set.
	 */
	public void setRate(int rate){
		if (rate < 1 || rate > 100)
			throw new IllegalArgumentException("The rate must be  0 < rate <= 100.");
		this.rate = rate;
	}

	/**
	 * Set the upsamble, it must be 0 < upsample <= 100.
	 * @param upsample The upsample to set.
	 */
	public void setUpsample(int upsample){
		if (upsample < 1 || upsample > 100)
			throw new IllegalArgumentException("The rate must be  0 < upsample <= 100.");
		this.upsampl = upsample;
	}

	/**
	 * Set the axes to use for the creation of the song.
	 * @param x Set true if want to use x axe, false otherwise.
	 * @param y Set true if want to use y axe, false otherwise.
	 * @param z Set true if want to use z axe, false otherwise.
	 */
	public void setAxes(boolean x, boolean y, boolean z){
		useX = x;
		useY = y;
		useZ = z;
	}

	/**
	 * This method create a new file songName.wav with the array specified in the Constructor.
	 * @param context The Context where this method is called.
	 * @param songName The name of the song (without .wav)
	 * @return true if the creation is successful, false otherwise.
	 */
	public boolean createWavFile(Context context, String songName){
		try {
			String file = songName + ".wav"; //name of the .wav file

			final int UPSAMPLING = upsampl;
			byte[] dataAdded = new byte[size * UPSAMPLING];

			int num_axes = 0;
			if(useX)
				num_axes++;
			if(useY)
				num_axes++;
			if(useZ)
				num_axes++;

			// Create the data to add at the song (merge the arrays)
			for(int i = 0; i < size; i++){
				int sum_axes = 0;

				if(useX)
					sum_axes += x[i];
				if(useY)
					sum_axes += y[i];
				if(useZ)
					sum_axes += z[i];

				for(int j = i * UPSAMPLING; j < (i + 1) * UPSAMPLING; j++)
					dataAdded[j] = (byte)(sum_axes / num_axes);
			}	

			FileOutputStream fOut = context.openFileOutput(file, Context.MODE_PRIVATE);

			long totalAudioLen = dataAdded.length * NUM_OF_CHANNEL * (BITS_PER_SAMPLE / 8);
			long chunkSize = 36 + (dataAdded.length * NUM_OF_CHANNEL * (BITS_PER_SAMPLE / 8));
			long byteRate = FREQUENCY * NUM_OF_CHANNEL * (BITS_PER_SAMPLE / 8);

			WriteWaveFileHeader(fOut, totalAudioLen, 
					chunkSize, 
					FREQUENCY, NUM_OF_CHANNEL, 
					byteRate);


			AccelerAudioUtilities.averageArray(dataAdded); // Loop this method for more average!

			fOut.write(dataAdded);
			fOut.close();

			return true;

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * This method create the song and the image. 
	 * It return true if the creation is successful, 
	 * false if the session name is null or disk space is insufficient to save the song or an error in the create is occurred.
	 * @param context The Context where this method is called.
	 * @param session_name The name of the song, it mustn't be null.
	 * @return true if the creation is successful, false otherwise.
	 */
	public boolean createNewSession(Context context, String session_name){
		if(session_name == null || session_name.equals(""))
			return false;
	
		long byteRequest = 200 + 44 + upsampl * size; // 200: max image dimension, 44: bytes wav headers, size * upsample: song dimension.
		// Check if the space in the "disk" is sufficient to save the song and the image.
		if (!checkSpace(byteRequest)){
			Toast.makeText(context,"Memoria insufficiente per salvare la traccia.", Toast.LENGTH_LONG).show();
			return false;
		}

		// Create and save the image.
		boolean isSaved = AccelerAudioUtilities.saveImage(context, session_name, AccelerAudioUtilities.createImage());
		if(!isSaved){ // Check if the image is create and save.
			Toast.makeText(context,"Errore nel salvataggio dell'immagine.", Toast.LENGTH_LONG).show();
			return false;
		}

		// Create the song.
		boolean isCreated = createWavFile(context, session_name);

		if(!isCreated){ // Check if the song is created.
			Toast.makeText(context,"Errore nella creazione della traccia.", Toast.LENGTH_LONG).show();
			return false;
		}

		int duration = size * upsampl * 1000 / FREQUENCY; // Duration of the sound in milliSeconds.

		// Get data and time.
		String date = AccelerAudioUtilities.getCurrentDate();
		String time = AccelerAudioUtilities.getCurrentTime();

		// Add to the database.
		DBOpenHelper openHelper = new DBOpenHelper(context);
		SQLiteDatabase db = openHelper.getWritableDatabase();
		db.delete(TABLE, NAME + "='" + session_name + "'", null);
		ContentValues values = new ContentValues();
		values.put(DBOpenHelper.NAME, session_name);
		values.put(DBOpenHelper.FIRST_DATE, date);
		values.put(DBOpenHelper.FIRST_TIME, time);
		values.put(DBOpenHelper.LAST_MODIFY_DATE, date);
		values.put(DBOpenHelper.LAST_MODIFY_TIME, time);
		values.put(DBOpenHelper.DURATION, duration);   
		values.put(DBOpenHelper.RATE, rate);       
		values.put(DBOpenHelper.UPSAMPL, upsampl);            
		values.put(DBOpenHelper.X_CHECK, useX);
		values.put(DBOpenHelper.Y_CHECK, useY);
		values.put(DBOpenHelper.Z_CHECK, useZ);
		values.put(DBOpenHelper.X_VALUES, x);                // Add the three byte array to the database.
		values.put(DBOpenHelper.Y_VALUES, y);
		values.put(DBOpenHelper.Z_VALUES, z);
		values.put(DBOpenHelper.DATA_SIZE, size);            // Add the number samples to the database.
		long id = db.insert(DBOpenHelper.TABLE, null, values);

		if(id < 0)
			return false;

		return true;
	}

	/**
	 * This method modify an existing session, update the last modify date and time in the database and rename the image if is necessary.
	 * This method retun false if session_name or old_session_name are null or the space is insufficient or an error is occurred.
	 * @param context The Context where this method is called.
	 * @param session_name The new name for the session to modify.
	 * @param old_session_name The old name for the session before the modify.
	 * @return true if the modification is successful, false otherwise.
	 */
	public boolean modifySession(Context context, String session_name, String old_session_name){
		if(session_name == null || session_name.equals("") || old_session_name == null || old_session_name.equals(""))
			return false;
		
		long byteRequest = 44 + upsampl * size; // 44: bytes wav headers, size * upsample: song dimension.
		if (!checkSpace(byteRequest)){      // Check if the space in the "disk" is sufficient to save the song and the image.
			Toast.makeText(context,"Memoria insufficiente per modificare la traccia.", Toast.LENGTH_LONG).show();
			return false;
		}

		// Create the song.
		boolean isCreated = createWavFile(context, session_name);

		if(!isCreated){ // Check if the song is created.
			Toast.makeText(context, "Errore di creazione file", Toast.LENGTH_LONG).show();
			return false;
		}

		int duration = size * upsampl * 1000 / FREQUENCY; // Duration of the sound in milliSeconds.

		DBOpenHelper openHelper = new DBOpenHelper(context);
		SQLiteDatabase db = openHelper.getWritableDatabase();

		// Get data and time.
		String date = AccelerAudioUtilities.getCurrentDate();
		String time = AccelerAudioUtilities.getCurrentTime();

		// Update the database.
		ContentValues values = new ContentValues();
		values.put(DBOpenHelper.NAME, session_name);
		values.put(DBOpenHelper.LAST_MODIFY_DATE, date);
		values.put(DBOpenHelper.LAST_MODIFY_TIME, time);
		values.put(DBOpenHelper.DURATION, duration); 
		values.put(DBOpenHelper.UPSAMPL, upsampl);            
		values.put(DBOpenHelper.X_CHECK, useX);
		values.put(DBOpenHelper.Y_CHECK, useY);
		values.put(DBOpenHelper.Z_CHECK, useZ);
		db.update(DBOpenHelper.TABLE, values, NAME +"= '" + old_session_name + "'",null);

		// Delete the previous song and rename the image if the name is change.
		if(!old_session_name.equals(session_name)){
			File dir = context.getFilesDir();
			File image = new File(dir, old_session_name + ".png");
			File audio = new File(dir, old_session_name + ".wav");
			image.renameTo(new File(dir, session_name + ".png"));
			audio.delete();
		}

		return true;
	}

	/**
	 * This method create the header of the wav file as described by https://ccrma.stanford.edu/courses/422/projects/WaveFormat/
	 * @param out FileOuputStream where the header will write.
	 * @param totalAudioLen Subchunk2Size in the link.
	 * @param totalDataLen ChunkSize in the link.
	 * @param longSampleRate The Frequency.
	 * @param channels Number of Channel.
	 * @param byteRate The ByteRate.
	 * @throws IOException
	 */
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
		headerBuffer[34] = BITS_PER_SAMPLE;  // bits per sample
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

	/**
	 * This method get the free memory space in the device in byte and check if byteRequest < freeByte. Return true if the space is sufficient,
	 * false otherwise.
	 * @param byteRequest The number of byte request from the memory.
	 * @return true if byteRequest < freeByte, false otherwise.
	 */
	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	private boolean checkSpace(long byteRequest){
		// Get free space.
		File path = Environment.getDataDirectory();
		StatFs stat = new StatFs(path.getPath());
		long blockSize; 
		long availableBlocks; 
		int currentapiVersion = android.os.Build.VERSION.SDK_INT;
		if (currentapiVersion >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2){
			blockSize = stat.getBlockSizeLong(); // getBlockSizeLong() need API level 18
			availableBlocks= stat.getAvailableBlocksLong(); // getAvailableBlocksLong() need API level 18

		} else{
			blockSize = stat.getBlockSize(); // getBlockSizeLong() need API level 18
			availableBlocks= stat.getAvailableBlocks(); // getAvailableBlocksLong() need API level 18
		}
		long freeByte = blockSize * availableBlocks;		

		if (byteRequest > freeByte)
			return false;
		else 
			return true;
	}
}
