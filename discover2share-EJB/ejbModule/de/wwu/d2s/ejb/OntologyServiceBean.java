package de.wwu.d2s.ejb;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
				+ "ORDER BY ?resourceName";

		Query query = QueryFactory.create(sparqlQuery);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(ENDPOINT, query);
		ResultSet results = qexec.execSelect();

		List<Platform> platforms = new ArrayList<Platform>();
		Platform currentPlatform = new Platform();
		while (results.hasNext()) {
			QuerySolution result = results.next();
			
			if(result.get("resourceName").asResource().getURI()!=currentPlatform.getResourceName()){
				currentPlatform = new Platform();
				platforms.add(currentPlatform);
			}
			
			for (String var : results.getResultVars()) {
				RDFNode node = result.get(var);
				if (node == null)
					continue;

				if (node.isLiteral()) {
					String literal = node.asLiteral().getString();
					currentPlatform.set(var, literal);
				} else if (node.isResource()) {
					String uri = node.asResource().getURI();
					if (uri != null)
						currentPlatform.set(var, uri);
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
				+ "Select * " + "WHERE { "
				+ getPlatformQuery(name)
				+ "}";

		Query query = QueryFactory.create(sparqlQuery);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(ENDPOINT, query);
		ResultSet results = qexec.execSelect();

		Platform platform = null;
		if (results.hasNext())
			platform = new Platform();
		
		while (results.hasNext()) {
			QuerySolution result = results.next();
			for (String var : results.getResultVars()) {
				RDFNode node = result.get(var);
				if (node == null)
					continue;

				if (node.isLiteral()) {
					String literal = node.asLiteral().getString();
					platform.set(var, literal);
				} else if (node.isResource()) {
					String uri = node.asResource().getURI();
					if (uri != null)
						platform.set(var, uri);
				}
			}
		}

		qexec.close();
		return platform;
	}

	@Override
	public Map<String, Map<String, String>> getDescriptions() {
		// create a model using reasoner
		OntModel model1 = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF);

		// read the ontology file
		model1.read(ONTOLOGYURL, "Turtle");

		// Create a new query
		String queryString = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
				+ "PREFIX d2s: <http://www.discover2share.net/d2s-ont/> " 
				+ "SELECT * " 
				+ "WHERE { "
				+ "  ?c rdfs:subClassOf d2s:P2P_SCC_Dimension ."
				+ "  ?c rdfs:label ?label."
				+ "  ?c rdfs:comment ?comment." 
				+ "}";
		Query query = QueryFactory.create(queryString);
		QueryExecution qe = QueryExecutionFactory.create(query, model1);
		ResultSet results = qe.execSelect();
		
		Map<String, Map<String, String>> output = new HashMap<String, Map<String, String>>();
		while(results.hasNext()){
			Map<String, String> row = new HashMap<String, String>();
			
			QuerySolution result = results.nextSolution();
			String c = result.getResource("c").getURI().replace("http://www.discover2share.net/d2s-ont/", "");
			row.put("label", result.getLiteral("label").getString());
			row.put("comment", result.getLiteral("comment").getString());
			
			output.put(c, row);
		}

		qe.close();

		return output;
	}
	
	@Override
	public void addSuggestion(Platform platform){
		em.persist(platform);
	}
	
	@Override
	public void directSaveSuggestion(Platform platform) {
		OntologyWriter o = new OntologyWriter();
		OntModel model;
		if (platform.getEditFor() != null && !platform.getEditFor().isEmpty()) {
			removePlatform(platform.getEditFor());
			model = o.constructPlatform(platform, platform.getEditFor());
		} else {
			model = o.constructPlatform(platform);
		}
		
		OutputStream baos = new ByteArrayOutputStream();
		model.write(baos, "N-TRIPLE"); // transform data in ontology model into triples
		String query = "INSERT DATA { " + baos.toString() + "}"; // build insert query
		UpdateExecutionFactory.createRemote(UpdateFactory.create(query), UPDATEENDPOINT).execute(); // execute update to endpoint
	}

	@Override
	public List<Platform> getAllSuggestions() {
		return em.createQuery("from Platform", Platform.class).getResultList();
	}

	@Override
	public Platform getSuggestion(int id) {
		return em.find(Platform.class, id);
	}

	@Override
	public Map<String, String> doQuery(String query) {
		if(query.substring(0, 6).equals("query="))
			query = query.substring(6);
		try {
			query = URLDecoder.decode(query, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		Map<String, String> output = new HashMap<String, String>();
		try {
			Query sparqlQuery = QueryFactory.create(query);
			QueryExecution qexec = QueryExecutionFactory.sparqlService(ENDPOINT, sparqlQuery);
			ResultSet results = qexec.execSelect();
			ByteArrayOutputStream b = new ByteArrayOutputStream();
			ResultSetFormatter.outputAsJSON(b, results);
			String json = b.toString();
			
			qexec.close();
			
			output.put("success", json);
		} catch(Exception e) {
			output.put("error", e.getMessage());
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
		QueryExecution qexec = QueryExecutionFactory.sparqlService(ENDPOINT, query);
		ResultSet results = qexec.execSelect();

		List<Map<String, String>> output = new ArrayList<Map<String, String>>();
		while (results.hasNext()) {
			Map<String, String> current = new HashMap<String, String>();
			QuerySolution result = results.next();
			for (String var : results.getResultVars()) {
				RDFNode node = result.get(var);
				if (node == null)
					continue;

				if (node.isLiteral()) {
					String literal = node.asLiteral().getString();
					current.put(var, literal);
				} else if (node.isResource()) {
					String uri = node.asResource().getURI();
					if (uri != null)
						current.put(var, uri.substring(38));
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
		while (results.hasNext()) {
			Map<String, String> current = new HashMap<String, String>();
			QuerySolution result = results.next();
			for (String var : results.getResultVars()) {
				RDFNode node = result.get(var);
				if (node == null)
					continue;

				if (node.isLiteral()) {
					String literal = node.asLiteral().getString();
					current.put(var, literal);
				} else if (node.isResource()) {
					String uri = node.asResource().getURI();
					if (uri != null)
						current.put(var, uri.substring(38));
				}
			}
			output.add(current);
		}

		qexec.close();
		return output;
	}

	@Override
	public void deleteSuggestion(int id) {
		Platform p = em.find(Platform.class, id);
		if (p != null) 
			em.remove(p);
	}

	@Override
	public void saveSuggestion(int id) {
		Platform p = em.find(Platform.class, id);
		if(p != null) {
			directSaveSuggestion(p);
		}
		//em.remove(p); TODO uncomment
	}

	@Override
	public void editSuggestion(Platform platform) {
		em.merge(platform);
	}
	
	@Override
	public void removePlatform(String id) { //TODO remove everything
		String query = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
						+ "PREFIX d2s: <http://www.discover2share.net/d2s-ont/> "
						+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
						+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
						+ "PREFIX dbpp: <http://dbpedia.org/property/> "
						+ "PREFIX dbpo: <http://dbpedia.org/ontology/> "
						+ "PREFIX owl: <http://www.w3.org/2002/07/owl#> "
						+ "DELETE { "
						+ "d2s:" + id + " rdf:type d2s:P2P_SCC_Platform . "
						+ "d2s:" + id + " rdfs:label ?label . "
						+ "d2s:" + id + " dbpp:url ?url . "
						+ "d2s:" + id + " rdfs:comment ?description . "
						+ "d2s:" + id + " d2s:has_resource_type ?rt . "
						+ "d2s:" + id + " d2s:has_consumer_involvement ?ci . "
						+ "d2s:" + id + " d2s:launched_in ?launch . "
						+ "?launch dbpp:locationCity ?launchCity . "
						+ "?launch dbpp:locationCountry ?launchCountry . "
						+ "d2s:" + id + " dbpp:launchYear ?yearLaunch . "
						+ "d2s:" + id + " d2s:operator_resides_in ?residence . "
						+ "?residence dbpp:locationCity ?residenceCity . "
						+ "?residence dbpp:locationCountry ?residenceCountry . "
						+ "d2s:" + id + " d2s:has_market_mediation ?me . "
						+ "d2s:" + id + " d2s:has_market_integration ?integration . "
						+ "?integration rdf:type d2s:Market_Integration . "
						+ "?integration d2s:markets_are ?of . "
						+ "?integration d2s:has_scope ?sc . "
						+ "d2s:" + id + " d2s:has_money_flow ?mf . "
						+ "d2s:" + id + " d2s:has_p2p_scc_pattern ?patternNode . "
						+ "?patternNode rdf:type ?pa . "
						+ "?patternNode d2s:has_temporality ?te . "
						+ "d2s:" + id + " d2s:promotes ?co . "
						+ "d2s:" + id + " d2s:has_resource_owner ?ro . "
						+ "d2s:" + id + " d2s:min_service_duration ?serviceDurationMin . "
						+ "d2s:" + id + " d2s:max_service_duration ?serviceDurationMax . "
						+ "d2s:" + id + " d2s:has_app ?ap . "
						+ "d2s:" + id + " d2s:has_trust_contribution ?tc . "
						+ "d2s:" + id + " d2s:accessed_object_has_type ?ot . "
						+ "d2s:" + id + " dbpp:language ?lang . "
						+ "} WHERE { "
						+ getPlatformQuery(id)
						+ "}";
		
		UpdateExecutionFactory.createRemote(UpdateFactory.create(query), UPDATEENDPOINT).execute(); // execute update to endpoint
	}

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
				+ " dbpp:language ?lang."
				+ " ?lang rdfs:label ?language }.";
	}
}
