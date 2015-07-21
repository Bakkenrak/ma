package de.wwu.d2s.ejb;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;

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

@Stateless
public class OntologyServiceBean implements OntologyService {

	@Override
	public List<Map<String, String>> getAllPlatforms() {

		String sparqlEndpoint = "http://localhost:3030/d2s-ont/query";

		String sparqlQuery = "PREFIX d2s: <http://www.discover2share.net/d2s-ont/> "
				+ "PREFIX dbpp: <http://dbpedia.org/property/> "
				+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" + "Select * {"
				+ "	?name rdf:type d2s:P2P_SCC_Platform ." + " ?name rdfs:label ?label ." + " ?name dbpp:url ?url ."
				+ " ?name d2s:has_resource_type ?rt ." + " ?rt rdfs:label ?resourceType ." + "}";

		Query query = QueryFactory.create(sparqlQuery);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(sparqlEndpoint, query);
		ResultSet results = qexec.execSelect();

		List<Map<String, String>> output = new ArrayList<Map<String, String>>();
		while (results.hasNext()) {
			Map<String, String> row = new HashMap<String, String>();

			QuerySolution result = results.next();
			for (String var : results.getResultVars()) {
				RDFNode node = result.get(var);
				if (node.isLiteral())
					row.put(var, node.asLiteral().getString());
				else if (node.isResource())
					row.put(var, node.asResource().getURI());

			}
			output.add(row);
		}

		qexec.close();

		return output;
	}

	@Override
	public Map<String, List<String>> getPlatform(String name) {
		String sparqlEndpoint = "http://localhost:3030/d2s-ont/query";

		String sparqlQuery = "PREFIX d2s: <http://www.discover2share.net/d2s-ont/> "
				+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
				+ "PREFIX dbpp: <http://dbpedia.org/property/> " + "PREFIX dbpo: <http://dbpedia.org/ontology/> "
				+ "Select * " + "WHERE { " + "  d2s:"
				+ name
				+ " rdf:type d2s:P2P_SCC_Platform ."
				+ "  d2s:"
				+ name
				+ " rdfs:label ?label ."
				+ "  d2s:"
				+ name
				+ " dbpp:url ?url ."
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
				+ " d2s:launched_in ?launch } ."
				+ " OPTIONAL {  ?launch dbpp:locationCity ?launchCity } ."
				+ " OPTIONAL {  ?launch dbpp:locationCountry ?launchCountry } ."
				+ " OPTIONAL {  d2s:"
				+ name
				+ " dbpp:launchYear ?launchYear } ."
				+ " OPTIONAL {  d2s:"
				+ name
				+ " dbpo:location ?residence } ."
				+ " OPTIONAL {  ?residence dbpp:locationCity ?residenceCity } ."
				+ " OPTIONAL {  ?residence dbpp:locationCountry ?residenceCountry } ."
				+ " OPTIONAL {  d2s:"
				+ name
				+ " d2s:has_market_mediation ?me ."
				+ "  ?me rdfs:label ?mediation } ."
				+ " OPTIONAL {  d2s:"
				+ name
				+ " d2s:has_market_integration ?integration } ."
				+ " OPTIONAL {  ?integration d2s:markets_are ?of ."
				+ "  ?of rdfs:label ?offering } ."
				+ " OPTIONAL {  ?integration d2s:has_scope ?sc ."
				+ "  ?sc rdfs:label ?scope } ."
				+ " OPTIONAL {  d2s:"
				+ name
				+ " d2s:has_money_flow ?mf ."
				+ "  ?mf rdfs:label ?moneyFlow } ."
				+ " OPTIONAL {  d2s:"
				+ name
				+ " d2s:has_p2p_scc_pattern ?patternNode ."
				+ "  ?patternNode rdf:type ?pa ."
				+ "  ?pa rdfs:label ?pattern } ."
				+ " OPTIONAL {  ?patternNode d2s:has_temporality ?te ."
				+ "  ?te rdfs:label ?temporality } ."
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
				+ " d2s:min_service_duration ?minDuration } ."
				+ " OPTIONAL {  d2s:"
				+ name
				+ " d2s:max_service_duration ?maxDuration } ."
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
				+ " d2s:accessed_object_has_type ?ot ." + "  ?ot rdfs:label ?objectType } ." + "}";

		Query query = QueryFactory.create(sparqlQuery);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(sparqlEndpoint, query);
		ResultSet results = qexec.execSelect();

		Map<String, List<String>> platform = new HashMap<String, List<String>>();
		while (results.hasNext()) {
			QuerySolution result = results.next();
			for (String var : results.getResultVars()) {
				RDFNode node = result.get(var);
				if (node == null)
					continue;

				if (node.isLiteral()) {
					String literal = node.asLiteral().getString();
					if (platform.containsKey(var) && !platform.get(var).contains(literal))
						platform.get(var).add(literal);
					else {
						List<String> list = new ArrayList<String>();
						list.add(literal);
						platform.put(var, list);
					}
				} else if (node.isResource()) {
					String uri = node.asResource().getURI();
					if (uri != null && platform.containsKey(var) && !platform.get(var).contains(uri))
						platform.get(var).add(uri);
					else {
						List<String> list = new ArrayList<String>();
						list.add(uri);
						platform.put(var, list);
					}
				}
			}
		}

		qexec.close();
		return platform;
	}

	@Override
	public Map<String, Map<String, String>> getDescriptions() {
		String ontologyUrl = "http://localhost:3030/d2s-ont/";
		// create a model using reasoner
		OntModel model1 = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF);

		// read the ontology file
		model1.read(ontologyUrl, "Turtle");

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

}
