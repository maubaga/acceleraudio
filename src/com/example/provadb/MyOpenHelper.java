package com.example.provadb;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MyOpenHelper extends SQLiteOpenHelper 
{ 
	private static final String DATABASE_NAME = "mydb.db"; 
	private static final int DATABASE_VERSION = 1; 
	public static final String TABLE = "studenti";
	public static final String MATR = "matricola";
	public static final String NAME = "nome";  

	public MyOpenHelper(Context context) 
	{ 
		super(context, DATABASE_NAME, null, DATABASE_VERSION); 
	} 

	@Override 
	public void onCreate(SQLiteDatabase db) 
	{ 
		String sql = "create table " + TABLE + "( " + MATR 
				+ " integer primary key, " + NAME + " text not null);"; 
		db.execSQL(sql); 
	} 

	@Override 
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
	{ 
		String sql = null; 
		if (oldVersion == 1)
			sql = "alter table " + TABLE + " add (" + MATR + " integer, " + NAME + "text not null);"; 

		if (sql != null)
			db.execSQL(sql); 
	} 
}
