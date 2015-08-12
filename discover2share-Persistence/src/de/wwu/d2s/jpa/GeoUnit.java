package de.wwu.d2s.jpa;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GeoUnit implements Serializable{
	private static final long serialVersionUID = 4998750649061597103L;

	private String geonames;
	
	private String resource;
	
	private String label;
	
	public GeoUnit(){}

	public String getGeonames() {
		return geonames;
	}

	public void setGeonames(String geonames) {
		this.geonames = geonames;
	}

	public String getResource() {
		return resource;
	}

	public void setResource(String resource) {
		this.resource = resource;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

}
