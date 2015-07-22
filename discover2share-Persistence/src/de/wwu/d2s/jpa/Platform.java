package de.wwu.d2s.jpa;

import java.io.Serializable;
import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;

@Entity
public class Platform implements Serializable {
	private static final long serialVersionUID = -6541452756921763514L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;
	
	private String name;
	private String url;
	private String description;
	
	@ElementCollection
	@CollectionTable(joinColumns=@JoinColumn(name="id"))
	@Column(name="resourceType")
	private List<String> resourceTypes;
	
	@ElementCollection
	@CollectionTable(joinColumns=@JoinColumn(name="id"))
	@Column(name="consumerism")
	private List<String> consumerisms;
	
	private String pattern;
	private String temporality;
	
	@ElementCollection
	@CollectionTable(joinColumns=@JoinColumn(name="id"))
	@Column(name="marketMediation")
	private List<String> marketMediations;
	
	private String typeOfAccessedObject;
	private String resourceOwner;
	private String serviceDurationMin;
	private String serviceDurationMax;
	private String consumerInvolvement;
	
	@ElementCollection
	@CollectionTable(joinColumns=@JoinColumn(name="id"))
	@Column(name="moneyFlow")
	private List<String> moneyFlows;
	
	private String offering;
	private String geographicScope;
	private String yearLaunch;
	private String launchCountry;
	private String launchCity;
	private String residenceCountry;
	private String residenceCity;
	
	@ElementCollection
	@CollectionTable(joinColumns=@JoinColumn(name="id"))
	@Column(name="app")
	private List<String> apps;
	
	public Platform(){
		
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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
		marketMediations = marketMediations;
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

	public List<String> getMoneyFlows() {
		return moneyFlows;
	}

	public void setMoneyFlows(List<String> moneyFlows) {
		this.moneyFlows = moneyFlows;
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
}
