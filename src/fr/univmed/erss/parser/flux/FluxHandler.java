package fr.univmed.erss.parser.flux;

import java.util.LinkedList;
import java.util.List;


import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

import fr.univmed.erss.object.Flux;


public class FluxHandler extends DefaultHandler{

	private static final String LOG_TAG = "FluxHandler";
	
	public static final String URL_SOURCE= "http://pierre-louis.conte.perso.esil.univmed.fr/adresse_flux_rss.xml";
	
	private Flux flux;
	private String name;
	private String url;
	//private boolean checked;
	
	private StringBuffer buffer = null;
	private Boolean collect = false;
	private List<Flux> fluxs = null;
	
	
	public List<Flux> getFluxs() {
		return fluxs;
	}
	
	@Override
	public void startDocument() throws SAXException {
		Log.i(LOG_TAG, "Line Handle : Start Document");
		
	}
	
	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		if (localName.equals("liste_flux")) {
			fluxs = new LinkedList<Flux>();
			collect = true;
		} else if (localName.equals("flux") && collect) {
			Log.i(LOG_TAG,"New FLUX");
			flux = new Flux();
		} else if (flux != null && collect) {
			buffer = new StringBuffer();
		} else if(flux==null && localName.equals("name")) {
			name = new StringBuffer().toString();
			Log.i(LOG_TAG, name);
		} else if(flux==null && localName.equals("url")) {
			url = new StringBuffer().toString();
			Log.i(LOG_TAG, url);
		} 
	}
	
	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		if (collect) {
			String lecture = new String(ch, start, length);
			lecture.replaceAll("\\n", "");
			if (buffer != null)
				buffer.append(lecture);
		}
	}
	
	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		if (!collect)
			return;

		if (localName.equals("flux")) {
			Log.i(LOG_TAG, flux.getName()+"-"+ flux.getUrl());
			flux.setChecked(true);
			fluxs.add(flux);
			flux = null;
		} else if(flux==null) {
			return;
		} else if (localName.equals("name")) {
			flux.setName(buffer.toString());
		} else if (localName.equals("url")) {
			flux.setUrl(buffer.toString());
		}
		buffer = null;
	}
	
	@Override
	public void endDocument() throws SAXException {
		Log.i(LOG_TAG, "Line Handle : End Document");
	}
}
