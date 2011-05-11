package fr.univmed.erss.object;

public class RSS {
	
	public long id;
	public String url;
//= "http://romain.de-luca.perso.esil.univmed.fr/promotions_hotels.xml";
//= "http://romain.de-luca.perso.esil.univmed.fr/Agenda_Culturel_Marseille.xml";
	public boolean checked;
	
	//-----------------------------------------------------//
	//					Constructeurs					   //
	//-----------------------------------------------------//
	
	public RSS(String url, boolean checked) {
		super();
		this.url = url;
		this.checked = checked;
	}
	
	public RSS() {
	
	}

	//-----------------------------------------------------//
	//				Getters & Setters					   //
	//-----------------------------------------------------//
	
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
	public void setChecked(String checked) {
		if(checked == "true")
		{
			this.checked = true;
		}
		else
			this.checked = false;
	}
	
}