package fr.univmed.erss.parser;

import java.util.LinkedList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

public class ItemHandler extends DefaultHandler {

	private static final String LOG_TAG = "ItemHandler";
	private Item item = null;
	private StringBuffer buffer = null;
	private Boolean collect = false;
	private List<Item> items = null;
	private String title;
	private String description;
	private String link;
	private String language;
	private String category;
	
	public List<Item> getItems() {
		return items;
	}

	@Override
	public void startDocument() throws SAXException {
		Log.i(LOG_TAG, "Line Handle : Start Document");
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		if (localName.equals("channel")) {
			items = new LinkedList<Item>();
			collect = true;
		} else if (localName.equals("item") && collect) {
			item = new Item();
		} else if (item != null && collect) {
			buffer = new StringBuffer();
		} else if(item==null && localName.equals("title")) {
			title = new StringBuffer().toString();
		} else if(item==null && localName.equals("description")) {
			description = new StringBuffer().toString();
		} else if(item==null && localName.equals("link")) {
			link = new StringBuffer().toString();
		} else if(item==null && localName.equals("category")) {
			category = new StringBuffer().toString();
		} else if(item==null && localName.equals("language")) {
			language = new StringBuffer().toString();
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

		if (localName.equals("item")) {
			items.add(item);
			item = null;
		} else if(item==null) {
			return;
		} else if (localName.equals("title")) {
			item.setTitle(buffer.toString());
		} else if (localName.equals("description")) {
			item.setDescription(buffer.toString());
		} else if (localName.equals("guid")) {
			item.setGuid(buffer.toString());
		} else if (localName.equals("link")) {
			item.setLink(buffer.toString());
		} else if (localName.equals("pubDate")) {
			item.setPubDate(buffer.toString());
		} else if (localName.equals("category")) {
			item.setCategory(buffer.toString());
		}
		buffer = null;
	}

	@Override
	public void endDocument() throws SAXException {
		Log.i(LOG_TAG, "Line Handle : End Document");
	}
}
