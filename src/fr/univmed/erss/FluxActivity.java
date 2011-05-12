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
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
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


public class FluxActivity extends ListActivity{

	private final String LOG_TAG = "FluxActivity";

	private List<Flux> fluxs = new LinkedList<Flux>();
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
                
		new ThreadParse().execute();
		
	}
	
	private void updateFluxsFromWeb () throws ParserConfigurationException,
	SAXException, IOException {
		
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser xmlReader;
		xmlReader = factory.newSAXParser();
		FluxHandler handler = new FluxHandler();

		URL source = new URL(FluxHandler.URL_SOURCE);
		InputSource inputSourceUrl = new InputSource(source.toString());
		xmlReader.parse(inputSourceUrl, handler);

		fluxs = handler.getFluxs();
		
		erssDB = new ErssDB(this);
		Log.i(LOG_TAG, "new ErssDB(this);");
		this.erssDB.Open();
		Log.i(LOG_TAG, "this.erssDB.Open();");
		db = erssDB.getDatabase();
		Log.i(LOG_TAG, "erssDB.getDatabase();");
		
		//db.execSQL("DELETE FROM "+ FluxTable.TABLE_NAME+";");
		Log.i(LOG_TAG, "db.execSQL(DELETE);");
		
		for(Flux flux : fluxs)
			FluxTable.insert(db, flux);
		
		this.erssDB.Close();
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
		        	FluxActivity.this.getListView().setItemChecked(i, true);
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
			return position+1; // TODO Prendre l'ID à partir de la db
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
