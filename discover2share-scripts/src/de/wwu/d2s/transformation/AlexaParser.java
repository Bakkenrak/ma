package de.wwu.d2s.transformation;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTable;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.update.UpdateExecutionFactory;
import com.hp.hpl.jena.update.UpdateFactory;

import de.wwu.d2s.ejb.OntologyService;
import de.wwu.d2s.ejb.OntologyServiceBean;
import de.wwu.d2s.jpa.Platform;
import de.wwu.d2s.util.JsonReader;

/**
 * Provides a class to parse information about user countries from an Alexa.com page. If executed directly, it will insert the asserted data using the defined
 * SPARQL Update ENDPOINT.
 */
public class AlexaParser {

	private static Logger log;

	private static Map<String, Resource> countryIndex = new HashMap<String, Resource>();

	private static String ENDPOINT = "http://localhost:3030/Testify/update"; // SPARQL Update Endpoint to insert triples into
	// JSON file containing all countries of the ontology
	private static String COUNTRIESJSON = "http://localhost:8080/discover2share-Web/resources/js/countries.json"; 

	// ontology namespaces
	private final static String D2S = "http://www.discover2share.net/d2s-ont/";
	private final static String DBPP = "http://dbpedia.org/property/";
	private final static String RDF = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	private final static String DCT = "http://purl.org/dc/terms/";

	public static void main(String[] args) {
		log = Logger.getLogger(AlexaParser.class.getName()); // instantiate logger

		// Ontology Model to create new data in
		OntModel ontologyModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
		// instantiate commonly used Properties and Resource
		Property rdfType = ontologyModel.createProperty(RDF + "type");
		Resource userDistributionClass = ontologyModel.createResource(D2S + "User_Distribution");
		Property usedIn = ontologyModel.createProperty(D2S + "used_in");
		Property date = ontologyModel.createProperty(DCT + "date");
		Property percentage = ontologyModel.createProperty(D2S + "user_percentage");
		Property locationCountry = ontologyModel.createProperty(DBPP + "locationCountry");

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

		int success = 0;
		OntologyService ontologyService = new OntologyServiceBean();
		List<Platform> platforms = ontologyService.getAllPlatforms(); // retrieve all platforms from the ontology
		for (Platform platform : platforms) { // for each platform
			Map<String, Double> userData = parseAlexa(platform.getUrl()); // parse user distribution data from Alexa
			if (userData == null) {
				log.info("No user distribution information found on Alexa for " + platform.getLabel());
			} else {
				// create platform resource in ontology model
				Resource platformResource = ontologyModel.createResource(D2S + platform.getResourceName());
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
				++success;
			}
		}
		log.info("Found user distribution data for " + success + " out of " + platforms.size() + " platforms.");

		log.info("Inserting new triples into Triplestore");
		OutputStream baos = new ByteArrayOutputStream();
		ontologyModel.write(baos, "N-TRIPLE"); // transform data in ontology model into triples
		String query = "INSERT DATA { " + baos.toString() + "}"; // build insert query
		UpdateExecutionFactory.createRemote(UpdateFactory.create(query), ENDPOINT).execute(); // execute update to endpoint
		log.info("Done");
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
		} catch (FailingHttpStatusCodeException | IOException e) {
			System.out.println("Error parsing Alexa page for website " + website);
		}
		return null;
	}

}
