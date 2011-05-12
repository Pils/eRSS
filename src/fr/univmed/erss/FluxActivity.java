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

import fr.univmed.erss.parser.flux.FluxHandler;
import fr.univmed.erss.object.Flux;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;


public class FluxActivity extends ListActivity{

	private final String TAG = "FluxActivity";

	private List<Flux> fluxs = new LinkedList<Flux>();
	
	//private static final String[] fluxs_name = new String[] { "Flux 1", "Flux 2" }; 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		Log.i(TAG, "onCreate()");

		
		new ThreadParse().execute();
		
		for(int i=0; i< fluxs.size() ; i++)
			Log.i(TAG, fluxs.get(i).getName());
	}
	
	private void updateFluxs () throws ParserConfigurationException,
	SAXException, IOException {
		
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser xmlReader;
		xmlReader = factory.newSAXParser();
		FluxHandler handler = new FluxHandler();

		URL source = new URL(FluxHandler.URL_SOURCE);
		InputSource inputSourceUrl = new InputSource(source.toString());
		xmlReader.parse(inputSourceUrl, handler);

		fluxs = handler.getFluxs();	
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
				updateFluxs();
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
			//	mAdapter.notifyDataSetChanged();
				Toast.makeText(FluxActivity.this, "List Updated",
						Toast.LENGTH_SHORT).show();
				} else {
				Toast.makeText(FluxActivity.this, "Update Fail",
						Toast.LENGTH_SHORT).show();
			}
		}
	}
	
/*	
	private class MyListAdapter extends BaseAdapter {

		public int getCount() {
			return fluxs.size();
		}

		public Object getItem(int position) {
			return fluxs.get(position);
		}

		public long getItemId(int position) {
			return position+1; // TODO Prendre l'ID à partir de la db
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			return null;
		}
		
	}*/
}
