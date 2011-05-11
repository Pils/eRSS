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

import fr.univmed.erss.parser.Item;
import fr.univmed.erss.parser.ItemHandler;
import android.app.ListActivity;
import android.app.ProgressDialog;
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
import android.widget.TextView;
import android.widget.Toast;

public class PickActivity extends ListActivity  {
	// TAG for log information provided by this class .
	private final String TAG = "PickActivity";

	private List<Item> items = new LinkedList<Item>();
	private EfficientAdapter mAdapter;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		Log.i(TAG, "onCreate");

		mAdapter = new EfficientAdapter();
		setListAdapter(mAdapter);

		new ThreadParse().execute();
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.homeactivity, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.sync:
			new ThreadParse().execute();
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void retrieveData() throws ParserConfigurationException,
			SAXException, IOException {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser xmlReader;
		xmlReader = factory.newSAXParser();
		ItemHandler handler = new ItemHandler();

		URL source = new URL(RSS.URL_HOTEL);
		InputSource inputSourceUrl = new InputSource(source.toString());
		xmlReader.parse(inputSourceUrl, handler);
		

		//xmlReader.parse(getAssets().open("promotions_hotels.xml"), handler);

		items = handler.getItems();
	}

	/**
	 * Asynctask to avoid hanging activity .
	 */
	private class ThreadParse extends AsyncTask<Void, Void, Boolean> {
		private ProgressDialog pDialog;

		@Override
		protected void onPreExecute() {
			pDialog = new ProgressDialog(PickActivity.this);
			pDialog.setTitle("Sync");
			pDialog.setMessage("Syncing with remote location .");
			pDialog.show();
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				retrieveData();
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
				mAdapter.notifyDataSetChanged();
				Toast.makeText(PickActivity.this, "List Updated",
						Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(PickActivity.this, "Update Fail",
						Toast.LENGTH_SHORT).show();
			}
		}
	}

	/**
	 * Intern Class for displaying a custom List of Objects line .
	 * 
	 */
	private class EfficientAdapter extends BaseAdapter {
		private LayoutInflater mInflater;

		class ViewHolder {
			TextView text;
		}

		public EfficientAdapter() {
			// Cache the LayoutInflate to avoid asking for a new one each time.
			mInflater = LayoutInflater.from(PickActivity.this);
		}

		/**
		 * The number of items in the list is determined by the number of
		 * speeches in our array.
		 * 
		 * @see android.widget.ListAdapter#getCount()
		 */
		public int getCount() {
			return items.size();
		}

		/**
		 * Since the data comes from an array, just returning the index is
		 * sufficent to get at the data. If we were using a more complex data
		 * structure, we would return whatever object represents one row in the
		 * list.
		 * 
		 * @see android.widget.ListAdapter#getItem(int)
		 */
		public Object getItem(int position) {
			return position;
		}

		/**
		 * Use the array index as a unique id.
		 * 
		 * @see android.widget.ListAdapter#getItemId(int)
		 */
		public long getItemId(int position) {
			return position;
		}

		/**
		 * Make a view to hold each row.
		 * 
		 * @see android.widget.ListAdapter#getView(int, android.view.View,
		 *      android.view.ViewGroup)
		 */
		public View getView(int position, View convertView, ViewGroup parent) {
			// A ViewHolder keeps references to children views to avoid
			// unneccessary calls
			// to findViewById() on each row.
			ViewHolder holder;

			// When convertView is not null, we can reuse it directly, there is
			// no need
			// to reinflate it. We only inflate a new View when the convertView
			// supplied
			// by ListView is null.
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.list_item_icon_text,
						null);
				// Creates a ViewHolder and store references to the two children
				// views
				// we want to bind data to.
				holder = new ViewHolder();
				holder.text = (TextView) convertView.findViewById(R.id.text);

				convertView.setTag(holder);
			} else {
				// Get the ViewHolder back to get fast access to the TextView
				// and the ImageView.
				holder = (ViewHolder) convertView.getTag();
			}

			// Bind the data efficiently with the holder.
			holder.text.setText(items.get(position).getTitle());

			return convertView;
		}
	}

}
