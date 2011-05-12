package fr.univmed.erss.database.table;

import fr.univmed.erss.object.Flux;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class FluxTable {

	public static final String TABLE_NAME = "sourceflux";
	
	public static final String NAME = "name";
	public static final String ID = "id";
	public static final String URL = "url";
	public static final String CHECKED = "checked";
	
	public static final String CREATE_TABLE = 
		"CREATE TABLE " + TABLE_NAME + " ("
		+ ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
		+ NAME + " TEXT NOT NULL, "
		+ URL + " TEXT NOT NULL, "
		+ CHECKED + " TEXT NOT NULL"
		+");";
	
	public FluxTable () {
		
	}
	
	public static long insert ( SQLiteDatabase db, Flux elm ) {
		
		ContentValues values = new ContentValues();
		
		values.put(NAME, elm.getName());
		values.put(URL, elm.getUrl());
		values.put(CHECKED, elm.isChecked());
		
		return db.insert(TABLE_NAME, null, values);
	}
	
	// update
	public static int update(SQLiteDatabase db, Flux elm) {
		ContentValues values = new ContentValues();
		
		values.put(NAME, elm.getName());
		values.put(URL, elm.getUrl());
		values.put(CHECKED, elm.isChecked());
		
		return db.update(TABLE_NAME, values, ID+"="+elm.getId(), null);
	}
	
	// delete
	public static int delete(SQLiteDatabase db, long id) {

		return db.delete(TABLE_NAME, ID+"="+id, null);
	}
	
	// Convert from cursor 
	public static Flux fromCursor ( Cursor c ) {
		
		Flux elm = new Flux();
		
		elm.setId(c.getLong(c.getColumnIndex(ID)));
		elm.setName(c.getString(c.getColumnIndex(NAME)));
		elm.setUrl(c.getString(c.getColumnIndex(URL)));
		elm.setChecked(c.getInt(c.getColumnIndex(CHECKED)));
				
		return elm;
	}
}
