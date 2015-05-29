package de.wwu.d2s.web.beans;

import java.util.List;

import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;

import de.wwu.d2s.ejb.TestService;
import de.wwu.d2s.jpa.Person;

@ManagedBean
public class Index {
	
	@EJB
	private TestService ejb;
	
	private List<Person> persons;
	
	private List<String> sparqlResults;
	
	public Index() {
	}
	
	public List<String> getSparqlResults(){
		if(sparqlResults==null) sparqlResults = ejb.getSPARQLStuff();
		return sparqlResults;
	}
	
	public List<Person> getPersons(){
		if(persons == null) persons = ejb.getTests();
		
		return persons;
	}

}
