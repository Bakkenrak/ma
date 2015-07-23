import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein;
import util.Pair;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

public class OntologyWriter {

	private Logger log;
	private Levenshtein levenshtein = new Levenshtein();

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
	private Map<String, Resource> countryResources = new HashMap<String, Resource>();
	private Map<String, Resource> countryResourcesFullName = new HashMap<String, Resource>();
	private Map<String, Pair<Resource, Resource>> cityResources = new HashMap<String, Pair<Resource, Resource>>();
	private Map<String, Map<String, String>> countryIndex = new HashMap<String, Map<String, String>>();

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
	private Property locationCity;
	private Property locationCountry;
	private Property hasApp;
	private Property usedIn;
	private Property date;
	private Property percentage;
	private Property skosRelatedMatch;

	private Resource cityClass;
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
	private Resource perTransaction;
	private Resource perListing;
	private Resource membershipFee;
	private Resource mixedObjectType;
	private Resource experientialObjectType;
	private Resource functionalObjectType;
	private Resource privateResourceOwner;
	private Resource businessResourceOwner;
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
	private Resource regionWide;
	private Resource global;
	private Resource androidApp;
	private Resource iOSApp;
	private Resource windowsPhoneApp;

	public OntologyWriter() {
		log = Logger.getLogger(this.getClass().getName());

		log.info("Build country index");
		buildCountryIndex();

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
		hasResourceType = ontologyModel.createProperty(D2S + "has_resource_type");
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
		locationCity = ontologyModel.createProperty(DBPP + "locationCity");
		locationCountry = ontologyModel.createProperty(DBPP + "locationCountry");
		hasApp = ontologyModel.createProperty(D2S + "has_app");
		usedIn = ontologyModel.createProperty(D2S + "used_in");
		date = ontologyModel.createProperty(DCT + "date");
		percentage = ontologyModel.createProperty(D2S + "user_percentage");
		skosRelatedMatch = ontologyModel.createProperty(SKOS + "relatedMatch");

		log.info("Defining D2S classes/instances.");
		// create resources once in the beginning to use for every platform
		cityClass = ontologyModel.createResource(D2S + "City");

		resourceTypeClass = ontologyModel.createResource(D2S + "Resource_Type");

		p2pSccPlatformClass = ontologyModel.createResource(D2S + "P2P_SCC_Platform");

		socialConsumerism = ontologyModel.createResource(D2S + "Social");
		economicConsumerism = ontologyModel.createResource(D2S + "Economic");
		environmentalConsumerism = ontologyModel.createResource(D2S + "Environmental");
		noConsumerism = ontologyModel.createResource(D2S + "None");

		deferredPattern = ontologyModel.createResource(D2S + "Deferred");
		immediatePattern = ontologyModel.createResource(D2S + "Immediate");
		recurrentPattern = ontologyModel.createResource(D2S + "Recurrent");

		indirectProfit = ontologyModel.createResource(D2S + "Indirect_Profit");
		profitFromBoth = ontologyModel.createResource(D2S + "Profit_from_both");
		profitFromPeerConsumers = ontologyModel.createResource(D2S + "Profit_from_peer_consumers");
		profitFromPeerProviders = ontologyModel.createResource(D2S + "Profit_from_peer_providers");
		perTransaction = ontologyModel.createResource(D2S + "Per_transaction");
		perListing = ontologyModel.createResource(D2S + "Per_listing");
		membershipFee = ontologyModel.createResource(D2S + "Membership_fee");

		mixedObjectType = ontologyModel.createResource(D2S + "Mixed");
		experientialObjectType = ontologyModel.createResource(D2S + "Experiential");
		functionalObjectType = ontologyModel.createResource(D2S + "Functional");

		privateResourceOwner = ontologyModel.createResource(D2S + "Private");
		businessResourceOwner = ontologyModel.createResource(D2S + "Business");
		privateAndBusinessResourceOwner = ontologyModel.createResource(D2S + "Private_and_business");

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

		marketIntegrationClass = ontologyModel.createResource(D2S + "Market_Integration");
		integrated = ontologyModel.createResource(D2S + "Integrated");
		separated = ontologyModel.createResource(D2S + "Separated");
		neighbourhoodWide = ontologyModel.createResource(D2S + "Neighbourhood-wide");
		cityWide = ontologyModel.createResource(D2S + "City-wide");
		stateWide = ontologyModel.createResource(D2S + "State-wide");
		countryWide = ontologyModel.createResource(D2S + "Country-wide");
		regionWide = ontologyModel.createResource(D2S + "Region-wide");
		global = ontologyModel.createResource(D2S + "Global");

		androidApp = ontologyModel.createResource(D2S + "Android_app");
		iOSApp = ontologyModel.createResource(D2S + "iOS_app");
		windowsPhoneApp = ontologyModel.createResource(D2S + "Windows_Phone_app");
	}

	private void buildCountryIndex() {
		JSONObject json;
		try {
			json = JsonReader.readJsonFromUrl("http://localhost:8080/discover2share-Web/resources/js/countries.json");
			JSONArray a = json.getJSONArray("countries");
			for (int i = 0; i < a.length(); i++) {
				JSONObject o = a.getJSONObject(i);
				Map<String, String> map = new HashMap<String, String>();
				map.put("countryId", o.getString("countryId"));
				map.put("countryName", o.getString("countryName"));
				map.put("countryCode", o.getString("countryCode"));
				map.put("resourceName",
						o.getString("countryName").replace(" ", "_").replace("[", "%5B").replace("]", "%5D"));
				countryIndex.put(o.getString("countryCode"), map);
			}
		} catch (IOException | JSONException e) {
			e.printStackTrace();
		}
	}

	public void writeAll(List<ExcelPlatform> platforms, String outputFile) {
		int nrPlatforms = platforms.size();
		for (int i = 0; i < nrPlatforms; i++) {
			currentPlatform = platforms.get(i);

			log.info("Creating '" + currentPlatform.getName() + "' platform (" + (i + 1) + "/" + nrPlatforms + ")");
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
		patternDimension(currentPlatform.getDeferredP2PPattern().toLowerCase());
		marketMediationDimension(currentPlatform.getMarketMediation().toLowerCase());
		marketMediationDimension2();
		typeOfAccessedObjectDimension(currentPlatform.getTypeOfAccessedObject().toLowerCase());
		resourceOwnerDimension(currentPlatform.getResourceOwner().toLowerCase());
		minServiceDurationDimension(currentPlatform.getServiceDurationMin().toLowerCase());
		maxServiceDurationDimension(currentPlatform.getServiceDurationMax().toLowerCase());
		consumerInvolvementDimension(currentPlatform.getConsumerInvolvement().toLowerCase());
		moneyFlowDimension(currentPlatform.getMoneyFlow().toLowerCase());
		marketIntegrationDimension();
		launchYearDimension();
		locationDimension(currentPlatform.getLaunchCity(), currentPlatform.getLaunchCountry().toUpperCase(), launchedIn);
		locationDimension(currentPlatform.getResidenceCity(), currentPlatform.getResidenceCountry().toUpperCase(),
				location);
		smartphoneAppDimension();
		userDistributionDimension();
	}

	private void initializePlatform() {
		// Create new platform instance
		platformResource = ontologyModel.createResource(D2S + "platform_" + currentPlatform.getIdNew());
		// Set type
		platformResource.addProperty(rdfType, p2pSccPlatformClass);
		// add rdfs:label property
		platformResource.addProperty(rdfsLabel, currentPlatform.getName());
		// add dbpp:url property
		platformResource.addProperty(dbppUrl, currentPlatform.getStrippedUrl(), XSDDatatype.XSDanyURI);
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
		resourceType.addProperty(rdfsLabel, currentPlatform.getResourceType());
		// add d2s:has_resource_type
		platformResource.addProperty(hasResourceType, resourceType);
	}

	private void sustainableConsumerismDimension() {
		if (currentPlatform.getEconomical().equals("o") && currentPlatform.getEnvironmental().equals("o")
				&& currentPlatform.getSocial().equals("o")) {
			platformResource.addProperty(promotes, noConsumerism);
		} else {
			if (currentPlatform.getEconomical().equals("x"))
				platformResource.addProperty(promotes, economicConsumerism);
			if (currentPlatform.getEnvironmental().equals("x"))
				platformResource.addProperty(promotes, environmentalConsumerism);
			if (currentPlatform.getSocial().equals("x"))
				platformResource.addProperty(promotes, socialConsumerism);
		}

		// value testing
		if (!currentPlatform.getEconomical().isEmpty() && !currentPlatform.getEconomical().equals("o")
				&& !currentPlatform.getEconomical().equals("x"))
			log.warn("Economical column not 'o'/'x'. Is: '" + currentPlatform.getEconomical() + "'");
		if (!currentPlatform.getEnvironmental().isEmpty() && !currentPlatform.getEnvironmental().equals("o")
				&& !currentPlatform.getEnvironmental().equals("x"))
			log.warn("Environmental column not 'o'/'x'. Is: '" + currentPlatform.getEnvironmental() + "'");
		if (!currentPlatform.getSocial().isEmpty() && !currentPlatform.getSocial().equals("o")
				&& !currentPlatform.getSocial().equals("x"))
			log.warn("Social column not 'o'/'x'. Is: '" + currentPlatform.getSocial() + "'");
	}

	private String[] patternValues = { "deferred", "immediate", "recurrent" };

	private void patternDimension(String value) {
		if (value.isEmpty())
			return;

		if (value.equals(patternValues[0])) {
			Resource pattern = ontologyModel.createResource();
			pattern.addProperty(rdfType, deferredPattern);
			platformResource.addProperty(hasPattern, pattern);
		} else if (value.equals(patternValues[1])) {
			Resource pattern = ontologyModel.createResource();
			pattern.addProperty(rdfType, immediatePattern);
			platformResource.addProperty(hasPattern, pattern);
		} else if (value.equals(patternValues[2])) {
			Resource pattern = ontologyModel.createResource();
			pattern.addProperty(rdfType, recurrentPattern);
			platformResource.addProperty(hasPattern, pattern);
		} else {
			float maxSimilarity = 0;
			String maxSimValue = "";

			for (String val : patternValues) {
				float sim = levenshtein.getSimilarity(value, val);
				if (sim > maxSimilarity) {
					maxSimilarity = sim;
					maxSimValue = val;
				}
			}

			if (maxSimilarity > 0.7)
				patternDimension(maxSimValue);
			else
				log.warn("P2P Pattern column not 'deferred'/'immediate'/'recurrent'. Is: '" + value + "'");
		}
	}

	private String[] mediationValues = { "profit from both", "profit from both peer consumers and peer providers",
			"indirect profit", "profit from peer consumers", "profit from peer providers" };

	private void marketMediationDimension(String value) {
		if (value.isEmpty())
			return;

		if (value.equals(mediationValues[0]) || value.equals(mediationValues[1]))
			platformResource.addProperty(hasMarketMediation, profitFromBoth);
		else if (value.equals(mediationValues[2]))
			platformResource.addProperty(hasMarketMediation, indirectProfit);
		else if (value.equals(mediationValues[3]))
			platformResource.addProperty(hasMarketMediation, profitFromPeerConsumers);
		else if (value.equals(mediationValues[4]))
			platformResource.addProperty(hasMarketMediation, profitFromPeerProviders);
		else {
			float maxSimilarity = 0;
			String maxSimValue = "";

			for (String val : mediationValues) {
				float sim = levenshtein.getSimilarity(value, val);
				if (sim > maxSimilarity) {
					maxSimilarity = sim;
					maxSimValue = val;
				}
			}

			if (maxSimilarity > 0.7)
				marketMediationDimension(maxSimValue);
			else
				log.warn("Market mediation column not 'profit from both peer consumers and peer providers'/'indirect profit'/'profit from peer consumers'/'profit from peer providers'. Is: '"
						+ value + "'");
		}
	}

	private void marketMediationDimension2() {
		if (currentPlatform.getPerTransaction().equals("x"))
			platformResource.addProperty(hasMarketMediation, perTransaction);
		else if (!currentPlatform.getPerTransaction().equals("o") && !currentPlatform.getPerTransaction().isEmpty())
			log.warn("Per transaction is not 'o'/'x'. Is: '" + currentPlatform.getPerTransaction() + "'");

		if (currentPlatform.getPerListing().equals("x"))
			platformResource.addProperty(hasMarketMediation, perListing);
		else if (!currentPlatform.getPerListing().equals("o") && !currentPlatform.getPerListing().isEmpty())
			log.warn("Per listing is not 'o'/'x'. Is: '" + currentPlatform.getPerListing() + "'");

		if (currentPlatform.getMembershipFee().equals("x"))
			platformResource.addProperty(hasMarketMediation, membershipFee);
		else if (!currentPlatform.getMembershipFee().equals("o") && !currentPlatform.getMembershipFee().isEmpty())
			log.warn("Membership fee is not 'o'/'x'. Is: '" + currentPlatform.getMembershipFee() + "'");
	}

	private String[] objectTypeValues = { "mixed", "functional", "experiential" };

	private void typeOfAccessedObjectDimension(String value) {
		if (value.isEmpty())
			return;

		if (value.equals(objectTypeValues[0]))
			platformResource.addProperty(accessedObjectHasType, mixedObjectType);
		else if (value.equals(objectTypeValues[1]))
			platformResource.addProperty(accessedObjectHasType, functionalObjectType);
		else if (value.equals(objectTypeValues[2]))
			platformResource.addProperty(accessedObjectHasType, experientialObjectType);
		else {
			float maxSimilarity = 0;
			String maxSimValue = "";

			for (String val : objectTypeValues) {
				float sim = levenshtein.getSimilarity(value, val);
				if (sim > maxSimilarity) {
					maxSimilarity = sim;
					maxSimValue = val;
				}
			}

			if (maxSimilarity > 0.7)
				typeOfAccessedObjectDimension(maxSimValue);
			else
				log.warn("Type of acc. object column not 'mixed'/'functional'/'experiential'. Is: '" + value + "'");
		}
	}

	private String[] resourceOwnerValues = { "private", "private and business", "business" };

	private void resourceOwnerDimension(String value) {
		if (value.isEmpty())
			return;

		if (value.equals(resourceOwnerValues[0]))
			platformResource.addProperty(hasResourceOwner, privateResourceOwner);
		else if (value.equals(resourceOwnerValues[1]))
			platformResource.addProperty(hasResourceOwner, privateAndBusinessResourceOwner);
		else if (value.equals(resourceOwnerValues[2])) {
			platformResource.addProperty(hasResourceOwner, businessResourceOwner);
		} else {
			float maxSimilarity = 0;
			String maxSimValue = "";

			for (String val : resourceOwnerValues) {
				float sim = levenshtein.getSimilarity(value, val);
				if (sim > maxSimilarity) {
					maxSimilarity = sim;
					maxSimValue = val;
				}
			}

			if (maxSimilarity > 0.7)
				resourceOwnerDimension(maxSimValue);
			else
				log.warn("Resource owner column not 'private'/'private and business'. Is: '" + value + "'");

		}
	}

	private String[] serviceDurationValues = { "minutes", "hours", "days", "weeks", "months", "years" };

	private void minServiceDurationDimension(String value) {
		if (value.isEmpty())
			return;

		if (value.equals(serviceDurationValues[0]))
			platformResource.addProperty(minServiceDuration, minutes);
		else if (value.equals(serviceDurationValues[1]))
			platformResource.addProperty(minServiceDuration, hours);
		else if (value.equals(serviceDurationValues[2]))
			platformResource.addProperty(minServiceDuration, days);
		else if (value.equals(serviceDurationValues[3]))
			platformResource.addProperty(minServiceDuration, weeks);
		else if (value.equals(serviceDurationValues[4]))
			platformResource.addProperty(minServiceDuration, months);
		else if (value.equals(serviceDurationValues[5]))
			platformResource.addProperty(minServiceDuration, years);
		else {
			float maxSimilarity = 0;
			String maxSimValue = "";

			for (String val : serviceDurationValues) {
				float sim = levenshtein.getSimilarity(value, val);
				if (sim > maxSimilarity) {
					maxSimilarity = sim;
					maxSimValue = val;
				}
			}

			if (maxSimilarity > 0.7)
				minServiceDurationDimension(maxSimValue);
			else
				log.warn("Service duration min column not 'minutes'/'hours'/'days'/'weeks'/'months'. Is: '" + value
						+ "'");
		}

	}

	private void maxServiceDurationDimension(String value) {
		if (value.isEmpty())
			return;

		if (value.equals(serviceDurationValues[0]))
			platformResource.addProperty(maxServiceDuration, minutes);
		else if (value.equals(serviceDurationValues[1]))
			platformResource.addProperty(maxServiceDuration, hours);
		else if (value.equals(serviceDurationValues[2]))
			platformResource.addProperty(maxServiceDuration, days);
		else if (value.equals(serviceDurationValues[3]))
			platformResource.addProperty(maxServiceDuration, weeks);
		else if (value.equals(serviceDurationValues[4]))
			platformResource.addProperty(maxServiceDuration, months);
		else if (value.equals(serviceDurationValues[5]))
			platformResource.addProperty(maxServiceDuration, years);
		else {
			float maxSimilarity = 0;
			String maxSimValue = "";

			for (String val : serviceDurationValues) {
				float sim = levenshtein.getSimilarity(value, val);
				if (sim > maxSimilarity) {
					maxSimilarity = sim;
					maxSimValue = val;
				}
			}

			if (maxSimilarity > 0.7)
				maxServiceDurationDimension(maxSimValue);
			else
				log.warn("Service duration max column not 'minutes'/'hours'/'days'/'weeks'/'months'. Is: '" + value
						+ "'");
		}

	}

	private String[] consumerInvolvementValues = { "full-service", "self-service", "in-between" };

	private void consumerInvolvementDimension(String value) {
		if (value.isEmpty())
			return;

		if (value.equals(consumerInvolvementValues[0]))
			platformResource.addProperty(hasConsumerInvolvement, fullService);
		else if (value.equals(consumerInvolvementValues[1]))
			platformResource.addProperty(hasConsumerInvolvement, selfService);
		else if (value.equals(consumerInvolvementValues[2]))
			platformResource.addProperty(hasConsumerInvolvement, inBetween);
		else {
			float maxSimilarity = 0;
			String maxSimValue = "";

			for (String val : consumerInvolvementValues) {
				float sim = levenshtein.getSimilarity(value, val);
				if (sim > maxSimilarity) {
					maxSimilarity = sim;
					maxSimValue = val;
				}
			}

			if (maxSimilarity > 0.7)
				consumerInvolvementDimension(maxSimValue);
			else
				log.warn("Consumer involvement column not 'full-service'/'self-service'/'in-between'. Is: '" + value
						+ "'");
		}
	}

	private String[] moneyFlowValues = { "c2c", "c2b2c", "free", "c2b" };

	private void moneyFlowDimension(String value) {
		if (value.isEmpty())
			return;

		if (value.equals(moneyFlowValues[0]))
			platformResource.addProperty(hasMoneyFlow, c2c);
		else if (value.equals(moneyFlowValues[1]))
			platformResource.addProperty(hasMoneyFlow, c2b2c);
		else if (value.equals(moneyFlowValues[2]))
			platformResource.addProperty(hasMoneyFlow, free);
		else if (value.equals(moneyFlowValues[3])) {
		} else {
			float maxSimilarity = 0;
			String maxSimValue = "";

			for (String val : moneyFlowValues) {
				float sim = levenshtein.getSimilarity(value, val);
				if (sim > maxSimilarity) {
					maxSimilarity = sim;
					maxSimValue = val;
				}
			}

			if (maxSimilarity > 0.7)
				moneyFlowDimension(maxSimValue);
			else
				log.warn("Money flow column not 'c2c'/'c2b2c'/'free'. Is: '" + value + "'");
		}
	}

	private void marketIntegrationDimension() {
		if (currentPlatform.getGlobalIntegration().isEmpty()
				&& currentPlatform.getGlobalIntegrationFinestLevel().isEmpty())
			return;

		Resource marketIntegration = ontologyModel.createResource(); // anonymous instance
		marketIntegration.addProperty(rdfType, marketIntegrationClass);
		platformResource.addProperty(hasMarketIntegration, marketIntegration);

		marketOffering(marketIntegration, currentPlatform.getGlobalIntegration().toLowerCase());

		marketWidth(marketIntegration, currentPlatform.getGlobalIntegrationFinestLevel().toLowerCase());
	}

	private String[] marketOfferingValues = { "integrated", "separated", "separated communities" };

	private void marketOffering(Resource marketIntegration, String value) {
		if (value.isEmpty())
			return;

		if (value.equals(marketOfferingValues[0]))
			marketIntegration.addProperty(marketsAre, integrated);
		else if (value.equals(marketOfferingValues[1]) || value.equals(marketOfferingValues[2]))
			marketIntegration.addProperty(marketsAre, separated);
		else {
			float maxSimilarity = 0;
			String maxSimValue = "";

			for (String val : marketOfferingValues) {
				float sim = levenshtein.getSimilarity(value, val);
				if (sim > maxSimilarity) {
					maxSimilarity = sim;
					maxSimValue = val;
				}
			}

			if (maxSimilarity > 0.7)
				marketOffering(marketIntegration, maxSimValue);
			else
				log.warn("Global integration column not 'integrated'/'separated'. Is: '" + value + "'");
		}
	}

	private String[] marketWidthValues = { "neighbourhood-wide", "city-wide", "state-wide", "country-wide",
			"region-wide", "global" };

	private void marketWidth(Resource marketIntegration, String value) {
		if (value.isEmpty())
			return;

		if (value.equals(marketWidthValues[0]))
			marketIntegration.addProperty(hasScope, neighbourhoodWide);
		else if (value.equals(marketWidthValues[1]))
			marketIntegration.addProperty(hasScope, cityWide);
		else if (value.equals(marketWidthValues[2]))
			marketIntegration.addProperty(hasScope, stateWide);
		else if (value.equals(marketWidthValues[3]))
			marketIntegration.addProperty(hasScope, countryWide);
		else if (value.equals(marketWidthValues[4]))
			marketIntegration.addProperty(hasScope, regionWide);
		else if (value.equals(marketWidthValues[5]))
			marketIntegration.addProperty(hasScope, global);
		else {
			float maxSimilarity = 0;
			String maxSimValue = "";

			for (String val : marketWidthValues) {
				float sim = levenshtein.getSimilarity(value, val);
				if (sim > maxSimilarity) {
					maxSimilarity = sim;
					maxSimValue = val;
				}
			}

			if (maxSimilarity > 0.7)
				marketWidth(marketIntegration, maxSimValue);
			else
				log.warn("Global integration finest level column not 'neighbourhood-wide'/'city-wide'/'state-wide'/'country-wide'/'region-wide'/'global'. Is: '"
						+ value + "'");
		}
	}

	private void launchYearDimension() {
		if (currentPlatform.getYearLaunch().isEmpty())
			return;

		try {
			int year = Integer.parseInt(currentPlatform.getYearLaunch());
			Resource launchYearResource = ontologyModel.createResource(DBPR + year);
			platformResource.addProperty(launchYear, launchYearResource);
		} catch (NumberFormatException e) {
			log.warn("Year launch column not a proper number. Is: '" + currentPlatform.getYearLaunch() + "'");
		}
	}

	private void locationDimension(String city, String country, Property property) {
		if ((city.isEmpty() || city.equals("?")) && (country.isEmpty() || country.equals("?")))
			return;

		Resource countryResource = null;
		Resource cityResource = null;

		if (!cityResources.containsKey(city + country)) {
			JSONObject json = findCity(city, country);
			if (json != null) {
				try {
					JSONObject o = json.getJSONArray("geonames").getJSONObject(0);
					int cityId = o.getInt("geonameId");
					String cityName = o.getString("toponymName");

					cityResource = ontologyModel.createResource(D2S
							+ cityName.replace(" ", "_").replace("[", "%5B").replace("]", "%5D"));
					cityResource.addProperty(rdfType, cityClass);
					cityResource.addProperty(rdfsLabel, cityName);
					Resource cityGeonames = ontologyModel.createResource("http://www.geonames.org/" + cityId);
					cityResource.addProperty(skosRelatedMatch, cityGeonames);

					JSONObject cityInfo = JsonReader
							.readJsonFromUrl("http://api.geonames.org/getJSON?username=discover2share&geonameId="
									+ cityId);
					if (cityInfo.has("wikipediaURL")) {
						String dbpediaId = cityInfo.getString("wikipediaURL").replace("en.wikipedia.org/wiki", "");
						Resource dbpedia = ontologyModel.createResource("http://dbpedia.org/resource" + dbpediaId);
						cityResource.addProperty(skosRelatedMatch, dbpedia);
					}

					String countryCode = o.getString("countryCode");
					if (!countryResources.containsKey(countryCode)) {
						if (countryIndex.containsKey(countryCode)) {
							countryResource = ontologyModel.createResource(D2S
									+ countryIndex.get(countryCode).get("resourceName"));
							countryResources.put(country, countryResource);
						} else
							log.warn("Could not find a country with code '" + countryCode + "'.");
					} else {
						countryResource = countryResources.get(countryCode);
					}
					cityResource.addProperty(locationCountry, countryResource);

					cityResources.put(city + country, new Pair<Resource, Resource>(cityResource, countryResource));
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if (!countryResources.containsKey(country)) {
				if (countryIndex.containsKey(country)) {
					countryResource = ontologyModel.createResource(D2S + countryIndex.get(country).get("resourceName"));
					countryResources.put(country, countryResource);
					if (!city.isEmpty() && !city.equals("?"))
						log.warn("Could not find the city '" + city + "'.");
				} else
					log.warn("Could not find the city '" + city + "' nor a country with code '" + country + "'.");
			} else {
				countryResource = countryResources.get(country);
			}
		} else {
			cityResource = cityResources.get(city + country).getFirst();
			countryResource = cityResources.get(city + country).getSecond();
		}

		if (countryResource != null) {
			Resource locationResource = ontologyModel.createResource();
			platformResource.addProperty(property, locationResource);

			locationResource.addProperty(locationCountry, countryResource);
			if (cityResource != null)
				locationResource.addProperty(locationCity, cityResource);
		}
	}

	public JSONObject findCity(String city, String country) {
		if (city.isEmpty() || city.equals("?"))
			return null;

		String base = "http://api.geonames.org/searchJSON?username=discover2share&maxRows=1&featureClass=P";
		String query1 = base + "&name=" + city.replace(" ", "%20") + "&country=" + country;
		String query2 = base + "&q=" + city.replace(" ", "%20") + "&country=" + country;
		try {
			JSONObject json = JsonReader.readJsonFromUrl(query1);

			if (json.has("totalResultsCount") && json.getInt("totalResultsCount") > 0) {
				return json;
			} else {
				json = JsonReader.readJsonFromUrl(query2);

				if (json.has("totalResultsCount") && json.getInt("totalResultsCount") > 0)
					return json;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public JSONObject findCountry(String country) {
		if (country.isEmpty() || country.equals("?"))
			return null;

		String query = "http://api.geonames.org/searchJSON?username=discover2share&maxRows=1&featureClass=A&isNameRequired&q="
				+ country;

		try {
			JSONObject json = JsonReader.readJsonFromUrl(query);

			if (json.has("totalResultsCount") && json.getInt("totalResultsCount") > 0) {
				return json;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private void smartphoneAppDimension() {
		if (currentPlatform.getAndroid().equals("x"))
			platformResource.addProperty(hasApp, androidApp);
		else if (!currentPlatform.getAndroid().equals("o") && !currentPlatform.getAndroid().isEmpty())
			log.warn("Android column is not 'o'/'x'. Is: '" + currentPlatform.getAndroid() + "'");

		if (currentPlatform.getiOS().equals("x"))
			platformResource.addProperty(hasApp, iOSApp);
		else if (!currentPlatform.getiOS().equals("o") && !currentPlatform.getiOS().isEmpty())
			log.warn("iOS column is not 'o'/'x'. Is: '" + currentPlatform.getiOS() + "'");

		if (currentPlatform.getWindowsPhone().equals("x"))
			platformResource.addProperty(hasApp, windowsPhoneApp);
		else if (!currentPlatform.getWindowsPhone().equals("o") && !currentPlatform.getWindowsPhone().isEmpty())
			log.warn("Windows phone column is not 'o'/'x'. Is: '" + currentPlatform.getWindowsPhone() + "'");
	}

	private void userDistributionDimension() {
		Map<String, Double> userCountries = AlexaParser.parseAlexa(currentPlatform.getStrippedUrl());
		if (userCountries != null) {
			for (String country : userCountries.keySet()) {
				Resource countryResource = null;
				if (!countryResourcesFullName.containsKey(country)) {
					JSONObject json = findCountry(country);
					if (json != null) {
						try {
							String countryCode = json.getJSONArray("geonames").getJSONObject(0)
									.getString("countryCode");
							if (countryIndex.containsKey(countryCode)) {
								countryResource = ontologyModel.createResource(D2S
										+ countryIndex.get(countryCode).get("resourceName"));
								countryResourcesFullName.put(country, countryResource);
							} else
								log.warn("Could not find the country '" + country + "'.");
						} catch (Exception e) {
							log.warn("Could not find the country '" + country + "'.");
						}
					} else {
						log.warn("Could not find the country '" + country + "'.");
					}
				} else {
					countryResource = countryResourcesFullName.get(country);
				}

				if (countryResource != null) {
					Resource node = ontologyModel.createResource();
					platformResource.addProperty(usedIn, node);
					Literal dateLiteral = ontologyModel.createTypedLiteral(GregorianCalendar.getInstance());
					node.addLiteral(date, dateLiteral);
					node.addLiteral(percentage, userCountries.get(country));
					node.addProperty(locationCountry, countryResource);
				}
			}
		}
	}
}
