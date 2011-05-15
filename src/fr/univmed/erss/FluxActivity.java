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

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
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


public class FluxActivity extends ListActivity {

	private final String LOG_TAG = "FluxActivity";

	private List<Flux> fluxs = new LinkedList<Flux>();;
	private FluxAdapter fAdapter;
	
	private ErssDB erssDB;
	private SQLiteDatabase db;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		Log.i(LOG_TAG, "onCreate()");

		fAdapter = new FluxAdapter();
		setListAdapter(fAdapter);
		
		final ListView listView = getListView();
        listView.setItemsCanFocus(false);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        
        setTitle(R.string.flux_activity_title);
        
        erssDB = new ErssDB(this);
        erssDB.Open();
		db = erssDB.getDatabase();
		boolean doRefresh = FluxTable.isEmpty(db);
        erssDB.Close();
		if( doRefresh )
           	new ThreadParse().execute();
		else
			updateFluxsFromFluxTable();
	}
	
	private void updateFluxsFromFluxTable () {
		
		this.erssDB.Open();
		db = erssDB.getDatabase();
		
		Cursor cursor = db.query(FluxTable.TABLE_NAME, null, null, null, null, null, null);
		while(cursor.moveToNext())
		{
			fluxs.add(FluxTable.fromCursor(cursor));
			//Log.i(LOG_TAG, cursor.getColumnName(3)+"->"+cursor.getString(3));
		}
		cursor.close();
		this.erssDB.Close();
		
		for(int i=0; i<fluxs.size(); i++)
		{
			if(fluxs.get(i).isChecked())
				this.getListView().setItemChecked(i, true);
			else
				this.getListView().setItemChecked(i, false);
        	Log.i(LOG_TAG,"flux checked ?"+ fluxs.get(i).isChecked());
		}
		
	}
	
	private void updateFluxTableFromWeb () throws ParserConfigurationException,
	SAXException, IOException {
		
		List<Flux> liste_fluxs = new LinkedList<Flux>();
		
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser xmlReader;
		xmlReader = factory.newSAXParser();
		FluxHandler handler = new FluxHandler();

		URL source = new URL(FluxHandler.URL_SOURCE);
		InputSource inputSourceUrl = new InputSource(source.toString());
		xmlReader.parse(inputSourceUrl, handler);

		liste_fluxs = handler.getFluxs();
		
		fluxs.clear();
		
		this.erssDB.Open();
		db = erssDB.getDatabase();
		
		db.execSQL("DELETE FROM "+ FluxTable.TABLE_NAME +";");
		Log.i(LOG_TAG, "db.execSQL(DELETE);");
		
		
		for(Flux flux : liste_fluxs)
			FluxTable.insert(db, flux);
		
		this.erssDB.Close();
	}
	
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		
		if(fluxs.get(position).isChecked() == true)
		{
			fluxs.get(position).setChecked(false);
			erssDB.Open();
			db = erssDB.getDatabase();
			FluxTable.update(db, fluxs.get(position));
			erssDB.Close();
		}
		else
		{
			fluxs.get(position).setChecked(true);
			erssDB.Open();
			db = erssDB.getDatabase();
			FluxTable.update(db, fluxs.get(position));
			erssDB.Close();
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.fluxactivity, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.actualise:
			new ThreadParse().execute();
			return true;
		case R.id.evenementliste:
			Intent intent = new Intent(this, EventActivity.class);
			startActivity(intent);
		default:
			return super.onOptionsItemSelected(item);

		}
	}


	/**
	 * Asynctask to avoid hanging activity .
	 */
	private class ThreadParse extends AsyncTask<Void, Void, Boolean> {
		private ProgressDialog pDialog;

		@Override
		protected void onPreExecute() {
			pDialog = new ProgressDialog(FluxActivity.this);
			pDialog.setTitle(R.string.sync_title);
			pDialog.setMessage(getString(R.string.sync_message));
			pDialog.show();
		}

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

		@Override
		protected void onPostExecute(Boolean result) {
			pDialog.cancel();
			
			if (result) {
				updateFluxsFromFluxTable();
		        fAdapter.notifyDataSetChanged();
				Toast.makeText(FluxActivity.this, R.string.update_success,
						Toast.LENGTH_SHORT).show();
				} else {
				Toast.makeText(FluxActivity.this, R.string.update_fail,
						Toast.LENGTH_SHORT).show();
			}
		}
	}
	
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
			tv.setText(getItem(position).getName());
			return tv;
		}
		
	}

}
