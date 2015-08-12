package de.wwu.d2s.jpa;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

	private String resourceName;
	private String label;
	private String url;
	private String description;

	@ElementCollection(fetch=FetchType.EAGER)
	@CollectionTable(joinColumns = @JoinColumn(name = "id"))
	@Column(name = "resourceType")
	private Set<String> resourceTypes = new HashSet<String>();

	@ElementCollection(fetch=FetchType.EAGER)
	@CollectionTable(joinColumns = @JoinColumn(name = "id"))
	@Column(name = "consumerism")
	private Set<String> consumerisms = new HashSet<String>();

	private String pattern;
	private String temporality;

	@ElementCollection(fetch=FetchType.EAGER)
	@CollectionTable(joinColumns = @JoinColumn(name = "id"))
	@Column(name = "marketMediation")
	private Set<String> marketMediations = new HashSet<String>();

	private String typeOfAccessedObject;
	private String resourceOwner;
	private String serviceDurationMin;
	private String serviceDurationMax;
	private String consumerInvolvement;
	private String moneyFlow;
	private String offering;
	private String geographicScope;
	private String yearLaunch;
	private GeoUnit launchCountry = new GeoUnit();
	private GeoUnit launchCity = new GeoUnit();
	private GeoUnit residenceCountry = new GeoUnit();
	private GeoUnit residenceCity = new GeoUnit();

	@ElementCollection(fetch=FetchType.EAGER)
	@CollectionTable(joinColumns = @JoinColumn(name = "id"))
	@Column(name = "app")
	private Set<String> apps = new HashSet<String>();
	
	@ElementCollection(fetch=FetchType.EAGER)
	@CollectionTable(joinColumns = @JoinColumn(name = "id"))
	@Column(name = "language")
	private Set<String> languages = new HashSet<String>();
	
	@ElementCollection(fetch=FetchType.EAGER)
	@CollectionTable(joinColumns = @JoinColumn(name = "id"))
	@Column(name = "trustContribution")
	private Set<String> trustContributions = new HashSet<String>();

	public Platform() { //hibernate constructor
	}	
	

	public void set(String var, String val) {
		switch (var){
			case "resourceName":
				setResourceName(val);
				break;
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
			case "launchCountry":
				launchCountry.setResource(val);
				break;
			case "launchCountryGeonames":
				launchCountry.setGeonames(val);
				break;
			case "launchCountryName":
				launchCountry.setLabel(val);
				break;
			case "launchCity":
				launchCity.setResource(val);
				break;
			case "launchCityGeonames":
				launchCity.setGeonames(val);
				break;
			case "launchCityName":
				launchCity.setLabel(val);
				break;
			case "residenceCountry":
				residenceCountry.setResource(val);
				break;
			case "residenceCountryGeonames":
				residenceCountry.setGeonames(val);
				break;
			case "residenceCountryName":
				residenceCountry.setLabel(val);
				break;
			case "residenceCity":
				residenceCity.setResource(val);
				break;
			case "residenceCityGeonames":
				residenceCity.setGeonames(val);
				break;
			case "residenceCityName":
				residenceCity.setLabel(val);
				break;
			case "app":
				addApp(val);
				break;
			case "language":
				addLanguage(val);
				break;
			case "trustContribution":
				addTrustContribution(val);
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
	
	private void addTrustContribution(String val) {
		if(!trustContributions.contains(val))
			trustContributions.add(val);
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

	public Set<String> getResourceTypes() {
		return resourceTypes;
	}

	public void setResourceTypes(Set<String> resourceTypes) {
		this.resourceTypes = resourceTypes;
	}

	public Set<String> getConsumerisms() {
		return consumerisms;
	}

	public void setConsumerisms(Set<String> consumerisms) {
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

	public Set<String> getMarketMediations() {
		return marketMediations;
	}

	public void setMarketMediations(Set<String> marketMediations) {
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

	public GeoUnit getLaunchCountry() {
		return launchCountry;
	}

	public void setLaunchCountry(GeoUnit launchCountry) {
		this.launchCountry = launchCountry;
	}

	public GeoUnit getLaunchCity() {
		return launchCity;
	}

	public void setLaunchCity(GeoUnit launchCity) {
		this.launchCity = launchCity;
	}

	public GeoUnit getResidenceCountry() {
		return residenceCountry;
	}

	public void setResidenceCountry(GeoUnit residenceCountry) {
		this.residenceCountry = residenceCountry;
	}

	public GeoUnit getResidenceCity() {
		return residenceCity;
	}

	public void setResidenceCity(GeoUnit residenceCity) {
		this.residenceCity = residenceCity;
	}

	public Set<String> getApps() {
		return apps;
	}

	public void setApps(Set<String> apps) {
		this.apps = apps;
	}


	public Date getCreated() {
		return created;
	}


	public void setCreated(Date created) {
		this.created = created;
	}


	public Set<String> getLanguages() {
		return languages;
	}


	public void setLanguages(Set<String> languages) {
		this.languages = languages;
	}


	public String getResourceName() {
		return resourceName;
	}


	public void setResourceName(String resourceName) {
		this.resourceName = resourceName;
	}


	public Set<String> getTrustContributions() {
		return trustContributions;
	}


	public void setTrustContributions(Set<String> trustContributions) {
		this.trustContributions = trustContributions;
	}
}
