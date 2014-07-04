package interfaces.acceleraudio;

import android.content.Context;

public interface ISongCreator {

	/**
	 * Set the rate, it must be 0 < rate <= 100.
	 * @param rate The rate to set.
	 */
	public void setRate(int rate);
	
	/**
	 * Set the upsample, it must be 0 < upsample <= 100.
	 * @param upsample The upsample to set.
	 */
	public void setUpsample(int upsample);
	
	/**
	 * Set the axes to use for the song creation.
	 * @param x Set true if want to use x axe, false otherwise.
	 * @param y Set true if want to use y axe, false otherwise.
	 * @param z Set true if want to use z axe, false otherwise.
	 */
	public void setAxes(boolean x, boolean y, boolean z);
	
	/**
	 * This method creates a new file songName.wav with the array specified in the Constructor.
	 * @param context The Context where this method is called.
	 * @param songName The name of the song (without .wav)
	 * @return true if the creation is successful, false otherwise.
	 */
	public boolean createWavFile(Context context, String songName);

	/**
	 * Create the song and the image. 
	 * It returns true if the creation is successful, 
	 * false if the session name is null or disk space is insufficient to save the song or if an error in the creation is occurred.
	 * @param context The Context where this method is called.
	 * @param session_name The name of the song, it mustn't be null.
	 * @return true if the creation is successful, false otherwise.
	 */
	public boolean createNewSession(Context context, String session_name);

	/**
	 * Modify an existing session, update the last modify date and time in the database and rename the image if is necessary.
	 * @param context The Context where this method is called.
	 * @param session_name The new name for the session to modify.
	 * @param oldSessionName The old name for the session before the modify.
	 * @return true if the modification is successful, false otherwise.
	 */
	public boolean modifySession(Context context, String session_name, String old_session_name);
}
