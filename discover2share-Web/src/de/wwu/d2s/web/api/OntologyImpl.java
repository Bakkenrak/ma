package de.wwu.d2s.web.api;

import java.util.List;
import java.util.Map;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import com.hp.hpl.jena.query.ResultSet;

import de.wwu.d2s.ejb.OntologyService;

@Stateless
public class OntologyImpl implements OntologyApi {

	@EJB
	private OntologyService ontologyService;
	
	@Override
	public List<Map<String, String>> getAllPlatforms() {
		return ontologyService.getAllPlatforms();
	}

	@Override
	public Map<String, String> getPlatform(String url) {
		return ontologyService.getPlatform(url);
	}

}
