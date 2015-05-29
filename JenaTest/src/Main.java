import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class Main {

	public static void main(String args[]) {
		String SOURCE = "http://www.opentox.org/api/1.1";
		String NS = SOURCE + "#";
		// create a model using reasoner
		OntModel model1 = ModelFactory
				.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF);
		// create a model which doesn't use a reasoner
		OntModel model2 = ModelFactory
				.createOntologyModel(OntModelSpec.OWL_MEM);

		// read the RDF/XML file
		model1.read(SOURCE, "RDF/XML");
		model2.read(SOURCE, "RDF/XML");
		// prints out the RDF/XML structure
		//qe.close();
		System.out.println(" ");

		// Create a new query
		String queryString = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
				+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  "
				+ "select ?uri "
				+ "where { "
				+ "?uri rdfs:subClassOf <http://www.opentox.org/api/1.1#Feature>  "
				+ "} \n ";
		Query query = QueryFactory.create(queryString);

		System.out.println("----------------------");

		System.out.println("Query Result Sheet");

		System.out.println("----------------------");

		System.out.println("Direct&Indirect Descendants (model1)");

		System.out.println("-------------------");

		// Execute the query and obtain results
		QueryExecution qe = QueryExecutionFactory.create(query, model1);
		com.hp.hpl.jena.query.ResultSet results = qe.execSelect();

		// Output query results
		ResultSetFormatter.out(System.out, results, query);

		qe.close();

		System.out.println("----------------------");
		System.out.println("Only Direct Descendants");
		System.out.println("----------------------");

		// Execute the query and obtain results
		qe = QueryExecutionFactory.create(query, model2);
		results = qe.execSelect();

		// Output query results
		ResultSetFormatter.out(System.out, results, query);
		qe.close();
	}

}
