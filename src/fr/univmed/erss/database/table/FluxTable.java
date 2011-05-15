package fr.univmed.erss.database.table;

import java.util.LinkedList;
import java.util.List;

import fr.univmed.erss.object.Flux;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Classe qui gere la table "sourceflux" dans la bdd de l'appli
 * elle contient les adresses source des flux rss que l'utilisateur
 * veut suivre ou non.
 * @author pilou
 *
 */
public class FluxTable {

	public static final String TABLE_NAME = "sourceflux";//Nom de la table
	
	public static final String NAME = "name";//Champ d'une colonne
	public static final String ID = "id";	//Champ d'une colonne
	public static final String URL = "url";	//Champ d'une colonne
	public static final String CHECKED = "checked"; //Champ d'une colonne
	
	//String de creation de la table
	public static final String CREATE_TABLE = 
		"CREATE TABLE " + TABLE_NAME + " ("
		+ ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
		+ NAME + " TEXT NOT NULL, "
		+ URL + " TEXT NOT NULL, "
		+ CHECKED + " TEXT NOT NULL"
		+");";
	
	/**
	 * Constructeur par defaut
	 */
	public FluxTable () {
		
	}
	
	/**
	 * Insere un nouveau flux dans la table
	 * @param db : Base de donnee dans laquelle on insere
	 * @param elm : Element a inserer
	 * @return
	 */
	public static long insert ( SQLiteDatabase db, Flux elm ) {
		
		ContentValues values = new ContentValues();
		//Mise en forme des elements pour insertion
		values.put(NAME, elm.getName());
		values.put(URL, elm.getUrl());
		values.put(CHECKED, elm.isChecked());
		
		return db.insert(TABLE_NAME, null, values);//insertion
	}
	
	/**
	 * Met a jour un Flux de la bdd
	 * @param db : Bdd a mettre a jour
	 * @param elm : Element a mettre a jour
	 * @return
	 */
	public static int update(SQLiteDatabase db, Flux elm) {
		ContentValues values = new ContentValues();
		//Mise en forme du flux, pour mise a jour
		values.put(NAME, elm.getName());
		values.put(URL, elm.getUrl());
		values.put(CHECKED, elm.isChecked());
		
		return db.update(TABLE_NAME, values, ID+"="+elm.getId(), null);//Mise a jour
	}
	
	/**
	 * Supprime un Flux de la bdd
	 * @param db : Bdd qui sera modifie
	 * @param id : ID du flux a supprimer
	 * @return
	 */
	public static int delete(SQLiteDatabase db, long id) {

		return db.delete(TABLE_NAME, ID+"="+id, null);//Suppression
	}
	
	/**
	 * Verifie si la table est vide dans la bdd
	 * @param db : bdd a verifier
	 * @return true : si la table et vide, false sinon
	 */
	public static boolean isEmpty (SQLiteDatabase db)
	{
		Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, null);
		while(cursor.moveToNext())
		{
			cursor.close();
			return false;
		}
		cursor.close();
		return true;
	}
	
	/**
	 * Converti un element de cursor en Flux
	 * @param c element de cursor a convertir
	 * @return element au format Flux
	 */
	public static Flux fromCursor ( Cursor c ) {
		
		Flux elm = new Flux();
		
		elm.setId(c.getLong(c.getColumnIndex(ID)));
		elm.setName(c.getString(c.getColumnIndex(NAME)));
		elm.setUrl(c.getString(c.getColumnIndex(URL)));
		elm.setChecked(c.getInt(c.getColumnIndex(CHECKED)));
				
		return elm;
	}
	
	/**
	 * Recupere la liste de tous les Flux contenu dans la table
	 * @param db : bdd a recuperer
	 * @return La list de tous les fluxs
	 */
	public static List<Flux> getFluxList (SQLiteDatabase db)
	{
		List<Flux> fluxs = new LinkedList<Flux>();

		Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, null);
		while(cursor.moveToNext())
		{
			fluxs.add(fromCursor(cursor));
		}
		cursor.close();

		return fluxs;
	}
}
