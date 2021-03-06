package fr.univmed.erss;


import fr.univmed.erss.calendar.GoogleAgenda;
import fr.univmed.erss.object.Item;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Classe definissant l'ecran et l'activite qui affiche un objet apres un clic long
 * @author xavier
 *
 */
public class ItemActivity extends Activity{
	
	// TAG for log resIdinformation provided by this class .
	private final String LOG_TAG = "ItemActivity";
	private static final int CALENDAR_LIST = 0;
	private GoogleAgenda agenda = new GoogleAgenda(this);
	private int selectedCalendar;
	private CharSequence[] listCal;
	
	private Item myItem = new Item();
	private String title;
	private String description;
	private String link;
	private String pubDate;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		Log.i(LOG_TAG, "onCreate()");
		
		final String mimeType = "text/html";
        final String encoding = "utf-8";
        
        /* récupération de l'item cliqué 
         * et des champs nécessaires*/
        Intent thisIntent = getIntent();
        title = thisIntent.getExtras().getString("title");
        description = thisIntent.getExtras().getString("description");
        link = thisIntent.getExtras().getString("link");
        pubDate = thisIntent.getExtras().getString("pubDate");
        
        /*utile pour l'ajout d'un évènement */
        myItem.setTitle(title);
        myItem.setDescription(description);
        myItem.setLink(link);
        myItem.setPubDate(pubDate);
        
        /* Chargement de la vue */
        setContentView(R.layout.itemview);
        
		//On récupère les champs et on leur affecte le texte
        TextView titleText = (TextView) findViewById(R.id.title);
		titleText.setText(title);
		
		/* Récupération de la description en format html
		 * on l'affiche avec un WebView
		 */
		WebView wv1 = (WebView) findViewById(R.id.description);
		wv1.loadData(description, mimeType, encoding);
		
		/* affichage du lien web de l'évènement et sur clic ouvre dans un navigateur */
		TextView linkText = (TextView) findViewById(R.id.link);
		linkText.setText(Html.fromHtml("<a href=\"" + link + "\">"+link + "</a>"));
		linkText.setMovementMethod(LinkMovementMethod.getInstance());
		
		/* affichage de la date de publication de l'évènement */
        TextView pubDateText = (TextView) findViewById(R.id.pubDate);
		pubDateText.setText(pubDate);
	}
	
	/**
	 * Ouverture du menu, appui sur le bouton Menu du telephone
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.itemactivity, menu); //On develope menu decrit dans /res/menu/itemactivity.xml
		return true;
	}
	
	/**
	 * Quand on clique sur un element du menu
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		/*On regarde la source du clic*/
		switch (item.getItemId()) {
			case R.id.ajouter:	// Si il s'agit du bouton calendar
				/* Gestion du calendrier avec ajout de l'item courant
				 * création d'un agenda local
				 * recherche des calendriers existants
				 * ajout de l'évènement dans le calendrier sélectionné
				 */
				showDialog(CALENDAR_LIST);
				return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	public Dialog onCreateDialog(int id) {
		AlertDialog dialog = null;
		switch (id) {
		case CALENDAR_LIST:
			/* Création du dialog de calendars.
			 * A noté qu'on l'a initialisé au préalable dans onPostExecute())
			*/
			listCal=agenda.listAllCalendarDetails();
			dialog = new AlertDialog.Builder(ItemActivity.this)
					.setTitle(R.string.calendar_dialog_title)
					.setSingleChoiceItems(listCal, selectedCalendar,
							new DialogInterface.OnClickListener(){
								public void onClick(DialogInterface dialog, int selected) {
									Toast.makeText(getApplicationContext(),
									(String)listCal[selected] + "  et  " + selected,
									Toast.LENGTH_SHORT).show();
								}
							})
					.setPositiveButton(R.string.alert_dialog_ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int selected) {
									agenda.createEvent(agenda.findUpdateCalendar(
											(String)listCal[selectedCalendar]), myItem);
											}		
							})
					.setNegativeButton(R.string.alert_dialog_cancel,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
								}
							})
					.create();
			break;
		default:
			dialog = null;
		}
		return dialog;
	}
}
