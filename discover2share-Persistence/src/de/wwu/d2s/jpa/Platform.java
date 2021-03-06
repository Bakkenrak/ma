package de.wwu.d2s.jpa;

import java.io.Serializable;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Serializable entity class that describes a P2P SCC Platform.
 */
@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
public class Platform implements Serializable {
	private static final long serialVersionUID = -6541452756921763514L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;

	private String externalId; // ID for lookup of suggestions that can be edited by non-users

	private Date created;

	private String editFor; // for edit suggestions: the ontology platform's URI

	private String resourceName; // the resource URI used for this platform in the ontology

	private String label;
	private String url;
	private String description;

	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	private Set<ResourceType> resourceTypes = new HashSet<ResourceType>();

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(joinColumns = @JoinColumn(name = "id"))
	@Column(name = "consumerism")
	private Set<String> consumerisms = new HashSet<String>();

	private String pattern;
	private String temporality;

	@ElementCollection(fetch = FetchType.EAGER)
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

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(joinColumns = @JoinColumn(name = "id"))
	@Column(name = "app")
	private Set<String> apps = new HashSet<String>();

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(joinColumns = @JoinColumn(name = "id"))
	@Column(name = "language")
	private Set<String> languages = new HashSet<String>();

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(joinColumns = @JoinColumn(name = "id"))
	@Column(name = "trustContribution")
	private Set<String> trustContributions = new HashSet<String>();

	public Platform() { // hibernate constructor
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
		case "launchCountryCode":
			launchCountry.setCountryCode(val);
			launchCity.setCountryCode(val);
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
		case "residenceCountryCode":
			residenceCountry.setCountryCode(val);
			residenceCity.setCountryCode(val);
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
		ResourceType rt = new ResourceType();
		rt.setLabel(val);
		if (!resourceTypes.contains(rt))
			resourceTypes.add(rt);
	}

	private void addConsumerism(String val) {
		if (!consumerisms.contains(val))
			consumerisms.add(val);
	}

	private void addMarketMediation(String val) {
		if (!marketMediations.contains(val))
			marketMediations.add(val);
	}

	private void addApp(String val) {
		if (!apps.contains(val))
			apps.add(val);
	}

	private void addLanguage(String val) {
		if (!languages.contains(val))
			languages.add(val);
	}

	private void addTrustContribution(String val) {
		if (!trustContributions.contains(val))
			trustContributions.add(val);
	}

	/**
	 * Executed only when the platform object is first persisted in the database.
	 */
	@PrePersist
	protected void onCreate() {
		created = new Date(); // current timestamp

		// generate a random string as an external ID
		SecureRandom random = new SecureRandom();
		externalId = new BigInteger(130, random).toString(32);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getExternalId() {
		return externalId;
	}

	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getEditFor() {
		return editFor;
	}

	public void setEditFor(String editFor) {
		this.editFor = editFor;
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

	public Set<ResourceType> getResourceTypes() {
		return resourceTypes;
	}

	public void setResourceTypes(Set<ResourceType> resourceTypes) {
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
		if (serviceDurationMin == null) {
			this.serviceDurationMin = null;
			return;
		}
		// if service duration is full OWL Time URL, transform
		if (serviceDurationMin.equals("http://www.w3.org/2006/time#unitMinute")) {
			this.serviceDurationMin = "Minutes";
		} else if (serviceDurationMin.equals("http://www.w3.org/2006/time#unitHour")) {
			this.serviceDurationMin = "Hours";
		} else if (serviceDurationMin.equals("http://www.w3.org/2006/time#unitDay")) {
			this.serviceDurationMin = "Days";
		} else if (serviceDurationMin.equals("http://www.w3.org/2006/time#unitWeek")) {
			this.serviceDurationMin = "Weeks";
		} else if (serviceDurationMin.equals("http://www.w3.org/2006/time#unitMonth")) {
			this.serviceDurationMin = "Months";
		} else {
			this.serviceDurationMin = serviceDurationMin;
		}
	}

	public String getServiceDurationMax() {
		return serviceDurationMax;
	}

	public void setServiceDurationMax(String serviceDurationMax) {
		if (serviceDurationMax == null) {
			this.serviceDurationMax = null;
			return;
		}
		
		if (serviceDurationMax.equals("http://www.w3.org/2006/time#unitMinute")) {
			this.serviceDurationMax = "Minutes";
		} else if (serviceDurationMax.equals("http://www.w3.org/2006/time#unitHour")) {
			this.serviceDurationMax = "Hours";
		} else if (serviceDurationMax.equals("http://www.w3.org/2006/time#unitDay")) {
			this.serviceDurationMax = "Days";
		} else if (serviceDurationMax.equals("http://www.w3.org/2006/time#unitWeek")) {
			this.serviceDurationMax = "Weeks";
		} else if (serviceDurationMax.equals("http://www.w3.org/2006/time#unitMonth")) {
			this.serviceDurationMax = "Months";
		} else {
			this.serviceDurationMax = serviceDurationMax;
		}
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
