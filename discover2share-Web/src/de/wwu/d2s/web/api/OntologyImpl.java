package de.wwu.d2s.web.api;

import java.util.List;
import java.util.Map;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import de.wwu.d2s.ejb.OntologyService;
import de.wwu.d2s.jpa.Platform;

@Stateless
public class OntologyImpl implements OntologyApi {

	@EJB
	private OntologyService ontologyService;
	
	@Override
	public List<Map<String, String>> getAllPlatforms() {
		return ontologyService.getAllPlatforms();
	}

	@Override
	public Map<String, List<String>> getPlatform(String url) {
		return ontologyService.getPlatform(url);
	}

	@Override
	public Map<String, Map<String, String>> getDescriptions() {
		return ontologyService.getDescriptions();
	}

	@Override
	public void createPlatform(Platform platform) {
		ontologyService.createPlatform(platform);
	}

}
