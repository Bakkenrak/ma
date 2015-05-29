package de.wwu.d2s.ejb;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import de.wwu.d2s.jpa.Person;

@Stateless
public class TestServiceBean implements TestService {
	@PersistenceContext
	private EntityManager em;
	
	@Override
	public List<String> getSPARQLStuff(){
		List<String> gnarl = new ArrayList<String>();
		
		String SOURCE = "http://localhost:3030/Persistify";
		String NS = SOURCE + "";
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
		String queryString = "SELECT ?subject ?predicate ?object"
				+ "WHERE {"
				+ "  ?subject ?predicate ?object"
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
		
		return gnarl;
		
	}

	@Override
	public List<Person> getTests() {
		List<Person> l = new ArrayList<Person>();
		for (int i = 1; i < 5; i++) {
			Person person = new Person();
			person.setName("Hans " + i);
			person.setAge(15 + i);
			em.persist(person);
			l.add(person);
		}
		return em.createQuery("FROM Person", Person.class).getResultList();
	}
}
