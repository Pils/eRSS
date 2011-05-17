package fr.univmed.erss;

import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import fr.univmed.erss.database.ErssDB;
import fr.univmed.erss.database.table.FluxTable;
import fr.univmed.erss.object.Flux;
import fr.univmed.erss.parser.flux.FluxHandler;

/**
 * Classe definissant l'ecran et l'activite qui gere la liste des flux a suivre
 * @author pilou
 *
 */
public class FluxActivity extends ListActivity {

	private final String LOG_TAG = "FluxActivity";//Utile pour debug
	
	private List<Flux> fluxs = new LinkedList<Flux>();;//Contient la liste des flux qui seront affiche
	private FluxAdapter fAdapter; //Contient l'adapter pour les flux
	
	//Gere la bdd
	private ErssDB erssDB;
	private SQLiteDatabase db;
	
	/**
	 * Creation de l'activity
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		Log.i(LOG_TAG, "onCreate()");

		fAdapter = new FluxAdapter();//Adapter des elements de la liste (vu qu'on utilise un tableau de Flux)
		setListAdapter(fAdapter); 		//Creera un liste de flux avec checkbox
		
		final ListView listView = getListView();
        listView.setItemsCanFocus(false); 
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);//Autoriser la selection de plusieurs elements dans la liste
        
        setTitle(R.string.flux_activity_title); // Titre de la page de l'activity
        
        /*Ouverture de la base de donnee*/
        erssDB = new ErssDB(this);
        erssDB.Open();
		db = erssDB.getDatabase();
		boolean doRefresh = FluxTable.isEmpty(db);//Verification de l'etat de la bdd
        erssDB.Close();
		if( doRefresh ) //Si elle vide
           	new ThreadParse().execute(); //On effectue une mise a jour de la bdd des fluxs
		else
			updateFluxsFromFluxTable();  //Sinon on recupere les donnee a partir de la bdd pour les afficher dans l'etat
										//tel qu'ils etaient a la derniere utilisation de l'appli.
	}
	 
	/**
	 * Mise a jour de la liste des fluxs a partir de table de flux sur la bdd existante
	 */
	private void updateFluxsFromFluxTable () 
	{
		/*Acquisition de la liste des fluxs a partir de la database*/
		this.erssDB.Open();
		db = erssDB.getDatabase();
		fluxs = FluxTable.getFluxList(db);
		this.erssDB.Close();
		
		/*Gestion de l'affichage de la checkbox pour chaque flux*/
		for(int i=0; i<fluxs.size(); i++)
		{
			if(fluxs.get(i).isChecked())
				this.getListView().setItemChecked(i, true);
			else
				this.getListView().setItemChecked(i, false);
        	//Log.i(LOG_TAG,"flux checked ?"+ fluxs.get(i).isChecked());
		}
		
	}

	/**
	 * Mise a jour de la table de flux sur la  bdd a partir du web
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	private void updateFluxTableFromWeb () throws ParserConfigurationException,
	SAXException, IOException {
		
		List<Flux> liste_fluxs = new LinkedList<Flux>();
		/*Recuperation de la liste flux : configuration du parser*/
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser xmlReader;
		xmlReader = factory.newSAXParser();
		FluxHandler handler = new FluxHandler(); //definition du parser a utiliser

		URL source = new URL(FluxHandler.URL_SOURCE);//URL parser a parser
		InputSource inputSourceUrl = new InputSource(source.toString());
		xmlReader.parse(inputSourceUrl, handler);//Parsing

		liste_fluxs = handler.getFluxs();//Recuperation des donnees parse
		
		/*Mise a jour de la base de donnee*/
		this.erssDB.Open();
		db = erssDB.getDatabase();
		//Suppression de la bdd de la liste deja existante
		db.execSQL("DELETE FROM "+ FluxTable.TABLE_NAME +";");
		//Log.i(LOG_TAG, "db.execSQL(DELETE);");
		for(Flux flux : liste_fluxs)
			FluxTable.insert(db, flux);//Insertion de tous les fluxs propose sur le web
		this.erssDB.Close();
	}
	
	/**
	 * Evenement de click sur un element de la liste
	 */
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		/*On switch l'etat checked d'un element et on le sauvegarde dans la bdd*/
		if(fluxs.get(position).isChecked() == true)
		{//Si il est vrai on le passe faux
			fluxs.get(position).setChecked(false);
			erssDB.Open();
			db = erssDB.getDatabase();
			FluxTable.update(db, fluxs.get(position)); //Sauvegarde
			erssDB.Close();
		}
		else
		{//Sinon on le passe vrai
			fluxs.get(position).setChecked(true);
			erssDB.Open();
			db = erssDB.getDatabase();
			FluxTable.update(db, fluxs.get(position));//Sauvegarde dans la bdd
			erssDB.Close();
		}
	}
	
	/**
	 * Ouverture du menu, appui sur le bouton Menu du telephone
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.fluxactivity, menu); //On develope menu decrit dans /res/menu/fluxactivity.xml
		return true;
	}
	
	/**
	 * Quand on clique sur un element du menu
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		/*On regarde la source du clic*/
		switch (item.getItemId()) {
		case R.id.actualise:	// Si il s'agit du bouton actualiser
			//On crée une alert qui informe l'utilisateur que cela effacera ses preferences
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(R.string.actualise_warning)
				.setCancelable(false)
				.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
					//S'il réponds oui, on actualise la liste
					public void onClick(DialogInterface dialog, int id) {
						new ThreadParse().execute(); 	
					}
				})
				.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
					//S'il reponds non, on reste à l'ecran normal d'activity
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
			AlertDialog alert = builder.create();
			alert.show(); //On affiche l'alert
			return true;
		case R.id.evenementliste:	//Si il s'agit du bouton evenement
			Intent intent = new Intent(this, EventActivity.class);	// On lance une nouvelle EventActivity
			startActivity(intent);
		default:
			return super.onOptionsItemSelected(item);

		}
	}


	/**
	 * Tache asynchrone pour eviter de bloquer l'activity
	 * @author pilou
	 *
	 */
	private class ThreadParse extends AsyncTask<Void, Void, Boolean> {
		private ProgressDialog pDialog;

		/**
		 * Action qui est execute toujours en debut de tache
		 */
		@Override
		protected void onPreExecute() {
			pDialog = new ProgressDialog(FluxActivity.this);
			pDialog.setTitle(R.string.sync_title);
			pDialog.setMessage(getString(R.string.sync_message));
			pDialog.show();
		}

		/**
		 * Action qui s'execute en tache de fond
		 */
		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				updateFluxTableFromWeb();
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
				return false;
			} catch (SAXException e) {
				e.printStackTrace();
				return false;
			}
			return true;
		}

		/**
		 * Action qui est execute quand la tache est finie
		 */
		@Override
		protected void onPostExecute(Boolean result) {
			pDialog.cancel();
			/*Verification si la mise à jour a reussi ou pas*/
			if (result) {	// Si elle a reussi
				updateFluxsFromFluxTable(); //On met a jour les fluxs à partir de la table dans la bdd
		        fAdapter.notifyDataSetChanged();	//On notifie que la liste evolue à l'adapter
				Toast.makeText(FluxActivity.this, R.string.update_success,
						Toast.LENGTH_SHORT).show();	//On notifie l'user du success
				} else {//sinon
				Toast.makeText(FluxActivity.this, R.string.update_fail,
						Toast.LENGTH_SHORT).show(); //On notifie l'user du fail	
			}
		}
	}
	
	/**
	 * Adapter specifique pour gerer l'affichage d'une liste de flux
	 * @author pilou
	 *
	 */
	private class FluxAdapter extends BaseAdapter {

		public int getCount() {
			return fluxs.size();
		}

		public Flux getItem(int position) {
			return fluxs.get(position);
		}

		public long getItemId(int position) {
			return this.getItem(position).getId();
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			TextView tv;
			if (convertView == null) {
				tv = (TextView) LayoutInflater.from(FluxActivity.this).inflate(
						android.R.layout.simple_list_item_multiple_choice, parent, false);
			} else {
				tv = (TextView) convertView;
			}
			tv.setText(getItem(position).getName());//On affiche le nom du flux 
			return tv;
		}
		
	}

}
