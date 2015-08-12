package de.wwu.d2s.transformation;

import java.io.File;
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

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

import de.wwu.d2s.util.JsonReader;
import de.wwu.d2s.util.Pair;

/**
 * Transforms P2P SCC platform objects into instances of the Discover2Share ontology.
 */
public class OntologyWriter {

	private Logger log;
	private Levenshtein levenshtein = new Levenshtein(); // Levenshtein similarity
	private final double SIMILARITY = 0.7;

	// ontology namespaces
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
	private final String OWL = "http://www.w3.org/2002/07/owl#";

	// the central element for output
	private OntModel ontologyModel;

	// current platform object and RDF resource centrally available for all methods
	private Resource platformResource;
	private ExcelPlatform currentPlatform;

	// Maps to avoid unnecessary AJAX calls to geonames.org
	private Map<String, Resource> countryResources = new HashMap<String, Resource>();
	private Map<String, Pair<Resource, Resource>> cityResources = new HashMap<String, Pair<Resource, Resource>>();
	private Map<String, Map<String, String>> countryIndex = new HashMap<String, Map<String, String>>();
	// Map to avoid multiple creations of Year instances in the ontology
	private Map<Integer, Resource> yearResources = new HashMap<Integer, Resource>();

	// properties available centrally for all methods and platforms to only require one instantiation
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
	private Property operatorResidesIn;
	private Property locationCity;
	private Property locationCountry;
	private Property hasApp;
	private Property usedIn;
	private Property date;
	private Property percentage;
	private Property owlSameAs;

	// resources available centrally for all methods and platforms to only require one instantiation
	private Resource yearClass;
	private Resource cityClass;
	private Resource resourceType;
	private Resource threeDPrinters;
	private Resource accommodations;
	private Resource accommodationsFlatmate;
	private Resource adventureAndOutdoorRelatedResources;
	private Resource boats;
	private Resource books;
	private Resource cameras;
	private Resource campingVehicles;
	private Resource cargoBicycles;
	private Resource carsRide;
	private Resource carsTaxi;
	private Resource carsRent;
	private Resource carsTaxiBussesRide;
	private Resource clothing;
	private Resource dogs;
	private Resource foodDining;
	private Resource foodSelfGrown;
	private Resource jets;
	private Resource land;
	private Resource laundryMachines;
	private Resource media;
	private Resource miscellaneous;
	private Resource miscellaneousCombined;
	private Resource motorizedVehicles;
	private Resource nonMotorizedSportGear;
	private Resource parkingSpaces;
	private Resource retailSpaces;
	private Resource spaces;
	private Resource sportFacilities;
	private Resource storageSpaces;
	private Resource tools;
	private Resource transporters;
	private Resource venues;
	private Resource WiFiRouters;
	private Resource workSpaces;
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
	private Resource userDistributionClass;

	/**
	 * Instantiates the new ontology model and most resources and properties
	 */
	public OntologyWriter() {
		log = Logger.getLogger(this.getClass().getName()); // instantiate logger

		log.info("Build country index");
		buildCountryIndex(); // transform json array into map with country codes as keys

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
		operatorResidesIn = ontologyModel.createProperty(D2S + "operator_resides_in");
		locationCity = ontologyModel.createProperty(DBPP + "locationCity");
		locationCountry = ontologyModel.createProperty(DBPP + "locationCountry");
		hasApp = ontologyModel.createProperty(D2S + "has_app");
		usedIn = ontologyModel.createProperty(D2S + "used_in");
		date = ontologyModel.createProperty(DCT + "date");
		percentage = ontologyModel.createProperty(D2S + "user_percentage");
		owlSameAs = ontologyModel.createProperty(OWL + "sameAs");

		log.info("Defining D2S classes/instances.");
		// create resources once in the beginning to use for every platform
		yearClass = ontologyModel.createResource(D2S + "Year");
		cityClass = ontologyModel.createResource(D2S + "City");

		resourceType = ontologyModel.createResource(D2S + "Resource_Type");
		threeDPrinters = ontologyModel.createResource(D2S + "3d_printers");
		accommodations = ontologyModel.createResource(D2S + "Accommodations");
		accommodationsFlatmate = ontologyModel.createResource(D2S + "Accommodations_flatmate");
		adventureAndOutdoorRelatedResources = ontologyModel.createResource(D2S
				+ "Adventure-_and_outdoor-related_resources");
		boats = ontologyModel.createResource(D2S + "Boats");
		books = ontologyModel.createResource(D2S + "Books");
		cameras = ontologyModel.createResource(D2S + "Cameras");
		campingVehicles = ontologyModel.createResource(D2S + "Camping_vehicles");
		cargoBicycles = ontologyModel.createResource(D2S + "Cargo_bicycles");
		carsRide = ontologyModel.createResource(D2S + "Cars_ride");
		carsTaxi = ontologyModel.createResource(D2S + "Cars_taxi");
		carsRent = ontologyModel.createResource(D2S + "Cars_rent");
		carsTaxiBussesRide = ontologyModel.createResource(D2S + "Cars_taxi_busses_ride");
		clothing = ontologyModel.createResource(D2S + "Clothing");
		dogs = ontologyModel.createResource(D2S + "Dogs");
		foodDining = ontologyModel.createResource(D2S + "Food_dining");
		foodSelfGrown = ontologyModel.createResource(D2S + "Food_self-grown");
		jets = ontologyModel.createResource(D2S + "Jets");
		land = ontologyModel.createResource(D2S + "Land");
		laundryMachines = ontologyModel.createResource(D2S + "Laundry_machines");
		media = ontologyModel.createResource(D2S + "Media");
		miscellaneous = ontologyModel.createResource(D2S + "Miscellaneous");
		miscellaneousCombined = ontologyModel.createResource(D2S + "Miscellaneous_(combined)");
		motorizedVehicles = ontologyModel.createResource(D2S + "Motorized_vehicles");
		nonMotorizedSportGear = ontologyModel.createResource(D2S + "Non-motorized_(sport)_gear");
		parkingSpaces = ontologyModel.createResource(D2S + "Parking_spaces");
		retailSpaces = ontologyModel.createResource(D2S + "Retail_spaces");
		spaces = ontologyModel.createResource(D2S + "Spaces");
		sportFacilities = ontologyModel.createResource(D2S + "Sport_facilities");
		storageSpaces = ontologyModel.createResource(D2S + "Storage_spaces");
		tools = ontologyModel.createResource(D2S + "Tools");
		transporters = ontologyModel.createResource(D2S + "Transporters");
		venues = ontologyModel.createResource(D2S + "Venues");
		WiFiRouters = ontologyModel.createResource(D2S + "Wi-Fi_routers");
		workSpaces = ontologyModel.createResource(D2S + "Work_spaces");

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
		
		userDistributionClass = ontologyModel.createResource(D2S + "User_Distribution");
	}

	/**
	 * Retrieve JSON from discover2share server and transform it into a map with country codes as keys and a map as
	 * value. Latter map contains all attributes of a country object with their names as keys and values as values.
	 */
	private void buildCountryIndex() {
		try {
			// read JSON from discover2share server
			JSONObject json = JsonReader
					.readJsonFromUrl("http://localhost:8080/discover2share-Web/resources/js/countries.json");
			JSONArray a = json.getJSONArray("countries"); // retrieved object contains an array at the key "countries"
			for (int i = 0; i < a.length(); i++) { // for each country
				JSONObject o = a.getJSONObject(i);
				Map<String, String> map = new HashMap<String, String>(); // create a map of its attributes
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

	/**
	 * Iterates through the list of platforms and starts their respective construction. Finally writes the model out
	 * into a text file.
	 * 
	 * @param platforms
	 *            List of platform objects.
	 * @param outputFile
	 *            Filepath to output the result into. Provided by the user.
	 */
	public void writeAll(List<ExcelPlatform> platforms, String outputFile) {
		log.info("Making a first request to the GeoNames API, which usually fails!");
		try {
			JsonReader
					.readJsonFromUrl("http://api.geonames.org/searchJSON?username=discover2share&maxRows=1&featureClass=P&country=DE&q=Celle");
		} catch (IOException | JSONException e1) {
			log.info("Failed indeed. Should work from now on though!");
		}

		int nrPlatforms = platforms.size();
		for (int i = 0; i < nrPlatforms; i++) {
			currentPlatform = platforms.get(i);

			log.info("Creating '" + currentPlatform.getName() + "' platform (" + (i + 1) + "/" + nrPlatforms + ")");
			constructPlatform();
		}

		try (OutputStream out = new FileOutputStream(outputFile)) {
			ontologyModel.write(out, "RDF/XML"); // Output format RDF/XML. Could also be e.g. Turtle
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Calls all necessary steps required to construct the D2S representation of a platform.
	 */
	private void constructPlatform() {
		initializePlatform();

		resourceTypeDimension(currentPlatform.getResourceType());
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
				operatorResidesIn);
		smartphoneAppDimension();
		userDistributionDimension();
	}

	/**
	 * Creates the ontology Resource of type d2s:P2P_SCC_Platform representing the platform. Label and URL are added as
	 * properties.
	 */
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

	// Array containing all resource types used in by the platforms to transform
	private String[] resourceTypes = { "3d printers", "accommodations", "accommodations / flatmate",
			"adventure- and outdoor-related resources", "boats", "books", "cameras", "camping vehicles",
			"cargo bicycles", "cars / ride", "cars / taxi", "cars / rent", "cars, taxi, busses / ride", "clothing",
			"dogs", "food / dining", "food / self-grown", "jets", "land", "laundry machines", "media", "miscellaneous",
			"miscellaneous (combined)", "motorized vehicles", "non-motorized (sport) gear", "parking spaces",
			"retail spaces", "spaces", "sport facilities", "storage spaces", "tools", "tranporters", "venues",
			"Wi-Fi routers", "work spaces" };

	/**
	 * Creates a link between the platform resource an its value for the resource type dimension.
	 * 
	 * @param value
	 *            Platform's resource type as defined in the excel table.
	 */
	private void resourceTypeDimension(String value) {
		if (value.isEmpty())
			return;

		if (value.equals(resourceTypes[0])) {
			platformResource.addProperty(hasResourceType, threeDPrinters);
		} else if (value.equals(resourceTypes[1])) {
			platformResource.addProperty(hasResourceType, accommodations);
		} else if (value.equals(resourceTypes[2])) {
			platformResource.addProperty(hasResourceType, accommodationsFlatmate);
		} else if (value.equals(resourceTypes[3])) {
			platformResource.addProperty(hasResourceType, adventureAndOutdoorRelatedResources);
		} else if (value.equals(resourceTypes[4])) {
			platformResource.addProperty(hasResourceType, boats);
		} else if (value.equals(resourceTypes[5])) {
			platformResource.addProperty(hasResourceType, books);
		} else if (value.equals(resourceTypes[6])) {
			platformResource.addProperty(hasResourceType, cameras);
		} else if (value.equals(resourceTypes[7])) {
			platformResource.addProperty(hasResourceType, campingVehicles);
		} else if (value.equals(resourceTypes[8])) {
			platformResource.addProperty(hasResourceType, cargoBicycles);
		} else if (value.equals(resourceTypes[9])) {
			platformResource.addProperty(hasResourceType, carsRide);
		} else if (value.equals(resourceTypes[10])) {
			platformResource.addProperty(hasResourceType, carsTaxi);
		} else if (value.equals(resourceTypes[11])) {
			platformResource.addProperty(hasResourceType, carsRent);
		} else if (value.equals(resourceTypes[12])) {
			platformResource.addProperty(hasResourceType, carsTaxiBussesRide);
		} else if (value.equals(resourceTypes[13])) {
			platformResource.addProperty(hasResourceType, clothing);
		} else if (value.equals(resourceTypes[14])) {
			platformResource.addProperty(hasResourceType, dogs);
		} else if (value.equals(resourceTypes[15])) {
			platformResource.addProperty(hasResourceType, foodDining);
		} else if (value.equals(resourceTypes[16])) {
			platformResource.addProperty(hasResourceType, foodSelfGrown);
		} else if (value.equals(resourceTypes[17])) {
			platformResource.addProperty(hasResourceType, jets);
		} else if (value.equals(resourceTypes[18])) {
			platformResource.addProperty(hasResourceType, land);
		} else if (value.equals(resourceTypes[19])) {
			platformResource.addProperty(hasResourceType, laundryMachines);
		} else if (value.equals(resourceTypes[20])) {
			platformResource.addProperty(hasResourceType, media);
		} else if (value.equals(resourceTypes[21])) {
			platformResource.addProperty(hasResourceType, miscellaneous);
		} else if (value.equals(resourceTypes[22])) {
			platformResource.addProperty(hasResourceType, miscellaneousCombined);
		} else if (value.equals(resourceTypes[23])) {
			platformResource.addProperty(hasResourceType, motorizedVehicles);
		} else if (value.equals(resourceTypes[24])) {
			platformResource.addProperty(hasResourceType, nonMotorizedSportGear);
		} else if (value.equals(resourceTypes[25])) {
			platformResource.addProperty(hasResourceType, parkingSpaces);
		} else if (value.equals(resourceTypes[26])) {
			platformResource.addProperty(hasResourceType, retailSpaces);
		} else if (value.equals(resourceTypes[27])) {
			platformResource.addProperty(hasResourceType, spaces);
		} else if (value.equals(resourceTypes[28])) {
			platformResource.addProperty(hasResourceType, sportFacilities);
		} else if (value.equals(resourceTypes[29])) {
			platformResource.addProperty(hasResourceType, storageSpaces);
		} else if (value.equals(resourceTypes[30])) {
			platformResource.addProperty(hasResourceType, tools);
		} else if (value.equals(resourceTypes[31])) {
			platformResource.addProperty(hasResourceType, transporters);
		} else if (value.equals(resourceTypes[32])) {
			platformResource.addProperty(hasResourceType, venues);
		} else if (value.equals(resourceTypes[33])) {
			platformResource.addProperty(hasResourceType, WiFiRouters);
		} else if (value.equals(resourceTypes[34])) {
			platformResource.addProperty(hasResourceType, workSpaces);
		} else { // if no match with the known resource types is possible
			float maxSimilarity = 0;
			String maxSimValue = "";

			for (String val : resourceTypes) { // for each resource type
				float sim = levenshtein.getSimilarity(value, val); // calculate similarity to cope with typos
				if (sim > maxSimilarity) { // only keep similarity if it is the highest so far
					maxSimilarity = sim;
					maxSimValue = val;
				}
			}

			if (maxSimilarity > SIMILARITY) // when a similarity higher than the threshold is asserted
				resourceTypeDimension(maxSimValue); // restart method with max similar value
			else { // if no similarity found
				value = Character.toUpperCase(value.charAt(0)) + value.substring(1);
				try {
					// create new resource type. Remove illegal charcters from used in the desciptions name
					Resource newType = ontologyModel.createResource(D2S + new File(value).toURI().toURL());
					newType.addProperty(rdfType, resourceType);
					newType.addProperty(rdfsLabel, value);
					platformResource.addProperty(hasResourceType, newType);
					log.info("New resource type '" + value + "' created.");
				} catch (Exception e) {
					log.warn("Could not identify resource type '" + value + "' nor create a new one.");
				}
			}
		}
	}

	/**
	 * Creates a link between the platform resource an its values for the sustainable consumerism dimension.
	 */
	private void sustainableConsumerismDimension() {
		if (currentPlatform.getEconomical().equals("o") && currentPlatform.getEnvironmental().equals("o")
				&& currentPlatform.getSocial().equals("o")) { // if all three types are marked as not promoted via "o"
			platformResource.addProperty(promotes, noConsumerism); // add link to type "none"
		} else { // otherwise set link where marked as promoted via "x"
			if (currentPlatform.getEconomical().equals("x"))
				platformResource.addProperty(promotes, economicConsumerism);
			if (currentPlatform.getEnvironmental().equals("x"))
				platformResource.addProperty(promotes, environmentalConsumerism);
			if (currentPlatform.getSocial().equals("x"))
				platformResource.addProperty(promotes, socialConsumerism);
		}

		// output warnings where something other than "o" or "x" where used in the excel table
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

	/**
	 * Creates a link between the platform resource an its value for the P2P SCC pattern dimension.
	 * 
	 * @param value
	 *            Platform's pattern as defined in the excel table.
	 */
	private void patternDimension(String value) {
		if (value.isEmpty())
			return;

		if (value.equals(patternValues[0])) { // when matching
			// create a new anonymous instance of type deferred to be able to add temporality to it later on
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

			if (maxSimilarity > SIMILARITY)
				patternDimension(maxSimValue);
			else
				log.warn("P2P Pattern column not 'deferred'/'immediate'/'recurrent'. Is: '" + value + "'");
		}
	}

	private String[] mediationValues = { "profit from both", "profit from both peer consumers and peer providers",
			"indirect profit", "profit from peer consumers", "profit from peer providers" };

	/**
	 * Creates a link between the platform resource an its value for the market mediation dimension.
	 * 
	 * @param value
	 *            Platform's market mediation as defined in the excel table.
	 */
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

			if (maxSimilarity > SIMILARITY)
				marketMediationDimension(maxSimValue);
			else
				log.warn("Market mediation column not 'profit from both peer consumers and peer providers'/'indirect profit'/'profit from peer consumers'/'profit from peer providers'. Is: '"
						+ value + "'");
		}
	}

	/**
	 * Creates links between the platform resource an its values concerning additional mediation types.
	 */
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

	/**
	 * Creates a link between the platform resource an its value for the type of accessed object dimension.
	 */
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

			if (maxSimilarity > SIMILARITY)
				typeOfAccessedObjectDimension(maxSimValue);
			else
				log.warn("Type of acc. object column not 'mixed'/'functional'/'experiential'. Is: '" + value + "'");
		}
	}

	private String[] resourceOwnerValues = { "private", "private and business", "business" };

	/**
	 * Creates a link between the platform resource an its value for the resource owner dimension.
	 * 
	 * @param value
	 *            Platform's resource type as defined in the excel table.
	 */
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

			if (maxSimilarity > SIMILARITY)
				resourceOwnerDimension(maxSimValue);
			else
				log.warn("Resource owner column not 'private'/'private and business'. Is: '" + value + "'");

		}
	}

	private String[] serviceDurationValues = { "minutes", "hours", "days", "weeks", "months", "years" };

	/**
	 * Creates a link between the platform resource an its value for the min service duration.
	 * 
	 * @param value
	 *            Platform's min service duration as defined in the excel table.
	 */
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

			if (maxSimilarity > SIMILARITY)
				minServiceDurationDimension(maxSimValue);
			else
				log.warn("Service duration min column not 'minutes'/'hours'/'days'/'weeks'/'months'. Is: '" + value
						+ "'");
		}

	}

	/**
	 * Creates a link between the platform resource an its value for the max service duration.
	 * 
	 * @param value
	 *            Platform's max service duration as defined in the excel table.
	 */
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

			if (maxSimilarity > SIMILARITY)
				maxServiceDurationDimension(maxSimValue);
			else
				log.warn("Service duration max column not 'minutes'/'hours'/'days'/'weeks'/'months'. Is: '" + value
						+ "'");
		}

	}

	private String[] consumerInvolvementValues = { "full-service", "self-service", "in-between" };

	/**
	 * Creates a link between the platform resource an its value for the consumer involvement dimension.
	 * 
	 * @param value
	 *            Platform's consumer involvement as defined in the excel table.
	 */
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

			if (maxSimilarity > SIMILARITY)
				consumerInvolvementDimension(maxSimValue);
			else
				log.warn("Consumer involvement column not 'full-service'/'self-service'/'in-between'. Is: '" + value
						+ "'");
		}
	}

	private String[] moneyFlowValues = { "c2c", "c2b2c", "free", "c2b" };

	/**
	 * Creates a link between the platform resource an its value for the money flow dimension.
	 * 
	 * @param value
	 *            Platform's money flow as defined in the excel table.
	 */
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

			if (maxSimilarity > SIMILARITY)
				moneyFlowDimension(maxSimValue);
			else
				log.warn("Money flow column not 'c2c'/'c2b2c'/'free'. Is: '" + value + "'");
		}
	}

	/**
	 * Creates a link between the platform resource and a new market integration instance. This instance is then
	 * connected to the respective market offering and geographic scope.
	 */
	private void marketIntegrationDimension() {
		if (currentPlatform.getGlobalIntegration().isEmpty()
				&& currentPlatform.getGlobalIntegrationFinestLevel().isEmpty())
			return;

		Resource marketIntegration = ontologyModel.createResource(); // anonymous instance
		marketIntegration.addProperty(rdfType, marketIntegrationClass);
		platformResource.addProperty(hasMarketIntegration, marketIntegration);

		marketOffering(marketIntegration, currentPlatform.getGlobalIntegration().toLowerCase());

		geographicScope(marketIntegration, currentPlatform.getGlobalIntegrationFinestLevel().toLowerCase());
	}

	private String[] marketOfferingValues = { "integrated", "separated", "separated communities" };

	/**
	 * Creates a link between a platform's market integration instance and its value for the market offering.
	 * 
	 * @param marketIntegration
	 *            Instance created for the current platform.
	 * @param value
	 *            Market offering value as defined in the excel table.
	 */
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

			if (maxSimilarity > SIMILARITY)
				marketOffering(marketIntegration, maxSimValue);
			else
				log.warn("Global integration column not 'integrated'/'separated'. Is: '" + value + "'");
		}
	}

	private String[] geographicScopeValues = { "neighbourhood-wide", "city-wide", "state-wide", "country-wide",
			"region-wide", "global" };

	/**
	 * Creates a link between a platform's market integration instance and its value for the geographic scope.
	 * 
	 * @param marketIntegration
	 *            Instance created for the current platform.
	 * @param value
	 *            Geographic scope as defined in the excel table.
	 */
	private void geographicScope(Resource marketIntegration, String value) {
		if (value.isEmpty())
			return;

		if (value.equals(geographicScopeValues[0]))
			marketIntegration.addProperty(hasScope, neighbourhoodWide);
		else if (value.equals(geographicScopeValues[1]))
			marketIntegration.addProperty(hasScope, cityWide);
		else if (value.equals(geographicScopeValues[2]))
			marketIntegration.addProperty(hasScope, stateWide);
		else if (value.equals(geographicScopeValues[3]))
			marketIntegration.addProperty(hasScope, countryWide);
		else if (value.equals(geographicScopeValues[4]))
			marketIntegration.addProperty(hasScope, regionWide);
		else if (value.equals(geographicScopeValues[5]))
			marketIntegration.addProperty(hasScope, global);
		else {
			float maxSimilarity = 0;
			String maxSimValue = "";

			for (String val : geographicScopeValues) {
				float sim = levenshtein.getSimilarity(value, val);
				if (sim > maxSimilarity) {
					maxSimilarity = sim;
					maxSimValue = val;
				}
			}

			if (maxSimilarity > SIMILARITY)
				geographicScope(marketIntegration, maxSimValue);
			else
				log.warn("Global integration finest level column not 'neighbourhood-wide'/'city-wide'/'state-wide'/'country-wide'/'region-wide'/'global'. Is: '"
						+ value + "'");
		}
	}

	/**
	 * Creates a link between the platform and the resource representing its launch year.
	 */
	private void launchYearDimension() {
		if (currentPlatform.getYearLaunch().isEmpty())
			return;

		try {
			int year = Integer.parseInt(currentPlatform.getYearLaunch());
			Resource launchYearResource;
			if (!yearResources.containsKey(year)) { // when no instance for this year has been created before
				launchYearResource = ontologyModel.createResource(D2S + year); // create new resource
				launchYearResource.addProperty(rdfType, yearClass); // of type Year
				// create Resource for DBpedia equivalent
				Resource launchYearResourceDBpedia = ontologyModel.createResource(DBPR + year);
				launchYearResource.addProperty(owlSameAs, launchYearResourceDBpedia); // connect to DBpedia equivalent
				// add new resource to map to avoid duplicate creation in the future
				yearResources.put(year, launchYearResource);
			} else
				// if instance for this year was already created
				launchYearResource = yearResources.get(year); // use it

			platformResource.addProperty(launchYear, launchYearResource); // connect platform and year instance
		} catch (NumberFormatException e) {
			log.warn("Year launch column not a proper number. Is: '" + currentPlatform.getYearLaunch() + "'");
		}
	}

	/**
	 * Connects the platform either to an anonymous class representing its foundation or residence place depending on
	 * the given property. The anonymous class bundles city and country resources of that respective place.
	 * 
	 * @param city
	 *            City as defined in the excel table.
	 * @param country
	 *            Country code as defined in the excel table.
	 * @param property
	 *            Property to use when connecting place and platform.
	 */
	private void locationDimension(String city, String country, Property property) {
		if ((city.isEmpty() || city.equals("?")) && (country.isEmpty() || country.equals("?")))
			return; // stop if neither city nor country are provided

		Resource countryResource = null;
		Resource cityResource = null;

		// if resource for the given city-country combination was not already created
		if (!cityResources.containsKey(city + country)) {
			JSONObject json = findCity(city, country); // lookup combination on geonames.org
			if (json != null) { // if a result was found
				try {
					JSONObject o = json.getJSONArray("geonames").getJSONObject(0); // take first object aka the result
					int cityId = o.getInt("geonameId");
					String cityName = o.getString("toponymName");

					// Create proxy instance of d2s:City. Remove illegal characters for resource name.
					cityResource = ontologyModel.createResource(D2S
							+ cityName.replace(" ", "_").replace("[", "%5B").replace("]", "%5D"));
					cityResource.addProperty(rdfType, cityClass);
					cityResource.addProperty(rdfsLabel, cityName); // Add city name as label
					// Create resource for the found geonames concept
					Resource cityGeonames = ontologyModel.createResource("http://www.geonames.org/" + cityId);
					cityResource.addProperty(owlSameAs, cityGeonames); // link the two equivalents

					// make another request to geonames to retrieve extended info on the city concept
					JSONObject cityInfo = JsonReader
							.readJsonFromUrl("http://api.geonames.org/getJSON?username=discover2share&geonameId="
									+ cityId);
					// if the extended info contains a proper link to the respective wikipedia article
					if (cityInfo.has("wikipediaURL")) {
						// Create resource for DBpedia concept belonging to the wikipedia article
						String dbpediaId = cityInfo.getString("wikipediaURL").replace("en.wikipedia.org/wiki", "");
						Resource dbpedia = ontologyModel.createResource("http://dbpedia.org/resource" + dbpediaId);
						// Connect the two equivalents
						cityResource.addProperty(owlSameAs, dbpedia);
					}

					// Take country code from the geonames city info instead of the one provided in the excel table to
					// avoid retaining possible city-country assignment faults
					String countryCode = o.getString("countryCode");
					// if a resource for the country was not yet created
					if (!countryResources.containsKey(countryCode)) {
						if (countryIndex.containsKey(countryCode)) { // get necessary country info from the index
							countryResource = ontologyModel.createResource(D2S
									+ countryIndex.get(countryCode).get("resourceName")); // create new resource
							// add the resource to the map for future re-use
							countryResources.put(country, countryResource);
						} else
							// Country code is apparently not among those in the complete list maintained on the
							// Discover2Share server
							log.warn("Could not find a country with code '" + countryCode + "'.");
					} else { // if a resource was previously created for the country
						countryResource = countryResources.get(countryCode); // use that one
					}
					cityResource.addProperty(locationCountry, countryResource); // connect city and country resources

					// put city and country resource pair in the map for future re-use
					cityResources.put(city + country, new Pair<Resource, Resource>(cityResource, countryResource));
				} catch (Exception e) {
					e.printStackTrace();
				}
			} // if the city was not found and no resource for the given country has been created yet
			else if (!countryResources.containsKey(country)) {
				if (countryIndex.containsKey(country)) { // get necessary country info from the index
					// create new resource
					countryResource = ontologyModel.createResource(D2S + countryIndex.get(country).get("resourceName"));
					countryResources.put(country, countryResource); // add the resource to the map for future re-use
					if (!city.isEmpty() && !city.equals("?")) // if a city was provided to begin with
						log.warn("Could not find the city '" + city + "'."); // warn that the given city was not found
				} else
					// if no country info could be found
					// warn that neither city nor country were found
					log.warn("Could not find the city '" + city + "' nor a country with code '" + country + "'.");
			} else { // if a resource was previously created for the country
				countryResource = countryResources.get(country); // use that one
			}
		} else { // if a resource pair for the given city and country was previously created, use both resources.
			cityResource = cityResources.get(city + country).getFirst();
			countryResource = cityResources.get(city + country).getSecond();
		}

		if (countryResource != null) { // if a resource was created for the country
			Resource locationResource = ontologyModel.createResource(); // create a new anonymous resource
			platformResource.addProperty(property, locationResource); // connect platform and anonymous resource
			// connect anonymous resource and country resource
			locationResource.addProperty(locationCountry, countryResource);

			if (cityResource != null) // if a resource was created for the city as well
				// connect anonymous resource and city resource
				locationResource.addProperty(locationCity, cityResource);
		}
	}

	/**
	 * Makes a call to the geonames.org API to retrieve a city matching the given city and country.
	 * 
	 * @param city
	 *            City to search for.
	 * @param country
	 *            Country to search in (optional)
	 * @return JSON result object
	 */
	public JSONObject findCity(String city, String country) {
		if (city.isEmpty() || city.equals("?"))
			return null;

		// Define query that ought to return maximum one result of featureClass P (city, village,...)
		String base = "http://api.geonames.org/searchJSON?username=discover2share&maxRows=1&featureClass=P";
		// try querying by exact name in the given country
		String query1 = base + "&name=" + city.replace(" ", "%20") + "&country=" + country;
		// otherwise by all attributes of cities in the given country
		String query2 = base + "&q=" + city.replace(" ", "%20") + "&country=" + country;
		try {
			JSONObject json = JsonReader.readJsonFromUrl(query1); // run first query

			if (json.has("totalResultsCount") && json.getInt("totalResultsCount") > 0) {
				return json; // return the result if one was found
			} else { // otherwise run the second query
				json = JsonReader.readJsonFromUrl(query2);

				if (json.has("totalResultsCount") && json.getInt("totalResultsCount") > 0)
					return json; // return the result if one was found.
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null; // if no results were found at all, return null-
	}

	/**
	 * Connects the platform with the countries it is used in as a representation of the user distribution dimension.
	 */
	private void userDistributionDimension() {
		// parse the countries the platform is used in from its Alexa page
		Map<String, Double> userCountries = AlexaParser.parseAlexa(currentPlatform.getStrippedUrl());
		if (userCountries != null) { // if any countries were found at Alexa
			for (String country : userCountries.keySet()) { // for each
				Resource countryResource = null;
				// if no resource for this country name was yet created
				if (!countryResources.containsKey(country)) {
					if (countryIndex.containsKey(country)) {
						// create a resource from this info
						countryResource = ontologyModel.createResource(D2S
								+ countryIndex.get(country).get("resourceName"));
						// add it to the map for future re-use
						countryResources.put(country, countryResource);
					} else
						// Country code is apparently not among those in the complete list maintained on the
						// Discover2Share server
						log.warn("Could not find the country '" + country + "'.");
				} else { // if a resource for this country name was already created
					countryResource = countryResources.get(country); // use it
				}

				if (countryResource != null) { // if a country resource was found
					// create an anonymous node to bundle country, percentage of overall users for that country and
					// the information retrieval date
					Resource node = ontologyModel.createResource();
					node.addProperty(rdfType, userDistributionClass);
					platformResource.addProperty(usedIn, node); // connect platform and anonymous node
					// create a dateTime literal for the current time
					Literal dateLiteral = ontologyModel.createTypedLiteral(GregorianCalendar.getInstance());
					node.addLiteral(date, dateLiteral); // add the retrieval date to the anonymous node
					node.addLiteral(percentage, userCountries.get(country)); // add the user percentage
					node.addProperty(locationCountry, countryResource); // link the country
				}
			}
		}
	}

	/**
	 * Makes a call to the geonames.org API to retrieve a country matching the given name.
	 * 
	 * @param country
	 *            The country to search for
	 * @return JSON result object
	 */
	public JSONObject findCountry(String country) {
		if (country.isEmpty() || country.equals("?"))
			return null;

		// Query that ought to return at most one result of feature class A (country, state, region,...) and where at
		// least one of the words in the search string is part of the entities name.
		String query = "http://api.geonames.org/searchJSON?username=discover2share&maxRows=1&featureClass=A&isNameRequired&q="
				+ country;

		try {
			JSONObject json = JsonReader.readJsonFromUrl(query); // Make request

			if (json.has("totalResultsCount") && json.getInt("totalResultsCount") > 0) {
				return json; // return result if found
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null; // if no result was found, return null
	}

	/**
	 * Connects the platform to its values from the Smartphone App dimension
	 */
	private void smartphoneAppDimension() {
		if (currentPlatform.getAndroid().equals("x")) // if an android app is marked as existent
			platformResource.addProperty(hasApp, androidApp); // link platform and android app class
		// warn if a value other than "x" or "o" was used in the excel table
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
}
