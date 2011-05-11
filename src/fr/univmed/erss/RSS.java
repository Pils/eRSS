package fr.univmed.erss;

public class RSS {
	public String url;
//= "http://romain.de-luca.perso.esil.univmed.fr/promotions_hotels.xml";
//= "http://romain.de-luca.perso.esil.univmed.fr/Agenda_Culturel_Marseille.xml";
	public boolean checked;
	
	public RSS(String url, boolean checked) {
		super();
		this.url = url;
		this.checked = checked;
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
	
}
