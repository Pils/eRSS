package fr.univmed.erss.object;

/**
 * Classe qui defini les elements Flux
 * Ces elements sont les adresse sources des flux rss a recuperer
 * L'element comprends le nom de la source, l'url, ainsi que l'id dans la bdd, et si il est selectionné comme à suivre au non.
 * @author pilou
 *
 */
public class Flux {
	//TODO les liens doivent disparaitre
	public final static String URL_HOTEL = "http://romain.de-luca.perso.esil.univmed.fr/promotions_hotels.xml";
	public final static String URL_AGENDA = "http://romain.de-luca.perso.esil.univmed.fr/Agenda_Culturel_Marseille.xml";
	
	public long id;
	public String name;
	public String url;

	public boolean checked;//True si le flux est à suivre, False sinon
	
	//-----------------------------------------------------//
	//					Constructeurs					   //
	//-----------------------------------------------------//
	
	public Flux(String url, boolean checked) {
		super();
		this.url = url;
		this.checked = checked;
	}
	
	public Flux() {
	
	}

	//-----------------------------------------------------//
	//				Getters & Setters					   //
	//-----------------------------------------------------//
	public void setName (String name) {
		this.name = name;
	}
	public String getName () {
		return name;
	}
	public void setId (long id) {
		this.id = id;
	}
	public long getId () {
		return id;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public boolean isChecked() {
		return checked;
	}
	public void setChecked(boolean checked) {
		this.checked = checked;
	}
	public void setChecked(int checked) {
		if(checked == 1)
			this.checked = true;
		else
			this.checked = false;
	}
	
	public void setChecked(String checked) {
		if(checked == "true")
			this.checked = true;
		else
			this.checked = false;
	}
	
}
