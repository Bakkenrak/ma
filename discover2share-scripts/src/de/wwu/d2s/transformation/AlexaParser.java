package de.wwu.d2s.transformation;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTable;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.update.UpdateExecutionFactory;
import com.hp.hpl.jena.update.UpdateFactory;

import de.wwu.d2s.util.JsonReader;
import de.wwu.d2s.util.Platform;

/**
 * Provides a class to parse information about user countries from an Alexa.com page. If executed directly, it will insert the asserted data using the defined
 * SPARQL Update ENDPOINT.
 */
public class AlexaParser {

	private static Logger log;

	private Map<String, Resource> countryIndex = new HashMap<String, Resource>();

	private static String QUERYENDPOINT = "http://localhost:3030/d2s-ont/query"; // SPARQL Query Endpoint to retrieve platforms from
	private static String UPDATEENDPOINT = "http://localhost:3030/d2s-ont/update"; // SPARQL Update Endpoint to insert triples into
	// JSON file containing all countries of the ontology
	private static String COUNTRIESJSON = "http://localhost:8080/discover2share-Web/resources/js/countries.json";

	// ontology namespaces
	private final static String D2S = "http://www.discover2share.net/d2s-ont/";
	private final static String DBPP = "http://dbpedia.org/property/";
	private final static String RDF = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	private final static String DCT = "http://purl.org/dc/terms/";

	private OntModel ontologyModel;
	private Property rdfType;
	private Resource userDistributionClass;
	private Property usedIn;
	private Property date;
	private Property percentage;
	private Property locationCountry;
	private Property alexaPage;

	public static void main(String[] args) {		
		log = Logger.getLogger(AlexaParser.class.getName()); // instantiate logger

		// handle commandline arguments
		if (args.length > 0)
			QUERYENDPOINT = args[0];
		if (args.length > 1)
			UPDATEENDPOINT = args[1];
		if (args.length > 2)
			COUNTRIESJSON = args[2];
		String fileOutput = null;
		for (int i = 0; i < args.length; i++) {
			System.out.println(args[i]);
			if (args[i].equals("fileOutput")) {
				if (i+1 >= args.length) {
					log.error("You chose file output but didn't provide a filename in the next argument.");
					System.exit(1);
				}
				fileOutput = args[i+1];
				log.info("Output to file " + fileOutput + " selected.");
			}
		}
		
		log.info("Expecting the SPARQL Endpoint for data retrieval at " + QUERYENDPOINT);
		log.info("Expecting the SPARQL Endpoint for data insertion at " + UPDATEENDPOINT);
		log.info("Expecting the Countries JSON at " + COUNTRIESJSON);
		AlexaParser ax = new AlexaParser(null);
		OntModel ontModel = ax.alterOntologyModel(true, null); // have a new ontology model created and info from alexa added to it

		if (fileOutput == null) { // write directly to triplestore
			log.info("Inserting new triples into Triplestore");
			OutputStream baos = new ByteArrayOutputStream();
			ontModel.write(baos, "N-TRIPLE"); // transform data in ontology model into triples
			String query = "INSERT DATA { " + baos.toString() + "}"; // build insert query
			UpdateExecutionFactory.createRemote(UpdateFactory.create(query), UPDATEENDPOINT).execute(); // execute update to endpoint
		} else { // write to file
			log.info("Writing triples to " + fileOutput);
			try (OutputStream out = new FileOutputStream(fileOutput)) {
				ontModel.write(out, "RDF/XML"); // Output format RDF/XML. Could also be e.g. Turtle
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		log.info("Done");
	}

	public AlexaParser(OntModel ontModel) {
		// Ontology Model to create new data in
		if (ontModel != null)
			ontologyModel = ontModel;
		else
			// if no model is provided
			ontologyModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM); // create new one

		// instantiate commonly used Properties and Resource
		rdfType = ontologyModel.createProperty(RDF + "type");
		userDistributionClass = ontologyModel.createResource(D2S + "User_Distribution");
		usedIn = ontologyModel.createProperty(D2S + "used_in");
		date = ontologyModel.createProperty(DCT + "date");
		percentage = ontologyModel.createProperty(D2S + "user_percentage");
		locationCountry = ontologyModel.createProperty(DBPP + "locationCountry");
		alexaPage = ontologyModel.createProperty(D2S + "alexa_page");

		JSONArray countries;
		try { // retrieve list of countries from JSON file
			countries = JsonReader.readJsonFromUrl(COUNTRIESJSON).getJSONArray("countries");
			for (int i = 0; i < countries.length(); i++) { // build country index, transform every JSONObject into a country code - country resource pair
				JSONObject country = countries.getJSONObject(i);
				countryIndex.put(country.getString("countryCode"), ontologyModel.createResource(D2S + country.getString("resourceName")));
			}
		} catch (JSONException | IOException e) {
			e.printStackTrace();
			log.error("Error retrieving countries from JSON. Aborting...");
			return;
		}
	}

	/**
	 * Parses the respective Alexa page for every platform in the ontology or a specific given platform. If user country information is found, it is added to
	 * the ontology model.
	 * 
	 * @param allPlatforms
	 *            Boolean determining whether to parse all platforms in the ontology
	 * @param p
	 *            Specific platform to parse Alexa info for.
	 * @return The ontology model or null if not all platforms are to be parsed and no specific platform is provided
	 */
	public OntModel alterOntologyModel(boolean allPlatforms, Platform p) {
		int success = 0;
		List<Platform> platforms;
		if (allPlatforms) { // if all platforms in the ontology are to be parsed
			platforms = getAllPlatforms(); // retrieve all platforms from the ontology
		} else if (p != null) { // if a specific platform is given to parse
			platforms = new ArrayList<Platform>(); // new empty list
			platforms.add(p); // with only this one platform
		} else {
			return null;
		}
		for (Platform platform : platforms) { // for each platform
			Map<String, Double> userData = parseAlexa(platform.getUrl()); // parse user distribution data from Alexa
			if (userData == null) { // if no user distribution was found on the platform's respective Alexa page
				log.info("No user distribution information found on Alexa for " + platform.getLabel());
			} else {
				// create platform resource in ontology model
				Resource platformResource = ontologyModel.createResource(D2S + platform.getResourceName());
				platformResource.addProperty(alexaPage, "http://www.alexa.com/siteinfo/" + platform.getUrl(), XSDDatatype.XSDanyURI); //add link to alexa page
				for (String countryCode : userData.keySet()) { // for each country in the user distribution data
					Resource country = countryIndex.get(countryCode); // retrieve country resource from index
					if (country != null) {
						Resource node = ontologyModel.createResource(); // create anonymous node
						platformResource.addProperty(usedIn, node); // connect platform and anonymous node
						node.addProperty(rdfType, userDistributionClass); // anonamous node is of type User_Distribution
						// create a dateTime literal for the current time
						Literal dateLiteral = ontologyModel.createTypedLiteral(GregorianCalendar.getInstance());
						node.addLiteral(date, dateLiteral); // add the retrieval date to the anonymous node
						node.addLiteral(percentage, userData.get(countryCode)); // add the user percentage
						node.addProperty(locationCountry, country); // link the country
					}
				}
				++success; // count successes for log output
			}
		}
		log.info("Found user distribution data for " + success + " out of " + platforms.size() + " platforms.");

		return ontologyModel;
	}

	/**
	 * Parses the user country data from the given alexa page if possible
	 * 
	 * @param website
	 *            The website to lookup on alexa
	 * @return Map with country names as keys and percentage of overall users as values
	 */
	public static Map<String, Double> parseAlexa(String website) {
		// turn off output of irrelevant information
		java.util.logging.Logger.getLogger("com.gargoylesoftware.htmlunit").setLevel(Level.OFF);

		WebClient webClient = new WebClient(); // create virtual browser
		webClient.getOptions().setJavaScriptEnabled(false); // faster execution, less errors
		webClient.getOptions().setCssEnabled(false);

		HtmlPage currentPage;
		try {
			currentPage = webClient.getPage("http://www.alexa.com/siteinfo/" + website); // browse to page

			// find the country table
			HtmlTable countryTable = currentPage.getHtmlElementById("demographics_div_country_table");
			if (countryTable != null) {
				String cssClass = countryTable.getAttribute("class"); // read the table's css classes
	
				// if "data-table-nodata" is not among the table's classes, data is available
				if (!cssClass.contains("data-table-nodata")) {
					// parse table rows
					DomNodeList<HtmlElement> countryTableRows = countryTable.getHtmlElementsByTagName("tbody").get(0).getElementsByTagName("tr");
	
					Map<String, Double> tableData = new HashMap<String, Double>(); // map to output
	
					for (HtmlElement line : countryTableRows) { // for each row
						DomNodeList<HtmlElement> cells = line.getElementsByTagName("td"); // parse row cells
						// remove the % sign from the second cell's content and convert to double
						Double percentage = Double.parseDouble(cells.get(1).asText().replace("%", "").trim());
						// retrieve country code from link's href
						String code = cells.get(0).getElementsByTagName("a").get(0).getAttribute("href").replace("/topsites/countries/", "");
						tableData.put(code, percentage); // add to output map
					}
					return tableData;
				}
			}
		} catch (Exception e) {
			System.out.println("Error parsing Alexa page for website " + website);
		}
		return null;
	}
	
	private List<Platform> getAllPlatforms() {
		String sparqlQuery = "PREFIX d2s: <http://www.discover2share.net/d2s-ont/> "
				+ "PREFIX dbpp: <http://dbpedia.org/property/> "
				+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" + "Select * {"
				+ "	?resourceName rdf:type d2s:P2P_SCC_Platform ." 
				+ " OPTIONAL{ ?resourceName rdfs:label ?label }." 
				+ " ?resourceName dbpp:url ?url."
				+ "}"
				+ "ORDER BY ?resourceName"; // ordered by the platforms' resource names

		Query query = QueryFactory.create(sparqlQuery); // construct query
		QueryExecution qexec = QueryExecutionFactory.sparqlService(QUERYENDPOINT, query); // query endpoint
		ResultSet results = qexec.execSelect(); // execute

		List<Platform> platforms = new ArrayList<Platform>();
		Platform currentPlatform = new Platform();
		while (results.hasNext()) { // for each result row
			QuerySolution result = results.next();
			
			// if current row describes a different platform than before
			if(result.get("resourceName").asResource().getURI() != currentPlatform.getResourceName()){
				currentPlatform = new Platform(); // create new platform object
				platforms.add(currentPlatform); // add to output list
			}
			
			for (String var : results.getResultVars()) { // for every variable in the result set
				RDFNode node = result.get(var); // receive RDF node for the current variable
				if (node == null)
					continue; // skip if node is null

				if (node.isLiteral()) { // if node is literal
					String literal = node.asLiteral().getString(); // extract string representation
					currentPlatform.set(var, literal); // set in platform object
				} else if (node.isResource()) { // if node is resource
					String uri = node.asResource().getURI(); // get its URI
					if (uri != null)
						currentPlatform.set(var, uri); // set in platform object
				}
			}
		}
		qexec.close();
		return platforms;
	}

}
