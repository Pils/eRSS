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
        
        erssDB = new ErssDB(this);
        //TODO if( databaseIsEmpty() )
		new ThreadParse().execute();
		
	}
	
	
	private void updateFluxsFromWeb () throws ParserConfigurationException,
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
		Log.i(LOG_TAG, "this.erssDB.Open();");
		db = erssDB.getDatabase();
		Log.i(LOG_TAG, "erssDB.getDatabase();");
		
		db.execSQL("DELETE FROM "+ FluxTable.TABLE_NAME +";");
		Log.i(LOG_TAG, "db.execSQL(DELETE);");
		
		
		for(Flux flux : liste_fluxs)
			FluxTable.insert(db, flux);
		
		Cursor cursor = db.query(FluxTable.TABLE_NAME, null, null, null, null, null, null);
		while(cursor.moveToNext())
		{
			fluxs.add(FluxTable.fromCursor(cursor));
			//Log.i(LOG_TAG, cursor.getColumnName(3)+"->"+cursor.getString(3));
		}
		cursor.close();
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
		Toast.makeText(this, "position=" + position + " id=" + id + " val=" + fluxs.get(position).getUrl()
				, Toast.LENGTH_SHORT).show();
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
			Intent intent = new Intent(this, PickActivity.class);
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
			pDialog.setTitle("Sync");
			pDialog.setMessage("Syncing with remote location .");
			pDialog.show();
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				updateFluxsFromWeb();
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
				for(int i=0; i<fluxs.size(); i++)
				{
		        	FluxActivity.this.getListView().setItemChecked(i, true);
		        	Log.i(LOG_TAG,"flux checked ?"+ fluxs.get(i).isChecked());
				}
		        fAdapter.notifyDataSetChanged();
				Toast.makeText(FluxActivity.this, "List Updated",
						Toast.LENGTH_SHORT).show();
				} else {
				Toast.makeText(FluxActivity.this, "Update Fail",
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
