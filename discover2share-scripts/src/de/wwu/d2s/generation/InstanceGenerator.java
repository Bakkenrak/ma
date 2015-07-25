package de.wwu.d2s.generation;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

import de.wwu.d2s.util.JsonReader;

/**
 * A class offering methods to generate all countries and continents on earth as instances of their respective D2S
 * ontology classes. Furthermore all languages by ISO639-1 standard are generated.
 * The output is written into a text file.
 * 
 * The output of this generator was already included in the discover2share ontology.
 */
public class InstanceGenerator {
	// Ontology namespaces
	private final static String RDF = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	private final static String RDFS = "http://www.w3.org/2000/01/rdf-schema#";
	private final static String D2S = "http://www.discover2share.net/d2s-ont/";
	private final static String SKOS = "http://www.w3.org/2004/02/skos/core#";
	private final static String DBPP = "http://dbpedia.org/property/";

	// Instantiate logger
	private final Logger log = Logger.getLogger(InstanceGenerator.class);

	// Properties defined centrally to be used in every method
	private Property rdfType;
	private Property rdfsLabel;
	private Property skosRelatedMatch;

	// The central component of the output
	private OntModel ontologyModel;

	// Map with continent codes as keys and the respective continent resources as values
	private Map<String, Resource> continents;

	public static void main(String[] args) {
		// Have user enter the desired output file path
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("Path and filename to save output file under:");
		String owl = null;
		try {
			owl = br.readLine();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		if (owl == null) // quit if no user input provided
			return;

		InstanceGenerator generator = new InstanceGenerator(); // Create new instance of this class

		OntModel ontologyModel = generator.getOntologyModel(); // Start the generation

		File outputFile = new File(owl); // Create file object to save output in
		try (OutputStream out = new FileOutputStream(outputFile)) {
			ontologyModel.write(out, "RDF/XML"); // Write output as RDF/XML to the file. Could also be e.g. Turtle.
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public InstanceGenerator() {
		// Create a new model
		ontologyModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
		// Set prefixes to use in output
		ontologyModel.setNsPrefix("skos", SKOS);
		ontologyModel.setNsPrefix("d2s-ont", D2S);
		ontologyModel.setNsPrefix("dbpp", DBPP);

		// Instantiate commonly used properties
		rdfType = ontologyModel.createProperty(RDF + "type");
		rdfsLabel = ontologyModel.createProperty(RDFS + "label");
		skosRelatedMatch = ontologyModel.createProperty(SKOS + "relatedMatch");

		generateContinents(); // generate and add all continents to the model
		generateCountries(); // generate and add all countries to the model
		generateLanguages(); // generate and add all languages to the model
	}

	/**
	 * Creates instances of class d2s:Continent for every continent. Links them to their dbpedia and geonames
	 * equivalents.
	 */
	private void generateContinents() {
		// Instantiate commonly used properties
		Property continentCode = ontologyModel.createProperty(D2S + "continent_code");
		Resource continent = ontologyModel.createResource(D2S + "Continent");

		continents = new HashMap<String, Resource>();

		// Africa
		Resource africa = ontologyModel.createResource(D2S + "Africa"); // create new resource
		africa.addProperty(rdfType, continent); // of type d2s:Continent
		africa.addProperty(rdfsLabel, "Africa");
		// link to geonames equivalent
		africa.addProperty(skosRelatedMatch, ontologyModel.createResource("http://www.geonames.org/6255146"));
		africa.addProperty(continentCode, "AF"); // add continent code
		// link to DBpedia equivalent
		africa.addProperty(skosRelatedMatch, ontologyModel.createResource("http://dbpedia.org/resource/Africa"));
		continents.put("AF", africa); // add to map for future re-use

		// Antarctica
		Resource antarctica = ontologyModel.createResource(D2S + "Antarctica");
		antarctica.addProperty(rdfType, continent);
		antarctica.addProperty(rdfsLabel, "Antarctica");
		antarctica.addProperty(skosRelatedMatch, ontologyModel.createResource("http://www.geonames.org/6255152"));
		antarctica.addProperty(continentCode, "AN");
		antarctica
				.addProperty(skosRelatedMatch, ontologyModel.createResource("http://dbpedia.org/resource/Antarctica"));
		continents.put("AN", antarctica);

		// Asia
		Resource asia = ontologyModel.createResource(D2S + "Asia");
		asia.addProperty(rdfType, continent);
		asia.addProperty(rdfsLabel, "Asia");
		asia.addProperty(skosRelatedMatch, ontologyModel.createResource("http://www.geonames.org/6255147"));
		asia.addProperty(continentCode, "AS");
		asia.addProperty(skosRelatedMatch, ontologyModel.createResource("http://dbpedia.org/resource/Asia"));
		continents.put("AS", asia);

		// Europe
		Resource europe = ontologyModel.createResource(D2S + "Europe");
		europe.addProperty(rdfType, continent);
		europe.addProperty(rdfsLabel, "Europe");
		europe.addProperty(skosRelatedMatch, ontologyModel.createResource("http://www.geonames.org/6255148"));
		europe.addProperty(continentCode, "EU");
		europe.addProperty(skosRelatedMatch, ontologyModel.createResource("http://dbpedia.org/resource/Europe"));
		continents.put("EU", europe);

		// Northern America
		Resource northernAmerica = ontologyModel.createResource(D2S + "Northern_America");
		northernAmerica.addProperty(rdfType, continent);
		northernAmerica.addProperty(rdfsLabel, "Northern America");
		northernAmerica.addProperty(skosRelatedMatch, ontologyModel.createResource("http://www.geonames.org/7729890"));
		northernAmerica.addProperty(continentCode, "NA");
		northernAmerica.addProperty(skosRelatedMatch,
				ontologyModel.createResource("http://dbpedia.org/resource/Northern_America"));
		continents.put("NA", northernAmerica);

		// Oceania
		Resource oceania = ontologyModel.createResource(D2S + "Oceania");
		oceania.addProperty(rdfType, continent);
		oceania.addProperty(rdfsLabel, "Oceania");
		oceania.addProperty(skosRelatedMatch, ontologyModel.createResource("http://www.geonames.org/6255151"));
		oceania.addProperty(continentCode, "OC");
		oceania.addProperty(skosRelatedMatch, ontologyModel.createResource("http://dbpedia.org/resource/Oceania"));
		continents.put("OC", oceania);

		// South America
		Resource southAmerica = ontologyModel.createResource(D2S + "South_America");
		southAmerica.addProperty(rdfType, continent);
		southAmerica.addProperty(rdfsLabel, "South America");
		southAmerica.addProperty(skosRelatedMatch, ontologyModel.createResource("http://www.geonames.org/6255150"));
		southAmerica.addProperty(continentCode, "SA");
		southAmerica.addProperty(skosRelatedMatch,
				ontologyModel.createResource("http://dbpedia.org/resource/South_America"));
		continents.put("SA", southAmerica);
	}

	/**
	 * Generates instances of class d2s:Country for every country on earth. Links them to their respective DBpedia and
	 * geonames equivalents.
	 */
	public void generateCountries() {
		// instantiate commonly used properties
		Resource countryClass = ontologyModel.createResource(D2S + "Country");
		Property countryCode = ontologyModel.createProperty(DBPP + "countryCode");
		Property continent = ontologyModel.createProperty(DBPP + "continent");

		try {
			// retrieve countries from discover2share server
			JSONObject json = JsonReader
					.readJsonFromUrl("http://localhost:8080/discover2share-Web/resources/js/countries.json");
			JSONArray a = json.getJSONArray("countries"); // take the list of countries
			for (int i = 0; i < a.length(); i++) { // for each
				JSONObject country = a.getJSONObject(i);

				log.info("Constructing " + country.getString("countryName") + " (" + (i + 1) + "/" + a.length() + ")");

				// Replace illegal characters in the name to use as in the resource IRI.
				String countryIRI = D2S
						+ country.getString("countryName").replace(" ", "_").replace("[", "%5B").replace("]", "%5D");
				Resource countryResource = ontologyModel.createResource(countryIRI); // create a new resource
				countryResource.addProperty(rdfType, countryClass); // of type d2s:Country
				countryResource.addProperty(rdfsLabel, country.getString("countryName"));
				Resource geonamesId = ontologyModel.createResource("http://www.geonames.org/"
						+ country.getString("countryId")); // create resource for the geonames equivalent
				countryResource.addProperty(skosRelatedMatch, geonamesId); // link to the geonames equivalent
				countryResource.addProperty(countryCode, country.getString("countryCode")); // all country code property
				// link country to the continent it is situated in
				countryResource.addProperty(continent, continents.get(country.getString("continentCode")));

				JSONObject countryInfo = JsonReader
						.readJsonFromUrl("http://api.geonames.org/getJSON?username=discover2share&geonameId="
								+ country.getString("countryId")); // retrieve extended info on the country
				if (countryInfo.has("wikipediaURL")) { // if a wikipedia url is contained in the extended info
					// create a resource to the DBpedia entity for that wikipedia article
					Resource dbpedia = ontologyModel.createResource("http://dbpedia.org/resource"
							+ countryInfo.getString("wikipediaURL").replace("en.wikipedia.org/wiki", ""));
					countryResource.addProperty(skosRelatedMatch, dbpedia); // link the two equivalents
				}
			}
		} catch (IOException | JSONException e1) {
			e1.printStackTrace();
		}
	}

	/**
	 * Generates instances of d2s:Language for every language in the ISO639-1 standard.
	 */
	public void generateLanguages() {
		// Create commonly used resources and properties
		Resource languageClass = ontologyModel.createResource(D2S + "Language");
		Property languageCode = ontologyModel.createProperty(D2S + "language_code");

		try {
			// retrieve the list of languages from the discover2share server
			JSONObject json = JsonReader
					.readJsonFromUrl("http://localhost:8080/discover2share-Web/resources/js/languages.json");
			JSONArray a = json.getJSONArray("languages"); // get the array containing the languages
			for (int i = 0; i < a.length(); i++) { // for each
				JSONObject language = a.getJSONObject(i);

				log.info("Constructing " + language.getString("name") + " (" + (i + 1) + "/" + a.length() + ")");

				// Create a new resource
				Resource languageResource = ontologyModel.createResource(D2S + language.getString("resourceName"));
				languageResource.addProperty(rdfType, languageClass); // of type d2s:Language
				languageResource.addProperty(rdfsLabel, language.getString("name"));
				// create resource for dbpedia equivalent
				Resource dbpedia = ontologyModel.createResource(language.getString("dbpedia"));
				languageResource.addProperty(skosRelatedMatch, dbpedia); // link the equivalents
				languageResource.addProperty(languageCode, language.getString("code")); // add language code
			}
		} catch (IOException | JSONException e1) {
			e1.printStackTrace();
		}
	}

	public OntModel getOntologyModel() {
		return ontologyModel;
	}
}
