package de.wwu.d2s.util;


/**
 * Basic class describing the core features of a P2P SCC Platform.
 */
public class Platform {
	
	private int id;

	private String resourceName; // the resource URI used for this platform in the ontology

	private String label;
	private String url;
	
	public Platform() {
	}

	/**
	 * Determines the repective setter for the given variable. This method can be used to transform SPARQL query results into a platform object.
	 * 
	 * @param var The SPARQL result variable to determine the setter by
	 * @param val The variable's value to pass to the setter.
	 */
	public void set(String var, String val) {
		switch (var) {
		case "resourceName":
			setResourceName(val);
			break;
		case "label":
			setLabel(val);
			break;
		case "url":
			setUrl(val);
			break;
		}
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getResourceName() {
		return resourceName;
	}

	public void setResourceName(String resourceName) {
		this.resourceName = resourceName;
	}
}
