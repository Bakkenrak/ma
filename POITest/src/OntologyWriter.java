import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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
	private Resource years;
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
		hasMarketMediation = ontologyModel.createProperty(D2S
				+ "has_market_mediation");
		accessedObjectHasType = ontologyModel.createProperty(D2S
				+ "accessed_object_has_type");
		hasResourceOwner = ontologyModel.createProperty(D2S
				+ "has_resource_owner");
		minServiceDuration = ontologyModel.createProperty(D2S
				+ "min_service_duration");
		maxServiceDuration = ontologyModel.createProperty(D2S
				+ "max_service_duration");
		hasConsumerInvolvement = ontologyModel.createProperty(D2S
				+ "has_consumer_involvement");
		hasMoneyFlow = ontologyModel.createProperty(D2S + "has_money_flow");
		hasMarketIntegration = ontologyModel.createProperty(D2S
				+ "has_market_integration");
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
		experientialObjectType = ontologyModel.createResource(D2S
				+ "Experiential");
		functionalObjectType = ontologyModel.createResource(D2S + "Functional");

		privateResourceOwner = ontologyModel.createResource(D2S + "Private");
		privateAndBusinessResourceOwner = ontologyModel.createResource(D2S
				+ "Private_and_business");

		minutes = ontologyModel.createResource(TIME + "unitMinute");
		hours = ontologyModel.createResource(TIME + "unitHour");
		days = ontologyModel.createResource(TIME + "unitDay");
		weeks = ontologyModel.createResource(TIME + "unitWeek");
		months = ontologyModel.createResource(TIME + "unitMonth");
		years = ontologyModel.createResource(TIME + "unitYear");
		
		fullService = ontologyModel.createResource(D2S + "Full-service");
		selfService = ontologyModel.createResource(D2S + "Self-service");
		inBetween = ontologyModel.createResource(D2S + "In-Between");

		c2c = ontologyModel.createResource(D2S + "C2C");
		c2b2c = ontologyModel.createResource(D2S + "C2B2C");
		free = ontologyModel.createResource(D2S + "Free");

		marketIntegrationClass = ontologyModel.createResource(D2S
				+ "Market_Integration");
		integrated = ontologyModel.createResource(D2S + "Integrated");
		separated = ontologyModel.createResource(D2S + "Separated");
		neighbourhoodWide = ontologyModel.createResource(D2S
				+ "Neighbourhood-wide");
		cityWide = ontologyModel.createResource(D2S + "City-wide");
		stateWide = ontologyModel.createResource(D2S + "State-wide");
		countryWide = ontologyModel.createResource(D2S + "Country-wide");
		global = ontologyModel.createResource(D2S + "Global");

		androidApp = ontologyModel.createResource(D2S + "Android_app");
		iOSApp = ontologyModel.createResource(D2S + "iOS_app");
		windowsPhoneApp = ontologyModel.createResource(D2S
				+ "Windows_Phone_app");
	}

	public void writeAll(List<ExcelPlatform> platforms, String outputFile) {
		int nrPlatforms = platforms.size();
		for (int i = 0; i < nrPlatforms; i++) {
			currentPlatform = platforms.get(i);

			log.info("Creating '" + currentPlatform.getName() + "' platform ("
					+ (i + 1) + "/" + nrPlatforms + ")");

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
		platformResource = ontologyModel.createResource(D2S + "platform_"
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
		if (currentPlatform.getResourceType().isEmpty())
			return;

		// Create new resource type instance
		Resource resourceType = ontologyModel.createResource(); // anonymous instance
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
			platformResource.addProperty(promotes, noConsumerism);
		} else {
			if (currentPlatform.getEconomical().equals("x"))
				platformResource.addProperty(promotes, economicConsumerism);
			if (currentPlatform.getEnvironmental().equals("x"))
				platformResource
						.addProperty(promotes, environmentalConsumerism);
			if (currentPlatform.getSocial().equals("x"))
				platformResource.addProperty(promotes, socialConsumerism);
		}

		// value testing
		if (!currentPlatform.getEconomical().isEmpty()
				&& !currentPlatform.getEconomical().equals("o")
				&& !currentPlatform.getEconomical().equals("x"))
			log.warn("Economical column not 'o'/'x'. Is: '"
					+ currentPlatform.getEconomical() + "'");
		if (!currentPlatform.getEnvironmental().isEmpty()
				&& !currentPlatform.getEnvironmental().equals("o")
				&& !currentPlatform.getEnvironmental().equals("x"))
			log.warn("Environmental column not 'o'/'x'. Is: '"
					+ currentPlatform.getEnvironmental() + "'");
		if (!currentPlatform.getSocial().isEmpty()
				&& !currentPlatform.getSocial().equals("o")
				&& !currentPlatform.getSocial().equals("x"))
			log.warn("Social column not 'o'/'x'. Is: '"
					+ currentPlatform.getSocial() + "'");
	}

	private void patternDimension() {
		if (currentPlatform.getDeferredP2PPattern().isEmpty())
			return;

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
		default:
			log.warn("P2P Pattern column not 'deferred'/'immediate'/'recurrent'. Is: '"
					+ currentPlatform.getDeferredP2PPattern() + "'");
		}
	}

	private void marketMediationDimension() {
		if (currentPlatform.getMarketMediation().isEmpty())
			return;

		switch (currentPlatform.getMarketMediation().toLowerCase()) {
		case "profit from both peer consumers and peer providers":
			platformResource.addProperty(hasMarketMediation, profitFromBoth);
			break;
		case "indirect profit":
			platformResource.addProperty(hasMarketMediation, indirectProfit);
			break;
		case "profit from peer consumers":
			platformResource.addProperty(hasMarketMediation,
					profitFromPeerConsumers);
			break;
		case "profit from peer providers":
			platformResource.addProperty(hasMarketMediation,
					profitFromPeerProviders);
			break;
		case "not-for-profit":
			// TODO
			break;
		default:
			log.warn("Market mediation column not 'profit from both peer consumers and peer providers'/'indirect profit'/'profit from peer consumers'/'profit from peer providers'. Is: '"
					+ currentPlatform.getMarketMediation() + "'");
		}
	}

	private void typeOfAccessedObjectDimension() {
		if (currentPlatform.getTypeOfAccessedObject().isEmpty())
			return;

		switch (currentPlatform.getTypeOfAccessedObject().toLowerCase()) {
		case "mixed":
			platformResource
					.addProperty(accessedObjectHasType, mixedObjectType);
			break;
		case "functional":
			platformResource.addProperty(accessedObjectHasType,
					functionalObjectType);
			break;
		case "experiential":
			platformResource.addProperty(accessedObjectHasType,
					experientialObjectType);
			break;
		default:
			log.warn("Type of acc. object column not 'mixed'/'functional'/'experiential'. Is: '"
					+ currentPlatform.getTypeOfAccessedObject() + "'");
		}
	}

	private void resourceOwnerDimension() {
		if (currentPlatform.getResourceOwner().isEmpty())
			return;

		switch (currentPlatform.getResourceOwner().toLowerCase()) {
		case "private":
			platformResource
					.addProperty(hasResourceOwner, privateResourceOwner);
			break;
		case "private and business":
			platformResource.addProperty(hasResourceOwner,
					privateAndBusinessResourceOwner);
			break;
		case "business":
			break;
		default:
			log.warn("Resource owner column not 'private'/'private and business'. Is: '"
					+ currentPlatform.getResourceOwner() + "'");
		}
	}

	private void serviceDurationDimension() {
		if (!currentPlatform.getServiceDurationMin().isEmpty()) {

			switch (currentPlatform.getServiceDurationMin().toLowerCase()) {
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
			case "years":
				platformResource.addProperty(minServiceDuration, years);
				break;
			default:
				log.warn("Service duration min column not 'minutes'/'hours'/'days'/'weeks'/'months'. Is: '"
						+ currentPlatform.getServiceDurationMin() + "'");
			}
		}

		if (!currentPlatform.getServiceDurationMax().isEmpty()) {

			switch (currentPlatform.getServiceDurationMax().toLowerCase()) {
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
			case "years":
				platformResource.addProperty(maxServiceDuration, years);
				break;
			default:
				log.warn("Service duration max column not 'minutes'/'hours'/'days'/'weeks'/'months'. Is: '"
						+ currentPlatform.getServiceDurationMax() + "'");
			}
		}
	}

	private void consumerInvolvementDimension() {
		if (currentPlatform.getConsumerInvolvement().isEmpty())
			return;

		switch (currentPlatform.getConsumerInvolvement().toLowerCase()) {
		case "full-service":
			platformResource.addProperty(hasConsumerInvolvement, fullService);
			break;
		case "self-service":
			platformResource.addProperty(hasConsumerInvolvement, selfService);
			break;
		case "in-between":
			platformResource.addProperty(hasConsumerInvolvement, inBetween);
			break;
		default:
			log.warn("Consumer involvement column not 'full-service'/'self-service'/'in-between'. Is: '"
					+ currentPlatform.getConsumerInvolvement() + "'");
		}
	}

	private void moneyFlowDimension() {
		if (currentPlatform.getMoneyFlow().isEmpty())
			return;

		switch (currentPlatform.getMoneyFlow().toLowerCase()) {
		case "c2c":
			platformResource.addProperty(hasMoneyFlow, c2c);
			break;
		case "c2b2c":
			platformResource.addProperty(hasMoneyFlow, c2b2c);
			break;
		case "free":
			platformResource.addProperty(hasMoneyFlow, free);
			break;
		case "c2b":
			// TODO
			break;
		default:
			log.warn("Money flow column not 'c2c'/'c2b2c'/'free'. Is: '"
					+ currentPlatform.getMoneyFlow() + "'");
		}
	}

	private void marketIntegrationDimension() {
		if (currentPlatform.getGlobalIntegration().isEmpty()
				&& currentPlatform.getGlobalIntegrationFinestLevel().isEmpty())
			return;

		Resource marketIntegration = ontologyModel.createResource(); // anonymous
																		// instance
		marketIntegration.addProperty(rdfType, marketIntegrationClass);
		platformResource.addProperty(hasMarketIntegration, marketIntegration);

		if (!currentPlatform.getGlobalIntegration().isEmpty()) {
			switch (currentPlatform.getGlobalIntegration().toLowerCase()) {
			case "integrated":
				marketIntegration.addProperty(marketsAre, integrated);
				break;
			case "separated communities":
			case "separated":
				marketIntegration.addProperty(marketsAre, separated);
				break;
			default:
				log.warn("Global integration column not 'integrated'/'separated'. Is: '"
						+ currentPlatform.getGlobalIntegration() + "'");
			}
		}

		if (!currentPlatform.getGlobalIntegrationFinestLevel().isEmpty()) {
			switch (currentPlatform.getGlobalIntegrationFinestLevel()
					.toLowerCase()) {
			case "neighborhood-wide":
			case "neighbourhood-wide":
				marketIntegration.addProperty(hasScope, neighbourhoodWide);
				break;
			case "city-wide":
				marketIntegration.addProperty(hasScope, cityWide);
				break;
			case "region-wide":
			case "state-wide":
				marketIntegration.addProperty(hasScope, stateWide);
				break;
			case "country-wide":
				marketIntegration.addProperty(hasScope, countryWide);
				break;
			case "global":
				marketIntegration.addProperty(hasScope, global);
				break;
			default:
				log.warn("Global integration finest level column not 'neighbourhood-wide'/'city-wide'/'state-wide'/'country-wide'/'global'. Is: '"
						+ currentPlatform.getGlobalIntegrationFinestLevel()
						+ "'");
			}
		}
	}

	private void launchYearDimension() {
		if (currentPlatform.getYearLaunch().isEmpty())
			return;

		try {
			int year = Integer.parseInt(currentPlatform.getYearLaunch());
			Resource launchYearResource = ontologyModel.createResource(DBPR
					+ year);
			platformResource.addProperty(launchYear, launchYearResource);
		} catch (NumberFormatException e) {
			log.warn("Year launch column not a proper number. Is: '"
					+ currentPlatform.getYearLaunch() + "'");
		}
	}

	private void launchCountryDimension() {
		if (currentPlatform.getLaunchCountry().isEmpty())
			return;

		Resource country = findCountry(currentPlatform.getLaunchCountry()
				.toUpperCase());
		if (country != null)
			platformResource.addProperty(launchedIn, country);
		else
			log.warn("Launch Country column is no resolvable country code. Is: '"
					+ currentPlatform.getLaunchCountry() + "'");
	}

	private void residenceCountryDimension() {
		if (currentPlatform.getResidenceCountry().isEmpty())
			return;

		Resource country = findCountry(currentPlatform.getResidenceCountry()
				.toUpperCase());
		if (country != null)
			platformResource.addProperty(location, country);
		else
			log.warn("Residence Country column is no resolvable country code. Is: '"
					+ currentPlatform.getResidenceCountry() + "'");
	}

	private Resource findCountry(String country) {
		if (country.isEmpty())
			return null;

		if (countryMap.containsKey(country)) {

			return countryMap.get(country);

		} else {
			String lgdEndpoint = "http://linkedgeodata.org/sparql";

			String sparqlQuery = "Prefix lgdo:<http://linkedgeodata.org/ontology/> "
					+ "Prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
					+ "Select distinct ?country {{"
					+ "	?country rdf:type lgdo:Country ."
					+ "	?country lgdo:country_code_iso3166_1_alpha_2 '"
					+ country
					+ "'"
					+ "} union {"
					+ "	?country rdf:type lgdo:Country ."
					+ "	?country lgdo:ISO3166-1 '" + country + "'" + "}"
					+ "union {"
					+ " ?country rdf:type lgdo:Country ."
					+ " ?country <http://linkedgeodata.org/ontology/name%3Aabbreviation> '" + country + "'"
					+ "}}";

			Query query = QueryFactory.create(sparqlQuery);
			QueryExecution qexec = QueryExecutionFactory.sparqlService(
					lgdEndpoint, query);
			ResultSet results = qexec.execSelect();

			Resource countryResource = null;

			if (results.hasNext()) {
				QuerySolution first = results.nextSolution();
				countryResource = ontologyModel.createResource(first
						.getResource("country").getURI());

				countryMap.put(country, countryResource);
			}

			qexec.close();
			return countryResource;
		}
	}

	private void smartphoneAppDimension() {
		if (currentPlatform.getAndroid().equals("x"))
			platformResource.addProperty(hasApp, androidApp);
		else if (!currentPlatform.getAndroid().equals("o")
				&& !currentPlatform.getAndroid().isEmpty())
			log.warn("Android column is not 'o'/'x'. Is: '"
					+ currentPlatform.getAndroid() + "'");

		if (currentPlatform.getiOS().equals("x"))
			platformResource.addProperty(hasApp, iOSApp);
		else if (!currentPlatform.getiOS().equals("o")
				&& !currentPlatform.getiOS().isEmpty())
			log.warn("iOS column is not 'o'/'x'. Is: '"
					+ currentPlatform.getiOS() + "'");

		if (currentPlatform.getWindowsPhone().equals("x"))
			platformResource.addProperty(hasApp, windowsPhoneApp);
		else if (!currentPlatform.getWindowsPhone().equals("o")
				&& !currentPlatform.getWindowsPhone().isEmpty())
			log.warn("Windows phone column is not 'o'/'x'. Is: '"
					+ currentPlatform.getWindowsPhone() + "'");
	}
}
