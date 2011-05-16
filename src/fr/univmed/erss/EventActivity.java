package fr.univmed.erss;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import fr.univmed.erss.object.Flux;
import fr.univmed.erss.object.Item;
import fr.univmed.erss.parser.ItemHandler;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemLongClickListener;

public class EventActivity extends android.app.ListActivity implements
		OnItemLongClickListener {
	// TAG for log resIdinformation provided by this class .
	private final String TAG = "PickActivity";

	static final int DISPLAY_INFO = 0;
	static final int FILTRE = 1;

	private List<Item> items = new LinkedList<Item>();
	private EfficientAdapter mAdapter;

	private Item sItem;

	private HashMap<String, Bitmap> strImg = new HashMap<String, Bitmap>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(TAG, "onCreate");

		mAdapter = new EfficientAdapter();
		setListAdapter(mAdapter);
		getListView().setOnItemLongClickListener(this);

		new ThreadParse().execute();
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Toast.makeText(
				this,
				"position=" + position + " id=" + id + " val="
						+ items.get(position).getTitle(), Toast.LENGTH_SHORT)
				.show();
	}

	public boolean onItemLongClick(AdapterView<?> madapter, View v,
			int position, long id) {
		Log.i(TAG, "onItemLongClick");
		sItem = items.get(position);
		showDialog(DISPLAY_INFO);
		return true;
	}

	private CharSequence[] getSequenceCategorie(ArrayList<String> listCategorie){
		CharSequence[] sequenceCategorie = new CharSequence[listCategorie.size()];
		for(int i=0; i<listCategorie.size(); i++){
			sequenceCategorie[i] = listCategorie.get(i);
		}
		return sequenceCategorie;
	}
	
	private boolean containsCategory(ArrayList<String> listCategorie, String categorie){
		for(int i=0; i<listCategorie.size(); i++){
			if(listCategorie.get(i).equals(categorie))
				return true;
		}
		return false;
	}
	
	private CharSequence[] getListCategorie() {
		ArrayList<String> listCategorie = new ArrayList<String>();
		for(int i=0; i<items.size(); i++){
			String categorie = items.get(i).getCategory();
			if(!containsCategory(listCategorie,categorie))
				listCategorie.add(categorie);
		}
		return getSequenceCategorie(listCategorie);
	}
	
	private boolean[] getListCheckedItem() {
		boolean[] listCheckedItem = new boolean[items.size()];
		for(int i=0; i<items.size(); i++){
			listCheckedItem[i] = true;
		}
		return listCheckedItem;
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		AlertDialog dialog = null;
		switch (id) {
		case DISPLAY_INFO:
			final CharSequence[] items = { getString(R.string.display_title) };
			dialog = new AlertDialog.Builder(EventActivity.this)
					.setTitle(R.string.action)
					.setItems(items, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int item) {
							
							Intent intent = new Intent(EventActivity.this, ItemActivity.class);

							/*On rajoute les valeurs Ã  lâ€™Intent
							 * en tant quâ€™extra a ce dernier
							 * Les extras sont diffÃ©renciÃ©s par un â€œidâ€� (string)*/
							intent.putExtra("title", sItem.getTitle());
							intent.putExtra("description", sItem.getDescription());
							intent.putExtra("link", sItem.getLink());
							intent.putExtra("pubDate", sItem.getPubDate());
							startActivity(intent);
						}
					}).create();
			break;
		case FILTRE:
			final CharSequence[] listItems = getListCategorie();
			final boolean[] checkedItem = getListCheckedItem();

			dialog = new AlertDialog.Builder(EventActivity.this)
					.setTitle(R.string.filtre_dialog_title)
					.setMultiChoiceItems(listItems, checkedItem,
							new DialogInterface.OnMultiChoiceClickListener() {
								public void onClick(DialogInterface arg0,
										int item, boolean checked) {
									if (checked)
										Toast.makeText(getApplicationContext(),
												listItems[item],
												Toast.LENGTH_SHORT).show();
								}
							}).create();
			break;
		default:
			dialog = null;
		}
		return dialog;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.eventactivity, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.filtre:
			showDialog(FILTRE);
			break;
		case R.id.actualise:
			new ThreadParse().execute();
			return true;
		case R.id.flux:
			Intent intent = new Intent(this, FluxActivity.class);
			startActivity(intent);
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

		URL source = new URL(Flux.URL_AGENDA);
		InputSource inputSourceUrl = new InputSource(source.toString());
		xmlReader.parse(inputSourceUrl, handler);

		// xmlReader.parse(getAssets().open("promotions_hotels.xml"), handler);

		items = handler.getItems();
	}

	/**
	 * Asynctask to avoid hanging activity .
	 */
	private class ThreadParse extends AsyncTask<Void, Void, Boolean> {
		private ProgressDialog pDialog;

		@Override
		protected void onPreExecute() {
			pDialog = new ProgressDialog(EventActivity.this);
			pDialog.setTitle(R.string.sync_title);
			pDialog.setMessage(getString(R.string.sync_message));
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

		private Vector<String> initMap() {
			Vector<String> result = new Vector<String>();
			String car = null;

			car = new String("A");
			result.add(car); /* '\u00C0' ï¿½ alt-0192 */
			result.add(car); /* '\u00C1' ï¿½ alt-0193 */
			result.add(car); /* '\u00C2' ï¿½ alt-0194 */
			result.add(car); /* '\u00C3' ï¿½ alt-0195 */
			result.add(car); /* '\u00C4' ï¿½ alt-0196 */
			result.add(car); /* '\u00C5' ï¿½ alt-0197 */
			car = new String("AE");
			result.add(car); /* '\u00C6' ï¿½ alt-0198 */
			car = new String("C");
			result.add(car); /* '\u00C7' ï¿½ alt-0199 */
			car = new String("E");
			result.add(car); /* '\u00C8' ï¿½ alt-0200 */
			result.add(car); /* '\u00C9' ï¿½ alt-0201 */
			result.add(car); /* '\u00CA' ï¿½ alt-0202 */
			result.add(car); /* '\u00CB' ï¿½ alt-0203 */
			car = new String("I");
			result.add(car); /* '\u00CC' ï¿½ alt-0204 */
			result.add(car); /* '\u00CD' ï¿½ alt-0205 */
			result.add(car); /* '\u00CE' ï¿½ alt-0206 */
			result.add(car); /* '\u00CF' ï¿½ alt-0207 */
			car = new String("D");
			result.add(car); /* '\u00D0' ï¿½ alt-0208 */
			car = new String("N");
			result.add(car); /* '\u00D1' ï¿½ alt-0209 */
			car = new String("O");
			result.add(car); /* '\u00D2' ï¿½ alt-0210 */
			result.add(car); /* '\u00D3' ï¿½ alt-0211 */
			result.add(car); /* '\u00D4' ï¿½ alt-0212 */
			result.add(car); /* '\u00D5' ï¿½ alt-0213 */
			result.add(car); /* '\u00D6' ï¿½ alt-0214 */
			car = new String("*");
			result.add(car); /* '\u00D7' ï¿½ alt-0215 */
			car = new String("0");
			result.add(car); /* '\u00D8' ï¿½ alt-0216 */
			car = new String("U");
			result.add(car); /* '\u00D9' ï¿½ alt-0217 */
			result.add(car); /* '\u00DA' ï¿½ alt-0218 */
			result.add(car); /* '\u00DB' ï¿½ alt-0219 */
			result.add(car); /* '\u00DC' ï¿½ alt-0220 */
			car = new String("Y");
			result.add(car); /* '\u00DD' ï¿½ alt-0221 */
			car = new String("ï¿½");
			result.add(car); /* '\u00DE' ï¿½ alt-0222 */
			car = new String("B");
			result.add(car); /* '\u00DF' ï¿½ alt-0223 */
			car = new String("a");
			result.add(car); /* '\u00E0' ï¿½ alt-0224 */
			result.add(car); /* '\u00E1' ï¿½ alt-0225 */
			result.add(car); /* '\u00E2' ï¿½ alt-0226 */
			result.add(car); /* '\u00E3' ï¿½ alt-0227 */
			result.add(car); /* '\u00E4' ï¿½ alt-0228 */
			result.add(car); /* '\u00E5' ï¿½ alt-0229 */
			car = new String("ae");
			result.add(car); /* '\u00E6' ï¿½ alt-0230 */
			car = new String("c");
			result.add(car); /* '\u00E7' ï¿½ alt-0231 */
			car = new String("e");
			result.add(car); /* '\u00E8' ï¿½ alt-0232 */
			result.add(car); /* '\u00E9' ï¿½ alt-0233 */
			result.add(car); /* '\u00EA' ï¿½ alt-0234 */
			result.add(car); /* '\u00EB' ï¿½ alt-0235 */
			car = new String("i");
			result.add(car); /* '\u00EC' ï¿½ alt-0236 */
			result.add(car); /* '\u00ED' ï¿½ alt-0237 */
			result.add(car); /* '\u00EE' ï¿½ alt-0238 */
			result.add(car); /* '\u00EF' ï¿½ alt-0239 */
			car = new String("d");
			result.add(car); /* '\u00F0' ï¿½ alt-0240 */
			car = new String("n");
			result.add(car); /* '\u00F1' ï¿½ alt-0241 */
			car = new String("o");
			result.add(car); /* '\u00F2' ï¿½ alt-0242 */
			result.add(car); /* '\u00F3' ï¿½ alt-0243 */
			result.add(car); /* '\u00F4' ï¿½ alt-0244 */
			result.add(car); /* '\u00F5' ï¿½ alt-0245 */
			result.add(car); /* '\u00F6' ï¿½ alt-0246 */
			car = new String("/");
			result.add(car); /* '\u00F7' ï¿½ alt-0247 */
			car = new String("0");
			result.add(car); /* '\u00F8' ï¿½ alt-0248 */
			car = new String("u");
			result.add(car); /* '\u00F9' ï¿½ alt-0249 */
			result.add(car); /* '\u00FA' ï¿½ alt-0250 */
			result.add(car); /* '\u00FB' ï¿½ alt-0251 */
			result.add(car); /* '\u00FC' ï¿½ alt-0252 */
			car = new String("y");
			result.add(car); /* '\u00FD' ï¿½ alt-0253 */
			car = new String("ï¿½");
			result.add(car); /* '\u00FE' ï¿½ alt-0254 */
			car = new String("y");
			result.add(car); /* '\u00FF' ï¿½ alt-0255 */
			result.add(car); /* '\u00FF' alt-0255 */

			return result;
		}

		private String sansAccent(String chaine) {
			java.lang.StringBuffer Result = new StringBuffer(chaine);
			int MIN = 192;
			int MAX = 255;
			Vector<String> map = initMap();
			for (int bcl = 0; bcl < Result.length(); bcl++) {
				int carVal = chaine.charAt(bcl);
				if (carVal >= MIN && carVal <= MAX) { // Remplacement
					java.lang.String newVal = (java.lang.String) map.get(carVal
							- MIN);
					Result.replace(bcl, bcl + 1, newVal);
				}
			}
			return Result.toString();
		}

		@Override
		protected void onPostExecute(Boolean result) {
			pDialog.cancel();
			if (result) {
				mAdapter.notifyDataSetChanged();
				Toast.makeText(EventActivity.this, R.string.update_success,
						Toast.LENGTH_SHORT).show();

				// add the default icon
				strImg.put(null, BitmapFactory.decodeResource(
						getResources(),
						getResources().getIdentifier("evenement", "drawable",
								"fr.univmed.erss")));

				for (int i = 0; i < items.size(); i++) {
					if (!strImg.containsKey(items.get(i).getCategory())) {
						// drawable name
						String drawableName = sansAccent(items.get(i)
								.getCategory().toLowerCase().replace(" ", "_"));

						// drawable id from name
						int drawableId = getResources().getIdentifier(
								drawableName, "drawable", "fr.univmed.erss"); // package
																				// name
																				// not
																				// good
																				// idea
																				// .
						// retreive bitmap
						Bitmap icon = BitmapFactory.decodeResource(
								getResources(), drawableId);

						strImg.put(items.get(i).getCategory(), icon);
					}
				}
			} else {
				Toast.makeText(EventActivity.this, R.string.update_fail,
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
			ImageView icon;
		}

		public EfficientAdapter() {
			// Cache the LayoutInflate to avoid asking for a new one each time.
			mInflater = LayoutInflater.from(EventActivity.this);
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
				holder.icon = (ImageView) convertView.findViewById(R.id.icon);

				convertView.setTag(holder);
			} else {
				// Get the ViewHolder back to get fast access to the TextView
				// and the ImageView.
				holder = (ViewHolder) convertView.getTag();
			}

			Bitmap icon = strImg.get(items.get(position).getCategory());

			// Bind the data efficiently with the holder.
			holder.text.setText(items.get(position).getTitle());
			holder.icon.setImageBitmap(icon);

			return convertView;
		}
	}
}
