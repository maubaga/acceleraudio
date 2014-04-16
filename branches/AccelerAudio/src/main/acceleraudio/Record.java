/**
 * @author Mauro Bagatella
 * A simple object that contain 3 byte arrays
 */
package main.acceleraudio;

import interfaces.acceleraudio.IRecord;;

public class Record implements IRecord{
	private byte[] recordX;
	private byte[] recordY;
	private byte[] recordZ;
	private int recordCount;

	/**
	 * Class constructor
	 */
	public Record(){
		recordX = new byte[1024];
		recordY = new byte[1024];
		recordZ = new byte[1024];
		recordCount = 0;
	}

	/**
	 * A method that add three values, one for each arrays
	 * @param x the value to add at the first array
	 * @param y the value to add at the second array
	 * @param z the value to add at the third array
	 */
	public void add(byte x, byte y, byte z){
		try{
			recordX[recordCount] = x;
			recordY[recordCount] = y;
			recordZ[recordCount] = z;
		}
		catch (ArrayIndexOutOfBoundsException e){
			//The three arrays are too short, I need to resize them
			byte[] recordXtmp = new byte[recordX.length * 2];
			byte[] recordYtmp = new byte[recordX.length * 2];
			byte[] recordZtmp = new byte[recordX.length * 2];

			//Now i copy the old values in the new arrays
			for (int i = 0; i < recordX.length; i++){
				recordXtmp[i] = recordX[i];
				recordYtmp[i] = recordY[i];
				recordZtmp[i] = recordZ[i];
			}
			recordX = recordXtmp;
			recordY = recordYtmp;
			recordZ = recordZtmp;

			//Now I have add the last value
			recordX[recordCount] = x;
			recordY[recordCount] = y;
			recordZ[recordCount] = z;
		}
		recordCount++;
	}
	/**
	 * A method that return the number of values for each arrays, the total number of values is getSize() * 3
	 * @return An int that represent the number of values in an array
	 */
	public int getSize(){
		return recordCount;
	}

	/**
	 * Return the first array
	 * @return the first array 
	 */
	public byte[] getXarray(){
		return recordX;
	}

	/**
	 * Return the second array
	 * @return the second array
	 */
	public byte[] getYarray(){
		return recordY;
	}

	/**
	 * Return the third array
	 * @return the third array
	 */
	public byte[] getZarray(){
		return recordZ;
	}

	/**
	 * This method empties the arrays
	 */
	public void reset(){
		recordCount = 0;
	}

	/**
	 * Return a string form of the three array. For example:
	 * x= 2   y= -3  z= 17
	 * x= -4  y= 22  z= 41
	 * . . . 
	 */
	public String toString(){
		String result = "";
		for(int j = 0; j < recordCount; j++)
			result = result +"x= " + recordX[j] + "    y= " + recordY[j] + "    z= " + recordZ[j] +"\n";
		return result;
	}
	
}
