package de.wwu.d2s.ejb;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

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
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.update.UpdateExecutionFactory;
import com.hp.hpl.jena.update.UpdateFactory;

import de.wwu.d2s.jpa.Platform;
import de.wwu.d2s.util.OntologyWriter;

@Stateless
public class OntologyServiceBean implements OntologyService {
	
	private static final String ONTOLOGYURL = "http://localhost:3030/d2s-ont";
	private static final String ENDPOINT = ONTOLOGYURL + "/query";
	private static final String UPDATEENDPOINT = ONTOLOGYURL + "/update";
	
	
	@PersistenceContext
	EntityManager em;

	@Override
	public List<Platform> getAllPlatforms() {
		String sparqlQuery = "PREFIX d2s: <http://www.discover2share.net/d2s-ont/> "
				+ "PREFIX dbpp: <http://dbpedia.org/property/> "
				+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" + "Select * {"
				+ "	?resourceName rdf:type d2s:P2P_SCC_Platform ." 
				+ " OPTIONAL{ ?resourceName rdfs:label ?label }." 
				+ " ?resourceName dbpp:url ?url."
				+ " OPTIONAL{ ?resourceName d2s:has_resource_type ?rt ." 
				+ " 		  ?rt rdfs:label ?resourceType }." + "}"
				+ "ORDER BY ?resourceName"; // ordered by the platforms' resource names

		Query query = QueryFactory.create(sparqlQuery); // construct query
		QueryExecution qexec = QueryExecutionFactory.sparqlService(ENDPOINT, query); // query endpoint
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

	@Override
	public Platform getPlatform(String name) {
		String sparqlQuery = "PREFIX d2s: <http://www.discover2share.net/d2s-ont/> "
				+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
				+ "PREFIX dbpp: <http://dbpedia.org/property/> " 
				+ "PREFIX dbpo: <http://dbpedia.org/ontology/> "
				+ "PREFIX owl: <http://www.w3.org/2002/07/owl#> "
				+ "PREFIX dct: <http://purl.org/dc/terms/> "
				+ "Select * " + "WHERE { "
				+ getPlatformQuery(name)
				+ "}";

		Query query = QueryFactory.create(sparqlQuery); // construct query
		QueryExecution qexec = QueryExecutionFactory.sparqlService(ENDPOINT, query); // query endpoint
		ResultSet results = qexec.execSelect(); // execute

		Platform platform = null;
		if (results.hasNext()) // if there are any results
			platform = new Platform(); // instantiate new platform object
		
		while (results.hasNext()) { // iterate through results
			QuerySolution result = results.next();
			for (String var : results.getResultVars()) { // for every variable in the result set
				RDFNode node = result.get(var); // receive RDF node for the current variable
				if (node == null)
					continue; // skip if node is null

				if (node.isLiteral()) { // if node is literal
					String literal = node.asLiteral().getString(); // extract string representation
					platform.set(var, literal); // set in platform object
				} else if (node.isResource()) { // if node is resource
					String uri = node.asResource().getURI(); // get its URI
					if (uri != null)
						platform.set(var, uri); // set in platform object
				}
			}
		}
		qexec.close();
		return platform;
	}

	@Override
	public Map<String, Map<String, String>> getDescriptions() {
		// create a model using inference reasoner
		OntModel model1 = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF);
		
		java.util.logging.Logger.getLogger("org.apache.jena.riot").setLevel(Level.SEVERE); // disable unnecessary warnings
		// read the ontology, transform into model
		model1.read(ONTOLOGYURL, "Turtle");
		java.util.logging.Logger.getLogger("org.apache.jena.riot").setLevel(Level.ALL); // reset to normal logging

		// Create a new query to find all (transitive) subclasses of P2P_SCC_Dimension
		String queryString = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
				+ "PREFIX d2s: <http://www.discover2share.net/d2s-ont/> " 
				+ "SELECT * " 
				+ "WHERE { "
				+ "  ?c rdfs:subClassOf d2s:P2P_SCC_Dimension ."
				+ "  ?c rdfs:label ?label."
				+ "  ?c rdfs:comment ?comment." 
				+ "}";
		Query query = QueryFactory.create(queryString);
		QueryExecution qe = QueryExecutionFactory.create(query, model1); // query model
		ResultSet results = qe.execSelect();
		
		Map<String, Map<String, String>> output = new HashMap<String, Map<String, String>>();
		while(results.hasNext()){ // iterate through result set
			Map<String, String> row = new HashMap<String, String>(); // each row represented as a map
			
			QuerySolution result = results.nextSolution();
			// retrieve the current dimension class's URI
			String c = result.getResource("c").getURI().replace("http://www.discover2share.net/d2s-ont/", "");
			row.put("label", result.getLiteral("label").getString()); // put its label in the row map
			row.put("comment", result.getLiteral("comment").getString()); // put its description in the row map
			output.put(c, row); // put into output map, identify by the dimension class's URI
		}
		qe.close();
		return output;
	}
	
	@Override
	public Map<String, String> addSuggestion(Platform platform){
		Map<String, String> output = new HashMap<String, String>();
		try {
			em.persist(platform); // save platform suggestion object in relational database
			em.flush(); // makes sure that an ID is auto-generated and set in the object
			output.put("success", platform.getExternalId()); // return map containing the newly generated external ID
		} catch (Exception e) {
			output.put("error", e.getMessage()); // return error message if something went wrong
		}
		return output;
	}
	
	@Override
	public void directSaveSuggestion(Platform platform) {
		OntologyWriter o = new OntologyWriter();
		OntModel model;
		if (platform.getEditFor() != null && !platform.getEditFor().isEmpty()) { // if it's an edit suggestion for a platform in the ontology
			removePlatform(platform.getEditFor(), false); // remove this first from the ontology, but not possible non-standard triples referencing it
			model = o.constructPlatform(platform, platform.getEditFor()); // construct an ontology model of the platform using the old ID
		} else {
			model = o.constructPlatform(platform); // construct an ontology model of the platform (will use a completely new ID)
		}
		
		OutputStream baos = new ByteArrayOutputStream();
		model.write(baos, "N-TRIPLE"); // transform data in ontology model into triples
		String query = "INSERT DATA { " + baos.toString() + "}"; // build insert query
		UpdateExecutionFactory.createRemote(UpdateFactory.create(query), UPDATEENDPOINT).execute(); // execute update to endpoint
	}

	@Override
	public List<Platform> getAllSuggestions() {
		return em.createQuery("from Platform", Platform.class).getResultList(); // get all suggestions in the database
	}

	@Override
	public Platform getSuggestion(int id) {
		return em.find(Platform.class, id); // get suggestion with the given ID from the database
	}

	@Override
	public Map<String, String> doQuery(String query) {
		if(query.substring(0, 6).equals("query=")) // cut off possible leading 'query=' marker
			query = query.substring(6);
		try { // query will be in application/x-www-form-urlencoded format
			query = URLDecoder.decode(query, "UTF-8"); // convert to UTF-8 to avoid querying errors
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		Map<String, String> output = new HashMap<String, String>();
		try {
			Query sparqlQuery = QueryFactory.create(query);
			QueryExecution qexec = QueryExecutionFactory.sparqlService(ENDPOINT, sparqlQuery); // query endpoint
			ResultSet results = qexec.execSelect();
			ByteArrayOutputStream b = new ByteArrayOutputStream();
			ResultSetFormatter.outputAsJSON(b, results); // format result set as JSON in byte stream
			String json = b.toString(); // create string from JSON byte stream
			qexec.close();
			output.put("success", json); // output json string
		} catch(Exception e) {
			output.put("error", e.getMessage()); // output error message if something went wrong
		}
		return output;
	}

	@Override
	public List<Map<String, String>> getAllCities() {
		String sparqlQuery = "PREFIX d2s: <http://www.discover2share.net/d2s-ont/> "
				+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
				+ "PREFIX dbpp: <http://dbpedia.org/property/> "
				+ "SELECT ?resourceName ?name ?countryCode {"
				+ "  ?resourceName rdf:type d2s:City."
				+ "  ?resourceName rdfs:label ?name."
				+ "  ?resourceName dbpp:locationCountry ?country."
				+ "  ?country dbpp:countryCode ?countryCode.}";
		
		Query query = QueryFactory.create(sparqlQuery);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(ENDPOINT, query); // query endpoint
		ResultSet results = qexec.execSelect();

		List<Map<String, String>> output = new ArrayList<Map<String, String>>();
		while (results.hasNext()) { // for each row in the result set
			Map<String, String> current = new HashMap<String, String>(); // map representing the row
			QuerySolution result = results.next();
			for (String var : results.getResultVars()) { // for every variable in the result set
				RDFNode node = result.get(var); // receive RDF node for the current variable
				if (node == null)
					continue; // skip if node is null

				if (node.isLiteral()) { // if node is literal
					String literal = node.asLiteral().getString(); // extract string representation
					current.put(var, literal); // put into row map
				} else if (node.isResource()) { // if node is resource
					String uri = node.asResource().getURI(); // get its URI
					if (uri != null)
						current.put(var, uri.substring(38)); // put into row map
				}
			}
			output.add(current);
		}
		qexec.close();
		return output;
	}

	@Override
	public List<Map<String, String>> getAllResourceTypes() {
		String sparqlQuery = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
				+ "PREFIX d2s: <http://www.discover2share.net/d2s-ont/>"
				+ "Select ?resourceName ?name {"
				+ "  ?resourceName rdfs:subClassOf d2s:Resource_Type."
				+ "  ?resourceName rdfs:label ?name"
				+ "}";
		
		Query query = QueryFactory.create(sparqlQuery);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(ENDPOINT, query);
		ResultSet results = qexec.execSelect();

		List<Map<String, String>> output = new ArrayList<Map<String, String>>();
		while (results.hasNext()) { // for each row in the result set
			Map<String, String> current = new HashMap<String, String>(); // map representing the row
			QuerySolution result = results.next();
			for (String var : results.getResultVars()) { // for every variable in the result set
				RDFNode node = result.get(var); // receive RDF node for the current variable
				if (node == null)
					continue; // skip if node is null

				if (node.isLiteral()) { // if node is literal
					String literal = node.asLiteral().getString(); // extract string representation
					current.put(var, literal); // put into row map
				} else if (node.isResource()) { // if node is resource
					String uri = node.asResource().getURI(); // get its URI
					if (uri != null)
						current.put(var, uri.substring(38)); // put into row map
				}
			}
			output.add(current);
		}
		qexec.close();
		return output;
	}

	@Override
	public void deleteSuggestion(int id) {
		Platform p = em.find(Platform.class, id); // find suggestion with the given database
		if (p != null) // if found
			em.remove(p); // delete
	}

	@Override
	public void saveSuggestion(int id) {
		Platform p = em.find(Platform.class, id); // find suggestion with the given ID in the database
		if(p != null) { // if found
			directSaveSuggestion(p); // add to ontology
			em.remove(p); // remove suggestion from database
		}
	}

	@Override
	public void editSuggestion(Platform suggestion) {
		em.merge(suggestion); // update the given suggestion in the database
	}
	
	@Override
	public void removePlatform(String uri) {
		removePlatform(uri, true); // remove the platform with the given URI including all possible additional triples referencing it
	}
	
	private void removePlatform(String id, boolean removeCompletely) {
		String query = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " // build delete query
						+ "PREFIX d2s: <http://www.discover2share.net/d2s-ont/> "
						+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
						+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
						+ "PREFIX dbpp: <http://dbpedia.org/property/> "
						+ "PREFIX dbpo: <http://dbpedia.org/ontology/> "
						+ "PREFIX owl: <http://www.w3.org/2002/07/owl#> "
						+ "PREFIX dct: <http://purl.org/dc/terms/> "
						+ "DELETE { ";
		if (removeCompletely) { // if platform is to be deleted completely
			query += "d2s:" + id + " ?a ?b. " // delete all statements with the platform as subject
						+ "?c ?d d2s:" + id + ". " // or object
						+ "d2s:" + id + " d2s:used_in ?userDistribution. " // also remove user distribution info
						+ "?userDistribution dbpp:locationCountry ?userCountry. "
						+ "?userDistribution d2s:user_percentage ?userPercentage. "
						+ "?userDistribution dct:date ?distributionDate. ";
		} else { // remove the standard triples of a platform description that are otherwise already caught with the 'd2s:... ?a ?b' statement when removing completely
			query += "d2s:" + id + " rdf:type d2s:P2P_SCC_Platform . "
						+ "d2s:" + id + " rdfs:label ?label . "
						+ "d2s:" + id + " dbpp:url ?url . "
						+ "d2s:" + id + " rdfs:comment ?description . "
						+ "d2s:" + id + " d2s:has_resource_type ?rt . "
						+ "d2s:" + id + " d2s:has_consumer_involvement ?ci . "
						+ "d2s:" + id + " dbpp:launchYear ?yearLaunch . "
						+ "d2s:" + id + " d2s:has_market_mediation ?me . "
						+ "d2s:" + id + " d2s:has_money_flow ?mf . "
						+ "d2s:" + id + " d2s:promotes ?co . "
						+ "d2s:" + id + " d2s:has_resource_owner ?ro . "
						+ "d2s:" + id + " d2s:min_service_duration ?serviceDurationMin . "
						+ "d2s:" + id + " d2s:max_service_duration ?serviceDurationMax . "
						+ "d2s:" + id + " d2s:has_app ?ap . "
						+ "d2s:" + id + " d2s:has_trust_contribution ?tc . "
						+ "d2s:" + id + " d2s:accessed_object_has_type ?ot . "
						+ "d2s:" + id + " dbpp:language ?language . ";
		}
		query += "d2s:" + id + " d2s:launched_in ?launch . " // remove dimension info that uses intermediate nodes
					+ "?launch dbpp:locationCity ?launchCity . "
					+ "?launch dbpp:locationCountry ?launchCountry . "
					+ "d2s:" + id + " d2s:operator_resides_in ?residence . "
					+ "?residence dbpp:locationCity ?residenceCity . "
					+ "?residence dbpp:locationCountry ?residenceCountry . "
					+ "d2s:" + id + " d2s:has_market_integration ?integration . "
					+ "?integration rdf:type d2s:Market_Integration . "
					+ "?integration d2s:markets_are ?of . "
					+ "?integration d2s:has_scope ?sc . "
					+ "d2s:" + id + " d2s:has_p2p_scc_pattern ?patternNode . "
					+ "?patternNode rdf:type ?pa . "
					+ "?patternNode d2s:has_temporality ?te . "
					+ "} WHERE { "; // construct where clause
		
		if (removeCompletely) {
			query += "d2s:" + id + " ?a ?b. " // find all triples with the platform as subject
					+ "?c ?d d2s:" + id + ". "; // or object
		}
		
		query += getPlatformQuery(id) // append 'all details' query for the current platform
					+ "}";
		
		UpdateExecutionFactory.createRemote(UpdateFactory.create(query), UPDATEENDPOINT).execute(); // execute update to endpoint
	}

	/**
	 * @param name URI of the platform
	 * @return Query that returns all standard information for the platform with the given URI
	 */
	private String getPlatformQuery(String name) {
		return "d2s:"
				+ name
				+ " rdf:type d2s:P2P_SCC_Platform ."
				+ "  d2s:"
				+ name
				+ " rdfs:label ?label ."
				+ "  d2s:"
				+ name
				+ " dbpp:url ?url ."
				+ " OPTIONAL { d2s:"
				+ name
				+ " rdfs:comment ?description. }."
				+ " OPTIONAL {  d2s:"
				+ name
				+ " d2s:has_resource_type ?rt ."
				+ "  ?rt rdfs:label ?resourceType } ."
				+ " OPTIONAL {  d2s:"
				+ name
				+ " d2s:has_consumer_involvement ?ci ."
				+ "  ?ci rdfs:label ?consumerInvolvement } ."
				+ " OPTIONAL {  d2s:"
				+ name
				+ " d2s:launched_in ?launch ."
				+ " OPTIONAL {  ?launch dbpp:locationCity ?launchCity."
				+ "				?launchCity rdfs:label ?launchCityName."
				+ "				{?launchCity owl:sameAs ?launchCityGeonames."
				+ "				 FILTER(STRSTARTS(STR(?launchCityGeonames), 'http://www.geonames.org/'))} } ."
				+ " OPTIONAL {  ?launch dbpp:locationCountry ?launchCountry."
				+ "				?launchCountry rdfs:label ?launchCountryName."
				+ "				?launchCountry dbpp:countryCode ?launchCountryCode."
				+ "				{?launchCountry owl:sameAs ?launchCountryGeonames."
				+ "				 FILTER(STRSTARTS(STR(?launchCountryGeonames), 'http://www.geonames.org/'))} } }."
				+ " OPTIONAL {  d2s:"
				+ name
				+ " dbpp:launchYear ?yearLaunch } ."
				+ " OPTIONAL {  d2s:"
				+ name
				+ " d2s:operator_resides_in ?residence ."
				+ " OPTIONAL {  ?residence dbpp:locationCity ?residenceCity."
				+ "				?residenceCity rdfs:label ?residenceCityName."
				+ "				{?residenceCity owl:sameAs ?residenceCityGeonames."
				+ "				 FILTER(STRSTARTS(STR(?residenceCityGeonames), 'http://www.geonames.org/'))} } ."
				+ " OPTIONAL {  ?residence dbpp:locationCountry ?residenceCountry."
				+ "				?residenceCountry rdfs:label ?residenceCountryName."
				+ "				?residenceCountry dbpp:countryCode ?residenceCountryCode."
				+ "				{?residenceCountry owl:sameAs ?residenceCountryGeonames."
				+ "				 FILTER(STRSTARTS(STR(?residenceCountryGeonames), 'http://www.geonames.org/'))} } } ."
				+ " OPTIONAL {  d2s:"
				+ name
				+ " d2s:has_market_mediation ?me ."
				+ "  ?me rdfs:label ?marketMediation } ."
				+ " OPTIONAL {  d2s:"
				+ name
				+ " d2s:has_market_integration ?integration  ."
				+ " OPTIONAL {  ?integration d2s:markets_are ?of ."
				+ "  ?of rdfs:label ?offering } ."
				+ " OPTIONAL {  ?integration d2s:has_scope ?sc ."
				+ "  ?sc rdfs:label ?geographicScope } }."
				+ " OPTIONAL {  d2s:"
				+ name
				+ " d2s:has_money_flow ?mf ."
				+ "  ?mf rdfs:label ?moneyFlow } ."
				+ " OPTIONAL {  d2s:"
				+ name
				+ " d2s:has_p2p_scc_pattern ?patternNode ."
				+ "  ?patternNode rdf:type ?pa ."
				+ "  ?pa rdfs:label ?pattern  ."
				+ " OPTIONAL {  ?patternNode d2s:has_temporality ?te ."
				+ "  ?te rdfs:label ?temporality } }."
				+ " OPTIONAL {  d2s:"
				+ name
				+ " d2s:promotes ?co ."
				+ "  ?co rdfs:label ?consumerism } ."
				+ " OPTIONAL {  d2s:"
				+ name
				+ " d2s:has_resource_owner ?ro ."
				+ "  ?ro rdfs:label ?resourceOwner } ."
				+ " OPTIONAL {  d2s:"
				+ name
				+ " d2s:min_service_duration ?serviceDurationMin } ."
				+ " OPTIONAL {  d2s:"
				+ name
				+ " d2s:max_service_duration ?serviceDurationMax } ."
				+ " OPTIONAL {  d2s:"
				+ name
				+ " d2s:has_app ?ap ."
				+ "  ?ap rdfs:label ?app } ."
				+ " OPTIONAL { d2s:"
				+ name
				+ " d2s:has_trust_contribution ?tc ."
				+ "  ?tc rdfs:label ?trustContribution } ."
				+ " OPTIONAL {  d2s:"
				+ name
				+ " d2s:accessed_object_has_type ?ot ." 
				+ "  ?ot rdfs:label ?typeOfAccessedObject }."
				+ " OPTIONAL { d2s:"
				+ name 
				+ " dbpp:language ?language.}."
				+ " OPTIONAL { d2s:"
				+ name 
				+ " d2s:used_in ?userDistribution."
				+ " ?userDistribution dbpp:locationCountry ?userCountry."
				+ " ?userDistribution d2s:user_percentage ?userPercentage."
				+ " ?userDistribution dct:date ?distributionDate. }.";
	}

	@Override
	public Platform getSuggestionExternal(String id) { 
		// find and return the platform with the given external ID
		return em.createQuery("SELECT p FROM Platform p WHERE p.externalId = '" + id + "'", Platform.class).getSingleResult();
	}

	@Override
	public boolean editSuggestionExternal(String id, Platform platform) {
		// check if the ID parameter actually fits the platform object's externalID and check if a suggestion with that ID can be found in the database
		if (id.equals(platform.getExternalId()) && getSuggestionExternal(id) != null) {
			em.merge(platform); // update suggestion in the database
			return true; // success
		}
		return false; // no success
	}
}
