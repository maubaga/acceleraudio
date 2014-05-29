package main.acceleraudio;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBOpenHelper extends SQLiteOpenHelper {
	
	private static final String DATABASE_NAME = "AccAudio.db";
	private static final int DATABASE_VERSION = 1;
	public static final String TABLE = "registrazione";
	public static final String NAME = "nome_file";
	public static final String FIRST_DATE = "data";
	public static final String FIRST_TIME = "ora";
	public static final String LAST_MODIFY_DATE = "ultima_modifica_data";
	public static final String LAST_MODIFY_TIME = "ultima_modifica_ora";
	public static final String RATE = "bit_rate";
	public static final String UPSAMPL = "interpolazione";
	public static final String X_CHECK = "asse_x";
	public static final String Y_CHECK = "asse_y";
	public static final String Z_CHECK = "asse_z";
	public static final String X_VALUES = "x_values";
	public static final String Y_VALUES = "y_values";
	public static final String Z_VALUES = "z_values";	
	public static final String DATA_SIZE = "data_size";

	public DBOpenHelper(Context context){
		super(context, DATABASE_NAME, null, DATABASE_VERSION); 
	} 

	@Override 
	public void onCreate(SQLiteDatabase db) 
	{ 
		String sql = "create table " + TABLE + 
					"( "+ NAME + " text primary key, " + 
					FIRST_DATE + " text not null, " +
					FIRST_TIME + " text not null, " +//vedere formato su doc SQLite
					LAST_MODIFY_DATE + " text not null, " +
					LAST_MODIFY_TIME + " text not null, " +
					RATE + " integer, " +
					UPSAMPL + " integer, " +
					X_CHECK + " boolean, " +
					Y_CHECK + " boolean, " +
					Z_CHECK + " boolean, " +
					X_VALUES + " blob, " +
					Y_VALUES + " blob, " +
					Z_VALUES + " blob, " +
					DATA_SIZE + " integer);"; 
		db.execSQL(sql); 
	} 

	@Override 
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
		db.execSQL("DROP TABLE IF EXISTS " + TABLE);
		onCreate(db);
		
	}
	
}
