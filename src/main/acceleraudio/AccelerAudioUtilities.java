package main.acceleraudio;

import java.io.FileOutputStream;
import java.util.Calendar;

import android.content.Context;
import android.graphics.Bitmap;



public class AccelerAudioUtilities{
	/**
	 * This method convert date from the format yyyy-mm-dd to the format dd/mm/yyyy
	 * @param inputDate A String that represents the date in format yyyy-mm-dd
	 * @return  A String that represents the date in format dd/mm/yyyy
	 */
	public static String dateConverter(String inputDate){
		String yy = inputDate.substring(0, 4);
		String mm = inputDate.substring(5, 7);
		String dd = inputDate.substring(8, 10);
		String outputDate = dd + "/" + mm + "/" + yy;
		return outputDate;
	}
	
	/**
	 * This method return a String that represents current date in format yyyy-mm-dd
	 * @return  A String that represents the date in format yyyy-mm-dd
	 */
	public static String getCurrentDate(){
		
		final Calendar c = Calendar.getInstance();
		int yy = c.get(Calendar.YEAR);
		int mm = c.get(Calendar.MONTH);
		int dd = c.get(Calendar.DAY_OF_MONTH);

		String months = "";
		String days = "";
		String date = "";

		if(dd < 10)
			days = "0" + dd; 
		else
			days = "" + dd;
		if(mm < 10)
			months = "0" + (mm + 1); 
		else
			months = "" + (mm + 1);


		date = yy + "-" + months + "-" + days;
		
		return date;
	}
	
	/**
	 * This method return a String that represents current time in format hh:mm
	 * @return  A String that represents the time in format hh:mm
	 */
	public static String getCurrentTime(){
		final Calendar c = Calendar.getInstance();
		int hh = c.get(Calendar.HOUR_OF_DAY);
		int min = c.get(Calendar.MINUTE);

		String minutes = "";
		String hours = "";
		String time = "";

		if(min < 10)
			minutes = "0" + min; 
		else
			minutes = "" + min;
		
		if(hh < 10)
			hours = "0" + hh; 
		else
			hours = "" + hh;
		
		time = hours + ":" + minutes;
		
		return time;
	}
	
	/**
	 * Create a symmetrical 10*10 Bitmap in RGB_565 format from the current time.
	 * @return The Bitmap create.
	 */
	public static Bitmap createImage() {
		// Create a Bitmap
		Bitmap bmp = Bitmap.createBitmap(10, 10, Bitmap.Config.RGB_565); // this creates a MUTABLE bitmap
		long value = System.currentTimeMillis();

		// Color the Bitmap
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
	
	/**
	 * Save the Bitmap session_image with the name imageName.png, return true if the image is created, false otherwise.
	 * @param context The Context where the image is create.
	 * @param imageName The name of the image to save (without .png)
	 * @param session_image The Bitmap to save
	 * @return true if the image is created, false otherwise
	 */
	public static boolean saveImage(Context context, String imageName, Bitmap session_image){
		FileOutputStream out;
		String file = imageName + ".png"; //name of the .png file
		try {
			out = context.openFileOutput(file, Context.MODE_PRIVATE);
			session_image.compress(Bitmap.CompressFormat.PNG, 90, out);
			out.close();
			return true;
		} 
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

}
