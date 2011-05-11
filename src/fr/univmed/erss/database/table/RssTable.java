package fr.univmed.erss.database.table;

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
	
	
	
}
