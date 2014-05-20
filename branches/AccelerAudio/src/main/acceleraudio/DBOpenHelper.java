package main.acceleraudio;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class DBOpenHelper extends SQLiteOpenHelper {
	
	private static final String DATABASE_NAME = "AccAudio.db";
	private static final int DATABASE_VERSION = 1;
	public static final String TABLE = "registrazione";
	public static final String NAME = "nome file";
	public static final String DATE = "data";
	public static final String TIME = "ora";
	public static final String MODIFY = "ultima modifica";
	public static final String RATE = "bit-rate";
	public static final String UPSAMPL = "interpolazione";

	public DBOpenHelper(Context context){
		super(context, DATABASE_NAME, null, DATABASE_VERSION); 
	} 

	@Override 
	public void onCreate(SQLiteDatabase db) 
	{ 
		String sql = "create table " + TABLE + 
					"( "+ BaseColumns._ID + " integer primary key autoincrement, " +
					NAME + " text not null, " + 
					DATE + " text not null, " +
					TIME + " text not null, " +
					MODIFY + " text not null, " +
					RATE + " text not null, " +
					UPSAMPL + " integer);"; 
		db.execSQL(sql); 
	} 

	@Override 
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}
}
