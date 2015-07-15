public class ExcelPlatform {

	private String classified;
	private String idNew;
	private String idOriginal;
	private String name;
	private String url;
	private String strippedUrl;
	private String resourceType;
	private String economical;
	private String environmental;
	private String social;

	private String comment1;

	private String deferredP2PPattern;
	private String marketMediation;

	private String comment2;

	private String typeOfAccessedObject;

	private String comment3;

	private String resourceOwner;
	private String serviceDurationMin;
	private String serviceDurationMax;
	private String consumerInvolvement;
	private String moneyFlow;
	private String globalIntegration;
	private String globalIntegrationFinestLevel;
	private String perTransaction;
	private String perListing;
	private String membershipFee;
	private String combination;

	private String comment4;

	private String responsible;
	private String yearLaunch;
	private String launchCountry;
	private String launchCity;
	private String residenceCountry;
	private String residenceCity;
	private String iOS;
	private String android;
	private String windowsPhone;

	public ExcelPlatform(String[] colValues) {
		int i = 0;
		setClassified(colValues[i++]);
		setIdNew(colValues[i++]);
		setIdOriginal(colValues[i++]);
		setName(colValues[i++]);
		setUrl(colValues[i++]);
		setStrippedUrl(colValues[i++]);
		setResourceType(colValues[i++]);
		setEconomical(colValues[i++]);
		setEnvironmental(colValues[i++]);
		setSocial(colValues[i++]);
		setComment1(colValues[i++]);
		setDeferredP2PPattern(colValues[i++]);
		setMarketMediation(colValues[i++]);
		setComment2(colValues[i++]);
		setTypeOfAccessedObject(colValues[i++]);
		setComment3(colValues[i++]);
		setResourceOwner(colValues[i++]);
		setServiceDurationMin(colValues[i++]);
		setServiceDurationMax(colValues[i++]);
		setConsumerInvolvement(colValues[i++]);
		setMoneyFlow(colValues[i++]);
		setGlobalIntegration(colValues[i++]);
		setGlobalIntegrationFinestLevel(colValues[i++]);
		setPerTransaction(colValues[i++]);
		setPerListing(colValues[i++]);
		setMembershipFee(colValues[i++]);
		setCombination(colValues[i++]);
		setComment4(colValues[i++]);
		setResponsible(colValues[i++]);
		setYearLaunch(colValues[i++]);
		setLaunchCountry(colValues[i++]);
		setLaunchCity(colValues[i++]);
		setResidenceCountry(colValues[i++]);
		setResidenceCity(colValues[i++]);
		setiOS(colValues[i++]);
		setAndroid(colValues[i++]);
		setWindowsPhone(colValues[i]);
	}

	/**
	 * Checks whether 0 or 1 were used instead of the standard o and x
	 * 
	 * @param value
	 * @return corrected value
	 */
	private String checkOX(String value) {
		if (value.equals("0"))
			return "o";
		if (value.equals("1"))
			return "x";
		return value;
	}

	public String getClassified() {
		return classified;
	}

	public void setClassified(String classified) {
		this.classified = classified;
	}

	public String getIdNew() {
		return idNew;
	}

	public void setIdNew(String idNew) {
		this.idNew = idNew;
	}

	public String getIdOriginal() {
		return idOriginal;
	}

	public void setIdOriginal(String idOriginal) {
		this.idOriginal = idOriginal;
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

	public String getStrippedUrl() {
		return strippedUrl;
	}

	public void setStrippedUrl(String strippedUrl) {
		this.strippedUrl = strippedUrl;
	}

	public String getResourceType() {
		return resourceType;
	}

	public void setResourceType(String resourceType) {
		this.resourceType = resourceType;
	}

	public String getEconomical() {
		return economical;
	}

	public void setEconomical(String economical) {
		this.economical = checkOX(economical);
	}

	public String getEnvironmental() {
		return environmental;
	}

	public void setEnvironmental(String environmental) {
		this.environmental = checkOX(environmental);
	}

	public String getSocial() {
		return social;
	}

	public void setSocial(String social) {
		this.social = checkOX(social);
	}

	public String getComment1() {
		return comment1;
	}

	public void setComment1(String comment1) {
		this.comment1 = comment1;
	}

	public String getDeferredP2PPattern() {
		return deferredP2PPattern;
	}

	public void setDeferredP2PPattern(String deferredP2PPattern) {
		this.deferredP2PPattern = deferredP2PPattern;
	}

	public String getMarketMediation() {
		return marketMediation;
	}

	public void setMarketMediation(String marketMediation) {
		this.marketMediation = marketMediation;
	}

	public String getComment2() {
		return comment2;
	}

	public void setComment2(String comment2) {
		this.comment2 = comment2;
	}

	public String getTypeOfAccessedObject() {
		return typeOfAccessedObject;
	}

	public void setTypeOfAccessedObject(String typeOfAccessedObject) {
		this.typeOfAccessedObject = typeOfAccessedObject;
	}

	public String getComment3() {
		return comment3;
	}

	public void setComment3(String comment3) {
		this.comment3 = comment3;
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

	public String getGlobalIntegration() {
		return globalIntegration;
	}

	public void setGlobalIntegration(String globalIntegration) {
		if (globalIntegration.equals("seperated"))
			this.globalIntegration = "separated";
		else
			this.globalIntegration = globalIntegration;
	}

	public String getGlobalIntegrationFinestLevel() {
		return globalIntegrationFinestLevel;
	}

	public void setGlobalIntegrationFinestLevel(
			String globalIntegrationFinestLevel) {
		this.globalIntegrationFinestLevel = globalIntegrationFinestLevel;
	}

	public String getPerTransaction() {
		return perTransaction;
	}

	public void setPerTransaction(String perTransaction) {
		this.perTransaction = checkOX(perTransaction);
	}

	public String getPerListing() {
		return perListing;
	}

	public void setPerListing(String perListing) {
		this.perListing = checkOX(perListing);
	}

	public String getMembershipFee() {
		return membershipFee;
	}

	public void setMembershipFee(String membershipFee) {
		this.membershipFee = checkOX(membershipFee);
	}

	public String getCombination() {
		return combination;
	}

	public void setCombination(String combination) {
		this.combination = checkOX(combination);
	}

	public String getComment4() {
		return comment4;
	}

	public void setComment4(String comment4) {
		this.comment4 = comment4;
	}

	public String getResponsible() {
		return responsible;
	}

	public void setResponsible(String responsible) {
		this.responsible = responsible;
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

	public String getiOS() {
		return iOS;
	}

	public void setiOS(String iOS) {
		this.iOS = checkOX(iOS);
	}

	public String getAndroid() {
		return android;
	}

	public void setAndroid(String android) {
		this.android = checkOX(android);
	}

	public String getWindowsPhone() {
		return windowsPhone;
	}

	public void setWindowsPhone(String windowsPhone) {
		this.windowsPhone = checkOX(windowsPhone);
	}

}
