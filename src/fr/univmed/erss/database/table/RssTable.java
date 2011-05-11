package fr.univmed.erss.database.table;

import fr.univmed.erss.object.RSS;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class RssTable {

	public static final String TABLE_NAME = "fluxRSS";
	
	public static final String ID = "id";
	public static final String URL = "url";
	public static final String CHECKED = "checked";
	
	public static final String CREATE_TABLE = 
		"CREATE TABLE " + TABLE_NAME + " ("
		+ ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
		+ URL + " TEXT NOT NULL, "
		+ CHECKED + " TEXT NOT NULL"
		+");";
	
	public RssTable () {
		
	}
	
	public static long insert ( SQLiteDatabase db, RSS elm ) {
		
		ContentValues values = new ContentValues();
		
		values.put(URL, elm.getUrl());
		values.put(CHECKED, elm.isChecked());
		
		return db.insert(TABLE_NAME, null, values);
	}
	
	// update
	public static int update(SQLiteDatabase db, RSS elm) {
		ContentValues values = new ContentValues();
		
		values.put(URL, elm.getUrl());
		values.put(CHECKED, elm.isChecked());
		
		return db.update(TABLE_NAME, values, ID+"="+elm.getId(), null);
	}
	
	// delete
	public static int delete(SQLiteDatabase db, long id) {

		return db.delete(TABLE_NAME, ID+"="+id, null);
	}
	
	// Convert from cursor 
	public static RSS fromCursor ( Cursor c ) {
		
		RSS elm = new RSS();
		
		elm.setId(c.getLong(c.getColumnIndex(ID)));
		elm.setUrl(c.getString(c.getColumnIndex(URL)));
		elm.setChecked(c.getString(c.getColumnIndex(CHECKED)));
				
		return elm;
	}
}