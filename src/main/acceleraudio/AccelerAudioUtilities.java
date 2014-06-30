package main.acceleraudio;

import java.util.Calendar;



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

}
