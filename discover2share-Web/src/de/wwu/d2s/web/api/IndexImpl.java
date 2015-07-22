package de.wwu.d2s.web.api;

import javax.ejb.Stateless;

//Stateless required for EJB injection in the REST service
@Stateless
public class IndexImpl implements IndexApi {
	
	@Override
	public String vip() {
		return "du bist ein admin!";
	}

}
