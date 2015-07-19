package de.wwu.d2s.ejb;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.RDFNode;

@Stateless
public class OntologyServiceBean implements OntologyService {

	@Override
	public List<Map<String, String>> getAllPlatforms() {
		
		String sparqlEndpoint = "http://localhost:3030/d2s-ont/query";

		String sparqlQuery = "PREFIX d2s: <http://www.discover2share.net/d2s-ont/> "
				+ "PREFIX dbpp: <http://dbpedia.org/property/> "
				+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
				+ "Select * {"
				+ "	?name rdf:type d2s:P2P_SCC_Platform ."
				+ " ?name rdfs:label ?label ."
				+ " ?name dbpp:url ?url ."
				+ " ?name d2s:has_resource_type ?rt ."
				+ " ?rt rdfs:label ?resourceType ."
				+ "}";

		Query query = QueryFactory.create(sparqlQuery);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(
				sparqlEndpoint, query);
		ResultSet results = qexec.execSelect(); 
		
		
		List<Map<String, String>> output = new ArrayList<Map<String, String>>();
		while(results.hasNext()) {
			Map<String, String> row = new HashMap<String, String>();
			
			QuerySolution result = results.next();
			for(String var : results.getResultVars()){
				RDFNode node = result.get(var);
				if(node.isLiteral())
					row.put(var, node.asLiteral().getString());
				else if(node.isResource())
					row.put(var, node.asResource().getURI());
				
			}
			output.add(row);
		}
		
		
		qexec.close();
		
		return output;
	}

	@Override
	public Map<String, String> getPlatform(String url) {
		String sparqlEndpoint = "http://localhost:3030/d2s-ont/query";

		String sparqlQuery = "PREFIX d2s: <http://www.discover2share.net/d2s-ont/> "
				+ "PREFIX dbpp: <http://dbpedia.org/property/> "
				+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
				+ "Select * {"
				+ " d2s:" + url + " rdfs:label ?label ."
				+ " d2s:" +  url + " dbpp:url ?url ."
				+ " d2s:" +  url + " d2s:has_resource_type ?rt ."
				+ " ?rt rdfs:label ?resourceType ."
				+ "}";

		Query query = QueryFactory.create(sparqlQuery);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(
				sparqlEndpoint, query);
		ResultSet results = qexec.execSelect(); 
		
		Map<String, String> row = new HashMap<String, String>();
		if(results.hasNext()) {
			QuerySolution result = results.next();
			for(String var : results.getResultVars()){
				RDFNode node = result.get(var);
				if(node.isLiteral())
					row.put(var, node.asLiteral().getString());
				else if(node.isResource())
					row.put(var, node.asResource().getURI());
			}
		}
		qexec.close();
		return row;
	}

}
