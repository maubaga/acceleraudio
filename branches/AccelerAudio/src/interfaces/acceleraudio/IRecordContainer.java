package interfaces.acceleraudio;

public interface IRecordContainer {
	/**
	 * A method that add three values, one for each arrays
	 * @param x the value to add at the first array
	 * @param y the value to add at the second array
	 * @param z the value to add at the third array
	 */
	public void add(byte x, byte y, byte z);
	
	/**
	 * A method that return the number of values for each arrays, the total number of values is getSize() * 3
	 * @return An int that represent the number of values in an array
	 */
	public int getSize();
	
	/**
	 * Return the first array
	 * @return the first array 
	 */
	public byte[] getXarray();

	/**
	 * Return the second array
	 * @return the second array
	 */
	public byte[] getYarray();

	/**
	 * Return the third array
	 * @return the third array
	 */
	public byte[] getZarray();

	/**
	 * This method empties the arrays
	 */
	public void reset();

	/**
	 * Return a string form of the three array. For example:
	 * x= 2   y= -3  z= 17
	 * x= -4  y= 22  z= 41
	 * . . . 
	 */
	public String toString();
}
