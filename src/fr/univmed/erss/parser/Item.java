package fr.univmed.erss.parser;


public class Item {
	private String title;
	private String description;
	private String guid;
	private String link;
	private String pubDate;
	
	public Item() {}
	
	@Override
	public String toString() {
		return "Item [title=" + title + ", description=" + description
				+ ", guid=" + guid + ", link=" + link + ", pubDate=" + pubDate
				+ "]";
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getGuid() {
		return guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getPubDate() {
		return pubDate;
	}

	public void setPubDate(String pubDate) {
		this.pubDate = pubDate;
	}
}
