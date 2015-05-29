package de.wwu.d2s.web.api;

import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import de.wwu.d2s.ejb.TestService;
import de.wwu.d2s.jpa.Person;

//Stateless required for EJB injection in the REST service
@Stateless
public class IndexImpl implements IndexApi {
	@EJB
	private TestService ejb;
	
	@Override
	public List<Person> getTests() {
		return ejb.getTests();
	}

}
