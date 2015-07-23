import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

public class CountryGenerator {

	private final static String RDF = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	private final static String RDFS = "http://www.w3.org/2000/01/rdf-schema#";
	private final static String D2S = "http://www.discover2share.net/d2s-ont/";
	private final static String SKOS = "http://www.w3.org/2004/02/skos/core#";
	private final static String DBPP = "http://dbpedia.org/property/";
	
	private final Logger log = Logger.getLogger(CountryGenerator.class);

	private Property rdfType;
	private Property rdfsLabel;
	private Property skosRelatedMatch;

	private OntModel ontologyModel;

	private Map<String, Resource> continents;

	public static void main(String[] args) {
		CountryGenerator generator = new CountryGenerator();

		OntModel ontologyModel = generator.getOntologyModel();

		File outputFile = new File("D:\\countries.owl");
		try (OutputStream out = new FileOutputStream(outputFile)) {
			ontologyModel.write(out, "RDF/XML"); // "RDF/XML"
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public CountryGenerator() {
		ontologyModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
		ontologyModel.setNsPrefix("skos", SKOS);
		ontologyModel.setNsPrefix("d2s-ont", D2S);
		ontologyModel.setNsPrefix("dbpp", DBPP);

		rdfType = ontologyModel.createProperty(RDF + "type");
		rdfsLabel = ontologyModel.createProperty(RDFS + "label");
		skosRelatedMatch = ontologyModel.createProperty(SKOS + "relatedMatch");

		generateContinents();
		generateCountries();
	}

	private void generateContinents() {
		Property continentCode = ontologyModel.createProperty(D2S + "continent_code");
		Resource continent = ontologyModel.createResource(D2S + "Continent");

		continents = new HashMap<String, Resource>();

		// Africa
		Resource africa = ontologyModel.createResource(D2S + "Africa");
		africa.addProperty(rdfType, continent);
		africa.addLiteral(rdfsLabel, "Africa");
		africa.addProperty(skosRelatedMatch, ontologyModel.createResource("http://www.geonames.org/6255146"));
		africa.addLiteral(continentCode, "AF");
		africa.addProperty(skosRelatedMatch, ontologyModel.createResource("http://dbpedia.org/resource/Africa"));
		continents.put("AF", africa);

		// Antarctica
		Resource antarctica = ontologyModel.createResource(D2S + "Antarctica");
		antarctica.addProperty(rdfType, continent);
		antarctica.addLiteral(rdfsLabel, "Antarctica");
		antarctica.addProperty(skosRelatedMatch, ontologyModel.createResource("http://www.geonames.org/6255152"));
		antarctica.addLiteral(continentCode, "AN");
		antarctica.addProperty(skosRelatedMatch, ontologyModel.createResource("http://dbpedia.org/resource/Antarctica"));
		continents.put("AN", antarctica);

		// Asia
		Resource asia = ontologyModel.createResource(D2S + "Asia");
		asia.addProperty(rdfType, continent);
		asia.addLiteral(rdfsLabel, "Asia");
		asia.addProperty(skosRelatedMatch, ontologyModel.createResource("http://www.geonames.org/6255147"));
		asia.addLiteral(continentCode, "AS");
		asia.addProperty(skosRelatedMatch, ontologyModel.createResource("http://dbpedia.org/resource/Asia"));
		continents.put("AS", asia);

		// Europe
		Resource europe = ontologyModel.createResource(D2S + "Europe");
		europe.addProperty(rdfType, continent);
		europe.addLiteral(rdfsLabel, "Europe");
		europe.addProperty(skosRelatedMatch, ontologyModel.createResource("http://www.geonames.org/6255148"));
		europe.addLiteral(continentCode, "EU");
		europe.addProperty(skosRelatedMatch, ontologyModel.createResource("http://dbpedia.org/resource/Europe"));
		continents.put("EU", europe);

		// Northern America
		Resource northernAmerica = ontologyModel.createResource(D2S + "Northern_America");
		northernAmerica.addProperty(rdfType, continent);
		northernAmerica.addLiteral(rdfsLabel, "Northern America");
		northernAmerica.addProperty(skosRelatedMatch, ontologyModel.createResource("http://www.geonames.org/7729890"));
		northernAmerica.addLiteral(continentCode, "NA");
		northernAmerica.addProperty(skosRelatedMatch,
				ontologyModel.createResource("http://dbpedia.org/resource/Northern_America"));
		continents.put("NA", northernAmerica);

		// Oceania
		Resource oceania = ontologyModel.createResource(D2S + "Oceania");
		oceania.addProperty(rdfType, continent);
		oceania.addLiteral(rdfsLabel, "Oceania");
		oceania.addProperty(skosRelatedMatch, ontologyModel.createResource("http://www.geonames.org/6255151"));
		oceania.addLiteral(continentCode, "OC");
		oceania.addProperty(skosRelatedMatch, ontologyModel.createResource("http://dbpedia.org/resource/Oceania"));
		continents.put("OC", oceania);

		// South America
		Resource southAmerica = ontologyModel.createResource(D2S + "South_America");
		southAmerica.addProperty(rdfType, continent);
		southAmerica.addLiteral(rdfsLabel, "South America");
		southAmerica.addProperty(skosRelatedMatch, ontologyModel.createResource("http://www.geonames.org/6255150"));
		southAmerica.addLiteral(continentCode, "SA");
		southAmerica.addProperty(skosRelatedMatch,
				ontologyModel.createResource("http://dbpedia.org/resource/South_America"));
		continents.put("SA", southAmerica);
	}

	public void generateCountries() {
		Resource countryClass = ontologyModel.createResource(D2S + "Country");
		Property countryCode = ontologyModel.createProperty(DBPP + "countryCode");
		Property continent = ontologyModel.createProperty(DBPP + "continent");

		try {
			JSONObject json = JsonReader
					.readJsonFromUrl("http://localhost:8080/discover2share-Web/resources/js/countries.json");
			JSONArray a = json.getJSONArray("countries");
			for (int i = 0; i < a.length(); i++) {
				JSONObject country = a.getJSONObject(i);

				log.info("Constructing " + country.getString("countryName") + " (" + (i + 1) + "/" + a.length() + ")");

				String countryIRI = D2S
						+ country.getString("countryName").replace(" ", "_").replace("[", "%5B").replace("]", "%5D");
				Resource countryResource = ontologyModel.createResource(countryIRI);
				countryResource.addProperty(rdfType, countryClass);
				countryResource.addLiteral(rdfsLabel, country.getString("countryName"));
				Resource geonamesId = ontologyModel.createResource("http://www.geonames.org/"
						+ country.getString("countryId"));
				countryResource.addProperty(skosRelatedMatch, geonamesId);
				countryResource.addLiteral(countryCode, country.getString("countryCode"));
				countryResource.addProperty(continent, continents.get(country.getString("continentCode")));

				JSONObject countryInfo = JsonReader
						.readJsonFromUrl("http://api.geonames.org/getJSON?username=discover2share&geonameId="
								+ country.getString("countryId"));
				if (countryInfo.has("wikipediaURL")) {
					Resource dbpedia = ontologyModel.createResource("http://dbpedia.org/resource"
							+ countryInfo.getString("wikipediaURL").replace("en.wikipedia.org/wiki", ""));
					countryResource.addProperty(skosRelatedMatch, dbpedia);
				}
			}
		} catch (IOException | JSONException e1) {
			e1.printStackTrace();
		}
	}

	public OntModel getOntologyModel() {
		return ontologyModel;
	}
}
