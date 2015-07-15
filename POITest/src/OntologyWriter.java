import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

public class OntologyWriter {
	
	private Logger log;

	private final String RDF = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	private final String RDFS = "http://www.w3.org/2000/01/rdf-schema#";
	private final String D2S = "http://www.discover2share.net/d2s-ont/";
	private final String DBPP = "http://dbpedia.org/property/";
	private final String DBPR = "http://dbpedia.org/resource/";
	private final String DBPO = "http://dbpedia.org/ontology/";
	private final String DCT = "http://purl.org/dc/terms/";
	private final String LGD = "http://linkedgeodata.org/ontology/";
	private final String SKOS = "http://www.w3.org/2004/02/skos/core#";
	private final String TIME = "http://www.w3.org/2006/time#";
	private final String WORDNET = "http://wordnet-rdf.princeton.edu/wn31/";

	private OntModel ontologyModel;

	private Resource platformResource;
	private ExcelPlatform currentPlatform;
	private Map<String, Resource> countryMap = new HashMap<String, Resource>();

	private Property rdfType;
	private Property rdfsLabel;
	private Property dbppUrl;
	private Property hasResourceType;
	private Property promotes;
	private Property hasPattern;
	private Property hasMarketMediation;
	private Property accessedObjectHasType;
	private Property hasResourceOwner;
	private Property minServiceDuration;
	private Property maxServiceDuration;
	private Property hasConsumerInvolvement;
	private Property hasMoneyFlow;
	private Property hasMarketIntegration;
	private Property marketsAre;
	private Property hasScope;
	private Property launchYear;
	private Property launchedIn;
	private Property location;
	private Property hasApp;

	private Resource resourceTypeClass;
	private Resource p2pSccPlatformClass;
	private Resource socialConsumerism;
	private Resource economicConsumerism;
	private Resource environmentalConsumerism;
	private Resource noConsumerism;
	private Resource deferredPattern;
	private Resource immediatePattern;
	private Resource recurrentPattern;
	private Resource indirectProfit;
	private Resource profitFromBoth;
	private Resource profitFromPeerConsumers;
	private Resource profitFromPeerProviders;
	private Resource mixedObjectType;
	private Resource experientialObjectType;
	private Resource functionalObjectType;
	private Resource privateResourceOwner;
	private Resource privateAndBusinessResourceOwner;
	private Resource minutes;
	private Resource hours;
	private Resource days;
	private Resource weeks;
	private Resource months;
	private Resource fullService;
	private Resource selfService;
	private Resource inBetween;
	private Resource c2c;
	private Resource c2b2c;
	private Resource free;
	private Resource marketIntegrationClass;
	private Resource integrated;
	private Resource separated;
	private Resource neighbourhoodWide;
	private Resource cityWide;
	private Resource stateWide;
	private Resource countryWide;
	private Resource global;
	private Resource androidApp;
	private Resource iOSApp;
	private Resource windowsPhoneApp;
	
	
	public OntologyWriter() {
		log = Logger.getLogger(this.getClass().getName());
		
		log.info("Creating ontology model and setting namespace prefixes.");
		ontologyModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
		// define namespace prefixes to be used in output
		ontologyModel.setNsPrefix("dbpp", DBPP);
		ontologyModel.setNsPrefix("dbpr", DBPR);
		ontologyModel.setNsPrefix("dbpo", DBPO);
		ontologyModel.setNsPrefix("dct", DCT);
		ontologyModel.setNsPrefix("lgd", LGD);
		ontologyModel.setNsPrefix("skos", SKOS);
		ontologyModel.setNsPrefix("time", TIME);
		ontologyModel.setNsPrefix("wordnet", WORDNET);

		log.info("Defining D2S properties.");
		// create properties once in the beginning to use for every platform
		rdfType = ontologyModel.createProperty(RDF + "type");
		rdfsLabel = ontologyModel.createProperty(RDFS + "label");
		dbppUrl = ontologyModel.createProperty(DBPP + "url");
		hasResourceType = ontologyModel.createProperty(D2S
				+ "has_resource_type");
		promotes = ontologyModel.createProperty(D2S + "promotes");
		hasPattern = ontologyModel.createProperty(D2S + "has_p2p_scc_pattern");
		hasMarketMediation = ontologyModel.createProperty(D2S + "has_market_mediation");
		accessedObjectHasType = ontologyModel.createProperty(D2S + "accessed_object_has_type");
		hasResourceOwner = ontologyModel.createProperty(D2S + "has_resource_owner");
		minServiceDuration = ontologyModel.createProperty(D2S + "min_service_duration");
		maxServiceDuration = ontologyModel.createProperty(D2S + "max_service_duration");
		hasConsumerInvolvement = ontologyModel.createProperty(D2S + "has_consumer_involvement");
		hasMoneyFlow = ontologyModel.createProperty(D2S + "has_money_flow");
		hasMarketIntegration = ontologyModel.createProperty(D2S + "has_market_integration");
		marketsAre = ontologyModel.createProperty(D2S + "markets_are");
		hasScope = ontologyModel.createProperty(D2S + "has_scope");
		launchYear = ontologyModel.createProperty(DBPP + "launchYear");
		launchedIn = ontologyModel.createProperty(D2S + "launched_in");
		location = ontologyModel.createProperty(DBPO + "location");
		hasApp = ontologyModel.createProperty(D2S + "has_app");
		
		log.info("Defining D2S classes/instances.");
		// create resources once in the beginning to use for every platform
		resourceTypeClass = ontologyModel.createResource(D2S + "Resource_Type");
		
		p2pSccPlatformClass = ontologyModel.createResource(D2S
				+ "P2P_SCC_Platform");
		
		socialConsumerism = ontologyModel.createResource(D2S + "Social");
		economicConsumerism = ontologyModel.createResource(D2S + "Economic");
		environmentalConsumerism = ontologyModel.createResource(D2S
				+ "Environmental");
		noConsumerism = ontologyModel.createResource(D2S + "None");
		
		deferredPattern = ontologyModel.createResource(D2S + "Deferred");
		immediatePattern = ontologyModel.createResource(D2S + "Immediate");
		recurrentPattern = ontologyModel.createResource(D2S + "Recurrent");
		
		indirectProfit = ontologyModel.createResource(D2S + "Indirect_Profit");
		profitFromBoth = ontologyModel.createResource(D2S + "Profit_from_both");
		profitFromPeerConsumers = ontologyModel.createResource(D2S
				+ "Profit_from_peer_consumers");
		profitFromPeerProviders = ontologyModel.createResource(D2S
				+ "Profit_from_peer_providers");
		
		mixedObjectType = ontologyModel.createResource(D2S + "Mixed");
		experientialObjectType = ontologyModel.createResource(D2S + "Experiential");
		functionalObjectType = ontologyModel.createResource(D2S + "Functional");
		
		privateResourceOwner = ontologyModel.createResource(D2S + "Private");
		privateAndBusinessResourceOwner = ontologyModel.createResource(D2S + "Private_and_business");
		
		minutes = ontologyModel.createResource(TIME + "unitMinute");
		hours = ontologyModel.createResource(TIME + "unitHour");
		days = ontologyModel.createResource(TIME + "unitDay");
		weeks = ontologyModel.createResource(TIME + "unitWeek");
		months = ontologyModel.createResource(TIME + "unitMonth");
		
		fullService = ontologyModel.createResource(D2S + "Full-service");
		selfService = ontologyModel.createResource(D2S + "Self-service");
		inBetween = ontologyModel.createResource(D2S + "In-Between");
		
		c2c = ontologyModel.createResource(D2S + "C2C");
		c2b2c = ontologyModel.createResource(D2S + "C2B2C");
		free = ontologyModel.createResource(D2S + "Free");
		
		marketIntegrationClass = ontologyModel.createResource(D2S + "Market_Integration");
		integrated = ontologyModel.createResource(D2S + "Integrated");
		separated = ontologyModel.createResource(D2S + "Separated");
		neighbourhoodWide = ontologyModel.createResource(D2S + "Neighbourhood-wide");
		cityWide = ontologyModel.createResource(D2S + "City-wide");
		stateWide = ontologyModel.createResource(D2S + "State-wide");
		countryWide = ontologyModel.createResource(D2S + "Country-wide");
		global = ontologyModel.createResource(D2S + "Global");
		
		androidApp = ontologyModel.createResource(D2S + "Android_app");
		iOSApp = ontologyModel.createResource(D2S + "iOS_app");
		windowsPhoneApp = ontologyModel.createResource(D2S + "Windows_Phone_app");
	}

	public void writeAll(List<ExcelPlatform> platforms, String outputFile) {
		int nrPlatforms = platforms.size();
		for (int i=0; i < nrPlatforms; i++) {
			currentPlatform = platforms.get(i);
			
			log.info("Creating '" + currentPlatform.getName() + "' platform (" + (i+1) + "/" + nrPlatforms + ")");
			
			constructPlatform();
		}

		try (OutputStream out = new FileOutputStream(outputFile)) {
			ontologyModel.write(out, "RDF/XML"); // "RDF/XML"
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void constructPlatform() {
		initializePlatform();

		resourceTypeDimension();
		sustainableConsumerismDimension();
		patternDimension();
		marketMediationDimension();
		typeOfAccessedObjectDimension();
		resourceOwnerDimension();
		serviceDurationDimension();
		consumerInvolvementDimension();
		moneyFlowDimension();
		marketIntegrationDimension();
		launchYearDimension();
		launchCountryDimension();
		residenceCountryDimension();
		smartphoneAppDimension();
	}


	private void initializePlatform() {
		// Create new platform instance
		platformResource = ontologyModel.createResource(D2S
				+ currentPlatform.getIdNew());
		// Set type
		platformResource.addProperty(rdfType, p2pSccPlatformClass);
		// add rdfs:label property
		platformResource.addProperty(rdfsLabel, currentPlatform.getName(),
				XSDDatatype.XSDstring);
		// add dbpp:url property
		platformResource.addProperty(dbppUrl, currentPlatform.getStrippedUrl(),
				XSDDatatype.XSDanyURI);
	}

	// TODO
	private void resourceTypeDimension() {
		// Create new resource type instance
		Resource resourceType = ontologyModel.createResource(); //anonymous instance
		// Set type
		resourceType.addProperty(rdfType, resourceTypeClass);
		// add rdfs:label property
		resourceType.addProperty(rdfsLabel, currentPlatform.getResourceType(),
				XSDDatatype.XSDstring);
		// add d2s:has_resource_type
		platformResource.addProperty(hasResourceType, resourceType);
	}

	private void sustainableConsumerismDimension() {
		if (currentPlatform.getEconomical().equals("o")
				&& currentPlatform.getEnvironmental().equals("o")
				&& currentPlatform.getSocial().equals("o")) {
			platformResource.addProperty(promotes,
					noConsumerism);
		} else {
			if (currentPlatform.getEconomical().equals("x"))
				platformResource.addProperty(promotes,
						economicConsumerism);
			if (currentPlatform.getEnvironmental().equals("x"))
				platformResource.addProperty(promotes,
						environmentalConsumerism);
			if (currentPlatform.getSocial().equals("x"))
				platformResource.addProperty(promotes,
						socialConsumerism);
		}
	}

	private void patternDimension() {
		switch (currentPlatform.getDeferredP2PPattern().toLowerCase()) {
		case "deferred":
			platformResource.addProperty(hasPattern, deferredPattern);
			break;
		case "immediate":
			platformResource.addProperty(hasPattern, immediatePattern);
			break;
		case "recurrent":
			platformResource.addProperty(hasPattern, recurrentPattern);
			break;
		}
	}

	private void marketMediationDimension() {
		switch (currentPlatform.getMarketMediation().toLowerCase()) {
		case "profit from both peer consumers and peer providers":
			platformResource.addProperty(hasMarketMediation, profitFromBoth);
			break;
		case "indirect profit":
			platformResource.addProperty(hasMarketMediation, indirectProfit);
			break;
		case "profit from peer consumers":
			platformResource.addProperty(hasMarketMediation, profitFromPeerConsumers);
			break;
		case "profit from peer providers":
			platformResource.addProperty(hasMarketMediation, profitFromPeerProviders);
			break;
		}
	}
	
	private void typeOfAccessedObjectDimension(){
		switch(currentPlatform.getTypeOfAccessedObject().toLowerCase()){
		case "mixed":
			platformResource.addProperty(accessedObjectHasType, mixedObjectType);
			break;
		case "functional":
			platformResource.addProperty(accessedObjectHasType, functionalObjectType);
			break;
		case "experiential":
			platformResource.addProperty(accessedObjectHasType, experientialObjectType);
			break;
		}
	}
	
	private void resourceOwnerDimension(){
		switch(currentPlatform.getResourceOwner().toLowerCase()){
		case "private":
			platformResource.addProperty(hasResourceOwner,privateResourceOwner);
			break;
		case "private and business":
			platformResource.addProperty(hasResourceOwner,privateAndBusinessResourceOwner);
			break;
		}
	}
	
	private void serviceDurationDimension(){
		switch(currentPlatform.getServiceDurationMin().toLowerCase()){
		case "minutes":
			platformResource.addProperty(minServiceDuration, minutes);
			break;
		case "hours":
			platformResource.addProperty(minServiceDuration, hours);
			break;
		case "days":
			platformResource.addProperty(minServiceDuration, days);
			break;
		case "weeks":
			platformResource.addProperty(minServiceDuration, weeks);
			break;
		case "months":
			platformResource.addProperty(minServiceDuration, months);
			break;
		}
		
		switch(currentPlatform.getServiceDurationMax().toLowerCase()){
		case "minutes":
			platformResource.addProperty(maxServiceDuration, minutes);
			break;
		case "hours":
			platformResource.addProperty(maxServiceDuration, hours);
			break;
		case "days":
			platformResource.addProperty(maxServiceDuration, days);
			break;
		case "weeks":
			platformResource.addProperty(maxServiceDuration, weeks);
			break;
		case "months":
			platformResource.addProperty(maxServiceDuration, months);
			break;
		}
	}
	
	private void consumerInvolvementDimension(){
		switch(currentPlatform.getConsumerInvolvement().toLowerCase()){
		case "full-service":
			platformResource.addProperty(hasConsumerInvolvement, fullService);
			break;
		case "self-service":
			platformResource.addProperty(hasConsumerInvolvement, selfService);
			break;
		case "in-between":
			platformResource.addProperty(hasConsumerInvolvement, inBetween);
			break;
		}
	}
	
	private void moneyFlowDimension(){
		switch(currentPlatform.getMoneyFlow().toLowerCase()){
		case "c2c":
			platformResource.addProperty(hasMoneyFlow, c2c);
			break;
		case "c2b2c":
			platformResource.addProperty(hasMoneyFlow, c2b2c);
			break;
		case "free":
			platformResource.addProperty(hasMoneyFlow, free);
			break;
		}
	}
	
	private void marketIntegrationDimension(){
		Resource marketIntegration = ontologyModel.createResource(); //anonymous instance
		marketIntegration.addProperty(rdfType, marketIntegrationClass);
		
		switch(currentPlatform.getGlobalIntegration().toLowerCase()){
		case "integrated":
			marketIntegration.addProperty(marketsAre, integrated);
			break;
		case "separated":
			marketIntegration.addProperty(marketsAre, separated);
			break;
		}
		
		switch(currentPlatform.getGlobalIntegrationFinestLevel().toLowerCase()){
		case "neighbourhood-wide":
			marketIntegration.addProperty(hasScope, neighbourhoodWide);
			break;
		case "city-wide":
			marketIntegration.addProperty(hasScope, cityWide);
			break;
		case "state-wide":
			marketIntegration.addProperty(hasScope, stateWide);
			break;
		case "country-wide":
			marketIntegration.addProperty(hasScope, countryWide);
			break;
		case "global":
			marketIntegration.addProperty(hasScope, global);
			break;
		}
		
		platformResource.addProperty(hasMarketIntegration, marketIntegration);
	}
	
	private void launchYearDimension(){
		try{
			int year = Integer.parseInt(currentPlatform.getYearLaunch());
			Resource launchYearResource = ontologyModel.createResource(DBPR + year);
			platformResource.addProperty(launchYear, launchYearResource);
		}catch(NumberFormatException e){
			e.printStackTrace();
		}
	}

	private void launchCountryDimension() {		
		Resource country = findCountry(currentPlatform.getLaunchCountry().toUpperCase());
		if(country != null)
			platformResource.addProperty(launchedIn, country);
	}
	
	private void residenceCountryDimension() {		
		Resource country = findCountry(currentPlatform.getResidenceCountry().toUpperCase());
		if(country != null)
			platformResource.addProperty(location, country);
	}
	
	private Resource findCountry(String country) {
		if(country.isEmpty()) return null;
		
		if(countryMap.containsKey(country)) {
			
			return countryMap.get(country);
			
		} else {
			String lgdEndpoint = "http://linkedgeodata.org/sparql";
			
			String sparqlQuery = "Prefix lgdo:<http://linkedgeodata.org/ontology/> "
					+ "Prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
					+ "Select ?country {"
					+ "	?country rdf:type lgdo:Country ."
					+ "	?country lgdo:country_code_iso3166_1_alpha_2 '" + country + "'"
					+ "}";
	
			Query query = QueryFactory.create(sparqlQuery);
			QueryExecution qexec = QueryExecutionFactory.sparqlService(lgdEndpoint, query);
			ResultSet results = qexec.execSelect();
			
			Resource countryResource = null;
			
			if(results.hasNext()){
				QuerySolution first = results.nextSolution();
				countryResource = ontologyModel.createResource(first.getResource("country").getURI());
				
				countryMap.put(country, countryResource);
			}
			
			qexec.close();
			return countryResource;
		}
	}
	
	private void smartphoneAppDimension() {
		
		if (currentPlatform.getAndroid().equals("x"))
			platformResource.addProperty(hasApp,
					androidApp);
		if (currentPlatform.getiOS().equals("x"))
			platformResource.addProperty(hasApp,
					iOSApp);
		if (currentPlatform.getWindowsPhone().equals("x"))
			platformResource.addProperty(hasApp,
					windowsPhoneApp);
		
	}
}
