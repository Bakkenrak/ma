package de.wwu.d2s.ejb;

import java.util.List;

import javax.ejb.Remote;

import de.wwu.d2s.jpa.Person;

@Remote
public interface TestService {
	
	List<String> getSPARQLStuff();

	List<Person> getTests();
}
