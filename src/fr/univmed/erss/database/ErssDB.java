package fr.univmed.erss.database;

import fr.univmed.erss.database.table.FluxTable;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.util.Log;

/**
 * Classe qui gere la base de donnee generale de l'application eRSS
 * @author pilou
 *
 */
public class ErssDB {

	private static final int DB_VERSION = 1;
	private static final String DB_NAME = "erss.db";
	
	private SQLiteDatabase db;
	private SQLiteDb mdb;
	
	public ErssDB( Context context ) {
		mdb = new SQLiteDb(context, DB_NAME, null, DB_VERSION);
	}
	
	public void Open () {
		db = mdb.getWritableDatabase();
	}
	
	public void Close () {
		mdb.close();
	}

	public SQLiteDatabase getDatabase() {
		return db;
	}	
	
	public class SQLiteDb extends SQLiteOpenHelper {
		
		private static final String TAG = "BddSQLite";

		public SQLiteDb(Context context, String name, CursorFactory factory,
				int version) {
			super(context, name, factory, version);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(FluxTable.CREATE_TABLE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	        Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
	                + newVersion + ", which will destroy all old data");
			
	        // Delete and recreate .
	        db.execSQL("DROP TABLE IF EXISTS "+ FluxTable.TABLE_NAME);
	        onCreate(db);
		}

	}
}
