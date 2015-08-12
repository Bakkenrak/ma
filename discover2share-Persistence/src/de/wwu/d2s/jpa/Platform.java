package de.wwu.d2s.jpa;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.PrePersist;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
public class Platform implements Serializable {
	private static final long serialVersionUID = -6541452756921763514L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;

	private Date created;

	private String label;
	private String url;
	private String description;

	@ElementCollection(fetch=FetchType.EAGER)
	@CollectionTable(joinColumns = @JoinColumn(name = "id"))
	@Column(name = "resourceType")
	private List<String> resourceTypes = new ArrayList<String>();

	@ElementCollection(fetch=FetchType.EAGER)
	@CollectionTable(joinColumns = @JoinColumn(name = "id"))
	@Column(name = "consumerism")
	private List<String> consumerisms = new ArrayList<String>();

	private String pattern;
	private String temporality;

	@ElementCollection(fetch=FetchType.EAGER)
	@CollectionTable(joinColumns = @JoinColumn(name = "id"))
	@Column(name = "marketMediation")
	private List<String> marketMediations = new ArrayList<String>();

	private String typeOfAccessedObject;
	private String resourceOwner;
	private String serviceDurationMin;
	private String serviceDurationMax;
	private String consumerInvolvement;
	private String moneyFlow;
	private String offering;
	private String geographicScope;
	private String yearLaunch;
	private String launchCountry;
	private String launchCity;
	private String residenceCountry;
	private String residenceCity;

	@ElementCollection(fetch=FetchType.EAGER)
	@CollectionTable(joinColumns = @JoinColumn(name = "id"))
	@Column(name = "app")
	private List<String> apps = new ArrayList<String>();
	
	@ElementCollection(fetch=FetchType.EAGER)
	@CollectionTable(joinColumns = @JoinColumn(name = "id"))
	@Column(name = "language")
	private List<String> languages = new ArrayList<String>();

	public Platform() { //hibernate constructor
	}	
	

	public void set(String var, String val) {
		switch (var){
			case "label":
				setLabel(val);
				break;
			case "url":
				setUrl(val);
				break;
			case "description":
				setDescription(val);
				break;
			case "resourceType":
				addResourceType(val);
				break;
			case "consumerism":
				addConsumerism(val);
				break;
			case "pattern":
				setPattern(val);
				break;
			case "temporality":
				setTemporality(val);
				break;
			case "marketMediation":
				addMarketMediation(val);
				break;
			case "typeOfAccessedObject":
				setTypeOfAccessedObject(val);
				break;
			case "resourceOwner":
				setResourceOwner(val);
				break;
			case "serviceDurationMin":
				setServiceDurationMin(val);
				break;
			case "serviceDurationMax":
				setServiceDurationMax(val);
				break;
			case "consumerInvolvement":
				setConsumerInvolvement(val);
				break;
			case "moneyFlow":
				setMoneyFlow(val);
				break;
			case "offering":
				setOffering(val);
				break;
			case "geographicScope":
				setGeographicScope(val);
				break;
			case "yearLaunch":
				setYearLaunch(val);
				break;
			case "launchCountryGeonames":
				setLaunchCountry(val);
				break;
			case "launchCityGeonames":
				setLaunchCity(val);
				break;
			case "residenceCountryGeonames":
				setResidenceCountry(val);
				break;
			case "residenceCityGeonames":
				setResidenceCity(val);
				break;
			case "app":
				addApp(val);
				break;
			case "language":
				addLanguage(val);
				break;
		}
	}

	private void addResourceType(String val) {
		if(!resourceTypes.contains(val))
			resourceTypes.add(val);
	}

	private void addConsumerism(String val) {
		if(!consumerisms.contains(val))
			consumerisms.add(val);
	}
	
	private void addMarketMediation(String val) {
		if(!marketMediations.contains(val))
			marketMediations.add(val);
	}
	
	private void addApp(String val) {
		if(!apps.contains(val))
			apps.add(val);
	}
	
	private void addLanguage(String val) {
		if(!languages.contains(val))
			languages.add(val);
	}

	@PrePersist
	protected void onCreate() {
		created = new Date();
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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<String> getResourceTypes() {
		return resourceTypes;
	}

	public void setResourceTypes(List<String> resourceTypes) {
		this.resourceTypes = resourceTypes;
	}

	public List<String> getConsumerisms() {
		return consumerisms;
	}

	public void setConsumerisms(List<String> consumerisms) {
		this.consumerisms = consumerisms;
	}

	public String getPattern() {
		return pattern;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	public String getTemporality() {
		return temporality;
	}

	public void setTemporality(String temporality) {
		this.temporality = temporality;
	}

	public List<String> getMarketMediations() {
		return marketMediations;
	}

	public void setMarketMediations(List<String> marketMediations) {
		this.marketMediations = marketMediations;
	}

	public String getTypeOfAccessedObject() {
		return typeOfAccessedObject;
	}

	public void setTypeOfAccessedObject(String typeOfAccessedObject) {
		this.typeOfAccessedObject = typeOfAccessedObject;
	}

	public String getResourceOwner() {
		return resourceOwner;
	}

	public void setResourceOwner(String resourceOwner) {
		this.resourceOwner = resourceOwner;
	}

	public String getServiceDurationMin() {
		return serviceDurationMin;
	}

	public void setServiceDurationMin(String serviceDurationMin) {
		this.serviceDurationMin = serviceDurationMin;
	}

	public String getServiceDurationMax() {
		return serviceDurationMax;
	}

	public void setServiceDurationMax(String serviceDurationMax) {
		this.serviceDurationMax = serviceDurationMax;
	}

	public String getConsumerInvolvement() {
		return consumerInvolvement;
	}

	public void setConsumerInvolvement(String consumerInvolvement) {
		this.consumerInvolvement = consumerInvolvement;
	}

	public String getMoneyFlow() {
		return moneyFlow;
	}

	public void setMoneyFlow(String moneyFlow) {
		this.moneyFlow = moneyFlow;
	}

	public String getOffering() {
		return offering;
	}

	public void setOffering(String offering) {
		this.offering = offering;
	}

	public String getGeographicScope() {
		return geographicScope;
	}

	public void setGeographicScope(String geographicScope) {
		this.geographicScope = geographicScope;
	}

	public String getYearLaunch() {
		return yearLaunch;
	}

	public void setYearLaunch(String yearLaunch) {
		this.yearLaunch = yearLaunch;
	}

	public String getLaunchCountry() {
		return launchCountry;
	}

	public void setLaunchCountry(String launchCountry) {
		this.launchCountry = launchCountry;
	}

	public String getLaunchCity() {
		return launchCity;
	}

	public void setLaunchCity(String launchCity) {
		this.launchCity = launchCity;
	}

	public String getResidenceCountry() {
		return residenceCountry;
	}

	public void setResidenceCountry(String residenceCountry) {
		this.residenceCountry = residenceCountry;
	}

	public String getResidenceCity() {
		return residenceCity;
	}

	public void setResidenceCity(String residenceCity) {
		this.residenceCity = residenceCity;
	}

	public List<String> getApps() {
		return apps;
	}

	public void setApps(List<String> apps) {
		this.apps = apps;
	}


	public Date getCreated() {
		return created;
	}


	public void setCreated(Date created) {
		this.created = created;
	}


	public List<String> getLanguages() {
		return languages;
	}


	public void setLanguages(List<String> languages) {
		this.languages = languages;
	}
}
